package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * Generic data structure that reproduces a binary search tree except that you can use it to lookup
 * nodes that are greater than or less than the value of some input data. So, if the tree contains
 * 1,5,8, and 20, a call to getClosestLesserOrEqual(10) will return 8 and a call to
 * getClosestGreaterObject(10) will return 20. The class is generic, so it cannot store primitives,
 * and objects stored as data must extend the IndexedObject class.
 *
 * @param <T>
 * @author Glenn T. Gobbel - Apr 26, 2012
 */
public class IndexedTree<T extends IndexedObject> implements Serializable, Iterable<T> {
  public class IndexedTreeIterator<S extends IndexedTree<T>> implements Iterator<S> {

    private Deque<S> treeStack;
    private S nextTree;
    private S activeTree;

    public IndexedTreeIterator() {
      this.treeStack = new ArrayDeque<>();
      this.activeTree = (S) IndexedTree.this;
      while (this.activeTree.leftBranch != null) {
        this.treeStack.push(this.activeTree);
        this.activeTree = (S) this.activeTree.leftBranch;
      }
      this.nextTree = this.activeTree.rightBranch == null ? this.treeStack.pop()
          : (S) this.activeTree.rightBranch;
    }


    /**
     * Return an iterator that returns the first node whose index precedes or equals start (i.e., <=
     * start) and the next node index is greater than start. The index of the last node is
     * implicitly defined as infinity, so if start is greater than the maximum index value in the
     * tree, a call to the IndexedTreeIterator(int start) constructor will return an iterator that
     * returns just the last node on the first call to next(). (After that, hasNext() will return
     * false).
     *
     * <p>
     * <b>NOTE:</b> If start exceeds the index of the last node, an iterator beginning with the last
     * node will be returned.
     *
     *
     * @param start
     */
    public IndexedTreeIterator(final int start) {
      this();
      if (this.activeTree != null) {
        while (this.nextTree != null && this.nextTree.node.index <= start) {
          next();
        }
      }
    }

    @Override
    public boolean hasNext() {
      return this.activeTree != null;
    }

    @Override
    public S next() {
      S resultTree = this.activeTree;
      this.activeTree = this.nextTree;
      if (this.nextTree != null) {
        if (this.nextTree.rightBranch == null) {
          this.nextTree = !this.treeStack.isEmpty() ? this.treeStack.pop() : null;
        } else {
          this.nextTree = (S) this.nextTree.rightBranch;
          while (this.nextTree.leftBranch != null) {
            this.treeStack.push(this.nextTree);
            this.nextTree = (S) this.nextTree.leftBranch;
          }
        }
      }
      return resultTree;
    }

    public S peek() {
      return this.activeTree;
    }


  }

  private static final long serialVersionUID = -2844903678839273631L;
  protected T node = null;
  protected IndexedTree<T> leftBranch = null;
  protected IndexedTree<T> rightBranch = null;
  protected int depth = -1;

  public IndexedTree() {
    super();
  }

  /**
   * Create an empty searchable binary tree with null as the root node.
   *
   * @author Glenn Gobbel - Apr 26, 2012
   * @param inputList
   */
  public IndexedTree(final List<T> inputList) {
    Collections.sort(inputList);
    if (inputList.size() > 0) {
      this.listToSBT(inputList, 0, inputList.size() - 1, 0);
    }
  }

  private IndexedTree(final List<T> inputList, final int start, final int end, final int depth) {
    this.listToSBT(inputList, start, end, depth);
  }

  public int depth() {
    return this.depth;
  }


  public ArrayList<T> getAllNodes() {
    ArrayList<T> resultList = new ArrayList<>();

    if (this.node != null) {
      if (this.leftBranch != null) {
        resultList.addAll(this.leftBranch.getAllNodes());
      }
      resultList.add(this.node);
      if (this.rightBranch != null) {
        resultList.addAll(this.rightBranch.getAllNodes());
      }
    }
    return resultList;
  }

