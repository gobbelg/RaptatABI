package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelers;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Adds labels as tags to a phrase. Generally, the tags would be added as suffixes (e.g. "_TAG")
 * within the taggedTokenString field of the tokens that make up an AnnotatedPhrase instance.
 *
 * @author VHATVHGOBBEG
 *
 */
public interface Labeler {
  void tag(AnnotatedPhrase ap);
}
