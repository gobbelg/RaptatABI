package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TrueFalseScores;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.AnnotationOverlapGraphEdge;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.ResultWriter;

class DetailedAnnotationComparisonWriter extends ResultWriter {
  private static final int TP = 0, FP = 1, FN = 2;

  private static Logger LOGGER = Logger.getLogger(DetailedAnnotationComparisonWriter.class);


  /**
   * ResultWriter - Class to create a file to write the results from a test of a machine learning
   * algorithm. It generates a file to write to at construction, writes a new line of text when
   * given a String array, and closes the file when prompted
   *
   * @author Glenn Gobbel, July 19, 2010
   * @param fileDirectoryName
   * @param titleAppend
   * @param resultTitles
   * @param fileTag
   * @param invertTokenSequence
   */
  public DetailedAnnotationComparisonWriter(String fileDirectoryName, String[] resultTitles,
      String fileTag) {
    this.fileTitle = fileDirectoryName + File.separator + "Detailed_AnnotationComparison_" + fileTag
        + GeneralHelper.getTimeStamp();
    initializeWriter(resultTitles);
  }


  /**
   * Sep 15, 2017
   *
   * @param referenceNodes
   * @param docSource
   */
  /*
   * Needs to be deprecated and the 'New' named methods used for replacement. The non-'New' methods
   * may have errors
   */
  @Deprecated
  public void writeCompleteFalseNegatives(List<AnnotationScorerNode> referenceNodes,
      String docSource) {
    String[] results = new String[DetailedResultsIndex.values().length];
    Arrays.fill(results, "");
    DetailedAnnotationComparisonWriter.LOGGER.debug("\nFALSE NEGATIVES:" + docSource);

    for (AnnotationScorerNode curNode : referenceNodes) {
      if (!curNode.partialMatch) {
        results[DetailedResultsIndex.DOC.ordinal()] = "'" + docSource;
        results[DetailedResultsIndex.CONCEPT_MATCH.ordinal()] = "";
        results[DetailedResultsIndex.PARTIAL_MATCH.ordinal()] = "";
        results[DetailedResultsIndex.FULL_MATCH.ordinal()] = "";
        results[DetailedResultsIndex.TRUE_POSITIVE_PARTIAL.ordinal()] = "";
        results[DetailedResultsIndex.FALSE_POSITIVE_PARTIAL.ordinal()] = "";
        results[DetailedResultsIndex.FALSE_NEGATIVE_PARTIAL.ordinal()] = "Yes";
        results[DetailedResultsIndex.TRUE_POSITIVE_FULL.ordinal()] = "";
        results[DetailedResultsIndex.FALSE_POSITIVE_FULL.ordinal()] = "";
        results[DetailedResultsIndex.FALSE_NEGATIVE_FULL.ordinal()] = "Yes";

        results[DetailedResultsIndex.RAPTAT_START.ordinal()] = "";
        results[DetailedResultsIndex.RAPTAT_CONCEPT.ordinal()] = "";
        results[DetailedResultsIndex.RAPTAT_PHRASE.ordinal()] = "";

        results[DetailedResultsIndex.REFERENCE_START.ordinal()] =
            curNode.annotatedPhrase.getRawTokensStartOff();
        results[DetailedResultsIndex.REFERENCE_CONCEPT.ordinal()] =
            curNode.annotatedPhrase.getConceptName();

        StringBuffer tokenString = new StringBuffer();
        List<RaptatToken> phraseTokenList = curNode.annotatedPhrase.getProcessedTokens();
        if (phraseTokenList.size() > 0) {
          for (RaptatToken curToken : phraseTokenList) {
            tokenString.append(curToken.getTokenStringPreprocessed()).append(" ");
          }
        }
        results[DetailedResultsIndex.REFERENCE_PHRASE.ordinal()] = tokenString.toString();
        DetailedAnnotationComparisonWriter.LOGGER.debug(tokenString.toString());

        tokenString = new StringBuffer();
        if (phraseTokenList.size() > 0) {
          AnnotatedPhrase theSentence = phraseTokenList.get(0).getSentenceOfOrigin();
          List<RaptatToken> sentenceTokenList = theSentence.getRawTokens();
          for (RaptatToken curToken : sentenceTokenList) {
            tokenString.append(curToken.getTokenStringPreprocessed()).append(" ");
          }
        }

        results[DetailedResultsIndex.SENTENCE.ordinal()] = tokenString.toString();
        DetailedAnnotationComparisonWriter.LOGGER
            .debug("Sentence is:" + results[DetailedResultsIndex.SENTENCE.ordinal()]);
        this.writeln(results, "\t");
      }
    }
  }


