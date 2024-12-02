package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.tagging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.github.jcrfsuite.CrfTagger;
import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;
import com.google.common.primitives.Ints;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.CrfSuiteRunner;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CSFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.FeaturePrinter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.NegexTagger;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.SentenceContextAttributeAnalyzer;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;

/**
 * Determines attributes of AnnotatedPhrase instances using a cue-scope model. By default, if one of
 * the tokens in the phrase is "in scope" of a cue, the AnnotatedPhrase instance is assigned the
 * 'assignmentValue' of this tagger. Otherwise, the phrase is given the defaultValue.
 *
 * @author Glenn T. Gobbel Feb 19, 2016l
 */
public class CSAttributeTagger {
  private enum RunType {
    TAG
  }

  private CSFeatureBuilder featureBuilder;

  private CrfSuiteRunner crfSuiteRunner;


  private UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;

  /*
   * The type of context used to assign the attribute value to a token in an AnnotatedPhrase
   * instance
   */
  private ContextType contextType;

  private HashSet<String> includedScopeFeatures;

  static {
    try {
      CrfSuiteLoader.load();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  private static final Logger LOGGER = Logger.getLogger(CSAttributeTagger.class);

  public CSAttributeTagger(AttributeTaggerSolution taggerSolution, CrfSuiteRunner crfSuiteRunner) {
    this();
    this.crfSuiteRunner = crfSuiteRunner;
    this.contextType = taggerSolution.getContext();
    this.featureBuilder = (CSFeatureBuilder) taggerSolution.getFeatureBuilder();
    this.featureBuilder.initializeTransients();
    this.includedScopeFeatures = this.featureBuilder.getIncludedScopeFeaturesSet();

    crfSuiteRunner.initialize(taggerSolution);
  }


  private CSAttributeTagger() {
    super();
    CSAttributeTagger.LOGGER.setLevel(Level.INFO);
  }


  public void assignAttribute(AnnotatedPhrase annotatedPhrase) {
    List<AnnotatedPhrase> phraseAsList = new ArrayList<>(1);
    phraseAsList.add(annotatedPhrase);
    assignCSAttributes(phraseAsList);
  }


  public void assignCSAttributes(List<AnnotatedPhrase> annotatedPhrases) {
    /*
     * This method call finds sentences associated with the phrases in the list and eliminates
     * duplicates where the same sentence has more than one phrase. Order of sentences is unrelated
     * to phrase order in the annotatedPhrases parameter.
     */
    List<AnnotatedPhrase> sentences = AnnotatedPhrase.getAssociatedSentences(annotatedPhrases);

    /*
     * Build the features for each token in all the sentences. Each set of token features is stored
     * in an Item instance. All the features for all the tokens for a sentence are stored in an
     * ItemSequence instance, so there is one ItemSequence instance per sentence. The list of
     * ItemSequences thus represents the list of annotatedPhrases used as input to this method.
     */
    RaptatPair<List<ItemSequence>, List<ItemSequence>> cueAndScopeFeatures =
        this.featureBuilder.buildTaggingFeatures(sentences);

    /*
     * Determine which sentence contain tokens that are 'in scope'of a cue and which tokens in each
     * sentence are 'in scope'
     */
    List<ItemSequence> cueFeatureSentences = cueAndScopeFeatures.left;
    List<ItemSequence> scopeFeatureSentences = cueAndScopeFeatures.right;

    List<RaptatPair<Integer, Integer>> scopeLabels =
        getLabels(cueFeatureSentences, scopeFeatureSentences);

    /*
     * Mark tokens as 'in scope' using the contexts field of the RaptatToken instance
     */
    assignSentenceTokenContext(sentences, scopeLabels);

    addPhraseAttributes(annotatedPhrases);
  }


  /**
   * Helper method to assignAttributes that goes through a list of AnnotatedPhrase instances and
   * determines the value to assign to the attribute, attributeName.
   *
   * @param annotatedPhrases
   */
  private void addPhraseAttributes(List<AnnotatedPhrase> annotatedPhrases) {
    for (AnnotatedPhrase curPhrase : annotatedPhrases) {
      RaptatAttribute attribute = null;
      String uniqueID = "RapTAT_Attribute_" + this.idGenerator.getUnique();
      for (RaptatToken curToken : curPhrase.getProcessedTokens()) {
        RaptatToken parentToken;
        if ((parentToken = curToken.getParentToken()) == null) {
          parentToken = curToken;
        }

        String assignmentValue = null;
        if ((assignmentValue = parentToken.getContextValue(this.contextType)) != null) {
          attribute = new RaptatAttribute(uniqueID, this.contextType.getTypeName(), assignmentValue,
              AttributeAssignmentMethod.CUE_SCOPE_CRF);
          break;
        }
      }
      if (attribute == null) {
        attribute = new RaptatAttribute(uniqueID, this.contextType.getTypeName(),
            this.contextType.getDefaultValue(), AttributeAssignmentMethod.CUE_SCOPE_CRF);
      }
      curPhrase.addAttribute(attribute);
    }
  }


  /**
   * Mark sentence tokens that are 'in scope' based on the scopeLabels list, which shows which
   * sentence in the list (curPair.left) and token in the associated sentence (curPair.right) are in
   * scope. Use the contexts field of the RaptatToken instance to mark the token as 'in scope.'
   *
   * @param sentences
   * @param scopeLabels
   */
  private void assignSentenceTokenContext(List<AnnotatedPhrase> sentences,
      List<RaptatPair<Integer, Integer>> scopeLabels) {
    for (RaptatPair<Integer, Integer> curPair : scopeLabels) {
      RaptatToken tokenToAssign =
          sentences.get(curPair.left).getProcessedTokens().get(curPair.right);
      tokenToAssign.setContextValue(this.contextType, this.contextType.getCommonlyAssignedValue());
    }
  }


  /**
   * @param tokenLabels
   * @return
   */
  private int[] getCueLocations(List<Pair<String, Double>> tokenLabels) {
    Iterator<Pair<String, Double>> tokenIterator = tokenLabels.iterator();
    List<Integer> cuePositions = new ArrayList<>(tokenLabels.size());

    int currentPosition = 0;

    while (tokenIterator.hasNext()) {
      String curLabel = tokenIterator.next().first;
      if (curLabel.equals(RaptatConstants.BEGIN_CUE_STRING)
          || curLabel.equals(RaptatConstants.INSIDE_CUE_STRING)) {
        cuePositions.add(currentPosition);
      }
      currentPosition++;
    }

    return Ints.toArray(cuePositions);
  }


  /**
   * This is a helper method for assignAttributes.
   *
   * @param
   * @return A list of pairs where the 2 numbers stored in each pair correspond to a sentence and
   *         token that is positive for the attribute of interest. So if the 10th sentence in the
   *         features sentences had a cue token at position k, there would be an entry (n,k) in the
   *         returned list of pair.
   */
  private List<RaptatPair<Integer, Integer>> getLabels(List<ItemSequence> cueFeatures,
      List<ItemSequence> scopeFeatures) {
    List<RaptatPair<Integer, Integer>> labeledSentencesAndTokens = new ArrayList<>();

    /*
     * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
     */
    // Map<Integer, Map<Integer, Integer>> modifiableAttributeMap =
    // this.featureBuilder
    // .getTaggingAttributesForModification();

    CrfTagger cueTagger = this.crfSuiteRunner.getCueTagger();
    CrfTagger scopeTagger = this.crfSuiteRunner.getScopeTagger();

    Iterator<ItemSequence> cueFeatureIterator = cueFeatures.iterator();
    Iterator<ItemSequence> scopeFeatureIterator = scopeFeatures.iterator();
    int sentenceNumber = 0;
    while (cueFeatureIterator.hasNext()) {
      int tokenNumber;

      boolean cueFound = false;
      ItemSequence cueSentenceFeatures = cueFeatureIterator.next();
      ItemSequence scopeSentenceFeatures = scopeFeatureIterator.next();

      /* We first tag and find cues, changing features where found */
      {
        /*
         * We generally ignore the right side of every pair, which is the marginal probability of
         * the label
         */
        List<Pair<String, Double>> tokenLabels = cueTagger.tag(cueSentenceFeatures);

        // REGION LOGGING
        if (CSAttributeTagger.LOGGER.isDebugEnabled()) {
          System.out.println("=====================================\n");
          if (CSAttributeTagger.LOGGER.isTraceEnabled()) {
            FeaturePrinter.printAllFeatures(cueSentenceFeatures);
          }
          FeaturePrinter.printLabelsAndSentence(tokenLabels, cueSentenceFeatures);
        }
        // ENDREGION LOGGING

        /*
         * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
         */
        // tokenNumber = 0;
        // Iterator<Pair<String, Double>> labelIterator =
        // tokenLabels.iterator();
        // while (labelIterator.hasNext())
        // {
        // String curLabel = labelIterator.next().first;
        //
        // /*
        // * If we find a cue label (not an outside label), change the
        // * appropriate modifiable scope feature for that sentence
        // * and token
        // */
        // if ( !curLabel.equals( Constants.DEFAULT_CRF_OUTSIDE_STRING )
        // )
        // {
        // cueFound = true;
        // int attributeNumber = modifiableAttributeMap.get(
        // sentenceNumber ).get( tokenNumber );
        // scopeSentenceFeatures.get( tokenNumber ).get( attributeNumber
        // )
        // .setAttr( Constants.CUENEGATION_FEATURE_STRING );
        // }
        // tokenNumber++ ;
        // }

        /*
         * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
         * commented out above on 7/12/16
         */
        cueFound = updateScopeFeatures(tokenLabels, scopeSentenceFeatures);
      }

      /*
       * We next look for scope labels and add their sentence and token indices to the result if
       * found
       */
      {
        /*
         * If we found a cue in the sentence, we should look for its scope (otherwise we don't need
         * to look)
         */
        if (cueFound) {
          List<Pair<String, Double>> tokenLabels = scopeTagger.tag(scopeSentenceFeatures);

          // REGION LOGGING
          if (CSAttributeTagger.LOGGER.isDebugEnabled()) {
            System.out.println("\n");
            if (CSAttributeTagger.LOGGER.isTraceEnabled()) {
              FeaturePrinter.printAllFeatures(scopeSentenceFeatures);
            }
            FeaturePrinter.printLabelsAndSentence(tokenLabels, scopeSentenceFeatures);
          }
          // ENDREGION LOGGING

          tokenNumber = 0;
          Iterator<Pair<String, Double>> labelIterator = tokenLabels.iterator();
          while (labelIterator.hasNext()) {
            String curLabel = labelIterator.next().first;

            if (!curLabel.equals(RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING)) {
              labeledSentencesAndTokens.add(new RaptatPair<>(sentenceNumber, tokenNumber));

              // REGION LOGGING
              if (CSAttributeTagger.LOGGER.isTraceEnabled()) {
                System.out.println("Sentence:" + sentenceNumber + ", Token:" + tokenNumber);
              }
              // ENDREGION LOGGING
            }
            tokenNumber++;
          }
        }

        // REGION LOGGING
        else if (CSAttributeTagger.LOGGER.isDebugEnabled()) {
          System.out.println("\n--------------\nNO CUE FOUND\n--------------\n");
        }
        // ENDREGION LOGGING
      }
      sentenceNumber++;
    }
    return labeledSentencesAndTokens;
  }


  /**
   * Updates the features used for determining scope based on whether any tokens in the sentence
   * have been labeled as cues. Returns a boolean indicating whether ANY cue labels exist in the
   * sentence.
   *
   * @param tokenLabels
   * @param scopeSentenceFeatures
   * @return
   */
  private boolean updateScopeFeatures(List<Pair<String, Double>> tokenLabels,
      ItemSequence scopeSentenceFeatures) {
    int[] cueLocations = getCueLocations(tokenLabels);
    HashMap<Integer, List<Integer>> cueOffsetMap =
        this.featureBuilder.createMapFromCueLocations(cueLocations, tokenLabels.size());
    boolean cueFound = !cueOffsetMap.isEmpty();

    for (Integer curTokenIndex : cueOffsetMap.keySet()) {
      Item currentTokenItem = scopeSentenceFeatures.get(curTokenIndex);
      for (Integer curOffset : cueOffsetMap.get(curTokenIndex)) {
        String candidateFeature = RaptatConstants.CUE_OFFSET_STRING + "@" + curOffset.toString();
        if (this.includedScopeFeatures.isEmpty()
            || this.includedScopeFeatures.contains(candidateFeature)) {
          currentTokenItem.add(new Attribute(candidateFeature));
        }
      }
      scopeSentenceFeatures.set(curTokenIndex, currentTokenItem);
    }

    return cueFound;
  }


  public static void main(String[] args) {
    final boolean runAllFiles = true;
    final String xmlFilePath =
        "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\TrainingCorpusAndXMLForAssertionCrossValidation";
    final String textFilePath =
        "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\TrainingCorpusAndXMLForAssertionCrossValidation";

    final String singleXMLFilePath =
        "D:\\CARTCL-IIR\\ActiveLearning\\Sanjib\\AKI_Blocks5_6_7_Copy\\AKI_Block5\\adjudication\\Block_05_Note_024.txt.knowtator.xml";
    final String singleTextFilePath =
        "D:\\CARTCL-IIR\\ActiveLearning\\Sanjib\\AKI_Blocks5_6_7_Copy\\AKI_Block5\\corpus\\Block_05_Note_024.txt";

    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      public List<AnnotatedPhrase> preparePhrasesForTesting(
          TokenProcessingOptions tokenProcessingOptions) {
        List<AnnotatedPhrase> testPhrases = new ArrayList<>();

        List<File> testXMLFiles = new ArrayList<>();
        List<File> testTextFiles = new ArrayList<>();

        if (runAllFiles) {
          testTextFiles.addAll(Arrays.asList(
              new File(textFilePath).listFiles((FilenameFilter) new RegexFileFilter(".*\\.txt"))));
          testXMLFiles.addAll(Arrays.asList(
              new File(xmlFilePath).listFiles((FilenameFilter) new RegexFileFilter(".*\\.xml"))));
        } else {
          testXMLFiles.add(new File(singleXMLFilePath));
          testTextFiles.add(new File(singleTextFilePath));
        }

        AnnotationImporter theImporter = new AnnotationImporter(AnnotationApp.EHOST);
        PhraseTokenIntegrator theIntegrator = new PhraseTokenIntegrator();
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        textAnalyzer.setTokenProcessingParameters(tokenProcessingOptions);
        textAnalyzer.addSectionMatcher("allergy",
            "^\\s*(?:\\w+\\s+)?(?:allerg|allrg|alerg|alleg|adverse|advrs).*");
        textAnalyzer.addSentenceMatcher("allergy",
            "^\\s*(?:\\w+\\s+)?(?:allerg|allrg|alerg|alleg|adverse|advrs).*");

        for (int i = 0; i < testTextFiles.size(); i++) {
          System.out.println("IMPORTING:" + testTextFiles.get(i).getName());
          List<AnnotatedPhrase> currentPhrases = theImporter.importAnnotations(
              testXMLFiles.get(i).getAbsolutePath(), testTextFiles.get(i).getAbsolutePath(), null);
          RaptatDocument doc = textAnalyzer.processDocument(testTextFiles.get(i).getAbsolutePath());
          theIntegrator.addProcessedTokensToAnnotations(currentPhrases, doc, false, 0);

          testPhrases.addAll(currentPhrases);
        }
        return testPhrases;
      }


      @Override
      public void run() {
        UserPreferences.INSTANCE.initializeWorkingDirectory(false);
        RunType runType = RunType.TAG;

        switch (runType) {
          case TAG:
            String solutionFilePath = "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160720\\"
                + "CueScopeSolution_Rd00_CSTrainingFeatures_UnprocessedTokens_v01_160721_175700.atsn";
            String fileNameTag = "BasicFeatures_CapsAllowed";
            tag(solutionFilePath, fileNameTag);
        }
      }


      private void combineCSAndAllergicAttributes(List<AnnotatedPhrase> phrasesForTesting) {
        for (AnnotatedPhrase phrase : phrasesForTesting) {
          RaptatAttribute csAttribute, allergyAttribute;
          String assignedAttributeValue = ContextType.ASSERTIONSTATUS.getDefaultValue();
          for (RaptatAttribute attribute : phrase.getPhraseAttributes()) {
            /*
             * If either the general or allergy-associated assertion analysis indicates a negative
             * concept, set it to negative
             */
            if ((attribute.getName().toLowerCase().equals("allergyassertionstatus")
                || attribute.getName().toLowerCase().equals("assertionstatusraptat"))
                && attribute.getValues().get(0).toLowerCase()
                    .equals(ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue().toLowerCase())) {
              assignedAttributeValue = ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue();
              break;
            }
          }
          RaptatAttribute combinedAttribute =
              new RaptatAttribute("RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique(),
                  "combinedassertionstatus", assignedAttributeValue,
                  AttributeAssignmentMethod.CUE_SCOPE_CRF);
          phrase.addAttribute(combinedAttribute);
        }
      }


      private String reportAttributes(AnnotatedPhrase curPhrase,
          HashSet<String> attributesToReport) {
        String documentName = curPhrase.getProcessedTokens().get(0).getSentenceOfOrigin()
            .getDocumentOfOrigin().getTextSource().orElse("NoDocumentName");
        StringBuilder sb = new StringBuilder(documentName).append("\t");

        String conceptName = curPhrase.getConceptName();
        sb.append(conceptName.length() > 0 ? conceptName : "NoConceptName");

        String conceptPhrase = curPhrase.getPhraseStringUnprocessed();
        sb.append("\t").append(conceptPhrase.length() > 0 ? conceptPhrase : "NoConceptPhrase");

        for (RaptatAttribute curAttribute : curPhrase.getPhraseAttributes()) {
          String attributeName = curAttribute.getName().toLowerCase();
          if (attributesToReport.contains(attributeName)) {
            sb.append("\t").append(attributeName);
            for (String curValue : curAttribute.getValues()) {
              sb.append("\t").append(curValue);
            }
          }
        }
        return sb.toString();
      }


      private void tag(String solutionFilePath, String fileNameTag) {
        File solutionFile = new File(solutionFilePath);
        AttributeTaggerSolution taggerSolution = AttributeTaggerSolution.loadFromFile(solutionFile);

        TokenProcessingOptions tokenProcessingOptions = taggerSolution.getTokenProcessingOptions();
        List<AnnotatedPhrase> phrasesForTesting = preparePhrasesForTesting(tokenProcessingOptions);

        /*
         * This is where we would expect to start in a "real" use case
         */

        CSAttributeTagger tagger = new CSAttributeTagger(taggerSolution, new CrfSuiteRunner());

        tagger.assignCSAttributes(phrasesForTesting);
        new NegexTagger().assignAttributes(phrasesForTesting);
        String allergyAttributeName = "allergyassertionstatus";
        new SentenceContextAttributeAnalyzer(allergyAttributeName, "allergy", "positive",
            "negative", true).assignAttributesTest(phrasesForTesting);
        combineCSAndAllergicAttributes(phrasesForTesting);

        HashSet<String> attributesToReport = new HashSet<>();
        attributesToReport.add("assertionstatus");
        attributesToReport.add("negexassertionstatus");
        attributesToReport.add(allergyAttributeName.toLowerCase());
        attributesToReport.add("combinedassertionstatus");
        attributesToReport.add(ContextType.ASSERTIONSTATUS.getTypeName().toLowerCase());

        String resultDirectory = solutionFile.getParent();
        String resultFileName =
            "TagResults_" + fileNameTag + "_" + GeneralHelper.getTimeStamp() + ".txt";
        String resultFilePath = resultDirectory + File.separator + resultFileName;
        try {
          PrintWriter pw = new PrintWriter(new File(resultFilePath));
          for (AnnotatedPhrase curPhrase : phrasesForTesting) {
            String reportLine = reportAttributes(curPhrase, attributesToReport);
            System.out.println(reportLine);
            pw.println(reportLine);
          }
          pw.flush();
          pw.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
