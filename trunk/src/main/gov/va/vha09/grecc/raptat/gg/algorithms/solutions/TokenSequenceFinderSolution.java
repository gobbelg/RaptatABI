/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/** @author Glenn T. Gobbel Feb 17, 2016 */
public abstract class TokenSequenceFinderSolution implements Serializable {

  private static final long serialVersionUID = 5716973423340348845L;


  public abstract TokenProcessingOptions retrieveTokenProcessingOptions();


  /**
   * Load a solution from a file
   *
   * @return AlgorithmSolution
   * @throws ClassNotFoundException
   */
  public static TokenSequenceFinderSolution loadFromFile() {
    return loadFromFile("Select TokenSequenceFinder Solution File");
  }


  /**
   * Load a solution from a file, if the solutionFile parameter is null, the method return an empty
   * solution with Options set to the ones in the OptionsManager
   *
   * @param solutionFile
   * @return AnnotationSolution
   */
  public static TokenSequenceFinderSolution loadFromFile(File solutionFile) {
    TokenSequenceFinderSolution theSolution = null;
    ObjectInputStream inObject = null;

    try {
      inObject = new ObjectInputStream(new BufferedInputStream(new FileInputStream(solutionFile)));
      theSolution = (TokenSequenceFinderSolution) inObject.readObject();
    } catch (Exception e) {
      System.out.println("Unable to open and read in solution file");
      e.printStackTrace();
    } finally {
      try {
        if (inObject != null) {
          inObject.close();
        }
      } catch (IOException e) {
        System.out.println("Unable to close solution file");
        e.printStackTrace();
      }
    }

    return theSolution;
  }


  /**
   * Load a solution from a file
   *
   * @param prompt
   * @return AlgorithmSolution *
   */
  public static TokenSequenceFinderSolution loadFromFile(String prompt) {
    // Get the solutionFile
    OptionsManager theInstance = OptionsManager.getInstance();
    String lastDirectory = theInstance.getLastSelectedDir().getAbsolutePath();
    String[] fileType = {".soln"};
    File solutionFile = GeneralHelper.getFileByType(prompt, lastDirectory, fileType);

    if (solutionFile != null) {
      theInstance.setLastSelectedDir(solutionFile.getParentFile());
      return loadFromFile(solutionFile);
    }

    return null;
  }
}
