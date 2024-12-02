/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.tagging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.github.jcrfsuite.CrfTagger;
import com.github.jcrfsuite.util.Pair;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.CrfSuiteRunner;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CrfFeatureSet;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.FeaturePrinter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training.TaggerTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.CrfConceptSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.TokenSequenceFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import third_party.org.chokkan.crfsuite.ItemSequence;

/** @author Glenn T. Gobbel Apr 22, 2016 */
public class CRFTSFinder extends TokenSequenceFinder {
  private class ConceptLabel {
    private final int sentenceNumber;
    private final int tokenNumber;
    private final boolean startOfConcept;
    private final String conceptName;


    /**
     * @param sentenceNumber
     * @param tokenNumber
     * @param startOfConcept
     * @param conceptName
     */
    private ConceptLabel(int sentenceNumber, int tokenNumber, boolean startOfConcept,
        String conceptName) {
      this.sentenceNumber = sentenceNumber;
      this.tokenNumber = tokenNumber;
      this.startOfConcept = startOfConcept;
      this.conceptName = conceptName;
    }
  }

  private enum RunType {
    TAG;
  }

  private static final Logger LOGGER = Logger.getLogger(CRFTSFinder.class);


  private PrintWriter pwLogger = null;

  private FeatureBuilder featureBuilder;
  private CrfSuiteRunner crfSuiteRunner;
  private HashSet<String> conceptSet;

  /**
   * @param taggerSolution
   * @param crfSuiteRunner
   */
  public CRFTSFinder(CrfConceptSolution taggerSolution) {
    this();

    this.crfSuiteRunner = new CrfSuiteRunner();
    this.conceptSet = taggerSolution.getConceptSet();
    this.featureBuilder = taggerSolution.getFeatureBuilder();

    this.crfSuiteRunner.initialize(taggerSolution);
  }


