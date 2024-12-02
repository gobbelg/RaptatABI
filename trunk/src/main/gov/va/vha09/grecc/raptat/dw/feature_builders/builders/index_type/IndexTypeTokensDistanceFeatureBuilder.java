package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.index_type;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

public class IndexTypeTokensDistanceFeatureBuilder extends IndexTypeFeatureBuilder {
  public IndexTypeTokensDistanceFeatureBuilder() {
    super(new FeatureBuilderState(x -> x.getFeatureWithIndex()));
  }


  @Override
  protected String generatePhraseFeature(Label foundLabel, RunDirection direction) {
    return super.generatePhraseFeature(foundLabel, direction) + "_TOKEN_DISTANCE";
  }
}
