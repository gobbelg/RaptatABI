package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ****************************************************** Implements both HashTreeInStreamer and
 * HashTreeOutStreamer interfaces so that a class can use UnlabeledHashTrees stored in either memory
 * (as are instances of this class) or on disk (see UnlabeledHashTreeOutputStream and
 * UnlabeledHashTreeInputStream) using a common interface.
 *
 * @author Glenn Gobbel - Jul 7, 2012 *****************************************************
 */
public class UnlabeledHashTreeStreamer
    implements HashTreeInStreamer<UnlabeledHashTree>, HashTreeOutStreamer<UnlabeledHashTree> {
  private List<UnlabeledHashTree> theTreeList;

  private String saveToPath;

  public UnlabeledHashTreeStreamer(String pathForSaving) {
    this.saveToPath = pathForSaving;
    this.theTreeList = new ArrayList<>();
  }


  /**
   * ******************************************** Reads in one or a series of UnlabeledHashTree
   * instances from a file and stores them in the List, theTreeList.
   *
   * <p>
   * Precondition: The trees within the file pathToPreviousHashTree must have been written using an
   * UnlabeledHashTreeOutputStream instance.
   *
   * @param pathForSaving
   * @param pathToPreviousHashTree
   * @author Glenn Gobbel - Jul 8, 2012 ********************************************
   */
  public UnlabeledHashTreeStreamer(String pathForSaving, String pathToPreviousHashTree) {
    this(pathForSaving);

    UnlabeledHashTreeInputStream inputStreamer =
        new UnlabeledHashTreeInputStream(pathToPreviousHashTree);
    Iterator<UnlabeledHashTree> uhtIterator = inputStreamer.iterator();
    while (uhtIterator.hasNext()) {
      this.theTreeList.add(uhtIterator.next());
    }
  }

  // TODO Remove unused code found by UCDetector
  // public UnlabeledHashTreeStreamer(UnlabeledHashTree theTree)
  // {
  // theTreeList = new ArrayList<UnlabeledHashTree>();
  // theTreeList.add(theTree);
  // }


  // TODO Remove unused code found by UCDetector
  // public UnlabeledHashTreeStreamer(List<UnlabeledHashTree> aTreeList)
  // {
  // theTreeList = new ArrayList<UnlabeledHashTree>();
  // Iterator<UnlabeledHashTree> treeIterator = aTreeList.iterator();
  // if (treeIterator.hasNext())
  // {
  // theTreeList.add(treeIterator.next());
  // UnlabeledHashTree theOnlyTree = theTreeList.get(0);
  // while (treeIterator.hasNext())
  // {
  // theOnlyTree.merge(treeIterator.next());
  // }
  // }
  // }

  @Override
  public boolean append(HashTree<UnlabeledHashTree> newTree) {
    if (this.theTreeList.size() > 0) {
      UnlabeledHashTree theOnlyTree = this.theTreeList.get(0);
      return theOnlyTree.merge((UnlabeledHashTree) newTree);
    }
    this.theTreeList.add(new UnlabeledHashTree((UnlabeledHashTree) newTree));
    return true;
  }


  @Override
  public void clear() {
    this.theTreeList.clear();
  }


  @Override
  public Iterator<UnlabeledHashTree> iterator() {
    return this.theTreeList.iterator();
  }


  @Override
  public void writeTreesToDisk() {
    UnlabeledHashTreeOutputStream outStream = new UnlabeledHashTreeOutputStream(this.saveToPath);
    if (this.theTreeList.size() > 0) {
      UnlabeledHashTree theOnlyTree = this.theTreeList.get(0);
      outStream.write(theOnlyTree);
    } else {
      outStream.write(new UnlabeledHashTree());
    }
  }


  public static void main(String[] args) {
    UnlabeledHashTreeStreamer theStreamer = new UnlabeledHashTreeStreamer(null, null);

    Iterator<UnlabeledHashTree> theIterator = theStreamer.iterator();

    while (theIterator.hasNext()) {
      System.out.println(theIterator.next());
    }
  }
}
