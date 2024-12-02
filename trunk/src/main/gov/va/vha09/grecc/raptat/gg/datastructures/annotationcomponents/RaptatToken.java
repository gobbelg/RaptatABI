package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree.IndexedTreeIterator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.UIMAToken;

public class RaptatToken implements Serializable, Cloneable, Comparable<RaptatToken> {
  /** */
  private static final long serialVersionUID = 3338625581986428536L;

  /*
   * Tokens in AnnotatedPhrase instances that are not sentences are derived from document and
   * sentence tokens. This field keeps track of the token used to derive this token. It is null for
   * sentence/document tokens (sentence tokens are part of document tokens).
   */
  private RaptatToken parentToken = null;

  /*
   * The unprocessedString has the form of the string before preprocessing by LVG in the
   * textAnalyzer
   */
  private String tokenStringUnprocessed;

  /*
   * The rawTokenString has the form of the string after preprocessing by LVG in the textAnalyzer,
   * which generally lowercases any words, remove diacritics, and removes parenthetical plurals (e.g
   * 'antigen(s)' is a parenthetical plural form but 'antigens' is not)
   */
  public String tokenStringPreprocessed = "";

  protected String startOff = "";

  protected String endOff = "";

  protected String tokenPOS = "";

  protected String tokenStem = "";

  protected String reasonNoMedsContext = "";

  /*
   * An "augmented" token string has either POS tagging, context tagging, stemming, or nothing if a
   * stop word; it can be used in place of the rawTokenString for probabilistic annotation where the
   * probability of a word and, for example, its POS, is built into the augmented string. So, mapped
   * probabilities for the word "heart" would depend on the the POS of heart, with "heart_noun" and
   * "heart_adj" mapped to different probabilities.
   */
  protected String tokenStringAugmented = "";

  protected AnnotatedPhrase sentenceOfOrigin;

  /*
   * This field was created for specifying and storing features to use in CRF models, but it can be
   * used for anything. The field would typically consist of the token string, its label, and other
   * potentially useful info (such as part of speech) separated by a delimiter.
   */
  protected transient String tokenStringTagged = "";

  /*
   * Glenn comment - 10/26/15 - I worry about a token that associates with a concept leading to
   * inconsistencies because the concept associated with a token is usually stored at the
   * AnnotatedPhrase level
   */
  protected Collection<String> assignedConcepts = new HashSet<>();
  protected HashSet<String> facilitatedConcepts = new HashSet<>();

  protected HashSet<String> blockedConcepts = new HashSet<>();

  /* Keeps track of whether this token is part of a concept found by RapTAT */
  protected boolean raptatConceptAssigned = false;

  /*
   * Keeps track of whether this token is part of a concept found by a dictionary and the associated
   * concept should take precedence over any Raptat assigned concept
   */
  protected boolean dictionaryConceptAssignedAndPrecedent = false;

  /**
   * HashMap for storing the value of the contexts of the annotated phrases (such as: negation,
   * uncertainty, experiencer etc). Generally used to assign attributes based on, e.g. dictionaries
   * or crf models.
   */
  protected Map<String, String> contexts = new HashMap<>();

  /* This is the negation tag assigned by Negex */
  private String negexNegationTag = "";

  private RaptatToken previousToken = null;

  private RaptatToken nextToken = null;

  /**
   * Tracks whether a token is a member of the "processed tokens" in a document, which are the ones
   * used for probabilistic and logical reasoninig
   */
  private boolean isProcessed;

  private int tokenIndexInDocument = 0;

  /*
   * A token is allowed to extend over multiple lines. Keep lines ordered so the first one in the
   * set is the one highest in the document.
   */
  private TreeSet<TextDocumentLine> documentLines = new TreeSet<>();

  /*
   * A token *may* be a member of multiple token phrases
   */
  private Set<RaptatTokenPhrase> raptatTokenPhrases = new HashSet<>();

  /**
   * ******************************************** Copy constructor
   *
   * @param inputToken
   * @author Glenn Gobbel - Jun 28, 2012 ********************************************
   */
  public RaptatToken(final RaptatToken inputToken) {
    this.tokenStringPreprocessed = inputToken.tokenStringPreprocessed;
    this.tokenStem = inputToken.tokenStem;
    this.tokenStringAugmented = inputToken.tokenStringAugmented;
    this.tokenPOS = inputToken.tokenPOS;
    this.startOff = inputToken.startOff;
    this.endOff = inputToken.endOff;
    this.sentenceOfOrigin = inputToken.sentenceOfOrigin;
    this.parentToken = inputToken.parentToken;
    this.tokenIndexInDocument = inputToken.tokenIndexInDocument;
  }

