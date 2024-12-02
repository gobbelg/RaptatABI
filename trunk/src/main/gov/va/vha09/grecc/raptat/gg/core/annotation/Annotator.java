/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ProbabilisticTSFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.TokenSequenceFinderBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.CrfSuiteRunner;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.tagging.CSAttributeTagger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate.PhraseIDCrossValidationAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AttributeAnalyzerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AttributeConflictManagerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.TokenSequenceFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing.AnnotationGroupPostProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AssertionConflictManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeConflictManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.AssertionAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.AttributeSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextAttributeAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextAttributeWriter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ReasonNoMedsAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.SentenceContextAttributeAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

/** @author Glenn T. Gobbel Feb 17, 2016 */
public class Annotator {
  protected static final Logger LOGGER = Logger.getLogger(Annotator.class);
  protected static final StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();


  protected TextAnalyzer textAnalyzer = new TextAnalyzer();


  protected TokenSequenceFinder tsFinder;

  protected List<CSAttributeTagger> csAttributeTaggers = new ArrayList<>();

  /*
   * This is typically used to sift through and only keep annotations that have the "proper"
   * context, where "proper" depends on the use case. It can be left set to null to nullify use
   * unless needed for a particular use case. It maps concepts to the sifter that will be used to
   * determine if the concept that a phrase maps to is kept after sifting.
   */
  protected ContextSifter contextSifter = null;

  /*
   * This is used by the context sifter or attribute sifter (?) as one of the "requiredContexts"
   * that *may* be (depending on the use case) required for a phrase to be annotated and returned by
   * the annotate() method.
   */
  protected final List<String> defaultContexts = Arrays.asList("reason not on meds");

  /*
   * The attributeSifter takes a list of annotated phrase instances and removes those that do not
   * have the proper attribute or attribute value.
   */
  protected final AttributeSifter attributeSifter = null;

  /*
   * The contextAttributeWriter instance stores a list of ContextAttributeAnalyzers, which this
   * Annotator instance uses to determine the context of a phrase and what attribute to assign to
   * the phrase based on context.
   */
  protected ContextAttributeWriter contextAttributeWriter = new ContextAttributeWriter();

  protected final HashMap<String, AttributeConflictManager> attributeConflictManagers =
      new HashMap<>();

  protected Collection<AnnotationGroupPostProcessor> postProcessors = new ArrayList<>();

  protected HashSet<String> conceptsToAnnotate = null;

  /**
   * @param raptatSolution
   * @param dictionaryPath
   * @param acceptedConcepts
   * @param probabilisticThreshold
   */
  public Annotator(RaptatAnnotationSolution raptatSolution, HashSet<String> acceptedConcepts,
      double probabilisticThreshold) {
    this();
    this.tsFinder = TokenSequenceFinderBuilder.createTSFinder(
        raptatSolution.getTokenSequenceFinderSolution(), acceptedConcepts, probabilisticThreshold);
    Map<ContextType, AttributeTaggerSolution> models =
        raptatSolution.getAllAttributeTaggerSolutions();
    for (AttributeTaggerSolution curSolution : models.values()) {
      CrfSuiteRunner curRunner = new CrfSuiteRunner();
      this.csAttributeTaggers.add(new CSAttributeTagger(curSolution, curRunner));
    }
    this.textAnalyzer.setTokenProcessingParameters(raptatSolution.getTokenProcessingOptions());
    this.textAnalyzer.setFacilitatorBlockerDictionary(raptatSolution.getDictionary());
    this.textAnalyzer.addSentenceAndSectionMatchers(raptatSolution.getSentenceAndSectionMatchers());

    initializeAttributeAnalyzers(raptatSolution.getAttributeAnalyzerDescriptors());
    initializeConflictManagers(raptatSolution.getAttributeConflictManagerDescriptors());

    this.contextSifter = raptatSolution.getContextSifter();
  }

