package src.main.resources.config.pattern_config.grouped
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/*
 * Why are there states named ABI and ABI_PATTERN?
 * Same question for TBI
 */
class IndexTypeFSM extends FSMDefinitionBase {

  String ABI;
  String TBI;
  String ABI_PATTERN;
  String TBI_PATTERN;

  @Override
  public String get_fsm_name() {
    return "IndexType";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    ABI = "ABI";
    TBI = "TBI";
    ABI_PATTERN = "ABI";
    TBI_PATTERN = "TBI";
  }

  @Override
  protected void set_match_map() {
    map[ABI_PATTERN] = [
      'abi\'?s?(?<=:?)',
      '(ankle\\s*)?brachial\\s*ind(ex|ices)',
      'ankle\\s*[/-]\\s*brachial',
      'abi\\s*[/-]\\s*rest',
      'abix',
      'brachial.?index',
      'ankle',
      'abi\\s*value',
      'abi\\s*\\(numeric value\\)'
    ];
    map[TBI_PATTERN] = [
      'tbi\'?s?(?<=:?)',
      'toe\\s*brachial\\s*ind(ex|ices).?',
      /*
       * What is this capturing '1-2 toes'?
       */
      '\\d-\\d\\s*toes',
      'toe\\s*ind(ex|ices)',
      'toe\\s*pressure.?',
      'toe\\s*[/-]\\s*brachial\\s*ind(ex|ices).?',
      /*
       * Modified regex below to remove last optional '.' character.  With the '.' the regex fails to be validated.
       */
      //'toe-brachial\\s*ind(ex|ices).?',
      'toe-brachial\\s*ind(ex|ices)',
      'toe\\s*press(ure)?(?<=:?)',
      'hallux\\s*[/-]\\s*brachial'
    ];

  }

  @Override
  public String[][] get_fsm_definition_table() {
    //		return generate_two_state_fsm(TBI, TBI_PATTERN, ABI, ABI_PATTERN);

    StateAndPattern abi = new StateAndPattern(ABI, ABI_PATTERN);
    StateAndPattern tbi = new StateAndPattern(TBI, TBI_PATTERN);

    return generate_categorical_detection_fsm(abi, tbi);

  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}

//map[ABI_PATTERN] = [
//	'[Aa][Bb][Ii]',
//	'\bABI.?',
//	'ABI',
//	'abis?:',
//	'abi\'s',
//	'abix',
//	'Ankle.?Brachial.?Index.?',
//	'ankle.?brachial.?indices.?',
//	'Ankle\\s*Brachial\\s*Index',
//	'ankle\\s*brachial\\s*indices',
//	'Brachial.?Index',
//	'ANKLE',
//	'ankle/brachial',
//	'Ankle\\s*Brachial\\s*Index\\s*\\(ABI\\)',
//	'\\(ABI\\)',
//	'abi/rest',
//	'ABI\\s*\\(numeric value\\)',
//	'ABI\\s*value'
//];
//map[TBI_PATTERN] = [
//	'[Tt][Bb][Ii]',
//	'\\d-\\d\\s*toes',
//	'toe\\s*ind(ex|ices)',
//	'toe\\s*pressure.?',
//	'toe/brachial\\s*ind(ex|ices).?',
//	'toe\\s*/\\s*brachial\\s*ind(ex|ices).?',
//	'toe-brachial\\s*ind(ex|ices).?',
//	'\\(toe(-|\\s)brachial\\s*ind(ex|ices)\\).?',
//	'toe\\s*brachial\\s*ind(ex|ices).?',
//	'TBI.?',
//	'hallux/brachial',
//	'toe\\s*press(ure)?',
//	'toe\\s*press:',
//	'toe\\s*press:\\.{1,8}'
//];