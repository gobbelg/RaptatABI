package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * ********************************************************** ConceptPLengthTable -
 *
 * @author Glenn Gobbel, Jul 22, 2010 *********************************************************
 */
public class ConceptPLengthTable extends Hashtable<String, int[][]> {

  private static final long serialVersionUID = 2971160173267816270L;


  public ConceptPLengthTable() {
    super(RaptatConstants.NB_CONCEPT_TABLE_SIZE / 4);
  }


  public ConceptPLengthTable(int startCapacity) {
    super(startCapacity);
  }


  public int getFalseNegatives(String theConcept, int phraseLengthIndex) {
    if (containsKey(theConcept)) {
      return get(theConcept)[phraseLengthIndex][2];
    } else {
      return 0;
    }
  }


  public int getFalsePositive(String theConcept, int phraseLengthIndex) {
    if (containsKey(theConcept)) {
      return get(theConcept)[phraseLengthIndex][1];
    } else {
      return 0;
    }
  }


  public int getTruePositives(String theConcept, int phraseLengthIndex) {
    if (containsKey(theConcept)) {
      return get(theConcept)[phraseLengthIndex][0];
    } else {
      return 0;
    }
  }


  public void incrementFalseNegatives(String theConcept, int phraseLengthIndex) {
    if (containsKey(theConcept)) {
      get(theConcept)[phraseLengthIndex][0]++;
    } else {
      int[][] tempInts = new int[RaptatConstants.MAX_TOKENS_DEFAULT][3];
      tempInts[phraseLengthIndex][2] = 1;
      put(theConcept, tempInts);
    }
  }


  public void incrementFalsePositives(String theConcept, int phraseLengthIndex) {
    if (containsKey(theConcept)) {
      get(theConcept)[phraseLengthIndex][1]++;
    } else {
      int[][] tempInts = new int[RaptatConstants.MAX_TOKENS_DEFAULT][3];
      tempInts[phraseLengthIndex][1] = 1;
      put(theConcept, tempInts);
    }
  }


  public void incrementTruePositives(String theConcept, int phraseLengthIndex) {
    if (containsKey(theConcept)) {
      get(theConcept)[phraseLengthIndex][0]++;
    } else {
      int[][] tempInts = new int[RaptatConstants.MAX_TOKENS_DEFAULT][3];
      tempInts[phraseLengthIndex][0] = 1;
      put(theConcept, tempInts);
    }
  }


  /**
   * **************************************************************
   *
   * @return String[] **************************************************************
   */
  public String[] sortedKeys() {
    Set<String> setOfKeys = keySet();
    String[] result = new String[setOfKeys.size()];
    setOfKeys.toArray(result);
    Arrays.sort(result);
    return result;
  }
}
