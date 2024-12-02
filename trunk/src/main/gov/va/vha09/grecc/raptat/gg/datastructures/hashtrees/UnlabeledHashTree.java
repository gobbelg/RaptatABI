package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** UnlabeledHashTree - Stores number of
 * times a sequence of tokens occurred in a document but was not annotated (labeled).
 *
 * @author Glenn Gobbel, Oct 15, 2010 *********************************************************
 */
public class UnlabeledHashTree extends HashTree<UnlabeledHashTree>
    implements Iterable<HashTreeFields>, Serializable {
  public class UnlabeledIterator implements Iterator<HashTreeFields> {
    // Keep track of where we are in terms of the depth
    // of the hashtree
    private Stack<Enumeration<String>> hashTreeEnumerations;
    private Stack<UnlabeledHashTree> hashTrees;

    // Keep the current string sequence to return
    // during iteration
    private Stack<String> keyStack;

    private UnlabeledHashTreeFields curResult;
    boolean nextFound;


    private UnlabeledIterator() {

      this.hashTreeEnumerations = new Stack<>();
      this.hashTrees = new Stack<>();
      this.keyStack = new Stack<>();

      this.hashTrees.push(UnlabeledHashTree.this);
      this.hashTreeEnumerations.push(keys());
      this.nextFound = setNext();
    }


    @Override
    public boolean hasNext() {
      return this.nextFound;
    }


    /**
     * ************************************************************** Returns the next sequence of
     * string tokens in the hashTree that form an unlabeled token sequence
     *
     * @return - The UnlabeledHashTreeFields for the next token sequence
     *         **************************************************************
     */
    @Override
    public UnlabeledHashTreeFields next() {
      UnlabeledHashTreeFields resultFields = this.curResult;
      this.nextFound = setNext();
      return resultFields;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }


    private boolean setNext() {
      UnlabeledHashTree testTree;
      String testKey;
      Enumeration<String> curHashKeys;
      UnlabeledHashTree curTree;

      while (!this.hashTrees.empty()) {
        curTree = this.hashTrees.pop();
        curHashKeys = this.hashTreeEnumerations.pop();

        while (curHashKeys.hasMoreElements()) {
          testKey = curHashKeys.nextElement();
          testTree = curTree.get(testKey);
          if (testTree.unlabeledOccurrences > 0) {
            this.keyStack.push(testKey);
            this.curResult = new UnlabeledHashTreeFields(new ArrayList<>(this.keyStack),
                testTree.unlabeledOccurrences);
            this.hashTrees.push(curTree);
            this.hashTreeEnumerations.push(curHashKeys);
            if (testTree.size() > 0) {
              curTree = testTree;
              curHashKeys = curTree.keys();
              this.hashTrees.push(curTree);
              this.hashTreeEnumerations.push(curHashKeys);
            } else {
              this.keyStack.pop();
            }
            return true;
          }
          if (testTree.size() > 0) {
            this.keyStack.push(testKey);
            this.hashTrees.push(curTree);
            this.hashTreeEnumerations.push(curHashKeys);
            curTree = testTree;
            curHashKeys = curTree.keys();
          }
        }

        if (!this.keyStack.empty()) {
          this.keyStack.pop();
        }
      }
      return false;
    }
  }

  private static final long serialVersionUID = -4358244268547378760L;
  private String textSource = "";

  private long unlabeledOccurrences = 0;


  public UnlabeledHashTree() {
    super();
  }


  // Added by Malini
  public UnlabeledHashTree(String sourceDocument) {
    super();
    this.textSource = sourceDocument;
  }


  /**
   * ******************************************** Deep copy constructor
   *
   * @param copiedTree
   * @author Glenn Gobbel - Jul 10, 2012 ********************************************
   */
  public UnlabeledHashTree(UnlabeledHashTree copiedTree) {
    UnlabeledIterator theIterator = copiedTree.iterator();
    while (theIterator.hasNext()) {
      UnlabeledHashTreeFields curFields = theIterator.next();
      String[] theSequence = curFields.tokenSequence.toArray(new String[0]);
      this.addUnlabeledSequence(theSequence, curFields.unlabeled);
    }
    this.textSource = new String(copiedTree.textSource);
  }


  private UnlabeledHashTree(int hashSize, int depth) {
    super(hashSize, depth);
  }


  /**
   * *********************************************************** Add the sequence of tokens within
   * the list starting at curTokenPosition up to the length of the list or maxTokens, whichever
   * comes first. Mark each token as unlabeled except the one at skipPosition within the list. If
   * skipPosition is less than 0, we do not skip any and call another function so we don't have to
   * keep checking this. Preconditions: None. Postconditions: The tokens in the sequence are added
   * to this instance of an UnlabeledHashTree.
   *
   * @param sentenceTokens
   * @param curTokenPosition
   * @param skipPosition
   * @param useStems
   * @author Glenn Gobbel - Apr 7, 2012
   * @param maxTokens ***********************************************************
   */
  public void addUnlabeledSequence(List<RaptatToken> sentenceTokens, int curTokenPosition,
      int skipPosition, int maxTokens) {
    // If skip position is less than the current token position,
    // we don't have to keep checking for skipping and can go to
    // another function to handle this case.
    if (skipPosition < curTokenPosition) {
      this.addUnlabeledSequence(sentenceTokens, curTokenPosition, maxTokens);
    } else {
      UnlabeledHashTree curTree;
      String curTokenString = sentenceTokens.get(curTokenPosition).getTokenStringAugmented();

      if (!containsKey(curTokenString)) {
        curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
        put(curTokenString, curTree);
      } else {
        curTree = get(curTokenString);
      }
      if (curTokenPosition != skipPosition) {
        curTree.unlabeledOccurrences += RaptatConstants.MAX_LABELED_SCORE;
      }

      if (curTokenPosition < sentenceTokens.size() - 1 && this.treeDepth < maxTokens - 1) {
        curTree.addUnlabeledSequence(sentenceTokens, ++curTokenPosition, skipPosition, maxTokens);
      }
    }
  }


  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for each token in a token sequence
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   * @param tokenListIterator **************************************************************
   */
  public void addUnlabeledSequence(List<RaptatToken> inSequence,
      Iterator<RaptatToken> tokenListIterator) {
    UnlabeledHashTree curTree;
    String curTokenStringKey = tokenListIterator.next().getTokenStringAugmented();

    if (!containsKey(curTokenStringKey)) {
      curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
      put(curTokenStringKey, curTree);
    } else {
      curTree = get(curTokenStringKey);
    }

    if (!tokenListIterator.hasNext()) {
      curTree.unlabeledOccurrences += RaptatConstants.MAX_LABELED_SCORE;
    } else {
      curTree.addUnlabeledSequence(inSequence, tokenListIterator);
    }
  }


  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for each token in a token sequence
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   *        **************************************************************
   */
  public void addUnlabeledSequence(String[] inSequence) {
    UnlabeledHashTree curTree;

    if (!containsKey(inSequence[0])) {
      curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
      put(inSequence[0], curTree);
    } else {
      curTree = get(inSequence[0]);
    }

    if (inSequence.length == 1) {
      curTree.unlabeledOccurrences += RaptatConstants.MAX_LABELED_SCORE;
    }

    String[] subPhrase = GeneralHelper.getSubArray(inSequence, 1);
    if (subPhrase != null) {
      curTree.addUnlabeledSequence(subPhrase);
    }
  }


  /**
   * *********************************************************** Add the sequence of tokens within
   * the list starting at curTokenPosition up to the length of the list or MAX_ELEMENTS_DEFAULT,
   * whichever comes first. Only mark those at startIndex or greater as unlabeled.
   *
   * <p>
   * Preconditions:None. Postconditions: The tokens in the sequence are added to this instance of an
   * UnlabeledHashTree.
   *
   * @param sentenceTokens
   * @param curTokenPosition
   * @param skipPosition
   * @param useStems
   * @author Glenn Gobbel - Apr 7, 2012 ***********************************************************
   */
  public void addUnlabeledSequenceFromIndex(List<RaptatToken> sentenceTokens, int curTokenPosition,
      int startIndex, int maxTokens) {
    if (startIndex <= curTokenPosition) {
      this.addUnlabeledSequence(sentenceTokens, curTokenPosition, maxTokens);
    } else {
      UnlabeledHashTree curTree;
      String curTokenString = sentenceTokens.get(curTokenPosition).getTokenStringAugmented();

      if (!containsKey(curTokenString)) {
        curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
        put(curTokenString, curTree);
      } else {
        curTree = get(curTokenString);
      }
      if (curTokenPosition >= startIndex) {
        curTree.unlabeledOccurrences += RaptatConstants.MAX_LABELED_SCORE;
      }

      if (curTokenPosition < sentenceTokens.size() - 1 && this.treeDepth < maxTokens - 1) {
        curTree.addUnlabeledSequenceFromIndex(sentenceTokens, ++curTokenPosition, startIndex,
            maxTokens);
      }
    }
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof UnlabeledHashTree;
  }


  public boolean equals(UnlabeledHashTree otherTree) {
    if (this.unlabeledOccurrences != otherTree.unlabeledOccurrences) {
      return false;
    }

    if (!keySet().equals(otherTree.keySet())) {
      return false;
    }

    Enumeration<String> theKeys = keys();
    String curKey;
    while (theKeys.hasMoreElements()) {
      curKey = theKeys.nextElement();
      if (!get(curKey).equals(otherTree.get(curKey))) {
        return false;
      }
    }
    return true;
  }


  // Added by Malini
  public String getTextSourceName() {
    return this.textSource;
  }


  /**
   * *********************************************************** Returns the number of unlabeled
   * occurrences of a sequence of tokens provided as a list of strings being unlabeled in the
   * document corresponding to the instantiated UnlabeledHashTree
   *
   * @param tokenStringSequence
   * @return int - Number of unlabeled occurrences
   * @author Glenn Gobbel - Feb 8, 2012 ***********************************************************
   */
  public long getUnlabeledForStringSequence(List<String> stringSequence) {
    UnlabeledHashTree curTree = this;
    Iterator<String> sequenceIterator = stringSequence.iterator();

    while (curTree != null && sequenceIterator.hasNext()) {
      curTree = curTree.get(sequenceIterator.next());
    }

    if (curTree != null) {
      return curTree.unlabeledOccurrences;
    }
    return 0;
  }


  @Override
  public UnlabeledIterator iterator() {
    return new UnlabeledIterator();
  }


  public boolean merge(UnlabeledHashTree newTree) {
    UnlabeledIterator theSequences = newTree.iterator();

    while (theSequences.hasNext()) {
      UnlabeledHashTreeFields curSequenceFields = theSequences.next();
      List<String> tokenSequence = curSequenceFields.tokenSequence;
      long numUnlabeled = curSequenceFields.unlabeled;
      incrementUnlabeledSequence(tokenSequence, numUnlabeled);
    }

    // This return currently has no function - it should be
    // used to check for memory errors as the UnlabeledHashTree
    // could exceed memory limitations
    return true;
  }


  public void print() {
    System.out.println("Text Source:" + this.textSource);
    Enumeration<String> keyEnumerator = keys();

    while (keyEnumerator.hasMoreElements()) {
      String curKey = keyEnumerator.nextElement();
      this.print(curKey, get(curKey), 0);
    }
  }


  public void printUnlabeledSequences() {
    for (HashTreeFields curResult : this) {
      System.out.println(curResult);
    }
  }


  /**
   * *********************************************************** Add the sequence of token stems
   * within the list starting at curTokenPosition up to the length of the list or
   * MAX_ELEMENTS_DEFAULT, whichever comes first. Mark each token as unlabeled. Preconditions: None.
   * Postconditions: The tokens in the sequence are added to this instance of an UnlabeledHashTree.
   *
   * @param sentenceTokens
   * @param curTokenPosition
   * @param useStems
   * @author Glenn Gobbel - Apr 7, 2012
   * @param maxTokens ***********************************************************
   */
  private void addUnlabeledSequence(List<RaptatToken> sentenceTokens, int curTokenPosition,
      int maxTokens) {
    UnlabeledHashTree curTree;
    String curTokenString = sentenceTokens.get(curTokenPosition).getTokenStringAugmented();

    if (!containsKey(curTokenString)) {
      curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
      put(curTokenString, curTree);
    } else {
      curTree = get(curTokenString);
    }

    curTree.unlabeledOccurrences += RaptatConstants.MAX_LABELED_SCORE;

    // Recursively add the tokens fromm curTokenPosition
    // on until either we reach MAX_TOKENS_DEFAULT or
    // we reach the end of the sentence, whichever comes
    // first.
    if (curTokenPosition < sentenceTokens.size() - 1 && this.treeDepth < maxTokens - 1) {
      curTree.addUnlabeledSequence(sentenceTokens, ++curTokenPosition, maxTokens);
    }
  }


  // public String toString()
  // {
  // StringBuffer result = new StringBuffer();
  //
  // for (HashTreeFields curResult : this)
  // {
  // result.append(curResult);
  // }
  //
  // return new String(result);
  // }

  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for each token in a token sequence
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   *        **************************************************************
   */
  private void addUnlabeledSequence(String[] inSequence, long unlabeled) {
    UnlabeledHashTree curTree;

    if (!containsKey(inSequence[0])) {
      curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
      put(inSequence[0], curTree);
    } else {
      curTree = get(inSequence[0]);
    }

    if (inSequence.length == 1) {
      curTree.unlabeledOccurrences += unlabeled;
    }

    String[] subPhrase = GeneralHelper.getSubArray(inSequence, 1);
    if (subPhrase != null) {
      curTree.addUnlabeledSequence(subPhrase, unlabeled);
    }
  }


  /**
   * ************************************************************** Increment the unlabeled
   * occurrences for each token in a token sequence
   *
   * <p>
   * Precondition: inSequence is not empty
   *
   * @param inSequence - String array containing attributes to be incremented
   *        **************************************************************
   */
  private void incrementUnlabeledSequence(List<String> inSequence, long numberOfOccurrences) {
    UnlabeledHashTree curTree;
    String curTokenString = inSequence.get(0);
    if (!containsKey(curTokenString)) {
      curTree = new UnlabeledHashTree(getNextTreeSize(), this.treeDepth + 1);
      put(inSequence.get(0), curTree);
    } else {
      curTree = get(curTokenString);
    }

    if (inSequence.size() == 1) {
      curTree.unlabeledOccurrences += numberOfOccurrences;
    }

    List<String> subPhrase = inSequence.subList(1, inSequence.size());
    if (subPhrase != null && subPhrase.size() > 0) {
      curTree.incrementUnlabeledSequence(subPhrase, numberOfOccurrences);
    }
  }


  private void print(String aKey, UnlabeledHashTree itsTree, int numTabs) {

    for (int i = 0; i < numTabs; i++) {
      System.out.print("\t");
    }
    System.out.println("Level " + numTabs + ": '" + aKey + "' - " + "Unlabeled Occurrences:"
        + itsTree.unlabeledOccurrences);

    Enumeration<String> keyEnumerator = itsTree.keys();
    while (keyEnumerator.hasMoreElements()) {
      String curKey = keyEnumerator.nextElement();
      this.print(curKey, itsTree.get(curKey), numTabs + 1);
    }
  }
}
