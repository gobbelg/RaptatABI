package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

public class StartOffsetPhraseComparator implements Comparator<AnnotatedPhrase> {
  @Override
  public int compare(AnnotatedPhrase a1, AnnotatedPhrase a2) {
    return Integer.parseInt(a1.getRawTokensStartOff())
        - Integer.parseInt(a2.getRawTokensStartOff());
  }
}
