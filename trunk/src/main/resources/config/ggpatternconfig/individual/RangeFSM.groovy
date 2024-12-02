package src.main.resources.config.ggpatternconfig.individual
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

class IndexRangeFSM extends FSMDefinitionBase {

  String INDEX_RANGE;
  String INDEX_RANGE_PATTERN;

  @Override
  public String get_fsm_name() {
    return "IndexRange";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    INDEX_RANGE = "IndexRange";
    INDEX_RANGE_PATTERN = "Index_Range_Pattern";
  }

  @Override
  protected void set_match_map() {
    map[INDEX_RANGE_PATTERN] = [
      "(\\b(?:[012])(?:\\.\\d{0,2})?\\s*(?:\\-|to)\\s*(?:[012])(?:\\.\\d{0,2})?|(?:[012])(?:\\.\\d{0,2}\\s*(?:mm|cm|min)))"
    ];
  }

  @Override
  public String[][] get_fsm_definition_table() {
    StateAndPattern index_defn = new StateAndPattern(INDEX_RANGE, INDEX_RANGE_PATTERN);
    return generate_categorical_detection_fsm(index_defn);
  }
}
