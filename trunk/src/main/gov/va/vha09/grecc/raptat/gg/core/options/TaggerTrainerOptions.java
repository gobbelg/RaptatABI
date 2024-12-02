/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.io.File;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;

/**
 * Convenience class for passing parameter settings for training a cue-scope tagger between windows
 * where those settings are chosen
 *
 * @author Glenn T. Gobbel Apr 14, 2016
 */
public class TaggerTrainerOptions {
  private File scopeIncludedFeaturesFile = null;

  private File cueIncludedFeaturesFile = null;

  private File conceptIncludedFeaturesFile = null;

  private int wordConjunctionSize = RaptatConstants.WORD_CONJUNCTION_SIZE_DEFAULT;

  private int wordOffsetDistance = RaptatConstants.WORD_OFFSET_DISTANCE_DEFAULT;

  private int posConjunctionSize = RaptatConstants.POS_CONJUNCTION_SIZE_DEFAULT;

  private int posOffsetDistance = RaptatConstants.POS_OFFSET_DISTANCE_DEFAULT;

  private int cueOffsetFeatureMax = RaptatConstants.CUE_OFFSET_DISTANCE_FEATURE_MAX;

  private TokenProcessingOptions tokenOptions =
      new TokenProcessingOptions(OptionsManager.getInstance());


  /** This will create a TaggerTrainerOptions instance with default values */
  public TaggerTrainerOptions() {}


  /**
   * @param scopeFeaturesFile
   * @param cueFeaturesFile
   * @param wordConjunctionSize
   * @param wordOffsetDistance
   * @param posConjunctionSize
   * @param posOffsetDistance
   * @param tokenOptions
   */
  public TaggerTrainerOptions(File scopeFeaturesFile, File cueFeaturesFile, int wordConjunctionSize,
      int wordOffsetDistance, int posConjunctionSize, int posOffsetDistance,
      TokenProcessingOptions tokenOptions) {
    super();
    this.scopeIncludedFeaturesFile = scopeFeaturesFile;
    this.cueIncludedFeaturesFile = cueFeaturesFile;
    this.wordConjunctionSize = wordConjunctionSize;
    this.wordOffsetDistance = wordOffsetDistance;
    this.posConjunctionSize = posConjunctionSize;
    this.posOffsetDistance = posOffsetDistance;
    this.tokenOptions = tokenOptions;
  }


  /** @return the coneptIncludedFeaturesFile */
  public File getConceptIncludedFeaturesFile() {
    return this.conceptIncludedFeaturesFile;
  }


  /** @return the cueFeaturesFile */
  public File getCueFeaturesFile() {
    return this.cueIncludedFeaturesFile;
  }


  /** @return the cueOffsetFeatureMax */
  public int getCueOffsetFeatureMax() {
    return this.cueOffsetFeatureMax;
  }


  /** @return the posConjunctionSize */
  public int getPosConjunctionSize() {
    return this.posConjunctionSize;
  }


  /** @return the posOffsetDistance */
  public int getPosOffsetDistance() {
    return this.posOffsetDistance;
  }


  /** @return the schemaFeaturesFile */
  public File getScopeFeaturesFile() {
    return this.scopeIncludedFeaturesFile;
  }


  public TokenProcessingOptions getTokenOptions() {
    return this.tokenOptions;
  }


  /** @return the wordConjunctionSize */
  public int getWordConjunctionSize() {
    return this.wordConjunctionSize;
  }


  /** @return the wordOffsetDistance */
  public int getWordOffsetDistance() {
    return this.wordOffsetDistance;
  }


  /**
   * @param coneptIncludedFeaturesFile the coneptIncludedFeaturesFile to set
   */
  public void setConceptIncludedFeaturesFile(File coneptIncludedFeaturesFile) {
    this.conceptIncludedFeaturesFile = coneptIncludedFeaturesFile;
  }


  /**
   * @param cueFeaturesFile the cueFeaturesFile to set
   */
  public void setCueFeaturesFile(File cueFeaturesFile) {
    this.cueIncludedFeaturesFile = cueFeaturesFile;
  }


  /**
   * @param cueOffsetFeatureMax the cueOffsetFeatureMax to set
   */
  public void setCueOffsetFeatureMax(int cueOffsetFeatureMax) {
    this.cueOffsetFeatureMax = cueOffsetFeatureMax;
  }


  /**
   * @param posConjunctionSize the posConjunctionSize to set
   */
  public void setPosConjunctionSize(int posConjunctionSize) {
    this.posConjunctionSize = posConjunctionSize;
  }


  /**
   * @param posOffsetDistance the posOffsetDistance to set
   */
  public void setPosOffsetDistance(int posOffsetDistance) {
    this.posOffsetDistance = posOffsetDistance;
  }


  /**
   * @param schemaFeaturesFile the schemaFeaturesFile to set
   */
  public void setScopeFeaturesFile(File scopeFeaturesFile) {
    this.scopeIncludedFeaturesFile = scopeFeaturesFile;
  }


  /**
   * @param tokenOptions the tokenOptions to set
   */
  public void setTokenOptions(TokenProcessingOptions tokenOptions) {
    this.tokenOptions = tokenOptions;
  }


  /**
   * @param wordConjunctionSize the wordConjunctionSize to set
   */
  public void setWordConjunctionSize(int wordConjunctionSize) {
    this.wordConjunctionSize = wordConjunctionSize;
  }


  /**
   * @param wordOffsetDistance the wordOffsetDistance to set
   */
  public void setWordOffsetDistance(int wordOffsetDistance) {
    this.wordOffsetDistance = wordOffsetDistance;
  }
}
