package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

public class EndOffsetComparator implements Comparator<AnnotatedPhrase> {
  @Override
  public int compare(AnnotatedPhrase a1, AnnotatedPhrase a2) {
    int result =
        Integer.parseInt(a1.getRawTokensEndOff()) - Integer.parseInt(a2.getRawTokensEndOff());

    if (result < 0) {
      return -1;
    }

    if (result == 0) {
      return 0;
    }

    return 1;
  }
}
