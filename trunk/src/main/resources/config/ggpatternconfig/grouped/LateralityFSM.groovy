package src.main.resources.config.ggpatternconfig.grouped
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

class LateralityFSM extends FSMDefinitionBase {

  String LEFT;
  String RIGHT;
  String BILATERAL;

  @Override
  public String get_fsm_name() {
    return "Laterality";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    LEFT = "Left";
    RIGHT = "Right";
    BILATERAL = "Bilateral";
  }

  @Override
  protected void set_match_map() {
    map[RIGHT] = [
      'right(?=:|-|\\.|~|;|\\b|\\.:)',
      '(?<=(\\d|\\b))r(:|-|\\.|~|;|\\b|\\)|:|p|d)',
      '\\(r\\)',
      '\\brt(:|-|\\.|~|;|\\b|\\.:)',
      '[Rr][Ii][Gg][Hh][Tt]-?',
      /*
       * Where is case sensitivity controlled?
       */
      '[Rr][Tt](:|-|\\.|~|;|\\b|\\.:)',
      /*
       * What is 'rle' an acronym for?
       */
      'rle(:|-|\\.|~|;|\\b|\\.:)',
      'rdp',
      'rpt',
      'right[ \t]*\\-?[ \t]*sided?'
    ];

    map[LEFT] = [
      'left(?=:|-|\\.|~|;|\\b)',
      /*
       * What is this capturing?
       */
      '(?<=(\\d|\\b))l(:|-|\\.|~|;|\\b|\\.:|\\)|p|d)',
      '\\(l\\)',
      '[Ll][Ee][Ff][Tt]-?',
      'LLE(:|-|\\.|~|;|\\b)',
      '[Ll][Tt](:|-|\\.|~|;|\\b|\\.:)',
      'ldp',
      'lpt',
      '\\(?(l|r)\\)?',
      'left[ \t]*\\-?[ \t]*sided?'
    ];

    map[BILATERAL] = [
      'bilateral(ly)?',
      'bl',
      'both[ \t]*extremities',
      'right[ \t]*and[ \t]*left[ \t]*(sides)?',
      'b\\/l',
      'both[ \t]*sides',
      'bi',
    ];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern left = new StateAndPattern(LEFT, LEFT);
    StateAndPattern right = new StateAndPattern(RIGHT, RIGHT);
    StateAndPattern bilateral = new StateAndPattern(BILATERAL, BILATERAL);

    return generate_categorical_detection_fsm(left, right, bilateral);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
