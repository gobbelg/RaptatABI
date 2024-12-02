/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/**
 * Reads in CRF-based cue and scope model files into byte []'s for storage with RapTAT objects like
 * Solution instances.
 *
 * @author VHATVHGOBBEG
 */
class CrfModelFileConverter {

  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        String cueFilePath =
            "D:\\GobbelWorkspace\\Subversion\\branches\\GlennWorkspace\\Projects\\RaptatOnly_SanjibForReview\\CRFCueModel_160210.mdl";
        String scopeFilePath =
            "D:\\GobbelWorkspace\\Subversion\\branches\\GlennWorkspace\\Projects\\RaptatOnly_SanjibForReview\\CRFScopeModel_160210.mdl";
        try {
          byte[] cueModel = CrfModelFileConverter.getModelAsArray(new File(cueFilePath));
          byte[] scopeModel = CrfModelFileConverter.getModelAsArray(new File(scopeFilePath));

          String movedFilePath =
              "D:\\CARTCL-IIR\\ActiveLearning\\AKIAssertionTesting\\NegexCRFTrainingGG_";
          File cueFile = new File(movedFilePath + File.separator + "cueModelMoved");
          File scopeFile = new File(movedFilePath + File.separator + "scopeModelMoved");
          cueFile = CrfModelFileConverter.writeModelToFile(cueFile, cueModel);
          scopeFile = CrfModelFileConverter.writeModelToFile(scopeFile, scopeModel);

          System.out.println("Cue file:" + cueFile.getCanonicalPath());
          System.out.println("Scope file:" + scopeFile.getCanonicalPath());

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  static byte[] getModelAsArray(File binaryFile) {
    byte[] model;
    try {
      model = FileUtils.readFileToByteArray(binaryFile);
    } catch (IOException e) {
      System.err.println("Unable to get CRF model:" + e.getMessage());
      e.printStackTrace();
      model = null;
    }

    return model;
  }


  static File writeModelToFile(File modelFile, byte[] model) {
    try {
      FileUtils.writeByteArrayToFile(modelFile, model);
    } catch (IOException e) {
      System.err.println("Unable to create file to store CRF model:" + e.getMessage());
      e.printStackTrace();
      modelFile = null;
    }

    return modelFile;
  }
}
