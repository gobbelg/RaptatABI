import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

class IndexValueFSM extends FSMDefinitionBase {

	String INDEX;
	String INDEX_PATTERN;

	@Override
	public String get_fsm_name() {
		return "IsIndex";
	}

	@Override
	protected void initialize_state_and_pattern_names() {
		INDEX = "INDEX";
		INDEX_PATTERN = "INDEX";
	}

	@Override
	protected void set_match_map() {
		/**
		 * \044 is octal for $, which is a reserved character in a groovy string
		 */
		map[INDEX_PATTERN] = [
			"(?:(?:[\\(\\[]?(?:\\d+/)?[ \\t]*?(?<index>[<>]?[012]?\\.\\d{1,2})[ \\t]*?[\\)\\]]?))"
		];
	}

	@Override
	public String[][] get_fsm_definition_table() {

		StateAndPattern index_defn = new StateAndPattern(INDEX, INDEX_PATTERN);

		return generate_categorical_detection_fsm(index_defn);

		//		return [
		//			[START, NO_MATCH, START],
		//			[START, INDEX_PATTERN, INDEX, PERFORM_EMISSION_ACTION],
		//			[INDEX, NO_MATCH, START],
		//			[INDEX, INDEX_PATTERN, INDEX, PERFORM_EMISSION_ACTION]
		//		];
	}

	@Override
	public Boolean should_reset_labeling_on_next_not_found() {
		return true;
	}
}


//,
//"(0|00|01|1)\\.\\d{0,2}-(0|00|01|1)\\.\\d{0,2}",
//"\\.\\d{1,2}",
//"#\\bone",
//"\\b1\\b\\.?",
//"\\d\\d\\d?\\/\\d\\d\\d?",
//"zero",
//"unobtainable",
//"not\\s*detectable",
//"unable\\s*to\\s*get",
//">1",
//"<1"
