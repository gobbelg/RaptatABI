package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;

public class PhraseIDTrainOptions implements Serializable {
  private static final long serialVersionUID = 1475188822085575841L;
  private TSFinderMethod phraseIDMethod, priorTokenTraining, postTokenTraining;
  private PhraseIDSmoothingMethod smoothingMethod;
  private boolean linkTrainingToPrior, linkTrainingToPost;
  private int surroundingTokensToUse;


  /**
   * @param theOptions
   */
  public PhraseIDTrainOptions(OptionsManager theOptions) {
    super();
    this.phraseIDMethod = theOptions.getPhraseIDTrainingMethod();
    this.priorTokenTraining = theOptions.getPriorTokensTrainingMethod();
    this.postTokenTraining = theOptions.getPostTokensTrainingMethod();
    this.smoothingMethod = theOptions.getSmoothingMethod();
    this.linkTrainingToPrior = theOptions.getLinkPhraseToPriorTokens();
    this.linkTrainingToPost = theOptions.getLinkPhraseToPostTokens();
    this.surroundingTokensToUse = theOptions.getSurroundingTokenNumber();
  }


  public PhraseIDTrainOptions(TSFinderMethod phraseIDMethod, TSFinderMethod priorTokenTraining,
      TSFinderMethod postTokenTraining, PhraseIDSmoothingMethod smoothingMethod,
      boolean linkTrainingToPrior, boolean linkTrainingToPost, int surroundingTokensToUse) {
    super();
    this.phraseIDMethod = phraseIDMethod;
    this.priorTokenTraining = priorTokenTraining;
    this.postTokenTraining = postTokenTraining;
    this.smoothingMethod = smoothingMethod;
    this.linkTrainingToPrior = linkTrainingToPrior;
    this.linkTrainingToPost = linkTrainingToPost;
    this.surroundingTokensToUse = surroundingTokensToUse;
  }


  public boolean canEqual(Object other) {
    return other instanceof PhraseIDTrainOptions;
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof PhraseIDTrainOptions) {
      PhraseIDTrainOptions otherOptions = (PhraseIDTrainOptions) other;
      if (otherOptions.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.phraseIDMethod, otherOptions.phraseIDMethod)
            .append(this.priorTokenTraining, otherOptions.priorTokenTraining)
            .append(this.postTokenTraining, otherOptions.postTokenTraining)
            .append(this.smoothingMethod, otherOptions.smoothingMethod)
            .append(this.linkTrainingToPrior, otherOptions.linkTrainingToPrior)
            .append(this.linkTrainingToPost, otherOptions.linkTrainingToPost)
            .append(this.surroundingTokensToUse, otherOptions.surroundingTokensToUse).isEquals();
      }
    }
    return false;
  }


  /** @return the phraseIDMethod */
  public TSFinderMethod getPhraseIDMethod() {
    return this.phraseIDMethod;
  }


  /** @return the postTokenTraining */
  public TSFinderMethod getPostTokenTraining() {
    return this.postTokenTraining;
  }


  /** @return the priorTokenTraining */
  public TSFinderMethod getPriorTokenTraining() {
    return this.priorTokenTraining;
  }


  /** @return the smoothingMethod */
  public PhraseIDSmoothingMethod getSmoothingMethod() {
    return this.smoothingMethod;
  }


  /** @return the surroundingTokensToUse */
  public int getSurroundingTokensToUse() {
    return this.surroundingTokensToUse;
  }


  @Override
  public int hashCode() {
    int oddNumber = 99;
    int primeNumber = 47;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.phraseIDMethod).append(this.priorTokenTraining)
        .append(this.postTokenTraining).append(this.smoothingMethod)
        .append(this.linkTrainingToPrior).append(this.linkTrainingToPost)
        .append(this.surroundingTokensToUse).toHashCode();
  }


  /** @return the linkTrainingToPost */
  public boolean linkTrainingToPost() {
    return this.linkTrainingToPost;
  }


  /** @return the linkTrainingToPrior */
  public boolean linkTrainingToPrior() {
    return this.linkTrainingToPrior;
  }


  /**
   * @param linkTrainingToPost the linkTrainingToPost to set
   */
  public void setLinkTrainingToPost(boolean linkTrainingToPost) {
    this.linkTrainingToPost = linkTrainingToPost;
  }


  /**
   * @param linkTrainingToPrior the linkTrainingToPrior to set
   */
  public void setLinkTrainingToPrior(boolean linkTrainingToPrior) {
    this.linkTrainingToPrior = linkTrainingToPrior;
  }


  /**
   * @param phraseIDMethod the phraseIDMethod to set
   */
  public void setPhraseIDMethod(TSFinderMethod phraseIDMethod) {
    this.phraseIDMethod = phraseIDMethod;
  }


  /**
   * @param postTokenTraining the postTokenTraining to set
   */
  public void setPostTokenTraining(TSFinderMethod postTokenTraining) {
    this.postTokenTraining = postTokenTraining;
  }


  /**
   * @param priorTokenTraining the priorTokenTraining to set
   */
  public void setPriorTokenTraining(TSFinderMethod priorTokenTraining) {
    this.priorTokenTraining = priorTokenTraining;
  }


  /**
   * @param smoothingMethod the smoothingMethod to set
   */
  public void setSmoothingMethod(PhraseIDSmoothingMethod smoothingMethod) {
    this.smoothingMethod = smoothingMethod;
  }


  /**
   * @param surroundingTokensToUse the surroundingTokensToUse to set
   */
  public void setSurroundingTokensToUse(int surroundingTokensToUse) {
    this.surroundingTokensToUse = surroundingTokensToUse;
  }
}
