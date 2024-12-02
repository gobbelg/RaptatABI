package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.stringtypeanalysis;

import java.util.regex.Pattern;

public class SectionHeaderDetector implements TokenStringTyper {

  private static final int patternMatchSetting =
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE;
  private static final Pattern[] HEADER_TYPE_PATTERNS = new Pattern[] {
      Pattern.compile("^[ \t]*[A-Z][A-Za-z]*([ \t]+[A-Z][A-Za-z]*)*[ \t]*$"),
      Pattern.compile("^.*:[ \t]{0,2}$"),
      Pattern.compile("^\\s*[A-Za-z0-9]+(\\s{1,3}[A-Za-z0-9]+)*\\s{0,3}:.*$"),
      Pattern.compile("^[ \t]*[-]+[ \t]*[A-Za-z0-9]+.*$"),
      Pattern.compile(
          "^[ \t]*((\\d{1,2}\\.?)|(\\(? {0,2}\\d{1,2} {0,2}\\))|(\\[ {0,2}(x|y(es)?|no?)\\]))[ \\t]*.*$",
          patternMatchSetting)};



  @Override
  public boolean isType(String inputString) {
    for (Pattern pattern : HEADER_TYPE_PATTERNS) {
      if (pattern.matcher(inputString).matches()) {
        return true;
      }
    }

    return false;
  }

}
