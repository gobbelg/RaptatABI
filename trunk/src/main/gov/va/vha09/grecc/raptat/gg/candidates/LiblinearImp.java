/** */
package src.main.gov.va.vha09.grecc.raptat.gg.candidates;

import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/** @author Glenn Gobbel */
public class LiblinearImp {

  /** */
  public LiblinearImp() {
    // TODO Auto-generated constructor stub
  }


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {}
    });
  }
}
