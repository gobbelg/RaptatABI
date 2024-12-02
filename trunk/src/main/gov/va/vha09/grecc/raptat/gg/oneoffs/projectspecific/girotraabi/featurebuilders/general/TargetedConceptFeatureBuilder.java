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
public class TargetedConceptFeatureBuilder implements BaseFeatureBuilder {

	/**
	 * Implements the addPhraseFeatures method of the implemented interface.
	 *
	 */
	@Override
	public void addTokenPhraseFeatures(
			final Collection<RaptatTokenPhrase> targetedPhrases,
			final Label targetLabel,
			final Map<String, Set<String>> categoryToSubcategoryMap) {

		for (RaptatTokenPhrase targetPhrase : targetedPhrases) {
			targetPhrase.addRowFeatures(categoryToSubcategoryMap);
			targetPhrase.addDocumentFeatures(categoryToSubcategoryMap);
			targetPhrase.addColumnFeatures(categoryToSubcategoryMap);
		}
	}
}
