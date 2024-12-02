package src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * ****************************************************** Instances of this class test the
 * performance of the RapTAT application with respect to its ability to identify phrases for
 * annotation and map them to the proper concepts as defined by a user. The analysis score returned
 * includes both exact and partial phrase matching, which indicates the ability to identify token
 * sequences for annotation. Exact matches overlap by the same, exact number of tokens. Partial
 * matches overlap by 1 or more tokens, so an exact match is also a partial match. Lastly, it
 * determines the ability of the tool to map identified phrases to correct concepts. If a phrase is
 * a partial phrase match and maps to the correct concept, it is considered a "true positive." If a
 * phrase is a partial match but maps to the incorrect concept, it is scored as a "false positive"
 * for the incorrect concept and a "false negative" for the concept that the tool should have mapped
 * the phrase to. All phrases that are completely missed by the phrase matching part of the tool are
 * considered "false negatives" for the concept they match to. Similarly, all phrases that are
 * identified but should not map to anything are considered "false positives."
 *
 * @author Glenn Gobbel - Jul 8, 2012 *****************************************************
 */
public class PhraseIDBootstrapAnalyzer {
  private class BootstrapSampler<T extends Object> {
    protected ArrayList<String> theGroups;
    protected Hashtable<String, List<T>> subGroups;
    protected int sampleSize = 0; // Default of 0 if using the number of
    // groups
    protected Random generator;
    protected long seed;


    public BootstrapSampler() {}


    /**
     * ********************************************
     *
     * @param seed - For creating a random number generator.
     * @author Glenn Gobbel - Jun 19, 2012 ********************************************
     */
    public BootstrapSampler(long seed, int sampleSize) {
      // This is a list of the groups to be sampled with
      // 1 entry per group. The entries in this list
      // can be used as hashes to pull out xml and
      // text files from xmlGroups and textGroups fields
      this.theGroups = new ArrayList<>();

      // Each group is stored here as a hash that returns
      // a set of elements within the group, namely
      // documents and xml files
      this.subGroups = new Hashtable<>();

      // Random number generator for creating bootstrap
      // samples
      this.seed = seed;
      this.generator = new Random(seed);

      // If this is zero, the sampleSize is equal
      // to the number of groups
      this.sampleSize = sampleSize;
    }


    /**
     * *********************************************************** Create a hash in which the first
     * element hashes to the a string array containing full paths to the text document and the xml
     * file
     *
     * @param theElements
     * @param theDirectory
     * @author Glenn Gobbel - Jun 17, 2012
     *         ***********************************************************
     */
    @SuppressWarnings("unchecked")
    public void add(String[] theElements, String theDirectory) {
      String[] theFiles;
      String groupingElement = theElements[0];
      if (!this.subGroups.containsKey(groupingElement)) {
        List<T> subGroupList = new ArrayList<>();
        this.subGroups.put(groupingElement, subGroupList);
        this.theGroups.add(groupingElement);
      }
      theFiles = GeneralHelper.getSubArray(theElements, 1);
      theFiles[0] = theDirectory + File.separator + theFiles[0];
      theFiles[1] = theDirectory + File.separator + theFiles[1];
      this.subGroups.get(groupingElement).add((T) theFiles);
    }


    public List<T> getBootstrapSample() {
      ArrayList<T> results = new ArrayList<>();
      int groups = this.theGroups.size();
      int numberOfSamples = this.sampleSize == 0 ? groups : this.sampleSize;
      for (int i = 0; i < numberOfSamples; i++) {
        // Add all elements mapped to by the element chosen
        // by the generator to the results list
        results.addAll(this.subGroups.get(this.theGroups.get(this.generator.nextInt(groups))));
      }
      return results;
    }


    public List<T> getEntireSample() {
      ArrayList<T> results = new ArrayList<>();
      for (String curKey : this.theGroups) {
        results.addAll(this.subGroups.get(curKey));
      }
      return results;
    }


    /**
     * *********************************************************** Resets random sample generator to
     * previous state before starting any runs
     *
     * @author Glenn Gobbel - Jul 3, 2012
     *         ***********************************************************
     */
    public void resetGenerator() {
      this.generator = new Random(this.seed);
    }
  }

  private class BootstrapSamplerCached extends BootstrapSampler<AnnotationGroup> {
    private final AnnotationImporter theImporter = AnnotationImporter.getImporter();
    private final TextAnalyzer theAnalyzer = new TextAnalyzer();
    private final HashSet<String> acceptedConcepts;


