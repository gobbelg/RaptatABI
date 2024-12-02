package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.CSFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * Convenience class that allows CRF models built by programs such as CRFSuite to be stored with the
 * general RapTAT Solution object.
 *
 * @author VHATVHGOBBEG
 */
public class AttributeTaggerSolution implements Serializable {

  private static final long serialVersionUID = 4629101448539040991L;


  private byte[] cueModel;


  private byte[] scopeModel;

  private FeatureBuilder featureBuilder;

  private ContextType context;

  private TokenProcessingOptions tokenProcessingOptions;

  public AttributeTaggerSolution(File cueModelFile, File scopeModelFile,
      FeatureBuilder featureBuilder, ContextType context,
      TokenProcessingOptions tokenProcessingOptions) {
    this.featureBuilder = featureBuilder;
    this.context = context;
    this.tokenProcessingOptions = tokenProcessingOptions;
    readCueModel(cueModelFile);
    readScopeModel(scopeModelFile);
  }

  /** @return the attributeName */
  public String getAttributeName() {
    return this.context.getTypeName();
  }


  /** @return the context */
  public ContextType getContext() {
    return this.context;
  }


  /** @return the cueModelArray */
  public byte[] getCueModelArray() {
    return this.cueModel;
  }


  /** @return the featureBuilder */
  public FeatureBuilder getFeatureBuilder() {
    return this.featureBuilder;
  }


  /** @return the potentialAttributeValues */
  public Set<String> getPotentialAttributeValues() {
    return this.context.getPotentialValues();
  };


  /** @return the scopeModelArray */
  public byte[] getScopeModelArray() {
    return this.scopeModel;
  }


  /** @return the tokenProcessingOptions */
  public TokenProcessingOptions getTokenProcessingOptions() {
    return this.tokenProcessingOptions;
  }


  /**
   * Save the database
   *
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
   * @param featureBuilder the featureBuilder to set
   */
  public void setFeatureBuilder(FeatureBuilder featureBuilder) {
    this.featureBuilder = featureBuilder;
  }


  public File writeCueModel(String filePath) {
    File binaryFile = new File(filePath);
    binaryFile = CrfModelFileConverter.writeModelToFile(binaryFile, this.cueModel);
    return binaryFile;
  }


  public File writeScopeModel(String filePath) {
    File crfSuiteScopeFile = new File(filePath);
    crfSuiteScopeFile = CrfModelFileConverter.writeModelToFile(crfSuiteScopeFile, this.scopeModel);
    return crfSuiteScopeFile;
  }


  private void readCueModel(File binaryFile) {
    this.cueModel = CrfModelFileConverter.getModelAsArray(binaryFile);
  }


  private void readScopeModel(File binaryFile) {
    this.scopeModel = CrfModelFileConverter.getModelAsArray(binaryFile);
  }


  public static AttributeTaggerSolution loadFromFile(File solutionFile) {
    AttributeTaggerSolution theSolution = null;
    ObjectInputStream inObject = null;

    try {
      inObject = new ObjectInputStream(new FileInputStream(solutionFile));
      theSolution = (AttributeTaggerSolution) inObject.readObject();
    } catch (IOException e) {
      GeneralHelper.errorWriter("Unable to open and read in solution file");
      System.out.println("Unable to open and read in solution file.  " + e.getClass().getName()
          + " Error " + e.getMessage());
    } catch (ClassNotFoundException e) {
      System.out.println("Unable to open and read in solution file. " + e.getClass().getName()
          + " Error " + e.getMessage());
    } finally {
      try {
        if (inObject != null) {
          inObject.close();
        }
      } catch (IOException e) {
        System.out.println("Unable to close solution file\n" + e.getMessage());
      }
    }

    if (theSolution.featureBuilder instanceof CSFeatureBuilder) {
      ((CSFeatureBuilder) theSolution.featureBuilder).initializeTransients();
    }
    return theSolution;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {}
    });
  }
}
