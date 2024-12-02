package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.temporal;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class TemporalFeatureBuilder extends FeatureBuilder {

  public TemporalFeatureBuilder() {
    super();
  }


  public TemporalFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }


  @Override
  protected String getLabelToCheck() {
    return "Temporal";
  }

}
