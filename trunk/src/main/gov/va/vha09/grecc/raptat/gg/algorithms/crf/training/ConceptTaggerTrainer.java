/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.ConceptFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CrfFeatureSet;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.CrfConceptSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.TaggerTrainerOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/** @author Glenn T. Gobbel Apr 19, 2016 */
public class ConceptTaggerTrainer extends TaggerTrainer {
  // @formatter:off
  protected enum RunType {
    TRAIN,
    PRINTFEATURES,
    FILTERFEATURES,
    TRAIN_WITH_EXISTING_FEATURES,
    PRINT_CROSSVALIDATION_FEATURES,
    FILTERFEATURES_BYLABEL,
    CREATE_COMMON_FEATURES_FILE
  }
  // @formatter:on

  protected static final Logger LOGGER = Logger.getLogger(ConceptTaggerTrainer.class);


  {
    ConceptTaggerTrainer.LOGGER.setLevel(Level.INFO);
  }

  /**
   * @param tokenProcessingOptions
   */
  public ConceptTaggerTrainer(TokenProcessingOptions tokenProcessingOptions) {
    super(tokenProcessingOptions);
  }


  public CrfFeatureSet getDocumentFeatures(ConceptFeatureBuilder featureBuilder,
      HashSet<String> conceptSet, File textFile, File conceptsXML, boolean isForTraining) {
    List<AnnotatedPhrase> sentenceList =
        getSentenceList(textFile, conceptsXML, conceptSet, isForTraining);

    /*
     * Sort sentences only because it's easier to debug if sentences are always in same order
     */
    Collections.sort(sentenceList);
    CrfFeatureSet documentFeatures = featureBuilder.buildFeatures(sentenceList);

    return documentFeatures;
  }


