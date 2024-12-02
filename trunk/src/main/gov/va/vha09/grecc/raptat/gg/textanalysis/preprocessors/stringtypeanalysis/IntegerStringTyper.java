package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.stringtypeanalysis;

import java.util.regex.Pattern;

/**
 * IntegerTypeStringMatcher
 *
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Apr 21, 2019
 *
 */
public class IntegerStringTyper implements TokenStringTyper {

  private static final Pattern INTEGER_TYPE_PATTERN =
      Pattern.compile("^\\d+$", Pattern.CASE_INSENSITIVE);

  @Override
  public boolean isType(String inputString) {
    return INTEGER_TYPE_PATTERN.matcher(inputString).matches();
  }

}
