package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptPhraseModifier;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.general.GeneralBoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.general.GeneralTokenMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.negation.NegationBoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.negation.NegationTokenMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.reasonnomeds.ReasonNoMedsBoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.reasonnomeds.ReasonNoMedsTokenMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty.UncertaintyBoundaryMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty.UncertaintyTokenMarker;

/**
 * TokenContextAnalyzer objects uses rules like those in Negex to determine the context of a token.
 * For example, they can identify tokens that are negated or uncertain or part of a reason for
 * someone not being on medication.
 *
 * @author Glenn Gobbel - Jun 3, 2013
 */
public class TokenContextAnalyzer implements Serializable {

  private static final Logger logger = Logger.getLogger(TokenContextAnalyzer.class);

  private static final long serialVersionUID = -8732239638154877596L;


  private BoundaryMarker contextBoundaryMarker;

  private Mark[] contextMarks;

  /*
   * Glenn comment - 10/22/15 - It's not clear why there is a new class added instead of making
   * GenBoundaryMarker implement the BoundaryMarker interface. If that doesn't work, we should
   * probably reconsider the interface design or create a separate class.
   */
  private GeneralBoundaryMarker genContextBoundaryMarker = null;

  private TokenMarker tokenContextMarker;

  public TokenContextAnalyzer(ContextType theType) {
    this();
    switch (theType) {
      case NEGATION:
        this.contextBoundaryMarker = NegationBoundaryMarker.getInstance();
        this.tokenContextMarker =
            new NegationTokenMarker(RaptatConstants.NEGATION_WINDOW_SIZE_DEFAULT);
        break;

      case REASON_NOT_ON_MEDS:
        this.contextBoundaryMarker = ReasonNoMedsBoundaryMarker.getInstance();
        this.tokenContextMarker =
            new ReasonNoMedsTokenMarker(RaptatConstants.REASON_NO_MEDS_WINDOW_SIZE_DEFAULT);
        break;

      case UNCERTAINTY:
        this.contextBoundaryMarker = UncertaintyBoundaryMarker.getInstance();
        this.tokenContextMarker =
            new UncertaintyTokenMarker(RaptatConstants.UNCERTAIN_WINDOW_SIZE_DEFAULT);
        break;

      default:
        break;
    }
  }


  public TokenContextAnalyzer(String dictionaryPath, HashSet<String> acceptedConcepts) {
    this();

    if (TokenContextAnalyzer.logger.isInfoEnabled()) {
      System.out.println("Reading in dictionary");
    }
    this.genContextBoundaryMarker = new GeneralBoundaryMarker(dictionaryPath, acceptedConcepts);
    if (TokenContextAnalyzer.logger.isInfoEnabled()) {
      System.out.println("Completed reading of dictionary");
    }
    this.tokenContextMarker = new GeneralTokenMarker();
  }


  private TokenContextAnalyzer() {
    TokenContextAnalyzer.logger.setLevel(Level.INFO);
  }


  /**
   * Takes an array of strings that represent a sentence or phrase and produces an array of tags
   * that represent the context of each of the strings based on the other strings in that sentence.
   * The method assumes that the strings in the tokenStrings array are in their natural order (i.e.,
   * the order they appear in the sentence from first to last).
   *
   * @param tokenStrings
   * @param tokenContextTag
   * @return Returns array of tags for each of the strings in the tokenStrings array.
   */
  public String[] getContext(String[] tokenStrings, String tokenContextTag) {
    RaptatPair<Mark[], Boolean> markingResults =
        this.contextBoundaryMarker.markContextBoundaries(tokenStrings);

    /*
     * Only mark the context if we found something, as indicated by markingResults.right, that would
     * require us to change the token marks from their default values
     */
    if (markingResults.right) {
      this.contextMarks = markingResults.left;
      return this.tokenContextMarker.getContext(tokenStrings, markingResults.left, tokenContextTag);
    }

    String[] noContextResults = new String[tokenStrings.length];
    Arrays.fill(noContextResults, "");

    return noContextResults;
  }


  /**
   * Takes an array of strings that represent a sentence or phrase and produces an array of tags
   * that represent the context of each of the strings based on the other strings in that sentence.
   * The method assumes that the strings in the tokenStrings array are in their natural order (i.e.,
   * the order they appear in the sentence from first to last).
   *
   * @param tokenStrings
   * @param tokenContextTag
   * @return Returns array of tags for each of the strings in the tokenStrings array.
   */
  public RaptatPair<String[], Mark[]> getContextAndContextMarks(String[] tokenStrings,
      String tokenContextTag) {
    RaptatPair<Mark[], Boolean> markingResults =
        this.contextBoundaryMarker.markContextBoundaries(tokenStrings);

    // Only mark the context if we found something that would require
    // us to change the token marks from their default values
    if (markingResults.right) {
      this.contextMarks = markingResults.left;
      String[] context =
          this.tokenContextMarker.getContext(tokenStrings, markingResults.left, tokenContextTag);
      return new RaptatPair<>(context, markingResults.left);
    }

    String[] noContextResults = new String[tokenStrings.length];
    Arrays.fill(noContextResults, "");

    return new RaptatPair<>(noContextResults, markingResults.left);
  }


