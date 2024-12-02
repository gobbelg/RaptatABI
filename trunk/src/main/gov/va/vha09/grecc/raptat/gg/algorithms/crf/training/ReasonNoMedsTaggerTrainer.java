package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CrfFeatureSet;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.FeatureReader;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.ReasonNoMedsFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PassThroughFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.PhraseBasedSentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

public class ReasonNoMedsTaggerTrainer extends ConceptTaggerTrainer {
  private static final HashSet<String> reasonNoMedSubcategories =
      new HashSet<>(Arrays.asList(new String[] {"bp", "br", "compliance", "drug", "ex", "fluid",
          "hb", "hf", "hr", "npo", "rhythm", "vestibular", "tolerance", "vision"}));


  /**
   * @param tokenProcessingOptions
   * @param conceptSet
   */
  public ReasonNoMedsTaggerTrainer(TokenProcessingOptions tokenProcessingOptions,
      String dictionaryPath, HashSet<String> conceptSet) {
    super(tokenProcessingOptions);
    this.textAnalyzer.createDictionary(dictionaryPath, conceptSet);
  }


  /**
   * Takes a text file, an xml file with annotated concepts from eHOST or Knowtator, and a set of
   * concepts and returns the sentences in the document and the annotations in the xml (if the
   * concept of the annotation is found in the set of concepts supplied as a parameter.
   *
   * @param textFile
   * @param conceptSet
   * @param conceptsXML
   * @return
   */
  @Override
  public RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTestingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet) {
    /*
     * Process the document being analyzed to get the document tokens, which are needed for labeling
     * the tokens associated with the phrases
     */
    RaptatDocument doc = this.textAnalyzer.processDocument(textFile.getAbsolutePath());

    tagDictionaryAnnotationTokens(doc.getDictionaryAnnotations(false));

    /*
     * Get the phrases that have been annotated with a concept in the concept set
     */
    List<AnnotatedPhrase> annotatedPhrases = this.annotationImporter
        .importAnnotations(conceptsXML.getAbsolutePath(), textFile.getAbsolutePath(), conceptSet);
    this.theIntegrator.addProcessedTokensToAnnotations(annotatedPhrases, doc, false, 0);

    /*
     * We will use only those sentences that contain one of the phrases created by the dictionary
     * that refers to a beta-blocker or contains an . So this will select all sentences with a
     * phrase in the loaded feature-blocker dictionary that are not blocked or that are not
     * facilitated when required
     */
    List<AnnotatedPhrase> dictionaryAnnotations = doc.getDictionaryAnnotations(false);
    Set<AnnotatedPhrase> phrasesForFiltering =
        new HashSet<>(dictionaryAnnotations.size() + annotatedPhrases.size());

    List<AnnotatedPhrase> bbAnnotations = getBetaBlockerAnnotations(dictionaryAnnotations);
    phrasesForFiltering.addAll(bbAnnotations);
    List<AnnotatedPhrase> bbSentences = filterSentencesByPhrase(phrasesForFiltering);
    HashSet<AnnotatedPhrase> bbSet = new HashSet<>(bbSentences);

    phrasesForFiltering.clear();
    List<AnnotatedPhrase> rnmDictionaryAnnotations =
        getRnmDictionaryAnnotations(dictionaryAnnotations);
    phrasesForFiltering.addAll(rnmDictionaryAnnotations);
    List<AnnotatedPhrase> rnmSentences = filterSentencesByPhrase(phrasesForFiltering);
    HashSet<AnnotatedPhrase> rnmSet = new HashSet<>(rnmSentences);

    bbSet.retainAll(rnmSet);
    ArrayList<AnnotatedPhrase> testingSentences = new ArrayList<>(bbSet);

    List<AnnotatedPhrase> overtRNMAnnotations = getOvertRnmAnnotations(annotatedPhrases);

    return new RaptatPair<>(testingSentences, overtRNMAnnotations);
  }


  /**
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

    tagDictionaryAnnotationTokens(doc.getDictionaryAnnotations(false));

    /*
     * Get the phrases that have been annotated with some concept
     */
    List<AnnotatedPhrase> annotatedPhrases = this.annotationImporter
        .importAnnotations(conceptsXML.getAbsolutePath(), textFile.getAbsolutePath(), conceptSet);
    this.theIntegrator.addProcessedTokensToAnnotations(annotatedPhrases, doc, false, 0);

    /*
     * We will use only those sentences that contain one of the phrases created by the dictionary
     * within the textAnalyzer variable or that have one of the imported phrases. So this will
     * select all sentences with a phrase in the loaded feature-blocker dictionary that are not
     * blocked or that are not facilitated when required
     */
    // List<AnnotatedPhrase> dictionaryAnnotations =
    // doc.getDictionaryAnnotations();
    Set<AnnotatedPhrase> phrasesForFiltering = new HashSet<>(annotatedPhrases.size());

    /*
     * Modified and commented out the statement below by Glenn on 6-17-16 so that only sentence with
     * annotated phrases are used for training
     */
    // phrasesForFiltering.addAll( dictionaryAnnotations );

    /*
     * Modified and added the two statements below by Glenn on 6-17-16 so that negative beta blocker
     * sentences are included.
     */
    // List<AnnotatedPhrase> negativeBBAnnotations =
    // getNegativeBetaBlockerAnnotations( dictionaryAnnotations );
    // phrasesForFiltering.addAll( negativeBBAnnotations );

    // List<AnnotatedPhrase> bbAnnotations = getBetaBlockerAnnotations(
    // dictionaryAnnotations );
    // phrasesForFiltering.addAll( bbAnnotations );

    List<AnnotatedPhrase> overtRNMAnnotations = getOvertRnmAnnotations(annotatedPhrases);
    phrasesForFiltering.addAll(overtRNMAnnotations);

    // phrasesForFiltering.addAll( annotatedPhrases );

    List<AnnotatedPhrase> trainingSentences = filterSentencesByPhrase(phrasesForFiltering);

    return new RaptatPair<>(trainingSentences, overtRNMAnnotations);
  }


  protected void filterTrainingFileFeatures(String featureFilePath, String includedFeaturesFile) {
    BufferedReader br = null;
    System.out.println("Loading included features");
    HashSet<String> includedFeatures = loadIncludedFeatures(includedFeaturesFile);
    System.out.println("Features loaded");
    try {
      int sentence = 0;
      File featureFile = new File(featureFilePath);
      String fileName = featureFile.getName();
      int prefixLength = fileName.length() - 4;
      String endTag = fileName.substring(prefixLength);
      fileName = fileName.substring(0, prefixLength) + "_filter02_" + GeneralHelper.getTimeStamp()
          + endTag;
      String newFeatureFile = featureFile.getParent() + File.separator + fileName;
      PrintWriter pw = new PrintWriter(new File(newFeatureFile));
      br = new BufferedReader(new FileReader(featureFile));

      if (br != null) {
        String line;

        /* Skip first line which should always be empty */
        br.readLine();
        while ((line = br.readLine()) != null) {
          String[] tokens = line.split("\t");
          if (tokens.length == 0 || tokens[0].isEmpty()) {
            System.out.println("Filtered sentence " + ++sentence);
            pw.println();
            pw.flush();
          } else {
            StringBuilder sb = new StringBuilder(tokens[0]);
            for (int i = 1; i < tokens.length; i++) {
              if (includedFeatures.contains(tokens[i])) {
                sb.append("\t").append(tokens[i]);
              }
            }
            pw.println(sb.toString());
            pw.flush();
          }
        }
      }

      if (pw != null) {
        pw.println();
        pw.close();
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  protected void filterTrainingFileFeaturesByLabel(String featureFilePath,
      String includedFeaturesFile) {
    BufferedReader br = null;
    System.out.println("Loading included features");
    HashMap<String, HashSet<String>> includedFeaturesMap =
        loadIncludedFeaturesByLabel(includedFeaturesFile);
    System.out.println("Features loaded");
    try {
      int sentence = 0;
      File featureFile = new File(featureFilePath);
      String fileName = featureFile.getName();
      int prefixLength = fileName.length() - 4;
      String endTag = fileName.substring(prefixLength);
      fileName = fileName.substring(0, prefixLength) + "_Filtered_" + GeneralHelper.getTimeStamp()
          + endTag;
      String newFeatureFile = featureFile.getParent() + File.separator + fileName;
      PrintWriter pw = new PrintWriter(new File(newFeatureFile));
      br = new BufferedReader(new FileReader(featureFile));

      if (br != null) {
        String line;

        /* Skip first line which should always be empty */
        br.readLine();
        while ((line = br.readLine()) != null) {
          String[] tokens = line.split("\t");
          if (tokens.length == 0 || tokens[0].isEmpty()) {
            if (++sentence % 1000 == 0) {
              System.out.println("Filtered sentence " + sentence);
            }
            pw.println();
            pw.flush();
          } else {
            StringBuilder sb = new StringBuilder(tokens[0]);
            HashSet<String> featuresToInclude = includedFeaturesMap.get(tokens[0]);
            for (int i = 1; i < tokens.length; i++) {
              if (featuresToInclude.contains(tokens[i])) {
                sb.append("\t").append(tokens[i]);
              }
            }
            pw.println(sb.toString());
            pw.flush();
          }
        }
      }

      if (pw != null) {
        pw.println();
        pw.close();
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  /**
   * Helper method for getTrainingSentencesAndPhrases that marks the tokens of phrases so we can
   * later determine the labels to use for feature building as part of CRF model training
   *
   * @param phrasesToMark
   */
  @Override
  @SuppressWarnings("unused")
  protected void markPhraseTokens(List<AnnotatedPhrase> phrasesToMark) {
    for (AnnotatedPhrase annotatedPhrase : phrasesToMark) {
      String conceptName = null;

      for (RaptatAttribute curAttribute : annotatedPhrase.getPhraseAttributes()) {
        if (curAttribute.getName().equalsIgnoreCase("ReasonType")
            && curAttribute.getValues().get(0).equalsIgnoreCase("overt")) {
          conceptName = annotatedPhrase.getConceptName() + RaptatConstants.CONCEPT_CONNECTOR
              + curAttribute.getValues().get(0);
          break;
        }
      }

      /*
       * Skip phrases where the concept does not have an assigned reasonType attribute
       */
      if (conceptName != null) {
        List<RaptatToken> phraseTokens = annotatedPhrase.getProcessedTokens();
        int i = 0;
        for (RaptatToken curToken : phraseTokens) {
          RaptatToken parentToken;
          if ((parentToken = curToken.getParentToken()) == null) {
            parentToken = curToken;
          }

          if (i == 0) {
            parentToken.setTokenStringTagged(RaptatConstants.BEGIN_CONCEPT_LABEL
                + RaptatConstants.CONCEPT_CONNECTOR + conceptName);
          } else {
            parentToken.setTokenStringTagged(RaptatConstants.INSIDE_CONCEPT_LABEL
                + RaptatConstants.CONCEPT_CONNECTOR + conceptName);
          }
          i++;
        }
      }
    }
  }


  protected void printTrainingCrossValidationFeatures(String pathToCrossValidationFile,
      String outputDirectoryPath, int[] wordConjunctionSettings, int[] posConjunctionSettings,
      int[] wordConceptConjunctionSettings, String includedFeaturesFilePath,
      HashSet<String> conceptSet, boolean isForTraining) {
    /* Create directory for storing cross-validation feature files */
    String trainTestTag = isForTraining ? "Training_" : "Testing_";
    String outputDirectoryName = outputDirectoryPath + File.separator + "CVFeatures_Filtered_"
        + trainTestTag + "OvertRnmSelectedSent_" + GeneralHelper.getTimeStamp();
    try {
      FileUtils.forceMkdir(new File(outputDirectoryName));
    } catch (IOException e) {
      e.printStackTrace();
    }

    ReasonNoMedsFeatureBuilder featureBuilder =
        new ReasonNoMedsFeatureBuilder(wordConjunctionSettings, posConjunctionSettings,
            wordConceptConjunctionSettings, includedFeaturesFilePath);

    HashMap<String, List<CrfFeatureSet>> cvFeatureSets =
        getCVGroupFeatureSets(pathToCrossValidationFile, featureBuilder, conceptSet, isForTraining);

    printCVFeatureSets(cvFeatureSets, outputDirectoryName);
  }


  protected void writeTrainingFeatures(int[] wordConjunctionSettings, int[] posConjunctionSettings,
      int[] wordConceptConjunctionSettings, String includedFeaturesFilePath, String fileNameTag,
      RaptatPair<List<File>, List<File>> trainingLists, String trainingFilePath,
      HashSet<String> conceptSet) {
    ReasonNoMedsFeatureBuilder featureBuilder =
        new ReasonNoMedsFeatureBuilder(wordConjunctionSettings, posConjunctionSettings,
            wordConceptConjunctionSettings, includedFeaturesFilePath);

    try {
      String trainingModelName =
          "ReasonNoMedsModelTraining_FeatureRd00_" + GeneralHelper.getTimeStamp() + ".txt";
      File modelFile = new File(trainingFilePath + File.separator + trainingModelName);
      PrintWriter pw = new PrintWriter(modelFile);
      writeTrainingFeaturesUsingTrainingFiles(featureBuilder, trainingLists.left,
          trainingLists.right, conceptSet, pw);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  private List<AnnotatedPhrase> filterSentencesByPhrase(Set<AnnotatedPhrase> phrasesForFiltering) {
    PhraseBasedSentenceFilter sentenceFilter =
        new PhraseBasedSentenceFilter(new PassThroughFilter());
    Set<AnnotatedPhrase> trainingSentenceSet = sentenceFilter.selectSentences(phrasesForFiltering);

    return new ArrayList<>(trainingSentenceSet);
  }


  private List<AnnotatedPhrase> getBetaBlockerAnnotations(
      List<AnnotatedPhrase> dictionaryAnnotations) {
    List<AnnotatedPhrase> resultList = new ArrayList<>();

    for (AnnotatedPhrase annotation : dictionaryAnnotations) {
      if (annotation.getConceptName().equalsIgnoreCase("betablocker")) {
        resultList.add(annotation);
      }
    }
    return resultList;
  }


  private HashMap<String, List<CrfFeatureSet>> getCVGroupFeatureSets(
      String pathToCrossValidationFile, ReasonNoMedsFeatureBuilder featureBuilder,
      HashSet<String> conceptSet, boolean isForTraining) {
    HashMap<String, List<RaptatPair<File, File>>> cvGroups =
        GeneralHelper.getCVGroupFiles(pathToCrossValidationFile);
    HashMap<String, List<CrfFeatureSet>> resultMap = new HashMap<>(cvGroups.size());
    Set<String> cvGroupKeys = cvGroups.keySet();
    for (String cvGroup : cvGroupKeys) {
      List<RaptatPair<File, File>> textXmlList = cvGroups.get(cvGroup);
      List<CrfFeatureSet> featureList = new ArrayList<>(textXmlList.size());
      resultMap.put(cvGroup, featureList);

      for (RaptatPair<File, File> curPair : textXmlList) {
        CrfFeatureSet features = getDocumentFeatures(featureBuilder, conceptSet, curPair.left,
            curPair.right, isForTraining);
        featureList.add(features);
      }
    }

    return resultMap;
  }


  private List<AnnotatedPhrase> getNegativeBetaBlockerAnnotations(
      List<AnnotatedPhrase> dictionaryAnnotations) {
    List<AnnotatedPhrase> resultList = new ArrayList<>();

    annotationTestLoop: for (AnnotatedPhrase annotation : dictionaryAnnotations) {
      if (annotation.getConceptName().equalsIgnoreCase("betablocker")) {
        for (RaptatToken curToken : annotation.getProcessedTokens()) {
          RaptatToken parentToken;
          if ((parentToken = curToken.getParentToken()) == null) {
            parentToken = curToken;
          }
          String negationStatus = parentToken.getContextValue(ContextType.NEGATION);
          if (negationStatus.equalsIgnoreCase("negative")) {
            resultList.add(annotation);
            continue annotationTestLoop;
          }
        }
      }
    }
    return resultList;
  }


  private List<AnnotatedPhrase> getOvertRnmAnnotations(List<AnnotatedPhrase> annotatedPhrases) {
    List<AnnotatedPhrase> resultList = new ArrayList<>();

    annotationTestLoop: for (AnnotatedPhrase annotatedPhrase : annotatedPhrases) {
      List<RaptatAttribute> attributes = annotatedPhrase.getPhraseAttributes();
      for (RaptatAttribute attribute : attributes) {
        if (attribute.getName().equalsIgnoreCase("reasontype")) {
          for (String curValue : attribute.getValues()) {
            if (curValue.equalsIgnoreCase("overt")) {
              resultList.add(annotatedPhrase);
              continue annotationTestLoop;
            }
          }
        }
      }
    }
    return resultList;
  }


  private List<AnnotatedPhrase> getRnmDictionaryAnnotations(
      List<AnnotatedPhrase> dictionaryAnnotations) {
    List<AnnotatedPhrase> resultList = new ArrayList<>(dictionaryAnnotations.size());

    for (AnnotatedPhrase curAnnotation : dictionaryAnnotations) {
      String concept = curAnnotation.getConceptName();
      if (ReasonNoMedsTaggerTrainer.reasonNoMedSubcategories.contains(concept.toLowerCase())) {
        resultList.add(curAnnotation);
      }
    }
    return resultList;
  }


  private HashSet<String> loadIncludedFeatures(String includedFeaturesFile) {
    BufferedReader br = null;
    HashSet<String> resultSet = new HashSet<>(1000000);
    try {
      br = new BufferedReader(new FileReader(new File(includedFeaturesFile)));
      String line = null;
      while ((line = br.readLine()) != null) {
        resultSet.add(line);
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return resultSet;
  }


  private HashMap<String, HashSet<String>> loadIncludedFeaturesByLabel(
      String includedFeaturesFile) {
    BufferedReader br = null;
    HashMap<String, HashSet<String>> resultMap = new HashMap<>();
    try {
      br = new BufferedReader(new FileReader(new File(includedFeaturesFile)));
      String line = null;
      HashSet<String> mappedSet;
      while ((line = br.readLine()) != null) {
        String[] elements = line.split("\t");

        if ((mappedSet = resultMap.get(elements[0])) == null) {
          mappedSet = new HashSet<>(1000000);
          resultMap.put(elements[0], mappedSet);
        }
        mappedSet.add(elements[1]);
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return resultMap;
  }


  private void printCVFeatureSets(HashMap<String, List<CrfFeatureSet>> cvFeatureSets,
      String outputDirectoryPath) {
    for (String cvGroup : cvFeatureSets.keySet()) {
      ConceptTaggerTrainer.LOGGER.info("Printing features for CV group:" + cvGroup);
      try {
        String groupDirectoryName = "CVGroup_" + cvGroup;
        String groupDirectoryPath = outputDirectoryPath + File.separator + groupDirectoryName;
        FileUtils.forceMkdir(new File(groupDirectoryPath));
        String trainingFilePath =
            groupDirectoryPath + File.separator + "CVGroup_TrainingFile_" + cvGroup + ".txt";
        PrintWriter pwTraining = new PrintWriter(trainingFilePath);
        String testingFilePath =
            groupDirectoryPath + File.separator + "CVGroup_TestingFile_" + cvGroup + ".txt";
        PrintWriter pwTesting = new PrintWriter(testingFilePath);
        for (String featureGroup : cvFeatureSets.keySet()) {
          List<CrfFeatureSet> featureList = cvFeatureSets.get(featureGroup);
          PrintWriter featurePW;
          if (featureGroup.equals(cvGroup)) {
            featurePW = pwTesting;
          } else {
            featurePW = pwTraining;
          }

          for (CrfFeatureSet curFeatures : featureList) {
            printFeaturesAndLabels(curFeatures.trainingFeatures, curFeatures.trainingLabels,
                featurePW);
          }
        }

        pwTraining.close();
        pwTesting.close();
      } catch (FileNotFoundException e) {
        GeneralHelper.errorWriter("Unable to create printer to print cross-validation features");
        e.printStackTrace();
      } catch (IOException e) {
        GeneralHelper.errorWriter("Unable to create directory to print cross-validation features");
        e.printStackTrace();
      }
    }
  }


  /**
   * Examines a set of dictionary annotations and assigns "concepts" to the tokens based upon the
   * concept of the annotation. This is used to in later code to build features.
   *
   * @param dictionaryAnnotations
   */
  private void tagDictionaryAnnotationTokens(List<AnnotatedPhrase> dictionaryAnnotations) {
    for (AnnotatedPhrase annotatedPhrase : dictionaryAnnotations) {
      String concept = annotatedPhrase.getConceptName();
      for (RaptatToken token : annotatedPhrase.getProcessedTokens()) {
        RaptatToken taggedToken;
        if ((taggedToken = token.getParentToken()) == null) {
          taggedToken = token;
        }
        taggedToken.getAssignedConcepts().add(concept);
      }
    }
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      private List<File> trainingTextFiles = new ArrayList<>(512);
      private List<File> trainingConceptsXML = new ArrayList<>(512);

      private final String trainingFilePath =
          "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis";


      @Override
      public void run() {
        String textFilePath =
            "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainTextFiles";
        String conceptRefFilePath =
            "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainXMLFiles";
        String dictionaryPath =
            "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\ConceptFeatureDictionary_160512.txt";

        int wordConjunctionOffset = 4;
        int wordNgramSize = 4;
        int posConjunctionOffset = 4;
        int posNgramSize = 4;

        String fileNameTag = "Rd01_TaggerSolution_v01";

        TokenProcessingOptions tokenProcessingOptions =
            new TokenProcessingOptions(OptionsManager.getInstance());
        HashSet<String> conceptSet = new HashSet<>();

        /* Add to concept set to train using only specific concepts */
        conceptSet.add("reasonnomeds");

        createTrainingDocumentLists(textFilePath, conceptRefFilePath);

        ReasonNoMedsTaggerTrainer trainer =
            new ReasonNoMedsTaggerTrainer(tokenProcessingOptions, dictionaryPath, conceptSet);
        RaptatPair<List<File>, List<File>> trainingLists =
            new RaptatPair<>(this.trainingTextFiles, this.trainingConceptsXML);

        int[] wordConjunctionArray, posConjunctionArray, wordConceptArray;

        RunType runType = RunType.TRAIN_WITH_EXISTING_FEATURES;
        switch (runType) {
          case TRAIN:
            break;

          case TRAIN_WITH_EXISTING_FEATURES: {
            /*
             * This is where the features to be used in the model are found - after running this,
             * they will be stored in the feature builder stored with the solution.
             */
            String includedFeaturesFilePath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\"
                + "Analysis\\CVFeatures_Unfiltered_Testing_ForCommonParameterDetermination\\"
                + "ParameterStats\\CommonlyFoundFeatures_160623_211850.txt";

            /*
             * This is where the features used for training are stored
             */
            String featureFolderName =
                "CVFeatures_Filtered_Training_OvertRnmSelectedSent_160627_154351";

            /*
             * These lines create path to a set of solutions, typically used for cross validation
             * where the CV_Features part of the feature folder name is just replaced with
             * CV10FoldSolution
             */
            String solutionFolderName = featureFolderName.replace("CVFeatures", "CV10FoldSolution");
            String solutionDirectoryPath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\" + solutionFolderName
                    + "_" + GeneralHelper.getTimeStamp();

            for (int groupNumber = 1; groupNumber <= 10; groupNumber++) {
              wordConjunctionArray = new int[] {wordConjunctionOffset, wordNgramSize};
              posConjunctionArray = new int[] {posConjunctionOffset, posNgramSize};
              wordConceptArray = new int[] {7, 2};

              /*
               * This was added as a kludge to deal with when features sets already exist but we
               * need to save the feature builder instance with the trained solution
               */
              ReasonNoMedsFeatureBuilder featureBuilder =
                  new ReasonNoMedsFeatureBuilder(wordConjunctionArray, posConjunctionArray,
                      wordConceptArray, includedFeaturesFilePath);

              /*
               * This is the path to the actual file that contains the features to be used for
               * training
               */
              String pathToFeatureFile = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\"
                  + featureFolderName + "\\CVGroup_" + groupNumber + "\\CVGroup_TrainingFile_"
                  + groupNumber + ".txt";
              CrfFeatureSet featureSet = getTrainingFeatureSet(pathToFeatureFile);

              /*
               * Create individual folders for solutions for the various cross-validation groups
               */
              String fileTag = "Group_" + groupNumber;
              String groupDirectoryPath = solutionDirectoryPath + "\\" + fileTag;
              try {
                FileUtils.forceMkdir(new File(groupDirectoryPath));
              } catch (IOException e) {
                e.printStackTrace();
              }

              trainer.train(featureSet, featureBuilder, conceptSet, fileTag, groupDirectoryPath);
            }
          }
            break;

          case PRINTFEATURES: {
            String includedFeaturesFilePath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\"
                + "Analysis\\CVFeatures_OvertRNM160622_154105\\AllGroupsSingleTestingFeatures\\"
                + "CommonParameters\\CommonlyFoundFeatures_160622_174139.txt";
            wordConjunctionArray = new int[] {wordConjunctionOffset, wordNgramSize};
            posConjunctionArray = new int[] {posConjunctionOffset, posNgramSize};
            wordConceptArray = new int[] {7, 2};
            trainer.writeTrainingFeatures(wordConjunctionArray, posConjunctionArray,
                wordConceptArray, includedFeaturesFilePath, fileNameTag, trainingLists,
                this.trainingFilePath, conceptSet);
          }
            break;

          /*
           * This case prints out the features based on a set of cross-validation groups provided in
           * a csv file. The features set are then used to train several models in which the model
           * we be built on one data set and then tested on a separate data set. The csv file should
           * contain one column with the group description or number, the second column should
           * contain
           */
          case PRINT_CROSSVALIDATION_FEATURES: {
            wordConjunctionArray = new int[] {wordConjunctionOffset, wordNgramSize};
            posConjunctionArray = new int[] {posConjunctionOffset, posNgramSize};
            wordConceptArray = new int[] {7, 2};
            String pathToCrossValidationFile =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CrossValidGroups_10Fold_ForTraining_160613.csv";
            String outputDirectoryPath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis";
            String includedFeaturesFilePath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\"
                + "Analysis\\CVFeatures_Unfiltered_Testing_ForCommonParameterDetermination\\"
                + "ParameterStats\\CommonlyFoundFeatures_160623_211850.txt";
            boolean isForTraining = true;
            trainer.printTrainingCrossValidationFeatures(pathToCrossValidationFile,
                outputDirectoryPath, wordConjunctionArray, posConjunctionArray, wordConceptArray,
                includedFeaturesFilePath, conceptSet, isForTraining);
          }
            break;

          case FILTERFEATURES: {
            String includedFeaturesFilePath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\"
                + "Analysis\\CVFeatures_Unfiltered_Training_DictRNM_AllBB160623_174959\\"
                + "AllGroupsSingleTestingFeatures\\CommonFeatures\\CommonFeatures_160623_185126.txt";
            String curFeatureFilePath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\ReasonNoMedsModelTraining_160516_151308.txt";
            trainer.filterTrainingFileFeatures(curFeatureFilePath, includedFeaturesFilePath);
            break;
          }

          case FILTERFEATURES_BYLABEL: {
            String includedFeaturesFilePath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\"
                    + "CVFeatures_Unfiltered_Testing_ForCommonParameterDetermination\\ParameterStats\\CommonlyFoundFeatures_160623_211850.txt";
            for (int cvGroup = 1; cvGroup <= 10; cvGroup++) {
              String curFeatureFilePath =
                  "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CVFeatures_Unfiltered_Testing_RmnOrBbSent_160623_223807\\CVGroup_"
                      + cvGroup + "\\CVGroup_TestingFile_" + cvGroup + ".txt";
              trainer.filterTrainingFileFeaturesByLabel(curFeatureFilePath,
                  includedFeaturesFilePath);
            }
          }
            break;

          case CREATE_COMMON_FEATURES_FILE: {
            String featureFilesPath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\"
                + "Analysis\\CVFeatures_Unfiltered_Testing_ForCommonParameterDetermination\\ParameterStats";
            findCommonFeaturesByLabel(featureFilesPath);
          }
        }

        System.exit(0);
      }


      protected void createFeatureFileByLabel(String featureFilePathOld,
          String featureFilePathNew) {
        final int featureElement = 0;
        final int labelElement = 1;
        final int significanceElement = 7;

        try {
          BufferedReader br = new BufferedReader(new FileReader(featureFilePathOld));
          BufferedWriter bw = new BufferedWriter(new FileWriter(new File(featureFilePathNew)));

          String readLine = null;
          long linesRead = 0;
          while ((readLine = br.readLine()) != null) {
            linesRead++;
            if (linesRead % 10000 == 0) {
              System.out.println("Lines read:" + linesRead);
            }
            String[] elements = readLine.split("\t");

            if (Double.valueOf(elements[significanceElement]) > 0.0) {
              StringBuilder sb = new StringBuilder(elements[labelElement]);
              sb.append("\t").append(elements[featureElement]);
              bw.write(sb.toString() + "\n");
              bw.flush();
            }
          }
          br.close();
          bw.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }


      /**
       * Creates list of text and xml files
       *
       * @param conceptRefFilePath
       * @param textFilePath
       */
      protected void createTrainingDocumentLists(String textFilePath, String conceptRefFilePath) {
        File[] fileList = new File(textFilePath).listFiles();
        this.trainingTextFiles.addAll(Arrays.asList(fileList));
        fileList = new File(conceptRefFilePath).listFiles();
        this.trainingConceptsXML.addAll(Arrays.asList(fileList));
      }


      protected void findCommonFeaturesByLabel(String featureFileDirectory) {
        Collection<File> parameterFiles = FileUtils.listFiles(new File(featureFileDirectory),
            new RegexFileFilter(".*.txt"), TrueFileFilter.INSTANCE);

        HashMap<String, HashSet<String>> parameterMap = new HashMap<>();
        HashMap<String, HashSet<String>> foundMap = new HashMap<>();

        /* Find features that are significant in at least 2 files */
        for (File file : parameterFiles) {
          System.out.println("Processing file:" + file.getName());
          updateParameterMap(file, parameterMap, foundMap);
        }

        try {
          String newFeatureFilePath = featureFileDirectory + File.separator
              + "CommonlyFoundFeatures_" + GeneralHelper.getTimeStamp() + ".txt";
          System.out.println("Writing features to file:" + newFeatureFilePath);

          BufferedWriter bw = new BufferedWriter(new FileWriter(new File(newFeatureFilePath)));

          for (String curLabel : parameterMap.keySet()) {
            HashSet<String> mappedFeatures = parameterMap.get(curLabel);
            for (String curFeature : mappedFeatures) {
              StringBuilder sb = new StringBuilder(curLabel);
              sb.append("\t").append(curFeature);
              bw.write(sb.toString() + "\n");
              bw.flush();
            }
          }
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }


      private CrfFeatureSet getTrainingFeatureSet(String pathToFeatureFile) {
        CrfFeatureSet trainingSet = null;
        FeatureReader fr = new FeatureReader(pathToFeatureFile);

        trainingSet = fr.readFeatures();

        return trainingSet;
      }


      /**
       * Takes a file containing labels, features, and significance of the features and puts those
       * features that were significant in previous files (in association with a label) into a map
       * from label to features.
       *
       * @param featureFile
       * @param selectedFeatureMap - tracks features that have been found as significant 2 or more
       *        times in association with a given label.
       * @param foundFeatureMap - tracks significant features that have been previously found
       *        associated with a given label.
       */
      private void updateParameterMap(File featureFile,
          HashMap<String, HashSet<String>> selectedFeatureMap,
          HashMap<String, HashSet<String>> foundFeatureMap) {
        final int featureElement = 0;
        final int labelElement = 1;
        final int significanceElement = 7;

        try {
          BufferedReader br = new BufferedReader(new FileReader(featureFile));
          String readLine = null;

          while ((readLine = br.readLine()) != null) {
            String[] elements = readLine.split("\t");
            if (Double.valueOf(elements[significanceElement]) > 0.0) {
              HashSet<String> foundParameters;
              if ((foundParameters = foundFeatureMap.get(elements[labelElement])) != null) {
                /*
                 * If the mapped label exists but the feature hasn't been seen before, just add it
                 * to found parameters
                 */
                if (!foundParameters.contains(elements[featureElement])) {
                  foundParameters.add(elements[featureElement]);
                }
                /*
                 * If the mapped label exists and the feature has been seen before, add it to
                 * selected parameters.
                 */
                else {
                  HashSet<String> selectedParameters;
                  if ((selectedParameters =
                      selectedFeatureMap.get(elements[labelElement])) == null) {
                    selectedParameters = new HashSet<>(2000);
                    selectedFeatureMap.put(elements[labelElement], selectedParameters);
                  }
                  selectedParameters.add(elements[featureElement]);
                }
              }
              /*
               * If the label has never been mapped, the the feature must never have been found, so
               * we add the mapping and the feature to foundFeatureMap
               */
              else {
                foundParameters = new HashSet<>(2000);
                foundFeatureMap.put(elements[labelElement], foundParameters);
                foundParameters.add(elements[featureElement]);
              }
            }
          }
          br.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
