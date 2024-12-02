/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;

/** @author Glenn T. Gobbel Apr 15, 2016 */
public class FileUtilities {
	/*
	 * Specifies how name changes are implemented based on the regex in the call
	 * renameFilesWithRegex(String regex, String insertText, RenamingMethod
	 * method). The RenamingMethod determines whether the insertText string
	 * replaces, is appended to, or prepended to the part of the file name
	 * matching the string.
	 */
	private enum RenamingMethod {
		REPLACE, PREPEND, APPEND;
	}

	private enum RunType {
		RENAME, RENAME_WITH_REGEX, COPY, COPY_GENERIC, REPLACE_PTSD_TXT, RENAME_REPLACE_PTSD_FILE, DIVIDE_TRAIN_TEST, COPY_RECURSIVE, EXTRACT_TEXT_ROWS, COPY_BY_LIST, GET_FILE_CREATION_TIMES, COPY_DUPLICATE_NAMED_FILES_FROM_OTHER_DIRECTORY;
	}

	public static void main(final String[] args) {
		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RunType runType = RunType.COPY_DUPLICATE_NAMED_FILES_FROM_OTHER_DIRECTORY;

				switch (runType) {
				case COPY:
					moveFiles();
					break;
				case COPY_BY_LIST:
					copyFilesByList();
					break;
				case COPY_RECURSIVE:
					String startPath = null;
					copyRecursive(startPath);
					break;
				case COPY_GENERIC:
					moveFilesGeneric();
					break;
				case GET_FILE_CREATION_TIMES:
					// String pathToDirectory =
					// "U:\\Workspaces\\AMI\\Gobbel\\Moonstone\\MoonstoneDemo_8_14_2018_Vanderbilt_FASTER\\\\TestRunDocs_1thousand_Output_181003";
					String pathToDirectory = "U:\\Workspaces\\AMI\\Gobbel\\Moonstone\\MoonstoneDemo_8_14_2018_Vanderbilt_SLOW\\\\TestRunDocs_1thousand_Output_181003";
					String[] extensionFilter = new String[] {
							"xml"
					};
					getFileCreationTimes(pathToDirectory, extensionFilter);
					break;
				case RENAME:
					renameFiles();
					break;
				case RENAME_WITH_REGEX:
					String prependString = "txt";
					String matchString = "txzt";
					RenamingMethod renamingMethod = RenamingMethod.REPLACE;
					renameFilesWithRegex(matchString, prependString,
							renamingMethod);
					break;
				case REPLACE_PTSD_TXT:
					replacePtsdText();
					break;
				case RENAME_REPLACE_PTSD_FILE:
					renameReplacePtsdFile();
					break;
				case DIVIDE_TRAIN_TEST:
					trainTestDivide();
					break;
				case EXTRACT_TEXT_ROWS:
					String extractionFilePath = "D:\\MHA_NLP\\Glenn\\ElliotCorpusForRaptatTesting_160923"
							+ "\\OutputTest\\PsychotherapyOutpatientEncounterCorpusClean_161202.txt";
					File extractionFile = new File(extractionFilePath);
					extractTextRows(extractionFile);
					break;
				case COPY_DUPLICATE_NAMED_FILES_FROM_OTHER_DIRECTORY:
					String pathWithFileNames = "P:\\ORD_Girotra_201607120D\\Glenn\\misc\\temp\\saved";
					String pathWithFilesForCopying = "P:\\ORD_Girotra_201607120D\\Glenn\\misc\\temp\\SLC1000FileOutput";
					String targetDirectoryPath = "P:\\ORD_Girotra_201607120D\\Glenn\\misc\\temp\\targetSLC";
					copyDuplicateFiles(pathWithFileNames,
							pathWithFilesForCopying, targetDirectoryPath);
					break;
				}
				System.exit(0);
			}

