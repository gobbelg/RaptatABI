import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/*
 * How do state and pattern names relate to fsm_name?
 * Why are they put together into one file?
 * Is there a reason it's not called AnatomyFSM.groovy?
 * 
 */
class Anatomy extends FSMDefinitionBase {

	@Override
	public String get_fsm_name() {
		return "Anatomy";
	}

	String ANKLE;
	String TIBIAL;
	String ARM;
	String POSTERIOR_TIBIAL;
	String DORSAL_PEDAL;
	String INDEX;
	String THIGH;
	String DIGIT;
	String BRACHIAL;
	String TOE;

	@Override
	protected void initialize_state_and_pattern_names() {
		this.ANKLE = "ANKLE";
		this.TIBIAL = "TIBIAL";
		this.ARM = "ARM";
		this.POSTERIOR_TIBIAL = "POSTERIOR_TIBIAL";
		this.DORSAL_PEDAL = "DORSAL_PEDAL";
		this.INDEX = "INDEX";
		this.THIGH = "THIGH";
		this.DIGIT = "DIGIT"
		this.BRACHIAL = "BRACHIAL";
		this.TOE = "TOE";
	}

	@Override
	protected void set_match_map() {

		/*
		 * Remove these as we'll only use them if needed - we'll capture separate "pressure reference"
		 */
		//		String ignoreSystolicAndDiastolic = "(?<!(systolic|diastolic)\\s+)"
		//		String ignorePressure = "(?!\\s+(pressure|BP))" // pressure and blood pressure

		map[ANKLE] = ['ankle(?=:)?'];

		/*
		 * Why is 'tibial' commented out?
		 */
		//map[TIBIAL] = ['tibial'];
		map[ARM] = ['arm(?=:)?'];
		map[POSTERIOR_TIBIAL] = [
			'(\\(pt\\)|(?<!\\()pt(?!\\)))(?=:)?',
			'posterior\\s*tibial\\s*(?:artery)?(?=:)?'
			//			ignoreSystolicAndDiastolic + '(\\(pt\\)|(?<!\\()pt(?!\\)))(?=:)?', // capture (pt) or pt w/ no parens (negative lookbehind was picking up parens)
			//			ignoreSystolicAndDiastolic + 'posterior\\s*tibial\\s*(?:artery)?(?=:)?' + ignorePressure
		];
		map[DORSAL_PEDAL] = [
			'dp',
			'(\\(pt\\)|(?<!\\()pt(?!\\)))(?=:)?',
			'posterior\\s*tibial\\s*(?:artery)?(?=:)?'
			//			ignoreSystolicAndDiastolic + 'dorsal\\s*pedal(?=:)?',
			//			ignoreSystolicAndDiastolic + 'dorsal\\s*tibial(?=:)?',
			//			ignoreSystolicAndDiastolic + 'dorsalis\\s*pedis\\s*(?:artery)?(?=:)?' + ignorePressure,
			//			ignoreSystolicAndDiastolic + 'dors\\s*pedis(?=:)?'
		];
		map[INDEX] = ['index(?=:)?'];
		map[THIGH] = ['thigh(?=:)?'];
		map[DIGIT] = ['digit(?=:)?'];
		map[BRACHIAL] = ['brachial(?=:)?'];
		map[TOE] = ['toe(?=:)?']
	}

	@Override
	public String[][] get_fsm_definition_table() {

		StateAndPattern ankle = new StateAndPattern(ANKLE, ANKLE);
		StateAndPattern tibial = new StateAndPattern(TIBIAL, TIBIAL);
		StateAndPattern arm = new StateAndPattern(ARM, ARM);
		StateAndPattern PosteriorTibial = new StateAndPattern(POSTERIOR_TIBIAL, POSTERIOR_TIBIAL);
		StateAndPattern DorsalPedal = new StateAndPattern(DORSAL_PEDAL, DORSAL_PEDAL);
		StateAndPattern Index = new StateAndPattern(INDEX, INDEX);
		StateAndPattern Thigh = new StateAndPattern(THIGH, THIGH);
		StateAndPattern digit = new StateAndPattern(DIGIT, DIGIT);
		StateAndPattern brachial = new StateAndPattern(BRACHIAL, BRACHIAL);
		StateAndPattern toe = new StateAndPattern(TOE, TOE);

		return generate_categorical_detection_fsm(ankle, tibial, arm, PosteriorTibial, DorsalPedal, Index, Thigh, digit, brachial, toe);
	}

	@Override
	public Boolean should_reset_labeling_on_next_not_found() {
		return true;
	}
}
