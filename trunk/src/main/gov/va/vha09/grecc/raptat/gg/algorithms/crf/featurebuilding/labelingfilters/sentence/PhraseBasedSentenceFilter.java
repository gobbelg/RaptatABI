package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * Filter to select sentences that contain an AnnotatedPhrase instance for which the type of
 * PhraseFilter in the field, phraseFilter, would return true in response to the call
 * includePhrase(thePhrase).
 *
 * @author VHATVHGOBBEG
 */
public class PhraseBasedSentenceFilter extends SentenceFilter {

  public PhraseBasedSentenceFilter() {
    super();
  }


  /**
   * @param phraseFilter
   */
  public PhraseBasedSentenceFilter(PhraseFilter phraseFilter) {
    super(phraseFilter);
  }


  public PhraseFilter getPhraseFilter() {
    return this.phraseFilter;
  }


  @Override
  public Set<AnnotatedPhrase> selectSentences(List<AnnotatedPhrase> phrases) {
    Iterator<AnnotatedPhrase> phraseIterator = phrases.iterator();

    return this.selectSentences(phraseIterator);
  }


  @Override
  public Set<AnnotatedPhrase> selectSentences(Set<AnnotatedPhrase> phrases) {
    Iterator<AnnotatedPhrase> phraseIterator = phrases.iterator();

    return this.selectSentences(phraseIterator);
  }


  private Set<AnnotatedPhrase> selectSentences(Iterator<AnnotatedPhrase> phraseIterator) {
    Set<AnnotatedPhrase> resultSet = new HashSet<>();
    while (phraseIterator.hasNext()) {
      AnnotatedPhrase curPhrase = phraseIterator.next();
      if (this.phraseFilter != null && this.phraseFilter.includePhrase(curPhrase)) {
        List<RaptatToken> tokenList = curPhrase.getProcessedTokens();
        if (!tokenList.isEmpty()) {
          AnnotatedPhrase associatedSentence =
              curPhrase.getProcessedTokens().get(0).getSentenceOfOrigin();
          resultSet.add(associatedSentence);
        }
      }
    }
    return resultSet;
  }
}
