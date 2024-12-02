package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurrenceSet;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class BOWSequenceIDCalculator {
  static Logger logger = Logger.getLogger(BOWSequenceIDCalculator.class);
  private ConcurrentHashMap<String, OccurrenceSet>[] tokenConditionals;
  private PriorTable priors;
  private int tokenWindowWidth;
  private double[] missingTokenLikelihood;


  public BOWSequenceIDCalculator(BOWSequenceIDSolution inputSolution) {
    this.tokenConditionals = inputSolution.getTokenConditionals();
    this.priors = inputSolution.getPriors();
    this.tokenWindowWidth = inputSolution.getTokenWindowWidth();
    this.missingTokenLikelihood = inputSolution.getMissingTokenLikelihood();
  }


  public List<RaptatPair<List<RaptatToken>, Double>> getLikelySequences(
      ConcurrentHashMap<Integer, HashSet<Integer>> indicesForTesting, AnnotatedPhrase curSentence,
      double likelihoodCutOff) {
    double curTotalProbability = 0.0;
    LinkedList<RaptatPair<List<RaptatToken>, Double>> sentenceProbabilitiesList =
        new LinkedList<>();

    // The downstreamProbabilities map is used to cache downstream
    // probabilities linked with a
    // given
    // token so they don't have to be repeatedly calculated
    ConcurrentHashMap<RaptatToken, Double> downstreamProbabilities = new ConcurrentHashMap<>(64);

    List<String> sentenceTokenStrings = curSentence.getProcessedTokensAugmentedStringsAsSentence();
    List<RaptatToken> sentenceTokens = curSentence.getProcessedTokensAsSentence();
    int sentenceSize = sentenceTokenStrings.size();

    Set<Integer> indexKeys = indicesForTesting.keySet();
    for (Integer curStartIndex : indexKeys) {
      double upstreamProbability = getUpstreamProbability(curStartIndex, sentenceTokenStrings);
      HashSet<Integer> endIndices = indicesForTesting.get(curStartIndex);
      Double downstreamProbability;
      for (Integer curEndIndex : endIndices) {
        RaptatToken curEndToken = sentenceTokens.get(curEndIndex);
        if ((downstreamProbability = downstreamProbabilities.get(curEndToken)) == null) {
          downstreamProbability = getDownstreamProbability(curEndIndex, sentenceTokenStrings);
          downstreamProbabilities.put(curEndToken, downstreamProbability);
        }
        curTotalProbability = upstreamProbability + downstreamProbability;
        curTotalProbability +=
            getPhraseProbability(sentenceTokenStrings.subList(curStartIndex, curEndIndex));

        // Now determine prior indices to determine contribution of
        // prior
        int phraseLength = curEndIndex - curStartIndex;
        int upstreamLength =
            curStartIndex >= this.tokenWindowWidth ? this.tokenWindowWidth : curStartIndex;
        int downstreamLength =
            curEndIndex <= sentenceSize - this.tokenWindowWidth ? this.tokenWindowWidth
                : sentenceSize - curEndIndex;
        curTotalProbability +=
            this.priors.likelihood[upstreamLength][phraseLength][downstreamLength];

        if (curTotalProbability > likelihoodCutOff) {
          BOWSequenceIDCalculator.logger.debug("\nSequence Probability:" + curTotalProbability
              + " exceeds cutoff" + "\nAdding sequence for consideration:");
          BOWSequenceIDCalculator.logger
              .debug(Arrays.toString(sentenceTokens.subList(curStartIndex, curEndIndex).toArray()));
          sentenceProbabilitiesList.add(new RaptatPair<>(
              sentenceTokens.subList(curStartIndex, curEndIndex), curTotalProbability));
        }
      }
    }
    return sentenceProbabilitiesList;
  }


  /**
   * *********************************************************** Identify the first and last index
   * of sequences within a sentence that have at least one token that has occurred as part of an
   * annotated sequence. The first token in the sequences are stored in a hashmap where the index
   * value of the start maps to a set of potential ending indices. These are used to select sublists
   * to evaluate for likelihood of annotation. Note that the sublist method includes the first index
   * to the next to last index only when used in the form List.sublist(firstIndex,lastIndex).
   *
   * @param sentenceTokens
   * @return
   * @author Glenn Gobbel - Sep 18, 2012 ***********************************************************
   */
  public ConcurrentHashMap<Integer, HashSet<Integer>> getSequencesForEvaluation(
      List<RaptatToken> sentenceTokens) {
    int sentenceSize = sentenceTokens.size();
    List<Integer> annotatedIndices = new ArrayList<>(sentenceSize / 2);
    ConcurrentHashMap<Integer, HashSet<Integer>> resultIndices = new ConcurrentHashMap<>();

    // Get the conditionals that hold token strings that were part of an
    // annotated phrase
    ConcurrentHashMap<String, OccurrenceSet> conditionalsForEvaluation =
        this.tokenConditionals[RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX];

    for (int i = 0; i < sentenceSize; i++) {
      OccurrenceSet occurrences =
          conditionalsForEvaluation.get(sentenceTokens.get(i).getTokenStringAugmented());
      if (occurrences != null && occurrences.annotatedPhrases > 0) {
        annotatedIndices.add(i);
      }
    }

    if (annotatedIndices.isEmpty()) {
      return resultIndices;
    }

    // Create a list that contains beginning and end indices of all
    // potentially annotated
    // phrases

    // First we'll set the downstream indices corresponding to a given start
    // index
    for (Integer curStartIndex : annotatedIndices) {
      HashSet<Integer> curEndIndices = new HashSet<>();
      resultIndices.put(curStartIndex, curEndIndices);
      // We keep i less than the sentence size because we never want to
      // include the END token
      // as part of
      // an annotation, so it should never be returned as part of a
      // sequence for evaluation of
      // annotation
      for (int i = curStartIndex + 1; i - curStartIndex <= RaptatConstants.MAX_TOKENS_DEFAULT
          && i < sentenceSize; i++) {
        curEndIndices.add(i);
      }
    }

    // Now we go through and find the upstream indices corresponding to each
    // index
    for (Integer annotationIndex : annotatedIndices) {
      int endIndex = annotationIndex + 1;
      int startIndex = endIndex - RaptatConstants.MAX_TOKENS_DEFAULT;

      // We are using the processed tokens as sentence, and we never
      // should annotate the
      // "START" token
      // marking the start of a sentence, so we start no earlier than
      // index 1, which should be
      // the
      // token after the "START" token
      if (startIndex < 1) {
        startIndex = 1;
      }

      for (; startIndex < endIndex; startIndex++) {
        HashSet<Integer> curEndIndices;
        if ((curEndIndices = resultIndices.get(startIndex)) == null) {
          curEndIndices = new HashSet<>();
          resultIndices.put(startIndex, curEndIndices);
        }
        curEndIndices.add(endIndex);
      }
    }

    return resultIndices;
  }


  private double getDownstreamProbability(Integer downstreamStartIndex,
      List<String> inputSentence) {
    double result = 0.0;
    int sentenceSize = inputSentence.size();

    if (downstreamStartIndex < sentenceSize) {
      OccurrenceSet occSet;
      for (int i = downstreamStartIndex, j = 0; i < sentenceSize
          && j < this.tokenWindowWidth; i++, j++) {
        String curKey = inputSentence.get(i);
        if ((occSet = this.tokenConditionals[RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX]
            .get(curKey)) != null) {
          result += occSet.likelihood;
        } else {
          result += this.missingTokenLikelihood[RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX];
        }
      }
    }
    return result;
  }


  private double getPhraseProbability(List<String> phraseSequence) {
    double result = 0.0;
    OccurrenceSet occSet;

    for (String curTokenString : phraseSequence) {
      if ((occSet = this.tokenConditionals[RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX]
          .get(curTokenString)) != null) {
        result += occSet.likelihood;
      } else {
        result += this.missingTokenLikelihood[RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX];
      }
    }

    return result;
  }


  private double getUpstreamProbability(Integer phraseStartIndex, List<String> inputSentence) {
    double result = 0.0;

    if (phraseStartIndex != 0) {
      OccurrenceSet curSet;
      for (int i = phraseStartIndex - 1, j = 0; i >= 0 && j < this.tokenWindowWidth; i--, j++) {
        String curKey = inputSentence.get(i);
        if ((curSet = this.tokenConditionals[RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX]
            .get(curKey)) != null) {
          result += curSet.likelihood;
        } else {
          result += this.missingTokenLikelihood[RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX];
        }
      }
    }
    return result;
  }
}
