package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class LabeledSubsequenceHashTree extends LabeledHashTree {
  private static final long serialVersionUID = 7117812301857803967L;


  public LabeledSubsequenceHashTree() {
    super();
  }


  protected LabeledSubsequenceHashTree(int nextTreeSize, int depth, int labeled, int unlabeled,
      int deleted) {
    super(nextTreeSize, depth, labeled, unlabeled, deleted);
  }


  /**
   * *********************************************************** Adds the token string or string
   * stems of the current phrase to this tree. Unlike the superclass LabeledHashTree method, this
   * method labels everything up to the last token in a sequence, including each subsequence, with
   * the length of the sequence determining how much score to give it, with the score equal to the
   * sequence positions divided by the length multiplied by the maximum achievable score. The
   * maximum score is set so that the subsequence score is always an integer.
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
  @Override
  public List<RaptatPair<List<RaptatToken>, Integer>> addLabeledPhrase(
      List<RaptatToken> inputSequence) {
    int sequenceSize = inputSequence.size();
    List<RaptatPair<List<RaptatToken>, Integer>> newSequenceList = new ArrayList<>();

    for (int startTokenIndex = 0; startTokenIndex < sequenceSize; startTokenIndex++) {
      for (int endTokenIndex =
          startTokenIndex + 1; endTokenIndex <= sequenceSize; endTokenIndex++) {
        List<RaptatToken> subSequence = inputSequence.subList(startTokenIndex, endTokenIndex);
        int score = subSequence.size() * RaptatConstants.MAX_LABELED_SCORE / sequenceSize;
        int newSequenceDepth = this.addLabeledPhrase(subSequence, score, -1);
        // this.print();
        if (newSequenceDepth > -1) {
          newSequenceList.add(new RaptatPair<>(subSequence, newSequenceDepth));
        }
      }
    }

    return newSequenceList;
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof LabeledSubsequenceHashTree;
  }


  private int addLabeledPhrase(List<RaptatToken> inputSequence, int inputScore, int foundDepth) {
    LabeledSubsequenceHashTree curTree;
    String curToken = inputSequence.get(0).getTokenStringAugmented();
    this.probabilitiesUpdated = false;

    if (!containsKey(curToken)) {
      // All tokens before end get a labeled value of 0
      curTree = new LabeledSubsequenceHashTree(getNextTreeSize(), this.treeDepth + 1, 0, 0, 0);
      put(curToken, curTree);

      // If foundDepth is currently less than 1, then we hadn't previously
      // found evidence that
      // this is a new sequence, so set the foundDepth to current tree.
      // depth
      if (foundDepth < 0) {
        foundDepth = this.treeDepth;
      }
    } else {
      curTree = (LabeledSubsequenceHashTree) get(curToken);
    }

    if (inputSequence.size() == 1) {
      curTree.labeled += inputScore;
    }

    if (inputSequence.size() > 1) {
      foundDepth = curTree.addLabeledPhrase(inputSequence.subList(1, inputSequence.size()),
          inputScore, foundDepth);
    }
    return foundDepth;
  }
}
