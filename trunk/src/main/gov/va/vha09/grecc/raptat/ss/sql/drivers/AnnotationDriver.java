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
public class AnnotationDriver {
  public static long addAnnotation(long annotatorID, long conceptID, String xmlAnnotationID,
      String schema) throws NoConnectionException {
    String cols = "id_annotator, id_concept, xml_annotation_id";

    List<String> line = new ArrayList<>();
    line.add(annotatorID + "," + conceptID + ", '" + xmlAnnotationID + "'");

    SQLDriver.executeInsert(cols, line, schema, "annotation");

    return SQLDriver.getLastRowID(schema, "annotation");
  }


  public static void addAnnotationAttribute(long annID, long attrAttrValID, String xmlAttributeID,
      String schema) throws NoConnectionException {
    String cols = "id_annotation, id_attr_attrval, xml_attribute_id";

    List<String> line = new ArrayList<>();
    line.add(annID + "," + attrAttrValID + ", '" + xmlAttributeID + "'");

    SQLDriver.executeInsert(cols, line, schema, "rel_ann_attr");
  }


  public static boolean annotationExists(long annotatorID, long conceptID, String xmlAnnotationID,
      String schema) throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    try {
      String query = "select id_annotation as id from [" + schema + "].[annotation] "
          + "where id_annotation = '" + annotatorID + "'" + "and id_concept = '" + conceptID + "'"
          + "and xml_annotation_id = '" + xmlAnnotationID + "'";

      ResultSet rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        return true;
      } else {
        return false;
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return false;
  }
}
