package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.index_value;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

public class IndexValueFeatureBuilder extends FeatureBuilder {

  public IndexValueFeatureBuilder() {
    super();
  }


  @Override
  protected String generatePhraseFeature(Label foundLabel, RunDirection direction) {
    return foundLabel.getCategory();
  }


  @Override
  protected String getLabelToCheck() {
    return "IndexValue";
  }

}
