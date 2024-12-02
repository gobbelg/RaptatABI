package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.general;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.Annotator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SortableStringArrayList;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptPhraseModifier;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptPhraseType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class GeneralBoundaryMarker implements Serializable {
  private static final long serialVersionUID = -3167090984663364917L;

  private static Logger logger = Logger.getLogger(GeneralBoundaryMarker.class);


  private final Map<String, PhraseTree> phraseTrees = new HashMap<>();

  /*
   * Glenn Comment - 10/22/15 - There is redundancy in these maps that doesn't seem necessary and is
   * confusing.
   */
  private Map<String, Map<SortableStringArrayList, ConceptPhraseType>> conceptNameToFacilitatorsMap;

  private Map<String, Map<SortableStringArrayList, ConceptPhraseType>> conceptNameToBlockersMap;
  private Map<String, Map<SortableStringArrayList, ConceptPhraseType>> conceptNameToPhrasesMap;

  /**
   * Constructor for the General boundary marker. It uses a single dictionary file for blocker,
   * facilitator and concept phrases.
   *
   * @param dictionaryPath
   * @param acceptedConcepts
   */
  public GeneralBoundaryMarker(String dictionaryPath, HashSet<String> acceptedConcepts) {
    this();

    GeneralBoundaryMarker.logger
        .info("Reading in dictionary for concepts, facilitators, and blockers");
    readDictionary(dictionaryPath, acceptedConcepts);

    if (GeneralBoundaryMarker.logger.isDebugEnabled()) {
      System.out.println("\n===================================\nConcept, Facilitator, "
          + "and Blocker Dictionaries\n===================================\n");
      printDictionaries();
    }

    for (String conceptName : this.conceptNameToPhrasesMap.keySet()) {
      /* Get the dictionary phrases mapping to this concept */
      Map<SortableStringArrayList, ConceptPhraseType> phraseToConceptPhraseMap =
          this.conceptNameToPhrasesMap.get(conceptName);

      /* Get the facilitators associated with this concept */
      Map<SortableStringArrayList, ConceptPhraseType> phraseToFacilitatorPhraseMap;
      if ((phraseToFacilitatorPhraseMap =
          this.conceptNameToFacilitatorsMap.get(conceptName)) == null) {
        phraseToFacilitatorPhraseMap = new HashMap<>();
      }

      /* Get the blockers associated with this concept */
      Map<SortableStringArrayList, ConceptPhraseType> phraseToBlockerPhraseMap;
      if ((phraseToBlockerPhraseMap = this.conceptNameToBlockersMap.get(conceptName)) == null) {
        phraseToBlockerPhraseMap = new HashMap<>();
      }

      /*
       * Create a phrase tree for this specific concept in which all the text phrases mapping to the
       * concept and their associated facilitators and blockers are put into a single PhraseTree
       * object. The end of the phrase is marked by an indicator telling whether it is a mapped
       * phrase, facilitator, or blocker.
       */
      PhraseTree contextPhraseTree = initializeContextPhrases(phraseToConceptPhraseMap,
          phraseToFacilitatorPhraseMap, phraseToBlockerPhraseMap);

      if (GeneralBoundaryMarker.logger.isDebugEnabled()) {
        System.out.print("\n\n\n\n=======================\nConceptName:" + conceptName
            + "\n=======================\n\n");
        contextPhraseTree.print();
      }

      /* Store the PhraseTree object for concept "conceptName" in a map */
      this.phraseTrees.put(conceptName, contextPhraseTree);
    }
  }


  private GeneralBoundaryMarker() {
    GeneralBoundaryMarker.logger.setLevel(Level.INFO);
  }


  /**
   * Generates and returns a string array that indicates tokens within the array that are part of
   * the concept conceptName or are blocked or facilitated for that conceptName.
   *
   * @param facilitatedTokens
   * @param blockedTokens
   * @param tokenStrings
   * @param conceptName
   * @param hasAssignedConcept
   * @return
   */
  public String[] detectConcepts(String[] facilitatedTokens, String[] blockedTokens,
      String[] tokenStrings, String conceptName, boolean[] hasAssignedConcept) {
    int tokenListSize = tokenStrings.length;
    String[] result = new String[tokenListSize];
    Arrays.fill(result, "");

    StringBuilder sb;
    boolean blocked;
    boolean facilitated;
    boolean previouslyMarked; // previously marked as a concept
    ConceptPhrase conceptPhrase;

    for (int tokenIndex = 0; tokenIndex < tokenListSize; tokenIndex++) {

      previouslyMarked = false;

      /*
       * Only consider GenMarks that have not already been tagged and therefore are currently marked
       * as NONE
       */
      int foundPhraseLength = this.phraseTrees.get(conceptName).phraseLengthAtIndexIgnoreCase(
          tokenStrings, tokenIndex, GeneralMarks.ContextMark.CONCEPTWORD);

      /*
       * Glenn comment - 10/22/15 - foundPhraseLength > 0 indicates that there is a phrase mapping
       * to concept 'conceptName' at index tokenIndex
       */
      if (foundPhraseLength > 0) {
        blocked = false;
        facilitated = false;

        sb = new StringBuilder();

        /*
         * Glenn comment - 10/22/15 - In the for loop below, we are putting the tokens into one
         * phrase and determining if any of the tokens in that phrase are blocked, facilitated, or
         * already marked.
         */
        for (int j = tokenIndex; j < tokenIndex + foundPhraseLength; j++) {
          sb.append(tokenStrings[j]).append(' ');

          if (facilitatedTokens[j].equals(RaptatConstants.FACILITATED_TAG)) {
            facilitated = true;
          }
          if (blockedTokens[j].equals(RaptatConstants.BLOCKED_TAG)) {
            blocked = true;
          }
          if (hasAssignedConcept[j]) {
            previouslyMarked = true;
          }
        }

        /*
         * Glenn comment - 10/22/15 - This will retrieve a ConceptPhrase object indicating whether
         * the phrase can be blocked or requires facilitation
         */
        Map<SortableStringArrayList, ConceptPhraseType> phrasesForConcept =
            this.conceptNameToPhrasesMap.get(conceptName);
        conceptPhrase = (ConceptPhrase) phrasesForConcept.get(sb.toString().trim());

        /*
         * Glenn comment - 10/22/15 - It might be slightly more efficient to call a method that sets
         * the values of the result and marked arrays within the if..else blocks
         */
        for (int j = tokenIndex; j < tokenIndex + foundPhraseLength; j++) {
          if (!previouslyMarked) {
            if (blocked) {
              if (!conceptPhrase.blockable) {
                result[j] = conceptName;
                hasAssignedConcept[j] = true;
              } else {
                result[j] = RaptatConstants.BLOCKED_TAG + " " + conceptName;
              }
            } // we are here .. that means it is not blocked
            else if (facilitated) {
              result[j] = conceptName;
              hasAssignedConcept[j] = true;
            }
            /*
             * we are here.. means phrase is neither facilitated nor blocked
             */

            else if (!conceptPhrase.requiresFacilitation) {
              result[j] = conceptName;
              hasAssignedConcept[j] = true;
            }
          }
          /*
           * If we execute the 'else' block, it means that some of tokens in the phrase were
           * previously marked as another concept, We still want to mark the result as to whether
           * the token was blocked or facilitated.
           */
          else {
            if (blockedTokens[tokenIndex].equals(RaptatConstants.BLOCKED_TAG)) {
              result[tokenIndex] = RaptatConstants.BLOCKED_TAG + " " + conceptName;
            } else if (facilitatedTokens[tokenIndex].equals(RaptatConstants.FACILITATED_TAG)) {
              result[tokenIndex] = RaptatConstants.FACILITATED_TAG + " " + conceptName;
            }
          }
        }
      }
      /*
       * Glenn comment - 10/22/15 - Note that hasAssignedConcept[] is not set by these.
       *
       * To execute the 'else' block below, it means that we did not find any phrase in the phrase
       * tree corresponding to the concept of interest given by the parameter conceptName. We set
       * the result array to blocked or facilitated if it's empty.
       */
      else if (result[tokenIndex].equals("")) {
        if (blockedTokens[tokenIndex].equals(RaptatConstants.BLOCKED_TAG)) {
          result[tokenIndex] = RaptatConstants.BLOCKED_TAG + " " + conceptName;
        } else if (facilitatedTokens[tokenIndex].equals(RaptatConstants.FACILITATED_TAG)) {
          result[tokenIndex] = RaptatConstants.FACILITATED_TAG + " " + conceptName;
        }
      }

      /*
       * Skip over tokens that are part of a found phrase that was not previously marked
       */
      if (foundPhraseLength > 1 && !previouslyMarked) {
        tokenIndex += foundPhraseLength - 1;
      }
    }

    return result;
  }


  /**
   * Get all the dictionary-based annotations in the sequence tokenSequence and return as a
   * list.</br>
   * </br>
   * <b>PRECONDITION:</b> <i>tokenSequence</i> must not contain any tokens that are already part of
   * a RapTAT annotated phrase
   *
   * @param tokenSequence
   * @param sentenceTokens
   * @param markedBlockedConcepts
   * @param markedFacilitatedConcepts
   * @return
   */
  public List<AnnotatedPhrase> findAnnotations(List<RaptatToken> tokenSequence,
      List<RaptatToken> sentenceTokens, HashSet<String> markedFacilitatedConcepts,
      HashSet<String> markedBlockedConcepts) {
    List<AnnotatedPhrase> resultPhrases = null;

    String[] tokenStrings = RaptatToken.getPreprocessedTokenStringSequence(tokenSequence);

    /* Go through all concepts in phraseTrees */
    for (String curConcept : this.phraseTrees.keySet()) {
      PhraseTree curTree = this.phraseTrees.get(curConcept);
      for (int tokenIndex = 0; tokenIndex < tokenSequence.size(); tokenIndex++) {
        int foundPhraseLength = curTree.phraseLengthAtIndexIgnoreCase(tokenStrings, tokenIndex,
            GeneralMarks.ContextMark.CONCEPTWORD);

        if (foundPhraseLength > 0) {
          try {
            SortableStringArrayList foundPhrase =
                SortableStringArrayList.buildList(tokenStrings, tokenIndex, foundPhraseLength);
            List<RaptatToken> foundSequence =
                tokenSequence.subList(tokenIndex, tokenIndex + foundPhraseLength);
            ConceptPhrase conceptPhrase =
                (ConceptPhrase) this.conceptNameToPhrasesMap.get(curConcept).get(foundPhrase);
            boolean isFacilitated = true;
            boolean isBlocked = false;
            if (conceptPhrase.requiresFacilitation) {
              isFacilitated = phraseFacilitated(foundSequence, sentenceTokens,
                  markedFacilitatedConcepts.contains(curConcept), curConcept);
            }
            if (conceptPhrase.blockable) {
              isBlocked = phraseBlocked(foundSequence, sentenceTokens,
                  markedBlockedConcepts.contains(curConcept), curConcept);
            }
            if (isFacilitated && !isBlocked) {
              AnnotatedPhrase resultPhrase = createPhrase(foundSequence, curConcept);
              resultPhrases.add(resultPhrase);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    return resultPhrases;
  }


  public Set<String> getConceptList() {
    return this.conceptNameToPhrasesMap.keySet();
  }


  // @Override
  /**
   * This method creates an array of Mark objects that determines whether the strings in the
   * tokenStrings array are part of a facilitator or blocker phrase for the concept, conceptName. If
   * a tokenString is part of a facilitator or blocker, the ConceptPhraseModifier object at the same
   * index in the windows array as the string is set to the ConceptPhraseModifier object
   * corresponding to the facilitator or blocker.
   *
   * @param tokenStrings
   * @param modifierInfluenceWindows
   * @param conceptName
   * @return
   */
  public Mark[] getContextMarks(String[] tokenStrings,
      ConceptPhraseModifier[] modifierInfluenceWindows, String conceptName) {
    PhraseTree phraseTree = this.phraseTrees.get(conceptName);
    Map<SortableStringArrayList, ConceptPhraseType> facilitators =
        this.conceptNameToFacilitatorsMap.get(conceptName);
    Map<SortableStringArrayList, ConceptPhraseType> blockers =
        this.conceptNameToBlockersMap.get(conceptName);

    int tokenListSize = tokenStrings.length;
    Mark[] contextMarks = new Mark[tokenListSize];
    Arrays.fill(contextMarks, GeneralMarks.DefaultMark.NONE);

    setContextMarks(tokenStrings, blockers, phraseTree, contextMarks, modifierInfluenceWindows,
        GeneralMarks.ContextMark.BLOCKER);

    if (facilitators == null) {
      setUnmarkedAsFacilitators(tokenStrings, contextMarks, modifierInfluenceWindows);
    } else {
      setContextMarks(tokenStrings, facilitators, phraseTree, contextMarks,
          modifierInfluenceWindows, GeneralMarks.ContextMark.FACILITATOR);
    }

    return contextMarks;
  }


  public List<AnnotatedPhrase> getSentenceAnnotations(List<RaptatToken> sentenceTokens,
      String[] facilitatedTokens, String[] blockedTokens, String[] tokenStrings, String conceptName,
      boolean[] hasAssignedConcept) {
    boolean blocked;
    boolean facilitated;
    ConceptPhrase conceptPhrase;

    int tokenListSize = tokenStrings.length;
    List<AnnotatedPhrase> foundAnnotations = new ArrayList<>();
    List<RaptatPair<List<RaptatToken>, RaptatPair<String, Boolean>>> phrasesAndConcepts =
        new ArrayList<>();

    for (int tokenIndex = 0; tokenIndex < tokenListSize; tokenIndex++) {
      boolean assignConcept = false;
      boolean previouslyMarked = false;

      /*
       * Only consider GenMarks that have not already been tagged and therefore are currently marked
       * as NONE
       */
      int foundPhraseLength = this.phraseTrees.get(conceptName).phraseLengthAtIndexIgnoreCase(
          tokenStrings, tokenIndex, GeneralMarks.ContextMark.CONCEPTWORD);

      /*
       * Glenn comment - 10/22/15 - foundPhraseLength > 0 indicates that there is a phrase mapping
       * to concept 'conceptName' at index tokenIndex
       */
      if (foundPhraseLength > 0) {
        blocked = false;
        facilitated = false;

        /*
         * Glenn comment - 10/22/15 - In the for loop below, we are putting the tokens into one
         * phrase and determining if any of the tokens in that phrase are blocked, facilitated, or
         * already marked.
         *
         * The 'marked' array keeps track of which tokens have been marked as facilitated or
         * blocked.
         */
        List<String> phraseAsList = new ArrayList<>(foundPhraseLength);
        for (int j = tokenIndex; j < tokenIndex + foundPhraseLength; j++) {
          phraseAsList.add(tokenStrings[j]);

          if (facilitatedTokens[j].equals(RaptatConstants.FACILITATED_TAG)) {
            facilitated = true;
          }
          if (blockedTokens[j].equals(RaptatConstants.BLOCKED_TAG)) {
            blocked = true;
          }
          if (hasAssignedConcept[j]) {
            previouslyMarked = true;
          }
        }

        /*
         * Glenn comment - 10/22/15 - This will retrieve a ConceptPhrase object indicating whether
         * the phrase can be blocked or requires facilitation
         */
        List<String> test = null;
        Map<SortableStringArrayList, ConceptPhraseType> phrasesForConcept =
            this.conceptNameToPhrasesMap.get(conceptName);
        conceptPhrase = (ConceptPhrase) phrasesForConcept.get(phraseAsList);

        assignConcept = true;
        if (previouslyMarked || conceptPhrase.blockable && blocked
            || conceptPhrase.requiresFacilitation && !facilitated) {
          assignConcept = false;
        }

        if (assignConcept) {
          for (int j = tokenIndex; j < tokenIndex + foundPhraseLength; j++) {
            hasAssignedConcept[j] = true;
          }
          RaptatPair<String, Boolean> conceptNameAndOverride =
              new RaptatPair<>(conceptName, conceptPhrase.overridesRaptat);
          RaptatPair<List<RaptatToken>, RaptatPair<String, Boolean>> foundPhrase =
              new RaptatPair<>(sentenceTokens.subList(tokenIndex, tokenIndex + foundPhraseLength),
                  conceptNameAndOverride);
          phrasesAndConcepts.add(foundPhrase);
        }
      }
      /*
       * Skip over tokens that are part of a found phrase that was not previously marked
       */
      if (foundPhraseLength > 1 && assignConcept) {
        tokenIndex += foundPhraseLength - 1;
      }
    }

    foundAnnotations = Annotator.convertToAnnotations(phrasesAndConcepts, false, false, true);
    return foundAnnotations;
  }


  public void printPhraseTrees() {
    for (String curKey : this.phraseTrees.keySet()) {
      System.out.println("\n*******************\nConcept:" + curKey + "\n*******************\n");
      PhraseTree curTree = this.phraseTrees.get(curKey);
      for (RaptatPair<List<String>, PhraseTree> curPhrase : curTree) {
        System.out.print(curPhrase.right.getPhraseTag() + ":");
        StringBuilder sb = new StringBuilder("");
        for (String curString : curPhrase.left) {
          sb.append(" ").append(curString);
        }
        System.out.println(sb);
      }
    }
  }

  //
  // List<String[]> theList = new ArrayList<String[]>();
  // for (String curPhrase : thePhrases)
  // {
  // theList.add( curPhrase.split( " " ) );
  // }
  // return theList;


  /**
   * Jun 11, 2018
   *
   * @param facilitators
   * @return
   */
  private List<SortableStringArrayList> buildList(
      Map<SortableStringArrayList, ConceptPhraseType> phraseMapping) {
    Set<SortableStringArrayList> mappingSet = phraseMapping.keySet();

    return new ArrayList<>(mappingSet);
  }


  private AnnotatedPhrase createPhrase(List<RaptatToken> foundSequence, String curConcept) {
    throw new NotImplementedException(
        "TODO - " + this.getClass().getName() + " not yet fully implemented");
  }


  /**
   * Build PhraseTree objects that efficiently store facilitator, blocker, and concept phrase for
   * look up
   *
   * @param conceptPhrases
   * @param facilitators
   * @param blockers
   * @return
   */
  private PhraseTree initializeContextPhrases(
      Map<SortableStringArrayList, ConceptPhraseType> conceptPhrases,
      Map<SortableStringArrayList, ConceptPhraseType> facilitators,
      Map<SortableStringArrayList, ConceptPhraseType> blockers) {
    PhraseTree contextPhraseTree = new PhraseTree();

    contextPhraseTree.addPhrasesAsLists(this.buildList(facilitators),
        GeneralMarks.ContextMark.FACILITATOR);
    contextPhraseTree.addPhrasesAsLists(this.buildList(blockers), GeneralMarks.ContextMark.BLOCKER);
    contextPhraseTree.addPhrasesAsLists(this.buildList(conceptPhrases),
        GeneralMarks.ContextMark.CONCEPTWORD);

    return contextPhraseTree;
  }


  private boolean phraseBlocked(List<RaptatToken> tokenSequence, List<RaptatToken> sentenceTokens,
      boolean conceptMarked, String conceptName) {
    if (!conceptMarked) {
    }

    return RaptatToken.sequenceFacilitated(tokenSequence, conceptName);
  }


  private boolean phraseFacilitated(List<RaptatToken> tokenSequence,
      List<RaptatToken> sentenceTokens, boolean conceptMarked, String conceptName) {
    if (!conceptMarked) {
    }
    return RaptatToken.sequenceBlocked(tokenSequence, conceptName);
  }


  /**
   * This method was created for debugging to show the state of the facilitator, blocker, and
   * concept phrase dictionaries for this object
   */
  private void printDictionaries() {
    HashSet<String> allConcepts = new HashSet<>();
    allConcepts.addAll(this.conceptNameToFacilitatorsMap.keySet());
    allConcepts.addAll(this.conceptNameToBlockersMap.keySet());
    allConcepts.addAll(this.conceptNameToPhrasesMap.keySet());

    for (String curKey : allConcepts) {
      System.out.println(
          "\n**************************\nCONCEPT:" + curKey + "\n**************************\n");

      System.out.println("\nCONCEPT MAPPED PHRASES:");
      Map<SortableStringArrayList, ConceptPhraseType> conceptPhrases =
          this.conceptNameToPhrasesMap.get(curKey);
      for (SortableStringArrayList curPhrase : conceptPhrases.keySet()) {
        ConceptPhraseType phraseValue = conceptPhrases.get(curPhrase);
        System.out.println(phraseValue.phraseToString());
      }

      System.out.println("\nFACILITATORS:");
      Map<SortableStringArrayList, ConceptPhraseType> curFacilitators =
          this.conceptNameToFacilitatorsMap.get(curKey);
      if (curFacilitators != null) {
        for (SortableStringArrayList curPhrase : curFacilitators.keySet()) {
          System.out.println(curFacilitators.get(curPhrase).toString());
        }
      } else {
        System.out.println("NO FACILITATORS");
      }

      System.out.println("\nBLOCKERS:");
      Map<SortableStringArrayList, ConceptPhraseType> curBlockers =
          this.conceptNameToBlockersMap.get(curKey);
      if (curBlockers != null) {
        for (SortableStringArrayList curPhrase : curBlockers.keySet()) {
          System.out.println(curBlockers.get(curPhrase).toString());
        }
      } else {
        System.out.println("NO BLOCKERS");
      }
    }
  }


  /**
   * Jun 11, 2018
   *
   * @param conceptToFacilitatorsMap
   * @param conceptToBlockersMap
   * @param conceptToConceptPhrasesMap
   * @param overridesRaptat
   * @param tokens
   * @param phraseType
   * @param conceptName
   * @param textPhrase
   * @return
   */
  private void processLineFromDictionary(
      Map<String, Map<SortableStringArrayList, ConceptPhraseType>> conceptToFacilitatorsMap,
      Map<String, Map<SortableStringArrayList, ConceptPhraseType>> conceptToBlockersMap,
      Map<String, Map<SortableStringArrayList, ConceptPhraseType>> conceptToConceptPhrasesMap,
      boolean overridesRaptat, String[] tokens, String phraseType, String conceptName,
      String textPhrase) {
    boolean requiresFacilitator;
    boolean blockable;
    int backwardWindow;
    int forwardWindow;
    Map<SortableStringArrayList, ConceptPhraseType> phraseToConceptPhraseMap;
    SortableStringArrayList textAsStringList = textToStringList(textPhrase);

    /*
     * Glenn comment 10/21/15 -These two variables indicate window of influence for facilitators and
     * blockers and indicates whether a phrase must be facilitated or can be blocked for concept
     * phrases.
     */
    String forwardWindowOrFacilitated = null;
    String backWindowOrBlocked = null;

    if (tokens.length > 3) {
      forwardWindowOrFacilitated = tokens[3];
      if (tokens.length > 4) {
        backWindowOrBlocked = tokens[4];
      }
    }
    if (phraseType.equalsIgnoreCase("ConceptPhrase")) {
      if (backWindowOrBlocked == null) {
        blockable = false;

        if (forwardWindowOrFacilitated == null) {
          requiresFacilitator = false;
        } else {
          requiresFacilitator = Boolean.parseBoolean(tokens[3]);
        }
      } else {
        blockable = Boolean.parseBoolean(backWindowOrBlocked);
        requiresFacilitator = Boolean.parseBoolean(forwardWindowOrFacilitated);
      }

      if (tokens.length > 5) {
        overridesRaptat = Boolean.parseBoolean(tokens[5]);
      }

      if ((phraseToConceptPhraseMap = conceptToConceptPhrasesMap.get(conceptName)) == null) {
        phraseToConceptPhraseMap = new HashMap<>();
        conceptToConceptPhrasesMap.put(conceptName, phraseToConceptPhraseMap);
      }
      /*
       * Glenn comment 10/21/15 -There is some redundancy of the key and the elements of
       * ConceptPhrase (both contain the phrase itself).
       */
      phraseToConceptPhraseMap.put(textAsStringList,
          new ConceptPhrase(textAsStringList, requiresFacilitator, blockable, overridesRaptat));
    } else {
      if (backWindowOrBlocked == null) {
        backwardWindow = RaptatConstants.GENERAL_CONTEXT_WINDOW_RADIUS;

        if (forwardWindowOrFacilitated == null) {
          forwardWindow = RaptatConstants.GENERAL_CONTEXT_WINDOW_RADIUS;
        } else {
          forwardWindow = Integer.parseInt(forwardWindowOrFacilitated);
        }
      } else {
        forwardWindow = Integer.parseInt(forwardWindowOrFacilitated);
        backwardWindow = Integer.parseInt(backWindowOrBlocked);

        /*
         * if their value is less than 0, then the window spans the whole sentence (as long as it's
         * no bigger than Integer.MAX_VALUE!
         */
        if (forwardWindow < 0) {
          forwardWindow = Integer.MAX_VALUE;
        }
        if (backwardWindow < 0) {
          backwardWindow = Integer.MAX_VALUE;
        }
      }

      if (phraseType.equalsIgnoreCase("facilitator")) {
        Map<SortableStringArrayList, ConceptPhraseType> phraseToFacilitatorPhraseMap;

        if ((phraseToFacilitatorPhraseMap = conceptToFacilitatorsMap.get(conceptName)) == null) {
          phraseToFacilitatorPhraseMap = new HashMap<>();
          conceptToFacilitatorsMap.put(conceptName, phraseToFacilitatorPhraseMap);
        }

        /*
         * Glenn comment 10/21/15 -There is some redundancy of the key and the elements of
         * ConceptPhraseModifier (both contain the phrase itself).
         */
        phraseToFacilitatorPhraseMap.put(textAsStringList,
            new ConceptPhraseModifier(textAsStringList, backwardWindow, forwardWindow));
      } else if (phraseType.equalsIgnoreCase("blocker")) {
        Map<SortableStringArrayList, ConceptPhraseType> conceptToBlockerPhraseMap;

        if ((conceptToBlockerPhraseMap = conceptToBlockersMap.get(conceptName)) == null) {
          /*
           * Glenn comment 10/21/15 -There is some redundancy of the key and the elements of
           * ConceptPhraseModifier (both contain the phrase itself).
           */
          conceptToBlockerPhraseMap = new HashMap<>();
          conceptToBlockersMap.put(conceptName, conceptToBlockerPhraseMap);
        }

        conceptToBlockerPhraseMap.put(textAsStringList,
            new ConceptPhraseModifier(textAsStringList, backwardWindow, forwardWindow));
      }
    }
  }


  /**
   * Creates the facilitator, blocker and concept phrase list from a single file.
   *
   * @param filePath
   * @param conceptNameToFacilitatorsMap2
   * @param conceptNameToBlockersMap2
   * @param conceptNameToPhrasesMap2
   * @param acceptedConcepts
   * @return
   */
  private void readDictionary(String filePath, HashSet<String> acceptedConcepts)
  /*
   * Glenn Comment 10/25/15 - The logic for determining whether a 'ConceptPhrase' can be blocked or
   * requires facilitation should be modified. If there is no value in the 'requiresFacillitation'
   * or 'blockable' columns of the dictionary, both the values should be set to a default value of
   * false. I think we should also separate out the 'concept phrase' dictionary from a dictionary
   * containing facilitators and blockers (facilitator and blocker dictionary can be one). Finally,
   * we should check to see if there are actual blockers or facilitators for a a given concept. If
   * not, we should set requiresFacilitation and blockable to false for all phrases mapping to that
   * concept.
   *
   * Lastly, we should check the "consistency" of the dictionary and stop and report any
   * inconsistencies. A consistent phrase should not have any of its token subsequences be equal to
   * another phrase. If this occurs but it is the same type of phrase (e.g. a phrase mapping to the
   * same concept with the same settings of requiresFacilitation and blockable, a blocker for the
   * same concept with equal windows of influence, or a facilitator with the same windows), the
   * longer phrase could be discarded (unless the windows of influence were dramatically different).
   */
  {

    this.conceptNameToFacilitatorsMap = new HashMap<>();
    this.conceptNameToBlockersMap = new HashMap<>();
    this.conceptNameToPhrasesMap = new HashMap<>();

    boolean overridesRaptat = false;
    int linesRead = 0;
    BufferedReader br = null;

    try {
      br = new BufferedReader(new FileReader(filePath));
      String line;
      String[] tokens;

      /* Discard first line of titles */
      br.readLine();

      while ((line = br.readLine()) != null) {
        /* '#' character indicates comment */
        if (line.trim().startsWith("#") || line.trim().isEmpty()) {
          continue;
        }

        tokens = line.split("\\t");

        String phraseType = tokens[0].toLowerCase().trim();
        String conceptName = tokens[1].toLowerCase().trim();
        String textPhrase = tokens[2].toLowerCase().trim();

        if (acceptedConcepts == null || acceptedConcepts.contains(conceptName.toLowerCase())) {

          processLineFromDictionary(this.conceptNameToFacilitatorsMap,
              this.conceptNameToBlockersMap, this.conceptNameToPhrasesMap, overridesRaptat, tokens,
              phraseType, conceptName, textPhrase);
          linesRead++;
          if (GeneralBoundaryMarker.logger.isInfoEnabled() && linesRead % 1000 == 0) {
            System.out.println("Read " + linesRead / 1000 + "K lines of dictionary");
          }
        }
      }
    } catch (FileNotFoundException ex) {
      GeneralBoundaryMarker.logger.fatal(ex);
      System.exit(-1);
    } catch (IOException ex) {
      GeneralBoundaryMarker.logger.fatal(ex);
      System.exit(-1);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  /**
   * Method Description - Analyzes each token in an array of tokens and sets a mark in tokenMarks if
   * the phrase (starting at a given position) corresponds to a certain type of mark, "markToFind",
   * is found. It also modifies the windows parameter with the window or distance in terms of number
   * of tokens influenced on each side of the mark.
   *
   * @param tokenStrings
   * @param facilitators
   * @param blockers
   * @param phraseTree
   * @param tokenMarks
   * @param modifierInfluenceWindow
   * @param markToFind
   */
  private void setContextMarks(String[] tokenStrings,
      Map<SortableStringArrayList, ConceptPhraseType> phraseTextToModifierMap,
      PhraseTree phraseTree, Mark[] tokenMarks, ConceptPhraseType[] modifierInfluenceWindow,
      Mark markToFind) {
    for (int tokenIndex = 0; tokenIndex < tokenStrings.length; tokenIndex++) {
      /*
       * Only consider GenMarks that have not already been tagged and therefore are currently marked
       * as NONE
       */
      if (tokenMarks[tokenIndex] == Mark.DefaultMark.NONE) {
        int foundPhraseLength =
            phraseTree.phraseLengthAtIndexIgnoreCase(tokenStrings, tokenIndex, markToFind);

        if (foundPhraseLength > 0) {
          SortableStringArrayList phraseAsList =
              SortableStringArrayList.buildList(tokenStrings, tokenIndex, foundPhraseLength);

          for (int j = tokenIndex; j < tokenIndex + foundPhraseLength; j++) {
            // Stop if we reach a mark that has already been set
            // to something other than NONE
            if (tokenMarks[j] != Mark.DefaultMark.NONE) {
              break;
            }
            tokenMarks[j] = markToFind;

            modifierInfluenceWindow[j] = phraseTextToModifierMap.get(phraseAsList);
          }
        }
      }
    }
  }


  /*
   * Glenn comment - 10/22/15 - I'm not sure what this does. Why do we want to mark all tokens as
   * facilitator?
   */
  private void setUnmarkedAsFacilitators(String[] tokens, Mark[] tokenMarks,
      ConceptPhraseModifier[] windows) {
    for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
      /*
       * Only consider GenMarks that have not already been tagged and therefore are currently marked
       * as NONE
       */
      if (tokenMarks[tokenIndex] == Mark.DefaultMark.NONE) {
        tokenMarks[tokenIndex] = GeneralMarks.ContextMark.FACILITATOR;
        windows[tokenIndex] = new ConceptPhraseModifier("", 1, 1);
      }
    }
  }


  /**
   * Jun 11, 2018
   *
   * @param textPhrase
   * @return
   */
  private SortableStringArrayList textToStringList(String textPhrase) {
    String[] textStrings = textPhrase.split("\\s+");
    List<String> textAsList = new ArrayList<>(textStrings.length);
    for (String tokenString : textStrings) {
      textAsList.add(tokenString);
    }

    return new SortableStringArrayList(textAsList);
  }


  private static List<String[]> buildList(String[] thePhrases) {
    List<String[]> theList = new ArrayList<>();
    for (String curPhrase : thePhrases) {
      theList.add(curPhrase.split(" "));
    }
    return theList;
  }
}