  public RaptatToken(final String inputString, final int startOffset, final int endOffset) {
    this.tokenStringAugmented = inputString;
    this.startOff = String.valueOf(startOffset);
    this.endOff = String.valueOf(endOffset);
  }

  public RaptatToken(final String tokenWord, final String start, final String end, final String POS,
      final String stem) {
    this.tokenStringPreprocessed = tokenWord.toLowerCase();
    this.tokenPOS = POS;
    this.startOff = start;
    this.endOff = end;
    this.tokenStem = stem;
  }

  /**
   * ********************************************
   *
   * @param baseString
   * @param stemmedString
   * @param augmentedString
   * @param pos
   * @param tokenSpan
   * @author Glenn Gobbel - Jun 28, 2012
   * @param tokenIndex ********************************************
   */
  public RaptatToken(final String baseString, final String stemmedString,
      final String augmentedString, final String pos, final String theStartOff,
      final String theEndOff, final AnnotatedPhrase sentenceOfOrigin, final int tokenIndex) {
    this.tokenStringPreprocessed = baseString;
    this.tokenStem = stemmedString;
    this.tokenStringAugmented = augmentedString;
    this.tokenPOS = pos;
    this.startOff = theStartOff;
    this.endOff = theEndOff;
    this.sentenceOfOrigin = sentenceOfOrigin;
    this.tokenIndexInDocument = tokenIndex;
  }

  /**
   * Constructor used by the TextAnalyzer class during document processing to generate tokens for
   * the document
   *
   * @param unprocessedString
   * @param preprocessedString
   * @param stemmedString
   * @param augmentedString
   * @param pos
   * @param theStartOff
   * @param theEndOff
   * @param previousToken
   * @param reasonNoMedsContext
   * @param negationTag
   * @param negated
   * @param tokenIndex
   */
  public RaptatToken(final String unprocessedString, final String preprocessedString,
      final String stemmedString, final String augmentedString, final String pos,
      final String theStartOff, final String theEndOff, final RaptatToken previousToken,
      final String reasonNoMedsContext, final String negationTag, final boolean negated,
      final boolean uncertain, final int tokenIndex) {
    this(unprocessedString, preprocessedString, stemmedString, augmentedString, pos, theStartOff,
        theEndOff, reasonNoMedsContext, negated, uncertain);
    this.negexNegationTag = negationTag;
    this.previousToken = previousToken;
    if (previousToken != null) {
      previousToken.nextToken = this;
    }
    this.tokenIndexInDocument = tokenIndex;
  }

  /**
   * @param baseString
   * @param stemmedString
   * @param augmentedString
   * @param pos
   * @param theStartOff
   * @param theEndOff
   * @param reasonNoMedsContext
   * @param negated
   */
  public RaptatToken(final String unprocessedString, final String baseString,
      final String stemmedString, final String augmentedString, final String pos,
      final String theStartOff, final String theEndOff, final String reasonNoMedsContext,
      final boolean negated, final boolean uncertain) {
    setTokenStringUnprocessed(unprocessedString);
    this.tokenStringPreprocessed = baseString;
    this.tokenStem = stemmedString;
    this.tokenStringAugmented = augmentedString;
    this.tokenPOS = pos;
    this.startOff = theStartOff;
    this.endOff = theEndOff;
    this.reasonNoMedsContext = reasonNoMedsContext;

    final String uncertainValue = uncertain ? "uncertain" : "positive";
    this.contexts.put(ContextType.UNCERTAINTY.getTypeName(), uncertainValue);

    final String negationValue = negated ? "negative" : "positive";
    this.contexts.put(ContextType.NEGATION.getTypeName(), negationValue);
  }

  public RaptatToken(final UIMAToken uimaToken) {
    this.tokenStringAugmented = uimaToken.getTokenStringAugmented();
    this.startOff = String.valueOf(uimaToken.getBegin());
    this.endOff = String.valueOf(uimaToken.getEnd());
  }


  public void addBlocker(final String conceptName) {
    this.blockedConcepts.add(conceptName);
  }

  public void addConcept(final String conceptName) {
    this.assignedConcepts.add(conceptName);
  }

  public void addDocumentLine(final TextDocumentLine textDocumentLine) {
    this.documentLines.add(textDocumentLine);
  }