  /**
   * @param raptatSolution
   * @param dictionaryPath
   * @param acceptedConcepts
   * @param probabilisticThreshold
   */
  public Annotator(RaptatAnnotationSolution raptatSolution, String dictionaryPath,
      HashSet<String> acceptedConcepts, double probabilisticThreshold) {
    this();
    this.tsFinder = TokenSequenceFinderBuilder.createTSFinder(
        raptatSolution.getTokenSequenceFinderSolution(), acceptedConcepts, probabilisticThreshold);
    Map<ContextType, AttributeTaggerSolution> models =
        raptatSolution.getAllAttributeTaggerSolutions();
    for (AttributeTaggerSolution curSolution : models.values()) {
      CrfSuiteRunner curRunner = new CrfSuiteRunner();
      this.csAttributeTaggers.add(new CSAttributeTagger(curSolution, curRunner));
    }
    this.textAnalyzer.setTokenProcessingParameters(raptatSolution.getTokenProcessingOptions());

    TokenContextAnalyzer dictionary = raptatSolution.getDictionary();
    this.textAnalyzer.setFacilitatorBlockerDictionary(dictionary);

    if (dictionaryPath != null && !dictionaryPath.isEmpty()) {
      if (dictionary == null) {
        this.textAnalyzer.createDictionary(dictionaryPath, acceptedConcepts);
      } else {
        int response = JOptionPane.showConfirmDialog(null,
            "Solution file already contains dictionary.\nReplace it with dictionary at "
                + dictionaryPath,
            "Replace Existing Dictionary", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
          this.textAnalyzer.createDictionary(dictionaryPath, acceptedConcepts);
        }
      }
    }
    initializeAttributeAnalyzers(raptatSolution.getAttributeAnalyzerDescriptors());
    initializeConflictManagers(raptatSolution.getAttributeConflictManagerDescriptors());
  }


  /**
   * Constructor used only for cross-validation such as leave-one-out where the same textAnalyzer
   * instance and facilitator-blocker dictionary are used multiple times but with different
   * solutions.
   *
   * @param textAnalyzer
   * @param attributeTaggerSolutions
   */
  public Annotator(TextAnalyzer textAnalyzer,
      Collection<AttributeTaggerSolution> attributeTaggerSolutions) {
    this();
    this.csAttributeTaggers = new ArrayList<>(attributeTaggerSolutions.size());
    for (AttributeTaggerSolution attributeTaggerSolution : attributeTaggerSolutions) {
      CrfSuiteRunner crfRunner = new CrfSuiteRunner();
      this.csAttributeTaggers.add(new CSAttributeTagger(attributeTaggerSolution, crfRunner));
    }

    this.textAnalyzer = textAnalyzer;
  }


  Annotator() {
    Annotator.LOGGER.setLevel(Level.INFO);
  }


  public void addAttributeConflictManager(AttributeConflictManager manager) {
    if (manager != null) {
      this.attributeConflictManagers.put(manager.attributeName.toLowerCase(), manager);
    }
  }


  public void addAttributeConflictManagers(List<AttributeConflictManager> managers) {
    if (managers != null) {
      for (AttributeConflictManager manager : managers) {
        this.attributeConflictManagers.put(manager.attributeName.toLowerCase(), manager);
      }
    }
  }


  public void addAttributes(List<AnnotatedPhrase> proposedAnnotations) {
    /*
     * First add attributes that are determined by Bayesian probabilistic models. Assignment of
     * attributes is conditioned on the token sequence and the assigned concept.
     */
    this.tsFinder.setMappedAttributes(proposedAnnotations);

    /*
     * Now add attributes that are determined by "context" as determined by a
     * ContextAttributeAnalyzer instance. Instances of this type are typically ReasonNoMedsAnalyzer
     * instances or AssertionAnalyzer instances, which determine context based on rules and
     * surrounding phrases within dictionaries of phrases, like the rules implemented by Negex. They
     * can also be SentenceContextAttributeAnalyzer instances that determine the attribute based on
     * the "context" assigned to the sentence containing a phrase.
     *
     * Unlike attributes set using the setMappedAttributes() and
     * CSAttributeTagger-assignCSAttributes() methods, attributes set using the
     * setContextualAttributes are generally rule-based.
     */
    setContextualAttributes(proposedAnnotations);

    /*
     * Now assign attributes based on cue and scope CRF-based models
     */
    for (CSAttributeTagger curTagger : this.csAttributeTaggers) {
      curTagger.assignCSAttributes(proposedAnnotations);
    }

    if (!this.attributeConflictManagers.isEmpty()) {
      reconcileAttributes(proposedAnnotations);
    }
  }


