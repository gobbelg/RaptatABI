package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.Hashtable;

/**
 * ********************************************************** SmoothTable -
 *
 * @author Glenn Gobbel, Jul 22, 2010
 *         <p>
 *         *********************************************************
 */
public class SmoothTable extends Hashtable<String, int[]> {
  private static final long serialVersionUID = -7314261689046791892L;


  public SmoothTable() {
    super(10);
  }


  public SmoothTable(int startCapacity) {
    super(startCapacity);
  }


  /**
   * **************************************************************
   *
   * @param smoothType
   * @return int[] **************************************************************
   */
  public int[] getAllMatchValues(String smoothType) {
    return get(smoothType);
  }


  /**
   * **************************************************************
   *
   * @param smoothType
   * @return int **************************************************************
   */
  public int getFalseNegatives(String smoothType) {
    return get(smoothType)[2];
  }


  /**
   * **************************************************************
   *
   * @param smoothType
   * @return int **************************************************************
   */
  public int getFalsePositives(String smoothType) {
    return get(smoothType)[1];
  }


  /**
   * **************************************************************
   *
   * @param smoothType
   * @return int **************************************************************
   */
  public int getTruePositives(String smoothType) {
    return get(smoothType)[0];
  }


  /**
   * **************************************************************
   *
   * @param smoothType void **************************************************************
   */
  public void incrementFalseNegatives(String smoothType) {
    if (containsKey(smoothType)) {
      get(smoothType)[2]++;
    } else {
      int[] tempInts = {0, 0, 1};
      put(smoothType, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param smoothType void **************************************************************
   */
  public void incrementFalsePositives(String smoothType) {
    if (containsKey(smoothType)) {
      get(smoothType)[1]++;
    } else {
      int[] tempInts = {0, 1, 0};
      put(smoothType, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param smoothType void **************************************************************
   */
  public void incrementTruePositives(String smoothType) {
    if (containsKey(smoothType)) {
      get(smoothType)[0]++;
    } else {
      int[] tempInts = {1, 0, 0};
      put(smoothType, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param smoothType
   * @param truePositives
   * @param falsePositives
   * @param falseNegatives void **************************************************************
   */
  public void setConcept(String smoothType, int truePositives, int falsePositives,
      int falseNegatives) {
    int[] tempInts = {truePositives, falsePositives, falseNegatives};
    put(smoothType, tempInts);
  }


  /**
   * ************************************************************** Set the number of correct
   * matches, incorrect matches, and unmatched phrases associated with a concept
   *
   * @param smoothType
   * @param theMatches void **************************************************************
   */
  public void setConcept(String smoothType, int[] theMatches) {
    put(smoothType, theMatches);
  }
}
