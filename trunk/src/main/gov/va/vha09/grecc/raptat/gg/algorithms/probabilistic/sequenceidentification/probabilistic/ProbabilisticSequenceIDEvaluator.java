package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.PhraseLengthProbabilityPairComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluatorForward;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;

/**
 * ************************************************************* Uses a LabeledHashTreeInstance to
 * evaluate a set of sentences and identify the sequences of tokens (phrases) that should be
 * annotated.
 *
 * @author Shrimalini Jayaramaraja modified by Glenn Gobbel June 3, 2012
 *         ************************************************************
 */
public class ProbabilisticSequenceIDEvaluator extends SequenceIDEvaluator {

  protected static final PhraseLengthProbabilityPairComparator phraseSorter =
      new PhraseLengthProbabilityPairComparator(true, true);
  private static final StartOffsetPhraseComparator sentenceSorter =
      new StartOffsetPhraseComparator();
  protected LabeledHashTree theLabeledTree;
  protected double sequenceProbabilityThreshold =
      RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT;


  /**
   * @author Shrimalini Jayaramaraja
   * @param theLabeledHashTree
   */
  public ProbabilisticSequenceIDEvaluator(LabeledHashTree theLabeledHashTree) {
    super();
    this.theLabeledTree = theLabeledHashTree;
    this.theLabeledTree.updateProbabilities();
  }


  public ProbabilisticSequenceIDEvaluator(LabeledHashTree theLabeledHashTree,
      double sequenceThreshold) {
    this(theLabeledHashTree);
    this.sequenceProbabilityThreshold = sequenceThreshold;
  }


  @Override
  public List<List<RaptatToken>> getSentenceAnnotations(List<RaptatToken> sentenceTokens) {
    LinkedList<RaptatPair<List<RaptatToken>, Double>> sentenceProbabilitiesList =
        new LinkedList<>();
    LinkedList<List<RaptatToken>> sequencesForAnnotation = new LinkedList<>();

    for (int startTokenIndex = 0; startTokenIndex < sentenceTokens.size(); startTokenIndex++) {
      List<RaptatToken> subSeqTokens = new LinkedList<>();
      double sequenceProb = 1.0;

      /*
       * for every sentence from document, the likelihood of all combinations are checked and the
       * ones with >SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT are placed in the map. Note that
       * sequenceProb is returned as -1 if there is no tree corresponding the sequence, so we don't
       * need to search any further for phrases beginning with curStartToken
       */
      for (int endTokenIndex = startTokenIndex + 1; endTokenIndex <= sentenceTokens.size()
          && endTokenIndex - startTokenIndex <= RaptatConstants.MAX_TOKENS_DEFAULT
          && sequenceProb >= 0; endTokenIndex++) {
        subSeqTokens = sentenceTokens.subList(startTokenIndex, endTokenIndex);
        sequenceProb = this.theLabeledTree.getSequenceProbability(subSeqTokens);

        if (sequenceProb >= this.sequenceProbabilityThreshold) {
          if (SequenceIDEvaluator.logger.isDebugEnabled()) {
            for (int i = 0; i < subSeqTokens.size(); i++) {
              System.out.print(subSeqTokens.get(i).getTokenStringAugmented() + " ");
            }
            System.out.println(":" + sequenceProb);
          }

          sentenceProbabilitiesList.add(new RaptatPair<>(subSeqTokens, sequenceProb));
        }
      }
    }
    if (sentenceProbabilitiesList.size() > 0) {
      sequencesForAnnotation.addAll(getMostProbableSequences(sentenceProbabilitiesList,
          ProbabilisticSequenceIDEvaluator.phraseSorter));
    }

    return sequencesForAnnotation;
  }


  /**
   * ************************************************************************* Precondition: Tokens
   * within each sentence are already sorted by offset from low to high or high to low, depending on
   * whether the user wants to use inverted token sequences.
   *
   * @author Shrimalini Jayaramaraja, Modified by Glenn Gobbel on June 3, 2012
   * @param docSentences
   * @return List of phrases taken within a set of sentences that should be annotated based on
   *         likelihood within a LabeledHashTree instance
   *         *************************************************************************
   */
  @Override
  public List<List<RaptatToken>> getSequencesForAnnotation(List<AnnotatedPhrase> docSentences) {
    LinkedList<List<RaptatToken>> sequencesForAnnotation = new LinkedList<>();

    SequenceIDEvaluator.logger.debug("\n------ Document Sequence Probabilities -------");
    Collections.sort(docSentences, ProbabilisticSequenceIDEvaluator.sentenceSorter);
    for (AnnotatedPhrase sentence : docSentences) {
      sequencesForAnnotation.addAll(getSentenceAnnotations(sentence.getProcessedTokens()));
    }

    return sequencesForAnnotation;
  }


  /**
   * @param threshold
   */
  public void setSequenceThreshold(double threshold) {
    this.sequenceProbabilityThreshold = threshold;
  }


  @Override
  protected OverlapEvaluator getOverlapEvaluator() {
    if (this.overlapEvaluator != null) {
      return this.overlapEvaluator;
    }

    return new OverlapEvaluatorForward();
  }
}
