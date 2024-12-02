package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.HashTreeFields;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeFields;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class WordSequenceFrequency {

  private final static RaptatToken TOKEN_FOR_INSERT = new RaptatToken("$**NOTOKEN@@$", -1, -1);
  private final static Matcher NON_ALPHANUMERIC_MATCHER = Pattern.compile("^\\W").matcher("");
  private final static Matcher NUMBER_MATCHER =
      Pattern.compile("^[+-]?(?:(?:\\.\\d+)|(?:\\d{1,3}(,?\\d{3})*(?:\\.\\d+|\\.)?))$").matcher("");

  public WordSequenceFrequency() {}

  public static void main(final String[] args) {

    // UserPreferences.INSTANCE.initializeLVGLocation();
    // final Map<String, String> envMap = System.getenv();
    String lvgPath = System.getenv("LVG_DIR");
    UserPreferences.INSTANCE.setLvgPath(lvgPath);

    // Set to any integer less than 1 to print all phrase lengths
    final int targetNgramLength = 0;
    final int maxNgramLength = 4;
    final int minOccurrences = 4;
    final String directoryPath =
        "P:/ORD_Oh_PTR_201903029D/GobbelWorkspace/SmallPatientSet/010_PatientDocs";
    // "P:\\ORD_Girotra_201607120D\\Glenn\\eHOSTWorkspace\\FirstReference200DocsTokenPhraseLabels_Updated_191015\\corpus";
    final String outputDirectoryName = "Output";
    final String assignedLabel = "DermNgrams";

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        List<File> documentFiles = getDocumentFiles(directoryPath);
        final UnlabeledHashTree phraseFrequencyTree = buildPhraseFrequencyTree(documentFiles);

        final String outputDirectoryPath =
            new File(directoryPath).getParent() + File.separator + outputDirectoryName;
        final String outputPath =
            outputDirectoryPath + File.separator + assignedLabel + "_Output_PhraseLength_"
                + targetNgramLength + "_" + GeneralHelper.getTimeStamp() + ".txt";
        try {
          FileUtils.forceMkdir(new File(outputDirectoryPath));
          System.out.println("Writing results to :\n" + outputPath);
          final PrintWriter pw = new PrintWriter(outputPath);
          printPhraseOccurrences(phraseFrequencyTree, targetNgramLength, minOccurrences, pw);

          // '**************************************************************************
          // final Map<Long, List<String>> phraseOccurrenceMap =
          // this.getPhraseOccurrenceMap(phraseFrequencyTree, targetNgramLength);
          //
          // final List<Long> occurrenceList = new ArrayList<>(phraseOccurrenceMap.keySet());
          // Collections.sort(occurrenceList, Collections.reverseOrder());
          //
          // for (final Long listKey : occurrenceList) {
          // final List<String> phraseList = phraseOccurrenceMap.get(listKey);
          // Collections.sort(phraseList);
          // for (final String phrase : phraseList) {
          // // Add apostrophe (') so excel will read unusual, non-alphanumeric characters
          // // properly
          // final String prepend = nonAlphanumericMatcher.reset(phrase).find() ? "'" : "";
          // final String outputString = prepend + phrase + "\t" + listKey;
          // System.out.println(outputString);
          // pw.println(outputString);
          // pw.flush();
          // }
          // }
          // '**************************************************************************

          pw.close();
          System.out.println("Completed writing results to :\n" + outputPath);
          System.exit(0);
        } catch (final FileNotFoundException e) {
          e.printStackTrace();
          System.exit(-1);
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      protected UnlabeledHashTree buildPhraseFrequencyTree(final List<File> documentFiles) {
        UnlabeledHashTree phraseFrequencyTree = new UnlabeledHashTree();
        final TextAnalyzer theAnalyzer = new TextAnalyzer();
        theAnalyzer.setGenerateTokenPhrases(false);
        theAnalyzer.setTokenProcessingParameters(false, false, true, maxNgramLength,
            ContextHandling.NONE);

        int index = 1;
        for (final File nextFile : documentFiles) {
          System.out.println("Processing " + index++ + " of " + documentFiles.size() + " files");
          final RaptatDocument inputDocument =
              theAnalyzer.processDocument(nextFile.getAbsolutePath());
          inputDocument.activateBaseSentences();
          System.out.println("Building frequency tree for " + nextFile.getName());
          addToFrequencyTree(phraseFrequencyTree, inputDocument);
        }
        System.out.println("Phrase frequency tree complete");
        return phraseFrequencyTree;
      }

      private void addToFrequencyTree(final UnlabeledHashTree frequencyTree,
          final RaptatDocument raptatDocument) {
        final List<AnnotatedPhrase> activeSentences = raptatDocument.getActiveSentences();
        for (final AnnotatedPhrase curSentence : activeSentences) {
          final List<RaptatToken> sentenceTokens = curSentence.getProcessedTokens();
          final int sentenceSize = sentenceTokens.size();
          for (int curTokenIndex = 0; curTokenIndex < sentenceSize; curTokenIndex++) {
            frequencyTree.addUnlabeledSequence(sentenceTokens, curTokenIndex, -1,
                RaptatConstants.MAX_TOKENS_DEFAULT);
          }
        }
      }

      /**
       * @param directoryPath
       * @return
       */
      private List<File> getDocumentFiles(final String directoryPath) {
        File fileDirectory = new File(directoryPath);
        if (!fileDirectory.exists()) {
          fileDirectory =
              GeneralHelper.getDirectory("Select directory containing files for analysis");
        }
        // final File[] files = fileDirectory.listFiles(GeneralHelper.getFilenameFilter(".txt"));
        List<File> fileList = new ArrayList<>();
        try (final Stream<Path> fileWalk = Files.walk(Paths.get(directoryPath))) {
          fileList = fileWalk.filter(filePath -> filePath.toString().endsWith(".txt"))
              .map(Path::toFile).collect(Collectors.toList());
        } catch (final IOException e) {
          System.err.print("Unable to get files\n" + e.getLocalizedMessage());
          e.printStackTrace();
          System.exit(-1);
        }
        return fileList;
      }


      private void printPhraseOccurrences(final UnlabeledHashTree phrases,
          final int targetPhraseLength, final int minOccurrences, final PrintWriter pw) {

        for (final HashTreeFields phrase : phrases) {
          final UnlabeledHashTreeFields unlabeledPhrase = (UnlabeledHashTreeFields) phrase;
          if (targetPhraseLength < 1
              || unlabeledPhrase.tokenSequence.size() == targetPhraseLength) {
            final Iterator<String> sequenceIterator = unlabeledPhrase.tokenSequence.iterator();
            int actualPhraseLength = 1;
            String phraseString = sequenceIterator.next();

            if (!WordSequenceFrequency.NON_ALPHANUMERIC_MATCHER.reset(phraseString).find()) {

              if (WordSequenceFrequency.NUMBER_MATCHER.reset(phraseString).find()) {
                phraseString = "NUMBER";
              }
              final StringBuilder sb = new StringBuilder(phraseString);
              while (sequenceIterator.hasNext()) {
                phraseString = sequenceIterator.next();
                if (!WordSequenceFrequency.NON_ALPHANUMERIC_MATCHER.reset(phraseString).find()) {
                  if (WordSequenceFrequency.NUMBER_MATCHER.reset(phraseString).find()) {
                    phraseString = "NUMBER";
                  }
                  ++actualPhraseLength;
                  sb.append(" ").append(phraseString);
                }
              }
              final Long occurrences = unlabeledPhrase.unlabeled / 967680;
              if (occurrences >= minOccurrences) {
                String outputString =
                    actualPhraseLength + "\t" + occurrences + "\t" + "'" + sb.toString();
                System.out.println(outputString);
                pw.println(outputString);
                pw.flush();
              }
            }
          }
        }
      }
    });
  }
}
