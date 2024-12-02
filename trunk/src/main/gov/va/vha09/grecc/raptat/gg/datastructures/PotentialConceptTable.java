package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** PotentialConceptTable -
 *
 * @author Glenn Gobbel, Jul 22, 2010
 *         <p>
 *         *********************************************************
 */
public class PotentialConceptTable extends Hashtable<String, int[]> {
  private static final long serialVersionUID = -7314261689046791892L;


  public PotentialConceptTable() {
    super(RaptatConstants.NB_CONCEPT_TABLE_SIZE / 4);
  }


  public PotentialConceptTable(int startCapacity) {
    super(startCapacity);
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts
   * @return int[] **************************************************************
   */
  public int[] getAllMatchValues(String numOfConcepts) {
    return get(numOfConcepts);
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts
   * @return int **************************************************************
   */
  public int getCorrectMatches(String numOfConcepts) {
    return get(numOfConcepts)[0];
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts
   * @return int **************************************************************
   */
  public int getIncorrectMatches(String numOfConcepts) {
    return get(numOfConcepts)[1];
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts
   * @return int **************************************************************
   */
  public int getUnmatched(String numOfConcepts) {
    return get(numOfConcepts)[2];
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts void **************************************************************
   */
  public void incrementCorrect(String numOfConcepts) {
    if (containsKey(numOfConcepts)) {
      get(numOfConcepts)[0]++;
    } else {
      int[] tempInts = {1, 0, 0};
      put(numOfConcepts, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts void **************************************************************
   */
  public void incrementIncorrect(String numOfConcepts) {
    if (containsKey(numOfConcepts)) {
      get(numOfConcepts)[1]++;
    } else {
      int[] tempInts = {0, 1, 0};
      put(numOfConcepts, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts void **************************************************************
   */
  public void incrementUnmatched(String numOfConcepts) {
    if (containsKey(numOfConcepts)) {
      get(numOfConcepts)[2]++;
    } else {
      int[] tempInts = {0, 0, 1};
      put(numOfConcepts, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param numOfConcepts
   * @param correctMatches
   * @param incorrectMatches
   * @param unmatched void **************************************************************
   */
  public void setConcept(String numOfConcepts, int correctMatches, int incorrectMatches,
      int unmatched) {
    int[] tempInts = {correctMatches, incorrectMatches, unmatched};
    put(numOfConcepts, tempInts);
  }


  /**
   * ************************************************************** Set the number of correct
   * matches, incorrect matches, and unmatched phrases associated with a concept
   *
   * @param numOfConcepts
   * @param theMatches void **************************************************************
   */
  public void setConcept(String numOfConcepts, int[] theMatches) {
    put(numOfConcepts, theMatches);
  }


  public String toString(String theKey) {
    int[] theValues = get(theKey);

    String[] stringValues = GeneralHelper.intToStringArray(theValues);
    return theKey + "," + GeneralHelper.arrayToDelimString(stringValues, ",");
  }
}
