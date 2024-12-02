package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.DocumentMetaData;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class PostArffFileCompleted implements FnPostArffFileCompleted {

  public static class PostArffFileResults {

    private Attribute attributeToFoldOn;
    private Set<Attribute> attributeSet = new HashSet<>();

    public void addAttribute(Attribute attribute) {
      this.attributeSet.add(attribute);
    }

    public void attributeToFoldOn(Attribute attribute) {
      this.attributeToFoldOn = attribute;
    }

    public Set<Attribute> getAttributeSet() {
      return this.attributeSet;
    }

    public Attribute getAttributeToFoldOn() {
      return this.attributeToFoldOn;
    }

  }

  final private String INDEX_VALUE_TEXT_CONST = "Index_Value_Text";
  final private String START_OFFSET_CONST = "Start_Offset";
  final private String END_OFFSET_CONST = "End_Offset";
  final private String DOCUMENT_ID_CONST = "Document_ID";

  private TreeMap<Integer, DocumentMetaData> lineNumberToPhrase;

  public PostArffFileCompleted(TreeMap<Integer, DocumentMetaData> lineNumberToPhrase) {
    this.lineNumberToPhrase = lineNumberToPhrase;
  }

  @Override
  public PostArffFileResults writeArffFile(String arffFilePath)
      throws FileNotFoundException, IOException {

    String filePath = arffFilePath;
    ArffLoader.ArffReader arffReader =
        new ArffLoader.ArffReader(new BufferedReader(new FileReader(filePath)));
    Instances data = arffReader.getData();

    /**
     * Add attributes for phrase
     */
    Attribute index_value_attribute = new Attribute(this.INDEX_VALUE_TEXT_CONST, true);

    Attribute start_offset_attribute = new Attribute(this.START_OFFSET_CONST);

    Attribute end_offset_attribute = new Attribute(this.END_OFFSET_CONST);

    Attribute document_id = new Attribute(this.DOCUMENT_ID_CONST, true);

    /**
     *
     */
    Instances new_data = new Instances(data);
    new_data.insertAttributeAt(index_value_attribute, new_data.numAttributes());
    new_data.insertAttributeAt(start_offset_attribute, new_data.numAttributes());
    new_data.insertAttributeAt(end_offset_attribute, new_data.numAttributes());
    new_data.insertAttributeAt(document_id, new_data.numAttributes());

    /**
     * Get the attributes again, to get an updated index
     */

    index_value_attribute = new_data.attribute(this.INDEX_VALUE_TEXT_CONST);
    start_offset_attribute = new_data.attribute(this.START_OFFSET_CONST);
    end_offset_attribute = new_data.attribute(this.END_OFFSET_CONST);
    document_id = new_data.attribute(this.DOCUMENT_ID_CONST);

    /**
     * need to add indication of what to fold on, since we're folding on document_id
     */

    PostArffFileResults postArffFileResults = new PostArffFileResults();

    postArffFileResults.attributeToFoldOn(document_id);

    /**
     * Need to tell the trainer attributes to ignore
     */

    postArffFileResults.addAttribute(index_value_attribute);
    postArffFileResults.addAttribute(start_offset_attribute);
    postArffFileResults.addAttribute(end_offset_attribute);
    postArffFileResults.addAttribute(document_id);

    /**
     *
     */
    for (int row_index = 0; row_index < new_data.numInstances(); row_index++) {
      Instance instance = new_data.get(row_index);
      DocumentMetaData documentMetaData = this.lineNumberToPhrase.get(row_index);
      RaptatTokenPhrase raptatTokenPhrase = documentMetaData.getDocumentPhrase();
      instance.setValue(index_value_attribute.index(), raptatTokenPhrase.getPhraseText());
      instance.setValue(start_offset_attribute.index(),
          raptatTokenPhrase.getStartOffsetAsInt());
      instance.setValue(end_offset_attribute.index(), raptatTokenPhrase.getEndOffsetAsInt());
      instance.setValue(document_id.index(), documentMetaData.getDocumentId());
    }

    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, false))) {
      bufferedWriter.write(new_data.toString());
    }

    return postArffFileResults;
  }
}
