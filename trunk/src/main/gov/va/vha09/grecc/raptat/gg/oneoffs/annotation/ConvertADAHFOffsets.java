package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OffsetConverter;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UTFToAnnotatorOffsetConverter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.machinelearning.naivebayes.NBResultWriter;

public class ConvertADAHFOffsets {

  private class OffsetMapping {
    private RaptatPair<Integer, Integer> oldOffsets, newOffsets;
    private String phrase;
    private String concept;


    private OffsetMapping(String beginOffset, String endOffset, String phrase, String concept) {
      this.oldOffsets =
          new RaptatPair<>(Integer.parseInt(beginOffset), Integer.parseInt(endOffset));
      this.phrase = phrase;
      this.concept = concept;
    }


    public String[] toStringArray(String fileName) {
      String[] resultArray = new String[7];
      resultArray[0] = fileName;
      resultArray[1] = this.oldOffsets.left.toString();
      resultArray[2] = this.oldOffsets.right.toString();
      resultArray[3] = this.newOffsets.left.toString();
      resultArray[4] = this.newOffsets.right.toString();
      resultArray[5] = this.phrase;
      resultArray[6] = this.concept;
      return resultArray;
    }
  }


  public ConvertADAHFOffsets() {
    File mappingFile = GeneralHelper.getFile("Select mapping file");
    HashMap<String, List<OffsetMapping>> offsetMap = getOffsetMap(mappingFile);
    generateNewOffsets(offsetMap, AnnotationApp.EHOST);
    writeNewOffsets(offsetMap, mappingFile);
  }


  private void generateNewOffsets(HashMap<String, List<OffsetMapping>> offsetMap,
      AnnotationApp annotationSource) {
    Set<Entry<String, List<OffsetMapping>>> mapItems = offsetMap.entrySet();
    for (Entry<String, List<OffsetMapping>> curItem : mapItems) {
      String fileName = curItem.getKey();
      OffsetConverter converter = new UTFToAnnotatorOffsetConverter(fileName, annotationSource);
      List<OffsetMapping> mappings = curItem.getValue();

      for (OffsetMapping curOffsetMapping : mappings) {
        curOffsetMapping.newOffsets = converter.convertOffsetPair(curOffsetMapping.oldOffsets);
      }
    }
  }


  private HashMap<String, List<OffsetMapping>> getOffsetMap(File mappingFile) {
    String[] curData = new String[5];
    CSVReader reader = new CSVReader(mappingFile);
    HashMap<String, List<OffsetMapping>> offsetMap = new HashMap<>((int) reader.getReaderLength());
    while ((curData = reader.getNextData()) != null) {
      String filePath = curData[0];
      if (!offsetMap.containsKey(filePath)) {
        offsetMap.put(filePath, new ArrayList<OffsetMapping>());
      }
      List<OffsetMapping> offsetList = offsetMap.get(filePath);
      offsetList.add(new OffsetMapping(curData[1], curData[2], curData[3], curData[4]));
    }
    return offsetMap;
  }


  private void writeNewOffsets(HashMap<String, List<OffsetMapping>> offsetMap, File mappingFile) {
    String writeDirectory = mappingFile.getParentFile().getAbsolutePath();
    String[] resultTitles = new String[] {"Document", "OldStartOffset", "OldEndOffset",
        "NewStartOffset", "NewEndOffset", "AnnotatedPhrase"};
    NBResultWriter theWriter = new NBResultWriter(writeDirectory, resultTitles);

    Set<Entry<String, List<OffsetMapping>>> mapItems = offsetMap.entrySet();
    for (Entry<String, List<OffsetMapping>> curItem : mapItems) {
      String fileName = curItem.getKey();
      List<OffsetMapping> mappings = curItem.getValue();

      for (OffsetMapping curOffsetMapping : mappings) {
        String[] writtenLine = curOffsetMapping.toStringArray(fileName);
        theWriter.writeln(writtenLine, "\t");
      }
    }
  }


  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new ConvertADAHFOffsets();
      }
    });
  }
}
