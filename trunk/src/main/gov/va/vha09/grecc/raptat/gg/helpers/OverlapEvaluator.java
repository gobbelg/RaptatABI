/** */
package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ************************************************
 *
 * @author vhatvhgobbeg - Mar 5, 2013 ************************************************
 */
public interface OverlapEvaluator {
  public boolean areOverlapped(List<RaptatToken> tokenSequence1, List<RaptatToken> tokenSequence2);
}
