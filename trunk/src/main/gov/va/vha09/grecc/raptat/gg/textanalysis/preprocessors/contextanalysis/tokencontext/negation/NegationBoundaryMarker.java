package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.negation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class NegationBoundaryMarker implements BoundaryMarker {
  /** */
  private static final long serialVersionUID = 792444820909620302L;

  private static NegationBoundaryMarker negMarkerInstance = null;

  private static final PhraseTree negationPhraseTree = initializeContextPhrases();


  private NegationBoundaryMarker() {}


  @Override
  public RaptatPair<Mark[], Boolean> markContextBoundaries(List<RaptatToken> theTokens) {
    String[] tokensAsStrings = new String[theTokens.size()];

    // tokensAsStrings = (String []) theTokens.toArray();

    int i = 0;
    for (RaptatToken curToken : theTokens) {
      tokensAsStrings[i++] = curToken.getTokenStringPreprocessed();
    }

    return this.markContextBoundaries(tokensAsStrings);
  }


  @Override
  public RaptatPair<Mark[], Boolean> markContextBoundaries(String[] tokenStrings) {
    int tokenListSize = tokenStrings.length;
    Mark[] contextMarks = new Mark[tokenListSize];
    Arrays.fill(contextMarks, NegationMark.DefaultMark.NONE);

    /*
     * Note that the order of the values of NegationMark.Marker is critical as we first mark PSEUDO,
     * then PRE, then TERM, then POST
     */
    boolean negMarkerFound = false;
    markContext(tokenStrings, contextMarks, NegationMark.ContextMark.PSEUDO);
    negMarkerFound = markContext(tokenStrings, contextMarks, NegationMark.ContextMark.PRE);
    markContext(tokenStrings, contextMarks, NegationMark.ContextMark.TERM);
    negMarkerFound |= markContext(tokenStrings, contextMarks, NegationMark.ContextMark.POST);

    return new RaptatPair<>(contextMarks, negMarkerFound);
  }


  /**
   * Method Description - Analyzes each token in an array of tokens and marks them if the phrase
   * correspond to a certain type of mark, "markToFind", is found
   *
   * @param tokens
   * @param curTokenMarks
   * @param markToFind
   * @return
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private boolean markContext(String[] tokens, Mark[] curTokenMarks, Mark markToFind) {
    boolean markFound = false;
    for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
      /*
       * Only consider Marks that have not already been tagged and therefore are currently marked as
       * NONE
       */
      if (curTokenMarks[tokenIndex] == Mark.DefaultMark.NONE) {
        int foundPhraseLength = NegationBoundaryMarker.negationPhraseTree
            .phraseLengthAtIndexIgnoreCase(tokens, tokenIndex, markToFind);
        if (foundPhraseLength > 0) {
          markFound = true;
          for (int j = tokenIndex; j < tokenIndex + foundPhraseLength; j++) {
            // Stop if we reach a mark that has already been set
            // to something other than NONE
            if (curTokenMarks[j] != Mark.DefaultMark.NONE) {
              break;
            }
            curTokenMarks[j] = markToFind;
          }
        }
      }
    }
    return markFound;
  }


  public static synchronized NegationBoundaryMarker getInstance() {
    if (NegationBoundaryMarker.negMarkerInstance == null) {
      NegationBoundaryMarker.negMarkerInstance = new NegationBoundaryMarker();
    }

    return NegationBoundaryMarker.negMarkerInstance;
  }


  private static List<String[]> buildList(String[] thePhrases) {
    List<String[]> theList = new ArrayList<>();
    for (String curPhrase : thePhrases) {
      theList.add(curPhrase.split(" "));
    }
    return theList;
  }


  /**
   * Method Description - Create a list of phrases that indicate that one or more of the words that
   * come before the phrase are negated.
   *
   * @return List of string arrays (phrases) that negate the tokens that precede the phrase
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildPostList() {
    String[] stringList = {"are ruled out", "be ruled out", "being ruled out", "can be ruled out",
        "was ruled out", "could be ruled out", "free", "has been negative", "has been ruled out",
        "have been ruled out", "is ruled out", "is to be ruled out", "may be ruled out",
        "might be ruled out", "must be ruled out", "no longer present", "non diagnostic",
        "now resolved", "ought to be ruled out", "prophylaxis", "should be ruled out", "unlikely",
        "was negative", "was ruled out", "will be ruled out", "be discontinued",
        "has been discontinued", "have been discontinued", "has been stopped", "have been stopped",
        "is being stopped", "are being stopped",

        /*
         * The following were added by Glenn on 02-06-2016 based on initial performance of the CRF
         * module finding cues in the AKI training annotations
         */
        "? no", ": no", ": none", ": [no]", ": neg", "? n", "?n", ":no", "none", ":hold", ": hold",
        "negative", "neg", ": never", ": not", ": quit", ": resolved", ":never", ":not", ":quit",
        ":resolved", "was held", "will be held", "had been held", "had been stopped",
        "should have been held", "may be held", "would have been held", "had been discontinued",
        "had been ruled out", "should be held", "should be discontined", "should be stopped",
        "will be stopped", "has resolved", "has been resolved", "should be resolved",
        "will be resolved", "was replaced", "replaced with", "will be replaced",
        "should be replaced", "have been replaced", "have been replaced", "was stopped",
        "was resolved", "was discontinued", "was held", "was ruled out", "was negative",
        "instead of", "rather than", "was avoided", "has been avoided", "will be avoided",
        "should be avoided", "must be avoided", "was refused", "has been refused",
        "will be refused", "have been refused", "may be refused", "should be refused",
        "would be refused"};

    return buildList(stringList);
  }


  /**
   * Method Description - Create a list of phrases that indicate that one or more of the words that
   * come after the phrase are negated.
   *
   * @return List of string arrays that negate the tokens that follow
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildPreList() {
    String[] stringList = {"absence of", "adequate to rule her out", "adequate to rule her out for",
        "adequate to rule him out", "adequate to rule him out for", "adequate to rule out",
        "adequate to rule out for", "adequate to rule the patient out",
        "adequate to rule the patient out against", "adequate to rule the patient out for",
        "allergies", "allergies :", "allergies / adverse reactions",
        "allergies / adverse reactions :", "allergies / ADR", "allergies / ADR :", "any other",
        "as well as any", "can rule her out", "can rule her out against", "can rule her out for",
        "can rule him out", "can rule him out against", "can rule him out for", "can rule out",
        "can rule out against", "can rule out for", "can rule the patient out",
        "can rule the patient out against", "can rule the patient out for",
        "can rule the patinet out against", "can rule the patinet out for", "cannot", "cannot see",
        "checked for", "clear of", "declined", "declines", "denied", "denies", "denying",
        "did rule her out", "did rule her out against", "did rule her out for", "did rule him out",
        "did rule him out against", "did rule him out for", "did rule out", "did rule out against",
        "did rule out for", "did rule the patient out", "did rule the patient out against",
        "did rule the patient out for", "doesn't look like", "evaluate for", "fails to reveal",
        "free of", "inconsistent with", "is not", "isn't", "lack of", "lacked", "negative for",
        "never developed", "never had", "no", "no abnormal", "no cause of", "no complaints of",
        "no evidence", "no evidence to suggest", "no findings of", "no finding of",
        "no findings to indicate", "no history of", "no mammographic evidence of", "no new",
        "no new evidence", "no other evidence", "no radiographic evidence of", "no sign of",
        "no significant", "no signs of", "no suggestion of", "no suspicious", "not", "not appear",
        "not appreciate", "not associated with", "not complain of", "not demonstrate",
        "not exhibit", "not feel", "not had", "not have", "not have evidence of", "not know of",
        "not known to have", "not reveal", "not see", "not to be", "patient was not", "r/o",
        "rather than", "resolved", "ro", "rule her out", "rule her out for", "rule him out",
        "rule him out for", "rule out", "rule out for", "rule the patient out",
        "rule the patient out for", "ruled her out", "ruled her out against", "ruled her out for",
        "ruled him out", "ruled him out against", "ruled him out for", "ruled out",
        "ruled out against", "ruled out for", "ruled the patient out",
        "ruled the patient out against", "ruled the patient out for", "rules her out",
        "rules her out for", "rules him out", "rules him out for", "rules out", "rules out for",
        "rules the patient out", "rules the patient out for", "ruling out", "ruling her out for",
        "ruling him out for", "sufficient to rule her out", "sufficient to rule her out against",
        "sufficient to rule her out for", "sufficient to rule him out",
        "sufficient to rule him out against", "sufficient to rule him out for",
        "sufficient to rule out", "sufficient to rule out against", "sufficient to rule out for",
        "sufficient to rule the patient out", "sufficient to rule the patient out against",
        "sufficient to rule the patient out for", "test for", "to exclude", "unremarkable for",
        "was not", "wasn't", "what must be ruled out is", "with no", "without",
        "without any evidence of", "without evidence", "without indication of", "without sign of",
        "adverse", "discontinue", "discontinued", "deny", "discontinuing", "declining", "lacking",
        "excluding"

        /*
         * The following were added by Glenn on 02-06-2016 based on initial performance of the CRF
         * module finding cues in the AKI training annotations
         */
        , "non-", "without", "non", "stop:", "(-)", "[-]", "cannot", "hold for", "negative for",
        "stopping", "stopped", "stop", "hold", "holding", "held", "ruled out", "instead of",
        "in place of", "in lieu of", "replace", "replaced", "hold off", "off", "off of", "replaces",
        "substituted for", "substituting for", "substitute for", "avoids", "avoid", "avoided",
        "avoiding", "is replacing", "was replacing", "was stopping", "was holding",
        "was ruling out", "was discontinuing", "excluded", "refuse", "refuses", "refused",
        "refusing", "was refusing", "has been refusing"};

    return buildList(stringList);
  }


  /**
   * Method Description - Create a list of phrases that are "pseudo" negation phrases. That is, the
   * tokens in the phrase seem to indicate that something is being negated, but these tokens are
   * <b><u>not</u></b> actually negating.
   *
   * @return List of string arrays that are "pseudo-negating"
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildPseudoList() {
    String[] stringList = {"gram negative", "no change", "no definite change", "no increase",
        "no interval change", "no significant change", "no significant interval change",
        "no suspicious change", "not cause", "not certain if", "not certain whether", "not drain",
        "not extend", "not necessarily", "not on", "not only", "not significant interval change",
        "without difficulty",

        /*
         * The following were added by Glenn on 3-19-14. They would probably be better characterized
         * as 'double negatives," but they essentially do the same thing as pseudo negatives and are
         * handled the same way, so we put them here. They are derived from some of the negative
         * phrases, and we have added "not" or a form of "not" to create these phrases.
         */
        "can not rule her out", "can not rule her out against", "can not rule her out for",
        "can not rule him out", "can not rule him out against", "can not rule him out for",
        "can not rule out", "can not rule out against", "can not rule out for",
        "can not rule the patient out", "can not rule the patient out against",
        "can not rule the patient out for", "can not rule the patinet out against",
        "can not rule the patient out for", "cannot rule her out", "cannot rule her out against",
        "cannot rule her out for", "cannot rule him out", "cannot rule him out against",
        "cannot rule him out for", "cannot rule out", "cannot rule out against",
        "cannot rule out for", "cannot rule the patient out", "cannot rule the patient out against",
        "cannot rule the patient out for", "cannot rule the patinet out against",
        "cannot rule the patinet out for", "did not rule her out", "did not rule her out against",
        "did not rule her out for", "did not rule him out", "did not rule him out against",
        "did not rule him out for", "did not rule out", "did not rule out against",
        "did not rule out for", "did not rule the patient out",
        "did not rule the patient out against", "did not rule the patient out for",
        "did not rule the patinet out against", "did not rule the patient out for",
        "inadequate to rule her out", "inadequate to rule her out for",
        "inadequate to rule him out", "inadequate to rule him out for", "inadequate to rule out",
        "inadequate to rule out for", "inadequate to rule the patient out",
        "inadequate to rule the patient out against", "inadequate to rule the patient out for",
        "insufficient to rule her out", "insufficient to rule her out against",
        "insufficient to rule her out for", "insufficient to rule him out",
        "insufficient to rule him out against", "insufficient to rule him out for",
        "insufficient to rule out", "insufficient to rule out against",
        "insufficient to rule out for", "insufficient to rule the patient out",
        "insufficient to rule the patient out against", "insufficient to rule the patient out for",
        "insufficient to rule the patinet out against", "insufficient to rule the patinet out for",
        "no absence of", "not sufficient to rule her out", "not sufficient to rule her out against",
        "not sufficient to rule her out for", "not sufficient to rule him out",
        "not sufficient torule him out against", "not sufficient to rule him out for",
        "not sufficient torule out", "no sufficient torule out against",
        "not sufficient to rule out for", "not sufficient torule the patient out",
        "not sufficient to rule the patient out against",
        "not sufficient to rule the patient out for",
        "not sufficient to rule the patient out against",
        "not sufficient to rule the patinet out for", "nor any other",
        "not adequate to rule her out", "not adequate to rule her out for",
        "not adequate to rule him out", "not adequate to rule him out for",
        "not adequate to rule out", "not adequate to rule out for",
        "not adequate to rule the patient out", "not adequate to rule the patient out against",
        "not adequate to rule the patient out for", "unable to rule her out",
        "unable to rule her out against", "unable to rule her out for", "unable to rule him out",
        "unable to rule him out against", "unable to rule him out for", "unable to rule out",
        "unable to rule out against", "unable to rule out for", "unable to rule the patient out",
        "unable to rule the patient out against", "unable to rule the patient out for",
        "unable to rule the patinet out against", "unable to rule the patinet out for"};

    return buildList(stringList);
  }


  /**
   * Method Description - Create a list of phrases that indicate that the limit of the window of
   * influence of a "pre" negator or "post" negator has been reached.
   *
   * @return List of string arrays (phrases) that indicate a limiting boundary of a negation window.
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildTerminalList() {
    String[] stringList = {"although", "apart from", "as a cause for", "as a cause of",
        "as a etiology for", "as a etiology of", "as a reason for", "as a reason of",
        "as a secondary cause for", "as a secondary cause of", "as a secondary etiology for",
        "as a secondary etiology of", "as a secondary origin for", "as a secondary origin of",
        "as a secondary reason for", "as a secondary reason of", "as a secondary source for",
        "as a secondary source of", "as a source for", "as a source of", "as an cause for",
        "as an cause of", "as an etiology for", "as an etiology of", "as an origin for",
        "as an origin of", "as an reason for", "as an reason of", "as an secondary cause for",
        "as an secondary cause of", "as an secondary etiology for", "as an secondary etiology of",
        "as an secondary origin for", "as an secondary origin of", "as an secondary reason for",
        "as an secondary reason of", "as an secondary source for", "as an secondary source of",
        "as an source for", "as an source of", "as has", "as the cause for", "as the cause of",
        "as the etiology for", "as the etiology of", "as the origin for", "as the origin of",
        "as the reason for", "as the reason of", "as the secondary cause for",
        "as the secondary cause of", "as the secondary etiology for",
        "as the secondary etiology of", "as the secondary origin for", "as the secondary origin of",
        "as the secondary reason for", "as the secondary reason of", "as the secondary source for",
        "as the secondary source of", "as the source for", "as the source of", "aside from", "but",
        "cause for", "cause of", "causes for", "causes of", "etiology for", "etiology of", "except",
        "however", "nevertheless", "origin for", "origin of", "origins for", "origins of",
        "other possibilities of", "reason for", "reason of", "reasons for", "reasons of",
        "secondary", "secondary to", "source for", "source of", "sources for", "sources of",
        "still", "though", "trigger event for", "yet"};

    return buildList(stringList);
  }


  private static PhraseTree initializeContextPhrases() {
    PhraseTree contextPhraseTree = new PhraseTree();

    contextPhraseTree.addPhrases(buildPseudoList(), NegationMark.ContextMark.PSEUDO);
    contextPhraseTree.addPhrases(buildPreList(), NegationMark.ContextMark.PRE);
    contextPhraseTree.addPhrases(buildPostList(), NegationMark.ContextMark.POST);
    contextPhraseTree.addPhrases(buildTerminalList(), NegationMark.ContextMark.TERM);

    return contextPhraseTree;
  }
}
