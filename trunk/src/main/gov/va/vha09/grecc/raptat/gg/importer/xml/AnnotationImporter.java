package src.main.gov.va.vha09.grecc.raptat.gg.importer.xml;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.builder.Input.Builder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import opennlp.tools.util.Span;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.ButtonQueryDialog;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OffsetParser;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation.AnnotationMutator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * Adds functionality to XMLImporter by changing the offsets present in Knowtator XML so they match
 * those determined by Open NLP, and adding methods to check that phrases present in Knowtator XML
 * are present and at the expected locations within the document
 *
 * @author Glenn Gobbel - May 2, 2012
 */
/** @author Glenn T. Gobbel Mar 28, 2016 */
public class AnnotationImporter {

	private enum RunType {
		COMBINE_ATTRIBUTES_TO_CONCEPTS_FOR_ABI, PRINT_PHRASES_TO_CONSOLE, PROMOTE_VALUES_TO_CONCEPTS, REMOVE_ATTRIBUTE, TEST_EQUALS_FOR_XML, XML_FILES_TO_TAB_DELIMITED;
	}

	private IndexedTree<IndexedObject<Integer>> offsetComparer;

	private List<AnnotatedPhrase> unmatchedPhrases;

	protected Document doc = null;

	protected List<AnnotatedPhrase> annotatedPhrases;

	/** Lookup map for the attributes of the annotated phrases */
	protected HashMap<String, RaptatAttribute> stringSlotToAttributeMap = new HashMap<>();

	/**
	 * Lookup map for the concepts of the annotated phrases. It maps the
	 * "mention id" to a annotated concept which consists of the name of the
	 * concept and the attributes associated with that concept
	 */
	protected HashMap<String, AnnotatedConcept> annotationIdToConceptMap = new HashMap<>();

	/**
	 * Lookup map that maps complexSlotMentionID to the ConceptRelation, which
	 * contains the name of the relation and IDs of linked annotations
	 */
	protected HashMap<String, ConceptRelation> complexSlotToRelationMap = new HashMap<>();

	protected int unmatchedAnnotationNumber = 0;

	protected int matchedAnnotationNumber = 0;

	/* Set eHOST as the default program used for generating annotations */
	private AnnotationApp annotationSource = AnnotationApp.EHOST;

	/*
	 * Keeps track of all annotations via ID that are linked TO from another
	 * annotation. This field is used to determine if an annotation that is
	 * unmatched during import needs to be removed from a relationship.
	 */
	private HashSet<String> linkedToAnnotations = new HashSet<>();

	/*
	 * Determines whether the annotation is validated by comparison to schema
	 */
	private boolean reconcileAnnotationsWithSchema = true;

	/*
	 * Determines whether the annotation is compared to text and the offsets
	 * appropriately modified to comport with text offsets. Note that this flag
	 * setting interacts with the 'removeUnmatched' flag setting.
	 */
	private boolean reconcileAnnotationsWithText = true;

	/*
	 * This is a flag to remove any annotations from a list where the offsets do
	 * not match the text within the annotation. Note that this will be affected
	 * by the setting of 'reconcileAnnotationsWithText.' If
	 * 'reconcileAnnotationsWithText' is false, then 'removeUnmatched' will not
	 * cause removal of unmatched annotations as this process is dependent upon
	 * calling of the reconcileAnnotationsWithText method, which is dependent on
	 * the 'reconcileAnnotationsWithText' flag.
	 */
	private boolean removeUnmatched = true;

	protected static Logger logger = Logger.getLogger(AnnotationImporter.class);

	public AnnotationImporter(final AnnotationApp annotator) {
		this();
		this.annotationSource = annotator;
	}

	private AnnotationImporter() {
		AnnotationImporter.logger.setLevel(Level.INFO);
	}

	/**
	 * Compares the list of annotated phrases extracted from an xml document to
	 * the tokens within a document to make sure that the xml phrases can be
	 * identified within the text.
	 *
	 * <p>
	 * Preconditions: The document has been parsed and the document tokens have
	 * all been retrieved Postconditions: The list of annotated phrases is
	 * changed so that any phrases not found in the document are excluded and
	 * the offsets for the whole phrase and its tokens are set so they match the
	 * exact offsets for the tokens in the document.
	 *
	 * @param textDocumentPath
	 * @return List < AnnotatedPhrase > - annotated phrases in the XML that are
	 *         not found within the document
	 * @author Glenn Gobbel - Apr 25, 2012
	 */
	public List<AnnotatedPhrase> conformXMLOffsetsToText(Path textDocumentPath,
			final AnnotationApp annotationSource) {
		this.offsetComparer = OffsetParser.calculateOffsetCorrectionsNew(
				textDocumentPath, annotationSource);
		correctXMLOffsets();

		List<AnnotatedPhrase> resultList;

		if ( this.removeUnmatched ) {
			resultList = removeUnmatchedXMLPhrases(textDocumentPath);
		}
		else {
			resultList = this.annotatedPhrases;
		}

		return resultList;
	}

	/**
	 * Compares the list of annotated phrases extracted from an xml document to
	 * the tokens within a document to make sure that the xml phrases can be
	 * identified within the text.
	 *
	 * <p>
	 * Preconditions: The document has been parsed and the document tokens have
	 * all been retrieved Postconditions: The list of annotated phrases is
	 * changed so that any phrases not found in the document are excluded and
	 * the offsets for the whole phrase and its tokens are set so they match the
	 * exact offsets for the tokens in the document.
	 *
	 * @param documentPathString
	 * @return List < AnnotatedPhrase > - annotated phrases in the XML that are
	 *         not found within the document
	 * @author Glenn Gobbel - Apr 25, 2012
	 */
	public List<AnnotatedPhrase> conformXMLOffsetsToText(
			String documentPathString, final AnnotationApp annotationSource) {
		String urlStartString = "file:";
		if ( documentPathString.startsWith(urlStartString) ) {
			documentPathString = documentPathString
					.substring(urlStartString.length());
		}

		return conformXMLOffsetsToText(Path.of(documentPathString),
				annotationSource);
	}

	public List<AnnotatedPhrase> conformXMLOffsetsToText(String pathToDocument,
			final List<AnnotatedPhrase> annotatedPhrases) {
		this.annotatedPhrases = annotatedPhrases;
		String urlStartString = "file:";
		if ( pathToDocument.startsWith(urlStartString) ) {
			pathToDocument = pathToDocument.substring(urlStartString.length());
		}
		this.offsetComparer = OffsetParser.calculateOffsetCorrectionsNew(
				pathToDocument, this.annotationSource);
		correctXMLOffsets();

		List<AnnotatedPhrase> resultList;

		if ( this.removeUnmatched ) {
			resultList = removeUnmatchedXMLPhrases(pathToDocument);
		}
		else {
			resultList = this.annotatedPhrases;
		}

		return resultList;
	}

	public Document domXML(final File file)
			throws SAXException, IOException, ParserConfigurationException {
		return domXML(file.toPath());
	}

