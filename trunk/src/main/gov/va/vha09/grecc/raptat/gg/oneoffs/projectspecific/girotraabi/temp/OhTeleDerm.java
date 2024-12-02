package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.temp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.util.InvalidXMLException;

import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScoreObject;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorer;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorerParser;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.ScoringParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ZipExtractor;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.inception.CasToAnnotatedPhraseConverterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.analysis.bootstrap.BootstrapResultWriter;
import src.main.gov.va.vha09.grecc.raptat.rn.casconverter.LayerConfiguration;
import src.main.gov.va.vha09.grecc.raptat.rn.casparser.CasToAnnotationGroupConverter;
import src.main.gov.va.vha09.grecc.raptat.rn.casparser.XmiToCasImporter;

public class OhTeleDerm {

	private enum RunType {
		EXTRACT_XMI_FROM_INCEPTION_ZIP, INCEPTION_XMI_TO_EHOST_XML, REPLACE_EHOST_COLORS, IAA, CANCEL_PROCESS;

		@Override
		public String toString() {
			switch (this) {
			case EXTRACT_XMI_FROM_INCEPTION_ZIP:
				return "[Extract]  xmi from Inception zip";
			case INCEPTION_XMI_TO_EHOST_XML:
				return "[Convert]  Inception xmi to eHOST xml";
			case IAA:
				return "[Calculate]  interannotator agreement";
			case REPLACE_EHOST_COLORS:
				return "[Replace]  color of eHOST annotations";
			case CANCEL_PROCESS:
				return "[Cancel]  processing";
			default:
				return null;
			}
		}
	}

	private final static String INCEPTION_CAS_TOKEN_NAME = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token";
	private final static String INCEPTION_CAS_SENTENCE_NAME = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence";
	private final static JFrame mainFrame = new JFrame();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FileUtilities.setParentFrame(mainFrame);

		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

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

				/*
				 * Extract the *.xmi files from projects exported from Inception
				 * using the 1.1 xml form during export. This is used to unzip
				 * files.
				 *
				 * To use:
				 *
				 * 1) Create a "Run configuration" launcher in Eclipse
				 * (right-click green circle with white arrow in Eclipse menu).
				 *
				 * 2) In the 'Program arguments' box under the 'Arguments' tab
				 * in the dialog box for your run configuration, insert the
				 * single string, "EXTRACT_XMI_FROM_INCEPTION_ZIP".
				 *
				 * 3) Run your new configuration by selecting it and then
				 * clicking on run in the bottom right of the launch
				 * configuration dialog box.
				 *
				 * 4) Note that, with this configuration, the program will ask
				 * you to select the directory containing the zip files you want
				 * to extract. If preferred, you can avoid this dialog by
				 * placing the full path to the same directory on the second
				 * line of the 'Program arguments' box.
				 */
				case EXTRACT_XMI_FROM_INCEPTION_ZIP: {
					int filePathArgsIndex = 1;
					extractXmiFromInceptionZip(argsAsList, filePathArgsIndex);
				}
					break;

				/*
				 * Determine inter-annotator agreement. This is generally run
				 * from a '.props' file that specifies the parameters including
				 * 1) the two, separate directories containing *.xml files for
				 * each of two annotators, 2) a single directory containing the
				 * *.txt files they both annotated (corresponding to the *.xml
				 * files containing the annotations), and the path to the eHOST
				 * schema file (generally named 'projectschema.xml' but can be
				 * named anything) specifying the annotation
				 * configuration/schema.
				 *
				 * The absolution path representing the location of the '.props'
				 * file can be specified as a
				 */
				case IAA: {
					calculateIAA();
				}
					break;

