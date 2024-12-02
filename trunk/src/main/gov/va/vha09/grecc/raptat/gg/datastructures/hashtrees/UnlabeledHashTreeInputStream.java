package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ****************************************************** Maintains and reads from disk a list of
 * UnlabeledHashTrees.
 *
 * @author Glenn Gobbel - February 7, 2012 *****************************************************
 */
public class UnlabeledHashTreeInputStream implements HashTreeInStreamer<UnlabeledHashTree> {
  private class UnlabeledHashTreeInstreamIterator implements Iterator<UnlabeledHashTree> {
    ObjectInputStream inStream = null;
    UnlabeledHashTree curTree = null;


    public UnlabeledHashTreeInstreamIterator() {
      try {
        this.inStream = new ObjectInputStream(
            new FileInputStream(UnlabeledHashTreeInputStream.this.inStreamFile));
        if (this.inStream != null) {
          this.curTree = (UnlabeledHashTree) this.inStream.readObject();
        }
      } catch (EOFException e) {
        // End of file so no UnlabeledHashTrees
        // so curTree just remains null and
        // hasNext() will return false, as expected
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
      if (this.curTree == null) {
        finalize();
        return false;
      }
      return true;
    }


    @Override
    public UnlabeledHashTree next() {
      UnlabeledHashTree resultTree = this.curTree;
      try {
        this.curTree = (UnlabeledHashTree) this.inStream.readObject();
      } catch (EOFException e) {
        // System.out.println("EOF Found");
        this.curTree = null;
      } catch (IOException e) {
        System.out.println("Unable to read UnlabledHashTree from file");
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        System.out.println("Unable to identify class type in UnlabeledHashTree stream");
        e.printStackTrace();
      }

      UnlabeledHashTreeInputStream.this.index++;
      return resultTree;
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
          System.out.println("Unable to close UnlabeledHashTree stream");
          e.printStackTrace();
        }

        try {
          super.finalize();
        } catch (Throwable e) {
          System.out.println("Unable to close UnlabeledHashTree stream");
          e.printStackTrace();
        }
      }
    }
  }

  private File inStreamFile = null;

  // Keep track of which UnlabeledHashTree to
  // read in next
  private int index = 0;


  public UnlabeledHashTreeInputStream() {
    this.inStreamFile = new File(GeneralHelper.getFile("Select UnlabeledHashTree file").getName());
    try {
      this.inStreamFile.createNewFile();
    } catch (IOException e) {
      System.out.println("Unable to create new file for UnlabeledHashTreeInputStream:"
          + this.inStreamFile.getName() + System.getProperty("line.separator") + e);
      this.inStreamFile = null;
      e.printStackTrace();
    }
  }


  /**
   * ************************************************** Creates a new UnlabeledHashTreeInputStream
   * with the UnlabeledHashTrees populated from the file given as fileName.
   *
   * @param fileName **************************************************
   */
  public UnlabeledHashTreeInputStream(String fileName) {
    this.inStreamFile = new File(fileName);
    try {
      this.inStreamFile.createNewFile();
    } catch (IOException e) {
      this.inStreamFile = null;
      System.out.println("Unable to create new file for UnlabeledHashTreeInputStream:" + fileName
          + System.getProperty("line.separator") + e);
      e.printStackTrace();
    }
  }


  public void copyTo(UnlabeledHashTreeOutputStream outStream) {
    for (UnlabeledHashTree curTree : this) {
      outStream.append(curTree);
    }
  }


  public File getFile() {
    return this.inStreamFile;
  }


  public List<String> getFileNames() {
    List<String> fileList = new Vector<>();

    for (UnlabeledHashTree curTree : this) {
      fileList.add(curTree.getTextSourceName());
    }

    return fileList;
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
  public UnlabeledHashTree getTreeAtIndex(int treeIndex) {
    UnlabeledHashTree resultTree;
    resultTree = null;

    UnlabeledHashTreeInstreamIterator theIterator = iterator();
    for (int i = 0; i <= treeIndex; i++) {
      if (theIterator.hasNext()) {
        resultTree = theIterator.next();
      }
    }

    return resultTree;
  }


  @Override
  public UnlabeledHashTreeInstreamIterator iterator() {
    if (this.inStreamFile == null) {
      this.inStreamFile =
          new File(GeneralHelper.getFile("Select UnlabeledHashTree file").getName());
      try {
        this.inStreamFile.createNewFile();
      } catch (IOException e) {
        System.out.println("Unable to create new file for UnlabeledHashTreeInputStream:"
            + this.inStreamFile.getName() + System.getProperty("line.separator") + e);
        this.inStreamFile = null;
        e.printStackTrace();
      }
    }
    if (this.inStreamFile != null) {
      return new UnlabeledHashTreeInstreamIterator();
    }
    return null;
  }


  public void printTrees() {
    for (UnlabeledHashTree curTree : this) {
      System.out.println("\n===============================================");
      curTree.print();
    }
  }


  public void setFile(String fileName) {
    this.inStreamFile = new File(fileName);
    try {
      this.inStreamFile.createNewFile();
    } catch (IOException e) {
      this.inStreamFile = null;
      System.out.println("Unable to create new file for UnlabeledHashTreeInputStream:" + fileName
          + System.getProperty("line.separator") + e);
      e.printStackTrace();
    }
  }
}
