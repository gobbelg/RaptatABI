package src.main.gov.va.vha09.grecc.raptat.gg.core;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.ConceptAttributeRemap;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ********************************************************** Holds global constants used for
 * setting the initial size of the hash tables used to store concepts,word/concept pairs, words, and
 * concepts associated with words
 *
 * @author Glenn Gobbel, Jul 8, 2010
 *         <p>
 *         *********************************************************
 */
public abstract class RaptatConstants {
  public static enum AnnotationApp {
    EHOST("eHOST"), KNOWTATOR("Knowtator");

    private final String commonName;


    AnnotationApp(String commonName) {
      this.commonName = commonName;
    }


    /** @return the commonName */
    public String getCommonName() {
      return this.commonName;
    }
  }

  // Index in array to import annotations for use in XMLImporter
  public enum AnnotationDataIndex {
    MENTION_ID, START_OFFSET, END_OFFSET, SPAN_TEXT, ANNOTATOR_NAME, CONCEPT_NAME;
  }

  /*
   * Used by the TrainingWindow to enumerate the potential types of ContextAttributeAnalyzer types
   * for a user to chose from. This enum should be kept updated to contain all the subtypes of the
   * ContextAttributeAnalyzer interface.The selected ones will be incorporated into the solution
   * file.
   */
  public static enum AttributeAnalyzerType {
    ASSERTION("Assertion"), GENERAL_CONTEXT("General Context"), REASON_NO_MEDS("Reason No Meds");

    public final String displayName;

    AttributeAnalyzerType(String displayName) {
      this.displayName = displayName;
    }


    public static AttributeAnalyzerType getTypeFromDisplayName(String displayName) {
      for (AttributeAnalyzerType value : AttributeAnalyzerType.values()) {
        if (value.displayName.equalsIgnoreCase(displayName)) {
          return value;
        }
      }
      return null;
    }
  }

  /*
   * Used by the TrainingWindow to enumerate the potential types of ContextAttributeAnalyzer types
   * for a user to chose from. This enum should be kept updated to contain all the subtypes of the
   * ContextAttributeAnalyzer interface.The selected ones will be incorporated into the solution
   * file.
   */
  public static enum AttributeConflictManagerType {
    ASSERTION("Assertion");

    public final String displayName;


    AttributeConflictManagerType(String displayName) {
      this.displayName = displayName;
    }
  }

  public enum CONTEXT_SIFTER_FIELDS {
    ORDER, CONCEPTS, CONTEXT, FORBIDDEN_DISTANCE_REVERSE, FORBIDDEN_DISTANCE_FORWARD,
    REQUIRED_DISTANCE_REVERSE, REQUIRED_DISTANCE_FORWARD
  }

  /**
   * This enum type can be used when doing, for example, a task such as 'reason no meds' tagging. If
   * ContextHandling is set to 'Probabilistic', token strings that are "in context" have a tag added
   * to their augmentedString field, and this augmented string is used in all probabilistic methods,
   * so that when searching for a sign of a contraindication, like "cough", the string "cough" which
   * would not be tagged if out of context would be different than "cough_INCONTEXT", which is how
   * it might be tagged if "in context."
   */
  public static enum ContextHandling implements Serializable {
    NONE, RULEBASED, PROBABILISTIC
  }

  public static enum FileImportType {
    CSV, KNOWTATOR
  }

  /**
   * FilterType - Keeps track of the type of filtering being used for calculating likelihood in the
   * case of data elements the never occur in the training data
   *
   * @author Glenn Gobbel, Aug 4, 2010
   */
  public static enum FilterType {
    STANDARD, SMOOTHED, MODIFIED, UNSMOOTHED
  }

  public static enum OptimismCorrectionMethod {
    SKIP, AVG_ZERO_ONE, ASSUMEZERO, ASSUMEONE;
  }

  public static enum PerformanceType {
    PRECISION, RECALL, FMEASURE
  }

  public static enum PhraseIDSmoothingMethod {
    UNSMOOTHED, SMOOTHED, EXTENDED, AMPLIFIED, XTREME, XTREME_MOD
  }

  public static enum ScoringMode {
    CONCEPT_ONLY_SCORING, CONCEPT_AND_ATTRIBUTE_SCORING, ATTRIBUTE_ONLY
  }

