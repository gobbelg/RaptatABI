package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.TokenPhraseMaker;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.DataIteratorType;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableData;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableDirectoryFile;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.SqlDataIterator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation.AnnotationMutator;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters.AttributeFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters.AttributeFrequencyFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters.AttributeNameFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.AbiHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.AttributeHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.IgpsBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.InstancesGetterParameters;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.InstancesMerger;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures.ClosestTagFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures.MetaFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures.MetaFeatureBuilderAndArffCreator;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures.ProximityDescriptorMetaFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.options.OptionsBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.temp.FileUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.weka.entities.ArffCreationParameterExtended;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/** @author VHATVHGOBBEG */
public class GirotraABI {

//@formatter:off
  private enum ConceptTypeName {
    TARGET_NAME,
    FIRST_RELATED_CONCEPT_NAME,
    SECOND_RELATED_CONCEPT_NAME,
    FIRST_CONCEPT_TARGET_RELATION_NAME,
    SECOND_CONCEPT_TARGET_RELATION_NAME,
    FIRST_CONCEPT_TARGET_ATTRIBUTE_NAME,
    SECOND_CONCEPT_TARGET_ATTRIBUTE_NAME
  }
//@formatter:on

//@formatter:off
  private enum RunType {
	RUN_MODEL,
    TRAIN_AND_TEST_WITH_ITERATOR,
    TRAIN_MODEL_REVISED,
    CREATE_TOKEN_PHRASE_LABEL_XML,
    MERGE_ANNOTATION_SETS,
    MERGE_RELATED_ANNOTATIONS,
    PROMOTE_RELATIONS_TO_CONCEPTS,
    REMAP_CONCEPT_NAMES,
    RUN_WEKA_CROSS_VALIDATION,
    XML_TO_WEKA_INSTANCES,
    XML_TO_TAB_DELIMITED,
    CANCEL_PROCESS;
  }
//@formatter:on

	public static final List<RaptatPair<Integer, Integer>> PROXIMITIES = new ArrayList<>();
	private static final Logger LOGGER = Logger.getLogger(GirotraABI.class);
	private static final String NO_CLASS_ASSIGNED_PHRASE_CLASS = "noclassassigned";
	private static final int REPORT_AND_GARBAGECOLLECTION_FREQUENCY = 100;
	private final static JFrame mainFrame = new JFrame();

	/*
	 * Proximities indicate closeness of some element to another in the text. A
	 * feature with a distance of 2,3, or 4 would match the proximity pair (2,
	 * 5). The distance 5 would not match. So the distance is from pair.left
	 * (inclusive) to pair.right (non-inclusive).
	 */
	static {
		PROXIMITIES.add(new RaptatPair<>(1, 2));

		PROXIMITIES.add(new RaptatPair<>(1, 4));
		PROXIMITIES.add(new RaptatPair<>(2, 4));

		PROXIMITIES.add(new RaptatPair<>(4, 7));
		PROXIMITIES.add(new RaptatPair<>(1, 7));

		PROXIMITIES.add(new RaptatPair<>(7, 13));
		PROXIMITIES.add(new RaptatPair<>(1, 13));

		PROXIMITIES.add(new RaptatPair<>(13, Integer.MAX_VALUE));
	}

	static {
		LOGGER.setLevel(Level.DEBUG);
	}

	/**
	 * @param raptatAnnotations
	 * @param textFile
	 * @param xmlExporter
	 */
	private void exportAnnotationList(
			final List<AnnotatedPhrase> annotationList, final File textFile,
			final XMLExporterRevised xmlExporter) {
		final RaptatDocument textDocument = new RaptatDocument();
		textDocument.setTextSourcePath(Optional.of(textFile.getAbsolutePath()));
		final AnnotationGroup annotationGroup = new AnnotationGroup(
				textDocument, annotationList);
		xmlExporter.exportReferenceAnnotationGroup(annotationGroup, true, true);
	}

	private MetaFeatureBuilderAndArffCreator initializeMetaFeatureBuilders(
			List<RaptatPair<Integer, Integer>> proximities,
			InstancesGetterParameters instanceGetterParameters) {
		return initializeMetaFeatureBuilders(
				new HashMap<PhraseClass, Optional<String>>(), proximities,
				instanceGetterParameters);
	}

	/**
	 * @param textPath
	 * @param phraseClassToXmlMap
	 * @param outputDirectory
	 * @param textAnalyzer
	 * @param proximityDistances
	 * @param attributeFilters
	 * @return
	 */
	private MetaFeatureBuilderAndArffCreator initializeMetaFeatureBuilders(
			final Map<PhraseClass, Optional<String>> phraseClassToXmlMap,
			final List<RaptatPair<Integer, Integer>> proximityDistances,
			InstancesGetterParameters igps) {
		LOGGER.info("\n\tCREATING FEATURE BUILDERS\n");

		/*
		 * MetaFeatureBuilder objects build features created based on features
		 * created by FeatureBuilder objects like those shown above.
		 */
		LOGGER.info("\n\tGETTING METAFEATURE BUILDERS\n");
		final MetaFeatureBuilder[] metaFeatureBuilders = new MetaFeatureBuilder[] {
				new ProximityDescriptorMetaFeatureBuilder(proximityDistances),
				new ClosestTagFeatureBuilder()
		};

		LOGGER.info("\n\tCREATING METAFEATUREBUILDER\n");
		ArffCreationParameterExtended arffParameter = new ArffCreationParameterExtended(
				phraseClassToXmlMap, null, "Girotra ABI Project",
				"Features and Metafeatures found in association with IndexValues Remapped to LateralityIndexValues",
				igps);
		final MetaFeatureBuilderAndArffCreator metaFeatureBuilderAndArffCreator = new MetaFeatureBuilderAndArffCreator(
				arffParameter, metaFeatureBuilders, igps.attributeFilters());
		metaFeatureBuilderAndArffCreator
				.setLogTokenPhrases(igps.logTokenPhrasesDirectory());

		return metaFeatureBuilderAndArffCreator;
	}

	private List<AnnotatedPhrase> remapConceptNames(
			final List<AnnotatedPhrase> annotatedPhrases,
			final Map<String, String> conceptMap) {

		if ( annotatedPhrases == null ) {
			return null;
		}

		List<AnnotatedPhrase> resultPhrases = new ArrayList<>();

		for (AnnotatedPhrase annotatedPhrase : annotatedPhrases) {
			AnnotatedPhrase copiedPhrase = new AnnotatedPhrase(annotatedPhrase);
			String conceptName = copiedPhrase.getConceptName();
			String mappedName = conceptMap.get(conceptName);
			if ( mappedName != null ) {
				copiedPhrase.setConceptName(mappedName);
			}

			resultPhrases.add(copiedPhrase);
		}
		return resultPhrases;
	}

	protected Map<String, Map<PhraseClass, Instances>> getCandidateConceptToPhraseClassToInstancesMap(
			final Map<PhraseClass, Optional<String>> phraseClassToXmlDirectoryMap,
			final List<RaptatPair<Integer, Integer>> proximityDistances,
			final InstancesGetterParameters igps)
			throws FileNotFoundException, IOException {

		LOGGER.info("\n\tGENERATING PHRASE CLASS INSTANCES\n");
		final InstanceBuilderModule instanceBuilderModule = initializeMetaFeatureBuilders(
				phraseClassToXmlDirectoryMap, proximityDistances, igps);
		final Map<String, Map<PhraseClass, Instances>> mappedInstances = instanceBuilderModule
				.buildMappedInstances();
		instanceBuilderModule.close();

		return mappedInstances;
	}

	protected List<RaptatTokenPhrase> getCandidatePhrasesAndFeatures(
			String targetConceptName, DocumentRecord textRecord,
			InstancesGetterParameters instanceGetterParameters,
			List<RaptatPair<Integer, Integer>> proximities) {
		final MetaFeatureBuilderAndArffCreator metaFeatureBuilderAndArffCreator = initializeMetaFeatureBuilders(
				proximities, instanceGetterParameters);

		List<RaptatTokenPhrase> candidateTokenPhrases = metaFeatureBuilderAndArffCreator
				.buildCandidatePhrasesAndFeatures(targetConceptName,
						textRecord);
		return candidateTokenPhrases;
	}

