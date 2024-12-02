package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * ********************************************************** OccProbTriplet - Object to hold an int
 * and two doubles in a single object. Used to store the number of occurrences of an attribute and
 * the conditional likelihood calculation values when the data is unsmoothed and smoothed (to
 * account for non-occurrences in the training data)
 *
 * @author Glenn Gobbel, Jul 8, 2010
 *         <p>
 *         *********************************************************
 */
public class OccurProbTriplet implements Serializable {
  private static final long serialVersionUID = -4485880809167181317L;
  public int occurrences = 0;
  public double unsmoothedProb = 0;
  public double smoothedProb = 0;
  public boolean probabilitiesUpdated = false;


  /**
   * *************************************************************** Construct an OccurProbTriplet
   * from an integer and two doubles
   *
   * @param inInt - the integer input
   * @param firstdbl - the first double input
   * @param secondDbl = the second double input
   *        ***************************************************************
   */
  public OccurProbTriplet(int inInt, double firstDbl, double secondDbl) {
    this.occurrences = inInt;
    this.unsmoothedProb = firstDbl;
    this.smoothedProb = secondDbl;
    this.probabilitiesUpdated = true;
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof OccurProbTriplet) {
      OccurProbTriplet otherTriplet = (OccurProbTriplet) other;
      if (otherTriplet.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.occurrences, otherTriplet.occurrences)
            .append(this.probabilitiesUpdated, otherTriplet.probabilitiesUpdated).isEquals();
      }
    }
    return false;
  }


  @Override
  public int hashCode() {
    int oddNumber = 15;
    int primeNumber = 11;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);
    // We don't put unsmoothedProb and smoothedProb in the hashCode because
    // these
    // are calculated directly from the occurrences and should be in sync
    // with the value
    // of occurrences if probabilitiesUpdated is true
    return hcBuilder.append(this.occurrences).append(this.probabilitiesUpdated).toHashCode();
  }


  @Override
  public String toString() {
    return "[" + this.occurrences + ", " + String.format("%.2f", this.unsmoothedProb) + ", "
        + String.format("%.2f", this.smoothedProb) + "]";
  }


  private boolean canEqual(Object other) {
    return other instanceof OccurProbTriplet;
  }
}