  /**
   * Sep 15, 2017
   *
   * @param raptatNodes
   * @param docSource
   */
  /*
   * Needs to be deprecated and the 'New' named methods used for replacement. The non-'New' methods
   * may have errors
   */
  @Deprecated
  public void writeCompleteFalsePositives(List<AnnotationScorerNode> raptatNodes,
      String docSource) {
    String[] results = new String[DetailedResultsIndex.values().length];
    Arrays.fill(results, "");
    DetailedAnnotationComparisonWriter.LOGGER.debug("FALSE POSITIVES:" + docSource);

    for (AnnotationScorerNode curNode : raptatNodes) {
      if (!curNode.partialMatch) {
        results[DetailedResultsIndex.DOC.ordinal()] = "'" + docSource;
        results[DetailedResultsIndex.CONCEPT_MATCH.ordinal()] = "";
        results[DetailedResultsIndex.PARTIAL_MATCH.ordinal()] = "";
        results[DetailedResultsIndex.FULL_MATCH.ordinal()] = "";
        results[DetailedResultsIndex.TRUE_POSITIVE_PARTIAL.ordinal()] = "";
        results[DetailedResultsIndex.FALSE_POSITIVE_PARTIAL.ordinal()] = "Yes";
        results[DetailedResultsIndex.FALSE_NEGATIVE_PARTIAL.ordinal()] = "";
        results[DetailedResultsIndex.TRUE_POSITIVE_FULL.ordinal()] = "";
        results[DetailedResultsIndex.FALSE_POSITIVE_FULL.ordinal()] = "Yes";
        results[DetailedResultsIndex.FALSE_NEGATIVE_FULL.ordinal()] = "";

        results[DetailedResultsIndex.REFERENCE_START.ordinal()] = "";
        results[DetailedResultsIndex.REFERENCE_CONCEPT.ordinal()] = "";
        results[DetailedResultsIndex.REFERENCE_PHRASE.ordinal()] = "";

        results[DetailedResultsIndex.RAPTAT_START.ordinal()] =
            curNode.annotatedPhrase.getRawTokensStartOff();
        results[DetailedResultsIndex.RAPTAT_CONCEPT.ordinal()] =
            curNode.annotatedPhrase.getConceptName();

        StringBuffer tokenString = new StringBuffer();
        List<RaptatToken> phraseTokens = curNode.annotatedPhrase.getProcessedTokens();
        if (phraseTokens.size() > 0) {
          for (RaptatToken curToken : phraseTokens) {
            tokenString.append(curToken.getTokenStringPreprocessed()).append(" ");
          }
        }
        results[DetailedResultsIndex.RAPTAT_PHRASE.ordinal()] = tokenString.toString();
        DetailedAnnotationComparisonWriter.LOGGER.debug(tokenString.toString());

        tokenString = new StringBuffer();
        if (phraseTokens.size() > 0) {
          AnnotatedPhrase theSentence = phraseTokens.get(0).getSentenceOfOrigin();
          List<RaptatToken> sentenceTokens = theSentence.getProcessedTokens();
          for (RaptatToken curToken : sentenceTokens) {
            tokenString.append(curToken.getTokenStringPreprocessed()).append(" ");
          }
        }
        results[DetailedResultsIndex.SENTENCE.ordinal()] = tokenString.toString();
        this.writeln(results, "\t");
      }
    }
  }


