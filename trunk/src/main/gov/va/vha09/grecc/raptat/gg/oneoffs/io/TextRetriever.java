package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class TextRetriever {
	public static void main(String[] args) {
		String connectionUrl = "jdbc:sqlserver://VHATVHRES6:1433;"
				+ "databaseName=Glenn;integratedsecurity=true;user=VHA09\\vhatvhgobbeg;password=fota$*_@!hsev20;";
		// String queryStart =
		// "Select CID FROM SNOMED_CID_Terms_Unclustered WHERE" +
		// " term LIKE ";
		String queryStart = "Select documentID, textRaw FROM TVHS_Processed_DocumentID_RawText";

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			GeneralHelper.tic("Starting SQL queries");

			try (Connection con = DriverManager.getConnection(connectionUrl);
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(queryStart)) {
				while ( rs.next() ) {
					String docTitle = rs.getString(1).replace("-", "_");
					String docText = rs.getString(2);
					PrintWriter resultWriter = new PrintWriter(
							docTitle + ".txt");
					resultWriter.print(docText);
					resultWriter.close();
				}
			}

		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
