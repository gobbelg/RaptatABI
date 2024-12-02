package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.common;

public class StringHelper {

  public static String leftTrim(String text) {
    String replace = text.replaceAll("^\\s*", "");
    return replace;
  }

  public static String rightTrim(String text) {
    String replace = text.replaceAll("\\s*$", "");
    return replace;
  }

}
