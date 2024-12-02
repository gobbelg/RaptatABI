package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * ****************************************************** Allow reading in a file containing a
 * series of the same object via iteration. The file must have been written using an
 * ListOutputStream object. The objects will be input in the same order that they were output using
 * a ListOutputStream.
 *
 * @author Glenn Gobbel - February 16, 2012 *****************************************************
 */
public class ListInputStream<E> implements Iterable<E> {
  public class ListInputStreamIterator<T> implements Iterator<T> {
    ObjectInputStream inStream = null;
    T curObject = null;


    @SuppressWarnings("unchecked")
    public ListInputStreamIterator() {
      try {
        this.inStream =
            new ObjectInputStream(new FileInputStream(ListInputStream.this.inStreamName));
        if (this.inStream != null) {
          // Discard first object as it is only a String marker to
          // indicate
          // that this file was written by an AppendableObjectStream
          this.inStream.readObject();
          this.curObject = (T) this.inStream.readObject();
        }
      } catch (EOFException e) {
        System.out.println("EOF Found - No objects in file");
        this.curObject = null;
      } catch (IOException e) {
        System.out.println("Unable to open file stream to read UnlabledHashTrees");
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        System.out.println("Unable to identify class type in UnlabeledHashTree stream");
        e.printStackTrace();
      }
    }


    @Override
    public boolean hasNext() {
      if (this.curObject == null) {
        this.finalize();
        return false;
      }
      return true;
    }


    @Override
    @SuppressWarnings("unchecked")
    public T next() {
      T resultObject = this.curObject;
      try {
        this.curObject = (T) this.inStream.readObject();
      } catch (EOFException e) {
        System.out.println("EOF Found");
        this.curObject = null;
      } catch (IOException e) {
        System.out.println("Unable to read Object instance from file");
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        System.out.println("Unable to identify class type in Object stream");
        e.printStackTrace();
      }

      ListInputStream.this.index++;
      return resultObject;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }


    /**
     * *************************************** Make sure the file is closed in case iterator is not
     * finished **************************************
     */
    @Override
    protected void finalize() {
      if (this.inStream != null) {
        try {
          this.inStream.close();
        } catch (IOException e) {
          System.out.println("Unable to close Object instance from stream");
          e.printStackTrace();
        }

        try {
          super.finalize();
        } catch (Throwable e) {
          System.out.println("Unable to close Object instance from stream");
          e.printStackTrace();
        }
      }
    }
  }

  private String inStreamName = null;

  private String outStreamMarker = RaptatConstants.LIST_OUTPUTSTREAM_MARKER;

  // Keep track of which UnlabeledHashTree to
  // read in next
  private int index = 0;


  /**
   * ************************************************** Creates a new ListInputStream with the
   * UnlabeledHashTrees populated from the file given as fileName.
   *
   * @param fileName
   * @throws NotListOutputStreamFile **************************************************
   */
  public ListInputStream(String fileName) throws NotListOutputStreamFile {
    this.inStreamName = fileName;
    if (!this.appendableOutputStreamFile()) {
      throw new NotListOutputStreamFile("ListInputStream instances can only"
          + "read from files created using an ListOutputStream");
    }
  }


  public String getFileName() {
    return this.inStreamName;
  }


  public int getIndex() {
    return this.index;
  }


  /**
   * ************************************************** Gets the tree at a specific index from the
   * start of the file using 0 index.
   *
   * @param treeIndex - which tree to get **************************************************
   */
  public E getObjectAtIndex(int treeIndex) {
    E resultObject;
    resultObject = null;

    ListInputStreamIterator<E> theIterator = new ListInputStreamIterator<>();

    for (int i = 0; i <= treeIndex; i++) {
      if (theIterator.hasNext()) {
        resultObject = theIterator.next();
      }
    }

    return resultObject;
  }


  @Override
  public ListInputStreamIterator<E> iterator() {
    if (this.inStreamName == null) {
      this.inStreamName = GeneralHelper.getFile("Select Object instance file").getName();
    }
    if (this.inStreamName != null) {
      return new ListInputStreamIterator<>();
    }
    return null;
  }


  public void setFileName(String fileName) throws NotListOutputStreamFile {
    this.inStreamName = fileName;
    if (!this.appendableOutputStreamFile()) {
      throw new NotListOutputStreamFile("ListInputStream instances can only"
          + "read from files created using an ListOutputStream");
    }
  }


  private boolean appendableOutputStreamFile() {
    ObjectInputStream inStream = null;
    String firstObject;
    try {
      inStream = new ObjectInputStream(new FileInputStream(this.inStreamName));
      if (inStream != null) {
        firstObject = (String) inStream.readObject();
        if (!firstObject.equals(this.outStreamMarker)) {
          return false;
        }
      }
    } catch (IOException e) {
      System.out.println("Unable to open file stream to read UnlabledHashTrees");
      e.printStackTrace();
      return false;
    } catch (ClassNotFoundException e) {
      System.out.println("Unable to identify class type in UnlabeledHashTree stream");
      e.printStackTrace();
      return false;
    } finally {
      if (inStream != null) {
        try {
          inStream.close();
        } catch (IOException e) {
          System.out.println(e);
          e.printStackTrace();
        }
      }
    }
    return true;
  }
}
