package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;

/**
 * ****************************************************** Convenience class to make it easy to pass
 * a group of training options between object instances. This class specifically stores the settings
 * for the tokens used in training or evaluation.
 *
 * @author Glenn Gobbel - Sep 11, 2012 *****************************************************
 */
public class TokenProcessingOptions implements Serializable {
  /** */
  private static final long serialVersionUID = -6583950860134985941L;

  private boolean useStems;
  private boolean usePOS;
  private boolean removeStopWords;
  private boolean invertTokenSequence;
  private int maxTokenNumber;
  private ContextHandling reasonNoMedsProcessing =
      OptionsManager.getInstance().getReasonNoMedsProcessing();

  private ContextHandling negationProcessing = OptionsManager.getInstance().getNegationProcessing();


  public TokenProcessingOptions(boolean useStems, boolean usePOS, boolean removeStopWords,
      boolean invertTokenSequence, ContextHandling reasonNoMedsProcessing,
      ContextHandling negationProcessing, int maxTokenNumber) {
    super();
    this.useStems = useStems;
    this.usePOS = usePOS;
    this.removeStopWords = removeStopWords;
    this.invertTokenSequence = invertTokenSequence;
    this.maxTokenNumber = maxTokenNumber;
    this.reasonNoMedsProcessing = reasonNoMedsProcessing;
    this.negationProcessing = negationProcessing;
  }


  public TokenProcessingOptions(OptionsManager theOptions) {
    super();
    this.useStems = theOptions.getUseStems();
    this.usePOS = theOptions.getUsePOS();
    this.removeStopWords = theOptions.getRemoveStopWords();
    this.invertTokenSequence = theOptions.getInvertTokenSequence();
    this.maxTokenNumber = theOptions.getTrainingTokenNumber();
    this.reasonNoMedsProcessing = theOptions.getReasonNoMedsProcessing();
    this.negationProcessing = theOptions.getNegationProcessing();
  }


  /**
   * Copy constructor
   *
   * @param tokenOptions
   */
  public TokenProcessingOptions(TokenProcessingOptions tokenOptions) {
    super();
    this.useStems = tokenOptions.useStems;
    this.usePOS = tokenOptions.usePOS;
    this.removeStopWords = tokenOptions.removeStopWords;
    this.invertTokenSequence = tokenOptions.invertTokenSequence;
    this.maxTokenNumber = tokenOptions.maxTokenNumber;
    this.reasonNoMedsProcessing = tokenOptions.reasonNoMedsProcessing;
    this.negationProcessing = tokenOptions.negationProcessing;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    TokenProcessingOptions rhs = (TokenProcessingOptions) obj;
    return new EqualsBuilder().append(this.invertTokenSequence, rhs.invertTokenSequence)
        .append(this.maxTokenNumber, rhs.maxTokenNumber)
        .append(this.negationProcessing, rhs.negationProcessing)
        .append(this.reasonNoMedsProcessing, rhs.reasonNoMedsProcessing)
        .append(this.removeStopWords, rhs.removeStopWords).append(this.usePOS, rhs.usePOS)
        .append(this.useStems, rhs.useStems).isEquals();
  }


  public int getMaxTokenNumber() {
    return this.maxTokenNumber;
  }


  public ContextHandling getNegationProcessing() {
    return this.negationProcessing;
  }


  public ContextHandling getReasonNoMedsProcessing() {
    return this.reasonNoMedsProcessing;
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder(53, 101).append(this.invertTokenSequence).append(this.maxTokenNumber)
        .append(this.negationProcessing).append(this.reasonNoMedsProcessing)
        .append(this.removeStopWords).append(this.usePOS).append(this.useStems).toHashCode();
  }


  /** @return the invertTokenSequence */
  public boolean invertTokenSequence() {
    return this.invertTokenSequence;
  }


  /** @return the removeStopWords */
  public boolean removeStopWords() {
    return this.removeStopWords;
  }


  /**
   * @param invertTokenSequence the invertTokenSequence to set
   */
  public void setInvertTokenSequence(boolean invertTokenSequence) {
    this.invertTokenSequence = invertTokenSequence;
  }


  /**
   * @param maxTokenNumber the maxTokenNumber to set
   */
  public void setMaxTokenNumber(int maxTokenNumber) {
    this.maxTokenNumber = maxTokenNumber;
  }


  public void setNegationProcessing(ContextHandling theHandling) {
    this.negationProcessing = theHandling;
  }


  public void setReasonNoMedsProcessing(ContextHandling theHandling) {
    this.reasonNoMedsProcessing = theHandling;
  }


  /**
   * @param removeStopWords the removeStopWords to set
   */
  public void setRemoveStopWords(boolean removeStopWords) {
    this.removeStopWords = removeStopWords;
  }


  /**
   * @param usePOS the usePOS to set
   */
  public void setUsePOS(boolean usePOS) {
    this.usePOS = usePOS;
  }


  /**
   * @param useStems the useStems to set
   */
  public void setUseStems(boolean useStems) {
    this.useStems = useStems;
  }


  /** @return the usePOS */
  public boolean usePOS() {
    return this.usePOS;
  }


  /** @return the useStems */
  public boolean useStems() {
    return this.useStems;
  }
}
