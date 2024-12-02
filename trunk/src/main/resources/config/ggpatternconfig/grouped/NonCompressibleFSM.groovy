package src.main.resources.config.ggpatternconfig.grouped
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern


/**
 * The Class CalcifiedFSM.
 *
 * @author westerd
 */
class NonCompressibleFSM extends FSMDefinitionBase {

  String NON_COMPPRESSIBLE;

  /**
   * Gets the fsm name.
   *
   * @return the fsm name
   */
  @Override
  public String get_fsm_name() {
    return NON_COMPPRESSIBLE;
  }

  /**
   * Initialize state and pattern names.
   */
  @Override
  protected void initialize_state_and_pattern_names() {
    NON_COMPPRESSIBLE = "NonCompressible";
  }

  /**
   * Sets the match map.
   */
  @Override
  protected void set_match_map() {
    map[NON_COMPPRESSIBLE] = [
      'calcifi(ed|cations?)',
      '(un|no(n|t)|im)[ \t]*\\-?[ \t]*compressible',
      'calcified'
    ];
  }

  /**
   * Gets the fsm definition table.
   *
   * @return the fsm definition table
   */
  @Override
  public String[][] get_fsm_definition_table() {
    StateAndPattern nonCompressible = new StateAndPattern(NON_COMPPRESSIBLE, NON_COMPPRESSIBLE);
    return generate_categorical_detection_fsm(nonCompressible );
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