	public Document domXML(final Path path)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory documentfactory = DocumentBuilderFactory
				.newInstance();
		documentfactory.setIgnoringElementContentWhitespace(true);
		InputStream bis = new BufferedInputStream(
				Files.newInputStream(path, StandardOpenOption.READ));
		return documentfactory.newDocumentBuilder().parse(bis);
	}

	/**
	 * Method to return a list of AnnotatedPhrase objects containing the tag
	 * info for every annotation in the XML.
	 *
	 * @author Developed by Shrimalini Jayaramaraja on March 8th, 2011
	 * @return
	 */
	public List<AnnotatedPhrase> getAllAnnotatedPhrases() {
		return this.annotatedPhrases;
	}

	/**
	 * @param xmlFile
	 * @param txtFile
	 * @param ta
	 * @param integrator
	 * @return List<AnnotatedPhrase
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public List<AnnotatedPhrase> getAnnotatedPhrases(final File xmlFile,
			final File txtFile, final TextAnalyzer ta,
			final PhraseTokenIntegrator integrator)
			throws SAXException, IOException, ParserConfigurationException {

		String textFile = JdomXMLImporter.getTextSource(xmlFile);

		/* Import the annotations */
		List<AnnotatedPhrase> thePhrases = new ArrayList<>();
		if ( (txtFile == null) || !txtFile.getName().equals(textFile) ) {
			this.doc = domXML(xmlFile);
			thePhrases = this.parseXMLEHostVersion(null);
		}

		/*
		 * Use the txtFile file to correct offsets via importAnnotations if
		 * txtFile is valid
		 */
		else {
			thePhrases = this.importAnnotations(xmlFile.getAbsolutePath(),
					txtFile.getAbsolutePath(), null);

			/*
			 * If we have a valid TextAnalyzer and PhraseTokenIntegrator
			 * instances, add tokens to annotations so we can get the sentences
			 * associated with annotations
			 */
			if ( (ta != null) && (integrator != null) ) {
				RaptatDocument raptatDocument = ta
						.processDocument(txtFile.getAbsolutePath());
				integrator.addProcessedTokensToAnnotations(thePhrases,
						raptatDocument, true, 0);
			}
		}
		return thePhrases;
	}

	public Document getDoc() {
		return this.doc;
	}

	public int getMatchedAnnotationNo() {
		return this.matchedAnnotationNumber;
	}

	public int getUnmatchedAnnotationNo() {
		return this.unmatchedAnnotationNumber;
	}

	public List<AnnotatedPhrase> getUnmatchedPhrases() {
		return this.unmatchedPhrases;
	}

	/**
	 * Import annotations from XML file generated by eHOST
	 *
	 * @param xmlPath
	 * @param textSourcePath
	 * @param acceptedConcepts
	 * @return
	 * @author Glenn Gobbel - Sep 11, 2012
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public List<AnnotatedPhrase> importAnnotations(final Path xmlPath,
			final Path textSourcePath, final Set<String> acceptedConcepts) {
		try {
			File xmlFile = xmlPath.toFile();
			this.doc = domXML(xmlFile);

			/*
			 * Modified on August 18, 2014 to incorporate Sanjib's changes that
			 * allow for multiple attributes with values and spans
			 */
			// annotatedPhrases = parseXML( acceptedConcepts );
			this.annotatedPhrases = this.parseXMLEHostVersion(acceptedConcepts);
		}
		catch (SAXException | IOException | ParserConfigurationException PCe) {
			PCe.printStackTrace();
		}

		List<AnnotatedPhrase> unmatchedPhraseList = this
				.conformXMLOffsetsToText(textSourcePath, this.annotationSource);

		if ( unmatchedPhraseList.size() > 0 ) {
			/* Create error file */
			File xmlFile = xmlPath.toFile();
			String errorFilePath = xmlFile.getParent() + "_ImportErrors_"
					+ xmlFile.getName() + ".txt";
			File errorFile = new File(errorFilePath);
			try {
				String errorString = unmatchedPhraseList.size()
						+ " unmatched, annotations for document "
						+ textSourcePath;
				PrintWriter pw = new PrintWriter(errorFile);
				pw.println(errorString);
				System.err.println(errorString);
				for (AnnotatedPhrase curPhrase : unmatchedPhraseList) {
					System.err.println(curPhrase);
					pw.println(curPhrase);
				}
				if ( AnnotationImporter.logger.isDebugEnabled() ) {
					GeneralHelper.errorWriter("Check unmatched annotations");
				}
				pw.close();
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("All annotations matched");
		}

		return this.annotatedPhrases;
	}

	/**
	 * Import annotations from XML file generated by eHOST
	 *
	 * @param xmlPathString
	 * @param textSourcePath
	 * @param acceptedConcepts
	 * @return
	 * @author Glenn Gobbel - Sep 11, 2012
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public List<AnnotatedPhrase> importAnnotations(final String xmlPathString,
			final Path textSourcePath, final Set<String> acceptedConcepts) {
		return importAnnotations(xmlPathString, textSourcePath,
				acceptedConcepts, true);
	}

	/**
	 * Import annotations from XML file generated by eHOST
	 *
	 * @param xmlPathString
	 * @param textSourcePath
	 * @param acceptedConcepts
	 * @return
	 * @author Glenn Gobbel - Sep 11, 2012
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public List<AnnotatedPhrase> importAnnotations(final String xmlPathString,
			final Path textSourcePath, final Set<String> acceptedConcepts,
			final boolean reconcileWithText) {
		try {
			File xmlFile = new File(xmlPathString);
			this.doc = domXML(xmlFile);

			/*
			 * Modified on August 18, 2014 to incorporate Sanjib's changes that
			 * allow for multiple attributes with values and spans
			 */
			// annotatedPhrases = parseXML( acceptedConcepts );
			this.annotatedPhrases = this.parseXMLEHostVersion(acceptedConcepts);

			if ( logger.isDebugEnabled() ) {
				logger.debug("Importing annotations from:\n\t" + xmlPathString);
				logger.debug("PHRASES:");
				int phraseIndex = 1;
				for (AnnotatedPhrase annotatedPhrase : this.annotatedPhrases) {
					logger.debug(
							"\t\t" + phraseIndex++ + ") " + annotatedPhrase);
				}
			}
		}
		catch (SAXException | IOException | ParserConfigurationException PCe) {
			PCe.printStackTrace();
		}

		if ( reconcileWithText ) {
			List<AnnotatedPhrase> unmatchedPhraseList = this
					.conformXMLOffsetsToText(textSourcePath,
							this.annotationSource);
			if ( unmatchedPhraseList.size() > 0 ) {
				/* Create error file */
				File xmlFile = new File(xmlPathString);
				String errorFilePath = xmlFile.getParent() + "_ImportErrors_"
						+ xmlFile.getName() + ".txt";
				File errorFile = new File(errorFilePath);
				try {
					String errorString = unmatchedPhraseList.size()
							+ " unmatched, annotations for document "
							+ textSourcePath;
					PrintWriter pw = new PrintWriter(errorFile);
					pw.println(errorString);
					System.err.println(errorString);
					for (AnnotatedPhrase curPhrase : unmatchedPhraseList) {
						System.err.println(curPhrase);
						pw.println(curPhrase);
					}
					if ( AnnotationImporter.logger.isDebugEnabled() ) {
						GeneralHelper
								.errorWriter("Check unmatched annotations");
					}
					pw.close();
				}
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				System.out.println("All annotations matched");
			}
		}
		return this.annotatedPhrases;
	}

	/**
	 * Import annotations generated by eHOST and stored as XML
	 *
	 * @param xmlPath
	 * @param textSourcePath
	 * @param acceptedConcepts
	 * @param schemaConceptList
	 * @return
	 */
	public List<AnnotatedPhrase> importAnnotations(final String xmlPath,
			final String textSourcePath, final HashSet<String> acceptedConcepts,
			final List<SchemaConcept> schemaConceptList) {
		return this.importAnnotations(xmlPath, textSourcePath, acceptedConcepts,
				schemaConceptList, null);
	}

	public List<AnnotatedPhrase> importAnnotations(final String xmlPath,
			final String textSourcePath, final HashSet<String> acceptedConcepts,
			final List<SchemaConcept> schemaConceptList, final PrintWriter pw,
			final boolean xmlContainsAdjudications,
			final boolean reconcileWithSchema,
			final boolean reconcileWithText) {
		try {
			File xmlFile = new File(xmlPath);
			this.doc = domXML(xmlFile);
			this.annotatedPhrases = this.parseXMLEHostVersion(acceptedConcepts,
					xmlContainsAdjudications);
			this.matchedAnnotationNumber = this.annotatedPhrases.size();
		}
		catch (SAXException | IOException | ParserConfigurationException PCe) {
			PCe.printStackTrace();
		}

		HashMap<String, SchemaConcept> schemaConceptMap = new HashMap<>();
		for (SchemaConcept schemaConcept : schemaConceptList) {
			if ( !schemaConcept.isRelationship() ) {
				schemaConceptMap.put(schemaConcept.getConceptName(),
						schemaConcept);
			}
		}

		if ( reconcileWithSchema ) {
			this.reconcileAnnotationsWithSchema(schemaConceptMap);
		}
		if ( reconcileWithText ) {
			this.reconcileAnnotationsWithText(textSourcePath, pw);
		}

		return this.annotatedPhrases;
	}

	/**
	 * Primary method for importing annotations from a Knowtator - generated XML
	 * file. Supplied with the Knowtator schema. The imported XML is conformed
	 * with the schema.
	 *
	 * @param xmlPath
	 * @param textSourcePath
	 * @param acceptedConcepts
	 * @param schemaPath
	 * @return
	 * @author Glenn Gobbel - Sep 11, 2012 5
	 */
	public List<AnnotatedPhrase> importAnnotations(final String xmlPath,
			final String textSourcePath, final HashSet<String> acceptedConcepts,
			final String schemaPath) {
		List<AnnotatedPhrase> importedAnnotations;

		File schemaFile = new File(schemaPath);
		List<SchemaConcept> schemaConceptList = SchemaImporter
				.importSchemaConcepts(schemaFile);

		if ( (schemaConceptList == null) || schemaConceptList.isEmpty() ) {
			System.out.println("No schema concepts found at path " + xmlPath
					+ "\n NOTE: May cause errors as no default attributes will be assigned");
			importedAnnotations = this.importAnnotations(xmlPath,
					textSourcePath, acceptedConcepts);
		}
		else {
			importedAnnotations = this.importAnnotations(xmlPath,
					textSourcePath, acceptedConcepts, schemaConceptList, null);
		}

		return importedAnnotations;
	}

	/**
	 * Import annotations from XML file generated by eHOST
	 *
	 * @param xmlPathString
	 * @param textSourcePathString
	 * @param acceptedConcepts
	 * @return
	 * @author Glenn Gobbel - Sep 11, 2012
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public List<AnnotatedPhrase> importAnnotations(final String xmlPathString,
			final String textSourcePathString,
			final Set<String> acceptedConcepts) {
		try {
			File xmlFile = new File(xmlPathString);
			this.doc = domXML(xmlFile);

			/*
			 * Modified on August 18, 2014 to incorporate Sanjib's changes that
			 * allow for multiple attributes with values and spans
			 */
			// annotatedPhrases = parseXML( acceptedConcepts );
			this.annotatedPhrases = this.parseXMLEHostVersion(acceptedConcepts);
		}
		catch (SAXException | IOException | ParserConfigurationException PCe) {
			PCe.printStackTrace();
		}

		List<AnnotatedPhrase> unmatchedPhraseList = this
				.conformXMLOffsetsToText(textSourcePathString,
						this.annotationSource);

		if ( unmatchedPhraseList.size() > 0 ) {
			/* Create error file */
			File xmlFile = new File(xmlPathString);
			String errorFilePath = xmlFile.getParent() + "_ImportErrors_"
					+ xmlFile.getName() + ".txt";
			File errorFile = new File(errorFilePath);
			try {
				String errorString = unmatchedPhraseList.size()
						+ " unmatched, annotations for document "
						+ textSourcePathString;
				PrintWriter pw = new PrintWriter(errorFile);
				pw.println(errorString);
				System.err.println(errorString);
				for (AnnotatedPhrase curPhrase : unmatchedPhraseList) {
					System.err.println(curPhrase);
					pw.println(curPhrase);
				}
				if ( AnnotationImporter.logger.isDebugEnabled() ) {
					GeneralHelper.errorWriter("Check unmatched annotations");
				}
				pw.close();
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("All annotations matched");
		}

		return this.annotatedPhrases;
	}

	/**
	 * Method called by several methods including those involved in leave one
	 * out analysis and training to generate a solution. The method only imports
	 * eHOST generated annotations.
	 *
	 * @param xmlPath
	 * @param textSourcePath
	 * @param acceptedConcepts
	 * @param schemaConceptList
	 * @return
	 */
	public List<AnnotatedPhrase> importAnnotations(final String xmlPath,
			final String textSourcePath, final Set<String> acceptedConcepts,
			final List<SchemaConcept> schemaConceptList, final PrintWriter pw) {
		return this.importAnnotations(xmlPath, textSourcePath, acceptedConcepts,
				schemaConceptList, pw, false);
	}

	public List<AnnotatedPhrase> importAnnotations(final String xmlPath,
			final String textSourcePath, final Set<String> acceptedConcepts,
			final List<SchemaConcept> schemaConceptList, final PrintWriter pw,
			final boolean xmlContainsAdjudications) {
		try {
			File xmlFile = new File(xmlPath);
			this.doc = domXML(xmlFile);
			this.annotatedPhrases = this.parseXMLEHostVersion(acceptedConcepts,
					xmlContainsAdjudications);
			this.matchedAnnotationNumber = this.annotatedPhrases.size();
		}
		catch (SAXException | IOException | ParserConfigurationException PCe) {
			PCe.printStackTrace();
		}

		HashMap<String, SchemaConcept> schemaConceptMap = new HashMap<>();
		for (SchemaConcept schemaConcept : schemaConceptList) {
			if ( !schemaConcept.isRelationship() ) {
				schemaConceptMap.put(schemaConcept.getConceptName(),
						schemaConcept);
			}
		}

		this.reconcileAnnotationsWithSchema(schemaConceptMap);
		this.reconcileAnnotationsWithText(textSourcePath, pw);

		return this.annotatedPhrases;
	}

	/**
	 * Import XML annotation file data
	 *
	 * @author Sanjib Saha - July 30, 2014
	 * @param xmlPath
	 *            XML file path
	 * @return list of annotated phrases
	 */
	public List<AnnotatedPhrase> importXMLAnnotation(final String xmlPath) {
		try {
			File xmlFile = new File(xmlPath);
			this.doc = domXML(xmlFile);
			this.annotatedPhrases = this.parseXMLEHostVersion(null);
		}
		catch (SAXException | IOException | ParserConfigurationException PCe) {
			PCe.printStackTrace();
		}

		return this.annotatedPhrases;
	}

	/**
	 * @return the reconcileAnnotationsWithSchema
	 */
	public boolean reconcileAnnotationsWithSchema() {
		return this.reconcileAnnotationsWithSchema;
	}

	/**
	 * @return the reconcileAnnotationsWithText
	 */
	public boolean reconcileAnnotationsWithText() {
		return this.reconcileAnnotationsWithText;
	}

	public boolean removeUnmatched() {
		return this.removeUnmatched;
	}

	/**
	 * @param annotationSource
	 *            the annotationSource to set
	 */
	public void setAnnotationSource(final AnnotationApp annotationSource) {
		this.annotationSource = annotationSource;
	}

	/**
	 * @param reconcileAnnotationsWithSchema
	 *            the reconcileAnnotationsWithSchema to set
	 */
	public void setReconcileAnnotationsWithSchema(
			final boolean reconcileAnnotationsWithSchema) {
		this.reconcileAnnotationsWithSchema = reconcileAnnotationsWithSchema;
	}

	/**
	 * @param reconcileAnnotationsWithText
	 *            the reconcileAnnotationsWithText to set
	 */
	public void setReconcileAnnotationsWithText(
			final boolean reconcileAnnotationsWithText) {
		this.reconcileAnnotationsWithText = reconcileAnnotationsWithText;
	}

	public void setRemoveUnmatched(final boolean removeUnmatched) {
		this.removeUnmatched = removeUnmatched;
	}

	/**
	 * Takes a given xml file and writes it to a tab-delimited file specified by
	 * the printer parameter. If a valid text file is supplied by the parameter
	 * txtfile, the sentence that the phrases in the xml came from are printed
	 * also.
	 *
	 * @param xmlFile
	 * @param txtFile
	 * @param printer
	 */
	public void writeXMLToTab(final File xmlFile, final File txtFile,
			final TextAnalyzer ta, final PhraseTokenIntegrator integrator,
			final PrintWriter printer) {
		String textFile = JdomXMLImporter.getTextSource(xmlFile);
		try {
			/* Import the annotations */
			List<AnnotatedPhrase> thePhrases = new ArrayList<>();
			if ( (txtFile == null) || !txtFile.getName().equals(textFile) ) {
				this.doc = domXML(xmlFile);
				thePhrases = this.parseXMLEHostVersion(null);
			}
			/*
			 * Use the txtFile file to correct offsets via importAnnotations if
			 * txtFile is valid
			 */
			else {
				thePhrases = this.importAnnotations(xmlFile.getAbsolutePath(),
						txtFile.getAbsolutePath(), null);

				/*
				 * If we have a valid TextAnalyzer and PhraseTokenIntegrator
				 * instances, add tokens to annotations so we can get the
				 * sentences associated with annotations
				 */
				if ( (ta != null) && (integrator != null) ) {
					RaptatDocument raptatDocument = ta
							.processDocument(txtFile.getAbsolutePath());
					integrator.addProcessedTokensToAnnotations(thePhrases,
							raptatDocument, true, 0);
				}
			}

			String tab = "\t";
			for (AnnotatedPhrase curPhrase : thePhrases) {
				StringBuilder sbStart = new StringBuilder(textFile);
				sbStart.append(tab);

				String conceptName = curPhrase.getConceptName();
				sbStart.append(conceptName).append(tab).append(tab).append(tab);

				StringBuilder sbMiddle = new StringBuilder();
				sbMiddle.append(curPhrase.getPhraseStringUnprocessed())
						.append(tab);
				sbMiddle.append(curPhrase.getRawTokensStartOff()).append(tab);
				sbMiddle.append(curPhrase.getRawTokensEndOff()).append(tab);
				sbMiddle.append(curPhrase.getMentionId()).append(tab);

				/* Add sentence string if there is one associated with phrase */
				List<RaptatToken> sentenceTokens;
				String sentenceText = "";
				if ( ((sentenceTokens = curPhrase.getProcessedTokens()) != null)
						&& (sentenceTokens.size() > 0) ) {
					AnnotatedPhrase phraseSentence = sentenceTokens.get(0)
							.getSentenceOfOrigin();
					sentenceText = phraseSentence != null
							? phraseSentence.getPhraseStringUnprocessed()
							: "";
					sentenceText = sentenceText.replaceAll("^\\s+", "");
					sentenceText = "'"
							+ sentenceText.replaceAll("\\n|\\r|\\t", " ");
					sentenceText = sentenceText.replaceAll("\\s+", " ");
				}

				printer.println(sbStart.toString() + sbMiddle.toString()
						+ sentenceText);
				printer.flush();

				for (RaptatAttribute curAttribute : curPhrase
						.getPhraseAttributes()) {
					String attributeName = curAttribute.getName();
					for (String value : curAttribute.getValues()) {
						sbStart = new StringBuilder(textFile).append(tab);
						sbStart.append(conceptName).append(tab);
						sbStart.append("ATT_").append(attributeName)
								.append(tab);
						sbStart.append(value).append(tab);
						printer.println(sbStart.toString() + sbMiddle.toString()
								+ sentenceText);
						printer.flush();
					}
				}

				for (ConceptRelation curRelationship : curPhrase
						.getConceptRelations()) {
					String relationName = curRelationship
							.getConceptRelationName();
					String mentionID = curRelationship.getRelatedAnnotationID();
					sbStart = new StringBuilder(textFile).append(tab);
					sbStart.append(conceptName).append(tab);
					sbStart.append("REL_").append(relationName).append(tab);
					sbStart.append(mentionID).append(tab);
					printer.println(sbStart.toString() + sbMiddle.toString()
							+ sentenceText);
					printer.flush();
				}

				printer.flush();
			}
		}
		catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Determines if an adjudicating node in an eHOST XML file of annotations
	 * was deleted based on adjudication
	 *
	 * @param annotationChildren
	 * @param childrenLength
	 * @return
	 */
	private boolean adjudicatedMatchDeleted(final NodeList annotationChildren,
			final int childrenLength) {
		for (int i = 0; i < childrenLength; i++) {
			if ( annotationChildren.item(i).getTextContent().equals(
					RaptatConstants.EHOST_ADJUDICATED_MATCH_DELETED_MARKER) ) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Correct offsets of xml annotated phrases so they match the exact location
	 * of all annotated phrases within the document as determined by openNLP
	 * parser and tokenizer when working on a UTF-8 encoded document.
	 *
	 * Preconditions: The document has been parsed and the document tokens have
	 * all been retrieved Postconditions: The list of annotated phrases is
	 * changed so that any phrases not found in the document are excluded and
	 * the offsets for the whole phrase and its tokens are set so they match the
	 * exact offsets for the tokens in the document.
	 *
	 * @param pathToDocument
	 *
	 * @author Glenn Gobbel - Apr 25, 2012
	 */
	private void correctXMLOffsets() {
		Integer curOffset;
		IndexedObject<Integer> lesserObject;
		if ( this.offsetComparer.maxDepth() > -1 ) {
			for (AnnotatedPhrase curPhrase : this.annotatedPhrases) {
				curOffset = Integer.parseInt(curPhrase.getRawTokensStartOff());
				lesserObject = this.offsetComparer
						.getClosestLesserOrEqualObject(curOffset);
				if ( lesserObject != null ) {
					curOffset += lesserObject.getIndexedObject();
					curPhrase.setRawStartOff(String.valueOf(curOffset));
				}

				curOffset = Integer.parseInt(curPhrase.getRawTokensEndOff());
				lesserObject = this.offsetComparer
						.getClosestLesserOrEqualObject(curOffset);
				if ( lesserObject != null ) {
					curOffset += lesserObject.getIndexedObject();
					curPhrase.setRawEndOff(String.valueOf(curOffset));
				}
			}
		}
	}

	/**
	 * Method to create a lookup map of all the mentionID and associated
	 * concepts for all the annotations read from the EHost XML file
	 *
	 * @author Sanjib Saha - July 30, 2014
	 */
	private void createAnnotationIdToConceptMap() {
		NodeList classMentions = this.doc.getElementsByTagName("classMention");
		NodeList classMentionChildren;

		NamedNodeMap classMentionsValues;

		String conceptName = null;
		String classMentionId;
		List<String> slotIds;

		for (int j = 0; j < classMentions.getLength(); j++) {
			classMentionsValues = classMentions.item(j).getAttributes();
			classMentionId = classMentionsValues.getNamedItem("id")
					.getNodeValue();
			classMentionChildren = classMentions.item(j).getChildNodes();

			slotIds = new ArrayList<>();

			for (int i = 0; i < classMentionChildren.getLength(); i++) {
				String nodeName = classMentionChildren.item(i).getNodeName();

				if ( nodeName.equals("mentionClass") ) {
					conceptName = classMentionChildren.item(i).getAttributes()
							.getNamedItem("id").getNodeValue().toLowerCase();
				}
				else if ( nodeName.equals("hasSlotMention") ) {
					slotIds.add(classMentionChildren.item(i).getAttributes()
							.getNamedItem("id").getNodeValue());
				}
			}

			this.annotationIdToConceptMap.put(classMentionId,
					new AnnotatedConcept(conceptName, slotIds));
		}
	}

	/**
	 * Method to create a lookup map of all relationship among annotations read
	 * from the EHost XML file
	 *
	 * @author Sanjib Saha - July 30, 2014
	 */
	private void createComplexSlotToRelationMap() {
		NodeList classMentions = this.doc
				.getElementsByTagName("complexSlotMention");
		NodeList complexSlotMentionChildren;

		NamedNodeMap complexSlotMentionValues;

		String conceptValue = null;
		String complexSlotMentionId;
		String attributeType;
		String attributeValue;

		String linkedAnnotationID = "";
		List<String> tempAttributeValues;
		List<RaptatAttribute> attributes;

		HashMap<String, List<String>> attributeValuesMap;

		for (int j = 0; j < classMentions.getLength(); j++) {
			complexSlotMentionValues = classMentions.item(j).getAttributes();
			complexSlotMentionId = complexSlotMentionValues.getNamedItem("id")
					.getNodeValue();
			complexSlotMentionChildren = classMentions.item(j).getChildNodes();

			attributes = new ArrayList<>();

			attributeValuesMap = new HashMap<>();

			for (int i = 0; i < complexSlotMentionChildren.getLength(); i++) {
				String nodeName = complexSlotMentionChildren.item(i)
						.getNodeName();
				if ( nodeName.equals("mentionSlot") ) {
					conceptValue = complexSlotMentionChildren.item(i)
							.getAttributes().getNamedItem("id").getNodeValue();
				}
				else if ( nodeName.equals("complexSlotMentionValue") ) {
					linkedAnnotationID = complexSlotMentionChildren.item(i)
							.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				else if ( nodeName.equals("attribute") ) {
					attributeType = complexSlotMentionChildren.item(i)
							.getAttributes().getNamedItem("id").getNodeValue();
					attributeValue = complexSlotMentionChildren.item(i)
							.getTextContent();

					if ( (tempAttributeValues = attributeValuesMap
							.get(attributeType)) == null ) {
						tempAttributeValues = new ArrayList<>();
						attributeValuesMap.put(attributeType,
								tempAttributeValues);
					}

					tempAttributeValues.add(attributeValue);
				}
			}

			for (String key : attributeValuesMap.keySet()) {
				attributes.add(new RaptatAttribute("", key,
						attributeValuesMap.get(key),
						AttributeAssignmentMethod.REFERENCE));
			}

			this.complexSlotToRelationMap.put(complexSlotMentionId,
					new ConceptRelation(complexSlotMentionId, conceptValue,
							linkedAnnotationID, attributes));

			this.linkedToAnnotations.add(linkedAnnotationID);
		}
	}

	/**
	 * Method to create a lookup map of all attributes associated for all
	 * annotations read from the EHost XML file
	 *
	 * @author Sanjib Saha - July 30, 2014
	 */
	private void createStringSlotToAttributeMap() {
		NodeList slotMentions = this.doc
				.getElementsByTagName("stringSlotMention");
		NodeList stringSlotMentionChildren;

		NamedNodeMap stringSlotMentionValues;

		String stringSlotMentionId;
		String attribute = null;
		List<String> attributeValues;

		for (int j = 0; j < slotMentions.getLength(); j++) {
			stringSlotMentionValues = slotMentions.item(j).getAttributes();
			stringSlotMentionId = stringSlotMentionValues.getNamedItem("id")
					.getNodeValue();
			stringSlotMentionChildren = slotMentions.item(j).getChildNodes();

			attributeValues = new ArrayList<>();

			for (int i = 0; i < stringSlotMentionChildren.getLength(); i++) {
				/*
				 * Modified by Glenn Gobbel on 10-02-14 so that attribute and
				 * attribute values are all converted to lower case
				 */
				String nodeName = stringSlotMentionChildren.item(i)
						.getNodeName();

				{
					if ( nodeName.equals("mentionSlot") ) {
						attribute = stringSlotMentionChildren.item(i)
								.getAttributes().getNamedItem("id")
								.getNodeValue().toLowerCase();
					}
					else if ( nodeName.equals("stringSlotMentionValue") ) {
						attributeValues.add(stringSlotMentionChildren.item(i)
								.getAttributes().getNamedItem("value")
								.getNodeValue().toLowerCase());
					}
				}
			}

			this.stringSlotToAttributeMap.put(stringSlotMentionId,
					new RaptatAttribute(stringSlotMentionId, attribute,
							attributeValues,
							AttributeAssignmentMethod.REFERENCE));
		}
	}

	/**
	 * Parse EHost XML annotation file data
	 *
	 * @author Sanjib Saha - July 30, 2014
	 * @param acceptedConcepts
	 *            accepted concepts
	 * @return list of annotated phrases
	 */
	private List<AnnotatedPhrase> parseXMLEHostVersion(
			final Set<String> acceptedConcepts) {
		return this.parseXMLEHostVersion(acceptedConcepts, false);
	}

	/**
	 * Parse EHost XML annotation file data
	 *
	 * @author Sanjib Saha - July 30, 2014
	 * @param acceptedConcepts
	 *            accepted concepts
	 * @return list of annotated phrases
	 */
	private List<AnnotatedPhrase> parseXMLEHostVersion(
			final Set<String> acceptedConcepts,
			final boolean adjudicationImport) {
		@SuppressWarnings("Convert2Diamond")
		List<AnnotatedPhrase> resultAnnotations = new LinkedList<>();

		String nodeType = adjudicationImport ? "adjudicating" : "annotation";
		NodeList annotationNodes = this.doc.getElementsByTagName(nodeType);

		String spanText = "";

		createStringSlotToAttributeMap();
		createAnnotationIdToConceptMap();
		createComplexSlotToRelationMap();

		for (int j = 0; j < annotationNodes.getLength(); j++) {
			List<Span> spans = new ArrayList<>();
			String[] annotationData = new String[6];
			List<RaptatAttribute> attributeList = new ArrayList<>();
			List<ConceptRelation> conceptRelations = new ArrayList<>();

			NodeList annotationChildren = annotationNodes.item(j)
					.getChildNodes();
			int childrenLength = annotationChildren.getLength();

			if ( !adjudicationImport
					|| !adjudicatedMatchDeleted(annotationChildren,
							childrenLength) ) {
				for (int i = 0; i < childrenLength; i++) {
					String mentionId, annotatorName, startOff, endOff;

					String nodeName = annotationChildren.item(i).getNodeName();

					if ( nodeName.equals("mention") ) {
						mentionId = annotationChildren.item(i).getAttributes()
								.getNamedItem("id").getNodeValue();

						AnnotatedConcept concept = this.annotationIdToConceptMap
								.get(mentionId);

						for (String attribute : concept.getIDList()) {
							RaptatAttribute phraseAttribute;
							ConceptRelation relation;

							/*
							 * Note that an attribute in the idList of a concept
							 * must be either a phrase attribute or a
							 * conceptRelation, so that if it's not an attribute
							 * it must be a conceptRelation
							 */
							if ( (phraseAttribute = this.stringSlotToAttributeMap
									.get(attribute)) != null ) {
								attributeList.add(phraseAttribute);
							}
							else if ( (relation = this.complexSlotToRelationMap
									.get(attribute)) != null ) {
								if ( (acceptedConcepts == null)
										|| acceptedConcepts.isEmpty()
										|| acceptedConcepts.contains(relation
												.getConceptRelationName()
												.toLowerCase()) ) {
									conceptRelations.add(relation);
								}
							}
							else {
								System.err.println(
										"Import error: no connecting reference to attribute "
												+ attribute + " of mentionId "
												+ mentionId
												+ " found in XML file");
							}
						}

						annotationData[AnnotationDataIndex.MENTION_ID
								.ordinal()] = mentionId;
						annotationData[AnnotationDataIndex.CONCEPT_NAME
								.ordinal()] = concept.getConcept()
										.toLowerCase();
					}
					else if ( nodeName.equals("annotator") ) {
						annotatorName = annotationChildren.item(i)
								.getTextContent().toLowerCase();
						annotationData[AnnotationDataIndex.ANNOTATOR_NAME
								.ordinal()] = annotatorName;
					}
					else if ( nodeName.equals("spannedText") ) {
						spanText = annotationChildren.item(i).getTextContent()
								.replaceAll("\\s+", " ");
					}
					else if ( nodeName.equals("span") ) {
						startOff = annotationChildren.item(i).getAttributes()
								.getNamedItem("start").getNodeValue();
						endOff = annotationChildren.item(i).getAttributes()
								.getNamedItem("end").getNodeValue();

						// Scope for multiple spans
						spans.add(new Span(Integer.parseInt(startOff),
								Integer.parseInt(endOff)));
					}
				}
			}

			Collections.sort(spans);

			// phraseSequence = 0;
			AnnotatedPhrase prevAnnotation = null;

			/*
			 * If the number of ellipses in the spanned text does not equal one
			 * less than the number of spans, there must be one or more ellipses
			 * in the document itself. These two variables are created to deal
			 * with that condition. NOTE: if there is a problem with a span
			 * (e.g. contains only single return character), the EHOST_ELLIPSE
			 * may be malformed and countMatches will return 0 even though there
			 * is more than one span.
			 */
			int remainingEllipseMatches = StringUtils.countMatches(spanText,
					RaptatConstants.EHOST_ELLIPSE);
			int remainingSpans = spans.size();

			// Creating connected annotations for multiple spanned annotations
			for (Span span : spans) {
				// annotationData[MENTION_ID_INDEX] += "_" + phraseSequence;
				annotationData[AnnotationDataIndex.START_OFFSET
						.ordinal()] = span.getStart() + "";
				annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] = span
						.getEnd() + "";

				/*
				 * Code below deals with the possibility that ellipses can
				 * either be in the document itself or used by eHOST as a marker
				 * between annotation spans. This code should work in *most*
				 * cases where the amount of whitespace (>1 whitespace repeated
				 * in a row) or ellipses are not "excessive".
				 *
				 * This problem occurs because of the way eHOST deals with
				 * creating the XML files storing annotations. It uses an
				 * instance of the class OutputToXML and the method buildXML().
				 * It sets the format via the Format class from jdom.org and
				 * uses a "compact format" via "getCompactFormat()". This method
				 * of formatting replaces multiple consecutive whitespace
				 * characters with a single whitespace character.
				 *
				 * This could be addressed more correctly by changing the way
				 * eHOST creates it's XML. Knowtator does not appear to remove
				 * spaces and so this could make eHOST more compatible with
				 * Knowtator. An alternative would be to examine the text
				 * document associated with the XML and determine the spans
				 * based on that.
				 */
				if ( (remainingSpans > 1) && (remainingEllipseMatches > 0) ) {
					int spanLength = span.getEnd() - span.getStart();
					int ellipseIndex = spanText
							.indexOf(RaptatConstants.EHOST_ELLIPSE);
					int ellipseEndIndex = ellipseIndex
							+ RaptatConstants.EHOST_ELLIPSE.length();

					/*
					 * Only skip over ellipses if the number of ellipses is more
					 * than should occur given the number of remaining spans.
					 * Also, only consider ellipses as possibly being in the
					 * document itself if the position of the end of the ellipse
					 * is less than the length of the current span. If both
					 * conditions are true, skip over the ellipse as it is part
					 * of the span rather than a marker to separate spans.
					 */
					while ( (remainingEllipseMatches >= remainingSpans)
							&& (ellipseEndIndex < spanLength) ) {
						ellipseIndex = spanText.indexOf(
								RaptatConstants.EHOST_ELLIPSE, ellipseEndIndex);
						ellipseEndIndex = ellipseIndex
								+ RaptatConstants.EHOST_ELLIPSE.length();
						--remainingEllipseMatches;
					}

					try {
						annotationData[AnnotationDataIndex.SPAN_TEXT
								.ordinal()] = spanText.substring(0,
										ellipseIndex);
					}
					catch (Exception ex) {
						System.out.println(ex.toString() + "\n" + spanText
								+ " EllipseIndex:" + ellipseIndex);
					}
					spanText = spanText.substring(ellipseEndIndex);
					--remainingEllipseMatches;
					--remainingSpans;
				}
				else {
					annotationData[AnnotationDataIndex.SPAN_TEXT
							.ordinal()] = spanText;
				}

				if ( ((acceptedConcepts == null) || acceptedConcepts.isEmpty()
						|| acceptedConcepts.contains(
								annotationData[AnnotationDataIndex.CONCEPT_NAME
										.ordinal()].toLowerCase()))
						&& (annotationData[AnnotationDataIndex.START_OFFSET
								.ordinal()] != null)
						&& (annotationData[AnnotationDataIndex.START_OFFSET
								.ordinal()].length() > 0)
						&& (annotationData[AnnotationDataIndex.END_OFFSET
								.ordinal()] != null)
						&& (annotationData[AnnotationDataIndex.END_OFFSET
								.ordinal()].length() > 0)
						&& (annotationData[AnnotationDataIndex.SPAN_TEXT
								.ordinal()] != null)
						&& (annotationData[AnnotationDataIndex.SPAN_TEXT
								.ordinal()].length() > 0) ) {

					AnnotatedPhrase annotation = new AnnotatedPhrase(
							annotationData, attributeList, conceptRelations);

					if ( prevAnnotation != null ) {
						prevAnnotation.setNextPhrase(annotation);
						annotation.setPrevPhrase(prevAnnotation);
					}

					prevAnnotation = annotation;
					resultAnnotations.add(annotation);
				}
			}
		}

		Collections.sort(resultAnnotations, new StartOffsetPhraseComparator());
		setIndices(resultAnnotations);
		return resultAnnotations;
	}

	/**
	 * @param schemaConceptMap
	 * @throws IrregularPhraseConcept
	 */
	private void reconcileAnnotationsWithSchema(
			final HashMap<String, SchemaConcept> schemaConceptMap) {
		HashSet<String> attributes;
		SchemaConcept schemaConcept;
		for (AnnotatedPhrase phrase : this.annotatedPhrases) {
			schemaConcept = schemaConceptMap.get(phrase.getConceptName());

			if ( schemaConcept == null ) {
				System.err.println(
						"Schema file does not match with the imported xml file annotation for concept:"
								+ phrase.getConceptName());
			}

			/*
			 * Check to see if phrase contains all the expected attributes for
			 * the concept it maps to. If not, add the attribute and its default
			 * value (or a null equivalent string if there is no default).
			 */
			attributes = new HashSet<>();

			for (RaptatAttribute attr : phrase.getPhraseAttributes()) {
				attributes.add(attr.getName());
			}

			for (String attr : schemaConcept.getAttributeValueMap().keySet()) {
				if ( !attributes.contains(attr) ) {
					String defaultValue = schemaConcept
							.getDefaultAttributeValue(attr);
					if ( defaultValue == null ) {
						defaultValue = RaptatConstants.NULL_ATTRIBUTE_VALUE;
					}
					List<String> defaultAttrValues = new ArrayList<>(1);
					defaultAttrValues.add(defaultValue);
					phrase.addAttribute(
							new RaptatAttribute("", attr, defaultAttrValues,
									AttributeAssignmentMethod.REFERENCE));
				}
			}
		}
	}

	/**
	 * @param textSourcePath
	 * @param pw
	 */
	private void reconcileAnnotationsWithText(final String textSourcePath,
			final PrintWriter pw) {
		List<AnnotatedPhrase> unmatchedPhraseList = this
				.conformXMLOffsetsToText(textSourcePath, this.annotationSource);

		if ( unmatchedPhraseList.size() > 0 ) {
			String errorString = unmatchedPhraseList.size()
					+ " unmatched, annotations for document " + textSourcePath;

			System.err.println(errorString);
			for (AnnotatedPhrase annotatedPhrase : unmatchedPhraseList) {
				System.err.println(annotatedPhrase);
			}

			if ( pw != null ) {
				pw.println(errorString);
				for (AnnotatedPhrase annotatedPhrase : unmatchedPhraseList) {
					pw.println(annotatedPhrase);
				}
				pw.flush();
			}
		}

		this.unmatchedAnnotationNumber = unmatchedPhraseList.size();
	}

	/**
	 * Removes a list of AnnotatedPhrases from any ConceptRelation objects
	 * within the field this.annotatedPhrases. This is generally done when an
	 * annotation was found during import that wasn't properly described in the
	 * xml.
	 *
	 * @param phrasesForRemoval
	 */
	private void removePhrasesFromRelations(
			final List<AnnotatedPhrase> phrasesForRemoval) {
		HashSet<String> removalIDs = new HashSet<>(phrasesForRemoval.size());

		/*
		 * Determine which of the phrasesForRemoval are actually
		 * 'linkedToAnnotations' and make a set of these.
		 */
		for (AnnotatedPhrase curPhrase : phrasesForRemoval) {
			String annotationID = curPhrase.getMentionId();

			if ( this.linkedToAnnotations.contains(annotationID) ) {
				removalIDs.add(curPhrase.getMentionId());
			}
		}

		if ( !removalIDs.isEmpty() ) {
			/*
			 * Now loop through all the annotated phrases that were imported,
			 * and remove the annotations that are linked to by any of the
			 * concept relations of the annotation. If, after doing this, no
			 * annotations are left in the relation, remove the relation also.
			 */
			for (AnnotatedPhrase curPhrase : this.annotatedPhrases) {
				HashSet<ConceptRelation> phraseRelations = new HashSet<>(
						curPhrase.getConceptRelations());
				Iterator<ConceptRelation> relationIterator = phraseRelations
						.iterator();
				while ( relationIterator.hasNext() ) {
					ConceptRelation phraseRelation = relationIterator.next();
					String linkedToAnnotationID = phraseRelation
							.getRelatedAnnotationID();
					if ( removalIDs.contains(linkedToAnnotationID) ) {
						relationIterator.remove();
					}
				}
			}
		}
	}

	/**
	 * Create a list of phrases within the XML document that are not found at
	 * the expected offset positions within the document.
	 *
	 * <p>
	 * Preconditions: The offsets within the XML file have already been set so
	 * that they conform to the openNLP document
	 *
	 * @param pathToDocument
	 * @author Glenn Gobbel - Apr 27, 2012
	 */
	private List<AnnotatedPhrase> removeUnmatchedXMLPhrases(
			final Path textDocumentPath) {
		int bomSkipChars = 0;
		BufferedReader reader = null;
		this.unmatchedPhrases = new ArrayList<>();

		try {
			Pattern nonWhiteSpaceRegex = Pattern.compile("\\s+");

			/*
			 * We skip the first character if there is a byte offset marker,
			 * which may be present in UTF-8 documents but is unused and
			 * unaccounted for by openNLP
			 */
			if ( OffsetParser.hasByteOrderMark(textDocumentPath) ) {
				bomSkipChars = 1;
			}

			Iterator<AnnotatedPhrase> phraseIterator = this.annotatedPhrases
					.iterator();
			while ( phraseIterator.hasNext() ) {
				AnnotatedPhrase curPhrase = phraseIterator.next();

				int curXMLOffset = Integer
						.parseInt(curPhrase.getRawTokensStartOff());
				int endXMLOffset = Integer
						.parseInt(curPhrase.getRawTokensEndOff());
				char[] docText = new char[endXMLOffset - curXMLOffset];

				if ( AnnotationImporter.logger.isDebugEnabled() ) {
					BufferedReader readerTest = Files.newBufferedReader(
							textDocumentPath, Charset.forName("UTF-8"));
					testReader(readerTest);
				}
				reader = Files.newBufferedReader(textDocumentPath,
						Charset.forName("UTF-8"));
				reader.skip(curXMLOffset + bomSkipChars);
				reader.read(docText);

				/*
				 * We remove white space because XMLImporter replaces //
				 * non-whitespace characters with whitespace and because the
				 * counting of whitespace characters is different in Knowtator
				 * vs. OpenNLP
				 */
				char[] xmlText = curPhrase.getPhraseStringUnprocessed()
						.toCharArray();
				String xmlString = nonWhiteSpaceRegex
						.matcher(String.valueOf(xmlText)).replaceAll("");
				String docString = nonWhiteSpaceRegex
						.matcher(String.valueOf(docText)).replaceAll("");

				if ( (xmlString == null) || (docString == null)
						|| !xmlString.equals(docString) ) {
					this.unmatchedPhrases.add(curPhrase);

					// GeneralHelper.errorWriter( "Unmatched Phrase:" +
					// curPhrase.toString() );
					// Back up one as the list will be reduced in size
					phraseIterator.remove();
				}
				try {
					if ( reader != null ) {
						reader.close();
					}
				}
				catch (IOException e) {
					System.out.println(
							"Unable to close file for correcting XML offsets\n"
									+ e);
					e.printStackTrace();
				}
			}
		}
		catch (FileNotFoundException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("UTF-8 encoding not supported\n" + e);
			e.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("Error during XML importation\n" + e);
			e.printStackTrace();
		}
		finally {
			try {
				if ( reader != null ) {
					reader.close();
				}
			}
			catch (IOException e) {
				System.out.println(
						"Unable to close file for correcting XML offsets\n"
								+ e);
				e.printStackTrace();
			}
		}

		if ( !this.unmatchedPhrases.isEmpty() ) {
			removePhrasesFromRelations(this.unmatchedPhrases);
		}
		return this.unmatchedPhrases;
	}

	/**
	 * Create a list of phrases within the XML document that are not found at
	 * the expected offset positions within the document.
	 *
	 * <p>
	 * Preconditions: The offsets within the XML file have already been set so
	 * that they conform to the openNLP document
	 *
	 * @param pathToDocument
	 * @author Glenn Gobbel - Apr 27, 2012
	 */
	private List<AnnotatedPhrase> removeUnmatchedXMLPhrases(
			final String pathToDocument) {
		int bomSkipChars = 0;
		BufferedReader reader = null;
		this.unmatchedPhrases = new ArrayList<>();

		try {
			Pattern nonWhiteSpaceRegex = Pattern.compile("\\s+");

			/*
			 * We skip the first character if there is a byte offset marker,
			 * which may be present in UTF-8 documents but is unused and
			 * unaccounted for by openNLP
			 */
			if ( OffsetParser.hasByteOrderMark(pathToDocument) ) {
				bomSkipChars = 1;
			}

			Iterator<AnnotatedPhrase> phraseIterator = this.annotatedPhrases
					.iterator();
			while ( phraseIterator.hasNext() ) {
				AnnotatedPhrase curPhrase = phraseIterator.next();

				int curXMLOffset = Integer
						.parseInt(curPhrase.getRawTokensStartOff());
				int endXMLOffset = Integer
						.parseInt(curPhrase.getRawTokensEndOff());
				char[] docText = new char[endXMLOffset - curXMLOffset];

				if ( AnnotationImporter.logger.isDebugEnabled() ) {
					FileInputStream fisTest = new FileInputStream(
							pathToDocument);
					BufferedReader readerTest = new BufferedReader(
							new InputStreamReader(fisTest, "UTF-8"));
					testReader(readerTest);
				}
				FileInputStream fis = new FileInputStream(pathToDocument);
				reader = new BufferedReader(
						new InputStreamReader(fis, "UTF-8"));
				reader.skip(curXMLOffset + bomSkipChars);
				reader.read(docText);

				/*
				 * We remove white space because XMLImporter replaces //
				 * non-whitespace characters with whitespace and because the
				 * counting of whitespace characters is different in Knowtator
				 * vs. OpenNLP
				 */
				char[] xmlText = curPhrase.getPhraseStringUnprocessed()
						.toCharArray();
				String xmlString = nonWhiteSpaceRegex
						.matcher(String.valueOf(xmlText)).replaceAll("");
				String docString = nonWhiteSpaceRegex
						.matcher(String.valueOf(docText)).replaceAll("");

				if ( (xmlString == null) || (docString == null)
						|| !xmlString.equals(docString) ) {
					this.unmatchedPhrases.add(curPhrase);

					// GeneralHelper.errorWriter( "Unmatched Phrase:" +
					// curPhrase.toString() );
					// Back up one as the list will be reduced in size
					phraseIterator.remove();
				}
				try {
					if ( reader != null ) {
						reader.close();
					}
				}
				catch (IOException e) {
					System.out.println(
							"Unable to close file for correcting XML offsets\n"
									+ e);
					e.printStackTrace();
				}
			}
		}
		catch (FileNotFoundException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("UTF-8 encoding not supported\n" + e);
			e.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("Error during XML importation\n" + e);
			e.printStackTrace();
		}
		finally {
			try {
				if ( reader != null ) {
					reader.close();
				}
			}
			catch (IOException e) {
				System.out.println(
						"Unable to close file for correcting XML offsets\n"
								+ e);
				e.printStackTrace();
			}
		}

		if ( !this.unmatchedPhrases.isEmpty() ) {
			removePhrasesFromRelations(this.unmatchedPhrases);
		}
		return this.unmatchedPhrases;
	}

	private void setIndices(final List<AnnotatedPhrase> inputAnnotations) {
		int i = 0;
		for (AnnotatedPhrase curPhrase : inputAnnotations) {
			curPhrase.setIndexInDocument(i++);
		}
	}

	/**
	 * Method developed only to test how a read reads in characters from a file
	 *
	 * @param reader
	 */
	private void testReader(final BufferedReader reader) {
		int readlength = 16;
		char[] chars = new char[readlength];
		try {
			int i = 0;
			while ( reader.read(chars) != -1 ) {
				System.out
						.println("\n"
								+ StringUtils.leftPad(
										Integer.toHexString(i++ * 16), 8, "0")
								+ ":\t" + new String(chars));
				chars = new char[readlength];
			}
			reader.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static AnnotationImporter getImporter() {
		String queryString = "Application used to create annotations?";
		String[] buttonNames = new String[AnnotationApp.values().length];
		int i = 0;
		for (AnnotationApp app : AnnotationApp.values()) {
			buttonNames[i++] = app.getCommonName();
		}
		ButtonQueryDialog bqd = new ButtonQueryDialog(buttonNames, queryString);
		String annotationSource = bqd.showQuery();
		AnnotationApp annotationApp = AnnotationApp.EHOST;
		for (AnnotationApp app : AnnotationApp.values()) {
			if ( annotationSource.toLowerCase()
					.equals(app.toString().toLowerCase()) ) {
				annotationApp = app;
				break;
			}
		}
		return new AnnotationImporter(annotationApp);
	}

	public static AnnotationImporter getImporter(
			final AnnotationApp annotationApp) {
		if ( annotationApp == null ) {
			return AnnotationImporter.getImporter();
		}
		else {
			return new AnnotationImporter(annotationApp);
		}
	}

	public static List<SchemaConcept> getSchemaConcepts(final Component parent,
			final String pathToSchema) {
		List<SchemaConcept> conceptList = null;

		File schemaFile = new File(pathToSchema);
		if ( !schemaFile.isFile() || !schemaFile.canRead() ) {
			Object[] options = {
					"Yes", "No", "Cancel"
			};

			boolean continueQuery = true;
			while ( continueQuery ) {
				int optionResult = JOptionPane.showOptionDialog(parent,
						"Do you want to use a schema\n(needed for attribute training)?",
						"Use Schema?", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);

				switch (optionResult) {
				case JOptionPane.YES_OPTION:
					OptionsManager theOptions = OptionsManager.getInstance();
					schemaFile = GeneralHelper.getFile("Select schema file",
							theOptions.getLastSelectedDir().getAbsolutePath());
					if ( (schemaFile != null) && schemaFile.isFile()
							&& schemaFile.canRead() ) {
						continueQuery = false;
					}
					break;
				case JOptionPane.NO_OPTION:
					return new ArrayList<>();
				default:
					return null;
				}
			}
		}

		conceptList = SchemaImporter.importSchemaConcepts(schemaFile);
		return conceptList;
	}

	public static void main(final String[] args) {

		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RunType runType = RunType.COMBINE_ATTRIBUTES_TO_CONCEPTS_FOR_ABI;
				String xmlSourceFolderPath, txtSourceFolderPath,
						xmlTargetFolderPath, acceptedConceptsPath;
				AnnotationMutator mutator = new AnnotationMutator();

				switch (runType) {
				case REMOVE_ATTRIBUTE: {
					String attributeForRemoval = "suggestion";
					xmlSourceFolderPath = "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\AZ_XML_NoUncertHypothet_ALL";
					txtSourceFolderPath = "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\AZ_TXT_ALL";
					// xmlSourceFolderPath =
					// "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\XmlShort";
					// txtSourceFolderPath =
					// "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\TxtShort";

					xmlTargetFolderPath = "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\AZ_XML_NoUncertHypothet_SuggRemoved_ALL";
					acceptedConceptsPath = "D:\\ORD_Matheny_201401107D_AstraZeneca\\OIC_Annotation\\AnnotationProjects\\Gobbel\\AZ_CrossValidation_170504\\AcceptedConcepts_170504.txt";

					mutator.removeAttributesFromPhrases(attributeForRemoval,
							acceptedConceptsPath, xmlSourceFolderPath,
							txtSourceFolderPath, xmlTargetFolderPath);
				}
					break;

				case PRINT_PHRASES_TO_CONSOLE: {
					printPhrasesToConsole();
				}
					break;

				case XML_FILES_TO_TAB_DELIMITED: {
					/*
					 * Insert full path to directory containing xml files
					 * containing annotations
					 */
					String xmlFolderPath = "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\RuthXML_ElliotTrainingCorpus";

					/*
					 * Insert full path to directory containing text files
					 * corresponding to the previous xml files
					 */
					String txtFolderPath = "D:\\MHA_NLP\\Glenn\\ElliotVsRuthVsRaptat_ElliotTrainingData\\TXT_ElliotTrainingCorpus";
					xmlFilesToTabDelimited(xmlFolderPath, txtFolderPath);
				}
					break;
				case PROMOTE_VALUES_TO_CONCEPTS: {
					xmlSourceFolderPath = "D:\\MHA_NLP\\Glenn\\XMLSource";
					txtSourceFolderPath = "D:\\MHA_NLP\\Glenn\\TXTSource";
					xmlTargetFolderPath = "D:\\MHA_NLP\\Glenn\\XMLTarget";
					String schemaConceptFilePath = "D:\\MHA_NLP\\Glenn\\PtsdProjectSchema_ModifiedForRaptatAssertion_161208.xml";
					acceptedConceptsPath = "D:\\MHA_NLP\\Glenn\\IBMCorpusForInitialRaptatTraining_161208\\AcceptableConcepts_161202.txt";
					mutator.promoteValuesToConcepts(xmlSourceFolderPath,
							txtSourceFolderPath, xmlTargetFolderPath,
							schemaConceptFilePath, acceptedConceptsPath);
				}
					break;
				case TEST_EQUALS_FOR_XML: {
					AnnotationImporter annotationImporter = new AnnotationImporter(
							AnnotationApp.EHOST);
					String filePathOne = "D:\\GlennWorkspace\\ExamplesForKaruna\\XmlFiles\\800215512043_v01.txt.knowtator.xml";
					String filePathTwo = "D:\\GlennWorkspace\\ExamplesForKaruna\\XmlFiles\\800215512043_v02.txt.knowtator.xml";
					try {
						File fileOne = new File(filePathOne);
						File fileTwo = new File(filePathTwo);
						Document docOne = annotationImporter.domXML(fileOne);
						Document docTwo = annotationImporter.domXML(fileTwo);
						Builder sourceOne = Input.from(docOne);
						Builder sourceTwo = Input.from(docTwo);
						Diff myDiff = DiffBuilder.compare(sourceOne)
								.withTest(sourceTwo)
								.withNodeMatcher(new DefaultNodeMatcher(
										ElementSelectors.byNameAndAllAttributes))
								.checkForSimilar().ignoreWhitespace().build();
						System.out.println(myDiff.toString());
					}
					catch (SAXException | IOException
							| ParserConfigurationException e) {
						e.printStackTrace();
					}
				}
					break;
				case COMBINE_ATTRIBUTES_TO_CONCEPTS_FOR_ABI: {
					// xmlSourceFolderPath =
					// "P:\\ORD_Girotra_201607120D\\Glenn\\GlennInstallOfABIToolEvaluation\\AttributeToConceptRemapping_190101\\XML_200ManuallyAnnotated";
					// txtSourceFolderPath =
					// "P:\\ORD_Girotra_201607120D\\Glenn\\GlennInstallOfABIToolEvaluation\\AttributeToConceptRemapping_190101\\TXT";
					// String targetFolderPath =
					// "P:\\ORD_Girotra_201607120D\\Glenn\\GlennInstallOfABIToolEvaluation\\AttributeToConceptRemapping_190101\\XML_Remapped";
					// String schemaConceptFilePath =
					// "P:\\ORD_Girotra_201607120D\\Glenn\\GlennInstallOfABIToolEvaluation\\ABIToolSchemaAndConcepts\\projectschema.xml";
					// acceptedConceptsPath =
					// "P:\\ORD_Girotra_201607120D\\Glenn\\GlennInstallOfABIToolEvaluation\\ABIToolSchemaAndConcepts\\AcceptedConcepts_ABITool_190101.csv";

					xmlSourceFolderPath = "P:\\ORD_Girotra_201607120D\\Glenn\\ManuscriptData_200704\\SLCToolEvaluation\\SLCToolOutputGroups7and8_200711";
					txtSourceFolderPath = "P:\\ORD_Girotra_201607120D\\Glenn\\ManuscriptData_200704\\SLCToolEvaluation\\\\Groups7and8Corpus_200704";
					String targetFolderPath = "P:\\ORD_Girotra_201607120D\\Glenn\\ManuscriptData_200704\\SLCToolEvaluation\\AttributesPromotedSLCToolOutputGroups7and8_200711";
					String schemaConceptFilePath = "P:\\ORD_Girotra_201607120D\\Glenn\\ManuscriptData_200704\\SLCToolEvaluation\\slcOutputSchema.xml";
					acceptedConceptsPath = "P:\\ORD_Girotra_201607120D\\Glenn\\ManuscriptData_200704\\SLCToolEvaluation\\acceptedConceptsForPromotion.txt";

					String prependString = "Index_Value";

					Map<String, Set<String>> conceptNamePositions = new HashMap<>();
					List<String> mappedAttributes = Arrays.asList(new String[] {
							"left", "right"
					});
					conceptNamePositions.put("laterality_value",
							new HashSet<>(mappedAttributes));
					mappedAttributes = Arrays.asList(new String[] {
							"abi", "tbi"
					});
					conceptNamePositions.put("index_type",
							new HashSet<>(mappedAttributes));

					Map<String, String> mappedAttributeValue = new HashMap<>();
					mappedAttributeValue.put("left", "Left");
					mappedAttributeValue.put("right", "Right");
					mappedAttributeValue.put("abi", "Abi");
					mappedAttributeValue.put("tbi", "Tbi");

					mutator.combineAttributeValuesIntoConcepts(
							xmlSourceFolderPath, txtSourceFolderPath,
							targetFolderPath, schemaConceptFilePath,
							acceptedConceptsPath, conceptNamePositions,
							mappedAttributeValue, prependString);
				}
					break;
				default:
					break;
				}
				System.exit(0);
			}

			private void printPhrasesToConsole() {
				PrintWriter pw = null;
				String pwFilePath = "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\"
						+ "ImportErrors_" + GeneralHelper.getTimeStamp()
						+ ".txt";
				try {
					pw = new PrintWriter(pwFilePath);
				}
				catch (FileNotFoundException e) {
					GeneralHelper.errorWriter(
							"Unable to create printwriter for writing import errors");
					e.printStackTrace();
					System.exit(-1);
				}

				String textDirectoryPath = "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\TestingCorpus";
				List<String> textFiles = Arrays
						.asList(new File(textDirectoryPath).list());
				Collections.sort(textFiles);

				String xmlDirectoryPath = "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\TestingXML";
				List<String> xmlFiles = Arrays
						.asList(new File(xmlDirectoryPath).list());
				Collections.sort(xmlFiles);

				String schemaPath = "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\AKIProjectSchema.xml";
				List<SchemaConcept> schemaClasses = SchemaImporter
						.importSchemaConcepts(schemaPath);
				AnnotationImporter theImporter = new AnnotationImporter(
						SchemaImporter.getSchemaApp());

				Iterator<String> xmlNameIterator = xmlFiles.iterator();
				Iterator<String> textNameIterator = textFiles.iterator();

				while ( xmlNameIterator.hasNext()
						&& textNameIterator.hasNext() ) {
					String xmlName = xmlNameIterator.next();
					String textName = textNameIterator.next();

					if ( !xmlName.contains(textName) ) {
						System.out.println("XML:" + xmlName);
						System.out.println("Text:" + textName);
						GeneralHelper.errorWriter("XML and Text do not match!");
					}
					String xmlPath = xmlDirectoryPath + File.separator
							+ xmlName;
					String textPath = textDirectoryPath + File.separator
							+ textName;
					List<AnnotatedPhrase> annotatedPhrases = theImporter
							.importAnnotations(xmlPath, textPath, null,
									schemaClasses, pw);

					for (AnnotatedPhrase curPhrase : annotatedPhrases) {
						System.out.println(curPhrase.toString());
					}
				}
				pw.flush();
				pw.close();
			}

			private void xmlFilesToTabDelimited(final String xmlFolderPath,
					final String txtFolderPath) {
				AnnotationImporter xmlImporter = new AnnotationImporter();
				TextAnalyzer ta = new TextAnalyzer();
				PhraseTokenIntegrator integrator = new PhraseTokenIntegrator();
				PrintWriter pw = null;
				String pwFilePath = xmlFolderPath + File.separator
						+ "Annotations_" + GeneralHelper.getTimeStamp()
						+ ".txt";
				try {
					pw = new PrintWriter(pwFilePath);
				}
				catch (FileNotFoundException e) {
					GeneralHelper.errorWriter("Unable to create printwriter");
					e.printStackTrace();
					System.exit(-1);
				}

				Collection<File> xmlFiles = FileUtils
						.listFiles(new File(xmlFolderPath), new String[] {
								"xml"
				}, false);

				Collection<File> txtFiles = FileUtils
						.listFiles(new File(txtFolderPath), new String[] {
								"txt"
				}, false);
				HashMap<String, File> xmlToTxtFileMap = new HashMap<>(
						txtFiles.size());
				for (File txtFile : txtFiles) {
					xmlToTxtFileMap.put(txtFile.getName() + ".knowtator.xml",
							txtFile);
				}

				int fileIndex = 1;
				int files = xmlFiles.size();
				for (File curXmlFile : xmlFiles) {
					System.out.println("\nImporting and Writing File "
							+ fileIndex++ + " of " + files + ":"
							+ curXmlFile.getName());
					File curTxtFile = xmlToTxtFileMap.get(curXmlFile.getName());
					if ( curTxtFile == null ) {
						System.out.println(
								"\n=====================\nNO TEXT FILE FOUND\n=====================");
					}
					xmlImporter.writeXMLToTab(curXmlFile, curTxtFile, ta,
							integrator, pw);
				}
			}
		});
	}
}
