/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.weka.entities;

import java.util.Map;
import java.util.Optional;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.InstancesGetterParameters;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * Extends existing ArffCreationParameter class which will be deprecated and
 * replaced by this one. This class extension allows for a TextAnalyzer field
 * which can be used during .arff file creation.
 *
 * @author VHATVHGOBBEG
 *
 */
public class ArffCreationParameterExtended extends ArffCreationParameter {

	public final TextAnalyzer textAnalyzer;

	public ArffCreationParameterExtended(
			Map<PhraseClass, Optional<String>> phraseClassToXmlMap,
			FeatureBuilder[] featureBuilders, String originProject,
			String fileDescription, InstancesGetterParameters igps) {
		super(igps.iterableData(), phraseClassToXmlMap, igps.arffOutputPath(),
				featureBuilders, originProject, fileDescription);
		this.textAnalyzer = igps.textAnalyzer();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		// TODO Auto-generated method stub

	}

}
