/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/** @author VHATVHSAHAS1 */
public class SchemasDriver {
  public static long addSchema(FileInputStream fStream, String schemaType, String filePath,
      String fileHash, String schema) throws NoConnectionException {
    Connection connection = SQLDriver.connect();

    try {
      String columns = "schema_file, schema_type, file_path, file_hash";
      String query = "insert into [" + schema + "].[schemas] (" + columns + " ) values (?,?,?,?);";

      PreparedStatement ps = connection.prepareStatement(query);
      ps.setBinaryStream(1, fStream);
      ps.setString(2, schemaType);
      ps.setString(3, filePath);
      ps.setString(4, fileHash);

      ps.executeUpdate();

    } catch (SQLException ex) {
      Logger.getLogger(SchemasDriver.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return SQLDriver.getLastRowID(schema, "schemas");
  }


  public static long getSchemaID(FileInputStream fStream, String schemaSource, String fileName,
      String fileHash, AtomicBoolean schemaExists, String schema) throws NoConnectionException {
    long id;

    try {
      id = schemaExists(fileHash, schema);
      schemaExists.set(true);
    } catch (NoDataException ex) {
      id = addSchema(fStream, schemaSource, fileName, fileHash, schema);
      schemaExists.set(false);
    }

    return id;
  }


  public static void main(String[] args) {
    FileInputStream fis = null;

    try {
      File file = new File("D:\\Saha Workspace\\Test\\config\\projectschema.xml");
      fis = new FileInputStream(file);

      long id = SchemasDriver.getSchemaID(fis, "knowtator", "\\projectschema.xml", "dsfs",
          new AtomicBoolean(false), "");

      System.out.println(id);
    } catch (NoConnectionException ex) {
      ex.printStackTrace();
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } finally {
      try {
        fis.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }


  public static long schemaExists(String fileHash, String schema)
      throws NoConnectionException, NoDataException {
    Connection connection = SQLDriver.connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs;
    long id = 0;

    try {
      String query = "select id_schema as id " + "from [" + schema + "].[schemas] "
          + "where file_hash = '" + fileHash + "'";

      rs = SQLDriver.executeSelect(query, connection);

      if (rs.next()) {
        do {
          id = rs.getLong("id");
        } while (rs.next());
      } else {
        throw new NoDataException("No schema.");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      SQLDriver.closeConnection(connection);
    }

    return id;
  }
}