  public String[] getContextCue(String[] tokenStrings) {
    RaptatPair<Mark[], Boolean> markingResults =
        this.contextBoundaryMarker.markContextBoundaries(tokenStrings);

    this.contextMarks = markingResults.left;
    String cue[] = new String[this.contextMarks.length];

    try {
      for (int i = 0; i < this.contextMarks.length; i++) {
        cue[i] = this.contextMarks[i].toString();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return cue;
  }


  public GeneralBoundaryMarker getGenContextBoundaryMarker() {
    return this.genContextBoundaryMarker;
  }


  public List<AnnotatedPhrase> getGroupConcepts(AnnotationGroup group) {
    List<AnnotatedPhrase> groupSentences = group.getRaptatDocument().getActiveSentences();
    List<AnnotatedPhrase> groupAnnotations = new ArrayList<>();
    for (AnnotatedPhrase curSentence : groupSentences) {
      List<AnnotatedPhrase> curSentenceAnnotations = this.getTokenConcepts(curSentence);
      groupAnnotations.addAll(curSentenceAnnotations);
    }
    return groupAnnotations;
  }


  public List<AnnotatedPhrase> getSentenceAnnotations(AnnotatedPhrase sentence) {
    List<AnnotatedPhrase> sentenceAnnotations = new ArrayList<>();
    String[] tokenStrings = sentence.getRawTokensPreprocessedStrings().toArray(new String[0]);
    boolean hasAssignedConcept[] = new boolean[tokenStrings.length];
    Arrays.fill(hasAssignedConcept, false);

    /*
     * We want to know the order of concepts as this establishes precedence in terms of labeling
     * phrases as dictionary-based annotations. Once assigned, the fact that a token has been
     * assigned is stored in the array 'hasAssignedConcept'.
     */
    List<String> concepts = new ArrayList<>(this.genContextBoundaryMarker.getConceptList());
    Collections.sort(concepts);

    /*
     * We're iterating multiple times through each sentence, once for every concept. It should be
     * possible to increase the efficiency of this using a single PhraseTree instance to map all
     * phrases in sentence to concepts and their blockers and modifiers.
     */
    for (String conceptName : concepts) {
      ConceptPhraseModifier[] modifierWindows = new ConceptPhraseModifier[tokenStrings.length];

      /*
       * Glenn comment - 10/22/15 - After calling this, the array of marks, this.contextMarks, is
       * equal in length to the tokenStrings array, and every mark indicates if the corresponding
       * token string at the same index is a facilitator or a blocker of the concept being currently
       * analyzed, conceptName. The modifierWindows array, passed as a parameter,stores the range of
       * influence, upstream and downstream separately, for each token.
       *
       * Note that the impact of the facilitator or blocker is not distributed in this call but in
       * the method getContextGeneral(). It seems that this code labels everything as a facilitator
       * within window +/- one token from the token under consideration.
       */
      this.contextMarks =
          this.genContextBoundaryMarker.getContextMarks(tokenStrings, modifierWindows, conceptName);

      /*
       * The call to getContextMarks above determined if a token string was part of a facilitator or
       * blocker and, if so, the window of influence.
       *
       * This next call determines if that string and those in the window of influence are actually
       * facilitated or blocked. This is returned in a pair of string arrays that indicate, for each
       * string in tokenStrings array, whether the corresponding string in tokensStrings is
       * facilitated or blocked with respect to the concept, conceptName.
       *
       * Glenn comment - 10/23/15 - I don't think this call is necessary if all the marks are set to
       * "NONE". There should be a check to skip this call. Ideally, the call to getContextMarks
       * would return a boolean indicating this.
       */
      RaptatPair<String[], String[]> facilitatedBlockedTokens = this.tokenContextMarker
          .getGeneralContext(tokenStrings, this.contextMarks, modifierWindows);

      String[] facilitatedTokens = facilitatedBlockedTokens.left;
      String[] blockedTokens = facilitatedBlockedTokens.right;

      /*
       * Glenn comment - 10/23/15 -The marked parameter will be changed if a concept is found in the
       * tokenStrings sent with this method call, so marked keeps track, as we go through concepts,
       * of which token strings have already been assigned to concepts. The String [] result object
       * will contain the concept names corresponding to the tokenStrings in the tokenStrings []
       * object. It will also indicate strings that are part of phrases that facilitate or block a
       * concept assignment to another string in the sentence.
       */
      List<AnnotatedPhrase> conceptAnnotations =
          this.genContextBoundaryMarker.getSentenceAnnotations(sentence.getRawTokens(),
              facilitatedTokens, blockedTokens, tokenStrings, conceptName, hasAssignedConcept);
      sentence.addSentenceAssociatedConcepts(conceptAnnotations);
      sentenceAnnotations.addAll(conceptAnnotations);
    }
    return sentenceAnnotations;
  }


  public List<AnnotatedPhrase> getTokenConcepts(AnnotatedPhrase sentence) {
    List<AnnotatedPhrase> sentenceAnnotations = new ArrayList<>();

    /*
     * markedFacilitatedConcepts and markedBlockedConcepts are used in the findAnnotations method.
     * They keep track of whether facilitators and blockers for a concept have already been
     * determined for sentence.
     */
    HashSet<String> markedFacilitatedConcepts = new HashSet<>();
    HashSet<String> markedBlockedConcepts = new HashSet<>();

    List<RaptatToken> sentenceTokens = sentence.getProcessedTokens();
    List<List<RaptatToken>> unannotatedSequences = findUnannotatedSequences(sentenceTokens);
    for (List<RaptatToken> curTokenSequence : unannotatedSequences) {
      List<AnnotatedPhrase> sequenceAnnotations = getTokenSequenceAnnotations(curTokenSequence,
          sentenceTokens, markedFacilitatedConcepts, markedBlockedConcepts);
      sentenceAnnotations.addAll(sequenceAnnotations);
    }
    return sentenceAnnotations;
  }


  public List<AnnotatedPhrase> getTokenConcepts(AnnotationGroup group) {
    List<AnnotatedPhrase> resultList = new ArrayList<>();
    for (AnnotatedPhrase sentence : group.getRaptatDocument().getActiveSentences()) {
      List<AnnotatedPhrase> sentenceAnnotations = this.getTokenConcepts(sentence);
      resultList.addAll(sentenceAnnotations);
    }

    return resultList;
  }


  public void setTokenConcepts(List<RaptatToken> sentenceTokens) {
    boolean hasAssignedConcept[] = new boolean[sentenceTokens.size()];
    Arrays.fill(hasAssignedConcept, false);

    Set<String> concepts = this.genContextBoundaryMarker.getConceptList();

    String[] tokenStrings = new String[sentenceTokens.size()];
    for (int i = 0; i < sentenceTokens.size(); i++) {
      tokenStrings[i] = sentenceTokens.get(i).getTokenStringPreprocessed();
    }

    for (String conceptName : concepts) {
      ConceptPhraseModifier[] modifierWindows = new ConceptPhraseModifier[tokenStrings.length];

      /*
       * Glenn comment - 10/22/15 - After calling this, the array of marks, this.contextMarks, is
       * equal in length to the tokenStrings array, and every mark indicates if the corresponding
       * token string at the same index is a facilitator or a blocker of a particular concept,
       * conceptName. The windows for the effective window size for the facilitators and blockers is
       * stored in windows.
       *
       * Note that the impact of the facilitator or blocker is not distributed in this call but in
       * the method getContextGeneral(). It seems that this code label everything as a facilitator
       * within window +/- one token from the token under consideration. The windows array, passed
       * as a parameter,stores the range of influence, upstream and downstream separately, for the
       * token.
       */
      this.contextMarks =
          this.genContextBoundaryMarker.getContextMarks(tokenStrings, modifierWindows, conceptName);

      /*
       * The call to getContextMarks above determined if a string was part of a facilitator or
       * blocker and, if so, the window of influence. This next call determines if that string and
       * those in the window of influence are actually facilitated or blocked. This is returned in a
       * pair of string arrays that indicate, for each string in tokenStrings array, whether the
       * corresponding string in tokensStrings is facilitated or blocked with respect to the
       * concept, conceptName.
       *
       * Glenn comment - 10/23/15 - I don't think this call is necessary if all the marks are set to
       * "NONE". There should be a check to skip this call. Ideally, the call to getContextMarks
       * would return a boolean indicating this.
       */
      RaptatPair<String[], String[]> facilitatedBlockedTokens = this.tokenContextMarker
          .getGeneralContext(tokenStrings, this.contextMarks, modifierWindows);

      String[] facilitatedTokens = facilitatedBlockedTokens.left;
      String[] blockedTokens = facilitatedBlockedTokens.right;

      /*
       * Glenn comment - 10/23/15 -The 'hasAssignedConcept' parameter will be changed if a concept
       * is found in the tokenStrings sent with this method call, so 'hasAssignedConcept' keeps
       * track, as we go through the concepts that are part of the dictionary, of which token
       * strings have already been assigned to concepts. The String [] result object will contain
       * the concept names corresponding to the tokenStrings in the tokenStrings [] object. It will
       * also indicate strings that are part of phrases that facilitate or block a concept
       * assignment to another string in the sentence.
       */
      String[] result = this.genContextBoundaryMarker.detectConcepts(facilitatedTokens,
          blockedTokens, tokenStrings, conceptName, hasAssignedConcept);

      setConceptDetailsForTokens(sentenceTokens, result);
    }
  }


  public void writeTrainingFiles(String fileName, String text) {
    File f = new File("data\\CRFTrainingFiles\\" + fileName);

    try {
      BufferedWriter output = new BufferedWriter(new FileWriter(f));
      output.write(text);
      output.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Takes a sequence of tokens and finds subsequences of tokens within that sequence that have not
   * already been assigned to phrases that were annotated by Raptat and assigned to a concept.
   *
   * @param tokenSequence
   * @return
   */
  private List<List<RaptatToken>> findUnannotatedSequences(List<RaptatToken> tokenSequence) {
    List<List<RaptatToken>> resultSequences = new ArrayList<>();
    Iterator<RaptatToken> sequenceIterator = tokenSequence.iterator();
    while (sequenceIterator.hasNext()) {
      List<RaptatToken> curSequence = new ArrayList<>();
      RaptatToken curToken;
      while (sequenceIterator.hasNext()
          && !(curToken = sequenceIterator.next()).raptatConceptAssigned()) {
        curSequence.add(curToken);
      }
      if (curSequence.size() > 0) {
        resultSequences.add(curSequence);
      }
    }

    return resultSequences;
  }


  /**
   * Finds the phrases that should be annotated according to the dictionary created for the
   * genContextBoundaryMarker. </br>
   * <b>PRECONDITION:</b> None of the tokens in the sequence are part of a phrase selected for
   * annotation by RapTAT.
   *
   * @param tokenSequence
   * @param sentenceTokens
   * @param markedBlockedConcepts
   * @param markedFacilitatedConcepts
   * @return
   */
  private List<AnnotatedPhrase> getTokenSequenceAnnotations(List<RaptatToken> tokenSequence,
      List<RaptatToken> sentenceTokens, HashSet<String> markedFacilitatedConcepts,
      HashSet<String> markedBlockedConcepts) {
    List<AnnotatedPhrase> sequenceAnnotations = this.genContextBoundaryMarker.findAnnotations(
        tokenSequence, sentenceTokens, markedFacilitatedConcepts, markedBlockedConcepts);
    if (!sequenceAnnotations.isEmpty()) {
      List<List<RaptatToken>> subsequences = findUnannotatedSequences(tokenSequence);
      for (List<RaptatToken> curSequence : subsequences) {
        sequenceAnnotations.addAll(getTokenSequenceAnnotations(curSequence, sentenceTokens,
            markedFacilitatedConcepts, markedBlockedConcepts));
      }
    }
    return sequenceAnnotations;
  }


  /*
   * Glenn comment - 10/23/15 -This method takes the tokens for which concepts, facilitators, or
   * blockers were found and assigns them to fields of the token. This is contrary to the general
   * design of RapTAT where there are AnnotatedPhrase objects that have concepts and the tokens
   * corresponding to those concepts.
   *
   * It's not clear what this modification, of having a token associated with a concept does, but a
   * downside is that it could allow for inconsistencies between what concept is assigned to a token
   * and what concept is assigned to the annotated phrase associated with that token. Another
   * potential problem is that, if one token within a phrase is already assigned to a concept, the
   * other tokens that are not assigned to that assigned can still be assigned to the concept found
   * in the facilitator blocker code. Rather than assigning a concept to the token here, it would
   * probably be better to generate a list of annotated phrases with the tokens attached. Then these
   * dictionary-based annotated phrases can be compared to Raptat-generated annotations, and the
   * dictionary-based ones that have tokens in common with the annotated phrases can be eliminated.
   */
  private void setConceptDetailsForTokens(List<RaptatToken> tokens, String[] result) {
    for (int i = 0; i < result.length; i++) {
      if (result[i].length() > 0) {
        String[] resTokens = result[i].split("\\s");

        if (resTokens[0].equals(RaptatConstants.FACILITATED_TAG)) {
          tokens.get(i).addFacilitator(resTokens[1]);
        } else if (resTokens[0].equals(RaptatConstants.BLOCKED_TAG)) {
          tokens.get(i).addBlocker(resTokens[1]);
        } else {
          if (!tokens.get(i).raptatConceptAssigned()) {
            tokens.get(i).addConcept(result[i]);
            tokens.get(i).setRaptatConceptAssigned(true);
          }
        }
      }
    }
  }


  public static void main(String[] args) throws Exception {}
}