  public enum SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS {
    ATTRIBUTE_NAME, SENTENCE_CONTEXT_NAME, DEFAULT_ATTRIBUTE_VALUE,
    CONTEXT_ASSIGNED_ATTRIBUTE_VALUE, RETURN_DEFAULT
  }

  public static enum SentenceSectionTaggerType {
    SENTENCE, SECTION;
  }

  public static enum solutionType {
    PROBABILISTIC, CRF
  }

  public static enum SpecialContext {
    YES, NO
  }

  public static enum TemporalContext {
    RECENT, HISTORICAL, HYPOTHETICAL
  }

  public static enum TrueFalseScores {
    TP, FP, FN;
  }

  public static enum TSFinderMethod {
    PROBABILISTIC, NAIVEBAYES, BAGOFWORDS, NONNAIVEBAYES, CRF
  }

public static final ArrayList<String> BINARY_FEATURE_VALUES = new ArrayList<>(
Arrays.asList("0", "1"));

  // String marker to represent all concepts
  public static final String ALL_CONCEPTS = "%%$_$$_ALL_CONCEPTS_$$_$%%@";

  // Minimum height for Analysis options window
  public static final int ANALYSIS_OPTIONS_MIN_HEIGHT = 350;

  // Malini:Height/width for options window
  // Minimum height for Analysis options window
  public static final int ANALYSIS_OPTIONS_MIN_WIDTH = 400;

  // Minimum height for annotate window
  public static final int ANNOTATE_WINDOW_MIN_HEIGHT = 600;

  // Expected number of attributes associated with a single concept
  public static final int ATTRIBUTES_PER_CONCEPT = 32;

  public static final String BLOCKED_TAG = "BLOCKED";

  // Default number of bootstrap iterations to run
  public static final int BOOTSTRAP_ITERATIONS_DEFAULT = 1000;

  // Strings used for column titles in result file for overall results
  public static final String[] BOOTSTRAP_TOTAL_RESULT_TITLES =
      {"TP_Apparent", "FP_Apparent", "FN_Apparent", "Precision_Apparent", "Recall_Apparent",
          "F_Measure_Apparent", "TP_Bootstrap", "FP_Bootstrap", "FN_Bootstrap",
          "Precision_Bootstrap", "Recall_Bootstrap", "F_Measure_Bootstrap", "TP_Test", "FP_Test",
          "FN_Test", "Precision_Test", "Recall_Test", "F_Measure_Test"};

  // Strings used for column titles in result file for
  // concept-by-concept bootstrap results
  public static final String[] BOOTSTRAP_CONCEPT_RESULT_TITLES = {"Concept", "Iterations",
      "Precision_Avg", "Precision_SD", "Recall_Avg", "Recall_SD", "F_Measure_Avg", "F_Measure_SD"};

  // Fraction of samples to use for training during
  // bootstrapping
  public static final double BOOTSTRAP_SAMPLE_FRACTION_DEFAULT = 1.0;

  // Default size of the samples returned by a Bootstrapper
  // object where 0 returns a sample size equal to the original dataset
  public static final int BOOTSTRAP_SAMPLE_SIZE_DEFAULT = 0;

  public static final int BOW_SEQUENCE_ID_PRIOR_INDEX = 0;

  public static final int BOW_SEQUENCE_ID_PHRASE_INDEX = 1;

  public static final int BOW_SEQUENCE_ID_POST_INDEX = 2;

  public static final int BOW_SEQUENCE_ID_ANNOTATED_INDEX = 0;

  public static final int BOW_SEQUENCE_ID_NOT_ANNOTATED_INDEX = 1;

  public static final int CARRIAGE_RETURN_CHAR = 13;

  // Title of radio button controller for phrase compression options
  public static final String COMPRESSION_TITLE = "Compression Words";

  // Indicator of whether to create a summary of any results
  public static final boolean CREATE_SUMMARY_START = true;

  // Path to CRFSuite application
  public static final String CRFSUITE_PATH = "D:\\GobbelWorkspace\\Subversion\\branches\\"
      + "GlennWorkspace\\Resources\\crfsuite-0.12_win32\\crfsuite.exe";

  // Determine who is running the application during development
  public static final String CURRENT_DEVELOPER = "Glenn";

  public static final int NEGATION_WINDOW_SIZE_DEFAULT = 6;

  public static final int UNCERTAIN_WINDOW_SIZE_DEFAULT = 10;

