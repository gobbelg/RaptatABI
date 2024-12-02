/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/** @author VHATVHSAHAS1 */
public class ConceptDriver {
  public static long addConcept(String conceptName, long schemaID, String schema)
      throws NoConnectionException {
    String columns = "concept_name, id_schema";
    Connection connection = SQLDriver.connect();

    try {
      String query = "insert into [" + schema + "].[concept] (" + columns + " ) values (?,?);";

      PreparedStatement ps = connection.prepareStatement(query);
      ps.setString(1, conceptName);
      ps.setLong(2, schemaID);

      ps.executeUpdate();
    } catch (SQLException ex) {
      Logger.getLogger(SchemasDriver.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return SQLDriver.getLastRowID(schema, "concept");
  }


  public static void addConceptAttribute(long attributeID, long conceptID, String schema)
      throws NoConnectionException {
    String columns = "id_attribute, id_concept";
    Connection connection = SQLDriver.connect();

    try {
      String query = "insert into [" + schema + "].[rel_con_attr] (" + columns + " ) values (?,?);";

      PreparedStatement ps = connection.prepareStatement(query);
      ps.setLong(1, attributeID);
      ps.setLong(2, conceptID);

      ps.executeUpdate();
    } catch (SQLException ex) {
      Logger.getLogger(SchemasDriver.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      SQLDriver.closeConnection(connection);
    }
  }


  public static long conceptExists(String conceptName, long schemaID, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_concept as id from [" + schema + "].[concept] "
          + "where concept_name = '" + conceptName + "'" + " AND id_schema = " + schemaID;

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        id = rs.getLong("id");
      } else {
        throw new NoDataException("No concept: " + conceptName);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    SQLDriver.closeConnection(connection);

    return id;
  }


  public static long getConceptID(String concept, long schemaID, String schema)
      throws NoConnectionException {
    long id;

    try {
      id = conceptExists(concept, schemaID, schema);
    } catch (NoDataException ex) {
      id = addConcept(concept, schemaID, schema);
    }

    return id;
  }
}
