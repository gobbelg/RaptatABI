package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

// import src.main.gov.va.vha09.grecc.raptat.gg.lexicaltools.Stemmer;
/**
 * Responsible for taking a set of annotations from a given document and adding a list of tokens to
 * those annotations
 *
 * @author Glenn Gobbel - Jun 28, 2012
 */
public class PhraseTokenIntegrator {
  // Modified by Glenn Gobbel on May 2, 2012
  // Made all instance variable "protected"
  // to allow access by subclasses only
  // This variable maps start offset to original string, POS, and end offset
  // stored as a single string separate by spaces.

  protected static Logger logger = Logger.getLogger("PhraseTokenIntegrato.class");


  protected HashMap<String, String> offsetToTokenLookup = new HashMap<>();


  protected List<String[]> AnnotationsInfo;

  protected List<AnnotatedPhrase> allAnnotatedPhrases;
  protected HashMap<String, String> POSMap = new HashMap<>();
  protected HashMap<String, String> myMap = new HashMap<>();
  protected List<String[]> posDetails = new ArrayList<>();

  protected List<String[]> phrasePOSList = new ArrayList<>();

  protected String POS = null;

  public PhraseTokenIntegrator() {
    PhraseTokenIntegrator.logger.setLevel(Level.INFO);
  }

  /**
   * Use the document and its tokens to add the list of tokens contained within an annotated phrase.
   * There is no need to know about useStems or removeStopWords because this method will take tokens
   * associated with the document and integrate them into the phrases, and these tokens already have
   * stemming accounted for in the augmented token strings field of the tokens. Also, stop words are
   * similarly removed as tokens and can't be used as phrase tokens. The usePOS parameter is needed
   * because phrases that partially overlap tokens will be identified and assigned POS of "OTHER"
   * and, if usePOS is true, these will be appended to the token string to form the augmented token
   * string for the token created to represent this partially overlapped token.
   *
   * <p>
   * The tokens are added as a list in sorted order according to start offset. If the number of
   * tokens in a phrase is greater than the tokensToAdd parameter, such a phrase will be split to
   * conform to this number (tokensToAdd), and rawTokens, rawTokenNumber, startOffset, and endOffset
   * will be lost. <br>
   * <br>
   * <b>*******PRECONDITION********:</b><br>
   * The annotated phrases in the parameter thePhrases must be sorted by start offset.
   *
   * @param thePhrases
   * @param doc
   * @param usePOS
   * @param tokensToAdd - set to 0 to add all tokens, set to < 0 to add none
   * @return list of phrases that have been split because their size was larger than tokensToAdd or
   *         removed because they were not the head of a linked phrase
   * @author Glenn Gobbel - Jun 28, 2012
   */
  public Hashtable<Integer, AnnotatedPhrase> addProcessedTokensToAnnotations(
      List<AnnotatedPhrase> thePhrases, RaptatDocument doc, boolean usePOS, int tokensToAdd) {
    Hashtable<Integer, AnnotatedPhrase> splitPhrases = new Hashtable<>();

    if (tokensToAdd > -1) {
      List<AnnotatedPhrase> modifiedPhrases = new ArrayList<>();
      Iterator<AnnotatedPhrase> phraseIterator = thePhrases.iterator();
      IndexedTree<IndexedObject<RaptatToken>> processedDocTokens = doc.getProcessedTokens();

      while (phraseIterator.hasNext()) {
        AnnotatedPhrase curPhrase = phraseIterator.next();

        /*
         * Only process phrases that are "head" phrases
         */
        if (curPhrase.getPrevPhrase() == null) {
          AnnotatedPhrase headPhrase = curPhrase;
          List<RaptatToken> phraseTokens = new ArrayList<>();
          /*
           * The tokens of a multi-spanned annotation are merged to the head of the Phrase chain.
           * All tokens are stored in the processedTokens field of the head of the Phrase chain.
           */
          while (headPhrase != null) {
            phraseTokens.addAll(getPhraseTokens(headPhrase, processedDocTokens, usePOS, -1));
            headPhrase = headPhrase.getNextPhrase();
          }

          if (PhraseTokenIntegrator.logger.isDebugEnabled()) {
            headPhrase = curPhrase;
            StringBuilder totalString = new StringBuilder("");
            while (headPhrase != null) {
              totalString.append(headPhrase.getPhraseStringUnprocessed()).append(" ");
              headPhrase = headPhrase.getNextPhrase();
            }
            PhraseTokenIntegrator.logger.debug(totalString);
          }

          if (tokensToAdd > 0 && phraseTokens.size() > tokensToAdd) {
            if (PhraseTokenIntegrator.logger.isDebugEnabled()) {
              Collection<AnnotatedPhrase> tempPhrases =
                  splitPhrase(curPhrase, phraseTokens, tokensToAdd);

              for (AnnotatedPhrase testPhrase : tempPhrases) {
                List<RaptatToken> tokenList = testPhrase.getProcessedTokens();
                System.out.println("Tokens After Split:" + tokenList.size());
                System.out.print("Phrase: ");
                StringBuilder phrase = new StringBuilder("");
                for (RaptatToken curToken : tokenList) {
                  phrase.append(curToken.getTokenStringPreprocessed()).append(" ");
                }
                System.out.println(phrase);
              }
            }

            modifiedPhrases.addAll(splitPhrase(curPhrase, phraseTokens, tokensToAdd));
            splitPhrases.put(curPhrase.getIndexInDocument(), curPhrase);
            phraseIterator.remove();
          }

          /*
           * Set the "processedStartOffset" and "processedEndOffset according to the actual
           * processed tokens in the phrase (even after stop word removal). Note that the tokens in
           * the list are not sorted as this occurs automatically within this algorithm
           */
          else if (phraseTokens.size() > 0) {
            curPhrase.setProcessedStartOffset(phraseTokens.get(0).getStartOffset());
            curPhrase
                .setProcessedEndOffset(phraseTokens.get(phraseTokens.size() - 1).getEndOffset());
            curPhrase.setProcessedTokens(phraseTokens);
          }

        } else {
          phraseIterator.remove();
        }
      }

      thePhrases.addAll(modifiedPhrases);
    }

    return splitPhrases;
  }


