package src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class PhraseIDCrossValidationSampler
    implements Iterable<RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>>>, Serializable {
  private class CrossValidationIterator
      implements Iterator<RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>>> {
    private int positionInList = 0;

    /*
     * Determines how many sample groups are used for training. If set to 0, then we are using
     * leave-one-out cross-validation in which the trainingGroupSize is all but one sample group.
     * That is, we are just leaving one group out for testing.
     */
    private final int trainingGroupSize;


    /** Default to leave-one-out cross-validation */
    public CrossValidationIterator() {
      this.trainingGroupSize = PhraseIDCrossValidationSampler.this.cvSampleList.size() - 1;
    }


    /**
     * @param trainingGroupSize
     */
    public CrossValidationIterator(int trainingGroupSize) {
      this.trainingGroupSize = trainingGroupSize;
    }


    @Override
    public boolean hasNext() {
      return this.positionInList < PhraseIDCrossValidationSampler.this.cvSampleList.size();
    }


    @Override
    public RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>> next() {
      int listSizeEstimate = 512;
      Set<String> trainingGroupKeys = new HashSet<>(this.trainingGroupSize);
      List<AnnotationGroup> trainingGroups = new ArrayList<>(listSizeEstimate);

      int maxGroupNumber = PhraseIDCrossValidationSampler.this.cvSampleMap.size();
      for (int i = 0; i < this.trainingGroupSize; i++) {
        int groupNumber = (this.positionInList + i) % maxGroupNumber;
        String groupKey = PhraseIDCrossValidationSampler.this.cvSampleList.get(groupNumber);
        trainingGroupKeys.add(groupKey);
        trainingGroups.addAll(PhraseIDCrossValidationSampler.this.cvSampleMap.get(groupKey));
      }

      this.positionInList++;
      trainingGroups.addAll(PhraseIDCrossValidationSampler.this.trainingOnlyGroups);

      List<AnnotationGroup> testingGroups = new ArrayList<>(listSizeEstimate);

      Set<String> sampleKeys = PhraseIDCrossValidationSampler.this.cvSampleMap.keySet();
      for (String key : sampleKeys) {
        if (!trainingGroupKeys.contains(key)) {
          testingGroups.addAll(PhraseIDCrossValidationSampler.this.testingGroupMap.get(key));
        }
      }

      return new RaptatPair<>(trainingGroups, testingGroups);
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static final long serialVersionUID = -6802653982525669450L;

  private static Logger LOGGER = Logger.getLogger(PhraseIDCrossValidationSampler.class);

  /*
   * This is a list of the groups to be sampled with 1 entry per group. The entries in this list can
   * be used as keys to pull out xml and text files from xmlGroups and textGroups fields
   */
  private final ArrayList<String> cvSampleList = new ArrayList<>();

  /*
   * Each sample group is stored via a hash that returns a list of AnnotationGroup instances
   * corresponding to the group. The reference annotations in the AnnotationGroup objects are used
   * for training each fold of the cross-validation.
   */
  private final Hashtable<String, List<AnnotationGroup>> cvSampleMap = new Hashtable<>();

  /*
   * These are AnnotationGroup instances that are used only for training during cross-validation and
   * never tested. This may be empty if none are specified. AnnotationGroup instances that are
   * specified when creating this sampler are stored here and NOT in the cvSampleMap
   */
  private final List<AnnotationGroup> trainingOnlyGroups = new ArrayList<>();

  /*
   * These are AnnotationGroup instances used only for testing, such as when you want to see whether
   * a set of annotations will train the system to reproduce another set of annotations such as a
   * well-established reference standard.
   */
  private final Hashtable<String, List<AnnotationGroup>> testingGroupMap = new Hashtable<>();

  private AnnotationImporter theImporter = null;

  private TextAnalyzer textAnalyzer;

  private String trainingFileDirectory = System.getProperty("user.home");

  private boolean testSampleDistinct = false;


  /**
   * @param textAnalyzer
   */
  public PhraseIDCrossValidationSampler(AnnotationApp annotationApp) {
    this();

    if (annotationApp != null) {
      this.theImporter = new AnnotationImporter(annotationApp);
    } else {
      this.theImporter = AnnotationImporter.getImporter();
    }
  }


  /**
   * @param textAnalyzer
   */
  public PhraseIDCrossValidationSampler(TextAnalyzer textAnalyzer) {
    this();
    this.textAnalyzer = textAnalyzer;
    this.theImporter = AnnotationImporter.getImporter();
  }


  /**
   * @param textAnalyzer
   */
  public PhraseIDCrossValidationSampler(TextAnalyzer textAnalyzer, AnnotationApp annotationApp) {
    this();
    this.textAnalyzer = textAnalyzer;

    if (annotationApp != null) {
      this.theImporter = new AnnotationImporter(annotationApp);
    } else {
      this.theImporter = AnnotationImporter.getImporter();
    }
  }


  private PhraseIDCrossValidationSampler() {
    PhraseIDCrossValidationSampler.LOGGER.setLevel(Level.INFO);
  }


  public List<AnnotationGroup> getAllAnnotationGroups() {
    ArrayList<AnnotationGroup> results = new ArrayList<>();
    for (String curKey : this.cvSampleList) {
      results.addAll(this.cvSampleMap.get(curKey));
      if (this.testSampleDistinct) {
        results.addAll(this.testingGroupMap.get(curKey));
      }
    }
    results.addAll(this.trainingOnlyGroups);
    return results;
  }


  /**
   * *********************************************************** This generates the number of groups
   *
   * @return - the number of groups
   * @author Glenn Gobbel - Jul 24, 2012 ***********************************************************
   */
  public int getNumberOfSamples() {
    return this.cvSampleList.size();
  }


  public List<AnnotationGroup> getSampleAnnotationGroups() {
    ArrayList<AnnotationGroup> results = new ArrayList<>();
    for (String curKey : this.cvSampleList) {
      results.addAll(this.cvSampleMap.get(curKey));
    }
    return results;
  }


  /**
   * @param schemaConcepts
   * @param trainingConcepts
   * @param scoringConcepts
   * @param samplingDataFilePath
   * @param separateTestingDataSet
   * @param testingDataDirectoryPath
   * @return
   */
  public boolean getSamplingData(List<SchemaConcept> schemaConcepts,
      HashSet<String> trainingConcepts, HashSet<String> scoringConcepts,
      String samplingDataFilePath, Boolean separateTestingDataSet,
      String testingDataDirectoryPath) {

    /*
     * Get the data and where it's stored as a .csv file, which needs to be in the same directory as
     * the text and xml files being analyzed.
     */
    CSVReader dataReader = null;
    if (samplingDataFilePath != null) {
      dataReader = new CSVReader(new File(samplingDataFilePath));
    }

    if (dataReader == null || !dataReader.isValid()) {
      dataReader =
          new CSVReader(OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath(), false,
              "Select .csv " + "file containing training data samples");
    }

    /*
     * Return false if dataReader is not valid or user cancels out of JOption dialog
     */
    if (!dataReader.isValid()) {
      return false;
    }

    this.trainingFileDirectory = dataReader.getInFileDirectory();
    PhraseIDCrossValidationSampler.LOGGER
        .info("Generating cross-validation data set from files in " + this.trainingFileDirectory);

    File importErrorFile = new File(this.trainingFileDirectory + File.separator + "ImportErros_"
        + GeneralHelper.getTimeStamp() + ".txt");
    PrintWriter importErrorPrinter = null;
    try {
      importErrorPrinter = new PrintWriter(importErrorFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    /*
     * This option was added for the case when the training data may contain "dictionary" data that
     * we don't want to use for testing or any time we need to train and test on separate data
     */
    int testingOption = JOptionPane.CANCEL_OPTION;

    /*
     * If there is no clear indication of whether there is another separate testing data set or
     * there should be but it's unspecified, check to see if the user wants to add one.
     */
    if (separateTestingDataSet == null || separateTestingDataSet == true
        && (testingDataDirectoryPath == null || testingDataDirectoryPath.isEmpty())) {
      testingOption = JOptionPane.showConfirmDialog(null, "Run testing on separate data set?", "",
          JOptionPane.YES_NO_CANCEL_OPTION);
    }
    /*
     * Automatically set testing data to yes if separateTestingDataSet is true and there is any
     * information in the testDataDirectoryPath variable.
     */
    else if (separateTestingDataSet == true && testingDataDirectoryPath != null
        && !testingDataDirectoryPath.isEmpty()) {
      testingOption = JOptionPane.YES_OPTION;
    } else {
      testingOption = JOptionPane.NO_OPTION;
    }

    if (testingOption != JOptionPane.CANCEL_OPTION) {
      return addCrossValidationSamples(dataReader, schemaConcepts, trainingConcepts,
          scoringConcepts, importErrorPrinter, testingOption, testingDataDirectoryPath);
    }

    return false;
  }


  public String getTrainingFileDirectory() {
    return this.trainingFileDirectory;
  }


  // /**
  // * @param schemaConcepts
  // * @param acceptedConcepts
  // * @return
  // *
  // */
  // public boolean getSamplingData(List<SchemaConcept> schemaConcepts,
  // HashSet<String> acceptedConcepts)
  // {
  // return getSamplingData( schemaConcepts, acceptedConcepts, null, null );
  // }

  @Override
  public Iterator<RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>>> iterator() {
    return new CrossValidationIterator();
  }


  public Iterator<RaptatPair<List<AnnotationGroup>, List<AnnotationGroup>>> iterator(
      int trainingGroupSize) {
    /*
     * Default to leave-one-out cross-validation if trainingGroupSize is not a positive integer
     */
    if (trainingGroupSize < 1) {
      return new CrossValidationIterator();
    }
    return new CrossValidationIterator(trainingGroupSize);
  }


  /**
   * @param dataReader
   * @param schemaConcepts
   * @param trainingConcepts
   * @param importErrorPrinter
   * @param testingOption
   * @return
   */
  private boolean addCrossValidationSamples(CSVReader dataReader,
      List<SchemaConcept> schemaConcepts, HashSet<String> trainingConcepts,
      HashSet<String> scoringConcepts, PrintWriter importErrorPrinter, int testingOption,
      String testingDataDirectoryPath) {
    String[] nextDataSet;
    this.testSampleDistinct = testingOption == JOptionPane.YES_OPTION;

    if (this.testSampleDistinct) {
      if (testingDataDirectoryPath == null || testingDataDirectoryPath.isEmpty()) {
        testingDataDirectoryPath = GeneralHelper
            .getDirectory("Select directory containing test xml files", this.trainingFileDirectory)
            .getPath();

        if (testingDataDirectoryPath == null) {
          return false;
        }
      }
    } else {
      testingDataDirectoryPath = this.trainingFileDirectory;
    }

    /*
     * Create a data structure to easily generate bootstrap or cross-validation sets for training
     * and testing
     */
    while ((nextDataSet = dataReader.getNextData()) != null) {
      /*
       * Check to make sure line has all needed data
       */
      if (nextDataSet.length > 2) {
        addSample(nextDataSet, schemaConcepts, trainingConcepts, scoringConcepts,
            this.trainingFileDirectory, testingDataDirectoryPath, importErrorPrinter);
      }
    }

    return true;
  }


  /**
   * *********************************************************** Imports the text documents,
   * creating a RaptatDocument instance, and the annotations of that document. These are stored in
   * the fields sampleGroups, trainingOnlyGroups, and testGroups.
   *
   * <p>
   * Note that this does not attach tokens to the imported annotations. This has to be performed
   * separately via a PhraseTokenIntegrator instance.
   *
   * @param dataElements
   * @param testFileDirectory
   * @param trainingFileDirectory
   * @author Glenn Gobbel - Jun 17, 2012
   * @param schemaConceptList
   * @param trainingConcepts
   * @param scoringConcepts
   * @param importErrorPrinter ***********************************************************
   */
  private void addSample(String[] dataElements, List<SchemaConcept> schemaConceptList,
      HashSet<String> trainingConcepts, HashSet<String> scoringConcepts,
      String trainingFileDirectory, String testFileDirectory, PrintWriter importErrorPrinter) {
    final int SAMPLE_GROUP_INDEX = 0;
    final int DOC_PATH_INDEX = 1;
    final int XML_PATH_INDEX = 2;
    final int USE_FOR_TRAINING_AND_TESTING_INDEX = 3;
    PhraseTokenIntegrator tokenIntegrator = new PhraseTokenIntegrator();

    String crossValidationSampleGroup = dataElements[SAMPLE_GROUP_INDEX];

    /*
     * If there is a 4th element in the elements, it is used to indicate whether the document
     * associated with a given row is to be used for training only and not used during test. This is
     * done when we want pre-populate our training with example annotations. Note that the default
     * is to use the group for both training and testing.
     */
    boolean useForTrainingAndTesting = true;
    if (dataElements.length == 4) {
      useForTrainingAndTesting =
          dataElements[USE_FOR_TRAINING_AND_TESTING_INDEX].equalsIgnoreCase("yes");
    }

    PhraseIDCrossValidationSampler.LOGGER
        .info("Creating Annotation group for Element:" + crossValidationSampleGroup);

    /*
     * If we haven't seen this group before, create a new group in the cvSampleList, cvSampleMap,
     * and testingGroupMap, but only if the group is used for both training and testing.
     */
    if (!this.cvSampleMap.containsKey(crossValidationSampleGroup) && useForTrainingAndTesting) {
      this.cvSampleMap.put(crossValidationSampleGroup, new ArrayList<AnnotationGroup>());
      this.cvSampleList.add(crossValidationSampleGroup);

      this.testingGroupMap.put(crossValidationSampleGroup, new ArrayList<AnnotationGroup>());
    }

    String trainingDocPath = trainingFileDirectory + File.separator + dataElements[DOC_PATH_INDEX];
    String trainingXMLPath = trainingFileDirectory + File.separator + dataElements[XML_PATH_INDEX];

    PhraseIDCrossValidationSampler.LOGGER
        .info("Processing document:" + dataElements[DOC_PATH_INDEX]);

    RaptatDocument theDocument = this.textAnalyzer.processDocument(trainingDocPath);

    // [[ Logger Folding
    if (PhraseIDCrossValidationSampler.LOGGER.isDebugEnabled()) {
      System.out.println("DOCUMENT SENTENCES:");
      List<AnnotatedPhrase> sentences = theDocument.getActiveSentences();
      int i = 0;
      for (AnnotatedPhrase sentence : sentences) {
        System.out.print(++i + ") ");
        sentence.print();
      }
    }
    PhraseIDCrossValidationSampler.LOGGER
        .info("Importing annotations from training XML:" + dataElements[XML_PATH_INDEX]);
    // ]] End Logger Folding

    List<AnnotatedPhrase> trainingAnnotations = this.theImporter.importAnnotations(trainingXMLPath,
        trainingDocPath, trainingConcepts, schemaConceptList, importErrorPrinter);

    tokenIntegrator.setNumberOfRawTokens(trainingAnnotations, theDocument, false);

    // [[ Logger Folding
    if (PhraseIDCrossValidationSampler.LOGGER.isDebugEnabled()) {
      System.out.println("TRAINING ANNOTATIONS:");
      int i = 0;
      for (AnnotatedPhrase curAnnotation : trainingAnnotations) {
        System.out.print(++i + ") ");
        curAnnotation.print();
      }
    }
    // ]] End Logger Folding

    /*
     * Note that if useForTrainingAndTesting is false, there is NO test sample at all for this
     * particular annotated document. If useForTrainingAndTesting is true, the XML MAY BE separate
     * from the ones used for training. The XML WILL BE separate if the testFileDirectory and the
     * trainingFileDirectory are NOT equal (testSampleDistinct is 'true').
     */
    if (useForTrainingAndTesting) {
      AnnotationGroup theSampleGroup = new AnnotationGroup(theDocument, trainingAnnotations);
      this.cvSampleMap.get(crossValidationSampleGroup).add(theSampleGroup);

      if (!this.testSampleDistinct) {
        this.testingGroupMap.get(crossValidationSampleGroup).add(theSampleGroup);
      } else {

        PhraseIDCrossValidationSampler.LOGGER
            .info("Importing annotations from test XML:" + dataElements[XML_PATH_INDEX]);

        String testXML = testFileDirectory + File.separator + dataElements[XML_PATH_INDEX];
        List<AnnotatedPhrase> testAnnotations =
            this.theImporter.importAnnotations(testXML, trainingDocPath, scoringConcepts);
        tokenIntegrator.setNumberOfRawTokens(testAnnotations, theDocument, false);

        // [[ Logger Folding
        if (PhraseIDCrossValidationSampler.LOGGER.isDebugEnabled()) {
          System.out.println("TEST ANNOTATIONS:");
          int i = 0;
          for (AnnotatedPhrase curAnnotation : testAnnotations) {
            System.out.print(++i + ") ");
            curAnnotation.print();
          }
        }
        // ]] End Logger Folding

        this.testingGroupMap.get(crossValidationSampleGroup)
            .add(new AnnotationGroup(theDocument, testAnnotations));
      }
    } else {
      this.trainingOnlyGroups.add(new AnnotationGroup(theDocument, trainingAnnotations));
    }
  }
}
