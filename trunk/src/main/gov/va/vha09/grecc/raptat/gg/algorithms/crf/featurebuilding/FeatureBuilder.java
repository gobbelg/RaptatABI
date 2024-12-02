/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import org.apache.log4j.Logger;
import cc.mallet.pipe.Pipe;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Classes extending this abstract class should be able to generate "features" that are used by
 * CRFSuite.
 */
public abstract class FeatureBuilder implements Serializable {
  private static final long serialVersionUID = 7914476044606332724L;

  protected static final String GREEK = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|"
      + "lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega)";
  protected static final Logger LOGGER = Logger.getLogger(FeatureBuilder.class);
  /*
   * The pipe will determine and create the features, stored in instances within the instance list
   */
  protected Pipe featureBuilderPipe = null;


  public FeatureBuilder() {}


  public abstract CrfFeatureSet buildFeatures(List<AnnotatedPhrase> sentences);


  /**
   * CRFSuite uses ':' and '\' characters as markers for information for the following characters.
   * The characters must be escaped o allow use of ':' and '\' as actual text objects rather than
   * markers.
   *
   * @param inputString
   * @return
   */
  protected String escapeCrfSuiteCharacters(String inputString) {
    /*
     * This block escapes the colon and backslash, converting ':' to '\:' and '\' to '\\' for use by
     * CRFSuite.
     */
    StringBuilder sbOut = new StringBuilder(2 * inputString.length());
    for (int i = 0; i < inputString.length(); i++) {
      switch (inputString.charAt(i)) {
        case ':':
          sbOut.append('\\').append(':');
          break;
        case '\\':
          sbOut.append('\\').append('\\');
          break;
        default:
          sbOut.append(inputString.charAt(i));
      }
    }

    return sbOut.toString();
  }


  /**
   * @param includedFeaturesFilePath
   */
  protected void initializeIncludedFeatures(String includedFeaturesFilePath,
      HashSet<String> includedCueFeaturesSet) {
    try {
      if (includedFeaturesFilePath != null) {
        BufferedReader reader = new BufferedReader(new FileReader(includedFeaturesFilePath));
        String curLine;
        while ((curLine = reader.readLine()) != null) {
          curLine = curLine.trim();
          if (curLine.length() > 0) {
            includedCueFeaturesSet.add(curLine);
          }
        }
        reader.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
