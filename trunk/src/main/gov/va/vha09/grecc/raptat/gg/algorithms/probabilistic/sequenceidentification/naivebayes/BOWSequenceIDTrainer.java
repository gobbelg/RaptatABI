package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class BOWSequenceIDTrainer extends SequenceIDTrainer {
  static Logger logger = Logger.getLogger(BOWSequenceIDTrainer.class);
  private TSFinderMethod priorTokenTraining = null, postTokenTraining = null;
  private final PhraseIDSmoothingMethod smoothingMethod;
  private boolean linkToPriorTokens, linkToPostTokens;
  private final BOWSequenceIDSolution theSolution;
  private final int tokenWindowWidth;


  /**
   * ******************************************** Constructor called when a solution already exists
   * that needs to be updated
   *
   * @param previousSolution
   * @param trainingOptions
   * @author Glenn Gobbel - Oct 12, 2012 ********************************************
   */
  public BOWSequenceIDTrainer(BOWSequenceIDSolution previousSolution) {
    PhraseIDTrainOptions trainingOptions = previousSolution.getTrainingOptions();

    this.tokenWindowWidth = trainingOptions.getSurroundingTokensToUse();
    this.smoothingMethod = trainingOptions.getSmoothingMethod();

    if (this.tokenWindowWidth > 0) {
      this.priorTokenTraining = trainingOptions.getPriorTokenTraining();
      this.postTokenTraining = trainingOptions.getPostTokenTraining();
      this.linkToPriorTokens = trainingOptions.linkTrainingToPrior();
      this.linkToPostTokens = trainingOptions.linkTrainingToPost();
    }

    this.theSolution = previousSolution;
  }


  /**
   * ******************************************** Constructor called when there is no existing
   * solution
   *
   * @param trainingOptions
   * @author Glenn Gobbel - Oct 12, 2012 ********************************************
   */
  public BOWSequenceIDTrainer(PhraseIDTrainOptions trainingOptions) {
    this.tokenWindowWidth = trainingOptions.getSurroundingTokensToUse();
    this.smoothingMethod = trainingOptions.getSmoothingMethod();

    if (this.tokenWindowWidth > 0) {
      this.priorTokenTraining = trainingOptions.getPriorTokenTraining();
      this.postTokenTraining = trainingOptions.getPostTokenTraining();
      this.linkToPriorTokens = trainingOptions.linkTrainingToPrior();
      this.linkToPostTokens = trainingOptions.linkTrainingToPost();
    }

    this.theSolution = new BOWSequenceIDSolution(trainingOptions);
  }


  @Override
  public void clearTraining() {
    this.theSolution.clear();
  }


  @Override
  public PhraseIDSmoothingMethod getPhraseIDSmoothingMethod() {
    // BOWSequenceIDTraining does not have optional smoothing
    // methods, so this just returns null for compliance with
    // the implemented interface
    return null;
  }


  @Override
  public SequenceIDStructure getSequenceIDStructure() {
    return this.theSolution;
  }


  @Override
  public void reversePhraseAndTokenOrder(List<AnnotatedPhrase> sentences) {
    // TODO Auto-generated method stub

  }


  @Override
  public void updatePreviousTraining(AnnotationGroup theGroup) {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public void updateProbabilities() {
    if (this.theSolution != null) {
      this.theSolution.updateProbabilities();
    }

    if (BOWSequenceIDTrainer.logger.getEffectiveLevel() == Level.DEBUG) {
      this.theSolution.printTokenConditional(1);
    }
  }


  @Override
  public void updateTraining(AnnotationGroup trainingData) {
    List<AnnotatedPhrase> docSentences = trainingData.getRaptatDocument().getActiveSentences();
    // Create mapping of sentences to annotated phrases
    Hashtable<AnnotatedPhrase, List<AnnotatedPhrase>> sentenceToAnnotationMap =
        getSentenceToAnnotationMap(docSentences, trainingData.referenceAnnotations);

    for (AnnotatedPhrase curSentence : docSentences) {
      BOWSequenceIDTrainer.logger.debug("Training on sentence:" + curSentence.toString());
      List<AnnotatedPhrase> curAnnotations = sentenceToAnnotationMap.get(curSentence);
      if (curAnnotations.isEmpty()) {
        addNonAnnotatedSentence(curSentence);
      } else {
        addSentenceWithAnnotations(curSentence, curAnnotations);
      }
    }
  }


  protected void createSolution(List<AnnotationGroup> trainingData) {
    int dataSize = trainingData.size();

    if (dataSize > 0) {
      for (AnnotationGroup curGroup : trainingData) {
        updateTraining(curGroup);
      }
      this.theSolution.updateProbabilities();
    }
  }


  private void addNonAnnotatedSentence(AnnotatedPhrase curSentence) {
    List<String> tokenStrings = curSentence.getProcessedTokensAugmentedStringsAsSentence();
    this.theSolution.updateNonAnnotated(tokenStrings, this.tokenWindowWidth);
  }


  /**
   * *********************************************************** The algorithm will first add all
   * tokens as unannotated. Then, it will go through the list of annotated phrases and tokens,
   * decrementing the "non-annotated" values and decrementing the "not upstream of an annotation"
   * values as well as incrementing their annotated occurrence values and the upstream occurrence
   * values of the appopriate tokens. We do it this way to avoid having to check multiple times
   * whether a token is annotated, upstream, or downstream. We assume most are not.
   *
   * @param theSentence
   * @param theAnnotations
   * @author Glenn Gobbel - Sep 17, 2012 ***********************************************************
   */
  private void addSentenceWithAnnotations(AnnotatedPhrase theSentence,
      List<AnnotatedPhrase> theAnnotations) {
    int estimatedSentenceLength = 32;
    HashSet<RaptatToken> allPhraseTokens = new HashSet<>(estimatedSentenceLength);
    HashSet<RaptatToken> allUpstreamTokens = new HashSet<>(estimatedSentenceLength);
    HashSet<RaptatToken> allDownstreamTokens = new HashSet<>(estimatedSentenceLength);

    PriorTable thePriors = this.theSolution.getPriors();
    List<RaptatToken> sentenceTokens = theSentence.getProcessedTokensAsSentence();

    for (AnnotatedPhrase curPhrase : theAnnotations) {
      List<RaptatToken> phraseTokens = curPhrase.getProcessedTokens();
      allPhraseTokens.addAll(phraseTokens);
      allUpstreamTokens.addAll(getUpstreamTokens(sentenceTokens, phraseTokens));
      allDownstreamTokens.addAll(getDownstreamTokens(sentenceTokens, phraseTokens));

      // We keep track of number of annotated tokens as the unannotated
      // are used
      // to calculate how many unannotated phrases exist in the sentence.
      thePriors.phraseSizeCounts[phraseTokens.size() - 1].annotated++;
    }

    if (!theAnnotations.isEmpty()) {
      thePriors.probabilitiesUpdated = false;
    }

    // The number of unannotated phrases for a phrase of size x is the
    // number of
    // tokens in the sentence that are not annotated divided by x rounded to
    // the closest integer. The incrementUnannotated(int) method calculates
    // this and stores it in each element of an array, where each element
    // corresponds
    // to phrases of length index + 1, so the 0th index stores unannotated
    // phrases
    // of length 1.
    thePriors.incrementUnannotated(sentenceTokens.size() - allPhraseTokens.size());

    this.theSolution.updateAnnotated(allPhraseTokens, RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX);
    this.theSolution.updateAnnotated(allUpstreamTokens,
        RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX);
    this.theSolution.updateAnnotated(allDownstreamTokens,
        RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX);

    for (RaptatToken curToken : sentenceTokens) {
      String tokenString = curToken.getTokenStringAugmented();
      if (!allPhraseTokens.contains(curToken) && !RaptatToken.startOrEndToken(curToken)) {
        this.theSolution.incrementUnannotated(tokenString, 1,
            RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX);
      }
      if (!allUpstreamTokens.contains(curToken)) {
        this.theSolution.incrementUnannotated(tokenString, 1,
            RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX);
      }
      if (!allDownstreamTokens.contains(curToken)) {
        this.theSolution.incrementUnannotated(tokenString, 1,
            RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX);
      }
    }
  }


  /**
   * *********************************************************** Determine the token strings that
   * are downstream from a phrase within a sentence and return the token strings as a list
   *
   * @param sentenceTokens
   * @param sentenceTokenStrings
   * @param phraseTokens
   * @return
   * @author Glenn Gobbel - Sep 19, 2012 ***********************************************************
   */
  private List<RaptatToken> getDownstreamTokens(List<RaptatToken> sentenceTokens,
      List<RaptatToken> phraseTokens) {
    int endPhraseIndex = sentenceTokens.indexOf(phraseTokens.get(phraseTokens.size() - 1));
    int sentenceLength = sentenceTokens.size();

    if (endPhraseIndex < sentenceLength - 1) {
      int endDownstreamIndex;
      if ((endDownstreamIndex = endPhraseIndex + 1 + this.tokenWindowWidth) > sentenceTokens
          .size()) {
        endDownstreamIndex = sentenceTokens.size();
      }

      return sentenceTokens.subList(endPhraseIndex + 1, endDownstreamIndex);
    }
    return null;
  }


  /**
   * *********************************************************** Creates a mapping so that every
   * sentence can be hashed to return the phrases within the sentence that are annotated
   *
   * @param theSentences
   * @param theAnnotations
   * @return Hashtable in which the sentence hashes to a list of annotated phrases
   * @author Glenn Gobbel - Sep 17, 2012 ***********************************************************
   */
  private Hashtable<AnnotatedPhrase, List<AnnotatedPhrase>> getSentenceToAnnotationMap(
      List<AnnotatedPhrase> theSentences, List<AnnotatedPhrase> theAnnotations) {
    Hashtable<AnnotatedPhrase, List<AnnotatedPhrase>> sentenceToAnnotationMap = new Hashtable<>();

    for (AnnotatedPhrase curSentence : theSentences) {
      sentenceToAnnotationMap.put(curSentence, new ArrayList<AnnotatedPhrase>(3));
    }

    for (AnnotatedPhrase curPhrase : theAnnotations) {
      AnnotatedPhrase annSentence = curPhrase.getProcessedTokens().get(0).getSentenceOfOrigin();
      List<AnnotatedPhrase> sentenceAnnotations = sentenceToAnnotationMap.get(annSentence);
      if (sentenceAnnotations != null) {
        sentenceAnnotations.add(curPhrase);
      }
    }

    return sentenceToAnnotationMap;
  }


  /**
   * *********************************************************** Determine the token strings that
   * are upstream from a phrase within a sentence and return the token strings as a list
   *
   * @param sentenceTokens
   * @param sentenceTokenStrings
   * @param phraseTokens
   * @return
   * @author Glenn Gobbel - Sep 19, 2012 ***********************************************************
   */
  private List<RaptatToken> getUpstreamTokens(List<RaptatToken> sentenceTokens,
      List<RaptatToken> phraseTokens) {
    int startPhraseIndex = sentenceTokens.indexOf(phraseTokens.get(0));

    if (startPhraseIndex > 0) {
      int startUpstreamIndex =
          startPhraseIndex > this.tokenWindowWidth ? startPhraseIndex - this.tokenWindowWidth : 0;
      return sentenceTokens.subList(startUpstreamIndex, startPhraseIndex);
    }
    return null;
  }
}
