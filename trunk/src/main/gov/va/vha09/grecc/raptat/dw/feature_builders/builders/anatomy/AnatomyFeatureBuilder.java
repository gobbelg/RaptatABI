package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.anatomy;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class AnatomyFeatureBuilder extends FeatureBuilder {

  public AnatomyFeatureBuilder() {
    super();
  }


  public AnatomyFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }


  @Override
  protected String getLabelToCheck() {
    return "Anatomy";
  }

}