  /*
   * * Get the object in the tree that is closest in value to a test value but greater than that
   * value
   *
   * @param testData
   *
   * @return Value of object in tree that is closest to the testValue but of equal or greater value.
   *
   * @author Glenn Gobbel - Apr 27, 2012
   */
  public T getClosestGreaterObject(final int testData) {
    if (this.node == null) {
      return null;
    }

    int curDataCompare = 0;

    if (testData > this.node.getIndex()) {
      curDataCompare = -1;
    } else if (testData < this.node.getIndex()) {
      curDataCompare = 1;
    }

    if (curDataCompare > 0) // testValue < objectValue
    {
      if (this.leftBranch == null) {
        // No smaller value, so no object less
        // than objectValue and possibly greater
        // than testValue exists
        return this.node;
      }
      // Look at left branch to see if any value smaller than
      // the objectValue is greater than testValue
      T nextNode = this.leftBranch.getClosestGreaterObject(testData);
      return nextNode != null ? nextNode : this.node;
    } // testValue >= objectValue
    else {
      if (this.rightBranch == null) {
        // No larger value, so return object value
        return null;
      }
      // Look at right branch to see if any value is larger than
      // the objectValue but still less than testValue
      return this.rightBranch.getClosestGreaterObject(testData);
    }
  }



  /*
   * * Get the object in the tree that is closest in value to a test value but lesser or equal to.
   *
   * @param testData
   *
   * @return Value of object in tree that is closest to the testValue but of equal or less value.
   *
   * @author Glenn Gobbel - Apr 27, 2012
   */
  public T getClosestLesserOrEqualObject(final int testIndex) {
    if (this.node == null) {
      return null;
    }

    int nodeIndex = this.node.getIndex();
    if (nodeIndex == testIndex) {
      return this.node;
    } else {
      if (testIndex < nodeIndex) {
        if (this.leftBranch == null) {
          // No smaller value, so return nothing
          return null;
        }
        // Look at left branch to see if any node index is less than testIndex
        return this.leftBranch.getClosestLesserOrEqualObject(testIndex);
      } // testIndex > nodeIndex, so search right
      else {
        if (this.rightBranch == null) {
          // No larger value, so return object value
          return this.node;
        }
        // Look at right branch to see if any nodeIndex there is less than textIndex
        T nextNode = this.rightBranch.getClosestLesserOrEqualObject(testIndex);
        return nextNode != null ? nextNode : this.node;
      }
    }
  }

  /*
   * * Get the tree with a node is closest in value to a test value but lesser or equal to.
   *
   * @param testData
   *
   * @return the tree that fits criterion
   *
   * @author Glenn Gobbel - Apr 27, 2012
   */
  public IndexedTree getClosestLesserOrEqualTree(final int inputIndex) {
    if (this.node == null) {
      return null;
    }
    int thisTreeIndex = this.node.getIndex();

    if (thisTreeIndex == inputIndex) {
      return this;
    } else {
      if (thisTreeIndex > inputIndex) {
        if (this.leftBranch == null) {
          // No smaller value, so return nothing
          return null;
        }
        return this.leftBranch.getClosestLesserOrEqualTree(inputIndex);
      } // testData > testTreeIndex
      else {
        if (this.rightBranch == null) {
          // No larger value, so return this tree
          return this;
        }
        // Look at right branch to see if any value is larger than
        // thisTreeIndex but still less than testValue
        IndexedTree otherTree = this.rightBranch.getClosestLesserOrEqualTree(inputIndex);
        return otherTree != null ? otherTree : this;
      }
    }
  }

  /**
   * @return the node
   */
  public T getNode() {
    return this.node;
  }

  @Deprecated
  public Set<IndexedObject<RaptatToken>> getObjectsCoveringRange(final int startOffset,
      final int endOffset) {
    // TODO Auto-generated method stub
    return null;
  }

