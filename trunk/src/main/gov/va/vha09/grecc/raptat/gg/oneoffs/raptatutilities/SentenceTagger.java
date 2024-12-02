package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.JdomXMLImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class SentenceTagger {
  protected static Logger logger = Logger.getLogger(SentenceTagger.class);

  private static TextAnalyzer theTextAnalyzer = new TextAnalyzer();
  private static Long annotationID = System.currentTimeMillis();

  private static final String BEGIN_TAG = "_BeginSentence";
  private static final String END_TAG = "_EndSentence";


  private File xmlReadDirectory = new File(
      "D:\\CARTCL-IIR\\Glenn\\AssertionAnnotationDevelopment\\AllFernAdjudicatedAKIBlocks\\AllUnfilteredXML");

  private File textReadDirectory = new File(
      "D:\\CARTCL-IIR\\Glenn\\AssertionAnnotationDevelopment\\AllFernAdjudicatedAKIBlocks\\AllCorpusDocs");
  private File xmlWriteDirectory = new File("D:\\GobbelWorkspace\\temp");

  public SentenceTagger() {
    SentenceTagger.logger.setLevel(Level.INFO);
  }


  public File getTextReadDirectory() {
    return this.textReadDirectory;
  }


  public File getXmlReadDirectory() {
    return this.xmlReadDirectory;
  }


  public File getXmlWriteDirectory() {
    return this.xmlWriteDirectory;
  }


  public List<AnnotationGroup> importFiles(List<String> xmlPaths, List<String> textPaths) {
    AnnotationImporter theImporter = AnnotationImporter.getImporter();

    /* Build a hashtable to quickly look up file paths */
    Hashtable<String, String> textPathSet = new Hashtable<>(textPaths.size());
    for (String curTextPath : textPaths) {
      /*
       * Create a hashtable where the last item in the file path is used to hash to the full file
       * path
       */
      textPathSet.put(new File(curTextPath).getName(), curTextPath);
    }

    /*
     * Now build a list of string [] containing the xml path at index 0 and the text path at index
     * 1.
     */
    List<AnnotationGroup> trainingGroups = new ArrayList<>(textPathSet.size());
    for (String curXMLPath : xmlPaths) {
      String curXMLSource = JdomXMLImporter.getTextSource(curXMLPath);

      /*
       * Only process XML documents where text source document is contained with the set of
       * documents being processed (i.e. in textPathSet)
       */
      if (textPathSet.containsKey(curXMLSource)) {
        String curTextPath = textPathSet.get(curXMLSource);

        // Note that the parameters passed to the constructor will be
        // used for processing
        // unless the default constructor is used
        RaptatDocument theDocument = SentenceTagger.theTextAnalyzer.processDocument(curTextPath);
        List<AnnotatedPhrase> referenceAnnotations =
            theImporter.importAnnotations(curXMLPath, curTextPath, null);
        trainingGroups.add(new AnnotationGroup(theDocument, referenceAnnotations));
      }
    }

    return trainingGroups;
  }


  private void addRelations(List<AnnotatedPhrase> phrases) {
    List<ConceptRelation> relation;
    Collections.sort(phrases, new StartOffsetPhraseComparator());
    int i = 0;
    while (i < phrases.size()) {
      AnnotatedPhrase beginPhrase = phrases.get(i++);
      AnnotatedPhrase endPhrase = phrases.get(i++);

      relation = createConceptRelation(beginPhrase, "EndToBeginSentenceLink");
      endPhrase.setConceptRelations(relation);

      relation = createConceptRelation(endPhrase, "BeginToEndSentenceLink");
      beginPhrase.setConceptRelations(relation);
    }
  }


  private void addSentenceBoundaryAnnotations(List<AnnotationGroup> annotationGroups,
      boolean tagOnlyAnnotated) {

    for (AnnotationGroup curGroup : annotationGroups) {
      List<RaptatPair<List<RaptatToken>, String>> tags;
      Collection<AnnotatedPhrase> sentencesToTag = new HashSet<>(100);

      if (tagOnlyAnnotated) {
        for (AnnotatedPhrase curPhrase : curGroup.referenceAnnotations) {
          List<RaptatToken> tokens = curPhrase.getProcessedTokens();
          if (tokens.size() > 0) {
            sentencesToTag.add(tokens.get(0).getSentenceOfOrigin());
          }
        }
      } else {
        sentencesToTag = curGroup.getRaptatDocument().getActiveSentences();
      }
      tags = createSentenceTags(sentencesToTag);
      List<AnnotatedPhrase> tagsAsAnnotations = convertToAnnotations(tags);
      addRelations(tagsAsAnnotations);
      curGroup.referenceAnnotations.addAll(tagsAsAnnotations);
    }
  }


  private List<AnnotatedPhrase> convertToAnnotations(
      List<RaptatPair<List<RaptatToken>, String>> tags) {
    List<AnnotatedPhrase> tagsAsAnnotations = new ArrayList<>(tags.size());

    for (RaptatPair<List<RaptatToken>, String> curTag : tags) {
      AnnotatedPhrase tagAsPhrase;

      String[] annotationData = new String[6];
      annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] =
          String.valueOf(SentenceTagger.annotationID++);

      List<RaptatToken> curTokenSequence = curTag.left;
      RaptatToken tagToken = curTokenSequence.get(0);
      annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] = tagToken.getStartOffset();
      annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] = tagToken.getEndOffset();
      annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = curTag.right;

      tagAsPhrase = new AnnotatedPhrase(annotationData, curTokenSequence);
      tagAsPhrase.setPhraseStringUnprocessed(tagToken.getTokenStringAugmented());
      tagsAsAnnotations.add(tagAsPhrase);
    }

    return tagsAsAnnotations;
  }


  private List<ConceptRelation> createConceptRelation(AnnotatedPhrase phrase, String relationName) {
    List<ConceptRelation> relationList = new ArrayList<>(1);
    String relationID = String.valueOf(SentenceTagger.annotationID++);
    String targetID = phrase.getMentionId();
    ConceptRelation newRelation = new ConceptRelation(relationID, relationName, targetID, null);
    relationList.add(newRelation);
    return relationList;
  }


  private List<RaptatPair<List<RaptatToken>, String>> createSentenceTags(
      Collection<AnnotatedPhrase> sentencesWithAnnotations) {
    int tokenMarkerLength = 5;
    List<RaptatPair<List<RaptatToken>, String>> tags =
        new ArrayList<>(2 * sentencesWithAnnotations.size());

    for (AnnotatedPhrase curSentence : sentencesWithAnnotations) {
      String sentenceString = curSentence.getPhraseStringUnprocessed();

      if (sentenceString.length() > tokenMarkerLength) {
        int startOffset = Integer.parseInt(curSentence.getRawTokensStartOff());
        int endOffset = Integer.parseInt(curSentence.getRawTokensEndOff());
        int length = endOffset - startOffset;

        String endTokenString = sentenceString.substring(length - tokenMarkerLength, length);
        RaptatToken endToken =
            new RaptatToken(endTokenString, endOffset - tokenMarkerLength, endOffset);
        List<RaptatToken> endTokenList = Arrays.asList(new RaptatToken[] {endToken});
        tags.add(new RaptatPair<>(endTokenList, SentenceTagger.END_TAG));

        String startTokenString = sentenceString.substring(0, tokenMarkerLength);
        RaptatToken startToken =
            new RaptatToken(startTokenString, startOffset, startOffset + tokenMarkerLength);
        List<RaptatToken> startTokenList = Arrays.asList(new RaptatToken[] {startToken});
        tags.add(new RaptatPair<>(startTokenList, SentenceTagger.BEGIN_TAG));
      }
    }
    return tags;
  }


  private List<String> getFilePaths(File directory, final String fileExtension) {
    File[] files = directory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File file, String string) {
        return string.endsWith(fileExtension);
      }
    });

    List<String> filePaths = new ArrayList<>(files.length);

    for (File curFile : files) {
      filePaths.add(curFile.getAbsolutePath());
    }
    return filePaths;
  }


  private void initializeReadAndWriteDirectories() {
    this.xmlReadDirectory = GeneralHelper.getDirectory("Choose directory with XML files");
    this.xmlWriteDirectory =
        GeneralHelper.getDirectory("Choose directory for storing sentence-tagged XML");
    this.textReadDirectory = GeneralHelper.getDirectory("Choose directory with text files");

    String filteredDirectoryName = "FilteredXML";
    this.xmlWriteDirectory =
        new File(this.xmlWriteDirectory.getAbsolutePath() + File.separator + filteredDirectoryName);
    try {
      FileUtils.forceDelete(this.xmlWriteDirectory);
    } catch (IOException ex) {
      if (!(ex instanceof FileNotFoundException)) {
        GeneralHelper.errorWriter("Unable to delete the directory for storing filtered xml files");
        System.exit(-99);
      }
    }

    try {
      FileUtils.forceMkdir(this.xmlWriteDirectory);
    } catch (IOException ex) {
      GeneralHelper.errorWriter("Unable to create the directory for storing filtered xml files");
      System.exit(-99);
    }
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        SentenceTagger tagger = new SentenceTagger();
        // tagger.initializeReadAndWriteDirectories();
        List<String> xmlPaths = tagger.getFilePaths(tagger.getXmlReadDirectory(), ".xml");
        List<String> textPaths = tagger.getFilePaths(tagger.getTextReadDirectory(), ".txt");
        List<AnnotationGroup> annotationGroups = tagger.importFiles(xmlPaths, textPaths);
        SentenceTagger.theTextAnalyzer.updateTrainingGroups(annotationGroups);

        if (SentenceTagger.logger.getLevel() == Level.DEBUG) {
          System.out.println("XML Paths: \n------------");
          for (String curPath : xmlPaths) {
            System.out.println(curPath);
          }

          System.out.println("Text Paths: \n------------");
          for (String curPath : textPaths) {
            System.out.println(curPath);
          }
        }

        tagger.addSentenceBoundaryAnnotations(annotationGroups, true);
        File resultFolder = tagger.getTextReadDirectory().getParentFile().getParentFile();
        String resultFolderPath = resultFolder.getAbsolutePath() + File.separator + "SentenceTags_"
            + GeneralHelper.getTimeStamp();
        resultFolder = new File(resultFolderPath);
        XMLExporter.exportAnnotationGroups(annotationGroups, resultFolder, true, true, false);
        JOptionPane.showMessageDialog(null, "Sentence tagging completed");
        System.exit(0);
      }
    });
  }
}
