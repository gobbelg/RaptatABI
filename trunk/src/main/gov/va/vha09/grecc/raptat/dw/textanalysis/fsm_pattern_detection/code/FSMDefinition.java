package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.util.List;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchDetails;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchPatternValidate;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.FSM;

/**
 * This class extends the functionality of the
 * src.main.gov.va.vha09.grecc.raptat.gg.helpers.FSM.java by providing the means to build out the
 * underlying int[][][] map from and FSM name, an enumeration of states, a collection of named
 * matching patterns (input to FSM), and a series of named match actions (FSM emissions). Leverages
 * functionality from the underlying FSM.java class to manage state transitions.
 */
final public class FSMDefinition {

  private static Logger LOGGER = Logger.getLogger(FSMDefinition.class.getName());

  /** The name. */
  private String __name;

  /** The states. */
  private States __states;

  /** The match patterns. */
  private MatchPatterns __match_patterns;

  /** The match actions. */
  private MatchActions __match_actions;

  /** The state machine. */
  private int[][][] __state_machine;

  /** The last label value. */
  private String __last_label_value = "None";

  /** The Constant NoMatchAction. */
  final private IMatchAction NoMatchAction =
      (RaptatToken t, String feature_type, String feature_value) -> {
        ((RaptatTokenFSM) t).add_feature(feature_type, this.__last_label_value);
      };

  /** The start state. */
  final private State __start_state;

  /** The fsm. */
  FSM __fsm;

  Boolean __clear_after_not_found = false;

  private IRaptatTokenText __raptat_token_text_callback;


  /**
   * Instantiates a new FSM definition.
   *
   * @param name the name
   * @param states the states
   * @param match_patterns the match patterns
   * @param match_actions the match actions
   * @param start_state the start state
   */
  public FSMDefinition(String name, States states, MatchPatterns match_patterns,
      MatchActions match_actions, State start_state) {
    this(name, states, match_patterns, match_actions, start_state,
        x -> x.getTokenStringUnprocessed());

  }


  public FSMDefinition(String name, States states, MatchPatterns match_patterns,
      MatchActions match_actions, State start_state, IRaptatTokenText raptat_token_text_callback) {
    this(name, start_state);
    this.__states = states;
    this.__match_actions = match_actions;
    this.__match_actions.add(this.NoMatchAction);

    this.__match_patterns = match_patterns;
    this.__match_patterns.add(MatchPattern.None); // Since this is a pattern
    // matching FSM, add in None
    // as a default, since there
    // are finite states
    this.__state_machine = new int[states.count()][match_patterns.count()][2];

    this.__raptat_token_text_callback = raptat_token_text_callback;

  }


  /**
   * Instantiates a new FSM definition.
   *
   * @param name the name
   * @param start_state the start state
   */
  private FSMDefinition(String name, State start_state) {
    this.__start_state = start_state;
    this.__name = name;
  }


  public void clear_if_not_found() {
    this.__clear_after_not_found = true;
  }


  /**
   * Define.
   *
   * @param current_state the current state
   * @param match_pattern the match pattern
   * @param new_state the new state
   * @throws FSMElementException the FSM element exception
   */
  public void define(State current_state, MatchPattern match_pattern, State new_state)
      throws FSMElementException {
    this.define(current_state, match_pattern, new_state, this.NoMatchAction);
  }


  /**
   * Define.
   *
   * @param current_state the current state
   * @param match_pattern the match pattern
   * @param new_state the new state
   * @param action the action
   * @throws FSMElementException the FSM element exception
   */
  public void define(State current_state, MatchPattern match_pattern, State new_state,
      IMatchAction action) throws FSMElementException {
    int current_state_position = this.__states.get_index(current_state);
    int new_state_position = this.__states.get_index(new_state);
    int input = this.__match_patterns.get_index(match_pattern);
    int emission_index = this.__match_actions.get_index(action);

    this.__state_machine[current_state_position][input][0] = new_state_position;
    this.__state_machine[current_state_position][input][1] = emission_index;
  }


  /**
   * Gets the encoded fsm.
   *
   * @return the encoded fsm
   */
  public int[][][] get_encoded_fsm() {
    return this.__state_machine;
  }


  public MatchPatterns get_match_patterns() {
    return this.__match_patterns;
  }


  public List<MatchPattern> get_match_patterns(String... pattern_names) {
    List<MatchPattern> match_patterns = this.__match_patterns.get_matches(pattern_names);
    return match_patterns;
  }


  /**
   * Gets the name.
   *
   * @return the name
   */
  public String get_name() {
    return this.__name;
  }


  /**
   * Label.
   *
   * @param token the token
   * @param match_type
   * @throws FSMElementException the FSM element exception
   */
  public void label(RaptatToken token, MatchType match_type,
      MatchPatternValidate validationCallback) throws FSMElementException {

    if (this.__fsm == null) {
      this.__fsm = new FSM(this.__state_machine);
      this.__fsm.setState(this.__states.get_index(this.__start_state));
    }

    MatchPattern found_pattern = MatchPattern.None;

    for (MatchPattern matchPattern : this.__match_patterns.get_elements()) {
      MatchDetails<RaptatToken> matches =
          matchPattern.matches(token, this.__raptat_token_text_callback, match_type);

      if (matches != null) {
        if (validationCallback != null && validationCallback.validate(matches) == false) {
          matches = null;
        } else {
          found_pattern = matchPattern;
          break;
        }
      }
    }

    // List<List<Integer>> currentStateLookup = __fsm.getCurrentStateLookup();

    int matchPatternIndex = this.__match_patterns.get_index(found_pattern);

    int action = this.__fsm.update(matchPatternIndex);

    // LOGGER.info( "\r\n" + Joiner.on( "\r\n" ).join( new String[]
    // {
    // "token: " + token, "found_pattern: " + found_pattern
    // } ) );

    IMatchAction match_action = this.__match_actions.get(action);

    String label_type = this.__name;
    String label_value;
    if (found_pattern != MatchPattern.None) {
      label_value = found_pattern.get_match_pattern_name();
    } else {
      String none_name = MatchPattern.None.get_match_pattern_name();
      label_value = this.__clear_after_not_found == false ? this.__last_label_value : none_name;
    }
    this.__last_label_value = label_value;
    match_action.action(token, label_type, label_value);

  }


  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {

    return get_name();
  }

}
