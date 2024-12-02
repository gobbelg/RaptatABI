package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.attributemapping.ConceptAttributeMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;

/**
 * ********************************************************** Interface to assure that all the
 * databases generated from training using the various machine learning methods contain a saveToFile
 * method to store solutions. Note that many of the boolean fields, such as usePOS and useStems, are
 * stored here because they are needed to determine token processing durin application of the
 * solution even though they are not directly used by the instance of the ConceptMapSolution itself.
 *
 * @author Glenn Gobbel, Sep 2, 2010
 *         <p>
 *         *********************************************************
 */
public abstract class ConceptMapSolution implements Serializable {
  private static final long serialVersionUID = 2264211584028947170L;

  private boolean usePOS = RaptatConstants.USE_PARTS_OF_SPEECH;

  private boolean useStems = RaptatConstants.USE_TOKEN_STEMMING;

  private boolean removeStopWords = RaptatConstants.REMOVE_STOP_WORDS;

  private boolean invertTokenSequence = RaptatConstants.INVERT_TOKEN_SEQUENCE;

  private boolean useNulls = RaptatConstants.USE_NULLS_START;

  protected int maxTokensInPhrase = 0;

  private final ContextHandling reasonNotOnMedsProcessing =
      OptionsManager.getInstance().getReasonNoMedsProcessing();

  private final ContextHandling negationProcessing =
      OptionsManager.getInstance().getNegationProcessing();

  protected ConceptAttributeMapSolution conceptAttributeSolution;

  private final Map<String, SchemaConcept> schemaConcepts = new HashMap<>(20);


  public ConceptMapSolution() {}


  public ConceptMapSolution(boolean useNulls, boolean useStems, boolean usePartsOfSpeech) {
    this.useNulls = useNulls;
    this.useStems = useStems;
    this.usePOS = usePartsOfSpeech;
  }


  public ConceptMapSolution(boolean useNulls, boolean useStems, boolean usePartsOfSpeech,
      boolean invertTokenSequence, boolean removeStopWords) {
    this.useNulls = useNulls;
    this.useStems = useStems;
    this.usePOS = usePartsOfSpeech;
    this.invertTokenSequence = invertTokenSequence;
    this.removeStopWords = removeStopWords;
  }


  public ConceptMapSolution(int numOfTokens) {
    this.conceptAttributeSolution = new ConceptAttributeMapSolution(numOfTokens);
  }


  public ConceptMapSolution(int maxTokensInPhrase, boolean useNulls, boolean useStems,
      boolean usePartsOfSpeech, boolean invertTokenSequence, boolean removeStopWords) {
    this(useNulls, useStems, usePartsOfSpeech, invertTokenSequence, removeStopWords);
    this.maxTokensInPhrase = maxTokensInPhrase;
    this.conceptAttributeSolution = new ConceptAttributeMapSolution(maxTokensInPhrase);
  }


  public boolean canEqual(Object other) {
    return other instanceof ConceptMapSolution;
  }


  @Override
  public boolean equals(Object other) {
    // FIXME Update to include the caMapSolution field
    if (other instanceof ConceptMapSolution) {
      ConceptMapSolution otherCMSolution = (ConceptMapSolution) other;
      if (otherCMSolution.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.usePOS, otherCMSolution.usePOS)
            .append(this.useStems, otherCMSolution.useStems)
            .append(this.removeStopWords, otherCMSolution.removeStopWords)
            .append(this.invertTokenSequence, otherCMSolution.invertTokenSequence)
            .append(this.useNulls, otherCMSolution.useNulls)
            .append(this.reasonNotOnMedsProcessing, otherCMSolution.reasonNotOnMedsProcessing)
            .append(this.negationProcessing, otherCMSolution.negationProcessing).isEquals();
      }
    }
    return false;
  }


  public ConceptAttributeMapSolution getConceptAttributeMapSolution() {
    return this.conceptAttributeSolution;
  }


  public abstract NBConditionalTable[] getConditionals();


  public boolean getInvertTokenSequence() {
    return this.invertTokenSequence;
  }


  public ContextHandling getNegationProcessing() {
    return this.negationProcessing;
  }


  public int getNumOfTokens() {
    return this.maxTokensInPhrase;
  }


  /** @return the reasonNotOnMedsProcessing */
  public ContextHandling getReasonNotOnMedsProcessing() {
    return this.reasonNotOnMedsProcessing;
  }


  public boolean getRemoveStopWords() {
    return this.removeStopWords;
  }


  /** @return the schemaConcepts */
  public Map<String, SchemaConcept> getSchemaConcepts() {
    return this.schemaConcepts;
  }


  public boolean getUseNulls() {
    return this.useNulls;
  }


  public boolean getUsePOS() {
    return this.usePOS;
  }


  public boolean getUseStems() {
    return this.useStems;
  }


  @Override
  public int hashCode() {
    // FIXME Update to include the caMapSolution field

    HashCodeBuilder hcBuilder = new HashCodeBuilder();

    return hcBuilder.append(this.usePOS).append(this.useStems).append(this.removeStopWords)
        .append(this.invertTokenSequence).append(this.useNulls)
        .append(this.reasonNotOnMedsProcessing).append(this.negationProcessing).toHashCode();
  }


  public abstract void initiateProbCalcs();


  public TokenProcessingOptions retrieveTokenProcessingOptions() {
    return new TokenProcessingOptions(this.useStems, this.usePOS, this.removeStopWords,
        this.invertTokenSequence, this.reasonNotOnMedsProcessing, this.negationProcessing,
        this.maxTokensInPhrase);
  }


  public abstract void saveToFile(File solutionFile);


  public void setInvertTokenSequence(boolean invertTokenSequence) {
    this.invertTokenSequence = invertTokenSequence;
  }


  public void setNullsStemsPOS(boolean useNulls, boolean useStems, boolean usePOS) {
    this.useNulls = useNulls;
    this.useStems = useStems;
    this.usePOS = usePOS;
  }


  public void setNullsStemsPOSInvert(boolean useNulls, boolean useStems, boolean usePartsOfSpeech,
      boolean invertTokenSequence) {
    this.useNulls = useNulls;
    this.useStems = useStems;
    this.usePOS = usePartsOfSpeech;
    this.invertTokenSequence = invertTokenSequence;
  }


  /**
   * @param schemaConceptList
   */
  public void setSchemaConcepts(List<SchemaConcept> schemaConceptList) {
    for (SchemaConcept schemaConcept : schemaConceptList) {
      this.schemaConcepts.put(schemaConcept.getConceptName(), schemaConcept);
    }
  }


  public void setUseNulls(boolean useNulls) {
    this.useNulls = useNulls;
  }


  public void setUsePOS(boolean usePartsOfSpeech) {
    this.usePOS = usePartsOfSpeech;
  }


  public void setUseStems(boolean useStems) {
    this.useStems = useStems;
  }


  public abstract void updateLikelihood();


  /**
   * This method will call updateTraining(String [], String) and allows the call to an instance of
   * ConceptAttributeMap to updateTraining which requires the entire AnnotatedPhrase instance and
   * not just the String representing the concept.
   *
   * @param testData
   * @param string
   * @author Glenn Gobbel - Jan 7, 2015
   */
  public abstract void updateTraining(String[] tokenStrings, AnnotatedPhrase phrase);


  public abstract void updateTraining(String[] tokenStrings, String concept);
}
