/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.List;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/**
 * Convenience class for storing all the training features and labels from all documents
 *
 * @author Glenn T. Gobbel Mar 2, 2016
 */
public class CueScopeFeatureSet extends CrfFeatureSet {
  public final List<ItemSequence> cueTrainingFeatures;
  public final List<StringList> cueTrainingLabels;

  public final List<ItemSequence> scopeTrainingFeatures;
  public final List<StringList> scopeTrainingLabels;


  /**
   * @param cueTrainingFeatures
   * @param cueTrainingLabels
   * @param scopeTrainingFeatures
   * @param scopeTrainingLabels
   */
  public CueScopeFeatureSet(List<ItemSequence> cueTrainingFeatures,
      List<StringList> cueTrainingLabels, List<ItemSequence> scopeTrainingFeatures,
      List<StringList> scopeTrainingLabels) {
    super();
    this.cueTrainingFeatures = cueTrainingFeatures;
    this.cueTrainingLabels = cueTrainingLabels;
    this.scopeTrainingFeatures = scopeTrainingFeatures;
    this.scopeTrainingLabels = scopeTrainingLabels;
  }
}
