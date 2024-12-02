package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

final public class RaptatTokenFSM extends RaptatToken {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4244625928869745364L;

  /** The features. */
  Map<String, FSMFeatureData> __features = new HashMap<>();

  private String originalString;

  private int lineDifferenceStart;


  /**
   * Instantiates a new ABI raptat token extended.
   *
   * @param input_string the input string
   */
  public RaptatTokenFSM(String input_string) {
    this(input_string, 0, input_string.length());
  }


  /**
   * Instantiates a new ABI raptat token extended.
   *
   * @param input_string the input string
   * @param startOffset the start offset
   * @param endOffset the end offset
   */
  public RaptatTokenFSM(String input_string, int startOffset, int endOffset) {
    super(RaptatTokenHelper.AsPreprocessed(input_string, startOffset, endOffset, 0));
  }


  public RaptatTokenFSM(String stringToProccess, String originalString, int lineDifferenceStart) {
    this(stringToProccess);
    this.originalString = originalString;
    this.lineDifferenceStart = lineDifferenceStart;
  }


  /**
   * Adds the feature.
   *
   * @param feature_type the feature type
   * @param feature_value the feature value
   */
  public void add_feature(String feature_type, String feature_value) {
    this.add_feature(feature_type, feature_value, null);
  }


  /**
   * Gets the column label index type.
   *
   * @return the column label index type
   */
  public String get_column_label_index_type() {
    return get_feature("column_label_index_type");
  }


  /**
   * Gets the column label laterality.
   *
   * @return the column label laterality
   */
  public String get_column_label_laterality() {
    return get_feature("column_label_laterality");
  }


  /**
   * Gets the fsm feature map.
   *
   * @return the fsm feature map
   */
  public Map<String, FSMFeatureData> get_fsm_feature_map() {
    return this.__features;
  }


  /**
   * Gets the line label index type.
   *
   * @return the line label index type
   */
  public String get_line_label_index_type() {
    return get_feature("line_label_index_type");
  }


  /**
   * Gets the line label laterality.
   *
   * @return the line label laterality
   */
  public String get_line_label_laterality() {
    return get_feature("line_label_laterality");
  }


  /**
   * Gets the upstream label body part.
   *
   * @return the upstream label body part
   */
  public String get_upstream_label_body_part() {
    return get_feature("upstream_label_body_part");
  }


  /**
   * Gets the upstream label index type.
   *
   * @return the upstream label index type
   */
  public String get_upstream_label_index_type() {
    return get_feature("upstream_label_index_type");
  }


  /**
   * Gets the upstream label laterality.
   *
   * @return the upstream label laterality
   */
  public String get_upstream_label_laterality() {
    return get_feature("upstream_label_laterality");
  }


  public int getLineDifferenceStart() {
    return this.lineDifferenceStart;
  }


  public String getOriginalString() {
    return this.originalString;
  }


  /**
   * Sets the column label laterality.
   *
   * @param _column_label_laterality the new column label laterality
   */
  public void set_column_label_laterality(String _column_label_laterality) {
    this.add_feature("column_label_laterality", _column_label_laterality);
  }


  /**
   * Sets the line label laterality.
   *
   * @param line_label_laterality the new line label laterality
   */
  public void set_line_label_laterality(String line_label_laterality) {
    this.add_feature("line_label_laterality", line_label_laterality);
  }


  /**
   * Sets the upstream label body part.
   *
   * @param upstream_label_body_part the new upstream label body part
   */
  public void set_upstream_label_body_part(String upstream_label_body_part) {
    this.add_feature("upstream_label_body_part", upstream_label_body_part);
  }


  /**
   * Sets the upstream label index type.
   *
   * @param upstream_label_index_type the new upstream label index type
   */
  public void set_upstream_label_index_type(String upstream_label_index_type) {
    this.add_feature("upstream_label_index_type", upstream_label_index_type);
  }


  /**
   * Sets the upstream label laterality.
   *
   * @param upstream_label_laterality the new upstream label laterality
   */
  public void set_upstream_label_laterality(String upstream_label_laterality) {
    this.add_feature("upstream_label_laterality", upstream_label_laterality);
  }


  @Override
  public String toString() {
    return MessageFormat.format("Value: {0}", getTokenStringPreprocessed());
  }


  /**
   * Adds the feature.
   *
   * @param feature_type the feature type
   * @param feature_value the feature value
   * @param group_id the group id
   */
  private void add_feature(String feature_type, String feature_value, String group_id) {
    if (this.__features.containsKey(feature_type)) {
      throw new IllegalArgumentException(
          "Feature " + feature_type + " already labeled for this token");
    }
    FSMFeatureData feature_data = new FSMFeatureData(feature_type, feature_value);
    this.__features.put(feature_type, feature_data);
  }


  /**
   * Gets the feature.
   *
   * @param string the string
   * @return the feature
   */
  private String get_feature(String string) {

    return null;
  }


  /**
   * Sets the column label index type.
   *
   * @param index_type the new column label index type
   */
  private void set_column_label_index_type(String index_type) {
    this.add_feature("column_label_index_type", index_type);
  }


  /**
   * Sets the row label index type.
   *
   * @param index_type the new row label index type
   */
  private void set_row_label_index_type(String index_type) {
    this.add_feature("line_label_index_type", index_type);
  }


  /**
   * Sets the row label laterality.
   *
   * @param laterality the new row label laterality
   */
  private void set_row_label_laterality(String laterality) {
    this.add_feature("line_label_laterality", laterality);
  }

}
