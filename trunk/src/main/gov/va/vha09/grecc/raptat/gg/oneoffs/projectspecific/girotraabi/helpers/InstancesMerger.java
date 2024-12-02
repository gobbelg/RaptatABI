package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Takes an Instances object and merges it with another one in different ways to
 * create compatible Instances objects for training, testing, and evaluation of
 * a classifier.
 *
 * @author VHATVHGOBBEG
 *
 */
public class InstancesMerger {

	private static final Logger LOGGER = Logger
			.getLogger(InstancesMerger.class);
	static {
		LOGGER.setLevel(Level.INFO);
	}

	/**
	 *
	 */
	public InstancesMerger() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Take a given list of Instances objects and return another list where the
	 * Instances objects over all those in the list have the same set of
	 * attributes. The identifierAttributes and classIndex of the attributes in
	 * each Instances object are assumed to be the same across all instances.
	 * The attribute values within each of the Instance objects in each of the
	 * Instances objects are unchanged by this process. Any added attribute to a
	 * given Instance object will have the value of 'missing.'
	 *
	 * @param instancesList
	 * @param instancesTag
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static List<Instances> conformInstances(
			final List<Instances> instancesList, final String instancesTag)
			throws IllegalArgumentException {

		ArrayList<Attribute> mergedAttributeList = mergeIntancesAttributes(
				instancesList);

		if ( LOGGER.getLevel() == Level.DEBUG ) {
			AttributeHelper.printAttributeList(mergedAttributeList);
		}

		List<Instances> resultList = new ArrayList<>(instancesList.size());
		for (Instances inputInstances : instancesList) {

			String instancesName = inputInstances.relationName() + "_"
					+ instancesTag;
			Instances resultInstances = new Instances(instancesName,
					mergedAttributeList, inputInstances.size());
			int numberOfMergedAttributes = resultInstances.numAttributes();
			resultInstances.setClassIndex(numberOfMergedAttributes - 1);
			Enumeration<Instance> instanceEnumerator = inputInstances
					.enumerateInstances();
			while ( instanceEnumerator.hasMoreElements() ) {

				Instance inputInstance = instanceEnumerator.nextElement();
				Instance resultInstance = new DenseInstance(
						numberOfMergedAttributes);

				/*
				 * Set a reference from within the instance to the Instances
				 * object (i.e., 'dataset) it will be within. Does not check if
				 * the instance is compatible with the Instances object. Note:
				 * the Instances object does not know about this instance. If
				 * the structure of the Instances object's header gets changed,
				 * this instance will not be adjusted automatically.
				 *
				 * That is, the Instances object should know its own name, the
				 * attributes of the Instance objects that refer to it, and the
				 * number of Instance objects that refer to it.
				 */
				resultInstance.setDataset(resultInstances);

				int inputAttributeSize = inputInstance.numAttributes();
				double[] inputAttributeValues = inputInstance.toDoubleArray();
				for (int i = 0; i < inputAttributeSize; i++) {

					String attributeName = inputInstance.attribute(i).name();
					Attribute resultAttribute = resultInstances
							.attribute(attributeName);
					if ( resultAttribute.type() == Attribute.STRING ) {
						/*
						 * Get the value of the attribute at index i of input
						 * instances, which should be a String object because
						 * resultAttribute and inputInstance.attribute(i) have
						 * the same name and should therefore be of the same
						 * type.
						 */
						String value = inputInstance.stringValue(i);
						resultInstance.setValue(resultAttribute, value);
						continue;
					}
					resultInstance.setValue(resultAttribute,
							inputAttributeValues[i]);
				}

				resultInstances.add(resultInstance);
			}
			resultList.add(resultInstances);
		}

		return resultList;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param instancesList
	 * @param classIndex
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static ArrayList<Attribute> mergeIntancesAttributes(
			final List<Instances> instancesList)
			throws IllegalArgumentException {

		Set<Attribute> mergedAttributeSet = new HashSet<>();
		Set<Attribute> mergedIdentifierSet = new HashSet<>();
		Set<Attribute> mergedClassSet = new HashSet<>();
		System.out.println("Merging model and evaluation set instances");
		/*
		 * First collect all the attributes
		 */
		for (Instances instances : instancesList) {
			/*
			 * Class attribute is not covered by enumerator
			 */
			Attribute classAttribute = instances.classAttribute();
			mergedClassSet.add(new Attribute(classAttribute.name(),
					AttributeHelper.getAttributeValues(classAttribute)));
			Enumeration<Attribute> attributeEnumerator = instances
					.enumerateAttributes();
			while ( attributeEnumerator.hasMoreElements() ) {
				Attribute nextAttribute = attributeEnumerator.nextElement();

				/*
				 * In the case of String type attributes, we have to create a
				 * new one because each Attribute of this type stores all the
				 * string values for the individual Instance objects in an
				 * Instances data set. It stores them in the form of a map of
				 * the string to an index. In any String Attribute within an
				 * Instance object, the value is stored as an index rather than
				 * the string.
				 */
				if ( nextAttribute.isString() ) {
					mergedIdentifierSet.add(new Attribute(nextAttribute.name(),
							(ArrayList<String>) null));
				}
				else {
					mergedAttributeSet.add(new Attribute(nextAttribute.name(),
							AttributeHelper.getAttributeValues(nextAttribute)));
				}
			}
		}

		if ( mergedClassSet.size() > 1 ) {
			throw new IllegalArgumentException(
					"All Instances object supplied to"
							+ " conformInstances method must have the identical class attribute at the same index");
		}
		ArrayList<Attribute> mergedAttributeList = new ArrayList<>();
		mergedAttributeList.addAll(mergedIdentifierSet);
		mergedAttributeList.addAll(mergedAttributeSet);
		mergedAttributeList.addAll(mergedClassSet);
		return mergedAttributeList;
	}

}
