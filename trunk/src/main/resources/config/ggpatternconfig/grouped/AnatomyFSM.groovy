package src.main.resources.config.ggpatternconfig.grouped
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/*
 * Configuration file to get various elements related to measurement of ABI and TBI in tests for peripheral artery disease
 *
 */
class AnatomyFSM extends FSMDefinitionBase {

  @Override
  public String get_fsm_name() {
    return "Anatomy";
  }

  String ANKLE;
  String TIBIA;
  String ARM;
  String THIGH;
  String TOE;
  String OTHER;

  @Override
  protected void initialize_state_and_pattern_names() {
    this.ANKLE = "Ankle";
    this.TIBIA = "Tibia";
    this.ARM = "Arm";
    this.THIGH = "Thigh";
    this.TOE = "Toe";
    this.OTHER= "Other";
  }

  @Override
  protected void set_match_map() {
    map[ANKLE] = ['ankle'];
    map[ARM] = ['arm'];
    map[THIGH] = [
      '((lower|upper|distal|proximal)[ \t]*)?(thigh|calf)'
    ];
    map[TOE] = [
      'toe',
      'digit(al)?',
      'gt',
      'greater toe'
    ];
    map[OTHER] = [
      '((lower|distal)[ \t]*)?extremit(y|ies)',
      'knee'
    ];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern ankle = new StateAndPattern(ANKLE, ANKLE);
    StateAndPattern tibia = new StateAndPattern(TIBIA, TIBIA);
    StateAndPattern arm = new StateAndPattern(ARM, ARM);
    StateAndPattern thigh = new StateAndPattern(THIGH, THIGH);
    StateAndPattern toe = new StateAndPattern(TOE, TOE);
    StateAndPattern other = new StateAndPattern(OTHER, OTHER);

    return generate_categorical_detection_fsm(ankle, tibia, arm, thigh, toe,other);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
