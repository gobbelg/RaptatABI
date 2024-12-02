/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.training;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.training.ProbabilisticSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TSTrainingParameterObject.TrainingAndAnnotationParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TSTrainingParameterObject.TrainingOnlyParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ReverseStartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ReverseStartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.JdomXMLImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * Trains TokenSequenceAnnotators to identify phrases and their mapped concepts within a document
 *
 * @author Glenn T. Gobbel Feb 13, 2016
 */
public abstract class TokenSequenceSolutionTrainer {
  protected static final Logger LOGGER = Logger.getLogger(TokenSequenceSolutionTrainer.class);
  private static ReverseStartOffsetPhraseComparator reversePhraseSorter =
      new ReverseStartOffsetPhraseComparator();
  private static ReverseStartOffsetTokenComparator reverseTokenSorter =
      new ReverseStartOffsetTokenComparator();


  protected TextAnalyzer textAnalyzer;


  protected HashSet<String> trainingConcepts;


  protected boolean invertTokenSequence = false;


  /**
   * @param textAnalyzer
   * @param trainingConceptsSet
   */
  public TokenSequenceSolutionTrainer(TextAnalyzer textAnalyzer,
      HashSet<String> trainingConceptsSet) {
    this();
    this.textAnalyzer = textAnalyzer;
    this.trainingConcepts = trainingConceptsSet;
  }


  /**
   * @param textAnalyzer
   * @param acceptedConceptsSet
   */
  public TokenSequenceSolutionTrainer(TextAnalyzer textAnalyzer,
      HashSet<String> acceptedConceptsSet, boolean invertTokenSequence) {
    this(textAnalyzer, acceptedConceptsSet);
    this.invertTokenSequence = invertTokenSequence;
  }

  protected TokenSequenceSolutionTrainer() {
    TokenSequenceSolutionTrainer.LOGGER.setLevel(Level.INFO);
  }

  public abstract SequenceIDTrainer getTsiTrainer();

  public void prepareForTraining(List<AnnotationGroup> annotationGroups,
      boolean invertTokenSequence, boolean useFilteredSentences) throws Exception {
    SequenceIDTrainer tsiTrainer = getTsiTrainer();
    for (AnnotationGroup annotationGroup : annotationGroups) {
      this.prepareForTraining(annotationGroup, tsiTrainer, invertTokenSequence);
      if (useFilteredSentences) {
        annotationGroup.activateFilteredSentences();
      } else {
        annotationGroup.activateBaseSentences();
      }
    }
  }


  public void trainUsingTrainingSet(List<RaptatPair<String, String>> textAndXmlPaths,
      List<SchemaConcept> schemaConceptList, Collection<SentenceFilter> sentenceFilters,
      AnnotationImporter importer) {
    for (RaptatPair<String, String> curPair : textAndXmlPaths) {
      RaptatDocument theDocument = this.textAnalyzer.processDocument(curPair.left);
      List<AnnotatedPhrase> referenceAnnotations = importer.importAnnotations(curPair.right,
          curPair.left, this.trainingConcepts, schemaConceptList, null);

      /*
       * The prepareForTrainingMethod() should create filtered sentence sets and
       * UnlabeledPhraseTrees for theDocument
       */
      AnnotationGroup annotationGroup =
          this.prepareForTraining(theDocument, referenceAnnotations, sentenceFilters);

      /*
       * Calls to updateTraining() update the labeled and unlabeledPhraseTrees and update concept
       * mapping based on annotationGroup data
       */
      updateTraining(annotationGroup);
    }
    if (this instanceof ProbabilisticSolutionTrainer) {
      SequenceIDTrainer sidTrainer = ((ProbabilisticSolutionTrainer) this).getTsiTrainer();
      if (sidTrainer instanceof ProbabilisticSequenceIDTrainer) {
        ((ProbabilisticSequenceIDTrainer) sidTrainer).addUnlabeledToLabeledTree();
      }
    }
  }


