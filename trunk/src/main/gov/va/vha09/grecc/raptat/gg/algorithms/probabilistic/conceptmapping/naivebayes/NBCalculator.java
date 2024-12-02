package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapCalculator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.FilterType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbTriplet;

/**
 * NaiveBayesCalculator - Calculates the most likely concept based on a NaiveBayesDB (database) and
 * an array of attributes
 *
 * @author Glenn Gobbel, Jul 12, 2010
 */
public class NBCalculator extends ConceptMapCalculator {
  protected NBPriorTable priors;
  protected NBConditionalTable[] conditionals;

  /*
   * FilterType indicates how data is filtered to handle test data elements that never occur in the
   * training data
   */
  protected FilterType filter;
  protected String[] resultString;


  public NBCalculator(NBPriorTable thePriors, NBConditionalTable[] conditionals,
      FilterType inFilter) {
    this.priors = thePriors;
    this.conditionals = conditionals;
    this.filter = inFilter;
    this.resultString = new String[3];
  }


  public NBCalculator(NBSolution theSolution, FilterType inFilter) {
    this.priors = theSolution.getPriors();
    this.conditionals = theSolution.getConditionals();
    this.filter = inFilter;
    this.resultString = new String[3];
  }


  /**
   * ************************************************************** calcBestConcept - Calculates the
   * best concept to match a particular phrase
   *
   * @param theAttributes
   * @return String array indicating 1) the concept that produces the highest likelihood; 2) the
   *         number of concepts tested; and 3) the type of smoothing used
   * @throws AttributeDBMismatchException String
   *         **************************************************************
   */
  @Override
  public String[] calcBestConcept(String[] tokenStrings, HashSet<String> acceptableConcepts) {

    int factors = this.conditionals.length;

    // Now get maximum likelihood concept
    if (this.filter == FilterType.STANDARD) {
      /*
       * Set smoothing to determine how best to select potential concepts (Smoothing allows for all
       * potential concepts; no smoothing means we can only use concepts that occur with all
       * attribute values)
       */
      boolean smoothed = false;
      HashSet<String> potentialConcepts =
          getPotentialConcepts(tokenStrings, acceptableConcepts, smoothed);
      if (potentialConcepts.size() > 0) {
        this.resultString[0] = bestConceptNoSmooth(tokenStrings, potentialConcepts.iterator());
        this.resultString[1] = Integer.toString(potentialConcepts.size());
        this.resultString[2] = "No Smoothing Needed";
        return this.resultString;
      } else {
        smoothed = true;
        potentialConcepts = getPotentialConcepts(tokenStrings, acceptableConcepts, smoothed);
        this.resultString[0] = bestConceptSmoothed(tokenStrings, potentialConcepts.iterator());
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
      HashSet<String> potentialConcepts =
          getPotentialConcepts(tokenStrings, acceptableConcepts, smoothed);
      this.resultString[0] = bestConceptMod(tokenStrings, potentialConcepts.iterator());
      this.resultString[1] = Integer.toString(potentialConcepts.size());
      this.resultString[2] = "Modified Smoothing Used";
      return this.resultString;
    } else {
      // Set smoothing to determine how best to select potential concepts
      // (No smoothing means we can only use concepts that occur with all
      // attribute
      // values)
      boolean smoothed = true;
      HashSet<String> potentialConcepts =
          getPotentialConcepts(tokenStrings, acceptableConcepts, smoothed);
      this.resultString[0] = bestConceptSmoothed(tokenStrings, potentialConcepts.iterator());
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
  private String bestConceptMod(String[] attributes, Iterator<String> testConcepts) {
    String curMaxConcept = RaptatConstants.NULL_ELEMENT;
    double curMaxValue = Double.NEGATIVE_INFINITY;
    OccurProbGroup curPriorValues;
    OccurProbTriplet curCondValues;
    int attLength = attributes.length;

    // First set the maximum concept value and the associated
    // concept to the first of the potential values.
    if (testConcepts.hasNext()) {
      curMaxConcept = testConcepts.next();
      curPriorValues = this.priors.get(curMaxConcept);
      curMaxValue = curPriorValues.unsmoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        curCondValues = this.conditionals[i].getValue(attributes[i], curMaxConcept);

        /*
         * If curCondValues is null, it means that this concept was not associated with this
         * concept. Because of smoothing, we produce Log ( Occurrences of Attribute with Concept +
         * 1) or Log(1), which is zero. So, as a result we just don't add anything at all
         */
        if (curCondValues != null) {
          curMaxValue += curCondValues.smoothedProb;
        }
      }
    }

    // Now find the maximum concept value among the other
    // candidates
    while (testConcepts.hasNext()) {
      String testConcept = testConcepts.next();
      double testValue = this.priors.get(testConcept).unsmoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        curCondValues = this.conditionals[i].getValue(attributes[i], testConcept);

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
   * should only be called if the set of attributes all match to all potential concepts.
   *
   * @param attributes
   * @param testConcepts
   * @return String **************************************************************
   */
  private String bestConceptNoSmooth(String[] attributes, Iterator<String> testConcepts) {

    String curMaxConcept = RaptatConstants.NULL_ELEMENT;
    double curMaxValue = 0;
    int attLength = attributes.length;

    // First set the maximum concept value and the associated
    // concept to the first of the potential values. There must
    // be *some* value for every concept.
    if (testConcepts.hasNext()) {
      curMaxConcept = testConcepts.next();

      // We use attLength - 1 because of zero indexing
      curMaxValue = this.priors.get(curMaxConcept).unsmoothedProbs[attLength - 1];

      for (int i = 0; i < attributes.length; i++) {
        curMaxValue += this.conditionals[i].getValue(attributes[i], curMaxConcept).unsmoothedProb;
      }
    }

    // Now find the maximum concept value among the other
    // candidates
    while (testConcepts.hasNext()) {
      String testConcept = testConcepts.next();
      double testValue = this.priors.get(testConcept).unsmoothedProbs[attLength - 1];
      for (int i = 0; i < attributes.length; i++) {
        // Cannot be null as testConcepts only contains concepts
        // associated
        // with all the attributes if there is no smoothing (see
        // getPotentialConcepts)
        assert this.conditionals[i].getValue(attributes[i], testConcept) != null;

        testValue += this.conditionals[i].getValue(attributes[i], testConcept).unsmoothedProb;
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
   * best concept (maximal valued concept) for the set of token strings provided using standard
   * LaPlace smoothing.
   *
   * @param tokenStrings
   * @param testConcepts void **************************************************************
   */
  private String bestConceptSmoothed(String[] tokenStrings, Iterator<String> testConcepts) {

    OccurProbTriplet curCondValues;
    String curMaxConcept = RaptatConstants.NULL_ELEMENT;
    double curMaxValue = Double.NEGATIVE_INFINITY;
    int attLength = tokenStrings.length;

    /* Find the maximum concept value among the candidates */
    while (testConcepts.hasNext()) {
      String testConcept = testConcepts.next();
      double testValue = this.priors.get(testConcept).smoothedProbs[attLength - 1];
      for (int i = 0; i < tokenStrings.length; i++) {
        curCondValues = this.conditionals[i].getValue(tokenStrings[i], testConcept);

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
   * ************************************************************** For each attribute, find the
   * associated, potential concepts and put them into a table
   *
   * @param tokenStrings - Token strings to be mapped to a concept
   * @param acceptableConcepts
   * @param smoothing
   * @return Potential concepts or none if none found
   *         **************************************************************
   */
  private HashSet<String> getPotentialConcepts(String[] tokenStrings,
      HashSet<String> acceptableConcepts, boolean smoothing) {
    /* Size should be <= size of table of priors */
    HashSet<String> potentialConcepts = new HashSet<>(this.priors.size());

    if (smoothing) {
      for (int i = 0; i < tokenStrings.length; i++) {
        /*
         * Only add concepts for token string values (keys)that map to a concept
         */
        if (this.conditionals[i].containsKey(tokenStrings[i])) {
          Enumeration<String> mappedConcepts = this.conditionals[i].get(tokenStrings[i]).keys();
          while (mappedConcepts.hasMoreElements()) {
            String nextConcept = mappedConcepts.nextElement();
            if (acceptableConcepts == null || acceptableConcepts.contains(nextConcept)) {
              potentialConcepts.add(nextConcept);
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
      if (this.conditionals[0].containsKey(tokenStrings[0])) {
        Enumeration<String> mappedConcepts = this.conditionals[0].get(tokenStrings[0]).keys();
        while (mappedConcepts.hasMoreElements()) {
          int startIndex = 1;
          int endIndex = tokenStrings.length - 1;
          String curConcept = mappedConcepts.nextElement();
          if (acceptableConcepts == null || acceptableConcepts.contains(curConcept)) {
            if (inConditionalSet(tokenStrings, curConcept, startIndex, endIndex)) {
              potentialConcepts.add(curConcept);
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
    if (startIndex > endIndex || startIndex >= this.conditionals.length) {
      return true;
    }
    if (this.conditionals[startIndex].containsKey(attributes[startIndex])) {
      if (this.conditionals[startIndex].get(attributes[startIndex]).containsKey(checkItem)) {
        return inConditionalSet(attributes, checkItem, startIndex + 1, endIndex);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
