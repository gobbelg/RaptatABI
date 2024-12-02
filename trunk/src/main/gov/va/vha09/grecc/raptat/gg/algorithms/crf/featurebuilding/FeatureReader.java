package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.github.jcrfsuite.util.CrfSuiteLoader;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

public class FeatureReader {
  static {
    try {
      CrfSuiteLoader.load();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  private BufferedReader inReader;

  public FeatureReader(String pathToFeatureFile) {
    File readFile = new File(pathToFeatureFile);
    try {
      this.inReader = new BufferedReader(new FileReader(readFile));
    } catch (FileNotFoundException e) {
      GeneralHelper.errorWriter("Unable to create FeatureReader instance to read feature file");
      e.printStackTrace();
    }
  }


  public CrfFeatureSet readFeatures() {
    CrfFeatureSet featureSet = null;
    List<ItemSequence> itemSequences = new ArrayList<>(200000);
    List<StringList> stringLists = new ArrayList<>(200000);
    String inputLine;
    StringList tokenLabels = new StringList();
    ItemSequence sentenceTokenAttributes = new ItemSequence();
    try {
      inputLine = this.inReader.readLine();
      while (inputLine != null) {
        if (!inputLine.isEmpty()) {
          Item tokenAttributes = new Item();
          String[] elements = inputLine.split("\t");
          tokenLabels.add(elements[0]);
          for (int i = 1; i < elements.length; i++) {
            tokenAttributes.add(new Attribute(elements[i]));
          }
          sentenceTokenAttributes.add(tokenAttributes);
        } else {
          if (!tokenLabels.isEmpty()) {
            itemSequences.add(sentenceTokenAttributes);
            stringLists.add(tokenLabels);
          }
          tokenLabels = new StringList();
          sentenceTokenAttributes = new ItemSequence();
        }
        inputLine = this.inReader.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
      GeneralHelper.errorWriter(e.getMessage() + " Unable to read features in feature file");
    }

    featureSet = new CrfFeatureSet(itemSequences, stringLists);

    return featureSet;
  }


  public static void main(String[] args) {
    String pathToFile =
        "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CrossValid10FoldFeatures\\ReasonNoMedsModelTraining_FeatureRd00_TaggerFeatures_160612_170529_Group_1.txt";
    FeatureReader fr = new FeatureReader(pathToFile);

    fr.readFeatures();

    System.exit(0);
  }
}
