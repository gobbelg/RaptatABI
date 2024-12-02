package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ****************************************************** Class to store sequences of tokens in an
 * efficient manner as a tree structure. The iterator can be used to return the sequences in the
 * instance. Note that iteration return the sequence as the left side of a pair returned by the
 * iterator. The LabeledHashTree instance on the right is a dummy variable used only to make this
 * class conform to the interface IterableHashTree.
 *
 * @author Glenn Gobbel - Jun 23, 2012 *****************************************************
 */
public class SequenceHashTree extends HashTree<SequenceHashTree>
    implements IterableHashTree<LabeledHashTree> {
  private class SequenceIterator implements Iterator<RaptatPair<List<String>, LabeledHashTree>> {
    // Keep track of where we are in terms of the depth
    // of the hashtree
    private Stack<Enumeration<String>> hashTreeEnumerations;
    private Stack<SequenceHashTree> hashTrees;
    private List<String> curResult;

    // Keep the current string sequence to return
    // during iteration
    private Stack<String> keyStack;

    boolean nextFound;


    private SequenceIterator() {
      this.hashTreeEnumerations = new Stack<>();
      this.hashTrees = new Stack<>();
      this.keyStack = new Stack<>();

      this.hashTrees.push(SequenceHashTree.this);
      this.hashTreeEnumerations.push(keys());
      this.nextFound = setNext();
    }


    @Override
    public boolean hasNext() {
      return this.nextFound;
    }


    /**
     * *********************************************************** OVERRIDES PARENT METHOD
     *
     * @return
     * @author Glenn Gobbel - Jun 24, 2012
     *         ***********************************************************
     */
    @Override
    public RaptatPair<List<String>, LabeledHashTree> next() {
      RaptatPair<List<String>, LabeledHashTree> theResult =
          new RaptatPair<>(this.curResult, new LabeledHashTree());
      this.nextFound = setNext();
      return theResult;
    }


    /**
     * *********************************************************** OVERRIDES PARENT METHOD
     *
     * @author Glenn Gobbel - Jun 24, 2012
     *         ***********************************************************
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }


    private boolean setNext() {
      SequenceHashTree testTree;
      String curKey;
      Enumeration<String> curHashKeys;
      SequenceHashTree curTree;

      while (!this.hashTrees.empty()) {
        curTree = this.hashTrees.pop();
        curHashKeys = this.hashTreeEnumerations.pop();

        while (curHashKeys.hasMoreElements()) {
          // Get the next key and add it to the
          // stack to return it as part of the
          // returned sequence.
          curKey = curHashKeys.nextElement();
          this.keyStack.push(curKey);
          this.curResult = new ArrayList<>(this.keyStack);

          // Return the current tree and key enumeration
          // to the stack
          this.hashTrees.push(curTree);
          this.hashTreeEnumerations.push(curHashKeys);

          // See if the current key hashes to another
          // tree with keys, and if so, add them to the
          // stack. Otherwise, pop the key off the stack
          // as we are done with it.
          testTree = curTree.get(curKey);
          if (testTree.size() > 0) {
            this.hashTrees.push(testTree);
            this.hashTreeEnumerations.push(testTree.keys());
          } else {
            this.keyStack.pop();
          }
          return true;
        }
        if (!this.keyStack.isEmpty()) {
          this.keyStack.pop();
        }
      }
      return false;
    }
  }

  private static final long serialVersionUID = 8741842894375052767L;


  public SequenceHashTree() {
    super();
  }


  public SequenceHashTree(List<AnnotatedPhrase> thePhrases) {
    super();
    for (AnnotatedPhrase curPhrase : thePhrases) {
      addSequence(curPhrase.getProcessedTokens());
    }
  }


  /**
   * ********************************************
   *
   * @param nextTreeSize
   * @param depth
   * @author Glenn Gobbel - Jun 24, 2012 ********************************************
   */
  private SequenceHashTree(int nextTreeSize, int depth) {
    super(nextTreeSize, depth);
  }


  public void addSequence(List<RaptatToken> theSequence) {
    SequenceHashTree nextTree;
    if (theSequence.size() > 0) {
      String curToken = theSequence.get(0).getTokenStringPreprocessed();

      if (!containsKey(curToken)) {
        // All tokens before end get a start labeled value of 1
        nextTree = new SequenceHashTree(getNextTreeSize(), this.treeDepth + 1);
        put(theSequence.get(0).getTokenStringPreprocessed(), nextTree);
      } else {
        nextTree = get(curToken);
      }
      if (theSequence.size() > 1) {
        nextTree.addSequence(theSequence.subList(1, theSequence.size()));
      }
    }
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof SequenceHashTree;
  }


  @Override
  public Iterator<RaptatPair<List<String>, LabeledHashTree>> iterator() {
    return new SequenceIterator();
  }
}
