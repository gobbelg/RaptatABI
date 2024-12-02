package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;

/**
 * Provides ability to select only phrases that map to the concept AssertionScope.
 *
 * <p>
 * Phrases <u>NOT</u> selected by this class are those that 1) do not link to a cue in the same
 * sentence, 2) are not in the same sentence as one in the candidateSentenceSet, 3) do not have
 * tokens all in the same sentence, and those that 4) link to a cue whose tokens are not all in the
 * same sentence.
 *
 * @author VHATVHGOBBEG
 */
public class NegativeAssertionScopeFilter extends PhraseFilter {
  /** */
  private static final long serialVersionUID = -6745743090954589935L;

  /*
   * cueIDSet maps the id of AnnotatedPhrase instances that are cues to the AnnotatedPhrase instance
   * itself
   */
  private Map<String, AnnotatedPhrase> cueIDSet = null;

  private Set<AnnotatedPhrase> associatedCues = new HashSet<>();


  /**
   * Generate a HashMap from cue phrase ID to the phrase itself. This is used to get the cue phrase
   * associated with an assertion scope phrase based on the conceptRelations field of the scope
   * phrase. The cueIDSet can be useful in determining if cue and associated scope are in the same
   * sentence after sentence splitting by a TextAnalyzer instance.
   *
   * @param cueScopeAnnotations
   */
  public void createCueAnnotationSet(List<AnnotatedPhrase> cueScopeAnnotations) {
    this.cueIDSet = new HashMap<>();

    /*
     * Create a NegativeAssertionCueFilter that will call includePhrase to both identify cue phrases
     * that are Negative assertion cues and to remove sentences from the candidateSentenceSet for
     * which the cue crosses a sentence boundary
     */
    NegativeAssertionCueFilter negativeCueFilter = new NegativeAssertionCueFilter();
    negativeCueFilter.candidateSentenceSet = this.candidateSentenceSet;
    for (AnnotatedPhrase curPhrase : cueScopeAnnotations) {
      if (negativeCueFilter.includePhrase(curPhrase)) {
        this.cueIDSet.put(curPhrase.getMentionId(), curPhrase);
      }
    }
  }


  /** @return the associatedCues */
  public Set<AnnotatedPhrase> getAssociatedCues() {
    return this.associatedCues;
  }


  /**
   * This only includes phrases who 1) have the concept "AssertionScope", 2) whose tokens are all in
   * one sentence, 3) that link to cues within the same sentence, and 4) that are in sentences in
   * the candidateSentenceSet. <br>
   * <br>
   * <b>NOTE 1:</b> This method stores phrases that return true into the includedPhrases field of
   * the class instance and it stores the associated cue phrases into the field, associatedCues.
   * <br>
   * <br>
   * <b>NOTE 2:</b> A side effect of a call to this method is that sentences in the
   * candidateSentenceSet that have scopes and cues that cross sentence boundaries are removed from
   * the candidateSentenceSet.
   */
  @Override
  public boolean includePhrase(AnnotatedPhrase scopePhraseCandidate) {
    if (this.cueIDSet == null) {
      System.err.println(
          "cueIDSet is null; set using createCueAnnotationSet() before calling includePhrase()");
    }
    if (scopePhraseCandidate.getConceptName().equalsIgnoreCase("AssertionScope")) {
      /* Reject scope phrases that have no relation to a cue phrase */
      List<ConceptRelation> conceptRelations = scopePhraseCandidate.getConceptRelations();
      if (conceptRelations == null || conceptRelations.isEmpty()) {
        return false;
      }

      for (ConceptRelation curRelation : conceptRelations) {
        String curID = curRelation.getRelatedAnnotationID();

        AnnotatedPhrase cuePhrase;
        if ((cuePhrase = this.cueIDSet.get(curID)) != null) {
          /*
           * Check if all tokens in phrase variable are from same sentence. If the answer is no,
           * exclude any associated sentences from candidateSentenceSet.
           */
          if (checkSentencePhraseTokenOverlap(scopePhraseCandidate)) {
            AnnotatedPhrase scopeSentence =
                scopePhraseCandidate.getProcessedTokens().get(0).getSentenceOfOrigin();
            AnnotatedPhrase cueSentence =
                cuePhrase.getProcessedTokens().get(0).getSentenceOfOrigin();

            /*
             * Make sure cue and scope are from same sentence. If not, do not add to includedPhrases
             * or associatedCues and exclude the sentences that contain them.
             */
            if (cueSentence.equals(scopeSentence)) {
              this.includedPhrases.add(scopePhraseCandidate);
              this.associatedCues.add(cuePhrase);
              return true;
            } else {
              this.candidateSentenceSet.remove(cueSentence);
              this.candidateSentenceSet.remove(scopeSentence);
            }
          }

          /*
           * If the phrase is not fully contained within the candidateSentenceSet, also exclude
           * sentences in the candidateSentenceSet associated with the cue related to the phrase
           */
          else if (this.candidateSentenceSet != null) {
            excludeTokenSentence(cuePhrase);
          }
        }
      }
    }
    return false;
  }


  public void resetAssociatedCues() {
    this.associatedCues.clear();
  }
}
