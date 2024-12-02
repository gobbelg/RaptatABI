/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - May 15, 2012 *****************************************************
 */
public class BOWConceptMapTrainer extends NBTrainer {

  /**
   * *************************************************************** Constructor for an object that
   * will calculate and use the likelihood of a database for a naive Bayes network
   *
   * @param useNulls
   * @param convertTokensToStems
   * @param addPOSToTokens
   * @param invertTokenSequence
   * @param reasonNoMedsProcessing ***************************************************************
   */
  public BOWConceptMapTrainer(boolean useNulls, boolean useStems, boolean usePOS,
      boolean invertTokenSequence, boolean removeStopWords,
      ContextHandling reasonNoMedsProcessing) {
    super(useNulls, useStems, usePOS, invertTokenSequence, removeStopWords, reasonNoMedsProcessing);
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @param trainingData
   * @author Glenn Gobbel - May 15, 2012 ***********************************************************
   */
  @Override
  protected void createSolution(DataImportReader trainingData) {
    Vector<Long> durationList = null;

    // Each row of data contains a variable number of grouping
    // columns, followed by a variable set of tokens in a phrase,
    // finally followed by a single concept. Each set of data
    // analyzed consists of a string array containing first
    // the tokens in the phrase followed by the concept at the
    // end.
    int numTokens = trainingData.getNumPhraseTokens();
    int startTokensPosition = trainingData.getStartTokensPosition();

    this.theSolution =
        new BOWConceptMapSolution(numTokens, this.useNulls, this.convertTokensToStems,
            this.addPOSToTokens, this.invertTokenSequence, this.removeStopWords);

    if (getUseNulls()) {
      durationList = this.addToSolutionUseNulls(trainingData, startTokensPosition, numTokens + 1);
    } else {
      durationList = this.addToSolution(trainingData, startTokensPosition, numTokens + 1);
    }

    if (RaptatConstants.TRACK_DURATION && durationList != null) {
      String trainTimeFileName =
          trainingData.getFileDirectory() + "TrainTime_" + GeneralHelper.getTimeStamp() + ".csv";
      GeneralHelper.writeVectorAtInterval(trainTimeFileName, durationList,
          RaptatConstants.PHRASE_REPORT_INTERVAL);
    }
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @param trainingData
   * @author Glenn Gobbel - May 15, 2012 ***********************************************************
   */
  @Override
  protected void createSolution(List<AnnotatedPhrase> trainingData) {
    int dataSize = trainingData.size();

    if (dataSize > 0) {

      this.theSolution =
          new BOWConceptMapSolution(this.maxElements, this.useNulls, this.convertTokensToStems,
              this.addPOSToTokens, this.invertTokenSequence, this.removeStopWords);

      // Run through the data line-by-line updating the database
      // after each line. Update the likelihood table whenever there
      // is a new document number (provided as the first element on the
      // line
      ListIterator<AnnotatedPhrase> dataIterator = trainingData.listIterator();

      // for (String[] curString: trainingData)
      // System.out.println(Arrays.toString(curString));
      if (getUseNulls()) {
        this.addToSolutionUseNulls(dataIterator, dataSize);
      } else {
        this.addToSolution(dataIterator, dataSize);
      }
    }
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @param trainingData
   * @param groupingColumns
   * @author Glenn Gobbel - May 15, 2012 ***********************************************************
   */
  @Override
  protected void createSolution(List<String[]> trainingData, int groupingColumns) {
    // Make sure we have some data and that the data has at least two
    // elements
    // after the grouping columns
    int dataSize = trainingData.size();

    if (dataSize > 0 && trainingData.get(0).length - groupingColumns > 1) {
      int expectedNumElements = trainingData.get(0).length;

      this.theSolution = new BOWConceptMapSolution(expectedNumElements - groupingColumns - 1,
          this.useNulls, this.convertTokensToStems, this.addPOSToTokens, this.invertTokenSequence,
          this.removeStopWords);

      // Run through the data line-by-line updating the database
      // after each line. Update the likelihood table whenever there
      // is a new document number (provided as the first element on the
      // line
      ListIterator<String[]> dataIterator = trainingData.listIterator();

      // for (String[] curString: trainingData)
      // System.out.println(Arrays.toString(curString));
      if (getUseNulls()) {
        this.addToSolutionUseNulls(dataIterator, dataSize, groupingColumns);
      } else {
        this.addToSolution(dataIterator, dataSize, groupingColumns);
      }
    }
  }
}
