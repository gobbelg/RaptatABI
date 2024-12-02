/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/** @author gobbelgt */
public class FileStringReplacer {

	public static final String TEXT_FILEPATH = "C:\\Users\\gobbelgt\\Documents\\SubversionMatheny\\branches\\GlennWorkspace\\Projects\\RaptatBaseProject\\resources\\exampleForRegexStringReplacement.txt";
	public static final String REGEX_FILEPATH = "C:\\Users\\gobbelgt\\Documents\\SubversionMatheny\\branches\\GlennWorkspace\\Projects\\RaptatBaseProject\\resources\\exampleRegexes_190410.txt";

	public static void main(String[] args) {

		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				File textFile = new File(TEXT_FILEPATH);

				if ( !textFile.exists() ) {
					textFile = GeneralHelper.getFile(
							"Select text file containing text to be replaced");
				}

				File regexFile = new File(REGEX_FILEPATH);

				if ( !regexFile.exists() ) {
					regexFile = GeneralHelper.getFile(
							"Select text file containing regular expressions and strings to be used for replacement.\n"
									+ "Each line should contain the regular expression to locate followed by a tab followed by the string to use for replacement. ");
				}

				try (PrintWriter pw = new PrintWriter(
						getReplacementFile(textFile))) {
					List<RaptatPair<String, String>> regexAndStringReplacements = getReplacementRegexesAndStrings(
							regexFile);

					try (BufferedReader br = new BufferedReader(
							new FileReader(textFile))) {
						String textLine;
						while ( (textLine = br.readLine()) != null ) {
							System.out.println("TextLine:" + textLine);
							String replacementLine = getReplacementLine(
									textLine, regexAndStringReplacements);
							System.out.println(
									"ReplacementLine:" + replacementLine);
							pw.println(replacementLine);
							pw.flush();
						}
					}
					catch (IOException exception) {
						exception.printStackTrace();
					}

				}
				catch (FileNotFoundException exception1) {
					exception1.printStackTrace();
					GeneralHelper.errorWriter("Unable to create printwriter");
					System.exit(-1);
				}
			}

			private File getReplacementFile(File textFile) {
				String parentDirectoryPath = textFile.getParent();
				String textFileNameOld = textFile.getName();
				String[] textFileNameOnly = textFileNameOld.split("\\.");
				return new File(parentDirectoryPath,
						textFileNameOnly[0] + "_Replaced.txt");
			}

			private String getReplacementLine(String textLine,
					List<RaptatPair<String, String>> regexAndStringReplacements) {

				for (RaptatPair<String, String> regexAndReplacement : regexAndStringReplacements) {
					textLine = textLine.replaceAll(regexAndReplacement.left,
							regexAndReplacement.right);
				}
				return textLine;
			}

			private List<RaptatPair<String, String>> getReplacementRegexesAndStrings(
					File regexFile) {

				Set<RaptatPair<String, String>> resultSet = new HashSet<>();
				try (BufferedReader br = new BufferedReader(
						new FileReader(regexFile))) {
					String textLine;
					int line = 0;
					while ( (textLine = br.readLine()) != null ) {
						String[] regexReplacementPair = textLine.split("\t");
						if ( regexReplacementPair.length != 2 ) {
							GeneralHelper.errorWriter(
									"Incorrect formatting on line " + line
											+ " of regex string replacement file ");
							System.exit(-1);
						}
						++line;
						resultSet.add(new RaptatPair<>(regexReplacementPair[0],
								regexReplacementPair[1]));
					}
				}
				catch (IOException exception) {
					exception.printStackTrace();
				}
				return new ArrayList<>(resultSet);
			}
		});
	}
}
