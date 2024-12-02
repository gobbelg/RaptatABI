package src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Level;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.ConceptBasedPhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.PhraseBasedSentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.tagging.CRFTSFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training.ReasonNoMedsTaggerTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.CrfConceptSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.training.ProbabilisticSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.PhraseIDBootstrapAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScoreObject;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.Annotator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing.AnnotationGroupPostProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AssertionConflictManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeConflictManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.crossvalidation.CVParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.core.crossvalidation.CVParser;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.AttributeTaggerChooser;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.analysis.bootstrap.BootstrapResultWriter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ReasonNoMedsAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.SentenceContextAttributeAnalyzer;

public class PhraseIDCrossValidationAnalyzer extends PhraseIDBootstrapAnalyzer {
  private enum RunType {
    PROBABILISTIC_ANALYSIS, AKI_PROJECT_ANALYSIS, CREATE_GARVIN_ANALYSIS,
    RADIOLOGY_CIRRHOSIS_PROJECT, PTSD_PROJECT_ANALYSIS, AZ_PROJECT_ANALYSIS, PRESSURE_ULCER_ANALYSIS
  }

  protected static final int ONE_SURROUNDING_TOKENS_MASK = 8;

  protected static final int THREE_SURROUNDING_TOKENS_MASK = 16;

  protected static final int SEVEN_SURROUNDING_TOKENS_MASK = 32;

  protected static final int START_OPTION_INDEX_DEFAULT = 56;

  protected static final int END_OPTION_INDEX_DEFAULT = 57;

  protected static final PhraseIDSmoothingMethod PHRASE_ID_SMOOTHING_DEFAULT =
      PhraseIDSmoothingMethod.XTREME_MOD;

  private static final int SURROUNDING_TOKENS_TO_USE_DEFAULT = 0;

  private static final boolean LINK_PHRASE_TO_PRIOR_TOKENS_DEFAULT = false;

  private static final boolean LINK_PHRASE_TO_POST_TOKENS_DEFAULT = false;

  private static final boolean CREATE_DETAILED_REPORT_DEFAULT = true;

  private static final boolean STORE_RESULTS_AS_XML_DEFAULT = true;

  /*
   * This controls whether there is a separate data set for training and testing during
   * cross-validation, which may occur if there are some phrases that the user wants to always
   * include in the training set but never used for testing, such as a pre-defined lexicon of mapped
   * phrases, which could be used to spike the training of the system.
   */
  private static final boolean SEPARATE_DATA_SET_FOR_TESTING_DEFAULT = false;


  protected DecimalFormat doubleFormatter = new DecimalFormat("0.00");


  /*
   * Set to true if a SentenceFilter instance is specified to use only some of the sentences for
   * training
   */
  private boolean sentencesFiltered = false;


  public PhraseIDCrossValidationAnalyzer() {
    PhraseIDBootstrapAnalyzer.LOGGER.setLevel(Level.INFO);
  }


  public PhraseIDCrossValidationAnalyzer(TSFinderMethod phraseIDTrainingMethod,
      PhraseIDSmoothingMethod smoothingMethod, TSFinderMethod priorTokenTrainingMethod,
      TSFinderMethod postTokenTrainingMethod, int surroundingTokensToUse,
      boolean linkPhraseToPriorTokens, boolean linkPhraseToPostTokens) {
    super(phraseIDTrainingMethod, smoothingMethod, priorTokenTrainingMethod,
        postTokenTrainingMethod, surroundingTokensToUse, linkPhraseToPriorTokens,
        linkPhraseToPostTokens);
  }


  /**
   * *********************************************************** Uses bit values to determine option
   * settings for running the training and testing during bootstrapping. It runs through all options
   * settings if it is called 16 times as there are 4 binary options, as set by the binary numbers
   * from 0000 to 1111
   *
   * @author Glenn Gobbel - Jun 18, 2012
   * @param optionsValue
   * @param trainingMethod
   * @return ***********************************************************
   */
  public String setNextOptions(int optionsValue, TSFinderMethod trainingMethod) {
    String optionsString = "";

    if (trainingMethod == TSFinderMethod.PROBABILISTIC) {
      optionsString = setNextProbabilisticOptions(optionsValue);
    } else if (trainingMethod == TSFinderMethod.NAIVEBAYES) {
      optionsString = setNextBayesOptions(optionsValue);
    } else if (trainingMethod == TSFinderMethod.CRF) {
      optionsString = setNextCRFOptions(optionsValue);
    }

    System.out.println("Current options:" + optionsString);
    return optionsString;
  }


