package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.reasonnomeds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public final class ReasonNoMedsBoundaryMarker implements BoundaryMarker {
  /** */
  private static final long serialVersionUID = 7032987450264852474L;

  private static ReasonNoMedsBoundaryMarker reasonNoMedsBoundaryMarkerInstance = null;

  private static final PhraseTree contextPhraseTree = initializeContextPhrases();


  private ReasonNoMedsBoundaryMarker() {}


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
    Arrays.fill(contextMarks, ReasonNoMedsMark.DefaultMark.NONE);

    boolean markerFound = false;

    markerFound = markContext(tokenStrings, contextMarks, ReasonNoMedsMark.ContextMark.PRE);
    markContext(tokenStrings, contextMarks, ReasonNoMedsMark.ContextMark.TERM);
    markerFound |= markContext(tokenStrings, contextMarks, ReasonNoMedsMark.ContextMark.POST);

    return new RaptatPair<>(contextMarks, markerFound);
  }


  private boolean markContext(String[] tokens, Mark[] curTokenMarks, Mark markToFind) {
    boolean markFound = false;
    for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
      // Only consider Marks that are currently marked as NONE
      if (curTokenMarks[tokenIndex] == Mark.DefaultMark.NONE) {
        int foundPhraseLength = ReasonNoMedsBoundaryMarker.contextPhraseTree
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


  public static synchronized ReasonNoMedsBoundaryMarker getInstance() {
    if (ReasonNoMedsBoundaryMarker.reasonNoMedsBoundaryMarkerInstance == null) {
      ReasonNoMedsBoundaryMarker.reasonNoMedsBoundaryMarkerInstance =
          new ReasonNoMedsBoundaryMarker();
    }

    return ReasonNoMedsBoundaryMarker.reasonNoMedsBoundaryMarkerInstance;
  }


  private static List<String[]> buildList(String[] thePhrases) {
    List<String[]> theList = new ArrayList<>();
    for (String curPhrase : thePhrases) {
      theList.add(curPhrase.split(" "));
    }
    return theList;
  }


  private static List<String[]> buildPostList() {
    List<String[]> resultList = new ArrayList<>();
    return resultList;
  }


  private static List<String[]> buildPreList() {
    String[] stringList = {"accupril", "ace", "ace - i", "ace inhibitor", "ace inhibitors", "acei",
        "ace - inhibitor", "ace - inhibitors", "aceis", "acei ' s", "aceon", "altace",
        "angiotensin converting enzyme", "angiotensin converting enzyme inhibitors",
        "angiotensin converting enzymes", "angiotensin ii receptor blocker",
        "angiotensin ii receptor blockers", "angiotensin receptor blocker",
        "angiotensin receptor blockers", "angiotensin-converting enzyme",
        "angiotensinconverting enzyme inhibitor", "angiotensin-converting enzyme inhibitor", "arb",
        "arbs", "atacand", "avapro", "benazepril", "benicar", "candesartan", "cap", "capote",
        "capozide", "captopril", "cozaar", "cozar", "cp", "diova", "diovan", "enalapril",
        "eprosartan", "fos", "fosinopril", "irbesartan", "lexxe", "lis", "lisinopril", "losartan",
        "lotensin", "lotrel", "mavik", "meoxipril", "micardis", "moexipril", "monopril", "nyzaar",
        "olm", "olmesartan", "perindopril", "prinivil", "prinzide", "quinapril", "ramipril",
        "sartans", "tarka", "teczem", "telmisartan", "teveten", "tradolapril", "trandolapril",
        "uniretic", "univasc", "valsartan", "vasereti", "vasotec", "zestoretic", "zestril"};

    return buildList(stringList);
  }


  private static List<String[]> buildTerminalList() {
    List<String[]> resultList = new ArrayList<>();
    return resultList;
  }


  private static PhraseTree initializeContextPhrases() {
    PhraseTree contextPhraseTree = new PhraseTree();

    contextPhraseTree.addPhrases(buildPreList(), ReasonNoMedsMark.ContextMark.PRE);
    contextPhraseTree.addPhrases(buildPostList(), ReasonNoMedsMark.ContextMark.POST);
    contextPhraseTree.addPhrases(buildTerminalList(), ReasonNoMedsMark.ContextMark.TERM);

    return contextPhraseTree;
  }
}
