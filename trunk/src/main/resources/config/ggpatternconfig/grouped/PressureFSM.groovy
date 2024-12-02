package src.main.resources.config.ggpatternconfig.grouped

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

class PressureFSM extends FSMDefinitionBase {

  String PRESSURE_ANKLE;
  String PRESSURE_ARM;
  String PRESSURE_TOE;
  String PRESSURE_UNSPECIFIED;

  /**
   * Gets the fsm name.
   *
   * @return the fsm name
   */
  @Override
  public String get_fsm_name() {
    return "Pressure";
  }

  /**
   * Initialize state and pattern names.
   */
  @Override
  protected void initialize_state_and_pattern_names() {
    PRESSURE_ANKLE = "PressureAnkle";
    PRESSURE_ARM = "PressureArm";
    PRESSURE_TOE = "PressureToe";
    PRESSURE_UNSPECIFIED = "PressureUnspecified";
  }

  /**
   * Sets the match map.
   */
  @Override
  protected void set_match_map() {
    String bpString = '(b\\/?p|(blood[ \t]*press(\\.|ure)?))';
    map[PRESSURE_ANKLE] = [
      'ankle[ \t]+'+ bpString,
      'dorsalis[ \t]*pedis[ \t]*(artery[ \t]*(systolic)?)?'+ bpString,
      'posterior[ \t]*tibial[ \t]*(artery[ \t]*(systolic)?)?'+ bpString
    ];
    map[PRESSURE_ARM] = ['brachial[ \t]*(arm|artery)?[ \t]*(systolic)[ \t]*'+ bpString];
    map[PRESSURE_TOE] = ['g\\.?t\\.?p\\.?', '(toe|digit(al)?)[ \t]*pressures?'];
    map[PRESSURE_UNSPECIFIED] = ['pressure'];
  }

  /**
   * Gets the fsm definition table.
   *
   * @return the fsm definition table
   */
  @Override
  public String[][] get_fsm_definition_table() {
    StateAndPattern pressureAnkle = new StateAndPattern(PRESSURE_ANKLE, PRESSURE_ANKLE);
    StateAndPattern pressureArm = new StateAndPattern(PRESSURE_ARM, PRESSURE_ARM);
    StateAndPattern pressureToe = new StateAndPattern(PRESSURE_TOE, PRESSURE_TOE);
    StateAndPattern pressureUnspecified = new StateAndPattern(PRESSURE_UNSPECIFIED, PRESSURE_UNSPECIFIED);

    return generate_categorical_detection_fsm(pressureAnkle, pressureArm, pressureToe, pressureUnspecified );
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

