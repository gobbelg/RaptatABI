package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.TextReader;

public class CSVAnnotationConverter {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TextReader theReader = new TextReader("Select file with annotations");
    String inDirectory = theReader.getInFileDirectory();
    String[] curLine;
    HashMap<String, List<AnnotatedPhrase>> docAnnotations = new HashMap<>();
    String[] phraseData = new String[6];
    phraseData[0] = "";
    phraseData[4] = "";
    String testDoc = "", doc = "", beginSpan = "", endSpan = "", annoText = "";
    StringBuilder concept = new StringBuilder();
    while ((curLine = theReader.getNextData()) != null) {
      testDoc = curLine[0].replace("-", "_");
      if (testDoc.equals(doc) && curLine[1].equals(annoText) && curLine[2].equals(beginSpan)
          && curLine[3].equals(endSpan)) {
        concept.append("_");
        concept.append(curLine[4]);
      } else if (doc.equals("")) {
        doc = testDoc;
        annoText = curLine[1];
        beginSpan = curLine[2];
        endSpan = curLine[3];
        concept.append(curLine[4]);
      } else {
        phraseData[1] = beginSpan;
        phraseData[2] = endSpan;
        phraseData[3] = annoText;
        phraseData[5] = concept.toString();

        AnnotatedPhrase thePhrase = new AnnotatedPhrase(phraseData);
        if (docAnnotations.containsKey(doc)) {
          docAnnotations.get(doc).add(thePhrase);
        } else {
          docAnnotations.put(doc, new ArrayList<AnnotatedPhrase>());
          docAnnotations.get(doc).add(thePhrase);
        }
        doc = "";
        concept = new StringBuilder();
      }
    }

    theReader.close();

    Set<String> theDocs = docAnnotations.keySet();

    for (String curDoc : theDocs) {
      String textSourcePath = inDirectory + File.separator + curDoc + ".txt";
      XMLExporter theExporter = new XMLExporter(textSourcePath, true);
      theExporter.writeAllDataObj(docAnnotations.get(curDoc), false);
      theExporter.writeXMLFile(inDirectory, "");
    }
  }
}
