package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * Class to determine whether a phrase has the Attribute "AssertionStatus." The includePhrase()
 * method returns false when the AnnotatedPhrase parameter instance, phrase, does not have all its
 * tokens in the same sentence AND does not have an AssertionStatus attribute.
 *
 * @author Glenn Gobbel
 */
public class AssertionStatusFilter extends PhraseFilter {
  /** */
  private static final long serialVersionUID = -511628159735344365L;


  @Override
  public boolean includePhrase(AnnotatedPhrase phrase) {
    /* First check if all tokens in phrase are from same sentence */
    try {
      if (!equivalentTokenSentence(phrase)) {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      GeneralHelper.errorWriter(e.toString());
      System.exit(-1);
    }

    /* Make sure phrase has the correct attribute */
    for (RaptatAttribute curAttribute : phrase.getPhraseAttributes()) {
      if (curAttribute.getName().equalsIgnoreCase("assertionstatus")) {
        return true;
      }
    }
    return false;
  }
}
