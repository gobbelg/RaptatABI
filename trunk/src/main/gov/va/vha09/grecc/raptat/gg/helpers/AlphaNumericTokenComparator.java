package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class AlphaNumericTokenComparator implements Comparator<RaptatToken> {
  @Override
  public int compare(RaptatToken t1, RaptatToken t2) {
    return t1.getTokenStringAugmented().compareTo(t2.getTokenStringAugmented());
  }
}
