/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.TokenPhraseMaker;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import weka.classifiers.Classifier;
import weka.core.Attribute;

/**
 * @author VHATVHGOBBEG
 *
 */
public record AbiSolution(
		Map<String, Map<PhraseClass, Classifier>> abiClassifierMapping,
		Map<String, Map<PhraseClass, List<Attribute>>> mapToTrainingAttributes,
		Map<String, Map<PhraseClass, Attribute>> mapToClassAttributes,
		TokenPhraseMaker tokenPhraseMaker,
		TokenProcessingOptions tokenProcessingOptions) implements Serializable {

	// private static Map<String, Map<PhraseClass, Map<String, Attribute>>>
	// mapToTrainingAttributeNames;

	/*
	 * Use the mapToTrainingAttributes field to create a
	 * maptoTrainingAttributeNames, which will be used to look up attributes
	 * within the model without having to repeatedly create attributes from
	 * RaptatTokenPhrase features or determine the names of the attributes to
	 * look up features (which are strings).
	 */
	// public AbiSolution {
	// mapToTrainingAttributeNames = new HashMap<String, Map<PhraseClass,
	// Map<String, Attribute>>>();
	// for (String conceptName : mapToTrainingAttributes.keySet()) {
	// Map<PhraseClass, Set<Attribute>> phraseClassToAttributeSet =
	// mapToTrainingAttributes
	// .get(conceptName);
	// Map<PhraseClass, Map<String, Attribute>> phraseClassToAttributeNames =
	// mapToTrainingAttributeNames
	// .computeIfAbsent(conceptName, key -> new HashMap<>());
	// for (PhraseClass phraseClass : phraseClassToAttributeSet.keySet()) {
	// Set<Attribute> attributeSet = phraseClassToAttributeSet
	// .get(phraseClass);
	// Map<String, Attribute> attributeNameToAttributeMap =
	// phraseClassToAttributeNames
	// .computeIfAbsent(phraseClass, key -> new HashMap<>());
	// for (Attribute attribute : attributeSet) {
	// attributeNameToAttributeMap.put(attribute.name(),
	// attribute);
	// }
	// }
	// }
	// }

	// public Map<String, Map<PhraseClass, Map<String, Attribute>>>
	// getMapToTrainingAttributeNames() {
	// return mapToTrainingAttributeNames;
	// }

	public void save(String modelPathString) {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(new File(modelPathString))))) {
			oos.writeObject(this);
			oos.flush();
		}
		catch (IOException e) {
			System.err.println("Unable to save ABI solution model file to "
					+ modelPathString);
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public static AbiSolution load(String modelPathString) {
		return load(new File(modelPathString));
	}

	public static AbiSolution load(String modelDirectory, String modelName) {
		Path modelPath = Paths.get(modelDirectory, modelName);
		File modelFile = modelPath.toFile();
		return load(modelFile);
	}

	private static AbiSolution load(File modelFile) {

		System.out.println("LOADING MODEL:" + modelFile.getAbsolutePath());
		AbiSolution abiSolution = null;
		try (ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(modelFile)))) {
			System.out.println("READING IN SERIALIZED MODEL");
			abiSolution = (AbiSolution) ois.readObject();
			System.out.println("MODEL READ FINISHED");
		}
		catch (ClassNotFoundException | IOException e) {
			System.err.println(
					"Unable to read model file:" + modelFile.getAbsolutePath());
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		return abiSolution;
	}
}
