package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ReverseProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.JdomXMLImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

@Deprecated
public class TrainingProcessor {

  protected NBTrainer conceptMappingTrainer;
  protected SequenceIDTrainer tsiTrainer = null;
  protected ProbabilisticTSFinderSolution theAlgorithmSolution;

  protected boolean useStems = RaptatConstants.USE_TOKEN_STEMMING;
  protected boolean usePOS = RaptatConstants.USE_PARTS_OF_SPEECH;
  protected boolean removeStopWords = RaptatConstants.REMOVE_STOP_WORDS;
  protected boolean invertTokenSequence = RaptatConstants.INVERT_TOKEN_SEQUENCE;
  protected int maxTokenNumber = RaptatConstants.MAX_TOKENS_DEFAULT;
  protected boolean useNulls = RaptatConstants.USE_NULLS_START;
  protected ContextHandling reasonNoMedsProcessing =
      OptionsManager.getInstance().getReasonNoMedsProcessing();
  protected ContextHandling negationProcessing =
      OptionsManager.getInstance().getNegationProcessing();
  protected boolean keepUnlabeledOnDisk = OptionsManager.getInstance().getKeepUnlabeledOnDisk();

  private File solutionSaveFile;
  private String uhtSerialPath = "";
  protected HashSet<String> acceptedConcepts = null;
  protected static TextAnalyzer theTextAnalyzer = new TextAnalyzer();
  private static Logger logger = Logger.getLogger(TrainingProcessor.class);


  /**
   * *********************************************************** Constructor method used by
   * BootstrapAnalyzer and CrossValidationAnalyzer. This constructor generates unlabeled hash trees
   * for all the training groups so they don't have to be regenerated in the iterative processes
   * involved with bootstrapping and leave one out analysis
   *
   * @param tokenOptions
   * @param phraseIDOptions
   * @param acceptedConcepts
   * @param solutionFile
   * @author Glenn Gobbel - Aug 24, 2012
   *         ************************************************************
   */
  public TrainingProcessor(TokenProcessingOptions tokenOptions,
      PhraseIDTrainOptions phraseIDOptions, HashSet<String> acceptedConcepts, File solutionFile) {
    this();
    this.useStems = tokenOptions.useStems();
    this.usePOS = tokenOptions.usePOS();
    this.removeStopWords = tokenOptions.removeStopWords();
    this.invertTokenSequence = tokenOptions.invertTokenSequence();
    this.maxTokenNumber = tokenOptions.getMaxTokenNumber();
    this.reasonNoMedsProcessing = tokenOptions.getReasonNoMedsProcessing();
    this.acceptedConcepts = acceptedConcepts;

    TrainingProcessor.theTextAnalyzer.setTokenProcessingParameters(this.useStems, this.usePOS,
        this.removeStopWords, this.maxTokenNumber, this.reasonNoMedsProcessing);

    this.solutionSaveFile = solutionFile;

    this.uhtSerialPath = this.solutionSaveFile.getParent() + File.separator
        + GeneralHelper.removeFileExtension(this.solutionSaveFile.getName(), '.') + ".rpt";

    initializeTrainers(phraseIDOptions);
  }


  protected TrainingProcessor() {
    TrainingProcessor.logger.setLevel(Level.INFO);
  }


  public List<AnnotationGroup> buildAnnotationTrainingList(List<String> textFilesPathList,
      List<String> referenceXMLFilesPathList, AnnotationImporter theImporter) {
    this.buildAnnotationTrainingList(textFilesPathList, referenceXMLFilesPathList,
        new ArrayList<SchemaConcept>(), theImporter);
    return null;
  }


