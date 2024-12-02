package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import com.google.common.collect.Lists;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.JdomXMLImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

public class AnnotationMerger {
  private enum RunType {
    MERGE, MERGE_USING_CONTEXT;
  }

  private File readDirectoryOne;


  private File readDirectoryTwo;

  private File writeDirectory;
  protected static Logger logger = Logger.getLogger(AnnotationFilter.class);

  private void initializeReadAndWriteDirectories() {
    this.readDirectoryOne =
        GeneralHelper.getDirectory("Choose directory with first set of XML files");
    this.readDirectoryTwo =
        GeneralHelper.getDirectory("Choose directory with second set of XML files");
    this.writeDirectory = GeneralHelper.getDirectory(
        "Choose directory for storing merged XML files", this.readDirectoryOne.getParent());

    initializeWriteDirectory(this.writeDirectory);
  }


  private void initializeWriteDirectory(File mergedDirectory) {
    String mergedDirectoryName = "MergedXML";
    this.writeDirectory =
        new File(this.writeDirectory.getAbsolutePath() + File.separator + mergedDirectoryName);
    try {
      FileUtils.forceDelete(this.writeDirectory);
    } catch (IOException ex) {
      if (!(ex instanceof FileNotFoundException)) {
        GeneralHelper.errorWriter("Unable to delete the directory for storing filtered xml files");
        System.exit(-99);
      }
    }

    try {
      FileUtils.forceMkdir(this.writeDirectory);
    } catch (IOException ex) {
      GeneralHelper.errorWriter("Unable to create the directory for storing filtered xml files");
      System.exit(-99);
    }
  }


  private AnnotationGroup mergeAnnotationGroups(AnnotationGroup groupOne,
      AnnotationGroup groupTwo) {
    Set<AnnotatedPhrase> resultGroupPhrases =
        new HashSet<>(groupOne.raptatAnnotations.size() + groupTwo.raptatAnnotations.size());

    resultGroupPhrases.addAll(groupOne.raptatAnnotations);
    resultGroupPhrases.addAll(groupTwo.raptatAnnotations);

    List<AnnotatedPhrase> resultPhraseList = Lists.newArrayList(resultGroupPhrases);
    AnnotationGroup resultGroup =
        new AnnotationGroup(groupOne.getRaptatDocument(), resultPhraseList);
    return resultGroup;
  }


  private Map<String, AnnotationGroup> readAnnotations(File readDirectory)
      throws SAXException, IOException, ParserConfigurationException {
    File[] xmlFiles = readDirectory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File file, String string) {
        return string.endsWith(".xml");
      }
    });

    Map<String, AnnotationGroup> resultGroups = new HashMap<>(xmlFiles.length);
    AnnotationImporter importer = AnnotationImporter.getImporter();

    for (File curXML : xmlFiles) {
      String textFileName = JdomXMLImporter.getTextSource(curXML);

      /*
       * We create a dumbed-down RaptatDocument instance because we need it in later calls
       */
      RaptatDocument raptatDocument =
          new RaptatDocument(null, null, null, Optional.of(textFileName));
      AnnotationGroup curGroup = new AnnotationGroup(raptatDocument, null);

      List<AnnotatedPhrase> curAnnotations = importer.importXMLAnnotation(curXML.getAbsolutePath());
      curGroup.setRaptatAnnotations(curAnnotations);
      resultGroups.put(textFileName, curGroup);
    }
    return resultGroups;
  }


  private void writeAnnotations(List<AnnotationGroup> annotationGroups) {
    for (AnnotationGroup curGroup : annotationGroups) {
      AnnotationMerger.logger
          .info("Exporting annotations to: " + curGroup.getRaptatDocument().getTextSource());
      XMLExporter.exportAnnotationGroup(curGroup, this.writeDirectory, false);
    }
  }


  protected List<AnnotationGroup> mergeAnnotations(Map<String, AnnotationGroup> groupOneAnnotations,
      Map<String, AnnotationGroup> groupTwoAnnotations) {
    List<AnnotationGroup> mergedGroups = new ArrayList<>(groupOneAnnotations.size());

    for (String groupOneKey : groupOneAnnotations.keySet()) {
      AnnotationGroup groupTwo = null;
      if ((groupTwo = groupTwoAnnotations.get(groupOneKey)) != null) {
        AnnotationGroup mergedGroup =
            mergeAnnotationGroups(groupOneAnnotations.get(groupOneKey), groupTwo);
        mergedGroups.add(mergedGroup);
        groupTwoAnnotations.remove(groupOneKey);
      } else {
        System.err.println("No matching annotation group:" + groupOneKey);
      }
    }

    /* groupTwoAnnotations should be empty */
    for (String groupTwoKey : groupTwoAnnotations.keySet()) {

      System.err.println("No matching annotation group:" + groupTwoKey);
    }

    return mergedGroups;
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        AnnotationMerger merger = new AnnotationMerger();
        RunType runType = RunType.MERGE;

        switch (runType) {
          case MERGE:
            this.mergeAnnotationGroups(merger);
            break;
          case MERGE_USING_CONTEXT:
            mergerUsingContext(merger);
            break;
          default:
            break;
        }

        System.out.println("\nMerging complete.\nExiting annotation filtering program.");
        System.exit(0);
      }


      private void mergeAnnotationGroups(AnnotationMerger merger) {
        /*
         * Create instance of AnnotationFilter to process data
         */
        merger.initializeReadAndWriteDirectories();

        try {
          /*
           * Read in the annotations of both groups
           */
          Map<String, AnnotationGroup> groupOneAnnotations =
              merger.readAnnotations(merger.readDirectoryOne);
          Map<String, AnnotationGroup> groupTwoAnnotations =
              merger.readAnnotations(merger.readDirectoryTwo);

          List<AnnotationGroup> mergedAnnotations =
              merger.mergeAnnotations(groupOneAnnotations, groupTwoAnnotations);
          /*
           * Write out the annotations
           */
          merger.writeAnnotations(mergedAnnotations);
        } catch (SAXException | IOException | ParserConfigurationException e) {
          System.err.println(
              "Error while reading or writing annotations for merging\n" + e.getLocalizedMessage());
          e.printStackTrace();
          System.exit(-1);
        }
      }


      private void mergerUsingContext(AnnotationMerger merger) {
        this.mergeAnnotationGroups(merger);
      }
    });
  }
}
