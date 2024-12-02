package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.MunkresAssignmentTest;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures.AnnotationToTokenPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * ArffExporter
 *
 * @author gtony
 * @date May 27, 2019
 */
public class ArffExporter implements AutoCloseable {

  private enum RunType {
    TEST;
  }

  private static final Logger LOGGER = Logger.getLogger(ArffExporter.class);
  private static final String CLASS_INDEX_LABEL = "<|1A&b@*/|>";

  {
    LOGGER.setLevel(Level.INFO);
  }

  private File arffFile;
  private File featureFile;
  private PrintWriter arffPrintWriter;
  private PrintWriter featurePrintWriter;
  private final String creationDate =
      new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
  private final ArffExportDescriptor exportDescriptor;

  /*
   * featureIndexMap stores all the features found when writing out the features of all the token
   * phrases from a document corpus
   */
  private final Map<String, Integer> featureIndexMap = new LinkedHashMap<>();

  private AnnotationImporter annotationImporter;

  /*
   * These are the classifications or labels used for training and cross-validation. It corresponds
   * to the possible classifications of a phrase with a given concept name, such as the
   * classifications of right and left for an index value phrase in an ABI report.
   */
  private final Set<String> phraseClassClassifications = new HashSet<>();

  /**
   * @param projectOriginName
   * @param creationDate
   * @param fileDescription
   * @param arffPrintWriter
   */
  public ArffExporter(final ArffExportDescriptor exportDescriptor, final String directoryPath)
      throws IOException {
    // this( directoryPath );
    this.exportDescriptor = exportDescriptor;
    this.phraseClassClassifications.addAll(exportDescriptor.phraseClass.getClassifications());
    this.createPrintWriters(directoryPath, exportDescriptor.phraseClass);
    this.createAnnotationImporters(null);
    this.writeArffDescription();
    this.writeArffRelation();
    this.writefeatureFileHeader();
  }

  /**
   * @param origin
   * @param creationDate
   * @param fileDescription
   * @param arffPrintWriter
   */
  public ArffExporter(final String origin, final String fileDescription,
      final PhraseClass phraseClass, final String directoryPath) throws IOException {
    /** 5/29/19 - Dax - updated to simplify calls to "this" constructor */
    this(new ArffExportDescriptor(origin, fileDescription, phraseClass), directoryPath);
  }

  @Override
  public void close() throws Exception {
    if (this.arffPrintWriter != null && this.featurePrintWriter != null) {
      this.writeAttributes();
      this.appendFiles();
    }
    if (this.arffPrintWriter != null) {
      this.arffPrintWriter.flush();
      this.arffPrintWriter.close();
    }
    if (this.featurePrintWriter != null) {
      this.featurePrintWriter.flush();
      this.featurePrintWriter.close();
    }
  }

  public List<RaptatTokenPhrase> exportDocumentPhraseFeatures(final RaptatDocument document,
      final String textFilePath, final String xmlFilePath) throws IOException {
    if (this.exportDescriptor.phraseClass == null) {
      GeneralHelper.errorWriter("Specify token phrase labels before exporting arff files");
      return null;
    }

    final List<RaptatTokenPhrase> documentPhrases = document.getTokenPhrases();


    this.writePhraseFeatures(documentPhrases, textFilePath, xmlFilePath);

    /**
     * 6/3/19 - Dax - returning phrases to keep track of them for later addition to the weka file;
     * using fact they're added in order
     */
    return documentPhrases;
  }

  /**
   * @param raptatTokenPhrases
   * @param annotatedPhrases
   */
  public int[][] getAdjacencyMatrix(final List<RaptatTokenPhrase> raptatTokenPhrases,
      final List<AnnotatedPhrase> annotatedPhrases) {
    final int[][] adjacencyMatrix = new int[raptatTokenPhrases.size()][annotatedPhrases.size()];

    int i = 0;
    for (final RaptatTokenPhrase tokenPhrase : raptatTokenPhrases) {
      int j = 0;
      for (final AnnotatedPhrase annotatedPhrase : annotatedPhrases) {
        /*
         * This could probably be sped up to more rapidly find overlapping phrases
         */
        adjacencyMatrix[i][j++] =
            AnnotationToTokenPhraseComparator.getSimilarityScore(tokenPhrase, annotatedPhrase);
      }
      i++;
    }

    return adjacencyMatrix;
  }

  public String getCreationDate() {
    return this.creationDate;
  }

