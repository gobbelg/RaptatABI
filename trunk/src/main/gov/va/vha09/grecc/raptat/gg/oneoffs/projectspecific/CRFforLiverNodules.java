/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/** @author Glenn T. Gobbel Apr 8, 2016 */
public class CRFforLiverNodules {

  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // PhraseIDCrossValidationSampler theSampler = new
        // PhraseIDCrossValidationSampler();
        // theSampler.getSamplingData();
        // List<AnnotationGroup> annotationGroups =
        // theSampler.getAllAnnotationGroups();
        // System.out.println();
      }
    });
  }
}