  public void writeOverlapScore(AnnotationScorerNode raptatNode, AnnotationScorerNode referenceNode,
      List<RaptatPair<String, Integer>> conceptValueResults, String docSource) {}


  /**
   * Writes detailed results based on the matching of the node to another, generally a reference
   * node. If it is matched, nothing is done, as the reference node will also be matched and scored.
   * However, if it is not matched, it is scored as a false positive
   *
   * <p>
   * Sep 15, 2017
   *
   * @param raptatNode
   * @param docSource
   */
  void writeRaptatNodeResult(AnnotationScorerNode raptatNode, String docSource) {
    String[] results = new String[DetailedResultsIndex.values().length];
    Arrays.fill(results, "");

    results[DetailedResultsIndex.DOC.ordinal()] = "'" + docSource;
    results[DetailedResultsIndex.FALSE_POSITIVE_PARTIAL.ordinal()] = "Yes";

    results[DetailedResultsIndex.RAPTAT_START.ordinal()] =
        raptatNode.annotatedPhrase.getRawTokensStartOff();
    results[DetailedResultsIndex.RAPTAT_CONCEPT.ordinal()] =
        raptatNode.annotatedPhrase.getConceptName();
    results[DetailedResultsIndex.RAPTAT_MENTION_ID.ordinal()] =
        raptatNode.annotatedPhrase.getMentionId();

    StringBuffer tokenString = new StringBuffer();
    List<RaptatToken> phraseTokenList = raptatNode.annotatedPhrase.getProcessedTokens();
    if (phraseTokenList.size() > 0) {
      for (RaptatToken curToken : phraseTokenList) {
        {
          tokenString.append(curToken.getTokenStringPreprocessed()).append(" ");
        }
      }
    }
    results[DetailedResultsIndex.RAPTAT_PHRASE.ordinal()] = tokenString.toString();

    tokenString = new StringBuffer();

    /*
     * Note that this is only checking the tokens from the reference node, so we only create a
     * sentence if the reference node has phrase tokens
     */
    if (phraseTokenList.size() > 0) {
      AnnotatedPhrase theSentence = phraseTokenList.get(0).getSentenceOfOrigin();
      List<RaptatToken> sentenceTokens = theSentence.getRawTokens();
      for (RaptatToken curToken : sentenceTokens) {
        tokenString.append(curToken.getTokenStringPreprocessed()).append(" ");
      }
    }

    results[DetailedResultsIndex.SENTENCE.ordinal()] = tokenString.toString();
    this.writeln(results, "\t");
  }


