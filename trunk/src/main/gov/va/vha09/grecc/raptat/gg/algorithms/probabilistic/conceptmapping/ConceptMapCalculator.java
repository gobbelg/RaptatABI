/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping;

import java.util.HashSet;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - May 16, 2012 *****************************************************
 */
public abstract class ConceptMapCalculator {
  public abstract String[] calcBestConcept(String[] theAttributes,
      HashSet<String> acceptableConcepts);
}
