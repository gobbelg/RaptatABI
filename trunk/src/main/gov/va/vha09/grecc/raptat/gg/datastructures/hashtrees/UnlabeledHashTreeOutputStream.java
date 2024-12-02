package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * *********************************************** A class used for writing out a single
 * UnlabeledHashTree or a list of UnlabeledHashTree instances. UnlabeledHashTrees, single or as a
 * list, can be appended to the end of an existing file with calls to the append() methods.
 *
 * @author Glenn Gobbel - February 7, 2012 ***********************************************
 */
public class UnlabeledHashTreeOutputStream implements HashTreeOutStreamer<UnlabeledHashTree> {
  private class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream out) throws IOException {
      super(out);
    }


    @Override
    protected void writeStreamHeader() throws IOException {
      reset();
    }
  }

  private File outStreamFile = null;


  public UnlabeledHashTreeOutputStream() {
    this.outStreamFile = new File(requestFileName());
    try {
      this.outStreamFile.createNewFile();
    } catch (IOException e) {
      System.out.println(
          "Unable to create UnlabeledHashTreeOutstream File: " + this.outStreamFile.getName() + e);
      e.printStackTrace();
    }
  }


  /**
   * ************************************************** Creates a new UnlabeledHashTreeOutputStream.
   * UnlabeledHashTrees will be save into the file specified in the path given by fileName.
   *
   * @param fileName **************************************************
   */
  public UnlabeledHashTreeOutputStream(String fileName) {
    this.outStreamFile = new File(fileName);
    try {
      this.outStreamFile.createNewFile();
    } catch (IOException e) {
      System.out.println("Unable to create UnlabeledHashTreeOutstream File: " + fileName + e);
      e.printStackTrace();
    }
  }


  @Override
  public boolean append(HashTree<UnlabeledHashTree> theTree) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamFile == null) {
      this.outStreamFile = new File(requestFileNameForAppending());
    }

    if (this.outStreamFile != null) {
      try {
        if (this.outStreamFile.exists() && this.outStreamFile.length() > 0) {
          outStream = new AppendableObjectOutputStream(
              new BufferedOutputStream(new FileOutputStream(this.outStreamFile, true),
                  RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        } else {
          outStream = new ObjectOutputStream(
              new BufferedOutputStream(new FileOutputStream(this.outStreamFile),
                  RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        }

        if (outStream != null) {
          outStream.writeObject(theTree);
        }
      } catch (IOException e) {
        System.out.println("Unable to open file stream to write UnlabledHashTree");
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


  public boolean append(List<UnlabeledHashTree> theTrees) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamFile == null) {
      this.outStreamFile = new File(requestFileName());
    }

    if (this.outStreamFile != null) {
      try {
        if (this.outStreamFile.exists() && this.outStreamFile.length() > 0) {
          outStream = new AppendableObjectOutputStream(
              new BufferedOutputStream(new FileOutputStream(this.outStreamFile, true),
                  RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        } else {
          outStream = new ObjectOutputStream(
              new BufferedOutputStream(new FileOutputStream(this.outStreamFile),
                  RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        }

        if (outStream != null) {
          Iterator<UnlabeledHashTree> theList = theTrees.iterator();
          while (theList.hasNext()) {
            outStream.writeObject(theList.next());
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


  @Override
  public void clear() {
    // Do nothing to conform to interface
  }


  public File getFile() {
    return this.outStreamFile;
  }


  public void setFile(String fileName) {
    this.outStreamFile = new File(fileName);
    try {
      this.outStreamFile.createNewFile();
    } catch (IOException e) {
      this.outStreamFile = null;
      System.out.println("Unable to create new file for UnlabeledHashTreeInputStream:" + fileName
          + System.getProperty("line.separator") + e);
      e.printStackTrace();
    }
  }


  public boolean write(List<UnlabeledHashTree> theTrees) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamFile == null) {
      this.outStreamFile = new File(requestFileName());
    }

    if (this.outStreamFile != null) {
      try {
        outStream = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(this.outStreamFile),
                RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        if (outStream != null) {
          Iterator<UnlabeledHashTree> theList = theTrees.iterator();
          while (theList.hasNext()) {
            outStream.writeObject(theList.next());
          }
        }
      } catch (IOException e) {
        System.out.println("Unable to open file stream to write " + "UnlabledHashTrees to: "
            + this.outStreamFile.getName());
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


  public boolean write(UnlabeledHashTree theTree) {
    ObjectOutputStream outStream = null;
    boolean noWriteErrors = true;

    if (this.outStreamFile == null) {
      this.outStreamFile = new File(requestFileName());
    }

    if (this.outStreamFile != null) {
      try {
        outStream = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(this.outStreamFile),
                RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
        if (outStream != null) {
          outStream.writeObject(theTree);
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


  /**
   * *********************************************************** OVERRIDES PARENT METHOD This does
   * not do anything but is only included to conform to the HashTreeOutStreamer interface. This
   * class keeps its hash trees on disk, so writing them to disk is not necessary.
   *
   * @author Glenn Gobbel - Jul 9, 2012 ***********************************************************
   */
  @Override
  public void writeTreesToDisk() {
    throw new NotImplementedException("Not implemented - included only to conform to interface");
  }


  private String requestFileName() {
    String resultName = null;
    try {
      resultName =
          GeneralHelper.getDirectory("Select the " + "directory for storing the UnlabeledHashTrees")
              .getCanonicalPath();
      resultName += File.separator + "UHT_" + System.currentTimeMillis() + ".rpt";
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return resultName;
  }


  private String requestFileNameForAppending() {
    String resultName = null;

    try {
      String[] fileType = {".rpt"};
      resultName = GeneralHelper
          .getFileByType("Select the " + "file for appending the UnlabeledHashTrees", fileType)
          .getCanonicalPath();
      resultName += File.separator + "UHT_" + System.currentTimeMillis() + ".rpt";
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return resultName;
  }
}
