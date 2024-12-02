package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import opennlp.tools.util.Span;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class I2B2AnnotationImporter {
  static TextAnalyzer textAnalyzer = new TextAnalyzer();


  public I2B2AnnotationImporter() {}


  public void close() {
    // textAnalyzer.destroy();
  }


  public static void main(String[] args) {
    try {
      I2B2AnnotationImporter.textAnalyzer.setTokenProcessingParameters(false, false, false, 7,
          ContextHandling.NONE);
      File dirFile = GeneralHelper.getDirectory("Get directory containing files",
          "/Users/glenn/Downloads/i2b2DataForRaptat");
      String[] fileTypes = {".con"};
      File[] theFiles = dirFile.listFiles(GeneralHelper.getFileFilter(fileTypes));
      List<String> fileNames = new ArrayList<>();
      List<String> concepts = new ArrayList<>();
      List<String[]> annotations = new ArrayList<>();

      for (int fileIndex = 0; fileIndex < theFiles.length; fileIndex++) {
        BufferedReader theReader = new BufferedReader(new FileReader(theFiles[fileIndex]));
        String fileName = theFiles[fileIndex].getName();
        String textLine;
        int tokenIndex = 0;
        while ((textLine = theReader.readLine()) != null) {
          String[] barStrings = textLine.split("\\|\\|");
          int startOffset = barStrings[0].indexOf("\"");
          int endOffset = lastQuoteOffset(barStrings[0], startOffset);
          String annotatedText = barStrings[0].substring(startOffset + 1, endOffset);

          String[] location = barStrings[0].substring(endOffset + 1).split("\\s");
          String[] startPos = location[1].split(":");
          String[] endPos = location[2].split(":");
          int startLine = Integer.parseInt(startPos[0]);
          int startToken = Integer.parseInt(startPos[1]);
          int endLine = Integer.parseInt(endPos[0]);
          int endToken = Integer.parseInt(endPos[1]);

          startOffset = barStrings[1].indexOf("\"");
          endOffset = barStrings[1].indexOf("\"", startOffset + 1);
          String concept = barStrings[1].substring(startOffset + 1, endOffset);
          fileNames.add(fileName);
          concepts.add(concept);
          String[] textTokens = tokenizeText(annotatedText, tokenIndex);
          tokenIndex += textTokens.length;
          annotations.add(textTokens);

          System.out.print("File:" + fileName);
          System.out.print("Annotation:'" + annotatedText);
          System.out.print("', Concept:" + concept);
          System.out.print(", Starts on line " + startLine + " at token " + startToken);
          System.out.println(", Ends on line " + endLine + " at token " + endToken);
        }
        theReader.close();
      }
      String resultFile =
          dirFile + File.separator + "i2b2Annotations_" + System.currentTimeMillis() + ".csv";
      PrintWriter resultWriter =
          new PrintWriter(new BufferedWriter(new FileWriter(new File(resultFile), false)), true);
      Iterator<String> conceptIterator = concepts.iterator();
      Iterator<String[]> annotationIterator = annotations.iterator();
      for (String curFileName : fileNames) {
        String[] nextAnnotation = annotationIterator.next();
        String nextConcept = conceptIterator.next();
        resultWriter.print(curFileName + "," + nextConcept);
        for (int annotationIndex = 0; annotationIndex < nextAnnotation.length; annotationIndex++) {
          resultWriter.print("," + nextAnnotation[annotationIndex]);
        }
        resultWriter.println();
      }
      resultWriter.flush();
      resultWriter.close();
    } catch (FileNotFoundException e) {
      System.out.println(e);
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    }
  }


  private static String[] convertToArray(List<RaptatToken> inputTokens) {
    String[] result = new String[inputTokens.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = inputTokens.get(i).getTokenStringAugmented();
    }
    return result;
  }


  private static int lastQuoteOffset(String inputString, int startOffset) {
    int quoteIndex = startOffset, result = startOffset;
    while ((quoteIndex = inputString.indexOf("\"", quoteIndex + 1)) != -1) {
      result = quoteIndex;
    }
    return result;
  }


  private static String[] tokenizeText(String annotatedText, int tokenIndex) {
    Span textSpan = new Span(0, 0);
    annotatedText = annotatedText.replaceAll("[,\"]", "");
    // Add line below to substitute numbers with '#'
    // annotatedText = annotatedText.replaceAll( "[0-9]+", "#" );
    RaptatPair<List<RaptatToken>, List<RaptatToken>> textTokens =
        I2B2AnnotationImporter.textAnalyzer.getSentenceTokens(annotatedText, textSpan, tokenIndex);
    return convertToArray(textTokens.right);
  }


  private static String trimQuotes(String inString) {
    return inString.substring(1, inString.length() - 1);
  }
}