  public static final int REASON_NO_MEDS_WINDOW_SIZE_DEFAULT = 20;

  /*
   * Stores the string used to indicate demarcations between a string representing a token and other
   * fields, such as parts of speech and labels, that are used by conditional random field models.
   */
  public static final String CRF_FIELD_DELIMITER_DEFAULT = "_D_L@M#T_";

  public static final String CRF_ASSERTION_CUE_MARKER = "_N_C@U#E_";

  public static final int DEFAULT_BUFFERED_STREAM_SIZE = 524288;

  public static final String DEFAULT_SOLUTION_DIRECTORY = "/raptat/solutionFiles";

  // Location of documentID column in CSV file. We use
  // zero indexing so the "first" column is actually
  // column 0.
  public static final int DOC_ID_COLUMN = 1;

  // Do a default analysis of context
  public static final boolean DO_CONTEXT_ANALYSIS = false;

  public static final String EHOST_ELLIPSE = " ... ";

  // This is the estimated number of unique tokens in the corpus
  public static final int ESTIMATED_VOCABULARY_SIZE = 20;

  // Title of radio button controller for phrase expansion options
  public static final String EXPANSION_TITLE = "Expansion Words";

  // Array of possible formats for saving a mapping of phrases
  // to concepts
  public static final String[] EXPORTABLE_FILETYPES = {"Knowtator XML", "eHOST XML"};

  // Used to determine default setting of the format to use when saving
  // a mapping of phrases to concepts
  public static final int EXPORT_FORMAT_START = 0;

  // Size of window to how far blockers or facilitators can be and still
  // affect whether a dictionary phrase maps to a concept
  public static final int GENERAL_CONTEXT_WINDOW_RADIUS = 10;

  public static final String FACILITATED_TAG = "FACILITATED";

  public static final String[] FILTER_STRINGS = {"Standard", "Smoothed"};

  // Type of smoothing to start application;
  public static final String FILTER_START = RaptatConstants.FILTER_STRINGS[1];

  public static final String GEARS_IMAGE_PATH = "/src/main/resources/gears.png";

  // Malini:Added constants used by options manager
  public static final int GROUPBY_COLUMN_NUMBER_DEFAULT = 2;

  public static final String[] GROUP_BY_COLUMN_OPTIONS = {"None", "1", "2", "3", "4", "5"};

  public static final String GROUP_BY_COLUMN_INPUT_ERROR =
      "<html><b>Grouping Column Input Error:</b><br><br>"
          + "Please enter an either none or an integer between 1 "
          + " and the number of columns in the data <br><html>";

  // Added by Glenn on Feb 21, 2012
  // Modify LVG_PROPERTIES_PATH below for use on Glenn's Mac
  // public static final String LVG_PROPERTIES_PATH =
  // "/data/config/lvg.properties";

  // This is the number of positions that a labeled token is likely
  // to occur within a single document
  public static final int HASH_SET_SIZE_DEFAULT = 20;

  // This is based on the number of unique first words of phrases
  // in the MCVS data
  public static final int LABELED_HASH_TREE_SIZE_DEPTH_0_DEFAULT = 16;
  // This is based on the number of unique second words of phrases
  // in the MCVS data
  public static final int LABELED_HASH_TREE_SIZE_DEPTH_1_DEFAULT = 8;
  // This is based on the number of unique third words of phrases
  // in the MCVS data
  public static final int LABELED_HASH_TREE_SIZE_DEPTH_2_DEFAULT = 4;
  // This is based on the number of unique fourth or higher words of phrases
  // in the MCVS data
  public static final int LABELED_HASH_TREE_SIZE_DEPTH_OVER2_DEFAULT = 2;
  public static final int LINE_FEED_CHAR = 10;
  // Marker that should be placed at the beginning of a file
  // written using an ListOutputStream object - gets checked
  // by ListInputStream
  public static final String LIST_OUTPUTSTREAM_MARKER = "ListOutputStream";

  // File types that can be imported by RapTat
  public static final String[] IMPORTABLE_FILETYPES = {".csv", ".txt", ".pprj"};

  // Spacing around info box on main page
  public static final int INSET = 15;

  // Determine whether token sequence used
  // for training should be in left-to-right (English reading)
  // order or inverted, so the rightmost word is first
  public static final boolean INVERT_TOKEN_SEQUENCE = false;

