/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * Determines whether a phrase should be included, typically for some subsequent operation. It does
 * this through the includePhrase(AnnotatedPhrase phrase) method. Any subclasses of this method
 * should override the includePhrase() method to choose specific phrases based on criteria other
 * than tht specified by this class.
 *
 * <p>
 * For this class, a call to includePhrase() will return false for any AnnotatedPhrase instance
 * whose tokens are in two different sentences.
 *
 * @author VHATVHGOBBEG
 */
public class PhraseFilter implements Serializable {
  private static final long serialVersionUID = 4906052307911509657L;

  /*
   * Keeps track of sentences that will be used for training. Only phrases that are in a sentence
   * that is part of the candidateSentenceSet will be included, unless the sentenceSet is null.
   *
   * NOTE: by leaving the candidateSentenceSet set to null, sentences are NOT removed based on a
   * phrase having tokens in two sentences, and cue phrases are NOT excluded based on not being
   * found in the candidateSentenceSet. This is useful when using a PhraseFilter within a
   * SentenceSelectorFilter to find sentences associated with phrases.
   */
  protected Set<AnnotatedPhrase> candidateSentenceSet = null;

  /*
   * Tracks phrases that have been run through includePhrase method with a result of true.
   */
  protected Set<AnnotatedPhrase> includedPhrases = new HashSet<>();


  /**
   * Removes all sentences from the sentenceSet that contain one of the tokens in the
   * AnnotatedPhrase parameter, 'phrase'
   *
   * @param phrase
   */
  public void excludeTokenSentence(AnnotatedPhrase phrase) {
    List<RaptatToken> tokenList = phrase.getProcessedTokens();
    if (tokenList != null && !tokenList.isEmpty()) {
      Set<AnnotatedPhrase> tokenSentenceSet = new HashSet<>();
      for (RaptatToken curToken : tokenList) {
        tokenSentenceSet.add(curToken.getSentenceOfOrigin());
      }
      for (AnnotatedPhrase curSentence : tokenSentenceSet) {
        this.candidateSentenceSet.remove(curSentence);
      }
    }
  }


  /** @return the candidateSentenceSet */
  public Set<AnnotatedPhrase> getCandidateSentenceSet() {
    return this.candidateSentenceSet;
  }


  /** @return the includedPhrases */
  public Set<AnnotatedPhrase> getIncludedPhrases() {
    return this.includedPhrases;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding. labelingfilters
   * .phrase.PhraseFilter#includePhrase(src.main.gov.va.vha09.grecc
   * .raptat.gg.datastructures.AnnotatedPhrase)
   */
  public boolean includePhrase(AnnotatedPhrase phrase) {
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


  /** Removes the phrases that have been added to includedPhrases */
  public void resetIncludedPhrases() {
    this.includedPhrases.clear();
  }


  public void setCandidateSentences(Set<AnnotatedPhrase> sentenceSet) {
    this.candidateSentenceSet = sentenceSet;
  }


  protected boolean checkSentencePhraseTokenOverlap(AnnotatedPhrase phrase) {
    /*
     * Check if all tokens in phrase are from same sentence and, if not, exclude the connected
     * sentences from the candidateSentenceSet
     */
    try {
      if (!equivalentTokenSentence(phrase)) {
        if (this.candidateSentenceSet != null) {
          excludeTokenSentence(phrase);
        }
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      GeneralHelper.errorWriter(e.toString());
      System.exit(-1);
    }

    /*
     * Then check if phraseTokens are in a sentence within the existing candidateSentenceSet (if one
     * exists, otherwise return true and include it).
     */
    if (this.candidateSentenceSet == null || this.candidateSentenceSet
        .contains(phrase.getProcessedTokens().get(0).getSentenceOfOrigin())) {
      return true;
    }
    return false;
  }


  /**
   * Check whether all tokens in a phrase are from the same sentence.
   *
   * @param tokens
   * @return
   * @throws Exception
   */
  protected boolean equivalentTokenSentence(AnnotatedPhrase phrase) throws Exception {
    List<RaptatToken> phraseTokens = phrase.getProcessedTokens();

    /*
     * We do not throw an error here because there may be a valid reason for not having phrase
     * tokens - specifically that the phrase may consist of stop words that were removed
     */
    if (phraseTokens == null || phraseTokens.isEmpty()) {
      System.err.println("No processed tokens to assess whether to include phrase:" + phrase);
      return false;
    }
    if (phraseTokens.size() == 1) {
      return true;
    }

    Iterator<RaptatToken> phraseTokenIterator = phraseTokens.iterator();
    AnnotatedPhrase startSentence = phraseTokenIterator.next().getSentenceOfOrigin();

    while (phraseTokenIterator.hasNext()) {
      AnnotatedPhrase curSentence = phraseTokenIterator.next().getSentenceOfOrigin();
      if (!curSentence.equals(startSentence)) {
        return false;
      }
    }
    return true;
  }
}
