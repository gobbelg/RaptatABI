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
public class RelationDriver {
  public static long addRelation(String relationName, String schema) throws NoConnectionException {
    String cols = "relation_name";

    List<String> line = new ArrayList<>();
    line.add("'" + relationName + "'");

    SQLDriver.executeInsert(cols, line, schema, "relation");

    return SQLDriver.getLastRowID(schema, "relation");
  }


  public static long getRelationID(String relationName, String schema)
      throws NoConnectionException {
    long id;

    try {
      id = relationExists(relationName, schema);
    } catch (NoDataException ex) {
      id = addRelation(relationName, schema);
    }

    return id;
  }


  public static long relationExists(String relationName, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_relation as id from [" + schema + "].[relation] "
          + "where relation_name = '" + relationName + "'";

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        do {
          id = rs.getLong("id");
        } while (rs.next());
      } else {
        throw new NoDataException("No relation: " + relationName);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    SQLDriver.closeConnection(connection);

    return id;
  }
}
