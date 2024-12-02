package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/**
 * This is a finite state machine class. Its basic function is to take an input, which is a FS
 * instance, and update itself. As part of the update process, it may emit an integer emitted by the
 * transition from the initial state to the new state.
 *
 * @author VHATVHGOBBEG
 */
public class FSM {
  public static final int STATE_INDEX = 0;

  public static final int EMSSION_INDEX = 1;


  /**
   * This determines the next state and emission given an input into the FSM instance. Given a
   * current state and input, the next state is given by
   * updateEmissionTable[currentState][inputValue][STATE_INDEX] and the emission value by
   * updateEmissionTable[currentState][inputValue][EMSSION_INDEX].
   */
  private int[][][] updateEmissionTable;

  /**
   * This is the current state of the FSM instance. By default, 0 is the start state. The state can
   * be changed via input or by a call to setState;
   */
  private int currentState = 0;

  public FSM(int[][][] updateEmissionTable) {
    if (!isValidTable(updateEmissionTable)) {
      throw new IllegalArgumentException(
          "Invalid update emission table for constructing FSM instance - states provided in table exceed table array size");
    }
    this.updateEmissionTable = updateEmissionTable;
  }


  public void setState(int state) {
    this.currentState = state;
  }


  public int update(int input) {
    int[] stateAndEmission = this.updateEmissionTable[this.currentState][input];
    this.currentState = stateAndEmission[FSM.STATE_INDEX];
    return stateAndEmission[FSM.EMSSION_INDEX];
  }


  private boolean isValidTable(int[][][] updateEmissionTable) {
    for (int[][] currentStateIndex : updateEmissionTable) {
      for (int[] newStateIndex : currentStateIndex) {
        if (newStateIndex[0] >= updateEmissionTable.length) {
          return false;
        }
      }
    }
    return true;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        int[][][] testFSM = {
            /* State 0 transitions */
            {
                /* New state and emission if input is 0 */
                {3, 4},
                /* New state and emission if input is 1 */
                {5, 6}},
            /* State 1 transitions */
            {
                /* New state and emission if input is 0 */
                {1, 2},
                /* New state and emission if input is 0 */
                {2, 1}},
            /* State 2 transitions */
            {{}, {}},
            /* State 3 transitions */
            {{}, {}}};
        int alpha = 1;
      }
    });
  }
}
