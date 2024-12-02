package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis;

import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/**
 * TextStringObject
 *
 * <p>
 * Created as a way of storing the part of speech found by a regular expression applied to a string.
 * This supplements the methods provided by the part-of-speech tagger to identify types of numbers,
 * dates, and units of measure that might be used clinically.
 *
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Apr 21, 2019
 */
public class TokenStringObject {

  public enum RaptatStringType {
    UNASSIGNED, INTEGER, DECIMAL, SCIENTIFIC, WORD, DATE, UNIT;

    public String getAssignedPartOfSpeech() {
      return name().toLowerCase();
    }
  }

  private enum RunType {
    TEST_RUN;
  }


  public final String tokenString;

  public final RaptatStringType stringType;
  public final int startOffset;

  /**
   * @param textString
   * @param stringType
   */
  public TokenStringObject(String tokenString, RaptatStringType stringType, int startOffset) {
    super();
    this.tokenString = tokenString;
    this.stringType = stringType;
    this.startOffset = startOffset;
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
