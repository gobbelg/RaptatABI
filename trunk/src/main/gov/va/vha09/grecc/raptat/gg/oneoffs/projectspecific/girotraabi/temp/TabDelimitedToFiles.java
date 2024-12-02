/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.temp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.apache.uima.util.FileUtils;

import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;

/**
 * Created to allow converting a tab-delimited file provided by Sudarshan to a
 * set of files
 *
 * @author VHATVHGOBBEG Creation Date - 09/18/25
 *
 */
public class TabDelimitedToFiles {

	private final static String FILE_ID = "PatientSID_DocID_Sta3n:\t";
	private final static String DATE_ID = "BeginDateTime:\t";
	private final static Pattern fileEndRegex = Pattern.compile("\"\t\\d");

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// DataImportReader dr = new TabReader("Select tab-delimited file");
		File tabFile = new File(
				"P:/ORD_Nguyen_202206062D/Glenn/DataAnalysis/FirstPADAssessment/CohortList.txt");
		DataImportReader dr = new TabReader(tabFile);
		String[] data;
		String parentPathString = dr.getFileDirectory();
		String txtFileFolderName = "txtFiles_" + GeneralHelper.getTimeStamp();
		File txtFileDirectory = new File(parentPathString, txtFileFolderName);
		FileUtils.mkdir(txtFileDirectory);

		while ( ((data = dr.getNextData()) != null) ) {

			/*
			 * Remove any blank space before next document text begins
			 */
			if ( (data.length == 0) || String.join("", data).isBlank() ) {
				continue;
			}

			System.out.println("Creating document:" + data[0]);

			String fileId = data[0] + "_" + data[1] + "_" + data[2];
			int dateTimeIndex = 4;
			String date = getDate(data, dateTimeIndex);
			String fileName = fileId + "_Report.txt";

			/*
			 * Remove quote commonly found at beginning of string
			 */
			String reportText = data[8].length() > 0 ? data[8].substring(1)
					: data[8];

			File txtFile = new File(txtFileDirectory, fileName);
			try (PrintWriter pw = new PrintWriter(txtFile)) {
				pw.print(FILE_ID);
				pw.println(fileId);
				pw.print(DATE_ID);
				pw.println(date);

				pw.println();
				pw.println();
				pw.println(reportText);

				/*
				 * Read and write other lines of text until we come upon a match
				 * to the fileEndRegex
				 */
				while ( ((data = dr.getNextData()) != null)
						&& !isDocumentEnd(data) ) {
					pw.println(data[0]);
				}
				pw.flush();
				pw.close();
			}
			catch (FileNotFoundException e) {
				System.err.println("Unable to create PrintWriter to write file:"
						+ txtFile);
				e.printStackTrace();
			}

		}
	}

	/**
	 * @param metadata
	 * @param dateTimeIndex
	 * @return
	 * @throws NumberFormatException
	 */
	private static String getDate(String[] metadata, int dateTimeIndex)
			throws NumberFormatException {

		String[] dateTime = metadata[dateTimeIndex].split(" ");
		String[] dataElements = dateTime[0].split("/");

		String month = String.format("%02d", Integer.parseInt(dataElements[0]));
		String day = String.format("%02d", Integer.parseInt(dataElements[1]));
		String year = String.format("%04d", Integer.parseInt(dataElements[2]));

		return year + "-" + month + "-" + day;
	}

	private static boolean isDocumentEnd(String[] data) {
		if ( (data.length == 2) && data[0].trim().equals("\"")
				&& data[1].trim().matches("\\d") ) {
			return true;
		}
		return false;
	}
}
