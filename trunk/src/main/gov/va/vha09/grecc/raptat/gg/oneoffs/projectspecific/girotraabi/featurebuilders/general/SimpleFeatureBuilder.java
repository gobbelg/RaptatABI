/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

/**
 * @author gobbelgt
 *
 */
public class SimpleFeatureBuilder implements BaseFeatureBuilder {

  /**
   * Implements the addPhraseFeatures method of the implemented interface.
   *
   */
  @Override
  public void addTokenPhraseFeatures(final Collection<RaptatTokenPhrase> tokenPhrases,
      final Label targetLabel, final Map<String, Set<String>> featureBuilderLabelMap) {

    /*
     * Target phrases are those with the targetLabel. In the case of the ABI project, this is the
     * label with the name "indexvalue." Essentially, there are typically, multiple tokenPhrases,
     * but we are only interested here in collecting the ones that are putative index values (like
     * '0.85' or '1.2' etc).
     */
    List<RaptatTokenPhrase> targetPhrases = new ArrayList<>();
    for (RaptatTokenPhrase tokenPhrase : tokenPhrases) {
      if (tokenPhrase.getPhraseLabels().contains(targetLabel)) {
        targetPhrases.add(tokenPhrase);
      }
    }

    for (RaptatTokenPhrase targetPhrase : targetPhrases) {
      targetPhrase.addRowFeatures(featureBuilderLabelMap);
      targetPhrase.addDocumentFeatures(featureBuilderLabelMap);
      targetPhrase.addColumnFeatures(featureBuilderLabelMap);
    }
  }

}
