/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms;

import java.util.HashSet;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.tagging.CRFTSFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.CrfConceptSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.TokenSequenceFinder;

/** @author Glenn T. Gobbel Mar 21, 2016 */
public class TokenSequenceFinderBuilder {

  public static TokenSequenceFinder createTSFinder(TokenSequenceFinderSolution tsFinderSolution,
      HashSet<String> acceptedConcepts, double probabilisticThreshold) {
    if (tsFinderSolution instanceof ProbabilisticTSFinderSolution) {
      return new ProbabilisticTSFinder((ProbabilisticTSFinderSolution) tsFinderSolution,
          acceptedConcepts, probabilisticThreshold);
    } else if (tsFinderSolution instanceof CrfConceptSolution) {
      return new CRFTSFinder((CrfConceptSolution) tsFinderSolution);
    }

    return null;
  }
}