    /**
     * ********************************************
     *
     * @param seed - For creating a random number generator.
     * @author Glenn Gobbel - Jun 19, 2012
     * @param acceptedConcepts ********************************************
     */
    public BootstrapSamplerCached(HashSet<String> acceptedConcepts, long seed, int sampleSize) {
      super();

      this.acceptedConcepts = acceptedConcepts;

      // This is a list of the groups to be sampled with
      // 1 entry per group. The entries in this list
      // can be used as hashes to pull out xml and
      // text files from xmlGroups and textGroups fields
      this.theGroups = new ArrayList<>();

      // Each group is stored here as a hash that returns
      // a set of elements within the group, namely
      // documents and xml files
      this.subGroups = new Hashtable<>();

      // Random number generator for creating bootstrap
      // samples
      this.seed = seed;
      this.generator = new Random(seed);

      // If this is zero, the sampleSize is equal
      // to the number of groups
      this.sampleSize = sampleSize;
    }


    /**
     * *********************************************************** Create a hash in which the first
     * element hashes to the a string array containing full paths to the text document and the xml
     * file
     *
     * @param theElements
     * @param theDirectory
     * @author Glenn Gobbel - Jun 17, 2012
     *         ***********************************************************
     */
    @Override
    public void add(String[] theElements, String theDirectory) {
      String[] theFiles;
      String groupingElement = theElements[0];
      if (!this.subGroups.containsKey(groupingElement)) {
        List<AnnotationGroup> subGroupList = new ArrayList<>();
        this.subGroups.put(groupingElement, subGroupList);
        this.theGroups.add(groupingElement);
      }
      theFiles = GeneralHelper.getSubArray(theElements, 1);
      theFiles[0] = theDirectory + File.separator + theFiles[0];
      theFiles[1] = theDirectory + File.separator + theFiles[1];

      // GeneralHelper.tic("Processing document " + curPath[0]);
      RaptatDocument theDocument = this.theAnalyzer.processDocument(theFiles[0]);
      // GeneralHelper.toc();

      // GeneralHelper.tic("Importing annotations");
      List<AnnotatedPhrase> referenceAnnotations =
          this.theImporter.importAnnotations(theFiles[1], theFiles[0], this.acceptedConcepts);
      // GeneralHelper.toc();

      this.subGroups.get(groupingElement)
          .add(new AnnotationGroup(theDocument, referenceAnnotations));
    }


    public void close() {
      // theAnalyzer.destroy();
    }
  }

  /*
   * These are bit placements that specify settings of binary meta-parameters. Used to run through
   * set of integers and generate performance based on a full range of possible combinations of
   * meta-parameter settings. For example, if the integer used to determine settings was 3, which
   * corresponds to a binary value of 011, the analysis would be run with an inverted token sequence
   * (invertTokenSequenceMask bit set 1) and lemmatization (lemmatization bit set) of the token
   * strings.
   */
  protected static final int invertTokenSequenceMask = 1;

  protected static final int lemmatizationMask =
      2 * PhraseIDBootstrapAnalyzer.invertTokenSequenceMask;
  protected static final int posMask = 2 * PhraseIDBootstrapAnalyzer.lemmatizationMask;
  protected static final int stopWordsMask = 2 * PhraseIDBootstrapAnalyzer.posMask;
  protected static final int xtremeMask = 2 * PhraseIDBootstrapAnalyzer.stopWordsMask;
  protected static final int sevenMaxTokensMask = 2 * PhraseIDBootstrapAnalyzer.xtremeMask;
  protected static final int tenMaxTokensMask = 2 * PhraseIDBootstrapAnalyzer.sevenMaxTokensMask;

  private static final long seedSetting = 18492049756L;

  protected static Logger LOGGER = Logger.getLogger(PhraseIDBootstrapAnalyzer.class);


  protected boolean useStems, usePOS, removeStopWords, invertTokenSequence;

  protected int maxTokenNumber;

  protected ContextHandling reasonNotOnMedsProcessing =
      OptionsManager.getInstance().getReasonNoMedsProcessing();
  protected ContextHandling negationProcessing =
      OptionsManager.getInstance().getNegationProcessing();
  public PhraseIDSmoothingMethod smoothingMethod;
  protected TSFinderMethod phraseIDMethod, priorTokenTrainingMethod, postTokenTrainingMethod;

  protected int surroundingTokensToUse;

  protected boolean linkPhraseToPriorTokens, linkPhraseToPostTokens;

  public PhraseIDBootstrapAnalyzer() {
    PhraseIDBootstrapAnalyzer.LOGGER.setLevel(Level.INFO);
  }


