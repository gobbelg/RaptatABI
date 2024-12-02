/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.crossvalidation;

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
public class CVParser {

  /*
   * TRAINING_DATA_FILEPATH is the full path to the '.csv' file containing the groups used for
   * cross-validation, the text files, and the knowtator files. The first line should be a header
   * line giving the names of the columns. Any names can be used as it is ignored by the system. The
   * first column should contain the group number, the second column the name of the text file, and
   * the third column the name of the annotation file corresponding to the xml file.
   */
  private static final String TRAINING_DATA_FILEPATH_SHORTOPT = "r";
  private static final String TRAINING_DATA_FILEPATH_LONGOPT = "trainingDataFilePath";

  /*
   * SCHEMA_FILEPATH is the full path to the schema created in eHOST or Knowtator. In eHOST, it is
   * typically called 'projectschema.xml' and is in the workspace of eHOST where the files were
   * annotated.
   */
  private static final String SCHEMA_FILEPATH_SHORTOPT = "s";
  private static final String SCHEMA_FILEPATH_LONGOPT = "schemaFilePath";

  /*
   * TESTING_DATA_FILEPATH is the full path the directory of .xml files containing the annotations
   * used for testing during cross-validation. If this is not specified, the data used for training
   * is used for both training and testing
   *
   */
  private static final String TESTING_DIRECTORY_FILEPATH_SHORTOPT = "q";
  private static final String TESTING_DIRECTORY_FILEPATH_LONGOPT = "testingDirectoryFilePath";

  /*
   * TRAINING_GROUP_SIZE is the number of groups used for training during cross-validation. All
   * other groups will be used for testing. If a number less than 1 is supplied, a leave-one-out
   * cross-validation is used, so group size is one less than the total number of groups.
   */
  private static final String TRAINING_GROUP_SIZE_SHORTOPT = "u";

  private static final String TRAINING_GROUP_SIZE_LONGOPT = "trainingGroupSize";

  /*
   * ANNOTATION_APP specifies the application used for annotation. Either eHOST or Knowtator can be
   * used to create the annotations used for annotation. Because the resulting files are different,
   * the system needs to know which one created the annotations. That information is specified here
   * as a string as either 'EHOST' or 'KNOWTATOR'. Use of lower or upper case is optional.
   */
  private static final String ANNOTATION_APP_SHORTOPT = "a";

  private static final String ANNOTATION_APP_LONGOPT = "annotationApp";

  /*
   * TRAINING_CONCEPTS_FILEPATH is the full file path to a tab-delimited text file of concepts to be
   * used in training. Not all the concepts in the schema need to be included, but this list cannot
   * accept concepts that are not in the schema. Each concept is specified on a separate line, and
   * the first line is a title for the list and is ignored.
   */
  private static final String TRAINING_CONCEPTS_FILEPATH_SHORTOPT = "c";
  private static final String TRAINING_CONCEPTS_FILEPATH_LONGOPT = "trainingConceptsFilePath";

  /*
   * SCORING_CONCEPTS_FILEPATH is the full file path to a tab-delimited text file of concepts to be
   * used in scoring. This may be different than the training concepts mentioned above in that
   * dictionary concepts may not be imported from annotation files. The training concepts only
   * serves as a filter of training annoitations imported from an .xml file. Not all the concepts in
   * the schema need to be included, but this list cannot accept concepts that are not in the
   * schema. Each concept is specified on a separate line, and the first line is a title for the
   * list and is ignored.
   */
  private static final String SCORING_CONCEPTS_FILEPATH_SHORTOPT = "l";

  private static final String SCORING_CONCEPTS_FILEPATH_LONGOPT = "scoringConceptsFilePath";

  /*
   * DICTIONARY_FILEPATH is the full path to a dictionary of concept, promoter, and inhibiter
   * phrases for concepts. The file also specifies the distance forward and backward of influence of
   * promoters and inhibitors, whether concept phrases are influenced by inhibitors and promoters,
   * and whether any concept in text that matches one in the dictionary should override
   * probabilistically identified concepts. The first line is a header line that names the columns.
   * Any names can be used as it is ignored by the system.
   */
  private static final String DICTIONARY_FILEPATH_SHORTOPT = "d";

  private static final String DICTIONARY_FILEPATH_LONGOPT = "dictionaryFilePath";

