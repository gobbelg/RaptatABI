package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.text.RandomStringGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** LabeledHashTree - Stores number of
 * times a sequence of tokens was labeled or unlabeled in a document as well as number of times a
 * label was deleted by a reviewer following automated annotation. Also keeps track of the
 * likelihood of a particular sequence being labeled, which is calculated when updateProbabilities()
 * is called.
 *
 * <p>
 * A user can check whether the likelihood of the tree and all subtrees have been updated using the
 * method, probabilitiesUpdated(). For safety, the protected boolean, probabiltiesUpdated, should
 * not be examined for this purpose as it refers only to this, top tree and no subtrees.
 *
 * @author Glenn Gobbel, Oct 15, 2010 *********************************************************
 */
public class LabeledHashTree extends HashTree<LabeledHashTree>
    implements IterableHashTree<LabeledHashTree>, Serializable, SequenceIDStructure {
  /**
   * ************************************************************** Provides an implementation of
   * iterable to return the labeled sequences and their corresponding field values.
   *
   * @author Glenn Gobbel - Feb 8, 2012
   *         *************************************************************
   */
  private class LabeledIterator implements Iterator<RaptatPair<List<String>, LabeledHashTree>> {
    // Keep track of where we are in terms of the depth
    // of the hashtree
    private final ArrayDeque<Enumeration<String>> hashTreeKeys;
    private final ArrayDeque<LabeledHashTree> hashTrees;

    // Keep the current string sequence to return
    // during iteration
    private final ArrayDeque<String> keyStack;

    private RaptatPair<List<String>, LabeledHashTree> curResult;
    boolean nextFound;


    private LabeledIterator() {

      this.hashTreeKeys = new ArrayDeque<>();
      this.hashTrees = new ArrayDeque<>();
      this.keyStack = new ArrayDeque<>();

      this.hashTrees.addLast(LabeledHashTree.this);
      this.hashTreeKeys.addLast(keys());
      this.nextFound = setNext();
    }


    @Override
    public boolean hasNext() {
      return this.nextFound;
    }


    /**
     * ************************************************************** Returns the next sequence of
     * string tokens in the hashTree along with the labeled, unlabeled, deleted, and likelihood for
     * the sequence.
     *
     * @return - The labeledHashTreeFields for the next token sequence
     *         **************************************************************
     */
    @Override
    public RaptatPair<List<String>, LabeledHashTree> next() {
      RaptatPair<List<String>, LabeledHashTree> theResult = this.curResult;
      this.nextFound = setNext();
      return theResult;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }


    private boolean setNext() {
      while (!this.hashTrees.isEmpty()) {
        LabeledHashTree curTree = this.hashTrees.removeLast();
        Enumeration<String> curHashKeys = this.hashTreeKeys.removeLast();

        while (curHashKeys.hasMoreElements()) {
          String testKey = curHashKeys.nextElement();
          LabeledHashTree testTree = curTree.get(testKey);
          if (testTree.labeled > 0) {
            this.keyStack.addLast(testKey);
            List<String> keys = new ArrayList<>(this.keyStack);
            this.curResult = new RaptatPair<>(keys, testTree);
            this.hashTrees.addLast(curTree);
            this.hashTreeKeys.addLast(curHashKeys);
            if (testTree.size() > 0) {
              curTree = testTree;
              curHashKeys = curTree.keys();
              this.hashTrees.addLast(curTree);
              this.hashTreeKeys.addLast(curHashKeys);
            } else {
              this.keyStack.removeLast();
            }
            return true;
          }
          if (testTree.size() > 0) {
            this.keyStack.addLast(testKey);
            this.hashTrees.addLast(curTree);
            this.hashTreeKeys.addLast(curHashKeys);
            curTree = testTree;
            curHashKeys = curTree.keys();
          }
        }

        if (!this.keyStack.isEmpty()) {
          this.keyStack.removeLast();
        }
      }
      return false;
    }
  }

  private static final long serialVersionUID = 8400123512310067818L;


  protected long labeled = 0;


  private long unlabeled = 0;

  private long deleted = 0;

  private double p_labeled = 0;

  // Keep track of whether the likelihood (p_labeled) for all the subtrees
  // extending from this
  // tree have been updated to a correct value based on their labeled and
  // unlabeled values.
  protected boolean probabilitiesUpdated = true;

  public LabeledHashTree() {
    super();
  }

  public LabeledHashTree(int hashSize, int depth) {
    super(hashSize, depth);
  }


  public LabeledHashTree(int hashSize, int depth, int labeled, int unlabeled, int deleted) {

    this(hashSize, depth);
    this.labeled = labeled;
    this.unlabeled = unlabeled;
    this.deleted = deleted;
    if (labeled > 0) {
      this.p_labeled = (float) labeled / (labeled + unlabeled);
    } else {
      this.p_labeled = 0;
    }
    this.probabilitiesUpdated = true;
  }


  private LabeledHashTree(int hashSize) {
    super(hashSize);
  }


  /**
   * *********************************************************** Increments the deleted count of the
   * input phrase
   *
   * @param curTokenPhrase
   * @param useStems
   * @author Glenn Gobbel - Jun 1, 2012 ***********************************************************
   */
  public void addDeletedPhrase(List<RaptatToken> inputPhrase) {

    LabeledHashTree curTree;
    this.probabilitiesUpdated = false;
    String curToken = inputPhrase.get(0).getTokenStringAugmented();

    if (!containsKey(curToken)) {
      curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 0, 0, 1);
      put(inputPhrase.get(0).getTokenStringAugmented(), curTree);
    } else {
      curTree = get(curToken);
    }

    if (inputPhrase.size() == 1) {
      curTree.deleted++;
    }

    List<RaptatToken> subPhrase = inputPhrase.subList(1, inputPhrase.size());
    if (!subPhrase.isEmpty()) {
      curTree.addDeletedPhrase(subPhrase);
    }
  }


  /**
   * ************************************************************** Increment the deleted
   * occurrences for each token in a token sequence
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   * @return - Boolean indicating if the phrase is unique
   *         **************************************************************
   */
  public void addDeletedPhrase(String[] inSequence) {
    LabeledHashTree curTree;
    this.probabilitiesUpdated = false;

    if (!containsKey(inSequence[0])) {
      curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 1, 0, 0);
      put(inSequence[0], curTree);
    } else {
      curTree = get(inSequence[0]);
    }

    if (inSequence.length == 1) {
      curTree.deleted++;
    }

    String[] subPhrase = GeneralHelper.getSubArray(inSequence, 1);
    if (subPhrase != null) {
      curTree.addDeletedPhrase(subPhrase);
    }
  }


  /**
   * *********************************************************** Adds the token string or string
   * stems of the current phrase to this tree. Precondition: Tokens in the input phrase are in the
   * exact order that they exist within the list that constitutes the inputSequence. Also, there
   * must be at least one token in the input phrase or the called function will generate an error.
   *
   * <p>
   * Post-condition: Strings are added to the tree and the setting of the labeled field is updated
   * to reflect this.
   *
   * @param inputSequence
   * @param useStems
   * @return Returns a pair of objects for each sequence that is "new." The left side of the pair
   *         contains the new sequence. The right side contains the depth into the tree at which the
   *         sequence becomes new - returns -1 if not a new sequence at any depth. For example, if
   *         "the dog is running" is the phrase, and "the dog" is already in the tree, the number
   *         returned would be 2, and both "the dog is" and "the dog is running' would be new. If
   *         the entire sequence was new, the number returned would be 0, and "the", "the dog", etc.
   *         would all be new sequences.
   * @author Glenn Gobbel - Apr 6, 2012 ***********************************************************
   */
  public List<RaptatPair<List<RaptatToken>, Integer>> addLabeledPhrase(
      List<RaptatToken> inputSequence) {
    List<RaptatPair<List<RaptatToken>, Integer>> newSequences = new ArrayList<>();
    int newSequenceDepth;
    if ((newSequenceDepth = this.addLabeledPhrase(inputSequence, -1)) > -1) {
      newSequences.add(new RaptatPair<>(inputSequence, newSequenceDepth));
    }
    return newSequences;
  }


  /**
   * ************************************************************** Add the sequence to the
   * LabeledHashTree and increment the labeled occurrences for last token in a token sequence
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   * @return - Boolean indicating if the phrase is unique
   *         **************************************************************
   */
  public boolean addLabeledPhrase(String[] inSequence) {
    boolean uniquePhrase = false;
    LabeledHashTree curTree;
    this.probabilitiesUpdated = false;

    if (!containsKey(inSequence[0])) {
      curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 1, 0, 0);
      put(inSequence[0], curTree);
      uniquePhrase = true;
    } else {
      curTree = get(inSequence[0]);
    }

    if (inSequence.length == 1) {
      if (curTree.labeled < 1) {
        uniquePhrase = true;
      }
      curTree.labeled++;
    }

    String[] subPhrase = GeneralHelper.getSubArray(inSequence, 1);
    if (subPhrase != null) {
      uniquePhrase = curTree.addLabeledPhrase(subPhrase);
    }

    return uniquePhrase;
  }


  /**
   * *********************************************************** Add a list of tokens to the labeled
   * tree and mark the sequence as labeled if it's position is greater than or equal to the value of
   * index
   *
   * @param theSequence
   * @param index
   * @author Glenn Gobbel - Jun 24, 2012 ***********************************************************
   */
  public void addPhraseLabelFromIndex(List<RaptatToken> inputSequence, int index) {
    if (inputSequence != null && inputSequence.size() > 0) {
      LabeledHashTree curTree;
      this.probabilitiesUpdated = false;
      String curToken = inputSequence.get(0).getTokenStringAugmented();
      if (!containsKey(curToken)) {
        // All tokens before end get a start labeled value of 1
        if (this.treeDepth >= index) {
          curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 1, 0, 0);
        } else {
          curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 0, 0, 0);
        }
        put(curToken, curTree);
      } else {
        curTree = get(curToken);
        if (this.treeDepth >= index) {
          curTree.labeled++;
        }
      }
      if (inputSequence.size() > 1) {
        curTree.addPhraseLabelFromIndex(inputSequence.subList(1, inputSequence.size()), index);
      }
    }
  }


  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for a token sequence
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   *        **************************************************************
   */
  public void addUnlabeledPhrase(String[] inSequence) {
    LabeledHashTree curTree;
    this.probabilitiesUpdated = false;

    if (!containsKey(inSequence[0])) {
      curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 1, 0, 0);
      put(inSequence[0], curTree);
    } else {
      curTree = get(inSequence[0]);
    }

    if (inSequence.length == 1) {
      curTree.unlabeled++;
    }

    String[] subPhrase = GeneralHelper.getSubArray(inSequence, 1);
    if (subPhrase != null) {
      curTree.addUnlabeledPhrase(subPhrase);
    }
  }


  // See http://www.artima.com/lejava/articles/equality.html for why this is
  // useful
  @Override
  public boolean canEqual(Object other) {
    return other instanceof LabeledHashTree;
  }


  @Override
  public void compareTo(Object other) {
    if (other instanceof LabeledHashTree) {
      LabeledHashTree otherTree = (LabeledHashTree) other;
      super.compareTo(otherTree);
      System.out.println("Labeled values equal:" + (this.labeled == otherTree.labeled));
      System.out.println("Unlabeled values equal:" + (this.unlabeled == otherTree.unlabeled));
      System.out.println("Deleted values equal:" + (this.deleted == otherTree.deleted));
      System.out.println("ProbabilitiesUpdated values equal:"
          + (this.probabilitiesUpdated == otherTree.probabilitiesUpdated));

    } else {
      System.out.println("Cannot compare\nNot a LabeledHashTree instance");
    }
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof LabeledHashTree) {
      LabeledHashTree otherTree = (LabeledHashTree) other;
      if (otherTree.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        boolean test = super.equals(otherTree);
        test = this.labeled == otherTree.labeled;
        test = this.unlabeled == otherTree.unlabeled;
        test = this.deleted == otherTree.deleted;
        test = this.probabilitiesUpdated == otherTree.probabilitiesUpdated;
        return eBuilder.appendSuper(super.equals(otherTree)).append(this.labeled, otherTree.labeled)
            .append(this.unlabeled, otherTree.unlabeled).append(this.deleted, otherTree.deleted)
            .append(this.probabilitiesUpdated, otherTree.probabilitiesUpdated).isEquals();
      }
    }
    return false;
  }


  // Added by Malini:
  public double getLabeledProbability() {
    this.probabilitiesUpdated = true;

    if (this.labeled > 0) {
      return this.p_labeled = (double) this.labeled / (this.labeled + this.unlabeled);
    } else {
      return 0;
    }
  }


  /**
   * ***********************************************************
   *
   * @param sequence
   * @return likelihood that the sequence is labeled or 0 if sequence does not exist in the tree
   * @author Modified by Glenn Gobbel - Jun 2, 2012
   *         ***********************************************************
   */
  public double getSequenceProbability(List<RaptatToken> sequence) {
    LabeledHashTree currentTree = this;
    String curString;
    for (RaptatToken curToken : sequence) {
      // We skip blank augmented token strings as they indicate stop
      // word removal
      if (!(curString = curToken.getTokenStringAugmented()).equals("")) {
        if ((currentTree = currentTree.get(curString)) == null) {
          return -1;
        }
      }
    }
    return currentTree.getLabeledProbability();
  }


  @Override
  public int hashCode() {
    int oddNumber = 37;
    int primeNumber = 17;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.labeled).append(this.unlabeled).append(this.deleted)
        .append(this.probabilitiesUpdated).append(super.hashCode()).toHashCode();
  }


  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for the last token in the token sequence. Returns 0 if the sequence does not exist
   * in the tree.
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * <p>
   * Postcondition: unlabeledOccurrences of the sequence are incremented by numberUnlabeled
   *
   * @param inSequence
   * @param numberUnlabeled
   * @return - 1 if sequence found and incremented, 0 otherwise.
   *         **************************************************************
   */
  public int incrementStringSequenceUnlabeled(List<String> stringSequence, long numberUnlabeled) {
    LabeledHashTree curTree = this;
    Iterator<String> sequenceIterator = stringSequence.iterator();
    ArrayDeque<LabeledHashTree> treeStack = new ArrayDeque<>(stringSequence.size());

    while (curTree != null && sequenceIterator.hasNext()) {
      // Store trees onto stack so that we can change the
      // probabilitiesUpdated variables if
      // needed
      treeStack.push(curTree);
      curTree = curTree.get(sequenceIterator.next());
    }

    if (curTree != null) {
      curTree.unlabeled += numberUnlabeled;
      while (!treeStack.isEmpty()) {
        treeStack.pop().probabilitiesUpdated = false;
      }
      return 1;
    }
    return 0;
  }


  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for the last token in the token sequence. Returns 0 if the sequence does not exist
   * in the tree. Precondition: inSequence is not empty and exists in the tree. Postcondition:
   * unlabeledOccurrences of the sequence are incremented by incrementValue
   *
   * @param inSequence
   * @param incrementValue
   * @return - 1 if sequence found and incremented, 0 otherwise.
   *         **************************************************************
   */
  public int incrementTokenSequenceUnlabeled(List<RaptatToken> tokenSequence, int incrementValue) {
    LabeledHashTree curTree = this;
    Iterator<RaptatToken> sequenceIterator = tokenSequence.iterator();
    ArrayDeque<LabeledHashTree> treeStack = new ArrayDeque<>(tokenSequence.size());

    while (curTree != null && sequenceIterator.hasNext()) {
      // Store trees onto stack so that we can change the
      // probabilitiesUpdated variables if
      // needed
      treeStack.push(curTree);
      curTree = curTree.get(sequenceIterator.next().getTokenStringAugmented());
    }

    if (curTree != null) {
      curTree.unlabeled += incrementValue;
      while (!treeStack.isEmpty()) {
        treeStack.pop().probabilitiesUpdated = false;
      }
      return 1;
    }
    return 0;
  }


  @Override
  public Iterator<RaptatPair<List<String>, LabeledHashTree>> iterator() {
    return new LabeledIterator();
  }


  @Override
  public void print() {
    Enumeration<String> keyEnumerator = keys();
    System.out.println();

    while (keyEnumerator.hasMoreElements()) {
      String curKey = keyEnumerator.nextElement();
      this.print(curKey, get(curKey), 0);
    }
  }


  public void printLabeledSequences() {
    for (RaptatPair<List<String>, LabeledHashTree> curResult : this) {
      System.out.println(curResult.left);
    }
  }


  /**
   * *********************************************************** Determines whether the likelihood
   * in the current tree and all subtrees have been updated.
   *
   * @return boolean
   * @author Glenn Gobbel - Feb 8, 2012 ***********************************************************
   */
  public boolean probabilitiesUpdated() {
    ArrayDeque<LabeledHashTree> hashTrees = new ArrayDeque<>();
    ArrayDeque<Enumeration<String>> hashTreeEnumerations = new ArrayDeque<>();
    Enumeration<String> curHashKeys;
    LabeledHashTree curTree;

    hashTrees.push(this);
    hashTreeEnumerations.push(keys());

    while (!hashTrees.isEmpty()) {
      curTree = hashTrees.removeLast();
      curHashKeys = hashTreeEnumerations.removeLast();

      if (!curTree.probabilitiesUpdated) {
        return false;
      }

      while (curHashKeys.hasMoreElements()) {
        curTree = curTree.get(curHashKeys.nextElement());
        hashTrees.push(curTree);
        hashTreeEnumerations.push(curTree.keys());
      }
    }
    return true;
  }


  @Override
  public void showComparison(Object other) {
    if (other instanceof LabeledHashTree) {
      LabeledHashTree otherTree = (LabeledHashTree) other;
      for (RaptatPair<List<String>, LabeledHashTree> curItem : otherTree) {
        LabeledHashTree thisResult = getTree(curItem.left);
        LabeledHashTree otherResult = otherTree.getTree(curItem.left);
        if (thisResult != null) {
          if (thisResult.labeled != otherResult.labeled
              || thisResult.unlabeled != otherResult.unlabeled
              || thisResult.deleted != otherResult.deleted
              || thisResult.probabilitiesUpdated != otherResult.probabilitiesUpdated) {
            System.out.println(
                "\n----------------\nDifferences in " + "fields for tree at:\n----------------");
            System.out.println(Arrays.toString(curItem.left.toArray()));
            System.out.println("This tree: labeled:" + thisResult.labeled + "  unlabeled:"
                + thisResult.unlabeled + "  deleted:" + thisResult.deleted
                + "  probabilitiesUpdated:" + thisResult.probabilitiesUpdated);
            System.out.println("Other tree: labeled:" + otherResult.labeled + "  unlabeled:"
                + otherResult.unlabeled + "  deleted:" + otherResult.deleted
                + "  probabilitiesUpdated:" + otherResult.probabilitiesUpdated);
          } else {
            System.out.println(
                "\n----------------\nNo difference in " + "fields for tree at:\n----------------");
            System.out.println(Arrays.toString(curItem.left.toArray()));
            System.out.println("This tree: labeled:" + thisResult.labeled + "  unlabeled:"
                + thisResult.unlabeled + "  deleted:" + thisResult.deleted
                + "  probabilitiesUpdated:" + thisResult.probabilitiesUpdated);
            System.out.println("Other tree: labeled:" + otherResult.labeled + "  unlabeled:"
                + otherResult.unlabeled + "  deleted:" + otherResult.deleted
                + "  probabilitiesUpdated:" + otherResult.probabilitiesUpdated);
          }
        } else {
          System.out.println(
              "\n-------------------------------\nSequence from other tree not found in this tree:\n"
                  + "-------------------------------");
          System.out.println(Arrays.toString(curItem.left.toArray()));
        }
      }

    } else {
      System.out.println("Cannot compare\nNot a LabeledHashTree instance");
    }
  }


  /**
   * *********************************************************** Update the values of p_labeled, the
   * likelihood of a sequence of tokens being labeled, for the entire tree. This will reset
   * probabilitiesUpdated to true;
   *
   * @author Glenn Gobbel - Feb 1, 2012 ***********************************************************
   */
  public void updateProbabilities() {
    String curKey = null;
    ArrayDeque<Enumeration<String>> hashTreeEnumerations = new ArrayDeque<>();
    ArrayDeque<LabeledHashTree> hashTrees = new ArrayDeque<>();

    LabeledHashTree curTree = this;
    Enumeration<String> curHashKeys = curTree.keys();
    if (curHashKeys.hasMoreElements()) {
      curKey = curHashKeys.nextElement();
    }

    while (curKey != null) {
      LabeledHashTree tempTree = curTree.get(curKey);
      if (tempTree.labeled > 0) {
        tempTree.p_labeled = (float) tempTree.labeled / (tempTree.labeled + tempTree.unlabeled);
        tempTree.probabilitiesUpdated = true;
      }

      // If there are keys in tree (tempTree) returned from hashing using
      // curKey, store the tree as curKey and use its keys as curHashKeys
      if (tempTree.size() > 0) {
        // Save the keys for the current hashtree on top of the
        // stack for later use if necessary
        hashTrees.push(curTree);
        hashTreeEnumerations.push(curHashKeys);

        // Since we've already checked that the size of tempTree
        // is not 0, there must be at least one element, so we don't
        // need to check before calling keys() or nextElement();
        curTree = tempTree;
        curHashKeys = curTree.keys();
        curKey = curHashKeys.nextElement();
      }

      // If there are no keys in tempTree, go to next key in curTree.
      // or go down in stack to find a tree that has more keys.
      else {
        curKey = null;

        // Check to see if there are any more keys for the current hash
        // tree, and,
        // if not, step back to the hashkeys and hashtree stored on the
        // stack
        while (!curHashKeys.hasMoreElements() && !hashTreeEnumerations.isEmpty()) {
          curTree = hashTrees.removeLast();
          curHashKeys = hashTreeEnumerations.removeLast();
        }
        if (curHashKeys.hasMoreElements()) {
          curKey = curHashKeys.nextElement();
        }
      }
    }
  }


  /**
   * *********************************************************** Adds the token string or string
   * stems of the current phrase to this tree. Helper method for addLabeledPhrase(List<Token>).
   *
   * <p>
   * Precondition: Tokens in the input phrase are in the exact order that they exist within the
   * string that constitutes the inputPhrase. Post-condition: Strings are added to the tree and the
   * setting of the labeled field is updated to reflect this.
   *
   * @param inputSequence
   * @param curDepth
   * @return The depth at which the sequence was found to be new. For example, if "the dog is
   *         running" is the phrase, and "the dog" is already in the tree, the number returned would
   *         be 2, and both "the dog is" and "the dog is running would be new. If the entire
   *         sequence was new, the number returned would be 0, and "the", "the dog", etc. would all
   *         be new sequences.
   * @author Glenn Gobbel - Apr 6, 2012 ***********************************************************
   */
  private int addLabeledPhrase(List<RaptatToken> inputSequence, int curDepth) {
    int foundDepth = curDepth;
    LabeledHashTree curTree;
    this.probabilitiesUpdated = false;
    String curToken = inputSequence.get(0).getTokenStringAugmented();

    if (!containsKey(curToken)) {
      // All tokens before end get a start labeled value of 1
      curTree = new LabeledHashTree(getNextTreeSize(), this.treeDepth + 1, 1, 0, 0);
      put(curToken, curTree);

      // If foundDepth is currently less than 1, then we hadn't previously
      // found evidence that
      // this is a new sequence, so set the foundDepth to current tree.
      // depth
      if (foundDepth < 0) {
        foundDepth = this.treeDepth;
      }
    } else {
      curTree = get(curToken);
    }

    if (inputSequence.size() == 1) {
      curTree.labeled++;
    }

    if (inputSequence.size() > 1) {
      foundDepth =
          curTree.addLabeledPhrase(inputSequence.subList(1, inputSequence.size()), foundDepth);
    }
    return foundDepth;
  }


  private LabeledHashTree getTree(List<String> stringSequence) {
    Iterator<String> sequenceIterator = stringSequence.iterator();
    LabeledHashTree curTree = this;

    while (curTree != null && sequenceIterator.hasNext()) {
      curTree = curTree.get(sequenceIterator.next());
    }
    return curTree;
  }


  private void print(String aKey, LabeledHashTree itsTree, int numTabs) {
    for (int i = 0; i < numTabs; i++) {
      System.out.print("\t");
    }
    System.out.print("Level " + numTabs + ": '" + aKey + "' - " + "Labeled:" + itsTree.labeled);
    System.out.print(", Unlabeled:" + itsTree.unlabeled);
    System.out.print(", Deleted:" + itsTree.deleted);
    System.out.println(", ProbabilityLabeled:" + itsTree.p_labeled);
    Enumeration<String> keyEnumerator = itsTree.keys();
    while (keyEnumerator.hasMoreElements()) {
      String curKey = keyEnumerator.nextElement();
      this.print(curKey, itsTree.get(curKey), numTabs + 1);
    }
  }


  public static LabeledHashTree generateRandomTree(int maxWidth, int maxDepth) {
    LabeledHashTree resultTree = new LabeledHashTree(maxWidth);
    Random randNumGenerator = new Random();
    randNumGenerator.setSeed(System.currentTimeMillis());

    if (maxWidth > 0 && maxDepth > 0) {
      addRandomSubTree(resultTree, maxWidth, maxDepth, randNumGenerator);
    }
    return resultTree;
  }


  /**
   * *********************************************************** Method to generate a random
   * LabeledHashTree instance for testing purposes. The number of phrases in the tree will be @param
   * width
   *
   * @param depth
   * @return
   * @author Glenn Gobbel - Feb 27, 2012 ***********************************************************
   */
  private static void addRandomSubTree(LabeledHashTree curTree, int width, int depth,
      Random randNumGenerator) {
    int maxChars = 14;
    int maxCount = 20;
    int curDepth;

    int treeWidth = randNumGenerator.nextInt(width);
    for (int i = 0; i < treeWidth; i++) {
      LabeledHashTree subTree = new LabeledHashTree(width);
      subTree.labeled = randNumGenerator.nextInt(maxCount);
      subTree.unlabeled = randNumGenerator.nextInt(maxCount);
      subTree.deleted = randNumGenerator.nextInt(maxCount);
      subTree.p_labeled = (float) subTree.labeled / (subTree.labeled + subTree.unlabeled);

      curDepth = randNumGenerator.nextInt(depth);
      if (curDepth > 0) {
        addRandomSubTree(subTree, width, curDepth, randNumGenerator);
      }

      RandomStringGenerator stringGenerator =
          new RandomStringGenerator.Builder().withinRange('0', 'z').build();
      curTree.put(stringGenerator.generate(randNumGenerator.nextInt(maxChars)), subTree);
    }
  }
}
