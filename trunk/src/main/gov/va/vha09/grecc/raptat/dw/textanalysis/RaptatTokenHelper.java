package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class RaptatTokenHelper {
  public static RaptatToken AsPreprocessed(String value, int startOffset, int endOffset,
      int tokenIndex) {
    return new RaptatToken(value, null, null, null, String.valueOf(startOffset),
        String.valueOf(endOffset), null, tokenIndex);
  }
}
