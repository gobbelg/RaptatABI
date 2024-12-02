package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.interpretation;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class InterpretationFeatureBuilder extends FeatureBuilder {

  public InterpretationFeatureBuilder() {
    super();
  }

  public InterpretationFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }

  @Override
  protected String getLabelToCheck() {
    return "Interpretation";
  }
}
