package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.AttributeHelper;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class InstancesBuilder {

	/**
	 * Build the Instances object based on an existing collection of Attribute
	 * objects. If these come only from the features of RaptatTokenPhrase
	 * objects, we generally add "commonAttributes" that are largely used to
	 * identify different phrases within text. Note that we don't generally need
	 * to add these during evaluation as there will be a set of attributes
	 * provided by the training solution file that includes the classifier.
	 *
	 * @param attributes
	 * @param phraseClass
	 * @param addCommonAttributes
	 * @return
	 */
	public Instances buildInstancesObjectFromAttributes(
			List<Attribute> attributes, PhraseClass phraseClass,
			boolean addCommonAttributes) {
		/*
		 * Common attributes are those that are shared across Instance objects
		 * for training and that will be used largely for identifying the
		 * location and text of the targetConcept we are trying to label, in the
		 * case of the ABI project, this is the index value itself. The last
		 * attribute added is a 'qualitativeIndexValue' to cluster values in the
		 * same range together in case that is important with respect to the
		 * features that are important in a random forest model.
		 *
		 */
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>(
				attributes.size() + AttributeHelper.COMMON_ATTRIBUTES.size());

		if ( addCommonAttributes ) {
			AttributeHelper.COMMON_ATTRIBUTES.stream()
					.forEach(attribute -> attributeList.add(attribute));
		}
		attributes.stream().forEach(attribute -> attributeList.add(attribute));

		Attribute classAttribute = new Attribute(phraseClass.toString(),
				new ArrayList<>(phraseClass.getClassifications()));
		attributeList.add(classAttribute);

		/*
		 * From the Weka API for the Instances constructor:
		 *
		 * Creates an empty set of instances. Uses the given attribute
		 * information. Sets the capacity of the set of instances to 0 if its
		 * negative. Given attribute information must not be changed after this
		 * constructor has been used.
		 */
		Instances instances = new Instances(phraseClass.toString(),
				attributeList, 2048);
		instances.setClass(classAttribute);

		return instances;
	}

	/*
	 * Method used when evaluating existing RandomForest model to determine the
	 * concept mapping of a list of RaptatTokenPhrase instances
	 */
	public void updateInstancesFromTokenPhrases(Instances instancesObject,
			List<RaptatTokenPhrase> candidateTokenPhrases,
			Collection<Attribute> trainingAttributes, PhraseClass phraseClass) {

		for (RaptatTokenPhrase tokenPhrase : candidateTokenPhrases) {
			double[] doubleArray = new double[instancesObject.numAttributes()];
			Arrays.fill(doubleArray, 0);
			Instance instance = new DenseInstance(1, doubleArray);

			instance.setValue(
					instancesObject.attribute(
							AttributeHelper.AttributeIndex.DOC_ID.toString()),
					tokenPhrase.getTextSource());
			instance.setValue(instancesObject.attribute(
					AttributeHelper.AttributeIndex.START_OFFSET.toString()),
					Integer.toString(tokenPhrase.getStartOffsetAsInt()));
			instance.setValue(instancesObject.attribute(
					AttributeHelper.AttributeIndex.END_OFFSET.toString()),
					Integer.toString(tokenPhrase.getEndOffsetAsInt()));
			instance.setValue(instancesObject
					.attribute(AttributeHelper.AttributeIndex.INDEX_VALUE_STRING
							.toString()),
					tokenPhrase.getPhraseText());
			try {
				instance.setValue(instancesObject.attribute(
						AttributeHelper.AttributeIndex.INDEX_VALUE_QUALITATIVE
								.toString()),
						AttributeHelper.getIndexValueCategory(tokenPhrase));
			}
			catch (NumberFormatException e) {
				System.err.println("ERROR MESSAGE:" + e.getLocalizedMessage());
				System.err.println("Unable to parse phrase in document:"
						+ tokenPhrase.getTextSource());
				System.err.println("Error parsing candidate phrase: "
						+ tokenPhrase.getPhraseText());
				continue;
			}

			Set<String> tokenPhraseFeatures = tokenPhrase.getPhraseFeatures();
			Set<Attribute> commonAttributes = new HashSet<Attribute>(
					AttributeHelper.getCommonAttributes());
			for (Attribute attribute : trainingAttributes) {
				if ( !commonAttributes.contains(attribute) ) {
					if ( tokenPhraseFeatures.contains(attribute.name()) ) {
						instance.setValue(attribute,
								RaptatConstants.BINARY_FEATURE_VALUES.get(1));
					}
					// else {
					// instance.setValue(attribute,
					// RaptatConstants.BINARY_FEATURE_VALUES.get(0));
					// }
				}
			}

			instance.setMissing(instancesObject.classAttribute());
			instancesObject.add(instance);
		}
	}

	/**
	 * Build all instances for the token phrases provided in
	 * tokenPhraseToClassMap, the features of these token phrases and the
	 * attributes they are mapped to. The tokenPhraseToClassMap can be set to
	 * null and no classification will be assigned to the instance.
	 *
	 * @param instances
	 *
	 * @param documentName
	 * @param attributeNameToAttributeMap
	 * @param tokenPhraseToClassMap
	 * @return
	 */
	public void updateInstancesObject(final Instances instances,
			final String documentName,
			final Map<String, Attribute> attributeNameToAttributeMap,
			final Map<RaptatTokenPhrase, Optional<String>> tokenPhraseToClassMap) {
		Collection<Attribute> attributes = attributeNameToAttributeMap.values();
		updateInstancesObject(instances, documentName, attributes,
				tokenPhraseToClassMap);
	}

	/**
	 * Build all instances for the token phrases provided in
	 * tokenPhraseToClassMap, the features of these token phrases and the
	 * attributes they are mapped to. The tokenPhraseToClassMap can be set to
	 * null and no classification will be assigned to the instance.
	 *
	 * @param instances
	 *
	 * @param documentName
	 * @param attributeNameToAttributeMap
	 * @param tokenPhraseToClassMap
	 * @return
	 */
	private void updateInstancesObject(final Instances instances,
			final String documentName, final Collection<Attribute> attributes,
			final Map<RaptatTokenPhrase, Optional<String>> tokenPhraseToClassMap) {

		int instancesSize = instances.numAttributes();
		for (RaptatTokenPhrase tokenPhrase : tokenPhraseToClassMap.keySet()) {

			double[] doubleArray = new double[instancesSize];
			Arrays.fill(doubleArray, 0);
			Instance instance = new DenseInstance(1, doubleArray);
			instance.setValue(
					instances.attribute(
							AttributeHelper.AttributeIndex.DOC_ID.toString()),
					documentName);
			instance.setValue(instances.attribute(
					AttributeHelper.AttributeIndex.START_OFFSET.toString()),
					Integer.toString(tokenPhrase.getStartOffsetAsInt()));
			instance.setValue(instances.attribute(
					AttributeHelper.AttributeIndex.END_OFFSET.toString()),
					Integer.toString(tokenPhrase.getEndOffsetAsInt()));
			instance.setValue(instances
					.attribute(AttributeHelper.AttributeIndex.INDEX_VALUE_STRING
							.toString()),
					tokenPhrase.getPhraseText());
			instance.setValue(instances.attribute(
					AttributeHelper.AttributeIndex.INDEX_VALUE_QUALITATIVE
							.toString()),
					AttributeHelper.getIndexValueCategory(tokenPhrase));

			Set<String> tokenPhraseFeatures = tokenPhrase.getPhraseFeatures();
			for (Attribute attribute : attributes) {

				String attributeName = attribute.name();
				if ( tokenPhraseFeatures.contains(attributeName) ) {
					instance.setValue(attribute,
							RaptatConstants.BINARY_FEATURE_VALUES.get(1));
				}
				else {
					instance.setValue(attribute,
							RaptatConstants.BINARY_FEATURE_VALUES.get(0));
				}
			}
			Optional<String> mappedClass = tokenPhraseToClassMap
					.get(tokenPhrase);
			if ( (mappedClass != null) && mappedClass.isPresent() ) {
				instance.setValue(instances.classAttribute(),
						mappedClass.get());
			}
			else {
				instance.setMissing(instances.classAttribute());
			}
			instances.add(instance);
		}
	}
}
