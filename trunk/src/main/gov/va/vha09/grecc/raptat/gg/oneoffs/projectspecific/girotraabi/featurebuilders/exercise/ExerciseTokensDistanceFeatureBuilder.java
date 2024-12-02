package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.exercise;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilderState;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

public class ExerciseTokensDistanceFeatureBuilder extends ExerciseFeatureBuilder {
  /**
   * Instantiates a new laterality tokens distance feature builder.
   */
  public ExerciseTokensDistanceFeatureBuilder() {
    super(new FeatureBuilderState(x -> x.getFeatureWithIndex()));
  }


  /**
   *
   */
  @Override
  protected String generatePhraseFeature(Label foundLabel, RunDirection direction) {
    return super.generatePhraseFeature(foundLabel, direction) + "_TOKEN_DISTANCE";
  }
}
