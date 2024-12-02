package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;

public abstract class FeatureBuilderWriter implements AutoCloseable {

  private String outputFilePath;
  protected File tempFile;


  public FeatureBuilderWriter(String outputFilePath) {
    this();
    this.outputFilePath = outputFilePath;
  }


  public FeatureBuilderWriter(String fileName, String fileExtension) throws IOException {
    this();
    this.tempFile = File.createTempFile(fileName, fileExtension);
  }


  private FeatureBuilderWriter() {}


  @Override
  public void close() throws Exception {

  }


  public String getOutputFilePath() {
    return this.outputFilePath;
  }


  public File getTempFile() {
    return this.tempFile;
  }


  /**
   * Write.
   *
   * @param tokenPhraseList the token phrase list
   * @throws Exception
   */
  public void write(List<RaptatTokenPhrase> tokenPhraseList) throws Exception {

    Map<RaptatTokenPhrase, TreeMap<String, Boolean>> mapIndexValueToFeatures = new HashMap<>();
    Set<String> featureNames = new TreeSet<>();

    for (RaptatTokenPhrase phrase : tokenPhraseList) {
      if (phrase.getPhraseLabels().contains(new RaptatTokenPhrase.Label("IndexValue"))) {
        if (mapIndexValueToFeatures.containsKey(phrase) == false) {
          mapIndexValueToFeatures.put(phrase, new TreeMap<String, Boolean>());
        }
        TreeMap<String, Boolean> featureSet = mapIndexValueToFeatures.get(phrase);
        for (String feature : phrase.getPhraseFeatures()) {
          featureNames.add(feature);
          featureSet.put(feature, true);
        }
      }
    }

    List<List<String>> output = new ArrayList<>();

    List<String> featureNamesList = new ArrayList<>(featureNames);
    featureNamesList.add(0, "IndexValue");

    output.add(featureNamesList);

    for (RaptatTokenPhrase phrase : mapIndexValueToFeatures.keySet()) {
      List<String> row = new ArrayList<>();
      row.add(phrase.getPhraseText());
      TreeMap<String, Boolean> features = mapIndexValueToFeatures.get(phrase);

      for (String feature : featureNames) {
        boolean found = false;
        if (features.containsKey(feature)) {
          found = features.get(feature);
        }
        row.add(found ? "1" : "");
      }
      output.add(row);
    }

    derivedWrite(output);

  }


  protected abstract void derivedWrite(List<List<String>> output) throws Exception;
}
