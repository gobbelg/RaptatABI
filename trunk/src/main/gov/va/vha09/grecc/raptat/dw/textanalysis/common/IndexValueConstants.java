package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.common;

import com.google.common.base.Joiner;

public class IndexValueConstants {

  public static final String IndexValueKey = "index";

  public static final String IndexValueRegEx = Joiner.on("").join(new String[] {"(?:", // line
      "(?:[\\(\\[]?(?:\\d+/)?", // line
      "(?:[\\t]*?", // line
      "(?<index>(?<!\\.)(?<!\\d)", // line
      "(?:[\\<\\>]\\s*)?", // line
      "(?:[012]?\\.\\d{1,2}", // line
      "|", // line
      "[012](?!\\d|\\.|\\p{L}|\\/|\\:|\\ |\\+)", // line
      ")", // line
      ")", // line
      ")", // line
      "[ \\t]*?", // line
      "[\\)\\]]?", // line
      ")", // line
      ")" // line
  });

  public static final String IndexValueIgnoreRegEx = Joiner.on("")
      .join(new String[] {"(?<exlcusion>", // line
          "(\\b[012](\\.\\d{0,2}|\\b))", // line
          "([ \\t]*)", // line
          "(", // line
          "(", // line
          "(?:\\-|to|\\&)", // line
          "(?:[ \\t]*)", // line
          "(?:([012]\\s*(\\.\\d{0,2}|\\b)(?![\\d])|below))", // line
          ")", // line
          "|", // line
          "(mm|cm|min)", // line
          ")", // line
          ")" // line
      });

}
