/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/** @author VHATVHSAHAS1 */
public class AnnotatorDriver {

  public static long addAnnotator(String annotatorName, String schema)
      throws NoConnectionException {
    String cols = "annotator_name";

    List<String> line = new ArrayList<>();
    line.add("'" + annotatorName + "'");

    SQLDriver.executeInsert(cols, line, schema, "annotator");

    return SQLDriver.getLastRowID(schema, "annotator");
  }


  public static long annotatorExists(String annotatorName, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_annotator as id from [" + schema + "].[annotator] "
          + "where annotator_name = '" + annotatorName + "'";

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        do {
          id = rs.getLong("id");
        } while (rs.next());
      } else {
        throw new NoDataException("No annotator: " + annotatorName);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }


  public static long getAnnotatorID(String annotator, String schema) throws NoConnectionException {
    long id;

    try {
      id = annotatorExists(annotator, schema);
    } catch (NoDataException ex) {
      id = addAnnotator(annotator, schema);
    }

    return id;
  }
}
