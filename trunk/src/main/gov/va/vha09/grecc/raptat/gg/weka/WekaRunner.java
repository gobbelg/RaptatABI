/** */
package src.main.gov.va.vha09.grecc.raptat.gg.weka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.SwingUtilities;

// import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import weka.classifiers.meta.MultiSearch;
import weka.classifiers.meta.multisearch.DefaultSearch;
import weka.classifiers.trees.J48;
import weka.core.Option;

/**
 * @author gtony
 *         <p>
 *         Here is an example that optimises three parameters of
 *         LinearRegression: a Boolean option, a three-valued option, and a
 *         numeric parameter. weka.classifiers.meta.MultiSearch -E CC -search
 *         "weka.core.setupgenerator.ListParameter -property
 *         attributeSelectionMethod -custom-delimiter , -list \"No attribute
 *         selection,M5 method,Greedy method\"" -search
 *         "weka.core.setupgenerator.MathParameter -property ridge -min -5.0
 *         -max 5.0 -step 1.0 -base 10.0 -expression pow(BASE,I)" -search
 *         "weka.core.setupgenerator.ListParameter -property
 *         eliminateColinearAttributes -list \"false true\"" -class-label 1
 *         -algorithm "weka.classifiers.meta.multisearch.DefaultSearch
 *         -sample-size 100.0 -initial-folds 2 -subsequent-folds 10
 *         -initial-test-set . -subsequent-test-set . -num-slots 1" -log-file
 *         /Users/eibe -S 1 -W weka.classifiers.functions.LinearRegression
 *         -output-debug-info -- -S 0 -C -R 1.0 -num-decimal-places 4 Make sure
 *         that you include -output-debug-info to check that all parameters you
 *         specified are actually modified! MultiSearch silently ignores
 *         parameters that it cannot find.
 *         <p>
 *         Cheers, Eibe
 */
public class WekaRunner {

	private enum RunType {
		TEST_MULTI_SEARCH_HELP, TEST_MULTI_SEARCH_RUN, TEST_WEKA_RUN, TEST_MULTI_SEARCH_APP_RUN, OPTION_EXPLORER
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RunType runType = RunType.TEST_MULTI_SEARCH_APP_RUN;

				switch (runType) {
				case TEST_MULTI_SEARCH_HELP:
					testMultiSearchHelp();
					break;
				case TEST_MULTI_SEARCH_RUN:
					testMultiSearchRun();
					break;
				case TEST_WEKA_RUN:
					testWekaRun();
					break;
				case TEST_MULTI_SEARCH_APP_RUN:
					testMultiSearchAppRun();
					break;
				case OPTION_EXPLORER:
					exploreOptions();
				default:
					break;
				}

				System.exit(0);
			}

			private void exploreOptions() {
				Enumeration options = new DefaultSearch().listOptions();
				int optionIndex = 1;
				while ( options.hasMoreElements() ) {
					Option option = (Option) options.nextElement();
					System.out.println("---------------\n+Option:"
							+ optionIndex++ + "\n---------------");
					System.out.println("Name:" + option.name());
					System.out.println("Description:" + option.name());
					System.out.println("NumArguments:" + option.numArguments());
					System.out.println("Synopsis:" + option.synopsis());
				}
			}

			/**
			 * This is an example method for showing how the multiclass package
			 * can be called from a java method and run to determine the best
			 * hyperparameters for a J48 decision tree
			 */
			private void testMultiSearchAppRun() {
				List<String> parameterList = new ArrayList<>();

				parameterList.add("-E");
				parameterList.add("MCC");
				parameterList.add("-num-decimal-places");
				parameterList.add("4");
				parameterList.add("-search");
				parameterList.add(
						"weka.core.setupgenerator.MathParameter -property confidenceFactor -min -8 -max -2 -step 0.1 -base 2.0 -expression pow(BASE,I)");
				parameterList.add("-search");
				parameterList.add(
						"weka.core.setupgenerator.ListParameter -property minNumObj -list \"0 1 2 3 4\"");
				parameterList.add("-search");
				parameterList.add(
						"weka.core.setupgenerator.ListParameter -property unpruned -list \"true false\"");
				parameterList.add("-t");

				// Change next line as needed to point to data for analysis
				parameterList.add("src/main/resources/weka/iris.arff");
				parameterList.add("-class-label");
				parameterList.add("1");
				parameterList.add("-algorithm");
				parameterList.add(
						"weka.classifiers.meta.multisearch.DefaultSearch -sample-size 100.0 -initial-folds 10 -num-slots 1 -D");
				parameterList.add("-log-file");

				// Change next line as needed to point to directory containing
				// Weka
				parameterList.add("C:/Program Files/Weka-3-8");
				parameterList.add("-S");
				parameterList.add("1");
				parameterList.add("-W");
				parameterList.add("weka.classifiers.trees.J48");
				// parameterList.add("--");
				// parameterList.add("-C");
				// parameterList.add(".0001");
				// parameterList.add("-M");
				// parameterList.add("2");
				// parameterList.add("-output-debug-info");

				// parameterList.add("weka.classifiers.functions.Logistic");
				// parameterList.add("--");
				// parameterList.add("-R");
				// parameterList.add(".0001");
				// parameterList.add("-M");
				// parameterList.add("-1");
				// parameterList.add("-num-decimal-places");
				// parameterList.add("4");
				// parameterList.add("-output-debug-info");
				String[] parameterArray = parameterList.toArray(new String[0]);

				MultiSearch.main(parameterArray);
			}

