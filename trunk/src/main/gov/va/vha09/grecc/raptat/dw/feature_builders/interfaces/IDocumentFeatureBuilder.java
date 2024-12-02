package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

public interface IDocumentFeatureBuilder {

  /**
   * publicly exposed call to enact feature builder over RaptatDocument
   *
   * @param document
   */
  void process(RaptatDocument document);

}
