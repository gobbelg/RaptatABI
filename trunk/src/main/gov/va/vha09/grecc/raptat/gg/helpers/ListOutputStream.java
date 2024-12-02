package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * *********************************************** A class used for writing out a single object or a
 * list of object instances. Objects, single or as a list, can be appended to the end of an existing
 * file with calls to the append() methods. Calls to write() will overwrite files of the same name
 * if they already exist.
 *
 * @author Glenn Gobbel - February 16, 2012 ***********************************************
 */
public class ListOutputStream<E extends Serializable> {
  /**
   * ***************************************************************** This class is needed to make
   * sure that no header gets written when appending the stream
   *
   * @author Glenn Gobbel - Feb 16, 2012
   *         ****************************************************************
   */
  private class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream out) throws IOException {
      super(out);
    }


    @Override
    protected void writeStreamHeader() throws IOException {
      reset();
    }
  }

  private String outStreamName = null;

  private String outStreamMarker = RaptatConstants.LIST_OUTPUTSTREAM_MARKER;


  public ListOutputStream() {
    this.outStreamName = this.requestFileName();
  }


  /**
   * ************************************************** Creates a new ObjectOutputStreamer. Objects
   * will be save into the file specified in the path given by fileName.
   *
   * @param fileName **************************************************
   */
  public ListOutputStream(String fileName) {
    this.outStreamName = fileName;
  }


  public boolean append(E theObject) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamName == null) {
      this.outStreamName = this.requestFileForAppending();
    }

    if (this.outStreamName != null) {
      try {
        File outFile = new File(this.outStreamName);
        if (outFile.exists()) {
          outStream = new AppendableObjectOutputStream(new BufferedOutputStream(
              new FileOutputStream(outFile, true), RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
          if (outStream != null) {
            outStream.writeObject(theObject);
          }
        } else {
          outStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile),
              RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
          if (outStream != null) {
            outStream.writeObject(this.outStreamMarker);
            outStream.writeObject(theObject);
          }
        }

      } catch (IOException e) {
        System.out.println("Unable to open file stream to write Object instance");
        e.printStackTrace();
        noWriteErrors = false;
      } finally {
        try {
          if (outStream != null) {
            outStream.flush();
            outStream.close();
          }
        } catch (Exception e) {
          System.out.println("Unable to close file stream for writing Object instance");
          e.printStackTrace();
          noWriteErrors = false;
        }
      }
    } else {
      noWriteErrors = false;
    }

    return noWriteErrors;
  }


  public boolean append(List<E> theObjects) {

    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamName == null) {
      this.outStreamName = this.requestFileForAppending();
    }

    if (this.outStreamName != null) {
      try {
        File outFile = new File(this.outStreamName);
        if (outFile.exists()) {
          outStream = new AppendableObjectOutputStream(new BufferedOutputStream(
              new FileOutputStream(outFile, true), RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
          if (outStream != null) {
            Iterator<E> theList = theObjects.iterator();
            while (theList.hasNext()) {
              outStream.writeObject(theList.next());
            }
          }
        } else {
          outStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile),
              RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
          if (outStream != null) {
            Iterator<E> theList = theObjects.iterator();
            if (theList.hasNext()) {
              outStream.writeObject(this.outStreamMarker);
            }
            while (theList.hasNext()) {
              outStream.writeObject(theList.next());
            }
          }
        }

      } catch (IOException e) {
        System.out.println("Unable to open file stream to write UnlabledHashTrees");
        e.printStackTrace();
        noWriteErrors = false;
      } finally {
        try {
          if (outStream != null) {
            outStream.flush();
            outStream.close();
          }
        } catch (Exception e) {
          System.out.println("Unable to close file stream for writing UnlabledHashTree");
          e.printStackTrace();
          noWriteErrors = false;
        }
      }
    } else {
      noWriteErrors = false;
    }

    return noWriteErrors;
  }


  public String getFileName() {
    return this.outStreamName;
  }


  public void setFileName(String fileName) {
    this.outStreamName = fileName;
  }


  public boolean write(E theObject) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamName == null) {
      this.outStreamName = this.requestFileName();
    }

    if (this.outStreamName != null) {
      try {
        outStream = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(this.outStreamName),
                RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        if (outStream != null) {
          outStream.writeObject(this.outStreamMarker);
          outStream.writeObject(theObject);
        }
      } catch (IOException e) {
        System.out.println("Unable to open file stream to write Object instances");
        e.printStackTrace();
        noWriteErrors = false;
      } finally {
        try {
          if (outStream != null) {
            outStream.flush();
            outStream.close();
          }
        } catch (Exception e) {
          System.out.println("Unable to close file stream for writing Object instances");
          e.printStackTrace();
          noWriteErrors = false;
        }
      }
    } else {
      noWriteErrors = false;
    }
    return noWriteErrors;
  }


  public boolean write(List<E> theObjects) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamName == null) {
      this.outStreamName = this.requestFileName();
    }

    if (this.outStreamName != null) {
      try {
        outStream = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(this.outStreamName)));
        if (outStream != null) {
          Iterator<E> theList = theObjects.iterator();
          if (theList.hasNext()) {
            // Only write marker if list has at least
            // one object in it
            outStream.writeObject(this.outStreamMarker);
          }
          while (theList.hasNext()) {
            outStream.writeObject(theList.next());
          }
        }
      } catch (IOException e) {
        System.out.println("Unable to open file stream to write object");
        e.printStackTrace();
        noWriteErrors = false;
      } finally {
        try {
          if (outStream != null) {
            outStream.flush();
            outStream.close();
          }
        } catch (Exception e) {
          System.out.println("Unable to close file stream for writing object");
          e.printStackTrace();
          noWriteErrors = false;
        }
      }
    } else {
      noWriteErrors = false;
    }

    return noWriteErrors;
  }


  private String requestFileForAppending() {
    String resultName = null;

    try {
      String[] fileType = {".rpt"};
      resultName = GeneralHelper
          .getFileByType("Select the " + "file for appending the Object Instances", fileType)
          .getCanonicalPath();
      resultName += File.separator + "RapTAT_OBJ_" + System.currentTimeMillis() + ".rpt";
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return resultName;
  }


  private String requestFileName() {
    String resultName = null;
    try {
      resultName =
          GeneralHelper.getDirectory("Select the " + "directory for storing the Object instances")
              .getCanonicalPath();
      resultName += File.separator + "RapTAT_OBJ_" + System.currentTimeMillis() + ".rpt";
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return resultName;
  }
}
