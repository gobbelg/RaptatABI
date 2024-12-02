/**
 *
 */
package src.main.resources.config.ggpatternconfig.grouped

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/**
 * @author VHATVHGOBBEG
 *
 */
class ExerciseFSM extends FSMDefinitionBase {

  String EXERCISE;

  @Override
  public String get_fsm_name() {
    return "Exercise";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    EXERCISE = "Exercise";
  }

  @Override
  protected void set_match_map() {
    map[EXERCISE] = ['excercise', 'exc\\.?'];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern exercise = new StateAndPattern(EXERCISE, EXERCISE);

    return generate_categorical_detection_fsm(exercise);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