  /**
   * Writes detailed results based on the matching of the node to another, generally
   * raptat-generated node. If it is matched, it's marked as a true positive. Otherwise, it's scored
   * as a false negative. Sep 15, 2017
   *
   * @param performanceScorer
   * @param referenceNode
   * @param referenceAnnotationMap
   * @param raptatAnnotationMap
   * @param connectionGraphMap
   * @param schemaConceptMap
   * @param docSource
   */
  void writeReferenceNodeResult(PerformanceScorer performanceScorer,
      AnnotationScorerNode referenceNode,
      Hashtable<String, AnnotationScorerNode> raptatAnnotationMap,
      Hashtable<String, AnnotationScorerNode> referenceAnnotationMap,
      HashMap<String, SchemaConcept> schemaConceptMap, String docSource) {
    String[] conceptResultsStringArray = new String[DetailedResultsIndex.values().length];
    Arrays.fill(conceptResultsStringArray, "");

    /*
     * First fill the conceptResultsStringArray, which will be written to disk, with the "basic"
     * information, which excludes attributes and relationships
     */
    conceptResultsStringArray[DetailedResultsIndex.DOC.ordinal()] = "'" + docSource;

    conceptResultsStringArray[DetailedResultsIndex.PARTIAL_MATCH.ordinal()] =
        referenceNode.partialMatch ? "Yes" : "";
    conceptResultsStringArray[DetailedResultsIndex.FULL_MATCH.ordinal()] =
        referenceNode.exactMatch ? "Yes" : "";
    conceptResultsStringArray[DetailedResultsIndex.CONCEPT_MATCH.ordinal()] =
        referenceNode.partialMatch ? "Yes" : "";
    conceptResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_PARTIAL.ordinal()] =
        referenceNode.partialMatch ? "Yes" : "";
    conceptResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_FULL.ordinal()] =
        referenceNode.exactMatch ? "Yes" : "";
    conceptResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_PARTIAL.ordinal()] =
        !referenceNode.partialMatch ? "Yes" : "";
    conceptResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_FULL.ordinal()] =
        !referenceNode.exactMatch ? "Yes" : "";

    AnnotatedPhrase referenceAnnotation = referenceNode.annotatedPhrase;
    conceptResultsStringArray[DetailedResultsIndex.REFERENCE_START.ordinal()] =
        referenceAnnotation.getRawTokensStartOff();
    conceptResultsStringArray[DetailedResultsIndex.REFERENCE_CONCEPT.ordinal()] =
        referenceAnnotation.getConceptName();
    conceptResultsStringArray[DetailedResultsIndex.REFERENCE_MENTION_ID.ordinal()] =
        referenceAnnotation.getMentionId();
    List<RaptatToken> phraseTokenList = referenceAnnotation.getProcessedTokens();
    conceptResultsStringArray[DetailedResultsIndex.REFERENCE_PHRASE.ordinal()] =
        RaptatToken.getPreprocessedStringFromTokens(phraseTokenList);

    if (referenceNode.partialMatch) {
      AnnotatedPhrase raptatAnnotation = referenceNode.matchingAnnotation;
      conceptResultsStringArray[DetailedResultsIndex.RAPTAT_CONCEPT.ordinal()] =
          raptatAnnotation.getConceptName();
      conceptResultsStringArray[DetailedResultsIndex.RAPTAT_MENTION_ID.ordinal()] =
          raptatAnnotation.getMentionId();
      conceptResultsStringArray[DetailedResultsIndex.RAPTAT_START.ordinal()] =
          raptatAnnotation.getRawTokensStartOff();
      conceptResultsStringArray[DetailedResultsIndex.RAPTAT_PHRASE.ordinal()] =
          RaptatToken.getPreprocessedStringFromTokens(raptatAnnotation.getProcessedTokens());
    }

    /*
     * Note that this is only checking the tokens from the reference node, so we only create a
     * sentence if the reference node has phrase tokens
     */
    if (phraseTokenList.size() > 0) {
      AnnotatedPhrase theSentence = phraseTokenList.get(0).getSentenceOfOrigin();
      List<RaptatToken> sentenceTokens = theSentence.getRawTokens();
      conceptResultsStringArray[DetailedResultsIndex.SENTENCE.ordinal()] =
          RaptatToken.getUnprocessedStringFromTokens(sentenceTokens);
    }
    this.writeln(conceptResultsStringArray, "\t");
    /* Writing of "basic" information complete */

    if (referenceNode.partialMatch) {
      /* First handle writing attributes and their values */
      List<RaptatPair<String, Integer>> conceptValueResults =
          performanceScorer.compareConceptAttributeValues(referenceNode.matchingAnnotation,
              referenceNode.annotatedPhrase);

      for (RaptatPair<String, Integer> attributeResult : conceptValueResults) {
        String[] attributeResultArray =
            updateResultsWithConceptAttribute(attributeResult, conceptResultsStringArray);

        /* Note that each attribute result is put on a separate line */
        this.writeln(attributeResultArray, "\t");
      }

      /*
       * Next handle relationships, their linked concepts, and their attributes and values
       */
      HashMap<String, AnnotationOverlapGraphEdge> mentionIdToAnnotationGraphEdgeMap =
          performanceScorer.getMentionIdToAnnotationGraphEdgeMap();
      compareAndWriteRelationships(referenceNode, raptatAnnotationMap, referenceAnnotationMap,
          schemaConceptMap, mentionIdToAnnotationGraphEdgeMap, conceptResultsStringArray);
    }
  }


  private void compareAndWriteRelationships(AnnotationScorerNode referenceNode,
      Hashtable<String, AnnotationScorerNode> raptatIdToAnnotationMap,
      Hashtable<String, AnnotationScorerNode> referenceIdToAnnotationMap,
      HashMap<String, SchemaConcept> schemaConceptMap,
      HashMap<String, AnnotationOverlapGraphEdge> mentionIdToAnnotationGraphEdgeMap,
      String[] conceptResultsStringArray) {
    AnnotatedPhrase raptatPhrase = referenceNode.matchingAnnotation;
    AnnotatedPhrase referencePhrase = referenceNode.annotatedPhrase;

    /*
     * Create sets that equally hash to reference and raptat concept relations if they are the same
     * (have same name and are connected to one of the two nodes on the end of an
     * AnnotationOverlapGraphEdge
     */
    HashSet<RelationshipScorerNode> referenceRelations = new HashSet<>();
    for (ConceptRelation conceptRelation : referencePhrase.getConceptRelations()) {
      String conceptRelationName = conceptRelation.getConceptRelationName();
      String linkedToMentionId = conceptRelation.getRelatedAnnotationID();
      AnnotationOverlapGraphEdge connectionEdge =
          mentionIdToAnnotationGraphEdgeMap.get(linkedToMentionId);

      /*
       * If not connection edge found, it must not have a matching raptat annotation and is a FN
       */
      if (connectionEdge == null) {
        AnnotatedPhrase linkedToPhrase =
            referenceIdToAnnotationMap.get(linkedToMentionId).annotatedPhrase;
        String linkedToConceptName = linkedToPhrase.getConceptName();
        writeDetailedFalseRelationshipScore(conceptRelationName, linkedToMentionId,
            linkedToConceptName, conceptResultsStringArray, RaptatConstants.TrueFalseScores.FN);
      } else {
        referenceRelations.add(new RelationshipScorerNode(conceptRelationName, connectionEdge));
      }
    }

    HashSet<RelationshipScorerNode> raptatRelations = new HashSet<>();
    for (ConceptRelation conceptRelation : raptatPhrase.getConceptRelations()) {
      String conceptRelationName = conceptRelation.getConceptRelationName();
      String linkedToMentionId = conceptRelation.getRelatedAnnotationID();
      AnnotationOverlapGraphEdge connectionEdge =
          mentionIdToAnnotationGraphEdgeMap.get(linkedToMentionId);
      /*
       * If no connection edge is found, it must not have a matching reference annotation and is a
       * FP
       */
      if (connectionEdge == null) {
        AnnotatedPhrase linkedToPhrase =
            raptatIdToAnnotationMap.get(linkedToMentionId).annotatedPhrase;
        String linkedToConceptName = linkedToPhrase.getConceptName();
        writeDetailedFalseRelationshipScore(conceptRelationName, linkedToMentionId,
            linkedToConceptName, conceptResultsStringArray, RaptatConstants.TrueFalseScores.FP);
      } else {
        raptatRelations.add(new RelationshipScorerNode(conceptRelationName, connectionEdge));
      }
    }

    /*
     * This for loop is designed to avoid concurrent modification exception. When a node is in both
     * the referenceRelations and the raptatRelations set, it is a true positive and is added to the
     * truePositive relations list. It is also removed from the raptatRelations set so that what is
     * left within this set once the loop is finished is just the falsePositive instances. We cannot
     * modify referenceRelations by removing nodes as we are iterating on it (to leave just false
     * negatives) - doing so will lead to concurrent modification exception.
     */
    List<RelationshipScorerNode> truePositiveRelations = new ArrayList<>();
    List<RelationshipScorerNode> falseNegativeReferenceRelations = new ArrayList<>();
    for (RelationshipScorerNode node : referenceRelations) {
      if (raptatRelations.contains(node)) {
        truePositiveRelations.add(node);
        raptatRelations.remove(node);
      } else {
        falseNegativeReferenceRelations.add(node);
      }
    }

    /* Score True Positives */
    for (RelationshipScorerNode node : truePositiveRelations) {
      String referenceLinkedToMentionId = node.edge.getRefNodeMentionID();
      AnnotatedPhrase linkedToAnnotation =
          referenceIdToAnnotationMap.get(referenceLinkedToMentionId).annotatedPhrase;
      String linkedToConcept = linkedToAnnotation.getConceptName();
      String raptatLinkedToMentionId = node.edge.getRaptatNodeMentionID();
      writeDetailedTrueRelationshipScore(node.relationshipName, raptatLinkedToMentionId,
          referenceLinkedToMentionId, linkedToConcept, conceptResultsStringArray);
    }

    /* Score False Positives */
    for (RelationshipScorerNode node : raptatRelations) {
      String linkedToMentionId = node.edge.getRaptatNodeMentionID();
      AnnotatedPhrase linkedToAnnotation =
          raptatIdToAnnotationMap.get(linkedToMentionId).annotatedPhrase;
      String linkedToConceptName = linkedToAnnotation.getConceptName();
      writeDetailedFalseRelationshipScore(node.relationshipName, linkedToMentionId,
          linkedToConceptName, conceptResultsStringArray, RaptatConstants.TrueFalseScores.FP);
    }

    /* Score False Negatives */
    for (RelationshipScorerNode node : falseNegativeReferenceRelations) {
      String linkedToMentionId = node.edge.getRefNodeMentionID();
      AnnotatedPhrase linkedToAnnotation =
          referenceIdToAnnotationMap.get(linkedToMentionId).annotatedPhrase;
      String linkedToConceptName = linkedToAnnotation.getConceptName();
      writeDetailedFalseRelationshipScore(node.relationshipName, linkedToMentionId,
          linkedToConceptName, conceptResultsStringArray, RaptatConstants.TrueFalseScores.FN);
    }
  }


  private void initializeWriter(String[] resultTitles) {

    try {
      String saveFile = this.fileTitle + ".txt";
      File resFile = new File(saveFile);
      this.resultPW = new PrintWriter(new BufferedWriter(new FileWriter(resFile, false)), true);
      this.writeln(resultTitles, "\t");
    } catch (IOException e) {
      System.out.println("Unable to create result file");
      // e.printStackTrace();
      System.exit(0);
    }
  }


  private void setConceptAttributeValueResult(String[] conceptResults,
      String concatenatedConceptAttributeValue, DetailedResultsIndex concept,
      DetailedResultsIndex attribute, DetailedResultsIndex value) {
    String[] conceptAttributeValueArray =
        concatenatedConceptAttributeValue.split(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
    conceptResults[concept.ordinal()] = conceptAttributeValueArray[0];
    conceptResults[attribute.ordinal()] = conceptAttributeValueArray[1];
    conceptResults[value.ordinal()] = conceptAttributeValueArray[2];
  }


  /**
   * Takes a result of matching for an attributeValue and a result[] for a concept and converts it
   * into a result[] for the attributeValue
   *
   * @param attributeResult
   * @param attributeResultsStringArray
   * @return
   */
  private String[] updateResultsWithConceptAttribute(RaptatPair<String, Integer> attributeValue,
      String[] conceptResultsStringArray) {
    String[] attributeResultsStringArray =
        Arrays.copyOf(conceptResultsStringArray, conceptResultsStringArray.length);

    switch (attributeValue.right) {
      case TP:
        setConceptAttributeValueResult(attributeResultsStringArray, attributeValue.left,
            DetailedResultsIndex.RAPTAT_CONCEPT, DetailedResultsIndex.RAPTAT_ATTRIBUTE,
            DetailedResultsIndex.RAPTAT_ATTRIBUTE_VALUE);
        setConceptAttributeValueResult(attributeResultsStringArray, attributeValue.left,
            DetailedResultsIndex.REFERENCE_CONCEPT, DetailedResultsIndex.REFERENCE_ATTRIBUTE,
            DetailedResultsIndex.REFERENCE_ATTRIBUTE_VALUE);
        attributeResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_ATTRIBUTE_VALUE.ordinal()] =
            "Yes";
        attributeResultsStringArray[DetailedResultsIndex.FALSE_POSITIVE_ATTRIBUTE_VALUE.ordinal()] =
            "";
        attributeResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_ATTRIBUTE_VALUE.ordinal()] =
            "";
        break;
      case FP:
        setConceptAttributeValueResult(attributeResultsStringArray, attributeValue.left,
            DetailedResultsIndex.RAPTAT_CONCEPT, DetailedResultsIndex.RAPTAT_ATTRIBUTE,
            DetailedResultsIndex.RAPTAT_ATTRIBUTE_VALUE);
        attributeResultsStringArray[DetailedResultsIndex.REFERENCE_CONCEPT.ordinal()] =
            attributeResultsStringArray[DetailedResultsIndex.RAPTAT_CONCEPT.ordinal()];
        attributeResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_ATTRIBUTE_VALUE.ordinal()] =
            "";
        attributeResultsStringArray[DetailedResultsIndex.FALSE_POSITIVE_ATTRIBUTE_VALUE.ordinal()] =
            "Yes";
        attributeResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_ATTRIBUTE_VALUE.ordinal()] =
            "";
        break;
      case FN:
        setConceptAttributeValueResult(attributeResultsStringArray, attributeValue.left,
            DetailedResultsIndex.REFERENCE_CONCEPT, DetailedResultsIndex.REFERENCE_ATTRIBUTE,
            DetailedResultsIndex.REFERENCE_ATTRIBUTE_VALUE);
        attributeResultsStringArray[DetailedResultsIndex.RAPTAT_CONCEPT.ordinal()] =
            attributeResultsStringArray[DetailedResultsIndex.REFERENCE_CONCEPT.ordinal()];
        attributeResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_ATTRIBUTE_VALUE.ordinal()] =
            "";
        attributeResultsStringArray[DetailedResultsIndex.FALSE_POSITIVE_ATTRIBUTE_VALUE.ordinal()] =
            "";
        attributeResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_ATTRIBUTE_VALUE.ordinal()] =
            "Yes";
        break;
    }

    return attributeResultsStringArray;
  }


  private void writeDetailedFalseRelationshipScore(String conceptRelationName,
      String linkedToMentionId, String linkedToConceptName, String[] conceptResultsStringArray,
      TrueFalseScores trueFalseScore) {
    String[] relationshipResultsStringArray =
        Arrays.copyOf(conceptResultsStringArray, conceptResultsStringArray.length);
    int relationshipNameIndex = -1;
    int linkedToMentionIdIndex = -1;
    int linkedToConceptIndex = -1;
    switch (trueFalseScore) {
      case FP:
        relationshipResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_RELATIONSHIP.ordinal()] =
            "";
        relationshipResultsStringArray[DetailedResultsIndex.FALSE_POSITIVE_RELATIONSHIP.ordinal()] =
            "Yes";
        relationshipResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_RELATIONSHIP.ordinal()] =
            "";
        relationshipNameIndex = DetailedResultsIndex.RAPTAT_RELATIONSHIP_NAME.ordinal();
        linkedToMentionIdIndex =
            DetailedResultsIndex.RAPTAT_RELATIONSHIP_LINKED_TO_MENTIONID.ordinal();
        linkedToConceptIndex = DetailedResultsIndex.RAPTAT_RELATIONSHIP_LINKED_TO_CONCEPT.ordinal();
        break;
      case FN:
        relationshipResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_RELATIONSHIP.ordinal()] =
            "";
        relationshipResultsStringArray[DetailedResultsIndex.FALSE_POSITIVE_RELATIONSHIP.ordinal()] =
            "";
        relationshipResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_RELATIONSHIP.ordinal()] =
            "Yes";
        relationshipNameIndex = DetailedResultsIndex.REFERENCE_RELATIONSHIP_NAME.ordinal();
        linkedToMentionIdIndex =
            DetailedResultsIndex.REFERENCE_RELATIONSHIP_LINKED_TO_MENTIONID.ordinal();
        linkedToConceptIndex =
            DetailedResultsIndex.REFERENCE_RELATIONSHIP_LINKED_TO_CONCEPT.ordinal();
        break;
      default:
        break;
    }

    relationshipResultsStringArray[relationshipNameIndex] = conceptRelationName;
    relationshipResultsStringArray[linkedToMentionIdIndex] = linkedToMentionId;
    relationshipResultsStringArray[linkedToConceptIndex] = linkedToConceptName;
    this.writeln(relationshipResultsStringArray, "\t");
  }


  private void writeDetailedTrueRelationshipScore(String conceptRelationName,
      String raptatLinkedToMentionId, String referenceLinkedToMentionId, String linkedToConceptName,
      String[] conceptResultsStringArray) {
    String[] relationshipResultsStringArray =
        Arrays.copyOf(conceptResultsStringArray, conceptResultsStringArray.length);

    relationshipResultsStringArray[DetailedResultsIndex.TRUE_POSITIVE_RELATIONSHIP.ordinal()] =
        "Yes";
    relationshipResultsStringArray[DetailedResultsIndex.FALSE_POSITIVE_RELATIONSHIP.ordinal()] = "";
    relationshipResultsStringArray[DetailedResultsIndex.FALSE_NEGATIVE_RELATIONSHIP.ordinal()] = "";
    relationshipResultsStringArray[DetailedResultsIndex.RAPTAT_RELATIONSHIP_NAME.ordinal()] =
        conceptRelationName;
    relationshipResultsStringArray[DetailedResultsIndex.RAPTAT_RELATIONSHIP_LINKED_TO_MENTIONID
        .ordinal()] = raptatLinkedToMentionId;
    relationshipResultsStringArray[DetailedResultsIndex.RAPTAT_RELATIONSHIP_LINKED_TO_CONCEPT
        .ordinal()] = linkedToConceptName;
    relationshipResultsStringArray[DetailedResultsIndex.REFERENCE_RELATIONSHIP_NAME.ordinal()] =
        conceptRelationName;
    relationshipResultsStringArray[DetailedResultsIndex.REFERENCE_RELATIONSHIP_LINKED_TO_MENTIONID
        .ordinal()] = referenceLinkedToMentionId;
    relationshipResultsStringArray[DetailedResultsIndex.REFERENCE_RELATIONSHIP_LINKED_TO_CONCEPT
        .ordinal()] = linkedToConceptName;

    this.writeln(relationshipResultsStringArray, "\t");
  }


  /**
   * This is a place holder to report the attribute and value results for a relationship that has
   * been scored as a "true positive." The method should insert the results of comparing the
   * attributes and their values in the pair of ConceptRelation objects into the String [] provided
   * as a parameter. Note that when this method was created, the left side of the pair was the
   * ConceptRelation object for the reference annotation, and the right was the ConceptRelation
   * object for the raptat annotation. When this method is called, it is assume that all parts of
   * the relationshipResultsStringArray have been filled (using the ordinal values described by the
   * ResultsIndex enum), so the only thing that remains to do is to fill in the attribute results.
   *
   * @param referenceAndRaptatRelations
   * @param relationshipResultsStringArray
   */
  private void writeRelationshipAttributes(
      RaptatPair<ConceptRelation, ConceptRelation> referenceAndRaptatRelations,
      String[] relationshipResultsStringArray) {
    // TODO Auto-generated method stub
    System.out.println("The writeRelationshipAttributes() method is not yet implemented");
    System.exit(-1);;
  }
}
