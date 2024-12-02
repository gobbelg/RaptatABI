package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - May 16, 2012 *****************************************************
 */
public class BOWConceptMapSolution extends NBSolution {
  private static final long serialVersionUID = 1929091607957357418L;


  /**
   * ********************************************
   *
   * @param conceptPosition
   * @param useNulls
   * @param useStems
   * @param usePOS
   * @param invertTokenSequence
   * @param removeStopWords
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public BOWConceptMapSolution(int conceptPosition, boolean useNulls, boolean useStems,
      boolean usePOS, boolean invertTokenSequence, boolean removeStopWords) {
    super(1, useNulls, useStems, usePOS, invertTokenSequence, removeStopWords);

    this.conditionals = new BOWConditionalTable[1];
    this.conditionals[0] = new BOWConditionalTable(RaptatConstants.NB_UNIQUE_TOKEN_TABLE_SIZE);

    this.vocabSize = new int[1];
    this.vocabSize[0] = 0;

    this.priors = new BOWPriorTable(RaptatConstants.NB_CONCEPT_TABLE_SIZE, conceptPosition);
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof BOWConceptMapSolution;
  }


  /**
   * ************************************************************** Put the values used to calculate
   * likelihood of a particular concept occurring with a set of attributes into priors (PriorTable)
   * and conditionals (NBConditionalTable [])
   * **************************************************************
   */
  @Override
  public void initiateProbCalcs() {
    // -----------------------------------------------------------
    // First calculate the likelihood amounts contributed by the concepts
    // With smoothing, this is equal to log( occurrences of Ck ) -
    // log( occurrences of Ck + V0 ) - log( occurrences of Ck + V1 )
    // - . . . - log( occurrences of Ck + Vh ) where Ck is the kth
    // concept, and Vh is the size of the vocabulary for attribute
    // h ( or position h in the case of a word phrase).
    // -----------------------------------------------------------

    this.vocabSize[0] = this.conditionals[0].size();
    ((BOWPriorTable) this.priors).calcAllProbs(this.vocabSize[0]);

    ((BOWConditionalTable) this.conditionals[0]).calcAllProbs((BOWPriorTable) this.priors);
  }


  /**
   * ************************************************************** Update the values used to
   * calculate likelihood of a particular concept occurring with a set of attributes into the
   * concept occurrence table and attribute occurrence table
   * **************************************************************
   */
  @Override
  public void updateLikelihood() {
    // Need to update likelihood values for all concepts if the
    // vocabulary size has changed. If not, we update it only
    // for those concepts whose number of occurrences has changed
    if (this.vocabSize[0] != this.conditionals[0].size()) {
      this.vocabSize[0] = this.conditionals[0].size();
      ((BOWPriorTable) this.priors).calcAllProbs(this.vocabSize[0]);
    } else {
      ((BOWPriorTable) this.priors).calcModProbs(this.vocabSize[0]);
    }

    ((BOWConditionalTable) this.conditionals[0]).calcModProbs((BOWPriorTable) this.priors);
  }


  /**
   * ******************************************************************* Update the number of
   * occurrences of a given word in the database
   *
   * @param curData - String array of the words in a phrase
   * @param theConcept - String representing concept mapped to by words in phrase
   *        *******************************************************************
   */
  @Override
  public void updateTraining(String[] trainData, AnnotatedPhrase phrase) {
    if (trainData != null && phrase != null) {
      String theConcept = phrase.getConceptName();
      this.updateTraining(trainData, theConcept);
      this.conceptAttributeSolution.updateTraining(theConcept, phrase.getPhraseAttributes(),
          trainData);
    }
  }


  @Override
  public void updateTraining(String[] trainData, String theConcept) {

    // theConcept is the word/phrase/number in the annotation
    // scheme that the phrase is mapped to
    ((BOWPriorTable) this.priors).increment(theConcept, trainData);

    for (int i = 0; i < trainData.length; i++) {
      this.conditionals[0].increment(trainData[i], theConcept);
    }
  }
}