  public String getFileDescription() {
    return this.exportDescriptor.fileDescription;
  }

  public String getFilePath() {
    return this.arffFile.getAbsolutePath();
  }

  private void appendFiles() throws IOException {
    this.featurePrintWriter.flush();
    this.featurePrintWriter.close();

    final FileInputStream fis = new FileInputStream(this.featureFile);
    final BufferedReader br = new BufferedReader(new InputStreamReader(fis));

    String line = null;

    while ((line = br.readLine()) != null) {

      /*
       * Need to insert statement here replacing the previous string holding a place for the class
       * attribute index value with the actual value, which should be the size of the
       * featureIndexMap (e.g. line.replace("INDEX_$VALUE",featureIndexMap.size());
       */
      line = line.replace(CLASS_INDEX_LABEL, String.valueOf(this.featureIndexMap.size()));
      this.arffPrintWriter.println(line);
      this.arffPrintWriter.flush();
    }

    br.close();
  }

  /**
   * Creates the annotation importers needed to determine the classes assigned to the instances in
   * the exported .arff file
   *
   * @param object
   */
  private void createAnnotationImporters(AnnotationApp app) {
    if (app == null) {
      app = AnnotationApp.EHOST;
    }
    this.annotationImporter = new AnnotationImporter(app);
  }

  private void createPrintWriters(final String directoryPath) throws IOException {
    createPrintWriters(directoryPath, null);
  }

  private void createPrintWriters(final String directoryPath, final PhraseClass phraseClass)
      throws IOException {
    final String phraseClassString = phraseClass == null ? "" : phraseClass.toString() + "_";
    this.arffFile = new File(directoryPath,
        "ArffABI_" + phraseClassString + GeneralHelper.getTimeStamp() + ".arff");
    this.featureFile = File.createTempFile("tokenPhrasedata_" + GeneralHelper.getTimeStamp(), "tmp",
        new File(directoryPath));
    /** 5/30/19 - Dax - Added to remove the temp file once done */
    this.featureFile.deleteOnExit();

    this.arffPrintWriter = new PrintWriter(this.arffFile);
    this.featurePrintWriter = new PrintWriter(this.featureFile);

  }

  /**
   * Takes a list of phrases and pulls out the ones with a concept name corresponding to the export
   * descriptor's phrase class for this instance of the ArffExporter class.
   *
   * @param documentPhrases
   * @return
   */
  private List<RaptatTokenPhrase> getCandidatePhrases(
      final List<RaptatTokenPhrase> documentPhrases) {
    final List<RaptatTokenPhrase> candidatePhrases = new ArrayList<>();
    final Label conceptNameLabel =
        new Label(this.exportDescriptor.phraseClass.conceptName.toLowerCase());
    for (final RaptatTokenPhrase phrase : documentPhrases) {
      if (phrase.getPhraseLabels().contains(conceptNameLabel)) {
        candidatePhrases.add(phrase);
      }
    }
    return candidatePhrases;
  }

  /**
   * @param candidatePhrases
   * @param annotatedPhrases
   * @return
   */
  private Map<RaptatTokenPhrase, String> getPhraseToClassMap(
      final List<RaptatTokenPhrase> candidatePhrases,
      final List<AnnotatedPhrase> annotatedPhrases) {

    final Map<RaptatTokenPhrase, String> resultMap = new HashMap<>(candidatePhrases.size());
    int[] candidateMatching =
        matchCandidatePhrasesToAnnotations(candidatePhrases, annotatedPhrases);

    int i = 0;
    for (final RaptatTokenPhrase tokenPhrase : candidatePhrases) {

      // Modified by Glenn 5/29/19 - replaced "NoClassAssigned" to
      // PhraseClass enum value string
      final String mappedClass =
          candidateMatching[i] == -1 ? PhraseClass.NO_CLASS_ASSIGNED.conceptName
              : annotatedPhrases.get(candidateMatching[i]).getConceptName();
      i = i + 1;
      resultMap.put(tokenPhrase, mappedClass);
    }
    return resultMap;
  }

