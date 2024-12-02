package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * ********************************************************** Class to create a table for priors
 * whereby supplying the key for a concept returns a group of numbers, one representing the number
 * of occurrences of the concept, the others being arrays representing the unsmoothed and smoothed
 * likelihood associated with that key. The arrays returned correspond to the potential number of
 * attributes in a phrase. If the phrase is m in length, one looks at the value at index [m-1] in
 * the array to determine the associated likelihood for a phrase of that length.
 *
 * @author Glenn T. Gobbel, Jul 5, 2010 *********************************************************
 */
public class NonNBPriorTable extends Hashtable<String, NonNBOccurProbSet> implements Serializable {
  // Allow object of this class to be serializable
  private static final long serialVersionUID = -2052945451685440856L;


  /**
   * *************************************************************** Creates a one-way occurrence
   * table based on a hashtable. Providing a key returns a pair containing the number of occurrences
   * and the likelihood associated with those occurrences
   *
   * @param startCapacity - Starting size of hashtable holding occurrences
   *        ***************************************************************
   */
  public NonNBPriorTable(int startCapacity) {
    super(startCapacity);
  }


  /**
   * ************************************************************** Increment a given prior.
   * Determine whether to increment smoothing parameter by 1 or 2 based on whether the phrase
   * associated with the parameter has ever been seen before in the ConditionalTree for calculating
   * Non-naive Bayes likelihood.
   *
   * @param theConcept
   * @param newPhrase **************************************************************
   */
  public void increment(String aKey, boolean newPhrase) {
    NonNBOccurProbSet curOccurProbSet;

    if (containsKey(aKey)) {
      curOccurProbSet = get(aKey);
    } else {
      curOccurProbSet = new NonNBOccurProbSet(0, 0);
      put(aKey, curOccurProbSet);
    }
    curOccurProbSet.incrementOccurrences(newPhrase);
  }
}
