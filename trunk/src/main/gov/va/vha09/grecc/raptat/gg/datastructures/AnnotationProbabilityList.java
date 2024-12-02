package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.ArrayList;

/**
 * ****************************************************** Used by the NBSequenceIDTrainer (and
 * NTTokenSequenceEvaluator) to store the likelihood of a token at a particular position in a phrase
 * of being classified as unannotated (position 0 in the list) or annotated (position 1 in the
 * list).
 *
 * @author Glenn Gobbel - Aug 2, 2012 *****************************************************
 */
public class AnnotationProbabilityList extends ArrayList<OccurProbTriplet> {
  private static final long serialVersionUID = -8433591165277140222L;
  public static final int NOT_ANNOTATED = 0;
  public static final int ANNOTATED = 1;


  public AnnotationProbabilityList() {
    super(2);
  }
}
