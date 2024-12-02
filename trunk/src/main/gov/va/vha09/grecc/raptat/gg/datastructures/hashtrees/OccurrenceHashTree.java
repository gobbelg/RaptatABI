package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class OccurrenceHashTree extends HashTree<OccurrenceHashTree> {
  private static final long serialVersionUID = 3627095325574782571L;
  List<OccurrenceHashTree> nextTrees = new ArrayList<>();


  public OccurrenceHashTree(int nextTreeSize, int depth) {
    super(nextTreeSize, depth);
  }


  public void addSequence(List<RaptatToken> theSequence) {
    OccurrenceHashTree nextTree;
    if (theSequence.size() > 0) {
      String curToken = theSequence.get(0).getTokenStringPreprocessed();

      if (!containsKey(curToken)) {
        // All tokens before end get a start labeled value of 1
        nextTree = new OccurrenceHashTree(getNextTreeSize(), this.treeDepth + 1);
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
    return other instanceof OccurrenceHashTree;
  }
}
