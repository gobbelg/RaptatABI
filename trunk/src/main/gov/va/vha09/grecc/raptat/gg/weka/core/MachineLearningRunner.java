package src.main.gov.va.vha09.grecc.raptat.gg.weka.core;

import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/**
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Jan 12, 2019
 */
public class MachineLearningRunner {

  private enum RunType {
    TEST_RUN;
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
