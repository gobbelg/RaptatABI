/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.parsers.wrapping;

import src.main.gov.va.vha09.grecc.raptat.gg.helpers.FSInput;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.FSM;

/**
 * Tracks the state of line wrapping in a text document
 *
 * @author VHATVHGOBBEG
 *
 */
public class LineWrapParser {

  public enum Emission {
    HANDLE_NO_WRAPPING_MATCH, HANDLE_START_WRAPPING_MATCH, HANDLE_CONTINUE_WRAPPING_MATCH,;
  }

  public enum State {
    NOT_WRAPPING, STARTED_WRAPPING
  }

  /*
   * These are the potential results of evaluating a line of text. There are three potential inputs
   * - depending on the given line of text: a line can indicate that it should not be wrapped to the
   * next line, that wrapping to the next line should be considered (started), or that wrapping has
   * already taken place and should be ocntinued.
   */
  public enum WrappingInput implements FSInput {
    STOP_WRAPPING, START_WRAPPING, CONTINUE_WRAPPING;
  }

  private FSM wrappingParserFSM;
  {
    int[][][] wrappingFsmSettings = getWrappingParserFsmSettings();
    this.wrappingParserFSM = new FSM(wrappingFsmSettings);
    this.wrappingParserFSM.setState(State.NOT_WRAPPING.ordinal());
  }

  public void reset() {
    this.wrappingParserFSM.setState(State.NOT_WRAPPING.ordinal());
  }

  public int update(int inputState) {
    return this.wrappingParserFSM.update(inputState);
  }

  private static int[][][] getWrappingParserFsmSettings() {
    int[][][] settings = new int[][][] {
        /*
         * Each of the below lists indicates the new state given a "WrappingInput. The second
         * element in every pair it the "procedure" to carry out in response to the state and given
         * input.
         */
        //@formatter:off
        /* NOT_WRAPPING STATE Transitions and Emissions */
        {{State.NOT_WRAPPING.ordinal(), Emission.HANDLE_NO_WRAPPING_MATCH.ordinal()},
        {State.STARTED_WRAPPING.ordinal(), Emission.HANDLE_START_WRAPPING_MATCH.ordinal()}},
        /* STARTED_WRAPPING STATE Transitions and Emissions */
        {{State.NOT_WRAPPING.ordinal(), Emission.HANDLE_NO_WRAPPING_MATCH.ordinal()},
        {State.STARTED_WRAPPING.ordinal(),  Emission.HANDLE_START_WRAPPING_MATCH.ordinal()},
        {State.STARTED_WRAPPING.ordinal(), Emission.HANDLE_CONTINUE_WRAPPING_MATCH.ordinal()}}
         //@formatter:on

    };
    return settings;
  }

}
