package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.HashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.IterableHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/**
 * Used to store phrases and their labels. Used for storing phrases that determine context of tokens
 * that precede or follow the phrase. IMPORTANT NOTE - The phraseTag label can only be set the first
 * time a phrase is added to the PhraseTree instance. The only way to change the label is to
 * generate a new instance and repopulate it with phrases. This is done for efficiency.
 *
 * @author Glenn Gobbel - Jun 3, 2013
 */
public class PhraseTree extends HashTree<PhraseTree>
    implements IterableHashTree<PhraseTree>, Serializable {
  /**
   * Provides an implementation of iterable to return the labeled sequences and their corresponding
   * field values.
   *
   * @author Glenn Gobbel - Feb 8, 2012
   */
  private class PhraseTreeIterator implements Iterator<RaptatPair<List<String>, PhraseTree>> {
    /*
     * Keep track of where we are in terms of the depth of the hashtree
     */
    private final ArrayDeque<Enumeration<String>> phraseTreeKeys;
    private final ArrayDeque<PhraseTree> phraseTrees;

    /*
     * Keep the current string sequence to return during iteration
     */
    private final ArrayDeque<String> keyStack;

    private RaptatPair<List<String>, PhraseTree> curResult;
    boolean nextFound;


    private PhraseTreeIterator() {

      this.phraseTreeKeys = new ArrayDeque<>();
      this.phraseTrees = new ArrayDeque<>();
      this.keyStack = new ArrayDeque<>();

      this.phraseTrees.addLast(PhraseTree.this);
      this.phraseTreeKeys.addLast(keys());
      this.nextFound = setNext();
    }


    @Override
    public boolean hasNext() {
      return this.nextFound;
    }


    /**
     * Returns the next sequence of string tokens in the hashTree along with the labeled, unlabeled,
     * deleted, and likelihood for the sequence.
     *
     * @return - The labeledHashTreeFields for the next token sequence
     */
    @Override
    public RaptatPair<List<String>, PhraseTree> next() {
      RaptatPair<List<String>, PhraseTree> theResult = this.curResult;
      this.nextFound = setNext();
      return theResult;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }


    private boolean setNext() {
      while (!this.phraseTrees.isEmpty()) {
        PhraseTree curTree = this.phraseTrees.removeLast();
        Enumeration<String> curHashKeys = this.phraseTreeKeys.removeLast();

        while (curHashKeys.hasMoreElements()) {
          String testKey = curHashKeys.nextElement();
          PhraseTree testTree = curTree.get(testKey);
          if (testTree.phraseTag != null) {
            this.keyStack.addLast(testKey);
            List<String> keys = new ArrayList<>(this.keyStack);
            this.curResult = new RaptatPair<>(keys, testTree);
            this.phraseTrees.addLast(curTree);
            this.phraseTreeKeys.addLast(curHashKeys);
            if (testTree.size() > 0) {
              curTree = testTree;
              curHashKeys = curTree.keys();
              this.phraseTrees.addLast(curTree);
              this.phraseTreeKeys.addLast(curHashKeys);
            } else {
              this.keyStack.removeLast();
            }
            return true;
          }
          if (testTree.size() > 0) {
            this.keyStack.addLast(testKey);
            this.phraseTrees.addLast(curTree);
            this.phraseTreeKeys.addLast(curHashKeys);
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

  private static final long serialVersionUID = 3777907279564137001L;

  private Mark phraseTag = null;

  /**
   * Determines whether phrases put into the tree and compare to the tree should consider character
   * case. This is done to add flexibility to the class. We also have included a method
   * phraseLengthAtIndexIgnoreCase() which speeds up processing by avoiding an "if" test.
   */
  private boolean ignoreCase = true;


  public PhraseTree() {
    super();
  }


  public PhraseTree(boolean ignoreCase) {
    this();
    this.ignoreCase = ignoreCase;
  }


  private PhraseTree(int treeSize, int treeDepth, boolean ignoreCase) {
    super(treeSize, treeDepth);
    this.ignoreCase = ignoreCase;
  }


  /**
   * <b>Method Description</b> - Adds the phrases in the list, thePhrases, to the phrase tree, with
   * the marker, marker, at the end of the String arrays that make up the each phrase in the list
   *
   * @param thePhrases
   * @param marker
   * @author Glenn Gobbel - Jul 17, 2014
   */
  public void addPhrases(List<String[]> thePhrases, Mark marker) {
    for (String[] curPhrase : thePhrases) {
      this.addPhrase(curPhrase, marker, 0);
    }
  }


  /**
   * <b>Method Description</b> - Adds the phrases in the list, thePhrases, to the phrase tree, with
   * the marker, marker, at the end of the String arrays that make up the each phrase in the list.
   * Renamed from addPhrases to allow for differentiating methods due to type erasure in Java.
   *
   * @param thePhrases
   * @param marker
   * @author Glenn Gobbel - Jul 17, 2014
   */
  public void addPhrasesAsLists(List<SortableStringArrayList> thePhrases, Mark marker) {
    for (List<String> curPhrase : thePhrases) {
      this.addPhrase(curPhrase, marker, 0);
    }
  }


  // /**
  // * <b>Method Description</b> - Adds the phrases in the list, thePhrases,
  // to
  // * the phrase tree, with the marker, marker, at the end of the String
  // arrays
  // * that make up the each phrase in the list
  // *
  // * @param thePhrases
  // * @param marker
  // *
  // * @author Glenn Gobbel - Jul 17, 2014
  // */
  // public void addPhrases(List<List<String>> thePhrases, Mark marker)
  // {
  // for (String[] curPhrase : thePhrases)
  // {
  // addPhrase( curPhrase, marker, 0 );
  // }
  // }

  @Override
  public boolean canEqual(Object other) {
    return other instanceof PhraseTree;
  }


  public boolean containsPhrase(String[] phrase) {
    if (phrase == null || phrase.length == 0) {
      return false;
    }

    PhraseTree curTree = this;
    int curIndex = 0;

    if (this.ignoreCase) {
      while (curIndex < phrase.length
          && (curTree = curTree.get(phrase[curIndex].toLowerCase())) != null) {
        ++curIndex;
      }
    } else {
      while (curIndex < phrase.length && (curTree = curTree.get(phrase[curIndex])) != null) {
        ++curIndex;
      }
    }
    return phrase.length == curIndex;
  }


  public Mark getPhraseTag() {
    return this.phraseTag;
  }


  /**
   * Method to find all the phraseTag Mark objects for the strings in the tokenStrings array
   * starting at startIndex and continuing to the end of the array.
   *
   * @param tokenStrings
   * @param phraseIndex
   * @return
   */
  public HashMap<Mark, List<Integer>> getPhraseTags(String[] tokenStrings, int startIndex) {
    if (tokenStrings == null) {
      return null;
    }

    HashMap<Mark, List<Integer>> foundMarkList = new HashMap<>();

    PhraseTree curTree = this;
    int phraseIndex = startIndex;
    Mark foundMark;

    while (phraseIndex < tokenStrings.length) {
      /*
       * Stop if there is no further part of the tokenString array in the current phraseTree object
       */
      if ((curTree = curTree.get(tokenStrings[phraseIndex])) == null) {
        return foundMarkList;
      }

      /*
       * If a marking phraseTag is found in the current phraseTree, add it to the HashMap of results
       */
      if ((foundMark = curTree.getPhraseTag()) != null) {
        List<Integer> indexList;
        if ((indexList = foundMarkList.get(foundMark)) == null) {
          indexList = new ArrayList<>();
          foundMarkList.put(foundMark, indexList);
        }
        indexList.add(phraseIndex);
      }

      /*
       * Increase phraseIndex so we can test the string at the next index in tokenStrings to see if
       * it is in the curTree phraseTree object
       */
      ++phraseIndex;
    }

    return foundMarkList;
  }


  @Override
  public Iterator<RaptatPair<List<String>, PhraseTree>> iterator() {
    return new PhraseTreeIterator();
  }


  /**
   * Given an array of token strings, this method finds whether any sequence of the strings in the
   * array, starting at 'startIndex' and going to the length of the string, maps to a tree for which
   * the phraseTag field is equal to the parameter 'markToFind.'
   *
   * @param tokenStrings
   * @param startIndex
   * @param markToFind
   * @return
   */
  public int phraseLengthAtIndex(String[] tokenStrings, int startIndex, Mark markToFind) {
    if (tokenStrings == null || startIndex >= tokenStrings.length) {
      return 0;
    }

    int curMarkerLocation = startIndex;
    int curIndex = startIndex;
    PhraseTree curTree = this;

    if (this.ignoreCase) {

      while (curIndex < tokenStrings.length
          && (curTree = curTree.get(tokenStrings[curIndex].toLowerCase())) != null) {
        ++curIndex;
        if (curTree.phraseTag == markToFind) {
          curMarkerLocation = curIndex;
        }
      }
    } else {
      while (curIndex < tokenStrings.length
          && (curTree = curTree.get(tokenStrings[curIndex])) != null) {
        ++curIndex;
        if (curTree.phraseTag == markToFind) {
          curMarkerLocation = curIndex;
        }
      }
    }
    return curMarkerLocation - startIndex;
  }


  /**
   * Method added only to speed processing and avoid a test for ignore case, which is carried out by
   * phraseLengthAtIndex. Given an array of token strings, this method finds whether any sequence of
   * the strings in the array, starting at 'startIndex' and going to the length of the string, maps
   * to a tree for which the phraseTag field is equal to the parameter 'markToFind.'
   *
   * @param tokenStrings
   * @param startIndex
   * @param markToFind
   * @return
   */
  public int phraseLengthAtIndexIgnoreCase(String[] tokenStrings, int startIndex, Mark markToFind) {
    if (tokenStrings == null || startIndex >= tokenStrings.length) {
      return 0;
    }

    int curMarkerLocation = startIndex;
    int curIndex = startIndex;
    PhraseTree curTree = this;

    while (curIndex < tokenStrings.length
        && (curTree = curTree.get(tokenStrings[curIndex].toLowerCase())) != null) {
      ++curIndex;
      if (curTree.phraseTag == markToFind) {
        curMarkerLocation = curIndex;
      }
    }

    return curMarkerLocation - startIndex;
  }


  public void print() {
    StringBuilder sb = new StringBuilder();
    this.print(sb);
  }


  /**
   * Adds a phrase to this phrase tree, starting with the string at phraseIndex
   *
   * @param thePhrase
   * @param marker
   * @param phraseIndex
   *        <p>
   *        Glenn Gobbel - Jul 17, 2014
   */
  private void addPhrase(List<String> thePhrase, Mark marker, int phraseIndex) {
    PhraseTree curTree;

    String phrase = thePhrase.get(phraseIndex);
    if (!containsKey(phrase)) {
      curTree = new PhraseTree(getNextTreeSize(), this.treeDepth + 1, this.ignoreCase);
      put(phrase, curTree);
      ++phraseIndex;
    } else {
      curTree = get(phrase);
      ++phraseIndex;
    }

    if (phraseIndex < thePhrase.size()) {
      curTree.addPhrase(thePhrase, marker, phraseIndex);
    } else {
      curTree.phraseTag = marker;
    }
  }


  /**
   * Adds a phrase to this phrase tree, starting with the string at phraseIndex
   *
   * @param thePhrase
   * @param marker
   * @param phraseIndex
   *        <p>
   *        Glenn Gobbel - Jul 17, 2014
   */
  private void addPhrase(String[] thePhrase, Mark marker, int phraseIndex) {
    PhraseTree curTree;

    if (!containsKey(thePhrase[phraseIndex])) {
      curTree = new PhraseTree(getNextTreeSize(), this.treeDepth + 1, this.ignoreCase);
      put(thePhrase[phraseIndex], curTree);
      ++phraseIndex;
    } else {
      curTree = get(thePhrase[phraseIndex]);
      ++phraseIndex;
    }

    if (phraseIndex < thePhrase.length) {
      curTree.addPhrase(thePhrase, marker, phraseIndex);
    } else {
      curTree.phraseTag = marker;
    }
  }


  private void print(StringBuilder tabStrings) {
    int length = tabStrings.length() / 4;
    String startTabs = tabStrings.toString();
    System.out.println("\n" + startTabs + "----------------------");
    System.out.println(startTabs + "BeginLevel:" + length);
    System.out.println(startTabs + "----------------------");

    tabStrings.append("    ");

    for (String curKey : keySet()) {
      System.out.println(tabStrings + curKey);

      PhraseTree nextTree = get(curKey);
      if (nextTree.isEmpty() && nextTree.phraseTag != null) {
        System.out.println(tabStrings + "Tag:" + nextTree.phraseTag);
      } else {
        nextTree.print(new StringBuilder(tabStrings));
      }

      System.out.println();
    }
    System.out.println("\n" + startTabs + "----------------------");
    System.out.println(startTabs + "EndLevel:" + length);
    System.out.println(startTabs + "----------------------");
  }
}
