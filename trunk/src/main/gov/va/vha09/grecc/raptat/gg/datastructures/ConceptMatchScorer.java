package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import org.apache.commons.text.RandomStringGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScoreObject;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** ConceptMatchScorer - Extends a
 * Hashtable<String,int[]> data structure. Keeps track of matches (true positives, false positives,
 * false negatives) with respect to any key (such as a given concept or number of potential
 * concepts).
 *
 * @author Glenn Gobbel, Dec 13, 2011 *********************************************************
 */
public class ConceptMatchScorer extends Hashtable<PerformanceScoreObject, int[]> {
  private static final long serialVersionUID = -7314261689046791892L;


  public ConceptMatchScorer() {
    super();
  }


  public ConceptMatchScorer(Hashtable<PerformanceScoreObject, int[]> theHash) {
    super(theHash);
  }


  public ConceptMatchScorer(int startCapacity) {
    super(startCapacity);
  }


  public int getFalseNegatives(PerformanceScoreObject theKey) {
    return get(theKey)[2];
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


  public int getFalsePositives(PerformanceScoreObject theKey) {
    return get(theKey)[1];
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


  public double[] getPerformanceScores(PerformanceScoreObject psoKey) {
    double[] results = new double[3];
    int tp, fp, fn;

    tp = this.getTruePositives(psoKey);
    fp = this.getFalsePositives(psoKey);
    fn = this.getFalseNegatives(psoKey);
    results[0] = GeneralHelper.calcPrecision(tp, fp);
    results[1] = GeneralHelper.calcRecall(tp, fn);
    results[2] = GeneralHelper.calcF_Measure(tp, fp, fn);

    return results;
  }


  /**
   * ************************************************* getPerformanceValues - Dec 13, 2011 Return
   * precision, recall, and f-measure as a double[] based on current settings of true positives,
   * false positives, and false negatives associated with theKey
   *
   * @param theKey
   * @return double[] of precision, recall, and f-measures
   *         *************************************************
   */
  public double[] getPerformanceScores(String theKey) {
    return this.getPerformanceScores(new PerformanceScoreObject(theKey));
  }


  public int getTruePositives(PerformanceScoreObject theKey) {
    return get(theKey)[0];
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
    PerformanceScoreObject psoKey = new PerformanceScoreObject(theKey);
    if (containsKey(psoKey)) {
      get(psoKey)[2]++;
    } else {
      int[] tempInts = {0, 0, 1};
      put(psoKey, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @param theKey
   * @return void **************************************************************
   */
  public void incrementFalsePositives(String theKey) {
    PerformanceScoreObject psoKey = new PerformanceScoreObject(theKey);
    if (containsKey(psoKey)) {
      get(psoKey)[1]++;
    } else {
      int[] tempInts = {0, 1, 0};
      put(psoKey, tempInts);
    }
  }


  // TODO Remove unused code found by UCDetector
  // public String toString(String theKey)
  // {
  // int[] theValues = get(theKey);
  //
  // String[] stringValues = GeneralHelper.intToStringArray(theValues);
  // return theKey + ","
  // + GeneralHelper.arrayToDelimString(stringValues, ",");
  // }

  /**
   * **************************************************************
   *
   * @param theKey
   * @return void **************************************************************
   */
  public void incrementTruePositives(String theKey) {
    PerformanceScoreObject psoKey = new PerformanceScoreObject(theKey);

    if (containsKey(psoKey)) {
      get(psoKey)[0]++;
    } else {
      int[] tempInts = {1, 0, 0};
      put(psoKey, tempInts);
    }
  }


  /**
   * ************************************************* update - Dec 13, 2011 Updates the number of
   * true positives, false positives and true negatives associated with a concept
   *
   * @param resultConceptIndex
   * @param expectedConceptIndex
   * @param curData
   * @param expectedConceptIndex
   * @param resultConceptIndex
   * @return int [] of 3 elements containing the true positives, false positives, and true negatives
   *         found in the data *************************************************
   */
  public void update(List<String[]> theResults, int expectedConceptIndex, int resultConceptIndex) {

    for (String[] curResult : theResults) {
      String resultConcept = curResult[resultConceptIndex];
      String expectedConcept = curResult[expectedConceptIndex];
      if (resultConcept.equals(expectedConcept)) {
        incrementTruePositives(expectedConcept);
      } else {
        incrementFalseNegatives(expectedConcept);
        if (!resultConcept.equalsIgnoreCase(RaptatConstants.NULL_ELEMENT)) {
          incrementFalsePositives(resultConcept);
        }
      }
    }
  }


  /**
   * ************************************************* update - Dec 13, 2011 Updates the number of
   * true positives, false positives and true negatives associated with a concept
   *
   * @param curData
   * @param expectedConceptIndex
   * @param resultConceptIndex
   * @return int [] of 3 elements containing the true positives, false positives, and true negatives
   *         found in the data *************************************************
   */
  public int[] update(String expectedConcept, String resultConcept) {
    int[] results = {0, 0, 0};

    if (resultConcept.equalsIgnoreCase(expectedConcept)) {
      incrementTruePositives(expectedConcept);
      results[0]++;

    } else {
      incrementFalseNegatives(expectedConcept);
      results[2]++;
      if (!resultConcept.equalsIgnoreCase(RaptatConstants.NULL_ELEMENT)) {
        incrementFalsePositives(resultConcept);
        results[1]++;
      }
    }
    return results;
  }


  public static ConceptMatchScorer generateTestCase() {
    final int HASH_SIZE = 10, ARRAY_SIZE = 3, MAX_CHARS = 20, MAX_VAL = 100;

    Hashtable<PerformanceScoreObject, int[]> testTable = new Hashtable<>(HASH_SIZE);
    Random randomNumberGenerator = new Random(System.currentTimeMillis());

    for (int i = 0; i < HASH_SIZE; i++) {
      RandomStringGenerator stringGenerator =
          new RandomStringGenerator.Builder().withinRange('0', 'z').build();
      String curString = stringGenerator.generate(randomNumberGenerator.nextInt(MAX_CHARS));
      int[] curArray = new int[ARRAY_SIZE];
      for (int j = 0; j < curArray.length; j++) {
        curArray[j] = randomNumberGenerator.nextInt(MAX_VAL);
      }
      testTable.put(new PerformanceScoreObject(curString), curArray);
    }

    return new ConceptMatchScorer(testTable);
  }
}
