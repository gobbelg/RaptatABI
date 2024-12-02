/** */
package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.Options;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorerParser;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.ScoringParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/** @author Glenn Gobbel */
public class ConfigurationFileParser {
  private static final String COMMENT_MARKER_STRING = "#";


  private Options parserOptions;

  public ConfigurationFileParser(Options parserOptions) {
    this();
    this.parserOptions = parserOptions;
  }


  /** */
  private ConfigurationFileParser() {}


  /**
   * Sep 22, 2017
   *
   * @param string
   * @return
   */
  public String[] parseFile(String pathToFile) {
    InputStream is = null;
    try {
      is = new FileInputStream(pathToFile);
    } catch (FileNotFoundException e) {
      System.err
          .println("File:" + pathToFile + " does not seem to exist. " + e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(-1);
    }
    return parseStream(is);
  }


  /**
   * Sep 22, 2017
   *
   * @param string
   * @return
   */
  protected String[] parseStream(InputStream is) {
    BufferedReader br = null;
    List<String> argumentList = new ArrayList<>();

    try {
      br = new BufferedReader(new InputStreamReader(is));
      String currentLine = "";
      String argument = "";
      String[] tokens;

      while (currentLine != null) {
        currentLine = br.readLine();

        /*
         * If the line is not the end of the file, not empty, and doesn't start with a comment
         * marker, add it to the existing argument string. If not, process the existing argument
         * string and reset it to an empty string.
         */
        if (currentLine != null && !currentLine.isEmpty()
            && !currentLine.startsWith(ConfigurationFileParser.COMMENT_MARKER_STRING)) {
          argument += currentLine.trim();
        } else if (argument.length() > 0) {
          /*
           * Detect and handle quotes with multiple sets of parameters
           */
          if (argument.indexOf(" \"") > -1) {
            tokens = extractQuotedParameters(argument);
          } else {
            tokens = argument.split("\\s+");
          }
          String[] optionAndValues = convertToOptions(tokens);
          for (String token : optionAndValues) {
            argumentList.add(token);
          }
          argument = "";
        }
      }
    } catch (IOException ex) {
      System.err.println("Unable to read configuration file. " + ex.getLocalizedMessage());
      ex.printStackTrace();
      System.exit(-1);
    }

    String[] args = argumentList.toArray(new String[] {});
    return args;
  }


  /**
   * Sep 22, 2017
   *
   * @param tokens
   * @return
   */
  private String[] convertToOptions(String[] tokens) {
    String option = tokens[0];
    option = option.replaceAll("^-*", "");
    if (this.parserOptions.hasLongOption(option)) {
      option = "-" + option;
    }
    option = "-" + option;
    tokens[0] = option;
    return tokens;
  }


  private String[] extractQuotedParameters(String line) {
    String[] resultArray = new String[2];
    ArrayDeque<String> parameters = new ArrayDeque<>();

    /* Add the command itself */
    int firstHyphenIndex = line.indexOf('-');
    int firstSpaceIndex = line.indexOf(' ');
    resultArray[0] = line.substring(firstHyphenIndex, firstSpaceIndex);

    int openQuoteIndex = line.length();
    int closeQuoteIndex = line.lastIndexOf("\"", openQuoteIndex);
    openQuoteIndex = line.lastIndexOf(" \"", closeQuoteIndex);
    parameters.push("\"");
    parameters.push(line.substring(openQuoteIndex + 2, closeQuoteIndex));
    parameters.push("\"");
    while ((closeQuoteIndex = line.lastIndexOf("\" ", openQuoteIndex)) > -1) {
      openQuoteIndex = line.lastIndexOf(" \"", closeQuoteIndex);
      parameters.push("\" ");
      parameters.push(line.substring(openQuoteIndex + 2, closeQuoteIndex));
      parameters.push("\"");
    }
    StringBuilder parameter = new StringBuilder();
    while (!parameters.isEmpty()) {
      parameter.append(parameters.pop());
    }
    resultArray[1] = parameter.toString();
    return resultArray;
  }


  /**
   * Obtains set of configuration parameters for running the main method of a class Sep 22, 2017
   *
   * @param args
   */
  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        String[] fileArguments;
        PerformanceScorerParser psp = new PerformanceScorerParser();
        ConfigurationFileParser fileParser = new ConfigurationFileParser(psp.getGeneralOptions());
        String defaultFilePath = "/src/main/resources/performanceScorer_test_170922.config";

        if (args == null || args.length == 0) {
          InputStream is = ConfigurationFileParser.class.getResourceAsStream(defaultFilePath);
          fileArguments = fileParser.parseStream(is);
        } else {
          fileArguments = fileParser.parseFile(args[0]);
        }

        ScoringParameterObject parameters = psp.parseCommandLine(fileArguments);
        System.out.println(parameters.toString());

        System.exit(-1);
      }
    });
  }
}
