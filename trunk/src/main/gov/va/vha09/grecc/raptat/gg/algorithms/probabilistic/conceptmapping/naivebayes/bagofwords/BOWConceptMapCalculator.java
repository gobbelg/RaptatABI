/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.AttributeDBMismatchException;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBCalculator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.FilterType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbTriplet;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - May 16, 2012 *****************************************************
 */
public class BOWConceptMapCalculator extends NBCalculator {

  /**
   * ********************************************
   *
   * @param theSolution
   * @param inFilter
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public BOWConceptMapCalculator(BOWConceptMapSolution theSolution, FilterType inFilter) {
    super(theSolution, inFilter);
  }


  /**
   * ************************************************************** calcBestConcept - Calculates the
   * best concept to match a particular phrase. Note that concept matching using bag of words is
   * really only set up to or works with smoothing. Without smoothing, the calculations can be quite
   * long because there can be cases for which the number of occurrences of a concept and the number
   * of times an attribute is associated with the concept are equal. For such a case, the log
   * becomes -Infinity, so we have to work around this possibility.
   *
   * @param theAttributes
   * @return String array indicating 1) the concept that produces the highest likelihood; 2) the
   *         number of concepts tested; and 3) the type of smoothing used
   * @throws AttributeDBMismatchException String
   *         **************************************************************
   */
  @Override
  public String[] calcBestConcept(String[] theAttributes, HashSet<String> acceptableConcepts) {
    // Now get maximum likelihood concept
    if (this.filter == FilterType.STANDARD) {
      // Set smoothing to determine how best to select potential concepts
      // (Smoothing allows for all potential concepts; no smoothing means
      // we can only use concepts that occur with all attribute values)
      boolean smoothed = false;
      Hashtable<String, Boolean> potentialConcepts =
          getPotentialConcepts(theAttributes, acceptableConcepts, smoothed);
      if (potentialConcepts.size() > 0) {
        Enumeration<String> testConcepts = potentialConcepts.keys();
        this.resultString[0] = bestConceptNoSmooth(theAttributes, testConcepts);
        this.resultString[1] = Integer.toString(potentialConcepts.size());
        this.resultString[2] = "No Smoothing Needed";
        return this.resultString;
      } else {
        smoothed = true;
        potentialConcepts = getPotentialConcepts(theAttributes, acceptableConcepts, smoothed);
        this.resultString[0] = bestConceptSmoothed(theAttributes, potentialConcepts.keys());
        this.resultString[1] = Integer.toString(potentialConcepts.size());
        this.resultString[2] = "Smoothing Required";
        return this.resultString;
      }

    } else if (this.filter == FilterType.MODIFIED) {
      // Set smoothing to determine how best to select potential concepts
      // (No smoothing means we can only use concepts that occur with all
      // attribute
      // values)
      boolean smoothed = true;
      Hashtable<String, Boolean> potentialConcepts =
          getPotentialConcepts(theAttributes, acceptableConcepts, smoothed);
      Enumeration<String> testConcepts = potentialConcepts.keys();
      this.resultString[0] = bestConceptMod(theAttributes, testConcepts);
      this.resultString[1] = Integer.toString(potentialConcepts.size());
      this.resultString[2] = "Modified Smoothing Used";
      return this.resultString;
    } else {
      boolean smoothed = true;
      Hashtable<String, Boolean> potentialConcepts =
          getPotentialConcepts(theAttributes, acceptableConcepts, smoothed);
      Enumeration<String> testConcepts = potentialConcepts.keys();
      this.resultString[0] = bestConceptSmoothed(theAttributes, testConcepts);
      this.resultString[1] = Integer.toString(potentialConcepts.size());
      this.resultString[2] = "Smoothing Used";
      return this.resultString;
    }
  }


  /**
   * ************************************************************** Determine best concept (maximal
   * valued concept) for the set of attributes provided using a modified smoothing method where only
   * the numerator has values added to account for attributes that are absent from the training
   * data.
   *
   * @param attributes - string array of the attribues
   * @param testConcepts - Enumeration of the concepts under consideration
   * @return - String representing concept with the maximum likelihood value among a set of tested
   *         concepts **************************************************************
   */
  private String bestConceptMod(String[] attributes, Enumeration<String> testConcepts) {
    String curMaxConcept = RaptatConstants.NULL_ELEMENT;
    double curMaxValue = 0;
    OccurProbGroup curPriorValues;
    OccurProbTriplet curCondValues;
    int attLength = attributes.length;

    // First set the maximum concept value and the associated
    // concept to the first of the potential values.
    if (testConcepts.hasMoreElements()) {
      curMaxConcept = testConcepts.nextElement();
      curPriorValues = this.priors.get(curMaxConcept);
      curMaxValue = curPriorValues.unsmoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        curCondValues = this.conditionals[0].getValue(attributes[i], curMaxConcept);

        // If curCondValues is null, it means that this concept was not
        // associated
        // with this concept. Because of smoothing, we produce Log (
        // Occurrences
        // of Attribute with Concept + 1) or Log(1), which is zero. So,
        // as a result
        // we just don't add anything at all
        if (curCondValues != null) {
          curMaxValue += curCondValues.unsmoothedProb;
        }
      }
    }

