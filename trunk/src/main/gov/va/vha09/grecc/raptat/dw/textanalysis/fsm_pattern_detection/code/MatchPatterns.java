package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


final public class MatchPatterns extends FSMElement<MatchPattern> {

  /**
   * Instantiates a new match patterns.
   *
   * @param match_patterns the match patterns
   */
  public MatchPatterns(MatchPattern... match_patterns) {
    super(match_patterns);
  }


  public MatchPattern get_matches(String pattern_input_str) {
    Set<MatchPattern> match_patterns = get_elements();
    MatchPattern rv = null;
    for (MatchPattern match_pattern : match_patterns) {
      if (match_pattern.get_match_pattern_name() == pattern_input_str) {
        rv = match_pattern;
        break;
      }
    }
    return rv;
  }


  public List<MatchPattern> get_matches(String... pattern_names) {

    List<MatchPattern> match_patterns = new ArrayList<>();
    for (String pattern_name : pattern_names) {
      MatchPattern matches = this.get_matches(pattern_name);
      if (matches != null) {
        match_patterns.add(matches);
      }
    }
    return match_patterns;
  }
}
