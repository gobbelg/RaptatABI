/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.general;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

/**
 * @author gobbelgt
 *
 */
public interface BaseFeatureBuilder {

  /**
   * Takes a collection of phrases. Those labeled with the targetLabel are given features based on
   * the labels of the other phrases in the collection. Only those with a Label name field equal to
   * a key in the featureBuilderLabelMap and a value in the mapped set of values are used to create
   * features.
   *
   * @param tokenPhrases
   * @param targetLabel
   * @param featureBuilderLabelMap
   */
  public void addTokenPhraseFeatures(Collection<RaptatTokenPhrase> tokenPhrases, Label targetLabel,
      Map<String, Set<String>> featureBuilderLabelMap);

}
