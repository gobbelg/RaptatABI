package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * Provides the means to define a series of regular expressions which may be tested against a token
 * for matching.
 */
final public class MatchPattern {

  public class MatchDetails<T extends RaptatToken> {
    private Integer __start = null;
    private Integer __end = null;
    private T __original_token = null;
    private String __found_text;
    private Matcher __matcher;
    private Pattern foundPattern;


    public MatchDetails(int start, int end) {
      this.__start = start;
      this.__end = end;
    }


    public MatchDetails(Matcher matcher, T token) {
      this(matcher);
      this.__original_token = token;
    }


    protected MatchDetails(Matcher matcher) {
      this.__start = matcher.start();
      this.__end = matcher.end();
      this.__found_text = matcher.group();
      this.__matcher = matcher;
    }


    public MatchDetails<RaptatTokenFSM> clone_to_offset(MatchDetails<RaptatTokenFSM> match_details,
        int startOffset) {
      MatchDetails<RaptatTokenFSM> rv = new MatchDetails<RaptatTokenFSM>(
          match_details.__start + startOffset, match_details.__end + startOffset);
      rv.__original_token = match_details.__original_token;
      rv.__found_text = match_details.__found_text;
      return rv;
    }


    public final Integer get_end() {
      return this.__end;
    }


    public String get_found_text() {
      return this.__found_text;
    }


    public T get_original_token() {
      return this.__original_token;
    }


    public final Integer get_start() {
      return this.__start;
    }


    public Pattern getFoundPattern() {
      return this.foundPattern;
    }


    public Matcher getMatcher() {
      return this.__matcher;
    }


    @Override
    public String toString() {

      return String.format("[%s, %s) - %s", this.__start, this.__end, this.__found_text);
    }

  }

  public interface MatchPatternValidate {
    boolean validate(MatchDetails<RaptatToken> foundMatchDetails);
  }

  public enum MatchType {
    Exact, Contains
  }

  /** The Constant None. */
  public static final MatchPattern None;

  static {
    None = new MatchPattern("None");
  }

  /** The match pattern name. */
  private String __match_pattern_name;

  /** The regex patterns. */
  private String[] __regex_pattern_strings;

  private Pattern[] __regex_patterns;

  private Comparator<String> patternComparator;


  /**
   * Instantiates a new match pattern.
   *
   * @param match_pattern_name the match pattern name
   * @param regex_patterns the regex patterns
   */
  public MatchPattern(String match_pattern_name, String[] regex_patterns) {
    this(match_pattern_name);
    /**
     * Asserts largest pattern should go first in evaluation
     */
    if (regex_patterns != null && regex_patterns.length > 0) {
      Arrays.sort(regex_patterns, (Comparator<String>) (lhs, rhs) -> rhs.length() - lhs.length());
    }
    this.__regex_pattern_strings = regex_patterns;
  }


  public MatchPattern(String match_pattern_name, String[] regex_patterns,
      Comparator<String> patternComparator) {
    this(match_pattern_name, regex_patterns);
    this.patternComparator = patternComparator;
  }


  /**
   * Instantiates a new match pattern.
   */
  private MatchPattern() {}


  /**
   * Instantiates a new match pattern.
   *
   * @param match_pattern_name the match pattern name
   */
  private MatchPattern(String match_pattern_name) {
    this();
    this.__match_pattern_name = match_pattern_name;
  }


  /**
   * Gets the match pattern name.
   *
   * @return the match pattern name
   */
  public String get_match_pattern_name() {
    return this.__match_pattern_name;
  }


  public <T extends RaptatToken> MatchDetails<T> matches(T token,
      IRaptatTokenText token_text_callback, MatchType match_type) {
    // late-bind compile until needed; will happen once
    if (this.__regex_patterns == null && this.__regex_pattern_strings != null) {
      this.__regex_patterns = new Pattern[this.__regex_pattern_strings.length];
      for (int index = 0; index < this.__regex_pattern_strings.length; index++) {
        String regex_pattern_string = this.__regex_pattern_strings[index];
        this.__regex_patterns[index] =
            Pattern.compile(regex_pattern_string, Pattern.CASE_INSENSITIVE);
      }
    }

    MatchDetails<T> found = null;

    if (this.__regex_patterns != null) {
      // iterate through all the possible combinations of definitions for
      // this match
      String token_text = token_text_callback.get(token);
      if (token_text != null) {
        for (Pattern pattern : this.__regex_patterns) {
          Matcher matcher = pattern.matcher(token_text);
          boolean find = matcher.find();
          if (find) {
            boolean exact_match = matcher.start() == 0
                && matcher.end() == token.get_end_offset_as_int() && match_type == MatchType.Exact;
            boolean partial_match = match_type == MatchType.Contains;
            if (exact_match || partial_match) {
              found = new MatchDetails<T>(matcher, token);
              // START change here
              break;
            }
          }
        }
      }
    }

    return found;
  }


  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return this.__match_pattern_name;
  }

}
