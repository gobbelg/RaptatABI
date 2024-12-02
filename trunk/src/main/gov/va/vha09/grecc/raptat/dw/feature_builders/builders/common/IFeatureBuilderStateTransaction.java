package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common;

public interface IFeatureBuilderStateTransaction extends IFeatureBuilderState, AutoCloseable {
  void commit();

}
