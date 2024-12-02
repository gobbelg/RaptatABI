package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.util.FileUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.MunkresAssignmentTest;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.AnnotationOverlapGraphEdge;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PerformanceScore;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.analysis.bootstrap.BootstrapResultWriter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * Determines performance score with respect to both identifying phrases for annotation and also
 * mapping the phrases to the proper concept. The performance score returned for phrase
 * identification includes both exact and partial phrase matching, which indicates the ability to
 * identify token sequences for annotation. Exact matches overlap by the same, exact number of
 * tokens. Partial matches overlap by 1 or more tokens, so an exact match is also a partial match.
 * Lastly, it determines the ability of the tool to map identified phrases to correct concepts. If a
 * phrase is a partial phrase match and maps to the correct concept, it is considered a "true
 * positive." If a phrase is a partial match but maps to the incorrect concept, it is scored as a
 * "false positive" for the incorrect concept and a "false negative" for the concept that the tool
 * should have mapped the phrase to. All phrases that are completely missed by the phrase matching
 * part of the tool are considered "false negatives" for the concept they match to. Similarly, all
 * phrases that are identified but should not map to anything are considered "false positives."
 *
 * @author Glenn Gobbel - Jun 18, 2012
 */
public class PerformanceScorer {

  private int maxPhraseTokens = OptionsManager.getInstance().getTrainingTokenNumber();

  private HashSet<String> acceptedConcepts = null;

  protected String textSource;

  protected File scoreSaveDirectory = OptionsManager.getInstance().getScoreDirectory();


  protected Hashtable<PerformanceScoreObject, int[]> runningSummedResult = null;


  private DetailedAnnotationComparisonWriter annotationComparisonWriter = null;


  private HashMap<String, AnnotationOverlapGraphEdge> mentionIdToAnnotationGraphEdgeMap;


  private RaptatConstants.ScoringMode scoringMode =
      RaptatConstants.ScoringMode.CONCEPT_AND_ATTRIBUTE_SCORING;


  /*
   * Glenn comment - 10/30/15 - More credit (weight) is given to concept matching than attribute
   * matching. Using this approach, there would have to be many, many more attributes matching to
   * overcome a concept mismatch of two overlapping phrases.
   */
  private int weightOfConceptMatch = 10;

  private int weightOfAttributeMatch = 1;

  private static final int TP = 0, FP = 1, FN = 2;

  private static Logger LOGGER = Logger.getLogger(PerformanceScorer.class);

  /*
   * Glenn comment - 10/30/15 - Note that this is lower than the CONCEPT_MISMATCH_SCORE, and it is
   * negative because we were initially counting failure of attributes to match as negative points,
   * so this makes it lower even if a few attributes don't match. Later, we only counted attributes
   * that match as contributing positively to the score with unmatched attributes not counting for
   * anything.
   *
   */
  // This value is assigned if there is no overlap between the
  // phrases.
  private static final int NO_OVERLAP_SCORE = Integer.MIN_VALUE;

  /*
   * This value (or lower down to but not including NO_OVERLAP_SCORE) is assigned to an 'annotation
   * graph edge' if the concepts do not match but the tokens in the pair of annotations do overlap.
   * An edge value greater than this indicates a concept match in addition to token overlap.
   */
  private static final int CONCEPT_MISMATCH_SCORE =
      (int) (PerformanceScorer.NO_OVERLAP_SCORE / 2.0);

  /** */
  public PerformanceScorer() {
    PerformanceScorer.LOGGER.setLevel(Level.INFO);
  }

  /**
   * Constructs a PerformanceScorer instance that compares concepts in two different XML files
   * corresponding to one text file.
   *
   * @param acceptedConcepts - Only concepts in the HashSet will be scored. Pass null to score all
   *        concepts that occur in either of the two XML files under comparison.
   * @param maxPhraseTokens - maximum number of tokens allowable in an annotated phrase. If an
   *        annotated phrase is longer than this, it will be split so that it is no longer than the
   *        maximum size in tokens, and the same concept will be assigned to all the phrases
   *        resulting from the split
   * @author Glenn Gobbel - Jan 21, 2014
   */
  public PerformanceScorer(final HashSet<String> acceptedConcepts, final int maxPhraseTokens) {
    this();
    this.maxPhraseTokens = maxPhraseTokens;
    this.acceptedConcepts = acceptedConcepts;
  }

  /**
   * Overloaded Constructor for Performance Scorer Assigns weights for the concept matching and
   * attribute matching. Default values are conceptMatchWeight 10 attributeMatchWeight 1 scoringMode
   * CONCEPT_AND_ATTRIBUTE_SCORING
   *
   * @param acceptedConcepts Only concepts in the HashSet will be scored. Pass null to score all
   *        concepts that occur in either of the two XML files under comparison
   * @param maxPhraseTokens Maximum number of tokens allowable in an annotated phrase. If an
   *        annotated phrase is longer than this, it will be split so that it is no longer than the
   *        maximum size in tokens, and the same concept will be assigned to all the phrases
   *        resulting from the split
   * @param conceptMatchWeight The weight assigned for every concept match
   * @param attributeMatchWeight The weight assigned for every attribute match
   * @param scoringMode The scoring method
   */
  public PerformanceScorer(final HashSet<String> acceptedConcepts, final int maxPhraseTokens,
      final int conceptMatchWeight, final int attributeMatchWeight,
      final RaptatConstants.ScoringMode scoringMode) {
    this();
    this.maxPhraseTokens = maxPhraseTokens;
    this.acceptedConcepts = acceptedConcepts;
    this.weightOfAttributeMatch = attributeMatchWeight;
    this.weightOfConceptMatch = conceptMatchWeight;
    this.scoringMode = scoringMode;
  }

  /**
   * Constructor for Performance Scorer Assigns weights for the concept matching and attribute
   * matching. Default values are conceptMatchWeight 10 attributeMatchWeight 1 scoringMode
   * CONCEPT_AND_ATTRIBUTE_SCORING
   *
   * @param conceptMatchWeight The weight assigned for every concept match
   * @param attributeMatchWeight The weight assigned for every attribute match
   * @param scoringMode The scoring method
   */
  public PerformanceScorer(final int conceptMatchWeight, final int attributeMatchWeight,
      final RaptatConstants.ScoringMode scoringMode) {
    this();
    this.weightOfAttributeMatch = attributeMatchWeight;
    this.weightOfConceptMatch = conceptMatchWeight;
    this.scoringMode = scoringMode;
  }

  /**
   * This is a method used for scoring performance when doing cross-validation. For a single run of
   * score performance the constructor, PerformanceScorer(HashSet \ <String\> acceptedConcepts, int
   * maxPhraseTokens) should be used instead.
   *
   * @param fileDirectory
   * @param createDetailedReport
   * @param settingsFileTag
   * @author Glenn Gobbel - Jan 21, 2014
   */
  public PerformanceScorer(final String fileDirectory, final boolean createDetailedReport,
      final String settingsFileTag) {
    this();
    if (createDetailedReport) {
      DetailedResultsIndex[] indexValues = DetailedResultsIndex.values();
      String[] resultTitles = new String[indexValues.length];
      for (int i = 0; i < indexValues.length; i++) {
        resultTitles[i] = indexValues[i].toString();
      }

      this.annotationComparisonWriter =
          new DetailedAnnotationComparisonWriter(fileDirectory, resultTitles, settingsFileTag);
    }
  }


  /**
   * *********************************************************** Method to calculate the F-measure.
   *
   * @param precision computed with TP and FP
   * @param recall computed with TP and FN
   * @return F-measure value as Float
   * @author Developed by Shrimalini Jayaramaraja on August 16th 2011 * * * *
   *         **********************************************************
   */
  public float calculateF_Measure(final float precision, final float recall) {
    @SuppressWarnings("UnusedAssignment")
    float fMeasure = 0;
    fMeasure = 2 * (precision * recall) / (precision + recall);
    return fMeasure;
  }


  /**
   * ** Method to compute Precision.
   *
   * @param numOfTP True positives
   * @param numOfFP False negatives
   * @return Precision value as Float
   * @author Developed by Shrimalini Jayaramaraja on August 16th 2011
   */
  public float calculatePrecision(final float numOfTP, final float numOfFP) {
    @SuppressWarnings("UnusedAssignment")
    float precision = 0;
    precision = numOfTP / (numOfTP + numOfFP);
    return precision;
  }


  /**
   * **************************************************************** Method to compute Recall.
   * Preconditions: True positive and False negative values have been computed
   *
   * @param numOfTP True positives
   * @param numOfFN False negatives
   * @return Recall value as Float
   * @author Developed by Shrimalini Jayaramaraja on August 16th 2011 * * * *
   *         ************************************************************
   */
  public float calculateRecall(final float numOfTP, final float numOfFN) {
    @SuppressWarnings("UnusedAssignment")
    float recall = 0;
    recall = numOfTP / (numOfTP + numOfFN);
    return recall;
  }


  /** */
  public void closeWriter() {
    if (this.annotationComparisonWriter != null) {
      this.annotationComparisonWriter.close();
    }
  }


