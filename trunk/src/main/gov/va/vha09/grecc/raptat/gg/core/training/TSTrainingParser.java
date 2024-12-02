/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.training;

import javax.swing.SwingUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.NotImplementedException;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
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
public class TSTrainingParser {

  /*
   * TRAINING_DATA_FILEPATH is the full path to a two column '.csv' file. The first line should be a
   * header line giving the names of the columns. Any names can be used as it is ignored by the
   * system. The first column the name of the text file, and the second column the name of the
   * annotation file corresponding to the xml file.
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
   * ANNOTATION_APP specifies the application used for annotation. Either eHOST or Knowtator can be
   * used to create the annotations used for annotation. Because the resulting files are different,
   * the system needs to know which one created the annotations. That information is specified here
   * as a string as either 'EHOST' or 'KNOWTATOR'. Use of lower or upper case is optional.
   */
  private static final String ANNOTATION_APP_SHORTOPT = "a";
  private static final String ANNOTATION_APP_LONGOPT = "annotationApp";

  /*
   * CONCEPTS_FILEPATH is the full file path to a tab-delimited text file of concepts to be used in
   * training. Not all the concepts in the schema need to be included, but this list cannot accept
   * concepts that are not in the schema. Each concept is specified on a separate line, and the
   * first line is a title for the list and is ignored.
   */
  private static final String CONCEPTS_FILEPATH_SHORTOPT = "c";
  private static final String CONCEPTS_FILEPATH_LONGOPT = "conceptsFilePath";

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
   * sentence filter for training.
   */
  private static final String SENTENCE_FILTER_SHORTOPT = "i";
  private static final String SENTENCE_FILTER_LONGOPT = "sentenceFilters";

  /*
   * Attribute Tagger Paths are the full path to AttributeTaggerSolution objects stored on disk.
   * AttributeTaggerSolutions are objects that use the Cue-Scope model combined with conditional
   * random field modeling to determine attributes, such as assertion, or concept mentions. They are
   * stored on disk with the '.atsn' suffix. AttributeTaggerSolution object are ***not trained
   * here*** or used in the training here, so they should be included only for incorporation to the
   * generated solution file.
   */
  private static final String ATTRIBUTE_TAGGER_PATHS_SHORTOPT = "b";
  private static final String ATTRIBUTE_TAGGER_PATHS_LONGOPT = "attributeTaggers";

  /*
   * Attribute Conflict Managers specify the fully-qualified names of
   * AttributeConflictManagerObjects. Multiple ones should be separate by a '^' character.
   * AttributeConflictManager objects adjudicate disagreements when two different methods assign an
   * attribute value to a mention and the values are in conflict. An archetype of this would be an
   * AssertionConflictManager which adjudicates between assertion values assigned based on context
   * and based on a CRF-based Cue-Scope model. AttributeConflictManager objects are rule-based and
   * do not contribute to training. They should be ***included here only for incorporation into the
   * final solution*** that is generated as a result of training.
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
   */
  private static final String SECTION_MATCH_TEXT_SHORTOPT = "v";
  private static final String SECTION_MATCH_TEXT_LONGOPT = "sectionMatchText";
  private static final String SECTION_MATCH_FILE_SHORTOPT = "w";
  private static final String SECTION_MATCH_FILE_LONGOPT = "sectionMatchFilePath";

  /*
   * SENTENCE_MATCH descriptors provide the names and regular expressions for identifying particular
   * types of sentence of text. They are identical to SECTION_MATCHER descriptors except that they
   * only label a single sentence rather than all sentences within a section.
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
   * different fields.
   *
   * SentenceContextAttributeAnalyzer instances assign attributes to phrases if those phrases occur
   * in sentence in which the sentenceContextName of the field of a SentenceContextAttributeAnalyzer
   * matches any of the sentenceAssoociatedConcepts for the AnnotatedPhrase representing the
   * sentence. If it does match, the AnnotatedPhrase instance is assigned an attribute with the
   * attributeName field and the contextAssignedAttributeValue of the
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

  private static final String INVERT_TOKEN_SEQUENCE_LONGOPT = "invertTokenSequence";
  private static final String USE_STEMS_LONGOPT = "useStems";
  private static final String USE_POS_LONGOPT = "usePOS";
  private static final String REMOVE_STOP_WORDS_LONGOPT = "removeStopWords";
  private static final String TRAINING_TOKEN_NUMBER_LONGOPT = "trainingTokenNumber";

  /*
   * This describes the method used by the probabilistic token sequence finder to handle token
   * strings that it has not seen during training
   */
  private static final String TOKEN_SEQUENCE_SMOOTHING_METHOD_LONGOPT =
      "tokenSequenceSmoothingMethod";

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

