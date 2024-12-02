/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Takes a list of out sentences from a candidate set of sentences that contain a phrase that is not
 * included by the phraseFilter field instance. Note that the field candidateSentenceSet is modified
 * when the selectSentences() method is run.
 *
 * @author Glenn T. Gobbel Apr 20, 2016
 */
public class ExclusionFilter extends PhraseBasedSentenceFilter {
  public ExclusionFilter(PhraseFilter phraseFilter, List<AnnotatedPhrase> candidateSentences) {
    super(phraseFilter);
    phraseFilter.setCandidateSentences(new HashSet<>(candidateSentences));
  }


  @Override
  public Set<AnnotatedPhrase> selectSentences(List<AnnotatedPhrase> phrases) {
    for (AnnotatedPhrase curPhrase : phrases) {
      if (this.phraseFilter != null && !this.phraseFilter.includePhrase(curPhrase)) {
        this.phraseFilter.excludeTokenSentence(curPhrase);
      }
    }
    return this.phraseFilter.getCandidateSentenceSet();
  }
}
