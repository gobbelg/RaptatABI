/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.annotation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
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
public class AnnotatorParser {

  private final Options generalOptions = new Options();

  private final Options helpOptions = new Options();

  private final Options propertiesOptions = new Options();


  public AnnotatorParser() {
    Option solutionOption =
        Option.builder("s").required(true).hasArg(true).longOpt("solutionFilePath")
            .desc("Full path to solution file to be used for annotating text files").build();

    Option textOption = Option.builder("t").required(false).hasArg(true).longOpt("textDir")
        .desc("Full path to directory containing text files from which the annotations originated")
        .build();
    Option batchOption = Option.builder("b").required(false).hasArg(true).longOpt("batchFilePath")
        .desc("Full path to file that contains list of text directories to be annotated").build();
    OptionGroup textVsBatchOption = new OptionGroup();
    textVsBatchOption.setRequired(true);
    textVsBatchOption.addOption(textOption).addOption(batchOption);

    Option exportOption = Option.builder("e").required(true).hasArg(true).longOpt("exportDir").desc(
        "Full  path to directory that will contain the xml files that store the annotations, 1 xml file per text file")
        .build();
    Option conceptsFile =
        Option.builder("c").required(false).hasArg(true).longOpt("conceptsFilePath")
            .desc("Full path to text file listing the concepts to be scored").build();
    Option appOption = Option.builder("a").required(false).hasArg(true).longOpt("annotationApp")
        .desc(
            "Application format for the annotations in the xml file (EHost or Knowtator; defaults to eHost)")
        .build();

    this.generalOptions.addOptionGroup(textVsBatchOption).addOption(solutionOption)
        .addOption(exportOption).addOption(conceptsFile).addOption(appOption);

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
  public AnnotatorParameterObject parseCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    String solutionFilePath = null;
    String textDirectoryPath = null;
    String exportDirectoryPath = null;
    String conceptsFilePath = null;
    AnnotationApp annotationApp = null;
    boolean isBatch = false;

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
      if (cmd.hasOption('s') || cmd.hasOption("solutionFilePath")) {
        solutionFilePath = cmd.getOptionValue('s');
      }

      if (cmd.hasOption('t') || cmd.hasOption("textDir")) {
        textDirectoryPath = cmd.getOptionValue('t');
      }

      if (cmd.hasOption('b') || cmd.hasOption("batchFilePath")) {
        textDirectoryPath = cmd.getOptionValue('b');
        isBatch = true;
      }

      if (cmd.hasOption('e') || cmd.hasOption("exportFilePath")) {
        exportDirectoryPath = cmd.getOptionValue('e');
      }

      if (cmd.hasOption('c') || cmd.hasOption("conceptsFilePath")) {
        conceptsFilePath = cmd.getOptionValue('c');
      }

      if (cmd.hasOption('a') || cmd.hasOption("annotationApp")) {
        annotationApp =
            AnnotationApp.valueOf(cmd.getOptionValue('a').replaceAll("\"", "").toUpperCase());
      }

    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    AnnotatorParameterObject annotationParameters = new AnnotatorParameterObject(solutionFilePath,
        textDirectoryPath, isBatch, exportDirectoryPath, conceptsFilePath, annotationApp);

    return annotationParameters;
  }


  private void help() {
    String header = "Annotate a set of documents within a directory or set of directories\n\n";
    String footer = "";

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar AnnotatorParser.jar", header, this.generalOptions, footer, true);
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
