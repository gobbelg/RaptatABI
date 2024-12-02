package src.main.resources.config.pattern_config.grouped
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

class LateralityFSM extends FSMDefinitionBase {

  String LEFT;
  String RIGHT;
  String BILATERAL;
  String LEFT_PATTERN;
  String RIGHT_PATTERN;
  String BILATERAL_PATTERN;

  @Override
  public String get_fsm_name() {
    return "Laterality";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    LEFT = "Left";
    RIGHT = "Right";
    BILATERAL = "Bilateral";
    LEFT_PATTERN = "LEFT";
    RIGHT_PATTERN = "RIGHT";
    BILATERAL_PATTERN = "BILATERAL_PATTERN";
  }

  @Override
  protected void set_match_map() {
    map[RIGHT_PATTERN] = [
      'right(?=:|-|\\.|~|;|\\b|\\.:)',
      /*
       * What is this capturing?
       */
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
      'rpt'
    ];

    map[LEFT_PATTERN] = [
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
      'lpt'
    ];
    /*
     * Why was BILATERAL_PATTERN commented out?
     */
    //		map[BILATERAL_PATTERN] = [
    //			'bilateral(ly)?',
    //			'b/l',
    //			'both\\s*extremities',
    //			'right\\s*and\\s*left',
    //			'right\\s*and\\s*left\\s*ankle',
    //			'b\\/l',
    //			'both\\s*sides'
    //		];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern left = new StateAndPattern(LEFT, LEFT_PATTERN);
    StateAndPattern right = new StateAndPattern(RIGHT, RIGHT_PATTERN);
    //		StateAndPattern bilateral = new StateAndPattern(BILATERAL, BILATERAL_PATTERN);

    return generate_categorical_detection_fsm(left, right/*, bilateral*/);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