  /*
   * SENTENCE_FILTER filters all the text sentences and uses only those that match the specified
   * sentence filter.
   */
  private static final String SENTENCE_FILTER_SHORTOPT = "i";

  private static final String SENTENCE_FILTER_LONGOPT = "sentenceFilters";
  /*
   * START_INDEX and STOP_INDEX values are indices that specify the options to use when running
   * cross-validation. The values run go from START_INDEX (inclusive) to STOP_INDEX (exclusive), so
   * a setting of 56 and 58 for these two values would run options with values of 56 and 57 but not
   * 58. They determine whether to 1) invert the token sequence used for training and testing, 2)
   * use of stemming, 3) use of parts-of-speech, 4) whether to remove stop words, 4) type of
   * smoothing to use for phrase not in the training set, and the maximum tokens a phrase can
   * contain. The index value is converted to binary form to determine option settings. See
   * setNextOptions() method in the PhraseIDCrossValidationAnalyzer and the
   * PhraseIDBootstrapAnalyzer for further information about this. Setting START_INDEX to 56 and
   * STOP_INDEX to 57 will run only the 56 option setting, which corresponds to no inversion of
   * token sequence, no stemming, no use of part of speech tagging, but removal of stop works. The
   * type of smoothing is PhraseIDSmoothingMethod.XTREME_MOD, and the maximum token number for a
   * phrase is 7. These are the values that have been found empirically to work well.
   */
  private static final String START_INDEX_SHORTOPT = "x";
  private static final String START_INDEX_LONGOPT = "startIndex";
  private static final String STOP_INDEX_SHORTOPT = "j";
  private static final String STOP_INDEX_LONGOPT = "stopIndex";

  /*
   * Attribute Tagger Paths are the full path to AttributeTaggerSolution objects stored on disk.
   * AttributeTaggerSolutions are objects that use the Cue-Scope model combined with conditional
   * random field modeling to determine attributes, such as assertion, or concept mentions. They are
   * stored on disk with the '.atsn' suffix.
   */
  private static final String ATTRIBUTE_TAGGER_PATHS_SHORTOPT = "b";
  private static final String ATTRIBUTE_TAGGER_PATHS_LONGOPT = "attributeTaggers";

  /*
   * Attribute Conflict Managers specify the fully-qualified names of
   * AttributeConflictManagerObjects. Multiple ones should be separate by a '^' character.
   * AttributeConflictManager objects adjudicate disagreements when two different methods assign an
   * attribute value to a mention and the values are in conflict. An archetype of this would be an
   * AssertionConflictManager which adjudicates between assertion values assigned based on context
   * and based on a CRF-based Cue-Scope model.
   */
  private static final String ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT = "g";
  private static final String ATTRIBUTE_CONFLICT_MANAGERS_LONGOPT = "attributeConflictManagers";

  /*
   * The Token Sequence Finder method is specified as a string indicating the standard probabilistic
   * method of RapTAT or an alternative including CRF, NAIVEBAYES, NONNAIVEBAYES, OR BAGOFWORDS.
   * 'PROBABILISTIC' is the default; other methods are incomplete.
   */
  private static final String TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT = "m";
  private static final String TOKEN_SEQUENCE_FINDER_METHOD_LONGOPT = "tokenSequenceFinderMethod";

  /*
   * SECTION_MATCH descriptors provide the names and regular expressions for identifying particular
   * types of sections of text. These are combined with implementations of the
   * SentenceContextAttributeAnalyzer so that attributes can be assigned to concepts based on the
   * sections they are found within. If a section match is detected, all sentences are labeled with
   * the name of the section matcher, which is stored in the sentenceAssociatedConcepts field. An
   * example is the presence of "allergy" sections within medical documents. Using a
   * SentenceContextAttributeAnalyzer, any sentences in the "allergy" section will be detected, and
   * all mentions of a concept may be labeled with an "assertionstatus" of "negative."
   *
   * NOTE: Inclusion of a SECTION_MATCHER without at least one SentenceContextAttributeAnalyzer in
   * the properties will result in an error.
   */
  private static final String SECTION_MATCH_TEXT_SHORTOPT = "v";
  private static final String SECTION_MATCH_TEXT_LONGOPT = "sectionMatchText";
  private static final String SECTION_MATCH_FILE_SHORTOPT = "w";
  private static final String SECTION_MATCH_FILE_LONGOPT = "sectionMatchFilePath";

