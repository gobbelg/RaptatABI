package src.main.resources.config.ggpatternconfig.grouped
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/*
 * Why are there states named ABI and ABI_PATTERN?
 * Same question for TBI
 */
class IndexTypeFSM extends FSMDefinitionBase {

  String ABI;
  String TBI;

  @Override
  public String get_fsm_name() {
    return "IndexType";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    ABI = "ABI";
    TBI = "TBI";
  }

  @Override
  protected void set_match_map() {
    map[ABI] = [
      'abi\'?s?(?<=:?)',
      '(ankle[ \t]*)?brachial[ \t]*ind(ex|ices)',
      'ankle[ \t]*[/-][ \t]*brachial',
      'abi[ \t]*[/-][ \t]*rest',
      'abix',
      'brachial.?index',
      'ankle',
      'abi[ \t]*value',
      'abi[ \t]*\\(numeric value\\)',
      'a[ \t]*\\/?[ \t]*a[ \t]*(ratio|i)?',
      'ankle[ \t]*[\\/\\-]?[ \t]*arm[ \t]*(press\\.?(ure)?|ind(ex|ices))',
    ];
    map[TBI] = [
      'tbi\'?s?(?<=:?)',
      'toe[ \t]*brachial[ \t]*ind(ex|ices).?',
      '\\d-\\d[ \t]*toes',
      'toe[ \t]*ind(ex|ices)',
      'toe[ \t]*pressure.?',
      'toe[ \t]*[/-][ \t]*brachial[ \t]*ind(ex|ices).?',
      'toe-brachial[ \t]*ind(ex|ices)',
      'toe[ \t]*press\\.?(ure)?',
      'hallux[ \t]*[/-][ \t]*brachial',
      'd\\.?b\\.?i\\.?',
      '(toe|digit(al)?)[ \t]*[\\/\\-]?[ \t]*(arm|brachial)[ \t]*(press\\.?(ure)?|ind(ex|ices))',
      '(toe|digit(al)?)[ \t]*abis?',
    ];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern abi = new StateAndPattern(ABI, ABI);
    StateAndPattern tbi = new StateAndPattern(TBI, TBI);

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