package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ****************************************************** This tree is used as the
 * primaryLabeledTree during training using XTREME_MOD training. It extends the
 * LabeledSubsequenceHashTree in that it does the scoring slightly differently. As for that
 * hashtree, everything is calculated from the MAX_LABELED_SCORE constant, and only the last token,
 * when it is in the proper position in the tree, get the MAX_LABELED_SCORE.
 *
 * @author Glenn Gobbel - Feb 11, 2013 *****************************************************
 */
public class ModifiedSubsequenceHashTree extends LabeledSubsequenceHashTree {

  /** */
  private static final long serialVersionUID = 8052108445322626681L;


  public ModifiedSubsequenceHashTree() {
    super();
  }


  private ModifiedSubsequenceHashTree(int nextTreeSize, int depth, int labeled, int unlabeled,
      int deleted) {
    super(nextTreeSize, depth, labeled, unlabeled, deleted);
  }


  /**
   * *********************************************************** Adds the token string or string
   * stems of the current phrase to this tree. Unlike the superclass LabeledHashTree method, this
   * method labels everything up to the last token in a sequence, including each subsequence, with
   * the length of the sequence determining how much score to give it, with the score equal to the
   * sequence positions divided by the length multiplied by the base score. The maximum score is set
   * so that the subsequence score is always an integer. It differs from the
   * LabeledSubsequenceHashTree parent class in that when an annotated phrase is added to a tree,
   * when subsequences are closer to the end of the actual phrase, they get more weight in terms of
   * labeling. So, if the phrase was "big red dog", "big" as the first term in the tree would get
   * less label value than "dog" as the first term in the tree, because dog is closer to and, in
   * fact is the end of the phrase.
   *
   * <p>
   * Precondition: Tokens in the input phrase are in the exact order that they exist within the
   * string that constitutes the inputPhrase.
   *
   * <p>
   * Post-condition: Strings are added to the tree and the setting of the labeled field is updated
   * to reflect this.
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

    // This loop selects subsequences of the phrase that start closer and
    // closer to the end
    // of the phrase. So, for the phrase "big red dog", it first selects
    // "big red dog", then
    // "red dog", and then just "dog."
    for (int startTokenIndex = 0; startTokenIndex < sequenceSize; startTokenIndex++) {
      int curMaxLength = sequenceSize - startTokenIndex;

      // curMaxScore is the maximum possible score and the score for the
      // token at the end of
      // a sequence of this length
      int curMaxScore = RaptatConstants.MAX_LABELED_SCORE * curMaxLength / sequenceSize;

      // curScore starts out at the score for a single token at the
      // beginning of the current
      // subsequence. This uses a log scale so that there is less weight
      // given to
      // tokens at the beginning of long phrases.
      int curScore = curMaxScore / (int) Math.pow(2, curMaxLength - 1);

      // This loop selects subsequences extending further and further from
      // the beginning of
      // the phrase selected by the outer loop. So, for "big red dog", it
      // build the
      // ModifiedSubsequenceHashTree by adding, in turn, the phrases
      // "big", then "big red",
      // and finally "big red dog" before going back to the outer loop.
      for (int endTokenIndex =
          startTokenIndex + 1; endTokenIndex <= sequenceSize; endTokenIndex++) {
        List<RaptatToken> subSequence = inputSequence.subList(startTokenIndex, endTokenIndex);

        int newSequenceDepth = this.addLabeledPhrase(subSequence, curScore, -1);
        // this.print();
        if (newSequenceDepth > -1) {
          newSequenceList.add(new RaptatPair<>(subSequence, newSequenceDepth));
        }
        curScore *= 2;
      }
    }

    return newSequenceList;
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof ModifiedSubsequenceHashTree;
  }


  private int addLabeledPhrase(List<RaptatToken> inputSequence, int inputScore, int foundDepth) {
    ModifiedSubsequenceHashTree curTree;
    String curToken = inputSequence.get(0).getTokenStringAugmented();
    this.probabilitiesUpdated = false;

    if (!containsKey(curToken)) {
      // All tokens before end get a labeled value of 0
      curTree = new ModifiedSubsequenceHashTree(getNextTreeSize(), this.treeDepth + 1, 0, 0, 0);
      put(curToken, curTree);

      // If foundDepth is currently less than 1, then we hadn't previously
      // found evidence that
      // this is a new sequence, so set the foundDepth to current tree.
      // depth
      if (foundDepth < 0) {
        foundDepth = this.treeDepth;
      }
    } else {
      curTree = (ModifiedSubsequenceHashTree) get(curToken);
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
