package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/*
 * Tests the efficiency of creating and using likelihood tables in a Bayes network programmed using
 * Java
 *
 * @author Glenn Gobbel, July 5, 2010
 *
 * *********************************************************
 */

public class NonNBTrainer extends ConceptMapTrainer {

  private NonNBSolution theNBSolution;


  /**
   * *************************************************************** Constructor for an object that
   * will calculate and use the likelihood of a database for a naive Bayes network
   *
   * @param convertTokensToStems ***************************************************************
   */
  public NonNBTrainer(boolean useNulls, boolean useStems) {
    this.useNulls = useNulls;
    this.convertTokensToStems = useStems;
  }


  @Override
  protected Vector<Long> addToSolution(DataImportReader trainingData, int startTokensPosition,
      int numElementsForAnalysis) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolution(ListIterator<AnnotatedPhrase> dataIterator, int dataSize) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolution(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(DataImportReader trainingData,
      int startTokensPosition, int numElementsForAnalysis) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(ListIterator<AnnotatedPhrase> dataIterator,
      int dataSize) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns) {
    // TODO Auto-generated method stub
    return null;
  }


  /**
   * ************************************************************** Creates the data structure used
   * for likelihood calculations
   *
   * @return - void **************************************************************
   */
  @Override
  protected void createSolution(DataImportReader trainingData) {
    String curDocument, newDocument;
    String[] curData;

    // Initialize the database using the length of the data to determine
    // the number of attributes in the data. The first element contains
    // the document number and the last the concept.
    // Only lines with correct number of elements will
    // be evaluated
    curData = trainingData.getNextData();
    int expectedNumElements = curData.length;
    int numOfAttributes = expectedNumElements - 2;

    if (numOfAttributes > 0) {
      this.theNBSolution = new NonNBSolution(numOfAttributes);

      // Run through the data line-by-line updating the database
      // after each line. Update the likelihood table whenever there
      // is a new document number (provided as the first element on the
      // line)
      // Get first line with correct number of elements
      do {
        curData = trainingData.getNextData();
      } while (curData != null && curData.length != expectedNumElements);

      // Keep reading while not at end of data
      while (curData != null) {
        // Keep updating database as long as the document number
        // is unchanged
        curDocument = curData[0];
        newDocument = curDocument;
        while (curDocument.equals(newDocument)) {
          this.theNBSolution.updateConditionalTree(curData, this.useNulls,
              this.convertTokensToStems);
          do // Get next line with correct number of elements
          {
            curData = trainingData.getNextData();
          } while (curData != null && curData.length != expectedNumElements);

          if (curData != null) {
            newDocument = curData[0];
          } else {
            newDocument = null;
          }
        }

        // Now update to the new document and update the likelihood
        // values with the database based on the data from the old
        // document
        System.out.println("Updating likelihood for document " + curDocument);
        curDocument = newDocument;
        this.theNBSolution.updateLikelihood();
      }
      this.solutionInitiated = true;
    }
  }


  @Override
  protected void createSolution(List<AnnotatedPhrase> theAnnotations) {
    // TODO Auto-generated method stub

  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Trainer#createSolution (java.util.List)
   */
  @Override
  protected void createSolution(List<String[]> theData, int startTokensPosition) {
    // TODO Auto-generated method stub
  }
}
