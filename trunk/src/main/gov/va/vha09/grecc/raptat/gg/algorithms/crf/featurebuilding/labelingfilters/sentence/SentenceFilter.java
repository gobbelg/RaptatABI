package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence;

import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Selects sentences to include for CRF training based on a PhraseFilter instance and the annotated
 * phrases supplied as a parameter to the selectSentences() methods.
 *
 * @author VHATVHGOBBEG
 */
public abstract class SentenceFilter {
  protected PhraseFilter phraseFilter;


  protected SentenceFilter() {
    this(null);
  }


  protected SentenceFilter(PhraseFilter phraseFilter) {
    this.phraseFilter = phraseFilter;
  }


  public abstract Set<AnnotatedPhrase> selectSentences(List<AnnotatedPhrase> phrases);


  public abstract Set<AnnotatedPhrase> selectSentences(Set<AnnotatedPhrase> phrases);
}