  /*
   * SENTENCE_MATCH descriptors provide the names and regular expressions for identifying particular
   * types of sentence of text. They are identical to SECTION_MATCHER descriptors except that they
   * only label a single sentence rather than all sentences within a section.
   *
   * NOTE: Inclusion of a SENTENCE_MATCHER without at least one SentenceContextAttributeAnalyzer in
   * the properties will result in an error.
   */
  private static final String SENTENCE_MATCH_TEXT_SHORTOPT = "y";
  private static final String SENTENCE_MATCH_TEXT_LONGOPT = "sentenceMatchText";
  private static final String SENTENCE_MATCH_FILE_SHORTOPT = "z";
  private static final String SENTENCE_MATCH_FILE_LONGOPT = "sentenceMatchFilePath";

  /*
   * SENTENCE_CONTEXT_ANALYZER descriptors provide one or more sets of elements for constructing
   * SentenceContextAttributeAnalyzer instances. The various instances are separated by as '^'
   * character, and the fields are provided as key value pairs of the form
   * fieldName01:valueOfieldName01, fieldName02:valueOfFieldName02, etc, with commas separate the
   * different fields. SentenceContextAttributeAnalyzer instances assign attributes to phrases if
   * those phrases occur in sentence in which the sentenceContextName of the field of a
   * SentenceContextAttributeAnalyzer matches any of the sentenceAssoociatedConcepts for the
   * AnnotatedPhrase representing the sentence. If it does match, the AnnotatedPhrase instance is
   * assigned an attribute with the attributeName field and the contextAssignedAttributeValue of the
   * SentenceContextAttributeAnalyzer.
   */
  private static final String SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT = "k";
  private static final String SENTENCE_CONTEXT_ANALYZER_TEXT_LONGOPT =
      "sentenceContextAnalyzerText";
  private static final String SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT = "n";
  private static final String SENTENCE_CONTEXT_ANALYZER_FILE_LONGOPT =
      "sentenceContextAnalyzerFile";

  /*
   * CONTEXT_SIFTER settings can be provided as text in a configuration file OR (not both) in a
   * separate file whose path is indicated in the configuration file. There can be multiple
   * CONTEXT_SIFTERs specified as text, and they are separated by the '^' character. The fields
   * specified for each CONTEXT_SIFTER are 'order':OrderOfProcessingContextSifters (in case there
   * are multiple separated by the '^' character),
   * 'concepts':listOfConceptsAffectedByContextSeparatedByCommas, 'context':
   * booleanExpressionOfConceptsThatMustBePresentOrAbsentFromSurroundSentences,
   * 'required_distance_reverse':
   * IntegerOfNumberOfSentencesBeforeTheOneWithAConceptThatMustCheckForContext, and
   * 'required_distance_forward':
   * IntegerOfNumberOfSentencesAfterTheOneWithAConceptThatMustCheckForContext. Note that the fields
   * themselves should only be separated by whitespace.
   */
  private static final String CONTEXT_SIFTER_TEXT_SHORTOPT = "e";
  private static final String CONTEXT_SIFTER_TEXT_LONGOPT = "contextSifterText";
  private static final String CONTEXT_SIFTER_FILE_SHORTOPT = "f";
  private static final String CONTEXT_SIFTER_FILE_LONGOPT = "contextSifterFilePath";

  /*
   * ANNOTATION_GROUP_POSTPROCESSOR descriptors describe the full path to all classes that implement
   * the AnotationGroupPostProcessor interface and that should be run as the last step during
   * annotation of an AnnotationGroup instance. The post processor instances will be run in the
   * order they are declared in the command line or properties file. Multiple descriptors should be
   * separated by the '^' character.
   */
  private static final String ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT = "t";
  private static final String ANNOTATION_GROUP_POSTPROCESSOR_LONGOPT =
      "annotationPostProcessorDescriptors";

  private static final String HELP_SHORTOPT = "h";
  private static final String HELP_LONGOPT = "help";
  /*
   * This option allows properties to be loaded from a file rather than on the command line
   */
  private static final String PROPERTIES_PARSE_SHORTOPT = "p";

  private static final String PROPERTIES_PARSE_LONGOPT = "propertiesFile";
  private final Options helpOptions = new Options();
  private final Options propertiesOptions = new Options();
  private final Options generalOptions = new Options();


