package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated.TrainingProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.deprecated.AnnotationFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class GarvinCreate {
  private enum PhraseMark implements Mark {
    GENERIC_PHRASE_MARK;
  }


  private TextAnalyzer ta = new TextAnalyzer();

  private HashMap<Integer, HashMap<String, AnnotationGroup>> cvGroups = new HashMap<>(100);
  private List<AnnotationGroup> allAnnotationGroups = new ArrayList<>(200);
  private TrainingProcessor tp;
  private AnnotationFinder theAnnotator;

  private PerformanceScorer scorer;

  private GarvinCreate() {}


  private void conformAnnotations() {
    AnnotationImporter importer = AnnotationImporter.getImporter();
    for (AnnotationGroup curGroup : this.allAnnotationGroups) {
      String pathToDocument = curGroup.getRaptatDocument().getTextSourcePath().get();
      List<AnnotatedPhrase> annotations = curGroup.referenceAnnotations;
      importer.conformXMLOffsetsToText(pathToDocument, annotations);
    }
  }


  private void createScorer(File inputFile) {
    /*
     * This sets the scoring file within the Options Manager, which is used when an instance of a
     * PerformanceScorer is created
     */
    setScoreFile(inputFile.getParent());
    HashSet<String> acceptedConcepts = new HashSet<>(8);
    acceptedConcepts.add("ReasonNoMeds");
    int maxTokens = 7;
    this.scorer = new PerformanceScorer(acceptedConcepts, maxTokens);
  }


  private void createTrainingProcessor(File saveSolutionDirectory) {
    boolean useStems = false;
    boolean usePOS = true;
    boolean removeStopWords = true;
    boolean invertTokenSequence = false;
    ContextHandling reasonNoMedsProcessing = ContextHandling.NONE;
    ContextHandling negationProcessing = ContextHandling.NONE;
    int maxTokenNumber = 7;
    TokenProcessingOptions options = new TokenProcessingOptions(useStems, usePOS, removeStopWords,
        invertTokenSequence, reasonNoMedsProcessing, negationProcessing, maxTokenNumber);
    this.tp = new TrainingProcessor(options, new PhraseIDTrainOptions(OptionsManager.getInstance()),
        null, saveSolutionDirectory);
  }


  private void filterAnnotations(
      RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>> trainingAndTestGroups) {
    PhraseTree pt = generatePhraseTree(trainingAndTestGroups.right);

    removeUnmarkedAnnotations(trainingAndTestGroups.left, pt);
  }


  private boolean findPhrase(String[] stringArray, PhraseTree phraseTree) {
    for (int i = 0; i < stringArray.length; i++) {
      int phraseLocation =
          phraseTree.phraseLengthAtIndexIgnoreCase(stringArray, i, PhraseMark.GENERIC_PHRASE_MARK);
      if (phraseLocation > 0) {
        return true;
      }
    }
    return false;
  }


  private PhraseTree generatePhraseTree(List<AnnotationGroup> trainingGroups) {
    List<String[]> thePhrases = new ArrayList<>(200);
    for (AnnotationGroup curGroup : trainingGroups) {
      for (String[] curPhrase : curGroup.contextPhrases) {
        thePhrases.add(curPhrase);
      }
    }
    boolean ignoreCase = true;
    PhraseTree pt = new PhraseTree(ignoreCase);
    pt.addPhrases(thePhrases, PhraseMark.GENERIC_PHRASE_MARK);
    return pt;
  }


  private File getInputFile() {
    String[] fileTypes = {".txt"};
    File inputFile = GeneralHelper.getFileByType("Select file containing needed info", fileTypes);
    return inputFile;
  }


  private RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>> getTrainingAndTestGroups(
      int testingIndex, List<Integer> cvKeys) {
    List<AnnotationGroup> testingGroups =
        new ArrayList<>(this.cvGroups.get(cvKeys.get(testingIndex)).values());
    List<AnnotationGroup> trainingGroups = new ArrayList<>(50);
    for (int j = 0; j < cvKeys.size(); j++) {
      if (j != testingIndex) {
        trainingGroups.addAll(this.cvGroups.get(cvKeys.get(testingIndex)).values());
      }
    }
    return new RaptatPair<>(trainingGroups, testingGroups);
  }


  private void importData(String[] inputData) {
    /*
     * Expect that data will have form such that [0] = Document number; [1] = Cross-validation
     * group; [2] = Start offset; [3] = End offset; [4] = Text; [5] = Annotator; [6] = Concept
     * class;
     */
    AnnotationGroup curAnnotationGroup;
    Integer curGroup = Integer.parseInt(inputData[1]);
    HashMap<String, AnnotationGroup> annotationGroups;

    if ((annotationGroups = this.cvGroups.get(inputData[0])) == null) {
      annotationGroups = new HashMap<>(5);
      this.cvGroups.put(curGroup, annotationGroups);
    }

    if ((curAnnotationGroup = annotationGroups.get(inputData[0])) == null) {

      RaptatDocument curDocument = this.ta.processDocument(inputData[0]);
      curAnnotationGroup = new AnnotationGroup(curDocument, null);
      annotationGroups.put(inputData[0], curAnnotationGroup);
      this.allAnnotationGroups.add(curAnnotationGroup);
    }

    if (inputData[6].equalsIgnoreCase("BB") || inputData[6].equalsIgnoreCase("ReasonNoMeds")) {
      String[] annotationData = new String[6];
      annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] =
          "RAPTAT_INSTANCE_" + System.currentTimeMillis();
      for (int i = 1; i < 6; i++) {
        annotationData[i] = inputData[i + 1];
      }
      AnnotatedPhrase ap = new AnnotatedPhrase(annotationData, null, null);
      curAnnotationGroup.addReferenceAnnotation(ap);
    } else if (inputData[6].equals("CausalCue")) {
      String[] cueTokenStrings = inputData[6].split(" ");
      List<String> stringList = new ArrayList<>(cueTokenStrings.length);
      for (int i = 0; i < cueTokenStrings.length; i++) {
        String curString = cueTokenStrings[i].trim();
        if (curString.length() > 0) {
          stringList.add(curString);
        }
      }
      String[] stringArray = new String[stringList.size()];
      stringArray = stringList.toArray(stringArray);
      curAnnotationGroup.addContextPhrase(stringArray);
    }
  }


  /* Mark sentence containing a concept named concept name */
  private void importFiles(File inputFile) {
    TabReader tr = new TabReader(inputFile);

    String[] dataLine;
    while ((dataLine = tr.getNextData()) != null) {
      importData(dataLine);
    }
  }


  private void initialize(File saveSolutionDirectory) {
    createTrainingProcessor(saveSolutionDirectory);
    this.theAnnotator = new AnnotationFinder(null);
  }


  private void markSentences(List<AnnotationGroup> annotatedGroups, String conceptName,
      PhraseTree phraseTree) {
    for (AnnotationGroup curGroup : annotatedGroups) {
      for (AnnotatedPhrase curPhrase : curGroup.raptatAnnotations) {
        if (curPhrase.getConceptName().equalsIgnoreCase(conceptName)) {
          List<RaptatToken> phraseTokens = curPhrase.getProcessedTokens();
          AnnotatedPhrase associatedSentence = phraseTokens.get(0).getSentenceOfOrigin();
          String[] sentenceStringTokens = associatedSentence.getRawTokensAugmentedStringsAsArray();
          boolean phraseFound = findPhrase(sentenceStringTokens, phraseTree);
          if (phraseFound) {
            associatedSentence.addSentenceAssociatedConcept("bb");
          }
        }
      }
    }
  }


  private void removeUnmarkedAnnotations(List<AnnotationGroup> annotatedGroups,
      PhraseTree phraseTree) {
    String conceptName = "bb";
    markSentences(annotatedGroups, conceptName, phraseTree);
    for (AnnotationGroup curGroup : annotatedGroups) {
      List<AnnotatedPhrase> finalAnnotations = new ArrayList<>(curGroup.raptatAnnotations.size());
      for (AnnotatedPhrase curPhrase : curGroup.raptatAnnotations) {
        if (curPhrase.getConceptName().equalsIgnoreCase("ReasonNoMeds")) {
          List<RaptatToken> rawTokens = curPhrase.getRawTokens();
          AnnotatedPhrase associatedSentence = rawTokens.get(0).getSentenceOfOrigin();
          if (associatedSentence.getSentenceAssociatedConcepts().contains(conceptName)) {
            finalAnnotations.add(curPhrase);
          }
        }
      }
      curGroup.setRaptatAnnotations(finalAnnotations);
    }
  }


  private void resetRaptatAnnotations(List<AnnotationGroup> annotationGroups) {
    for (AnnotationGroup curGroup : annotationGroups) {
      curGroup.raptatAnnotations.clear();
    }
  }


  private void runCrossValidation() {
    List<Integer> cvKeys = new LinkedList<>(this.cvGroups.keySet());
    Collections.sort(cvKeys);

    /*
     * We skip over i=0 as this the annotations in this group will always be used for training
     */
    for (int i = 1; i < cvKeys.size(); i++) {
      RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>> trainingAndTestGroups =
          getTrainingAndTestGroups(i, cvKeys);
      this.tp.trainRaptat(trainingAndTestGroups.left, false);
      this.theAnnotator.setSolution(this.tp.getAlgorithmSolution());
      this.theAnnotator.identifyPhrasesAndAnnotations(trainingAndTestGroups.right, null, false);
      filterAnnotations(trainingAndTestGroups);
      this.scorer.scorePerformance(trainingAndTestGroups.right);
      resetRaptatAnnotations(trainingAndTestGroups.right);
    }
  }


  private void setScoreFile(String parent) {
    String scoreFileName = new StringBuilder(parent).append(File.separator).append("CVScore__")
        .append(System.currentTimeMillis()).toString();
    OptionsManager.getInstance().setScoreDir(new File(scoreFileName));
  }


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        GarvinCreate gc = new GarvinCreate();
        File inputFile = gc.getInputFile();
        gc.createScorer(inputFile);
        gc.initialize(inputFile);

        gc.importFiles(inputFile);
        gc.conformAnnotations();

        gc.allAnnotationGroups = gc.tp.prepareForTraining(gc.allAnnotationGroups);
        gc.runCrossValidation();
        gc.scorer.writeResultsSimple();
      }
    });
  }
}
