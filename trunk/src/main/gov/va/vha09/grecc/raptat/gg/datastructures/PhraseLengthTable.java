package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * ********************************************************** PhraseLengthTable -
 *
 * @author Glenn Gobbel, Jul 22, 2010
 *         <p>
 *         *********************************************************
 */
public class PhraseLengthTable {
  public int[][] theTable = new int[RaptatConstants.MAX_TOKENS_DEFAULT][3];


  public PhraseLengthTable() {
    super();
  }


  public int[] getAllMatchValues(int phraseLengthIndex) {
    return this.theTable[phraseLengthIndex];
  }


  public int getFalseNegatives(int phraseLengthIndex) {
    return this.theTable[phraseLengthIndex][2];
  }


  public int getFalsePositives(int phraseLengthIndex) {
    return this.theTable[phraseLengthIndex][1];
  }


  public int getTruePositives(int phraseLengthIndex) {
    return this.theTable[phraseLengthIndex][0];
  }


  public void incrementFalseNegatives(int phraseLengthIndex) {
    this.theTable[phraseLengthIndex][2]++;
  }


  public void incrementFalsePositives(int phraseLengthIndex) {
    this.theTable[phraseLengthIndex][1]++;
  }


  public void incrementTruePositives(int phraseLengthIndex) {
    this.theTable[phraseLengthIndex][0]++;
  }


  public int rows() {
    return this.theTable.length;
  }


  public void setPhrase(int phraseLengthIndex, int correctMatches, int incorrectMatches,
      int unmatched) {
    this.theTable[phraseLengthIndex][0] = correctMatches;
    this.theTable[phraseLengthIndex][1] = incorrectMatches;
    this.theTable[phraseLengthIndex][2] = unmatched;
  }


  public void setPhrase(int phraseLengthIndex, int[] matchValues) {
    for (int i = 0; i < matchValues.length; i++) {
      this.theTable[phraseLengthIndex][i] = matchValues[i];
    }
  }
}
