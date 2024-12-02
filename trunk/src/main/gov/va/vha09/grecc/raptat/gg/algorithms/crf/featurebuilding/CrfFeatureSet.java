/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.ArrayList;
import java.util.List;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/**
 * Class for returning features resulting from running annotations through the pipe of various
 * FeatureBuilder instances
 *
 * @author Glenn T. Gobbel Apr 18, 2016
 */
public class CrfFeatureSet {
  public final List<ItemSequence> trainingFeatures;
  public final List<StringList> trainingLabels;


  public CrfFeatureSet(List<ItemSequence> trainingFeatures, List<StringList> trainingLabels) {
    this.trainingFeatures = trainingFeatures;
    this.trainingLabels = trainingLabels;
  }


  protected CrfFeatureSet() {
    this.trainingFeatures = new ArrayList<>();
    this.trainingLabels = new ArrayList<>();
  }
}