  /**
   * Create a map that maps each candidatePhrase to its corresponding class as determined from the
   * xml file. Any phrases that have no matching phrase in the xml will be assigned "NoClassFound"
   * (or something similar).
   *
   * @param xmlPath
   * @param textPath
   * @param candidatePhrases
   * @return
   */
  private Map<RaptatTokenPhrase, String> getPhraseToClassMap(final String xmlPath,
      final String textPath, final List<RaptatTokenPhrase> candidatePhrases) {

    List<AnnotatedPhrase> phrases = this.annotationImporter.importAnnotations(xmlPath, textPath,
        this.phraseClassClassifications);
    phrases = this.mutateAndFilterPhrases(phrases);

    LOGGER.debug("GETTING PHRASE TO CLASS MAP FOR:" + textPath);

    return this.getPhraseToClassMap(candidatePhrases, phrases);
  }

  /**
   * @param candidatePhrases
   * @param annotatedPhrases
   * @return
   */
  private int[] matchCandidatePhrasesToAnnotations(final List<RaptatTokenPhrase> candidatePhrases,
      final List<AnnotatedPhrase> annotatedPhrases) {
    /*
     * The adjacency matrix relating candidate to annotatedPhrases
     */
    final int[][] adjacencyMatrix = this.getAdjacencyMatrix(candidatePhrases, annotatedPhrases);

    int[] candidateMatching = new int[0];
    if (adjacencyMatrix.length > 0) {

      if (LOGGER.isDebugEnabled()) {
        printAdjacencyMatrix(adjacencyMatrix);
      }
      final MunkresAssignmentTest mr = new MunkresAssignmentTest(adjacencyMatrix);
      candidateMatching = mr.solve();
    }
    return candidateMatching;
  }

  /*
   * Modified by Glenn 5/29/19 - added method so the method of comparing RaptatTokenPhrase labels
   * and imported annotated phrases within the AnnotationToTokenPhraseComparator methods will work
   * correctly. This filteres a list of AnnotatedPhrase instances and returns only those whose
   * concept name is the same as one of the export descriptors phrase class classifications.
   */
  private List<AnnotatedPhrase> mutateAndFilterPhrases(final List<AnnotatedPhrase> phrases) {
    final List<AnnotatedPhrase> modifiedPhrases = new ArrayList<>();

    for (final AnnotatedPhrase phrase : phrases) {
      if (this.exportDescriptor.phraseClass.getClassifications()
          .contains(phrase.getConceptName().toLowerCase())) {
        modifiedPhrases.add(phrase);
      }
    }
    return modifiedPhrases;
  }

  private void printAdjacencyMatrix(final int[][] adjacencyMatrix) {
    LOGGER.debug("AdjacencyMatrix:");
    for (int i = 0; i < adjacencyMatrix.length; i++) {
      System.out.println("\t\tRow " + i + ":" + Arrays.toString(adjacencyMatrix[i]));
    }

  }

  private void writeArffDescription() {
    this.arffPrintWriter.println("% Origin:  " + this.exportDescriptor.projectOriginName);
    this.arffPrintWriter.println("%");
    this.arffPrintWriter.println("% Creation Date:  " + this.creationDate);
    this.arffPrintWriter.println("%");
    this.arffPrintWriter.println("% Description:  " + this.exportDescriptor.fileDescription);
    this.arffPrintWriter.println("%");
    this.arffPrintWriter.flush();
  }

  private void writeArffRelation() {
    this.arffPrintWriter.println("@RELATION " + this.exportDescriptor.phraseClass.getConceptName());
    this.arffPrintWriter.println();
  }

  private void writeAttributes() {
    final List<String> featureList = new ArrayList<>(this.featureIndexMap.keySet());

    final String att = "@ATTRIBUTE\t";
    featureList.forEach(feature -> this.arffPrintWriter.println(att + feature + "\t{0, 1}"));

    // Modified by Glenn 5/29/19 - changed the code below to include the
    // possibility when
    // no class is assigned (i.e. the RaptatTokenPhrase does not correspond
    // to an imported annotated
    // phrase).
    final StringBuilder sb = new StringBuilder("{");
    sb.append(PhraseClass.NO_CLASS_ASSIGNED.conceptName);

    for (final String classification : this.exportDescriptor.phraseClass.getClassifications()) {
      sb.append(", ");
      sb.append(classification);
    }

    sb.append("}");
    this.arffPrintWriter.println(att + "class\t" + sb.toString());
  }

  private void writefeatureFileHeader() {
    this.featurePrintWriter.println();
    this.featurePrintWriter.println("@DATA");
  }