  public PhraseIDBootstrapAnalyzer(TSFinderMethod phraseIDTrainingMethod,
      PhraseIDSmoothingMethod smoothingMethod, TSFinderMethod priorTokenTrainingMethod,
      TSFinderMethod postTokenTrainingMethod, int surroundingTokensToUse,
      boolean linkPhraseToPriorTokens, boolean linkPhraseToPostTokens) {
    this();
    PhraseIDBootstrapAnalyzer.LOGGER.info("Constructing PhraseIDAnalyzer");
    this.phraseIDMethod = phraseIDTrainingMethod;
    this.smoothingMethod = smoothingMethod;
    this.priorTokenTrainingMethod = priorTokenTrainingMethod;
    this.postTokenTrainingMethod = postTokenTrainingMethod;
    this.surroundingTokensToUse = surroundingTokensToUse;
    this.linkPhraseToPriorTokens = linkPhraseToPriorTokens;
    this.linkPhraseToPostTokens = linkPhraseToPostTokens;
  }


  /**
   * *********************************************************** Uses bit values to determine option
   * settings for running the training and testing during bootstrapping. It runs through all options
   * settings if it is called 16 times as there are 4 binary options, as set by the binary numbers
   * from 0000 to 1111
   *
   * @author Glenn Gobbel - Jun 18, 2012 ***********************************************************
   */
  public String setNextOptions(int optionsValue) {
    optionsValue %= 192;
    String optionsString = "Bootstrap";

    // '&' is the 'and' bit operator
    this.invertTokenSequence =
        (optionsValue & PhraseIDBootstrapAnalyzer.invertTokenSequenceMask) > 0 ? true : false;
    this.useStems = (optionsValue & PhraseIDBootstrapAnalyzer.lemmatizationMask) > 0 ? true : false;
    this.usePOS = (optionsValue & PhraseIDBootstrapAnalyzer.posMask) > 0 ? true : false;
    this.removeStopWords =
        (optionsValue & PhraseIDBootstrapAnalyzer.stopWordsMask) > 0 ? true : false;

    // If optionsValue below 8, use unsmoothed, 8-15 use extended, 16-23 use
    // xtreme,
    // // and 24-31 use xtreme modified.
    // if ( ( optionsValue & xtremeMask ) > 0 )
    // {
    // if ( ( optionsValue & extendedMask ) > 0 )
    // {
    // smoothingMethod = PhraseIDSmoothing.XTREME_MOD;
    // optionsString += "XM_";
    // }
    // else
    // {
    // smoothingMethod = PhraseIDSmoothing.XTREME;
    // optionsString += "X_";
    // }
    // }
    // else if ( ( optionsValue & extendedMask ) > 0 )
    // {
    // smoothingMethod = PhraseIDSmoothing.EXTENDED;
    // optionsString += "E_";
    // }
    // else
    // {
    // smoothingMethod = PhraseIDSmoothing.UNSMOOTHED;
    // optionsString += "U_";
    // }

    if ((optionsValue & PhraseIDBootstrapAnalyzer.xtremeMask) > 0) {

      this.smoothingMethod = PhraseIDSmoothingMethod.XTREME_MOD;
      optionsString += "XM_";

    } else {
      this.smoothingMethod = PhraseIDSmoothingMethod.XTREME;
      optionsString += "X_";
    }

    if ((optionsValue & PhraseIDBootstrapAnalyzer.sevenMaxTokensMask) > 0) {
      this.maxTokenNumber = 7;
    } else if ((optionsValue & PhraseIDBootstrapAnalyzer.tenMaxTokensMask) > 0) {
      this.maxTokenNumber = 10;
    } else {
      this.maxTokenNumber = 3;
    }

    // Return string that represents the settings of the options currently
    // used. Upper case stands for ones that are true, and lower case
    // stands for those that are false
    optionsString += Integer.toString(this.maxTokenNumber) + (this.useStems ? "S" : "s")
        + (this.usePOS ? "P" : "p") + (this.removeStopWords ? "R" : "r")
        + (this.invertTokenSequence ? "I" : "i");

    System.out.println("Current options:" + optionsString);
    return optionsString;
  }


  private void cleanUp(String fileDirectory) {
    File theDirectory = new File(fileDirectory);
    String[] fileTypes = {".ser", ".rpt", ".xml", ".soln"};
    File[] filesForDeletion = theDirectory.listFiles(GeneralHelper.getFileFilter(fileTypes));
    for (File curFile : filesForDeletion) {
      curFile.delete();
    }
  }


  public static void main(String[] args) {
    PhraseIDBootstrapAnalyzer theBootstrapper = new PhraseIDBootstrapAnalyzer();
    BootstrapSamplerCached sampler = theBootstrapper.new BootstrapSamplerCached(null, 0, 0);
    System.out.println("Complete");
  }
}
