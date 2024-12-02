package src.main.gov.va.vha09.grecc.raptat.gg.sql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class SQLTextExtractor {
	String storageDirectory = "";

	private static final int REPORT_ID_COLUMN = 1, TEXT_COLUMN = 2;

	public SQLTextExtractor() {
		this.storageDirectory = GeneralHelper
				.getDirectory("Select directory"
						+ " for storing files and concept mappings")
				.getAbsolutePath();
	}

	private void writeFile(String recordID, String textOfFile) {
		PrintWriter pw = null;
		String textPath = this.storageDirectory + File.separator + recordID
				+ ".txt";
		File outFile = new File(textPath);
		try {
			pw = new PrintWriter(outFile);
			pw.write(recordID + "\n\n");
			// pw.write(textOfFile);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if ( pw != null ) {
				pw.close();
			}
		}
	}

	public static void main(String[] args) {
		SQLTextExtractor extractor = new SQLTextExtractor();
		String connectionUrl = "jdbc:sqlserver://vhaechcartan01.v19.med.va.gov;"
				+ "databaseName=NLP;integratedsecurity=true;user=VHA09\\vhatvhgobbeg;password=fota$*_@!hsev32;";
		String dbTitle = "StressTest554Temp";
		String queryStart = "Select top 10 " + "reportID, text FROM " + dbTitle;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println(
					"Unable to find com.microsoft.sqlserver.jdbc.SQLServerDriver class "
							+ e.getLocalizedMessage());
			System.exit(-1);
		}
		try (Connection con = DriverManager.getConnection(connectionUrl);
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(queryStart)) {
			GeneralHelper.tic("Starting SQL queries");
			while ( rs.next() ) {
				String curID = rs.getString(SQLTextExtractor.REPORT_ID_COLUMN);
				System.out.println("Current ID:" + curID);
				extractor.writeFile(curID,
						rs.getString(SQLTextExtractor.TEXT_COLUMN));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Unable to connect to server or execute query "
					+ e.getLocalizedMessage());
			System.exit(-1);
		}
	}
}
