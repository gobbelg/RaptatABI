package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.stringtypeanalysis;

import java.util.regex.Pattern;

/**
 * DecimalTypeStringMatcher
 *
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Apr 21, 2019
 *
 */
public class DecimalStringTyper implements TokenStringTyper {

  private static final Pattern DECIMAL_TYPE_PATTERN =
      Pattern.compile("^[-+]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)$", Pattern.CASE_INSENSITIVE);

  @Override
  public boolean isType(String inputString) {
    return DECIMAL_TYPE_PATTERN.matcher(inputString).matches();
  }
}