  /**
   * Determines matching of the attributes in two annotatedPhrase objects, a raptatPhrase and a
   * referencePhrase. This should only be called if raptatPhrase and referencePhrase have the same
   * concept name
   *
   * @param raptatPhrase
   * @param referencePhrase
   * @return A list of pairs, with the left side giving a concatenation of concept, attribute, and
   *         attribute value, and the right side indicating whether it is a true positive, false
   *         positive, or false negative using the TP, FP, FN integer constants of this class.
   */
  public List<RaptatPair<String, Integer>> compareConceptAttributeValues(
      final AnnotatedPhrase raptatPhrase, final AnnotatedPhrase referencePhrase) {
    List<RaptatPair<String, Integer>> resultList = new ArrayList<>();

    String conceptName = raptatPhrase.getConceptName();

    /*
     * The attributeValueMap variables map an attribute name to the set of values for the attribute
     * for a particular annotated phrase
     */
    Map<String, Set<String>> raptatAttributeValueMap = createAttributeToValueMap(raptatPhrase);
    Map<String, Set<String>> referenceAttributeValueMap =
        createAttributeToValueMap(referencePhrase);
    for (String attributeName : raptatAttributeValueMap.keySet()) {
      /* Handle if the reference contains the attribute */
      if (referenceAttributeValueMap.containsKey(attributeName)) {
        Set<String> referenceValues = referenceAttributeValueMap.get(attributeName);
        for (String value : raptatAttributeValueMap.get(attributeName)) {
          StringBuilder sb = new StringBuilder(conceptName)
              .append(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
          sb.append(attributeName).append(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
          sb.append(value);
          Integer valueMatched =
              referenceValues.contains(value) ? PerformanceScorer.TP : PerformanceScorer.FP;
          resultList.add(new RaptatPair<>(sb.toString(), valueMatched));

          /*
           * Take out any true, matched values in the referenceAttributeValue map so only false
           * negative values remain in referenceValues for this attribute name.
           */
          if (valueMatched == PerformanceScorer.TP) {
            referenceValues.remove(value);
          }
        }
      }
      /*
       * Handle if the reference does not contain the attribute (these will be false positives)
       */
      else {
        for (String value : raptatAttributeValueMap.get(attributeName)) {
          StringBuilder sb = new StringBuilder(conceptName)
              .append(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
          sb.append(attributeName).append(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
          sb.append(value);
          resultList.add(new RaptatPair<>(sb.toString(), PerformanceScorer.FP));
        }
      }
    }

    /*
     * Now assign remaining, unmatched referenceAttribute values as false negatives
     */
    for (String attributeName : referenceAttributeValueMap.keySet()) {
      for (String value : referenceAttributeValueMap.get(attributeName)) {
        StringBuilder sb = new StringBuilder(conceptName)
            .append(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
        sb.append(attributeName).append(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD);
        sb.append(value);
        resultList.add(new RaptatPair<>(sb.toString(), PerformanceScorer.FN));
      }
    }

    return resultList;
  }


  public HashMap<String, AnnotationOverlapGraphEdge> getMentionIdToAnnotationGraphEdgeMap() {
    return this.mentionIdToAnnotationGraphEdgeMap;
  }


  /**
   * Method used by phraseIDCrossValidationAnalysis to get performance score
   *
   * @param raptatAnnotations
   * @param referenceAnnotations
   * @return
   * @author Glenn Gobbel - Jan 21, 2014 Modified by Sanjib Saha - Aug 07, 2014 Modified by Sanjib
   *         Saha - October 29, 2014
   */
  @Deprecated // Called only by Garvin create project, bootstrap
  // analysis, and
  // active learning
  public Hashtable<PerformanceScoreObject, int[]> getPerformanceScore(
      final List<AnnotatedPhrase> raptatAnnotations,
      final List<AnnotatedPhrase> referenceAnnotations) {
    int[] curScore; // curScore[0] = TP, curScore[1] = FP,
    // curScore[2] = FN
    Hashtable<PerformanceScoreObject, int[]> performance = new Hashtable<>();

    boolean match;

    /*
     * The scorerList variables are lists of annotations converted into lists of "annotation nodes"
     * which keep track of whether an annotation has been matched in terms of phrase identification
     * (partial or exact) and concept
     */
    ArrayList<AnnotationScorerNode> raptatScorerList = createScorerNodeList(raptatAnnotations);
    ArrayList<AnnotationScorerNode> referenceScorerList =
        createScorerNodeList(referenceAnnotations);

    /*
     * We will store phrase matching as a separate concept, so annotations will get scored for the
     * "real" concept as well as the concept that is annotated. We keep track of partial matches,
     * overlapping by at least one token, and exact matches overlapping with all tokens in the
     * raptat and reference annotation.
     */
    performance.put(PerformanceScoreObject.EXACT_MATCH_PSO, new int[] {0, 0, 0});
    performance.put(PerformanceScoreObject.PARTIAL_MATCH_PSO, new int[] {0, 0, 0});

    for (AnnotationScorerNode curRaptatNode : raptatScorerList) {
      AnnotatedPhrase curRaptatPhrase = curRaptatNode.annotatedPhrase;

      // Skipping the middle phrases of a multi-spanned annotation
      if (curRaptatPhrase.getPrevPhrase() != null) {
        continue;
      }

      for (AnnotationScorerNode curReferenceNode : referenceScorerList) {
        // Now do the matching
        AnnotatedPhrase curReferencePhrase = curReferenceNode.annotatedPhrase;

        // Skipping any phrases that are not the head span of
        // multi-spanned phrases
        if (curReferencePhrase.getPrevPhrase() != null) {
          continue;
        }

        int tokenOverlap = this.getTokenOverlap(curRaptatPhrase, curReferencePhrase);

        // Only evaluate further if there is at least partial
        // overlap
        if (tokenOverlap > -1) {
          // Indicates at least partial overlap
          String raptatConceptName = curRaptatPhrase.getConceptName().toLowerCase();

          /*
           * First deal with phrase overlap without worrying about the concept. Only increment
           * partial phrase match if one did not already happen for the reference annotation or
           * raptat annotation.
           */
          if (!curReferenceNode.partialMatch && !curRaptatNode.partialMatch) {
            curScore = performance.get(PerformanceScoreObject.PARTIAL_MATCH_PSO);
            curScore[PerformanceScorer.TP]++;
            curRaptatNode.partialMatchIgnoringConcept = true;
            curReferenceNode.partialMatchIgnoringConcept = true;

            /*
             * Token Overlap of > 0 indicates exact match. Only count as exact match if neither the
             * reference phrase nor the raptat phrase was already counted as an exact match - i.e.
             * do not double count exact matches
             */
            if (tokenOverlap > 0 && !curReferenceNode.exactMatch && !curRaptatNode.exactMatch) {
              curScore = performance.get(RaptatConstants.TOTAL_EXACT_MATCH_NAME);
              curScore[PerformanceScorer.TP]++;

              curRaptatNode.exactMatchIgnoringConcept = true;
              curReferenceNode.exactMatchIgnoringConcept = true;
            }
          }

          /*
           * Now score if the concept is the same in addition to there being at least partial
           * overlap.
           */
          if (raptatConceptName.equalsIgnoreCase(curReferencePhrase.getConceptName())) {
            PerformanceScoreObject raptatConceptNamePSO =
                new PerformanceScoreObject(raptatConceptName);

            if ((curScore = performance.get(raptatConceptNamePSO)) == null) {
              curScore = new int[] {0, 0, 0};
              performance.put(raptatConceptNamePSO, curScore);

              // new code to support concept wise partial
              // and
              // exact match result
              PerformanceScoreObject partialConceptNamePSO =
                  new PerformanceScoreObject("partialphrasematch_" + raptatConceptName);
              performance.put(partialConceptNamePSO, new int[] {0, 0, 0});

              PerformanceScoreObject exactConceptNamePSO =
                  new PerformanceScoreObject("exactphrasematch_" + raptatConceptName);
              performance.put(exactConceptNamePSO, new int[] {0, 0, 0});
            }

            match = getPerformanceAttributeScore(curReferenceNode, curRaptatNode, performance,
                tokenOverlap, curScore);

            // If match is true, node already matched
            if (match) {
              break;
            }
          } // End for-loop for ref Nodes
        }
      }
    }

    updatePerformance(performance, raptatScorerList, referenceScorerList);

    return performance;
  }


  public Hashtable<String, int[]> getPerformanceScoreForActiveLearning(
      final List<AnnotatedPhrase> raptatAnnotations,
      final List<AnnotatedPhrase> referenceAnnotations) {

    int[] curScore; // curScore[0] = TP, curScore[1] = FP,
    // curScore[2] = FN
    Hashtable<String, int[]> performance = new Hashtable<>();

    ArrayList<AnnotationScorerNode> raptatScorerList = createScorerNodeList(raptatAnnotations);
    ArrayList<AnnotationScorerNode> referenceScorerList =
        createScorerNodeList(referenceAnnotations);

    performance.put(RaptatConstants.TOTAL_EXACT_MATCH_NAME, new int[] {0, 0, 0});
    performance.put(RaptatConstants.TOTAL_PARTIAL_MATCH_NAME, new int[] {0, 0, 0});

    for (AnnotationScorerNode curRaptatNode : raptatScorerList) {
      AnnotatedPhrase curRaptatPhrase = curRaptatNode.annotatedPhrase;

      for (AnnotationScorerNode curReferenceNode : referenceScorerList) {
        AnnotatedPhrase curReferencePhrase = curReferenceNode.annotatedPhrase;

        // Skipping the middle phrases of a multi-spanned
        // annotation
        if (curReferencePhrase.getPrevPhrase() != null) {
          continue;
        }

        int tokenOverlap = this.getTokenOverlap(curRaptatPhrase, curReferencePhrase);

        if (tokenOverlap > -1) {
          // Indicates at least partial overlap

          if (!curReferenceNode.partialMatch && !curRaptatNode.partialMatch) {
            curScore = performance.get(RaptatConstants.TOTAL_PARTIAL_MATCH_NAME);
            curScore[PerformanceScorer.TP]++;

            curRaptatNode.partialMatchIgnoringConcept = true;
            curReferenceNode.partialMatchIgnoringConcept = true;

            if (tokenOverlap > 0 && !curReferenceNode.exactMatch && !curRaptatNode.exactMatch) {
              curScore = performance.get(RaptatConstants.TOTAL_EXACT_MATCH_NAME);
              curScore[PerformanceScorer.TP]++;

              curRaptatNode.exactMatchIgnoringConcept = true;
              curReferenceNode.exactMatchIgnoringConcept = true;
            }
          }

          break;
        }
      }
    }

    updatePerformanceForActiveLearning(performance, raptatScorerList, referenceScorerList);

    return performance;
  }


  /** @return the runningSummedResult */
  public Hashtable<PerformanceScoreObject, int[]> getRunningSummedResult() {
    return this.runningSummedResult;
  }


  public List<AnnotatedPhrase> processAnnotatedPhrase(final List<AnnotatedPhrase> annPhrase) {
    ArrayList<AnnotationScorerNode> raptatScorerList = createScorerNodeList(annPhrase);
    List<AnnotatedPhrase> processedPhrases = new ArrayList<>();

    for (AnnotationScorerNode node : raptatScorerList) {
      processedPhrases.add(node.annotatedPhrase);
    }

    return processedPhrases;
  }


  // Method used by calculateFirNetricsCached which is called by the
  // Bootstrap
  // analyzer. It is also called in the GarvinCreate class and by the
  // Garvin
  // project in the crossvalidation class
  /**
   * This method needs to be removed as it has been replaced by scorePerformanceWithGraph, which
   * corrected some errors in the scoring system
   *
   * @param annotationGroups
   * @return
   */
  @Deprecated
  public Hashtable<PerformanceScoreObject, int[]> scorePerformance(
      final List<AnnotationGroup> annotationGroups) {
    Level loggerLevel = PerformanceScorer.LOGGER.getLevel();
    PerformanceScorer.LOGGER.setLevel(Level.INFO);
    Hashtable<PerformanceScoreObject, int[]> curResult;
    Hashtable<PerformanceScoreObject, int[]> summedResult = null;

    for (AnnotationGroup group : annotationGroups) {
      curResult = getPerformanceScore(group.raptatAnnotations, group.referenceAnnotations);

      if (this.annotationComparisonWriter != null) {
        writeDetailedPerformanceScore(group.raptatAnnotations, group.referenceAnnotations,
            group.getRaptatDocument().getTextSource().orElse("NoTextSourcePath"));
      }
      summedResult = mergeResults(curResult, summedResult);
    }
    this.runningSummedResult = mergeResults(summedResult, this.runningSummedResult);
    System.out.println("Cumulative result:");
    this.print(this.runningSummedResult);
    if (PerformanceScorer.LOGGER.isDebugEnabled()) {
      GeneralHelper.errorWriter("Check cumulative results for iterations");
    }
    PerformanceScorer.LOGGER.setLevel(loggerLevel);
    return summedResult;
  }


  // Method used by PhraseIDCrossValidationPerformance
  /**
   * @param annotationGroups
   * @param schemaConceptList
   * @return
   */
  public Hashtable<PerformanceScoreObject, int[]> scorePerformanceWithGraph(
      final List<AnnotationGroup> annotationGroups, final List<SchemaConcept> schemaConceptList) {
    /*
     * schemaConceptMap maps schema concept names to the actual concepts in the schema. It does not
     * include attributes or relationships in the concept names used as keys.
     */
    HashMap<String, SchemaConcept> schemaConceptMap = createSchemaConceptMap(schemaConceptList);

    /*
     * performance maps the concept name +/- the attribute and value names or +/- the relationship
     * names, linked to concept names
     */
    Hashtable<PerformanceScoreObject, int[]> psoToPerformanceMap =
        createPsoToPerformanceMap(schemaConceptMap);

    for (AnnotationGroup annotationGroup : annotationGroups) {
      RaptatDocument raptatDocument = annotationGroup.getRaptatDocument();
      PerformanceScorer.LOGGER.info("Scoring:" + raptatDocument.getTextSource());
      /*
       * For proper scoring, we need to use only the head phrases of multi-span annotations. We do
       * it for raptatAnnotations as well in case this functionality is added in the future
       */
      List<AnnotatedPhrase> referenceAnnotations =
          filterForHeadPhrases(annotationGroup.referenceAnnotations);
      List<AnnotatedPhrase> raptatAnnotations =
          filterForHeadPhrases(annotationGroup.raptatAnnotations);

      List<AnnotationOverlapGraphEdge> connectionGraph =
          createConnectionGraphAndMap(raptatAnnotations, referenceAnnotations, schemaConceptMap);

      /*
       * Create variables to store maps of mentionID to an Annotation node for each annotated phrase
       * (Raptat and reference)
       */
      RaptatPair<List<AnnotationScorerNode>, List<AnnotationScorerNode>> scoringNodes =
          createScoringNodes(raptatAnnotations, referenceAnnotations);

      List<AnnotationScorerNode> raptatNodes = scoringNodes.right;
      Hashtable<String, AnnotationScorerNode> raptatIdToAnnotationMap =
          getMentionIDToAnnotationNodeMap(raptatNodes);

      List<AnnotationScorerNode> referenceNodes = scoringNodes.left;
      Hashtable<String, AnnotationScorerNode> referenceIdToAnnotationMap =
          getMentionIDToAnnotationNodeMap(referenceNodes);

      addScoreToPerformanceMap(psoToPerformanceMap, raptatIdToAnnotationMap,
          referenceIdToAnnotationMap, connectionGraph, schemaConceptMap);

      if (PerformanceScorer.LOGGER.isDebugEnabled()) {
        writeResultsToConsole(psoToPerformanceMap,
            raptatDocument.getTextSource().orElse("NoTextSource"));
      }

      if (this.annotationComparisonWriter != null) {
        writeDetailedPerformanceScoreNew(raptatIdToAnnotationMap, referenceIdToAnnotationMap,
            schemaConceptMap, raptatDocument.getTextSource().orElse("NoTextSource"));
      }
    }

    this.runningSummedResult = mergeResults(psoToPerformanceMap, this.runningSummedResult);
    if (PerformanceScorer.LOGGER.isInfoEnabled()) {
      PerformanceScorer.LOGGER.info("Cumulative result:");
      this.print(this.runningSummedResult);
    }

    return psoToPerformanceMap;
  }


  public void writeResultsSimple() {
    // TODO: Need to finish completing this method. Added on
    // 2/1/2016 when
    // merging Glenn and Sanjib code.
  }


  /**
   * Method to calculate the precision, recall and F-measure for every concept in the hashMap and
   * write the results out to a file
   *
   * @param resultMap -Hashtable containing the performance scores by concept
   * @param textSources
   * @author Developed by Glenn Gobbel - January 31, 2013
   * @throws java.io.IOException
   * @throws java.io.IOException
   */
  @SuppressWarnings("UnusedAssignment")
  public void writeResultsSimple(final Hashtable<String, int[]> resultMap,
      final List<String> textSources) throws IOException {
    double precisPerConcept = 0.0;
    double recallPerConcept = 0.0;
    double f_mPerConcept = 0.0;
    BufferedWriter out = null;

    out = new BufferedWriter(new FileWriter(this.scoreSaveDirectory));
    // For every concept in the hashMap, calculate the precision,
    // recall
    // and F-measure. Write results into scoreDir with fileName
    // TextSourceScore.txt
    out.append(System.getProperty("line.separator"));
    out.append(System.getProperty("line.separator"));
    out.append("Score for textsources:" + System.getProperty("line.separator"));
    out.append("-----------------------------------------\n");
    for (String textSource1 : textSources) {
      out.append(textSource1);
      out.append(System.getProperty("line.separator"));
    }
    out.append("---------------------------------------------------------------\n");
    System.out.println("Concept\tTP\tFP\tFN\tPrecision\tRecall\tF\n");
    out.append("\nConcept\tTP\tFP\tFN\tPrecision\tRecall\tF\n");
    System.out.println("----------------------------------\n");
    Enumeration<String> concepts = resultMap.keys();

    while (concepts.hasMoreElements()) {
      String curConcept = concepts.nextElement();
      int[] res = resultMap.get(curConcept);

      precisPerConcept = GeneralHelper.calcPrecision(res[0], res[1]);
      recallPerConcept = GeneralHelper.calcRecall(res[0], res[2]);
      f_mPerConcept = GeneralHelper.calcF_Measure(res[0], res[1], res[2]);

      System.out.println(curConcept + "\t" + res[0] + "\t" + res[1] + "\t" + res[2]);
      System.out.println("Precision= " + precisPerConcept);
      System.out.println("Recall= " + recallPerConcept);
      System.out.println("F-measure= " + f_mPerConcept);
      System.out.println("********************************\n");

      out.append(curConcept + "\t" + res[0] + "\t" + res[1] + "\t" + res[2]);
      out.append("\t" + precisPerConcept);
      out.append("\t" + recallPerConcept);
      out.append("\t" + f_mPerConcept);
      out.append(System.getProperty("line.separator"));
    }
    out.close();
  }


  /**
   * **************************************************************** Method to calculate the
   * precision, recall and F-measure for every concept in the hashMap and write the results into
   * system console.
   *
   * @param performance HashMap containing the performance scores
   * @param textSourceName Name of the text file specified as source text in the XML file.
   * @author Developed by Shrimalini Jayaramaraja on August 16th 2011
   *         <p>
   *         *****************************************************************
   */
  @SuppressWarnings("UnusedAssignment")
  public void writeResultsToConsole(final Hashtable<PerformanceScoreObject, int[]> performance,
      final String textSourceName) {
    float precisPerConcept = 0;
    float recallPerConcept = 0;
    float f_mPerConcept = 0;

    System.out.println("Score for textsource=" + textSourceName);

    // For every concept in the hashMap, calculate the precision,
    // recall
    // and F-measure and Write results into scoreDir with fileName
    // TextSourceScore.txt
    System.out.println("\nConcept     TP      FP      FN\n");

    System.out.println("----------------------------------\n");
    Enumeration<PerformanceScoreObject> concepts = performance.keys();
    while (concepts.hasMoreElements()) {
      PerformanceScoreObject curConcept = concepts.nextElement();
      int[] res = performance.get(curConcept);
      System.out.println(curConcept + "\t" + res[0] + "\t" + res[1] + "\t" + res[2]);

      precisPerConcept = calculatePrecision(res[0], res[1]);
      recallPerConcept = calculateRecall(res[0], res[2]);
      f_mPerConcept = calculateF_Measure(precisPerConcept, recallPerConcept);
      System.out.println("Precision= " + precisPerConcept);

      System.out.println("Recall= " + recallPerConcept);

      System.out.println("F-measure= " + f_mPerConcept);

      System.out.println("----------------------------------\n");
    }
  }


  /**
   * @param performanceMap
   * @param schemaConcept
   * @param conceptName
   */
  private void addAttributesAndValuesToPerformanceMap(
      final Hashtable<PerformanceScoreObject, int[]> performanceMap,
      final SchemaConcept schemaConcept, final String conceptName) {
    String attributeName;
    List<String> attributeValues;
    for (Map.Entry<String, List<String>> entry : schemaConcept.getAttributeValueMap().entrySet()) {
      attributeName = entry.getKey();
      attributeValues = entry.getValue();

      for (String value : attributeValues) {
        PerformanceScoreObject scoreObject =
            new PerformanceScoreObject(conceptName, attributeName, value, null, null, null, null);
        performanceMap.put(scoreObject, new int[] {0, 0, 0});
      }
    }
  }


  private void addRelationshipsToPerformanceMap(
      final Hashtable<PerformanceScoreObject, int[]> conceptToPerformanceMap,
      final HashMap<String, HashMap<String, HashSet<String>>> linkedFromConceptsMap,
      final SchemaConcept schemaConcept) {
    String conceptName = schemaConcept.getConceptName().toLowerCase();
    HashMap<String, HashSet<String>> relationsToConceptsToMap =
        linkedFromConceptsMap.get(conceptName);
    for (String relationshipName : relationsToConceptsToMap.keySet()) {
      HashMap<String, List<String>> attributeValueMap = schemaConcept.getAttributeValueMap();
      Set<String> conceptToNames = relationsToConceptsToMap.get(relationshipName);
      for (String conceptToName : conceptToNames) {
        PerformanceScoreObject relationshipOnlyPSO = new PerformanceScoreObject(conceptName, null,
            null, relationshipName, conceptToName, null, null);
        conceptToPerformanceMap.put(relationshipOnlyPSO, new int[] {0, 0, 0});
        for (String attributeName : attributeValueMap.keySet()) {
          List<String> attributeValues = attributeValueMap.get(attributeName);
          for (String attributeValue : attributeValues) {
            PerformanceScoreObject relationshipAndAttributePSO =
                new PerformanceScoreObject(conceptName, null, null, relationshipName, conceptToName,
                    attributeName, attributeValue);
            conceptToPerformanceMap.put(relationshipAndAttributePSO, new int[] {0, 0, 0});
          }
        }
      }
    }
  }


  /**
   * * Calculate the performance score based on a connection graph provided as a list of connected
   * graph edges. The score is stored in the psoToPerformanceMap parameter, which can be used by
   * other methods to find the score for a given combination of concept & attribute or concept,
   * relationship, and attribute.
   *
   * @param psoToPerformanceMap
   * @param raptatIdToAnnotationMap
   * @param referenceIdToAnnotationMap
   * @param annOverlapGraphEdges
   * @param schemaConceptMap
   */
  private void addScoreToPerformanceMap(
      final Hashtable<PerformanceScoreObject, int[]> psoToPerformanceMap,
      final Hashtable<String, AnnotationScorerNode> raptatIdToAnnotationMap,
      final Hashtable<String, AnnotationScorerNode> referenceIdToAnnotationMap,
      final List<AnnotationOverlapGraphEdge> annOverlapGraphEdges,
      final HashMap<String, SchemaConcept> schemaConceptMap) {

    /*
     * First go through the edges to find true positives for the for the reference/raptat
     * annotations
     */
    for (AnnotationOverlapGraphEdge edge : annOverlapGraphEdges) {
      /*
       * Edge value greater than or equal to CONCEPT_MISMATCH_SCORE means overlap exists between two
       * Annotations. *Concepts* match if edge value exceeds CONCEPT_MISMATCH_SCORE (otherwise, we
       * just have overlap)
       */
      if (edge.getEdgeValue() >= PerformanceScorer.CONCEPT_MISMATCH_SCORE) {
        AnnotationScorerNode referenceAnnotationNode =
            referenceIdToAnnotationMap.get(edge.getRefNodeMentionID());
        AnnotationScorerNode raptatAnnotationNode =
            raptatIdToAnnotationMap.get(edge.getRaptatNodeMentionID());
        updateSummaryPerformanceScore(referenceAnnotationNode, raptatAnnotationNode,
            referenceIdToAnnotationMap, raptatIdToAnnotationMap, psoToPerformanceMap,
            edge.getEdgeValue(), schemaConceptMap);
      }
    }

    /* Now handle false negatives for the concept */
    for (AnnotationScorerNode refScorerNode : referenceIdToAnnotationMap.values()) {
      if (!refScorerNode.partialMatch) {
        updatePerformanceScoreForConcept(psoToPerformanceMap, refScorerNode, PerformanceScorer.FN,
            schemaConceptMap);
      }
    }

    /* Now handle false positives for the concept */
    for (AnnotationScorerNode raptatScorerNode : raptatIdToAnnotationMap.values()) {
      if (!raptatScorerNode.partialMatch) {
        updatePerformanceScoreForConcept(psoToPerformanceMap, raptatScorerNode,
            PerformanceScorer.FP, schemaConceptMap);
      }
    }
  }


  /**
   * Takes a list of schema concepts and generates a mapping from the concept names of all concepts
   * that are part of a relationship that they are the "from" concept of. Each name maps to a
   * HashMap of all the relationships they are the "from" concept of to the concept names of the
   * concepts they are linked to through that relationship. So the mapping is: ("from" concept name)
   * -> (relationshipName) -> (set of "to" concept names).
   *
   * @param schemaConceptMap
   * @return
   */
  private HashMap<String, HashMap<String, HashSet<String>>> buildLinkedFromConceptsMap(
      final HashMap<String, SchemaConcept> schemaConceptMap) {
    HashMap<String, HashMap<String, HashSet<String>>> linkedFromConceptsMap = new HashMap<>();

    /*
     * We need to know which concepts have relationships, what relationships they have, and what
     * concepts they are linked to so that we can build that into the conceptToPerformanceMap, so we
     * do that in this loop.
     *
     */
    for (String conceptName : schemaConceptMap.keySet()) {
      SchemaConcept schemaConcept = schemaConceptMap.get(conceptName);
      if (schemaConcept.isRelationship()) {
        String relationshipName = schemaConcept.getConceptName().toLowerCase();
        for (String linkedFromConcept : schemaConcept.getLinkedFromConcepts()) {
          linkedFromConcept = linkedFromConcept.toLowerCase();
          HashMap<String, HashSet<String>> relationshipToLinkedToConceptsMap = null;
          if ((relationshipToLinkedToConceptsMap =
              linkedFromConceptsMap.get(linkedFromConcept)) == null) {
            relationshipToLinkedToConceptsMap = new HashMap<>();
            linkedFromConceptsMap.put(linkedFromConcept, relationshipToLinkedToConceptsMap);
          }
          HashSet<String> linkedToConcepts = null;
          if ((linkedToConcepts =
              relationshipToLinkedToConceptsMap.get(relationshipName)) == null) {
            linkedToConcepts = new HashSet<>();
            relationshipToLinkedToConceptsMap.put(relationshipName, linkedToConcepts);
          }
          for (String linkedToConcept : schemaConcept.getLinkedToConcepts()) {
            linkedToConcepts.add(linkedToConcept.toLowerCase());
          }
        }
      }
    }
    return linkedFromConceptsMap;
  }


  /**
   * Creates a Bi-partite graph between Reference Annotations and Raptat annotations having edges
   * between common (overlapped) annotations <br>
   * <br>
   * <b>PRECONDITION:</b> If the annotations are multi-spanned, the lists of raptatAnnotations and
   * referenceAnnotations must contain only the head phrases
   *
   * @param raptatAnnotations The Raptat Annotation
   * @param referenceAnnotations The Reference Annotation
   * @return A list of Edges that contain the optimally matched Raptat and Reference annotations and
   *         each match's corresponding score
   */
  private List<AnnotationOverlapGraphEdge> createConnectionGraphAndMap(
      final List<AnnotatedPhrase> raptatAnnotations,
      final List<AnnotatedPhrase> referenceAnnotations,
      final HashMap<String, SchemaConcept> schemaConceptMap) {
    /*
     * The adjacency matrix relating reference to raptat annotations
     */
    int[][] connectionGraph = new int[raptatAnnotations.size()][referenceAnnotations.size()];

    int i = 0;

    for (AnnotatedPhrase raptatPhrase : raptatAnnotations) {
      int j = 0;
      for (AnnotatedPhrase referencePhrase : referenceAnnotations) {
        /*
         * This could probably be sped up to more rapidly find overlapping phrases
         */
        connectionGraph[i][j] =
            getMatchingScoreBetweenAnnotations(raptatPhrase, referencePhrase, schemaConceptMap);
        if (PerformanceScorer.LOGGER.isDebugEnabled() & connectionGraph[i][j] > -1) {
          StringBuilder sb = new StringBuilder(
              "RaptatPhrase " + i + ":" + raptatPhrase.getPhraseStringUnprocessed());
          sb.append("(" + raptatPhrase.getRawTokensStartOff() + ":"
              + raptatPhrase.getRawTokensEndOff() + ")");
          sb.append("/" + "RefPhrase " + j + ":" + referencePhrase.getPhraseStringUnprocessed());
          sb.append("(" + referencePhrase.getRawTokensStartOff() + ":"
              + referencePhrase.getRawTokensEndOff() + ")");
          sb.append("[Connection:" + connectionGraph[i][j] + "]");
          System.out.println(sb.toString());
        }
        j++;
      }

      i++;
    }

    if (PerformanceScorer.LOGGER.isDebugEnabled()) {
      int k = 0;
      System.out.println(
          "\n\n=============================================================\nConnection graph "
              + "relating raptat to reference annotations:\n=================================="
              + "===========================");
      System.out.println("{");
      for (int[] curRow : connectionGraph) {
        System.out.println("{");
        for (int curColumn : curRow) {
          System.out.print(curColumn + ",  ");
        }
        System.out.println("}, ");
      }
    }

    /*
     * Use algorithm to get best match of raptat and reference annotations.
     *
     * Note that the numbers in the return array correspond to the column value for each
     * corresponding row that maximizes the matching. For example, if return = [ 2 1 3 0], it
     * indicates that the maximum matching is provided by taking column 2 for row 0, column 1 for
     * row 1, column 3 for row 2, and column 0 for row 3. A value of -1 indicates that the row
     * element is unmatched.
     *
     * In this case, the indices in the array correspond to the raptat annotations, and the number
     * at those indices correspond to the reference annotations.
     */
    int[] raptatReferenceMatching = new int[0];
    if (connectionGraph.length > 0) {
      MunkresAssignmentTest mr = new MunkresAssignmentTest(connectionGraph);
      raptatReferenceMatching = mr.solve();
    }
    //
    // HungarianAlgorithm ha = new HungarianAlgorithm(
    // connectionGraph,
    // false );
    // int[] raptatReferenceMatching = ha.execute();

    if (PerformanceScorer.LOGGER.isDebugEnabled()) {
      System.out.println("\n\n==========================\nOptimal Connections by "
          + "Hungarian Algorithm\n==========================\n");
      for (i = 0; i < raptatReferenceMatching.length; i++) {
        System.out.println(i + " --> " + raptatReferenceMatching[i]);
      }
    }

    List<AnnotationOverlapGraphEdge> annOverlapGraphEdges = new ArrayList<>();
    this.mentionIdToAnnotationGraphEdgeMap = new HashMap<>();

    for (i = 0; i < raptatReferenceMatching.length; i++) {
      if (raptatReferenceMatching[i] >= 0) {
        AnnotationOverlapGraphEdge newEdge = new AnnotationOverlapGraphEdge(
            referenceAnnotations.get(raptatReferenceMatching[i]).getMentionId(),
            raptatAnnotations.get(i).getMentionId(),
            connectionGraph[i][raptatReferenceMatching[i]]);
        annOverlapGraphEdges.add(newEdge);

        if (PerformanceScorer.LOGGER.isDebugEnabled()) {
          if (i == 0) {
            System.out.print("\n\n========================\nOverlappingAnnotationEdges");
            System.out.println("\n========================\n");
          }
          System.out.println("\n---------------------\n\n" + i + ") " + newEdge);
          System.out.println("\nRaptatAnnotation:" + raptatAnnotations.get(i));
          System.out.println(
              "\nReferenceAnnotation:" + referenceAnnotations.get(raptatReferenceMatching[i]));
        }
      }
    }

    /*
     * Build the connectionGraphmap for later use in assigning score via updateRelationshipScore().
     * This maps a reference annotation mentionID to a AnnotationGraphEdge storing the reference
     * annotation mentionID, the raptat annotation mentionID, and the connection score between the 2
     * annotations. It does this only for the optimal connections between reference and raptat
     * annotations.
     */
    for (AnnotationOverlapGraphEdge edge : annOverlapGraphEdges) {
      this.mentionIdToAnnotationGraphEdgeMap.put(edge.getRefNodeMentionID(), edge);
      this.mentionIdToAnnotationGraphEdgeMap.put(edge.getRaptatNodeMentionID(), edge);
    }

    return annOverlapGraphEdges;
  }


  private Hashtable<PerformanceScoreObject, int[]> createPsoToPerformanceMap(
      final HashMap<String, SchemaConcept> schemaConceptMap) {
    Hashtable<PerformanceScoreObject, int[]> performanceMap = new Hashtable<>();

    /*
     * The linkedFromConceptsMap is a mapping from the concept names of all concepts that are part
     * of a relationship that they are the "from" concept of. Each name maps to a HashMap of all the
     * relationships they are the "from" concept of and to the concept names of the concepts they
     * are linked "to" through that relationship. ("from" conceptName) -> relationshipName -> (Set
     * of "to" conceptNames)
     */
    HashMap<String, HashMap<String, HashSet<String>>> linkedFromConceptsMap =
        buildLinkedFromConceptsMap(schemaConceptMap);

    for (SchemaConcept schemaConcept : schemaConceptMap.values()) {
      /*
       * Skip relationships as they will be scored as part of each concept they are linked from
       */
      if (schemaConcept.isRelationship()) {
        continue;
      }

      String conceptName = schemaConcept.getConceptName().toLowerCase();

      PerformanceScoreObject conceptNameOnlyObject = new PerformanceScoreObject(conceptName);
      performanceMap.put(conceptNameOnlyObject, new int[] {0, 0, 0});
      PerformanceScoreObject exactMatchConceptObject =
          new PerformanceScoreObject("exactphrasematch_" + conceptName);
      performanceMap.put(exactMatchConceptObject, new int[] {0, 0, 0});

      addAttributesAndValuesToPerformanceMap(performanceMap, schemaConcept, conceptName);

      if (linkedFromConceptsMap.containsKey(conceptName)) {
        addRelationshipsToPerformanceMap(performanceMap, linkedFromConceptsMap, schemaConcept);
      }
    }

    performanceMap.put(PerformanceScoreObject.EXACT_MATCH_PSO, new int[] {0, 0, 0});

    performanceMap.put(PerformanceScoreObject.PARTIAL_MATCH_PSO, new int[] {0, 0, 0});

    for (PerformanceScoreObject key : performanceMap.keySet()) {
      System.out.println(key);
    }

    return performanceMap;
  }


  private HashMap<String, SchemaConcept> createSchemaConceptMap(
      final List<SchemaConcept> schemaConcepts) {
    HashMap<String, SchemaConcept> schemaConceptMap = new HashMap<>();
    for (SchemaConcept schemaConcept : schemaConcepts) {
      schemaConceptMap.put(schemaConcept.getConceptName(), schemaConcept);
    }

    return schemaConceptMap;
  }


  /**
   * Creates a list of AnnotationScorerNode type based on a list of annotated phrases.
   *
   * @param raptatAnnotations
   * @param raptatScorerList
   * @author Glenn Gobbel - Jun 18, 2012
   */
  private ArrayList<AnnotationScorerNode> createScorerNodeList(
      final List<AnnotatedPhrase> theAnnotations) {

    ArrayList<AnnotationScorerNode> resultList = new ArrayList<>();

    // Modified by Sanjib Saha - Aug 12, 2014 for multi-spanned
    // annotations
    for (AnnotatedPhrase curPhrase : theAnnotations) {
      if (curPhrase.getPrevPhrase() == null) {
        resultList.add(new AnnotationScorerNode(curPhrase));
      }
    }

    return resultList;
  }


  private RaptatPair<List<AnnotationScorerNode>, List<AnnotationScorerNode>> createScoringNodes(
      final List<AnnotatedPhrase> raptatAnnotations,
      final List<AnnotatedPhrase> referenceAnnotations) {
    /*
     * The scorer list is a list of annotations converted into "annotation nodes" which keep track
     * of whether an annotation has been matched in terms of phrase identification (partial or
     * exact) and concept
     */
    ArrayList<AnnotationScorerNode> raptatNodes = createScorerNodeList(raptatAnnotations);
    ArrayList<AnnotationScorerNode> referenceNodes = createScorerNodeList(referenceAnnotations);

    return new RaptatPair<>(referenceNodes, raptatNodes);
  }


  /**
   * @param referenceAnnotations
   * @return
   */
  private List<AnnotatedPhrase> filterForHeadPhrases(
      final List<AnnotatedPhrase> referenceAnnotations) {
    List<AnnotatedPhrase> filteredList = new ArrayList<>(referenceAnnotations.size());
    for (AnnotatedPhrase annotatedPhrase : referenceAnnotations) {
      if (annotatedPhrase.getPrevPhrase() == null) {
        filteredList.add(annotatedPhrase);
      }
    }
    return filteredList;
  }


  /**
   * Calculate the weighted matching score between two annotations. Returns 0 for concept mismatch
   * and non-overlap only for Attribute only matching to comply with Hungarian algorithm. If not in
   * ScoringMode.ATTRIBUTE_ONLY mode, the score will be this.weightOfConceptMatch +
   * (NumberOfMatchedAttributes * this.weightOfAttributeMatch).
   *
   * @param raptatPhrase The Raptat Phrase
   * @param referencePhrase The Reference Phrase
   * @param schemaConceptMap The Map with all Concepts
   * @return
   */
  @SuppressWarnings("null")
  private int getMatchingScoreBetweenAnnotations(final AnnotatedPhrase raptatPhrase,
      final AnnotatedPhrase referencePhrase,
      final HashMap<String, SchemaConcept> schemaConceptMap) {
    /* score < 0 indicates no overlap between phrase span */
    if (this.getTokenOverlap(raptatPhrase, referencePhrase) < 0) {
      /*
       * Glenn comment - 10/29/15 - I'm not sure why, if there is no overlap and we are only doing
       * attribute scoring that this returns 0 versus NO_OVERLAP_SCORE if there is just no overlap.
       */
      if (this.scoringMode == RaptatConstants.ScoringMode.ATTRIBUTE_ONLY) {
        return 0;
      }

      return PerformanceScorer.NO_OVERLAP_SCORE;
    }

    /*
     * If we get to here in the code, then overlap exists between phrases. Return
     * CONCEPT_MISMATCH_SCORE if the concepts are not matched
     */
    if (!raptatPhrase.getConceptName().toLowerCase()
        .equals(referencePhrase.getConceptName().toLowerCase())) {
      // return 0 if we are considering only attribute matching
      // score
      if (this.scoringMode == RaptatConstants.ScoringMode.ATTRIBUTE_ONLY) {
        return 0;
      }

      return PerformanceScorer.CONCEPT_MISMATCH_SCORE;
    }

    /*
     * If we get to this point in the code, there is a concept match and at least partial overlap.
     * Now score if the concept is the same in addition to there being at least partial overlap.
     */
    String conceptName = referencePhrase.getConceptName().toLowerCase();

    /* Create concatenation of concept with no attribute or value */
    // String mappedConceptName = concatenateConceptToAttributeValue( conceptName,
    // "", "" );
    SchemaConcept theSchemaConcept = schemaConceptMap.get(conceptName);

    int score = 0;

    List<RaptatAttribute> raptatAttributes;
    List<RaptatAttribute> refAttributes;

    Set<String> raptatAttributeValues;
    Set<String> refAttributeValues;

    RaptatAttribute refAttribute;
    RaptatAttribute raptatAttribute;

    /*
     * Do not allow double counting of matches. A raptat annotation will only be considered a true
     * positive if the reference annotation has not already been matched by another annotation.
     * Also, check for concept attributes match
     */
    raptatAttributes = raptatPhrase.getPhraseAttributes();
    refAttributes = referencePhrase.getPhraseAttributes();

    /*
     * Create variable mapping attribute name to actual attribute object
     */
    HashMap<String, RaptatAttribute> refAttributeMap = new HashMap<>();
    HashMap<String, RaptatAttribute> raptatAttributeMap = new HashMap<>();
    for (RaptatAttribute attribute : raptatAttributes) {
      raptatAttributeMap.put(attribute.getName(), attribute);
    }
    for (RaptatAttribute attribute : refAttributes) {
      refAttributeMap.put(attribute.getName(), attribute);
    }

    // adding the score for concept match
    if (this.scoringMode != RaptatConstants.ScoringMode.ATTRIBUTE_ONLY) {
      score += this.weightOfConceptMatch;
    } else {
      this.weightOfAttributeMatch = 1;
    }

    for (String attributeName : theSchemaConcept.getAttributeValueMap().keySet()) {
      refAttribute = refAttributeMap.get(attributeName);
      raptatAttribute = raptatAttributeMap.get(attributeName);

      /* None of the phrases have this attribute */
      if (refAttribute == null && raptatAttribute == null) {
        score += this.weightOfAttributeMatch;
      } else if (refAttribute == null || raptatAttribute == null) {
        continue;
      } else {
        raptatAttributeValues = new HashSet<>(raptatAttribute.getValues());
        refAttributeValues = new HashSet<>(refAttribute.getValues());

        for (String attrValue : theSchemaConcept.getAttributeValues(attributeName)) {
          // Both attributes contains the value
          if (raptatAttributeValues.contains(attrValue) && refAttributeValues.contains(attrValue)) {
            score += this.weightOfAttributeMatch;
          }
        }
      } // end if - else attribute
    } // end for-loop attributes

    return score;
  }


  /**
   * @param raptatNodes
   * @return
   */
  private Hashtable<String, AnnotationScorerNode> getMentionIDToAnnotationNodeMap(
      final List<AnnotationScorerNode> raptatNodes) {
    Hashtable<String, AnnotationScorerNode> raptatAnnotationMap = new Hashtable<>();

    /* Put the annotations into the maps */
    for (AnnotationScorerNode raptatAnnotation : raptatNodes) {
      raptatAnnotationMap.put(raptatAnnotation.annotatedPhrase.getMentionId(), raptatAnnotation);
    }
    return raptatAnnotationMap;
  }


  /**
   * Updates the performance score for the Phrase concepts and concept attributes
   *
   * @param curReferenceNode The Reference Node
   * @param curRaptatNode The Raptat Node
   * @param performance The performance score map
   * @param tokenOverlap The token overlap score
   * @param curScore The score
   */
  private boolean getPerformanceAttributeScore(final AnnotationScorerNode curReferenceNode,
      final AnnotationScorerNode curRaptatNode,
      final Hashtable<PerformanceScoreObject, int[]> performance, final int tokenOverlap,
      int[] curScore) {

    boolean match = true;
    int[] attrValScore;

    AnnotatedPhrase curReferencePhrase = curReferenceNode.annotatedPhrase;
    AnnotatedPhrase curRaptatPhrase = curRaptatNode.annotatedPhrase;

    List<RaptatAttribute> raptatAttributes;
    List<RaptatAttribute> refAttributes;
    RaptatAttribute refAttribute;

    String conceptName = curRaptatPhrase.getConceptName().toLowerCase();

    /*
     * Do not allow double counting of matches. A raptat annotation will only be considered a true
     * positive if the reference annotation has not already been matched by another annotation.
     *
     * Also checking for concept attributes match
     */
    raptatAttributes = curRaptatPhrase.getPhraseAttributes();
    refAttributes = curReferencePhrase.getPhraseAttributes();

    List<String> raptatAttributeValues;
    List<String> refAttributeValues;

    if (!curReferenceNode.partialMatch) {
      if (raptatAttributes == null && refAttributes == null) {
        match = true;
      } else if (raptatAttributes == null || refAttributes == null) {
        match = false;
      } else if (raptatAttributes.size() != refAttributes.size()) {
        match = false;
      }

      if (match && raptatAttributes != null) {
        HashMap<String, RaptatAttribute> refAttributeMap = new HashMap<>();

        for (RaptatAttribute attribute : refAttributes) {
          refAttributeMap.put(attribute.getName(), attribute);
          curReferenceNode.theAttributes.put(attribute.getName(), false);
        }

        for (RaptatAttribute raptatAttribute : raptatAttributes) {
          if ((refAttribute = refAttributeMap.get(raptatAttribute.getName())) == null) {
            match = false;
          }

          raptatAttributeValues = raptatAttribute.getValues();
          refAttributeValues = refAttribute.getValues();

          if (refAttributeValues.size() != raptatAttributeValues.size()) {
            match = false;
          }

          for (String refAttrValue : refAttributeValues) {
            PerformanceScoreObject pso =
                new PerformanceScoreObject(conceptName, raptatAttribute.getName(), refAttrValue);
            if (performance.get(pso) == null) {
              performance.put(pso, new int[] {0, 0, 0});
            }
          }

          for (String raptatAttrValue : raptatAttributeValues) {
            PerformanceScoreObject pso =
                new PerformanceScoreObject(conceptName, raptatAttribute.getName(), raptatAttrValue);
            if ((attrValScore = performance.get(pso)) == null) {
              attrValScore = new int[] {0, 0, 0};
              performance.put(pso, attrValScore);
            }

            if (refAttributeValues.contains(raptatAttrValue)) {
              attrValScore[PerformanceScorer.TP]++;
            } else {
              match = false;

              attrValScore = performance.get(pso);
              attrValScore[PerformanceScorer.FP]++;
            }
          }

          for (String refAttrValue : refAttributeValues) {
            if (!raptatAttributeValues.contains(refAttrValue)) {
              PerformanceScoreObject pso =
                  new PerformanceScoreObject(conceptName, raptatAttribute.getName(), refAttrValue);
              attrValScore = performance.get(pso);
              attrValScore[PerformanceScorer.FN]++;
            }
          }

          curRaptatNode.theAttributes.put(raptatAttribute.getName(), match);
          curReferenceNode.theAttributes.put(refAttribute.getName(), match);
        }
      }

      if (match) {

        curScore[PerformanceScorer.TP]++;

        PerformanceScoreObject pso =
            new PerformanceScoreObject("partialphrasematch_" + conceptName);
        curScore = performance.get(pso);
        curScore[PerformanceScorer.TP]++;

        curRaptatNode.partialMatch = true;
        curReferenceNode.partialMatch = true;
      }
    }

    // Only count partial matches for the raptat node
    // when the reference node annotation has never been matched.
    // This prevents double counting of partial matches.
    if (match) {
      if (!curReferenceNode.partialMatch) {
        curRaptatNode.partialMatch = true;
        curReferenceNode.partialMatch = true;

        if (tokenOverlap > 0) {
          PerformanceScoreObject pso =
              new PerformanceScoreObject("exactphrasematch_" + conceptName);
          curScore = performance.get(pso);
          curScore[PerformanceScorer.TP]++;
          // Exact overlap
          curRaptatNode.exactMatch = true;
          curReferenceNode.exactMatch = true;
        }
      }
    }

    return match;
  }


  /**
   * Determine the overlap between two multi-spanned annotated phrases. Returns 1 if the tokens
   * overlap completely, 0 if some of the tokens overlap, and -1 if None overlap.
   *
   * <p>
   * Preconditions: Token sequences are from the same document. Postconditions: None.
   *
   * @param phrase1
   * @param phrase2
   * @return Number of overlapping tokens in the two phrases
   * @author Sanjib Saha - Dec 5, 2014
   */
  private int getTokenOverlap(final AnnotatedPhrase phrase1, final AnnotatedPhrase phrase2) {
    /* Only do comparison if we are dealiing with the head phrase */
    if (phrase1.getPrevPhrase() != null || phrase2.getPrevPhrase() != null) {
      return -1;
    }

    List<RaptatToken> tokenList1 = new ArrayList<>();
    List<RaptatToken> tokenList2 = new ArrayList<>();

    AnnotatedPhrase head = phrase1;

    while (head != null) {
      if (head.getProcessedTokens() != null) {
        tokenList1.addAll(head.getProcessedTokens());
      }
      head = head.getNextPhrase();
    }

    head = phrase2;
    while (head != null) {
      if (head.getProcessedTokens() != null) {
        tokenList2.addAll(head.getProcessedTokens());
      }
      head = head.getNextPhrase();
    }

    return this.getTokenOverlap(tokenList1, tokenList2);
  }


  /**
   * Determine the overlap between two lists of tokens. Returns 1 if the tokens overlap completely,
   * 0 if some of the tokens overlap, and -1 if None overlap.
   *
   * <p>
   * Preconditions: Token sequences are from the same document. Postconditions: None.
   *
   * @param tokenList1
   * @param tokenList2
   * @return Number of overlapping tokens in the two phrases
   * @author Glenn Gobbel - Apr 10, 2012
   */
  @SuppressWarnings("null")
  private int getTokenOverlap(final List<RaptatToken> tokenList1,
      final List<RaptatToken> tokenList2) {
    if (tokenList1 == null || tokenList2 == null) {
      return -1;
    }

    RaptatToken list1Token, list2Token;
    Iterator<RaptatToken> list1Iterator, list2Iterator = null;

    int matchLength = 0;
    boolean matchFound = false;

    // Lists need to be in sequential order for algorithm to work
    StartOffsetTokenComparator tokenSequenceComparator = new StartOffsetTokenComparator();

    List<RaptatToken> copyOfList1 = new ArrayList<>(tokenList1);
    List<RaptatToken> copyOfList2 = new ArrayList<>(tokenList2);

    Collections.sort(copyOfList1, tokenSequenceComparator);
    Collections.sort(copyOfList2, tokenSequenceComparator);

    // Find first matching token
    list1Iterator = copyOfList1.iterator();

    while (list1Iterator.hasNext() && !matchFound) {
      list1Token = list1Iterator.next();

      list2Iterator = copyOfList2.iterator();

      while (list2Iterator.hasNext()) {
        list2Token = list2Iterator.next();

        if (list1Token.getTokenStringPreprocessed().equals(list2Token.getTokenStringPreprocessed())
            && list1Token.getStartOffset().equals(list2Token.getStartOffset())) // MatchFound
        {
          matchFound = true;
          matchLength++;
          break;
        }
      }
    }

    // Once the match is found, proceed until we reach the end of
    // the lists.
    if (matchFound) {
      while (list1Iterator.hasNext() && list2Iterator.hasNext()) {
        list1Token = list1Iterator.next();
        list2Token = list2Iterator.next();

        if (list1Token.getTokenStringPreprocessed().equals(list2Token.getTokenStringPreprocessed())
            && list1Token.getStartOffset().equals(list2Token.getStartOffset())) // MatchFound
        {
          matchLength++;
        }
      }

      if (matchLength < copyOfList1.size() || matchLength < copyOfList2.size()) {
        return 0; // Partial Overlap
      }

      return 1; // Exact Match
    }
    return -1; // No Overlap
  }


  /**
   * ***********************************************************
   *
   * @param curResult
   * @param summedResult
   * @return
   * @author Glenn Gobbel - Jun 19, 2012 * * * * * * * * * * * * * * * * * * *
   *         ****************************************
   */
  private Hashtable<PerformanceScoreObject, int[]> mergeResults(
      final Hashtable<PerformanceScoreObject, int[]> curResult,
      Hashtable<PerformanceScoreObject, int[]> summedResult) {
    int[] scoreToDate, newScore;

    if (summedResult == null) {
      summedResult = new Hashtable<>(curResult.size());
    }

    Enumeration<PerformanceScoreObject> theKeys = curResult.keys();
    while (theKeys.hasMoreElements()) {
      PerformanceScoreObject key = theKeys.nextElement();
      newScore = curResult.get(key);
      if ((scoreToDate = summedResult.get(key)) == null) {
        scoreToDate = new int[] {0, 0, 0};
        summedResult.put(key, scoreToDate);
      }
      scoreToDate[0] += newScore[0];
      scoreToDate[1] += newScore[1];
      scoreToDate[2] += newScore[2];
    }
    return summedResult;
  }


  private void print(final Hashtable<PerformanceScoreObject, int[]> result) {
    Enumeration<PerformanceScoreObject> keys = result.keys();
    while (keys.hasMoreElements()) {
      PerformanceScoreObject pso = keys.nextElement();
      int[] curScore = result.get(pso);
      System.out.println(pso + "\t" + curScore[0] + "\t" + curScore[1] + "\t" + curScore[2]);
    }
  }


  private void printTabDelimited(final Hashtable<String, int[]> scores,
      final String outputFilePath) {
    PrintWriter pw = null;
    try {
      File outputFile = new File(outputFilePath);
      pw = new PrintWriter(outputFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(-99);
    }

    Enumeration<String> hashKeys = scores.keys();
    while (hashKeys.hasMoreElements()) {
      String curKey = hashKeys.nextElement();
      int[] curScore = scores.get(curKey);
      pw.print(curKey);
      for (int i = 0; i < curScore.length; i++) {
        pw.print("\t" + curScore[i]);
      }
      pw.println("");
      pw.flush();
    }
    pw.close();
  }


  /**
   * *
   *
   * @param performance
   * @param curNode
   * @param elementToScore
   * @author Glenn Gobbel - Jun 18, 2012
   * @param performance
   */
  @Deprecated // Called only by Garvin create project, bootstrap
  // analysis, and
  // active learning
  private void scoreNode(final Hashtable<PerformanceScoreObject, int[]> performance,
      final AnnotationScorerNode curNode, final int elementToScore) {
    int[] curScore;
    boolean partialPhraseMatch = true, exactPhraseMatch = true, conceptMatch = true;

    String conceptName = curNode.annotatedPhrase.getConceptName().toLowerCase();

    if (!curNode.partialMatchIgnoringConcept) {
      curScore = performance.get(PerformanceScoreObject.PARTIAL_MATCH_PSO);
      curScore[elementToScore]++;

      // partialPhraseMatch = false;
    }

    if (!curNode.partialMatch) {
      PerformanceScoreObject partialConceptScore =
          new PerformanceScoreObject("partialphrasematch_" + conceptName);
      if ((curScore = performance.get(partialConceptScore)) == null) {
        curScore = new int[] {0, 0, 0};
        performance.put(partialConceptScore, curScore);
      }

      curScore[elementToScore]++;
    }

    if (!curNode.exactMatchIgnoringConcept) {
      curScore = performance.get(PerformanceScoreObject.EXACT_MATCH_PSO);
      curScore[elementToScore]++;
      // exactPhraseMatch = false;
    }

    if (!curNode.exactMatch) {
      PerformanceScoreObject exactConceptScore =
          new PerformanceScoreObject("exactphrasematch_" + conceptName);
      if ((curScore = performance.get(exactConceptScore)) == null) {
        curScore = new int[] {0, 0, 0};
        performance.put(exactConceptScore, curScore);
      }

      curScore[elementToScore]++;
    }

    if (!curNode.partialMatch) {
      PerformanceScoreObject conceptScoreObject = new PerformanceScoreObject(conceptName);
      if ((curScore = performance.get(conceptScoreObject)) == null) {
        curScore = new int[] {0, 0, 0};
        performance.put(conceptScoreObject, curScore);
      }

      curScore[elementToScore]++;
      conceptMatch = false;
    }

    if (PerformanceScorer.LOGGER.getEffectiveLevel() == Level.DEBUG
        && (!partialPhraseMatch || !exactPhraseMatch || !conceptMatch)) {
      AnnotatedPhrase curPhrase = curNode.annotatedPhrase;
      String phraseString =
          Arrays.toString(curPhrase.getProcessedTokensAugmentedStrings().toArray());

      System.out.print(phraseString + " (" + curPhrase.getRawTokensStartOff() + "-"
          + curPhrase.getRawTokensEndOff() + "):");

      if (!exactPhraseMatch) {
        System.out.print(" *No ExactPhraseMatch* ");
      }
      if (!partialPhraseMatch) {
        System.out.print(" *No PartialPhraseMatch* ");
      }
      if (!conceptMatch) {
        System.out.print(" *No ConceptMatch* ");
      }
      System.out.println();
    }
  }


  private void scoreNodeForActiveLearning(final Hashtable<String, int[]> performance,
      final AnnotationScorerNode curNode, final int elementToScore) {
    int[] curScore;

    if (!curNode.partialMatchIgnoringConcept) {
      curScore = performance.get(RaptatConstants.TOTAL_PARTIAL_MATCH_NAME);
      curScore[elementToScore]++;
    }

    if (!curNode.exactMatchIgnoringConcept) {
      curScore = performance.get(RaptatConstants.TOTAL_EXACT_MATCH_NAME);
      curScore[elementToScore]++;
    }
  }


  /**
   * Helper method for scorePerformance(). It updates performance scores based on data stored in all
   * the AnnotationScorerNodes which keep track of whether there has been a phrase or concept
   * matched for each of the Raptat and reference annotations
   *
   * @param performance
   * @param raptatScorerList
   * @param referenceScorerList
   * @author Glenn Gobbel - Jun 18, 2012
   */
  @Deprecated // Called only by Garvin create project, bootstrap
  // analysis, and
  // active learning
  private void updatePerformance(final Hashtable<PerformanceScoreObject, int[]> performance,
      final ArrayList<AnnotationScorerNode> raptatScorerList,
      final ArrayList<AnnotationScorerNode> referenceScorerList) {
    if (PerformanceScorer.LOGGER.isDebugEnabled()) {
      System.out.println("\nFALSE POSITIVES");
      System.out.println("-----------------");
    }
    for (AnnotationScorerNode curRaptatNode : raptatScorerList) {
      scoreNode(performance, curRaptatNode, PerformanceScorer.FP);
    }

    if (PerformanceScorer.LOGGER.isDebugEnabled()) {
      System.out.println("\nFALSE NEGATIVES");
      System.out.println("-----------------");
    }
    for (AnnotationScorerNode curRefNode : referenceScorerList) {
      scoreNode(performance, curRefNode, PerformanceScorer.FN);
    }
  }


  private void updatePerformanceForActiveLearning(final Hashtable<String, int[]> performance,
      final ArrayList<AnnotationScorerNode> raptatScorerList,
      final ArrayList<AnnotationScorerNode> referenceScorerList) {

    for (AnnotationScorerNode curRaptatNode : raptatScorerList) {
      scoreNodeForActiveLearning(performance, curRaptatNode, PerformanceScorer.FP);
    }

    for (AnnotationScorerNode curRefNode : referenceScorerList) {
      scoreNodeForActiveLearning(performance, curRefNode, PerformanceScorer.FN);
    }
  }


  /**
   * Updates the performance score for the phrase concept
   *
   * @param performance The performance score map
   * @param node The Node to be scored
   * @param scoreToUpdate The score to be updated
   * @param schemaConceptMap The map of all concepts
   */
  private void updatePerformanceScoreForConcept(
      final Hashtable<PerformanceScoreObject, int[]> performance, final AnnotationScorerNode node,
      final int scoreToUpdate, final HashMap<String, SchemaConcept> schemaConceptMap) {
    int[] score;

    String conceptName = node.annotatedPhrase.getConceptName().toLowerCase();

    PerformanceScoreObject conceptMatchPSO = new PerformanceScoreObject(conceptName);
    score = performance.get(conceptMatchPSO);
    score[scoreToUpdate]++;

    PerformanceScoreObject exactMatchConceptPSO =
        new PerformanceScoreObject("exactphrasematch_" + conceptName);
    score = performance.get(exactMatchConceptPSO);
    score[scoreToUpdate]++;

    PerformanceScoreObject partialMatchPSO =
        new PerformanceScoreObject(RaptatConstants.TOTAL_PARTIAL_MATCH_NAME);
    score = performance.get(partialMatchPSO);
    score[scoreToUpdate]++;

    PerformanceScoreObject totalMatchPSO =
        new PerformanceScoreObject(RaptatConstants.TOTAL_EXACT_MATCH_NAME);
    score = performance.get(totalMatchPSO);
    score[scoreToUpdate]++;
  }


  private void updatePsoScoreForRelationshipAttributes() {
    System.err
        .println("The updatePsoScoreForRelationshipAttributes() method is not yet implemented");
    System.exit(-1);
  }


  /**
   * @param annotatedPhrase
   * @param mentionIdToAnnotationMap
   * @param performance
   * @param relatedConcept
   * @param linkedToMentionId
   */
  private void updatePsoToScoreMapForRelationship(final AnnotatedPhrase annotatedPhrase,
      final Hashtable<String, AnnotationScorerNode> mentionIdToAnnotationMap,
      final Hashtable<PerformanceScoreObject, int[]> performance, final String conceptRelationName,
      final String linkedToMentionId, final int scoreIndex) {
    String fromConcept = annotatedPhrase.getConceptName();
    AnnotatedPhrase linkedToAnnotation =
        mentionIdToAnnotationMap.get(linkedToMentionId).annotatedPhrase;
    String toConcept = linkedToAnnotation.getConceptName();
    PerformanceScoreObject pso = new PerformanceScoreObject(fromConcept, null, null,
        conceptRelationName, toConcept, null, null);
    int[] score = performance.get(pso);
    score[scoreIndex]++;
  }


  /**
   * Updates the performance score for the Phrase concepts and concept attributes where there is at
   * least partial overlap between phrases assigned to the same concept
   *
   * @param referenceNode The Reference Node
   * @param tokenOverlap The token overlap score
   * @param performance The performance score map
   * @param schemaConceptMap The map containing all concepts
   */
  private void updateSummaryPerformanceAttributeScore(final AnnotationScorerNode referenceNode,
      final int tokenOverlap, final Hashtable<PerformanceScoreObject, int[]> performance,
      final HashMap<String, SchemaConcept> schemaConceptMap) {
    int[] score;
    RaptatAttribute refAttribute;
    RaptatAttribute raptatAttribute;

    String conceptName = referenceNode.annotatedPhrase.getConceptName().toLowerCase();
    SchemaConcept theSchemaConcept = schemaConceptMap.get(conceptName);

    List<RaptatAttribute> refAttributes = referenceNode.annotatedPhrase.getPhraseAttributes();
    List<RaptatAttribute> raptatAttributes = referenceNode.matchingAnnotation.getPhraseAttributes();

    List<String> raptatAttributeValues;
    List<String> refAttributeValues;

    HashMap<String, RaptatAttribute> refAttributeMap = new HashMap<>();
    HashMap<String, RaptatAttribute> raptatAttributeMap = new HashMap<>();

    for (RaptatAttribute attribute : raptatAttributes) {
      raptatAttributeMap.put(attribute.getName(), attribute);
    }

    for (RaptatAttribute attribute : refAttributes) {
      refAttributeMap.put(attribute.getName(), attribute);
    }

    for (String attributeName : theSchemaConcept.getAttributeValueMap().keySet()) {
      refAttribute = refAttributeMap.get(attributeName);
      raptatAttribute = raptatAttributeMap.get(attributeName);

      // Neither of the phrases have this attribute
      if (refAttribute == null && raptatAttribute == null) {
        PerformanceScoreObject nullPSO = new PerformanceScoreObject(conceptName, attributeName,
            RaptatConstants.NULL_ATTRIBUTE_VALUE, null, null, null, null);
        score = performance.get(nullPSO);
        score[PerformanceScorer.TP]++;
      }

      // Reference phrase does not have this attribute
      else if (refAttribute == null) {
        PerformanceScoreObject nullPSO = new PerformanceScoreObject(conceptName, attributeName,
            RaptatConstants.NULL_ATTRIBUTE_VALUE, null, null, null, null);
        score = performance.get(nullPSO);
        score[PerformanceScorer.FN]++;

        for (String attrValue : raptatAttribute.getValues()) {
          PerformanceScoreObject pso = new PerformanceScoreObject(conceptName, attributeName,
              attrValue, null, null, null, null);
          score = performance.get(pso);
          score[PerformanceScorer.FP]++;
        }
      }

      // Raptat phrase does not have this attribute
      else if (raptatAttribute == null) {
        PerformanceScoreObject nullPSO = new PerformanceScoreObject(conceptName, attributeName,
            RaptatConstants.NULL_ATTRIBUTE_VALUE, null, null, null, null);
        score = performance.get(nullPSO);
        score[PerformanceScorer.FP]++;

        for (String attrValue : refAttribute.getValues()) {
          PerformanceScoreObject pso = new PerformanceScoreObject(conceptName, attributeName,
              attrValue, null, null, null, null);
          score = performance.get(pso);
          score[PerformanceScorer.FN]++;
        }
      } else {
        raptatAttributeValues = raptatAttribute.getValues();
        refAttributeValues = refAttribute.getValues();

        for (String attrValue : theSchemaConcept.getAttributeValues(attributeName)) {
          PerformanceScoreObject pso = new PerformanceScoreObject(conceptName, attributeName,
              attrValue, null, null, null, null);

          // Both attribute contains the value
          if (raptatAttributeValues.contains(attrValue) && refAttributeValues.contains(attrValue)) {
            score = performance.get(pso);
            score[PerformanceScorer.TP]++;
          }

          // Reference attribute contains the value
          else if (refAttributeValues.contains(attrValue)) {
            score = performance.get(pso);
            score[PerformanceScorer.FN]++;
          }

          // Raptat attribute contains the value
          else if (raptatAttributeValues.contains(attrValue)) {
            score = performance.get(pso);
            score[PerformanceScorer.FP]++;
          }
        }
      } // end if - else attribute
    } // end for-loop attributes
  }


  /**
   * @param referenceNode
   * @param tokenOverlap
   * @param performance
   * @return
   */
  private void updateSummaryPerformanceConceptScore(final String conceptName,
      final int tokenOverlap, final Hashtable<PerformanceScoreObject, int[]> performance) {
    int[] score;

    PerformanceScoreObject conceptPSO = new PerformanceScoreObject(conceptName);
    score = performance.get(conceptPSO);
    score[PerformanceScorer.TP]++;

    PerformanceScoreObject exactMatchPSO =
        new PerformanceScoreObject("exactphrasematch_" + conceptName);

    if (tokenOverlap > 0) {
      score = performance.get(exactMatchPSO);
      score[PerformanceScorer.TP]++;
    } else {
      score = performance.get(exactMatchPSO);
      score[PerformanceScorer.FP]++;

      score = performance.get(exactMatchPSO);
      score[PerformanceScorer.FN]++;
    }
  }


  /**
   * Updates the performance score for the relationships and its attributes. This is only called if
   * the raptatPhrase and refPhrase overlap and map to the same concept
   *
   * @param referencePhrase
   * @param raptatPhrase
   * @param raptatIdToAnnotationMap
   * @param referenceIdToAnnotationMap
   * @param performance
   * @param schemaConceptMap
   */
  private void updateSummaryPerformanceRelationshipScore(final AnnotatedPhrase referencePhrase,
      final AnnotatedPhrase raptatPhrase,
      final Hashtable<String, AnnotationScorerNode> referenceIdToAnnotationMap,
      final Hashtable<String, AnnotationScorerNode> raptatIdToAnnotationMap,
      final Hashtable<PerformanceScoreObject, int[]> performance) throws Exception {
    PerformanceScorer.LOGGER.debug(
        "Relating: " + referencePhrase.getConceptName() + " " + raptatPhrase.getConceptName());

    /*
     * Create sets that equally has to reference and raptat concept relations if they are the same
     * (have same name and are connected to one of the two nodes on the end of an
     * AnnotationOverlapGraphEdge
     */
    HashSet<RelationshipScorerNode> referenceRelations = new HashSet<>();
    for (ConceptRelation conceptRelation : referencePhrase.getConceptRelations()) {
      String conceptRelationName = conceptRelation.getConceptRelationName();
      String linkedToMentionId = conceptRelation.getRelatedAnnotationID();
      AnnotationOverlapGraphEdge connectionEdge =
          this.mentionIdToAnnotationGraphEdgeMap.get(linkedToMentionId);

      /*
       * If not connection edge found, it must not have a matching raptat annotation and is a FN
       */
      if (connectionEdge == null) {
        updatePsoToScoreMapForRelationship(referencePhrase, referenceIdToAnnotationMap, performance,
            conceptRelationName, linkedToMentionId, RaptatConstants.TrueFalseScores.FN.ordinal());
      } else {
        referenceRelations.add(new RelationshipScorerNode(conceptRelationName, connectionEdge));
      }
    }

    HashSet<RelationshipScorerNode> raptatRelations = new HashSet<>();
    for (ConceptRelation conceptRelation : raptatPhrase.getConceptRelations()) {
      String conceptRelationName = conceptRelation.getConceptRelationName();
      String linkedToMentionId = conceptRelation.getRelatedAnnotationID();
      AnnotationOverlapGraphEdge connectionEdge =
          this.mentionIdToAnnotationGraphEdgeMap.get(linkedToMentionId);
      /*
       * If no connection edge is found, it must not have a matching reference anntoation and is a
       * FP
       */
      if (connectionEdge == null) {
        updatePsoToScoreMapForRelationship(raptatPhrase, raptatIdToAnnotationMap, performance,
            conceptRelationName, linkedToMentionId, RaptatConstants.TrueFalseScores.FP.ordinal());
      } else {
        raptatRelations.add(new RelationshipScorerNode(conceptRelationName, connectionEdge));
      }
    }

    /*
     * Determine true positives, false positives, and false negatives by comparing referenceRelation
     * to raptatRelation set. We don't change referenceRelations during loop so as to avoid
     * concurrent modification exception.
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
    String fromConcept = referencePhrase.getConceptName();
    for (RelationshipScorerNode node : truePositiveRelations) {
      String linkedToMentionID = node.edge.getRefNodeMentionID();
      AnnotatedPhrase linkedToAnnotation =
          referenceIdToAnnotationMap.get(linkedToMentionID).annotatedPhrase;
      String toConcept = linkedToAnnotation.getConceptName();
      PerformanceScoreObject pso = new PerformanceScoreObject(fromConcept, null, null,
          node.relationshipName, toConcept, null, null);
      int[] score = performance.get(pso);
      score[RaptatConstants.TrueFalseScores.TP.ordinal()]++;
    }

    /* Score False Positives */
    fromConcept = raptatPhrase.getConceptName();
    for (RelationshipScorerNode node : raptatRelations) {
      String linkedToMentionID = node.edge.getRaptatNodeMentionID();
      AnnotatedPhrase linkedToAnnotation =
          raptatIdToAnnotationMap.get(linkedToMentionID).annotatedPhrase;
      String toConcept = linkedToAnnotation.getConceptName();
      PerformanceScoreObject pso = new PerformanceScoreObject(fromConcept, null, null,
          node.relationshipName, toConcept, null, null);
      int[] score = performance.get(pso);
      score[RaptatConstants.TrueFalseScores.FP.ordinal()]++;
    }

    /* Score False Negatives */
    fromConcept = referencePhrase.getConceptName();
    for (RelationshipScorerNode node : falseNegativeReferenceRelations) {
      String linkedToMentionID = node.edge.getRefNodeMentionID();
      AnnotatedPhrase linkedToAnnotation =
          referenceIdToAnnotationMap.get(linkedToMentionID).annotatedPhrase;
      String toConcept = linkedToAnnotation.getConceptName();
      PerformanceScoreObject pso = new PerformanceScoreObject(fromConcept, null, null,
          node.relationshipName, toConcept, null, null);
      int[] score = performance.get(pso);
      score[RaptatConstants.TrueFalseScores.FN.ordinal()]++;
    }
  }


  /**
   * Updates the performance score for the Phrases that has at least partial overlap between
   * phrases. <br>
   * <br>
   * <b>PRECONDITION:</b> Nodes must overlap partially or exactly.
   *
   * @param referenceNode The Reference Node
   * @param raptatNode The Raptat Node
   * @param raptatIdToAnnotationMap
   * @param referenceIdToAnnotationMap
   * @param psoToPeformanceMap The performance score map
   * @param conceptMatchValue The match score between the Phrases
   * @param schemaConceptMap The map containing all concepts
   */
  private void updateSummaryPerformanceScore(final AnnotationScorerNode referenceNode,
      final AnnotationScorerNode raptatNode,
      final Hashtable<String, AnnotationScorerNode> referenceIdToAnnotationMap,
      final Hashtable<String, AnnotationScorerNode> raptatIdToAnnotationMap,
      final Hashtable<PerformanceScoreObject, int[]> psoToPeformanceMap,
      final int conceptMatchValue, final HashMap<String, SchemaConcept> schemaConceptMap) {

    /* Precondition that there is overlap */
    raptatNode.partialMatchIgnoringConcept = true;
    referenceNode.partialMatchIgnoringConcept = true;

    int tokenOverlap =
        this.getTokenOverlap(referenceNode.annotatedPhrase, raptatNode.annotatedPhrase);
    if (tokenOverlap > 0) {
      raptatNode.exactMatchIgnoringConcept = true;
      referenceNode.exactMatchIgnoringConcept = true;
    }

    /*
     * Handle case for which conceptMatchValue exceeds CONCEPT_MISMATCH_SCORE indicating that not
     * only spans but also concepts are matched
     */
    if (conceptMatchValue > PerformanceScorer.CONCEPT_MISMATCH_SCORE) {
      int[] score;

      // We already know that there is at least partial overlap
      score = psoToPeformanceMap.get(PerformanceScoreObject.PARTIAL_MATCH_PSO);
      score[PerformanceScorer.TP]++;

      /*
       * Score these as true as there is partial overlap and concepts are matched
       */
      raptatNode.partialMatch = true;
      referenceNode.partialMatch = true;

      referenceNode.setMatchedAnnotation(raptatNode);

      // Token Overlap of > 0 indicates exact match.
      if (tokenOverlap > 0) {
        raptatNode.exactMatch = true;
        referenceNode.exactMatch = true;

        score = psoToPeformanceMap.get(PerformanceScoreObject.EXACT_MATCH_PSO);
        score[PerformanceScorer.TP]++;
      } else {
        score = psoToPeformanceMap.get(PerformanceScoreObject.EXACT_MATCH_PSO);
        score[PerformanceScorer.FP]++;

        score = psoToPeformanceMap.get(PerformanceScoreObject.EXACT_MATCH_PSO);
        score[PerformanceScorer.FN]++;
      }

      String conceptName = referenceNode.annotatedPhrase.getConceptName().toLowerCase();
      updateSummaryPerformanceConceptScore(conceptName, tokenOverlap, psoToPeformanceMap);
      updateSummaryPerformanceAttributeScore(referenceNode, tokenOverlap, psoToPeformanceMap,
          schemaConceptMap);

      /*
       *
       * Glenn Comment - 11/4/15 - Added error catch because the updateRelationshipScore should
       * check that any relationships found should actually be relationships in the schema
       */
      try {
        AnnotatedPhrase referencePhrase = referenceNode.annotatedPhrase;
        AnnotatedPhrase raptatPhrase = referenceNode.matchingAnnotation;
        updateSummaryPerformanceRelationshipScore(referencePhrase, raptatPhrase,
            referenceIdToAnnotationMap, raptatIdToAnnotationMap, psoToPeformanceMap);
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
        GeneralHelper.errorWriter(e.getMessage());
      }
    }
  }


  /**
   * Writes out details needed for error analysis. It duplicates much of the code within
   * getPerformanceScore. It is written as a separate class to avoid multiple "if" testing and speed
   * processing.
   *
   * @param raptatAnnotations
   * @param referenceAnnotations
   * @param docSource
   */
  /*
   * Marking this as deprecated because it **should** probably be merged into
   * scorePerformanceWithGraph as it reproduces a lot of the code there
   */
  @Deprecated
  private void writeDetailedPerformanceScore(final List<AnnotatedPhrase> raptatAnnotations,
      final List<AnnotatedPhrase> referenceAnnotations, final String docSource) {
    ArrayList<AnnotationScorerNode> raptatScorerList = createScorerNodeList(raptatAnnotations);
    ArrayList<AnnotationScorerNode> referenceScorerList =
        createScorerNodeList(referenceAnnotations);

    /*
     * Go through each Raptat annotation and compare to the reference annotations
     */
    for (AnnotationScorerNode curRaptatNode : raptatScorerList) {
      AnnotatedPhrase curRaptatPhrase = curRaptatNode.annotatedPhrase;
      for (AnnotationScorerNode curReferenceNode : referenceScorerList) {
        /*
         * If the reference annotation hasn't already been matched with at least a partial match and
         * concept match, then check it
         */
        if (!curReferenceNode.exactMatch && !curReferenceNode.partialMatch) {
          AnnotatedPhrase curReferencePhrase = curReferenceNode.annotatedPhrase;

          /*
           * tokenOverlap will be -1 if no overlap, 0 if partial overlap, and 1 if complete overlap
           */
          int tokenOverlap = this.getTokenOverlap(curRaptatPhrase, curReferencePhrase);

          if (tokenOverlap > -1) // At least partial overlap
          {
            List<RaptatPair<String, Integer>> conceptValueResults =
                compareConceptAttributeValues(curRaptatPhrase, curReferencePhrase);

            String curConcept = curRaptatPhrase.getConceptName().toLowerCase();
            if (curConcept.equalsIgnoreCase(curReferencePhrase.getConceptName())) {
              curRaptatNode.partialMatch = true;
              curReferenceNode.partialMatch = true;
            }
            curRaptatNode.partialMatch = true;
            curReferenceNode.partialMatch = true;
            if (tokenOverlap > 0) // Exact overlap
            {
              curRaptatNode.exactMatch = true;
              curReferenceNode.exactMatch = true;
            }

            this.annotationComparisonWriter.writeOverlapScore(curRaptatNode, curReferenceNode,
                conceptValueResults, docSource);
          }
        }
      }
    }

    this.annotationComparisonWriter.writeCompleteFalsePositives(raptatScorerList, docSource);
    this.annotationComparisonWriter.writeCompleteFalseNegatives(referenceScorerList, docSource);
  }


  private void writeDetailedPerformanceScoreNew(
      final Hashtable<String, AnnotationScorerNode> raptatAnnotationMap,
      final Hashtable<String, AnnotationScorerNode> referenceAnnotationMap,
      final HashMap<String, SchemaConcept> schemaConceptMap, final String docSource) {
    for (AnnotationScorerNode referenceNode : referenceAnnotationMap.values()) {
      this.annotationComparisonWriter.writeReferenceNodeResult(this, referenceNode,
          raptatAnnotationMap, referenceAnnotationMap, schemaConceptMap, docSource);
    }

    for (AnnotationScorerNode raptatNode : raptatAnnotationMap.values()) {
      /*
       * Only write raptatNode results for those without a concept match. Those that are matched
       * will be handled by writing the reference node results
       */
      if (!raptatNode.partialMatch) {
        this.annotationComparisonWriter.writeRaptatNodeResult(raptatNode, docSource);
      }
    }
  }


  /**
   * @param performanceScores
   */
  protected void print(final List<PerformanceScore> performanceScores) {
    for (PerformanceScore score : performanceScores) {
      System.out.println(score.toString());
    }
  }


  public static Hashtable<PerformanceScoreObject, int[]> compareFileSets(
      final File textFileDirectory, final File referenceFileDirectory,
      final File raptatFileDirectory, final File schemaConceptFile,
      final File acceptableConceptsFile, final AnnotationApp annotationApp,
      final boolean removeUnmatched) {
    List<SchemaConcept> schemaConcepts = SchemaImporter.importSchemaConcepts(schemaConceptFile);
    HashSet<String> acceptedConcepts = new HashSet<>();
    if (acceptableConceptsFile != null) {
      acceptedConcepts = GeneralHelper.retrieveLinesAsSet(acceptableConceptsFile);
    }

    List<AnnotationGroup> annotationGroups =
        createAnnotationGroups(textFileDirectory, referenceFileDirectory, raptatFileDirectory,
            schemaConcepts, acceptedConcepts, annotationApp, removeUnmatched);

    String textFileParentPath = textFileDirectory.getParent();
    PerformanceScorer scorer = new PerformanceScorer(textFileParentPath, true, "");
    Hashtable<PerformanceScoreObject, int[]> resultScores =
        scorer.scorePerformanceWithGraph(annotationGroups, schemaConcepts);

    return resultScores;
  }


  /**
   * @param args
   */
  public static void main(final String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        PerformanceScorerParser parser = new PerformanceScorerParser();
        ScoringParameterObject parameters = parser.parseCommandLine(args);

        /*
         * Evaluation data
         */
        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestingCorpus"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestingXML_UncertainToPositive"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestXML_UncToPositiveRaptatOutput"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\AKIProjectSchemaNoUncertain.xml"
        // );

        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestingCorpus"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestingXML_UncertainToPositive"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestXML_UncToPositiveRaptatOutput"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\AKIProjectSchemaNoUncertain.xml"
        // );

        /*
         * AstraZeneca IAA
         */
        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\AnnotationManagment\\IntraAnnotaterAgreement\\Glenn_IAAscores\\Blocks_6_7_8"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\AnnotationManagment\\IntraAnnotaterAgreement\\Glenn_IAAscores\\Liz_6_7_8"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\AnnotationManagment\\IntraAnnotaterAgreement\\Glenn_IAAscores\\Nancy_6_7_8"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\AnnotationManagment\\IntraAnnotaterAgreement\\Glenn_IAAscores\\projectschema.xml"
        // );
        // File acceptableConceptsFile = new File(
        // "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\AnnotationManagment\\IntraAnnotaterAgreement\\Glenn_IAAscores\\acceptableConcepts_161129.txt"
        // );

        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestingCorpus"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestingXML_UncertainToPositive"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\Evaluation_161124\\TestXML_UncToPositiveRaptatWithDictionaryOutput"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_Analysis_160427\\AKIProjectSchema.xml"
        // );

        /*
         * AKI Project IAA Analysis
         */
        // File acceptableConceptsFile = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\AcceptableConcepts_160606.txt"
        // );
        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\IAA_Analysis_161117\\IAACorpus"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\IAA_Analysis_161117\\NancyXMLUncertainToPositiveAssertion"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\IAA_Analysis_161117\\LizXMLUncertainToPositiveAssertion"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_Analysis_160427\\AKIProjectSchema.xml"
        // );
        // File acceptableConceptsFile = new File(
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\AcceptableConcepts_160606.txt"
        // );

        /*
         * AMI MT Training IAA Evaluation
         */
        // File textFileDirectory = new File(
        // "D:\\AMI_RO1_Collaboration\\Annotation\\GlennAMI_NLPDevelopment\\AnnotatorTrainignMTAnnotationsRound3_4\\Txt"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\AMI_RO1_Collaboration\\Annotation\\GlennAMI_NLPDevelopment\\AnnotatorTrainignMTAnnotationsRound3_4\\HillardXML"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\AMI_RO1_Collaboration\\Annotation\\GlennAMI_NLPDevelopment\\AnnotatorTrainignMTAnnotationsRound3_4\\GentryXML"
        // );
        // File schemaConceptFile = new File(
        // "D:\\AMI_RO1_Collaboration\\Annotation\\GlennAMI_NLPDevelopment\\AnnotatorTrainignMTAnnotationsRound3_4\\projectschema.xml"
        // );
        // File acceptableConceptsFile = null;
        // AnnotationApp annotationApp = AnnotationApp.EHOST;

        // /*
        // * CirrhosisRadiology IAA
        // */
        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\IAA\\Corpus"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\IAA\\Set01XML"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\IAA\\Set02XML"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\projectschema.xml"
        // );
        // File acceptableConceptsFile = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\AcceptedConcepts.txt"
        // );

        /*
         * CirrhosisRadiology Raptat Performance
         */
        // File textFileDirectory = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\RaptatScoreTestingSets\\TestingCorpusSmall"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\RaptatScoreTestingSets\\RaptatXMLSmallSet"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\RaptatScoreTestingSets\\TestingXmlSmallSet"
        // );
        // File schemaConceptFile = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\projectschema.xml"
        // );
        // File acceptableConceptsFile = new File(
        // "D:\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\AcceptedConcepts.txt"
        // );

        /*
         * MHA Ruth vs Elliot vs Raptat Scoring of Elliot Training data
         */
        // File textFileDirectory = new File(
        // "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\TXT_ElliotTrainingCorpus"
        // );
        // File raptatFileDirectory = new File(
        // "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\RaptatXML_ElliotTrainingCorpus"
        // );
        // File referenceFileDirectory = new File(
        // "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\RuthXML_ElliotTrainingCorpus"
        // );
        // File schemaConceptFile = new File(
        // "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\projectschema_170427.xml"
        // );
        // File acceptableConceptsFile = new File(
        // "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\AcceptedConcepts_170427.txt"
        // );
        File textDirectory = new File(parameters.textDirectoryPath);
        File raptatDirectory = new File(parameters.raptatDirectoryPath);
        File referenceDirectory = new File(parameters.referenceDirectoryPath);
        File schemaFile = new File(parameters.schemaFilePath);
        File conceptsFile = null;
        if (parameters.conceptsFilePath != null && !parameters.conceptsFilePath.isEmpty()) {
          conceptsFile = new File(parameters.conceptsFilePath);
        }

        Hashtable<PerformanceScoreObject, int[]> resultScores =
            compareFileSets(textDirectory, referenceDirectory, raptatDirectory, schemaFile,
                conceptsFile, parameters.annotationApp, parameters.removeUnmatched);

        BootstrapResultWriter resultWriter =
            new BootstrapResultWriter(textDirectory.getAbsolutePath(), "TestData",
                RaptatConstants.CROSS_VALIDATION_CONCEPT_RESULT_TITLES);
        resultWriter.writeResults(resultScores);

        System.exit(0);
      }
    });
  }


  private static AnnotationGroup createAnnotationGroup(final List<SchemaConcept> schemaConcepts,
      final HashSet<String> acceptedConcepts, final TextAnalyzer textAnalyzer,
      final PhraseTokenIntegrator tokenIntegrator, final AnnotationImporter annotationImporter,
      final File referenceFile, final File raptatFile, final File textFile) {
    /*
     * Check to be that files correspond (both reference and raptat files should refer to text file)
     */
    String textFileName = textFile.getName();
    if (!referenceFile.getName().contains(textFileName)
        || !raptatFile.getName().contains(textFileName)) {
      GeneralHelper.errorWriter("Unmatched text file");
      System.exit(-1);
    }

    String referencePath = referenceFile.getAbsolutePath();
    String raptatPath = raptatFile.getAbsolutePath();
    String textPath = textFile.getAbsolutePath();

    RaptatDocument textDocument = textAnalyzer.processDocument(textPath);
    List<AnnotatedPhrase> referenceAnnotations = annotationImporter.importAnnotations(referencePath,
        textPath, acceptedConcepts, schemaConcepts);
    List<AnnotatedPhrase> raptatAnnotations = annotationImporter.importAnnotations(raptatPath,
        textPath, acceptedConcepts, schemaConcepts);
    tokenIntegrator.addProcessedTokensToAnnotations(referenceAnnotations, textDocument, false, 7);
    tokenIntegrator.addProcessedTokensToAnnotations(raptatAnnotations, textDocument, false, 7);

    AnnotationGroup annotationGroup = new AnnotationGroup(textDocument, referenceAnnotations);
    annotationGroup.setRaptatAnnotations(raptatAnnotations);
    return annotationGroup;
  }


  private static List<AnnotationGroup> createAnnotationGroups(final File textFileDirectory,
      final File referenceFileDirectory, final File raptatFileDirectory,
      final List<SchemaConcept> schemaConcepts, final HashSet<String> acceptedConcepts,
      final AnnotationApp annotationApp, final boolean removeUnmatched) {
    TextAnalyzer textAnalyzer = new TextAnalyzer();
    textAnalyzer.setGenerateTokenPhrases(false);
    PhraseTokenIntegrator tokenIntegrator = new PhraseTokenIntegrator();

    List<File> referenceFiles = FileUtils.getFiles(referenceFileDirectory);
    List<File> raptatFiles = FileUtils.getFiles(raptatFileDirectory);
    List<File> textFiles = FileUtils.getFiles(textFileDirectory);

    Collections.sort(referenceFiles);
    Collections.sort(raptatFiles);
    Collections.sort(textFiles);

    /*
     * Assume files are in such an order that they correspond to one another and process lists.
     */
    Iterator<File> referenceFileIterator = referenceFiles.iterator();
    Iterator<File> raptatFileIterator = raptatFiles.iterator();
    Iterator<File> textIterator = textFiles.iterator();

    List<AnnotationGroup> annotationGroups = new ArrayList<>(textFiles.size());

    AnnotationImporter annotationImporter;
    if (annotationApp == null) {
      annotationImporter = AnnotationImporter.getImporter();
    } else {
      annotationImporter = new AnnotationImporter(annotationApp);
    }
    annotationImporter.setRemoveUnmatched(removeUnmatched);

    while (referenceFileIterator.hasNext() && raptatFileIterator.hasNext()
        && textIterator.hasNext()) {
      File referenceFile = referenceFileIterator.next();
      File raptatFile = raptatFileIterator.next();
      File textFile = textIterator.next();

      AnnotationGroup annotationGroup = createAnnotationGroup(schemaConcepts, acceptedConcepts,
          textAnalyzer, tokenIntegrator, annotationImporter, referenceFile, raptatFile, textFile);

      annotationGroups.add(annotationGroup);
    }
    return annotationGroups;
  }


  /*
   * Create map to store annotatedPhrase attributes mapped to their values for determining whether
   * those attribute values have been matched by those in another phrase
   */
  private static Map<String, Set<String>> createAttributeToValueMap(
      final AnnotatedPhrase annotatedPhrase) {

    Map<String, Set<String>> attributeValueMap = new HashMap<>();
    for (RaptatAttribute attribute : annotatedPhrase.getPhraseAttributes()) {
      attributeValueMap.put(attribute.getName(), new HashSet<>(attribute.getValues()));
    }

    return attributeValueMap;
  }
}