  public TSTrainingParser() {

    /**
     * *********************************** Special Command Line Property Options
     * **********************************
     */
    Option helpOption = Option.builder(TSTrainingParser.HELP_SHORTOPT).required(false)
        .longOpt(TSTrainingParser.HELP_LONGOPT)
        .desc("Print help including all command line options").build();
    this.helpOptions.addOption(helpOption);

    Option propertiesParseOption = Option.builder(TSTrainingParser.PROPERTIES_PARSE_SHORTOPT)
        .required(false).hasArg(true).longOpt(TSTrainingParser.PROPERTIES_PARSE_LONGOPT)
        .desc("Path to file containing the options for running the PerformanceScorer class")
        .build();
    this.propertiesOptions.addOption(propertiesParseOption);

    /**
     * ********************************** Required Property Option Types
     * *********************************
     */
    Option trainingDataFilePath = Option.builder(TSTrainingParser.TRAINING_DATA_FILEPATH_SHORTOPT)
        .required(true).hasArg(true).longOpt(TSTrainingParser.TRAINING_DATA_FILEPATH_LONGOPT)
        .desc(
            "Full path to tab-delimited file of the text files and corresponding xml to be used for training; "
                + "first line in file is assumed to be a title header and is not used")
        .build();
    Option schemaFilePath = Option.builder(TSTrainingParser.SCHEMA_FILEPATH_SHORTOPT).required(true)
        .hasArg(true).longOpt(TSTrainingParser.SCHEMA_FILEPATH_LONGOPT)
        .desc("Full path to annotation schema file created by eHost or Knowtator").build();

    /**
     * *********************************** Not Required Property Option Types
     * **********************************
     */
    Option annotationApp = Option.builder(TSTrainingParser.ANNOTATION_APP_SHORTOPT).required(false)
        .hasArg(true).longOpt(TSTrainingParser.ANNOTATION_APP_LONGOPT)
        .desc("Provide name of application used to create annotations (Knowtator or eHost").build();
    Option conceptsFilePath = Option.builder(TSTrainingParser.CONCEPTS_FILEPATH_SHORTOPT)
        .required(false).hasArg(true).longOpt(TSTrainingParser.CONCEPTS_FILEPATH_LONGOPT)
        .desc("Full path to text file listing the concepts to be scored").build();
    Option dictionaryFilePath = Option.builder(TSTrainingParser.DICTIONARY_FILEPATH_SHORTOPT)
        .required(false).hasArg(true).longOpt(TSTrainingParser.DICTIONARY_FILEPATH_LONGOPT)
        .desc("Full path to dictionary to run during cross-validation").build();
    Option sentenceFilter = Option.builder(TSTrainingParser.SENTENCE_FILTER_SHORTOPT)
        .required(false).valueSeparator('^').hasArgs()
        .longOpt(TSTrainingParser.SENTENCE_FILTER_LONGOPT)
        .desc(
            "Fully-qualified names of any sentence filter to run during cross-validation, the associated phrase filter, "
                + "and the phrase filter constructor parameters; surround each name and associated information with "
                + "quotations, and separate distinct filter names and associations with a '^' character. Each sentence filter "
                + "should be indicated in the form \"FullPathToSentenceFilter:FullPathToPhraseFilter:PhraseFilterParameter_1:...:"
                + "PhraseFilterParameter_n\" where the PhraseFilter class implements the 'includePhrase()' method and the "
                + "parameters used to initialize a PhraseFilter instance ofthe indicated type are described by PhraseFilterPararmeter_1 to "
                + "PhraseFilterParameter_n in the order that they are provided to the PhraseFilter constructor")
        .build();
    Option attributeTaggerPaths = Option.builder(TSTrainingParser.ATTRIBUTE_TAGGER_PATHS_SHORTOPT)
        .required(false).hasArgs().longOpt(TSTrainingParser.ATTRIBUTE_TAGGER_PATHS_LONGOPT)
        .valueSeparator('^')
        .desc(
            "Full paths to any attribute taggers to run during cross-validation; surround each path with "
                + "quotations, and separate multiple paths with a '^' character")
        .build();
    Option attributeConflictManagers = Option
        .builder(TSTrainingParser.ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT).required(false).hasArgs()
        .longOpt(TSTrainingParser.ATTRIBUTE_CONFLICT_MANAGERS_LONGOPT).valueSeparator('^')
        .desc(
            "Provide fully-qualified class names of attribute conflict managers for annotating attributes during "
                + "cross-validation; separate multiple manager names with a '^' character")
        .build();
    Option tokenSequenceFinderMethod = Option
        .builder(TSTrainingParser.TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT).required(false)
        .hasArg(true).longOpt(TSTrainingParser.TOKEN_SEQUENCE_FINDER_METHOD_LONGOPT)
        .desc(
            "String naming TSFinderMethod for identifying annotated sequences (default = PROBABILISTIC)")
        .build();
    Option annotationGroupPostProcessors = Option
        .builder(TSTrainingParser.ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT).required(false).hasArgs()
        .longOpt(TSTrainingParser.ANNOTATION_GROUP_POSTPROCESSOR_LONGOPT).valueSeparator('^')
        .desc(
            "Fully-qualified paths to any AnnotationGroupPostProcessor to be run after the completion of "
                + "all other steps in annotating a document - separate multiple post processors with the '^' character")
        .build();
    Option invertTokenSequence =
        Option.builder().required(false).longOpt(TSTrainingParser.INVERT_TOKEN_SEQUENCE_LONGOPT)
            .desc("Reverse the sequence of Tokens in the document during training and annotation")
            .build();
    Option useStems = Option.builder().required(false).longOpt(TSTrainingParser.USE_STEMS_LONGOPT)
        .desc("Use lemmatization of token strings for training and annotation").build();
    Option usePOS = Option.builder().required(false).longOpt(TSTrainingParser.USE_POS_LONGOPT)
        .desc("Tag token strings with parts-of-speech for training and annotation").build();
    Option removeStopWords = Option.builder().required(false)
        .longOpt(TSTrainingParser.REMOVE_STOP_WORDS_LONGOPT)
        .desc(
            "Remove any tokens whose strings appear in the list of stop words hard-coded in RapTAT")
        .build();
    Option trainingTokenNumber = Option.builder().required(false)
        .longOpt(TSTrainingParser.TRAINING_TOKEN_NUMBER_LONGOPT).hasArg(true)
        .desc(
            "Enter integer for the maximum number of tokens allowed for an annotated phrase - phrases longer than this will be split ")
        .build();
    Option tokenSequenceSmoothingMethod = Option.builder().required(false).hasArg(true)
        .longOpt(TSTrainingParser.TOKEN_SEQUENCE_SMOOTHING_METHOD_LONGOPT)
        .desc(
            "Remove any tokens whose strings appear in the list of stop words hard-coded in RapTAT")
        .build();

    /**
     * *********************************** Grouped Property Options
     * **********************************
     */
    Option sectionMatchTextOption = Option.builder(TSTrainingParser.SECTION_MATCH_TEXT_SHORTOPT)
        .required(false).hasArgs().longOpt(TSTrainingParser.SECTION_MATCH_TEXT_LONGOPT)
        .valueSeparator('~')
        .desc(
            "Text describing regexes and their names separated by '~' character, each provided in the "
                + "form 'NameOfRegex':'RegexPattern'")
        .build();
    Option sectionMatchFileOption =
        Option.builder(TSTrainingParser.SECTION_MATCH_FILE_SHORTOPT).required(false).hasArgs()
            .longOpt(TSTrainingParser.SECTION_MATCH_FILE_LONGOPT).valueSeparator('^')
            .desc("Full path to file with regex names and patterns for section matching").build();

    Option sentenceMatchTextOption = Option.builder(TSTrainingParser.SENTENCE_MATCH_TEXT_SHORTOPT)
        .required(false).hasArgs().longOpt(TSTrainingParser.SENTENCE_MATCH_TEXT_LONGOPT)
        .valueSeparator('~')
        .desc(
            "Text describing regexes and their names separated by '~' character, each provided in the "
                + "form 'NameOfRegex':'RegexPattern'")
        .build();
    Option sentenceMatchFileOption =
        Option.builder(TSTrainingParser.SENTENCE_MATCH_FILE_SHORTOPT).required(false).hasArgs()
            .longOpt(TSTrainingParser.SENTENCE_MATCH_FILE_LONGOPT).valueSeparator('^')
            .desc("Full path to file with names and patterns for sentence matching").build();

    Option sentenceContextAnalyzerTextOption = Option
        .builder(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT).required(false).hasArgs()
        .longOpt(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_TEXT_LONGOPT).valueSeparator('^')
        .desc(
            "Text describing SentenceContextAnalyzer instances to use during annotation, each one provided by "
                + "a name:value pair of the fields needed for constructing a SentenceContextAnalyzer instance, as in "
                + "attributeName:instanceAttributeName, sentenceContextName:stringRepresentingSentenceContextName, "
                + "defaultAttributeValue:stringRepresentingDefaultAttributeValue, "
                + "contextAssignedAttributeValue:stringRepresentingContextAssignedAttributeValue, "
                + "returnDefault:trueOrFalse; fields should be separated by commas and different analyzers demarcated by"
                + " the '^' character")
        .build();
    Option sentenceContextAnalyzerFileOption = Option
        .builder(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT).required(false).hasArgs()
        .longOpt(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_FILE_LONGOPT).valueSeparator('^')
        .desc("Full path to file describing the SentenceContextAnalyzer instances, one per line")
        .build();

    Option contextSifterTextOption = Option.builder(TSTrainingParser.CONTEXT_SIFTER_TEXT_SHORTOPT)
        .hasArgs().valueSeparator('^').required(false)
        .longOpt(TSTrainingParser.CONTEXT_SIFTER_TEXT_LONGOPT)
        .desc(
            "Descriptors of any context sifters; delineate each sifter by surrounding it with quotes; "
                + "precede concepts for sifting with 'concepts:', allowable concepts with 'allowed:', and "
                + "required concepts with 'required:' without the single quotes; if there are multiple "
                + "concepts for sifting, allowable, or required concepts for a given sifter, use commas "
                + "as a separator.\n\nEXAMPLE: \"concepts:concept1,concept2 allowed: allowedconcept1, "
                + "allowedconcept2 required:requiredconcept1, requiredconcept2\"")
        .build();
    Option contextSifterFileOption = Option.builder(TSTrainingParser.CONTEXT_SIFTER_FILE_SHORTOPT)
        .hasArgs().required(false).longOpt(TSTrainingParser.CONTEXT_SIFTER_FILE_LONGOPT)
        .valueSeparator('^')
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

    OptionGroup sentenceContextAnalyzerTextVsFileOption = new OptionGroup();
    sentenceContextAnalyzerTextVsFileOption.setRequired(false);
    sentenceContextAnalyzerTextVsFileOption.addOption(sentenceContextAnalyzerTextOption)
        .addOption(sentenceContextAnalyzerFileOption);

    OptionGroup contextSifterTextVsFileOption = new OptionGroup();
    contextSifterTextVsFileOption.setRequired(false);
    contextSifterTextVsFileOption.addOption(contextSifterTextOption)
        .addOption(contextSifterFileOption);

    /**
     * *********************************** Options (Sets of Option Types)
     * **********************************
     */
    this.generalOptions.addOption(trainingDataFilePath).addOption(schemaFilePath)
        .addOption(annotationApp).addOption(conceptsFilePath).addOption(dictionaryFilePath)
        .addOption(sentenceFilter).addOption(attributeTaggerPaths)
        .addOption(attributeConflictManagers).addOption(tokenSequenceFinderMethod)
        .addOption(annotationGroupPostProcessors).addOption(invertTokenSequence).addOption(useStems)
        .addOption(usePOS).addOption(removeStopWords).addOption(trainingTokenNumber)
        .addOption(tokenSequenceSmoothingMethod).addOptionGroup(sectionMatchTextVsFileOption)
        .addOptionGroup(sentenceMatchTextVsFileOption)
        .addOptionGroup(sentenceContextAnalyzerTextVsFileOption)
        .addOptionGroup(contextSifterTextVsFileOption);
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
  public TSTrainingParameterObject parseCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    String trainingDataFilePath = null;
    String schemaFilePath = null;
    AnnotationApp annotationApp = null;
    String conceptsFilePath = null;
    String dictionaryFilePath = null;
    String[] sentenceFilters = null;
    String[] attributeTaggerPaths = null;
    String[] attributeConflictManagers = null;
    String tokenSequenceFinderMethod = "Probabilistic";
    String[] annotationGroupPostProcessors = null;
    boolean invertTokenSequence = OptionsManager.getInstance().getInvertTokenSequence();
    boolean useStems = OptionsManager.getInstance().getUseStems();
    boolean usePOS = OptionsManager.getInstance().getUsePOS();
    boolean removeStopWords = OptionsManager.getInstance().getRemoveStopWords();
    int trainingTokenNumber = OptionsManager.getInstance().getTrainingTokenNumber();
    String smoothingMethod = OptionsManager.getInstance().getSmoothingMethod().toString();
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
      if (cmd.hasOption(TSTrainingParser.HELP_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.HELP_LONGOPT)) {
        help();
        System.exit(0);
      }

      cmd = parser.parse(this.propertiesOptions, args, true);
      if (cmd.hasOption(TSTrainingParser.PROPERTIES_PARSE_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.PROPERTIES_PARSE_LONGOPT)) {
        args = readPropertiesFile(cmd.getOptionValue(TSTrainingParser.PROPERTIES_PARSE_SHORTOPT));
      }

      cmd = parser.parse(this.generalOptions, args);
      if (cmd.hasOption(TSTrainingParser.TRAINING_DATA_FILEPATH_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.TRAINING_DATA_FILEPATH_LONGOPT)) {
        trainingDataFilePath = cmd.getOptionValue(TSTrainingParser.TRAINING_DATA_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.SCHEMA_FILEPATH_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SCHEMA_FILEPATH_LONGOPT)) {
        schemaFilePath = cmd.getOptionValue(TSTrainingParser.SCHEMA_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.ANNOTATION_APP_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.ANNOTATION_APP_LONGOPT)) {
        annotationApp =
            AnnotationApp.valueOf(cmd.getOptionValue(TSTrainingParser.ANNOTATION_APP_SHORTOPT)
                .replaceAll("\"", "").toUpperCase());
      }

      if (cmd.hasOption(TSTrainingParser.CONCEPTS_FILEPATH_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.CONCEPTS_FILEPATH_LONGOPT)) {
        conceptsFilePath = cmd.getOptionValue(TSTrainingParser.CONCEPTS_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.DICTIONARY_FILEPATH_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.DICTIONARY_FILEPATH_LONGOPT)) {
        dictionaryFilePath = cmd.getOptionValue(TSTrainingParser.DICTIONARY_FILEPATH_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.SENTENCE_FILTER_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SENTENCE_FILTER_LONGOPT)) {
        sentenceFilters = cmd.getOptionValues(TSTrainingParser.SENTENCE_FILTER_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.ATTRIBUTE_TAGGER_PATHS_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.ATTRIBUTE_TAGGER_PATHS_LONGOPT)) {
        attributeTaggerPaths =
            cmd.getOptionValues(TSTrainingParser.ATTRIBUTE_TAGGER_PATHS_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.ATTRIBUTE_CONFLICT_MANAGERS_LONGOPT)) {
        attributeConflictManagers =
            cmd.getOptionValues(TSTrainingParser.ATTRIBUTE_CONFLICT_MANAGERS_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.TOKEN_SEQUENCE_FINDER_METHOD_LONGOPT)) {
        tokenSequenceFinderMethod =
            cmd.getOptionValue(TSTrainingParser.TOKEN_SEQUENCE_FINDER_METHOD_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.ANNOTATION_GROUP_POSTPROCESSOR_LONGOPT)) {
        annotationGroupPostProcessors =
            cmd.getOptionValues(TSTrainingParser.ANNOTATION_GROUP_POSTPROCESSOR_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.INVERT_TOKEN_SEQUENCE_LONGOPT)) {
        invertTokenSequence = true;
      }

      if (cmd.hasOption(TSTrainingParser.USE_STEMS_LONGOPT)) {
        useStems = true;
      }

      if (cmd.hasOption(TSTrainingParser.USE_POS_LONGOPT)) {
        usePOS = true;
      }

      if (cmd.hasOption(TSTrainingParser.REMOVE_STOP_WORDS_LONGOPT)) {
        removeStopWords = true;
      }

      if (cmd.hasOption(TSTrainingParser.TRAINING_TOKEN_NUMBER_LONGOPT)) {
        trainingTokenNumber = Integer.parseInt(cmd
            .getOptionValue(TSTrainingParser.TRAINING_TOKEN_NUMBER_LONGOPT).replaceAll("\"", ""));
      }

      if (cmd.hasOption(TSTrainingParser.TOKEN_SEQUENCE_SMOOTHING_METHOD_LONGOPT)) {
        smoothingMethod =
            cmd.getOptionValue(TSTrainingParser.TOKEN_SEQUENCE_SMOOTHING_METHOD_LONGOPT);
      }

      if (cmd.hasOption(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_TEXT_LONGOPT)) {
        sentenceContextAnalyzers =
            cmd.getOptionValues(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_FILE_LONGOPT)) {
        sentenceContextAnalyzers =
            cmd.getOptionValues(TSTrainingParser.SENTENCE_CONTEXT_ANALYZER_FILE_SHORTOPT);
        isSentenceContextAnalyzerFile = true;
      }

      if (cmd.hasOption(TSTrainingParser.SECTION_MATCH_TEXT_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SECTION_MATCH_TEXT_LONGOPT)) {
        sectionMatchers = cmd.getOptionValues(TSTrainingParser.SECTION_MATCH_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.SECTION_MATCH_FILE_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SECTION_MATCH_FILE_LONGOPT)) {
        sectionMatchers = cmd.getOptionValues(TSTrainingParser.SECTION_MATCH_FILE_SHORTOPT);
        isSectionMatchFile = true;
      }

      if (cmd.hasOption(TSTrainingParser.SENTENCE_MATCH_TEXT_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SENTENCE_MATCH_TEXT_LONGOPT)) {
        sentenceMatchers = cmd.getOptionValues(TSTrainingParser.SENTENCE_MATCH_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.SENTENCE_MATCH_FILE_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.SENTENCE_MATCH_FILE_LONGOPT)) {
        sentenceMatchers = cmd.getOptionValues(TSTrainingParser.SENTENCE_MATCH_FILE_SHORTOPT);
        isSentenceMatchFile = true;
      }

      if (cmd.hasOption(TSTrainingParser.CONTEXT_SIFTER_TEXT_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.CONTEXT_SIFTER_TEXT_LONGOPT)) {
        contextSifters = cmd.getOptionValues(TSTrainingParser.CONTEXT_SIFTER_TEXT_SHORTOPT);
      }

      if (cmd.hasOption(TSTrainingParser.CONTEXT_SIFTER_FILE_SHORTOPT)
          || cmd.hasOption(TSTrainingParser.CONTEXT_SIFTER_FILE_LONGOPT)) {
        contextSifters = cmd.getOptionValues(TSTrainingParser.CONTEXT_SIFTER_FILE_SHORTOPT);
        isContextSifterFile = true;
      }

    } catch (Exception e) {
      System.out.println("Parse exception invoked");
      e.printStackTrace();
      System.exit(-1);
    }

    return new TSTrainingParameterObject(trainingDataFilePath, schemaFilePath, annotationApp,
        conceptsFilePath, dictionaryFilePath, sentenceFilters, attributeTaggerPaths,
        attributeConflictManagers, tokenSequenceFinderMethod, annotationGroupPostProcessors,
        invertTokenSequence, useStems, usePOS, removeStopWords, trainingTokenNumber,
        smoothingMethod, sentenceContextAnalyzers, isSentenceContextAnalyzerFile, sectionMatchers,
        isSectionMatchFile, sentenceMatchers, isSentenceMatchFile, contextSifters,
        isContextSifterFile);
  }


  private void help() {
    String header = "Train RapTAT using an annotated data set\n\n";
    String footer = "";

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar TSTrainingParser.jar", header, this.generalOptions, footer,
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


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    if (lvgSetting > 0) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          System.out.println("Entered TSTrainingParser main method");
          TSTrainingParser parser = new TSTrainingParser();
          TSTrainingParameterObject parameters = parser.parseCommandLine(args);

          try {
            TokenSequenceSolutionTrainer.generateAndSaveSolution(parameters);
          } catch (NotImplementedException e) {
            System.err.println("Unable to run parsed parameters " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(-1);
          }
        }
      });
    }
  }
}
