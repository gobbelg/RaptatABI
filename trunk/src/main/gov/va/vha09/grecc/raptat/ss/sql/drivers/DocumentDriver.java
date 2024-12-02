/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/** @author VHATVHSAHAS1 */
public class DocumentDriver {
  public static long addDocument(FileInputStream fStream, String docSource, Long sourceID,
      String schema) throws NoConnectionException {
    Connection connection = SQLDriver.connect();

    try {
      String columns = "document_blob, document_source,doc_source_id";
      String query = "insert into [" + schema + "].[document] (" + columns + " ) values (?,?,?);";

      PreparedStatement ps = connection.prepareStatement(query);
      ps.setBinaryStream(1, fStream);
      ps.setString(2, docSource);
      ps.setLong(3, sourceID);

      ps.executeUpdate();

    } catch (SQLException ex) {
      Logger.getLogger(SchemasDriver.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return SQLDriver.getLastRowID(schema, "document");
  }


  // This method is written for the LEO project
  public static long addDocument(String docText, String docSource, Long sourceID, String schema)
      throws NoConnectionException {
    Connection connection = SQLDriver.connect();
    InputStream is = new ByteArrayInputStream(docText.getBytes());

    try {
      String columns = "document_blob, document_source,doc_source_id";
      String query = "insert into [" + schema + "].[document] (" + columns + " ) values (?,?,?);";

      PreparedStatement ps = connection.prepareStatement(query);
      ps.setBinaryStream(1, is);
      ps.setString(2, docSource);
      ps.setLong(3, sourceID);

      ps.executeUpdate();
    } catch (SQLException ex) {
      Logger.getLogger(SchemasDriver.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return SQLDriver.getLastRowID(schema, "document");
  }


  public static long documentExists(long sourceID, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_document as id from [" + schema + "].[document] "
          + "where doc_source_id = " + sourceID;

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        do {
          id = rs.getLong("id");
        } while (rs.next());
      } else {
        throw new NoDataException("doc does not exits");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }


  public static long getDocumentID(FileInputStream fStream, String docSource, Long sourceID,
      String schema) throws NoConnectionException {
    long id;

    try {
      id = documentExists(sourceID, schema);
    } catch (NoDataException ex) {
      id = addDocument(fStream, docSource, sourceID, schema);
    }

    return id;
  }


  public static long getDocumentID(String docText, String docSource, Long sourceID, String schema)
      throws NoConnectionException {
    long id;

    try {
      id = documentExists(sourceID, schema);
    } catch (NoDataException ex) {
      id = addDocument(docText, docSource, sourceID, schema);
    }

    return id;
  }
}