  public void printOffsetToTokenLookup() {
    ArrayList<String> keySet = new ArrayList<>(this.offsetToTokenLookup.keySet());
    Collections.sort(keySet);
    for (String curKey : keySet) {
      System.out.println(curKey + ":" + this.offsetToTokenLookup.get(curKey));
    }
  }


  public void printPOSMap(HashMap<String, String> map) {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      System.out.println(entry.getKey() + " " + entry.getValue());
    }
  }


  /**
   * Use the document and its tokens and determine and set the number of raw tokens in each phrase.
   * The usePOS parameter is needed because phrases that partially overlap tokens will be identified
   * and assigned POS of "OTHER" and, if usePOS is true, these will be appended to the token string
   * to form the augmented token string for the token created to represent this partially overlapped
   * token. Note that the tokens are added as a list in sorted order according to start offset.
   *
   * @param thePhrases
   * @param theDocument
   * @param usePOS
   * @author Glenn Gobbel - Jun 28, 2012
   */
  public void setNumberOfRawTokens(List<AnnotatedPhrase> thePhrases, RaptatDocument theDocument,
      boolean usePOS) {
    Iterator<AnnotatedPhrase> phraseIterator = thePhrases.iterator();
    AnnotatedPhrase curPhrase;
    int numberOfTokens;
    IndexedTree<IndexedObject<RaptatToken>> docTokens = theDocument.getProcessedTokens();

    while (phraseIterator.hasNext()) {
      curPhrase = phraseIterator.next();
      numberOfTokens = getRawPhraseTokens(curPhrase, docTokens, usePOS);
      curPhrase.setRawTokenNumber(numberOfTokens);
    }
  }


  /**
   * @param thePhrase
   * @param documentTokens
   * @param tokensToAdd - set to negative number to process all tokens
   * @return List<Token> - The tokens in the phrase including parts of speech and stems in sorted
   *         order by start offset.
   * @author Glenn Gobbel - June 26, 2012
   */
  private List<RaptatToken> getPhraseTokens(AnnotatedPhrase thePhrase,
      IndexedTree<IndexedObject<RaptatToken>> documentTokens, boolean usePOS, int tokensToAdd) {
    RaptatToken curToken;
    RaptatToken phraseToken = null;
    List<RaptatToken> phraseTokenList = new ArrayList<>();

    String phraseStringPreprocessed = thePhrase.getPhraseStringUnprocessed();
    int phraseStart = Integer.parseInt(thePhrase.getRawTokensStartOff());
    int phraseEnd = Integer.parseInt(thePhrase.getRawTokensEndOff());
    IndexedObject<RaptatToken> curIndexedToken =
        documentTokens.getClosestLesserOrEqualObject(phraseStart);

    if (curIndexedToken != null) {
      curToken = curIndexedToken.getIndexedObject();
    } else {
      curIndexedToken = documentTokens.getClosestGreaterObject(phraseStart);
      if (curIndexedToken != null) {
        curToken = curIndexedToken.getIndexedObject();
      } else {
        curToken = null;
      }
    }

    int addedTokens = 0;

    // Don't add more tokens than the maximum number provided within
    // tokensToAdd (add all if value < 0)
    while (curToken != null && (addedTokens < tokensToAdd || tokensToAdd < 0)
        && Integer.parseInt(curToken.getStartOffset()) < phraseEnd) {
      phraseToken = getSingleTokenForPhrase(curToken, phraseStringPreprocessed, phraseStart,
          phraseEnd, usePOS);

      if (phraseToken != null) {
        phraseTokenList.add(phraseToken);
        addedTokens++;
      }
      curIndexedToken =
          documentTokens.getClosestGreaterObject(Integer.parseInt(curToken.getEndOffset()) - 1);
      if (curIndexedToken != null) {
        curToken = curIndexedToken.getIndexedObject();
      } else {
        curToken = null;
      }
    }

    return phraseTokenList;
  }


  /**
   * Determine the number of raw tokens that would be in an AnnotatedPhrase object if it contained
   * all the raw tokens corresponding to the region between its rawStartOffset and rawEndOffset.
   * Algorithm works by finding first token overlapping the phrase rawStartOffset and then walking
   * forward over the tokens until it comes to one that no longer overlaps the phrase.
   *
   * @param thePhrase
   * @param offsetIndexTree
   * @param usePOS
   * @return
   * @author Glenn Gobbel - Feb 10, 2013
   */
  private int getRawPhraseTokens(AnnotatedPhrase thePhrase,
      IndexedTree<IndexedObject<RaptatToken>> offsetIndexTree, boolean usePOS) {
    RaptatToken candidateToken;

    int phraseStart = Integer.parseInt(thePhrase.getRawTokensStartOff());
    int phraseEnd = Integer.parseInt(thePhrase.getRawTokensEndOff());
    IndexedObject<RaptatToken> curIndexedObject =
        offsetIndexTree.getClosestLesserOrEqualObject(phraseStart);

    if (curIndexedObject != null) {
      candidateToken = curIndexedObject.getIndexedObject();
    } else {
      curIndexedObject = offsetIndexTree.getClosestGreaterObject(phraseStart);
      if (curIndexedObject != null) {
        candidateToken = curIndexedObject.getIndexedObject();
      } else {
        candidateToken = null;
      }
    }
    int phraseTokenNumber = 0;

    // If the candidateToken exists and overlaps the phrase, add it to the
    // count of length
    while (candidateToken != null && Integer.parseInt(candidateToken.getStartOffset()) < phraseEnd
        && Integer.parseInt(candidateToken.getEndOffset()) > phraseStart) {
      phraseTokenNumber++;
      curIndexedObject = offsetIndexTree
          .getClosestGreaterObject(Integer.parseInt(candidateToken.getEndOffset()) - 1);
      if (curIndexedObject != null) {
        candidateToken = curIndexedObject.getIndexedObject();
      } else {
        candidateToken = null;
      }
    }

    return phraseTokenNumber;
  }


  /**
   * Helper method for getPhraseTokens(). This method determines the part of speech and stem for one
   * token that is within or overlaps a given phrase
   *
   * <p>
   * Precondition: None Postcondition: None
   *
   * @param tokenData
   * @param phraseString
   * @param phraseStart
   * @param phraseEnd
   * @return Token
   * @author Glenn Gobbel - May 4, 2012
   */
  private RaptatToken getSingleTokenForPhrase(RaptatToken candidateToken, String phraseString,
      int phraseStart, int phraseEnd, boolean usePOS) {
    String tokenString;
    RaptatToken phraseToken = null;

    /*
     * For first token in phrase, check how much overlap there is with current phrase
     */
    if (Integer.parseInt(candidateToken.getStartOffset()) < phraseStart) {
      /*
       * If it does extend beyond, just use the overlap as the phraseToken string. We don't do
       * stemming and use "OTHER" for part-of-speech because it's only a partial string
       */
      tokenString = getPhraseStartOverlap(phraseString, phraseStart, phraseEnd,
          candidateToken.getTokenStringPreprocessed(),
          Integer.parseInt(candidateToken.getStartOffset()),
          Integer.parseInt(candidateToken.getEndOffset()));
      if (tokenString != null && tokenString.length() > 0) {
        String augmentedString = tokenString;
        if (usePOS) {
          augmentedString += "_OTHER";
        }

        /*
         * We don't stem partial string matches, so we assign tokenString to token stem during
         * construction
         */
        phraseToken = new RaptatToken(tokenString, tokenString, augmentedString, "OTHER",
            String.valueOf(phraseStart), String.valueOf(phraseStart + tokenString.length()),
            candidateToken.getSentenceOfOrigin(), candidateToken.getTokenIndexInDocument());
        phraseToken.setParentToken(candidateToken);
      }
    } else {
      /*
       * See if the end of the token string extends beyond the end of the phase
       */
      if (Integer.parseInt(candidateToken.getEndOffset()) > phraseEnd) {
        /*
         * If it does extend beyond, just use the overlap as the phraseToken string. We don't do
         * stemming and use "OTHER" for part-of-speech because it's only a partial string
         */
        tokenString = getPhraseEndOverlap(candidateToken.getTokenStringPreprocessed(),
            Integer.parseInt(candidateToken.getEndOffset()), phraseEnd);
        if (tokenString != null) {
          String augmentedString = tokenString;
          if (usePOS) {
            augmentedString += "_OTHER";
          }
          /*
           * We don't stem partial string matches, so we assign tokenString to token stem during
           * construction
           */
          phraseToken = new RaptatToken(tokenString, tokenString, augmentedString, "OTHER",
              candidateToken.getStartOffset(),
              String.valueOf(
                  Integer.parseInt(candidateToken.getStartOffset()) + tokenString.length()),
              candidateToken.getSentenceOfOrigin(), candidateToken.getTokenIndexInDocument());
          phraseToken.setParentToken(candidateToken);
        }
      } else {
        phraseToken = new RaptatToken(candidateToken);
        phraseToken.setParentToken(candidateToken);
      }
    }
    return phraseToken;
  }


  /**
   * Method that takes a single phrase and breaks it into multiple phrases that are equal or less in
   * length in terms of number of tokens in the phrase. Note that this does not handle rawTokens or
   * rawTokeNumber when the phrase is split. These are lost in the splitting process. Precondition:
   * maxTokens parameter > 0
   *
   * @param inPhrase
   * @param phraseTokens - in sorted order by start offset
   * @param maxTokens
   * @return
   * @author Glenn Gobbel - Oct 16, 2012 modified by Sanjib Saha Dec 2, 2014 to include
   *         multi-spanned annotations
   */
  private Collection<AnnotatedPhrase> splitPhrase(AnnotatedPhrase inPhrase,
      List<RaptatToken> phraseTokens, int maxTokens) {
    List<AnnotatedPhrase> resultList = new ArrayList<>();

    if (maxTokens > 0) {
      String annotatorName = inPhrase.getAnnotatorName();
      String annotatorID = inPhrase.getAnnotatorID();
      String mentionID = inPhrase.getMentionId();
      String concept = inPhrase.getConceptName();
      RaptatDocument theDocument = inPhrase.getDocumentOfOrigin();

      List<RaptatAttribute> attributes = inPhrase.getPhraseAttributes();
      List<ConceptRelation> relations = inPhrase.getConceptRelations();

      int indexInDocument = inPhrase.getIndexInDocument();
      int mentionIndex = 1;
      int i = 0;
      int totalTokens = phraseTokens.size();

      List<RaptatToken> curTokens;
      AnnotatedPhrase tempPhrase;
      AnnotatedPhrase prevTempPhrase = null;

      while (i < totalTokens) {
        if (totalTokens > i + maxTokens) {
          curTokens = phraseTokens.subList(i, i + maxTokens);
        } else {
          curTokens = phraseTokens.subList(i, totalTokens);
        }

        String phraseMention = mentionID + "_split_" + mentionIndex++;
        tempPhrase = new AnnotatedPhrase(curTokens, theDocument, concept, phraseMention,
            annotatorName, annotatorID, indexInDocument, attributes, relations);

        if (prevTempPhrase != null) {
          prevTempPhrase.setNextPhrase(tempPhrase);
          tempPhrase.setPrevPhrase(prevTempPhrase);
        }

        prevTempPhrase = tempPhrase;
        resultList.add(tempPhrase);

        i = i + maxTokens;
      }
    } else {
      GeneralHelper.errorWriter("Error - Call of PhraseTokenIntegrator.splitPhrase() with "
          + " maximum tokens set to <1");
    }

    return resultList;
  }


  /**
   * Helper method for processAnnotatedPhrase. Gets the part of a phrase that overlaps with a token
   * that starts at the same place as the phrase but ends after the phrase does.
   *
   * <p>
   * Precondition: Value of tokenEnd is greater than or equal to phraseEnd, and phraseEnd is greater
   * than or equal to the start offset of the token. Postcondition: None
   *
   * @param phraseString
   * @param phraseStart
   * @param phraseEnd
   * @param tokenString
   * @param tokenStart
   * @param tokenEnd
   * @return String - End part of phrase that overlaps with a token
   * @author Glenn Gobbel - May 4, 2012
   */
  public static String getPhraseEndOverlap(String tokenString, int tokenEnd, int phraseEnd) {
    int startDiff = tokenEnd - phraseEnd;

    try {
      return tokenString.substring(0, tokenString.length() - startDiff);
    } catch (Exception ex) {
      System.out.println("Exception: " + tokenString + " " + tokenEnd + " " + phraseEnd);
      return "";
    }
  }


  /**
   * Helper method for processAnnotatedPhrase. Gets the part of a phrase that overlaps with a token
   * that starts before the phrase does.
   *
   * <p>
   * Precondition: Value of tokenEnd is greater than or equal to phraseEnd. Postcondition: None
   *
   * @param phraseString
   * @param phraseStart
   * @param phraseEnd
   * @param tokenString
   * @param tokenStart
   * @param tokenEnd
   * @return String - overlapping part or entire phrase if entire phrase within token string
   * @author Glenn Gobbel - May 4, 2012
   */
  public static String getPhraseStartOverlap(String phraseString, int phraseStart, int phraseEnd,
      String tokenString, int tokenStart, int tokenEnd) {
    int startDiff = phraseStart - tokenStart;

    // If Token ends at or after phrase, the phrase
    // entirely within Token, so just return it.
    if (tokenEnd >= phraseEnd) {
      return phraseString;
    }

    if (startDiff <= tokenString.length()) {
      return tokenString.substring(startDiff);
    }

    return null; // Means phrase start is beyond end of the token
  }
}
