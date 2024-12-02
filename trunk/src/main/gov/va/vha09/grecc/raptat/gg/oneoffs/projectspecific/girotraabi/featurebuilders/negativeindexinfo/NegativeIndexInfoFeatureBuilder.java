package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.negativeindexinfo;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class NegativeIndexInfoFeatureBuilder extends FeatureBuilder {

  public NegativeIndexInfoFeatureBuilder() {
    super();
  }


  public NegativeIndexInfoFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }


  @Override
  protected String getLabelToCheck() {
    return "NegativeIndexInfo";
  }

}
