/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.File;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ************************************************
 *
 * @author vhatvhgobbeg - Jul 5, 2013 ************************************************
 */
public class ADAHF_Criteria {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String[] extensions = {".xml"};
    File[] filesToRead =
        GeneralHelper.getFilesInDirectory("Select directory with xml files", extensions);

    for (File curFile : filesToRead) {
    }
  }
}
