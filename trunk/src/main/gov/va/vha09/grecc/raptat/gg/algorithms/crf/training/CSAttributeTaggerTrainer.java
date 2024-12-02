/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.NotImplementedException;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CSFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CueScopeFeatureSet;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.AssertionStatusFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.NegativeAssertionScopeFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.PhraseBasedSentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.TaggerTrainerOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SimpleTriplet;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/**
 * Trains a CSAttributeTagger to identify tokens for a cue and scope model. It generates the train
 * data based on a list of text files, a series of xml files containing annotated phrases that are
 * affected the cue and scope model, and a series of xml files indicating cue and scope. It then
 * trains by calling the CRFTrainer in the JCRFSuite code.
 *
 * @author Glenn T. Gobbel Feb 17, 2016
 */
public class CSAttributeTaggerTrainer extends TaggerTrainer {
  /**
   * Convenience class for storing all the training data from a single document
   *
   * @author Glenn T. Gobbel Feb 29, 2016
   */
  private class CSTrainingSet {
    private static final int DEFAULT_TRAINING_SET_SIZE = 1024;
    private List<AnnotatedPhrase> cueTrainingSentences;
    private List<AnnotatedPhrase> cuePhrases;

    private HashSet<AnnotatedPhrase> scopeTrainingSentences;
    private List<AnnotatedPhrase> scopePhrases;


    private CSTrainingSet() {
      this.cueTrainingSentences = new ArrayList<>(CSTrainingSet.DEFAULT_TRAINING_SET_SIZE);
      this.cuePhrases = new ArrayList<>(CSTrainingSet.DEFAULT_TRAINING_SET_SIZE);
      this.scopeTrainingSentences = new HashSet<>(CSTrainingSet.DEFAULT_TRAINING_SET_SIZE);
      this.scopePhrases = new ArrayList<>(CSTrainingSet.DEFAULT_TRAINING_SET_SIZE);
    }
  }

  /*
   * These enums allow one to run the main class and do various tasks, generally indicated by the
   * enum name. The TRAIN_MULTIPLE allows training a set of models using various feature
   * conjunctions offsets and sizes of ngrams
   */
  private enum RunType {
    PRINT_FEATURES, TRAIN_NEGATION_SINGLE, CREATE_MULTIPLE_TRAINING_FILES, TEST_ANNOTATION_FILTERING
  }


  /*
   * This filter variable, sentenceSelector, will take a list of sentences presented as
   * AnnotatedPhrase instances and return those that will form the candidate set for training.
   *
   * In the case of cue training, the candidateSentenceSet will consist of all sentences containing
   * a phrase with an AssertionStatus attribute, regardless of whether that AssertionStatus is
   * positive or negative (because we want to train our system to learn both when cues are present,
   * such as negative assertion, or absent, such as when there is a positive assertion status.
   *
   * In the case of scope training, the candidateSenenceSet will consist only of those sentences
   * that have the scope for an existing cue. So we will only select sentences that contain an
   * AnnotatedPhrase that maps to the concept 'scope' (or some related concept name).
   */
  // private SentenceFilter sentenceSelector;

  /*
   * Running the includePhrase() method of the negationScopeFilter will be used to filter out
   * sentences that have only part of a cue-scope linked pair. That is, the cue phrase or the scope
   * phrase or the pair together cross a sentence boundary from one sentence to another. It assures
   * that training is only done on sentences with either no cue-scope pair (but contains an
   * annotated phrase) or where the cue-scope is fully within the sentence.
   */
  private final NegativeAssertionScopeFilter negationScopeFilter =
      new NegativeAssertionScopeFilter();

  /** */
  public CSAttributeTaggerTrainer() {
    super();
  }


  public CSAttributeTaggerTrainer(ContextType context,
      TokenProcessingOptions textProcessingOptions) {
    super(textProcessingOptions);
    this.context = context;
  }


  /**
   * @param tokenProcessingOptions
   */
  public CSAttributeTaggerTrainer(TokenProcessingOptions tokenProcessingOptions) {
    super(tokenProcessingOptions);
  }


