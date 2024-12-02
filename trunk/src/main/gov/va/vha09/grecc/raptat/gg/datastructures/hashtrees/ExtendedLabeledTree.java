package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ******************************************** A type of LabeledHashTree for which, whenever
 * sequences are added, all tokens in the sequence have their "labeled" value within the tree
 * incremented by one.
 *
 * @author Glenn Gobbel - Jul 8, 2012 ********************************************
 */
public class ExtendedLabeledTree extends LabeledHashTree {
  private static final long serialVersionUID = 7472897488501839049L;


  public ExtendedLabeledTree() {
    super();
  }


  private ExtendedLabeledTree(int nextTreeSize, int depth, int labeled, int unlabeled,
      int deleted) {
    super(nextTreeSize, depth, labeled, unlabeled, deleted);
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof ExtendedLabeledTree;
  }


  /**
   * *********************************************************** Adds the token string or string
   * stems of the current phrase to this tree. Helper method for addLabeledPhrase(List<Token>).
   * Unlike the superclass LabeledHashTree method, this method labels everything up to the last
   * token in a sequence equally, every time it is called, whereas, for the superclass, it is only
   * labeled equally when the sequence is first put into the structure.
   *
   * <p>
   * Precondition: Tokens in the input phrase are in the exact order that they exist within the
   * string that constitutes the inputPhrase. Also, input sequence is not null, and length ≥ 1.
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
  protected int addLabeledPhrase(List<RaptatToken> inputSequence, int curDepth) {
    int foundDepth = curDepth;
    ExtendedLabeledTree curTree;
    this.probabilitiesUpdated = false;
    String curToken = inputSequence.get(0).getTokenStringAugmented();

    if (!containsKey(curToken)) {
      // All tokens before end get a start labeled value of 1
      curTree = new ExtendedLabeledTree(getNextTreeSize(), this.treeDepth + 1, 0, 0, 0);
      put(curToken, curTree);

      // If foundDepth is currently less than 1, then we hadn't previously
      // found evidence that
      // this is a new sequence, so set the foundDepth to current tree.
      // depth
      if (foundDepth < 0) {
        foundDepth = this.treeDepth;
      }
    } else {
      curTree = (ExtendedLabeledTree) get(curToken);
    }
    curTree.labeled++;

    if (inputSequence.size() > 1) {
      foundDepth =
          curTree.addLabeledPhrase(inputSequence.subList(1, inputSequence.size()), foundDepth);
    }
    return foundDepth;
  }
}
