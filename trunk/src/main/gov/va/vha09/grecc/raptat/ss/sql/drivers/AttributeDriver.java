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
public class AttributeDriver {
  public static long addAttribute(String attributeName, String schema)
      throws NoConnectionException {
    String cols = "attribute_name";

    List<String> line = new ArrayList<>();
    line.add("'" + attributeName + "'");

    SQLDriver.executeInsert(cols, line, schema, "attribute");

    return SQLDriver.getLastRowID(schema, "attribute");
  }


  public static void addAttributeValues(long attributeID, long valueID, long schemaID,
      String schema) throws NoConnectionException {
    try {
      attrAttrValueExists(attributeID, valueID, schema);
    } catch (NoDataException ex) {
      String cols = "id_attribute, id_value";

      List<String> line = new ArrayList<>();
      line.add(attributeID + ", " + valueID);

      SQLDriver.executeInsert(cols, line, schema, "rel_attr_attrval");
    }
  }


  public static long attrAttrValueExists(long attributeID, long valueID, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_attr_attrval as id from [" + schema + "].[rel_attr_attrval] "
          + "where id_attribute = " + attributeID + " " + "AND id_value = " + valueID;

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        id = rs.getLong("id");
      } else {
        throw new NoDataException("No Attribute Value");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }


  public static long attributeExists(String attributeName, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_attribute as id from [" + schema + "].[attribute] "
          + "where attribute_name = '" + attributeName + "'";

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        id = rs.getLong("id");
      } else {
        throw new NoDataException("No Attribute: " + attributeName);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }


  public static long getAttributeID(String attribute, String schema) throws NoConnectionException {
    long id;

    try {
      id = attributeExists(attribute, schema);
    } catch (NoDataException ex) {
      id = addAttribute(attribute, schema);
    }

    return id;
  }
}
