package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces.IColumnFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces.IDocumentFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces.IRowFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * A class that implements the ability to take a series of feature builders and process them in the
 * order provided.
 *
 * @author VHATVHWesteD
 *
 */
final public class FeatureBuilderRunner
    implements IColumnFeatureBuilder, IRowFeatureBuilder, IDocumentFeatureBuilder {

  /** The feature builders. */
  private FeatureBuilder[] featureBuilders;


  /**
   * Instantiates a new feature builder runner.
   *
   * @param featureBuilders the feature builders
   */
  public FeatureBuilderRunner(FeatureBuilder... featureBuilders) {
    this.featureBuilders = featureBuilders;
    for (FeatureBuilder featureBuilder : this.featureBuilders) {
      featureBuilder.setRunner(this);
    }
  }


  /**
   * Process.
   *
   * @param document the document
   */
  @Override
  public void process(RaptatDocument document) {
    resetAllBuilders();
    for (FeatureBuilder featureBuilder : this.featureBuilders) {
      featureBuilder.process(document);
    }
  }


  /**
   * For each feature builder, call the corresponding method to process by column.
   *
   * @param phrase_list the phrase list
   * @param runDirection
   */
  @Override
  public void process_column(List<RaptatTokenPhrase> phrase_list, RunDirection runDirection,
      IndexedTree<IndexedObject<RaptatToken>> processedTokens) {
    resetAllBuilders();
    for (FeatureBuilder featureBuilder : this.featureBuilders) {
      featureBuilder.process_column(phrase_list, runDirection, processedTokens);
    }
  }


  /**
   * For each feature builder, call the corresponding method to process by document.
   *
   * @param tokenPhraseList
   * @param direction
   * @param processedTokens
   */
  public void process_document(List<RaptatTokenPhrase> tokenPhraseList, RunDirection direction,
      IndexedTree<IndexedObject<RaptatToken>> processedTokens) {
    resetAllBuilders();
    for (FeatureBuilder featureBuilder : this.featureBuilders) {
      featureBuilder.process_document(tokenPhraseList, direction, processedTokens);
    }
  }


  /**
   * For each feature builder, call the corresponding method to process by row.
   *
   * @param phrase_list the phrase list
   * @param runDirection
   * @param indexedTree
   */
  @Override
  public void process_row(List<RaptatTokenPhrase> phrase_list, RunDirection runDirection,
      IndexedTree<IndexedObject<RaptatToken>> processedTokens) {
    resetAllBuilders();
    for (FeatureBuilder featureBuilder : this.featureBuilders) {
      featureBuilder.process_row(phrase_list, runDirection, processedTokens);
    }

  }


  /**
   * Reset all builders.
   */
  private void resetAllBuilders() {
    for (FeatureBuilder featureBuilder : this.featureBuilders) {
      featureBuilder.reset();
    }
  }

}