  public static final String[] CONCEPT_MAPPING_METHODS =
      {"Bag of Words", "Naive Bayes", "Non-Naive Bayes", "Decision Tree"};

  // Learning method at start of program
  public static final String CONCEPT_MAPPING_METHOD_DEFAULT =
      RaptatConstants.CONCEPT_MAPPING_METHODS[1];

  public static final String[] CONCEPT_MAPPING_SOLUTIONS =
      {"BOWConceptMapSolution", "NBSolution", "NonNBSolution", "DecisionTreeSolution"};

  // Strings used for column titles in result file for overall results
  public static final String[] CROSS_VALIDATION_CONCEPT_RESULT_TITLES =
      {"Concept", "Attribute", "Value", "Relationship", "LinkedToConcept", "RelationshipAttribute",
          "RelationshipValue", "TP", "FP", "FN", "Precision", "Recall", "F_Measure"};

  // Flag to determine if we should log errors
  public static final boolean LOG_ERRORS = false;

  // Relative path to properties for lvg module
  public static final String LVG_PROPERTIES_PATH =
      File.separator + "data" + File.separator + "config" + File.separator + "lvg.properties";

  // Bit-flags used to specify LVG parts of speech
  // Note that the verb bit flag covers both regular
  // (flag = 1024) and auxiliary (flag = 4) verbs
  public static final int LVG_ADJECTIVE_BITFLAG = 1;

  public static final int LVG_ADVERB_BITFLAG = 2;

  public static final int LVG_MODAL_BITFLAG = 64;

  public static final int LVG_NOUN_BITFLAG = 128;

  public static final int LVG_PRONOUN_BITFLAG = 512;

  public static final int LVG_VERB_BITFLAG = 1024 + 4;

  // Strings used for column titles in result file
  public static final String[] MACHINE_LEARNING_RESULT_TITLES = {"Phrase", "Words in Phrase",
      "Expected Concept", "Result Concept", "Concepts Tested", "Smoothing Used"};

  /*
   * Maximum arguments for initializing a ContextSifter instance from the command line
   */
  public static final int MAX_CONTEXT_SIFTER_ARGUMENTS = 20;

  // Maximum words to exclude/insert for a phrase
  public static final int MAX_WORD_CHANGE = 2;

  // Holds maximum length of a sequence in tokens that can be handled by a
  // machine learning trainer
  public static final int MAX_TOKENS_DEFAULT = 7;

  public static final int MAX_EXPECTED_CONCEPTS_PER_SCHEMA = 64;

  public static final int MAX_EXPECTED_ATTRIBUTES_PER_CONCEPT = 8;

  public static final int MAX_EXPECTED_VALUES_PER_ATTRIBUTE = 8;

  public static final int MAX_SENTENCE_TOKENS = 100;

  public static final int MAX_UNIQUE_TOKENS_PER_CONCEPT_ATTRIBUTE = 16;

  public static final String MAIN_WINDOW_IMAGE = "/src/main/resources/RapTAT_ScreenSmall.png";

  public static final String MAIN_HELP_FILE = "/src/main/resources/RapTAT_Help.html";

  // Minimum height for mapping options window
  public static final int MAP_OPTIONS_MIN_HEIGHT = 150;

  // Minimum height for mapping options window
  public static final int MAP_OPTIONS_MIN_WIDTH = 250;

  public static final int MAX_LABELED_SCORE = 967680;

  // Starting size for hash holding attributes
  public static final int NB_UNIQUE_TOKEN_TABLE_SIZE = 10000;
  // Starting size to store
  public static final int NB_UNIQUE_TOKEN_CONCEPT_PAIRS = 20000;
  // Starting size of hash holding concepts
  public static final int NB_CONCEPT_TABLE_SIZE = 10000;
  // Starting size for hash used by each attribute to store
  // associated concepts
  public static final int NB_CONCEPTS_PER_ATTRIBUTE = 10;
  public static final int[] NNB_CONCEPTS_PER_ATTRIBUTE = {5, 2, 2, 2, 2, 2, 2};
  public static final String NEGATION_TAG = "NEG";

  public static final int[] NNB_COND_TREE_SIZES = {5, 2, 2, 2, 2, 2, 0};

  // Estimated maximum number of unique phrases in a given document
  public static final int NNB_PHRASES_PER_DOCUMENT = 400;

