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
class TemporalReferenceFSM extends FSMDefinitionBase {

  String DATE;
  String HISTORICAL;

  @Override
  public String get_fsm_name() {
    return "TemporalReference";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    DATE = "Date";
    HISTORICAL = "Historical";
  }

  @Override
  protected void set_match_map() {
    String monthStringNames ='(jan(\\.|uary)?|feb(\\.|ruary)?|mar(\\.|ch)?|apr(\\.|il))?|may|jun(\\.|e)|jul(\\.|y)|aug(\\.|ust)|sep(\\.|tember)|oct(\\.|ober)|nov(\\.|ember)|dec(\\.|ember)|';
    String monthString = '(0?[1-9]|1[012])';
    String dayString = '(0?[1-9]|[12][0-9]|3[01])';
    String yearString = '(19|20)?[0-9]{2}';
    map[DATE] = [
      monthString + '[- /.]' + dayString + '[- /.]' + yearString,
      monthStringNames + '[ \t]+' + dayString + '[ \t]*' + '[,]' + '[ \t]*' + yearString
    ];
    map[HISTORICAL] = ['earlier', 'previous(ly)?', 'prior', 'today'];
  }

  @Override
  public String[][] get_fsm_definition_table() {
    StateAndPattern date = new StateAndPattern(DATE, DATE);
    StateAndPattern historical = new StateAndPattern(HISTORICAL, HISTORICAL);

    return generate_categorical_detection_fsm(historical, date);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
