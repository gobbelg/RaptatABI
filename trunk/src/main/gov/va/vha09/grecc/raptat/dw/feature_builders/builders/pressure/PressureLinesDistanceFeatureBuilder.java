package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.pressure;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

public class PressureLinesDistanceFeatureBuilder extends PressureFeatureBuilder {
  public PressureLinesDistanceFeatureBuilder() {
    super(new FeatureBuilderState(x -> x.getFeatureWithLineDifference()));
  }


  @Override
  protected String generatePhraseFeature(Label foundLabel, RunDirection direction) {
    return super.generatePhraseFeature(foundLabel, direction) + "_LINE_DISTANCE";
  }


  @Override
  protected boolean isColumnOnlyBuilder() {
    return true;
  }
}
