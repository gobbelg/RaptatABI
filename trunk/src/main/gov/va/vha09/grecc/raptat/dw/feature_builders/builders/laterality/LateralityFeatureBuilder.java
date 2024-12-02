package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.laterality;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class LateralityFeatureBuilder extends FeatureBuilder {

  public LateralityFeatureBuilder() {
    super();
  }


  public LateralityFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }


  @Override
  protected String getLabelToCheck() {
    return "Laterality";
  }

}