				/*
				 * Process to take a set of xmi 1.1 files exported from the
				 * Inception annotation program and convert them into xml files
				 * that can be imported into eHOST or used by RapTAT for
				 * training, inter-annotator agreement, etc.
				 *
				 * The process assumes that the xmi files are stored in a
				 * directory named 'xmiFiles' within a specified parent
				 * directory. It also assumes that the text files that the xmi
				 * files refer to is stored in a directory name 'txtFiles' and
				 * that the text file names are the same as those referred to
				 * within the xmi files. The 'txtFiles' directory should be at
				 * the same level as the 'xmiFiles' directory and have the same
				 * parent directory as 'xmiFiles.'
				 *
				 * The process also assumes that the parent directory of
				 * 'xmiFiles' and 'txtFiles' contains another directory,
				 * 'AnnotationConfig.' That directory should contain 4 files:
				 *
				 * 1) 'attributes.txt' (text file with 2 columns, the first for
				 * long-form Inception layer name and the second for short-form
				 * Inception feature/attribute name - note that Inception does
				 * not distinguish features from attributes, so users must
				 * decide how they want an Inception feature displayed in eHOST)
				 *
				 * 2) 'features.txt' (text file with 3 columns, the first for
				 * long-form Inception layer name, the second for short-form
				 * feature name, and the third for "granularity" level with
				 * lower numbers indicating less granular, more general features
				 * - feature names are combined form least to most granularity
				 * when transformed for eHOST into a single concept).
				 *
				 * 3) 'layers.txt' (text file with 3 columns, the first for the
				 * layer type - 'span' or 'document' or 'sentence' or
				 * 'explicit_relation', the second layer for long-form layer
				 * name, and the third for the long-form name of the layer that
				 * contains any explicit layer - insert 'NULL' in third column
				 * when the layer is not an 'explicit_relation' type). Note that
				 * "explicit" layers are those explicitly specified by the
				 * Inception user to contain a relationship between two concepts
				 * in another layer. We define the "containing layer" as the one
				 * in which the two related concepts occur.
				 *
				 * 4) A 'TypeSystem.xml' file created by Inception and stored in
				 * the same directory as each of the xmi files exported by
				 * Inception. This file provides full specification of the
				 * Inception annotation schema.
				 *
				 * Note that the results are stored within a subdirectory called
				 * 'xmlOutput_DATETIMESTAMP' at the same directory level as the
				 * 'xmiFiles' and 'txtFiles.'
				 */
				case INCEPTION_XMI_TO_EHOST_XML: {
					int filePathArgsIndex = 1;
					convertXmiToXmlFiles(argsAsList, filePathArgsIndex);
				}
					break;
				case REPLACE_EHOST_COLORS: {
					FileUtilities.replaceEhostColors(Collections.emptyList());
				}
					break;
				default:
					break;
				}

				String message;
				String title = "";
				if ( runType == RunType.CANCEL_PROCESS ) {
					message = "No process selected";
				}
				else {
					message = "'" + runType.toString() + "' is complete";
					title = "Process Complete";
				}
				reportMessage(message, title);

				System.exit(0);

			}

			private void calculateIAA() {
				ScoringParameterObject parameters;
				if ( args.length > 0 ) {
					PerformanceScorerParser parser = new PerformanceScorerParser();
					parameters = parser.parseCommandLine(args);
				}
				else {
					parameters = getScoringParameters();
				}
				if ( parameters == null ) {
					mainFrame.setAlwaysOnTop(true);
					mainFrame.toFront();
					JOptionPane.showMessageDialog(null,
							"No IAA parameters selected", "IAA cancelled",
							JOptionPane.INFORMATION_MESSAGE);
					mainFrame.setAlwaysOnTop(false);
					return;
				}

				File textDirectory = new File(parameters.textDirectoryPath);
				File annotatorOneAnnotations = new File(
						parameters.raptatDirectoryPath);
				File annotatorTwoAnnotations = new File(
						parameters.referenceDirectoryPath);
				File schemaFile = new File(parameters.schemaFilePath);
				File conceptsFile = null;
				if ( (parameters.conceptsFilePath != null)
						&& !parameters.conceptsFilePath.isEmpty() ) {
					conceptsFile = new File(parameters.conceptsFilePath);
				}

				Hashtable<PerformanceScoreObject, int[]> resultScores = PerformanceScorer
						.compareFileSets(textDirectory, annotatorTwoAnnotations,
								annotatorOneAnnotations, schemaFile,
								conceptsFile, parameters.annotationApp,
								parameters.removeUnmatched);

				BootstrapResultWriter resultWriter = new BootstrapResultWriter(
						textDirectory.getAbsolutePath(), "TestData",
						RaptatConstants.CROSS_VALIDATION_CONCEPT_RESULT_TITLES);
			}

			private void convertXmiToXmlFiles(List<String> argsAsList,
					int filePathArgsIndex) {

				File projectDirectory = FileUtilities.getDirectory(argsAsList,
						filePathArgsIndex,
						"Select parent folder of \"xmiFiles,\" \"txtFiles,\" and \"inceptionConfig\" folders");
				if ( (projectDirectory == null)
						|| !projectDirectory.isDirectory() ) {
					reportMessage("Parent folder is null or not a directory",
							"");
					return;
				}

				/*
				 * Set up variables to be used for processing
				 */
				String xmiDirectoryName = projectDirectory.getAbsolutePath()
						+ File.separator + "xmiFiles";
				File xmiDirectory = new File(xmiDirectoryName);
				if ( !xmiDirectory.exists() ) {
					reportMessage(
							"No xmiFiles folder found in selected parent folder",
							"");
					return;
				}

				String txtDirectoryName = projectDirectory.getAbsolutePath()
						+ File.separator + "txtFiles";
				File txtDirectory = new File(txtDirectoryName);
				if ( !txtDirectory.exists() ) {
					reportMessage(
							"No txtFiles folder found in selected parent folder",
							"");
					return;
				}

				String xmlOutDirectoryName = projectDirectory.getAbsolutePath()
						+ File.separator + "convertedXmlOutput_"
						+ GeneralHelper.getTimeStamp();
				File xmlOutDirectory = new File(xmlOutDirectoryName);
				xmlOutDirectory.mkdirs();

				try {
					String inceptionConfigDirectoryPath = projectDirectory
							+ File.separator + "inceptionConfig";
					if ( !(new File(inceptionConfigDirectoryPath)).exists() ) {
						reportMessage(
								"No inceptionConfig folder found in selected parent folder",
								"");
						return;
					}
					LayerConfiguration annotationConfiguration = new LayerConfiguration(
							inceptionConfigDirectoryPath);

					/*
					 * Note that TypeSystem.xml file is assume to be and should
					 * be placed within 'configDirectoryName' directory
					 */
					String typeFileName = "TypeSystem.xml";
					TypeSystem ts = getTypeSystem(xmiDirectory,
							new File(inceptionConfigDirectoryPath),
							typeFileName);

					/*
					 * Create importer to get cas from xmi files
					 */
					XmiToCasImporter xmiToCasImporter = new XmiToCasImporter(
							getFile(inceptionConfigDirectoryPath,
									typeFileName));

					/*
					 * Create exporter to convert the imported files to eHOST
					 * *.xml
					 */
					XMLExporterRevised xmlExporter = new XMLExporterRevised(
							AnnotationApp.EHOST, xmlOutDirectory, true);

					/*
					 * Do conversion and export resulting files
					 */
					convertXmiToXmlFiles(xmiToCasImporter, xmlExporter, ts,
							annotationConfiguration, xmiDirectory,
							txtDirectory);

				}
				catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}

			private void convertXmiToXmlFiles(XmiToCasImporter casImporter,
					XMLExporterRevised exporter, TypeSystem ts,
					LayerConfiguration layerConfiguration, File xmiDirectory,
					File txtDirectory) throws Exception {

				/*
				 * We'll need these two types defined within Inception, token
				 * and sentence, to get the corresponding tokens and sentences
				 * for RapT
				 */
				Type tokenType = ts.getType(INCEPTION_CAS_TOKEN_NAME);
				Type sentenceType = ts.getType(INCEPTION_CAS_SENTENCE_NAME);

				File[] xmiFiles = xmiDirectory
						.listFiles(file -> !file.isDirectory() && file.getName()
								.toLowerCase().endsWith(".xmi"));

				for (File xmiFile : xmiFiles) {

					CAS cas = casImporter.importCas(xmiFile);
					String documenTitle = CasToAnnotationGroupConverter
							.getDocumentMetadata(cas, "documentTitle");
					File txtFile = new File(txtDirectory, documenTitle);

					/*
					 * We create a new CasToEhostXmlConverter for each xmiFile
					 * due to the number of variables involved in processing
					 * each one. We create these as fields during construction
					 * to avoid passing a large number of variables as
					 * parameters.
					 */
					CasToAnnotatedPhraseConverterRevised caspToApConverter = new CasToAnnotatedPhraseConverterRevised(
							layerConfiguration, cas, tokenType);
					CasToAnnotationGroupConverter casToEhostConverter = new CasToAnnotationGroupConverter(
							tokenType, sentenceType, caspToApConverter);

					AnnotationGroup annotationGroup = casToEhostConverter
							.convertToAnnotationGroup(layerConfiguration,
									xmiFile.getAbsolutePath(),
									txtFile.getAbsolutePath());

					String annotatorName = CasToAnnotationGroupConverter
							.getDocumentMetadata(cas, "documentId");
					annotationGroup.setAnnotatorName(annotatorName);

					exporter.exportReferenceAnnotationGroup(annotationGroup,
							true, true);
				}

			}

			/**
			 * For each of the folders provided as a parameter, find all the xmi
			 * files presenting the annotation of a file by an annotator, name
			 * the file based on the name of the file that was annotated, and
			 * save the renamed xmi files within targetDirectory.
			 *
			 * @param directoriesWithXmiFiles
			 * @throws IOException
			 */
			private List<File> createXmiFolders(
					List<File> directoriesWithXmiFiles, File outputDirectory)
					throws IOException {

				List<File> xmiFolders = new ArrayList<>();
				String subdirectoryName = "annotation";
				Set<String> excludedNames = new HashSet<>(
						List.of("admin", "INITIAL_CAS", "TypeSystem"));

				for (File xmiContainingDirectory : directoriesWithXmiFiles) {

					List<File> foundFiles = DirectorySearcher
							.getUnexcludedFiles(xmiContainingDirectory,
									subdirectoryName, excludedNames);
					if ( foundFiles.isEmpty() ) {
						System.err.println("No files found in "
								+ xmiContainingDirectory.getAbsolutePath());
						continue;
					}

					String annotatorName = GeneralHelper.removeFileExtension(
							foundFiles.get(0).getName(), '.');

					File annotatorsFolder = new File(outputDirectory,
							annotatorName);
					if ( !annotatorsFolder.exists()
							&& !annotatorsFolder.mkdirs() ) {
						System.err.println("Unable to store annotator files in "
								+ annotatorsFolder.getAbsolutePath());
						continue;
					}

					for (File foundXmiFile : foundFiles) {

						String annotatedFileName = foundXmiFile.getParentFile()
								.getParentFile().getName();
						annotatedFileName = GeneralHelper.removeFileExtension(
								annotatedFileName, '.') + (".xmi");
						Path sourcePath = foundXmiFile.toPath();
						Path targetPath = annotatorsFolder.toPath()
								.resolve(annotatedFileName);
						Files.copy(sourcePath, targetPath,
								StandardCopyOption.REPLACE_EXISTING);

						xmiFolders.add(targetPath.toFile());
					}
				}

				return xmiFolders;
			}

			private void extractXmiFromInceptionZip(List<String> argsAsList,
					int filePathArgsIndex) {
				/*
				 * Get directory containing zipped directories with *.xmi files
				 * for extraction.
				 */
				File directoryWithZipFiles = FileUtilities.getDirectory(
						argsAsList, filePathArgsIndex,
						"Select directory with zip files");

				String zipExtension = ".zip";

				Set<String> excludedFileNames = new HashSet<>();
				excludedFileNames.add("admin.zip");
				excludedFileNames.add("INITIAL_CAS.zip");

				if ( !directoryWithZipFiles.isDirectory() ) {
					System.err.println(
							"The path provided as 'zipDirectoryPath is not a directory");
					System.exit(-1);
				}

				List<File> extractedFoldersandFiles = new ArrayList<>();
				File[] zipFiles = directoryWithZipFiles
						.listFiles((dir, name) -> name.endsWith(zipExtension));
				for (File zippedFile : zipFiles) {
					File folderWithExtractedFiles = ZipExtractor
							.extractZip(zippedFile, false);
					extractedFoldersandFiles.add(folderWithExtractedFiles);
				}

				try {
					File xmiOutputDirectory = new File(directoryWithZipFiles,
							"xmiOutput" + GeneralHelper.getTimeStamp());

					createXmiFolders(extractedFoldersandFiles,
							xmiOutputDirectory);
				}
				catch (IOException e) {
					System.err
							.println("Unable to create output folders for xmi\n"
									+ e.getLocalizedMessage());
					e.printStackTrace();
					System.exit(-1);
				}

			}

			private CAS getCas(File xmiFile, File typeSystemFile) {
				CAS cas = null;
				try {
					XmiToCasImporter casImporter = new XmiToCasImporter(
							typeSystemFile);
					cas = casImporter.importCas(xmiFile);
					System.out.println("CAS imported");
				}
				catch (InvalidXMLException | IOException e) {
					e.printStackTrace();
					System.err.println(e.getMessage());
				}
				return cas;
			}

			private File getFile(String startDirectory,
					String... pathElements) {
				StringBuilder sb = new StringBuilder(startDirectory);
				for (String curArg : pathElements) {
					if ( !curArg.isBlank() ) {
						sb.append(File.separator).append(curArg);
					}
				}
				return new File(sb.toString());
			}

			private ScoringParameterObject getScoringParameters() {
				String textDirectoryPath = ScoringParameterObject
						.getTextDirectoryPath();
				if ( (textDirectoryPath == null)
						|| !(new File(textDirectoryPath).isDirectory()) ) {
					return null;
				}

				FileUtilities.setChooserStartDirectory(
						(new File(textDirectoryPath)).getParentFile());
				String annotatorOneDirectoryPath = ScoringParameterObject
						.getAnnotatorDirectoryPath(1);
				if ( (annotatorOneDirectoryPath == null)
						|| !(new File(annotatorOneDirectoryPath)
								.isDirectory()) ) {
					return null;
				}

				FileUtilities.setChooserStartDirectory(
						(new File(annotatorOneDirectoryPath)).getParentFile());
				String annotatorTwoDirectoryPath = ScoringParameterObject
						.getAnnotatorDirectoryPath(2);
				if ( (annotatorTwoDirectoryPath == null)
						|| !(new File(annotatorTwoDirectoryPath)
								.isDirectory()) ) {
					return null;
				}

				FileUtilities.setChooserStartDirectory(
						(new File(annotatorTwoDirectoryPath)).getParentFile());
				String schemaFilePath = ScoringParameterObject
						.getSchemaFilePath();
				if ( (schemaFilePath == null)
						|| !(new File(schemaFilePath).isFile()) ) {
					return null;
				}

				return new ScoringParameterObject.Builder()
						.textDirectoryPath(textDirectoryPath)
						.raptatDirectoryPath(annotatorOneDirectoryPath)
						.referenceDirectoryPath(annotatorTwoDirectoryPath)
						.schemaFilePath(schemaFilePath)
						.annotationApp(AnnotationApp.EHOST)
						.conceptsFilePath(null).removeUnmatched(false).build();
			}

			/*
			 * Loads a single xmi file from the xmiDirectoryName within the
			 * directory, projectDirectoryPath, and determine the TypeSystem
			 * instance for all the CAS associated with the files in the
			 * xmiDirectoryName directory. Note that this assumes that all the
			 * 'xmi' files use the same TypeSystem.
			 */
			private TypeSystem getTypeSystem(File xmiDirectory,
					File configDirectory, String typeFileName)
					throws IOException {

				File[] xmiFiles = xmiDirectory.listFiles(file -> file.isFile()
						&& file.getName().endsWith("xmi"));
				if ( xmiFiles.length < 1 ) {
					throw new IOException(
							"No accessible xmi files within xmi directory");
				}
				File xmiFile = xmiFiles[0];
				CAS cas = getCas(xmiFile, getFile(
						configDirectory.getAbsolutePath(), typeFileName));
				return cas.getTypeSystem();
			}

			/*
			 * Loads a single xmi file from the xmiDirectoryName within the
			 * directory, projectDirectoryPath, and determine the TypeSystem
			 * instance for all the CAS associated with the files in the
			 * xmiDirectoryName directory. Note that this assumes that all the
			 * 'xmi' files use the same TypeSystem.
			 */
			private TypeSystem getTypeSystem(String projectDirectoryPath,
					String xmiDirectoryName, String configDirectoryName,
					String typeFileName) throws IOException {

				File xmiDirectory = getFile(projectDirectoryPath,
						xmiDirectoryName);
				File configDirectory = getFile(projectDirectoryPath,
						configDirectoryName);
				return getTypeSystem(xmiDirectory, configDirectory,
						typeFileName);
			}

			/**
			 * @param message
			 * @param title
			 */
			private void reportMessage(String message, String title) {
				mainFrame.setAlwaysOnTop(true);
				mainFrame.toFront();
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.INFORMATION_MESSAGE);
				mainFrame.setAlwaysOnTop(false);
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
		});
	}
}
