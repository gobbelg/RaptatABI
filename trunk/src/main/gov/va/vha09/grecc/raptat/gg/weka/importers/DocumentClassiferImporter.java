package src.main.gov.va.vha09.grecc.raptat.gg.weka.importers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.JdomXMLImporter;

/**
 * DocumentClassiferImporter Class - Handles importing of sets of text files along with the document
 * level and mention level annotations for use in training a machine learning system to classify
 * documents into the categories described in the document level annotations. This class assumes
 * that thed mention and document level annotation are stored separately.
 *
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Jan 12, 2019
 */
public class DocumentClassiferImporter {

  private enum RunType {
    IMPORT_SINGLE_FILES, IMPORT_FILE_SET;
  }


  private boolean areValid(File textFile, File mentionLevelAnnotationFile,
      File documentLevelAnnotationFile) {
    String textSource = textFile.getName();
    String mentionSource = JdomXMLImporter.getTextSource(mentionLevelAnnotationFile);
    if (!mentionSource.equals(textSource)) {
      System.err
          .println("Source of annotations in the mention level file does not match the text file");
    }
    String documentSource = JdomXMLImporter.getTextSource(mentionLevelAnnotationFile);
    if (!documentSource.equals(textSource)) {
      System.err
          .println("Source of annotations in the document level file does not match the text file");
    }
    return true;
  }


  private boolean areValid(List<File> textFiles, List<File> mentionLevelAnnotationFiles,
      List<File> documentLevelAnnotationFiles) {
    if (textFiles.size() != mentionLevelAnnotationFiles.size()
        || textFiles.size() != documentLevelAnnotationFiles.size()) {
      System.err.println(
          "Text file directory, mention level directory, and document level directory are unequal lengths");
      return false;
    }

    ListIterator<File> textFileIterator = textFiles.listIterator();
    ListIterator<File> mentionAnnotationFileIterator = mentionLevelAnnotationFiles.listIterator();
    ListIterator<File> documentAnnotationFileIterator = documentLevelAnnotationFiles.listIterator();

    while (textFileIterator.hasNext()) {
      if (!this.areValid(textFileIterator.next(), mentionAnnotationFileIterator.next(),
          documentAnnotationFileIterator.next())) {
        return false;
      }
    }

    return true;
  }


  private File getFile(String filePath, String promptString, String... acceptableExtensions) {

    File file = null;
    do {
      if (filePath == null || filePath.isEmpty()) {
        file = GeneralHelper.getFileByType("Select " + promptString + " for analysis",
            acceptableExtensions);
      } else {
        file = new File(filePath);
      }

    } while (file == null || !file.isFile() || !file.exists());

    return file;
  }


  private List<File> getFileList(String directoryPath, String prompt, String[] fileExtensions) {
    File directory = null;
    if (directoryPath == null || directoryPath.isEmpty() || !new File(directoryPath).exists()
        || !new File(directoryPath).isDirectory()) {
      directory = GeneralHelper.getDirectory("Select directory containing " + prompt + "s");
    } else {
      directory = new File(directoryPath);
    }

    FilenameFilter fileNameFilter = GeneralHelper.getFilenameFilter(fileExtensions);
    File[] fileArray = directory.listFiles(fileNameFilter);
    List<File> files = Arrays.asList(fileArray);
    Collections.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));

    return files;
  }


  private List<RaptatPair<File, RaptatPair<File, File>>> importFileSetsForClassifier(
      String textDirectoryPath, String mentionLevelAnnotationDirectoryPath,
      String documentLevelAnnotationDirectoryPath) {

    List<File> textFiles = getFileList(textDirectoryPath, "text file", new String[] {"txt"});
    List<File> mentionLevelAnnotationFiles = getFileList(mentionLevelAnnotationDirectoryPath,
        "mention level annotation file", new String[] {"xml"});
    List<File> documentLevelAnnotationFiles = getFileList(documentLevelAnnotationDirectoryPath,
        "document level annotation file", new String[] {"xml"});

    List<RaptatPair<File, RaptatPair<File, File>>> resultList = new ArrayList<>(textFiles.size());

    if (this.areValid(textFiles, mentionLevelAnnotationFiles, documentLevelAnnotationFiles)) {
      ListIterator<File> textFileIterator = textFiles.listIterator();
      ListIterator<File> mentionAnnotationFileIterator = mentionLevelAnnotationFiles.listIterator();
      ListIterator<File> documentAnnotationFileIterator =
          documentLevelAnnotationFiles.listIterator();

      while (textFileIterator.hasNext()) {
        RaptatPair<File, File> mentionDocumentPair = new RaptatPair<>(
            mentionAnnotationFileIterator.next(), documentAnnotationFileIterator.next());
        RaptatPair<File, RaptatPair<File, File>> textMentionAndDocumentPair =
            new RaptatPair<>(textFileIterator.next(), mentionDocumentPair);
        resultList.add(textMentionAndDocumentPair);
      }
    }

    return resultList;
  }


  private RaptatPair<File, RaptatPair<File, File>> importFilesForClassifier(String textFilePath,
      String mentionLevelAnnotationFilePath, String documentLevelAnnotationFilePath) {

    File textFile = getFile(textFilePath, "text file", new String[] {"txt"});
    File mentionLevelAnnotationFile = getFile(mentionLevelAnnotationFilePath,
        "mention level annotation file", new String[] {"xml"});
    File documentLevelAnnotationFile = getFile(documentLevelAnnotationFilePath,
        "mental level annotation file", new String[] {"xml"});

    if (this.areValid(textFile, mentionLevelAnnotationFile, documentLevelAnnotationFile)) {
      RaptatPair<File, File> annotationPair =
          new RaptatPair<>(mentionLevelAnnotationFile, documentLevelAnnotationFile);
      return new RaptatPair<>(textFile, annotationPair);
    }
    return null;
  }


  public static void main(String[] args) {

    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        DocumentClassiferImporter importer = new DocumentClassiferImporter();
        RunType runType = RunType.IMPORT_SINGLE_FILES;

        switch (runType) {
          case IMPORT_SINGLE_FILES: {
            String textFilePath = "";
            String mentionLevelAnnotationFilePath = "";
            String documentLevelAnnotationFilePath = "";
            RaptatPair<File, RaptatPair<File, File>> files = importer.importFilesForClassifier(
                textFilePath, mentionLevelAnnotationFilePath, documentLevelAnnotationFilePath);
            System.out.println("\nTextFile:" + files.left.getName());
            System.out.println("\nMentionAnnotationFile:" + files.right.left.getName());
            System.out.println("\nDocumentAnnotationFile:" + files.right.right.getName());
          }
            break;
          case IMPORT_FILE_SET: {
            String textDirectoryPath = "";
            String mentionLevelAnnotationDirectoryPath = "";
            String documentLevelAnnotationDirectoryPath = "";
            importer.importFileSetsForClassifier(textDirectoryPath,
                mentionLevelAnnotationDirectoryPath, documentLevelAnnotationDirectoryPath);
          }
            break;
          default:
            break;
        }
        System.exit(0);
      }
    });
  }
}