  /**
   * Handles updating the solution after all training data has been submitted. It also closes and
   * save data structures that may be needed for updating training at a later time point.
   *
   * @param schemaConceptList
   * @return TokenSequenceFinderSolution
   */
  public abstract TokenSequenceFinderSolution updateAndReturnSolution(
      List<SchemaConcept> schemaConceptList);


  public abstract void updateTraining(AnnotationGroup annotationGroup);


  /**
   * Takes an AnnotationGroup instance used for training and modifies the processed tokens within
   * the document and annotations to conform to the current options used for training with regard to
   * using stems, parts of speech, stop word removal, number of tokens allowed in an annotation, and
   * whether to invert token order. If the tsiTrainer is a ProbabilisticSequenceIDTrainer, it also
   * builds unlabeled hash trees and adds them to the training group.
   *
   * <p>
   * Precondition: The tsiTrainer has been set and is not null
   *
   * <p>
   * Postcondition: The training groups in the list passed as a parameter are modified according to
   * the options set for training.
   *
   * @param trainingGroups
   * @author Glenn Gobbel - Oct 25, 2012
   * @param tsiTrainer
   * @param trainer
   * @return
   */
  protected AnnotationGroup prepareForTraining(RaptatDocument document,
      List<AnnotatedPhrase> referencePhrases) {
    AnnotationGroup trainingGroup = new AnnotationGroup(document, referencePhrases);
    this.prepareForTraining(trainingGroup, getTsiTrainer(), this.invertTokenSequence);
    return trainingGroup;
  }


  /**
   * *********************************************************** Takes the annotated phrases in a
   * document and puts them in reverse order according to start offset. Also takes the tokens within
   * each phrase and puts them in reverse order according to start offset.
   *
   * <p>
   * Precondition: None Postcondition: The annotationsInDocument are in reverse order and the tokens
   * in those annotations are also in reverse order
   *
   * @param annotationsInDocument
   * @author Glenn Gobbel - Jun 15, 2012 ***********************************************************
   */
  protected void reversePhraseAndTokenOrder(List<AnnotatedPhrase> annotationsInDocument) {
    List<RaptatToken> curTokens;
    Collections.sort(annotationsInDocument, TokenSequenceSolutionTrainer.reversePhraseSorter);
    for (AnnotatedPhrase curPhrase : annotationsInDocument) {
      curTokens = curPhrase.getProcessedTokens();
      Collections.sort(curTokens, TokenSequenceSolutionTrainer.reverseTokenSorter);
    }
  }


  private void prepareForTraining(AnnotationGroup annotationGroup, SequenceIDTrainer tsiTrainer,
      boolean invertTokenSequence) {
    RaptatDocument raptatDocument = annotationGroup.getRaptatDocument();
    /*
     * updateTrainingGroups() will regenerate processed tokens, add them to the reference
     * annotations, and split phrases where necessary. If an annotation has more than the maximum
     * allowed number of tokens, it will be split into smaller phrases as needed by this method.
     *
     * Note that this call to updateTrainingGroups() method regenerates processed tokens. This is
     * only necessary really for bootstrapping and leave one out analysis, so the efficiency of this
     * method when used just for training could be improved by taking that regeneration step out
     * when used only for training.
     */
    this.textAnalyzer.updateTrainingGroup(annotationGroup);
    if (invertTokenSequence) {
      TokenSequenceSolutionTrainer.LOGGER
          .info("Inverting token sequences in documents and annotations");
      reversePhraseAndTokenOrder(raptatDocument.getActiveSentences());
      reversePhraseAndTokenOrder(annotationGroup.referenceAnnotations);
    }

    if ((tsiTrainer = getTsiTrainer()) != null) {
      tsiTrainer.buildAllUnlabeledHashTrees(annotationGroup);
    }
  }


