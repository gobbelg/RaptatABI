package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ****************************************************** Used for sorting phrases in reverse order
 * based on start offset, so phases with the highest start offset are placed first.
 *
 * @author Glenn Gobbel - Jun 15, 2012 *****************************************************
 */
public class ReverseStartOffsetPhraseComparator implements Comparator<AnnotatedPhrase> {
  @Override
  public int compare(AnnotatedPhrase a1, AnnotatedPhrase a2) {
    return Integer.parseInt(a2.getRawTokensStartOff())
        - Integer.parseInt(a1.getRawTokensStartOff());
  }
}
