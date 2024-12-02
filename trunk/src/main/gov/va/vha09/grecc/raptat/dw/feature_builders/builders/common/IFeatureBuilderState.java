package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common;

public interface IFeatureBuilderState {
  String getNewState();


  String getOldState();


  void updateNewState(String newFeatureString);


  boolean will_update();
}