  /*
   * This has not been implemented. The method was created in the super class as a way to generalize
   * the method of printing cross-validation feature sets within the ReasonNoMedsTaggerTrainer class
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training. TaggerTrainer
   * #getDocumentTestingData(java.io.File, java.io.File, java.util.HashSet)
   */
  @Override
  public RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTestingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet) {
    throw new NotImplementedException("Not yet implemented: TO DO");
  }


  /**
   * Returns the training phrases and sentences from a single document as a pair with the sentences
   * on the left and the phrases on the right side of the pair. Only annotated phrases that map to
   * concepts in the conceptSet variable are returned
   *
   * @param textFile
   * @param conceptSet
   * @param allConceptsXML
   * @param assertionXML
   * @return
   */
  @Override
  public RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTrainingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet) {
    /*
     * Process the document being analyzed to get the document tokens, which are needed for labeling
     * the tokens associated with the phrases
     */
    RaptatDocument doc = this.textAnalyzer.processDocument(textFile.getAbsolutePath());

    /*
     * Get the phrases that have been annotated with some concept
     */
    List<AnnotatedPhrase> annotatedPhrases = this.annotationImporter
        .importAnnotations(conceptsXML.getAbsolutePath(), textFile.getAbsolutePath(), conceptSet);
    this.theIntegrator.addProcessedTokensToAnnotations(annotatedPhrases, doc, false, 0);

    return new RaptatPair<>(doc.getActiveSentences(), annotatedPhrases);
  }


  public void train(TaggerTrainerOptions trainingOptions, String solutionFileTag,
      RaptatPair<List<File>, List<File>> trainingLists, String trainingFilePath,
      boolean writeTrainingToFile, HashSet<String> conceptSet) {
    int wordConjuctionOffset = trainingOptions.getWordOffsetDistance();
    int wordNGramSize = trainingOptions.getWordConjunctionSize();
    int posConjuctionOffset = trainingOptions.getPosOffsetDistance();
    int posNGramSize = trainingOptions.getPosConjunctionSize();

    String includedFeaturesFilePath =
        trainingOptions.getConceptIncludedFeaturesFile().getAbsolutePath();

    this.train(wordConjuctionOffset, wordNGramSize, posConjuctionOffset, posNGramSize,
        includedFeaturesFilePath, solutionFileTag, trainingLists, trainingFilePath,
        writeTrainingToFile, conceptSet);
  }


  /**
   * @param featureBuilder
   * @param conceptSet2
   * @param left
   * @param right
   * @return
   */
  protected CrfFeatureSet generateTrainingFeaturesFromTrainingFiles(
      ConceptFeatureBuilder featureBuilder, List<File> trainingTextFiles,
      List<File> trainingXMLFiles, HashSet<String> conceptSet) {

    int maxSentencesPerDocument = 300;
    List<ItemSequence> sentenceFeatures =
        new ArrayList<>(trainingTextFiles.size() * maxSentencesPerDocument);
    List<StringList> tokenLabels =
        new ArrayList<>(trainingTextFiles.size() * maxSentencesPerDocument);

    for (int fileIndex = 0; fileIndex < trainingTextFiles.size(); fileIndex++) {
      File textFile = trainingTextFiles.get(fileIndex);
      File conceptsXML = trainingXMLFiles.get(fileIndex);
      boolean isTraining = true;
      CrfFeatureSet documentFeatures =
          getDocumentFeatures(featureBuilder, conceptSet, textFile, conceptsXML, isTraining);

      sentenceFeatures.addAll(documentFeatures.trainingFeatures);
      tokenLabels.addAll(documentFeatures.trainingLabels);
    }

    return new CrfFeatureSet(sentenceFeatures, tokenLabels);
  }


  protected void train(CrfFeatureSet trainingSet, ConceptFeatureBuilder featureBuilder,
      HashSet<String> conceptSet, String fileNameTag, String solutionDirectory) {
    long currentTime = System.currentTimeMillis() / 1000;
    String solutionName = "CrfSolution_" + fileNameTag + "_" + currentTime + ".mdl";
    String solutionPath = solutionDirectory + File.separator + solutionName;
    try {
      this.train(trainingSet.trainingFeatures, trainingSet.trainingLabels, solutionPath);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    } catch (NullPointerException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

    CrfConceptSolution solution = new CrfConceptSolution(new File(solutionPath), featureBuilder,
        conceptSet, this.tokenProcessingOptions);

    String finalSolutionName =
        "CrfSolution_" + fileNameTag + "_" + GeneralHelper.getTimeStamp() + ".atsn";
    File solutionFile = new File(solutionDirectory + File.separator + finalSolutionName);
    solution.saveToFile(solutionFile);
  }


  protected void writeTrainingFeatures(int wordConjuctionOffset, int wordNGramSize,
      int posConjuctionOffset, int posNGramSize, String includedFeaturesFilePath,
      String solutionFileTag, RaptatPair<List<File>, List<File>> trainingLists,
      String trainingFilePath, HashSet<String> conceptSet) {
    ConceptFeatureBuilder featureBuilder = new ConceptFeatureBuilder(wordConjuctionOffset,
        wordNGramSize, posConjuctionOffset, posNGramSize, includedFeaturesFilePath);

    CrfFeatureSet trainingSet = generateTrainingFeaturesFromTrainingFiles(featureBuilder,
        trainingLists.left, trainingLists.right, conceptSet);

    try {
      String trainingModelName = "conceptModelTraining_" + GeneralHelper.getTimeStamp() + ".txt";
      File modelFile = new File(trainingFilePath + File.separator + trainingModelName);
      PrintWriter pw = new PrintWriter(modelFile);
      TaggerTrainer.printFeaturesAndLabels(trainingSet.trainingFeatures, trainingSet.trainingLabels,
          pw);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  /**
   * This does the same thing as the generateTrainingFeaturesFromTrainingFiles() method but this
   * prints the token features to a file pointed to by printwriter pw rather than returning them as
   * one object
   *
   * @param featureBuilder
   * @param conceptSet2
   * @param left
   * @param right
   * @return
   */
  protected void writeTrainingFeaturesUsingTrainingFiles(ConceptFeatureBuilder featureBuilder,
      List<File> trainingTextFiles, List<File> trainingXMLFiles, HashSet<String> conceptSet,
      PrintWriter pw) {
    int fileNumber = trainingTextFiles.size();
    for (int fileIndex = 0; fileIndex < trainingTextFiles.size(); fileIndex++) {
      System.out.println("Building features for file " + (fileIndex + 1) + " of " + fileNumber);
      File textFile = trainingTextFiles.get(fileIndex);
      File conceptsXML = trainingXMLFiles.get(fileIndex);

      boolean isTraining = true;
      List<AnnotatedPhrase> trainingSentences =
          getSentenceList(textFile, conceptsXML, conceptSet, isTraining);

      /*
       * Sort sentences only because it's easier to debug if sentences are always in same order
       */
      Collections.sort(trainingSentences);
      CrfFeatureSet documentFeatures = featureBuilder.buildFeatures(trainingSentences);

      printFeaturesAndLabels(documentFeatures.trainingFeatures, documentFeatures.trainingLabels,
          pw);
    }
  }


  /**
   * Creates a set of phrases and sentence for training, excluding phrases that are in two separate
   * sentences and also excluding the sentences associated with the excluded phrase. The results are
   * returned as a pair with the sentences on the left (returned as a set) and phrases on the right
   * (returned as a list).
   *
   * @param sentences
   * @param conceptPhrases
   * @return
   * @return
   */
  private RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> filterTrainingData(
      List<AnnotatedPhrase> sentences, List<AnnotatedPhrase> conceptPhrases) {
    PhraseFilter phraseFilter = new PhraseFilter();
    phraseFilter.setCandidateSentences(new HashSet<>(sentences));
    List<AnnotatedPhrase> resultPhrases = new ArrayList<>(conceptPhrases.size());

    for (AnnotatedPhrase curPhrase : conceptPhrases) {
      if (phraseFilter.includePhrase(curPhrase)) {
        resultPhrases.add(curPhrase);
      } else {
        phraseFilter.excludeTokenSentence(curPhrase);
      }
    }

    List<AnnotatedPhrase> sentenceList = new ArrayList<>(phraseFilter.getCandidateSentenceSet());
    return new RaptatPair<>(sentenceList, resultPhrases);
  }


  /**
   * @param textFile
   * @param conceptsXML
   * @param conceptSet
   * @param isForTraining
   * @return
   */
  private List<AnnotatedPhrase> getSentenceList(File textFile, File conceptsXML,
      HashSet<String> conceptSet, boolean isForTraining) {
    RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> trainingTestingList;
    if (isForTraining) {
      trainingTestingList = getDocumentTrainingData(textFile, conceptsXML, conceptSet);
      trainingTestingList = filterTrainingData(trainingTestingList.left, trainingTestingList.right);
    } else {
      trainingTestingList = getDocumentTestingData(textFile, conceptsXML, conceptSet);
    }

    /*
     * Mark tokens of phrases that are to be used for CRF labels (i.e., begin, inside, or outside
     * labels) so that we can identify them when assigning labels to the tokens in each sentence.
     */
    this.markPhraseTokens(trainingTestingList.right);

    return trainingTestingList.left;
  }


  /**
   * Train a CRF model to identify a concept using a XML file with the concept or concepts and a
   * text file
   *
   * @param wordConjuctionOffset
   * @param wordNGramSize
   * @param posConjuctionOffset
   * @param posNGramSize
   * @param includedFeaturesFilePath
   * @param solutionFileTag
   * @param trainingLists
   * @param trainingFilePath
   * @param writeTrainingToFile
   * @param conceptSet
   */
  private void train(int wordConjuctionOffset, int wordNGramSize, int posConjuctionOffset,
      int posNGramSize, String includedFeaturesFilePath, String solutionFileTag,
      RaptatPair<List<File>, List<File>> trainingLists, String trainingFilePath,
      boolean writeTrainingToFile, HashSet<String> conceptSet) {
    ConceptFeatureBuilder featureBuilder = new ConceptFeatureBuilder(wordConjuctionOffset,
        wordNGramSize, posConjuctionOffset, posNGramSize, includedFeaturesFilePath);

    CrfFeatureSet trainingSet = generateTrainingFeaturesFromTrainingFiles(featureBuilder,
        trainingLists.left, trainingLists.right, conceptSet);

    if (ConceptTaggerTrainer.LOGGER.isDebugEnabled()) {
      PrintWriter pw = new PrintWriter(System.out);
      pw.println("\n\n-----------------------\nCUE TRAINING FEATURES\n-----------------------\n");
      TaggerTrainer.printFeaturesAndLabels(trainingSet.trainingFeatures, trainingSet.trainingLabels,
          pw);
    }

    if (writeTrainingToFile) {
      try {
        String cueModelTrainingName =
            "conceptModelTraining_" + GeneralHelper.getTimeStamp() + ".txt";
        File modelFile = new File(trainingFilePath + File.separator + cueModelTrainingName);
        PrintWriter pw = new PrintWriter(modelFile);
        TaggerTrainer.printFeaturesAndLabels(trainingSet.trainingFeatures,
            trainingSet.trainingLabels, pw);

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    this.train(trainingSet, featureBuilder, conceptSet, solutionFileTag, trainingFilePath);
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      private final List<File> trainingTextFiles = new ArrayList<>(512);
      private final List<File> trainingConceptsXML = new ArrayList<>(512);

      private final String trainingFilePath =
          "D:\\CARTCL-IIR\\ActiveLearning\\AKIAssertionTesting\\NegexCRFTrainingGG_";


      @Override
      public void run() {
        RunType runType = RunType.PRINTFEATURES;
        String includedFeaturesFilePath =
            "D:\\CARTCL-IIR\\ActiveLearning\\AKIAssertionTesting\\NegexCRFTrainingGG_\\RenalFeaturesRd02.txt";
        int wordConjuctionOffset = 3;
        int wordNgramSize = 3;
        int posConjunctionOffset = 3;
        int posNgramSize = 3;
        String fileNameTag = "Rd02_TaggerSolution_v01";
        TokenProcessingOptions tokenProcessingOptions =
            new TokenProcessingOptions(OptionsManager.getInstance());
        HashSet<String> conceptSet = new HashSet<>();
        /* Add to concept set to train using only specific concepts */
        conceptSet.add("renalfunctionimpairment");

        createTrainingDocumentLists(2, 4);

        ConceptTaggerTrainer trainer = new ConceptTaggerTrainer(tokenProcessingOptions);
        RaptatPair<List<File>, List<File>> trainingLists =
            new RaptatPair<>(this.trainingTextFiles, this.trainingConceptsXML);

        switch (runType) {
          case TRAIN:
            trainer.train(wordConjuctionOffset, wordNgramSize, posConjunctionOffset, posNgramSize,
                includedFeaturesFilePath, fileNameTag, trainingLists, this.trainingFilePath, true,
                conceptSet);
            break;
          case PRINTFEATURES:
            trainer.writeTrainingFeatures(wordConjuctionOffset, wordNgramSize, posConjunctionOffset,
                posNgramSize, includedFeaturesFilePath, fileNameTag, trainingLists,
                this.trainingFilePath, conceptSet);
            break;
        }
      }


      /**
       * Creates list of text files
       *
       * @param startIndex
       * @param endIndex
       */
      protected void createTrainingDocumentLists(int startIndex, int endIndex) {
        String textFilePath = "D:\\CARTCL-IIR\\ActiveLearning\\corpus\\block0";
        String conceptRefFilePath =
            "D:\\CARTCL-IIR\\ActiveLearning\\AKIAssertionTesting\\AKIConceptAnnotationBlocks\\Block0";

        File[] fileList;
        for (int i = startIndex; i < endIndex + 1; i++) {
          fileList = new File(textFilePath + i).listFiles();
          this.trainingTextFiles.addAll(Arrays.asList(fileList));
          fileList = new File(conceptRefFilePath + i).listFiles();
          this.trainingConceptsXML.addAll(Arrays.asList(fileList));
        }
      }
    });
  }
}
