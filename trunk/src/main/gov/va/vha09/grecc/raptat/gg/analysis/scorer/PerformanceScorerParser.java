/** */
package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ConfigurationFileParser;

/**
 * Parser to read in the arguments for running the PerformanceScorer class from the command line.
 * The arguments can be read in directly via the command line, or they can be read in from a
 * '.config' file by putting the command option -p followed by the full path to the '.config' file.
 * When using a '.config' file, each parameter should be placed on a separate line, with the name
 * (long or short) of the command line argument followed by the argument itself separated by one or
 * more whitespace characters.
 *
 * <p>
 * Providing the single argument -h or --help on the command line will result in a print out of the
 * possible arguments on the command line.
 *
 * @author Glenn Gobbel
 */
public class PerformanceScorerParser {

  private final Options generalOptions = new Options();

  private final Options helpOptions = new Options();

  private final Options propertiesOptions = new Options();


  public PerformanceScorerParser() {
    Option textOption = Option.builder("t").required(true).hasArg(true).longOpt("textDir")
        .desc("Full path to directory containing text files from which the annotations originated")
        .build();
    Option raptatOption = Option.builder("r").required(true).hasArg(true).longOpt("raptatDir")
        .desc("Full path to directory containing xml files created by RapTAT or another annotator")
        .build();
    Option referenceOption = Option.builder("f").required(true).hasArg(true).longOpt("referenceDir")
        .desc("Full path to directory containing xml files that are the reference annotations")
        .build();
    Option schemaOption = Option.builder("s").required(true).hasArg(true).longOpt("schemaFilePath")
        .desc("Full path to annotation schema file created by eHost or Knowtator").build();
    Option conceptsFile =
        Option.builder("c").required(false).hasArg(true).longOpt("conceptsFilePath")
            .desc("Full path to text file listing the concepts to be scored").build();
    Option appOption = Option.builder("a").required(false).hasArg(true).longOpt("annotationApp")
        .desc(
            "Application format for the annotations in the xml file (EHost or Knowtator; defaults to eHost)")
        .build();
    Option removeUnmatchedPhrases = Option.builder("u").required(false).hasArg(true)
        .longOpt("removeUnmatched")
        .desc(
            "If true, exclude any phrases where the offsets do not match the phrase that was annotated")
        .build();

    this.generalOptions.addOption(textOption).addOption(raptatOption).addOption(referenceOption)
        .addOption(schemaOption).addOption(conceptsFile).addOption(appOption)
        .addOption(removeUnmatchedPhrases);

    Option helpOption = Option.builder("h").required(false).longOpt("help")
        .desc("Print help including all command line options").build();
    this.helpOptions.addOption(helpOption);

    Option propertiesParseOption =
        Option.builder("p").required(false).hasArg(true).longOpt("propertiesFile")
            .desc("Path to file containing the options for running the PerformanceScorer class")
            .build();
    this.propertiesOptions.addOption(propertiesParseOption);
  }


  /** @return the generalOptions */
  public Options getGeneralOptions() {
    return this.generalOptions;
  }


  /** @return the helpOptions */
  public Options getHelpOptions() {
    return this.helpOptions;
  }


  /** @return the propertiesOptions */
  public Options getPropertiesOptions() {
    return this.propertiesOptions;
  }


  /**
   * Sep 21, 2017
   *
   * @param args
   */
  public ScoringParameterObject parseCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    String textDirectoryPath = null;
    String raptatDirectoryPath = null;
    String referenceDirectoryPath = null;
    String schemaFilePath = null;
    String conceptsFilePath = null;
    AnnotationApp annotationApp = null;
    String removeUnmatched = "true";

    try {
      cmd = parser.parse(this.helpOptions, args, true);
      if (cmd.hasOption('h') || cmd.hasOption("help")) {
        help();
        System.exit(0);
      }

      cmd = parser.parse(this.propertiesOptions, args, true);
      if (cmd.hasOption('p') || cmd.hasOption("propertiesFile")) {
        args = readPropertiesFile(cmd.getOptionValue('p'));
      }

      cmd = parser.parse(this.generalOptions, args);
      if (cmd.hasOption('t') || cmd.hasOption("textDir")) {
        textDirectoryPath = cmd.getOptionValue('t');
      }

      if (cmd.hasOption('r') || cmd.hasOption("raptatDir")) {
        raptatDirectoryPath = cmd.getOptionValue('r');
      }

      if (cmd.hasOption('f') || cmd.hasOption("referenceDir")) {
        referenceDirectoryPath = cmd.getOptionValue('f');
      }

      if (cmd.hasOption('s') || cmd.hasOption("schemaFilePath")) {
        schemaFilePath = cmd.getOptionValue('s');
      }

      if (cmd.hasOption('c') || cmd.hasOption("conceptsFilePath")) {
        conceptsFilePath = cmd.getOptionValue('c');
      }

      if (cmd.hasOption('a') || cmd.hasOption("annotationApp")) {
        annotationApp = AnnotationApp.valueOf(cmd.getOptionValue('a').toUpperCase());
      }

      if (cmd.hasOption('u') || cmd.hasOption("removedUnmatched")) {
        removeUnmatched = cmd.getOptionValue('u');
      }

    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    ScoringParameterObject spObject =
        new ScoringParameterObject(textDirectoryPath, raptatDirectoryPath, referenceDirectoryPath,
            schemaFilePath, conceptsFilePath, annotationApp, removeUnmatched);

    return spObject;
  }


  private void help() {
    String header =
        "Compare annotations generated by RapTAT or another annotator to a reference standard\n\n";
    String footer = "";

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar PerformanceScorer.jar", header, this.generalOptions, footer,
        true);
  }


  /**
   * Sep 22, 2017
   *
   * @param pathToFile
   * @return
   */
  private String[] readPropertiesFile(String pathToFile) {
    ConfigurationFileParser fileParser = new ConfigurationFileParser(this.generalOptions);
    String[] args = fileParser.parseFile(pathToFile);
    return args;
  }
}
