package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class PerformanceScore implements Comparable {
  public String concept = "";

  public int truePositives = 0, falsePositives = 0, falseNegatives = 0;
  public Double precision = Double.NaN, recall = Double.NaN, fMeasure = Double.NaN;

  public PerformanceScore(final String concept, final int[] matches) {
    this.concept = concept;
    this.truePositives = matches[0];
    this.falsePositives = matches[1];
    this.falseNegatives = matches[2];
    this.precision = GeneralHelper.calcPrecision(this.truePositives, this.falsePositives);
    this.recall = GeneralHelper.calcRecall(this.truePositives, this.falseNegatives);
    this.fMeasure =
        GeneralHelper.calcF_Measure(this.truePositives, this.falsePositives, this.falseNegatives);
  }


  public PerformanceScore(final String concept, final int[] matches, final double[] scores) {
    this.concept = concept;
    this.truePositives = matches[0];
    this.falsePositives = matches[1];
    this.falseNegatives = matches[2];
    this.precision = scores[0];
    this.recall = scores[1];
    this.fMeasure = scores[2];
  }


  @Override
  public int compareTo(Object other) {
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

    if (this == other) {
      return EQUAL;
    }

    if (other instanceof PerformanceScore) {
      PerformanceScore otherScore = (PerformanceScore) other;
      if (otherScore.concept.equals(RaptatConstants.TOTAL_PARTIAL_MATCH_NAME)) {
        return AFTER; // Partial phrase match comes first
      }
      if (this.concept.equals(RaptatConstants.TOTAL_PARTIAL_MATCH_NAME)) {
        return BEFORE; // Partial phrase match comes first
      }
      if (otherScore.concept.equals(RaptatConstants.TOTAL_EXACT_MATCH_NAME)) {
        return AFTER; // Exact phrase matches comes next
      }
      if (this.concept.equals(RaptatConstants.TOTAL_EXACT_MATCH_NAME)) {
        return BEFORE; // Partial phrase match comes first
      }
      return this.concept.compareTo(otherScore.concept);
    }
    return AFTER;
  }


  @Override
  public String toString() {

    StringBuffer resultString = new StringBuffer("");
    resultString.append("CONCEPT:").append(String.format("%-20s\t", this.concept));

    resultString.append(String.format("%-3s%-5d  ", "TP:", this.truePositives));
    resultString.append(String.format("%-3s%-5d  ", "FP:", this.falsePositives));
    resultString.append(String.format("%-3s%-5d  ", "FN:", this.falseNegatives));
    resultString.append(String.format("Prec:%-10.3f ", this.precision));
    resultString.append(String.format("Recall:%-10.3f ", this.recall));
    resultString.append(String.format("F-Meas:%-10.3f ", this.fMeasure));

    return resultString.toString();
  }


  public static void main(String[] args) {
    PerformanceScore startItem, compareItem;
    startItem = new PerformanceScore("ALL (PartialPhraseMatch)", new int[] {0, 0, 0},
        new double[] {0, 0, 0});
    compareItem = new PerformanceScore("ALL (ExactPhraseMatch)".toLowerCase(), new int[] {0, 0, 0},
        new double[] {0, 0, 0});
    System.out.println(startItem.compareTo(compareItem));
    System.out.println("ALL (ExactPhraseMatch)".compareTo("ALL (PartialPhraseMatch)"));
  }
}
