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
class ImpressionFSM extends FSMDefinitionBase {

  String IMPRESSION;

  @Override
  public String get_fsm_name() {
    return "Impression";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    IMPRESSION = "Impression";
  }

  @Override
  protected void set_match_map() {
    map[IMPRESSION] = ['impress\\.?(ion)?'];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern impression = new StateAndPattern(IMPRESSION, IMPRESSION);

    return generate_categorical_detection_fsm(impression);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
