/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;

/**
 * @author VHATVHSAHAS1 Created on March 25, 2015
 *         <p>
 *         This class handles the DB operations related to SQLServer
 */
public class SQLDriver {
  static String username;

  static String password;

  static String database;

  static String server;

  public static Connection _con;


  public SQLDriver(String user, String pass) {
    SQLDriver.username = user;
    SQLDriver.password = pass;
  }


  public boolean createDatabase(String dbName) throws NoConnectionException {
    String query = "SELECT 1 FROM sys.sysdatabases WHERE name = '" + dbName + "'";

    SQLDriver driver = new SQLDriver(SQLDriver.username, SQLDriver.password);
    driver.setServerAndDB(SQLDriver.server, SQLDriver.database);

    // Connection _con = SQLDriver.connect();
    boolean success = false;

    if (SQLDriver._con == null) {
      throw new NoConnectionException("connection problem");
    }

    ResultSet rs = SQLDriver.executeSelect(query, SQLDriver._con);

    try {
      if (!rs.next()) { // nothing is returned .. that is DB does not exist
        Statement stmt = SQLDriver._con.createStatement();
        stmt.execute("create database " + dbName);
        success = true;
      } else {
        success = false;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // SQLDriver.closeConnection( _con );
    return success;
  }


  public void setServerAndDB(String serverName, String DB) {
    SQLDriver.server = serverName;
    SQLDriver.database = DB;
  }


  /**
   * Closes the connection to the DB.
   *
   * @param con
   */
  public static void closeConnection(Connection con) {
    // try
    // {
    // con.close();
    // }
    // catch (SQLException e)
    // {
    // e.printStackTrace();
    // }
  }


  public static void closeConnectionGlobal() {
    try {
      SQLDriver._con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * Creates a connection using SQL server authentication.
   *
   * @return connection to the DB "AKI_NLP" or null
   */
  public static Connection connect() {
    // String connectionUrl =
    // "jdbc:sqlserver://vhatvhressql1.v09.med.va.gov;databaseName=AKI_NLP;";
    // String connectionUrl =
    // "jdbc:sqlserver://localhost;databaseName=AKI_NLP;";
    // String connectionUrl = "jdbc:sqlserver://localhost;";
    //
    // String username = "RapTAT_user";
    // String password = "raptat_USER";
    //
    // String connectionUrl = "jdbc:sqlserver://" + server + ";";
    //
    // if ( database != null )
    // {
    // connectionUrl += "databaseName=" + database + ";";
    // }
    //
    // Connection con = null;
    //
    // try
    // {
    // Class.forName( "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
    // con = DriverManager.getConnection( connectionUrl, username, password
    // );
    // }
    // catch (SQLException e)
    // {
    // e.printStackTrace();
    // }
    // catch (ClassNotFoundException e)
    // {
    // e.printStackTrace();
    // }

    return SQLDriver._con;
  }


  @Deprecated
  public static void connectTest() {
    String dbURL = "jdbc:sqlserver://localhost\\sqlexpress;user=Raptat_User;password=raptat_USER";
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(dbURL);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    if (conn != null) {
      System.out.println("Connected");
    }
  }


  public static void createTablesFromQueryFile(String filePath, String dbName, String schemaName)
      throws NoConnectionException {
    if (tablesExists(dbName, schemaName)) {
      return;
    }

    StringBuilder query = new StringBuilder();
    String line;

    try {
      BufferedReader br = new BufferedReader(new FileReader(filePath));

      while ((line = br.readLine()) != null) {
        query.append(line).append("\n");
      }

      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    String queryString = query.toString();
    queryString = queryString.replaceAll("dbo", schemaName);
    // queryString = queryString.replaceAll( "@level1name=N'",
    // "@level1name=N'" + schemaName);
    // queryString = queryString.replaceAll( "PK_", "PK_" + schemaName);
    // queryString = queryString.replaceAll( "FK_", "FK_" + schemaName);
    // System.out.println( queryString );

    Connection connection = connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    try {
      Statement stmt = connection.createStatement();
      stmt.execute("begin transaction query;");
      stmt.execute("use " + dbName);
      stmt.execute(queryString);
      stmt.execute("commit transaction query;");
    } catch (SQLException ex) {
      ex.printStackTrace();
      SQLDriver.closeConnection(connection);
    }

    SQLDriver.closeConnection(connection);
  }


  public static void dropTables(String filePath, String dbName, String schemaName)
      throws NoConnectionException {
    StringBuilder query = new StringBuilder();
    String line;

    try {
      BufferedReader br = new BufferedReader(new FileReader(filePath));

      while ((line = br.readLine()) != null) {
        query.append(line).append("\n");
      }

      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    String queryString = query.toString();
    queryString = queryString.replaceAll("dbo", schemaName);

    Connection connection = connect();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    try {
      Statement stmt = connection.createStatement();
      stmt.execute("begin transaction query;");
      stmt.execute("use " + dbName);
      stmt.execute(queryString);
      stmt.execute("commit transaction query;");
    } catch (SQLException ex) {
      ex.printStackTrace();
      SQLDriver.closeConnection(connection);
    }

    SQLDriver.closeConnection(connection);
  }


  public static void executeInsert(String cols, List<String> dataTable, String schema, String table)
      throws NoConnectionException {
    StringBuilder queryInsert = new StringBuilder("insert into [").append(schema).append("].");
    queryInsert.append("[").append(table).append("] ");
    queryInsert.append("(").append(cols).append(") values (");
    StringBuilder query;

    // Connection _con = connect();
    Statement stmt;

    if (SQLDriver._con == null) {
      throw new NoConnectionException("connection problem");
    }

    for (String lines : dataTable) {
      query = queryInsert.append(lines);

      query.append(");");

      try {
        stmt = SQLDriver._con.createStatement();
        stmt.execute("begin transaction query;");
        stmt.execute(query.toString());
        stmt.execute("commit transaction query;");
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    }

    // closeConnection( _con );
  }


  public static void executeQueryFile(String filePath, String dbName) throws NoConnectionException {
    StringBuilder query = new StringBuilder();
    String line;

    try {
      BufferedReader br = new BufferedReader(new FileReader(filePath));

      while ((line = br.readLine()) != null) {
        query.append(line).append("\n");
      }

      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Connection connection = connect();
    Statement stmt;

    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    try {
      stmt = connection.createStatement();
      stmt.execute("begin transaction query;");
      stmt.execute("use " + dbName);
      stmt.execute(query.toString());
      stmt.execute("commit transaction query;");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    SQLDriver.closeConnection(connection);
  }


  /**
   * Executes the select queries and return the result set
   *
   * @param query
   * @param connection
   * @return
   * @throws src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException
   */
  public static ResultSet executeSelect(String query, Connection connection)
      throws NoConnectionException {
    ResultSet rs = null;
    Statement stmt;

    try {
      stmt = connection.createStatement();
      rs = stmt.executeQuery(query);
    } catch (SQLException ex) {
      Logger.getLogger(SQLDriver.class.getName()).log(Level.SEVERE, null, ex);
    }

    return rs;
  }


  public static long getLastRowID(String schemaName, String table) throws NoConnectionException {
    ResultSet rs;
    Statement stmt;
    Connection connection = connect();

    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }

    String query = "SELECT IDENT_CURRENT('[" + schemaName + "].[" + table + "]') as id";
    long id = 0;

    try {
      stmt = connection.createStatement();
      rs = stmt.executeQuery(query);

      while (rs.next()) {
        id = rs.getLong("id");
      }
    } catch (SQLException ex) {
      Logger.getLogger(SQLDriver.class.getName()).log(Level.SEVERE, null, ex);
    }

    return id;
  }


  public static void init() {
    String connectionUrl = "jdbc:sqlserver://" + SQLDriver.server + ";";

    if (SQLDriver.database != null) {
      connectionUrl += "databaseName=" + SQLDriver.database + ";";
    }

    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      SQLDriver._con =
          DriverManager.getConnection(connectionUrl, SQLDriver.username, SQLDriver.password);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }


  // TODO - Shuvro... Needs to implement update
  // public static void executeUpdate(String cols, List<List<String>>
  // dataTable, String table)
  // throws NoConnectionException
  // {
  // // StringBuilder queryInsert = new StringBuilder("insert into (");
  // queryInsert.append(cols).append(") values (");
  // StringBuilder query;
  //
  // Connection connection = connect();
  // Statement stmt;
  //
  // if(connection == null){
  // throw new NoConnectionException("connection problem");
  // }
  //
  // for(List<String> lines: dataTable){
  // query = queryInsert.append(lines.get(0));
  //
  // for(String data: lines){
  // query.append(",").append(data);
  // }
  //
  // query.append(");");
  //
  // try {
  // stmt = connection.createStatement();
  // stmt.execute
  // } catch (SQLException ex) {
  // Logger.getLogger(SQLDriver.class.getName()).log(Level.SEVERE, null,
  // ex);
  // }
  // }
  //
  // closeConnection(connection);
  // }

  public static void main(String[] args) {
    String user = "Raptat_User", passwd = "raptat_USER";
    String server = "vhatvhsqlres1", dbname = "ORD_Matheny_201312053D";
    SQLDriver driver = new SQLDriver(user, passwd);
    driver.setServerAndDB(server, dbname);

    try {
      String schemaName = "Raptat1";
      SQLDriver.dropTables(".\\NLP DB\\NLP DB Drop Table.sql", dbname, schemaName);
      SQLDriver.createTablesFromQueryFile(".\\NLP DB\\NLP DB Create Table.sql", dbname, schemaName);

    } catch (NoConnectionException e) {
      e.printStackTrace();
    }

    // try{
    // Connection con = SQLDriver.connect();
    // System.out.println("connected");
    // SQLDriver.closeConnection(con);
    // }
    // catch (Exception ex){
    // ex.printStackTrace();
    // }
    //
    // SQLDriver.connectTest();

    // try {
    // SQLDriver.executeQueryFile("C:\\Users\\sahask\\workspace\\NLP DB\\NLP
    // DB Create User.sql");
    // SQLDriver.executeQueryFile("C:\\Users\\sahask\\workspace\\NLP DB\\NLP
    // DB Create Table.sql");
    // } catch (NoConnectionException e) {
    // e.printStackTrace();
    // }
  }


  public static boolean tablesExists(String dbName, String schema) throws NoConnectionException {
    // Connection _con = connect();
    if (SQLDriver._con == null) {
      throw new NoConnectionException("connection problem");
    }

    String queryCheckTableExists =
        "SELECT 1 FROM INFORMATION_SCHEMA.TABLES " + "WHERE TABLE_CATALOG='" + dbName + "' "
            + " AND TABLE_SCHEMA='" + schema + "' " + " AND TABLE_NAME='schemas'; ";

    ResultSet rs = SQLDriver.executeSelect(queryCheckTableExists, SQLDriver._con);
    try {
      if (rs.next()) {
        return true;
      }
    } catch (SQLException e1) {
      e1.printStackTrace();
      SQLDriver.closeConnection(SQLDriver._con);
    }

    // SQLDriver.closeConnection( _con );
    return false;
  }
}
