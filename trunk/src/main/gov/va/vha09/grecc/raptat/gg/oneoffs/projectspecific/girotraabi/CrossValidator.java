package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.AbiHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.AttributeHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.options.OptionsBuilder;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.output.prediction.InMemory;
import weka.classifiers.evaluation.output.prediction.InMemory.PredictionContainer;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;


public class CrossValidator {

  enum ActualVsPredictedClass {
    ACTUAL, PREDICTED;
  }

  private enum RunType {
    BASE_METHOD, RUN_INSTANCES;
  }


  Classifier getFilteredClassifier(final List<String> options) throws Exception {
    FilteredClassifier classifier = new FilteredClassifier();
    classifier.setOptions(options.toArray(new String[] {}));
    return classifier;
  }



  /**
   * Run cross-validation on sets of Mapped Instances objects mapped by concept (e.g., indexValue or
   * calcification) and phraseClass (e.g. left/right, abi/tbi).
   *
   * This is same method as runCVClassification but this uses PhraseClass objects in the map instead
   * of strings
   *
   * @param crossValidator
   *
   *
   * @param classifier
   * @param mappedInstances
   * @param optionsArray
   * @param filteredClassifierOptions
   * @return Return a mapping from a concatentation of attribute values to the actual and predicted
   *         classifications and a boolean indicating if actual and predicted are the same.
   * @throws NumberFormatException
   * @throws Exception
   */
  Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> runCVClassificationByPhraseClass(
      final Map<String, Map<PhraseClass, Instances>> mappedInstances,
      final List<String> crossValidationOptions, final List<String> filteredClassifierOptions)
      throws NumberFormatException, Exception {

    Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> resultMap =
        new HashMap<>();

    for (String conceptName : mappedInstances.keySet()) {

      Map<String, Map<String, Triple<String, String, Boolean>>> conceptResultMap =
          resultMap.computeIfAbsent(conceptName, k -> new HashMap<>());
      Map<PhraseClass, Instances> phraseClassToInstancesMap = mappedInstances.get(conceptName);

      for (PhraseClass phraseClass : phraseClassToInstancesMap.keySet()) {

        System.out.println(
            "CrossValidation Concept: " + conceptName + " PhraseClass: " + phraseClass.toString());

        Classifier classifier = getFilteredClassifier(filteredClassifierOptions);

        Instances instances = phraseClassToInstancesMap.get(phraseClass);
        Map<String, Triple<String, String, Boolean>> predictions = getCrossValidationPredictions(
            classifier, instances, crossValidationOptions.toArray(new String[] {}));
        conceptResultMap.put(phraseClass.toString(), predictions);
      }
    }

    return resultMap;
  }



  private Map<String, Triple<String, String, Boolean>> getCrossValidationPredictions(
      final Classifier cls, final Instances data, final String[] options)
      throws NumberFormatException, Exception {

    Map<String, Triple<String, String, Boolean>> resultMap = new HashMap<>();
    int seed = Integer.parseInt(Utils.getOption("s", options));
    int folds = Integer.parseInt(Utils.getOption("x", options));

    // cross-validate (10-fold) classifier, collects the prediction
    Evaluation eval = new Evaluation(data);
    InMemory predictionStore = new InMemory();

    /*
     * Additional attributes to store as well (e.g. ID for later identification)
     */
    String attributesToStore = Utils.getOption("po", options);
    predictionStore.setAttributes(attributesToStore);
    // instances)
    eval.crossValidateModel(cls, data, folds, new Random(seed), predictionStore);

    // output collected predictions
    Attribute classAttribute = data.classAttribute();

    for (PredictionContainer predictionContainer : predictionStore.getPredictions()) {
      String attributeValueHash =
          AttributeHelper.getAttributeValueHash(new TreeMap<>(predictionContainer.attributeValues));
      NominalPrediction nominalPrediction = (NominalPrediction) predictionContainer.prediction;
      String predictedClass = classAttribute.value((int) Math.round(nominalPrediction.predicted()));
      String actualClass = classAttribute.value((int) Math.round(nominalPrediction.actual()));

      resultMap.put(attributeValueHash, ImmutableTriple.of(actualClass, predictedClass,
          actualClass.equalsIgnoreCase(predictedClass)));

      System.out.println("- attribute values:\n" + attributeValueHash);
      System.out.println("Actual:" + actualClass);
      System.out.println("Predicted:" + predictedClass);
    }

    return resultMap;
  }



