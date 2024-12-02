package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

/**
 * A class which encapsulates the text name for a state as defined via configuration.
 */
final public class State {

  /** The start. */
  final private String __state;


  /**
   * Instantiates a new state.
   *
   * @param state the start
   */
  public State(String state) {
    this.__state = state;
  }


  public String get_state() {
    return this.__state;
  }


  @Override
  public String toString() {
    return get_state();
  }

}