  public void addContextAttributeAnalyzer(ContextAttributeAnalyzer analyzer) {
    this.contextAttributeWriter.addAnalyzer(analyzer);
  }


  public void addPostProcessor(AnnotationGroupPostProcessor postProcessor) {
    this.postProcessors.add(postProcessor);
  }


  /**
   * This produces annotations of the "active" sentences in the document. (Note that within an
   * AnnotationGroup instance). Before this method is called, the "active" sentence field should be
   * populated, generally by either the "base" or "filtered" sentences.
   *
   * @param annotationGroup
   */
  public void annotate(AnnotationGroup annotationGroup) {
    RaptatDocument raptatDocument = annotationGroup.getRaptatDocument();

    /* Return here : 171110 */
    /*
     * The tsFinder object identifies token sequences that should be annotated and the concepts
     * those sequences map to. Depending on the subtype of TokenSequenceFinder of the tsFinder
     * object, either Probabilistic/NaiveBayes or CRF based solutions can be used.
     */
    List<AnnotatedPhrase> proposedAnnotations = this.tsFinder.identifySequences(raptatDocument);

    /*
     * This adds dictionary annotations to the proposed annotations for each dictionary annotation
     * that does not overlap one of the proposed annotations or has precedence over proposed
     * annotations. That is, annotations proposed by the call to identifySequences() take precedence
     * over dictionary annotations unless otherwise specified (stored as a field in the tokens of a
     * dictionary annotation).
     */
    boolean clearPreviousAttributes = true;
    List<AnnotatedPhrase> dictionaryAnnotations =
        raptatDocument.getDictionaryAnnotations(clearPreviousAttributes);

    /*
     * Remove annotations that have forbidden phraseContext or that lack required context. This was
     * put in place originally to deal with "reason no meds" context. Changed so it can handle
     * concepts that provide context to other concepts and determine if they should be annotated.
     *
     * Note that we sift before merging proposed and dictionary as we want to use both of these
     * annotation lists in the sifting process. This allows us to mark tokens and token sequences
     * that are used in sifting before they are removed due to overlap with RapTAT-generated
     * annotations.
     */
    if (this.contextSifter != null) {
      this.contextSifter.siftUsingContext(proposedAnnotations);
      this.contextSifter.siftUsingContext(dictionaryAnnotations);
    }

    /*
     * This the method call that will filter out probabilistic, proposed annotations that overlap
     * precedent dictionary annotations, and then filter out non-precedent dictionary annotations
     * that overlap the remaining proposed annotations
     */
    proposedAnnotations =
        mergeDictionaryAndProposedAnnotations(dictionaryAnnotations, proposedAnnotations);

    annotationGroup.raptatAnnotations = proposedAnnotations;

    for (AnnotationGroupPostProcessor postProcessor : this.postProcessors) {
      postProcessor.postProcess(annotationGroup);
    }

    /*
     * Add attributes to the annotations remaining after post-processing
     */
    addAttributes(annotationGroup.raptatAnnotations);

    /*
     * Now filter the phrases based on the attributes assigned in the previous block. This can be
     * used to remove AnnotatedPhrase instances that do not have a required attribute, such as
     * "overt" reason for not being on meds, or that have a disallowed attribute.
     */
    if (this.attributeSifter != null) {
      this.attributeSifter.sift(annotationGroup.raptatAnnotations);
    }

    /*
     * Only annotations that are assigned concepts with the acceptableConcepts set should be
     * returned at the end of this call. We keep them until the end of the call because they may be
     * used earlier in the method for filtering or post-processing.
     */
    if (this.conceptsToAnnotate != null) {
      ListIterator<AnnotatedPhrase> annotationIterator =
          annotationGroup.raptatAnnotations.listIterator();

      while (annotationIterator.hasNext()) {
        if (!this.conceptsToAnnotate.contains(annotationIterator.next().getConceptName())) {
          annotationIterator.remove();
        }
      }
    }
  }


