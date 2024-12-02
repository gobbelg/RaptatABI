import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase

/*
 * What does this do?
 */
class IndexValueAccumulatorFSM extends FSMDefinitionBase {

	static final INDEX = "Index";
	static final LAST_DIGITS = "LastDigits";
	static final PERIOD = "Period";
	static final INITIAL_DIGIT = "InitialDigit";

	@Override
	public String get_fsm_name() {
		return "IndexValueAccumulator";
	}

	@Override
	protected void initialize_state_and_pattern_names() {
	}


	@Override
	protected void set_match_map() {

		map[INITIAL_DIGIT] = ["(0|00|01|1)"];

		map[LAST_DIGITS] = ["\\d{0,2}"];

		map[PERIOD] = ["\\."];
	}

	@Override
	public String[][] get_fsm_definition_table() {

		return [
			[START, INITIAL_DIGIT, INITIAL_DIGIT, PERFORM_ACCUMULATION_ACTION],
			[INITIAL_DIGIT, PERIOD, PERIOD, PERFORM_ACCUMULATION_ACTION],
			[PERIOD, LAST_DIGITS, START, PERFORM_EMISSION_ACTION]
		];
	}
}
