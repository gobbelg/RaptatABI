/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/** @author vhatvhsahas1 */
public class UncertaintyBoundaryMarker implements BoundaryMarker {

  /** */
  private static final long serialVersionUID = 1005641198050210391L;

  private static UncertaintyBoundaryMarker uncMarkerInstance = null;

  private static final PhraseTree uncertainPhraseTree = initializeContextPhrases();


  @Override
  public RaptatPair<Mark[], Boolean> markContextBoundaries(List<RaptatToken> theTokens) {
    String[] tokensAsStrings = new String[theTokens.size()];

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
    Arrays.fill(contextMarks, UncertaintyMark.DefaultMark.NONE);

    /*
     * Note that the order of the values of NegationMark.Marker is critical as we first mark PSEUDO,
     * then PRE, then TERM, then POST
     */
    boolean uncMarkerFound = false;
    markContext(tokenStrings, contextMarks, UncertaintyMark.ContextMark.PSEUDO);
    uncMarkerFound = markContext(tokenStrings, contextMarks, UncertaintyMark.ContextMark.PRE);
    markContext(tokenStrings, contextMarks, UncertaintyMark.ContextMark.TERM);
    uncMarkerFound |= markContext(tokenStrings, contextMarks, UncertaintyMark.ContextMark.POST);
    uncMarkerFound |= markContext(tokenStrings, contextMarks, UncertaintyMark.ContextMark.PREPOST);

    return new RaptatPair<>(contextMarks, uncMarkerFound);
  }