  /*
   * Before this method is called, the "active" sentence field should be populated, generally by
   * either the "base" or "filtered" sentences.
   */
  public void annotate(List<AnnotationGroup> annotationGroups) {
    for (AnnotationGroup annotationGroup : annotationGroups) {
      this.annotate(annotationGroup);
    }
  }


  /**
   * @param pathToTextFile
   * @return
   */
  public AnnotationGroup annotate(String pathToTextFile) {
    RaptatDocument raptatDocument = this.textAnalyzer.processDocument(pathToTextFile);
    raptatDocument.activateBaseSentences();
    AnnotationGroup annotationGroup = new AnnotationGroup(raptatDocument, null);
    this.annotate(annotationGroup);

    return annotationGroup;
  }


  public List<AnnotatedPhrase> createProposedAnnotation(RaptatDocument raptatDocument) {
    List<AnnotatedPhrase> proposedAnnotations = this.tsFinder.identifySequences(raptatDocument);
    return proposedAnnotations;
  }


  public void generateReasonNoMedsContextSifters() {
    GeneralHelper.errorWriter("No longer implemented - examine code");
    System.exit(-1);
  }


  public HashSet<String> getConceptsToAnnotate() {
    return this.conceptsToAnnotate;
  }


  public void mergeDictionaryAnnotations(List<AnnotatedPhrase> proposedAnnotations,
      RaptatDocument raptatDocument) {
    mergeDictionaryAndProposedAnnotations(raptatDocument.getDictionaryAnnotations(true),
        proposedAnnotations);
  }


  public void setConceptsToAnnotate(HashSet<String> acceptableConcepts) {
    this.conceptsToAnnotate = acceptableConcepts;
  }


  public void setContextSifter(ContextSifter contextSifter) {
    this.contextSifter = contextSifter;
  }


  public void setPostProcessors(Collection<AnnotationGroupPostProcessor> postProcessors) {
    if (postProcessors == null) {
      postProcessors = new ArrayList<>();
    }
    this.postProcessors = postProcessors;
  }


  /** @param d */
  public void setSequenceThreshold(double threshold) throws UnsupportedOperationException {
    if (this.tsFinder instanceof ProbabilisticTSFinder) {
      ((ProbabilisticTSFinder) this.tsFinder).setSequenceThreshold(threshold);
    } else {
      throw new UnsupportedOperationException(
          "Setting of thresholds for phrase identification is only supported for the "
              + "ProbabilisticSequenceIDEvaluator subtypes of SequenceIDEvaluator "
              + "subtypes of ProbabilisticTSFinder types");
    }
  }


  /**
   * Method typically used with a cross-validation process when we want to use solutions trained
   * with various settings such as thresholds or token processing options
   *
   * @param tsFinderSolution
   * @param acceptedConcepts
   * @param probabilisticThreshold
   */
  public void setTokenSequenceFinderSolution(TokenSequenceFinderSolution tsFinderSolution,
      HashSet<String> acceptedConcepts, double probabilisticThreshold) {
    this.tsFinder = TokenSequenceFinderBuilder.createTSFinder(tsFinderSolution, acceptedConcepts,
        probabilisticThreshold);

    this.textAnalyzer
        .setTokenProcessingParameters(tsFinderSolution.retrieveTokenProcessingOptions());
  }