			private void copyDuplicateFiles(final String pathWithFileNames,
					final String pathWithFilesForCopying,
					final String targetDirectoryPath) {
				try {

					File targetDirectory = new File(targetDirectoryPath);
					FileUtils.forceMkdir(targetDirectory);

					Set<String> namesForCopying = Files
							.list(Paths.get(pathWithFileNames))
							.map(path -> path.getFileName().toString())
							.collect(Collectors.toCollection(HashSet::new));
					namesForCopying.forEach(System.out::println);

					List<Path> pathsForCopying = Files
							.list(Paths.get(pathWithFilesForCopying))
							.filter(path -> namesForCopying
									.contains(path.getFileName().toString()))
							.collect(Collectors.toList());

					for (Path copyFromPath : pathsForCopying) {
						String targetPathName = targetDirectoryPath
								+ File.separatorChar
								+ copyFromPath.getFileName();
						Files.copy(copyFromPath, Paths.get(targetPathName));
						System.out.println(
								"Copied from " + copyFromPath.toString() + " to"
										+ targetPathName);
					}

				}
				catch (IOException e) {
					e.printStackTrace();
				}

			}

			private void copyFiles(final String startDirectory,
					final String newPath) {
				File startTextDirectory = new File(startDirectory);
				for (File curFile : startTextDirectory.listFiles()) {
					String fileName = curFile.getName();
					File newFile = new File(
							newPath + File.separator + fileName);
					try {
						FileUtils.copyFile(curFile, newFile);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			private void copyFilesByList() {
				String pathToFileSource = selectFileSource();
				List<File> fileList = getFileList(pathToFileSource);

				File destDir = GeneralHelper
						.getDirectory("Select target directory");
				for (File srcFile : fileList) {
					try {
						System.out.println("Copying:" + srcFile.getName());
						FileUtils.copyFileToDirectory(srcFile, destDir);
					}
					catch (IOException e) {
						System.err
								.println("Unable to copy " + srcFile.getName());
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}

			private void copyRecursive(final String startDirectoryPath) {
				String thePrompt = "Select directory to be copied";
				File sourceDirectory = GeneralHelper.getDirectory(thePrompt,
						startDirectoryPath);

				thePrompt = "Copy into which directory?";
				File targetDirectory = GeneralHelper.getDirectory(thePrompt,
						sourceDirectory.getParent());

				String copiedDirectoryPath = targetDirectory.getAbsolutePath()
						+ File.separator + sourceDirectory.getName();
				int replaceFile = JOptionPane.YES_OPTION;
				if ( new File(copiedDirectoryPath).exists() ) {
					replaceFile = JOptionPane.showConfirmDialog(null,
							"Target already exists.\\nReplace?",
							"Replace existing file", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
				}
				if ( replaceFile == JOptionPane.YES_OPTION ) {
					try {
						FileUtils.copyDirectoryToDirectory(sourceDirectory,
								targetDirectory);
					}
					catch (IOException e) {
						GeneralHelper.errorWriter(
								"Unable to copy directory:" + e.getMessage());
						e.printStackTrace();
					}
				}
			}

			/**
			 * Takes a tab-delimited text file and outputs each row to a
			 * separate file. The name of the output is created by concatenation
			 * of the initial elements in each row. The last element is written
			 * to the file.
			 *
			 * @param extractionFilePath
			 */
			private void extractTextRows(File sourceFile) {
				if ( (sourceFile == null) || !sourceFile.exists() ) {
					sourceFile = GeneralHelper
							.getFile("Select file for row extraction");
				}
				String sourceParentPath = sourceFile.getParent();

				DataImportReader dataReader = new TabReader(sourceFile);
				String[] rowInput = null;
				int columns = dataReader.getNumDataElements();

				int rowNumberTag = 0;
				HashSet<String> fileTitles = new HashSet<>();
				NumberFormat indexFormatter = new DecimalFormat("0000");
				PrintWriter pw = null;

				while ( (rowInput = dataReader.getNextData()) != null ) {
					String outputTitle = getOutputTitle(sourceFile, rowInput,
							columns, rowNumberTag, fileTitles, indexFormatter);

					File outputFile = new File(
							sourceParentPath + File.separator + outputTitle);
					try {
						pw = new PrintWriter(outputFile);
						String outputText = rowInput[rowInput.length - 1]
								.replaceAll("(?m) {5,}", "\n");
						outputText = outputText.replaceAll("(?m)^\" *|\\s*\"$",
								"");
						System.out.println(
								"Outputting file " + outputFile.getName());
						pw.println(outputText);
					}
					catch (FileNotFoundException e) {
						System.out.println(e.getMessage()
								+ "\nUnable to output file " + outputTitle);
						e.printStackTrace();
					}
					finally {
						if ( pw != null ) {
							pw.close();
						}
					}
				}
			}

			private void getFileCreationTimes(final String pathToDirectory,
					final String[] extensionFilter) {
				Collection<File> fileList = FileUtils.listFiles(
						new File(pathToDirectory), extensionFilter, false);
				int fileNumber = 0;

				for (File file : fileList) {
					try {
						BasicFileAttributes fileAttributes = Files
								.readAttributes(file.toPath(),
										BasicFileAttributes.class);
						System.out.println(
								++fileNumber + "\t" + file.getName() + "\t"
										+ fileAttributes.creationTime()
												.toMillis()
										+ "\t" + fileAttributes.size());
					}
					catch (IOException e) {
						System.err.println("Unable to read file:"
								+ file.getName() + "\t" + e.getMessage());
					}
				}
			}

			private List<File> getFileList(final String pathToFileSource) {
				List<File> fileList = new ArrayList<>();

				try (BufferedReader br = new BufferedReader(
						new FileReader(pathToFileSource))) {
					String line = null;
					while ( (line = br.readLine()) != null ) {
						fileList.add(new File(line));
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					System.err.println("Error finding or reading file, "
							+ ex.getLocalizedMessage());
					System.exit(-1);
				}

				return fileList;
			}

			private RaptatPair<Integer, Integer> getMatchingIndices(
					final Matcher regexMatcher, final String fileName) {

				Integer foundStart = regexMatcher.regionStart();
				Integer foundEnd = regexMatcher.regionEnd();
				return new RaptatPair<>(foundStart, foundEnd);
			}

			/**
			 * @param sourceFile
			 * @param rowInput
			 * @param columns
			 * @param rowNumberTag
			 * @param fileTitles
			 * @param indexFormatter
			 * @return
			 */
			private String getOutputTitle(final File sourceFile,
					final String[] rowInput, final int columns,
					final int rowNumberTag, final HashSet<String> fileTitles,
					final NumberFormat indexFormatter) {
				StringBuilder titleBuilder = new StringBuilder("");
				String sourceName = sourceFile.getName();

				if ( columns > 1 ) {
					titleBuilder.append(rowInput[0]);
					for (int i = 1; i < (columns - 1); i++) {
						titleBuilder.append("_").append(rowInput[i]);
					}
					if ( fileTitles.contains(titleBuilder.toString()) ) {
						titleBuilder.append("_")
								.append(indexFormatter.format(rowNumberTag));
					}
				}
				else {
					titleBuilder.append(sourceName).append("_")
							.append(indexFormatter.format(rowNumberTag));
				}
				return titleBuilder.append(".txt").toString();
			}

			private void moveFiles() {
				final String[] copyFromDirectories = {
						"D:\\ORD_Matheny_201312053D\\Glenn\\NancyBlocks"
						// \\Block_02_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlockss\\Block_03_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_04_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_05_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_06_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_07_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_08_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_09_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_10_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_11_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_12_Liz",
						// "D:\\ORD_Matheny_201312053D\\Glenn\\LizBlocks\\Block_13_Liz",
				};
				final String relativePathToXml = "\\NancyXML";
				final String copyToDirectory = "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_Analysis_160427";

				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(final File path) {
						String theName = path.getName();

						if ( theName.contains("") ) {
							return true;
						}

						return false;
					}
				};

				String newPathToXml = copyToDirectory + relativePathToXml;
				try {
					FileUtils.forceMkdir(new File(newPathToXml));
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				for (String curDirectoryPath : copyFromDirectories) {
					File curDirectory = new File(curDirectoryPath);
					File[] fileBlocks = curDirectory.listFiles(filter);

					for (File curFile : fileBlocks) {
						if ( curFile.getName().contains("") ) {
							copyFiles(curFile.getAbsolutePath(), newPathToXml);
						}
					}
				}
			}

			private void moveFilesGeneric() {
				final String copyToDirectory = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis";
				final String trainRelativePath = "\\TrainTextFiles";
				final String testRelativePath = "\\TestTextFiles";
				final String filterString = ".txt";
				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(final File path) {
						String theName = path.getName();

						if ( theName.contains(filterString) ) {
							return true;
						}

						return false;
					}
				};

				String testPath = copyToDirectory + testRelativePath;
				String trainPath = copyToDirectory + trainRelativePath;

				try {
					FileUtils.forceMkdir(new File(testPath));
					FileUtils.forceMkdir(new File(trainPath));
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				String pathFileString = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis_Old\\PatientNotesAndAnalysis_150402\\TextFileList_TrainTest_160509.txt";
				File directoryPathFile = new File(pathFileString);
				try {
					List<String> inputStrings = FileUtils
							.readLines(directoryPathFile);
					for (String curString : inputStrings) {
						String[] pathTarget = curString.split("\t");
						String curDirectoryPath = pathTarget[0];
						boolean isTestDirectory = pathTarget[1]
								.equalsIgnoreCase("Test");
						if ( (curDirectoryPath != null)
								&& (curDirectoryPath.length() > 0) ) {
							System.out.println("Transferring Directory to "
									+ pathTarget[1] + ":" + curDirectoryPath);
							File curDirectory = new File(curDirectoryPath);
							File[] fileBlocks = curDirectory.listFiles(filter);

							for (File curFile : fileBlocks) {
								String fileName = curFile.getName();
								String assignedPath = isTestDirectory ? testPath
										: trainPath;
								File newFile = new File(assignedPath
										+ File.separator + fileName);
								try {
									FileUtils.copyFile(curFile, newFile);
								}
								catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			private void renameFiles() {
				File startDirectory = GeneralHelper
						.getDirectory("Identify directory containing files");
				File[] files = startDirectory.listFiles();
				for (File curFile : files) {
					String curFilePath = curFile.getAbsolutePath();
					File newFile = new File(curFilePath + ".txt");
					try {
						FileUtils.copyFile(curFile, newFile);
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			private void renameFilesWithRegex(final String regex,
					final String inputText, final RenamingMethod method) {
				File startDirectory = GeneralHelper
						.getDirectory("Identify directory containing files");
				File[] files = startDirectory.listFiles();
				if ( files.length > 0 ) {
					Pattern regexPattern = Pattern.compile(regex);
					String fileParent = files[0].getParent();
					for (File curFile : files) {
						String fileName = curFile.getName();
						Matcher regexMatcher = regexPattern.matcher(fileName);
						if ( regexMatcher.find() ) {
							RaptatPair<Integer, Integer> indices;
							int insertionIndex;

							switch (method) {
							case REPLACE:
								fileName = fileName.replaceFirst(regex,
										inputText);
								break;
							case PREPEND:
								indices = getMatchingIndices(regexMatcher,
										fileName);
								insertionIndex = indices.left;
								fileName = fileName.substring(0, insertionIndex)
										+ inputText
										+ fileName.substring(insertionIndex);
								break;
							case APPEND:
								indices = getMatchingIndices(regexMatcher,
										fileName);
								insertionIndex = indices.right;
								fileName = fileName.substring(0, insertionIndex)
										+ inputText
										+ fileName.substring(insertionIndex);
								break;
							default:
								return;
							}

							File newFile = new File(
									fileParent + File.separator + fileName);
							curFile.renameTo(newFile);
						}
					}
				}
			}

			private void renameReplacePtsdFile() {
				String fileDirectory = "D:\\MHA_NLP\\PTSD_XML\\";
				File[] filesOfInterest = new File(fileDirectory).listFiles();
				for (File curFile : filesOfInterest) {
					String curFileName = curFile.getName();
					if ( curFileName.contains("-fixed") ) {
						String replacedFileName = curFileName
								.replaceAll("\\-fixed", "");
						File replacedFile = new File(fileDirectory
								+ File.separator + replacedFileName);
						try {
							FileUtils.copyFile(curFile, replacedFile);
							FileUtils.forceDelete(curFile);
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			private void replacePtsdText() {
				String directoryName = "D:\\MHA_NLP\\PTSD_XML";
				Pattern regex = Pattern.compile("Block_.+-fixed.+txt",
						Pattern.MULTILINE);
				Matcher matcher = regex.matcher("");
				File[] files = new File(directoryName).listFiles();
				for (File curFile : files) {
					try {
						// FileReader reads text files in the default encoding.
						FileReader fileReader = new FileReader(curFile);

						// Always wrap FileReader in BufferedReader.
						BufferedReader bufferedReader = new BufferedReader(
								fileReader);
						String fileString = org.apache.uima.util.FileUtils
								.reader2String(bufferedReader);
						matcher.reset(fileString);
						bufferedReader.close();

						String[] fileNameStrings = curFile.getName()
								.split("\\.");
						String txtFileName = fileNameStrings[0] + ".txt";
						if ( matcher.find() ) {
							fileString = matcher.replaceFirst(txtFileName);
						}

						PrintWriter pw = new PrintWriter(curFile);
						pw.print(fileString);
						pw.flush();
						pw.close();
					}
					catch (FileNotFoundException ex) {
						System.out.println("Unable to open file '"
								+ curFile.getName() + "'");
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			/** @return */
			private String selectFileSource() {
				String chosenFilePath = null;
				JFileChooser fileChooser = new JFileChooser(
						OptionsManager.getInstance().getLastSelectedDir());

				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter(
						"Select '.txt' file containing files for copying",
						"txt"));

				if ( fileChooser
						.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ) {
					OptionsManager.getInstance().setLastSelectedDir(
							fileChooser.getCurrentDirectory());
					chosenFilePath = fileChooser.getSelectedFile()
							.getAbsolutePath();
				}
				return chosenFilePath;
			}

			/**
			 * Divides files in a directory into training and test set based on
			 * a file, indicated by testFilesPath, that is a list of the ones to
			 * include as test. Those not included are part of the training set.
			 */
			private void trainTestDivide() {
				String testFilesPath = "D:\\MHA_NLP\\WatsonPTSDannotationData\\TestGroupXMLList.txt";
				String sourceDirectory = "D:\\MHA_NLP\\WatsonPTSDannotationData\\PTSD_TXT_Full_Cleaned";
				String testTargetDirectory = "D:\\MHA_NLP\\WatsonPTSDannotationData\\PTSD_TXT_Test";
				String trainTargetDirectory = "D:\\MHA_NLP\\WatsonPTSDannotationData\\PTSD_TXT_Train";

				try {
					List<String> testFilePaths = FileUtils
							.readLines(new File(testFilesPath));
					HashSet<String> testFileKeys = new HashSet<>(
							testFilePaths.size());
					for (String filePath : testFilePaths) {
						int fileNameDepth = 4;
						String fileNameKey = filePath
								.split("\\\\")[fileNameDepth].split("\\.")[0];
						testFileKeys.add(fileNameKey);
					}
					File[] sourceFiles = new File(sourceDirectory).listFiles();
					for (File sourceFile : sourceFiles) {
						String targetFilePath;

						String sourceFileName = sourceFile.getName();
						String fileNameKey = sourceFileName.split("\\.")[0];
						if ( testFileKeys.contains(fileNameKey) ) {
							targetFilePath = testTargetDirectory
									+ File.separator + sourceFileName;
						}
						else {
							targetFilePath = trainTargetDirectory
									+ File.separator + sourceFileName;
						}
						FileUtils.copyFile(sourceFile,
								new File(targetFilePath));
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
