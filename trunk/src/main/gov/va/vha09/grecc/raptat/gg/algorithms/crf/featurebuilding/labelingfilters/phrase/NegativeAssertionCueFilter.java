package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;

/**
 * Checks whether an AnnotatedPhrase object is a negative cue phrase. Phrases whose tokens are not
 * in a single sentence or whose tokens are not in the candidateSentenceSet (unless the set is null)
 * are excluded.
 *
 * @author Glenn Gobbel
 */
public class NegativeAssertionCueFilter extends PhraseFilter {

  /** */
  private static final long serialVersionUID = 4869209110672476744L;


  public Set<AnnotatedPhrase> getSentences() {
    return this.candidateSentenceSet;
  }


  @Override
  public boolean includePhrase(AnnotatedPhrase phrase) {
    if (phrase.getConceptName().equalsIgnoreCase("AssertionCue")) {
      List<RaptatAttribute> attributes = phrase.getPhraseAttributes();
      for (RaptatAttribute curAttribute : attributes) {
        if (curAttribute.getName().equalsIgnoreCase("assertioncuetype")) {
          List<String> values = curAttribute.getValues();
          for (String curValue : values) {
            if (curValue.equalsIgnoreCase("negative")) {
              if (checkSentencePhraseTokenOverlap(phrase)) {
                this.includedPhrases.add(phrase);
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
}
