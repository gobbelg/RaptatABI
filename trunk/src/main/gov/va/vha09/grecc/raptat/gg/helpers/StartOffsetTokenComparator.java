package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class StartOffsetTokenComparator implements Comparator<RaptatToken> {
  @Override
  public int compare(RaptatToken t1, RaptatToken t2) {
    return Integer.parseInt(t1.getStartOffset()) - Integer.parseInt(t2.getStartOffset());
  }
}
