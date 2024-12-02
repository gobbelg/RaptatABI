package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Level;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluatorReverse;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ReverseStartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;

/**
 * ****************************************************** A subclass of TokenSequenceEvaluator. This
 * class evaluates text in reverse token sequence order (last to first) to identify sequences for
 * annotation.
 *
 * @author Glenn Gobbel - Jul 8, 2012 *****************************************************
 */
public class ReverseProbabilisticSequenceIDEvaluator extends ProbabilisticSequenceIDEvaluator {
  private static final ReverseStartOffsetTokenComparator tokenSorter =
      new ReverseStartOffsetTokenComparator();


  public ReverseProbabilisticSequenceIDEvaluator(LabeledHashTree aLabeledHashTree) {
    super(aLabeledHashTree);
  }


  public ReverseProbabilisticSequenceIDEvaluator(LabeledHashTree aLabeledHashTree,
      double sequenceThreshold) {
    super(aLabeledHashTree, sequenceThreshold);
  }


  @Override
  public List<List<RaptatToken>> getSentenceAnnotations(List<RaptatToken> sentenceTokens) {
    Collections.sort(sentenceTokens, ReverseProbabilisticSequenceIDEvaluator.tokenSorter);
    return super.getSentenceAnnotations(sentenceTokens);
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
    SequenceIDEvaluator.logger.setLevel(Level.INFO);
    List<RaptatToken> sentenceTokens;
    LinkedList<List<RaptatToken>> sequencesForAnnotation = new LinkedList<>();

    Collections.sort(docSentences, new StartOffsetPhraseComparator());
    SequenceIDEvaluator.logger.debug("\n------ Document Sequence Probabilities -------");
    for (AnnotatedPhrase sentence : docSentences) {
      sentenceTokens = sentence.getProcessedTokens();
      sequencesForAnnotation.addAll(getSentenceAnnotations(sentenceTokens));
    }
    return sequencesForAnnotation;
  }


  @Override
  protected OverlapEvaluator getOverlapEvaluator() {
    if (this.overlapEvaluator != null) {
      return this.overlapEvaluator;
    }

    return new OverlapEvaluatorReverse();
  }
}