  private void clearSentencesForUnusedAnnotations(List<AnnotatedPhrase> unusedAnnotationList,
      HashSet<AnnotatedPhrase> confirmedAnnotationSet) {
    for (AnnotatedPhrase annotatedPhrase : unusedAnnotationList) {
      if (!confirmedAnnotationSet.contains(annotatedPhrase)) {
        annotatedPhrase.getAssociatedSentence().removeSentenceAssociatedConcepts();
      }
    }
  }


  /**
   * Identifies any conflicts in the attributes associated with an annotatedPhrase, which can occur
   * if attributes were added based on multiple methods, such as probabilistic mapping, context
   * rules, or Cue-Scope models. If a conflict exists, the method will return a map from attribute
   * name to all attributes associated with that name, whether a conflict exists or not for that
   * particular attribute. Otherwise, it will return an empty map.
   *
   * @param phrase
   * @return
   */
  private HashMap<String, Set<RaptatAttribute>> identifyAttributeConflicts(
      AnnotatedPhrase annotatedPhrase) {
    HashMap<String, Set<RaptatAttribute>> resultMap = new HashMap<>();

    boolean conflictFound = false;
    for (RaptatAttribute attribute : annotatedPhrase.getPhraseAttributes()) {
      String attributeName = attribute.getName();
      Set<RaptatAttribute> foundAttributes;
      if ((foundAttributes = resultMap.get(attributeName)) != null) {
        conflictFound = true;
      } else {
        foundAttributes = new HashSet<>();
        resultMap.put(attributeName, foundAttributes);
      }
      foundAttributes.add(attribute);
    }

    if (!conflictFound) {
      resultMap.clear();
    }

    return resultMap;
  }


  /**
   * @param analyzers
   */
  private void initializeAttributeAnalyzers(
      HashMap<AttributeAnalyzerType, List<String[]>> analyzers) {
    if (analyzers != null && !analyzers.isEmpty()) {
      List<ContextAttributeAnalyzer> analyzerList = new ArrayList<>();
      for (AttributeAnalyzerType analyzerType : analyzers.keySet()) {
        if (analyzerType.equals(AttributeAnalyzerType.ASSERTION)) {
          analyzerList.add(new AssertionAnalyzer());
        } else if (analyzerType.equals(AttributeAnalyzerType.GENERAL_CONTEXT)) {
          List<String[]> analyzerValues = analyzers.get(analyzerType);
          for (String[] constructorVals : analyzerValues) {
            ContextAttributeAnalyzer curAnalyzer = new SentenceContextAttributeAnalyzer(
                constructorVals[0], constructorVals[1], constructorVals[2], constructorVals[3],
                Boolean.parseBoolean(constructorVals[4]));
            analyzerList.add(curAnalyzer);
          }
        } else if (analyzerType.equals(AttributeAnalyzerType.REASON_NO_MEDS)) {
          analyzerList.add(new ReasonNoMedsAnalyzer());
        }
      }
      this.contextAttributeWriter = new ContextAttributeWriter(analyzerList);
    }
  }


  // @formatter:off
  /* The next 5 methods are created by dividing the annotate
  method in order to modularize the UIMA analysis
  Sanjib Saha - April 10, 2016 */

  /**
	 * @param attributeConflictManagerDescriptors
	 */
	private void initializeConflictManagers(Collection<String> descriptors)
	{
		for (String descriptor : descriptors)
		{
			if ( descriptor.equalsIgnoreCase( AttributeConflictManagerType.ASSERTION.displayName ) )
			{
				AttributeConflictManager manager = new AssertionConflictManager();
				this.attributeConflictManagers.put( manager.attributeName.toLowerCase(), manager );
			}
		}
	}