  // String to use in data for any null element values
  public static final String NULL_ELEMENT = "$**NULL@@$";

  // Starting size for word exclusion
  public static final int NUM_COMPRESS_START = 0;

  // Starting size for word insertion
  public static final int NUM_EXPAND_START = 0;

  public static final int NUM_OF_ITERATIONS = 1;

  // Number of columns included in the results
  public static final int NUMBER_SUMMARY_COLUMNS = 6;

  public static final String NUMBER_OF_ITERATIONS_INPUT_ERROR_STRING =
      "<html><b>Number of Iterations Input Error: </b><br><br> "
          + "Please use a positive integer<br><html>";

  // Used to convert int numbers to strings for result file column titles
  public static final String[] NUMSTRINGS =
      {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};

  // Acronyms for OpenNLP parts of speech
  public static final String[] OPEN_NLP_NOUNS = {"NN", "NNS"};

  public static final String[] OPEN_NLP_ADVERBS = {"RB", "RBR", "RBS"};

  public static final String[] OPEN_NLP_VERBS = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};

  public static final String[] OPEN_NLP_PRONOUNS = {"PRP", "PRP$"};

  public static final String[] OPEN_NLP_ADJECTIVES = {"JJ", "JJR", "JJS"};

  public static final String[] OPEN_NLP_MODALS = {"MD"};

  // How to correct for optimism in bootstrapping when a concept does
  // not exist in the bootstrap set used for training
  public static final OptimismCorrectionMethod OPTIMISM_CORRECTION_DEFAULT =
      OptimismCorrectionMethod.AVG_ZERO_ONE;

  // Default number of annotations to do before reporting them
  // to System.out
  public static final int PHRASE_REPORT_INTERVAL = 10000;

  public static final String[] PHRASE_ID_TRAINING_METHODS =
      {"Probabilistic", "Bag of Words", "Naive Bayes", "Non-Naive Bayes"};

  public static final String[] CONTEXT_METHODS = {"None", "Rule-Based", "Probabilistic"};

  public static final TSFinderMethod PHRASE_ID_TRAINING_METHOD_DEFAULT =
      TSFinderMethod.PROBABILISTIC;

  public static final int PHRASE_ID_SURROUNDING_TOKENS_DEFAULT = 0;

  public static final long RANDOM_SEED = -443434;

  // Default number of bootstrap iterations to run
  public static final long RANDOM_SEED_DEFAULT = 3683245;

  public static final ContextHandling REASON_NO_MEDS_PROCESSING_DEFAULT = ContextHandling.NONE;

  /*
   * Create array that specifies remappings of certain values of attributes within particular
   * concepts. For example, to specify that the concept "intake" with an attribute of "deliverymode"
   * and value of "oral" would be remapped to the value "notiv", the item in the array would be
   * specfied as: new Pair<Pair<String, String>, Pair<String,String>>( new Pair( "intake",
   * "deliverymode" ),new Pair("oral","notiv" )
   */
  public static final ConceptAttributeRemap[] CONCEPT_ATTRIBUTEVALUE_REMAPPINGS =
      {new ConceptAttributeRemap("contrastexposure", "contrasttype", "mr-based", "notxray"),
          new ConceptAttributeRemap("contrastexposure", "contrasttype", "unstated", "notxray"),
          new ConceptAttributeRemap("intake", "deliverymode", "oral", "notiv"),
          new ConceptAttributeRemap("intake", "deliverymode", "unstated", "notiv"),
          new ConceptAttributeRemap("intake", "fluidity", "solidandliquid", "liquid"),
          new ConceptAttributeRemap("intake", "fluidity", "solid", "notliquid"),
          new ConceptAttributeRemap("intake", "fluidity", "unstated", "notliquid")};

  public static final ContextHandling NEGATION_HANDLING_DEFAULT = ContextHandling.NONE;

  public static final String REASON_NO_MEDS_TAG = "rnm";

  /*
   * Stores relative path to thesaurus for use by Thesarus class to look up synonymMap
   */
  public static final String RELATIVE_THESAURUS_PATH = "/src/main/resources/th_en_US_new.dat";

  public static final boolean REMOVE_STOP_WORDS = true;

  /*
   * Determines whether the UnlabeledHashTree instances used during training are saved to allow for
   * updating. The default is false as they can be quite large.
   */
  public static final boolean SAVE_UNLABELED_TO_DISK_AFTER_TRAINING = false;

  // Threshold likelihood before a sequence is considered
  // for annotation by the TokenSequenceEvaluator
  public static final double SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT = 0.35;

  public static final double[] SEQUENCE_THRESHOLDS_DEFAULT = {0.35
      // 0.00, 0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50,
      // 0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85,
      // 0.90, 0.95

  };

  // String to show user when the sample fraction entered
  // for options for bootstrap analysis is not a number
  // between 0 and 1.0
  public static final String SAMPLE_FRACTION_INPUT_ERROR_STRING =
      "<html><b>Sample Fraction Input Error:</b><br><br>"
          + "Please use a fraction between 0.0 and 1.0<br><html>";

  // String to represent the start of a sentence
  public static final String SENTENCE_START_STRING = "$**START@@$";

  // String to represent the start of a sentence
  public static final String SENTENCE_END_STRING = "$**END@@$";

  public static final PhraseIDSmoothingMethod SMOOTHING_FOR_PHRASE_ID_DEFAULT =
      PhraseIDSmoothingMethod.XTREME_MOD;

  // Name of extension used to store "solution files"
  public static final String SOLUTION_EXTENSION = ".soln";

  public static final String SOLUTION_FILE_START = "annotator_solutions.sln";

  // Modified for i2b2 Testing
  // public static final String[] STOP_WORDS =
  // {
  // "of", "and", "with", "for", "nos", "to", "in", "by", "on", "the", "a",
  // "an"
  // };

  // Pattern used to split text phrases into elements
  public static final String SPLITTER_REGEX = "\\s\\.\\.\\.\\s|[\\s,]+";

  // Modified for Cirrhosis Study Testing on 11-27-17
  public static final String[] STOP_WORDS =
      {"of", "and", "nos", "to", "by", "on", "the", "a", "an"};

  // Determines whether disk storage is used during training
  // to hold unlabeled hash trees
  public static final boolean STORE_UNLABELED_ON_DISK_DURING_TRAINING = false;

  /*
   * Determines whether attribute values are appended to the concepts so that rather than finding
   * and mapping just the concept, one is finding and mapping the combination of concept and
   * attribute values.
   */
  public static final boolean TAG_CONCEPT_WITH_ATTRIBUTE_VALUES = true;

  /*
   * List of attributes that should not be included when tagging concepts with attribute values
   */
  public static final String[] EXCLUDED_ATTRIBUTE_TAGS =
      {"assertionstatus", "experincer", "timeframe"};

  // Minimum height for testing options window
  public static final int TEST_OPTIONS_MIN_HEIGHT = 270;

  // Minimum height for testing options window
  public static final int TEST_OPTIONS_MIN_WIDTH = 600;

  // Estimated number of tokens per document
  public static final int TOKENS_PER_DOCUMENT = 5000;

  // Name used to refer to total number concepts that overlap exactly
  public static final String TOTAL_EXACT_MATCH_NAME = "total (exactphrasematches)";

  // Name used to refer to total number concepts that overlap exactly
  public static final String TOTAL_PARTIAL_MATCH_NAME = "total (partialphrasematches)";

  // Flag to determine if we should keep track of certain items, such as
  // duration
  public static final boolean TRACK_DURATION = true;

  // Minimum height for training options window
  public static final int TRAIN_OPTIONS_MIN_HEIGHT = 425;

  // Minimum width for tagger training options window
  public static final int TAGGER_TRAIN_OPTIONS_MIN_WIDTH = 600;

  // Tag used in the sentenceAssociatedConcepts fields of sentences that are
  // part of templates
  public static final String TEMPLATE_SENTENCE_TAG = "TemplateSentence";

  // Minimum width for training options window
  public static final int TRAIN_OPTIONS_MIN_WIDTH = 360;

  // Minimum height for training window
  public static final int TRAIN_WINDOW_MIN_HEIGHT = 700;

  // Minimum width for training window
  public static final int TRAIN_WINDOW_MIN_WIDTH = 900;

  // Indicator of whether to substitute nulls for phrases less than 7 words
  // long
  public static final boolean USE_NULLS_START = false;

  // Value set by the training window-user selection
  public static boolean USE_TOKEN_STEMMING = false;

  // Value set by the training window-user selection
  public static boolean USE_PARTS_OF_SPEECH = false;

  // Determines whether to use the negation context of a token
  // for mapping and phrase identification
  public static final boolean USE_NEGATION_CONTEXT_DEFAULT = false;;

  /*
   * Determines whether we are using RapTAT with XML from eHOST, because eHOST does not handle UTF,
   * multi-byte characters. This constant is used during import.
   */
  public static final boolean USE_WITH_EHOST_XML = true;;

  // Initializes the list of names of text files annotated by raptat
  public static List<String> RPT_ANNOTATED_FILENAMES = null;

  // Initializes the list of paths of text files annotated by raptat
  public static List<String> RPT_ANNOTATED_FILEPATHS = null;;

  // Initializes the HashMap containing the List<AnnotatedPhrase> objects for
  // each Raptat annotated file
  public static HashMap<String, List<AnnotatedPhrase>> RAPTAT_ANNOTATIONS_MAP = null;;

  public static File LAST_SELECTED_DIR = new File(System.getProperty("user.home"));;

  public static String UL_DEFAULT_PATH = null;

  public static String EHOST_FIRST_CHILD_NODE_NAME = "eHOST_Project_Configure";

  // Seperator string between multiple spans
  public static String SPAN_SEPERATOR = " ... ";

  public static final String UNCERTAIN_TAG = "UNC";

  public static final String GENERAL_TAG = "GEN";

  public static final String BEGIN_CONCEPT_LABEL = "BEGIN_CRF_TAG";

  public static final String INSIDE_CONCEPT_LABEL = "INSIDE_CRF_TAG";

  public static final String CONCEPT_CONNECTOR = "|_|";

  public static final String CUENEGATION_FEATURE_STRING = "ISNEGATIONCUE";

  public static final String CUEUNCERTAIN_FEATURE_LABEL = "ISUNCERTAINCUE";

  public static final String CUE_OFFSET_STRING = "LABELEDCUE";

  /*
   * This is a string assigned to 'modifiable' attributes when creating a set of features for
   * tokens. It is used to re-assign a cue feature later as needed when used to determine scope
   */
  public static final String MODIFIABLE_CUE_ATTRIBUTE = "MODIFIABLE_CUE_ATTRIBUTE";

  public static final String DELIMITER_CONCEPT = "|_|";

  public static final String DELIMITER_PERFORMANCE_SCORE_FIELD = "&&%__%%&";

  public static final int DEFAULT_MODIFIABLE_ATTRIBUTES_AND_LABELS = 1000;

  public static final String DEFAULT_CRF_OUTSIDE_STRING = "O";

  public static final String BEGIN_CUE_STRING = "BC";

  public static final String INSIDE_CUE_STRING = "IC";

  public static final String BEGIN_SCOPE_STRING = "BS";

  public static final String INSIDE_SCOPE_STRING = "IS";

  public static final String SCOPE_FEATURE_TAG = "SF_TAG";

  public static final int DEFAULT_TRAINING_TOKEN_NUMBER = 1000000;

  /* Size of conjunctions of words to use for CRF training */
  public static final int WORD_CONJUNCTION_SIZE_DEFAULT = 4;

  /* Distance of offset of words to create conjunctions for CRF training */
  public static final int WORD_OFFSET_DISTANCE_DEFAULT = 4;

  public static final String TAGGER_MODEL_FILE_NAME = "mayo-pos.zip";
  // public static final String TAGGER_MODEL_FILE_NAME = "en-pos-maxent.bin";

  /* Size of conjunctions of parts of speech to use for CRF training */
  public static final int POS_CONJUNCTION_SIZE_DEFAULT = 4;

  /*
   * Distance of offset of parts of speech to create conjunctions for CRF training
   */
  public static final int POS_OFFSET_DISTANCE_DEFAULT = 4;

  public static final String NULL_ATTRIBUTE_VALUE = "&&_%null_att_val%%_&";

  public static final int CUE_OFFSET_DISTANCE_FEATURE_MAX = 8;

  public static final boolean STORE_CROSS_VALIDATION_RESULTS_AS_XML = true;

  public static final String MED_SECTION_TAG = "MED_SECTION_TAG";

  public static final Object EHOST_ADJUDICATED_MATCH_DELETED_MARKER = "MATCHES_DLETED";
}
