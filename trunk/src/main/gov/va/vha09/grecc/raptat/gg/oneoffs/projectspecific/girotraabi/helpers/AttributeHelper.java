/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author VHATVHGOBBEG
 *
 */
public class AttributeHelper {

	public enum AttributeIndex {
		DOC_ID, START_OFFSET, END_OFFSET, INDEX_VALUE_STRING, INDEX_VALUE_QUALITATIVE;
	}

	public static double[] INDEX_VALUE_BOUNDS = new double[] {
			Double.NEGATIVE_INFINITY, 0.0, 0.5, 1.0, 1.5, 2.0,
			Double.POSITIVE_INFINITY
	};

	public static final List<String> QUALITATIVE_INDEX_VALUE = new ArrayList<>(
			Arrays.asList("Zero", "ZeroTo0p5", "0p5To1", "1To1p5", "1p5To2",
					"Above2"));

	public static final List<Attribute> COMMON_ATTRIBUTES = AttributeHelper
			.createCommonAttributes();

	/**
	 *
	 */
	private AttributeHelper() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * Assumes that all the string attributes at first indices of an instance
	 * are identifiers. This returns these identifiers as a concatenated string.
	 */
	public static String getAttributeIdentifierHash(final Instance instance) {
		Enumeration<Attribute> attributeEnumerator = instance
				.enumerateAttributes();
		StringBuilder sb = new StringBuilder();
		if ( attributeEnumerator.hasMoreElements() ) {
			Attribute attribute = attributeEnumerator.nextElement();
			if ( !attribute.isString() ) {
				return sb.toString();
			}
			sb.append(instance.stringValue(attribute));
		}

		while ( attributeEnumerator.hasMoreElements() ) {
			Attribute attribute = attributeEnumerator.nextElement();
			if ( !attribute.isString() ) {
				return sb.toString();
			}
			sb.append("_").append(instance.stringValue(attribute));
		}
		return sb.toString();
	}

	public static String getAttributeValueHash(
			final Map<String, Object> attributeValues) {

		StringBuilder sb = new StringBuilder();
		attributeValues.forEach((k, v) -> sb.append(v).append("_"));
		return sb.substring(0, sb.length() - 1);
	}

	public static <T> List<T> getAttributeValues(final Attribute attribute) {

		List<T> values = new ArrayList<>();
		Enumeration<T> valueEnumerator = (Enumeration<T>) attribute
				.enumerateValues();
		while ( valueEnumerator.hasMoreElements() ) {

			T nextValue = valueEnumerator.nextElement();
			values.add(nextValue);
		}
		return values;
	}

	public static List<Attribute> getCommonAttributes() {
		return COMMON_ATTRIBUTES;
	}

	/**
	 * Assigns a string value based on the text within a token phrase that is an
	 * index value, generally of the form 'x.yz' where x, y, and z are integers.
	 *
	 * @param tokenPhrase
	 * @throws NumberFormatException
	 */
	public static String getIndexValueCategory(
			final RaptatTokenPhrase tokenPhrase) throws NumberFormatException {
		double indexValue = Double.parseDouble(tokenPhrase.getPhraseText());
		int valueIndex = 0;
		while ( valueIndex < AttributeHelper.QUALITATIVE_INDEX_VALUE.size() ) {
			if ( (indexValue > AttributeHelper.INDEX_VALUE_BOUNDS[valueIndex])
					&& (indexValue <= AttributeHelper.INDEX_VALUE_BOUNDS[valueIndex
							+ 1]) ) {
				return AttributeHelper.QUALITATIVE_INDEX_VALUE.get(valueIndex);
			}
			valueIndex++;
		}
		return null;
	}

	public static void printAttribute(final Attribute attribute,
			final int attributeIndex) {
		System.out.println("\n\n");
		System.out.println(AbiHelper.PrintMarks.LINE_SEPARATOR);
		System.out.println(" Attribute " + attributeIndex + " :");
		System.out.println("\tType:" + Attribute.typeToString(attribute));
		System.out.println("\tIndex:" + attribute.index());
		System.out.println("\tName:" + attribute.name());

		Enumeration<Object> valueEnumerator = attribute.enumerateValues();
		int valueIndex = 0;
		System.out.println("\t" + AbiHelper.PrintMarks.LINE_SEPARATOR);
		while ( valueEnumerator.hasMoreElements() ) {
			System.out.println("\t\tValue " + valueIndex++ + ":"
					+ valueEnumerator.nextElement());
		}
		System.out.println("\t" + AbiHelper.PrintMarks.LINE_SEPARATOR);
	}

	public static void printAttributeList(
			final ArrayList<Attribute> attributeList) {
		System.out.println("");
		System.out.println(AbiHelper.PrintMarks.DOUBLE_LINE_SEPARATOR);
		System.out.println("ATTRIBUTE LIST");
		System.out.println(AbiHelper.PrintMarks.DOUBLE_LINE_SEPARATOR);

		int attributeIndex = 0;
		for (Attribute attribute : attributeList) {
			printAttribute(attribute, attributeIndex);
			attributeIndex++;
		}

	}

	public static void zeroMissingValues(final Instances instances) {

		Enumeration<Instance> instanceEnumerator = instances
				.enumerateInstances();
		while ( instanceEnumerator.hasMoreElements() ) {
			Instance instance = instanceEnumerator.nextElement();
			Enumeration<Attribute> attributeEnumerator = instances
					.enumerateAttributes();
			while ( attributeEnumerator.hasMoreElements() ) {
				Attribute attribute = attributeEnumerator.nextElement();
				if ( instance.isMissing(attribute) ) {
					instance.setValue(attribute, "0");
				}
			}
		}

	}

	/*
	 * Get the attributes that will be shared across Instance objects and that
	 * will be used largely for identifying the location and text of the
	 * targetConcept we are trying to label, in the case of the ABI project,
	 * this is the index value itself. The last attribute added is a
	 * 'qualitativeIndexValue' to cluster values in the same range together in
	 * case that is important with respect to the features that are important in
	 * a random forest model.
	 */
	private static List<Attribute> createCommonAttributes() {

		List<Attribute> commonAttributes = new ArrayList<>(8);
		List<String> qualitativeIndexValues = new ArrayList<>(Arrays.asList(
				"Zero", "ZeroTo0p5", "0p5To1", "1To1p5", "1p5To2", "Above2"));

		commonAttributes.add(new Attribute(AttributeIndex.DOC_ID.toString(),
				(ArrayList<String>) null));
		commonAttributes
				.add(new Attribute(AttributeIndex.START_OFFSET.toString(),
						(ArrayList<String>) null));
		commonAttributes.add(new Attribute(AttributeIndex.END_OFFSET.toString(),
				(ArrayList<String>) null));
		commonAttributes
				.add(new Attribute(AttributeIndex.INDEX_VALUE_STRING.toString(),
						(ArrayList<String>) null));
		commonAttributes.add(
				new Attribute(AttributeIndex.INDEX_VALUE_QUALITATIVE.toString(),
						new ArrayList<>(qualitativeIndexValues)));

		return commonAttributes;
	}
}
