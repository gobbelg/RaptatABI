package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** NonNaiveBayesSolution -
 *
 * @author Glenn Gobbel, Sep 19, 2010
 *         <p>
 *         *********************************************************
 */
public class NonNBSolution extends ConceptMapSolution {

  private static final long serialVersionUID = 2808580761725415627L;

  // Number of potential attributes determining a concept
  private int numOfAttributes = 0;

  // This is essentially used to store the occurrences and likelihood
  // given a particular phrase of 1 .. numOfAttributes words in length
  private ConditionalTree conditionals;

  // This is used to determine the likelihood of given
  // concepts.
  private NonNBPriorTable priors;

  // Keeps track of concepts that have been updated so that likelihood
  // in priors (a PriorTable) will need to be updated.
  // No need to store this, so we make it transient
  private transient Hashtable<String, Boolean> modifiedConcepts;


  /**
   * ************************************************************** Set up new databases to hold the
   * occurrences of concepts, factors associated with concepts, and modified factors
   *
   * @param titleData - Data reader used to retrieve data for database
   *        **************************************************************
   */
  public NonNBSolution(int numOfAttributes) {
    this.numOfAttributes = numOfAttributes;
    this.conditionals = new ConditionalTree(RaptatConstants.NB_UNIQUE_TOKEN_TABLE_SIZE);
    this.priors = new NonNBPriorTable(RaptatConstants.NB_CONCEPT_TABLE_SIZE);
    this.modifiedConcepts = new Hashtable<>(RaptatConstants.NB_CONCEPT_TABLE_SIZE);
  }


  @Override
  public NBConditionalTable[] getConditionals() {
    // TODO Auto-generated method stub
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution# initiateProbCalcs ()
   */
  @Override
  public void initiateProbCalcs() {
    // TODO Auto-generated method stub

  }


  /**
   * ********************************************************** Save the database
   * **********************************************************
   */
  @Override
  public void saveToFile(File solutionFile) {
    ObjectOutputStream obj_out = null;
    try {
      // Write to disk with FileOutputStream
      FileOutputStream f_out = new FileOutputStream(solutionFile);

      // Write object with ObjectOutputStream
      obj_out = new ObjectOutputStream(
          new BufferedOutputStream(f_out, RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));

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
          // TODO Auto-generated catch block
          System.out.println(e);
          e.printStackTrace();
        }
      }
    }
  }


  /**
   * ************************************************************** Update the ConditionalTree and
   * prior tables in the solution Precondition: numOfAttributes is greater than zero
   *
   * @param curData - a String array used to hold a set of attributes
   * @param useNulls - a boolean to determine whether to use the word NULL_ELEMENT when it occurs in
   *        curData
   * @return void **************************************************************
   */
  public void updateConditionalTree(String[] curData, boolean useNulls, boolean useStems) {
    boolean uniquePhrase = true; // Shows whether phrase-concept previously
    // stored

    if (curData != null) {
      String[] thePhrase;

      // Include NULL attribute values in database if set as true by call
      // to this method
      if (useNulls) {
        thePhrase = GeneralHelper.getSubArray(curData, RaptatConstants.DOC_ID_COLUMN + 1,
            this.numOfAttributes);
      } else {
        // Find where nulls start to determine length of phrase
        int phraseLength = 0;
        while (phraseLength < this.numOfAttributes
            && curData[phraseLength] != RaptatConstants.NULL_ELEMENT) {
          phraseLength++;
        }

        thePhrase =
            GeneralHelper.getSubArray(curData, RaptatConstants.DOC_ID_COLUMN + 1, phraseLength);
      }

      if (thePhrase != null && thePhrase.length > 0) {
        String theConcept = curData[this.numOfAttributes + 1];
        uniquePhrase = this.conditionals.increment(thePhrase, theConcept);
        this.priors.increment(theConcept, uniquePhrase);

        // We store modified concepts so that associated likelihood can
        // be
        // calculated at end of each document. Note that conditional
        // likelihood are updated continuously during "increment"
        this.modifiedConcepts.put(theConcept, true);
      }
    }
  }


  /**
   * **************************************************************
   *
   * <p>
   * void **************************************************************
   */
  @Override
  public void updateLikelihood() {
    NonNBOccurProbSet curSet;

    Enumeration<String> theConcepts = this.modifiedConcepts.keys();
    while (theConcepts.hasMoreElements()) {
      curSet = this.priors.get(theConcepts.nextElement());
      curSet.updateProbability();
    }
  }


  @Override
  public void updateTraining(String[] trainData, AnnotatedPhrase phrase) {
    String theConcept = phrase.getConceptName();
    this.updateTraining(trainData, theConcept);
    this.conceptAttributeSolution.updateTraining(theConcept, phrase.getPhraseAttributes(),
        trainData);
  }


  @Override
  public void updateTraining(String[] trainData, String theConcept) {
    // TODO Auto-generated method stub

  }
}
