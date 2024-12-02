package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.artery;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class ArteryFeatureBuilder extends FeatureBuilder {

  public ArteryFeatureBuilder() {
    super();
  }


  public ArteryFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }


  @Override
  protected String getLabelToCheck() {
    return "Anatomy";
  }

}
