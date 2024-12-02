/**
 *
 */
package src.main.resources.config.ggpatternconfig.grouped

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/**
 * Captures information that generally indicates something similar to or in the same
 * context as index values but that actually may indicate that nearby labels are not index values
 *
 * @author VHATVHGOBBEG
 *
 */
class NegativeIndexInformation extends FSMDefinitionBase {

  String NON_INDEX_ARTERY;
  String OTHER_INDEX_TYPE;
  String INDEX_VALUE_INTERPRETATION;
  String LIST_INDEX;
  String FLOW;
  String MISC;
  String UNITS;


  @Override
  public String get_fsm_name() {
    return "NegativeIndexInformation";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    NON_INDEX_ARTERY = "NonIndexArtery";
    OTHER_INDEX_TYPE = "OtherIndexType";
    INDEX_VALUE_INTERPRETATION = "IndexValueInterpretation";
    LIST_INDEX = "ListIndex";
    FLOW = "Flow";
    MISC = "Miscellaneous";
    UNITS = "Units";
  }

  @Override
  protected void set_match_map() {
    map[NON_INDEX_ARTERY] = [
      'c\\.?f\\.?a\\.?',
      '(common[ \t]*)?femoral[ \t]*art(er(y|ies)?)?',
      '(distal[ \t]*)?popliteal[ \t]*(art(er(y|ies))?)?',
      's\\.?f\\.?a\\.?',
      'profundal[ \t]*(art(er(y|ies))?)?',
      'pop\\.?',
      'peroneal[ \t]*(art(er(y|ies))?)?',
      'lower[ \t]*extremity[ \t]*(art(er(y|ies))?)?',
      'i\\.?c\\.?(a\\.?)?',
      '(ext|int)\\.?(ernal)?[ \t]*carotid[ \t]*(art(er(y|ies))?)?',
      'fem',
    ];
    map[OTHER_INDEX_TYPE] = ['(radial|ulnar)[ \t]*(ind(ex|ices))?'];
    map[INDEX_VALUE_INTERPRETATION] = [
      'acceptable',
      '(borderline[ \t]*)?normal',
      'diminished',
      'mild[ \t]*((to|\\/)?[ \t]*moderate)?',
      'moderate[ \t]*(to|\\/)?[ \t]*mild',
      '(mild(ly)?|mod(\\.|erate(ly)?)?)[ \t]*(obstruct(ed|ion)?|reduc(ed|tion))',
      'mod(\\.|erate)?',
      'mod(\\.|erately)?[ \t]*severe',
      'not?[ \t]*sig(\\.|nificant)?',
      'obstruction',
      'sev(\\.|erity)?',
      'severe[ \t]*obstruction',
      'severely[ \t]*obstructed',
      'severe[ \t]*(pad|ischemia|obstruction)',
    ];
    map[LIST_INDEX] = ['(?<!\\([ \t]{0,4})\\d{1,2}[ \t]*\\)'];
    map[FLOW] = ['velocity', 'flow'];
    map[MISC] = ['plaques?', '(?:\\d[ \t]*)x(?:[ \t]*\\d)'];
    map[UNITS] = [
      '(mm|cm)?([ \t]*(\\/|per)?[ \t]*(s(ec)?))?',
      'hr',
      'min',
      'sec',
      'diameter',
      'mls?([ \t]*(\\/|per)?[ \t]*(m(in)?))?',
      'mm[ \t]*hg'
    ];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern nonIndexArtery = new StateAndPattern(NON_INDEX_ARTERY, NON_INDEX_ARTERY);
    StateAndPattern otherIndexType = new StateAndPattern(OTHER_INDEX_TYPE, OTHER_INDEX_TYPE);
    StateAndPattern indexValueInterpretation = new StateAndPattern(INDEX_VALUE_INTERPRETATION, INDEX_VALUE_INTERPRETATION);
    StateAndPattern listIndex = new StateAndPattern(LIST_INDEX, LIST_INDEX);
    StateAndPattern flow = new StateAndPattern(FLOW, FLOW);
    StateAndPattern misc = new StateAndPattern(MISC, MISC);
    StateAndPattern units = new StateAndPattern(UNITS, UNITS);

    return generate_categorical_detection_fsm(nonIndexArtery, otherIndexType, indexValueInterpretation, listIndex, flow, misc, units);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