    // Now find the maximum concept value among the other
    // candidates
    while (testConcepts.hasMoreElements()) {
      String testConcept = testConcepts.nextElement();
      double testValue = this.priors.get(testConcept).unsmoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        curCondValues = this.conditionals[0].getValue(attributes[i], testConcept);

        /*
         * If curCondValues is null, it means that this concept was not associated with this
         * concept. Because of smoothing, we produce Log ( Occurrences of Attribute with Concept +
         * 1) or Log(1), which is zero. So, as a result we just don't add anything at all
         */
        if (curCondValues != null) {
          testValue += curCondValues.smoothedProb;
        }
      }

      if (testValue > curMaxValue) {
        curMaxValue = testValue;
        curMaxConcept = testConcept;
      }
    }

    return curMaxConcept;
  }


  /**
   * ************************************************************** bestConceptNoSmooth - Find the
   * best concept for a set of attributes using no smoothing to account of zero occurrences. This
   * should only be called if the set of attributes all match to at least
   *
   * @param attributes
   * @param testConcepts
   * @return String **************************************************************
   */
  private String bestConceptNoSmooth(String[] attributes, Enumeration<String> testConcepts) {

    String curMaxConcept = RaptatConstants.NULL_ELEMENT;
    double curMaxValue = -Double.MAX_VALUE;
    int attLength = attributes.length;

    // Find the maximum concept value among the candidates
    while (testConcepts.hasMoreElements()) {
      String testConcept = testConcepts.nextElement();
      double testValue = this.priors.get(testConcept).unsmoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        testValue += this.conditionals[0].getValue(attributes[i], testConcept).unsmoothedProb;
      }

      if (testValue > curMaxValue) {
        curMaxValue = testValue;
        curMaxConcept = testConcept;
      }
    }

    return curMaxConcept;
  }


  /**
   * ************************************************************** bestConceptSmoothed - Determine
   * best concept (maximal valued concept) for the set of attributes provided using standard LaPlace
   * smoothing.
   *
   * @param attributes
   * @param testConcepts void **************************************************************
   */
  private String bestConceptSmoothed(String[] attributes, Enumeration<String> testConcepts) {
    OccurProbTriplet curCondValues;
    String curMaxConcept = RaptatConstants.NULL_ELEMENT;
    double curMaxValue = -Double.MAX_VALUE;
    int attLength = attributes.length;

    // Find the maximum concept value among the candidates
    while (testConcepts.hasMoreElements()) {
      String testConcept = testConcepts.nextElement();
      double testValue = this.priors.get(testConcept).smoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        curCondValues = this.conditionals[0].getValue(attributes[i], testConcept);
        if (curCondValues != null) {
          testValue += curCondValues.smoothedProb;
        }
      }

      if (testValue > curMaxValue) {
        curMaxValue = testValue;
        curMaxConcept = testConcept;
      }
    }

    return curMaxConcept;
  }


  /**
   * ************************************************************** For each attribute, find the
   * associated, potential concepts and put them into a table
   *
   * @param attributes - Attributes to be mapped to a concept
   * @param smoothing
   * @return Potential concepts or none if none found
   *         **************************************************************
   */
  private Hashtable<String, Boolean> getPotentialConcepts(String[] attributes,
      HashSet<String> acceptableConcepts, boolean smoothing) {
    Hashtable<String, Boolean> potentialConcepts = new Hashtable<>(this.priors.size()); // Size
    // should
    // be
    // <=
    // size
    // of
    // table
    // of
    // priors

    if (smoothing) {
      for (int i = 0; i < attributes.length; i++) {
        // Only add concepts for attribute values (keys)
        // that exist
        if (this.conditionals[0].containsKey(attributes[i])) {
          Enumeration<String> mappedConcepts = this.conditionals[0].get(attributes[i]).keys();
          while (mappedConcepts.hasMoreElements()) {
            String curConcept = mappedConcepts.nextElement();
            if (acceptableConcepts == null || acceptableConcepts.contains(curConcept)) {
              potentialConcepts.put(curConcept, true);
            }
          }
        }
      }
    } else {
      // If not smoothing, only add concepts for attribute values (keys)
      // that exist across all the attributes

      // Get elements in first attribute and check
      // whether each is a concept associated with all
      // other attributes
      if (this.conditionals[0].containsKey(attributes[0])) {
        Enumeration<String> mappedConcepts = this.conditionals[0].get(attributes[0]).keys();
        while (mappedConcepts.hasMoreElements()) {
          int startIndex = 1;
          int endIndex = attributes.length - 1;
          String curConcept = mappedConcepts.nextElement();
          if (acceptableConcepts == null || acceptableConcepts.contains(curConcept)) {
            if (inConditionalSet(attributes, curConcept, startIndex, endIndex)) {
              potentialConcepts.put(curConcept, true);
            }
          }
        }
      }
    }
    return potentialConcepts;
  }


  /**
   * ************************************************************** Recursive method to determine if
   * all conditional tables within an array have a particular item as a key.
   *
   * @param attributes
   * @param checkItem - Item to see if it exists as a key
   * @param startIndex - where to start checking within the array of conditionals
   * @param endIndex
   * @return boolean **************************************************************
   */
  private boolean inConditionalSet(String[] attributes, String checkItem, int startIndex,
      int endIndex) {
    if (startIndex > endIndex || startIndex >= attributes.length) {
      return true;
    }
    if (this.conditionals[0].containsKey(attributes[startIndex])) {
      if (this.conditionals[0].get(attributes[startIndex]).containsKey(checkItem)) {
        return inConditionalSet(attributes, checkItem, startIndex + 1, endIndex);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
