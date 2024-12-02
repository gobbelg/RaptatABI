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
public class AttrValueDriver {
  public static long addValue(String value, String schema) throws NoConnectionException {
    String cols = "attribute_value";

    List<String> line = new ArrayList<>();
    line.add("'" + value + "'");

    SQLDriver.executeInsert(cols, line, schema, "attr_value");

    return SQLDriver.getLastRowID(schema, "attr_value");
  }


  public static long getAttrValueID(String attribute, String schema) throws NoConnectionException {
    long id;

    try {
      id = valueExists(attribute, schema);
    } catch (NoDataException ex) {
      id = addValue(attribute, schema);
    }

    return id;
  }


  public static long valueExists(String value, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_value as id from [" + schema + "].[attr_value] "
          + "where attribute_value = '" + value + "'";

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        id = rs.getLong("id");
      } else {
        throw new NoDataException("No value");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }
}