  public RaptatPair<String, Integer> getSideAndDepth(final T testValue) {
    return this.getSideAndDepth(testValue, 0, "root");
  }

  @Override
  public IndexedTreeIterator iterator() {

    return new IndexedTreeIterator();
  }

  // public Iterator iterator(final int index) {
  //
  // return new IndexedTreeIterator(index);
  // }

  public IndexedTreeIterator iterator(final int start) {
    return new IndexedTreeIterator(start);
  }

  /*
   * Returns true if the data is in the binary tree
   *
   * @param testValue
   *
   * @return
   *
   * @author Glenn Gobbel - Apr 26, 2012
   */
  public boolean lookup(final T testValue) {
    if (this.node == null) {
      return false;
    }
    int dataCompare = this.node.compareTo(testValue);

    if (dataCompare == 0) {
      return true;
    } else {
      if (dataCompare > 0) {
        return this.leftBranch != null && this.leftBranch.lookup(testValue);
      }
      return this.rightBranch != null && this.rightBranch.lookup(testValue);
    }
  }

  public int maxDepth() {
    int leftBranchMax = -1;
    int rightBranchMax = -1;
    int maxBranch;

    if (this.leftBranch != null) {
      leftBranchMax = this.leftBranch.maxDepth();
    }
    if (this.rightBranch != null) {
      rightBranchMax = this.rightBranch.maxDepth();
    }
    maxBranch = leftBranchMax > rightBranchMax ? leftBranchMax : rightBranchMax;

    return maxBranch > this.depth ? maxBranch : this.depth;
  }

  public void print() {
    if (this.node != null) {
      System.out.print("Index:" + this.node.index);
      System.out.println(", IndexedObject:" + this.node.indexedObject);
    }
    if (this.leftBranch != null) {
      for (int i = 0; i < this.depth + 1; i++) {
        System.out.print("\t");
      }
      System.out.print("Left " + this.depth + " Branch--> ");
      this.leftBranch.print();
    }
    if (this.rightBranch != null) {
      for (int i = 0; i < this.depth + 1; i++) {
        System.out.print("\t");
      }
      System.out.print("Right " + this.depth + " Branch--> ");
      this.rightBranch.print();
    }
  }

  public void printAsList() {
    ArrayList<T> theNodes = this.getAllNodes();
    for (T curNode : theNodes) {
      System.out.println(curNode.getIndex() + ":" + curNode.getIndexedObject());
    }
  }

  private RaptatPair<String, Integer> getSideAndDepth(final T testValue, int startDepth,
      final String side) {

    if (this.node == null) {
      return null;
    }

    RaptatPair<String, Integer> resultPair = null;
    int dataCompare = this.node.compareTo(testValue);

    if (dataCompare == 0) {
      return new RaptatPair<>(side, startDepth);
    } else {
      if (dataCompare > 0) {
        if (this.leftBranch != null) {
          return this.leftBranch.getSideAndDepth(testValue, ++startDepth, "left");
        }
      } else {
        if (this.rightBranch != null) {
          return this.rightBranch.getSideAndDepth(testValue, ++startDepth, "right");
        }
      }
    }

    return resultPair;
  }

  /**
   * Helper method for creating a SearchableBinaryTree. Note that inputList must already be sorted.
   *
   * @param node
   * @param left
   * @param rightBranch
   * @author Glenn Gobbel - Apr 26, 2012
   */
  private void listToSBT(final List<T> inputList, final int start, final int end, final int depth) {
    int mid = (start + end) / 2;
    this.node = inputList.get(mid);
    this.depth = depth;
    if (mid > start) {
      this.leftBranch = new IndexedTree<>(inputList, start, mid - 1, depth + 1);
    }
    if (end > mid) {
      this.rightBranch = new IndexedTree<>(inputList, mid + 1, end, depth + 1);
    }
  }

}