  /**
	 * Merge dictionary annotations that have not already been found by RapTAT to
	 * the list of proposed annotations.
	 *
	 * @param dictionaryAnnotations
	 * @param proposedAnnotations
	 */
	private List<AnnotatedPhrase> mergeDictionaryAndProposedAnnotations(List<AnnotatedPhrase> dictionaryAnnotations,
			List<AnnotatedPhrase> proposedAnnotations)
	{
		List<AnnotatedPhrase> resultAnnotations = new ArrayList<>(
				proposedAnnotations.size() + dictionaryAnnotations.size() );

		/*
		 * First go through proposedAnnotations and make sure each one does not overlap
		 * a "precedent" dictionaryAnnotation
		 */
		for (AnnotatedPhrase annotatedPhrase : proposedAnnotations)
		{
			List<RaptatToken> annotationTokenSequence = annotatedPhrase.getProcessedTokens();
			if ( RaptatToken.containsDictionaryPrecedentToken( annotationTokenSequence ) )
			{
				RaptatToken.markRaptatConceptAssigned( annotationTokenSequence, false );
			}
			else
			{
				resultAnnotations.add( annotatedPhrase );
			}
		}

		/*
		 * Now go through the dictionaryAnnotations list and only add those that do not
		 * overlap an existing annotation in the resultAnnotations list
		 */
		dictionaryAnnotationsLoop: for (AnnotatedPhrase annotatedPhrase : dictionaryAnnotations)
		{
			/*
			 * Dictionary annotations can be assigned to a null concept for the purpose of
			 * blocking annotations by the RapTAT probabilistic method. These annotations
			 * are not included in the final output
			 */
			if ( !annotatedPhrase.getConceptName().toLowerCase().startsWith( RaptatConstants.NULL_ELEMENT.toLowerCase() ) )
			{
				List<RaptatToken> annotationTokenSequence = annotatedPhrase.getProcessedTokens();
				for (RaptatToken curToken : annotationTokenSequence)
				{
					if ( curToken.raptatConceptAssigned() )
					{
						continue dictionaryAnnotationsLoop;
					}
				}

				resultAnnotations.add( annotatedPhrase );
			}
			else
			{
				System.out.println( "Null concept found" );
			}
		}

		HashSet<AnnotatedPhrase> resultSet = new HashSet<>( resultAnnotations );
		clearSentencesForUnusedAnnotations( dictionaryAnnotations, resultSet );
		clearSentencesForUnusedAnnotations( proposedAnnotations, resultSet );
		AnnotatedPhrase.linkAnnotationsToSentences( resultSet );

		return resultAnnotations;
	}

  /**
	 * @param annotations
	 */
	private void reconcileAttributes(List<AnnotatedPhrase> annotations)
	{
		for (AnnotatedPhrase phrase : annotations)
		{
			HashMap<String, Set<RaptatAttribute>> conflictedAttributes;
			conflictedAttributes = identifyAttributeConflicts( phrase );
			if ( !conflictedAttributes.isEmpty() )
			{
				resolveAttributeConflicts( phrase, conflictedAttributes );
			}
		}
	}

  /**
   * @param annotatedPhrase
   * @param conflictedAttributeMap
   */
  private void resolveAttributeConflicts(
      AnnotatedPhrase annotatedPhrase,
      HashMap<String, Set<RaptatAttribute>> conflictedAttributeMap) {
    List<RaptatAttribute> newAttributes = new ArrayList<>(conflictedAttributeMap.size());

    for (String attributeName : conflictedAttributeMap.keySet()) {
      Set<RaptatAttribute> attributeSet = conflictedAttributeMap.get(attributeName);

      /*
       * If attribute size is greater than 1, there is a conflict between
       * 2 (or more) attributes
       */
      if (attributeSet.size() > 1) {
        AttributeConflictManager conflictManager;
        if ((conflictManager = this.attributeConflictManagers.get(attributeName)) != null) {
          RaptatAttribute resolvedAttribute =
              conflictManager.manageConflicts(attributeSet, annotatedPhrase);
          newAttributes.add(resolvedAttribute);
        }
        /*
         * Default, if there is not an assigned conflictManager, is just
         * to combine values in attributes
         */
        else {
          RaptatAttribute combinedAttribute =
              RaptatAttribute.combineSet(attributeName, attributeSet);
          newAttributes.add(combinedAttribute);
        }
      } else {
        newAttributes.addAll(attributeSet);
      }
    }
    annotatedPhrase.setPhraseAttributes(newAttributes);
  }

