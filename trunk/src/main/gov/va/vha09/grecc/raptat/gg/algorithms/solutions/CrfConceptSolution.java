/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.ConceptFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;

/** @author Glenn T. Gobbel Feb 17, 2016 */
public class CrfConceptSolution extends TokenSequenceFinderSolution {
  /** */
  private static final long serialVersionUID = 653656232876513333L;


  private byte[] model;


  private FeatureBuilder featureBuilder;

  private HashSet<String> conceptSet;

  private TokenProcessingOptions tokenProcessingOptions;

  /**
   * @param modelFile
   * @param featureBuilder
   * @param conceptSet
   * @param tokenProcessingOptions
   */
  public CrfConceptSolution(File modelFile, ConceptFeatureBuilder featureBuilder,
      HashSet<String> conceptSet, TokenProcessingOptions tokenProcessingOptions) {
    this.model = CrfModelFileConverter.getModelAsArray(modelFile);
    this.featureBuilder = featureBuilder;
    this.conceptSet = conceptSet;
    this.tokenProcessingOptions = tokenProcessingOptions;
  }

  private CrfConceptSolution() {
    // TODO Auto-generated constructor stub
  }


  /** @return the conceptSet */
  public HashSet<String> getConceptSet() {
    return this.conceptSet;
  }


  /** @return the featureBuilder */
  public FeatureBuilder getFeatureBuilder() {
    return this.featureBuilder;
  }


  /** @return the model */
  public byte[] getModel() {
    return this.model;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.candidates.coremodifications. solutions
   * .TokenSequenceFinderSolution#retrieveTokenProcessingOptions()
   */
  @Override
  public TokenProcessingOptions retrieveTokenProcessingOptions() {
    return this.tokenProcessingOptions;
  }


  /**
   * @param solutionFile
   */
  public void saveToFile(File solutionFile) {
    ObjectOutputStream obj_out = null;

    try {
      // Write object with ObjectOutputStream
      obj_out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(solutionFile),
          RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));

      // Write object out to disk
      obj_out.writeObject(this);

    } catch (FileNotFoundException e) {
      System.out.println("Unable to find solution file for writing");
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Problem writing solution file");
      e.printStackTrace();
    } finally {
      if (obj_out != null) {
        try {
          obj_out.flush();
          obj_out.close();
        } catch (IOException e) {
          System.out.println("Unable to close output stream " + "after writing to solution file");
          e.printStackTrace();
        }
      }
    }
  }


  /**
   * @param modelFilePath
   */
  public File writeModel(String modelFilePath) {
    File binaryFile = new File(modelFilePath);
    binaryFile = CrfModelFileConverter.writeModelToFile(binaryFile, this.model);
    return binaryFile;
  }


  public static void main(String[] args) {
    CrfConceptSolution ccs = new CrfConceptSolution();

    String initialModelPath = "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\"
        + "CV10FoldSolution_Filtered_Training_OvertRNMFeat_160623_220652\\Group_1\\CrfSolution_Group_1.mdl";

    String savedModelPath =
        "H:\\RaptatEclipseWorkspace_160508\\RaptatOnly_SanjibForReview\\undefined\\model_160626_150110.mdl";

    String secondModelPath =
        "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CV10FoldSolution_Filtered_Training_OvertRNMFeat_160623_220652\\Group_2\\CrfSolution_Group_2.mdl";

    boolean areEqual;

    areEqual = compareModels(initialModelPath, savedModelPath);
    System.out.println("Initial model and saved model are the same:" + areEqual);

    areEqual = compareModels(initialModelPath, secondModelPath);
    System.out.println("Initial model and second model are the same:" + areEqual);

    areEqual = compareModels(savedModelPath, secondModelPath);
    System.out.println("Saved model and second model are the same:" + areEqual);
  }


  private static boolean compareModels(String modelPathOne, String modelPathTwo) {
    byte[] modelOne = CrfModelFileConverter.getModelAsArray(new File(modelPathOne));
    byte[] modelTwo = CrfModelFileConverter.getModelAsArray(new File(modelPathTwo));
    return Arrays.equals(modelOne, modelTwo);
  }
}