			private void testMultiSearchHelp() {
				Process p; // Process tracks one external native process

				List<String> parameterList = new ArrayList<>();

				// Change next line as needed to point to java bin file
				parameterList
						.add("\"C:/Program Files/Java/jdk1.8.0_191/bin/java\"");
				parameterList.add("-Xmx2048m");
				parameterList.add("-classpath");

				// Change next line as needed to point to weka.jar file
				parameterList.add(
						"\"%CLASSPATH%;C:/Program Files/Weka-3-8/weka.jar\"");
				parameterList.add("weka.Run");
				parameterList.add("MultiSearch");
				parameterList.add("-h");

				ProcessBuilder builder = new ProcessBuilder(parameterList);
				builder.redirectErrorStream(true);

				try {
					p = builder.start();
					// getInputStream gives an Input stream connected to
					// the process p's standard output. Just use it to make
					// a BufferedReader to readLine() what the program writes
					// out.
					InputStream processInstream = p.getInputStream();
					BufferedReader is = new BufferedReader(
							new InputStreamReader(processInstream));

					String line;
					while ( (line = is.readLine()) != null ) {
						System.out.println(line);
					}
					is.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void testMultiSearchRun() {
				Process p; // Process tracks one external native process

				List<String> parameterList = new ArrayList<>();

				// Change next line as needed to point to directly containing
				// java.exe
				parameterList
						.add("\"C:/Program Files/Java/jdk1.8.0_191/bin/java\"");
				parameterList.add("-Xmx2048m");
				parameterList.add("-classpath");

				// Change next line as needed to point to weka.jar file
				parameterList.add(
						"\"%CLASSPATH%;C:/Program Files/Weka-3-8/weka.jar\"");
				parameterList.add("weka.Run");
				parameterList.add("MultiSearch");
				parameterList.add("-E");
				parameterList.add("CC");
				parameterList.add("-search");
				parameterList.add(
						"\"weka.core.setupgenerator.MathParameter -property ridge -min -10.0 -max 5.0 -step 1.0 -base 10.0 -expression pow(BASE,I)\"");
				parameterList.add("-t");

				// Change next line as needed to point to data for analysis
				parameterList.add(
						"C:/Users/gtony/OneDrive/Documents/wekaData/iris.arff");
				parameterList.add("-class-label");
				parameterList.add("1");
				parameterList.add("-algorithm");
				parameterList.add(
						"\"weka.classifiers.meta.multisearch.RandomSearch -sample-size 100.0 -num-folds 2 -test-set . -num-iterations 100 -S 1 -num-slots 1\"");
				parameterList.add("-log-file");
				parameterList.add("\"C:/Program Files/Weka-3-8\"");
				parameterList.add("-S");
				parameterList.add("1");
				parameterList.add("-W");
				parameterList.add("weka.classifiers.trees.J48");
				parameterList.add("--");
				// parameterList.add("-C");
				// parameterList.add("0.25");
				// parameterList.add("-M");
				// parameterList.add("2");

				ProcessBuilder builder = new ProcessBuilder(parameterList);
				builder.redirectErrorStream(true);

				try {
					p = builder.start();
					// getInputStream gives an Input stream connected to
					// the process p's standard output. Just use it to make
					// a BufferedReader to readLine() what the program writes
					// out.
					InputStream processInstream = p.getInputStream();
					BufferedReader is = new BufferedReader(
							new InputStreamReader(processInstream));

					String line;
					while ( (line = is.readLine()) != null ) {
						System.out.println(line);
					}
					is.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void testWekaRun() {
				List<String> parameterList = new ArrayList<>();
				// parameterList.add("J48");
				parameterList.add("-t");
				parameterList.add(
						"C:/Users/gtony/OneDrive/Documents/wekaData/iris.arff");
				// parameterList.add("--");
				parameterList.add("-C");
				parameterList.add("0.25");
				parameterList.add("-M");
				parameterList.add("2");
				String[] parameterArray = parameterList.toArray(new String[0]);

				J48.main(parameterArray);
			}
		});
	}
}
