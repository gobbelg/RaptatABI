package src.main.gov.va.vha09.grecc.raptat.gg.core;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.decisiontree.DecisionTreeEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.decisiontree.DecisionTreeTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords.BOWConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords.BOWConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes.NonNBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes.NonNBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate.PhraseIDCrossValidationAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.AnnotationWindow;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.ScorePerformanceWindow;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.TrainingWindow;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.ExportResults;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.ResultWriter;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.machinelearning.naivebayes.NBResultWriter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;

/**
 * ********************************************************** Handles all the responses to choosing
 * an item in a menu
 *
 * @author Glenn Gobbel, Aug 26, 2010
 *         <p>
 *         *********************************************************
 */
public class MenuResponseDispatcher {

  public MenuResponseDispatcher() {
    super();
  }


  public void annotateDocuments(final Point thePosition, final Dimension theDimension,
      JFrame applicationWindow) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          AnnotationWindow window = new AnnotationWindow(thePosition, theDimension);
          window.setVisible(true);
          window.setResizable(false);
          window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  public void dbCreation(Point thePosition, Dimension theDimension) {
    try {
      // DatabaseCreationWindow.getInstance(thePosition, theDimension);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void exportResults() {
    try {
      ExportResults window = new ExportResults();
      window.exportFrame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * ************************************************************** Load a solution from a file
   *
   * @return AnnotationSolution
   * @throws ClassNotFoundException **************************************************************
   */
  public ConceptMapSolution loadConceptMapSolution() {
    ConceptMapSolution theSolution = null;
    ObjectInputStream inObject = null;

    // Get the solutionFile
    String[] fileType = {".soln"};
    File solutionFile = GeneralHelper.getFileByType("Select Solution File", fileType);

    if (solutionFile != null) {
      try {
        inObject = new ObjectInputStream(new FileInputStream(solutionFile));
        theSolution = (ConceptMapSolution) inObject.readObject();
      } catch (Exception e) {
        System.out.println("Unable to open and read in solution file");
        e.printStackTrace();
      } finally {
        try {
          if (inObject != null) {
            inObject.close();
          }
        } catch (IOException e) {
          System.out.println("Unable to close solution file");
          e.printStackTrace();
        }
      }
    }
    return theSolution;
  }


  public RaptatPair<ProbabilisticTSFinderSolution, String> loadProbabilisticSolution() {
    ProbabilisticTSFinderSolution theSolution = null;
    String solutionDirectory = null;
    ObjectInputStream inObject = null;

    // Get the solutionFile
    String[] fileType = {".soln"};
    File solutionFile = GeneralHelper.getFileByType("Select Solution File", fileType);

    if (solutionFile != null) {
      try {
        inObject = new ObjectInputStream(new FileInputStream(solutionFile));
        theSolution = (ProbabilisticTSFinderSolution) inObject.readObject();
        solutionDirectory = solutionFile.getParent();

      } catch (Exception e) {
        System.out.println("Unable to open and read in solution file");
        e.printStackTrace();
      } finally {
        try {
          if (inObject != null) {
            inObject.close();
          }
        } catch (IOException e) {
          System.out.println("Unable to close solution file");
          e.printStackTrace();
        }
      }
      return new RaptatPair<>(theSolution, solutionDirectory);
    }
    return null;
  }


  public RaptatPair<RaptatAnnotationSolution, String> loadSolution() {
    return this.loadSolution(null);
  }


  /**
   * Returns a pair of Solution class
   *
   * @return
   */
  public RaptatPair<RaptatAnnotationSolution, String> loadSolution(String startDirectory) {
    RaptatAnnotationSolution theSolution = null;
    String solutionDirectory = null;
    ObjectInputStream inObject = null;

    // Get the solutionFile
    String[] fileType = {".soln"};

    File solutionFile = null;
    if (startDirectory == null || startDirectory.isEmpty()) {
      solutionFile = GeneralHelper.getFileByType("Select Solution File", fileType);
    } else {
      solutionFile = GeneralHelper.getFileByType("Select Solution File", startDirectory, fileType);
    }

    if (solutionFile != null) {
      try {
        inObject = new ObjectInputStream(new FileInputStream(solutionFile));

        theSolution = (RaptatAnnotationSolution) inObject.readObject();

        solutionDirectory = solutionFile.getParent();
      } catch (IOException e) {
        System.out.println("Unable to open and read in solution file" + e);
        // e.printStackTrace();
      } catch (ClassNotFoundException e) {
        System.out.println("Unable to open and read in solution file" + e);
      } finally {
        try {
          if (inObject != null) {
            inObject.close();
          }
        } catch (IOException e) {
          System.out.println("Unable to close solution file" + e);
          // e.printStackTrace();
        }
      }
      return new RaptatPair<>(theSolution, solutionDirectory);
    }
    return null;
  }


  /**
   * Takes a solution created with machine learning and tests it on data.
   *
   * @param curSolution
   * @param annotatorOptions void
   */
  public void mapSolution(ConceptMapSolution curSolution, OptionsManager annotatorOptions) {
    ResultWriter resultWriter = null;
    ConceptMapEvaluator curEvaluator = null;

    // Get the file for evaluating/testing the system
    boolean getGroupingColumns = true;
    DataImportReader dataToMap =
        new DataImporter("Select Mapping Data", getGroupingColumns).getReader();

    if (dataToMap != null) {
      String trainMethod = curSolution.getClass().getName();

      if (trainMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_SOLUTIONS[0])) {
        curEvaluator = new BOWConceptMapEvaluator(curSolution, annotatorOptions);
      } else if (trainMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_SOLUTIONS[1])) {
        curEvaluator = new NBEvaluator(curSolution, annotatorOptions);

      } else if (trainMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_SOLUTIONS[2])) {
        curEvaluator = new NonNBEvaluator(curSolution, annotatorOptions);
      } else if (trainMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_SOLUTIONS[3])) {
        curEvaluator = new DecisionTreeEvaluator(curSolution, annotatorOptions);
      } else {
        JOptionPane.showMessageDialog(null, "No testing method available for current solution file",
            "", JOptionPane.WARNING_MESSAGE);
      }

      if (curEvaluator != null) {
        // Use same file format for output as input with default being a
        // csv file
        if (dataToMap.getFileType().equals(RaptatConstants.IMPORTABLE_FILETYPES[1])) {
          resultWriter = new NBResultWriter(dataToMap.getFileDirectory(),
              new String[] {"Concept", "Candidates", "Smoothing"}, "\t");
        } else {
          resultWriter = new NBResultWriter(dataToMap.getFileDirectory(),
              new String[] {"Concept", "Candidates", "Smoothing"}, ",");
        }
        try {
          GeneralHelper.errorWriter("Starting mapping");
          List<String[]> results = curEvaluator.mapSolution(dataToMap);
          if (results != null) {
            GeneralHelper.errorWriter("Evaluation completed: results found");
            System.out.println("\nWriting results");
            resultWriter.writeResults(results);
          } else {
            GeneralHelper.errorWriter("Evaluation completed: no results found");
          }

        } catch (Exception ee) {

        } finally {
          if (resultWriter != null) {
            resultWriter.close();
          }
        }
      }
    }
  }


  public void runPhraseIDCrossValidationAnalysis(boolean filterAnnotations) {
    int detailedReport = JOptionPane.showConfirmDialog(null,
        "Create an additional, detailed report of errors?", "", JOptionPane.YES_NO_CANCEL_OPTION);
    if (detailedReport != JOptionPane.CANCEL_OPTION) {
      OptionsManager theOptions = OptionsManager.getInstance();
      TSFinderMethod phraseTraining = theOptions.getPhraseIDTrainingMethod();
      TSFinderMethod priorTokenTraining = theOptions.getPriorTokensTrainingMethod();
      TSFinderMethod postTokenTraining = theOptions.getPostTokensTrainingMethod();
      PhraseIDSmoothingMethod smoothingMethod = theOptions.getSmoothingMethod();

      int surroundingTokensToUse = theOptions.getSurroundingTokenNumber();
      boolean linkPhraseToPriorTokens = theOptions.getLinkPhraseToPriorTokens();
      boolean linkPhraseToPostTokens = theOptions.getLinkPhraseToPriorTokens();

      File schemaConceptFile = GeneralHelper.getFile("Select file containing schema",
          OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath());
      String facilitatorBlockerPath = GeneralHelper
          .getFile("Select facilitator/blocker dicitonary file",
              OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath())
          .getAbsolutePath();
      ContextSifter contextSifter = loadContextSifters();
      HashSet<String> trainingConcepts = PhraseIDCrossValidationAnalyzer.getConcepts(null,
          "Select tab-delimited file containing training concepts");
      HashSet<String> scoringConcepts = PhraseIDCrossValidationAnalyzer.getConcepts(null,
          "Select tab-delimited file containing scoring concepts");

      TextAnalyzer textAnalyzer = new TextAnalyzer(facilitatorBlockerPath, trainingConcepts);
      GeneralHelper.errorWriter(
          "Sentence and section pattern matchers for contextual text analysis must be inserted manually");
      Collection<AttributeTaggerSolution> attributeTaggers =
          PhraseIDCrossValidationAnalyzer.getAttributeTaggers();

      boolean storeResultsAsXML = RaptatConstants.STORE_CROSS_VALIDATION_RESULTS_AS_XML;
      SentenceFilter sentenceFilter = null;
      int startOptionIndex = 56;
      int stopOptionIndex = 57;
      PhraseIDCrossValidationAnalyzer.runProbabilisticCrossValidation(null, phraseTraining,
          smoothingMethod, priorTokenTraining, postTokenTraining, surroundingTokensToUse,
          linkPhraseToPriorTokens, linkPhraseToPostTokens, detailedReport == JOptionPane.YES_OPTION,
          storeResultsAsXML, schemaConceptFile, trainingConcepts, scoringConcepts, textAnalyzer,
          attributeTaggers, null, null, 0, null, sentenceFilter, startOptionIndex, stopOptionIndex,
          contextSifter, false, null);
    }
  }


  /**
   * Starts a new windows for scoring the performance of the RapTAT and Reference XML files.
   *
   * @param thePosition The upper-left corner of the window
   * @param theDimension The dimension of the window
   * @author Sanjib Saha - July 16, 2014
   */
  public void scorePerformance(Point thePosition, Dimension theDimension) {
    try {
      ScorePerformanceWindow.getInstance(thePosition, theDimension);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  // Modified by Sanjib Saha - Jul 22, 2014
  public void trainAnnotator(Point thePosition, Dimension theDimension, JFrame applicationWindow) {
    try {
      TrainingWindow.getInstance(thePosition, theDimension, "training");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public ConceptMapSolution trainConceptMapSolution(String learnMethod, boolean useNulls,
      boolean useStems, boolean usePOS, boolean invertTokenSequence, boolean removeStopWords,
      ContextHandling reasonNoMedsProcessing) {
    ConceptMapSolution curSolution = null;

    // Get the file for training the system
    boolean getGroupingColumns = true;
    DataImportReader fileReader =
        new DataImporter("Select Training Data", getGroupingColumns).getReader();

    if (fileReader != null && fileReader.isValid()) {
      // Set the file for storing the database that contains the solution
      // Uses the name of the training data file along with the date and
      // time and an extension to make it a solution file
      String solutionFileName = fileReader.getFileDirectory()
          + GeneralHelper.removeFileExtension(fileReader.getFileName(), '.') + "_"
          + GeneralHelper.getTimeStamp() + RaptatConstants.SOLUTION_EXTENSION;
      File solutionFile = new File(solutionFileName);

      ConceptMapTrainer curTrainer = null;

      if (solutionFile != null) {
        if (learnMethod.equals(RaptatConstants.CONCEPT_MAPPING_METHODS[0])) {
          curTrainer = new BOWConceptMapTrainer(useNulls, useStems, usePOS, invertTokenSequence,
              removeStopWords, reasonNoMedsProcessing);
        } else if (learnMethod.equals(RaptatConstants.CONCEPT_MAPPING_METHODS[1])) {
          // Set up a trainer to train the system.
          curTrainer = new NBTrainer(useNulls, useStems, usePOS, invertTokenSequence,
              removeStopWords, reasonNoMedsProcessing);
        } else if (learnMethod.equals(RaptatConstants.CONCEPT_MAPPING_METHODS[2])) {
          curTrainer = new NonNBTrainer(useNulls, useStems);
        } else if (learnMethod.equals(RaptatConstants.CONCEPT_MAPPING_METHODS[3])) {
          curTrainer = new DecisionTreeTrainer(useNulls, useStems);
        } else {
          System.out.println("No learning method set");
        }

        // Go ahead and create a solution if able to create a trainer
        if (curTrainer != null) {
          curSolution = curTrainer.getSolution(fileReader);
          GeneralHelper.errorWriter("Trainer created");

          if (curSolution != null) {
            GeneralHelper.errorWriter("Trainer completed: solution created");
            curSolution.saveToFile(solutionFile);
          } else {
            JOptionPane.showMessageDialog(null, "Unable to access data" + " - check format");
          }
        }
        fileReader.close();
      }
    } else {
      JOptionPane.showMessageDialog(null, "Select valid training file before training");
    }

    return curSolution;
  }


  /**
   * Starts a new windows to update the existing solution
   *
   * @param thePosition The upper-left corner of the window
   * @param theDimension The dimension of the window
   */
  public void updateAnnotator(Point thePosition, Dimension theDimension) {
    // NOTE: The class UpdateAnnotationWindow has been deprecated
    // try
    // {
    // UpdateAnnotatorWindow.getInstance( thePosition, theDimension,
    // "update" );
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // }
  }


  public void uploadAnnotations(Point thePosition, Dimension theDimension) {
    try {
      // AnnotationDBWindow.getInstance(thePosition, theDimension);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private ContextSifter loadContextSifters() {
    GeneralHelper.errorWriter("Loading of context sifters must be implemented first");
    System.exit(-1);
    return null;
  }
}
