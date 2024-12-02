package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * This is essentially the TDD_Convert_RaptatTokenPhrase_objects_into_eHOST_XML
 * class that Dax created copied from a test class to this class so that it can
 * be easily run from the GirotraABI class to show what RaptatTokenPhrase labels
 * are being created.
 *
 * @author Dax Westerman
 *
 * @Date - 10/14/19
 *
 */
public class TokenPhraseWriter {

	public void writePhrases(final String textFolderPath,
			final String xmlOutputFolderPath, final String sentenceLogFolder)
			throws NotDirectoryException {

		// Create TextAnalyzer instance

		final TextAnalyzer textAnalyzer = new TextAnalyzer(null, null);

		final SourceFolder sourceFolder = new SourceFolder(textFolderPath,
				"txt");
		final XMLExporterRevised exporter = new XMLExporterRevised(
				AnnotationApp.EHOST, new File(xmlOutputFolderPath), true);

		while ( sourceFolder.hasNext() ) {

			// Call processDocument() via the TextAnalyzer instance and store
			// the resulting document in a variable.

			try (FileWrapper fw = sourceFolder.next()) {
				final File fileToProcess = fw.baseFile();
				process(textAnalyzer, sentenceLogFolder, exporter,
						fileToProcess);
			}
			catch (NotDirectoryException e) {
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * This is an alternative to another writePhrases method. This one allows
	 * providing a specific TextAnalyzer. By doing so, the options and
	 * TokenPhaseMaker type of the TextAnalyzer instance can be set to
	 * non-default values.
	 *
	 * @param textFolderPath
	 * @param xmlOutputFolderPath
	 * @param sentenceLogFolder
	 * @param textAnalyzer
	 * @throws NotDirectoryException
	 */
	public void writePhrases(final String textFolderPath,
			final String xmlOutputFolderPath, final String sentenceLogFolder,
			final TextAnalyzer textAnalyzer) throws NotDirectoryException {

		final SourceFolder sourceFolder = new SourceFolder(textFolderPath,
				"txt");
		final File xmlDirectory = new File(xmlOutputFolderPath);
		if ( !xmlDirectory.exists() ) {
			try {
				FileUtils.forceMkdir(xmlDirectory);
			}
			catch (final IOException e) {
				System.err.println("Unable to create directory for xml output\n"
						+ e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}
		final XMLExporterRevised exporter = new XMLExporterRevised(
				AnnotationApp.EHOST, new File(xmlOutputFolderPath), true);

		int fileNumber = new File(textFolderPath).list().length;

		int fileIndex = 0;

		while ( sourceFolder.hasNext() ) {

			// Call processDocument() via the TextAnalyzer instance and store
			// the resulting document in a variable.

			try (FileWrapper fw = sourceFolder.next()) {
				final File fileToProcess = fw.baseFile();
				System.out.println("Processing file " + ++fileIndex + " of "
						+ fileNumber + ":" + fileToProcess.getAbsolutePath());
				process(textAnalyzer, sentenceLogFolder, exporter,
						fileToProcess);
			}
			catch (NotDirectoryException e) {
				throw e;
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/*
	 * Convert the RaptatTokenPhrase objects to a list of AnnotatedPhrase
	 * objects by: a. Get the list of Token objects within the RaptatTokenPhrase
	 * b. Use the constructor, AnnotatedPhrase(List<RaptatToken>,
	 * RaptatDocument, String (annConcept), String (mentionID), String
	 * (annotatorName), String (annotatorID), int (index),
	 * List<RaptatAttribute>, List<ConceptRelation>) - List<RaptatToken>
	 * constituent tokens of phrase - int (index) is index of phrase in list of,
	 * phrases found in RaptatDocument - List<RaptatAttribute>,
	 * List<ConceptRelation> -> empty i. RaptatDocument is the document the
	 * RaptatTokenPhrases came from ii. annConcept is either the _name or _value
	 * field of the RaptatTokenPhrase object (_**e.g., ABI, ANATOMY, etc.**_)
	 * iii. mentionID is a unique identifier, which can be retrieved via a call
	 * to UniqueIDGenerator.INSTANCE.getUnique() iv. annotatorName and
	 * annotatorID can both be 'RaptatABITool' v. index is the number of the
	 * RaptatTokenPhrase object within the list of RaptatTokePhrase objects when
	 * all are sorted in the list according to start offset vi. The list of
	 * RaptatAttrbute objects and ConceptRelation objects is an empty list
	 */
	private List<AnnotatedPhrase> convertRaptatTokenPhrasesToAnnotatedPhrases(
			final RaptatDocument document) {

		// get Raptat Token Phrases
		final List<RaptatTokenPhrase> tokenPhrases = document.getTokenPhrases();

		final List<AnnotatedPhrase> retVal = new ArrayList<>();

		final String ANNOTATOR_NAME = "RaptatABITool";
		final String ANNOTATOR_ID = "RaptatABITool";

		for (int index = 0; index < tokenPhrases.size(); index++) {
			final RaptatTokenPhrase raptatTokenPhrase = tokenPhrases.get(index);
			final List<RaptatToken> tokens = raptatTokenPhrase
					.getTokensAsList();
			final Set<Label> phraseLabels = raptatTokenPhrase.getPhraseLabels();
			for (final Label label : phraseLabels) {
				if ( label.hasAssignedCategory() ) {
					final String mentionId = UniqueIDGenerator.INSTANCE
							.getUnique();
					final String annConcept = label.getCategory() + "_"
							+ label.getSubcategory();
					final AnnotatedPhrase phrase = new AnnotatedPhrase(tokens,
							document, annConcept, mentionId, ANNOTATOR_NAME,
							ANNOTATOR_ID, index,
							new ArrayList<RaptatAttribute>(),
							new ArrayList<ConceptRelation>());
					retVal.add(phrase);
				}
			}
		}

		return retVal;
	}

	// Combine the list of AnnotatedPhrases created in the previous step
	// and the RaptatDocument created in step 2 into a single
	// AnnotationGroup object via a call to the constructor,
	// AnnotationGroup(RaptatDocument, List<AnnotatedPhrase>)
	private AnnotationGroup createAnnotatedGroups(
			final List<AnnotatedPhrase> annotatedPhrases,
			final RaptatDocument document) {
		final AnnotationGroup annoationGroup = new AnnotationGroup(document,
				annotatedPhrases);
		return annoationGroup;
	}

	// Export the AnnotationGroup object created in step 5 via a call to
	// the method exportAnnotationGroup(AnnotationGroup,
	// boolen (correctOffsets),
	// boolean (insertPhraseStrings),
	// boolean (useReferenceAnnotation) in the XMLRevisedExporter with all the
	// booleans set to true.
	//
	// boolen (correctOffsets), boolean (insertPhraseStrings), boolean
	// (useReferenceAnnotation) --> all true
	private void exportAnnotationGroups(final AnnotationGroup annotatedGroup,
			final XMLExporterRevised exporter) {
		final AnnotationGroup inputGroup = annotatedGroup;
		final boolean correctOffsets = true; // offsetConverter.convertAnnotatedPhraseOffsetList(
		// inputGroup.raptatAnnotations ); call
		// in method below is throwing null
		final boolean insertPhraseStrings = true;
		final boolean useReferenceAnnotations = true;
		exporter.exportAnnotationGroup(inputGroup, correctOffsets,
				insertPhraseStrings, useReferenceAnnotations);
	}

	/**
	 * 7/30/19 - DAX - Added to permit logging of all sentences
	 * https://github.com/CPHI-TVHS/ABIGirotra/issues/8
	 *
	 * @param fileName
	 * @param outputFolder
	 */
	private void logSentences(final RaptatDocument document,
			final String outputFolder, final String fileName) {
		final List<TextDocumentLine> textDocumentLines = document
				.getTextDocumentLines();

		File outputFolderFile = new File(outputFolder);
		if ( !outputFolderFile.exists() ) {
			try {
				FileUtils.forceMkdir(outputFolderFile);
			}
			catch (IOException e1) {
				System.err.println("Unable to create sentence logs\n"
						+ e1.getLocalizedMessage());
				e1.printStackTrace();
				System.exit(-1);
			}
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(
				outputFolder + "\\" + fileName + ".sentences.txt", false))) {
			for (int index = 0; index < textDocumentLines.size(); index++) {
				final TextDocumentLine textDocumentLine = textDocumentLines
						.get(index);
				writer.write(MessageFormat.format("{0} {1}\r\n", index + 1,
						textDocumentLine.lineText));
			}
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void process(final TextAnalyzer textAnalyzer,
			final String sentenceLogFolder, final XMLExporterRevised exporter,
			final File fileToProcess) {
		// Get the list of RaptatTokenPhrase objects within the resulting
		// RaptatDocument.
		final RaptatDocument document = textAnalyzer
				.processDocument(fileToProcess.getAbsolutePath());

		logSentences(document, sentenceLogFolder, fileToProcess.getName());

		// convert to Annotated Phrases
		final List<AnnotatedPhrase> annotatedPhrases = convertRaptatTokenPhrasesToAnnotatedPhrases(
				document);

		// convert to annotation groups
		final AnnotationGroup annotatedGroup = createAnnotatedGroups(
				annotatedPhrases, document);

		exportAnnotationGroups(annotatedGroup, exporter);
	}

	public static void main(final String[] args) {

	}

}
