package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ********************************************************** NaiveBayesSolution - Stores a database
 * of occurrences of concepts and attributes associated with those concepts
 *
 * @author Glenn Gobbel, Sep 18, 2010 Modified June 21, 2011
 *         *********************************************************
 */
public class NBSolution extends ConceptMapSolution {
  private static final long serialVersionUID = -401587308018560383L;

  /*
   * The "conditionals" field is used to store the likelihood of a given attribute for a given
   * concept. Here we have an array of conditionals, one for each attribute position. The
   * NBConditionalTable at each position contains the occurrences of a given attribute at that
   * position conditional upon when the a particular concept occurs.
   */
  protected NBConditionalTable[] conditionals;

  /*
   * The "priors" field indicates the likelihood of given concepts. A more careful description is
   * that it stores the number of occurrences of a given concept as well as the sum of the
   * likelihood of the concepts minus the number of occurrences for a given concept and the probable
   * contribution of a given concept to each potential phrase
   */
  protected NBPriorTable priors;

  /*
   * Size of vocabulary for each attribute - NOTE: only updated at calls to calculate likelihood
   * (e.g. initiateProbCals, updateProbCalcs)
   */
  protected int[] vocabSize;


  /**
   * ********************************************
   *
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public NBSolution() {
    super();
  }


  public NBSolution(boolean useNulls, boolean useStems, boolean usePOS, boolean invertTokenSequence,
      boolean removeStopWords) {
    super(useNulls, useStems, usePOS, invertTokenSequence, removeStopWords);
  }


  /**
   * ************************************************************** Set up new databases to hold the
   * occurrences of concepts, factors associated with concepts, and modified factors
   *
   * @param titleData - Data reader used to retrieve data for database
   *        **************************************************************
   */
  public NBSolution(int numOfTokens) {
    super(numOfTokens);
    // First element is document number, last element is just concepts
    // Other data are the attributes associated with the concepts
    this.conditionals = new NBConditionalTable[numOfTokens];
    this.vocabSize = new int[numOfTokens];

    for (int i = 0; i < numOfTokens; i++) {
      this.vocabSize[i] = 0;
      this.conditionals[i] = new NBConditionalTable(RaptatConstants.NB_UNIQUE_TOKEN_TABLE_SIZE);
    }

    this.priors = new NBPriorTable(RaptatConstants.NB_CONCEPT_TABLE_SIZE, numOfTokens);
  }


