package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.PhraseLengthProbabilityPairComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluatorForward;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;

public class BOWSequenceIDEvaluator extends SequenceIDEvaluator {
  protected static Logger logger = Logger.getLogger(BOWSequenceIDEvaluator.class);
  private BOWSequenceIDCalculator theCalculator;


  public BOWSequenceIDEvaluator(BOWSequenceIDSolution identifierStructure) {
    this.theCalculator = new BOWSequenceIDCalculator(identifierStructure);
  }


  @Override
  public List<List<RaptatToken>> getSentenceAnnotations(List<RaptatToken> sentenceTokens) {
    // TODO Auto-generated method stub
    return null;
  }


  /**
   * ************************************************************************* Precondition: Tokens
   * within each sentence are already sorted by offset from low to high or high to low, depending on
   * whether the user wants to use inverted token sequences.
   *
   * @author Glenn Gobbel on Sep 18, 2012
   * @param docSentences
   * @return List of phrases taken within a set of sentences that should be annotated based on
   *         likelihood within a LabeledHashTree instance
   *         *************************************************************************
   */
  @Override
  public List<List<RaptatToken>> getSequencesForAnnotation(List<AnnotatedPhrase> docSentences) {
    List<RaptatToken> sentenceTokens;
    LinkedList<RaptatPair<List<RaptatToken>, Double>> sentenceLikelihoodList = new LinkedList<>();
    LinkedList<List<RaptatToken>> sequencesForAnnotation = new LinkedList<>();
    PhraseLengthProbabilityPairComparator phraseSorter =
        new PhraseLengthProbabilityPairComparator(false, true);

    Collections.sort(docSentences, new StartOffsetPhraseComparator());
    for (AnnotatedPhrase curSentence : docSentences) {
      sentenceTokens = curSentence.getProcessedTokensAsSentence();
      ConcurrentHashMap<Integer, HashSet<Integer>> sequencesForEvaluation =
          this.theCalculator.getSequencesForEvaluation(sentenceTokens);
      if (!sequencesForEvaluation.isEmpty()) {
        sentenceLikelihoodList.clear();
        sentenceLikelihoodList.addAll(
            this.theCalculator.getLikelySequences(sequencesForEvaluation, curSentence, 0.0));
        if (sentenceLikelihoodList.size() > 0) {
          BOWSequenceIDEvaluator.logger.info("\nLikely sequences identified\n\n");
          sequencesForAnnotation
              .addAll(getMostProbableSequences(sentenceLikelihoodList, phraseSorter));
        }
      }
    }
    return sequencesForAnnotation;
  }


  @Override
  protected OverlapEvaluator getOverlapEvaluator() {
    if (this.overlapEvaluator != null) {
      return this.overlapEvaluator;
    }

    return new OverlapEvaluatorForward();
  }
}
