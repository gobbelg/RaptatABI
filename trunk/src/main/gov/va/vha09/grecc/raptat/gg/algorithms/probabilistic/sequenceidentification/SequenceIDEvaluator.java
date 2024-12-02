package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OverlapEvaluator;

public abstract class SequenceIDEvaluator {
  protected static Logger logger = Logger.getLogger(SequenceIDEvaluator.class);

  // This in an object to determine whether two token sequences overlap; it
  // needs to be implemented differently depending on whether we are using
  // phrases (OverlapEvaluatorForward vs OverlapEvaluatorReverse). Subclasses
  // need to provide the proper overlapEvaluator during construction.
  protected OverlapEvaluator overlapEvaluator = null;


  public SequenceIDEvaluator() {
    SequenceIDEvaluator.logger.setLevel(Level.INFO);
    this.overlapEvaluator = getOverlapEvaluator();
  }


  public abstract List<List<RaptatToken>> getSentenceAnnotations(List<RaptatToken> sentenceTokens);


  public abstract List<List<RaptatToken>> getSequencesForAnnotation(
      List<AnnotatedPhrase> docSentences);


  /**
   * Greedy method to pull out the longest and most probable annotations within a sentence so none
   * of the annotations overlap. We first pull out the longest followed by the most probable
   * sequence if two are equal in length and overlap.
   *
   * @param sentenceProbabilitiesList
   * @return List of phrases that should be annotated from a single sentence
   * @author Glenn Gobbel - Jun 3, 2012
   */
  protected List<List<RaptatToken>> getMostProbableSequences(
      LinkedList<RaptatPair<List<RaptatToken>, Double>> phraseList,
      PhraseLengthProbabilityPairComparator phraseSorter) {
    Collections.sort(phraseList, phraseSorter);
    List<List<RaptatToken>> resultList = new LinkedList<>();
    List<RaptatToken> curPhrase;
    RaptatPair<List<RaptatToken>, Double> curPair;
    ListIterator<RaptatPair<List<RaptatToken>, Double>> iterator;
    while (phraseList.size() > 0) {
      curPair = phraseList.removeFirst();
      curPhrase = curPair.left;

      // We need to generate a new array list to hold the tokens
      // as they were formed by sublist, which just gives a view
      // into an existing list.
      resultList.add(new ArrayList<>(curPhrase));
      SequenceIDEvaluator.logger.debug("Add sequence:" + Arrays.toString(curPhrase.toArray()));

      iterator = phraseList.listIterator();
      while (iterator.hasNext()) {
        if (this.overlapEvaluator.areOverlapped(curPhrase, iterator.next().left)) {
          iterator.remove();
        }
      }
    }
    return resultList;
  }


  protected abstract OverlapEvaluator getOverlapEvaluator();
}
