package src.main.resources.config.pattern_config.grouped

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

class PressureFSM extends FSMDefinitionBase {

  String PRESSURE;

  /** The PRESSURE pattern. */
  String PRESSURE_PATTERN;

  /**
   * Gets the fsm name.
   *
   * @return the fsm name
   */
  @Override
  public String get_fsm_name() {
    return PRESSURE;
  }

  /**
   * Initialize state and pattern names.
   */
  @Override
  protected void initialize_state_and_pattern_names() {
    PRESSURE = "Pressure";
    PRESSURE_PATTERN = "Pressure_Pattern"
  }

  /**
   * Sets the match map.
   */
  @Override
  protected void set_match_map() {
    map[PRESSURE_PATTERN] = ["pressure(?<=:?)"];
  }

  /**
   * Gets the fsm definition table.
   *
   * @return the fsm definition table
   */
  @Override
  public String[][] get_fsm_definition_table() {
    StateAndPattern defn = new StateAndPattern(PRESSURE, PRESSURE_PATTERN);
    return generate_categorical_detection_fsm(defn );
  }

  /**
   * Should reset labeling on next not found.
   *
   * @return the boolean
   */
  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}