  private void setContextualAttributes(List<AnnotatedPhrase> annotations) {
    for (AnnotatedPhrase annotatedPhrase : annotations) {
      List<RaptatToken> curTokenSequence = annotatedPhrase.getProcessedTokens();
      List<RaptatAttribute> contextAttributes =
          this.contextAttributeWriter.getSequenceAttributes(curTokenSequence);
      List<RaptatAttribute> phraseAttributes = annotatedPhrase.getPhraseAttributes();
      phraseAttributes.addAll(contextAttributes);
    }
  }

  /**
	 * @param phrasesAndConcepts
	 * @param invertTokenSequence
	 * @param raptatConceptAssigned
	 * @return The sequences annotated with concepts, returned as a list of
	 *         annotated phrases. *
	 * @author Glenn Gobbel - Jun 16, 2012
	 */
	public static List<AnnotatedPhrase> convertToAnnotations(
			List<RaptatPair<List<RaptatToken>, RaptatPair<String, Boolean>>> phrasesAndConcepts,
			boolean invertTokenSequence, boolean raptatConceptAssigned, boolean dictionaryAssigned)
	{
		String annotationData[] = new String[6];
		List<AnnotatedPhrase> theResults = new ArrayList<>( phrasesAndConcepts.size() );

		if ( invertTokenSequence )
		{
			for (RaptatPair<List<RaptatToken>, RaptatPair<String, Boolean>> curPhrase : phrasesAndConcepts)
			{
				List<RaptatToken> curTokenSequence = curPhrase.left;
				RaptatPair<String, Boolean> conceptNameAndOverride = curPhrase.right;

				RaptatToken.markRaptatConceptAssigned( curTokenSequence, raptatConceptAssigned );
				RaptatToken.markDictionaryConceptAssignedAndPrecedent( curTokenSequence, conceptNameAndOverride.right );
				Collections.sort( curTokenSequence, Annotator.tokenSorter );
				annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = UniqueIDGenerator.INSTANCE.getUnique();
				annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] = curTokenSequence.get( 0 ).getStartOffset();
				annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] = curTokenSequence
						.get( curTokenSequence.size() - 1 ).getEndOffset();
				annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = conceptNameAndOverride.left;
				AnnotatedPhrase annotatedPhrase = new AnnotatedPhrase( annotationData, curTokenSequence );
				annotatedPhrase.setDictionaryBasedPhrase( dictionaryAssigned );
				theResults.add( annotatedPhrase );
			}
		}
		else
		{
			for (RaptatPair<List<RaptatToken>, RaptatPair<String, Boolean>> curPhrase : phrasesAndConcepts)
			{
				List<RaptatToken> curTokenSequence = curPhrase.left;
				RaptatPair<String, Boolean> conceptNameAndOverride = curPhrase.right;

				RaptatToken.markRaptatConceptAssigned( curTokenSequence, raptatConceptAssigned );
				RaptatToken.markDictionaryConceptAssignedAndPrecedent( curTokenSequence, conceptNameAndOverride.right );
				annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = UniqueIDGenerator.INSTANCE.getUnique();
				annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] = curTokenSequence.get( 0 ).getStartOffset();
				annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] = curTokenSequence
						.get( curTokenSequence.size() - 1 ).getEndOffset();
				annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = conceptNameAndOverride.left;
				AnnotatedPhrase annotatedPhrase = new AnnotatedPhrase( annotationData, curTokenSequence );
				annotatedPhrase.setDictionaryBasedPhrase( dictionaryAssigned );
				theResults.add( annotatedPhrase );
			}
		}
		return theResults;
	}

  public static void main(String[] args)
	{
		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				AnnotatorParser parser = new AnnotatorParser();
				AnnotatorParameterObject parameters = parser.parseCommandLine( args );

				HashSet<String> includedConcepts = new HashSet<>();
				if ( parameters.conceptsFilePath != null && !parameters.conceptsFilePath.isEmpty() )
				{
					includedConcepts = PhraseIDCrossValidationAnalyzer.getConcepts( parameters.conceptsFilePath );
				}

				Annotator.LOGGER.info( "Loading trained RapTAT annotation solution" );
				RaptatAnnotationSolution theSolution = RaptatAnnotationSolution
						.loadFromFile( new File( parameters.solutionFilePath ) );
				Annotator.LOGGER.info( "Solution loading complete" );

				Annotator annotator = new Annotator_Revised( theSolution, includedConcepts,
						RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT );

				Iterator<File> fileDirectoryIterator = getTextDirectoryIterator( parameters.textDirectoryPath,
						parameters.isBatchDirectory );

				while (fileDirectoryIterator.hasNext())
				{
					File textDirectory = fileDirectoryIterator.next();
					String[] textFilesList = this.getTextFiles( textDirectory );
					Annotator.LOGGER.info( "Processing directory:" + textDirectory.getName() );

					File exportDirectory = getNextExportDirectory( parameters.exportDirectoryPath, textDirectory );
					XMLExporterRevised exporter = new XMLExporterRevised( parameters.annotationApp, exportDirectory,
							true );

					int docNumber = 1;
					int corpusSize = textFilesList.length;
					for (String curTextPath : textFilesList)
					{
						System.out.println( "Annotating document " + docNumber++ + " of " + corpusSize );
						AnnotationGroup curAnnotationGroup = annotator.annotate( curTextPath );
						if ( curAnnotationGroup.raptatAnnotations != null )
						{
							boolean correctOffsets = true;
							boolean insertPhraseStrings = true;
							exporter.exportRaptatAnnotationGroup( curAnnotationGroup, correctOffsets,
									insertPhraseStrings );
						}
						else
						{
							System.out.println( "Only null annotations for export" );
						}
					}
				}
			}


			private File getNextExportDirectory(String exportDirectoryPath, File textDirectory)
			{
				String exportDirectoryName = exportDirectoryPath + File.separator + textDirectory.getName()
						+ "_XMLOutput";
				File exportDirectory = new File( exportDirectoryName );
				try
				{
					FileUtils.forceMkdir( exportDirectory );
				}
				catch (IOException e)
				{
					System.err.println( "Unable to make directory for writing results - " + e.getLocalizedMessage() );
					e.printStackTrace();
					System.exit( -1 );
				}

				return exportDirectory;
			}


			private Iterator<File> getTextDirectoryIterator(String textDirectoryPath, boolean isBatchDirectory)
			{
				List<File> fileList = new ArrayList<>();
				if ( !isBatchDirectory )
				{
					fileList.add( new File( textDirectoryPath ) );
				}
				else
				{
					BufferedReader reader = null;
					try
					{
						InputStream filePathStream = new FileInputStream( new File( textDirectoryPath ) );
						reader = new BufferedReader( new InputStreamReader( filePathStream ) );

						String line;

						while (( line = reader.readLine() ) != null)
						{
							if ( !( line == null ) && !line.isEmpty() )
							{
								fileList.add( new File( line ) );
							}
						}
					}
					catch (IOException e)
					{
						System.err.println( "Unable to read " + textDirectoryPath + e.getLocalizedMessage() );
						e.printStackTrace();
						System.exit( -1 );
					}
					finally
					{
						if ( reader != null )
						{
							try
							{
								reader.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
				}

				return fileList.iterator();
			}


			private String[] getTextFiles(File textFileDirectory)
			{

				Collection<File> files = FileUtils.listFiles( textFileDirectory, new String[]
				{
						"txt"
				}, false );
				String[] filePaths = new String[files.size()];

				int i = 0;
				for (File curFile : files)
				{
					filePaths[i++ ] = curFile.getAbsolutePath();
				}

				return filePaths;
			}


			private String[] getTextFiles(String textFileDirectoryPath)
			{

				return this.getTextFiles( new File( textFileDirectoryPath ) );
			}
		} );
	}

  /* End of 5 methods for UIMA-based analysis */
  // @formatter:on

}
