package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ****************************************************** Used for sorting tokens in reverse order
 * based on start offset, so tokens with the highest start offset are placed first.
 *
 * @author Glenn Gobbel - Jun 15, 2012 *****************************************************
 */
public class ReverseStartOffsetTokenComparator implements Comparator<RaptatToken> {
  @Override
  public int compare(RaptatToken t1, RaptatToken t2) {
    return Integer.parseInt(t2.getStartOffset()) - Integer.parseInt(t1.getStartOffset());
  }
}