	/**
	 * @param textFilesPath
	 * @param xmlFilesPathAnnotatorOne
	 * @param xmlFilesPathAnnotatorTwo
	 * @param annotatorTwoConcepts
	 * @param annotatorOneConcepts
	 * @param conceptMap
	 * @param outputDirectoryPath
	 */
	protected void mergeAnnotationSets(final String textFilesPath,
			final String xmlFilesPathAnnotatorOne,
			final String xmlFilesPathAnnotatorTwo,
			final Set<String> annotatorOneConcepts,
			final Set<String> annotatorTwoConcepts,
			final Set<String> annotatorNames,
			final Map<String, String> conceptMap,
			final String outputDirectoryPath) {
		List<AnnotatedPhrase> mergedAnnotations = null;
		final AnnotationImporter xmlImporter = new AnnotationImporter(
				AnnotationApp.EHOST);
		final AnnotationMutator mutator = new AnnotationMutator();

		File outputDirectory = new File(outputDirectoryPath);
		if ( !outputDirectory.exists() ) {
			try {
				FileUtils.forceMkdir(outputDirectory);
			}
			catch (IOException e) {
				System.err.println("Unable to create folder "
						+ outputDirectoryPath + " for merged results\n"
						+ e.getLocalizedMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}
		final XMLExporterRevised xmlExporter = XMLExporterRevised
				.getExporter(new File(outputDirectoryPath), true);

		final Map<File, File> txtToXmlManualMap = GeneralHelper
				.getTxtToXmlFileMap(textFilesPath, xmlFilesPathAnnotatorOne);
		final Map<File, File> txtToXmlRapatMap = GeneralHelper
				.getTxtToXmlFileMap(textFilesPath, xmlFilesPathAnnotatorTwo);

		for (final File textFile : txtToXmlManualMap.keySet()) {
			final String xmlFilePathAnnotatorOne = txtToXmlManualMap
					.get(textFile).getAbsolutePath();
			final String xmlFilePathAnnotatorTwo = txtToXmlRapatMap
					.get(textFile).getAbsolutePath();

			List<AnnotatedPhrase> annotatorOneAnnotations = xmlImporter
					.importAnnotations(xmlFilePathAnnotatorOne,
							textFile.getAbsolutePath(), annotatorOneConcepts);
			annotatorOneAnnotations = mutator.filterForAnnotators(
					annotatorOneAnnotations, annotatorNames);
			List<AnnotatedPhrase> annotatorTwoAnnotations = xmlImporter
					.importAnnotations(xmlFilePathAnnotatorTwo,
							textFile.getAbsolutePath(), annotatorTwoConcepts);
			annotatorTwoAnnotations = mutator.filterForAnnotators(
					annotatorTwoAnnotations, annotatorNames);

			try {
				mergedAnnotations = mutator.mergeAnnotationLists(
						annotatorOneAnnotations, annotatorTwoAnnotations);
			}
			catch (final Exception e) {
				GeneralHelper.errorWriter(e.getLocalizedMessage());
				e.printStackTrace();
			}
			if ( (conceptMap != null) && !conceptMap.isEmpty()
					&& (mergedAnnotations != null)
					&& !mergedAnnotations.isEmpty() ) {
				mergedAnnotations = remapConceptNames(mergedAnnotations,
						conceptMap);
			}
			exportAnnotationList(mergedAnnotations, textFile, xmlExporter);
		}
	}

	protected int writeDetailedResults(
			Map<CandidatePhraseRecord, Map<PhraseClass, String>> candidatePhraseToAssignedClassMap,
			PrintWriter pw, DocumentRecord documentRecord) {
		int assignedValueTotal = 0;
		String delimiter = "\t";

		phraseRecordLoop: for (CandidatePhraseRecord phraseRecord : candidatePhraseToAssignedClassMap
				.keySet()) {
			List<PhraseClass> phraseClassList = new ArrayList<PhraseClass>();
			Map<PhraseClass, String> phraseClassToResultMap = candidatePhraseToAssignedClassMap
					.get(phraseRecord);
			for (PhraseClass phraseClass : phraseClassToResultMap.keySet()) {
				if ( phraseClassToResultMap.get(phraseClass).equalsIgnoreCase(
						PhraseClass.NO_CLASS_ASSIGNED.name()) ) {
					continue phraseRecordLoop;
				}
				phraseClassList.add(phraseClass);
			}
			StringBuilder sb = new StringBuilder(documentRecord
					.patientDocumentStationIdentifiers().toString(delimiter));
			sb.append(delimiter).append(documentRecord.noteDate());
			sb.append(delimiter).append(documentRecord.documentPath());
			sb.append(delimiter).append(phraseRecord.startOffset());
			sb.append(delimiter).append(phraseRecord.endOffset());
			sb.append(delimiter).append(phraseRecord.phraseText());
			Collections.sort(phraseClassList);
			boolean assignedTypeAndLaterality = true;
			for (PhraseClass phraseClass : phraseClassList) {
				sb.append(delimiter);
				String assignedPhraseClass = phraseClassToResultMap
						.get(phraseClass);
				sb.append(assignedPhraseClass);
				if ( assignedPhraseClass.equalsIgnoreCase(
						GirotraABI.NO_CLASS_ASSIGNED_PHRASE_CLASS) ) {
					assignedTypeAndLaterality = false;
				}
			}
			if ( assignedTypeAndLaterality ) {
				assignedValueTotal++;
			}
			pw.println(sb.toString());
			System.out.println(sb.toString());
		}
		pw.flush();

		return assignedValueTotal;
	}

	protected void writeDocumentResults(DocumentRecord documentRecord,
			int candidatePhrases, int valueAssignedPhrases,
			PrintWriter documentpw) {
		String delimiter = "\t";
		PatientDocumentStationRecord pdsRecord = documentRecord
				.patientDocumentStationIdentifiers();
		StringBuilder sb = new StringBuilder();
		sb.append(pdsRecord.toString(delimiter));
		sb.append(delimiter);
		sb.append(documentRecord.noteDate());
		sb.append(delimiter);
		sb.append(documentRecord.documentPath());
		sb.append(delimiter);
		sb.append(candidatePhrases);
		sb.append(delimiter);
		sb.append(valueAssignedPhrases);
		documentpw.println(sb);
		documentpw.flush();

		System.out.println(sb);
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final GirotraABI girotraAbiTool = new GirotraABI();

				/*
				 * After removing any comments from 'args,' use the RunType
				 * specified as the first element in the 'args' argument to the
				 * main method of this class. Otherwise, use the first value in
				 * the RunType enum as the default value unless another is
				 * chosen.
				 */
				List<String> argsAsList = FileUtilities
						.removeArrayComments(args);
				RunType runType = null;
				if ( (argsAsList.size() > 0) && (RunType
						.valueOf(argsAsList.get(0)) instanceof RunType) ) {
					runType = RunType.valueOf(argsAsList.get(0));
				}
				else {
					runType = selectRunType();
				}

				switch (runType) {
				case CREATE_TOKEN_PHRASE_LABEL_XML: {

					// final String textFolderPath =
					// "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_04/corpus";
					// final String xmlOutputFolderPath =
					// "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_04/ToolLabels_200213";
					// final String sentenceLogFolder =
					// "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_04/ToolLabelingLogs_200213";
					final String textFolderPath = "P:/ORD_Miller_202005037D/nashville/Glenn/HypoglycemiaFiles_201212";
					final String xmlOutputFolderPath = "P:/ORD_Miller_202005037D/nashville/Glenn/HypoglycemiaFiles_201212_XmlLabelOutput";
					final String sentenceLogFolder = "P:/ORD_Miller_202005037D/nashville/Glenn/HypoglycemiaFiles_SentenceLabelLogs";
					try {
						OptionsManager.getInstance().setRemoveStopWords(false);
						final TextAnalyzer ta = new TextAnalyzer();
						ta.setTokenProcessingOptions(
								OptionsManager.getInstance());
						// ta.createTokenPhraseMaker("ABILabelPatterns_v17_200212.txt");
						ta.createTokenPhraseMaker(
								"HypoglycemiaLabelPatterns_201214_v01.txt");
						final TokenPhraseWriter tpw = new TokenPhraseWriter();
						tpw.writePhrases(textFolderPath, xmlOutputFolderPath,
								sentenceLogFolder, ta);
					}
					catch (final NotDirectoryException e) {
						GeneralHelper.errorWriter(
								"Error during labeling and writing of token phrases\n"
										+ e.getLocalizedMessage());
						e.printStackTrace();
					}
				}
					break;

				/*
				 * Combine annotated concepts with a given name with the
				 * attributes of other annotations that have a relationship with
				 * this concept. This is used, for example, to convert an
				 * 'index_value' annotation that a laterality concept with a
				 * left attribute and an indextype attribute with attribute abi
				 * into a single concept with a name like 'indexvalue_left_abi,'
				 * so 3 concepts are merged into one.
				 */
				case MERGE_ANNOTATION_SETS: {
					final String directoryPath = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Group_06";
					final String textFilesPath = directoryPath + File.separator
							+ "corpus";
					final String xmlFilesPathAnnotatorOne = directoryPath
							+ File.separator + "SaketXml";
					final String xmlFilesPathAnnotatorTwo = directoryPath
							+ File.separator + "JuliaXml";
					final String outputDirectoryPath = directoryPath
							+ File.separator + "MergedXml";

					final Set<String> annotators = new HashSet<>(
							Arrays.asList(new String[] {
									"julia", "saket", "raptat"
					}));

					Map<String, String> conceptMap = new HashMap<>();
					conceptMap.put("01_Left_ABI", "index_value_abi_left");
					conceptMap.put("02_Right_ABI", "index_value_abi_right");
					conceptMap.put("03_Left_TBI", "index_value_tbi_left");
					conceptMap.put("04_Right_TBI", "index_value_tbi_right");
					conceptMap.put("05_LeftAndRight_ABI",
							"index_value_abi_bilateral");
					conceptMap.put("06_LeftAndRight_TBI",
							"index_value_tbi_bilateral");
					conceptMap.put("07_UnspecifiedLaterality_ABI",
							"index_value_abi_null");
					conceptMap.put("08_UnspecifiedLaterality_TBI",
							"index_value_tbi_null");
					conceptMap.put("09_Left_IndexValue",
							"index_value_null_left");
					conceptMap.put("10_Right_IndexValue",
							"index_value_null_right");
					conceptMap.put("11_UnspecifiedLaterality_IndexValue",
							"index_value_null_null");
					/*
					 * Convert map to be entirely lower case
					 */
					Map<String, String> lowerCaseMap = new HashMap<>();
					for (String mapKey : conceptMap.keySet()) {
						String value = conceptMap.get(mapKey);
						lowerCaseMap.put(mapKey.toLowerCase(),
								value.toLowerCase());
					}
					conceptMap = lowerCaseMap;

					final Set<String> annotatorOneConcepts = conceptMap
							.keySet();
					final Set<String> annotatorTwoConcepts = annotatorOneConcepts;

					girotraAbiTool.mergeAnnotationSets(textFilesPath,
							xmlFilesPathAnnotatorOne, xmlFilesPathAnnotatorTwo,
							annotatorOneConcepts, annotatorTwoConcepts,
							annotators, conceptMap, outputDirectoryPath);
					System.out.println("Ouput saved to:" + outputDirectoryPath);
				}
					break;

				/*
				 *
				 */
				case MERGE_RELATED_ANNOTATIONS: {
					final String pathToDataDirectory = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Group06";
					final String pathToXml = pathToDataDirectory
							+ File.separator + "originalXml";
					final String pathToText = pathToDataDirectory
							+ File.separator + "corpus";
					final String pathToResultFolder = pathToDataDirectory
							+ File.separator + "convertedXml";

					/*
					 * The typeNameMap indicates what annotations to import and
					 * how annotations will be converted. Annotations with a
					 * concept name mapped from TARGET_NAME will be combined
					 * with related annotations with concept names mapped from
					 * FIRST_CONCEPT_NAME and SECOND_CONCEPT_NAME. This provides
					 * a way to know which concepts will be combined and how.
					 */
					final Map<ConceptTypeName, String> typeNameMap = new HashMap<>();
					typeNameMap.put(ConceptTypeName.TARGET_NAME, "index_value");
					typeNameMap.put(ConceptTypeName.FIRST_RELATED_CONCEPT_NAME,
							"index_type");
					typeNameMap.put(
							ConceptTypeName.FIRST_CONCEPT_TARGET_RELATION_NAME,
							"indextypeofindexvalue");
					typeNameMap.put(
							ConceptTypeName.FIRST_CONCEPT_TARGET_ATTRIBUTE_NAME,
							"abiortbi");

					typeNameMap.put(ConceptTypeName.SECOND_RELATED_CONCEPT_NAME,
							"laterality");
					typeNameMap.put(
							ConceptTypeName.SECOND_CONCEPT_TARGET_RELATION_NAME,
							"lateralityrelationship");
					typeNameMap.put(
							ConceptTypeName.SECOND_CONCEPT_TARGET_ATTRIBUTE_NAME,
							"leftorright");

					combineRelatedAnnotations(pathToText, pathToXml,
							pathToResultFolder, typeNameMap);
				}
					break;

				case PROMOTE_RELATIONS_TO_CONCEPTS: {
					try {
						final String startDirectory = "P:/ORD_Girotra_201607120D/Glenn/ManuscriptData_200704/IAAData";
						final String textDirectoryPath = startDirectory
								+ File.separator + "Groups7and8Corpus_200704";
						final String inputXmlPath = startDirectory
								+ File.separator + "JuliaXmlGroups7and8_200704";
						final String outputFolderName = "ConceptPromotedRelationships";
						AnnotationMutator annotationMutator = new AnnotationMutator();
						final String outputDirectoryPath = startDirectory
								+ File.separator + outputFolderName + "_"
								+ GeneralHelper.getTimeStamp();
						FileUtils.forceMkdir(new File(outputDirectoryPath));

						String targetConcept = "index_value";
						Map<String, String> relationshipConceptToAttributeMap = new HashMap<>();
						relationshipConceptToAttributeMap.put("laterality",
								"leftorright");
						relationshipConceptToAttributeMap.put("index_type",
								"abiortbi");

						promoteRelationsToConcepts(textDirectoryPath,
								inputXmlPath, outputDirectoryPath,
								targetConcept,
								relationshipConceptToAttributeMap,
								annotationMutator);

					}
					catch (IOException e) {
						System.err.println(
								"Unable to locate or create files for running "
										+ e.getLocalizedMessage());
						e.printStackTrace();
						System.exit(-1);
					}

				}
					break;

				/*
				 * Takes a given set of concept names for annotations in an
				 * eHOST xml file and remaps them to another name.
				 */
				case REMAP_CONCEPT_NAMES: {
					try {
						final String startDirectory = "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForABITool_200513";
						final String textPath = startDirectory + File.separator
								+ "corpus";
						final String inputXmlPath = startDirectory
								+ File.separator
								+ "xmlAdjudicatedUpdate_200518";
						Map<String, String> remap = new HashMap<>();

						// remap.put("Index_Value_ABI_Left", "abi");
						// remap.put("Index_Value_ABI_Right", "abi");
						// remap.put("Index_Value_TBI_Left", "tbi");
						// remap.put("Index_Value_TBI_Right", "tbi");
						// remap.put("Index_Value_ABI_Null", "abi");
						// remap.put("Index_Value_TBI_Null", "tbi");

						remap.put("Index_Value_ABI_Left", "left");
						remap.put("Index_Value_ABI_Right", "right");
						remap.put("Index_Value_TBI_Left", "left");
						remap.put("Index_Value_TBI_Right", "right");
						remap.put("Index_Value_Null_Left", "left");
						remap.put("Index_Value_Null_Right", "right");

						/*
						 * Convert map to be entirely lower case
						 */
						Map<String, String> lowerCaseMap = new HashMap<>();
						for (String mapKey : remap.keySet()) {
							String value = remap.get(mapKey);
							lowerCaseMap.put(mapKey.toLowerCase(),
									value.toLowerCase());
						}
						remap = lowerCaseMap;

						OptionsManager.getInstance().setRemoveStopWords(false);
						final TextAnalyzer textAnalyzer = new TextAnalyzer();
						textAnalyzer.setTokenProcessingOptions(
								OptionsManager.getInstance());

						final String outputFolderName = "XmlAdjudUpdatedRemap_Laterality";
						final String outputDirectoryPath = startDirectory
								+ File.separator + outputFolderName + "_"
								+ GeneralHelper.getTimeStamp();
						FileUtils.forceMkdir(new File(outputDirectoryPath));
						remapConceptNames(textPath, inputXmlPath,
								outputDirectoryPath, remap);

					}
					catch (final IOException e) {
						System.err.println(
								"Unable to locate or create files for running "
										+ e.getLocalizedMessage());
						e.printStackTrace();
						System.exit(-1);
					}
				}
					break;

				case RUN_MODEL: {

					/*
					 * Load the previously trained model
					 */
					final String modelDirectory = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_06";
					String modelName = "TrainedIndexTypeAndLateralityModel_230729_171904.mdl";
					AbiSolution abiSolution = AbiSolution.load(modelDirectory,
							modelName);

					/*
					 * Set up a textAnalyzer that is equivalent to the one used
					 * for pre-processing of training data
					 */
					TextAnalyzer textAnalyzer = new TextAnalyzer();
					textAnalyzer.setTokenPhraseMaker(
							abiSolution.tokenPhraseMaker());
					textAnalyzer.setTokenProcessingParameters(
							abiSolution.tokenProcessingOptions());

					/*
					 * Make the directory names null because we are running the
					 * model, not testing it, so we don't need to compare the
					 * output to existing values
					 */
					String lateralityDirectoryTestName = null;
					String indexTypeDirectoryTestName = null;

					/*
					 * We use all features, so we don't need to filter any
					 * attributes or have the feature be a minimum proportion of
					 * all features
					 */
					Double minimumFeatureProportion = null;
					boolean filterAttributes = false;
					String logTokenPhrasesDirectory = "";

					/*
					 * Set whether the data will come from files or a SQL
					 * database and, if a file, indicate the directory
					 * containing all the notes to be processed
					 */
					DataIteratorType dataType = DataIteratorType.FILE_DATA;
					// String testingTextSourceParentDirectory =
					// "P:/ORD_Girotra_202110052D/Glenn/ConcatedNoteSets/AllTIUAbiNotes_Table_tiu_abi_111722";
					// String testingTextSourceDirectoryName =
					// "DeconcatRoot_230812_155321";
					String testingTextSourceParentDirectory = "P:/ORD_Nguyen_202206062D/Glenn/DataAnalysis/FirstPADAssessment";
					String testingTextSourceDirectoryName = "txtFiles_240918_194203";
					// String testingTextSourceDirectoryName =
					// "deconcat_002_127";
					String testingTextSourceInfo = testingTextSourceParentDirectory
							+ File.separator + testingTextSourceDirectoryName;

					/*
					 * 'fileIdLabelHeader' is a string on each note beginning
					 * the line that contains identifying information related to
					 * the file and its contents //
					 */
					// String fileIdLabelHeader = "PatientSID_DocID_Sta3n:"
					// + " \t";
					// String dateLabelHeader = "BeginDateTime:\\s+\r?\n?";

					String fileIdLabelHeader = "PatientSID_DocID_Sta3n:\t";
					String dateLabelHeader = "BeginDateTime:\t";

					/*
					 * Set up to write out results
					 */
					String fileType = "TIU";
					String detailedOutputFileName = "DetailedAbiTool_"
							+ fileType + "Results_"
							+ GeneralHelper.getTimeStamp() + ".txt";
					File detailedOutputFile = Path
							.of(testingTextSourceParentDirectory,
									detailedOutputFileName)
							.toFile();
					PrintWriter detailedpw = null;

					String documentOutputFileName = "DocumentAbiTool_"
							+ fileType + "NotesEvaluated_"
							+ GeneralHelper.getTimeStamp() + ".txt";
					File documentOutputFile = Path
							.of(testingTextSourceParentDirectory,
									documentOutputFileName)
							.toFile();
					PrintWriter documentpw = null;

					try {
						detailedpw = new PrintWriter(detailedOutputFile);
						documentpw = new PrintWriter(documentOutputFile);
					}
					catch (FileNotFoundException e) {
						System.err.println(
								"Unable to create files for writing results");
						System.err.println(e.getLocalizedMessage());
						e.printStackTrace();
						System.exit(-1);
					}

					String[] dataIteratorArgs = new String[3];
					dataIteratorArgs[0] = testingTextSourceInfo;
					dataIteratorArgs[1] = fileIdLabelHeader;
					dataIteratorArgs[2] = dateLabelHeader;

					/* Set values of igpsBuilder object */
					IgpsBuilder igpsBuilder = IgpsBuilder.getBuilder()
							.setGirotraAbiTool(girotraAbiTool)
							.setTextDataIterator(getIterableData(null, dataType,
									null, null, dataIteratorArgs))
							.setTextAnalyzer(textAnalyzer)
							.setLateralityDirectory(lateralityDirectoryTestName)
							.setIndexTypeDirectory(indexTypeDirectoryTestName)
							.setAttributeFilters(getAttributeFilters(
									Collections.emptyList(), filterAttributes,
									minimumFeatureProportion))
							.setLogTokenPhrasesDirectory(
									logTokenPhrasesDirectory);

					InstancesGetterParameters instanceGetterParameters = igpsBuilder
							.build();

					Iterator<DocumentRecord> textDataIterator = instanceGetterParameters
							.iterableData().iterator();

					int documentIndex = 0;
					while ( textDataIterator.hasNext() ) {

						DocumentRecord documentRecord = textDataIterator.next();
						int candidatePhrases = 0;
						int valueAssignedPhrases = 0;
						/*
						 * For each concept, we'll run through all the
						 * PhraseClass instances to which they map and combine
						 * those where the Instance objects for given concept
						 * classify as one of each of the mapped PhraseClasses.
						 *
						 * For example, for the concept IndexValue, we'll only
						 * consider "candidate" IndexValue token phrases as
						 * "true" IndexValue phrases if both IndexValueType and
						 * IndexValueLaterality both get classified as actual
						 * values (i.e., ABI/TBI and Left/Right)
						 */
						for (String targetConceptName : abiSolution
								.abiClassifierMapping().keySet()) {

							Map<CandidatePhraseRecord, Map<PhraseClass, String>> candidatePhraseToAssignedPhraseValueMap = assignConceptPhraseClassValuesToDocumentPhrases(
									abiSolution, instanceGetterParameters,
									documentRecord, targetConceptName);

							candidatePhrases += candidatePhraseToAssignedPhraseValueMap
									.size();
							valueAssignedPhrases += girotraAbiTool
									.writeDetailedResults(
											candidatePhraseToAssignedPhraseValueMap,
											detailedpw, documentRecord);
						}
						girotraAbiTool.writeDocumentResults(documentRecord,
								candidatePhrases, valueAssignedPhrases,
								documentpw);
						documentIndex++;
						if ( (documentIndex
								% REPORT_AND_GARBAGECOLLECTION_FREQUENCY) == 0 ) {
							System.out.println("Completed processing document "
									+ documentIndex);
							System.gc();
						}

					}
					detailedpw.flush();
					detailedpw.close();

					documentpw.flush();
					documentpw.close();
				}
					break;
				/*
				 * This case will take all the xml files in a directory
				 * (specified explicitly in the code below) and the corpus of
				 * corresponding documents (also specified below) and run
				 * cross-validation
				 */
				case RUN_WEKA_CROSS_VALIDATION: {
					try {
						final String dataDirectory = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_06";
						final Optional<String> outputDirectory = Optional
								.of(dataDirectory + File.separator
										+ "CrossValidationOutput");
						FileUtils.forceMkdir(new File(outputDirectory.get()));

						final Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassInstancesMap = buildWekaInstances(
								girotraAbiTool, dataDirectory, outputDirectory);

						CrossValidator crossValidator = new CrossValidator();

						String identifierAttributes = "1-4";
						List<String> filteredClassifierOptions = OptionsBuilder
								.generateFilteredClassifierOptions(
										identifierAttributes);

						List<String> crossValidationOptions = new ArrayList<>();
						OptionsBuilder.addCrossValidationOptions(
								crossValidationOptions);
						OptionsBuilder.addPredictionOptions(
								crossValidationOptions, identifierAttributes);

						Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> resultMap = crossValidator
								.runCVClassificationByPhraseClass(
										conceptToPhraseClassInstancesMap,
										crossValidationOptions,
										filteredClassifierOptions);

						Map<String, Map<String, List<String>>> matchedResults = AbiHelper
								.matchPhraseClassesByConcept(resultMap);
						AbiHelper.printMatchedResults(matchedResults,
								outputDirectory.get());

					}
					catch (final IOException e) {
						System.err.println(
								"Unable to locate or create files for running "
										+ e.getLocalizedMessage());
						e.printStackTrace();
						System.exit(-1);
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
					break;

				case TRAIN_MODEL_REVISED: {

					try {

						System.out.println("GENERATING TRAINING INSTANCES");

						/*
						 * Set values used in the fields of an IpgsBuilder,
						 * which will build the InstancesGetterParameter object
						 * used to get the instances used for training.
						 */
						String rootPath = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_06";
						String corpusDirectoryName = "corpus";
						String lateralityDirectoryName = "XmlLateralityExerciseValuesExcluded";
						String indexTypeDirectoryName = "XmlIndexTypeExerciseValuesExcluded";

						String trainingTextSourceInfo = rootPath
								+ File.separator + corpusDirectoryName;
						String lateralityDirectoryPathString = rootPath
								+ File.separator + lateralityDirectoryName;
						String indexDirectoryPathString = rootPath
								+ File.separator + indexTypeDirectoryName;

						/*
						 * 'fileIdLabelHeader' is a string on each note
						 * beginning the line that contains identifying
						 * information related to the file and its contents
						 */
						String fileIdLabelHeader = "PatientSID_DocID_Sta3n:"
								+ " \t";
						String dateLabelHeader = "BeginDateTime:\\s+\r?\n?";

						DataIteratorType dataType = DataIteratorType.FILE_DATA;
						String tokenPhraseMakerPatternFileName = "ABILabelPatterns_v18_200513.txt";

						boolean filterTrainingAttributes = false;
						Double trainingMinimumFeatureProportion = 0.05;
						String logTokenPhrasesDirectory = "";

						/* Set the values of the IgpsBuiler object */
						IgpsBuilder igpsBuilder = IgpsBuilder.getBuilder()
								.setGirotraAbiTool(girotraAbiTool)
								.setTextDataIterator(
										getIterableData(trainingTextSourceInfo,
												dataType, fileIdLabelHeader,
												dateLabelHeader, null))
								.setTextAnalyzer(getTextAnalyzer(
										tokenPhraseMakerPatternFileName))
								.setLateralityDirectory(
										lateralityDirectoryPathString)
								.setIndexTypeDirectory(indexDirectoryPathString)
								.setAttributeFilters(getAttributeFilters(
										Collections.emptyList(),
										filterTrainingAttributes,
										trainingMinimumFeatureProportion));
						igpsBuilder.setLogTokenPhrasesDirectory(
								logTokenPhrasesDirectory);

						/*
						 * Create a mapping from one or more 'target' concepts
						 * (e.g. indexvalue or compressibility) to PhraseClasses
						 * (e.g. indexvaluetype or indexvaluelaterality) to Weka
						 * Instances
						 */
						InstancesGetterParameters instanceGetterParameters = igpsBuilder
								.build();
						final Map<String, Map<PhraseClass, Instances>> mapToInstancesForTraining = getCandidateConceptAndPhraseClassToInstancesMap(
								instanceGetterParameters);

						if ( LOGGER.isDebugEnabled() ) {
							System.out.println(
									"Writing WEKA Instances to disk file");
							String testOutputPathName = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_06/outputForWeka";
							writeWekaInstances(testOutputPathName,
									mapToInstancesForTraining, "TrainingNew");
						}

						System.out.println("TRAINING INSTANCES GENERATED");

						final String identifierAttributes = "1-4";
						List<String> filteredClassifierOptions = OptionsBuilder
								.generateFilteredClassifierOptions(
										identifierAttributes);

						String[] optionsArray = filteredClassifierOptions
								.toArray(new String[] {});

						Map<String, Map<PhraseClass, Classifier>> abiClassifierMapping = getAbiClassifierMapping(
								mapToInstancesForTraining, optionsArray);
						TokenPhraseMaker tokenPhraseMaker = instanceGetterParameters
								.textAnalyzer().getTokenPhraseMaker();
						TokenProcessingOptions tokenProcessingOptions = instanceGetterParameters
								.textAnalyzer().getTokenProcessingOptions();
						Map<String, Map<PhraseClass, List<Attribute>>> mapToTrainingAttributes = getMapToTrainingAttributes(
								mapToInstancesForTraining);

						/* Where I left off 230608_1020 */
						Map<String, Map<PhraseClass, Attribute>> mapToClassAttributes = getMapToPhraseClassAttributes(
								mapToInstancesForTraining);
						AbiSolution abiSolution = new AbiSolution(
								abiClassifierMapping, mapToTrainingAttributes,
								mapToClassAttributes, tokenPhraseMaker,
								tokenProcessingOptions);

						System.out.println("ABI SOLUTION CREATED");

						String modelName = "TrainedIndexTypeAndLateralityModel_"
								+ GeneralHelper.getTimeStamp() + ".mdl";
						String modelPathString = rootPath + File.separator
								+ modelName;

						abiSolution.save(modelPathString);

						System.out.println("ABI SOLUTION WRITTEN TO DISK");
						System.out
								.println("Path to solution:" + modelPathString);
					}
					catch (Exception e) {
						e.printStackTrace();
						System.err.println(e.getMessage());
					}
				}
					break;

				/*
				 * The difference between TRAIN_AND_TEST case and the
				 * TRAIN_AND_TEST_INPUT_MAPPED_CLASSIFIER appears to be that
				 * TRAIN_AND_TEST calls the method getActualVSPredicted. In
				 * contrast, TRAIN_AND_TEST_INPUT_MAPPED_CLASSIFIER calls
				 * getActualVsPredictedInputMapping() method. The "InputMapping"
				 * method makes sure that the TRAIN and TEST sets have exactly
				 * the same features. Any feature present in one set that is not
				 * in the other is set to missing. In the case of not using
				 * InputMapping, any elements missing from one set is set to
				 * zero.
				 *
				 * The **preferred method** is **not** to use **InputMapping**
				 * so the largest set of features combined from both sets are
				 * used in both the training and testing set.
				 */
				case TRAIN_AND_TEST_WITH_ITERATOR: {
					try {

						System.out.println("GENERATING TRAINING INSTANCES");

						/*
						 * Set values used in the fields of an IpgsBuilder,
						 * which will build the InstancesGetterParameter object
						 * used to get the instances used for training.
						 */
						// Modified on 7/23/23 and replaced with
						// trainingRootPath below it
						String trainingRootPath = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_06";
						// String trainingRootPath =
						// "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_04Small";
						String trainingCorpusDirectoryName = "corpus";
						String trainingLateralityDirectoryName = "XmlLateralityExerciseValuesExcluded";
						String trainingIndexTypeDirectoryName = "XmlIndexTypeExerciseValuesExcluded";

						String trainingTextSourceInfo = trainingRootPath
								+ File.separator + trainingCorpusDirectoryName;
						String trainingLateralityDirectoryPathString = trainingRootPath
								+ File.separator
								+ trainingLateralityDirectoryName;
						String trainingIndexDirectoryPathString = trainingRootPath
								+ File.separator
								+ trainingIndexTypeDirectoryName;

						/*
						 * 'fileIdLabelHeader' is a string on each note
						 * beginning the line that contains identifying
						 * information related to the file and its contents
						 */
						String fileIdLabelHeader = "PatientSID_DocID_Sta3n:"
								+ " \t";
						String dateLabelHeader = "BeginDateTime:\\s+\r?\n?";

						DataIteratorType dataType = DataIteratorType.FILE_DATA;
						String tokenPhraseMakerPatternFileName = "ABILabelPatterns_v18_200513.txt";

						boolean filterTrainingAttributes = false;
						Double trainingMinimumFeatureProportion = 0.05;
						String logTokenPhrasesDirectory = "";

						/*
						 * Create and set the values of the IgpsBuilder object
						 */
						IgpsBuilder igpsBuilder = IgpsBuilder.getBuilder()
								.setGirotraAbiTool(girotraAbiTool)
								.setTextDataIterator(
										getIterableData(trainingTextSourceInfo,
												dataType, fileIdLabelHeader,
												dateLabelHeader, null))
								.setTextAnalyzer(getTextAnalyzer(
										tokenPhraseMakerPatternFileName))
								.setLateralityDirectory(
										trainingLateralityDirectoryPathString)
								.setIndexTypeDirectory(
										trainingIndexDirectoryPathString)
								.setAttributeFilters(getAttributeFilters(
										Collections.emptyList(),
										filterTrainingAttributes,
										trainingMinimumFeatureProportion))
								.setLogTokenPhrasesDirectory(
										logTokenPhrasesDirectory);

						/*
						 * Create a mapping from one or more 'target' concepts
						 * (e.g. indexvalue or compressibility) to PhraseClasses
						 * (e.g. indexvaluetype or indexvaluelaterality) to Weka
						 * Instances
						 */
						final Map<String, Map<PhraseClass, Instances>> mapToInstancesForTraining = getCandidateConceptAndPhraseClassToInstancesMap(
								igpsBuilder.build());

						// Debugging code added 7/21/23
						{
							// String testOutputPathName =
							// "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForAbiTool_SmallSample_230723/testOutput";
							String testOutputPathName = "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForABITool_200513/testOutput";
							writeWekaInstances(testOutputPathName,
									mapToInstancesForTraining, "TrainingNew");
						}

						System.out.println("GENERATING TESTING INSTANCES");

						/*
						 * Modify the values used in the fields of an
						 * IpgsBuilder, then use it again to build the
						 * InstancesGetterParameter object used to get the
						 * instances used for testing.
						 *
						 * Note that we set lateralityDirectoryTestName and
						 * indexTypeDirectoryTestName to null when we do not
						 * care about comparing the outputs to a reference
						 * standard.
						 *
						 * Also note that we do not filter out any features when
						 * running a model. Features in the training set that
						 * are missing in the testing set will be set to zero,
						 * and features in the testing set that are not in the
						 * training set will not be used.
						 */
						// String testingTextSourceInfo =
						// "P:/ORD_Girotra_202110052D/Glenn/TestTextNotes/DeconcatRoot_230501_201352";
						//
						// String lateralityDirectoryTestName = null;
						// String indexTypeDirectoryTestName = null;
						// String testingRootPath =
						// "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForAbiTool_SmallSample_230723";
						String testingRootPath = "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForABITool_200513";
						String testingCorpusDirectoryName = "corpus";
						String testingLateralityDirectoryName = "XmlAdjudUpdatedRemap_Laterality_200518_183300";
						String testingIndexTypeDirectoryName = "XmlAdjudUpdatedRemap_IndexType_200518_183055";

						String testingTextSourceInfo = testingRootPath
								+ File.separator + testingCorpusDirectoryName;
						String testingLateralityDirectoryPathString = testingRootPath
								+ File.separator
								+ testingLateralityDirectoryName;
						String testingIndexDirectoryPathString = testingRootPath
								+ File.separator
								+ testingIndexTypeDirectoryName;

						boolean filterTestingAttributes = false;
						Double testingMinimumFeatureProportion = null;

						/* Set altered values of igpsBuilder object */
						dataType = DataIteratorType.FILE_DATA;
						igpsBuilder
								.setTextDataIterator(
										getIterableData(testingTextSourceInfo,
												dataType, fileIdLabelHeader,
												dateLabelHeader, null))
								.setLateralityDirectory(
										testingLateralityDirectoryPathString)
								.setIndexTypeDirectory(
										testingIndexDirectoryPathString)
								.setAttributeFilters(getAttributeFilters(
										Collections.emptyList(),
										filterTestingAttributes,
										testingMinimumFeatureProportion));

						final Map<String, Map<PhraseClass, Instances>> mapToInstancesForTesting = getCandidateConceptAndPhraseClassToInstancesMap(
								igpsBuilder.build());

						// Debugging code added 7/21/23
						{
							String testOutputPathName = "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForAbiTool_SmallSample_230723/testOutput";
							writeWekaInstances(testOutputPathName,
									mapToInstancesForTesting, "TestingNew");
						}

						final Map<String, Map<PhraseClass, RaptatPair<Instances, Instances>>> mapToPairedInstances = pairTrainTestInstances(
								mapToInstancesForTraining,
								mapToInstancesForTesting);

						String predictionTaggingAttributes = "1-4";
						final Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> actualVsPredicted = getActualVsPredicted(
								mapToPairedInstances,
								predictionTaggingAttributes);

						Map<String, Map<String, List<String>>> matchedResults = AbiHelper
								.matchPhraseClassesByConcept(actualVsPredicted);

						String outputDirectory = new File(testingTextSourceInfo)
								.getParent() + File.separator
								+ "outputForWeka_NewTool_"
								+ GeneralHelper.getTimeStamp();
						FileUtils.forceMkdir(new File(outputDirectory));
						AbiHelper.printMatchedResults(matchedResults,
								outputDirectory);

					}
					catch (FileNotFoundException e) {
						System.err.println(
								"Unable to locate or create files for running "
										+ e.getLocalizedMessage());
						e.printStackTrace();
						System.exit(-1);
						e.printStackTrace();
					}
					catch (IOException e) {
						System.err.println(
								"Unable to read or write files for running "
										+ e.getLocalizedMessage());
						e.printStackTrace();
						System.exit(-1);
					}
				}
					break;

				case XML_TO_TAB_DELIMITED: {
					String eHostWorkspace = "P:/ORD_Girotra_202110052D/Glenn/eHOSTWorkspace";
					String projectName = "TIUNotes";
					String xmlFolderPath = eHostWorkspace + File.separator
							+ projectName + File.separator + "saved";
					String txtFolderPath = eHostWorkspace + File.separator
							+ projectName + File.separator + "corpus";
					XMLExporterRevised.xmlFilesToTabDelimited(xmlFolderPath,
							txtFolderPath);
				}
					break;

				default:
					break;
				}
				System.out.println();
				System.out.println("------------------------------");
				System.out.println("   Processing Complete");
				System.out.println("------------------------------");
				System.exit(0);
			}

			private Map<CandidatePhraseRecord, Map<PhraseClass, String>> assignConceptPhraseClassValuesToDocumentPhrases(
					AbiSolution abiSolution,
					InstancesGetterParameters instanceGetterParameters,
					DocumentRecord documentRecord, String targetConcept) {

				Map<PhraseClass, Classifier> phraseClassToClassifierMap = abiSolution
						.abiClassifierMapping().get(targetConcept);

				List<RaptatTokenPhrase> candidateTokenPhrases = instanceGetterParameters
						.girotraAbiTool()
						.getCandidatePhrasesAndFeatures(targetConcept,
								documentRecord, instanceGetterParameters,
								GirotraABI.PROXIMITIES);

				Map<CandidatePhraseRecord, Map<PhraseClass, String>> candidatePhraseToAssignedPhraseValueMap = new HashMap<>();
				InstancesBuilder instancesBuilder = new InstancesBuilder();

				for (PhraseClass phraseClass : phraseClassToClassifierMap
						.keySet()) {

					Classifier classifier = phraseClassToClassifierMap
							.get(phraseClass);

					List<Attribute> attributes = abiSolution
							.mapToTrainingAttributes().get(targetConcept)
							.get(phraseClass);

					assignPhraseClassValuesToDocumentPhrases(instancesBuilder,
							candidateTokenPhrases,
							candidatePhraseToAssignedPhraseValueMap,
							phraseClass, classifier, attributes);

				}

				return candidatePhraseToAssignedPhraseValueMap;
			}

			private void assignPhraseClassValuesToDocumentPhrases(
					InstancesBuilder instanceBuilder,
					List<RaptatTokenPhrase> candidateTokenPhrases,
					Map<CandidatePhraseRecord, Map<PhraseClass, String>> candidatePhraseToAssignedPhraseValueMap,
					PhraseClass phraseClass, Classifier classifier,
					List<Attribute> attributes) {

				Attribute classAttribute = new Attribute(phraseClass.toString(),
						new ArrayList<>(phraseClass.getClassifications()));

				boolean addCommonAttributes = false;
				Instances instances = instanceBuilder
						.buildInstancesObjectFromAttributes(attributes,
								phraseClass, addCommonAttributes);

				instanceBuilder.updateInstancesFromTokenPhrases(instances,
						candidateTokenPhrases, attributes, phraseClass);

				/*
				 * Classify the value of all the candidate index values in the
				 * document according to type and laterality
				 */
				Enumeration<Instance> instanceEnumerator = instances
						.enumerateInstances();
				while ( instanceEnumerator.hasMoreElements() ) {
					Instance instance = instanceEnumerator.nextElement();
					assignPhraseClassValueToPhrase(
							candidatePhraseToAssignedPhraseValueMap,
							phraseClass, classifier, classAttribute, instances,
							instance);
				}
			}

			private void assignPhraseClassValueToPhrase(
					Map<CandidatePhraseRecord, Map<PhraseClass, String>> candidatePhraseToAssignedPhraseValueMap,
					PhraseClass phraseClass, Classifier classifier,
					Attribute classAttribute, Instances instances,
					Instance instance) {
				CandidatePhraseRecord candidatePhraseRecord = CandidatePhraseRecord
						.getRecordFromInstance(instances, instance);
				Map<PhraseClass, String> phraseClassToAssignedValue = candidatePhraseToAssignedPhraseValueMap
						.computeIfAbsent(candidatePhraseRecord,
								k -> new HashMap<PhraseClass, String>());
				try {
					double predictedClassIndex = classifier
							.classifyInstance(instance);
					String assignedValue = classAttribute
							.value((int) predictedClassIndex);
					phraseClassToAssignedValue.put(phraseClass, assignedValue);
				}
				catch (Exception e) {
					System.err.println(
							"Unable to predict output for " + instance);
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
					System.exit(-1);
				}
			}

			/**
			 * This method builds a mapping of concept to a Weka instances for
			 * each PhraseClass specified. Each PhraseClass object specified
			 * should have a corresponding set of xml files, and the names of
			 * the classes of annotations in the file should be the same as the
			 * classifications field of the phrase class.
			 *
			 * For PhraseClass objects with the same conceptName field, the
			 * predicted classes will be combined to form a single class. For
			 * example, if index values like 0.78 are being annotated, they
			 * might have one PhraseClass predicted classification of abi
			 * (meaning it's an 'abi' type of index value) and another
			 * PhraseClass predicted classification of 'left' (meaning the index
			 * value has a laterality of 'left'). These would be combined to
			 * form 'left_abi' classification. The combined prediction would
			 * only be correct if both 'left' and 'abi' were correct
			 * classifications.
			 *
			 * The method explicitly specifies in the code the name of a
			 * "TokenPhraseMaker" used to assign labels to phrases identified in
			 * the code, which are used to both find a label of interest like
			 * index value and identify features to determine the classification
			 * of any label of interest. Also specified in the code are 1) any
			 * features that should be excluded if they have a name containing a
			 * particular string, 2) the minimum proportion of instances with
			 * the label of interest for inclusion, and the names of the
			 * directories containing the xml files and the name of the
			 * PhraseClass enum that contains the same classifications as in the
			 * file.
			 *
			 * Generally, the only items that should be changed are the name of
			 * the xml directories. The "TokenPhraseMaker," the excluded feature
			 * names, and the minimum proportion would stay the same for
			 * identifying a particular concept, like index value.
			 *
			 * @param girotraAbiTool
			 * @param xmlDataDirectory
			 * @param textPath
			 * @param outputPath
			 * @return
			 * @throws FileNotFoundException
			 * @throws IOException
			 */
			private Map<String, Map<PhraseClass, Instances>> buildWekaInstances(
					final GirotraABI girotraAbiTool, final String dataDirectory,
					final Optional<String> outputPath)
					throws FileNotFoundException, IOException {

				Optional<String> lateralityDirectoryTrainName = Optional
						.of("XmlLateralityExerciseValuesExcluded");
				Optional<String> indexTypeDirectoryTrainName = Optional
						.of("XmlIndexTypeExerciseValuesExcluded");
				Map<PhraseClass, Optional<String>> phraseClassToXmlDirectoryMap = getPhraseClassToXmlDirectoryMap(
						lateralityDirectoryTrainName,
						indexTypeDirectoryTrainName);
				String trainingTextSourceInfo = "P:/ORD_Girotra_201607120D/Glenn/TrainingFilesForABITool_200128/Groups_01_04";

				/*
				 * 'fileIdLabelHeader' is a string on each note beginning the
				 * line that contains identifying information related to the
				 * file and its contents
				 */
				String fileIdLabelHeader = "PatientSID_DocID_Sta3n:" + " \t";
				String dateLabelHeader = "BeginDateTime:\\s+\r?\n?";

				DataIteratorType iteratorType = DataIteratorType.FILE_DATA;

				DataIteratorType dataType = DataIteratorType.FILE_DATA;
				String tokenPhraseMakerPatternFileName = "ABILabelPatterns_v18_200513.txt";
				boolean filterTrainingAttributes = false;
				Double minimumFrequency = 0.05;
				String logTokenPhrasesDirectory = "";

				IgpsBuilder igpsBuilder = IgpsBuilder.getBuilder()
						.setArffOutputPath(outputPath.get())
						.setAttributeFilters(getAttributeFilters(
								Arrays.asList("row", "doc", "col"),
								filterTrainingAttributes, minimumFrequency))
						.setGirotraAbiTool(girotraAbiTool)
						.setLogTokenPhrasesDirectory(logTokenPhrasesDirectory)
						.setTextAnalyzer(getTextAnalyzer(
								tokenPhraseMakerPatternFileName))
						.setTextDataIterator(getIterableData(
								trainingTextSourceInfo, iteratorType,
								fileIdLabelHeader, dateLabelHeader, null));

				final Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassInstancesMap = girotraAbiTool
						.getCandidateConceptToPhraseClassToInstancesMap(
								phraseClassToXmlDirectoryMap, PROXIMITIES,
								igpsBuilder.build());
				return conceptToPhraseClassInstancesMap;
			}

			private void combineRelatedAnnotations(final String pathToText,
					final String pathToXml, final String pathToResultFolder,
					final Map<ConceptTypeName, String> typeNameMap) {

				Map<String, List<AnnotatedPhrase>> documentToAnnotationsMap = getDocumentToAnnotationMap(
						pathToText, pathToXml,
						new HashSet<>(typeNameMap.values()));
				Map<String, List<AnnotatedPhrase>> documentToRevisedAnnotationsMap = reviseCorpusAnnotations(
						documentToAnnotationsMap, typeNameMap);
				writeRevisedAnnotations(documentToRevisedAnnotationsMap,
						pathToResultFolder);

			}

			private RaptatPair<Instances, Instances> conformTrainTestPair(
					final RaptatPair<Instances, Instances> instancesPair) {
				Instances trainInstances = instancesPair.left;
				Instances testInstances = instancesPair.right;
				trainInstances.setRelationName(
						"Train_" + trainInstances.relationName());
				testInstances.setRelationName(
						"Test_" + testInstances.relationName());

				List<Instances> trainTestInstancesList = Arrays
						.asList(new Instances[] {
								trainInstances, testInstances
				});
				List<Instances> conformedInstances = InstancesMerger
						.conformInstances(trainTestInstancesList, "Conformed");

				Instances conformedTraining = null;
				Instances conformedTesting = null;
				for (Instances instances : conformedInstances) {
					if ( instances.relationName().toLowerCase()
							.startsWith("train") ) {
						// conformedTraining = trainInstances;
						conformedTraining = instances;
					}
					if ( instances.relationName().toLowerCase()
							.startsWith("test") ) {
						conformedTesting = instances;
					}
				}

				return new RaptatPair<>(conformedTraining, conformedTesting);
			}

			private Map<String, Map<PhraseClass, Classifier>> getAbiClassifierMapping(
					final Map<String, Map<PhraseClass, Instances>> mapToInstancesForTraining,
					String[] optionsArray) throws Exception {

				Map<String, Map<PhraseClass, Classifier>> abiSolution = new HashMap<>();

				for (String concept : mapToInstancesForTraining.keySet()) {
					Map<PhraseClass, Classifier> phraseClassToClassifierMap = abiSolution
							.computeIfAbsent(concept,
									k -> new HashMap<PhraseClass, Classifier>());
					Map<PhraseClass, Instances> phraseToInstancesMap = mapToInstancesForTraining
							.get(concept);
					for (PhraseClass phraseClass : phraseToInstancesMap
							.keySet()) {

						/*
						 * We make a copy of optionsArray here as the setOptions
						 * method below this assigment changes its argument
						 */
						String[] classifierOptions = Arrays.copyOf(optionsArray,
								optionsArray.length);
						FilteredClassifier classifier = new FilteredClassifier();
						classifier.setOptions(classifierOptions);
						System.out.println("Filtered Classifier Options: "
								+ String.join(" ", classifier.getOptions()));

						/*
						 * We make a defensive copy here of the Instances object
						 * pointed to here by phraseClass in case the subsequent
						 * methods modifies it or the Instance objects it
						 * contains
						 */
						Instances conceptAndPhraseClassInstances = phraseToInstancesMap
								.get(phraseClass);
						Instances trainingInstances = new Instances(
								conceptAndPhraseClassInstances);

						System.out.println("Train data attributes:");
						Enumeration<Attribute> attributes = trainingInstances
								.enumerateAttributes();
						while ( attributes.hasMoreElements() ) {
							Attribute attribute = attributes.nextElement();
							System.out.println(
									attribute.index() + ":" + attribute.name());
						}

						System.out.println(
								"\n\nConcept/PhraseClass Instances data attributes:");
						attributes = trainingInstances.enumerateAttributes();
						while ( attributes.hasMoreElements() ) {
							Attribute attribute = attributes.nextElement();
							System.out.println(
									attribute.index() + ":" + attribute.name());
						}
						classifier.buildClassifier(trainingInstances);

						phraseClassToClassifierMap.put(phraseClass, classifier);
					}
				}

				return abiSolution;
			}

			private Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> getActualVsPredicted(
					final Map<String, Map<PhraseClass, RaptatPair<Instances, Instances>>> mapToPairedInstances,
					final String predictionTaggingAttributes) {

				Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> resultMap = new HashMap<>();
				try {
					for (String conceptName : mapToPairedInstances.keySet()) {

						/*
						 * Add mapping for result map if needed
						 */
						Map<PhraseClass, RaptatPair<Instances, Instances>> phraseToTrainTestPairMap = mapToPairedInstances
								.get(conceptName);

						Map<String, Map<String, Triple<String, String, Boolean>>> phraseToAttributeMap = resultMap
								.computeIfAbsent(conceptName,
										k -> new HashMap<>());

						for (PhraseClass phraseClass : phraseToTrainTestPairMap
								.keySet()) {

							/*
							 * Add mapping for conceptToPhraseClassResult if
							 * needed
							 */
							Map<String, Triple<String, String, Boolean>> attributeToTripleMap = phraseToAttributeMap
									.computeIfAbsent(phraseClass.toString(),
											key -> new HashMap<>());

							RaptatPair<Instances, Instances> instancesPair = phraseToTrainTestPairMap
									.get(phraseClass);

							RaptatPair<Instances, Instances> conformedInstancesPair = conformTrainTestPair(
									instancesPair);
							Instances conformedTraining = conformedInstancesPair.left;
							Instances conformedTesting = conformedInstancesPair.right;

							/*
							 * The conformTrainTestPair will make the Instance
							 * objects within each of the Instances objects,
							 * conformedTraining and conformedTesting, have the
							 * same attributes. However, any attributes added to
							 * a given Instance object using this process will
							 * be set a value of 'missing.' We want the value to
							 * be '0' (zero) rather than missing. For our use
							 * case, attributes refer to text around other text
							 * sequences, and those are either present or
							 * absent. When present, it has the value '1', and,
							 * when absent or "missing," it has the value '0.'
							 *
							 * Set any attributes that are missing in the test
							 * set to zero as it means they are absent which may
							 * be informative
							 */
							AttributeHelper.zeroMissingValues(conformedTesting);

							// Block below is for debugging purposes only!!!
							if ( LOGGER.isDebugEnabled() ) {
								String instancesDirectoryName = "P:/ORD_Girotra_201607120D/Glenn/EvaluationFilesForABITool_200513/testOutput";

								String trainingFileName = "conformedTraining_"
										+ GeneralHelper.getTimeStamp()
										+ ".arff";
								File trainingInstancesFile = Path
										.of(instancesDirectoryName,
												trainingFileName)
										.toFile();
								writeInstances(conformedTraining,
										trainingInstancesFile);

								String testingFileName = "conformedTesting_"
										+ GeneralHelper.getTimeStamp()
										+ ".arff";
								File testingInstancesFile = Path
										.of(instancesDirectoryName,
												testingFileName)
										.toFile();
								writeInstances(conformedTesting,
										testingInstancesFile);
							}

							String identifierAttributes = "1-4";
							List<String> filteredClassifierOptions = OptionsBuilder
									.generateFilteredClassifierOptions(
											identifierAttributes);
							FilteredClassifier classifier = new FilteredClassifier();
							String[] optionsArray = filteredClassifierOptions
									.toArray(new String[] {});
							classifier.setOptions(optionsArray);
							System.out.println("Filtered Classifier Options: "
									+ String.join(" ",
											classifier.getOptions()));
							classifier.buildClassifier(conformedTraining);

							Evaluation evaluation = new Evaluation(
									conformedTraining);

							// output collected predictions
							double[] predictions = evaluation.evaluateModel(
									classifier, conformedTesting,
									new Object[0]);
							Enumeration<Instance> instanceEnumerator = conformedTesting
									.enumerateInstances();
							Attribute classAttribute = conformedTesting
									.classAttribute();

							for (int i = 0; i < predictions.length; i++) {

								Instance instance = instanceEnumerator
										.nextElement();
								String attributeValueHash = AttributeHelper
										.getAttributeIdentifierHash(instance);
								String actualClass = instance
										.toString(classAttribute);
								String predictedClass = classAttribute
										.value((int) predictions[i]);
								attributeToTripleMap.put(attributeValueHash,
										ImmutableTriple.of(actualClass,
												predictedClass,
												actualClass.equalsIgnoreCase(
														predictedClass)));

								System.out.println("- attribute values:\n"
										+ attributeValueHash);
								System.out.println("Actual:" + actualClass);
								System.out
										.println("Predicted:" + predictedClass);
							}
						}
					}
				}
				catch (Exception e) {
					System.err
							.println("Unable to run train and test instances\n"
									+ e.getLocalizedMessage());
					e.printStackTrace();
					System.exit(-1);
				}
				return resultMap;
			}

			/**
			 * @return
			 */
			private List<AttributeFilter> getAttributeFilters(
					List<String> excludedNames, final boolean filterForTraining,
					Double minimumFrequency) {
				List<AttributeFilter> attributeFilters = new ArrayList<>();

				/*
				 * Features that contain a string in the excludedNames list will
				 * be removed
				 */
				// List<String> excludedNames = Arrays.asList("distance");
				attributeFilters.add(new AttributeNameFilter(excludedNames));

				/*
				 * If training, features that occur in less than the
				 * minimumFrequency of all features will be removed
				 */
				if ( filterForTraining ) {
					attributeFilters.add(
							new AttributeFrequencyFilter(minimumFrequency));
				}
				return attributeFilters;
			}

			private String getAttributeValue(final AnnotatedPhrase phrase,
					final String attributeName) {
				for (RaptatAttribute attribute : phrase.getPhraseAttributes()) {
					if ( attributeName.equalsIgnoreCase(attribute.getName()) ) {
						return attribute.getValues().get(0);
					}
				}
				return null;
			}

			/**
			 * Get mapping of concepts and PhraseClass objects to Weka
			 * "Instances"
			 *
			 * @param igps
			 *            - An InstancesGetterParameters object containing the
			 *            parameters for getting a mapping from the concept to
			 *            the phrase class to the actual instances
			 *
			 * @return
			 * @throws FileNotFoundException
			 * @throws IOException
			 */
			private Map<String, Map<PhraseClass, Instances>> getCandidateConceptAndPhraseClassToInstancesMap(
					InstancesGetterParameters igps)
					throws FileNotFoundException, IOException {

				/*
				 * The same features will be used to map to different classes,
				 * and the classes will then be merged on any test cases. So,
				 * for example, we will fit both laterality and index type, and
				 * the laterality and index types that refer to the same index
				 * value will be merged. For example, if the index value 0.95 at
				 * the span of (10,14) is assigned an index type of ABI AND a
				 * laterality of right, the final assignment of 0.95 will be
				 * 'indexValue_ABI_right.'
				 *
				 * Note that in the map, phraseClassToXmlMap, the PhraseClass
				 * object keys will map to an empty optional unless we are
				 * training the system.
				 */
				Map<PhraseClass, Optional<String>> phraseClassToXmlDirectoryMap = getPhraseClassToXmlDirectoryMap(
						igps.lateralityDirectory(), igps.indexTypeDirectory());

				final Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassInstancesMap = igps
						.girotraAbiTool()
						.getCandidateConceptToPhraseClassToInstancesMap(
								phraseClassToXmlDirectoryMap, PROXIMITIES,
								igps);
				return conceptToPhraseClassInstancesMap;
			}

			private Map<String, List<AnnotatedPhrase>> getDocumentToAnnotationMap(
					final String textDirectoryPath,
					final String xmlDirectoryPath,
					final Set<String> acceptedConcepts) {

				AnnotationImporter annotationImporter = AnnotationImporter
						.getImporter(AnnotationApp.EHOST);
				List<RaptatPair<File, File>> txtXmlPairs = GeneralHelper
						.getXmlFilesAndMatchTxt(textDirectoryPath,
								xmlDirectoryPath);
				Map<String, List<AnnotatedPhrase>> resultMap = new HashMap<>(
						txtXmlPairs.size());

				for (RaptatPair<File, File> txtXmlPair : txtXmlPairs) {
					List<AnnotatedPhrase> annotations = annotationImporter
							.importAnnotations(
									txtXmlPair.right.getAbsolutePath(),
									txtXmlPair.left.getAbsolutePath(),
									acceptedConcepts);
					resultMap.put(txtXmlPair.left.getAbsolutePath(),
							annotations);
				}
				return resultMap;
			}

			private IterableData getIterableData(String directoryPathString,
					DataIteratorType dataIteratorType, String fileIdLabelHeader,
					String dateLabelHeader, String[] dataIteratorArgs) {
				IterableData iterableData = null;
				try {
					switch (dataIteratorType) {
					case FILE_DATA: {
						String[] args = new String[IterableDirectoryFile.IterableDirectoryFileArgs
								.values().length];
						iterableData = new IterableDirectoryFile(
								dataIteratorArgs);
						break;
					}
					case SQL_DATA: {
						iterableData = new SqlDataIterator("", "", "", 0);
						break;
					}
					/*
					 * Nev
					 */
					case TAB_DELIMITED_DATA: {
						iterableData = new TabDelimitedIterator();
					}
					default:
						throw new IllegalArgumentException(
								"Unexpected value: " + dataIteratorType);
					}
				}
				catch (Exception e) {
					System.err.println(
							"Unable to create iterator for processing data");
					e.printStackTrace();
				}
				return iterableData;
			}

			private Map<String, Map<PhraseClass, Attribute>> getMapToPhraseClassAttributes(
					Map<String, Map<PhraseClass, Instances>> mapToInstancesForTraining) {
				Map<String, Map<PhraseClass, Attribute>> mapToClassAttributes = new HashMap<String, Map<PhraseClass, Attribute>>();
				for (String conceptName : mapToInstancesForTraining.keySet()) {
					Map<PhraseClass, Attribute> phraseToAttributeMap = mapToClassAttributes
							.computeIfAbsent(conceptName,
									k -> new HashMap<PhraseClass, Attribute>());
					Map<PhraseClass, Instances> phraseToInstancesMap = mapToInstancesForTraining
							.get(conceptName);
					for (PhraseClass phraseClass : phraseToInstancesMap
							.keySet()) {
						Instances instances = phraseToInstancesMap
								.get(phraseClass);
						phraseToAttributeMap.put(phraseClass,
								instances.classAttribute());
					}
				}
				return mapToClassAttributes;
			}

			/**
			 * Gets all the attributes (except strings) within the Instances
			 * object used to train a classifier
			 */
			private Map<String, Map<PhraseClass, List<Attribute>>> getMapToTrainingAttributes(
					Map<String, Map<PhraseClass, Instances>> mapToInstancesForTraining) {
				Map<String, Map<PhraseClass, List<Attribute>>> mapToTrainingAttributes = new HashMap<String, Map<PhraseClass, List<Attribute>>>();
				for (String conceptName : mapToInstancesForTraining.keySet()) {
					Map<PhraseClass, List<Attribute>> phraseToAttributeMap = mapToTrainingAttributes
							.computeIfAbsent(conceptName,
									k -> new HashMap<PhraseClass, List<Attribute>>());
					Map<PhraseClass, Instances> phraseToInstancesMap = mapToInstancesForTraining
							.get(conceptName);
					for (PhraseClass phraseClass : phraseToInstancesMap
							.keySet()) {
						List<Attribute> attributeList = phraseToAttributeMap
								.computeIfAbsent(phraseClass,
										k -> new ArrayList<Attribute>());
						Instances instances = phraseToInstancesMap
								.get(phraseClass);
						Enumeration<Attribute> attributeEnumerator = instances
								.enumerateAttributes();
						while ( attributeEnumerator.hasMoreElements() ) {
							Attribute attribute = attributeEnumerator
									.nextElement();
							// if ( !attribute.isString() ) {
							attributeList.add(attribute);
							// }
						}
					}
				}
				return mapToTrainingAttributes;
			}

			/**
			 * Creates simple map for ABI project for learning to extract
			 * laterality and index type of index values. Thye mapping is from a
			 * PhraseClass type to full path (as a string) to the directory
			 * storing xml files from eHOST, and the xml files contain
			 * annotations of index values with either laterality or index type.
			 *
			 * @param lateralityXmlMapping
			 * @param indexTypeXmlMapping
			 * @return
			 */
			private Map<PhraseClass, Optional<String>> getPhraseClassToXmlDirectoryMap(
					final Optional<String> lateralityXmlMapping,
					final Optional<String> indexTypeXmlMapping) {
				Map<PhraseClass, Optional<String>> phraseClassToXmlMap = new HashMap<>();

				/*
				 * The laterality and indextype mappings are paths to where the
				 * xml for these phrase class types are stored
				 */
				phraseClassToXmlMap.put(PhraseClass.INDEX_VALUE_LATERALITY,
						lateralityXmlMapping);
				phraseClassToXmlMap.put(PhraseClass.INDEX_VALUE_TYPE,
						indexTypeXmlMapping);
				return phraseClassToXmlMap;
			}

			/**
			 * Returns the first AnnotatedPhrase instance found in a relation
			 * called targetedRelationName within the AnnotatedPhrase, phrase,
			 * supplied as a parameter. The found phrase must have an id within
			 * the mentionIdToPhraseMap parameter. Returns null if nothing
			 * found.
			 *
			 * @param phrase
			 * @param targetedRelationName
			 * @param mentionIdToPhraseMap
			 * @return
			 */
			private AnnotatedPhrase getRelatedConcept(
					final AnnotatedPhrase phrase,
					final String targetedRelationName,
					final Map<String, AnnotatedPhrase> mentionIdToPhraseMap) {
				for (ConceptRelation conceptRelation : phrase
						.getConceptRelations()) {
					if ( conceptRelation.getConceptRelationName()
							.equalsIgnoreCase(targetedRelationName) ) {
						String mentionId = conceptRelation
								.getRelatedAnnotationID().toLowerCase();
						if ( mentionIdToPhraseMap.containsKey(mentionId) ) {
							return mentionIdToPhraseMap.get(mentionId);
						}
					}
				}
				return null;
			}

			/**
			 * Creates and returns a mapping of annotated phrases that we are
			 * targeting (selected from the annotatedPhrases parameter) to the
			 * mentionIDs of the concepts for which the targeted phrase has a
			 * conceptRelation. Only mentionIDs with a concept name
			 * corresponding to the first and second concept names mapped to by
			 * the typeNameMap parameter are returned.
			 *
			 * @param documentAnnotations
			 * @param typeNameMap
			 *
			 * @return
			 */
			private Map<AnnotatedPhrase, MutablePair<String, String>> getTargetToRelatedConceptNamesMap(
					final List<AnnotatedPhrase> documentAnnotations,
					final Map<ConceptTypeName, String> typeNameMap) {
				/*
				 * Create a map to build a new set of annotations from for this
				 * document
				 */
				Map<AnnotatedPhrase, MutablePair<String, String>> targetToRelatedConceptMapping = new HashMap<>();

				String targetConceptName = typeNameMap
						.get(ConceptTypeName.TARGET_NAME);
				String firstConceptName = typeNameMap
						.get(ConceptTypeName.FIRST_RELATED_CONCEPT_NAME);
				String firstRelationName = typeNameMap.get(
						ConceptTypeName.FIRST_CONCEPT_TARGET_RELATION_NAME);
				String firstAttributeName = typeNameMap.get(
						ConceptTypeName.FIRST_CONCEPT_TARGET_ATTRIBUTE_NAME);

				String secondConceptName = typeNameMap
						.get(ConceptTypeName.SECOND_RELATED_CONCEPT_NAME);
				String secondRelationName = typeNameMap.get(
						ConceptTypeName.SECOND_CONCEPT_TARGET_RELATION_NAME);
				String secondAttributeName = typeNameMap.get(
						ConceptTypeName.SECOND_CONCEPT_TARGET_ATTRIBUTE_NAME);

				/*
				 * Create a map that we will use to find AnnotatedPhrase
				 * instances stored in ConceptRelations for another phrase
				 */
				Map<String, AnnotatedPhrase> idToPhraseMap = new HashMap<>();
				documentAnnotations.forEach(phrase -> idToPhraseMap
						.put(phrase.getMentionId().toLowerCase(), phrase));

				for (AnnotatedPhrase documentAnnotation : documentAnnotations) {
					String conceptName = documentAnnotation.getConceptName();

					if ( conceptName.equalsIgnoreCase(targetConceptName) ) {
						targetToRelatedConceptMapping.putIfAbsent(
								documentAnnotation, new MutablePair<>());
					}

					else if ( conceptName.equalsIgnoreCase(firstConceptName) ) {
						AnnotatedPhrase targetPhrase = getRelatedConcept(
								documentAnnotation, firstRelationName,
								idToPhraseMap);
						if ( targetPhrase != null ) {
							MutablePair<String, String> attributeValuePair = targetToRelatedConceptMapping
									.computeIfAbsent(targetPhrase,
											key -> new MutablePair<>());
							attributeValuePair.left = getAttributeValue(
									documentAnnotation, firstAttributeName);
						}

					}

					else if ( conceptName
							.equalsIgnoreCase(secondConceptName) ) {
						AnnotatedPhrase targetPhrase = getRelatedConcept(
								documentAnnotation, secondRelationName,
								idToPhraseMap);
						if ( targetPhrase != null ) {
							MutablePair<String, String> attributeValuePair = targetToRelatedConceptMapping
									.computeIfAbsent(targetPhrase,
											key -> new MutablePair<>());
							attributeValuePair.right = getAttributeValue(
									documentAnnotation, secondAttributeName);
						}
					}
				}
				return targetToRelatedConceptMapping;
			}

			/**
			 * @param tokenPhraseMakerFileName
			 * @return
			 */
			private TextAnalyzer getTextAnalyzer(
					final String tokenPhraseMakerFileName) {
				OptionsManager.getInstance().setRemoveStopWords(false);
				final TextAnalyzer textAnalyzer = new TextAnalyzer();
				textAnalyzer.setTokenProcessingOptions(
						OptionsManager.getInstance());
				textAnalyzer.createTokenPhraseMaker(tokenPhraseMakerFileName);
				return textAnalyzer;
			}

			/**
			 * Pairs training and test Instances so they can be paired together
			 * for subsequent training and testing.
			 *
			 * @param mapToInstancesForTraining
			 * @param mapToInstancesForTesting
			 * @return
			 */
			private Map<String, Map<PhraseClass, RaptatPair<Instances, Instances>>> pairTrainTestInstances(
					final Map<String, Map<PhraseClass, Instances>> mapToInstancesForTraining,
					final Map<String, Map<PhraseClass, Instances>> mapToInstancesForTesting) {

				Map<String, Map<PhraseClass, RaptatPair<Instances, Instances>>> resultMap = new HashMap<>();
				for (String conceptName : mapToInstancesForTraining.keySet()) {

					Map<PhraseClass, Instances> phraseToInstancesForTraining = mapToInstancesForTraining
							.get(conceptName);
					Map<PhraseClass, Instances> phraseToInstancesForTesting = mapToInstancesForTesting
							.get(conceptName);

					for (PhraseClass phraseClass : phraseToInstancesForTraining
							.keySet()) {

						Instances trainingInstances = phraseToInstancesForTraining
								.get(phraseClass);
						Instances testingInstances = phraseToInstancesForTesting
								.get(phraseClass);
						RaptatPair<Instances, Instances> instancesPair = new RaptatPair<>(
								trainingInstances, testingInstances);

						Map<PhraseClass, RaptatPair<Instances, Instances>> pairedPhraseToPairsMap = resultMap
								.computeIfAbsent(conceptName,
										k -> new HashMap<>());
						pairedPhraseToPairsMap.put(phraseClass, instancesPair);

					}

				}
				return resultMap;
			}

			private void promoteRelationsToConcepts(
					final String textDirectoryPath,
					final String xmlDirectoryPath,
					final String outputDirectoryPath,
					final String targetConceptName,
					final Map<String, String> relationshipToAttributeNames,
					final AnnotationMutator annotationMutator) {

				boolean correctOffsets = true;
				boolean insertPhraseStrings = true;
				boolean overwriteContents = true;
				XMLExporterRevised exporter = new XMLExporterRevised(
						AnnotationApp.EHOST, new File(outputDirectoryPath),
						overwriteContents);
				AnnotationImporter annotationImporter = AnnotationImporter
						.getImporter(AnnotationApp.EHOST);

				List<RaptatPair<File, File>> txtXmlPairs = GeneralHelper
						.getXmlFilesAndMatchTxt(textDirectoryPath,
								xmlDirectoryPath);
				for (RaptatPair<File, File> txtXmlPair : txtXmlPairs) {
					List<AnnotatedPhrase> annotations = annotationImporter
							.importAnnotations(
									txtXmlPair.right.getAbsolutePath(),
									txtXmlPair.left.getAbsolutePath(), null);
					List<AnnotatedPhrase> revisedAnnotations = annotationMutator
							.promoteRelationshipValuesToConcepts(annotations,
									targetConceptName,
									relationshipToAttributeNames);
					RaptatDocument document = new RaptatDocument();
					document.setTextSourcePath(
							Optional.of(txtXmlPair.left.getAbsolutePath()));
					AnnotationGroup annotationGroup = new AnnotationGroup(
							document, revisedAnnotations);

					exporter.exportReferenceAnnotationGroup(annotationGroup,
							correctOffsets, insertPhraseStrings);
				}

			}

			private void remapConceptNames(final String textPath,
					final String inputXmlPath, final String outputDirectoryPath,
					final Map<String, String> remap) {
				Map<String, List<AnnotatedPhrase>> documentToAnnotationMap = getDocumentToAnnotationMap(
						textPath, inputXmlPath, new HashSet<>(remap.keySet()));

				for (List<AnnotatedPhrase> documentAnnotations : documentToAnnotationMap
						.values()) {
					for (AnnotatedPhrase annotatedPhrase : documentAnnotations) {
						String conceptName = annotatedPhrase.getConceptName();
						String remappedName = remap.get(conceptName);
						if ( remappedName == null ) {
							remappedName = conceptName;
						}
						annotatedPhrase.setConceptName(remappedName);
					}
				}

				writeRevisedAnnotations(documentToAnnotationMap,
						outputDirectoryPath);
			}

			/**
			 * Revises annotations so that index value annotations are combined
			 * with their relationships of index value type and laterality to
			 * create a single annotation. So, an annotation of the number 1.23
			 * (for index value), that is also linked to an annotation for type
			 * abi and laterality right would be converted to the annotation
			 * with concept name indexvalue_right_abi.
			 *
			 * @param documentToAnnotationsMap
			 * @param typeNameMap
			 * @return
			 */
			private Map<String, List<AnnotatedPhrase>> reviseCorpusAnnotations(
					final Map<String, List<AnnotatedPhrase>> documentToAnnotationsMap,
					final Map<ConceptTypeName, String> typeNameMap) {

				Map<String, List<AnnotatedPhrase>> revisedCorpusAnnotations = new HashMap<>(
						documentToAnnotationsMap.size());

				for (String documentPath : documentToAnnotationsMap.keySet()) {

					List<AnnotatedPhrase> revisedDocumentAnnotationList = reviseDocumentAnnotations(
							documentToAnnotationsMap.get(documentPath),
							typeNameMap);
					revisedCorpusAnnotations.put(documentPath,
							revisedDocumentAnnotationList);
				}

				return revisedCorpusAnnotations;
			}

			/**
			 * Takes a collection of annotated phrases and combines them to form
			 * a new set combining index values with their type and laterality
			 * in to a single concept name
			 *
			 * @param documentAnnotations
			 * @param conceptTypeToNameMap
			 * @return
			 */
			private List<AnnotatedPhrase> reviseDocumentAnnotations(
					final List<AnnotatedPhrase> documentAnnotations,
					final Map<ConceptTypeName, String> conceptTypeToNameMap) {

				Map<AnnotatedPhrase, MutablePair<String, String>> targetToRelatedConceptMap = getTargetToRelatedConceptNamesMap(
						documentAnnotations, conceptTypeToNameMap);

				List<AnnotatedPhrase> revisedDocumentAnnotations = new ArrayList<>();
				for (AnnotatedPhrase targetPhrase : targetToRelatedConceptMap
						.keySet()) {
					MutablePair<String, String> relatedConcepts = targetToRelatedConceptMap
							.get(targetPhrase);
					AnnotatedPhrase revisedAnnotation = new AnnotatedPhrase(
							targetPhrase);
					String revisedConceptName = conceptTypeToNameMap
							.get(ConceptTypeName.TARGET_NAME) + "_"
							+ relatedConcepts.left + "_"
							+ relatedConcepts.right;
					revisedAnnotation.setConceptName(revisedConceptName);
					revisedDocumentAnnotations.add(revisedAnnotation);
				}
				return revisedDocumentAnnotations;
			}

			private RunType selectRunType() {
				RunType[] runTypes = RunType.values();

				// Show the dialog box with a dropdown list (combo box)
				mainFrame.setAlwaysOnTop(true);
				mainFrame.toFront();
				RunType selectedRunType = (RunType) JOptionPane.showInputDialog(
						null, "Selection process to run:",
						"Run Process Selection", JOptionPane.QUESTION_MESSAGE,
						null, runTypes, runTypes[0]);
				mainFrame.setAlwaysOnTop(false);

				// Check if the user selected a value
				if ( selectedRunType != null ) {
					// User made a selection
					System.out.println("Process selected:" + selectedRunType);
					// Return the selected value or use it further in your
					// application
				}
				else {
					// User canceled or closed the dialog box
					System.out.println("No selection made.");
					selectedRunType = RunType.CANCEL_PROCESS;
				}

				return selectedRunType;

			}

			private void writeRevisedAnnotations(
					final Map<String, List<AnnotatedPhrase>> documentToRevisedAnnotationsMap,
					final String pathToResultFolder) {

				File resultDirectory = new File(pathToResultFolder);
				if ( !resultDirectory.exists() ) {
					try {
						FileUtils.forceMkdir(resultDirectory);
					}
					catch (IOException e) {
						System.err.println("Unable to create directory:"
								+ pathToResultFolder + "\n" + e.getMessage());
						e.printStackTrace();
						System.exit(-1);
					}
				}
				/*
				 * Get the first document path to determine where to put the
				 * annotations
				 */
				Set<String> documentPathSet = documentToRevisedAnnotationsMap
						.keySet();
				if ( documentPathSet.isEmpty() ) {
					return;
				}

				boolean overwriteContents = true;
				XMLExporterRevised exporter = new XMLExporterRevised(
						AnnotationApp.EHOST, new File(pathToResultFolder),
						overwriteContents);

				for (String documentPath : documentPathSet) {
					RaptatDocument document = new RaptatDocument();
					document.setTextSourcePath(Optional.of(documentPath));
					AnnotationGroup annotationGroup = new AnnotationGroup(
							document,
							documentToRevisedAnnotationsMap.get(documentPath));

					boolean correctOffsets = true;
					boolean insertPhraseStrings = true;
					exporter.exportReferenceAnnotationGroup(annotationGroup,
							correctOffsets, insertPhraseStrings);
				}

			}

			private void writeWekaInstances(final String outputDirectory,
					final Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassInstances) {
				writeWekaInstances(outputDirectory,
						conceptToPhraseClassInstances, "");
			}

			private void writeWekaInstances(final String outputDirectory,
					final Map<String, Map<PhraseClass, Instances>> conceptToPhraseClassInstances,
					String tag) {

				for (String conceptName : conceptToPhraseClassInstances
						.keySet()) {
					Map<PhraseClass, Instances> phraseClassToInstancesMap = conceptToPhraseClassInstances
							.get(conceptName);
					for (PhraseClass phraseClass : phraseClassToInstancesMap
							.keySet()) {

						String filename = tag + "_" + conceptName + "_"
								+ phraseClass.toString() + "_"
								+ GeneralHelper.getTimeStamp() + ".txt";
						Instances instances = phraseClassToInstancesMap
								.get(phraseClass);
						WekaInstancesWriter instancesWriter = new WekaInstancesWriter(
								outputDirectory, filename, instances);
						instancesWriter.writeInstancesToFile();
					}
				}

			}
		});
	}

	protected static void writeInstances(Instances data, File instancesFile) {
		try (PrintWriter pw = new PrintWriter(instancesFile)) {
			pw.println(new Instances(data, 0));
			for (int i = 0; i < data.numInstances(); i++) {
				pw.println(data.instance(i).toStringMaxDecimalDigits(2));
			}
			pw.flush();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Unable to print instances to file:"
					+ instancesFile.getAbsolutePath());
			System.err.println(e.getLocalizedMessage());
		}
	}
}