  /**
   * Takes a list of text and xml paths and does the processing necessary to conform the xml and
   * text and generate a list of annotation training groups where each group corresponds to a
   * document and the annotations associated with that document
   *
   * @param textPaths
   * @param xmlPaths
   * @param saveUnlabeledToDisk
   * @return List<AnnotationTrainingGroup>
   * @author Glenn Gobbel - Oct 3, 2012
   * @param theImporter2
   */
  public List<AnnotationGroup> buildAnnotationTrainingList(List<String> textPaths,
      List<String> xmlPaths, List<SchemaConcept> schemaConcepts, AnnotationImporter theImporter) {
    /* Build a hashtable to quickly look up file paths */
    Hashtable<String, String> textPathSet = new Hashtable<>(textPaths.size());
    for (String curTextPath : textPaths) {
      /*
       * Create a hashtable where the last item in the file path is used to hash to the full file
       * path
       */
      textPathSet.put(new File(curTextPath).getName(), curTextPath);
    }

    /*
     * Now build a list of string [] containing the xml path at index 0 and the text path at index
     * 1.
     */
    List<AnnotationGroup> trainingGroups = new ArrayList<>(textPathSet.size());
    for (String curXMLPath : xmlPaths) {
      String curXMLSource = JdomXMLImporter.getTextSource(curXMLPath);

      /*
       * Only process XML documents where text source document is contained with the set of
       * documents being processed (i.e. in textPathSet)
       */
      if (textPathSet.containsKey(curXMLSource)) {
        String curTextPath = textPathSet.get(curXMLSource);

        /*
         * Note that the parameters passed to the constructor will be // used for processing //
         * unless the default constructor is used
         */
        RaptatDocument theDocument = TrainingProcessor.theTextAnalyzer.processDocument(curTextPath);
        List<AnnotatedPhrase> referenceAnnotations = theImporter.importAnnotations(curXMLPath,
            curTextPath, this.acceptedConcepts, schemaConcepts, null);
        trainingGroups.add(new AnnotationGroup(theDocument, referenceAnnotations));
      }
    }

    /*
     * The prepareTrainingData() method takes the annotations within the traiiningGroups, adds
     * tokens to them from the documents, and splits the annotations if they are longer than the
     * maximum allowed number of tokens. It also generates unlabeleldHashTrees for each
     * AnnotationGroup instance.
     */
    return prepareForTraining(trainingGroups);
  }


  public RaptatDocument buildDocument(String documentText, String documentPath) {
    return TrainingProcessor.theTextAnalyzer.processText(documentText, Optional.of(documentPath));
  }


  public ProbabilisticTSFinderSolution getAlgorithmSolution() {
    this.updateAlgorithmSolution();
    return this.theAlgorithmSolution;
  }


  public ProbabilisticTSFinderSolution getAlgorithmSolution(List<SchemaConcept> schemaConceptList) {
    this.updateAlgorithmSolution(schemaConceptList);
    return this.theAlgorithmSolution;
  }


  /**
   * ***********************************************************
   *
   * <p>
   * Takes the AnnotationGroup instances used for training and modifies the processed tokens within
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
   * @return ***********************************************************
   */
  public List<AnnotationGroup> prepareForTraining(List<AnnotationGroup> trainingGroups) {
    if (this.tsiTrainer != null) {
      /*
       * updateTrainingGroups() will regenerate processed tokens, add them to the reference
       * annotations, and split phrases where necessary. If an annotation has more than the maximum
       * allowed number of tokens, it will be split into smaller phrases as needed by this method.
       * Note that this call to updateTrainingGroups() method regenerates processed tokens. This is
       * only necessary really for bootstrapping and leave one out analysis, so the efficiency of
       * this method when used just for training could be improved by taking that regeneration step
       * out when used only for training.
       */

      TrainingProcessor.theTextAnalyzer.updateTrainingGroups(trainingGroups);
      if (this.invertTokenSequence) {
        TrainingProcessor.logger.info("Inverting token sequences in documents and annotations");
        for (AnnotationGroup curGroup : trainingGroups) {
          this.tsiTrainer
              .reversePhraseAndTokenOrder(curGroup.getRaptatDocument().getActiveSentences());
          this.tsiTrainer.reversePhraseAndTokenOrder(curGroup.referenceAnnotations);
        }
      }

      /*
       * Note that both ReverseProbabilisticSequenceIDTrainers and ProbabilisticSequenceIDTrainers
       * are both types of ProbabilisticSequenceIDTrainers, so this statement block will generate
       * unlabeled hash trees for both trainer types.
       */
      if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
        // logger.info( "\nBuilding unlabeled hash trees" );
        ((ProbabilisticSequenceIDTrainer) this.tsiTrainer)
            .generateUnlabeledHashTrees(trainingGroups);
      }

    } else {
      TrainingProcessor.logger
          .error("\nSequence ID Trainer must be initialized before calling this method");
      throw new NullPointerException("The token sequence ID trainer in TrainingProcessor is null");
    }

