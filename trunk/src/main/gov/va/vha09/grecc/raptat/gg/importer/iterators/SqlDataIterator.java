package src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.DocumentRecord;

public class SqlDataIterator implements IterableData {

	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private int batchSize;
	private int offset;

	public SqlDataIterator(String serverName, String databaseName, String query,
			int batchSize) throws SQLException {

		this.conn = getConnection(serverName, databaseName);
		this.stmt = this.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		this.rs = null;
		this.batchSize = batchSize;
		this.offset = 0;

		// Set up result set parameters
		this.stmt.setFetchSize(batchSize);
	}

	public boolean hasNext() {
		if ( this.rs == null ) {
			// First time calling hasNext(), execute query
			try {
				this.rs = this.stmt.executeQuery(getQuery(this.offset));
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		boolean hasMore = false;
		try {
			hasMore = this.rs.last() && !this.rs.isAfterLast();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		if ( !hasMore ) {
			// No more records, close resources
			closeResources();
		}

		return hasMore;
	}

	@Override
	public Iterator<DocumentRecord> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public DocumentRecord next() {
		if ( !hasNext() ) {
			throw new NoSuchElementException();
		}

		StringBuilder sb = new StringBuilder();

		try {
			ResultSetMetaData metadata = this.rs.getMetaData();
			int numColumns = metadata.getColumnCount();

			for (int i = 1; i <= numColumns; i++) {
				String columnValue = this.rs.getString(i);
				sb.append(columnValue).append(", ");
			}

			// Remove trailing ", "
			if ( sb.length() > 2 ) {
				sb.setLength(sb.length() - 2);
			}

			// Fetch next batch of records
			this.offset += this.batchSize;
			this.rs = this.stmt.executeQuery(getQuery(this.offset));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return new DocumentRecord(null, null, "", null, "");
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void closeResources() {
		try {
			this.rs.close();
			this.stmt.close();
			this.conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection(String serverName, String databaseName) {
		Connection connection = null;

		String sqlServerClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

		// JDBC connection string with trusted connection
		String connectionUrl = "jdbc:sqlserver://" + serverName
				+ ";databaseName=" + databaseName + ";integratedSecurity=true;";

		try {
			Class.forName(sqlServerClassName);
			connection = DriverManager.getConnection(connectionUrl);
			// Connection succeeded
			System.out.println("Connected to SQL Server");
		}
		catch (SQLException e) {
			// Connection failed
			System.err.println("Failed to connect to SQL Server");
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Failed to load find SQLServerDriver class");
			e.printStackTrace();
		}
		return connection;
	}

	private String getQuery(int offset) {
		return "SELECT * FROM mytable LIMIT " + this.batchSize + " OFFSET "
				+ offset;
	}
}