  /**
   * ************************************************************** Set up new solution to hold the
   * occurrences of concepts, factors associated with concepts, and modified factors
   *
   * @param removeStopWords
   * @param titleData - Data reader used to retrieve data for database
   *        **************************************************************
   */
  public NBSolution(int numOfAttributes, boolean useNulls, boolean useStems, boolean usePOS,
      boolean invertTokenSequence, boolean removeStopWords) {
    super(numOfAttributes, useNulls, useStems, usePOS, invertTokenSequence, removeStopWords);

    this.conditionals = new NBConditionalTable[numOfAttributes];
    this.vocabSize = new int[numOfAttributes];

    for (int i = 0; i < numOfAttributes; i++) {
      this.vocabSize[i] = 0;
      this.conditionals[i] = new NBConditionalTable(RaptatConstants.NB_UNIQUE_TOKEN_TABLE_SIZE);
    }

    this.priors = new NBPriorTable(RaptatConstants.NB_CONCEPT_TABLE_SIZE, numOfAttributes);
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof NBSolution;
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof NBSolution) {
      NBSolution otherSolution = (NBSolution) other;

      if (otherSolution.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();

        return eBuilder.append(this.maxTokensInPhrase, otherSolution.maxTokensInPhrase)
            .append(this.conditionals, otherSolution.conditionals)
            .append(this.priors, otherSolution.priors)
            .append(this.vocabSize, otherSolution.vocabSize).isEquals();
      }
    }
    return false;
  }


  /**
   * @return the attributeOccs
   * @uml.property name="conditionals"
   */
  @Override
  public NBConditionalTable[] getConditionals() {
    return this.conditionals;
  }


  /**
   * **************************************************************
   *
   * @param whichTable - Indicates which table to return
   * @return AttributeOccTable **************************************************************
   */
  public NBConditionalTable getOneConditional(int whichTable) {
    return this.conditionals[whichTable];
  }


  /**
   * @return the conceptOccs
   * @uml.property name="priors"
   */
  public NBPriorTable getPriors() {
    return this.priors;
  }


  /**
   * @return the vocabSize
   * @uml.property name="vocabSize"
   */
  public int[] getVocabSize() {
    return this.vocabSize;
  }


  @Override
  public int hashCode() {
    int oddNumber = 59;
    int primeNumber = 23;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.maxTokensInPhrase).append(this.conditionals).append(this.priors)
        .append(this.vocabSize).toHashCode();
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

    // Determine the size of the vocabularies for the first, second, etc
    // token position, which will be used in the smoothed calculations
    for (int i = 0; i < this.maxTokensInPhrase; i++) {
      this.vocabSize[i] = this.conditionals[i].size();
    }

    this.priors.calcAllLikelihoods(this.vocabSize);

    // -----------------------------------------------------------
    // Now calculate the likelihood values contributed by the attributes
    // which is equal to the log of the occurrences (unsmoothed)
    // or log of occurrences + 1 (smoothed) for a particular
    // attribute value
    // -----------------------------------------------------------
    for (NBConditionalTable curConditional : this.conditionals) {
      curConditional.calcAllProbs();
    }

    this.conceptAttributeSolution.initiateLikelihoodCalcs();
  }


  /**
   * ********************************************************** Save the database
   * **********************************************************
   */
  @Override
  public void saveToFile(File solutionFile) {
    ObjectOutputStream obj_out = null;

    try {
      // Write object with ObjectOutputStream
      obj_out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(solutionFile),
          RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));

      // Write object out to disk
      obj_out.writeObject(this);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (obj_out != null) {
        try {
          obj_out.close();
        } catch (IOException e) {
          System.out.println("Unable to close output stream " + "after writing in NBSolution");
          e.printStackTrace();
        }
      }
    }
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
    boolean vocabSizeChanged = false;
    for (int i = 0; i < this.maxTokensInPhrase; i++) {
      if (this.conditionals[i].vocabularySizeChanged()) {
        vocabSizeChanged = true;
        this.vocabSize[i] = this.conditionals[i].size();
      }
      this.conditionals[i].calcModifiedLikelihoods();
    }

    if (vocabSizeChanged) {
      // Note: No need to actually calculate unsmoothed likelihood
      // on unmodified concepts, but there does not appear to be an
      // efficient way to skip these calculations, so we just redo them
      this.priors.calcAllLikelihoods(this.vocabSize);
    } else {
      this.priors.calcModifiedLikelihoods(this.vocabSize);
    }

    this.conceptAttributeSolution.updateModifiedLikelihoods();
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.conceptmapping.
   * ConceptMapSolution#updateTraining (java.lang.String[], java.lang.String)
   */
  @Override
  public void updateTraining(String[] trainingPhraseStrings, AnnotatedPhrase phrase) {
    if (trainingPhraseStrings != null && phrase != null) {
      String theConcept = phrase.getConceptName();
      this.updateTraining(trainingPhraseStrings, theConcept);
      this.conceptAttributeSolution.updateTraining(theConcept, phrase.getPhraseAttributes(),
          trainingPhraseStrings);
    }
  }


  @Override
  public void updateTraining(String[] trainData, String theConcept) {
    this.priors.increment(theConcept);

    // Run through the words read in trainData
    for (int i = 0; i < trainData.length && i < this.conditionals.length; i++) {
      this.conditionals[i].increment(trainData[i], theConcept);
    }
  }
}
