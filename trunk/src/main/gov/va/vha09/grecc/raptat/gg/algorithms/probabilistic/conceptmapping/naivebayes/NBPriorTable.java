package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbGroup;

/*
 * Class to create a table for priors whereby supplying the key for a concept returns a group of
 * numbers, one representing the number of occurrences of the concept, the others being arrays of
 * the unsmoothed and smoothed likelihood associated with that key. The arrays returned correspond
 * to the potential number of attributes in a phrase. If the phrase is m in length, one looks at the
 * value at index [m-1] in the array to determine the associated likelihood for a phrase of that
 * length.
 *
 * @author Glenn T. Gobbel, Jul 5, 2010
 */
public class NBPriorTable extends HashMap<String, OccurProbGroup> implements Serializable {
  /* Allow object of this class to be serializable */
  private static final long serialVersionUID = -2052945451685440856L;

  /*
   * This corresponds to the number of attributes so we have a different likelihood for each
   * possible phrase length
   */
  protected int maxTokens;

  /*
   * Keeps track of all concepts for which the number of occurrences has changed since the last
   * update to the associated OccurProbGroup instance that the concept maps to
   */
  protected HashSet<String> modifiedConcepts;


  /**
   * *************************************************************** Creates a one-way occurrence
   * table based on a hashtable. Providing a key returns a pair containing the number of occurrences
   * and the likelihood associated with those occurrences
   *
   * @param startCapacity - Starting size of hashtable holding occurrences
   * @param maxTokens - Integer representing number of attribute positions
   *        ***************************************************************
   */
  public NBPriorTable(int startCapacity, int maxTokens) {
    super(startCapacity);
    this.maxTokens = maxTokens;
    this.modifiedConcepts = new HashSet<>(startCapacity);
  }


  /**
   * ********************************************************** Calculate the likelihood based on
   * the occurrences within the table
   *
   * @param uniqueTokenStrings - Number of unique token strings for each position in a phrase. There
   *        is only one for a bag of words implementation.
   *        *********************************************************
   */
  public void calcAllLikelihoods(int[] uniqueTokenStrings) {
    /*
     * Run through all the concepts and calculate values used for likelihood calculations
     */
    Iterator<String> conceptKeys = keySet().iterator();
    while (conceptKeys.hasNext()) {
      OccurProbGroup curGroup = get(conceptKeys.next());
      int conceptOccurrences = curGroup.occurrences;
      double startProb = Math.log10(conceptOccurrences);

      /*
       * Numerator of prior and denominator of conditional cancel out if only one token string in a
       * token phrase, and log10 of 1.0 is zero.
       */
      curGroup.unsmoothedProbs[0] = 0;
      curGroup.smoothedProbs[0] =
          startProb - Math.log10(conceptOccurrences + uniqueTokenStrings[0]);

      int priorPositions = curGroup.smoothedProbs.length;

      for (int i = 1; i < priorPositions; i++) {
        curGroup.unsmoothedProbs[i] = curGroup.unsmoothedProbs[i - 1] - startProb;

        /*
         * Check to see if this is a bag of words implementation where there is only one item in the
         * int [], uniqueTokenStrings versus a non-bag of words.
         */
        if (uniqueTokenStrings.length == 1) {
          curGroup.smoothedProbs[i] = curGroup.smoothedProbs[i - 1]
              - Math.log10(conceptOccurrences + uniqueTokenStrings[0]);
        } else {
          curGroup.smoothedProbs[i] = curGroup.smoothedProbs[i - 1]
              - Math.log10(conceptOccurrences + uniqueTokenStrings[i]);
        }
      }

      curGroup.probabilitiesUpdated = true;
    }
    this.modifiedConcepts.clear();
  }


  /**
   * ************************************************************** Calculate values for calculating
   * likelihood based on concepts that have been modified since the last calculation
   *
   * @param uniqueTokens - int [] of vocabulary size at each token position, and there is only one
   *        position for bag of words model
   * @param attributePositions
   * @param modifiedConcepts - Hashtable containing concepts modified since laste update
   *        **************************************************************
   */
  public void calcModifiedLikelihoods(int[] uniqueTokens) {
    /*
     * Now run through all modified concepts and calculate values used for likelihood
     */
    Iterator<String> e1 = this.modifiedConcepts.iterator();
    while (e1.hasNext()) {
      String curConcept = e1.next();
      OccurProbGroup curGroup = get(curConcept);
      int elementOccs = curGroup.occurrences;
      double startProb = Math.log10(elementOccs);
      curGroup.unsmoothedProbs[0] = 0;

      /*
       * Numerator of prior and denominator of conditional cancel out
       */
      curGroup.smoothedProbs[0] = startProb - Math.log10(elementOccs + uniqueTokens[0]);

      int priorPositions = curGroup.smoothedProbs.length;
      for (int i = 1; i < priorPositions; i++) {
        curGroup.unsmoothedProbs[i] = curGroup.unsmoothedProbs[i - 1] - startProb;
        if (uniqueTokens.length == 1) {
          curGroup.smoothedProbs[i] =
              curGroup.smoothedProbs[i - 1] - Math.log10(elementOccs + uniqueTokens[0]);
        } else {
          curGroup.smoothedProbs[i] =
              curGroup.smoothedProbs[i - 1] - Math.log10(elementOccs + uniqueTokens[i]);
        }
      }

      curGroup.probabilitiesUpdated = true;
    }
    this.modifiedConcepts.clear();
  }


  /**
   * ************************************************************** Adds one to the integer within
   * the pair hashed by aKey
   *
   * @param aKey - Key indicating value to increment
   *        **************************************************************
   */
  public void increment(String aKey) {
    this.modifiedConcepts.add(aKey);
    if (containsKey(aKey)) {
      OccurProbGroup curSet = get(aKey);
      curSet.occurrences++;
      curSet.probabilitiesUpdated = false;
    } else {
      put(aKey, new OccurProbGroup(1, new double[this.maxTokens], new double[this.maxTokens]));
    }
  }


  public void resetModifications() {
    this.modifiedConcepts.clear();
  }


  @Override
  public String toString() {
    String resString = "{";
    String curKey;
    OccurProbGroup curProbGroup;

    Iterator<String> probGroupKeys = keySet().iterator();

    while (probGroupKeys.hasNext()) {
      curKey = probGroupKeys.next();
      curProbGroup = get(curKey);
      resString += "{" + curKey + " => " + curProbGroup.toString() + "}";
    }

    return resString + "}";
  }
}
