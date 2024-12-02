package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** MatchesTable - Keeps track of matches
 * (true positives, false positives, false negatives) with respect to any key (such as a given
 * concept or number of potential concepts)
 *
 * @author Glenn Gobbel, Jul 22, 2010 *********************************************************
 */
public class MatchTable extends Hashtable<String, int[]> {
  private static final long serialVersionUID = -7314261689046791892L;


  public MatchTable() {
    super();
  }


  public MatchTable(int startCapacity) {
    super(startCapacity);
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return int[] **************************************************************
   */
  public int[] getAllMatchValues(String theKey) {
    return get(theKey);
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return int **************************************************************
   */
  public int getFalseNegatives(String theKey) {
    return get(theKey)[2];
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return int **************************************************************
   */
  public int getFalsePositives(String theKey) {
    return get(theKey)[1];
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return int **************************************************************
   */
  public int getTruePositives(String theKey) {
    return get(theKey)[0];
  }


  /**
   * **************************************************************
   *
   * @param theKey void **************************************************************
   */
  public void incrementFalseNegatives(String theKey) {
    if (containsKey(theKey)) {
      get(theKey)[2]++;
    } else {
      int[] tempInts = {0, 0, 1};
      put(theKey, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return void **************************************************************
   */
  public void incrementFalsePositives(String theKey) {
    if (containsKey(theKey)) {
      get(theKey)[1]++;
    } else {
      int[] tempInts = {0, 1, 0};
      put(theKey, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return void **************************************************************
   */
  public void incrementTruePositives(String theKey) {
    if (containsKey(theKey)) {
      get(theKey)[0]++;
    } else {
      int[] tempInts = {1, 0, 0};
      put(theKey, tempInts);
    }
  }


  /**
   * ************************************************************** setConcept
   *
   * @param theKey
   * @param truePositives
   * @param falsePositives
   * @param falseNegatives void **************************************************************
   */
  public void setConcept(String theKey, int truePositives, int falsePositives, int falseNegatives) {
    int[] tempInts = {truePositives, falsePositives, falseNegatives};
    put(theKey, tempInts);
  }


  /**
   * ************************************************************** Set the number of correct
   * matches, incorrect matches, and unmatched phrases associated with the given key
   *
   * @param theKey
   * @param theMatches void **************************************************************
   */
  public void setConcept(String theKey, int[] theMatches) {
    put(theKey, theMatches);
  }


  public String toString(String theKey) {
    int[] theValues = get(theKey);

    String[] stringValues = GeneralHelper.intToStringArray(theValues);
    return theKey + "," + GeneralHelper.arrayToDelimString(stringValues, ",");
  }
}