  private CRFTSFinder() {
    super();
    CRFTSFinder.LOGGER.setLevel(Level.INFO);

    if (CRFTSFinder.LOGGER.isDebugEnabled()) {
      try {
        String directoryPath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis";
        String fileName = "featureLogger_" + GeneralHelper.getTimeStamp() + ".txt";
        this.pwLogger = new PrintWriter(new File(directoryPath + File.separator + fileName));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * @param sentencesForTagging
   * @return
   */
  public List<AnnotatedPhrase> getAnnotations(List<AnnotatedPhrase> sentences) {
    CrfFeatureSet features = this.featureBuilder.buildFeatures(sentences);

    if (CRFTSFinder.LOGGER.isDebugEnabled() && this.pwLogger != null) {
      TaggerTrainer.printFeaturesAndLabels(features.trainingFeatures, features.trainingLabels,
          this.pwLogger);
    }

    List<RaptatPair<List<RaptatToken>, String>> labeledSequences =
        getLabeledSequences(features.trainingFeatures, sentences);

    boolean invertTokenSequence = false;

    return convertToAnnotations(labeledSequences, invertTokenSequence);
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.TokenSequenceFinder# identifySequences
   * (src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument)
   */
  @Override
  public List<AnnotatedPhrase> identifySequences(RaptatDocument raptatDocument) {
    return getAnnotations(raptatDocument.getActiveSentences());
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.TokenSequenceFinder#
   * setMappedAttributes(java.util.List)
   */
  @Override
  public void setMappedAttributes(List<AnnotatedPhrase> annotations) {
    // TODO Auto-generated method stub

  }


  /**
   * This is a helper method for assignAttributes.
   *
   * @param
   * @return A list of pairs where the 2 numbers stored in each pair correspond to a sentence and
   *         token that is positive for the attribute of interest. So if the 10th sentence in the
   *         features sentences had a positive token at position k, there would be an entry (n,k) in
   *         the returned list.
   */
  private List<RaptatPair<List<RaptatToken>, String>> getLabeledSequences(
      List<ItemSequence> allSentenceFeatures, List<AnnotatedPhrase> sentences) {
    List<RaptatPair<List<RaptatToken>, String>> resultAnnotations = new ArrayList<>();
    CrfTagger conceptTagger = this.crfSuiteRunner.getConceptTagger();

    Iterator<ItemSequence> featureIterator = allSentenceFeatures.iterator();
    Iterator<AnnotatedPhrase> sentenceIterator = sentences.iterator();
    while (featureIterator.hasNext()) {
      ItemSequence curSentenceFeatures = featureIterator.next();
      AnnotatedPhrase curSentence = sentenceIterator.next();
      {
        List<Pair<String, Double>> tokenLabels = conceptTagger.tag(curSentenceFeatures);

        if (CRFTSFinder.LOGGER.isDebugEnabled()) {
          System.out.println("=====================================\n");
          if (CRFTSFinder.LOGGER.isTraceEnabled()) {
            FeaturePrinter.printAllFeatures(curSentenceFeatures);
          }
          FeaturePrinter.printLabelsAndSentence(tokenLabels, curSentenceFeatures);
        }

        Iterator<RaptatToken> tokenIterator = curSentence.getProcessedTokens().iterator();
        Iterator<Pair<String, Double>> labelIterator = tokenLabels.iterator();

        boolean conceptFound = false;
        List<RaptatToken> phraseTokenSequence = null;
        String conceptName = null;
        while (labelIterator.hasNext()) {
          String curLabel = labelIterator.next().first;
          RaptatToken curToken = tokenIterator.next();

          if (!curLabel.equals(RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING)) {
            String[] conceptLabels =
                StringUtils.splitByWholeSeparator(curLabel, RaptatConstants.CONCEPT_CONNECTOR);

            /*
             * If in state where no concept has been found (either just starting search or previous
             * found string outside of label), then we'll create a new token sequence and store
             * concept name.
             */
            if (!conceptFound) {
              conceptFound = true;
              phraseTokenSequence = new ArrayList<>();
              phraseTokenSequence.add(curToken);
              conceptName = conceptLabels[1];
              resultAnnotations.add(new RaptatPair<>(phraseTokenSequence, conceptName));
            }

            /*
             * If in state where we have already found a concept, add current token to current token
             * sequence unless we have come across another label indicating we are at the beginning
             * of a new phrase
             */
            else {
              if (conceptLabels[0].equals(RaptatConstants.BEGIN_CONCEPT_LABEL)) {
                conceptFound = true;
                phraseTokenSequence = new ArrayList<>();
                phraseTokenSequence.add(curToken);
                conceptName = conceptLabels[1];
                resultAnnotations.add(new RaptatPair<>(phraseTokenSequence, conceptName));
              } else {
                phraseTokenSequence.add(curToken);
              }
            }
          } else {
            conceptFound = false;
          }
        }
      }
    }
    return resultAnnotations;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();
    final String pathToTextDocument = "";

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RunType runType = RunType.TAG;

        switch (runType) {
          case TAG:
            String solutionFilePath = "";
            File solutionFile = new File(solutionFilePath);
            CrfConceptSolution taggerSolution =
                (CrfConceptSolution) TokenSequenceFinderSolution.loadFromFile(solutionFile);
            TokenProcessingOptions tokenProcessingOptions =
                taggerSolution.retrieveTokenProcessingOptions();
            List<AnnotatedPhrase> sentencesForTagging =
                prepareSentencesForTagging(tokenProcessingOptions);
            CRFTSFinder tagger = new CRFTSFinder(taggerSolution);
            tagger.getAnnotations(sentencesForTagging);
            break;
        }
      }


      private List<AnnotatedPhrase> prepareSentencesForTagging(
          TokenProcessingOptions tokenProcessingOptions) {
        TextAnalyzer theAnalyzer = new TextAnalyzer();
        theAnalyzer.setTokenProcessingParameters(tokenProcessingOptions);
        RaptatDocument doc = theAnalyzer.processDocument(pathToTextDocument);
        return doc.getActiveSentences();
      }
    });
  }
}
