package src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers;

public class DatabaseDriver {
  // private String username;
  // private String password;
  // private String database;
  // private String server;
  //
  // public DatabaseDriver(String user, String pass){
  // username = user;
  // password = pass;
  // }
  //
  //
  // public void setServerAndDB(String server, String DB){
  // this.server = server;
  // this.database = DB;
  // }

  // public Connection connect(){
  // // String connectionUrl =
  // "jdbc:sqlserver://vhatvhressql1.v09.med.va.gov;databaseName=AKI_NLP;";
  // String connectionUrl = "jdbc:sqlserver://" + server + ";";
  //
  // if(database != null){
  // connectionUrl += "databaseName=" + database + ";";
  // }
  //
  // Connection con = null;
  //
  // try {
  // Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
  // con = DriverManager.getConnection(connectionUrl, username, password);
  // }
  // catch (SQLException e) {
  // e.printStackTrace();
  // }
  // catch (ClassNotFoundException e) {
  // e.printStackTrace();
  // }
  //
  //
  // return con;
  // }

  // public boolean createDatabase(String dbName) throws
  // NoConnectionException{
  // String query = "SELECT 1 FROM sys.sysdatabases WHERE name = '" + dbName
  // +"'";
  //
  // SQLDriver driver = new SQLDriver(username, password);
  // driver.setServerAndDB( server, database );
  //
  // Connection connection = SQLDriver.connect();
  // boolean success = false;
  //
  // if(connection == null){
  // throw new NoConnectionException("connection problem");
  // }
  //
  // ResultSet rs = SQLDriver.executeSelect(query, connection);
  //
  // try {
  // if(!rs.next()){ // nothing is returned .. that is DB does not exist
  // Statement stmt = connection.createStatement();
  // stmt.execute("create database " + dbName);
  // success = true;
  // }
  // else{
  // success = false;
  // }
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  //
  // SQLDriver.closeConnection(connection);
  // return success;
  // }

  // public void executeQueryFile(String filePath, String dbName) throws
  // NoConnectionException{
  // StringBuilder query = new StringBuilder();
  // String line;
  //
  // try{
  // BufferedReader br = new BufferedReader(new FileReader(filePath));
  //
  // while((line = br.readLine()) != null){
  // query.append(line).append("\n");
  // }
  //
  // br.close();
  // }
  // catch(IOException e){
  // e.printStackTrace();
  // }
  //
  //
  // SQLDriver driver = new SQLDriver(username, password);
  // driver.setServerAndDB( server, database );
  //
  // Connection connection = SQLDriver.connect();
  // Statement stmt;
  //
  // if(connection == null){
  // throw new NoConnectionException("connection problem");
  // }
  //
  // try {
  // stmt = connection.createStatement();
  // stmt.execute("begin transaction query;");
  // stmt.execute( "use " + dbName );
  // stmt.execute(query.toString());
  // stmt.execute("commit transaction query;");
  // } catch (SQLException ex) {
  // ex.printStackTrace();
  // }
  //
  // SQLDriver.closeConnection(connection);
  // }

  public static void main(String[] args) {

    // DatabaseDriver driver = new DatabaseDriver("admin", "admintest");
    // try {
    // String dbName = "Test";
    // boolean created = driver.createDatabase(dbName);
    //
    // if(created){
    // SQLDriver.executeQueryFile(".\\data\\NLP DB\\NLP DB Create User.sql",
    // dbName);
    // SQLDriver.executeQueryFile(".\\data\\NLP DB\\NLP DB Create
    // Table.sql", dbName);
    // }
    // } catch (NoConnectionException e) {
    // e.printStackTrace();
    // }
  }
}
