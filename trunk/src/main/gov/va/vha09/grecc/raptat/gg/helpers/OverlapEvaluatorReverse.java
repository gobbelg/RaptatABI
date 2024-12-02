/** */
package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ************************************************
 *
 * @author vhatvhgobbeg - Mar 5, 2013 ************************************************
 */
public class OverlapEvaluatorReverse implements OverlapEvaluator {

  /**
   * ***********************************************************
   *
   * @param curPhrase
   * @param testPhrase
   * @return
   * @author Glenn Gobbel - Jun 16, 2012 ***********************************************************
   */
  @Override
  public boolean areOverlapped(List<RaptatToken> curPhrase, List<RaptatToken> testPhrase) {
    int curPhraseStart = Integer.parseInt(curPhrase.get(curPhrase.size() - 1).getStartOffset());
    int curPhraseEnd = Integer.parseInt(curPhrase.get(0).getEndOffset());
    int testPhraseStart = Integer.parseInt(testPhrase.get(testPhrase.size() - 1).getStartOffset());
    int testPhraseEnd = Integer.parseInt(testPhrase.get(0).getEndOffset());

    return curPhraseStart < testPhraseEnd && curPhraseEnd > testPhraseStart;
  }
}
