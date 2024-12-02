package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class GarvinCreateVersion2 {
  private enum ElementIndex {
    BATCH, TEXT_FILENAME, TEXT_FILEPATH, CLASS, START_OFFSET, END_OFFSET, TEXT, ATTRIBUTE1, ATTRIBUTE1_VALUE, ATTRIBUTE2, ATTRIBUTE2_VALUE
  };

  // @formatter:off
  private enum ReportIndex {
    DOCUMENT,
    SENTENCE,
    OVERT_RNM,
    NONOVERT_RNM,
    BETABLOCKER_POSITIVE,
    BETABLOCKER_NEGATIVE,
    CAUSALCUE,
    BP,
    BR,
    COMPLIANCE,
    DRUG,
    EX,
    FLUID,
    HB,
    HF,
    HR,
    NPO,
    RHYTHM,
    VESTIBULAR,
    VISION,
    BETABLOCKER_POSITIVE_AND_OVERT_RNM,
    BETABLOCKER_NEGATIVE_AND_OVERT_RNM,
    CAUSALCUE_AND_OVERT_RNM,
    BP_AND_OVERT_RNM,
    BR_AND_OVERT_RNM,
    COMPLIANCE_AND_OVERT_RNM,
    DRUG_AND_OVERT_RNM,
    EX_AND_OVERT_RNM,
    FLUID_AND_OVERT_RNM,
    HB_AND_OVERT_RNM,
    HF_AND_OVERT_RNM,
    HR_AND_OVERT_RNM,
    NPO_AND_OVERT_RNM,
    RHYTHM_AND_OVERT_RNM,
    VESTIBULAR_AND_OVERT_RNM,
    VISION_AND_OVERT_RNM,
    BETABLOCKER_POSITIVE_AND_NONOVERT_RNM,
    BETABLOCKER_NEGATIVE_AND_NONOVERT_RNM,
    CAUSALCUE_AND_NONOVERT_RNM,
    BP_AND_NONOVERT_RNM,
    BR_AND_NONOVERT_RNM,
    COMPLIANCE_AND_NONOVERT_RNM,
    DRUG_AND_NONOVERT_RNM,
    EX_AND_NONOVERT_RNM,
    FLUID_AND_NONOVERT_RNM,
    HB_AND_NONOVERT_RNM,
    HF_AND_NONOVERT_RNM,
    HR_AND_NONOVERT_RNM,
    NPO_AND_NONOVERT_RNM,
    RHYTHM_AND_NONOVERT_RNM,
    VESTIBULAR_AND_NONOVERT_RNM,
    VISION_AND_NONOVERT_RNM
  };

  // @formatter:off
  private enum ReportIndexFine {
    DOCUMENT,
    SENTENCE,
    NONOVERT_RNM,
    OVERT_RNM,
    BETABLOCKER_POSITIVE,
    BETABLOCKER_NEGATIVE,
    CAUSALCUE,
    BP,
    BR,
    COMPLIANCE,
    DRUG,
    EX,
    FLUID,
    HB,
    HF,
    HR,
    NPO,
    RHYTHM,
    TOLERANCE,
    VESTIBULAR,
    VISION
  };
  // @formatter:on

  private enum RunType {
    CONVERT_TAB_TO_XML, PRINT_FEATURES, STATISTICAL_ANALYSIS, STATISTICAL_ANALYSIS_FINE,;
  };


  // @formatter:on

  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      private String dataFilePath =
          "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\RNMTrainingCleaned_160510.txt";
      private String textFileDirectoryPath =
          "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainTextFiles";


      @Override
      public void run() {
        RunType runType = RunType.STATISTICAL_ANALYSIS_FINE;

        switch (runType) {
          case CONVERT_TAB_TO_XML:
            HashMap<String, List<AnnotatedPhrase>> textPathToAnnotationMap =
                convertAnnotations(this.dataFilePath);
            exportAnnotations(textPathToAnnotationMap, this.textFileDirectoryPath);
            break;
          case PRINT_FEATURES:
            printTrainingFeatures();
            break;

          case STATISTICAL_ANALYSIS: {
            String textFilePath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainTextFiles";
            String xmlFilePath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainXMLFiles";
            String dictionaryPath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\ConceptFeatureDictionary_160512.txt";
            List<RaptatPair<File, File>> trainingFiles =
                createTrainingDocumentLists(textFilePath, xmlFilePath);

            boolean analysisFine = false;
            HashMap<String, List<RaptatPair<Integer, Integer>>> documentStatistics =
                generateAnnotationStatistics(trainingFiles, dictionaryPath, analysisFine);
            printAnalysis(documentStatistics, new File(textFilePath).getParent(), analysisFine);
          }
            break;
          case STATISTICAL_ANALYSIS_FINE: {
            String textFilePath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainTextFiles";
            String xmlFilePath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\TrainXMLFiles";
            String dictionaryPath =
                "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\ConceptFeatureDictionary_160512.txt";
            List<RaptatPair<File, File>> trainingFiles =
                createTrainingDocumentLists(textFilePath, xmlFilePath);

            boolean analysisFine = true;
            HashMap<String, List<RaptatPair<Integer, Integer>>> documentStatistics =
                generateAnnotationStatistics(trainingFiles, dictionaryPath, analysisFine);
            printAnalysis(documentStatistics, new File(textFilePath).getParent(), analysisFine);
          }
            break;
        }
      }


      private List<RaptatPair<Integer, Integer>> calculateSentenceAssociation(RaptatDocument doc,
          List<AnnotatedPhrase> referenceAnnotations) {
        HashMap<AnnotatedPhrase, RaptatPair<HashSet<String>, HashSet<String>>> associations =
            new HashMap<>();

        /*
         * First find the sentences associated with the dictionary annotations
         */
        for (AnnotatedPhrase annotation : doc.getDictionaryAnnotations(false)) {
          String conceptName = annotation.getConceptName().toUpperCase();
          if (annotation.getConceptName().equalsIgnoreCase("betablocker")) {
            String negationStatus = "positive";
            for (RaptatToken curToken : annotation.getProcessedTokens()) {
              RaptatToken parentToken;
              if ((parentToken = curToken.getParentToken()) == null) {
                parentToken = curToken;
              }
              String contextValue = parentToken.getContextValue(ContextType.NEGATION);
              if (!contextValue.equals(negationStatus)) {
                negationStatus = contextValue;
                break;
              }
            }
            conceptName += "_" + negationStatus.toUpperCase();
          }

          AnnotatedPhrase conceptSentence =
              annotation.getProcessedTokens().get(0).getSentenceOfOrigin();

          RaptatPair<HashSet<String>, HashSet<String>> associatedConcepts =
              associations.get(conceptSentence);

          if (associatedConcepts == null) {
            HashSet<String> dictionaryConcepts = new HashSet<>();
            HashSet<String> annotatedConcepts = new HashSet<>();
            associatedConcepts = new RaptatPair<>(annotatedConcepts, dictionaryConcepts);
            associations.put(conceptSentence, associatedConcepts);
          }

          associatedConcepts.right.add(conceptName);
        }

        /*
         * Now find the sentences associated with the reference annotations
         */
        for (AnnotatedPhrase annotation : referenceAnnotations) {
          String conceptName = "RNM";
          String conceptTag = "NONOVERT";
          for (RaptatAttribute curAttribute : annotation.getPhraseAttributes()) {
            if (curAttribute.getName().equalsIgnoreCase("ReasonType")) {
              conceptTag = curAttribute.getValues().get(0).toUpperCase();
              break;
            }
          }
          conceptName = conceptTag + "_" + conceptName;
          AnnotatedPhrase conceptSentence =
              annotation.getProcessedTokens().get(0).getSentenceOfOrigin();

          RaptatPair<HashSet<String>, HashSet<String>> associatedConcepts =
              associations.get(conceptSentence);

          if (associatedConcepts == null) {
            HashSet<String> dictionaryConcepts = new HashSet<>();
            HashSet<String> annotatedConcepts = new HashSet<>();
            associatedConcepts = new RaptatPair<>(annotatedConcepts, dictionaryConcepts);
            associations.put(conceptSentence, associatedConcepts);
          }

          associatedConcepts.left.add(conceptName);
        }

        return processAssociations(associations);
      }


      private List<RaptatPair<String, List<RaptatPair<Integer, Integer>>>> calculateSentenceAssociationsFine(
          RaptatDocument doc, List<AnnotatedPhrase> referenceAnnotations) {
        /*
         * Create map between a sentence with dictionary annotations and the concepts of the
         * annotations within such a sentence
         */
        HashMap<AnnotatedPhrase, HashSet<String>> associations = new HashMap<>();

        /*
         * First find the sentences associated with the dictionary annotations
         */
        for (AnnotatedPhrase annotation : doc.getDictionaryAnnotations(false)) {
          String conceptName = annotation.getConceptName().toUpperCase();
          if (annotation.getConceptName().equalsIgnoreCase("betablocker")) {
            String negationStatus = "positive";
            for (RaptatToken curToken : annotation.getProcessedTokens()) {
              RaptatToken parentToken;
              if ((parentToken = curToken.getParentToken()) == null) {
                parentToken = curToken;
              }
              String contextValue = parentToken.getContextValue(ContextType.NEGATION);
              if (!contextValue.equals(negationStatus)) {
                negationStatus = contextValue;
                break;
              }
            }
            conceptName += "_" + negationStatus.toUpperCase();
          }
          AnnotatedPhrase conceptSentence =
              annotation.getProcessedTokens().get(0).getSentenceOfOrigin();

          HashSet<String> associatedConcepts = associations.get(conceptSentence);

          if (associatedConcepts == null) {
            associatedConcepts = new HashSet<>();
            associations.put(conceptSentence, associatedConcepts);
          }

          associatedConcepts.add(conceptName);
        }

        /*
         * Now find the sentences associated with the reference annotations
         */
        for (AnnotatedPhrase annotation : referenceAnnotations) {
          String conceptName = "RNM";
          String conceptTag = "NONOVERT";
          for (RaptatAttribute curAttribute : annotation.getPhraseAttributes()) {
            if (curAttribute.getName().equalsIgnoreCase("ReasonType")) {
              conceptTag = curAttribute.getValues().get(0).toUpperCase();
              break;
            }
          }
          conceptName = conceptTag + "_" + conceptName;
          AnnotatedPhrase conceptSentence =
              annotation.getProcessedTokens().get(0).getSentenceOfOrigin();

          HashSet<String> associatedConcepts = associations.get(conceptSentence);

          if (associatedConcepts == null) {
            associatedConcepts = new HashSet<>();

            associations.put(conceptSentence, associatedConcepts);
          }

          associatedConcepts.add(conceptName);
        }

        return translateToIndicesFine(associations);
      }


      private HashMap<String, List<AnnotatedPhrase>> convertAnnotations(String pathToDataFile) {
        UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;
        TabReader dataReader = new TabReader(new File(pathToDataFile));
        String[] dataElements;
        HashMap<String, List<AnnotatedPhrase>> textToAnnotationMap = new HashMap<>(1024);

        while ((dataElements = dataReader.getNextData()) != null) {
          String pathToTextFile = dataElements[ElementIndex.TEXT_FILEPATH.ordinal()];
          List<AnnotatedPhrase> textFileAnnotations;
          if ((textFileAnnotations = textToAnnotationMap.get(pathToTextFile)) == null) {
            textFileAnnotations = new ArrayList<>(8);
            textToAnnotationMap.put(pathToTextFile, textFileAnnotations);
          }
          AnnotatedPhrase curPhrase = getAnnotation(dataElements, idGenerator);
          textFileAnnotations.add(curPhrase);
        }
        return textToAnnotationMap;
      }


      private void exportAnnotations(HashMap<String, List<AnnotatedPhrase>> textPathToAnnotationMap,
          String pathToTextFiles) {
        File textDirectory = new File(pathToTextFiles);
        File[] trainingFiles = textDirectory.listFiles(getFileFilter(".txt"));
        int i = 0;
        for (File file : trainingFiles) {
          String filePath = file.getAbsolutePath();
          System.out.println(
              "Exporting " + i++ + " of " + trainingFiles.length + " files to XML:" + filePath);
          List<AnnotatedPhrase> fileAnnotations;
          if ((fileAnnotations = textPathToAnnotationMap.get(filePath)) == null) {
            fileAnnotations = new ArrayList<>();
          }
          XMLExporter exporter = new XMLExporter(filePath, true);
          exporter.writeAllDataObjEHostVersion(fileAnnotations, new File(filePath), null, false);
        }
      }


      private HashMap<String, List<RaptatPair<Integer, Integer>>> generateAnnotationStatistics(
          List<RaptatPair<File, File>> trainingFiles, String dictionaryPath, boolean analysisFine) {
        HashMap<String, List<RaptatPair<Integer, Integer>>> documentStatistics = new HashMap<>();
        TokenProcessingOptions options = new TokenProcessingOptions(OptionsManager.getInstance());
        TextAnalyzer ta = new TextAnalyzer(dictionaryPath, null);
        ta.setTokenProcessingParameters(options);
        AnnotationImporter xmlImporter = AnnotationImporter.getImporter();
        PhraseTokenIntegrator tokenIntegrator = new PhraseTokenIntegrator();
        HashSet<String> conceptSet = new HashSet<>();
        conceptSet.add("reasonnomeds");
        int docNumber = 0;
        for (RaptatPair<File, File> curTraining : trainingFiles) {
          System.out.println("Processing document " + ++docNumber + " of " + trainingFiles.size());
          File textFile = curTraining.left;
          File xmlFile = curTraining.right;

          RaptatDocument doc = ta.processDocument(textFile.getAbsolutePath());
          List<AnnotatedPhrase> annotatedPhrases = xmlImporter
              .importAnnotations(xmlFile.getAbsolutePath(), textFile.getAbsolutePath(), conceptSet);
          tokenIntegrator.addProcessedTokensToAnnotations(annotatedPhrases, doc, false, 0);

          if (analysisFine) {
            List<RaptatPair<String, List<RaptatPair<Integer, Integer>>>> indexValues =
                calculateSentenceAssociationsFine(doc, annotatedPhrases);
            for (RaptatPair<String, List<RaptatPair<Integer, Integer>>> sentenceIndexPair : indexValues) {
              String sentencePhrase = sentenceIndexPair.left;
              documentStatistics.put(doc.getTextSource() + "\t" + sentencePhrase,
                  sentenceIndexPair.right);
            }
          } else {

            List<RaptatPair<Integer, Integer>> indexValues =
                calculateSentenceAssociation(doc, annotatedPhrases);
            documentStatistics.put(doc.getTextSource().get(), indexValues);
          }
        }

        return documentStatistics;
      }


      private AnnotatedPhrase getAnnotation(String[] dataElements, UniqueIDGenerator idGenerator) {
        String[] annotationData = new String[6];
        annotationData[0] = idGenerator.getUnique();
        annotationData[1] = dataElements[ElementIndex.START_OFFSET.ordinal()];
        annotationData[2] = dataElements[ElementIndex.END_OFFSET.ordinal()];
        annotationData[3] = dataElements[ElementIndex.TEXT.ordinal()];
        annotationData[4] = "GarvinGroupAdjudication";
        annotationData[5] = dataElements[ElementIndex.CLASS.ordinal()].toLowerCase();

        String curAtttributeID = "RapTAT_Attribute_" + idGenerator.getUnique();
        String curAttributeType = dataElements[ElementIndex.ATTRIBUTE1.ordinal()].toLowerCase();
        String curAttributeValue =
            dataElements[ElementIndex.ATTRIBUTE1_VALUE.ordinal()].toLowerCase();
        RaptatAttribute firstAttribute = new RaptatAttribute(curAtttributeID, curAttributeType,
            curAttributeValue, AttributeAssignmentMethod.REFERENCE);

        curAtttributeID = "RapTAT_Attribute_" + idGenerator.getUnique();
        curAttributeType = dataElements[ElementIndex.ATTRIBUTE2.ordinal()].toLowerCase();
        curAttributeValue = dataElements[ElementIndex.ATTRIBUTE2_VALUE.ordinal()].toLowerCase();
        RaptatAttribute secondAttribute = new RaptatAttribute(curAtttributeID, curAttributeType,
            curAttributeValue, AttributeAssignmentMethod.REFERENCE);

        AnnotatedPhrase annotatedPhrase = new AnnotatedPhrase(annotationData);
        annotatedPhrase.addAttribute(firstAttribute);
        annotatedPhrase.addAttribute(secondAttribute);

        return annotatedPhrase;
      }


      private FilenameFilter getFileFilter(final String nameFilter) {
        FilenameFilter filter = new FilenameFilter() {
          @Override
          public boolean accept(File fileDirectory, String fileName) {
            if (fileName.endsWith(nameFilter)) {
              return true;
            }
            return false;
          }
        };

        return filter;
      }


      private void printAnalysis(
          HashMap<String, List<RaptatPair<Integer, Integer>>> documentStatistics,
          String resultParentDirectoryPath, boolean printFine) {
        String fileTag = printFine ? "Fine_" : "";
        String resultFileName =
            "resultStatistics_" + fileTag + GeneralHelper.getTimeStamp() + ".txt";
        String resultFilePath = resultParentDirectoryPath + File.separator + resultFileName;
        String[] tempResultArray = null;
        if (printFine) {
          tempResultArray = new String[ReportIndexFine.values().length];
          for (ReportIndexFine curHeader : ReportIndexFine.values()) {
            tempResultArray[curHeader.ordinal()] = curHeader.toString().replace("_", " ");
          }
        } else {
          tempResultArray = new String[ReportIndex.values().length];
          for (ReportIndex curHeader : ReportIndex.values()) {
            tempResultArray[curHeader.ordinal()] = curHeader.toString().replace("_", " ");
          }
        }

        try {
          PrintWriter pw = new PrintWriter(resultFilePath);
          pw.println(GeneralHelper.arrayToDelimString(tempResultArray, "\t"));
          pw.flush();
          for (String docNameAndSentence : documentStatistics.keySet()) {
            Arrays.fill(tempResultArray, "");
            String[] docAndSentenceArray = docNameAndSentence.split("\t");
            tempResultArray[0] = docAndSentenceArray[0];
            tempResultArray[1] = docAndSentenceArray[1];
            for (RaptatPair<Integer, Integer> curPair : documentStatistics
                .get(docNameAndSentence)) {
              tempResultArray[curPair.left] = Integer.toString(curPair.right);
            }
            pw.println(GeneralHelper.arrayToDelimString(tempResultArray, "\t"));
            pw.flush();
          }
          pw.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      }


      private void printTrainingFeatures() {
        TextAnalyzer ta = new TextAnalyzer();
      }


      private List<RaptatPair<Integer, Integer>> processAssociations(
          HashMap<AnnotatedPhrase, RaptatPair<HashSet<String>, HashSet<String>>> associations) {
        HashMap<String, Integer> associationValues = new HashMap<>();
        for (RaptatPair<HashSet<String>, HashSet<String>> curAssociation : associations.values()) {
          /* Find all reference concepts and associations */
          for (String curReferenceConcept : curAssociation.left) {
            Integer refConceptValue = associationValues.get(curReferenceConcept);
            if (refConceptValue == null) {
              associationValues.put(curReferenceConcept, new Integer(1));
            } else {
              associationValues.put(curReferenceConcept, refConceptValue + 1);
            }

            for (String curDictionaryConcept : curAssociation.right) {
              String associationName = curDictionaryConcept + "_AND_" + curReferenceConcept;
              Integer associationValue = associationValues.get(associationName);
              if (associationValue == null) {

                associationValues.put(associationName, new Integer(1));
              } else {
                associationValues.put(associationName, associationValue + 1);
              }
            }
          }

          /* Now find just the dictionary concepts */

          for (String curDictionaryConcept : curAssociation.right) {
            Integer dictConceptValue = associationValues.get(curDictionaryConcept);
            if (dictConceptValue == null) {
              associationValues.put(curDictionaryConcept, new Integer(1));
            } else {
              associationValues.put(curDictionaryConcept, dictConceptValue + 1);
            }
          }
        }

        return translateToIndices(associationValues);
      }


      private List<RaptatPair<Integer, Integer>> translateToIndices(
          HashMap<String, Integer> associationValues) {
        List<RaptatPair<Integer, Integer>> resultList = new ArrayList<>();
        for (String curAssociation : associationValues.keySet()) {
          try {
            ReportIndex reportIndex = Enum.valueOf(ReportIndex.class, curAssociation);
            resultList.add(
                new RaptatPair<>(reportIndex.ordinal(), associationValues.get(curAssociation)));
          } catch (IllegalArgumentException e) {
            GeneralHelper.errorWriter(e.getMessage());
            e.printStackTrace();
          }
        }
        return resultList;
      }


      /**
       * Takes an association between sentences and a set of concepts in the sentence and outputs a
       * listing of the text in those sentences paired with a list representing the concepts in
       * terms of their ordinal value in ReportIndexFine enum. Each is paired with a value of 1,
       * which is just a dummy value to be consistent with earlier code of translateToIndices()
       * method.
       */
      private List<RaptatPair<String, List<RaptatPair<Integer, Integer>>>> translateToIndicesFine(
          HashMap<AnnotatedPhrase, HashSet<String>> associations) {

        List<RaptatPair<String, List<RaptatPair<Integer, Integer>>>> resultList = new ArrayList<>();
        for (AnnotatedPhrase curPhrase : associations.keySet()) {
          List<RaptatPair<Integer, Integer>> conceptsList = new ArrayList<>();

          for (String curConcept : associations.get(curPhrase)) {
            try {
              ReportIndexFine reportIndexFine = Enum.valueOf(ReportIndexFine.class, curConcept);
              conceptsList.add(new RaptatPair<>(reportIndexFine.ordinal(), 1));
            } catch (IllegalArgumentException e) {
              GeneralHelper.errorWriter(e.getMessage());
              e.printStackTrace();
            }
          }
          String phrase = curPhrase.getPhraseStringUnprocessed().replaceAll("^[\\w\\s]", "");
          phrase = "'" + phrase.replaceAll("\\n|\\r|\\t", "");
          resultList.add(new RaptatPair<>(phrase, conceptsList));
        }
        return resultList;
      }


      protected List<RaptatPair<File, File>> createTrainingDocumentLists(String textFilePath,
          String conceptRefFilePath) {
        File[] fileList = new File(textFilePath).listFiles();
        HashMap<String, File> textSet = new HashMap<>();
        ArrayList<RaptatPair<File, File>> resultList = new ArrayList<>(fileList.length);

        for (File curFile : fileList) {
          textSet.put(curFile.getName(), curFile);
        }

        fileList = new File(conceptRefFilePath).listFiles();
        int cutLength = ".knowtator.xml".length();
        for (File xmlFile : fileList) {
          String txtFileName = xmlFile.getName();
          int txtFileNameLength = txtFileName.length() - cutLength;
          txtFileName = txtFileName.substring(0, txtFileNameLength);
          File txtFile;
          if ((txtFile = textSet.get(txtFileName)) != null) {
            resultList.add(new RaptatPair<>(txtFile, xmlFile));
          }
        }

        return resultList;
      }
    });
  }
}