  /**
   * This is a method to calculate the precision, recall, and F-measure when using one particular
   * set of options (stemming, stop word removal, etc) during training and testing. It provides
   * metrics using various thresholds as specified by the parameter, 'thresholds'
   *
   * @param fileDirectory
   * @param theSampler
   * @param options
   * @param thresholds
   * @param createDetailedReport
   * @return
   * @author Glenn Gobbel - Mar 25, 2013
   * @param storeResultsAsXML
   * @param trainingConcepts
   * @param schemaConcepts
   * @param attributeTaggers
   * @param attributeConflictManagers
   * @param trainingGroupSize
   * @param contextSifter
   */
  private List<PhraseIDCrossValidationResult> calculateFitMetrics(
      PhraseIDCrossValidationSampler theSampler, String options, double[] thresholds,
      boolean createDetailedReport, boolean storeResultsAsXML, HashSet<String> trainingConcepts,
      HashSet<String> conceptsForAnnotationAndScoring, TextAnalyzer textAnalyzer,
      List<SchemaConcept> schemaConcepts, Collection<AttributeTaggerSolution> attributeTaggers,
      List<AttributeConflictManager> attributeConflictManagers,
      Collection<AnnotationGroupPostProcessor> postProcessors, int trainingGroupSize,
      ContextSifter contextSifter) {
    String resultFileDirectory = theSampler.getTrainingFileDirectory();
    String resultXMLFolder = null;
    if (storeResultsAsXML) {
      resultXMLFolder =
          resultFileDirectory + File.separator + "ResultXML_" + GeneralHelper.getTimeStamp();
      try {
        FileUtils.forceMkdir(new File(resultXMLFolder));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /*
     * Create solution file to store data structures for annotating test documents
     */
    String solutionFileName =
        resultFileDirectory + File.separator + "RapTAT_Res_" + System.currentTimeMillis() + ".soln";
    File solutionFile = new File(solutionFileName);

    /* Create a performance scorer for every setting of threshold */
    List<RaptatPair<Double, PerformanceScorer>> scorerList = createScorerList(options, thresholds,
        createDetailedReport, trainingGroupSize, resultFileDirectory);

    TokenProcessingOptions tokenOptions = new TokenProcessingOptions(this.useStems, this.usePOS,
        this.removeStopWords, this.invertTokenSequence, this.reasonNotOnMedsProcessing,
        this.negationProcessing, this.maxTokenNumber);
    PhraseIDTrainOptions phraseIDOptions = new PhraseIDTrainOptions(this.phraseIDMethod,
        this.priorTokenTrainingMethod, this.postTokenTrainingMethod, this.smoothingMethod,
        this.linkPhraseToPriorTokens, this.linkPhraseToPostTokens, this.surroundingTokensToUse);

    PhraseIDBootstrapAnalyzer.LOGGER.debug("Constructing annotation trainer");
    textAnalyzer.setTokenProcessingParameters(tokenOptions);
    boolean saveUnlabeledToDisk = false;
    ProbabilisticSolutionTrainer tsFinderTrainer = new ProbabilisticSolutionTrainer(tokenOptions,
        phraseIDOptions, trainingConcepts, solutionFile, false, saveUnlabeledToDisk, textAnalyzer);

    /*
     * Regenerate processed tokens in document and annotations, and generated
     * filteredUnlabeledPhraseTree instance and (non-filtered) UnlabeledPhraseTree instance for each
     * document in each AnnotationGroup
     */
    try {
      tsFinderTrainer.prepareForTraining(theSampler.getAllAnnotationGroups(),
          this.invertTokenSequence, this.sentencesFiltered);
    } catch (Exception e1) {
      GeneralHelper.errorWriter(
          "Unable to prepare training sets - trying to use filtered sets of sentences when none exist,"
              + e1.getLocalizedMessage());
      e1.printStackTrace();
    }

    Annotator theAnnotator = getAnnotator(textAnalyzer, attributeTaggers, attributeConflictManagers,
        contextSifter, postProcessors);
    theAnnotator.setConceptsToAnnotate(conceptsForAnnotationAndScoring);

    Iterator<RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>>> sampleGroupIterator;
    sampleGroupIterator = theSampler.iterator(trainingGroupSize);
    int iteration = 1;

    /*
     * Each iteration pulls out one sample for testing and uses the rest of the samples for
     * training. After training, we test using each possible setting of the sequence probability
     * threshold
     */
    while (sampleGroupIterator.hasNext()) {
      RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>> curSample =
          sampleGroupIterator.next();
      List<AnnotationGroup> trainingSample = curSample.left;
      List<AnnotationGroup> testSample = curSample.right;

      /*
       * Switch filtered for standard sentences (but only if filtering has been done). This will
       * make sure the "base" sentences are used for testing.
       */
      if (this.sentencesFiltered) {
        setActiveSentencesToBase(testSample);
      }

      GeneralHelper.tic("Training solution on leave one out samples");

      tsFinderTrainer.updateTraining(trainingSample);
      ProbabilisticTSFinderSolution curSolution =
          (ProbabilisticTSFinderSolution) tsFinderTrainer.updateAndReturnSolution(schemaConcepts);

      PhraseIDBootstrapAnalyzer.LOGGER.info("\nTraining of solution " + iteration + ": complete");
      GeneralHelper.toc();

      GeneralHelper.tic("Identifying phrases and annotations within left out sample");
      theAnnotator.setTokenSequenceFinderSolution(curSolution, trainingConcepts, 0);

      /*
       * Run through the settings for sequence probability threshold
       */
      for (int i = 0; i < thresholds.length; i++) {
        try {
          theAnnotator.setSequenceThreshold(thresholds[i]);
        } catch (Exception e) {
          GeneralHelper.errorWriter(e.getMessage());
        }
        theAnnotator.annotate(testSample);
        RaptatPair<Double, PerformanceScorer> thresholdPerformanceScorer = scorerList.get(i);
        PerformanceScorer performanceScorer = thresholdPerformanceScorer.right;
        performanceScorer.scorePerformanceWithGraph(testSample, schemaConcepts);

        if (storeResultsAsXML) {
          String thresholdString = this.doubleFormatter.format(thresholds[i]);
          thresholdString.replace(".", "p");
          String curResultFolderPath =
              resultXMLFolder + File.separator + "Thresold_" + thresholdString;
          File curResultFolder = new File(curResultFolderPath);
          try {
            FileUtils.forceMkdir(curResultFolder);
          } catch (IOException e) {
            e.printStackTrace();
          }

          boolean ignoreOverwrites = true;
          boolean correctOffsets = true;
          boolean insertPhraseStrings = true;
          XMLExporterRevised exporter =
              new XMLExporterRevised(AnnotationApp.EHOST, curResultFolder, ignoreOverwrites);
          for (AnnotationGroup annotationGroup : testSample) {
            exporter.exportRaptatAnnotationGroup(annotationGroup, correctOffsets,
                insertPhraseStrings);
          }
        }
      }
      PhraseIDBootstrapAnalyzer.LOGGER
          .info("\nTesting of of solution " + iteration++ + " complete");
      GeneralHelper.toc();

      tsFinderTrainer.clearTraining();

      /* Clear RaptatAnnotations in each annotation group */
      for (AnnotationGroup annotationGroup : testSample) {
        annotationGroup.raptatAnnotations.clear();
      }

      /*
       * Now switch back so "base" sentences are the active ones, which is important for the next
       * iteration in the loop as the testSample groups will be used for training
       */
      if (this.sentencesFiltered) {
        setActiveSentencesToFiltered(testSample);
      }
    }

    // Now process all the scorers in the list
    List<PhraseIDCrossValidationResult> performanceList = new ArrayList<>(thresholds.length);
    for (int i = 0; i < thresholds.length; i++) {
      PerformanceScorer curPerformanceScorer = scorerList.get(i).right;
      performanceList.add(new PhraseIDCrossValidationResult(thresholds[i], curPerformanceScorer,
          options, resultFileDirectory));
      if (createDetailedReport) {
        curPerformanceScorer.closeWriter();
      }
    }

    return performanceList;
  }


  private void cleanUp(String fileDirectory) {
    File theDirectory = new File(fileDirectory);
    String[] fileTypes = {".ser", ".rpt", ".xml", ".soln"};
    File[] filesForDeletion = theDirectory.listFiles(GeneralHelper.getFileFilter(fileTypes));
    for (File curFile : filesForDeletion) {
      curFile.delete();
    }
  }


  /*
   * Creates a filtered sentence set within the RaptatDocument associated with each AnnotationGroup
   * instance. Assumes that sentenceFilter is NOT NULL.
   */
  private void createFilteredSentences(PhraseIDCrossValidationSampler theSampler,
      SentenceFilter sentenceFilter) {
    for (AnnotationGroup group : theSampler.getAllAnnotationGroups()) {
      group.createFilteredSentences(sentenceFilter, group.referenceAnnotations);
    }
  }

  private List<RaptatPair<Double, PerformanceScorer>> createScorerList(String options,
      double[] thresholds, boolean createDetailedReport, int trainingGroupSize,
      String resultFileDirectory) {
    List<RaptatPair<Double, PerformanceScorer>> scorerList = new ArrayList<>(thresholds.length);
    for (int i = 0; i < thresholds.length; i++) {
      String thresholdMark = this.doubleFormatter.format(thresholds[i]).replace(".", "p");
      String settingsMark =
          "_" + options + "_" + thresholdMark + "_GroupSize" + trainingGroupSize + "_";
      scorerList.add(new RaptatPair<>(thresholds[i],
          new PerformanceScorer(resultFileDirectory, createDetailedReport, settingsMark)));
    }
    return scorerList;
  }

  /**
   * @param textAnalyzer
   * @param attributeTaggers
   * @param attributeConflictManagers
   * @param contextSifter
   * @return
   */
  private Annotator getAnnotator(TextAnalyzer textAnalyzer,
      Collection<AttributeTaggerSolution> attributeTaggers,
      List<AttributeConflictManager> attributeConflictManagers, ContextSifter contextSifter,
      Collection<AnnotationGroupPostProcessor> postProcessors) {
    PhraseIDBootstrapAnalyzer.LOGGER.debug("Constructing annotator");
    Annotator theAnnotator = new Annotator(textAnalyzer, attributeTaggers);
    theAnnotator.addContextAttributeAnalyzer(new SentenceContextAttributeAnalyzer("assertionstatus",
        "allergy", "positive", "negative", false));
    theAnnotator.addAttributeConflictManagers(attributeConflictManagers);
    theAnnotator.setPostProcessors(postProcessors);

    if (OptionsManager.getInstance().getUseReasonNoMedsContext()) {
      theAnnotator.addContextAttributeAnalyzer(new ReasonNoMedsAnalyzer());
      theAnnotator.generateReasonNoMedsContextSifters();
    }
    theAnnotator.setContextSifter(contextSifter);
    return theAnnotator;
  }


  /**
   * Oct 16, 2017
   *
   * @param createDetailedReport
   * @param storeResultsAsXML
   * @param trainingConcepts
   * @param textAnalyzer
   * @param attributeTaggers
   * @param attributeConflictManagers
   * @param trainingGroupSize
   * @param optionsString
   * @param theSampler
   * @param schemaConcepts
   * @param contextSifters
   * @throws IOException
   */
  private void runOptionSetting(boolean createDetailedReport, boolean storeResultsAsXML,
      HashSet<String> trainingConcepts, HashSet<String> conceptsForAnnotationAndScoring,
      TextAnalyzer textAnalyzer, Collection<AttributeTaggerSolution> attributeTaggers,
      List<AttributeConflictManager> attributeConflictManagers,
      Collection<AnnotationGroupPostProcessor> postProcessors, int trainingGroupSize,
      String optionsString, PhraseIDCrossValidationSampler theSampler,
      List<SchemaConcept> schemaConcepts, ContextSifter contextSifters) throws IOException {
    List<PhraseIDCrossValidationResult> resultList =
        calculateFitMetrics(theSampler, optionsString, RaptatConstants.SEQUENCE_THRESHOLDS_DEFAULT,
            createDetailedReport, storeResultsAsXML, trainingConcepts,
            conceptsForAnnotationAndScoring, textAnalyzer, schemaConcepts, attributeTaggers,
            attributeConflictManagers, postProcessors, trainingGroupSize, contextSifters);

    for (PhraseIDCrossValidationResult curResult : resultList) {
      double curThreshold = curResult.getSequenceThreshold();
      Hashtable<PerformanceScoreObject, int[]> curScores = curResult.getConceptMatchScore();
      BootstrapResultWriter conceptResultWriter =
          new BootstrapResultWriter(curResult.getResultDirectory() + File.separator,
              "CV_Summary" + optionsString + "_" + Double.toString(curThreshold).replace(".", "p")
                  + "_GrpSz" + trainingGroupSize + "_",
              RaptatConstants.CROSS_VALIDATION_CONCEPT_RESULT_TITLES);

      if (curScores != null) {
        conceptResultWriter.writeResults(curScores);
      }
      conceptResultWriter.close();
    }
  }


  /**
   * Method to exchange the filtered and unfiltered sentences in an AnnotationGroup collection.
   * Useful when only using some filtered sentence for training rather than all the sentences in the
   * document set.
   *
   * @param annotationGroups
   */
  private void setActiveSentencesToBase(List<AnnotationGroup> annotationGroups) {
    for (AnnotationGroup group : annotationGroups) {
      group.activateBaseSentences();
    }
  }


  /**
   * Method to exchange the filtered and unfiltered sentences in an AnnotationGroup collection.
   * Useful when only using some filtered sentence for training rather than all the sentences in the
   * document set.
   *
   * @param annotationGroups
   */
  private void setActiveSentencesToFiltered(List<AnnotationGroup> annotationGroups) {
    try {
      for (AnnotationGroup group : annotationGroups) {
        group.activateFilteredSentences();
      }
    } catch (Exception e) {
      GeneralHelper
          .errorWriter("Attempting to use filtered sentences for training when none exist\n"
              + e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }


  private String setNextBayesOptions(int optionsValue) {
    String optionsString = "CrossValidation_Bayes_";
    optionsValue %= 192;

    // '&' is the 'and' bit operator
    this.useStems = (optionsValue & PhraseIDBootstrapAnalyzer.lemmatizationMask) > 0 ? true : false;
    this.usePOS = (optionsValue & PhraseIDBootstrapAnalyzer.posMask) > 0 ? true : false;
    this.removeStopWords =
        (optionsValue & PhraseIDBootstrapAnalyzer.stopWordsMask) > 0 ? true : false;
    this.invertTokenSequence = false;

    if ((optionsValue & PhraseIDCrossValidationAnalyzer.SEVEN_SURROUNDING_TOKENS_MASK) > 0) {
      this.surroundingTokensToUse = 7;
      optionsString += "7_";
    } else if ((optionsValue & PhraseIDCrossValidationAnalyzer.THREE_SURROUNDING_TOKENS_MASK) > 0) {
      // If both 8 and 16 bit are set, then use
      // 5 surrounding tokens
      if ((optionsValue & PhraseIDCrossValidationAnalyzer.ONE_SURROUNDING_TOKENS_MASK) > 0) {
        this.surroundingTokensToUse = 5;
        optionsString += "5_";
      }
      // If only 16 bit set, use 3 surrounding tokens
      else {
        this.surroundingTokensToUse = 3;
        optionsString += "3_";
      }
    } else if ((optionsValue & PhraseIDCrossValidationAnalyzer.ONE_SURROUNDING_TOKENS_MASK) > 0) {
      this.surroundingTokensToUse = 1;
      optionsString += "1_";
    } else {
      this.surroundingTokensToUse = 0;
      optionsString += "0_";
    }

    // Return string that represents the settings of the options currently
    // used. Upper case stands for ones that are true, and lower case
    // stands for those that are false
    optionsString += (this.useStems ? "S" : "s") + (this.usePOS ? "P" : "p")
        + (this.removeStopWords ? "R" : "r");

    return optionsString;
  }


  /**
   * @param optionsValue
   * @return
   */
  private String setNextCRFOptions(int optionsValue) {
    throw new NotImplementedException(
        "TODO - " + this.getClass().getName() + " not yet fully implemented");
  }


  private String setNextProbabilisticOptions(int optionsValue) {
    String optionsString = "CrossValidation_Prob_";
    optionsValue %= 96;

    // '&' is the 'and' bit operator
    this.invertTokenSequence =
        (optionsValue & PhraseIDBootstrapAnalyzer.invertTokenSequenceMask) > 0 ? true : false;
    this.useStems = (optionsValue & PhraseIDBootstrapAnalyzer.lemmatizationMask) > 0 ? true : false;
    this.usePOS = (optionsValue & PhraseIDBootstrapAnalyzer.posMask) > 0 ? true : false;
    this.removeStopWords =
        (optionsValue & PhraseIDBootstrapAnalyzer.stopWordsMask) > 0 ? true : false;

    // if ( ( optionsValue & xtremeMask ) > 0 )
    // {
    // if ( ( optionsValue & extendedMask ) > 0 )
    // {
    // smoothingMethod = PhraseIDSmoothing.XTREME_MOD;
    // optionsString += "XM_";
    // }
    // else
    // {
    // smoothingMethod = PhraseIDSmoothing.XTREME;
    // optionsString += "X_";
    // }
    // }
    // else if ( ( optionsValue & extendedMask ) > 0 )
    // {
    // smoothingMethod = PhraseIDSmoothing.EXTENDED;
    // optionsString += "E_";
    // }
    // else
    // {
    // smoothingMethod = PhraseIDSmoothing.UNSMOOTHED;
    // optionsString += "U_";
    // }

    if ((optionsValue & PhraseIDBootstrapAnalyzer.xtremeMask) > 0) {

      this.smoothingMethod = PhraseIDSmoothingMethod.XTREME_MOD;
      optionsString += "XM_";

    } else {
      this.smoothingMethod = PhraseIDSmoothingMethod.XTREME;
      optionsString += "X_";
    }

    if ((optionsValue & PhraseIDBootstrapAnalyzer.sevenMaxTokensMask) > 0) {
      this.maxTokenNumber = 7;
    } else if ((optionsValue & PhraseIDBootstrapAnalyzer.tenMaxTokensMask) > 0) {
      this.maxTokenNumber = 10;
    } else {
      this.maxTokenNumber = 3;
    }

    // Return string that represents the settings of the options currently
    // used. Upper case stands for ones that are true, and lower case
    // stands for those that are false
    optionsString += Integer.toString(this.maxTokenNumber) + (this.useStems ? "S" : "s")
        + (this.usePOS ? "P" : "p") + (this.removeStopWords ? "R" : "r")
        + (this.invertTokenSequence ? "I" : "i");

    PhraseIDBootstrapAnalyzer.LOGGER
        .debug("\nIteration:" + optionsValue + "\tOptions:" + optionsString);
    return optionsString;
  }


  public static void addSectionAndSentenceMatchers(TextAnalyzer textAnalyzer,
      CVParameterObject cvParameters) {
    Map<RaptatConstants.SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers =
        new HashMap<>(2);

    List<RaptatPair<String, String>> sectionMatchers = cvParameters.getSectionMatchers();
    if (sectionMatchers != null) {
      sentenceAndSectionMatchers.put(RaptatConstants.SentenceSectionTaggerType.SECTION,
          sectionMatchers);
    }

    List<RaptatPair<String, String>> sentenceMatchers = cvParameters.getSentenceMatchers();
    if (sentenceMatchers != null) {
      sentenceAndSectionMatchers.put(RaptatConstants.SentenceSectionTaggerType.SENTENCE,
          sentenceMatchers);
    }

    textAnalyzer.addSentenceAndSectionMatchers(sentenceAndSectionMatchers);
  }


  public static Collection<AttributeTaggerSolution> getAttributeTaggers() {
    AttributeTaggerChooser taggerChooser =
        AttributeTaggerChooser.getInstance(null, null, null, null);
    return taggerChooser.getTaggers();
  }


  public static HashSet<String> getConcepts() {
    return getConcepts(null);
  }


  public static HashSet<String> getConcepts(String conceptsFilePath) {
    return getConcepts(conceptsFilePath,
        "Select text file with concepts to be included during processing");
  }


  public static HashSet<String> getConcepts(String conceptsFilePath, String queryString) {
    HashSet<String> conceptSet = new HashSet<>();

    CSVReader conceptReader = null;
    if (conceptsFilePath != null && !conceptsFilePath.isEmpty()) {
      File conceptsFile = new File(conceptsFilePath);
      if (conceptsFile.exists() && conceptsFile.canRead()) {
        conceptReader = new CSVReader(conceptsFile);
      }
    }

    if (conceptReader == null) {
      conceptReader = new CSVReader(queryString);
    }

    if (conceptReader.isValid()) {
      String[] nextDataSet;
      while ((nextDataSet = conceptReader.getNextData()) != null) {
        conceptSet.add(nextDataSet[0].toLowerCase());
      }
      conceptReader.close();
      OptionsManager.getInstance().setLastSelectedDir(new File(conceptReader.getInFileDirectory()));
    }

    return conceptSet;
  }


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    if (lvgSetting > 0) {
      SwingUtilities.invokeLater(new Runnable() {
        /*
         * This block contains parameters that are generally set before running a leave-one-out
         * cross-validation. They are generally unchanged during running, unlike some other
         * parameters.
         */
        String directoryPath;
        File schemaConceptFile;
        HashSet<String> trainingConcepts;
        Collection<AttributeTaggerSolution> attributeTaggers;
        String facilitatorBlockerPath;


        @Override
        public void run() {
          CVParser parser = new CVParser();
          CVParameterObject parameters = parser.parseCommandLine(args);

          try {
            PhraseIDCrossValidationAnalyzer.runProbabilisticCrossValidation(parameters);
          } catch (NotImplementedException e) {
            System.err.println("Unable to run parsed parameters " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(-1);
          }

          // /*
          // * trainingGroupSize determines how many groups are used
          // for training in
          // * cross-validation. If the value is set to less than 1,
          // leave-one-out
          // * cross-validation is used
          // */
          // int trainingGroupSize = 0;
          // RunType runType = RunType.RADIOLOGY_CIRRHOSIS_PROJECT;
          // int startOptionIndex = START_OPTION_INDEX_DEFAULT;
          // int endOptionIndex = END_OPTION_INDEX_DEFAULT;
          //
          // switch (runType)
          // {
          // case AZ_PROJECT_ANALYSIS:
          // astraZenecaConstipationCrossValidation(
          // trainingGroupSize, startOptionIndex,
          // endOptionIndex );
          // break;
          // case PROBABILISTIC_ANALYSIS:
          // setParameters();
          // TextAnalyzer ta = new TextAnalyzer(
          // facilitatorBlockerPath );
          // List<AttributeConflictManager> attributeConflictManagers
          // = new
          // ArrayList<AttributeConflictManager>();
          // probabilisticAnalysis( schemaConceptFile,
          // acceptedConcepts, ta,
          // attributeTaggers,
          // attributeConflictManagers, trainingGroupSize, null,
          // startOptionIndex,
          // endOptionIndex );
          // break;
          //
          // case AKI_PROJECT_ANALYSIS:
          // akiProjectCrossValidation( trainingGroupSize,
          // startOptionIndex,
          // endOptionIndex );
          // break;
          //
          // case PTSD_PROJECT_ANALYSIS:
          // ptsdProjectCrossValidation( trainingGroupSize,
          // startOptionIndex,
          // endOptionIndex );
          // break;
          //
          // case RADIOLOGY_CIRRHOSIS_PROJECT:
          // radiologyCirrhosisCrossValidation( trainingGroupSize,
          // startOptionIndex,
          // endOptionIndex );
          // break;
          //
          // case CREATE_GARVIN_ANALYSIS:
          // acceptedConcepts = new HashSet<String>();
          // acceptedConcepts.add( "reasonnomeds" );
          // String dictionaryPath =
          // "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\ConceptFeatureDictionary_160512.txt";
          //
          // String solutionFilesDirectoryPath =
          // "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CV10FoldSolution_Filtered_Training_OvertRnmSelectedSent_160627_154351";
          // String cvGroupsFilePath =
          // "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\"
          // + "CrossValidGroups_10Fold_ForTraining_160613.csv";
          //
          // List<RaptatPair<File, List<RaptatPair<File, File>>>>
          // solutionAndTestList =
          // getGarvinCreateProjectParameters(
          // solutionFilesDirectoryPath, cvGroupsFilePath );
          //
          // List<AnnotationGroup> raptatAndReferenceList;
          // raptatAndReferenceList = garvinProjectCrossValidation(
          // solutionAndTestList,
          // dictionaryPath );
          //
          // String fileTag = "ReasNoMed_Rd01";
          // Hashtable<String, int[]> resultScores =
          // scoreGarvinProjectPerformance(
          // solutionFilesDirectoryPath, fileTag,
          // raptatAndReferenceList );
          // printGarvinResultScores( solutionFilesDirectoryPath,
          // fileTag, resultScores );
          // break;
          // case PRESSURE_ULCER_ANALYSIS:
          // pressureUlcerCrossValidation( trainingGroupSize,
          // startOptionIndex,
          // endOptionIndex );
          // break;
          // default:
          // break;
          // }
          System.exit(0);
        }


        private void akiProjectCrossValidation(int trainingGroupSize, int startOptionIndex,
            int endOptionIndex) {
          String schemaConceptFilePath =
              "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\AKIProjectSchemaNoUncertain.xml";
          this.schemaConceptFile = new File(schemaConceptFilePath);

          /*
           * Uncomment this code to use dictionary during cross-validation
           */
          String dictionaryFilePath =
              "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\BlankDictionary_v01_170705.txt";
          TextAnalyzer textAnalyzer = new TextAnalyzer(dictionaryFilePath, null);
          // TextAnalyzer textAnalyzer = new TextAnalyzer();

          textAnalyzer.addSectionMatcher("allergy",
              "^\\s*(?:\\w+\\s+)?(?:allerg|allrg|alerg|alleg|adverse|advrs).*");
          textAnalyzer.addSentenceMatcher("allergy",
              "^\\s*(?:\\w+\\s+)?(?:allerg|allrg|alerg|alleg|adverse|advrs).*");

          String trainingConceptsPath =
              "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\AcceptableConcepts_160606.txt";

          this.trainingConcepts = getConcepts(trainingConceptsPath);
          HashSet<String> scoringConcepts = this.trainingConcepts;

          this.attributeTaggers = getAttributeTaggers();

          List<AttributeConflictManager> attributeConflictManagers;
          attributeConflictManagers = new ArrayList<>();
          attributeConflictManagers.add(new AssertionConflictManager());
          Collection<AnnotationGroupPostProcessor> postProcessors = new ArrayList<>();

          probabilisticAnalysis(this.schemaConceptFile, this.trainingConcepts, scoringConcepts,
              textAnalyzer, this.attributeTaggers, attributeConflictManagers, postProcessors,
              trainingGroupSize, null, startOptionIndex, endOptionIndex, null, false, null);
        }


        private void astraZenecaConstipationCrossValidation(int trainingGroupSize,
            int startOptionIndex, int endOptionIndex) {
          String schemaConceptFilePath =
              "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\projectschema_NoUncertainHypothetical.xml";
          this.schemaConceptFile = new File(schemaConceptFilePath);
          String dictionaryFilePath =
              "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\Constipation_Dictionary_v11_170521.txt";
          TextAnalyzer textAnalyzer = new TextAnalyzer(dictionaryFilePath, null);
          String acceptedConceptsPath =
              "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\AcceptedConcepts_170504.txt";

          /*
           * Generally, we have used this to adjudicate differences i negation due to allergies to
           * medications. We keep this here because, even though we are not annotating or
           * identifying allergy sections, the call to the method
           * calculateProbabilisticFitMetricsUpdated currently includes an annotator that responds
           * to allergy annotations. Without this, the assertionStatus attribute will be assigned
           * twice.
           */
          List<AttributeConflictManager> attributeConflictManagers = new ArrayList<>();
          attributeConflictManagers.add(new AssertionConflictManager());

          this.trainingConcepts = getConcepts(acceptedConceptsPath);
          HashSet<String> scoringConcepts = this.trainingConcepts;
          this.attributeTaggers = getAttributeTaggers();
          Collection<AnnotationGroupPostProcessor> postProcessors = new ArrayList<>();

          probabilisticAnalysis(this.schemaConceptFile, this.trainingConcepts, scoringConcepts,
              textAnalyzer, this.attributeTaggers, attributeConflictManagers, postProcessors,
              trainingGroupSize, null, startOptionIndex, endOptionIndex, null, false, null);
        }


        private List<AnnotationGroup> garvinProjectCrossValidation(
            List<RaptatPair<File, List<RaptatPair<File, File>>>> solutionAndTestList,
            String dictionaryPath) {
          List<AnnotationGroup> raptatReferenceList = new ArrayList<>();

          /*
           * The trainer instance will be used to get the documents and their sentences as well as
           * import the references. It's important to make sure that the dictionary and token
           * processing options are the same as those used to create the feature files and CRF based
           * solution that will be used here.
           */
          TokenProcessingOptions options = new TokenProcessingOptions(OptionsManager.getInstance());
          ReasonNoMedsTaggerTrainer trainer =
              new ReasonNoMedsTaggerTrainer(options, dictionaryPath, null);
          int solutionFileNumber = 0;

          for (RaptatPair<File, List<RaptatPair<File, File>>> curSolutionAndTest : solutionAndTestList) {
            CRFTSFinder annotator = this.getAnnotator(curSolutionAndTest.left);
            List<RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>>> documentList =
                getGarvinTestDocumentList(curSolutionAndTest.right, trainer);

            System.out.println("\n============================\"\nTesting solution "
                + ++solutionFileNumber + " of " + solutionAndTestList.size() + ":"
                + curSolutionAndTest.left.getName() + "\n============================\n");
            for (RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> curPair : documentList) {
              List<AnnotatedPhrase> sentences = curPair.left;

              if (sentences != null && !sentences.isEmpty()) {
                RaptatDocument document = sentences.get(0).getDocumentOfOrigin();
                List<AnnotatedPhrase> referenceAnnotations = curPair.right;
                List<AnnotatedPhrase> raptatAnnotations = annotator.getAnnotations(sentences);

                if (!referenceAnnotations.isEmpty() || !raptatAnnotations.isEmpty()) {
                  AnnotationGroup resultGroup = new AnnotationGroup(document, referenceAnnotations);
                  resultGroup.setRaptatAnnotations(raptatAnnotations);
                  raptatReferenceList.add(resultGroup);
                }
              }
            }
          }

          return raptatReferenceList;
        }


        private CRFTSFinder getAnnotator(File crfSolutionFile) {
          CrfConceptSolution solution =
              (CrfConceptSolution) TokenSequenceFinderSolution.loadFromFile(crfSolutionFile);
          CRFTSFinder annotator = new CRFTSFinder(solution);

          return annotator;
        }


        private Collection<File> getFileListRecursively(String pathToFilesDirectory,
            String matchString) {
          Collection<File> fileList = new ArrayList<>();

          if (pathToFilesDirectory == null || pathToFilesDirectory.isEmpty()) {
            pathToFilesDirectory =
                GeneralHelper.getDirectory("Select directory containing files").getAbsolutePath();
          }

          fileList = FileUtils.listFiles(new File(pathToFilesDirectory),
              new RegexFileFilter(matchString), TrueFileFilter.INSTANCE);
          return fileList;
        }


        /**
         * Creates a list of RaptatPair instances where each instance represents one train and test
         * element for n-fold cross-validation where n is the length of the returned list. The left
         * element of each pair is a file containing the RapTAT solution which has already been
         * trained on a distinct set of data; the right element contains another pair representing
         * the test group for testing the solution on. The left element of the test group is the
         * text file, and the right element of the test group is the xml file containing the
         * reference annotations for comparing to the generated annotations.
         *
         * @param pathToSolutions
         * @param cvGroupsFilePath
         * @return
         */
        private List<RaptatPair<File, List<RaptatPair<File, File>>>> getGarvinCreateProjectParameters(
            String pathToSolutions, String cvGroupsFilePath) {
          List<RaptatPair<File, List<RaptatPair<File, File>>>> solutionAndTestList =
              new ArrayList<>();
          Collection<File> solutionFiles = getFileListRecursively(pathToSolutions, ".*.atsn");

          HashMap<String, List<RaptatPair<File, File>>> testGroupFiles =
              GeneralHelper.getCVGroupFiles(cvGroupsFilePath);
          for (File curSolutionFile : solutionFiles) {
            List<String> matches = new ArrayList<>();
            try {
              String fileName = curSolutionFile.getName();
              Pattern regexPattern = Pattern.compile("Group_(\\d{1,2}).*", Pattern.MULTILINE);
              Matcher regexMatcher = regexPattern.matcher(fileName);
              while (regexMatcher.find()) {
                matches.add(regexMatcher.group(1));
              }
            } catch (PatternSyntaxException ex) {
              GeneralHelper.errorWriter(ex.getMessage());
            }
            /* Get rid of any leading 0's */
            String cvGroup = matches.get(0);
            List<RaptatPair<File, File>> testFiles = testGroupFiles.get(cvGroup);
            RaptatPair<File, List<RaptatPair<File, File>>> curSolutionTestSet =
                new RaptatPair<>(curSolutionFile, testFiles);
            solutionAndTestList.add(curSolutionTestSet);
          }

          return solutionAndTestList;
        }


        /**
         * Takes a list of RaptatPair instances, with the left side of the pair corresponding to a
         * document and the right side corresponding to an xml file (holding reference annotations
         * for the document), and gets the sentences in each document as a list of annotated phrases
         * and gets the annotations of the xml, also as a list of annotated phrases. It pairs the
         * two lists together as a new RaptatPair and adds the pair to a list for return.
         *
         * <p>
         * Note that reference phrase annotations are only returned if they have the concept
         * "reasonnomeds" with an 'reasontype' attribute of "overt."
         *
         * @param testFileList
         * @param trainer
         * @return
         */
        private List<RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>>> getGarvinTestDocumentList(
            List<RaptatPair<File, File>> testFileList, ReasonNoMedsTaggerTrainer trainer) {
          List<RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>>> documentList =
              new ArrayList<>();

          for (RaptatPair<File, File> curTestPair : testFileList) {
            File textFile = curTestPair.left;
            File xmlFile = curTestPair.right;
            RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> testingData =
                trainer.getDocumentTestingData(textFile, xmlFile, this.trainingConcepts);
            documentList.add(new RaptatPair<>(testingData.left, testingData.right));
          }

          return documentList;
        }


        private void pressureUlcerCrossValidation(int trainingGroupSize, int startOptionIndex,
            int endOptionIndex) {
          String schemaConceptFilePath =
              "P:\\ORD_Luther_201308073D\\Glenn\\AnnotationsPass2ForNLP\\PressureUlcerProjectSchema.xml";
          this.schemaConceptFile = new File(schemaConceptFilePath);

          /*
           * Comment Uncomment this code to use/not use dictionary during cross-validation
           */
          String dictionaryFilePath = "";
          TextAnalyzer textAnalyzer = new TextAnalyzer(dictionaryFilePath, null);
          // TextAnalyzer textAnalyzer = new TextAnalyzer();

          String trainingConceptsPath =
              "P:\\ORD_Luther_201308073D\\Glenn\\AnnotationsPass2ForNLP\\LivingSituationCueConcept_170517.txt";

          this.trainingConcepts = getConcepts(trainingConceptsPath);
          HashSet<String> scoringConcepts = this.trainingConcepts;
          this.attributeTaggers = getAttributeTaggers();

          List<AttributeConflictManager> attributeConflictManagers;
          attributeConflictManagers = new ArrayList<>();
          Collection<AnnotationGroupPostProcessor> postProcessors = new ArrayList<>();

          // attributeConflictManagers.add( new
          // AssertionConflictManager() );

          PhraseFilter phraseFilter = new ConceptBasedPhraseFilter(this.trainingConcepts);
          SentenceFilter sentenceFilter = new PhraseBasedSentenceFilter(phraseFilter);

          probabilisticAnalysis(this.schemaConceptFile, this.trainingConcepts, scoringConcepts,
              textAnalyzer, this.attributeTaggers, attributeConflictManagers, postProcessors,
              trainingGroupSize, sentenceFilter, startOptionIndex, endOptionIndex, null, false,
              null);
        }


        private void printGarvinResultScores(String solutionFilesDirectoryPath, String fileTag,
            Hashtable<String, int[]> resultScores) {
          BootstrapResultWriter conceptResultWriter =
              new BootstrapResultWriter(
                  solutionFilesDirectoryPath + File.separator, "CrossValidGarvin_Summary_" + fileTag
                      + "_" + GeneralHelper.getTimeStamp() + ".txt",
                  RaptatConstants.CROSS_VALIDATION_CONCEPT_RESULT_TITLES);

          if (resultScores != null) {
            conceptResultWriter.writeResults(resultScores);
          }
          conceptResultWriter.close();
        }


        private void probabilisticAnalysis(File schemaConceptFile, HashSet<String> trainingConcepts,
            HashSet<String> scoringConcepts, TextAnalyzer ta,
            Collection<AttributeTaggerSolution> attributeTaggers,
            List<AttributeConflictManager> attributeConflictManagers,
            Collection<AnnotationGroupPostProcessor> postProcessors, int trainingGroupSize,
            SentenceFilter sentenceFilter, int startOptionIndex, int endOptionIndex,
            ContextSifter contextSifter, boolean separateTestData, String testDataDirectoryPath) {
          TSFinderMethod trainingMethod = TSFinderMethod.PROBABILISTIC;

          PhraseIDCrossValidationAnalyzer.runProbabilisticCrossValidation(null, trainingMethod,
              null, trainingMethod, trainingMethod, 0, false, false, true, true, schemaConceptFile,
              trainingConcepts, scoringConcepts, ta, attributeTaggers, attributeConflictManagers,
              postProcessors, trainingGroupSize, null, sentenceFilter, startOptionIndex,
              endOptionIndex, contextSifter, separateTestData, testDataDirectoryPath);
          reportOptionSettings(trainingMethod);
        }


        private void ptsdProjectCrossValidation(int trainingGroupSize, int startOptionIndex,
            int endOptionIndex) {
          String schemaConceptFilePath =
              "D:\\MHA_NLP\\Glenn\\PtsdProjectSchema_ValuesToConcepts_161209.xml";
          this.schemaConceptFile = new File(schemaConceptFilePath);

          String dictionaryPath = "D:\\MHA_NLP\\Glenn\\facilitatorBlockerDictionary_v02_170112.txt";
          TextAnalyzer textAnalyzer = new TextAnalyzer(dictionaryPath, null);

          String trainingConceptsPath =
              "D:\\MHA_NLP\\Glenn\\IBMCorpusForInitialRaptatTraining_161209\\AcceptableConcepts_161209.txt";
          this.trainingConcepts = getConcepts(trainingConceptsPath);
          HashSet<String> scoringConcepts = this.trainingConcepts;
          this.attributeTaggers = getAttributeTaggers();

          List<AttributeConflictManager> attributeConflictManagers;
          attributeConflictManagers = new ArrayList<>();
          Collection<AnnotationGroupPostProcessor> postProcessors = new ArrayList<>();

          probabilisticAnalysis(this.schemaConceptFile, this.trainingConcepts, scoringConcepts,
              textAnalyzer, this.attributeTaggers, attributeConflictManagers, postProcessors,
              trainingGroupSize, null, startOptionIndex, endOptionIndex, null, false, null);
        }


        private void radiologyCirrhosisCrossValidation(int trainingGroupSize, int startOptionIndex,
            int endOptionIndex) {
          String schemaConceptFilePath =
              "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\projectschema.xml";
          String trainingConceptsPath =
              "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\AcceptedConcepts.txt";
          String dictionaryPath =
              "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CirrhosisLexicon_170308.txt";
          this.schemaConceptFile = new File(schemaConceptFilePath);
          TextAnalyzer textAnalyzer = new TextAnalyzer(dictionaryPath, null);

          /*
           * Generally, we have used this to adjudicate differences in negation due to allergies to
           * medications. We keep this here because, even though we are not annotating or
           * identifying allergy sections, the call to the method
           * calculateProbabilisticFitMetricsUpdated currently includes an annotator that responds
           * to allergy annotations. Without this, the assertionStatus attribute will be assigned
           * twice.
           */
          List<AttributeConflictManager> attributeConflictManagers = new ArrayList<>();
          attributeConflictManagers.add(new AssertionConflictManager());
          Collection<AnnotationGroupPostProcessor> postProcessors = new ArrayList<>();

          this.trainingConcepts = getConcepts(trainingConceptsPath);
          HashSet<String> scoringConcepts = this.trainingConcepts;
          this.attributeTaggers = getAttributeTaggers();

          probabilisticAnalysis(this.schemaConceptFile, this.trainingConcepts, scoringConcepts,
              textAnalyzer, this.attributeTaggers, attributeConflictManagers, postProcessors,
              trainingGroupSize, null, startOptionIndex, endOptionIndex, null, false, null);
        }


        private void reportOptionSettings(TSFinderMethod trainingMethod) {
          PhraseIDCrossValidationAnalyzer theAnalyzer = new PhraseIDCrossValidationAnalyzer();
          for (int i =
              PhraseIDCrossValidationAnalyzer.START_OPTION_INDEX_DEFAULT; i < PhraseIDCrossValidationAnalyzer.END_OPTION_INDEX_DEFAULT; i++) {
            String optionSettings = theAnalyzer.setNextOptions(i, trainingMethod);
            System.out.println(i + ") OptionsSettings:" + optionSettings);
          }
        }


        private Hashtable<PerformanceScoreObject, int[]> scoreGarvinProjectPerformance(
            String solutionFilesDirectoryPath, String fileTag,
            List<AnnotationGroup> raptatAndReferenceList) {
          boolean createDetailedReport = true;
          PerformanceScorer ps =
              new PerformanceScorer(solutionFilesDirectoryPath, createDetailedReport, fileTag);
          Hashtable<PerformanceScoreObject, int[]> resultScores =
              ps.scorePerformance(raptatAndReferenceList);
          return resultScores;
        }


        private void setParameters() {
          this.directoryPath = OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath();
          this.schemaConceptFile =
              GeneralHelper.getFile("Select file containing schema", this.directoryPath);
          File facilitatorBlockerFile = GeneralHelper
              .getFile("Select facilitator/blocker dicitonary file", this.directoryPath);
          if (facilitatorBlockerFile != null) {
            this.facilitatorBlockerPath = facilitatorBlockerFile.getAbsolutePath();
          }
          this.trainingConcepts = getConcepts();
          this.attributeTaggers = getAttributeTaggers();
        }
      });
    }
  }


  /**
   * @param trainingDataFilePath
   * @param phraseIDTrainingMethod
   * @param smoothingMethod
   * @param linkPhraseToPostTokens
   * @param linkPhraseToPriorTokens
   * @param surroundingTokensToUse
   * @param postTokenTraining
   * @param priorTokenTraining
   * @param createDetailedReport
   * @param storeResultsAsXML
   * @param schemaConceptFile
   * @param trainingConcepts
   * @param textAnalyzer
   * @param attributeTaggers
   * @param attributeConflictManagers
   * @param trainingGroupSize
   * @param annotationApp
   * @param sentenceFilter
   * @param contextSifters
   * @param filterAnnotations
   */
  public static void runProbabilisticCrossValidation(String trainingDataFilePath,
      TSFinderMethod phraseIDTrainingMethod, PhraseIDSmoothingMethod smoothingMethod,
      TSFinderMethod priorTokenTraining, TSFinderMethod postTokenTraining,
      int surroundingTokensToUse, boolean linkPhraseToPriorTokens, boolean linkPhraseToPostTokens,
      boolean createDetailedReport, boolean storeResultsAsXML, File schemaConceptFile,
      HashSet<String> trainingConcepts, HashSet<String> scoringConcepts, TextAnalyzer textAnalyzer,
      Collection<AttributeTaggerSolution> attributeTaggers,
      List<AttributeConflictManager> attributeConflictManagers,
      Collection<AnnotationGroupPostProcessor> postProcessors, int trainingGroupSize,
      AnnotationApp annotationApp, SentenceFilter sentenceFilter, int startOptionIndex,
      int endOptionIndex, ContextSifter contextSifters, boolean separateTestingData,
      String testingDataDirectoryPath) {

    String optionsString;

    /*
     * Create actual instance of a PhraseIDCrossValidationAnalyzer to run the analysis (we're
     * currently in a static method)
     */
    PhraseIDCrossValidationAnalyzer leaveOneAnalyzer = new PhraseIDCrossValidationAnalyzer(
        phraseIDTrainingMethod, smoothingMethod, priorTokenTraining, postTokenTraining,
        surroundingTokensToUse, linkPhraseToPriorTokens, linkPhraseToPostTokens);

    /*
     * Create instance of PhraseIDCrossValidationSampler to create groups for training and testing
     * for cross-validation
     */
    PhraseIDCrossValidationSampler theSampler =
        new PhraseIDCrossValidationSampler(textAnalyzer, annotationApp);

    /*
     * Get the schema that the annotations are based on and select only those that are in training
     * or scoring concepts
     */
    List<SchemaConcept> importedSchemaConcepts = null;
    List<SchemaConcept> filteredSchemaConcepts = null;
    if (schemaConceptFile != null) {
      importedSchemaConcepts = SchemaImporter.importSchemaConcepts(schemaConceptFile);
      HashSet<String> trainingAndScoringConcepts = new HashSet<>(scoringConcepts);
      trainingAndScoringConcepts.addAll(trainingConcepts);

      if (trainingAndScoringConcepts != null && !trainingAndScoringConcepts.isEmpty()) {
        filteredSchemaConcepts =
            SchemaImporter.filterSchemaConcepts(importedSchemaConcepts, trainingAndScoringConcepts);
      }
    }

    /*
     * The data used for training and testing over multiple iterations, the "sampling data," is
     * created just once via this call to getSamplingData(). This call populates theSampler
     * PhraseIDCrossValidationAnalyzer instance with AnnotationGroup instances of the various text
     * documents and their annotations. It does not attach tokens to the annotations so that
     * experiments using various token processing options can be run.
     */
    boolean validSamplingData = theSampler.getSamplingData(filteredSchemaConcepts, trainingConcepts,
        scoringConcepts, trainingDataFilePath, separateTestingData, testingDataDirectoryPath);

    if (validSamplingData) {

      if (sentenceFilter != null) {
        /*
         * Add processed tokens to annotations, which are needed for the next step of filtering
         */
        textAnalyzer.updateTrainingGroups(theSampler.getAllAnnotationGroups());

        leaveOneAnalyzer.createFilteredSentences(theSampler, sentenceFilter);

        leaveOneAnalyzer.sentencesFiltered = true;
      }

      try {
        /*
         * Here we iterate through the various settings of the options that can be used with a
         * "Probabilistic" sequence identifier and Naive Bayes concept mapper
         */
        for (int i = startOptionIndex; i < endOptionIndex; i++) {
          /* Set the options for running the sample */
          optionsString = leaveOneAnalyzer.setNextOptions(i, phraseIDTrainingMethod);

          leaveOneAnalyzer.runOptionSetting(createDetailedReport, storeResultsAsXML,
              trainingConcepts, scoringConcepts, textAnalyzer, attributeTaggers,
              attributeConflictManagers, postProcessors, trainingGroupSize, optionsString,
              theSampler, filteredSchemaConcepts, contextSifters);
        }

        JOptionPane.showMessageDialog(null, "Probabilistic leave-one-out analysis complete");
      } catch (IOException e) {
        System.out.println(e);
        e.printStackTrace();
      }
    }
  }


  private static void runProbabilisticCrossValidation(CVParameterObject cvParameters)
      throws NotImplementedException {
    String trainingDataFilePath = cvParameters.getTrainingDataFilePath();
    String testingDirectoryFilePath = cvParameters.getTestingDirectoryFilePath();
    boolean useSeparateTestingData =
        testingDirectoryFilePath != null && !testingDirectoryFilePath.isEmpty();

    TSFinderMethod phraseIDTrainingMethod = cvParameters.getTSFinderMethod();
    if (!phraseIDTrainingMethod.equals(TSFinderMethod.PROBABILISTIC)) {
      throw new NotImplementedException(
          phraseIDTrainingMethod.toString() + " has not been implemented for cross-validation");
    }
    TSFinderMethod priorTokenTraining = phraseIDTrainingMethod;
    TSFinderMethod postTokenTraining = phraseIDTrainingMethod;

    PhraseIDSmoothingMethod smoothingMethod =
        PhraseIDCrossValidationAnalyzer.PHRASE_ID_SMOOTHING_DEFAULT;

    File schemaConceptFile = new File(cvParameters.getSchemaFilePath());
    HashSet<String> scoringConcepts = cvParameters.getScoringConcepts();

    HashSet<String> trainingConcepts = cvParameters.getTrainingConcepts();
    TextAnalyzer textAnalyzer =
        new TextAnalyzer(cvParameters.getDictionaryFilePath(), trainingConcepts);
    addSectionAndSentenceMatchers(textAnalyzer, cvParameters);

    Collection<AttributeTaggerSolution> attributeTaggers = cvParameters.getAttributeTaggers();
    List<AttributeConflictManager> attributeConflictManagers =
        cvParameters.getAttributeConflictManagers();
    Collection<AnnotationGroupPostProcessor> postProcessors = cvParameters.getPostProcessors();
    int trainingGroupSize = cvParameters.getTrainingGroupSize();
    AnnotationApp annotationApp = cvParameters.getAnnotationApp();
    Collection<SentenceFilter> sentenceFilters = cvParameters.getSentenceFilters();
    int startOptionIndex = cvParameters.getStartIndex();
    int endOptionIndex = cvParameters.getStopIndex();
    ContextSifter contextSifter = cvParameters.getContextSifter();

    int surroundingTokensToUse = PhraseIDCrossValidationAnalyzer.SURROUNDING_TOKENS_TO_USE_DEFAULT;
    boolean linkPhraseToPriorTokens =
        PhraseIDCrossValidationAnalyzer.LINK_PHRASE_TO_PRIOR_TOKENS_DEFAULT;
    boolean linkPhraseToPostTokens =
        PhraseIDCrossValidationAnalyzer.LINK_PHRASE_TO_POST_TOKENS_DEFAULT;
    boolean createDetailedReport = PhraseIDCrossValidationAnalyzer.CREATE_DETAILED_REPORT_DEFAULT;
    boolean storeResultsAsXML = PhraseIDCrossValidationAnalyzer.STORE_RESULTS_AS_XML_DEFAULT;

    /*
     * Get first sentenceFilter in sentenceFilters collection. The collection was created to allow
     * for future tool expansion.
     */
    SentenceFilter sentenceFilter = null;
    if (sentenceFilters != null && !sentenceFilters.isEmpty()) {
      sentenceFilter = sentenceFilters.iterator().next();
    }

    runProbabilisticCrossValidation(trainingDataFilePath, phraseIDTrainingMethod, smoothingMethod,
        priorTokenTraining, postTokenTraining, surroundingTokensToUse, linkPhraseToPriorTokens,
        linkPhraseToPostTokens, createDetailedReport, storeResultsAsXML, schemaConceptFile,
        trainingConcepts, scoringConcepts, textAnalyzer, attributeTaggers,
        attributeConflictManagers, postProcessors, trainingGroupSize, annotationApp, sentenceFilter,
        startOptionIndex, endOptionIndex, contextSifter, useSeparateTestingData,
        testingDirectoryFilePath);
  }
}
