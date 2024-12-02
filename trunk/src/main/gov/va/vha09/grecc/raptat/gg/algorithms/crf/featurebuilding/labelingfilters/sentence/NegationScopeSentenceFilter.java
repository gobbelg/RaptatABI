package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence;

import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.NegativeAssertionScopeFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Extends the PhraseBasedSentenceFilter class by only using a ScopeFilter as the phraseFilter field
 * and providing access to fields within the ScopeFilter instance object. When the selectSentence()
 * method of an instance of this class is run on a list of all annotatedPhrases associated with a
 * RapTAT document, this instance will not only return the sentences to include for negation scope
 * training but also store the correct negation scope annotations and negation cue annotations to
 * use for training.
 *
 * @author VHATVHGOBBEG
 */
public class NegationScopeSentenceFilter extends PhraseBasedSentenceFilter {
  /*
   * Tracks whether the method selectSentences() was called - if not, getValidCuePhrases() should
   * not be called and will report an error as the cuePhrases are only generated and valid once
   * createCueAnnotationsSet() and selectSentences() have both been called.
   */
  private boolean selectSentencesCalled = false;

  /*
   * Tracks whether createCueAnnotationSet() was called. If createCueAnnotationSet() AND
   * selectSentences() were not both called, getCuePhrases() should not be called and will report an
   * error as the cuePhrases are only generated and valid once both have been called.
   */
  private boolean createCuesCalled = false;


  public NegationScopeSentenceFilter() {
    super();
    this.phraseFilter = new NegativeAssertionScopeFilter();
  }


  /**
   * Generate a HashMap from cue phrase ID to the phrase itself. This is used by the instance to
   * determine which phrases to include
   *
   * @param cueScopeAnnotations
   */
  public void createCueAnnotationSet(List<AnnotatedPhrase> cueScopeAnnotations) {
    reset();
    ((NegativeAssertionScopeFilter) this.phraseFilter).createCueAnnotationSet(cueScopeAnnotations);
    this.createCuesCalled = true;
  }


  /**
   * Allow users to access the cues associated with a scope filter.
   *
   * @return the associatedCues
   */
  public Set<AnnotatedPhrase> getValidCuePhrases() {
    if (!this.createCuesCalled) {
      System.err.println("Create cue annotation should be set via "
          + "createCueAnnotationSet() before calling getCuePhrases()");
    }
    if (!this.selectSentencesCalled) {
      System.err.println(
          "Cue phrases may not be valid as selectSentences() has not been previously called");
    }
    return ((NegativeAssertionScopeFilter) this.phraseFilter).getAssociatedCues();
  }


  public Set<AnnotatedPhrase> getValidScopePhrases() {
    return ((NegativeAssertionScopeFilter) this.phraseFilter).getIncludedPhrases();
  }


  public void reset() {
    this.phraseFilter.resetIncludedPhrases();
    ((NegativeAssertionScopeFilter) this.phraseFilter).resetAssociatedCues();
    this.createCuesCalled = false;
    this.selectSentencesCalled = false;
  }


  @Override
  public Set<AnnotatedPhrase> selectSentences(List<AnnotatedPhrase> phrases) {
    if (!this.createCuesCalled) {
      System.err.println("Create cue annotation should be set via "
          + "createCueAnnotationSet() before calling selectSentences()");
    }
    if (this.selectSentencesCalled) {
      System.err.println("Select sentences already called. The reset() method "
          + "should be called before calling selectSentences() again");
    }
    this.selectSentencesCalled = true;
    return super.selectSentences(phrases);
  }
}