  public CVParser() {
    /**
     * ********************************** Required Option Types *********************************
     */
    Option trainingDataFilePath = Option.builder(CVParser.TRAINING_DATA_FILEPATH_SHORTOPT)
        .required(true).hasArg(true).longOpt(CVParser.TRAINING_DATA_FILEPATH_LONGOPT)
        .desc(
            "Full path to tab-delimited file of the text files to be annotated and their grouping; each group"
                + "should be indicated in a column in the text file; each group is treated as a single entity "
                + "for training and testing purposes during cross-validation")
        .build();
    Option schemaFilePath = Option.builder(CVParser.SCHEMA_FILEPATH_SHORTOPT).required(false)
        .hasArg(true).longOpt(CVParser.SCHEMA_FILEPATH_LONGOPT)
        .desc("Full path to annotation schema file created by eHost or Knowtator").build();

    /**
     * *********************************** Not Required Option Types
     * **********************************
     */
    Option testingDirectoryPath = Option.builder(CVParser.TESTING_DIRECTORY_FILEPATH_SHORTOPT)
        .required(false).hasArg(true).longOpt(CVParser.TESTING_DIRECTORY_FILEPATH_LONGOPT)
        .desc(
            "Full path to directory of .xml files containing annotations used for testing during cross-validation.  "
                + "If this value is not specified, the training data files will be used for both traiining and testing.")
        .build();
    Option trainingGroupSize = Option.builder(CVParser.TRAINING_GROUP_SIZE_SHORTOPT).required(false)
        .hasArg(true).longOpt(CVParser.TRAINING_GROUP_SIZE_LONGOPT)
        .desc(
            "Provide size of the training group; if set to 0, performs 'leave-one-out' cross-validation")
        .build();
    Option annotationApp = Option.builder(CVParser.ANNOTATION_APP_SHORTOPT).required(false)
        .hasArg(true).longOpt(CVParser.ANNOTATION_APP_LONGOPT)
        .desc("Provide name of application used to create annotations (Knowtator or eHost").build();
    Option trainingConceptsFilePath = Option.builder(CVParser.TRAINING_CONCEPTS_FILEPATH_SHORTOPT)
        .required(false).hasArg(true).longOpt(CVParser.TRAINING_CONCEPTS_FILEPATH_LONGOPT)
        .desc("Full path to text file listing the concepts to be used for training").build();
    Option scoringConceptsFilePath = Option.builder(CVParser.SCORING_CONCEPTS_FILEPATH_SHORTOPT)
        .required(false).hasArg(true).longOpt(CVParser.SCORING_CONCEPTS_FILEPATH_LONGOPT)
        .desc("Full path to text file listing the concepts to be scored").build();
    Option dictionaryFilePath = Option.builder(CVParser.DICTIONARY_FILEPATH_SHORTOPT)
        .required(false).hasArg(true).longOpt(CVParser.DICTIONARY_FILEPATH_LONGOPT)
        .desc("Full path to dictionary to run during cross-validation").build();
    Option sentenceFilters = Option.builder(CVParser.SENTENCE_FILTER_SHORTOPT).required(false)
        .hasArgs().longOpt(CVParser.SENTENCE_FILTER_LONGOPT).valueSeparator('^')
        .desc("Full paths to any sentence to run during cross-validation; surround each path with "
            + "quotations, and separate multiple paths with a '^' character")
        .build();
    Option startIndex = Option.builder(CVParser.START_INDEX_SHORTOPT).required(true).hasArg(true)
        .longOpt(CVParser.START_INDEX_LONGOPT)
        .desc(
            "Starting index of nlp parameter variants to be used during cross-validation; cross-validation will be run "
                + "separately on each variant")
        .build();
    Option stopIndex = Option.builder(CVParser.STOP_INDEX_SHORTOPT).required(true).hasArg(true)
        .longOpt(CVParser.STOP_INDEX_LONGOPT)
        .desc(
            "Ending index of nlp parameter variants to be used during cross-validation; cross-validation will be run "
                + "separately on each variant")
        .build();
    Option attributeTaggerPaths = Option.builder(CVParser.ATTRIBUTE_TAGGER_PATHS_SHORTOPT)
        .required(false).hasArgs().longOpt(CVParser.ATTRIBUTE_TAGGER_PATHS_LONGOPT)
        .valueSeparator('^')
        .desc(
            "Full paths to any attribute taggers to run during cross-validation; surround each path with "
                + "quotations, and separate multiple paths with a '^' character")
        .build();
    Option attributeConflictManagers = Option.builder(CVParser.ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT)
        .required(false).hasArgs().longOpt(CVParser.ATTRIBUTE_CONFLICT_MANAGERS_LONGOPT)
        .valueSeparator('^')
        .desc(
            "Provide fully-qualified class names of attribute conflict managers for annotating attributes during "
                + "cross-validation; separate multiple manager names with a '^' character")
        .build();
    Option tokenSequenceFinderMethod = Option
        .builder(CVParser.TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT).required(false).hasArg(true)
        .longOpt(CVParser.TOKEN_SEQUENCE_FINDER_METHOD_LONGOPT)
        .desc(
            "String naming TSFinderMethod for identifying annotated sequences (default = PROBABILISTIC)")
        .build();
    Option annotationGroupPostProcessors = Option
        .builder(CVParser.ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT).required(false).hasArgs()
        .longOpt(CVParser.ANNOTATION_GROUP_POSTPROCESSOR_LONGOPT).valueSeparator('^')
        .desc(
            "Fully-qualified paths to any AnnotationGroupPostProcessor to be run after the completion of "
                + "all other steps in annotating a document - separate multiple post processors with the '^' character")
        .build();

    /**
     * *********************************** Grouped Options **********************************
     */
    Option sectionMatchTextOption = Option.builder(CVParser.SECTION_MATCH_TEXT_SHORTOPT)
        .required(false).hasArgs().longOpt(CVParser.SECTION_MATCH_TEXT_LONGOPT).valueSeparator('~')
        .desc(
            "Text describing regexes and their names separated by '~' character, each provided in the "
                + "form 'NameOfRegex':'RegexPattern'")
        .build();
    Option sectionMatchFileOption = Option.builder(CVParser.SECTION_MATCH_FILE_SHORTOPT)
        .required(false).hasArgs().longOpt(CVParser.SECTION_MATCH_FILE_LONGOPT).valueSeparator('^')
        .desc("Full path to file with regex names and patterns for section matching").build();

    Option sentenceMatchTextOption = Option.builder(CVParser.SENTENCE_MATCH_TEXT_SHORTOPT)
        .required(false).hasArgs().longOpt(CVParser.SENTENCE_MATCH_TEXT_LONGOPT).valueSeparator('~')
        .desc(
            "Text describing regexes and their names separated by '~' character, each provided in the "
                + "form 'NameOfRegex':'RegexPattern'")
        .build();
    Option sentenceMatchFileOption = Option.builder(CVParser.SENTENCE_MATCH_FILE_SHORTOPT)
        .required(false).hasArgs().longOpt(CVParser.SENTENCE_MATCH_FILE_LONGOPT).valueSeparator('^')
        .desc("Full path to file with names and patterns for sentence matching").build();

    Option sentenceAnalyzerTextOption = Option
        .builder(CVParser.SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT).required(false).hasArgs()
        .longOpt(CVParser.SENTENCE_CONTEXT_ANALYZER_TEXT_LONGOPT).valueSeparator('^')
        .desc(
            "Text describing SentenceContextAnalyzer instances to use during annotation, each one provided by "
                + "a name:value pair of the fields needed for constructing a SentenceContextAnalyzer instance, as in "
                + "attributeName:instanceAttributeName, sentenceContextName:stringRepresentingSentenceContextName, "
                + "defaultAttributeValue:stringRepresentingDefaultAttributeValue, "
                + "contextAssignedAttributeValue:stringRepresentingContextAssignedAttributeValue, "
                + "returnDefault:trueOrFalse; fields should be separated by commas and different analyzers demarcated by"
                + " the '^' character")
        .build();
    Option sentenceAnalyzerFileOption =
        Option.builder(CVParser.SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT).required(false).hasArgs()
            .longOpt(CVParser.SENTENCE_CONTEXT_ANALYZER_FILE_LONGOPT).valueSeparator('^')
            .desc("Full path to file describing the GeneralContextAnalyzer instances, one per line")
            .build();

    Option contextSifterTextOption = Option.builder(CVParser.CONTEXT_SIFTER_TEXT_SHORTOPT).hasArgs()
        .valueSeparator('^').required(false).longOpt(CVParser.CONTEXT_SIFTER_TEXT_LONGOPT)
        .desc(
            "Descriptors of any context sifters; delineate each sifter by surrounding it with quotes; "
                + "precede concepts for sifting with 'concepts:', allowable concepts with 'allowed:', and "
                + "required concepts with 'required:' without the single quotes; if there are multiple "
                + "concepts for sifting, allowable, or required concepts for a given sifter, use commas "
                + "as a separator.\n\nEXAMPLE: \"concepts:concept1,concept2 allowed: allowedconcept1, "
                + "allowedconcept2 required:requiredconcept1, requiredconcept2\"")
        .build();
    Option contextSifterFileOption = Option.builder(CVParser.CONTEXT_SIFTER_FILE_SHORTOPT).hasArgs()
        .valueSeparator('^').required(false).longOpt(CVParser.CONTEXT_SIFTER_FILE_LONGOPT)
        .desc(
            "Full path to any context sifters to run during cross-validation - in case of multiple paths, surround "
                + "with whitespace.  See contextSifterTextOption 'option=e' for format of context sifter descriptors")
        .build();

    OptionGroup sectionMatchTextVsFileOption = new OptionGroup();
    sectionMatchTextVsFileOption.setRequired(false);
    sectionMatchTextVsFileOption.addOption(sectionMatchTextOption)
        .addOption(sectionMatchFileOption);

    OptionGroup sentenceMatchTextVsFileOption = new OptionGroup();
    sentenceMatchTextVsFileOption.setRequired(false);
    sentenceMatchTextVsFileOption.addOption(sentenceMatchTextOption)
        .addOption(sentenceMatchFileOption);

    OptionGroup generalAnalyzerTextVsFileOption = new OptionGroup();
    generalAnalyzerTextVsFileOption.setRequired(false);
    generalAnalyzerTextVsFileOption.addOption(sentenceAnalyzerTextOption)
        .addOption(sentenceAnalyzerFileOption);

    OptionGroup contextSifterTextVsFileOption = new OptionGroup();
    contextSifterTextVsFileOption.setRequired(false);
    contextSifterTextVsFileOption.addOption(contextSifterTextOption)
        .addOption(contextSifterFileOption);

    /**
     * *********************************** Options (Sets of Option Types)
     * **********************************
     */
    this.generalOptions.addOption(trainingDataFilePath).addOption(schemaFilePath)
        .addOption(testingDirectoryPath).addOption(trainingGroupSize).addOption(annotationApp)
        .addOption(trainingConceptsFilePath).addOption(scoringConceptsFilePath)
        .addOption(dictionaryFilePath).addOption(sentenceFilters).addOption(startIndex)
        .addOption(stopIndex).addOption(attributeTaggerPaths).addOption(attributeConflictManagers)
        .addOption(tokenSequenceFinderMethod).addOption(annotationGroupPostProcessors)
        .addOptionGroup(sectionMatchTextVsFileOption).addOptionGroup(sentenceMatchTextVsFileOption)
        .addOptionGroup(generalAnalyzerTextVsFileOption)
        .addOptionGroup(contextSifterTextVsFileOption);

    Option helpOption =
        Option.builder(CVParser.HELP_SHORTOPT).required(false).longOpt(CVParser.HELP_LONGOPT)
            .desc("Print help including all command line options").build();
    this.helpOptions.addOption(helpOption);

    Option propertiesParseOption = Option.builder(CVParser.PROPERTIES_PARSE_SHORTOPT)
        .required(false).hasArg(true).longOpt(CVParser.PROPERTIES_PARSE_LONGOPT)
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
  public CVParameterObject parseCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    String trainingDataFilePath = null;
    String testingDirectoryFilePath = null;
    String schemaFilePath = null;
    String trainingGroupSizeParameter = null;
    AnnotationApp annotationApp = null;
    String trainingConceptsFilePath = null;
    String scoringConceptsFilePath = null;
    String dictionaryFilePath = null;
    String[] sentenceFilters = null;
    String startIndexInteger = null;
    String stopIndexInteger = null;
    String[] attributeTaggerPaths = null;
    String[] attributeConflictManagers = null;
    String tokenSequenceFinderMethod = "Probabilistic";
    String[] annotationGroupPostProcessors = null;
    String[] sectionMatchers = null;
    String[] sentenceMatchers = null;
    String[] contextSifters = null;
    String[] sentenceContextAnalyzers = null;

    boolean isSectionMatchFile = false;
    boolean isSentenceMatchFile = false;
    boolean isContextSifterFile = false;
    boolean isSentenceContextAnalyzerFile = false;

    try {
      cmd = parser.parse(this.helpOptions, args, true);
      if (cmd.hasOption(CVParser.HELP_SHORTOPT) || cmd.hasOption(CVParser.HELP_LONGOPT)) {
        help();
        System.exit(0);
      }

      cmd = parser.parse(this.propertiesOptions, args, true);
      if (cmd.hasOption(CVParser.PROPERTIES_PARSE_SHORTOPT)
          || cmd.hasOption(CVParser.PROPERTIES_PARSE_LONGOPT)) {
        args = readPropertiesFile(cmd.getOptionValue(CVParser.PROPERTIES_PARSE_SHORTOPT));
      }

      cmd = parser.parse(this.generalOptions, args);
      if (cmd.hasOption(CVParser.TRAINING_DATA_FILEPATH_SHORTOPT)
          || cmd.hasOption(CVParser.TRAINING_DATA_FILEPATH_LONGOPT)) {
        trainingDataFilePath = cmd.getOptionValue(CVParser.TRAINING_DATA_FILEPATH_SHORTOPT);
      }

      cmd = parser.parse(this.generalOptions, args);
      if (cmd.hasOption(CVParser.TESTING_DIRECTORY_FILEPATH_SHORTOPT)
          || cmd.hasOption(CVParser.TESTING_DIRECTORY_FILEPATH_LONGOPT)) {
        testingDirectoryFilePath = cmd.getOptionValue(CVParser.TESTING_DIRECTORY_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SCHEMA_FILEPATH_SHORTOPT)
          || cmd.hasOption(CVParser.SCHEMA_FILEPATH_LONGOPT)) {
        schemaFilePath = cmd.getOptionValue(CVParser.SCHEMA_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.TRAINING_GROUP_SIZE_SHORTOPT)
          || cmd.hasOption(CVParser.TRAINING_GROUP_SIZE_LONGOPT)) {
        trainingGroupSizeParameter = cmd.getOptionValue(CVParser.TRAINING_GROUP_SIZE_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.ANNOTATION_APP_SHORTOPT)
          || cmd.hasOption(CVParser.ANNOTATION_APP_LONGOPT)) {
        annotationApp = AnnotationApp.valueOf(cmd.getOptionValue(CVParser.ANNOTATION_APP_SHORTOPT)
            .replaceAll("\"", "").toUpperCase());
      }

      if (cmd.hasOption(CVParser.TRAINING_CONCEPTS_FILEPATH_SHORTOPT)
          || cmd.hasOption(CVParser.TRAINING_CONCEPTS_FILEPATH_LONGOPT)) {
        trainingConceptsFilePath = cmd.getOptionValue(CVParser.TRAINING_CONCEPTS_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SCORING_CONCEPTS_FILEPATH_SHORTOPT)
          || cmd.hasOption(CVParser.SCORING_CONCEPTS_FILEPATH_LONGOPT)) {
        scoringConceptsFilePath = cmd.getOptionValue(CVParser.SCORING_CONCEPTS_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.DICTIONARY_FILEPATH_SHORTOPT)
          || cmd.hasOption(CVParser.DICTIONARY_FILEPATH_LONGOPT)) {
        dictionaryFilePath = cmd.getOptionValue(CVParser.DICTIONARY_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SENTENCE_FILTER_SHORTOPT)
          || cmd.hasOption(CVParser.SENTENCE_FILTER_LONGOPT)) {
        sentenceFilters = cmd.getOptionValues(CVParser.SENTENCE_FILTER_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.START_INDEX_SHORTOPT)
          || cmd.hasOption(CVParser.START_INDEX_LONGOPT)) {
        startIndexInteger = cmd.getOptionValue(CVParser.START_INDEX_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.STOP_INDEX_SHORTOPT)
          || cmd.hasOption(CVParser.STOP_INDEX_LONGOPT)) {
        stopIndexInteger = cmd.getOptionValue(CVParser.STOP_INDEX_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.ATTRIBUTE_TAGGER_PATHS_SHORTOPT)
          || cmd.hasOption(CVParser.ATTRIBUTE_TAGGER_PATHS_LONGOPT)) {
        attributeTaggerPaths = cmd.getOptionValues(CVParser.ATTRIBUTE_TAGGER_PATHS_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT)
          || cmd.hasOption(CVParser.ATTRIBUTE_CONFLICT_MANAGERS_LONGOPT)) {
        attributeConflictManagers =
            cmd.getOptionValues(CVParser.ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT)
          || cmd.hasOption(CVParser.TOKEN_SEQUENCE_FINDER_METHOD_LONGOPT)) {
        tokenSequenceFinderMethod =
            cmd.getOptionValue(CVParser.TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT)
          || cmd.hasOption(CVParser.ANNOTATION_GROUP_POSTPROCESSOR_LONGOPT)) {
        annotationGroupPostProcessors =
            cmd.getOptionValues(CVParser.ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT)
          || cmd.hasOption(CVParser.SENTENCE_CONTEXT_ANALYZER_TEXT_LONGOPT)) {
        sentenceContextAnalyzers =
            cmd.getOptionValues(CVParser.SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT)
          || cmd.hasOption(CVParser.SENTENCE_CONTEXT_ANALYZER_FILE_LONGOPT)) {
        sentenceContextAnalyzers =
            cmd.getOptionValues(CVParser.SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT);
        isSentenceContextAnalyzerFile = true;
      }

      if (cmd.hasOption(CVParser.SECTION_MATCH_TEXT_SHORTOPT)
          || cmd.hasOption(CVParser.SECTION_MATCH_TEXT_LONGOPT)) {
        sectionMatchers = cmd.getOptionValues(CVParser.SECTION_MATCH_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SECTION_MATCH_FILE_SHORTOPT)
          || cmd.hasOption(CVParser.SECTION_MATCH_FILE_LONGOPT)) {
        sectionMatchers = cmd.getOptionValues(CVParser.SECTION_MATCH_FILE_SHORTOPT);
        isSectionMatchFile = true;
      }

      if (cmd.hasOption(CVParser.SENTENCE_MATCH_TEXT_SHORTOPT)
          || cmd.hasOption(CVParser.SENTENCE_MATCH_TEXT_LONGOPT)) {
        sentenceMatchers = cmd.getOptionValues(CVParser.SENTENCE_MATCH_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.SENTENCE_MATCH_FILE_SHORTOPT)
          || cmd.hasOption(CVParser.SENTENCE_MATCH_FILE_LONGOPT)) {
        sentenceMatchers = cmd.getOptionValues(CVParser.SENTENCE_MATCH_FILE_SHORTOPT);
        isSentenceMatchFile = true;
      }

      if (cmd.hasOption(CVParser.CONTEXT_SIFTER_TEXT_SHORTOPT)
          || cmd.hasOption(CVParser.CONTEXT_SIFTER_TEXT_LONGOPT)) {
        contextSifters = cmd.getOptionValues(CVParser.CONTEXT_SIFTER_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(CVParser.CONTEXT_SIFTER_FILE_SHORTOPT)
          || cmd.hasOption(CVParser.CONTEXT_SIFTER_FILE_LONGOPT)) {
        contextSifters = cmd.getOptionValues(CVParser.CONTEXT_SIFTER_FILE_SHORTOPT);
        isContextSifterFile = true;
      }

    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    return new CVParameterObject(trainingDataFilePath, testingDirectoryFilePath, schemaFilePath,
        trainingGroupSizeParameter, annotationApp, trainingConceptsFilePath,
        scoringConceptsFilePath, dictionaryFilePath, sentenceFilters, startIndexInteger,
        stopIndexInteger, attributeTaggerPaths, attributeConflictManagers,
        tokenSequenceFinderMethod, annotationGroupPostProcessors, sentenceContextAnalyzers,
        isSentenceContextAnalyzerFile, sectionMatchers, isSectionMatchFile, sentenceMatchers,
        isSentenceMatchFile, contextSifters, isContextSifterFile);
  }


  private void help() {
    String header = "Run cross-validation of RapTAT on an annotated data set\n\n";
    String footer = "";

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar PhraseIDCrossValidationAnalyzer.jar", header,
        this.generalOptions, footer, true);
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