  @Override
  public RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTestingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet) {
    // TODO Auto-generated method stub
    throw new NotImplementedException("Not yet implemented: TO DO");
  }


  @Override
  public RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTrainingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet) {
    throw new NotImplementedException("Not yet implemented for this subclass of TaggerTrainer");
  }


  public CSTrainingSet getTrainingSet(File textFile, File allConceptsXML, File assertionXML) {
    RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> initialData =
        this.getDocumentTrainingData(textFile, allConceptsXML, assertionXML);

    List<AnnotatedPhrase> phrasesOfInterest = initialData.left;
    List<AnnotatedPhrase> cueScopePhrases = initialData.right;

    CSTrainingSet trainingSet = filterTrainingData(phrasesOfInterest, cueScopePhrases);

    /*
     * Mark the tokens of phrases that are to be used for CRF labels so that we can identify them
     * when assigning labels to the tokens in each sentence.
     */
    this.markPhraseTokens(trainingSet.cuePhrases, RaptatConstants.BEGIN_CUE_STRING,
        RaptatConstants.INSIDE_CUE_STRING);
    this.markPhraseTokens(trainingSet.scopePhrases, RaptatConstants.BEGIN_SCOPE_STRING,
        RaptatConstants.INSIDE_SCOPE_STRING);

    return trainingSet;
  }


  /**
   * Takes a CueScopeFeatureSet instance, which contains the features and labels for training CRF
   * models to identify cue and scope, and prints them to a file tagged with the parameter
   * fileNameTag and located in the fileDirectory described by trainingFileDirectory
   *
   * @param featureSet
   * @param trainingFileDirectory
   * @param fileNameTag
   */
  public void printFeatureSet(CueScopeFeatureSet featureSet, String trainingFileDirectory,
      String fileNameTag) {
    PrintWriter pw;

    pw = getFeaturePrintWriter(trainingFileDirectory, fileNameTag, "CueFeatureTraining");
    printFeaturesAndLabels(featureSet.cueTrainingFeatures, featureSet.cueTrainingLabels, pw);

    pw = getFeaturePrintWriter(trainingFileDirectory, fileNameTag, "ScopeFeatureTraining");
    printFeaturesAndLabels(featureSet.scopeTrainingFeatures, featureSet.scopeTrainingLabels, pw);
  }


  /**
   * @param wordConjunctionOffset
   * @param wordNgramSize
   * @param posConjunctionOffset
   * @param posNgramSize
   * @param trainingTextFiles
   * @param trainingAllConceptsXML
   * @param trainingAssertionXML
   * @param trainingFeatureFilePath
   * @param cueIncludedFeaturesFilePath
   * @param scopeIncludedFeaturesFilePath
   * @param tagForFileName
   */
  public void printFeaturesFromTrainingFiles(int wordConjunctionOffset, int wordNgramSize,
      int posConjunctionOffset, int posNgramSize, int maxCueOffsetFeature,
      List<File> trainingTextFiles, List<File> trainingAllConceptsXML,
      List<File> trainingAssertionXML, String trainingFeatureFilePath,
      String cueIncludedFeaturesFilePath, String scopeIncludedFeaturesFilePath,
      String tagForFileName) {
    StringBuilder sb = new StringBuilder(tagForFileName).append("_");
    sb.append("WO_").append(wordConjunctionOffset).append("_");
    sb.append("WNG_").append(wordNgramSize).append("_");
    sb.append("PO_").append(posConjunctionOffset).append("_");
    sb.append("PNG_").append(posNgramSize);
    String fileNameTag = sb.toString();

    CSFeatureBuilder featureBuilder = new CSFeatureBuilder(wordConjunctionOffset, wordNgramSize,
        posConjunctionOffset, posNgramSize, maxCueOffsetFeature, cueIncludedFeaturesFilePath,
        scopeIncludedFeaturesFilePath);
    CueScopeFeatureSet featureSet = generateTrainingFeaturesFromTrainingFiles(featureBuilder,
        trainingTextFiles, trainingAllConceptsXML, trainingAssertionXML);
    printFeatureSet(featureSet, trainingFeatureFilePath, fileNameTag);
  }


  public void trainNegation(int wordConjuctionOffset, int wordNGramSize, int posConjuctionOffset,
      int posNGramSize, int maxCueOffsetFeatureString, String cueIncludedFeaturesFilePath,
      String scopeIncludedFeaturesFilePath, String solutionFileTag,
      SimpleTriplet<List<File>> trainingLists, String trainingFilePath,
      boolean writeTrainingToFile) {

    CSFeatureBuilder featureBuilder =
        new CSFeatureBuilder(wordConjuctionOffset, wordNGramSize, posConjuctionOffset, posNGramSize,
            maxCueOffsetFeatureString, cueIncludedFeaturesFilePath, scopeIncludedFeaturesFilePath);

    CueScopeFeatureSet trainingSet = generateTrainingFeaturesFromTrainingFiles(featureBuilder,
        trainingLists.left, trainingLists.middle, trainingLists.right);

    String cueModelPath;
    {
      // REGION LOGGING
      if (TaggerTrainer.LOGGER.isDebugEnabled()) {
        PrintWriter pw = new PrintWriter(System.out);
        pw.println("\n\n-----------------------\nCUE TRAINING FEATURES\n-----------------------\n");
        TaggerTrainer.printFeaturesAndLabels(trainingSet.cueTrainingFeatures,
            trainingSet.cueTrainingLabels, pw);
      }
      // ENDREGION LOGGING

      if (writeTrainingToFile) {
        try {
          String cueModelTrainingName = "cueModelTraining_" + GeneralHelper.getTimeStamp() + ".txt";
          File modelFile = new File(trainingFilePath + File.separator + cueModelTrainingName);
          PrintWriter pw = new PrintWriter(modelFile);
          TaggerTrainer.printFeaturesAndLabels(trainingSet.cueTrainingFeatures,
              trainingSet.cueTrainingLabels, pw);

        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      }
      long currentTime = System.currentTimeMillis() / 1000;
      String cueModelName = "CueSolution_" + currentTime + ".mdl";
      cueModelPath = trainingFilePath + File.separator + cueModelName;
      try {
        train(trainingSet.cueTrainingFeatures, trainingSet.cueTrainingLabels, cueModelPath);
      } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      } catch (NullPointerException e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }

    String scopeModelPath;

    /* Now do the scope training */
    {
      if (TaggerTrainer.LOGGER.isDebugEnabled()) {
        PrintWriter pw = new PrintWriter(System.out);
        pw.println(
            "\n\n-----------------------\nSCOPE TRAINING FEATURES\n-----------------------\n");
        TaggerTrainer.printFeaturesAndLabels(trainingSet.scopeTrainingFeatures,
            trainingSet.scopeTrainingLabels, pw);
      }

      if (writeTrainingToFile) {
        try {
          String scopeModelTrainingName =
              "scopeModelTraining_" + GeneralHelper.getTimeStamp() + ".txt";
          File modelFile = new File(trainingFilePath + File.separator + scopeModelTrainingName);
          PrintWriter pw = new PrintWriter(modelFile);
          TaggerTrainer.printFeaturesAndLabels(trainingSet.scopeTrainingFeatures,
              trainingSet.scopeTrainingLabels, pw);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      }
      long currentTime = System.currentTimeMillis() / 1000;
      String scopeModelName = "ScopeSolution_" + currentTime + ".mdl";
      scopeModelPath = trainingFilePath + File.separator + scopeModelName;
      try {
        train(trainingSet.scopeTrainingFeatures, trainingSet.scopeTrainingLabels, scopeModelPath);
      } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      } catch (NullPointerException e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }

    AttributeTaggerSolution solution = new AttributeTaggerSolution(new File(cueModelPath),
        new File(scopeModelPath), featureBuilder, this.context, this.tokenProcessingOptions);

    String finalSolutionName =
        "CueScopeSolution_" + solutionFileTag + "_" + GeneralHelper.getTimeStamp() + ".atsn";
    File solutionFile = new File(trainingFilePath + File.separator + finalSolutionName);
    solution.saveToFile(solutionFile);
  }


  /**
   * Trains a Cue-Scope model to identify negated terms. It allows for resetting of
   * TokenProcessingOptions to whatever the setting is within the trainingOptions parameter. This is
   * put here because the current constructors force establishing tokenProcessingOptions during
   * construction of an instance of a CSAttributeTaggerTrainer, but we need a way to change the
   * options.
   *
   * @param trainingOptions
   * @param solutionFileTag
   * @param trainingLists
   * @param trainingFilePath
   * @param writeTrainingToFile
   * @param setTokenProcessingOptions
   */
  public void trainNegation(TaggerTrainerOptions trainingOptions, String solutionFileTag,
      SimpleTriplet<List<File>> trainingLists, String trainingFilePath,
      boolean writeTrainingToFile) {
    int wordConjuctionOffset = trainingOptions.getWordOffsetDistance();
    int wordNGramSize = trainingOptions.getWordConjunctionSize();
    int posConjuctionOffset = trainingOptions.getPosOffsetDistance();
    int posNGramSize = trainingOptions.getPosConjunctionSize();
    int cueOffsetFeatureMax = trainingOptions.getCueOffsetFeatureMax();

    String cueIncludedFeaturesFilePath = trainingOptions.getCueFeaturesFile().getAbsolutePath();
    String scopeIncludedFeaturesFilePath = trainingOptions.getScopeFeaturesFile().getAbsolutePath();

    this.trainNegation(wordConjuctionOffset, wordNGramSize, posConjuctionOffset, posNGramSize,
        cueOffsetFeatureMax, cueIncludedFeaturesFilePath, scopeIncludedFeaturesFilePath,
        solutionFileTag, trainingLists, trainingFilePath, writeTrainingToFile);
  }


  /**
   * This is a Cue-Scope specific method to essentially filter what is used for training based on
   * whether a cue or scope phrase crosses a sentence boundary to another sentence and whether cue
   * and scope phrases are in the same or different sentences. Sentences used to train the finding
   * of cues are all sentences that have a concept of interest that either 1) have no cue and scope
   * OR 2a) that have a cue and scope in which the cue and scope are in the same sentence AND 2b)
   * all the cue and scope phrase tokens are in the same sentence. Sentences used to train the
   * finding of scope phrases are all those that 1) have a cue and scope in which the cue and scope
   * are in the same sentence and 2) all the cue and scope phrase tokens are in the same sentence,
   * so sentences used for scope training are a subset of cue training sentences.
   *
   * @param phrasesOfInterest
   * @param cueScopePhrases
   * @return
   */
  private CSTrainingSet filterTrainingData(List<AnnotatedPhrase> phrasesOfInterest,
      List<AnnotatedPhrase> cueScopePhrases) {
    CSTrainingSet finalTrainingData = new CSTrainingSet();

    /*
     * First select 'candidateSentences', which are those that contain an annotated phrase from
     * within the list parameter, 'phrasesOfInterest', where all the tokens in the phrase are in the
     * same sentence and the phrase has the attribute, 'AssertionStatus', regardless of whether the
     * value is positive, negative, or uncertain
     */
    SentenceFilter sentenceSelector = new PhraseBasedSentenceFilter(new AssertionStatusFilter());
    Set<AnnotatedPhrase> candidateSentences = sentenceSelector.selectSentences(phrasesOfInterest);

    this.negationScopeFilter.setCandidateSentences(candidateSentences);
    this.negationScopeFilter.createCueAnnotationSet(cueScopePhrases);

    /*
     * Filter out sentences where the scope crosses a sentence boundary or has a cue within another
     * sentence by calling includePhrase(). This not only filters out sentences but collects valid
     * cue and scope phrases, which can be retrieved with getAssociatedCues() and
     * getIncludedPhrases()
     */
    for (AnnotatedPhrase curCueScopePhrase : cueScopePhrases) {
      this.negationScopeFilter.includePhrase(curCueScopePhrase);
    }

    finalTrainingData.cuePhrases = new ArrayList<>(this.negationScopeFilter.getAssociatedCues());
    finalTrainingData.cueTrainingSentences =
        new ArrayList<>(this.negationScopeFilter.getCandidateSentenceSet());

    finalTrainingData.scopePhrases = new ArrayList<>(this.negationScopeFilter.getIncludedPhrases());

    HashSet<AnnotatedPhrase> scopeSentences = new HashSet<>(finalTrainingData.scopePhrases.size());
    for (AnnotatedPhrase curScopePhrase : finalTrainingData.scopePhrases) {
      scopeSentences.add(curScopePhrase.getProcessedTokens().get(0).getSentenceOfOrigin());
    }
    finalTrainingData.scopeTrainingSentences = scopeSentences;
    return finalTrainingData;
  }


  /**
   * This is typically called when creating file for training and creating a cue-scope CRF model. It
   * can be easily called multiple times with different settings of offset and ngram to determine
   * the optimum setting of these for creating conjunctions.
   *
   * @param wordConjunctionOffset
   * @param wordNgramSize
   * @param trainingSet
   * @param posConjunctionOffset
   * @param posNgramSize
   * @return
   */
  private CueScopeFeatureSet generateTrainingFeatures(int wordConjunctionOffset, int wordNgramSize,
      int posConjunctionOffset, int posNgramSize, int cuePostionTagOffset,
      CSTrainingSet trainingSet, String cueIncludedFeaturesFilePath,
      String scopeIncludedFeaturesFilePath) {
    CSFeatureBuilder featureBuilder = new CSFeatureBuilder(wordConjunctionOffset, wordNgramSize,
        posConjunctionOffset, posNgramSize, cuePostionTagOffset, cueIncludedFeaturesFilePath,
        scopeIncludedFeaturesFilePath);
    return (CueScopeFeatureSet) featureBuilder.buildTrainingFeatures(
        trainingSet.cueTrainingSentences, trainingSet.scopeTrainingSentences);
  }


  /**
   * This is the typical method called during training of a cue-scope tagger model, taking several
   * text files, files indicating the concepts of interest, and files indicating the annotations of
   * assertion cue and phrase. A FeatureBuilder instance is also supplied allowing building of
   * different features for training.
   *
   * @param trainingAssertionXML
   * @param trainingAllConceptsXML
   * @param trainingTextFiles
   * @param string
   */
  private CueScopeFeatureSet generateTrainingFeaturesFromTrainingFiles(
      CSFeatureBuilder featureBuilder, List<File> trainingTextFiles,
      List<File> trainingAllConceptsXML, List<File> trainingAssertionXML) {
    int maxSentencesPerDocument = 300;
    List<ItemSequence> cueSentenceFeatures =
        new ArrayList<>(trainingTextFiles.size() * maxSentencesPerDocument);
    List<StringList> cueTokenLabels =
        new ArrayList<>(trainingTextFiles.size() * maxSentencesPerDocument);

    List<ItemSequence> scopeSentenceFeatures =
        new ArrayList<>(trainingTextFiles.size() * maxSentencesPerDocument);
    List<StringList> scopeTokenLabels =
        new ArrayList<>(trainingTextFiles.size() * maxSentencesPerDocument);

    int totalSentences = 0;

    for (int fileIndex = 0; fileIndex < trainingTextFiles.size(); fileIndex++) {
      File textFile = trainingTextFiles.get(fileIndex);
      File allConceptsXML = trainingAllConceptsXML.get(fileIndex);
      File assertionXML = trainingAssertionXML.get(fileIndex);

      CSTrainingSet trainingSet = getTrainingSet(textFile, allConceptsXML, assertionXML);

      /*
       * Sort sentences only because it's easier to debug if sentences are always in same order
       */
      Collections.sort(trainingSet.cueTrainingSentences);
      CueScopeFeatureSet documentFeatures =
          (CueScopeFeatureSet) featureBuilder.buildTrainingFeatures(
              trainingSet.cueTrainingSentences, trainingSet.scopeTrainingSentences);

      if (TaggerTrainer.LOGGER.isDebugEnabled()) {
        totalSentences += trainingSet.cueTrainingSentences.size();
        System.out.println("TotalSentences:" + totalSentences);
      }

      cueSentenceFeatures.addAll(documentFeatures.cueTrainingFeatures);
      cueTokenLabels.addAll(documentFeatures.cueTrainingLabels);

      scopeSentenceFeatures.addAll(documentFeatures.scopeTrainingFeatures);
      scopeTokenLabels.addAll(documentFeatures.scopeTrainingLabels);
    }

    return new CueScopeFeatureSet(cueSentenceFeatures, cueTokenLabels, scopeSentenceFeatures,
        scopeTokenLabels);
  }


  /**
   * This just imports the AnnotatedPhrase instances that represent the concepts of interest and the
   * AnnotatedPhrase instances that represent the cue and scope. The method returns them as a pair
   * of lists.
   *
   * @param textFile
   * @param allConceptsXML
   * @param assertionXML
   * @return
   */
  private RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTrainingData(
      File textFile, File allConceptsXML, File assertionXML) {
    /*
     * Process the document being analyzed to get the document tokens, which are needed for labeling
     * the tokens associated with the phrases
     */
    RaptatDocument doc = this.textAnalyzer.processDocument(textFile.getAbsolutePath());

    /*
     * Get the phrases that have been annotated with some concept (not cue-scope)
     */
    List<AnnotatedPhrase> allConceptPhrases = this.annotationImporter
        .importAnnotations(allConceptsXML.getAbsolutePath(), textFile.getAbsolutePath(), null);
    this.theIntegrator.addProcessedTokensToAnnotations(allConceptPhrases, doc, false, 0);

    List<AnnotatedPhrase> cueScopeTrainingPhrases = this.annotationImporter
        .importAnnotations(assertionXML.getAbsolutePath(), textFile.getAbsolutePath(), null);
    this.theIntegrator.addProcessedTokensToAnnotations(cueScopeTrainingPhrases, doc, false, 0);

    return new RaptatPair<>(allConceptPhrases, cueScopeTrainingPhrases);
  }


  /**
   * @param trainingFileDirectory
   * @param fileNameTag
   * @param string
   * @return
   */
  private PrintWriter getFeaturePrintWriter(String trainingFileDirectory, String fileNameTag,
      String featureType) {
    PrintWriter pw = null;
    StringBuilder cueSB = new StringBuilder(trainingFileDirectory).append(File.separator);
    cueSB.append(featureType).append("_");
    if (fileNameTag != null && fileNameTag.length() > 0) {
      cueSB.append(fileNameTag).append("_");
    }
    cueSB.append(GeneralHelper.getTimeStamp()).append(".txt");
    try {
      pw = new PrintWriter(new File(cueSB.toString()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return pw;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      private List<File> trainingTextFiles = new ArrayList<>(512);
      private List<File> trainingAssertionXML = new ArrayList<>(512);
      private List<File> trainingAllConceptsXML = new ArrayList<>(512);

      private final String trainingFilePath = "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160720";


      /**
       * Allows creation of multiple training files with a variety of offsets and conjunction sizes.
       * These can then be used to train and evaluate a cue scope model and determine how
       * performance varies with offset and conjunction size.
       *
       * @param trainingFilePath
       * @param minOffset
       * @param maxOffset
       * @param minNgram
       * @param maxNgram
       * @param cueIncludedFeaturesFilePath
       * @param scopeIncludedFeaturesFilePath
       */
      public void createMultipleTrainingFiles(String trainingFilePath, int minOffset, int maxOffset,
          int minNgram, int maxNgram, int maxCueOffsetFeature, String cueIncludedFeaturesFilePath,
          String scopeIncludedFeaturesFilePath) {
        CSAttributeTaggerTrainer csAttributeTrainer = new CSAttributeTaggerTrainer();
        CSTrainingSet fullTrainingSet = csAttributeTrainer.new CSTrainingSet();

        for (int fileIndex = 0; fileIndex < this.trainingTextFiles.size(); fileIndex++) {
          File textFile = this.trainingTextFiles.get(fileIndex);
          File allConceptsXML = this.trainingAllConceptsXML.get(fileIndex);
          File assertionXML = this.trainingAssertionXML.get(fileIndex);

          CSTrainingSet documentTrainingSet =
              csAttributeTrainer.getTrainingSet(textFile, allConceptsXML, assertionXML);
          fullTrainingSet.cuePhrases.addAll(documentTrainingSet.cuePhrases);
          fullTrainingSet.cueTrainingSentences.addAll(documentTrainingSet.cueTrainingSentences);
          fullTrainingSet.scopePhrases.addAll(documentTrainingSet.scopePhrases);
          fullTrainingSet.scopeTrainingSentences.addAll(documentTrainingSet.scopeTrainingSentences);
        }
        int wordConjunctionOffset = 2;
        int wordNgramSize = 2;
        for (int i = minOffset; i <= maxOffset; i++) {
          for (int j = minNgram; j <= maxNgram; j++) {
            CueScopeFeatureSet curFeatures = csAttributeTrainer.generateTrainingFeatures(
                wordConjunctionOffset, wordNgramSize, i, j, maxCueOffsetFeature, fullTrainingSet,
                cueIncludedFeaturesFilePath, scopeIncludedFeaturesFilePath);

            String trainingFileName =
                i + "_" + j + "_CueTraining_" + GeneralHelper.getTimeStamp() + ".txt";
            String fullTrainingFilePath = trainingFilePath + File.separator + trainingFileName;
            try {
              PrintWriter pw = new PrintWriter(fullTrainingFilePath);
              TaggerTrainer.printFeaturesAndLabels(curFeatures.cueTrainingFeatures,
                  curFeatures.cueTrainingLabels, pw);
              pw.close();
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
      }


      @Override
      public void run() {
        RunType runType = RunType.TRAIN_NEGATION_SINGLE;
        String cueIncludedFeaturesFilePath = null;
        String scopeIncludedFeaturesFilePath = null;
        int wordConjuctionOffset = 4;
        int wordNgramSize = 4;
        int posConjunctionOffset = 4;
        int posNgramSize = 4;
        int maxCueOffsetFeature = 8;
        String fileNameTag = "Rd00_CSTrainingFeatures_UnprocessedTokensStopsNOTRemoved_v01";
        TokenProcessingOptions tokenProcessingOptions =
            new TokenProcessingOptions(OptionsManager.getInstance());

        /*
         * Generally stop words are removed - here we are testing the impact of not removing them
         */
        boolean removeStopWords = false;
        tokenProcessingOptions.setRemoveStopWords(removeStopWords);

        createTrainingDocumentLists(2, 4);

        switch (runType) {
          case PRINT_FEATURES:
            new CSAttributeTaggerTrainer(tokenProcessingOptions).printFeaturesFromTrainingFiles(
                wordConjuctionOffset, wordNgramSize, posConjunctionOffset, posNgramSize,
                maxCueOffsetFeature, this.trainingTextFiles, this.trainingAllConceptsXML,
                this.trainingAssertionXML, this.trainingFilePath, cueIncludedFeaturesFilePath,
                scopeIncludedFeaturesFilePath, fileNameTag);
            break;

          case TEST_ANNOTATION_FILTERING:
            String documentPath =
                "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\AssertionSentenceSelectionTesting\\Block_01_Note_001.txt";
            String assertionXMLPath =
                "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\AssertionSentenceSelectionTesting\\Block_01_Note_001_assertion.txt.knowtator.xml";
            String conceptXMLPath =
                "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\AssertionSentenceSelectionTesting\\Block_01_Note_001_concept.txt.knowtator.xml";
            new CSAttributeTaggerTrainer().getTrainingSet(new File(documentPath),
                new File(conceptXMLPath), new File(assertionXMLPath));
            break;

          case TRAIN_NEGATION_SINGLE:
            SimpleTriplet<List<File>> trainingLists = new SimpleTriplet<>(this.trainingTextFiles,
                this.trainingAllConceptsXML, this.trainingAssertionXML);
            boolean writeTrainingToFile = false;
            CSAttributeTaggerTrainer trainer =
                new CSAttributeTaggerTrainer(ContextType.ASSERTIONSTATUS, tokenProcessingOptions);
            trainer.trainNegation(wordConjuctionOffset, wordNgramSize, posConjunctionOffset,
                posNgramSize, maxCueOffsetFeature, cueIncludedFeaturesFilePath,
                scopeIncludedFeaturesFilePath, fileNameTag, trainingLists, this.trainingFilePath,
                writeTrainingToFile);
            break;

          case CREATE_MULTIPLE_TRAINING_FILES:
            int minOffset = 1;
            int maxOffset = 7;
            int minNGram = 1;
            int maxNGram = 7;
            createMultipleTrainingFiles(this.trainingFilePath, minOffset, maxOffset, minNGram,
                maxNGram, maxCueOffsetFeature, cueIncludedFeaturesFilePath,
                scopeIncludedFeaturesFilePath);
            break;
        }
      }


      /**
       * Creates list of text files, xml files with assertions for cue-scope training, and xml files
       * with the concepts that the cues and scopes pertain to. All are needed for cue-scope
       * training.
       *
       * @param startIndex
       * @param endIndex
       */
      protected void createTrainingDocumentLists(int startIndex, int endIndex) {
        String textFilePath = "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160714\\"
            + "AssertionAnnotationForTraining\\corpus\\block0";
        String assertionRefFilePath = "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160714\\"
            + "AssertionAnnotationForTraining\\AssertionAnnotationBlocks\\Block0";
        String conceptRefFilePath = "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160714\\"
            + "AssertionAnnotationForTraining\\AKIConceptAnnotationBlocks\\Block0";

        File[] fileList;
        for (int i = startIndex; i < endIndex + 1; i++) {
          fileList = new File(textFilePath + i).listFiles();
          this.trainingTextFiles.addAll(Arrays.asList(fileList));
          fileList = new File(assertionRefFilePath + i).listFiles();
          this.trainingAssertionXML.addAll(Arrays.asList(fileList));
          fileList = new File(conceptRefFilePath + i).listFiles();
          this.trainingAllConceptsXML.addAll(Arrays.asList(fileList));
        }
      }


      /**
       * Allows use of a limited set of text, cue-scope xml, and annotated concept xml files. The
       * files chosen correspond to textfiles included in the parameter filelist.
       *
       * @param fileList
       */
      private void resetTrainingDocumentLists(Collection<String> fileList) {
        List<File> resetTextFiles = new ArrayList<>();
        List<File> resetConceptFiles = new ArrayList<>();
        List<File> resetAssertionFiles = new ArrayList<>();

        for (int fileIndex = 0; fileIndex < this.trainingTextFiles.size(); fileIndex++) {
          String textFileName = this.trainingTextFiles.get(fileIndex).getName();
          // System.out.println( textFileName );

          if (fileList.contains(textFileName)) {
            resetTextFiles.add(this.trainingTextFiles.get(fileIndex));
            resetConceptFiles.add(this.trainingAllConceptsXML.get(fileIndex));
            resetAssertionFiles.add(this.trainingAssertionXML.get(fileIndex));
          }
        }
        this.trainingTextFiles = resetTextFiles;
        this.trainingAllConceptsXML = resetConceptFiles;
        this.trainingAssertionXML = resetAssertionFiles;
      }
    });
  }
}
