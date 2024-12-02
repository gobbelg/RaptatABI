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
public class SentenceDriver {
  public static long addSentence(long docID, String text, String schema)
      throws NoConnectionException {
    String columns = "id_document, sentence_text";
    Connection connection = SQLDriver.connect();

    try {
      String query = "insert into [" + schema + "].[sentence] (" + columns + " ) values (?,?);";

      PreparedStatement ps = connection.prepareStatement(query);
      ps.setLong(1, docID);
      ps.setString(2, text);

      ps.executeUpdate();

    } catch (SQLException ex) {
      Logger.getLogger(SchemasDriver.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return SQLDriver.getLastRowID(schema, "sentence");
  }


  public static long getsentenceID(String text, long docID, String schema)
      throws NoConnectionException {
    long id;

    try {
      id = sentenceExists(docID, text, schema);
    } catch (NoDataException ex) {
      id = addSentence(docID, text, schema);
    }

    return id;
  }


  public static long sentenceExists(long docID, String text, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    text = text.replaceAll("\'", "\'\'");

    try {
      String query = "select id_sentence as id from [" + schema + "].[sentence] "
          + "where sentence_text = '" + text + "' " + "AND id_document = " + docID;

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        do {
          id = rs.getLong("id");
        } while (rs.next());
      } else {
        throw new NoDataException("No annotator: " + text);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }
}
