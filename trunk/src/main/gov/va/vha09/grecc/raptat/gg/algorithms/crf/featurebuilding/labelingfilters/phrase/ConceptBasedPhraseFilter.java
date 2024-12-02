package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Extends the PhraseFilter class and overrides the includePhrase method. In this class,
 * includePhrase returns false when provided with any AnnotatedPhrase instance whose concept name is
 * not one of the Strings in the string set 'concepts,' one of the fields of this class.
 */
public class ConceptBasedPhraseFilter extends PhraseFilter {
  /** */
  private static final long serialVersionUID = 2844450975382118847L;

  private final Set<String> concepts = new HashSet<>();


  public ConceptBasedPhraseFilter() {}


  public ConceptBasedPhraseFilter(final Collection<String> concepts) {
    this.concepts.addAll(concepts);
  }


  public ConceptBasedPhraseFilter(final String conceptName) {
    this.concepts.add(conceptName);
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding. labelingfilters
   * .phrase.PhraseFilter#includePhrase(src.main.gov.va.vha09.grecc
   * .raptat.gg.datastructures.AnnotatedPhrase)
   */
  @Override
  public boolean includePhrase(final AnnotatedPhrase phrase) {
    if (super.includePhrase(phrase) && this.concepts.contains(phrase.getConceptName())) {
      return true;
    }
    return false;
  }
}
