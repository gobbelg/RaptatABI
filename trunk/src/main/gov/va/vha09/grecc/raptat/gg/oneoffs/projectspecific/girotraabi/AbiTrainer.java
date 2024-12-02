package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import ami.ClassifierDetails;
import ami.MultiSearchFactory;
import ami.MultiSearchInputParameter;
import ami.base.EvaluationMetricType;
import ami.classifier.config.BaseClassifierHyperparameterConfiguration;
import common.AltStratifiedInstances;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.exporters.ArffExportDescriptor;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.exporters.ArffExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * AbiTrainer
 *
 * @author gtony
 * @date May 26, 2019
 *
 */
public class AbiTrainer {

  public static interface IOnArffWritten {
    public void callback(AbiTrainer trainer) throws FileNotFoundException, IOException;
  }

  private enum RunType {
    TEST_RUN;
  }


  private ArffExporter arffExporter;

  private Attribute attribute_to_fold_on;

  private Set<BaseClassifierHyperparameterConfiguration> trainers;

  Set<Attribute> attributesToIgnore = new HashSet<>();

  public AbiTrainer(String origin, String fileDescription, PhraseClass phraseClass,
      String arffFileDirectoryPath,
      Set<BaseClassifierHyperparameterConfiguration> classifier_configurations) throws IOException {
    ArffExportDescriptor exporterDescriptor =
        new ArffExportDescriptor(origin, fileDescription, phraseClass);
    this.arffExporter = new ArffExporter(exporterDescriptor, arffFileDirectoryPath);
    this.setTrainingModel(classifier_configurations);
  }


  /**
   * Need to have the ability to ignore features in an arff file for model evaluation
   *
   * @param attribute
   */
  public void add_attribute_to_ignore(Attribute attribute) {
    if (attribute.index() == -1) {
      throw new IllegalArgumentException("attribute must be set in data source to get index.");
    }
    this.attributesToIgnore.add(attribute);
  }


  public void fold_on(Attribute fold_attribute) {
    if (fold_attribute.index() == -1) {
      throw new IllegalArgumentException("attribute must be set in data source to get index.");
    }
    this.attribute_to_fold_on = fold_attribute;

  }


  public String get_arff_file_path() {
    return this.arffExporter.getFilePath();
  }


  /**
   * Train the machine learning model based on the .arff file created by each call to update. Note
   * that once this is called, the arffExporter instance variable is closed so that no further data
   * can be added. This is because the exporter closes the files used to generate the .arff file for
   * training.
   *
   * @param onArffWrittenCallback
   *
   * @return
   */
  public ClassifierDetails train(IOnArffWritten onArffWrittenCallback) {
    try {
      this.arffExporter.close();

      onArffWrittenCallback.callback(this);

      boolean showDebug = true;

      BufferedReader reader = new BufferedReader(new FileReader(this.arffExporter.getFilePath()));
      ArffReader arff = new ArffReader(reader);
      /**
       * Wrapped with InstancesExt, a class that stratifies by attribute
       */
      Instances temp_data = arff.getData();
      AltStratifiedInstances data = new AltStratifiedInstances(temp_data);
      final Attribute class_attribute = data.attribute("class");
      data.setClassIndex(class_attribute.index());
      EvaluationMetricType eval_metric = EvaluationMetricType.EVALUATION_MATTHEWS_CC;
      MultiSearchInputParameter inputParameter = new MultiSearchInputParameter(data, this.trainers,
          eval_metric, this.attributesToIgnore, this.attribute_to_fold_on);
      final MultiSearchFactory msf = new MultiSearchFactory(inputParameter, true);

      final Map<Integer, ClassifierDetails> create_output_params = msf.process(showDebug);

      /**
       * Get first entry
       */
      return create_output_params.get(create_output_params.keySet().iterator().next().intValue());

    } catch (Exception e) {
      System.err.println("Unable to close .arff files for training");
      System.err.println(e.getLocalizedMessage());
      e.printStackTrace();
    }

    return null;
  }


  /**
   * Updates an ArffExporter object that writes the features of the token phrases in the object and
   * their assigned class to a .arff file.
   *
   * @param document
   * @param textPath
   * @param xmlPath
   * @return
   * @return
   * @throws IOException - Thrown if there is no file corresponding to textPath or xmlPath
   */
  public List<RaptatTokenPhrase> update(RaptatDocument document, String textPath, String xmlPath)
      throws IOException {
    return this.arffExporter.exportDocumentPhraseFeatures(document, textPath, xmlPath);
  }


  /**
   * Provide the Weka training model (e.g. decision tree, SVM, etc) that will be used when the train
   * method is called
   *
   * @param trainingModel
   */
  private void setTrainingModel(Set<BaseClassifierHyperparameterConfiguration> trainingModel) {
    this.trainers = trainingModel;
  }


  public static void main(String[] args) {

    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RunType runType = RunType.TEST_RUN;

        switch (runType) {
          case TEST_RUN:
            break;
          default:
            break;
        }
        System.exit(0);
      }
    });
  }
}
