package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.FeatureBuilderRunner;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.TokenPhraseMaker;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.MunkresAssignmentTest;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableData;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.DocumentRecord;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.InstancesBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters.AttributeFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.FeatureBuildersAndArffFileCreation;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.general.BaseFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.general.TargetedConceptFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities.TokenPhraseMakerGG;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.DocumentColumn;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.weka.entities.ArffCreationParameter;
import src.main.gov.va.vha09.grecc.raptat.gg.weka.entities.ArffCreationParameterExtended;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Essentially reproduces the FeatureBuilderAndArffCreator class to allow for
 * creating "metafeatures," which are features built from other features. Many
 * methods are recreated here because they are static in the superclass and
 * cannot be overridden.
 *
 * @author gtony
 */
public class MetaFeatureBuilderAndArffCreator
		extends FeatureBuildersAndArffFileCreation {

	{
		LOGGER.setLevel(Level.INFO);
	}
	/*
	 * The candidateConceptFeatureBuilder finds, given a phrase that maps to the
	 * target concept or label, the basic features of the target label based on
	 * surrounding RaptatTokenPhrase objects based on their distance in rows,
	 * columns, document distance from the target label.
	 */
	private BaseFeatureBuilder candidatePhraseFeatureBuilder = new TargetedConceptFeatureBuilder();

	private MetaFeatureBuilder[] metaFeatureBuilders;
	private List<AttributeFilter> attributeFilters = new ArrayList<>();

	private FileSystem inMemoryFileSystem = Jimfs
			.newFileSystem(Configuration.forCurrentPlatform());
	private Path inMemoryFilePath = this.inMemoryFileSystem
			.getPath(TEMP_DOCUMENT_PATH_STRING);
	/*
	 * Create a log file to determine whether the documents processed via the
	 * processUpdated()method contained any RaptatTokenPhrase instances;
	 */
	private Optional<String> logTokenPhrasesDirectory = Optional.empty();

	private final static String TEMP_DOCUMENT_PATH_STRING = "C:\\tempDocument.txt";

	private static final Logger LOGGER = Logger
			.getLogger(MetaFeatureBuilderAndArffCreator.class);

	/**
	 * @param arffCreationParameter
	 */
	public MetaFeatureBuilderAndArffCreator(
			final ArffCreationParameterExtended arffCreationParameter) {
		super(arffCreationParameter);
	}

	public MetaFeatureBuilderAndArffCreator(
			final ArffCreationParameterExtended arffCreationParameter,
			final MetaFeatureBuilder[] metaFeatureBuilders,
			final Optional<List<AttributeFilter>> attributeFilters) {
		this(arffCreationParameter);
		this.metaFeatureBuilders = metaFeatureBuilders;
		this.attributeFilters = attributeFilters
				.orElse(Collections.emptyList());
	}

	public List<RaptatTokenPhrase> buildCandidatePhrasesAndFeatures(
			String targetConceptName, DocumentRecord documentRecord) {

		String documentId = documentRecord.fileName();
		String documentText = "";
		try {
			documentText = new String(documentRecord.documentTextAsByteArray(),
					"UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Unable to convert byte array to String object");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		LOGGER.info("Processing document:" + documentId);
		TextAnalyzer textAnalyzer = ((ArffCreationParameterExtended) getParameter()).textAnalyzer;
		final RaptatDocument raptatDocument = textAnalyzer
				.processText(documentText, Optional.ofNullable(documentId));
		final String documentName = raptatDocument.getTextSource()
				.orElse("Unnamed Document");

		final TokenPhraseMaker tokenPhraseMaker = textAnalyzer
				.getTokenPhraseMaker();
		Map<String, Set<String>> categoryToSubcategoryMap = ((TokenPhraseMakerGG) tokenPhraseMaker)
				.getCategoryToSubcategoryMap();
		/*
		 * Copy the mapping as we may modify it when we search for features
		 */
		Map<String, Set<String>> catToSubcatMapCopy = new HashMap<>();
		for (Entry<String, Set<String>> entry : categoryToSubcategoryMap
				.entrySet()) {
			catToSubcatMapCopy.put(entry.getKey(),
					new HashSet<>(entry.getValue()));
		}

		/*
		 * Add features to the TokenPhrase instances within the document
		 */
		Label targetedConceptLabel = new Label(targetConceptName.toLowerCase());
		List<RaptatTokenPhrase> targetConceptTokenPhrases = RaptatTokenPhrase
				.getCandidateTokenPhrases(raptatDocument, targetedConceptLabel);
		this.candidatePhraseFeatureBuilder.addTokenPhraseFeatures(
				targetConceptTokenPhrases, targetedConceptLabel,
				catToSubcatMapCopy);
		buildMetafeatures(targetConceptTokenPhrases, targetConceptName);

		return targetConceptTokenPhrases;
	}

	/**
	 * This is the key method for this class and encompasses the building of
	 * features for all the phrase classes specified in the 'parameter' field
	 */
	@Override
	public Map<String, Map<PhraseClass, Instances>> buildMappedInstances() {

		/*
		 * The 'parameter' variable stores the information needed to create an
		 * arff file such as the directory containing the corpus, a map of
		 * PhraseClass instances containing the strings representing the
		 * conceptName instances we want to train to assign attribute values to.
		 * In the case of an index value, attribute values might be left, right,
		 * or bilateral to assign laterality, or they might be abi or tbi to
		 * assign index value type.
		 */
		final ArffCreationParameterExtended parameter = (ArffCreationParameterExtended) getParameter();

		/*
		 * Any phrase classes with the same conceptName will be merged into one
		 * class during cross-validation. Unique conceptNames will not be
		 * merged. Note that PhraseClass instances with the same conceptName can
		 * use the same features to train a classifier to classify among
		 * different sets of attribute values, so those features only need to be
		 * generated once.
		 *
		 * An example of this would be to classify among left, right, and
		 * bilateral, and to classify among abi and tbi. Because both are
		 * targeting the concept index value, which should be virtually the same
		 * phrases in both sets, we can use the same features as the features
		 * for a given phrase with the concept index value should be the same.
		 * What will change for a given index value is whether the laterality is
		 * left, right, bilateral, or nothing and whether the index type is abi,
		 * tbi, or nothing.
		 *
		 * We create this map so that we can connect a phrase class with its
		 * attribute values to an xml file that contains the reference attribute
		 * values (generally provided as 'concepts' not attribute values because
		 * in eHOST xml attributes provide greater specification to concepts).
		 */
		final Map<PhraseClass, Optional<String>> phraseClassToXmlDirectoryMap = parameter
				.getPhraseClassToXmlDirectoryMap();

		/*
		 * We may want to map conceptName (or targetLabel) to a list of
		 * phraseClasses if multiple phraseClasses come from a single concept,
		 * such as when attributes, like laterality and index type, are assigned
		 * to the same concept, index value. When this is the case, the
		 * phraseClasses can use the same features of putative concepts (i.e.
		 * putative index values) found in the text. The only difference is the
		 * classes the putative concepts are assigned to. This is done to reduce
		 * unnecessary document processing and feature generation.
		 *
		 * We generate a mapping of concept to the phrase classes for which the
		 * mapped concept is the "target" concept and store it in
		 * conceptNameToPhraseClassesMap.
		 */
		Map<String, Set<PhraseClass>> conceptNameToPhraseClassesMap = new HashMap<>();
		for (PhraseClass phraseClass : phraseClassToXmlDirectoryMap.keySet()) {
			String conceptForPhraseClass = phraseClass.getConceptName()
					.toLowerCase();
			Set<PhraseClass> mappedClasses = conceptNameToPhraseClassesMap
					.computeIfAbsent(conceptForPhraseClass,
							k -> new HashSet<>());
			mappedClasses.add(phraseClass);
		}

		final TokenPhraseMaker tokenPhraseMaker = parameter.textAnalyzer
				.getTokenPhraseMaker();
		Map<String, Set<String>> categoryToSubcategoryMap = ((TokenPhraseMakerGG) tokenPhraseMaker)
				.getCategoryToSubcategoryMap();

		/*
		 * Get the Weka instances corresponding to each PhraseClass type in the
		 * phraseClassToXmlMap
		 */
		Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassToInstancesMap = getConceptToPhraseClassToInstancesMap(
				phraseClassToXmlDirectoryMap, conceptNameToPhraseClassesMap,
				parameter.textAnalyzer, categoryToSubcategoryMap);
		return conceptToPhraseClassToInstancesMap;
	}

	@Override
	public void close() {
		try {
			this.inMemoryFileSystem.close();
		}
		catch (IOException e) {
			System.err.println(
					"Unable to close in memory file system upon closing\nMetaFeatureBuilderAndArffCreator iinstance");
			e.printStackTrace();
		}
	}

	/* Commented out by Glenn on 4/12/23 as no longer needed */
	// /**
	// * This is being replaced by processUpdated() method which will handle
	// merging of
	// classifications
	// * with the same name and allow for mapping of multiple classes.
	// Classifications that have the
	// * same concept names of their PhraseClass instances will be merged if
	// they map to the same
	// * phrase. Phrase class instances with distinct names will not be merged.
	// *
	// */
	// @SuppressWarnings("unused")
	// @Override
	// @Deprecated
	// public ModuleResultBase process() {
	// ArffCreationResults results = null;
	//
	// final TreeMap<Integer, DocumentMetaData> lineNumberToPhraseMap = new
	// TreeMap<>();
	//
	// final FnPostArffFileCompleted arffCompletedFunction =
	// new PostArffFileCompleted(lineNumberToPhraseMap);
	//
	// final ArffCreationParameterExtended parameter =
	// (ArffCreationParameterExtended) getParameter();
	//
	// final String fileDescription = parameter.getFileDescription();
	// final String projectOrigin = parameter.getProjectOrigin();
	//
	// /*
	// * Any phrase classes with the same conceptName will be merged into one
	// class during
	// * cross-validation.
	// */
	// final Map<PhraseClass, Optional<String>> phraseClassToXmlMap =
	// parameter.getPhraseClassToXmlDirectoryMap();
	// Set<String> conceptNames = new HashSet<>();
	// phraseClassToXmlMap.keySet()
	// .forEach(phraseClass -> conceptNames.add(phraseClass.getConceptName()));
	//
	// final TextAnalyzer textAnalyzer = parameter.textAnalyzer;
	// Map<String, Set<String>> categoryToSubcategoryMap =
	// ((TokenPhraseMakerGG)
	// textAnalyzer.getTokenPhraseMaker()).getCategoryToSubcategoryMap();
	//
	// /*
	// * Copy the mapping as we may modify it when we search for features
	// */
	// Map<String, Set<String>> catToSubcatMapCopy = new HashMap<>();
	// for (Entry<String, Set<String>> entry :
	// categoryToSubcategoryMap.entrySet()) {
	// catToSubcatMapCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
	// }
	//
	// String arffFilePath = null;
	//
	// /*
	// * Use just the first phrase class since we are modifying the code
	// */
	// PhraseClass phraseClass = phraseClassToXmlMap.keySet().toArray(new
	// PhraseClass[] {})[0];
	// try (ArffFileWriter writer = new ArffFileWriter(projectOrigin,
	// fileDescription, phraseClass,
	// parameter.getOutputDirectory().get())) {
	//
	// arffFilePath = writer.getArffFilePath();
	//
	// int current_file_count = 0;
	//
	// final TextDataIterator documents = getTextDataIterator();
	// while (documents.hasNext()) {
	// DocumentRecord documentRecord = documents.next();
	// final String documentPath = document.getPath();
	// final RaptatDocument raptatDocument =
	// textAnalyzer.processDocument(documentPath);
	//
	// this.candidateConceptFeatureBuilder.addTokenPhraseAttributes(
	// raptatDocument.getTokenPhrases(),
	// new Label(phraseClassToXmlMap.toString().toLowerCase()),
	// catToSubcatMapCopy);
	// String targetedLabelName = null;
	// buildAllAttributes(raptatDocument, targetedLabelName);
	//
	// final String xmlDocumentPath = getXmlDocumentPath(
	// phraseClassToXmlMap.get(phraseClass).get(),
	// FilenameUtils.getName(document.getName()));
	//
	// writer.exportPhraseFeatures(raptatDocument, document.getPath(),
	// xmlDocumentPath);
	//
	// for (final RaptatTokenPhrase phrase : raptatDocument.getTokenPhrases()) {
	// if (isTargetConcept(phrase, phraseClass)) {
	// lineNumberToPhraseMap.put(lineNumberToPhraseMap.size(),
	// new DocumentMetaData(phrase, documentPath, xmlDocumentPath));
	// }
	// }
	//
	// LOGGER.info(MessageFormat.format("\n\t{0} of {1} FILES PR0CESSED",
	// ++current_file_count,
	// documents.length));
	// }
	//
	// } catch (final Exception e) {
	// System.err.println(e.getMessage());
	// results = null;
	// }
	//
	// PostArffFileResults postArffWriteResults;
	// try {
	// /** 6/18/19 - DAX - For now this is written at the end */
	// postArffWriteResults = arffCompletedFunction.writeArffFile(arffFilePath);
	// results = new ArffCreationResults(postArffWriteResults, arffFilePath);
	// } catch (final IOException e) {
	// getLogger().severe(e.getMessage());
	// results = null;
	// }
	//
	// return results;
	// }

	/**
	 * @param raptatTokenPhrases
	 * @param annotatedPhrases
	 */
	public int[][] getAdjacencyMatrix(
			final List<RaptatTokenPhrase> raptatTokenPhrases,
			final List<AnnotatedPhrase> annotatedPhrases) {
		final int[][] adjacencyMatrix = new int[raptatTokenPhrases
				.size()][annotatedPhrases.size()];

		int i = 0;
		for (final RaptatTokenPhrase tokenPhrase : raptatTokenPhrases) {
			int j = 0;
			for (final AnnotatedPhrase annotatedPhrase : annotatedPhrases) {
				/*
				 * This could probably be sped up to more rapidly find
				 * overlapping phrases
				 */
				adjacencyMatrix[i][j] = AnnotationToTokenPhraseComparator
						.getSimilarityScore(tokenPhrase, annotatedPhrase);
				j++;
			}
			i++;
		}

		return adjacencyMatrix;
	}

	public void setAttributeFilters(
			final Collection<AttributeFilter> attributeFilters) {
		this.attributeFilters = new ArrayList<>(attributeFilters);
	}

	/**
	 * @param logTokenPhrases
	 *            the logTokenPhrases to set
	 */
	public void setLogTokenPhrases(Optional<String> logTokenPhrases) {
		this.logTokenPhrasesDirectory = logTokenPhrases;
	}

	/**
	 * Builds the column features.
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 * @param runDirection
	 *            the run direction
	 */
	private void build_column_features(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders,
			final RunDirection runDirection) {
		final FeatureBuilderRunner runner = new FeatureBuilderRunner(
				featureBuilders);
		final List<DocumentColumn> listOfColumnsOfPhrases = document
				.getDocumentColumns();
		for (final DocumentColumn documentColumn : listOfColumnsOfPhrases) {
			runner.process_column(documentColumn.columnPhrases, runDirection,
					document.getProcessedTokens());
		}
	}

	/**
	 * Builds the features for columns, rows, and whole document
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 */
	private void build_features(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders) {

		build_column_features(document, featureBuilders, RunDirection.Forward);
		build_column_features(document, featureBuilders, RunDirection.Reverse);
		build_row_features(document, featureBuilders, RunDirection.Forward);
		build_row_features(document, featureBuilders, RunDirection.Reverse);
		build_features_for_document(document, featureBuilders,
				RunDirection.Forward);
		build_features_for_document(document, featureBuilders,
				RunDirection.Reverse);
		for (final RaptatTokenPhrase tokenPhrase : document.getTokenPhrases()) {
			final Set<String> lowerCasedFeatures = new HashSet<>();
			for (final String feature : tokenPhrase.getPhraseFeatures()) {
				lowerCasedFeatures.add(feature.toLowerCase());
			}
			tokenPhrase.resetPhraseFeatures(lowerCasedFeatures);
		}
	}

	/**
	 * Builds the features for all phrases in the document, left-to-right,
	 * top-to-bottom
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 */
	private void build_features_for_document(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders,
			final RunDirection direction) {
		final FeatureBuilderRunner runner = new FeatureBuilderRunner(
				featureBuilders);
		runner.process_document(document.getTokenPhrases(), direction,
				document.getProcessedTokens());
	}

	/**
	 * Builds the row features.
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 * @param runDirection
	 *            the run direction
	 */
	private void build_row_features(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders,
			final RunDirection runDirection) {
		final FeatureBuilderRunner runner = new FeatureBuilderRunner(
				featureBuilders);
		final List<TextDocumentLine> textDocumentLines = document
				.getTextDocumentLines();
		for (final TextDocumentLine line : textDocumentLines) {
			runner.process_row(line.getTokenPhraseList(), runDirection,
					document.getProcessedTokens());
		}
	}

	private void buildMetafeatures(
			Collection<RaptatTokenPhrase> candidatePhrases,
			final String targetLabelName) {
		for (final MetaFeatureBuilder metaFeatureBuilder : this.metaFeatureBuilders) {
			metaFeatureBuilder.buildFeatures(candidatePhrases);
		}
	}

	/**
	 * Calculate total number of candidate phrases that might be mapped to the
	 * targeted concepts. This is equivalent to the maximum number of times a
	 * given attribute (aka "feature") might occur in a given corpus of
	 * documents provided by documentToTargetConceptToTokenPhrasesMap. This can
	 * be used to determine a frequency of each attribute occurring in the
	 * corpus (i.e., number of times attribute occurs/total number of candidate
	 * phrases).
	 *
	 * @param documentToTargetConceptToTokenPhrasesMap
	 * @return
	 */
	private HashMap<String, Integer> calculateTotalTokenPhrasesPerTargetConcept(
			final Map<String, Map<String, List<RaptatTokenPhrase>>> documentToTargetConceptToTokenPhrasesMap) {
		/*
		 * Calculate total number of candidate phrases so we can determine where
		 * to have a cut-off regarding filtering regard attributes that have low
		 * frequency. This code might be better incorporated into the
		 * filterAttributeMappins
		 */
		HashMap<String, Integer> targetConceptToTotalTokenPhrasesMap = new HashMap<>();
		for (String documentName : documentToTargetConceptToTokenPhrasesMap
				.keySet()) {
			Map<String, List<RaptatTokenPhrase>> targetConceptToTokenPhraseMap = documentToTargetConceptToTokenPhrasesMap
					.get(documentName);
			for (String targetConceptName : targetConceptToTokenPhraseMap
					.keySet()) {
				Integer phrasesInDocument = targetConceptToTokenPhraseMap
						.get(targetConceptName).size();
				targetConceptToTotalTokenPhrasesMap.compute(targetConceptName,
						(key, value) -> value == null ? phrasesInDocument
								: value + phrasesInDocument);
			}
		}
		return targetConceptToTotalTokenPhrasesMap;
	}

	/**
	 * Convert token phrases that have been mapped to a class into Instance
	 * objects for use by Weka.
	 *
	 * @param phraseToClassMap
	 * @param textSource
	 * @param modelSpecificAttributeNames
	 * @param modelSpecificAttributes
	 * @return
	 */
	private ArrayList<Attribute> convertToAttributes(
			final Map<RaptatTokenPhrase, String> phraseToClassMap,
			final String textSource,
			final Map<String, Attribute> modelSpecificAttributes) {

		for (RaptatTokenPhrase tokenPhrase : phraseToClassMap.keySet()) {

			for (String phraseFeature : tokenPhrase.getPhraseFeatures()) {
				Attribute mappedAttrbute = modelSpecificAttributes
						.computeIfAbsent(phraseFeature,
								key -> new Attribute(key,
										RaptatConstants.BINARY_FEATURE_VALUES));
			}
		}
		return null;
	}

	/**
	 * @param conceptNameToPhraseClassesMap
	 * @param conceptToAttributeNameToAttributeMap
	 * @return
	 */
	private Map<String, Map<PhraseClass, Instances>> createConceptToPhraseClassToInstancesMap(
			final Map<String, Set<PhraseClass>> conceptNameToPhraseClassesMap,
			final Map<String, Map<String, Attribute>> conceptToAttributeNameToAttributeMap) {

		Map<String, Map<PhraseClass, Instances>> resultMap = new HashMap<>();
		InstancesBuilder instanceBuilder = new InstancesBuilder();
		for (String conceptName : conceptNameToPhraseClassesMap.keySet()) {

			Map<String, Attribute> attributeNameToAttributeMap = conceptToAttributeNameToAttributeMap
					.get(conceptName);
			if ( (attributeNameToAttributeMap != null)
					&& !attributeNameToAttributeMap.isEmpty() ) {
				Collection<Attribute> attributes = conceptToAttributeNameToAttributeMap
						.get(conceptName).values();
				if ( (attributes != null) && !attributes.isEmpty() ) {

					List<Attribute> attributeList = new ArrayList<>(attributes);
					Map<PhraseClass, Instances> phraseClassToInstancesObjectMap = resultMap
							.computeIfAbsent(conceptName,
									key -> new HashMap<>());

					for (PhraseClass phraseClass : conceptNameToPhraseClassesMap
							.get(conceptName)) {
						boolean addCommonInstances = true;
						Instances instances = instanceBuilder
								.buildInstancesObjectFromAttributes(
										attributeList, phraseClass,
										addCommonInstances);

						phraseClassToInstancesObjectMap.put(phraseClass,
								instances);
					}
				}
			}
		}
		return resultMap;
	}

	/**
	 * Filter out attributes based on the filters associated with this object (a
	 * MetaFeatureBuilderAndArffCreator) based on the
	 * conceptToFeatureNameToAttributeMap and the
	 * conceptToFeatureToFrequencyMap.
	 *
	 * @param targetConceptToFeatureNameToAttributeMap
	 * @param targetConceptToFeatureToFrequencyMap
	 * @param targetConceptToTotalTokenPhrasesMap
	 */
	private void filterAttributeMappings(
			final Map<String, Map<String, Attribute>> targetConceptToFeatureNameToAttributeMap,
			final Map<String, Map<String, Integer>> targetConceptToFeatureToFrequencyMap,
			final HashMap<String, Integer> targetConceptToTotalTokenPhrasesMap) {

		for (String conceptName : targetConceptToFeatureNameToAttributeMap
				.keySet()) {

			Integer totalTokenPhrases = targetConceptToTotalTokenPhrasesMap
					.get(conceptName);
			Map<String, Integer> featureToFrequencyMap = targetConceptToFeatureToFrequencyMap
					.get(conceptName);
			for (AttributeFilter attributeFilter : this.attributeFilters) {
				attributeFilter.initialize(new RaptatPair<>(
						featureToFrequencyMap, totalTokenPhrases));
			}

			Map<String, Attribute> featureNameToAttributeMap = targetConceptToFeatureNameToAttributeMap
					.get(conceptName);
			for (AttributeFilter attributeFilter : this.attributeFilters) {
				List<String> featureNames = new ArrayList<>(
						featureNameToAttributeMap.keySet());
				for (String featureName : featureNames) {
					Attribute attribute = featureNameToAttributeMap
							.get(featureName);
					if ( !attributeFilter.acceptAttribute(attribute) ) {
						featureNameToAttributeMap.remove(featureName);
					}
				}
			}
		}
	}

	// /**
	// * Takes a list of phrases and returns those that have a phraseLabel
	// * instance that is equivalent to to a Label with the name and value both
	// * equal to conceptPhraseName
	// *
	// * @param documentPhrases
	// * @return
	// */
	// private List<RaptatTokenPhrase> getCandidatePhrases(
	// final List<RaptatTokenPhrase> documentPhrases,
	// final String conceptPhraseName) {
	// final List<RaptatTokenPhrase> identifiedPhrases = new ArrayList<>();
	// final Label conceptNameLabel = new Label(
	// conceptPhraseName.toLowerCase());
	// for (final RaptatTokenPhrase phrase : documentPhrases) {
	// if ( phrase.getPhraseLabels().contains(conceptNameLabel) ) {
	// identifiedPhrases.add(phrase);
	// }
	// }
	// return identifiedPhrases;
	// }

	/**
	 * Get a map indicating the Weka instances corresponding to each candidate
	 * concept name and PhraseClass type in the targetConceptToPhraseClassMap
	 * map and phraseClassToXmlMap
	 *
	 * @param phraseClassToXmlDirectoryMap
	 * @param conceptNameToPhraseClassesMap
	 * @param textAnalyzer
	 * @param catToSubcatMapCopy
	 * @return
	 */
	private Map<String, Map<PhraseClass, Instances>> getConceptToPhraseClassToInstancesMap(
			final Map<PhraseClass, Optional<String>> phraseClassToXmlDirectoryMap,
			final Map<String, Set<PhraseClass>> conceptNameToPhraseClassesMap,
			final TextAnalyzer textAnalyzer,
			final Map<String, Set<String>> categoryToSubcategoryMap) {

		/*
		 * candidateConceptToAttributeNameToAttributeMap maps all the concept
		 * names to a mapping of all the feature names to their corresponding
		 * attributes of each feature name for that concept. A given concept
		 * should have the same feature names and corresponding Weka attributes.
		 * Note that 'Attribute' is a Weka construct to encapsulate feature
		 * names and how the feature is described (e.g. quantitative vs nominal
		 * vs binary).
		 */
		Map<String, Map<String, Attribute>> candidateConceptToAttributeNameToAttributeMap = new HashMap<>();

		/*
		 * documentToConceptToPhrasesMap will map each document to a concept
		 * within the document. We'll use this to create Weka instance objects
		 * once all the attributes of the concept are found over all documents.
		 * Note that for a given document and concept, the RaptatTokenPhrase
		 * objects should be the same
		 *
		 * TODO: Provide concrete example of document and concept. Does concept
		 * mean "category" as in the category to subcategory map? If it's actual
		 * concept, how are these associated with RaptatTokenPhrase objects?
		 * Presumably this allow for multiple concepts in a given document, like
		 * indexValue and compressibility, that may rely on a different set of
		 * RaptatTokenPhrase objects from the document. Presumably, the concept
		 * maps to all phrases in the document that are relevant to the concept.
		 */
		Map<String, Map<String, List<RaptatTokenPhrase>>> documentToCandidateConceptToTokenPhrasesMap = new HashMap<>();

		/*
		 * Create a candidateConceptToAttributeToFrequencyMap that will map, for
		 * each concept, how often each attribute occurs (i.e. its frequency).
		 * We go ahead and add the "mapped to map" so we don't have to keep
		 * checking for it in other methods called by this one. This will be
		 * used later for filtering attributes. It would probably be better to
		 * have this included as a lambda or series of lambdas rather than put
		 * them associated with a specific filter as we do here.
		 */
		Map<String, Map<String, Integer>> candidateConceptToAttributeNameAndFrequencyMap = new HashMap<>();
		conceptNameToPhraseClassesMap.forEach(
				(key, value) -> candidateConceptToAttributeNameAndFrequencyMap
						.put(key, new HashMap<String, Integer>(2048)));

		/*
		 * Populate the candidateConceptToAttributeNameToAttributeMap, the
		 * documentToCandidateConceptToTokenPhrasesMap, and the
		 * candidateConceptToAttributeNameAndFrequencyMap. This is where most of
		 * the work happens building all the attributes in the corpus. Note that
		 * 'Attribute' is a Weka construct to encapsulate attribute names and
		 * how the attribute is described (e.g. quantitative vs nominal vs
		 * binary). Attribute is synonymous with 'feature' in other machine
		 * learning systems.
		 */
		Set<String> targetedTokenPhrases = conceptNameToPhraseClassesMap
				.keySet();
		populateMappingsForAttributeBuilding(
				candidateConceptToAttributeNameToAttributeMap,
				documentToCandidateConceptToTokenPhrasesMap,
				candidateConceptToAttributeNameAndFrequencyMap,
				targetedTokenPhrases, categoryToSubcategoryMap, textAnalyzer);

		if ( LOGGER.isDebugEnabled() ) {
			String loggingTag = "FEATURES BEFORE FILTERING";
			logFeatureToAttributeMap(
					candidateConceptToAttributeNameToAttributeMap, loggingTag);
		}

		HashMap<String, Integer> targetConceptToTotalTokenPhrasesMap = calculateTotalTokenPhrasesPerTargetConcept(
				documentToCandidateConceptToTokenPhrasesMap);

		/*
		 * Filter out attributes based on the filters associated with this
		 * object (a MetaFeatureBuilderAndArffCreator) based on the
		 * conceptToFeatureNameToAttributeMap and the
		 * conceptToFeatureToFrequencyMap.
		 */
		filterAttributeMappings(candidateConceptToAttributeNameToAttributeMap,
				candidateConceptToAttributeNameAndFrequencyMap,
				targetConceptToTotalTokenPhrasesMap);

		if ( LOGGER.isDebugEnabled() ) {
			String loggingTag = "FEATURES AFTER FILTERING";
			logFeatureToAttributeMap(
					candidateConceptToAttributeNameToAttributeMap, loggingTag);
		}

		/*
		 * Create the map we'll be return as a result. This will create the
		 * Instances objects mapped to by the PhraseClass objects, but the
		 * Instance (singular) objects will be added later with the
		 * updateInstances method.
		 */
		Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassToInstancesMap = createConceptToPhraseClassToInstancesMap(
				conceptNameToPhraseClassesMap,
				candidateConceptToAttributeNameToAttributeMap);

		AnnotationApp annotationApp = AnnotationApp.EHOST;
		AnnotationImporter annotationImporter = new AnnotationImporter(
				annotationApp);

		Iterator<DocumentRecord> documentIterator = getTextDataIterator()
				.iterator();
		InstancesBuilder instanceBuilder = new InstancesBuilder();
		while ( documentIterator.hasNext() ) {

			DocumentRecord documentRecord = documentIterator.next();
			String documentId = documentRecord.fileName();

			/*
			 * Store documentText data in in memory file system to be compatible
			 * with existing code
			 */
			try {
				Files.write(this.inMemoryFilePath,
						documentRecord.documentTextAsByteArray());
			}
			catch (IOException e) {
				System.err.println("Unable to write " + documentId
						+ " file to memory for processing");
				e.printStackTrace();
			}

			System.out.println("Updating instances for document " + documentId);

			Map<String, List<RaptatTokenPhrase>> conceptToTokenPhrasesMap = documentToCandidateConceptToTokenPhrasesMap
					.get(documentId);

			for (String conceptName : conceptNameToPhraseClassesMap.keySet()) {

				Map<String, Attribute> attributeNameToAttributeMap = candidateConceptToAttributeNameToAttributeMap
						.get(conceptName);
				List<RaptatTokenPhrase> raptatTokenPhrases = conceptToTokenPhrasesMap
						.get(conceptName);

				/*
				 * Loop through the phraseClass instances for the current
				 * document and conceptName. In the case of ABI values, examples
				 * of phraseClass instance values would be
				 * 'Index_Value_Laterality' and 'Index_Value_Type'
				 */
				for (PhraseClass phraseClass : conceptNameToPhraseClassesMap
						.get(conceptName)) {

					/*
					 * We use optionalAnnotatedPhrases when we are assigning a
					 * class and not testing whether it is assigned correctly.
					 * The xml path provides the path to the reference
					 * annotations for testing, so if that path is not present,
					 * the annotatedPhrases are not present.
					 */
					Optional<List<AnnotatedPhrase>> annotatedPhrases = Optional
							.empty();
					Optional<String> optionalXmlDirectoryPath = phraseClassToXmlDirectoryMap
							.get(phraseClass);
					if ( optionalXmlDirectoryPath.isPresent() ) {

						boolean conformXmlOffsetsToText = true;
						String xmlPathString = optionalXmlDirectoryPath.get()
								+ File.separator + documentId
								+ ".knowtator.xml";
						annotatedPhrases = Optional
								.of(annotationImporter.importAnnotations(
										xmlPathString, this.inMemoryFilePath,
										phraseClass.getClassifications(),
										conformXmlOffsetsToText));
					}

					/*
					 * The tokenPhraseToClassMap contains the mappings of all
					 * tokenPhrases for the current conceptName and phraseClass
					 * to their classification (e.g. left, right, unassigned for
					 * the concept indexValue an phraseClass
					 * Index_Value_Laterality).
					 */
					Map<RaptatTokenPhrase, Optional<String>> tokenPhraseToClassMap = getPhraseToAssignedClassMap(
							raptatTokenPhrases, annotatedPhrases);
					Instances instances = conceptToPhraseClassToInstancesMap
							.get(conceptName).get(phraseClass);
					instanceBuilder.updateInstancesObject(instances, documentId,
							attributeNameToAttributeMap, tokenPhraseToClassMap);

				}
			}
		}
		return conceptToPhraseClassToInstancesMap;
	}

	private PrintWriter getLoggingPrintWriter(String outputDirectoryPathString)
			throws URISyntaxException, IOException {
		Optional<PrintWriter> optionalPrintWriter;
		String pwFileName = "DocumentTokenPhrases_"
				+ GeneralHelper.getTimeStamp() + ".txt";
		File outputDirectory = new File(outputDirectoryPathString);
		if ( !outputDirectory.exists() ) {
			FileUtils.forceMkdir(outputDirectory);
		}
		String outputFilePathString = outputDirectory + File.separator
				+ pwFileName;
		System.out.println(
				"Logging document token phrases to " + outputFilePathString);
		PrintWriter pw = new PrintWriter(new File(outputFilePathString));
		pw.println(
				"Document\tContainsPutativeIndexValue\tPhraseLabel\tPhraseText\tStartOffset\tEndOffset");
		pw.flush();
		return pw;
	}

	/**
	 * Create a map that maps each candidatePhrase to its corresponding class as
	 * determined based on matching them to the annotatedPhrases supplied as a
	 * parameter. Any phrases that are candidates and have no matching annotated
	 * phrase will be assigned to the string representation of
	 * PhraseClass.NO_CLASS_ASSIGNED.
	 *
	 * @param candidatePhrases
	 * @param annotatedPhrases
	 * @return
	 */
	private Map<RaptatTokenPhrase, Optional<String>> getPhraseToAssignedClassMap(
			final List<RaptatTokenPhrase> candidatePhrases,
			final Optional<List<AnnotatedPhrase>> optionalPhrases) {

		final Map<RaptatTokenPhrase, Optional<String>> resultMap = new HashMap<>(
				candidatePhrases.size());

		if ( !optionalPhrases.isPresent() ) {
			candidatePhrases.forEach(raptatTokenPhrase -> resultMap
					.put(raptatTokenPhrase, Optional.empty()));
			return resultMap;
		}

		List<AnnotatedPhrase> annotatedPhrases = optionalPhrases.get();
		int[] matchingArray = matchCandidatePhrasesToAnnotations(
				candidatePhrases, annotatedPhrases);

		int i = 0;
		for (final RaptatTokenPhrase tokenPhrase : candidatePhrases) {

			final String assignedClassName = matchingArray[i] == -1
					? PhraseClass.NO_CLASS_ASSIGNED.getConceptName()
					: annotatedPhrases.get(matchingArray[i]).getConceptName();
			i = i + 1;
			resultMap.put(tokenPhrase, Optional.of(assignedClassName));

		}
		return resultMap;
	}

	/**
	 * Find the proper (targeted) phrases for building metafeatures. We want
	 * only phrases that have a Label name equal to the name of the label being
	 * targeted, which is equivalent to the phraseClass name (via toString) of
	 * the ArffCreationParameter parameter field of this object. * *
	 *
	 * @param raptatDocument
	 * @param targetLabelName
	 * @return
	 */
	private List<RaptatTokenPhrase> getTargetedPhrases(
			final RaptatDocument raptatDocument, final String targetLabelName) {
		final RaptatTokenPhrase.Label labelBeingTargeted = new RaptatTokenPhrase.Label(
				targetLabelName.toLowerCase());
		final List<RaptatTokenPhrase> targetedPhrases = new ArrayList<>();
		for (final RaptatTokenPhrase candidatePhrase : raptatDocument
				.getTokenPhrases()) {
			if ( candidatePhrase.containsLabelName(labelBeingTargeted) ) {
				targetedPhrases.add(candidatePhrase);
			}
		}
		return targetedPhrases;
	}

	private final IterableData getTextDataIterator() {

		return ((ArffCreationParameter) getParameter()).getIterableTextData();

	}

	// /**
	// * Gets the ehost xml document name.
	// *
	// * @param xmlDirectoryPath
	// * the ehost xml document path
	// * @param fileName
	// * the file name
	// * @return the ehost xml document name
	// */
	// private String getXmlDocumentPath(final String xmlDirectoryPath,
	// final String fileName) {
	// return xmlDirectoryPath + "\\" + fileName + ".knowtator.xml";
	// }
	//
	// private boolean isTargetConcept(final RaptatTokenPhrase phrase,
	// final PhraseClass phraseClass) {
	// final boolean isTargetConcept = phrase.getPhraseLabels()
	// .contains(new RaptatTokenPhrase.Label(
	// phraseClass.getConceptName().toLowerCase()));
	// return isTargetConcept;
	// }

	/**
	 * ABI specific method to log whether document contains one or more
	 * "indexvalue" labeled RaptatTokenPhrase instances
	 *
	 * @param pw
	 * @param raptatDocument
	 */
	private void logDocumentPhrases(final PrintWriter pw,
			final RaptatDocument raptatDocument) {
		String textSource = raptatDocument.getTextSource()
				.orElse("Unnamed Document");
		List<RaptatTokenPhrase> tokenPhrases = raptatDocument.getTokenPhrases();
		if ( (tokenPhrases == null) || tokenPhrases.isEmpty() ) {
			pw.println(textSource + "\tFALSE");
			pw.flush();
			return;
		}
		for (RaptatTokenPhrase tokenPhrase : raptatDocument.getTokenPhrases()) {
			for (Label label : tokenPhrase.getPhraseLabels()) {
				if ( label.getCategory().equalsIgnoreCase("indexValue") ) {
					pw.println(textSource + "\tTRUE");
					pw.flush();
					return;
				}
			}
		}
		pw.println(textSource + "\tFALSE");
		pw.flush();
		return;
	}

	private void logFeatureToAttributeMap(
			final Map<String, Map<String, Attribute>> conceptToFeatureNameToAttributeMap,
			final String loggingTag) {
		String spacer = "-----------------------";
		System.out.println(spacer);
		System.out.println(loggingTag);
		System.out.println(spacer);
		for (String concept : conceptToFeatureNameToAttributeMap.keySet()) {
			System.out.println("CONCEPT: " + concept);
			Map<String, Attribute> featureToAttributeMap = conceptToFeatureNameToAttributeMap
					.get(concept);
			for (String feature : featureToAttributeMap.keySet()) {
				System.out.println(feature);
			}
		}

	}

	/**
	 * @param conceptToFeatureToFrequencyMap
	 * @param loggingTag
	 */
	private void logFeatureToFrequencyMap(
			final Map<String, Map<String, Integer>> conceptToFeatureToFrequencyMap,
			final String loggingTag) {
		String spacer = "-----------------------";
		System.out.println(spacer);
		System.out.println(loggingTag);
		System.out.println(spacer);
		for (String concept : conceptToFeatureToFrequencyMap.keySet()) {
			System.out.println("CONCEPT: " + concept);
			Map<String, Integer> featureToFrequencyMap = conceptToFeatureToFrequencyMap
					.get(concept);
			for (Entry<String, Integer> featureAndFrequency : featureToFrequencyMap
					.entrySet()) {
				System.out.println(featureAndFrequency.getValue() + " : "
						+ featureAndFrequency.getKey());
			}
		}
	}

	/**
	 * @param candidatePhrases
	 * @param annotatedPhrases
	 * @return
	 */
	private int[] matchCandidatePhrasesToAnnotations(
			final List<RaptatTokenPhrase> candidatePhrases,
			final List<AnnotatedPhrase> annotatedPhrases) {
		/*
		 * The adjacency matrix relating candidate to annotatedPhrases
		 */
		final int[][] adjacencyMatrix = getAdjacencyMatrix(candidatePhrases,
				annotatedPhrases);

		int[] candidateMatching = new int[0];
		if ( adjacencyMatrix.length > 0 ) {
			final MunkresAssignmentTest mr = new MunkresAssignmentTest(
					adjacencyMatrix);
			candidateMatching = mr.solve();
		}

		/*
		 * Remove any annotated phrases that do not overlap. This is a kludge.
		 * The MunkresAssignmentTest solve() method should really handle this by
		 * setting non-matching to -1. Why it does not always do this is
		 * unclear.
		 */
		int tokenPhraseIndex = 0;
		for (RaptatTokenPhrase tokenPhrase : candidatePhrases) {
			if ( candidateMatching[tokenPhraseIndex] >= 0 ) {
				AnnotatedPhrase matchedAnnotatedPhrase = annotatedPhrases
						.get(candidateMatching[tokenPhraseIndex]);
				int annotationStart = Integer.parseInt(
						matchedAnnotatedPhrase.getRawTokensStartOff());
				int annotationEnd = Integer
						.parseInt(matchedAnnotatedPhrase.getRawTokensEndOff());
				int tokenStart = tokenPhrase.getStartOffsetAsInt();
				int tokenEnd = tokenPhrase.getEndOffsetAsInt();
				if ( !((tokenEnd > annotationStart)
						&& (tokenStart < annotationEnd)) ) {
					candidateMatching[tokenPhraseIndex] = -1;
				}
			}
			tokenPhraseIndex++;
		}
		return candidateMatching;

	}

	/**
	 * Populate: a) the candidateConceptToAttributeNameAndAttributeMap, b) the
	 * documentToCandidateConceptToTokenPhrasesMap, and c) the
	 * candidateConceptToAttributeNameAndFrequencyMap for all documents being
	 * processed. Note that the a
	 *
	 * @param candidateConceptToAttributeNameAndAttributeMap
	 * @param documentToCandidateConceptToTokenPhrasesMap
	 * @param candidateConceptToAttributeNameAndFrequencyMap
	 * @param targetConcepts
	 * @param categoryToSubcategoriesMap
	 * @param textAnalyzer
	 */
	private void populateMappingsForAttributeBuilding(
			final Map<String, Map<String, Attribute>> candidateConceptToAttributeNameAndAttributeMap,
			final Map<String, Map<String, List<RaptatTokenPhrase>>> documentToCandidateConceptToTokenPhrasesMap,
			final Map<String, Map<String, Integer>> candidateConceptToAttributeNameAndFrequencyMap,
			final Set<String> targetConcepts,
			final Map<String, Set<String>> categoryToSubcategoriesMap,
			final TextAnalyzer textAnalyzer) {

		/*
		 * Copy the category to subcategory mapping as we may modify it when
		 * searching for attributes/features
		 */
		Map<String, Set<String>> catToSubcatMapCopy = new HashMap<>();
		for (Entry<String, Set<String>> entry : categoryToSubcategoriesMap
				.entrySet()) {
			catToSubcatMapCopy.put(entry.getKey(),
					new HashSet<>(entry.getValue()));
		}

		PrintWriter pw = null;
		/*
		 * Optionally, log all documents and indicate whether they contain at
		 * least one indexValue object. This was created to make sure all
		 * documents were processed regardless of presence or absence of at
		 * least one indexValue object.
		 */
		try {
			if ( !(this.logTokenPhrasesDirectory == null)
					&& !this.logTokenPhrasesDirectory.isEmpty() ) {
				pw = getLoggingPrintWriter(this.logTokenPhrasesDirectory.get());
			}

			/*
			 * Process each document in the set and identify *ALL* the
			 * attributes. We are doing this because we need to know the total
			 * number of attributes BEFORE we can create the Weka instance
			 * objects used for training and testing our model.
			 */
			Iterator<DocumentRecord> documentIterator = getTextDataIterator()
					.iterator();
			while ( documentIterator.hasNext() ) {

				DocumentRecord nextDocumentRecord = documentIterator.next();
				String nextDocumentId = nextDocumentRecord.fileName();
				String nextDocumentText = new String(
						nextDocumentRecord.documentTextAsByteArray(), "UTF-8");
				LOGGER.info("Building attributes - processing document:"
						+ nextDocumentId);
				final RaptatDocument raptatDocument = textAnalyzer.processText(
						nextDocumentText, Optional.ofNullable(nextDocumentId));
				final String documentName = raptatDocument.getTextSource()
						.orElse("Unnamed Document");

				/*
				 * Prints out whether a specific target label, "indexvalue", is
				 * even in the document
				 */
				if ( pw != null ) {
					logDocumentPhrases(pw, raptatDocument);
				}

				Map<String, List<RaptatTokenPhrase>> conceptToTokenPhrasesMap = new HashMap<>();
				documentToCandidateConceptToTokenPhrasesMap.put(documentName,
						conceptToTokenPhrasesMap);

				/*
				 * Token phrases for the same concept should have the same
				 * attributes. For example, index value concepts should point to
				 * the same phrases (index values like 0.20, 0.30, 1.2, etc) and
				 * therefore have the same context and attributes, but a concept
				 * like 'compressibility' would correspond to different phrases,
				 * different context, and different attributes. The only thing
				 * that should change for a different PhraseClass instance is
				 * the assigned classification. For example, a phrase, 0.20,
				 * assigned to the concept indexValue could have a
				 * classification of 'right' for the PhraseClass
				 * INDEX_VALUE_LATERALITY and a classification of 'abi' for the
				 * PhraseClass INDEX_VALUE_TYPE.
				 *
				 * We loop through conceptNames so we only have to build
				 * features once for each concept.
				 */
				for (String targetConceptName : targetConcepts) {

					/*
					 * Add features to the TokenPhrase instances within the
					 * document
					 */
					Label targetConceptLabel = new Label(
							targetConceptName.toLowerCase());
					List<RaptatTokenPhrase> candidateTokenPhrases = RaptatTokenPhrase
							.getCandidateTokenPhrases(raptatDocument,
									targetConceptLabel);

					this.candidatePhraseFeatureBuilder.addTokenPhraseFeatures(
							candidateTokenPhrases, targetConceptLabel,
							catToSubcatMapCopy);
					buildMetafeatures(candidateTokenPhrases, targetConceptName);

					conceptToTokenPhrasesMap.put(targetConceptName,
							candidateTokenPhrases);

					/*
					 * Get all the Attributes and their names corresponding to
					 * the current concept name and document.
					 */
					Map<String, Attribute> attributeNameToAttributeMap = candidateConceptToAttributeNameAndAttributeMap
							.computeIfAbsent(targetConceptName,
									key -> new HashMap<>(2 ^ 16));
					Map<String, Integer> attributeFrequency = updateConceptSpecificAttributes(
							candidateTokenPhrases, attributeNameToAttributeMap);
					attributeFrequency.forEach((key,
							value) -> candidateConceptToAttributeNameAndFrequencyMap
									.get(targetConceptName)
									.merge(key, value, (v1, v2) -> v1 + v2));
				}
			}
		}
		catch (URISyntaxException | IOException e) {
			System.err.println(
					"Unable to log token phrases\n" + e.getLocalizedMessage());
			e.printStackTrace();
		}
		finally {
			if ( pw != null ) {
				pw.close();
			}
		}
	}

	/**
	 * Loop through the RaptatTokenPhrase objects in the tokenPhrases list,
	 * populate the phraseStringToAttributeMap parameter, and return how many
	 * times the name of the attribute is found in the RaptatTokenPhrase
	 * objects.
	 *
	 * @param tokenPhrases
	 * @param documentName
	 * @param phraseStringToAttributeMap
	 * @return
	 */
	private Map<String, Integer> updateConceptSpecificAttributes(
			final List<RaptatTokenPhrase> tokenPhrases,
			final Map<String, Attribute> phraseStringToAttributeMap) {

		Map<String, Integer> featureFrequency = new HashMap<>();
		for (RaptatTokenPhrase tokenPhrase : tokenPhrases) {
			Set<String> phraseFeatures = tokenPhrase.getPhraseFeatures();

			for (String phraseFeatureString : phraseFeatures) {
				Attribute phraseMappedAttribute = null;
				if ( (phraseMappedAttribute = phraseStringToAttributeMap
						.get(phraseFeatureString)) == null ) {
					phraseMappedAttribute = new Attribute(phraseFeatureString,
							RaptatConstants.BINARY_FEATURE_VALUES);
					phraseStringToAttributeMap.put(phraseFeatureString,
							phraseMappedAttribute);
				}
				featureFrequency.compute(phraseFeatureString,
						(key, value) -> value == null ? 1 : value + 1);
			}
		}

		return featureFrequency;
	}

}