  public void addDocumentLines(final Collection<TextDocumentLine> textDocumentLines) {
    this.documentLines.addAll(textDocumentLines);
  }

  public void addFacilitator(final String conceptName) {
    this.facilitatedConcepts.add(conceptName);
  }

  public void addRaptatTokenPhrase(final RaptatTokenPhrase raptatTokenPhrase) {
    this.raptatTokenPhrases.add(raptatTokenPhrase);
  }

  public void addRaptatTokenPhrases(final Collection<RaptatTokenPhrase> raptatTokenPhrases) {
    this.raptatTokenPhrases.addAll(raptatTokenPhrases);
  }

  @Override
  public RaptatToken clone() {
    try {
      return (RaptatToken) super.clone();
    } catch (final CloneNotSupportedException ex) {
      ex.printStackTrace();
      Logger.getLogger(AnnotatedPhrase.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final RaptatToken comparisonToken) {
    final int lhStart = Integer.parseInt(this.startOff);
    final int rhStart = Integer.parseInt(comparisonToken.startOff);

    return lhStart - rhStart;
  }

  public boolean dictionaryConceptAssignedAndPrecedent() {
    return this.dictionaryConceptAssignedAndPrecedent;
  }

  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @param obj
   * @return
   * @author Glenn Gobbel - Sep 11, 2012 ***********************************************************
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final RaptatToken other = (RaptatToken) obj;
    if (this.endOff == null) {
      if (other.endOff != null) {
        return false;
      }
    } else if (!this.endOff.equals(other.endOff)) {
      return false;
    }
    if (this.sentenceOfOrigin == null) {
      if (other.sentenceOfOrigin != null) {
        return false;
      }
    } else if (!this.sentenceOfOrigin.equals(other.sentenceOfOrigin)) {
      return false;
    }
    if (this.startOff == null) {
      if (other.startOff != null) {
        return false;
      }
    } else if (!this.startOff.equals(other.startOff)) {
      return false;
    }
    if (this.tokenStringPreprocessed == null) {
      if (other.tokenStringPreprocessed != null) {
        return false;
      }
    } else if (!this.tokenStringPreprocessed.equals(other.tokenStringPreprocessed)) {
      return false;
    }
    return true;
  }

  public int get_end_offset_as_int() {
    return Integer.parseInt(getEndOffset());
  }

  // ///////////////////////////////

  // Replaced by HashMap contexts; Sanjib Saha - Jan 12, 2015
  // protected boolean negated = false;

  /*
   * This method was deprecated because tokens can extend over multiple lines
   */
  @Deprecated
  public Integer get_line_index() {
    if (this.documentLines == null || this.documentLines.isEmpty()) {
      return -1;
    }
    return -1;
  }

  public int get_start_offset_as_int() {
    return Integer.parseInt(getStartOffset());
  }

  public Collection<String> getAssignedConcepts() {
    return this.assignedConcepts;
  }

  public HashSet<String> getBlocked() {
    return this.blockedConcepts;
  }

  /**
   * Returns the corresponding value associated with the context for the token. Returns string
   * "null" if there is no context value found.
   *
   * <p>
   * Sanjib Saha; Jan 12, 2015
   *
   * @param key
   * @return
   */
  public String getContextValue(final ContextType key) {
    final String typeName = key.getTypeName();
    if (this.contexts.containsKey(typeName) || this.contexts.containsKey(typeName.toLowerCase())) {
      return this.contexts.get(typeName);
    }

    return null;
  }

  public List<TextDocumentLine> getDocumentLines() {
    return getDocumentOfOrigin().getTextDocumentLines();
  }

  /** @return the RaptatDocument instance containing this token */
  public RaptatDocument getDocumentOfOrigin() {
    return this.sentenceOfOrigin.getDocumentOfOrigin();
  }

  public String getEndOffset() {
    return this.endOff;
  }

  public HashSet<String> getFacilitated() {
    return this.facilitatedConcepts;
  }

  /** @return the negationTag */
  public String getNegexNegationTag() {
    return this.negexNegationTag;
  }

  /** @return the nextToken */
  public RaptatToken getNextToken() {
    return this.nextToken;
  }

  /** @return the parentToken */
  public RaptatToken getParentToken() {
    return this.parentToken;
  }

  public String getPOS() {
    return this.tokenPOS;
  }

  /** @return the previousToken */
  public RaptatToken getPreviousToken() {
    return this.previousToken;
  }

  public Set<RaptatTokenPhrase> getRaptatTokenPhrases() {
    return this.raptatTokenPhrases;
  }

  public String getReasonNoMedsContext() {
    return this.reasonNoMedsContext;
  }

  /** @return the sentenceOfOrigin */
  public AnnotatedPhrase getSentenceOfOrigin() {
    return this.sentenceOfOrigin;
  }


  public String getStartOffset() {
    return this.startOff;
  }

  public String getStem() {
    return this.tokenStem;
  }

  public String getTextSource() {
    return this.sentenceOfOrigin.getTextSource();
  }


  public int getTokenIndexInDocument() {
    return this.tokenIndexInDocument;
  }

  /**
   * @param startOffset
   * @param endOffset
   * @return
   */
  public String getTokenPreprocessedSubstring(final int start, final int end) {

    final int startOffsetInt = Integer.parseInt(this.startOff);
    final int endOffsetInt = Integer.parseInt(this.endOff);

    if (this.tokenStringPreprocessed.equals("") || this.startOff.equals("")
        || this.endOff.equals("") || end <= start || start > endOffsetInt
        || end <= startOffsetInt) {
      return "";
    }

    if (start > startOffsetInt) {
      if (end < endOffsetInt) {
        // Return part of string between start and end
        return this.tokenStringPreprocessed.substring(start - startOffsetInt, end - start);
      } else {
        // Return part of string starting with start
        return this.tokenStringPreprocessed.substring(start - startOffsetInt);
      }
    } else {
      if (end < endOffsetInt) {
        // Return part of string up to end offset
        return this.tokenStringPreprocessed.substring(0, end - startOffsetInt);
      } else {
        // Return entire string
        return this.tokenStringPreprocessed;
      }
    }
  }

  public String getTokenStringAugmented() {
    return this.tokenStringAugmented;
  }

  /**
   * *********************************************************** Returns the string of the original
   * token. Note that all strings are converted to lower case when used to create the tokenString
   * field of a token
   *
   * @return String
   * @author Glenn Gobbel - Feb 8, 2013 ***********************************************************
   */
  public String getTokenStringPreprocessed() {
    return this.tokenStringPreprocessed;
  }

  /** @return the taggedTokenString */
  public String getTokenStringTagged() {
    return this.tokenStringTagged;
  }

  /** @return the tokenStringUnprocessed */
  public String getTokenStringUnprocessed() {
    return this.tokenStringUnprocessed;
  }

  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @return
   * @author Glenn Gobbel - Sep 11, 2012 ***********************************************************
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.endOff == null ? 0 : this.endOff.hashCode());
    result =
        prime * result + (this.sentenceOfOrigin == null ? 0 : this.sentenceOfOrigin.hashCode());
    result = prime * result + (this.startOff == null ? 0 : this.startOff.hashCode());
    result = prime * result
        + (this.tokenStringPreprocessed == null ? 0 : this.tokenStringPreprocessed.hashCode());
    return result;
  }

  /** @return the isProcessed */
  public boolean isProcessed() {
    return this.isProcessed;
  }

  /**
   * Updates the context map of the Token with the context and the corresponding value
   *
   * <p>
   * Sanjib Saha; Jan 12, 2015
   *
   * @param key
   * @param value
   */
  public void putContextValue(final ContextType contextType, final String value) {
    if (contextType.getPotentialValues().contains(value)) {
      this.contexts.put(contextType.getTypeName().toLowerCase(), value);
    } else {
      final String errorString =
          "ContextType:" + contextType.toString() + " can not be assigned the value:" + value;
      GeneralHelper.errorWriter(errorString);
      throw new IllegalArgumentException(errorString);
    }
  }

  public boolean raptatConceptAssigned() {
    return this.raptatConceptAssigned;
  }

  public void resetAugmentedUsingOptions(final boolean usePOS) {}

  public void set_containing_text_document_line(final TextDocumentLine current_line) {
    this.documentLines.clear();
    this.documentLines.add(current_line);
  }

  public void setContextValue(final ContextType type, final String value) {
    putContextValue(type, value);
  }

  public void setDocumentLine(final TextDocumentLine textDocumentLine) {
    this.documentLines.clear();
    this.documentLines.add(textDocumentLine);
  }


  public void setDocumentLines(final Collection<TextDocumentLine> textDocumentLines) {
    this.documentLines.clear();
    this.documentLines.addAll(textDocumentLines);
  }

  public void setEndOff(final String end) {
    this.endOff = end;
  }

  /**
   * @param nextToken the nextToken to set
   */
  public void setNextToken(final RaptatToken nextToken) {
    this.nextToken = nextToken;
  }

  /**
   * @param parentToken the parentToken to set
   */
  public void setParentToken(final RaptatToken parentToken) {
    this.parentToken = parentToken;
  }

  public void setPOS(final String pos) {
    this.tokenPOS = pos;
  }

  /**
   * @param previousToken the previousToken to set
   */
  public void setPreviousToken(final RaptatToken previousToken) {
    this.previousToken = previousToken;
  }

  /**
   * @param isProcessed
   */
  public void setProcessed(final boolean isProcessed) {
    this.isProcessed = isProcessed;
  }

  public void setRaptatConceptAssigned(final boolean recognizedByRaptat) {
    this.raptatConceptAssigned = recognizedByRaptat;
  }

  public void setRaptatTokenPhrases(final Collection<RaptatTokenPhrase> raptatTokenPhrases) {
    this.raptatTokenPhrases = new HashSet<>(raptatTokenPhrases);
  }

  /**
   * @param reasonNoMedsTag
   */
  public void setReasonNoMedsContext(final String contextTag) {
    this.reasonNoMedsContext = contextTag;
  }

  /**
   * @param sentenceOfOrigin the sentenceOfOrigin to set
   */
  public void setSentenceOfOrigin(final AnnotatedPhrase sentenceOfOrigin) {
    this.sentenceOfOrigin = sentenceOfOrigin;
  }

  public void setStartOff(final String start) {
    this.startOff = start;
  }

  /*
   * BEGIN - DAX - 5/7/19
   *
   * Added elements to RaptatToken to support column and row assignment
   */

  public void setStem(final String stem) {
    this.tokenStem = stem;
  }

  public void setTokenStringAugmented(final String augmentedString) {
    this.tokenStringAugmented = augmentedString;
  }

  public void setTokenStringPreprocessed(final String token) {
    this.tokenStringPreprocessed = token;
  }

  /**
   * Sets the taggedTokenString to the String provided as a parameter. Commonly used during CRF
   * feature building to mark tokens that are part of annotated phrases - it is used to determine
   * the label to associate with tokens.
   *
   * @param taggedTokenString
   */
  public void setTokenStringTagged(final String taggedTokenString) {
    this.tokenStringTagged = taggedTokenString;
  }

  /*
   * END - DAX - 5/7/19
   *
   * Added elements to RaptatToken to support column and row assignment
   */

  /**
   * @param tokenStringUnprocessed the tokenStringUnprocessed to set
   */
  public void setTokenStringUnprocessed(final String tokenStringUnprocessed) {
    this.tokenStringUnprocessed = tokenStringUnprocessed;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, RaptatStringStyle.getInstance())
        .append("TokenString", this.tokenStringPreprocessed).append("Stem", this.tokenStem)
        .append("AugmentedString", this.tokenStringAugmented).append("POS", this.tokenPOS)
        .append("StartOffset", this.startOff).append("EndOffset", this.endOff).toString();
    // return ( "{" + this.tokenString + ", " + this.tokenStem + ", " +
    // this.tokenStringAugmented + ", "
    // + this.tokenPOS + ", " + this.startOff + ":" + this.endOff + "}" );
  }

  public static boolean containsDictionaryPrecedentToken(final List<RaptatToken> tokenSequence) {
    for (final RaptatToken raptatToken : tokenSequence) {
      if (raptatToken.dictionaryConceptAssignedAndPrecedent) {
        return true;
      }
    }
    return false;
  }


  /**
   * Create an indexed tree out of a list of tokens so that the tree can be used to identify tokens
   * within a range of values making up a phrase or sentence
   *
   * @param theTokens
   *
   * @author Glenn Gobbel - Jun 28, 2012
   *
   * @return
   */
  public static IndexedTree<IndexedObject<RaptatToken>> generateIndexedTree(
      final List<RaptatToken> theTokens) {
    IndexedObject<RaptatToken> curObject;
    final List<IndexedObject<RaptatToken>> tokensAsObjects = new ArrayList<>(theTokens.size());
    for (final RaptatToken curToken : theTokens) {
      curObject = new IndexedObject<>(Integer.parseInt(curToken.getStartOffset()), curToken);
      tokensAsObjects.add(curObject);
    }
    return new IndexedTree<>(tokensAsObjects);
  }


  public static String getPreprocessedStringFromTokens(final List<RaptatToken> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      return null;
    }

    final Iterator<RaptatToken> iterator = tokens.iterator();
    final StringBuilder sb = new StringBuilder(iterator.next().tokenStringPreprocessed);

    while (iterator.hasNext()) {
      sb.append(" ").append(iterator.next().tokenStringPreprocessed);
    }

    return sb.toString();
  }



  public static String[] getPreprocessedTokenStringSequence(final List<RaptatToken> tokenSequence) {
    final String[] result = new String[tokenSequence.size()];
    int i = 0;
    for (final RaptatToken curToken : tokenSequence) {
      result[i++] = curToken.tokenStringPreprocessed;
    }
    return result;
  }


  /**
   * Given an indexedTree of RaptatToken-based objects, finds the tokens beginning with office start
   * (inclusive of RaptatToken) to end (exclusive of RaptatToken objects that begin at or after end
   * value)
   *
   * @param tokenTree
   * @param resultTokenList
   * @param start
   * @param end
   */
  public static List<RaptatToken> getTokensCoveringRange(
      final IndexedTree<IndexedObject<RaptatToken>> tokenTree, final int start, final int end) {

    List<RaptatToken> resultTokenList = new ArrayList<>();
    IndexedTreeIterator tokenIterator = tokenTree.iterator(start);

    while (tokenIterator.hasNext()) {
      RaptatToken token = (RaptatToken) tokenIterator.next().getNode().getIndexedObject();
      if (token.get_start_offset_as_int() >= end) {
        break;
      }
      resultTokenList.add(token);
    }

    return resultTokenList;
  }

  /**
   * Given an indexedTree of RaptatToken-based objects, finds the tokens beginning with office start
   * (inclusive of RaptatToken) to end (exclusive of RaptatToken objects that begin at or after end
   * value)
   *
   * @param tokenTree
   * @param resultTokenList
   * @param start
   * @param end
   */
  @Deprecated
  public static List<RaptatToken> getTokensCoveringRangeOld(
      final IndexedTree<IndexedObject<RaptatToken>> tokenTree, final int start, final int end) {

    List<RaptatToken> resultTokenList = new ArrayList<>();
    RaptatToken currentToken = tokenTree.getClosestLesserOrEqualObject(start).getIndexedObject();
    resultTokenList.add(currentToken);

    while ((currentToken = currentToken.getNextToken()) != null
        && Integer.parseInt(currentToken.endOff) <= end) {
      resultTokenList.add(currentToken);
    }

    return resultTokenList;
  }


  public static String getUnprocessedStringFromTokens(final List<RaptatToken> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      return null;
    }

    final Iterator<RaptatToken> iterator = tokens.iterator();
    final StringBuilder sb = new StringBuilder(iterator.next().tokenStringUnprocessed);

    while (iterator.hasNext()) {
      sb.append(" ").append(iterator.next().tokenStringUnprocessed);
    }

    return sb.toString();
  }

  public static void markDictionaryConceptAssignedAndPrecedent(
      final List<RaptatToken> tokenSequence, final boolean markAsAssignedAndPrecedent) {
    for (final RaptatToken curToken : tokenSequence) {
      curToken.dictionaryConceptAssignedAndPrecedent = markAsAssignedAndPrecedent;
    }
  }

  public static void markRaptatConceptAssigned(final List<RaptatToken> tokenSequence,
      final boolean markAsAssigned) {
    for (final RaptatToken curToken : tokenSequence) {
      curToken.raptatConceptAssigned = markAsAssigned;
    }
  }

  public static boolean sequenceBlocked(final List<RaptatToken> tokenSequence,
      final String conceptName) {
    for (final RaptatToken curToken : tokenSequence) {
      if (curToken.blockedConcepts.contains(conceptName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean sequenceFacilitated(final List<RaptatToken> tokenSequence,
      final String conceptName) {
    for (final RaptatToken curToken : tokenSequence) {
      if (curToken.facilitatedConcepts.contains(conceptName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean startOrEndToken(final RaptatToken curToken) {
    if (curToken.tokenStringPreprocessed.equals(RaptatConstants.SENTENCE_START_STRING)
        || curToken.tokenStringPreprocessed.equals(RaptatConstants.SENTENCE_END_STRING)) {
      return true;
    }
    return false;
  }
}