    return trainingGroups;
  }


  /**
   * Takes one AnnotationGroup and updates training based on this group
   *
   * @param inputGroup
   * @author Glenn Gobbel - Dec 13, 2013
   */
  public void trainRaptat(AnnotationGroup inputGroup) {
    prepareTrainingData(inputGroup);
    TrainingProcessor.logger
        .debug("Updating sequence training for:" + inputGroup.getRaptatDocument().getTextSource());
    this.tsiTrainer.updateTraining(inputGroup);

    TrainingProcessor.logger
        .debug("Updating concept mapping for:" + inputGroup.getRaptatDocument().getTextSource());
    this.conceptMappingTrainer.updateTraining(inputGroup.referenceAnnotations);

    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
      ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).addUnlabeledToLabeledTree();
    }

    if (TrainingProcessor.logger.isDebugEnabled()) {
      this.tsiTrainer.getSequenceIDStructure().print();
    }
  }


  public void trainRaptat(List<AnnotationGroup> bootstrapGroups, boolean saveUnlabeledToDisk) {
    this.trainRaptat(bootstrapGroups, saveUnlabeledToDisk, false);
  }


  /**
   * Primary method used to train RapTAT to identify phrase for annotation and map them to concepts
   * within a defined schema. The options used are typically those provided by the OptionsManager
   * singleton, but the options may be varied during testing of the optimal options settings via
   * leave one out or bootstrap analysis.
   *
   * @param trainingGroups
   * @param saveUnlabeledToDisk
   * @param saveSolutionToDisk
   * @author Glenn Gobbel - Dec 31, 2013
   */
  public void trainRaptat(List<AnnotationGroup> trainingGroups, boolean saveUnlabeledToDisk,
      boolean saveSolutionToDisk) {
    this.tsiTrainer.clearTraining();
    this.conceptMappingTrainer.clearSolution();

    for (AnnotationGroup curGroup : trainingGroups) {
      TrainingProcessor.logger
          .debug("Updating sequence training for:" + curGroup.getRaptatDocument().getTextSource());
      this.tsiTrainer.updateTraining(curGroup);

      TrainingProcessor.logger
          .debug("Updating concept mapping for:" + curGroup.getRaptatDocument().getTextSource());
      this.conceptMappingTrainer.updateTraining(curGroup.referenceAnnotations);
    }

    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
      /*
       * Write any unsaved UnlabeledHashTree instance to disk if indicated. Turn saving off if you
       * will not be updating the solution files.
       */
      if (saveUnlabeledToDisk) {
        ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).closeTreeStreams();
      }
      ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).addUnlabeledToLabeledTree();
    }

    this.updateAlgorithmSolution();

    if (TrainingProcessor.logger.isDebugEnabled()) {
      this.tsiTrainer.getSequenceIDStructure().print();
    }

    if (saveSolutionToDisk) {
      this.theAlgorithmSolution.saveToFile(this.solutionSaveFile);
    }
  }


  /**
   * Method used to train RapTAT using Probabilistic method to identify phrase for annotation and
   * map them to concepts within a defined schema. The options used are typically those provided by
   * the OptionsManager singleton, but the options may be varied during testing of the optimal
   * options settings via leave one out or bootstrap analysis.
   *
   * @param trainingGroups
   * @param saveUnlabeledToDisk
   * @param schemaConceptList
   * @return
   * @author Sanjib Saha - Dec 4, 2014
   */
  public ProbabilisticTSFinderSolution trainUsingProbabilisticMethod(
      List<AnnotationGroup> trainingGroups, boolean saveUnlabeledToDisk,
      List<SchemaConcept> schemaConceptList) {
    this.tsiTrainer.clearTraining();
    this.conceptMappingTrainer.clearSolution();

    for (AnnotationGroup curGroup : trainingGroups) {
      TrainingProcessor.logger
          .debug("Updating sequence training for:" + curGroup.getRaptatDocument().getTextSource());
      this.tsiTrainer.updateTraining(curGroup);

      TrainingProcessor.logger
          .debug("Updating concept mapping for:" + curGroup.getRaptatDocument().getTextSource());
      this.conceptMappingTrainer.updateTraining(curGroup.referenceAnnotations);
    }

    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
      /*
       * Write any unsaved UnlabeledHashTree instance to disk if indicated. Turn saving off if you
       * will not be updating the solution files
       */
      if (saveUnlabeledToDisk) {
        ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).closeTreeStreams();
      }
      ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).addUnlabeledToLabeledTree();
    }

    this.updateAlgorithmSolution(schemaConceptList);

    if (TrainingProcessor.logger.isDebugEnabled()) {
      this.tsiTrainer.getSequenceIDStructure().print();
    }

    return this.theAlgorithmSolution;
  }


  public void updateTraining(List<AnnotationGroup> trainingGroups, boolean saveUnlabeledToDisk) {
    this.trainRaptat(trainingGroups, saveUnlabeledToDisk, this.solutionSaveFile != null);
  }


  public ProbabilisticTSFinderSolution updateTrainingWithProbabilisticMethod(
      List<AnnotationGroup> trainingGroups, List<SchemaConcept> schemaConceptList) {
    return trainUsingProbabilisticMethod(trainingGroups, true, schemaConceptList);
  }


  private void initializeTrainers(PhraseIDTrainOptions phraseIDOptions) {
    TSFinderMethod identificationMethod = phraseIDOptions.getPhraseIDMethod();
    PhraseIDSmoothingMethod smoothingMethod = phraseIDOptions.getSmoothingMethod();

    if (this.invertTokenSequence) {
      /*
       * Note that invertTokenSequence option not implemented for BAGOFWORDS
       */
      if (identificationMethod.equals(TSFinderMethod.BAGOFWORDS)) {
        TrainingProcessor.logger.info("Using BOW and inverted sequence for sequence ID");
        this.tsiTrainer = new BOWSequenceIDTrainer(phraseIDOptions);
      } else if (identificationMethod.equals(TSFinderMethod.PROBABILISTIC)) {
        TrainingProcessor.logger
            .info("Using Probabilistic method and inverted sequence for sequence ID");
        this.tsiTrainer = new ReverseProbabilisticSequenceIDTrainer(this.uhtSerialPath,
            this.keepUnlabeledOnDisk, smoothingMethod);
      }
    } else {
      if (identificationMethod.equals(TSFinderMethod.BAGOFWORDS)) {
        // logger.info(
        // "Using BOW and standard sequence for sequence ID" );
        this.tsiTrainer = new BOWSequenceIDTrainer(phraseIDOptions);
      } else if (identificationMethod.equals(TSFinderMethod.PROBABILISTIC)) {
        // logger.info(
        // "Using Probabilistic method and standard sequence for
        // sequence ID"
        // );
        this.tsiTrainer = new ProbabilisticSequenceIDTrainer(this.uhtSerialPath,
            this.keepUnlabeledOnDisk, smoothingMethod);
      }
    }

    this.conceptMappingTrainer = new NBTrainer(this.useNulls, this.useStems, this.usePOS,
        this.invertTokenSequence, this.removeStopWords, this.reasonNoMedsProcessing);
  }


  /**
   * Takes one AnnotationGroup instance used for training and modifies the processed tokens within
   * the document and annotations to conform to the current options used for training with regard to
   * using stems, parts of speech, stop word removal, number of tokens allowed in an annotation, and
   * whether to invert token order. If the tsiTrainer is a ProbabilisticSequenceIDTrainer, it also
   * builds unlabeled hash trees to the training group.
   *
   * <p>
   * Precondition: The tsiTrainer has been set and is not null Postcondition: The training groups in
   * the list passed as a parameter are modified according to the options set for training.
   *
   * @param inputGroup
   * @author Glenn Gobbel - Dec 13, 2013
   */
  private void prepareTrainingData(AnnotationGroup inputGroup) {
    if (this.tsiTrainer != null) {
      TrainingProcessor.theTextAnalyzer.updateTrainingGroup(inputGroup);
      if (this.invertTokenSequence) {
        TrainingProcessor.logger.info("Inverting token sequences in documents and annotations");

        this.tsiTrainer
            .reversePhraseAndTokenOrder(inputGroup.getRaptatDocument().getActiveSentences());
        this.tsiTrainer.reversePhraseAndTokenOrder(inputGroup.referenceAnnotations);
      }

      /*
       * Note that both ReverseProbabilisticSequenceIDTrainers and ProbabilisticSequenceIDTrainers
       * are types of ProbabilisticSequenceIDTrainers
       */
      else if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
        TrainingProcessor.logger.info("\nBuilding standard, unlabeled hash trees");
        ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).generateUnlabeledHashTree(inputGroup);
      }

    } else {
      TrainingProcessor.logger
          .error("\nSequence ID Trainer must be initialized before calling this method");
      throw new NullPointerException("The token sequence ID trainer in TrainingProcessor is null");
    }
  }


  protected void updateAlgorithmSolution() {
    this.updateAlgorithmSolution(this.uhtSerialPath, new ArrayList<SchemaConcept>());
  }


  protected void updateAlgorithmSolution(List<SchemaConcept> schemaConcepts) {
    this.updateAlgorithmSolution(this.uhtSerialPath, schemaConcepts);
  }


  protected void updateAlgorithmSolution(String uhtPath, List<SchemaConcept> schemaConcepts) {
    TrainingProcessor.logger.debug("Updating probabilities");
    this.tsiTrainer.updateProbabilities();

    TrainingProcessor.logger.info("Creating algorithm solution");
    this.theAlgorithmSolution =
        new ProbabilisticTSFinderSolution(uhtPath, this.tsiTrainer.getSequenceIDStructure(),
            this.conceptMappingTrainer.getCurrentSolution(), this.acceptedConcepts,
            this.maxTokenNumber, this.tsiTrainer.getPhraseIDSmoothingMethod(), schemaConcepts);
    TrainingProcessor.logger.info("Completed algorithm solution creation");
  }
}
