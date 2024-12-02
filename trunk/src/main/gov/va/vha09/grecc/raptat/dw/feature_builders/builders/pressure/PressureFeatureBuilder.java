package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.pressure;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class PressureFeatureBuilder extends FeatureBuilder {
  public PressureFeatureBuilder() {
    super();
  }

  public PressureFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }

  @Override
  protected String getLabelToCheck() {
    return "Pressure";
  }
}
