package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * This is a dummy filter that allows inclusion of all phrases. Used to keep the method calls
 * consistent in terms of structure.
 *
 * @author VHATVHGOBBEG
 */
public class PassThroughFilter extends PhraseFilter {

  /** */
  private static final long serialVersionUID = -4217070278041930754L;


  @Override
  public boolean includePhrase(AnnotatedPhrase phrase) {
    return true;
  }
}
