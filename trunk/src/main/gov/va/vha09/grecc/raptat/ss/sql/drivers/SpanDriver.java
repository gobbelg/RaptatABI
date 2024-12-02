/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;

/** @author VHATVHSAHAS1 */
public class SpanDriver {
  public static void addSpan(long annotationID, int start, int end, String spanText, int sequence,
      long sentenceID, String schema) throws NoConnectionException {
    String cols = "id_annotation, start_offset, end_offset, span_text, span_sequence, id_sentence";

    List<String> line = new ArrayList<>();
    spanText = spanText.replaceAll("\'", "\'\'");
    line.add(annotationID + "," + start + "," + end + ",'" + spanText + "'," + sequence + ","
        + sentenceID);

    SQLDriver.executeInsert(cols, line, schema, "span");
  }
}
