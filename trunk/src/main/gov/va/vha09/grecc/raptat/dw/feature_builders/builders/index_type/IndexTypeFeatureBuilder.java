package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.index_type;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;

public class IndexTypeFeatureBuilder extends FeatureBuilder {

  public IndexTypeFeatureBuilder() {
    super();
  }


  public IndexTypeFeatureBuilder(FeatureBuilderState featureBuilderState) {
    super(featureBuilderState);
  }


  @Override
  protected String getLabelToCheck() {
    return "IndexType";
  }

}
