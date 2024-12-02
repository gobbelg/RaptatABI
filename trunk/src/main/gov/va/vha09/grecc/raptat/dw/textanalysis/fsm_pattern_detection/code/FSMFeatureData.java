package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import com.google.common.base.Joiner;

/**
 * The Class FeatureData.
 */
final public class FSMFeatureData implements Comparable<FSMFeatureData> {

  /** The feature type. */
  private String __feature_type;

  /** The feature value. */
  private String __feature_value;


  /**
   * Instantiates a new feature data.
   *
   * @param feature_type the feature type
   * @param feature_value the feature value
   */
  public FSMFeatureData(String feature_type, String feature_value) {
    this.__feature_type = feature_type;
    this.__feature_value = feature_value;
  }


  @Override
  public int compareTo(FSMFeatureData rhs) {
    FSMFeatureData lhs = this;
    if (lhs.__feature_type.equals(rhs.__feature_type)
        && lhs.__feature_value.equals(rhs.__feature_value)) {
      return 0;
    }
    return -1;
  }


  /**
   * Gets the feature type.
   *
   * @return the feature type
   */
  public final String get_feature_type() {
    return this.__feature_type;
  }


  /**
   * Gets the feature value.
   *
   * @return the feature value
   */
  public final String get_feature_value() {
    return this.__feature_value;
  }

  @Override
  public String toString() {
    return Joiner.on(", ").join(new String[] {get_feature_type(), get_feature_value()});
  }

}
