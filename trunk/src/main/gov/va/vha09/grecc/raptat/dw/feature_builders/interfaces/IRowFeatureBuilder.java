package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public interface IRowFeatureBuilder {

  /**
   * Process row.
   *
   * @param phrase_list the phrase list
   * @param runDirection the run direction
   */
  void process_row(List<RaptatTokenPhrase> phrase_list, RunDirection runDirection,
      IndexedTree<IndexedObject<RaptatToken>> processedTokens);

}
