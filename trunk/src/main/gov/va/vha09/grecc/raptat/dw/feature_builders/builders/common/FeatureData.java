package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common;

import java.text.MessageFormat;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;

/**
 * A class to hold current data about a feature, including the text, its ordinal reference in a
 * document, the starting line index, and it's current line index.
 *
 * @author VHATVHWesteD
 *
 */
public class FeatureData {
  private int currentLineIndex;
  private final String feature;

  private final int startLineIndex;
  private final int startTokenIndex;
  private final int endTokenIndex;
  private int currentStartTokenIndex;
  private int currentEndTokenIndex;


  /**
   * Instantiates a new feature data.
   *
   * @param feature the feature
   * @param phrase the phrase
   * @param startTokenIndex the start token index
   * @param endTokenIndex the end token index
   */
  public FeatureData(String feature, RaptatTokenPhrase phrase, int startTokenIndex,
      int endTokenIndex) {
    this.feature = feature;
    this.startLineIndex = phrase.get_line_index();
    this.currentLineIndex = phrase.get_line_index();
    this.startTokenIndex = startTokenIndex;
    this.endTokenIndex = endTokenIndex;
    this.currentStartTokenIndex = startTokenIndex;
    this.currentEndTokenIndex = endTokenIndex;
  }


  public final String getFeature() {
    return this.feature;
  }


  public String getFeatureWithIndex() {
    final int tokenDifference = this.currentStartTokenIndex - this.endTokenIndex;
    return MessageFormat.format("{0}_{1}", getFeature(), Math.abs(tokenDifference));
  }


  public String getFeatureWithLineDifference() {
    final int lineDifference = this.currentLineIndex - this.startLineIndex;
    /**
     * 6-25-19 - DAX - Zero-eth entry not written out
     */
    return lineDifference > 0
        ? MessageFormat.format("{0}_{1}", getFeature(), String.valueOf(Math.abs(lineDifference)))
        : null;
  }


  @Override
  public String toString() {
    return "";
  }


  public void update(RaptatTokenPhrase phrase, int startTokenIndex, int endTokenIndex) {
    this.currentStartTokenIndex = startTokenIndex;
    this.currentEndTokenIndex = endTokenIndex;
    this.currentLineIndex = phrase.get_line_index();
  }
}
