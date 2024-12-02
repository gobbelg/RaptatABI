/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Class built as a one-off to allow import of xml-based annotation files, filtering of the
 * annotations within the files to meet some criteria, and then writing the xml back to new files
 * that can be read by eHOST. One can filter by either attribute and value
 * (getFilteredAnnotations()) or by concept name ((getFilteredAnnotationsByConcept). When filtering
 * by concept, one can delete attributes or relations if needed.
 *
 * @author vhatvhgobbeg
 */
public class AnnotationFilter {
  private class FilterNameValue {
    private final String name;
    private final String value;


    private FilterNameValue(String name, String value) {
      this.name = name;
      this.value = value;
    }


    private boolean isMatch(AnnotatedPhrase testPhrase) {
      for (RaptatAttribute curAttribute : testPhrase.getPhraseAttributes()) {
        if (curAttribute.getName().equalsIgnoreCase(this.name)) {
          /*
           * If the attributeValue is null or the empty string and the attributeName matches, return
           * true
           */
          if (this.value == null || this.value.equals("")) {
            return true;
          }
          for (String curValue : curAttribute.getValues()) {
            if (curValue.equalsIgnoreCase(this.value)) {
              return true;
            }
          }
        }
      }
      return false;
    }
  }

  private enum RunType {
    MENTAL_HEALTH, CIRRHOSIS_VARICES_SHUNT_ASCITES, PTSD_RUTH;
  }

  private File readDirectory;


  private File writeDirectory;

  private XMLExporterRevised xmlExporter;

  protected static Logger logger = Logger.getLogger(AnnotationFilter.class);

  public AnnotationFilter() {
    this.initializeReadAndWriteDirectories();
  }


  public AnnotationFilter(File readDirectory, File writeDirectory) {
    AnnotationFilter.logger.setLevel(Level.DEBUG);
    this.initializeReadAndWriteDirectories(readDirectory, writeDirectory);
    this.xmlExporter = new XMLExporterRevised(AnnotationApp.EHOST, this.writeDirectory, true);
  }


  private void discardUnmatchedRelations(List<AnnotatedPhrase> filteredAnnotations) {
    Stream<AnnotatedPhrase> stream = filteredAnnotations.stream();
    Map<String, AnnotatedPhrase> idToPhraseMap =
        stream.collect(Collectors.toMap(AnnotatedPhrase::getMentionId, Function.identity()));

    for (AnnotatedPhrase annotatedPhrase : filteredAnnotations) {
      List<ConceptRelation> relations = annotatedPhrase.getConceptRelations();
      ListIterator<ConceptRelation> relationsIterator = relations.listIterator();
      while (relationsIterator.hasNext()) {
        ConceptRelation relation = relationsIterator.next();
        String relationID = relation.getRelatedAnnotationID();

        if (!idToPhraseMap.containsKey(relationID)) {
          relationsIterator.remove();
        }
      }
    }
  }


  private void filterAnnotations(List<AnnotationGroup> annotationGroups,
      List<FilterNameValue> phraseFilters) {
    for (AnnotationGroup curGroup : annotationGroups) {
      List<AnnotatedPhrase> filteredAnnotations =
          getFilteredAnnotations(curGroup.raptatAnnotations, phraseFilters);
      curGroup.setRaptatAnnotations(filteredAnnotations);
    }
  }


  private String getFileName(File curXML) {
    String fileName = curXML.getName();
    AnnotationFilter.logger.info("Importing annotations from:" + fileName);
    int lastPeriod = fileName.lastIndexOf(".", fileName.lastIndexOf(".") - 1);
    fileName = fileName.substring(0, lastPeriod);
    return fileName;
  }


  private List<AnnotatedPhrase> getFilteredAnnotations(List<AnnotatedPhrase> raptatAnnotations,
      List<FilterNameValue> phraseFilters) {
    List<AnnotatedPhrase> resultList = new ArrayList<>(raptatAnnotations.size());
    for (AnnotatedPhrase curAnnotation : raptatAnnotations) {
      for (FilterNameValue curFilter : phraseFilters) {
        /* Add the phrase if it is a match for any of the filters */
        if (curFilter.isMatch(curAnnotation)) {
          resultList.add(curAnnotation);
          break;
        }
      }
    }
    return resultList;
  }


  /**
   * Takes a list of annotated phrases and return the ones with a concept name matching one of the
   * names of one of the concept filters supplied as a list within the parameters
   *
   * @param annotatedPhrases
   * @param conceptFilters
   * @param deleteAttributes
   * @param deleteRelations
   * @return
   */
  private List<AnnotatedPhrase> getFilteredAnnotationsByConcept(
      List<AnnotatedPhrase> annotatedPhrases, Set<String> conceptNameSet) {
    List<AnnotatedPhrase> resultList = new ArrayList<>(annotatedPhrases.size());
    for (AnnotatedPhrase curAnnotation : annotatedPhrases) {
      if (conceptNameSet.contains(curAnnotation.getConceptName().toLowerCase())) {
        resultList.add(curAnnotation);
      }
    }

    return resultList;
  }


  private List<AnnotationGroup> readAnnotations() {
    File[] xmlFiles = this.readDirectory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File file, String string) {
        return string.endsWith(".xml");
      }
    });

    List<AnnotationGroup> resultGroups = new ArrayList<>(xmlFiles.length);
    AnnotationImporter importer = AnnotationImporter.getImporter();

    for (File curXML : xmlFiles) {
      String fileName = getFileName(curXML);

      /*
       * We create a dumbed-down RaptatDocument instance because we need it in later calls
       */
      RaptatDocument raptatDocument = new RaptatDocument(null, null, null, Optional.of(fileName));
      AnnotationGroup curGroup = new AnnotationGroup(raptatDocument, null);

      List<AnnotatedPhrase> curAnnotations = importer.importXMLAnnotation(curXML.getAbsolutePath());
      curGroup.setRaptatAnnotations(curAnnotations);
      resultGroups.add(curGroup);
    }
    return resultGroups;
  }


  private void writeAnnotations(List<AnnotationGroup> annotationGroups) {
    for (AnnotationGroup curGroup : annotationGroups) {
      AnnotationFilter.logger
          .info("Exporting annotations to: " + curGroup.getRaptatDocument().getTextSource());
      this.xmlExporter.exportAnnotationGroup(curGroup, false, false, false);
    }
  }


  protected void filterAnnotationsByConcept(List<AnnotationGroup> annotationGroups,
      Set<String> conceptFilters) {
    for (AnnotationGroup curGroup : annotationGroups) {
      List<AnnotatedPhrase> filteredAnnotations =
          getFilteredAnnotationsByConcept(curGroup.raptatAnnotations, conceptFilters);
      discardUnmatchedRelations(filteredAnnotations);
      curGroup.setRaptatAnnotations(filteredAnnotations);
    }
  }


  void initializeReadAndWriteDirectories() {
    File readDirectory = GeneralHelper.getDirectory("Choose directory with XML files");
    File writeDirectory = GeneralHelper.getDirectory(
        "Choose directory for storing filtered XML files", this.readDirectory.getParent());

    String filteredDirectoryName = "FilteredXML";
    writeDirectory =
        new File(writeDirectory.getAbsolutePath() + File.separator + filteredDirectoryName);

    this.initializeReadAndWriteDirectories(readDirectory, writeDirectory);
  }


  void initializeReadAndWriteDirectories(File readDirectory, File writeDirectory) {
    this.readDirectory = readDirectory;
    this.writeDirectory = writeDirectory;

    try {
      FileUtils.forceDelete(writeDirectory);
    } catch (IOException ex) {
      if (!(ex instanceof FileNotFoundException)) {
        GeneralHelper.errorWriter("Unable to delete the directory for storing filtered xml files");
        System.exit(-99);
      }
    }

    try {
      FileUtils.forceMkdir(writeDirectory);
    } catch (IOException ex) {
      GeneralHelper.errorWriter("Unable to create the directory for storing filtered xml files");
      System.exit(-99);
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      @SuppressWarnings("ResultOfObjectAllocationIgnored")
      public void run() {
        RunType runType = RunType.PTSD_RUTH;
        AnnotationFilter filter;
        File readDirectory;
        File writeDirectory;
        String[] conceptList;

        switch (runType) {
          case MENTAL_HEALTH:
            filter = new AnnotationFilter();
            filterMentalHealthAnnotations(filter);
            break;
          case CIRRHOSIS_VARICES_SHUNT_ASCITES:
            readDirectory = new File(
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingXML");
            writeDirectory = new File(
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\Training_XML_VaricesShuntsAscitesOnly");
            filter = new AnnotationFilter(readDirectory, writeDirectory);
            conceptList = new String[] {"07_Ascites", "06_varices", "09_portosystemicshunt"};
            this.filterAnnotations(filter, conceptList, readDirectory, writeDirectory);
            break;
          case PTSD_RUTH:
            readDirectory =
                new File("C:\\Users\\VHATVHGOBBEG\\Documents\\Misc\\PTSD_GroupAnnotation\\saved");
            writeDirectory = new File("C:\\Users\\VHATVHGOBBEG\\Documents\\Misc\\FilteredXML");
            filter = new AnnotationFilter(readDirectory, writeDirectory);
            conceptList = new String[] {"Duration", "Frequency", "PTSD_Symptom",
                "PTSD_SymptomChange", "PTSD_TreatmentType", "PTSD_TreatmentChange",
                "PTSD_TreatmentNonCompliance", "PTSD_TreatmentDosage", "PTSD_TreamentAudience"};
            this.filterAnnotations(filter, conceptList, readDirectory, writeDirectory);
          default:
            break;
        }

        System.out.println("\n\nFiltering complete.\nExiting annotation filtering program.");
        System.exit(0);
      }


      private void filterAnnotations(AnnotationFilter filter, String[] conceptList,
          File readDirectory, File writeDirectory) {
        filter.initializeReadAndWriteDirectories(readDirectory, writeDirectory);
        List<AnnotationGroup> startAnnotations = filter.readAnnotations();

        Set<String> conceptFilterSet = new HashSet<>(conceptList.length);

        for (String conceptName : conceptList) {
          conceptFilterSet.add(conceptName.toLowerCase());
        }

        filter.filterAnnotationsByConcept(startAnnotations, conceptFilterSet);

        filter.writeAnnotations(startAnnotations);
      }


      private void filterMentalHealthAnnotations(AnnotationFilter filter) {
        /*
         * Read in the annotations
         */
        List<AnnotationGroup> startAnnotations = filter.readAnnotations();

        /*
         * Filter the annotations
         */
        List<FilterNameValue> phraseFilters = new ArrayList<>();
        phraseFilters.add(filter.new FilterNameValue("SymptomCategory", "HomicideIdeation"));
        phraseFilters.add(filter.new FilterNameValue("SymptomCategory", "SuicideIdeation"));
        phraseFilters.add(filter.new FilterNameValue("SymptomCategory", "Both"));

        filter.filterAnnotations(startAnnotations, phraseFilters);

        /*
         * Write out the annotations
         */
        filter.writeAnnotations(startAnnotations);
      }
    });
  }
}
