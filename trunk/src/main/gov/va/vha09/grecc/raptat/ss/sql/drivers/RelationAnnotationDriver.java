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
public class RelationAnnotationDriver {
  public static long addRelationAnnotation(String relationName, long idAnnotation, int sequence,
      String schema) throws NoConnectionException {
    String cols = "id_relation, id_annotation, annotation_sequence";
    long idRelation;

    idRelation = RelationDriver.getRelationID(relationName, schema);

    List<String> line = new ArrayList<>();
    line.add(idRelation + ", " + idAnnotation + ", '" + sequence + "'");

    SQLDriver.executeInsert(cols, line, schema, "relation_concept_attribute");

    return SQLDriver.getLastRowID(schema, "relation_concept_attribute");
  }


  public static long relationAnnotationExists(String relationName, long idAnnotation, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;
    long idRelation;

    idRelation = RelationDriver.relationExists(relationName, schema);

    try {
      String query = "select id_rel_ann as id from [" + schema + "].[relation_relation_annotation] "
          + "where id_relation = '" + idRelation + "'" + " and id_annotation = '" + idAnnotation
          + "'";

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        do {
          id = rs.getLong("id");
        } while (rs.next());
      } else {
        throw new NoDataException("No value");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    SQLDriver.closeConnection(connection);

    return id;
  }
}