  private boolean markContext(String[] tokens, Mark[] curTokenMarks, Mark markToFind) {
    boolean markFound = false;

    for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
      /*
       * Only consider Marks that have not already been tagged and therefore are currently marked as
       * NONE
       */
      if (curTokenMarks[tokenIndex] == Mark.DefaultMark.NONE) {
        int foundPhraseLength = UncertaintyBoundaryMarker.uncertainPhraseTree
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


  public static synchronized UncertaintyBoundaryMarker getInstance() {
    if (UncertaintyBoundaryMarker.uncMarkerInstance == null) {
      UncertaintyBoundaryMarker.uncMarkerInstance = new UncertaintyBoundaryMarker();
    }

    return UncertaintyBoundaryMarker.uncMarkerInstance;
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
   * come before the phrase are uncertain.
   *
   * @return List of string arrays (phrases) that negate the tokens that precede the phrase
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildPostList() {
    String[] stringList = {"?", "be a potential", "is a potential", "be believed", "been belived",
        "is believed", "was believed", "be called for", "been called for", "be considered",
        "been considered", "is considered", "was cosidered", "be entertained", "been entertained",
        "is entertained", "was entertained", "are estimated", "be estimated", "been estimated",
        "is estimated", "was estimated", "were estimated", "are felt", "be felt", "been felt",
        "is felt", "are implied", "be implied", "been implied", "is implied", "was implied",
        "were implied", "are needed", "be needed", "been needed", "is needed", "be possibility",
        "been possibility", "is possibility", "was possibility", "are postulated", "be postulated",
        "been postulated", "is postulated", "was postulated", "were postulated", "be potential",
        "been potential", "is potential", "was potential", "be potentially", "been potentially",
        "is potentially", "was potentially", "be predicted", "been predicted", "was predicted",
        "are presumed", "be presumed", "been presumed", "is presumed", "were presumed",
        "are probable", "be probable", "been probable", "is probable", "was probable",
        "were probable", "are proposed", "be proposed", "been proposed", "is proposed",
        "was proposed", "were proposed", "are putative", "be putative", "been putative",
        "is putative", "was putative", "were putative", "be questioned", "been questioned",
        "is questioned", "was questioned", "be risk", "been risk", "is risk", "was risk",
        "risk from", "are risked", "be risked", "been risked", "is risked", "was risked",
        "were risked", "are suggested", "be suggested", "been suggested", "were suggested",
        "was suggested", "is suggested", "are supposed", "be supposed", "been supposed",
        "were supposed", "was supposed", "is supposed", "are suspected", "be suspected",
        "been suspected", "were suspected", "was suspected", "is suspected", "are thought",
        "be thought", "been thought", "are certain", "be certain", "been certain", "is certain",
        "was certain", "were certain", "are unclear", "be unclear", "been unclear", "is unclear",
        "was unclear", "were unclear", "are unknown", "be unknown", "been unknown", "is unknown",
        "was unknown", "were unknown", "are likely", "be likely", "been likely", "is likely",
        "was likely", "were likely", "are unsure", "be unsure", "been unsure", "is unsure",
        "was unsure", "were unsure"};

    return buildList(stringList);
  }


  /**
   * Method Description - Create a list of phrases that indicate that one or more of the words that
   * come after the phrase are uncertain.
   *
   * @return List of string arrays that negate the tokens that follow
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildPreList() {
    String[] stringList = {"almost no", "apparent", "apparently", "appears", "assess for",
        "assessment for", "believe", "believed", "believes", "believing", "call for", "can",
        "check for", "checked for", "checking for", "checks for", "compatible with", "concern for",
        "concerned for", "concerning for", "concerns for", "consider", "consideration",
        "considered", "considering", "considers", "consistent with", "correlate with",
        "correlated with", "correlates with", "correlating with", "correlation with", "could",
        "ddx", "differential diagnoses", "differential diagnosis", "doubt", "doubted", "doubtful",
        "doubting", "doubts", "either", "entertain", "entertained", "entertaining", "entertains",
        "equivocal for", "estimate", "estimated", "estimates", "estimating", "eval for",
        "evaluate for", "evaluated for", "evaluates for", "evaluating for", "evaluation for",
        "evidence for", "evidence of", "feel that", "feeling that", "feels that", "felt that",
        "hypotheses", "hypothesis", "hypothesize", "hypothesized", "hypothesizes", "hypothesezing",
        "hypothetical", "implication", "implications", "implied", "implies", "imply", "implying",
        "impression of", "improbable", "indication of", "indications of", "indicative of",
        "instructions for", "likely", "may", "maybe", "might", "monitor for", "monitored for",
        "monitoring for", "monitors for", "needs", "not certain about", "not certain if",
        "not certaing whether", "not necessarily", "or", "perhaps", "poss", "possibilities",
        "possibility", "possible", "possibly", "postulate", "postulated", "postulates",
        "postulating", "potential", "potentially", "predict", "predictive of", "predicts",
        "presumably", "presumed", "prevention of", "probabilities", "probability", "probable",
        "probably", "propose", "proposed", "proposes", "proposing", "putative", "question",
        "questionable", "questioned", "questioning", "questions", "reportedly", "representation of",
        "representative of", "return for", "risk", "risk for", "risk of", "risk stratification for",
        "risking", "seem", "seemed", "seeming", "seemingly", "seems", "seriologies for",
        "seriology for", "should", "should he experience", "should she experience",
        "should the patient experience", "sign of", "signs of", "speculate", "speculates",
        "speculated", "speculating", "speculation", "suggest", "sueggested", "suggesting",
        "suggestion", "suggesions", "suggestive", "suggestive of", "suggests", "suppose",
        "supposed", "supposedly", "supposes", "supposing", "suspect", "suspected", "suspecting",
        "suspects", "suspicion", "suspicions", "suspicions for", "t / c", "test for", "tested for",
        "testing for", "tests for", "think", "thinking", "thinks", "is thought", "thought",
        "was thought", "were thought", "till", "til", "typical of", "uncertain", "unclear",
        "unknown", "unlikely", "unsure", "until", "watch for", "watch her for", "watch him for",
        "watch patient for", "whether", "whether or not", "work up for", "workup for",
        "worrisome for", "would"};

    return buildList(stringList);
  }


  private static List<String[]> buildPrePostList() {
    String[] stringList = {"as neccesary for", "as needed for", "if", "in the event that",
        "p . r . n .", "prm", "unless", "v . s .", "versus", "vs", "vs ."};

    return buildList(stringList);
  }


  /**
   * Method Description - Create a list of phrases that are "pseudo" uncertain phrases. That is, the
   * tokens in the phrase seem to indicate that something is being negated, but these tokens are
   * <b><u>not</u></b> actually negating.
   *
   * @return List of string arrays that are "pseudo-negating"
   * @author Glenn Gobbel - Jul 17, 2014
   */
  private static List<String[]> buildPseudoList() {
    String[] stringList = {"cause likely", "cause likely d / t", "cause likely due to",
        "cause likely from", "cause likely to be", "etiology likely", "etiology likely d / t",
        "etiology likely due to", "etiology likely from", "etiology likely to be", "reason likely",
        "reason likely d / t", "reason likely due to"};

    return buildList(stringList);
  }


  /**
   * Method Description - Create a list of phrases that indicate that the limit of the window of
   * influence of a "pre" uncertainty indicator or "post" uncertainty indicator has been reached.
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

    contextPhraseTree.addPhrases(buildPseudoList(), UncertaintyMark.ContextMark.PSEUDO);
    contextPhraseTree.addPhrases(buildPreList(), UncertaintyMark.ContextMark.PRE);
    contextPhraseTree.addPhrases(buildPostList(), UncertaintyMark.ContextMark.POST);
    contextPhraseTree.addPhrases(buildTerminalList(), UncertaintyMark.ContextMark.TERM);
    contextPhraseTree.addPhrases(buildPrePostList(), UncertaintyMark.ContextMark.PREPOST);

    return contextPhraseTree;
  }
}
