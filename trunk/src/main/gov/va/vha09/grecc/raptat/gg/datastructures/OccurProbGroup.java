package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * ********************************************************** OccProbGroup - Object to hold an int
 * and two double arrays in a single object. Used to store the number of occurrences of an attribute
 * and the conditional likelihood calculation values when the data is unsmoothed and smoothed (to
 * account for non-occurrences in the training data)
 *
 * @author Glenn Gobbel, Jul 8, 2010
 *         <p>
 *         *********************************************************
 */
public class OccurProbGroup implements Serializable {
  private static final long serialVersionUID = -3294375007241116114L;

  private static Logger logger = Logger.getLogger(OccurProbGroup.class);

  static {
    OccurProbGroup.logger.setLevel(Level.INFO);
  }

  /* Make these fields public for rapid access */
  public int occurrences = 0;

  /*
   * Note that the mappedTokens field is only used with BOWPriorTables not NBPriorTables
   */
  public HashMap<String, Integer> mappedTokens =
      new HashMap<>(RaptatConstants.ATTRIBUTES_PER_CONCEPT);

  public double[] unsmoothedProbs = null;

  public double[] smoothedProbs = null;
  public boolean probabilitiesUpdated = false;


  /**
   * *************************************************************** Construct a IntDbl Pair from an
   * Integer and Double object
   *
   * @param inInt
   * @param second ***************************************************************
   */
  public OccurProbGroup(int inInt, double[] firstDbl, double[] secondDbl) {
    this.occurrences = inInt;
    this.unsmoothedProbs = firstDbl;
    this.smoothedProbs = secondDbl;
    this.probabilitiesUpdated = true;
  }


  /**
   * *************************************************************** Construct a IntDbl Pair from an
   * Integer and Double object
   *
   * @param inInt
   * @param second ***************************************************************
   */
  public OccurProbGroup(int inInt, double[] firstDbl, double[] secondDbl, String[] mappedTokens) {
    this.occurrences = inInt;
    this.unsmoothedProbs = firstDbl;
    this.smoothedProbs = secondDbl;
    for (String curToken : mappedTokens) {
      this.mappedTokens.put(curToken, 1);
    }

    this.probabilitiesUpdated = true;
  }


  public void addTokens(String[] tokenData) {
    OccurProbGroup.logger.debug("Adding tokens:" + Arrays.toString(tokenData));
    for (String curToken : tokenData) {
      if (this.mappedTokens.containsKey(curToken)) {
        Integer updatedValue = this.mappedTokens.get(curToken) + 1;
        this.mappedTokens.put(curToken, updatedValue);
      } else {
        this.mappedTokens.put(curToken, 1);
      }
    }
  }


  public boolean canEqual(Object other) {
    return other instanceof OccurProbGroup;
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof OccurProbGroup) {
      OccurProbGroup otherGroup = (OccurProbGroup) other;
      if (otherGroup.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.occurrences, otherGroup.occurrences)
            .append(this.mappedTokens, otherGroup.mappedTokens)
            .append(this.probabilitiesUpdated, otherGroup.probabilitiesUpdated).isEquals();
      }
    }
    return false;
  }


  @Override
  public int hashCode() {
    int oddNumber = 23;
    int primeNumber = 59;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.occurrences).append(this.mappedTokens)
        .append(this.probabilitiesUpdated).toHashCode();
  }


  @Override
  public String toString() {
    String resString = "[";
    resString += this.occurrences;
    if (this.unsmoothedProbs != null && this.smoothedProbs != null) {
      resString +=
          ", " + Arrays.toString(this.smoothedProbs) + ", " + Arrays.toString(this.unsmoothedProbs);
    }
    return resString + "]";
  }
}
