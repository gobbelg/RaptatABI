/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * Used to identify and exclude (return false in response to includePhrase(AnnotatedPhrase) )
 * phrases whose tokens are not all in the same sentence.
 *
 * @author Glenn T. Gobbel Apr 20, 2016
 */
public class BasicPhraseFilter extends PhraseFilter {

  /** */
  private static final long serialVersionUID = 7438064927918043188L;


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding. labelingfilters
   * .phrase.PhraseFilter#includePhrase(src.main.gov.va.vha09.grecc
   * .raptat.gg.datastructures.AnnotatedPhrase)
   */
  @Override
  public boolean includePhrase(final AnnotatedPhrase phrase) {
    try {
      if (!equivalentTokenSentence(phrase)) {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      GeneralHelper.errorWriter(e.toString());
      System.exit(-1);
    }
    return true;
  }
}
