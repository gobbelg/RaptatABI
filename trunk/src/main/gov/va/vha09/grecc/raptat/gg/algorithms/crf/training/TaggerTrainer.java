/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.github.jcrfsuite.CrfTrainer;
import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/**
 * Abstract superclass to support training of CRF models
 *
 * @author Glenn T. Gobbel Apr 19, 2016
 */
public abstract class TaggerTrainer {

  protected static final String DEFAULT_ALGORITHM = "lbfgs";

  protected static final String DEFAULT_GRAPHICAL_MODEL_TYPE = "crf1d";

  static {
    try {
      CrfSuiteLoader.load();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected static final Logger LOGGER = Logger.getLogger(TaggerTrainer.class);


  protected TextAnalyzer textAnalyzer = new TextAnalyzer();


  protected AnnotationImporter annotationImporter;

  protected PhraseTokenIntegrator theIntegrator = new PhraseTokenIntegrator();
  protected ContextType context = null;
  protected TokenProcessingOptions tokenProcessingOptions =
      new TokenProcessingOptions(OptionsManager.getInstance());

  protected TaggerTrainer() {
    TaggerTrainer.LOGGER.setLevel(Level.INFO);
  }

  protected TaggerTrainer(TokenProcessingOptions tokenProcessingOptions)
      throws IllegalArgumentException {
    this();

    if (tokenProcessingOptions.invertTokenSequence()) {
      String errorDescription =
          "Raptat CRF language models do not support inverting of token sequences";
      GeneralHelper.errorWriter(errorDescription);
      throw new IllegalArgumentException(errorDescription);
    }
    this.textAnalyzer.setTokenProcessingParameters(tokenProcessingOptions);
    this.tokenProcessingOptions = tokenProcessingOptions;
  }


  public abstract RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTestingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet);


  public abstract RaptatPair<List<AnnotatedPhrase>, List<AnnotatedPhrase>> getDocumentTrainingData(
      File textFile, File conceptsXML, HashSet<String> conceptSet);


  /**
   * @param features
   * @param labels
   * @param modelFilePath
   * @param trainingSet
   * @throws NullPointerException
   * @throws ArrayIndexOutOfBoundsException
   */
  public void train(List<ItemSequence> features, List<StringList> labels, String modelFilePath)
      throws NullPointerException, ArrayIndexOutOfBoundsException {
    if (features == null || labels == null) {
      throw new NullPointerException("Data supplied for CRF training is null");
    }

    if (features.isEmpty() || labels.isEmpty()) {
      throw new ArrayIndexOutOfBoundsException("Data supplied for CRF training is empty");
    }

    Pair<String, String>[] parameters = null;
    CrfTrainer.train(features, labels, modelFilePath, TaggerTrainer.DEFAULT_ALGORITHM,
        TaggerTrainer.DEFAULT_GRAPHICAL_MODEL_TYPE, parameters);
  }


  /**
   * Helper method for getTrainingSentencesAndPhrases that marks the tokens of phrases so we can
   * later determine the labels to use for feature building as part of CRF model training
   *
   * @param phrasesToMark
   */
  @SuppressWarnings("unused")
  protected void markPhraseTokens(List<AnnotatedPhrase> phrasesToMark) {
    for (AnnotatedPhrase curPhrase : phrasesToMark) {
      String conceptName = curPhrase.getConceptName();
      List<RaptatToken> phraseTokens = curPhrase.getProcessedTokens();
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


  /**
   * Helper method for getTrainingSentencesAndPhrases that marks the tokens of phrases so we can
   * later determine the labels to use for feature building as part of CRF model training
   *
   * @param phrasesToMark
   */
  protected void markPhraseTokens(List<AnnotatedPhrase> phrasesToMark, String beginMark,
      String insideMark) {
    for (AnnotatedPhrase curPhrase : phrasesToMark) {
      List<RaptatToken> phraseTokens = curPhrase.getProcessedTokens();
      int i = 0;
      for (RaptatToken curToken : phraseTokens) {
        RaptatToken parentToken;
        if ((parentToken = curToken.getParentToken()) == null) {
          parentToken = curToken;
        }

        if (i == 0) {
          parentToken.setTokenStringTagged(beginMark);
        } else {
          parentToken.setTokenStringTagged(insideMark);
        }
        i++;
      }
    }
  }


  /**
   * Uses a PrintWriter instance supplied as a parameter to print out a file in which token labels
   * and the features for each token are printed out in a tab-separated format.
   *
   * @param sentenceFeatures
   * @param sentenceTokenLabels
   * @param pw
   */
  public static void printFeaturesAndLabels(List<ItemSequence> sentenceFeatures,
      List<StringList> sentenceTokenLabels, PrintWriter pw) {
    for (int i = 0; i < sentenceFeatures.size(); i++) {
      pw.println();
      ItemSequence curSentenceFeatures = sentenceFeatures.get(i);
      StringList curSentenceTokenLabels = sentenceTokenLabels.get(i);
      for (int j = 0; j < curSentenceFeatures.size(); j++) {
        Item curToken = curSentenceFeatures.get(j);
        StringBuilder sb = new StringBuilder(curSentenceTokenLabels.get(j));
        for (int k = 0; k < curToken.size(); k++) {
          sb.append("\t").append(curToken.get(k).getAttr());
        }
        pw.println(sb.toString());
      }
    }
    pw.flush();
  }


  /**
   * Prints out features and labels to System.out in which token labels and the features for each
   * token are printed out in a tab-separated format.
   *
   * @param sentenceFeatures
   * @param sentenceTokenLabels
   */
  protected static void printFeaturesAndLabels(List<ItemSequence> sentenceFeatures,
      List<StringList> sentenceTokenLabels) {
    for (int i = 0; i < sentenceFeatures.size(); i++) {
      System.out.println();
      ItemSequence curSentenceFeatures = sentenceFeatures.get(i);
      StringList curSentenceTokenLabels = sentenceTokenLabels.get(i);
      for (int j = 0; j < curSentenceFeatures.size(); j++) {
        Item curToken = curSentenceFeatures.get(j);
        StringBuilder sb = new StringBuilder(curSentenceTokenLabels.get(j));
        for (int k = 0; k < curToken.size(); k++) {
          sb.append("\t").append(curToken.get(k).getAttr());
        }
        System.out.println(sb.toString());
      }
    }
    System.out.flush();
  }
}