  /**
   * Writes out the features of each RaptatTokenPhrase instance as indices to a ***temporary***
   * file. These need to be written in the order specified by the tokenPhrases list parameter. The
   * code essentially writes the "bottom half" of the arff file where each row corresponds to a
   * single phrase with the features listed as indices in a common-separated list folowed by the
   * assigned class/concept of the phrase. The list of features is sparse for each phrase in that
   * only features that are present in the phrase are included.
   *
   * @param tokenPhrases
   * @param phraseToClassMap
   */
  private void writePhraseFeatures(final List<RaptatTokenPhrase> tokenPhrases,
      final Map<RaptatTokenPhrase, String> phraseToClassMap) {
    for (final RaptatTokenPhrase tokenPhrase : tokenPhrases) {
      this.featurePrintWriter.print("{");
      final TreeSet<Integer> featureIndicesForPhrase = new TreeSet<>();

      for (final String feature : tokenPhrase.getPhraseFeatures()) {
        this.featureIndexMap.putIfAbsent(feature, this.featureIndexMap.size());
        final Integer featureIndex = this.featureIndexMap.get(feature);
        /**
         * 5/29/19 - Dax - Error from arff reader, "indices have to be ordered", so imposing order
         * before writing
         */
        featureIndicesForPhrase.add(featureIndex);
      }

      for (final Integer featureIndex : featureIndicesForPhrase) {
        this.featurePrintWriter.print(featureIndex + " " + "1, ");
        this.featurePrintWriter.flush();
      }

      /** 5/29/19 - Dax - Updated to attempt to get the last attribute entry */
      this.featurePrintWriter.print(CLASS_INDEX_LABEL + " " + phraseToClassMap.get(tokenPhrase));
      this.featurePrintWriter.println("}");
      this.featurePrintWriter.flush();
    }
  }

  /**
   * Write out the features
   *
   * @param documentPhrases
   * @param textFilePath
   * @param xmlFilePath
   */
  private void writePhraseFeatures(final List<RaptatTokenPhrase> documentPhrases,
      final String textFilePath, final String xmlFilePath) {
    /*
     * Get the phrases with a phrase class classification in the phraseClass set of this
     * ArffExporter instance. Note that the candidatePhrase list will contain the matching phrases
     * in the same order as they appear in the documentPhrases list. They are "candidates" for
     * matching to the annotations described by the xml.
     */
    final List<RaptatTokenPhrase> candidatePhrases = this.getCandidatePhrases(documentPhrases);

    /*
     * Create a map that maps each candidatePhrase to its corresponding class as determined from the
     * xml file. Any phrases that have no matching phrase in the xml will be assigned "NoClassFound"
     * (or something similar).
     */
    final Map<RaptatTokenPhrase, String> phraseToClassMap =
        this.getPhraseToClassMap(xmlFilePath, textFilePath, candidatePhrases);

    /*
     * Writes out the features of the phrases that match when comparing candidate RaptatTokenPhrase
     * instances to the AnnotatedPhrase instances imported from the xml file.
     */
    this.writePhraseFeatures(candidatePhrases, phraseToClassMap);
  }

  public static void main(final String[] args) {

    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final RunType runType = RunType.TEST;
        final String arffFilePath = "";
        final String origin = "Girotra ABI Project";
        final String fileDescription = "Contains training data for use in determining left/right,\n"
            + "ABI/TBI index values and compressibility for ankle-brachial index tests";
        final ArffExportDescriptor exportDescriptor =
            new ArffExportDescriptor(origin, fileDescription, PhraseClass.INDEX_VALUE_TYPE);
        final RaptatDocument document = new RaptatDocument();
        final String textFilePath = "";
        final String xmlFilePath = "";

        switch (runType) {
          case TEST:
            this.testArffExporter(exportDescriptor, arffFilePath, document, textFilePath,
                xmlFilePath);
            break;
          default:
            break;
        }

        System.exit(0);
      }

      private void testArffExporter(final ArffExportDescriptor exportDescriptor,
          final String arffFilePath, final RaptatDocument document, final String textFilePath,
          final String xmlFilePath) {
        try (ArffExporter exporter = new ArffExporter(exportDescriptor, arffFilePath)) {
          final List<RaptatTokenPhrase> exportDocumentPhraseFeatures =
              exporter.exportDocumentPhraseFeatures(document, textFilePath, xmlFilePath);
        } catch (final Exception exception) {
          System.err.println("Unable to create ARFF exporter");
          exception.printStackTrace();
        }
      }
    });
  }
}
