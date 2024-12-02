package src.main.gov.va.vha09.grecc.raptat.gg.weka.core;

import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/**
 * DocumentClassifier
 *
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Jan 13, 2019
 */
public class DocumentClassifier {

  private enum RunType {
    CLASSIFY_DOCUMENTS;
  }


  protected void classifyDocuments() {}


  public static void main(String[] args) {

    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        DocumentClassifier classifier = new DocumentClassifier();
        RunType runType = RunType.CLASSIFY_DOCUMENTS;

        switch (runType) {
          case CLASSIFY_DOCUMENTS:
            classifier.classifyDocuments();
            break;
          default:
            break;
        }
        System.exit(0);
      }
    });
  }
}