  private Instances getInstances(final List<String> optionsList) throws Exception {
    String[] optionsArray = optionsList.toArray(new String[] {});
    String filePath = Utils.getOption("t", optionsArray);
    CSVLoader loader = new CSVLoader();
    loader.setFieldSeparator("\\t");
    loader.setFile(new File(filePath));
    Instances instances = loader.getDataSet();
    instances.setClassIndex(instances.numAttributes() - 1);
    return instances;
  }

  /**
   * Run cross-validation on sets of Mapped Instances objects mapped by concept (e.g., indexValue or
   * calcification) and phraseClass (e.g. left/right, abi/tbi).
   *
   *
   * @param classifier
   * @param mappedInstances
   * @param optionsArray
   * @return Return a mapping from a concatentation of attribute values to the actual and predicted
   *         classifications and a boolean indicating if actual and predicted are the same.
   * @throws NumberFormatException
   * @throws Exception
   */
  private Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> runCVClassification(
      final Classifier classifier, final Map<String, Map<String, Instances>> mappedInstances,
      final String[] optionsArray) throws NumberFormatException, Exception {

    Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> resultMap =
        new HashMap<>();

    for (String conceptName : mappedInstances.keySet()) {

      Map<String, Map<String, Triple<String, String, Boolean>>> conceptResultMap =
          resultMap.computeIfAbsent(conceptName, k -> new HashMap<>());
      Map<String, Instances> phraseClassToInstancesMap = mappedInstances.get(conceptName);

      for (String phraseClassName : phraseClassToInstancesMap.keySet()) {

        Instances instances = phraseClassToInstancesMap.get(phraseClassName);
        Map<String, Triple<String, String, Boolean>> predictions =
            getCrossValidationPredictions(classifier, instances, optionsArray);
        conceptResultMap.put(phraseClassName, predictions);
      }
    }

    return resultMap;
  }

  /**
   * Performs the cross-validation. See Javadoc of class for information on command-line parameters.
   *
   * @param args the command-line parameters
   * @throws Exception if something goes wrong
   */
  public static void main(final String[] args) throws Exception {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        RunType runType = RunType.RUN_INSTANCES;
        CrossValidator cvTest = new CrossValidator();

        switch (runType) {
          case BASE_METHOD:

            break;

          case RUN_INSTANCES:
            try {
              String directoryPath = "C:\\Users\\gtony\\OneDrive\\Desktop\\tempForWekaTesting";

              List<String> instanceInputOptions = new ArrayList<>();

              instanceInputOptions.add("-t");
              instanceInputOptions.add(directoryPath + File.separator + "tempForWekaTesting.txt");
              Instances instances = cvTest.getInstances(instanceInputOptions);

              Map<String, Instances> innerInstances = new HashMap<>();
              innerInstances.put("phraseClass", instances);
              Map<String, Map<String, Instances>> testMappedInstances = new HashMap<>();
              testMappedInstances.put("concept", innerInstances);

              String attributesForRemoval = "1";
              List<String> filteredClassifierOptions =
                  OptionsBuilder.generateFilteredClassifierOptions(attributesForRemoval);
              Classifier classifier = cvTest.getFilteredClassifier(filteredClassifierOptions);

              List<String> crossValidationOptions = new ArrayList<>();
              OptionsBuilder.addCrossValidationOptions(crossValidationOptions);

              OptionsBuilder.addPredictionOptions(crossValidationOptions, attributesForRemoval);

              Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> resultMap =
                  cvTest.runCVClassification(classifier, testMappedInstances,
                      crossValidationOptions.toArray(new String[] {}));
              Map<String, Map<String, List<String>>> matchedResults =
                  AbiHelper.matchPhraseClassesByConcept(resultMap);
              AbiHelper.printMatchedResults(matchedResults, directoryPath);
            } catch (Exception e) {
              e.printStackTrace();
            }
            break;

          default:
            break;
        }

      }



    });

  }
}
