package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import opennlp.tools.util.Span;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.sql.AnnotatedPhraseFields;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * ****************************************************** Stores information about a sequence of
 * tokens that might correspond to a sequence of words in a phrase or an entire sentence. Tokens are
 * stored as both a processedTokens list and a rawTokens list. "Raw" tokens are those that were not
 * lost due to stop word removal. Note that there is no guarantee that tokens in the raw token list
 * are different from those in the processed token list (i.e. they may refer to the the same token
 * object).
 *
 * <p>
 * Note that the processedStartOffset and processedEndOffset may differ from rawTokensStartOffset
 * and rawTokensEndOffset due to removal of stop words. The processed start offset corresponds to
 * the start offset of the raw token that is the same as the start offset of the first token in the
 * processed tokens list. The processed end offset is the same as the end offset of the last token
 * in the processed tokens list.
 *
 * @author Glenn Gobbel - Jul 12, 2012 *****************************************************
 */
public class AnnotatedPhrase extends RaptatTokenSequence
    implements Serializable, Comparable<AnnotatedPhrase>, Cloneable {
  private String mentionID = "";

  private String annotatorName = "";

  private String annotatorID = "";

  private String rawTokensStartOffset = "";

  private String rawTokensEndOffset = "";

  private String phraseStringUnprocessed = "";

  private String conceptName = "";

  private List<RaptatToken> processedTokens = new ArrayList<>();

  private String processedTokensStartOffset = "-1";

  private String processedTokensEndOffset = "-1";
  private String creationDate = "";
  // "Raw" tokens are those that were not lost due to stop word removal.
  // There is no guarantee that tokens in the raw token list are different
  // from those in the processed token list (i.e. they may refer to the
  // the same token object).
  private List<RaptatToken> rawTokens = new ArrayList<>();
  private int rawTokenNumber;
  private boolean dictionaryBasedPhrase = false;
  private RaptatDocument documentOfOrigin = null;

  private int indexInDocument = 0;

  /** List of annotated phrase attributes */
  private List<RaptatAttribute> phraseAttributes = new ArrayList<>();

  /** List of relation among annotated phrases */
  private List<ConceptRelation> conceptRelations = new ArrayList<>();

  private AnnotatedPhrase prevPhrase = null;

  private AnnotatedPhrase nextPhrase = null;

  /**
   * This field is only used if this AnnotatedPhrase instance corresponds to a sentence. We set up a
   * separate field from prevPhrase because prevPhrase is used in the hashCode to detect equivalence
   * when AnnotatePhrase instances that are not sentences are linked and have discontinuous spans
   */
  private AnnotatedPhrase previousSentence = null;

  /**
   * This field is only used if this AnnotatedPhrase instance corresponds to a sentence. We set up a
   * separate field from nextPhrase because nextPhrase is used in the hashCode to detect equivalence
   * when AnnotatePhrase instances that are not sentences are linked and have discontinuous spans
   */
  private AnnotatedPhrase nextSentence = null;

  /*
   * Used for marking sentences, which are generally stored in documents as annotated phrases, with
   * any concepts that may occur in the sentence. The concept maps to the number of times the
   * concept occurs. If the number of times the concept is included is not accurate, the mapped
   * value should be set to -1
   */
  private HashMap<String, Integer> sentenceAssociatedConcepts = new HashMap<>(8);

  private static final long serialVersionUID = 5416739163794011765L;

  protected static Logger logger = Logger.getLogger(AnnotatedPhrase.class);

  /**
   * Creates copy of input annotatedPhrase parameter except for the mentionID, which is generated
   * anew. Note that this annotatedPhrase and the one supplied as a parameter will reference the
   * same objects via all other fields in the objects.
   *
   * @param annotatedPhrase
   */
  public AnnotatedPhrase(final AnnotatedPhrase annotatedPhrase) {
    this.mentionID = UniqueIDGenerator.INSTANCE.getUnique();
    this.annotatorName = annotatedPhrase.annotatorName;
    this.annotatorID = annotatedPhrase.annotatorID;
    this.rawTokensStartOffset = annotatedPhrase.rawTokensStartOffset;
    this.rawTokensEndOffset = annotatedPhrase.rawTokensEndOffset;
    this.phraseStringUnprocessed = annotatedPhrase.phraseStringUnprocessed;
    this.conceptName = annotatedPhrase.conceptName;
    this.processedTokens = annotatedPhrase.processedTokens;
    this.processedTokensStartOffset = annotatedPhrase.processedTokensStartOffset;
    this.creationDate = annotatedPhrase.creationDate;
    this.rawTokens = annotatedPhrase.rawTokens;
    this.dictionaryBasedPhrase = annotatedPhrase.dictionaryBasedPhrase;
    this.documentOfOrigin = annotatedPhrase.documentOfOrigin;
    this.indexInDocument = annotatedPhrase.indexInDocument;
    this.phraseAttributes = annotatedPhrase.phraseAttributes;
    this.conceptRelations = annotatedPhrase.conceptRelations;
    this.prevPhrase = annotatedPhrase.prevPhrase;
    this.nextPhrase = annotatedPhrase.nextPhrase;
    this.previousSentence = annotatedPhrase.previousSentence;
    this.nextSentence = annotatedPhrase.nextSentence;
    this.sentenceAssociatedConcepts = annotatedPhrase.sentenceAssociatedConcepts;
  }

  /**
   * @param phraseFields
   */
  public AnnotatedPhrase(final AnnotatedPhraseFields phraseFields) {
    this.annotatorID = phraseFields.annotatorID;
    this.annotatorName = phraseFields.annotatorName;
    this.conceptName = phraseFields.conceptName;
    this.mentionID = phraseFields.mentionID;
    this.rawTokensStartOffset = phraseFields.startOffset;
    this.rawTokensEndOffset = phraseFields.endOffset;
    this.phraseStringUnprocessed = phraseFields.phraseText;
    this.phraseAttributes = phraseFields.attributes;
    this.conceptRelations = phraseFields.conceptRelations;
    /** 6/12/19 - DAX - Added reference to create date */
    this.creationDate = phraseFields.creationDate;
  }

  /**
   * Constructor used specifically by splitPhrase() method of a PhraseTokenIntegrator instance
   * annotation data, attribute list and relationship list. In this case, the annPhrase field is
   * created based on the tokens, which are processed, so it will not correspond exactly to what is
   * in the document as stop words may be removed. By design decision, a non-sentence annotated
   * phrase does not contain raw tokens though it does contain raw offsets. Since there are no true
   * raw tokens, rawTokenNumber is set to the number of processed tokens.
   *
   * @param phraseTokens
   * @param documentOfOrigin
   * @param annConcept
   * @param mentionID
   * @param annotatorName
   * @param annotatorID
   * @param index
   * @param attributes
   * @param relations
   * @author Sanjib Saha - Dec 2, 2014
   */
  public AnnotatedPhrase(final List<RaptatToken> phraseTokens,
      final RaptatDocument documentOfOrigin, final String annConcept, final String mentionID,
      final String annotatorName, final String annotatorID, final int index,
      final List<RaptatAttribute> attributes, final List<ConceptRelation> relations) {
    if (phraseTokens != null) {
      this.processedTokensStartOffset = phraseTokens.get(0).getStartOffset();
      this.processedTokensEndOffset = phraseTokens.get(phraseTokens.size() - 1).getEndOffset();
      this.processedTokens = phraseTokens;
    } else {
      /*
       * This should never occur but we put here for safety and to induce error if called and
       * attempted for use
       */
      this.processedTokensStartOffset = "-1";
      this.processedTokensEndOffset = "-1";
      this.processedTokens = new ArrayList<>();
    }

    this.rawTokensStartOffset = this.processedTokensStartOffset;
    this.rawTokensEndOffset = this.processedTokensEndOffset;
    this.rawTokenNumber = this.processedTokens.size();

    Iterator<RaptatToken> tokenIterator = phraseTokens.iterator();
    StringBuilder thePhrase = new StringBuilder(tokenIterator.next().tokenStringPreprocessed);
    while (tokenIterator.hasNext()) {
      thePhrase.append(" ").append(tokenIterator.next().tokenStringPreprocessed);
    }
    this.phraseStringUnprocessed = thePhrase.toString();

    this.documentOfOrigin = documentOfOrigin;
    this.conceptName = annConcept.toLowerCase();
    this.mentionID = mentionID;
    this.annotatorName = annotatorName;
    this.annotatorID = annotatorID;
    this.indexInDocument = index;
    this.creationDate = GeneralHelper.getTimeStampLong();

    if (attributes != null) {
      Collections.sort(attributes);
      this.phraseAttributes = attributes;
    } else {
      this.phraseAttributes = new ArrayList<>();
    }
    if (relations != null) {
      Collections.sort(relations);
      this.conceptRelations = relations;
    } else {
      this.conceptRelations = new ArrayList<>();
    }
  }

  /**
   * ******************************************** Constructs an annotated phrase. Used by
   * processDocument to build a sentence. It is assume that the processedTokens refer to rawTokens,
   * so, when this method sets sentenceOfOrigin, it only does it for the raw tokens thus covering
   * the processedTokens also.
   *
   * @param thePhrase
   * @param theOffsets
   * @param phraseTokens
   * @param addTokensAsSentence
   * @param previousSentence
   * @param documentOfOrigin
   * @param sentenceConcepts
   * @author Glenn Gobbel - Oct 19, 2012 ********************************************
   */
  public AnnotatedPhrase(final String thePhrase, final Span theOffsets,
      final RaptatPair<List<RaptatToken>, List<RaptatToken>> phraseTokens,
      final boolean addTokensAsSentence, final AnnotatedPhrase previousSentence,
      final RaptatDocument documentOfOrigin, final Set<String> sentenceConcepts,
      final String annotatorID, final String annotatorName) {
    this.rawTokensStartOffset = String.valueOf(theOffsets.getStart());
    this.rawTokensEndOffset = String.valueOf(theOffsets.getEnd());
    this.phraseStringUnprocessed = thePhrase;
    this.rawTokens = phraseTokens.left;
    if (this.rawTokens == null) {
      this.rawTokens = new ArrayList<>();
    }
    this.rawTokenNumber = this.rawTokens.size();

    this.processedTokens = phraseTokens.right;
    if (this.processedTokens == null || this.processedTokens.size() < 1) {
      this.processedTokens = new ArrayList<>();
      this.processedTokensStartOffset = "-1";
      this.processedTokensEndOffset = "-1";
    } else {
      this.processedTokensStartOffset = this.processedTokens.get(0).startOff;
      this.processedTokensEndOffset =
          this.processedTokens.get(this.processedTokens.size() - 1).endOff;
    }

    this.previousSentence = previousSentence;
    if (previousSentence != null) {
      previousSentence.nextSentence = this;
    }

    this.documentOfOrigin = documentOfOrigin;

    if (sentenceConcepts != null) {
      /*
       * We assign -1 to the concept because this is normally used to count how many times the
       * concept occurs in the sentence, and here we are only using the map in a binary fashion
       * (i.e. present or absent)
       */
      for (String curConcept : sentenceConcepts) {
        this.sentenceAssociatedConcepts.put(curConcept, -1);
      }
    }
    this.annotatorID = annotatorID;
    this.annotatorName = annotatorName;
    this.creationDate = GeneralHelper.getTimeStampLong();

    // If the phrase is a sentence, each token should contains a reference
    // to the sentence it comes from for use in evaluation
    if (addTokensAsSentence) {
      for (RaptatToken curToken : this.rawTokens) {
        curToken.setSentenceOfOrigin(this);
      }
    }
  }

  /**
   * ******************************************** Constructor used by XMLImporter in the
   * buildAnnotationTrainingList() method. Note that it does not assign a value to the
   * rawTokenNumber field.
   *
   * @param annotationData
   * @author Glenn Gobbel - Feb 8, 2013 ********************************************
   */
  public AnnotatedPhrase(final String[] annotationData) {
    this.mentionID = annotationData[AnnotationDataIndex.MENTION_ID.ordinal()];
    this.rawTokensStartOffset = annotationData[AnnotationDataIndex.START_OFFSET.ordinal()];
    this.rawTokensEndOffset = annotationData[AnnotationDataIndex.END_OFFSET.ordinal()];
    this.phraseStringUnprocessed = annotationData[AnnotationDataIndex.SPAN_TEXT.ordinal()];
    this.annotatorID = annotationData[AnnotationDataIndex.ANNOTATOR_NAME.ordinal()];
    this.conceptName = annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()].toLowerCase();
    this.creationDate = GeneralHelper.getTimeStampLong();
  }

  /**
   * Constructor for AnnotationPhrase using annotation data, attribute list and relationship list
   *
   * @author Sanjib Saha - Jul 30, 2014
   * @param annotationData annotation data
   * @param attributes list of attributes
   * @param relations list of relations
   */
  public AnnotatedPhrase(final String[] annotationData, final List<RaptatAttribute> attributes,
      final List<ConceptRelation> relations) {
    this();
    this.mentionID = annotationData[AnnotationDataIndex.MENTION_ID.ordinal()];
    this.rawTokensStartOffset = annotationData[AnnotationDataIndex.START_OFFSET.ordinal()];
    this.rawTokensEndOffset = annotationData[AnnotationDataIndex.END_OFFSET.ordinal()];
    this.phraseStringUnprocessed = annotationData[AnnotationDataIndex.SPAN_TEXT.ordinal()];
    this.annotatorID = annotationData[AnnotationDataIndex.ANNOTATOR_NAME.ordinal()];
    this.conceptName = annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()].toLowerCase();
    this.creationDate = GeneralHelper.getTimeStampLong();
    if (attributes != null) {
      Collections.sort(attributes);
      this.phraseAttributes = attributes;
    } else {
      this.phraseAttributes = new ArrayList<>();
    }
    if (relations != null) {
      Collections.sort(relations);
      this.conceptRelations = relations;
    } else {
      this.conceptRelations = new ArrayList<>();
    }
  }

  /**
   * ******************************************** Constructor used to create AnnotationPhrase
   * objects with token data
   *
   * @param annotationData
   * @param phraseTokens
   * @author Glenn Gobbel - Feb 8, 2013
   * @param phraseAttributes ********************************************
   */
  public AnnotatedPhrase(final String[] annotationData, final List<RaptatToken> phraseTokens) {
    this.mentionID = annotationData[AnnotationDataIndex.MENTION_ID.ordinal()];
    this.rawTokensStartOffset = annotationData[AnnotationDataIndex.START_OFFSET.ordinal()];
    this.rawTokensEndOffset = annotationData[AnnotationDataIndex.END_OFFSET.ordinal()];
    this.phraseStringUnprocessed = annotationData[AnnotationDataIndex.SPAN_TEXT.ordinal()];
    this.annotatorID = annotationData[AnnotationDataIndex.ANNOTATOR_NAME.ordinal()];
    this.conceptName = annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()].toLowerCase();
    if ((this.processedTokens = phraseTokens) == null || phraseTokens.isEmpty()) {
      this.processedTokens = new ArrayList<>();
      this.processedTokensStartOffset = "-1";
      this.processedTokensEndOffset = "-1";
    } else {
      AnnotatedPhrase sentenceOfOrigin = phraseTokens.get(0).getSentenceOfOrigin();
      sentenceOfOrigin.addSentenceAssociatedConcept(this.conceptName);
    }
    this.creationDate = GeneralHelper.getTimeStampLong();
  }

  protected AnnotatedPhrase() {
    AnnotatedPhrase.logger.setLevel(Level.INFO);
  }

  public void addAttribute(final RaptatAttribute attr) {
    this.phraseAttributes.add(attr);
  }

  public void addSentenceAssociatedConcept(final String concept) {
    Integer conceptOccurrences;
    if ((conceptOccurrences = this.sentenceAssociatedConcepts.get(concept)) == null) {
      conceptOccurrences = 0;
    }
    this.sentenceAssociatedConcepts.put(concept, ++conceptOccurrences);
  }

  public void addSentenceAssociatedConcepts(final Collection<AnnotatedPhrase> conceptAnnotations) {
    if (conceptAnnotations != null) {
      for (AnnotatedPhrase annotatedPhrase : conceptAnnotations) {
        addSentenceAssociatedConcept(annotatedPhrase.conceptName);
      }
    }
  }

  @Override
  public void addSequenceAssociatedConcept(final String conceptName) {
    Integer conceptOccurrences;
    if ((conceptOccurrences = this.sentenceAssociatedConcepts.get(conceptName)) == null) {
      conceptOccurrences = 0;
    }
    this.sentenceAssociatedConcepts.put(conceptName, ++conceptOccurrences);
  }

  @Override
  public AnnotatedPhrase clone() {
    try {
      return (AnnotatedPhrase) super.clone();
    } catch (CloneNotSupportedException ex) {
      ex.printStackTrace();
      AnnotatedPhrase.logger.fatal(ex);
    }

    return null;
  }

  // Modified by Sanjib Saha - Aug 1, 2014 to incorporate multiple spans
  // Modified by Sanjib Saha - Aug 19, 2015 to sort according to start offset
  // Modfied by Glenn Gobbel - Aug 19, 2016 to sort also according to document
  // when sorting phrases from multiple documents
  // Modified by Glenn Gobbel - Jan 30, 2020 to sort also on length of the phrases with longer
  // phrases coming first if start offsets and documents are the same
  @Override
  public int compareTo(AnnotatedPhrase otherPhrase) {
    AnnotatedPhrase thisPhrase = this;

    while (thisPhrase.prevPhrase != null) {
      thisPhrase = thisPhrase.prevPhrase;
    }

    while (otherPhrase.prevPhrase != null) {
      otherPhrase = otherPhrase.prevPhrase;
    }

    CompareToBuilder comparisonBuilder = new CompareToBuilder();
    int thisStartOffset = Integer.parseInt(this.rawTokensStartOffset);
    int otherStartOffset = Integer.parseInt(otherPhrase.rawTokensStartOffset);

    int thisPhraseLength = rawPhraseLength();
    int otherPhraseLength = otherPhrase.rawPhraseLength();


    comparisonBuilder.append(thisPhrase.documentOfOrigin, otherPhrase.documentOfOrigin);
    comparisonBuilder.append(thisStartOffset, otherStartOffset);

    /*
     * If thisPhraseLength > otherPhraseLength, then it should return -1 (i.e. the longer phrase
     * comes first)
     */
    comparisonBuilder.append(otherPhraseLength, thisPhraseLength);

    return comparisonBuilder.toComparison();
  }

  /**
   * OVERRIDES PARENT METHOD
   *
   * @param obj
   * @return
   * @author Glenn Gobbel - Sep 12, 2012
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

    AnnotatedPhrase phrase1 = this;
    AnnotatedPhrase phrase2 = (AnnotatedPhrase) obj;

    while (phrase1.prevPhrase != null) {
      phrase1 = phrase1.prevPhrase;
    }

    while (phrase2.prevPhrase != null) {
      phrase2 = phrase2.prevPhrase;
    }

    AnnotatedPhrase head1 = phrase1;
    AnnotatedPhrase head2 = phrase2;

    // check if the number of spans are equal in both annotations
    while (phrase1 != null && phrase2 != null) {
      phrase1 = phrase1.nextPhrase;
      phrase2 = phrase2.nextPhrase;
    }

    if (phrase1 != null || phrase2 != null) {
      return false;
    }

    // check if the annotated phrases are equal in both annotations
    phrase1 = head1;
    phrase2 = head2;

    while (phrase1 != null) {
      if (phrase1.phraseStringUnprocessed == null) {
        if (phrase2.phraseStringUnprocessed != null) {
          return false;
        }
      } else if (!phrase1.phraseStringUnprocessed.equals(phrase2.phraseStringUnprocessed)) {
        return false;
      }

      phrase1 = phrase1.nextPhrase;
      phrase2 = phrase2.nextPhrase;
    }

    // check if the document of origins are equal in both annotations
    phrase1 = head1;
    phrase2 = head2;

    while (phrase1 != null) {
      if (phrase1.documentOfOrigin == null) {
        if (phrase2.documentOfOrigin != null) {
          return false;
        }
      } else if (!phrase1.documentOfOrigin.equals(phrase2.documentOfOrigin)) {
        return false;
      }

      phrase1 = phrase1.nextPhrase;
      phrase2 = phrase2.nextPhrase;
    }

    // check if the raw end offsets are equal in both annotations
    phrase1 = head1;
    phrase2 = head2;

    while (phrase1 != null) {
      if (phrase1.rawTokensEndOffset == null) {
        if (phrase2.rawTokensEndOffset != null) {
          return false;
        }
      } else if (!phrase1.rawTokensEndOffset.equals(phrase2.rawTokensEndOffset)) {
        return false;
      }

      phrase1 = phrase1.nextPhrase;
      phrase2 = phrase2.nextPhrase;
    }

    // check if the raw start offsets are equal in both annotations
    phrase1 = head1;
    phrase2 = head2;

    while (phrase1 != null) {
      if (phrase1.rawTokensStartOffset == null) {
        if (phrase2.rawTokensStartOffset != null) {
          return false;
        }
      } else if (!phrase1.rawTokensStartOffset.equals(phrase2.rawTokensStartOffset)) {
        return false;
      }

      phrase1 = phrase1.nextPhrase;
      phrase2 = phrase2.nextPhrase;
    }

    // check if the concept relations are equal in both annotations
    phrase1 = head1;
    phrase2 = head2;

    while (phrase1 != null) {
      if (phrase1.conceptRelations == null) {
        if (phrase2.conceptRelations != null) {
          return false;
        }
      } else if (!phrase1.conceptRelations.equals(phrase2.conceptRelations)) {
        return false;
      }

      phrase1 = phrase1.nextPhrase;
      phrase2 = phrase2.nextPhrase;
    }

    return true;
  }

  public String getAnnotatorID() {
    return this.annotatorID;
  }

  public String getAnnotatorName() {
    return this.annotatorName;
  }

  public AnnotatedPhrase getAssociatedSentence() {
    if (this.processedTokens != null && this.processedTokens.size() > 0) {
      AnnotatedPhrase sentence = this.processedTokens.get(0).sentenceOfOrigin;
      if (sentence != null) {
        return sentence;
      }
    }
    return null;
  }

  /*
   * Creates a single name for an annotated phrase based on not only the concept name but also the
   * attributes, attribute values, and relationships
   */
  public String getCanonicalName(final String prependString) {
    StringBuilder sb = new StringBuilder(prependString);
    sb.append("\tCON\t");
    sb.append(this.conceptName);
    sb.append("\t");
    String attributeRelationshipPrepend = sb.toString();
    sb.append("\n");

    /*
     * Now add lines for each attribute-value combo associated with the concept
     */
    for (RaptatAttribute attribute : this.phraseAttributes) {
      sb.append(attribute.getCanonicalName(attributeRelationshipPrepend));
    }

    /*
     * Now add lines for each relationship-attribute-value combo associated with this concept
     */
    for (ConceptRelation relation : getConceptRelations()) {
      sb.append(relation.getCanonicalName(attributeRelationshipPrepend));
    }
    return sb.toString();
  }

  public String getConceptName() {
    return this.conceptName;
  }

  /**
   * Returns the list of annotated phrase relations
   *
   * @return list of relations
   */
  public List<ConceptRelation> getConceptRelations() {
    return this.conceptRelations;
  }

  public String getCreationDate() {
    return this.creationDate;
  }

  public RaptatDocument getDocumentOfOrigin() {
    return this.documentOfOrigin;
  }

  /** @return the indexInDocument */
  public int getIndexInDocument() {
    return this.indexInDocument;
  }

  public String getMentionId() {
    return this.mentionID;
  }

  /**
   * Returns the next part of the complex annotation
   *
   * @return next annotation of the chain
   */
  public AnnotatedPhrase getNextPhrase() {
    return this.nextPhrase;
  }

  /** @return the nextSentence */
  public AnnotatedPhrase getNextSentence() {
    return this.nextSentence;
  }

  /**
   * Returns the list of annotated phrase attributes
   *
   * @return list of attributes
   */
  public List<RaptatAttribute> getPhraseAttributes() {
    return this.phraseAttributes;
  }

  public String getPhraseStringUnprocessed() {
    return this.phraseStringUnprocessed;
  }

  /** @return the previousSentence */
  public AnnotatedPhrase getPreviousSentence() {
    return this.previousSentence;
  }

  /**
   * Returns the previous part of the complex annotation
   *
   * @return previous annotation of the chain
   */
  public AnnotatedPhrase getPrevPhrase() {
    return this.prevPhrase;
  }

  public List<RaptatToken> getProcessedTokens() {
    return this.processedTokens;
  }

  /**
   * *********************************************************** Generates "Start" and "End" tokens
   * to add to the sentence tokens and use in training
   *
   * @return List of tokens with start and end tokens at the beginning and end
   * @author Glenn Gobbel - Sep 19, 2012 ***********************************************************
   */
  public List<RaptatToken> getProcessedTokensAsSentence() {
    int tokenListSize = this.processedTokens.size();
    List<RaptatToken> resultList = new ArrayList<>(tokenListSize + 2);

    String tokenStartOffset = this.processedTokens.get(tokenListSize - 1).startOff;
    tokenStartOffset = String.valueOf(Integer.parseInt(tokenStartOffset) - 1);
    String tokenEndOffset = String.valueOf(
        Integer.parseInt(tokenStartOffset) + RaptatConstants.SENTENCE_START_STRING.length());
    resultList.add(new RaptatToken(RaptatConstants.SENTENCE_START_STRING,
        RaptatConstants.SENTENCE_START_STRING, RaptatConstants.SENTENCE_START_STRING,
        RaptatConstants.SENTENCE_START_STRING, RaptatConstants.SENTENCE_START_STRING,
        tokenStartOffset, tokenEndOffset, "", false, false));
    resultList.addAll(this.processedTokens);

    tokenStartOffset = this.processedTokens.get(tokenListSize - 1).endOff;
    tokenStartOffset = String.valueOf(Integer.parseInt(tokenStartOffset) + 1);
    tokenEndOffset = String
        .valueOf(Integer.parseInt(tokenStartOffset) + RaptatConstants.SENTENCE_END_STRING.length());
    resultList.add(new RaptatToken(RaptatConstants.SENTENCE_END_STRING,
        RaptatConstants.SENTENCE_END_STRING, RaptatConstants.SENTENCE_END_STRING,
        RaptatConstants.SENTENCE_END_STRING, RaptatConstants.SENTENCE_END_STRING, tokenStartOffset,
        tokenEndOffset, "", false, false));
    return resultList;
  }

  public String getProcessedTokensAsString() {
    Iterator<RaptatToken> tokenIterator = this.processedTokens.iterator();

    if (!tokenIterator.hasNext()) {
      return "";
    }
    StringBuilder sb = new StringBuilder(tokenIterator.next().tokenStringPreprocessed);

    while (tokenIterator.hasNext()) {
      sb.append(" ").append(tokenIterator.next().tokenStringPreprocessed);
    }

    return sb.toString();
  }

  public List<String> getProcessedTokensAugmentedStrings() {
    List<String> result = new ArrayList<>(this.processedTokens.size());
    for (RaptatToken curToken : this.processedTokens) {
      result.add(curToken.getTokenStringAugmented());
    }

    return result;
  }

  public List<String> getProcessedTokensAugmentedStringsAsSentence() {
    List<String> result = new ArrayList<>(this.processedTokens.size());
    result.add(RaptatConstants.SENTENCE_START_STRING);
    for (RaptatToken curToken : this.processedTokens) {
      result.add(curToken.getTokenStringAugmented());
    }
    result.add(RaptatConstants.SENTENCE_END_STRING);

    return result;
  }

  /** @return the processedEndOffset */
  public String getProcessedTokensEndOffset() {
    return this.processedTokensEndOffset;
  }

  public List<String> getProcessedTokensPreprocessedStrings() {
    List<String> result = new ArrayList<>(this.processedTokens.size());
    for (RaptatToken curToken : this.processedTokens) {
      result.add(curToken.getTokenStringPreprocessed());
    }

    return result;
  }

  /** @return the processedStartOffset */
  public String getProcessedTokensStartOffset() {
    return this.processedTokensStartOffset;
  }

  public int getRawTokenNumber() {
    return this.rawTokenNumber;
  }

  public List<RaptatToken> getRawTokens() {
    return this.rawTokens;
  }

  public String[] getRawTokensAugmentedStringsAsArray() {
    String[] resultArray = null;
    if (this.rawTokens.size() > 0) {
      resultArray = new String[this.rawTokens.size()];
    }

    Iterator<RaptatToken> tokenIterator = this.rawTokens.iterator();
    int tokenIndex = 0;
    while (tokenIterator.hasNext()) {
      resultArray[tokenIndex++] = tokenIterator.next().getTokenStringAugmented();
    }
    return resultArray;
  }

  @Override
  public String getRawTokensEndOff() {
    return this.rawTokensEndOffset;
  }

  public List<String> getRawTokensPreprocessedStrings() {
    List<String> result = new ArrayList<>(this.rawTokens.size());
    for (RaptatToken curToken : this.rawTokens) {
      result.add(curToken.getTokenStringPreprocessed());
    }

    return result;
  }

  @Override
  public String getRawTokensStartOff() {
    return this.rawTokensStartOffset;
  }

  public Set<String> getSentenceAssociatedConcepts() {
    return this.sentenceAssociatedConcepts.keySet();
  }

  @Override
  public Set<String> getSequenceAssociatedConcepts() {
    return getSentenceAssociatedConcepts();
  }

  @Override
  public String getSequenceStringUnprocessed() {
    return this.phraseStringUnprocessed;

  }

  public String getTextSource() {
    return this.documentOfOrigin.getTextSource().orElse("NoTextSourceFile");
  }


  public boolean hasAttributeAndValue(final String attributeName, final String attributeValue) {
    for (RaptatAttribute attribute : this.phraseAttributes) {
      if (attribute.getName().equalsIgnoreCase(attributeName)) {
        for (String value : attribute.getValues()) {
          if (value.equalsIgnoreCase(attributeValue)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @return
   * @author Glenn Gobbel - Sep 12, 2012 Modified by Sanjib saha - Aug 4, 2014 to include multiple
   *         spans in one annotation ***********************************************************
   */
  @Override
  public int hashCode() {
    AnnotatedPhrase annotation = this;

    while (annotation.prevPhrase != null) {
      annotation = annotation.prevPhrase;
    }

    final int prime = 31;
    int result = 1;

    while (annotation != null) {
      result = prime * result + (annotation.phraseStringUnprocessed == null ? 0
          : annotation.phraseStringUnprocessed.hashCode());
      result = prime * result
          + (annotation.documentOfOrigin == null ? 0 : annotation.documentOfOrigin.hashCode());
      result = prime * result
          + (annotation.rawTokensEndOffset == null ? 0 : annotation.rawTokensEndOffset.hashCode());
      result = prime * result + (annotation.rawTokensStartOffset == null ? 0
          : annotation.rawTokensStartOffset.hashCode());
      result = prime * result
          + (annotation.conceptRelations == null ? 0 : annotation.conceptRelations.hashCode());
      result = prime * result
          + (annotation.phraseAttributes == null ? 0 : annotation.phraseAttributes.hashCode());

      annotation = annotation.nextPhrase;
    }

    return result;
  }

  /** @return the dictionaryBasedPhrase */
  public boolean isDictionaryBasedPhrase() {
    return this.dictionaryBasedPhrase;
  }

  public void print() {
    System.out.println("PHRASE:" + getPhraseStringUnprocessed()
        + (this.conceptName != null ? " | CONCEPT:" + this.conceptName : ""));
    List<RaptatToken> phraseTokens;
    if ((phraseTokens = getProcessedTokens()) != null) {
      int i = 0;
      for (RaptatToken curToken : phraseTokens) {
        System.out.println("\t(" + ++i + ") " + curToken.getTokenStringPreprocessed() + " | "
            + curToken.getTokenStringAugmented());
      }
      System.out.println();
    }
  }

  public void removeSentenceAssociatedConcepts() {
    this.sentenceAssociatedConcepts = new HashMap<>(8);
  }

  public void setAnnotatorID(final String id) {
    this.annotatorID = id;
  }

  public void setAnnotatorName(final String annotatorName) {
    this.annotatorName = annotatorName;
  }

  public void setConceptName(final String concept) {
    this.conceptName = concept;
  }

  /**
   * Returns the list of annotated phrase relations
   *
   * @return list of relations
   */
  public void setConceptRelations(final List<ConceptRelation> relations) {
    if (relations != null) {
      this.conceptRelations = relations;
    } else {
      this.conceptRelations = new ArrayList<>();
    }
  }

  /**
   * @param dictionaryBasedPhrase the dictionaryBasedPhrase to set
   */
  public void setDictionaryBasedPhrase(final boolean dictionaryBasedPhrase) {
    this.dictionaryBasedPhrase = dictionaryBasedPhrase;
  }

  /**
   * @param indexInDocument the indexInDocument to set
   */
  public void setIndexInDocument(final int indexInDocument) {
    this.indexInDocument = indexInDocument;
  }

  public void setMentionId(final String id) {
    this.mentionID = id;
  }

  /**
   * Set the previous part of the complex annotation
   *
   * @param nextPhrase next annotated phrase
   */
  public void setNextPhrase(final AnnotatedPhrase nextPhrase) {
    this.nextPhrase = nextPhrase;
  }

  /**
   * @param nextSentence the nextSentence to set
   */
  public void setNextSentence(final AnnotatedPhrase nextSentence) {
    this.nextSentence = nextSentence;
  }

  public void setPhraseAttributes(final List<RaptatAttribute> attributes) {
    if (attributes != null) {
      Collections.sort(attributes);
      this.phraseAttributes = attributes;
    } else {
      this.phraseAttributes = new ArrayList<>();
    }
  }

  public void setPhraseStringUnprocessed(final String phrase) {
    this.phraseStringUnprocessed = phrase;
  }

  /**
   * @param previousSentence the previousSentence to set
   */
  public void setPreviousSentence(final AnnotatedPhrase previousSentence) {
    this.previousSentence = previousSentence;
  }

  /**
   * Sets the previous part of the complex annotation
   *
   * @param prevPhrase previous annotated phrase
   */
  public void setPrevPhrase(final AnnotatedPhrase prevPhrase) {
    this.prevPhrase = prevPhrase;
  }

  /**
   * @param processedEndOffset the processedEndOffset to set
   */
  public void setProcessedEndOffset(final String processedEndOffset) {
    this.processedTokensEndOffset = processedEndOffset;
  }

  /**
   * @param processedStartOffset the processedStartOffset to set
   */
  public void setProcessedStartOffset(final String processedStartOffset) {
    this.processedTokensStartOffset = processedStartOffset;
  }

  public void setProcessedTokens(final List<RaptatToken> newTokens) {
    if ((this.processedTokens = newTokens) == null) {
      this.processedTokens = new ArrayList<>();
      this.processedTokensStartOffset = "-1";
      this.processedTokensEndOffset = "-1";
    }
  }

  public void setRawEndOff(final String offset) {
    this.rawTokensEndOffset = offset;
  }

  public void setRawStartOff(final String offset) {
    this.rawTokensStartOffset = offset;
  }

  public void setRawTokenNumber(final int numberOfTokens) {
    this.rawTokenNumber = numberOfTokens;
  }

  /**
   * ***********************************
   *
   * <p>
   * Added by Glenn Gobbel on May 9, 2012 **********************************
   */
  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this, RaptatStringStyle.getInstance());
    tsb.append("Concept",
        this.conceptName != null && this.conceptName.length() > 0 ? this.conceptName
            : "NO_CONCEPT");

    StringBuilder attributeSB = new StringBuilder("\n\tAttributes:");
    attributeSB.append("[");

    Iterator<RaptatAttribute> attributeIterator = this.phraseAttributes.iterator();
    while (attributeIterator.hasNext()) {
      RaptatAttribute curPhraseAttribute = attributeIterator.next();
      attributeSB.append(curPhraseAttribute.getName()).append("=");
      Iterator<String> valueIterator = curPhraseAttribute.getValues().iterator();
      if (valueIterator.hasNext()) {
        attributeSB.append(valueIterator.next());
      }
      while (valueIterator.hasNext()) {
        attributeSB.append(",").append(valueIterator.next());
      }
      if (attributeIterator.hasNext()) {
        attributeSB.append("; ");
      }
    }
    attributeSB.append("]\n");

    tsb.append(attributeSB.toString());
    tsb.append("Phrase",
        this.phraseStringUnprocessed != null && this.phraseStringUnprocessed.length() > 0
            ? this.phraseStringUnprocessed
            : "NO_PHRASE")
        .append("MentionID",
            this.mentionID != null && this.mentionID.length() > 0 ? this.mentionID
                : "NO_MENTION_ID")
        .append("StartOffset",
            this.rawTokensStartOffset != null && this.rawTokensStartOffset.length() > 0
                ? this.rawTokensStartOffset
                : "NO_START_OFFSET")
        .append("EndOffset",
            this.rawTokensEndOffset != null && this.rawTokensEndOffset.length() > 0
                ? this.rawTokensEndOffset
                : "NO_END_OFFSET");

    StringBuilder tokenStringBuilder =
        new StringBuilder(System.lineSeparator() + "    ProcessedPhraseTokens:");
    if (this.processedTokens != null) {
      int i = 0;
      for (RaptatToken curToken : this.processedTokens) {
        tokenStringBuilder.append(System.lineSeparator() + "      " + System.lineSeparator())
            .append("Token " + i++ + ":").append(curToken.toString());
      }
    } else {
      tokenStringBuilder.append("NO_TOKENS");
    }

    return tsb.append(tokenStringBuilder.toString()).toString();
  }

  private int rawPhraseLength() {

    int rawPhraseLength = 0;

    AnnotatedPhrase startPhrase = this;
    while (startPhrase.prevPhrase != null) {
      startPhrase = startPhrase.prevPhrase;
    }

    while (startPhrase != null) {
      int startOffset = Integer.parseInt(startPhrase.rawTokensStartOffset);
      int endOffset = Integer.parseInt(startPhrase.rawTokensEndOffset);
      rawPhraseLength += endOffset - startOffset;
      startPhrase = startPhrase.nextPhrase;
    }

    return rawPhraseLength;
  }

  /**
   * Compare length of two annotated phrases, phraseOne and phraseTwo, and return -1 if phraseTwo is
   * shorter than phraseOne, 0 if they are of equal length, and 1 if phraseTwo is longer.
   *
   * @param phraseOne
   * @param phraseTwo
   * @return
   */
  public static int compareLengths(final AnnotatedPhrase phraseOne,
      final AnnotatedPhrase phraseTwo) {
    int phraseOneLength = phraseOne.rawPhraseLength();
    int phraseTwoLength = phraseTwo.rawPhraseLength();

    if (phraseTwoLength > phraseOneLength) {
      return 1;
    }

    if (phraseTwoLength < phraseOneLength) {
      return -1;
    }

    return 0;
  }

  /**
   * @param annotatedPhrases
   * @return
   */
  public static List<AnnotatedPhrase> getAssociatedSentences(
      final List<AnnotatedPhrase> annotatedPhrases) {
    HashSet<AnnotatedPhrase> sentences = new HashSet<>(annotatedPhrases.size());
    for (AnnotatedPhrase curPhrase : annotatedPhrases) {
      if (curPhrase.processedTokens != null && curPhrase.processedTokens.size() > 0) {
        AnnotatedPhrase sentence = curPhrase.processedTokens.get(0).sentenceOfOrigin;
        if (sentence != null) {
          sentences.add(sentence);
        }
      }
    }
    return new ArrayList<>(sentences);
  }

  public static void linkAnnotationsToSentences(final HashSet<AnnotatedPhrase> resultSet) {
    for (AnnotatedPhrase annotatedPhrase : resultSet) {
      if (annotatedPhrase.processedTokens != null && annotatedPhrase.processedTokens.size() > 0) {
        AnnotatedPhrase sentence = annotatedPhrase.processedTokens.get(0).sentenceOfOrigin;
        if (sentence != null) {
          sentence.addSentenceAssociatedConcept(annotatedPhrase.conceptName);
        }
      }
    }
  }

  public static void main(final String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {}
    });
  }

  public static boolean rawOffsetsOverlap(final AnnotatedPhrase phrase01,
      final AnnotatedPhrase phrase02) {
    return Integer.parseInt(phrase01.rawTokensStartOffset) < Integer
        .parseInt(phrase02.rawTokensEndOffset)
        && Integer.parseInt(phrase01.rawTokensEndOffset) > Integer
            .parseInt(phrase02.rawTokensStartOffset);
  }
}