  private AnnotationGroup prepareForTraining(RaptatDocument document,
      List<AnnotatedPhrase> referenceAnnotations, Collection<SentenceFilter> sentenceFilters) {
    AnnotationGroup trainingGroup = new AnnotationGroup(document, referenceAnnotations);
    trainingGroup.createFilteredSentences(sentenceFilters, referenceAnnotations);

    /*
     * This will regenerate tokens, split annotations as needed based on maxTokens allowed, and
     * build filtered and unfiltered UnlabeledPhraseTrees for the trainingGroiup
     */
    this.prepareForTraining(trainingGroup, getTsiTrainer(), this.invertTokenSequence);

    /*
     * We set the filtered sentences and filtered unlabeledHashTree instances as active for the
     * group because the filtered sets are created from the base set when no filtering is done. So
     * this saves us from checking if the set was filtered or not.
     */
    try {
      trainingGroup.activateFilteredSentences();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return trainingGroup;
  }


  public static void generateAndSaveSolution(TSTrainingParameterObject parameters) {
    /* Set the tool global options for the training run */
    OptionsManager optionsManager = OptionsManager.getInstance();
    initializeTokenAndPhraseOptions(optionsManager, parameters);

    /*
     * Create text analyzer and instantiate it so that options are properly set. This particular
     * TextAnalyzer constructor sets the options to the current settings within the OptionsManager,
     * and these options are set in previous statements.
     */
    TextAnalyzer textAnalyzer = new TextAnalyzer(parameters.annotationOnlyParameters.dictionary);

    TrainingOnlyParameterObject trParameters = parameters.trainingOnlyParameters;
    TrainingAndAnnotationParameterObject taParameters = parameters.trainingAndAnnotationParameters;

    /*
     * Get the files used for training
     */
    String trainingDataFile = parameters.trainingOnlyParameters.trainingDataFilePath;
    List<RaptatPair<String, String>> textAndXmlPaths = prepareTrainingSet(trainingDataFile);

    /*
     * Place solution file in same place as training data file
     */
    String solutionFileName = "RaptatSolution_" + GeneralHelper.getTimeStamp() + ".soln";
    String solutionFilePath =
        new File(trainingDataFile).getParent() + File.separator + solutionFileName;
    File solutionFile = new File(solutionFilePath);

    /*
     * Instantiate trainer and train
     */
    TokenProcessingOptions tokenProcessingOptions = new TokenProcessingOptions(optionsManager);
    PhraseIDTrainOptions phraseIDOptions = new PhraseIDTrainOptions(optionsManager);

    TokenSequenceSolutionTrainer tsFinderTrainer = null;
    if (trParameters.tsFinderMethod.equals(RaptatConstants.TSFinderMethod.PROBABILISTIC)) {
      tsFinderTrainer = new ProbabilisticSolutionTrainer(tokenProcessingOptions, phraseIDOptions,
          taParameters.acceptedConcepts, solutionFile, trParameters.keepUnlabeledOnDisk,
          trParameters.saveUnlabeledToDisk, textAnalyzer);
    }
    /*
     * This will eventually handle other types of TokenSequenceSolutionTrainer instances, like
     * CRF-based ones
     */
    else {
      GeneralHelper
          .errorWriter(trParameters.tsFinderMethod.toString() + " training not yet implemented");
      throw new IllegalArgumentException();
    }

    AnnotationImporter importer = AnnotationImporter.getImporter(trParameters.annotationApp);
    tsFinderTrainer.trainUsingTrainingSet(textAndXmlPaths,
        parameters.trainingAndAnnotationParameters.schemaConcepts, trParameters.sentenceFilters,
        importer);
    TokenSequenceFinderSolution solution =
        tsFinderTrainer.updateAndReturnSolution(taParameters.schemaConcepts);

    /*
     * Write the solution file to disk
     */
    if (solution != null) {
      RaptatAnnotationSolution.packageAndSaveSolution(parameters, solution, solutionFile);

    } else {
      GeneralHelper.errorWriter("Unable to create solution");
    }
  }


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    if (lvgSetting > 0) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          TSTrainingParser parser = new TSTrainingParser();
          TSTrainingParameterObject parameters = parser.parseCommandLine(args);

          try {
            TokenSequenceSolutionTrainer.generateAndSaveSolution(parameters);
          } catch (NotImplementedException e) {
            System.err.println("Unable to run parsed parameters " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(-1);
          }
        }
      });
    }
  }


  /**
   * Create a pair of lists of file paths to .txt and .xml to use for training RapTAT
   *
   * @param trainingDataFilePath
   * @return
   * @throws IllegalArgumentException
   */
  private static RaptatPair<List<String>, List<String>> getTrainingData(String trainingDataFilePath)
      throws IllegalArgumentException {
    String[] nextDataSet;
    List<String> textFilePathList = new ArrayList<>();
    List<String> xmlFilePathList = new ArrayList<>();

    CSVReader dataReader = null;
    if (trainingDataFilePath != null) {
      dataReader = new CSVReader(new File(trainingDataFilePath));
    }

    if (dataReader == null || !dataReader.isValid()) {
      dataReader =
          new CSVReader(OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath(), false,
              "Select .csv " + "file containing training data samples");
    }

    if (dataReader.isValid()) {
      String trainingFileDirectory = dataReader.getInFileDirectory();
      TokenSequenceSolutionTrainer.LOGGER
          .info("Generating training data set from files in " + trainingFileDirectory);

      while ((nextDataSet = dataReader.getNextData()) != null) {
        /*
         * Check to make sure line is correct length and not blank
         */
        if (nextDataSet.length > 1) {
          String textFile = nextDataSet[0].trim();
          if (textFile.length() > 0) {
            textFilePathList.add(textFile);
            TokenSequenceSolutionTrainer.LOGGER.info("TextFile: " + textFile + " loaded");
          }
          String xmlFile = nextDataSet[1].trim();
          if (xmlFile.length() > 0) {
            xmlFilePathList.add(xmlFile);
            TokenSequenceSolutionTrainer.LOGGER.info("XmlFile: " + xmlFile + " loaded");
          }
        }
      }

      return new RaptatPair<>(textFilePathList, xmlFilePathList);
    }

    throw new IllegalArgumentException("Training data file path is invalid");
  }


  private static void initializeTokenAndPhraseOptions(OptionsManager optionsManager,
      TSTrainingParameterObject parameters) {
    TrainingAndAnnotationParameterObject taParameters = parameters.trainingAndAnnotationParameters;
    optionsManager.setInvertTokenSequence(taParameters.invertTokenSequence);
    optionsManager.setStemmingOption(taParameters.useStems);
    optionsManager.setUsePOS(taParameters.usePOS);
    optionsManager.setRemoveStopWords(taParameters.removeStopWords);

    TrainingOnlyParameterObject trParamaters = parameters.trainingOnlyParameters;
    optionsManager.setPhraseIDTrainingMethod(trParamaters.tsFinderMethod);
    optionsManager.setPhraseIDSmoothingMethod(trParamaters.tsSmoothingMethod);
  }


  private static List<RaptatPair<String, String>> prepareTrainingSet(String trainingDataFilePath) {
    RaptatPair<List<String>, List<String>> textAndXMlTrainingData =
        getTrainingData(trainingDataFilePath);
    List<RaptatPair<String, String>> resultList =
        new ArrayList<>(textAndXMlTrainingData.left.size());

    /*
     * Build a hashtable to quickly look up file paths from text file names
     */
    Hashtable<String, String> textPathSet = new Hashtable<>(textAndXMlTrainingData.left.size());
    for (String textPath : textAndXMlTrainingData.left) {
      textPathSet.put(new File(textPath).getName(), textPath);
    }

    for (String xmlPath : textAndXMlTrainingData.right) {
      String xmlTextSource = JdomXMLImporter.getTextSource(xmlPath);

      /*
       * Only process XML documents where text source document is contained with the set of
       * documents being processed (i.e. in textPathSet)
       */
      if (textPathSet.containsKey(xmlTextSource)) {
        String curTextPath = textPathSet.get(xmlTextSource);
        RaptatPair<String, String> textXmlPair = new RaptatPair<>(curTextPath, xmlPath);
        resultList.add(textXmlPair);

        /*
         * All text sources should be matched by an xml file Check this by removing and reporting if
         * not empty
         */
        textPathSet.remove(xmlTextSource);
      } else {
        System.err.println("Text source for " + xmlPath + " does not exist");
      }
    }

    for (String textPath : textPathSet.keySet()) {
      System.err.println("No XML file for " + textPath);
    }
    return resultList;
  }
}
