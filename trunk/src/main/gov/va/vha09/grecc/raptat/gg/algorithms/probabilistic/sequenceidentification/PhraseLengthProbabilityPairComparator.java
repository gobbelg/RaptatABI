/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification;

import java.util.Comparator;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ****************************************************** Gives an ordering to AnnotatedPhrase
 * objects based first on length and then the likelihood, where the AnnotatedPhrase objects come as
 * pairs with the left part of the pair have the object itself and the right containing the
 * likelihood.
 *
 * @author Glenn Gobbel - Jun 3, 2012 *****************************************************
 */
public class PhraseLengthProbabilityPairComparator
    implements Comparator<RaptatPair<List<RaptatToken>, Double>> {
  private boolean useLength = true;
  private boolean useProbability = false;


  public PhraseLengthProbabilityPairComparator(boolean useLength, boolean useProbability) {
    if (useLength == false && useProbability == false) {
      System.err.println("Error instantiating PhraseLengthProbabilityPair");
      throw new RuntimeException("Incorrect construction of PhraseLengthProbabilityPair Comparator:"
          + " Both useLength and useProbability cannot both be initialized to false");
    }
    this.useLength = useLength;
    this.useProbability = useProbability;
  }


  @Override
  public int compare(RaptatPair<List<RaptatToken>, Double> a1,
      RaptatPair<List<RaptatToken>, Double> a2) {
    int returnValue = 0;

    if (this.useLength && (returnValue = a2.left.size() - a1.left.size()) != 0) {
      return returnValue;
    }

    // If we get to here, the lengths must be equal or we don't use lengths
    // for comparison
    // so we determine return value based on likelihood if useProbability is
    // true
    // on likelihood
    if (this.useProbability) {
      if (a1.right > a2.right) {
        return -1;
      }
      if (a1.right < a2.right) {
        return 1;
      }
    }
    return 0;
  }
}
