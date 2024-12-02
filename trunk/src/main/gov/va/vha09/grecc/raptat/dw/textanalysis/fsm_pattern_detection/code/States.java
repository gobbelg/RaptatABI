package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.util.Set;

final public class States extends FSMElement<State> {

  public States(State... states) {
    super(states);
  }

  public State get_state(String start_state_str) {
    Set<State> states = get_elements();
    State rv = null;
    for (State state : states) {
      if (state.get_state() == start_state_str) {
        rv = state;
        break;
      }
    }
    return rv;
  }

}
