/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation.AnnotationMutator;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation.AnnotationMutator.ConceptRelationshipKey;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/** @author Glenn Gobbel */
public class MathenyCirrhosis {
  private enum RunType {
    SPECIFY_LOCATION_CONCEPTS, SPECIFY_LOCATION_CONCEPTS_LIVER_OR_ABDOMEN;
  }


  /** */
  public MathenyCirrhosis() {}


  /**
   * May 7, 2018
   *
   * @param textFilesPath
   * @param xmlFilesPath
   * @param baseSchemaPath
   * @param newSchemaPath
   * @param targetedConcept
   * @param targetedConcept
   * @param targetedRelationship
   * @param remapping
   */
  protected void remapTargetedConcept(String textFilesPath, String xmlFilesPath,
      String baseSchemaPath, String newSchemaPath, String targetedConcept,
      String targetedRelationship,
      HashMap<String, HashMap<ConceptRelationshipKey, String>> remapping) {
    List<RaptatPair<File, File>> files =
        GeneralHelper.getXmlFilesAndMatchTxt(textFilesPath, xmlFilesPath);

    AnnotationImporter annotationImporter = new AnnotationImporter(AnnotationApp.EHOST);
    TextAnalyzer ta = new TextAnalyzer();
    AnnotationMutator annotationMutator = new AnnotationMutator();
    String resultFolderPath = new File(newSchemaPath).getParent();

    boolean overwriteExisting = true;
    XMLExporterRevised exporter =
        new XMLExporterRevised(AnnotationApp.EHOST, new File(resultFolderPath), overwriteExisting);
    /*
     * Assume files are in such an order that they correspond to one another and process lists.
     */
    Iterator<RaptatPair<File, File>> fileIterator = files.iterator();

    while (fileIterator.hasNext()) {
      RaptatPair<File, File> filePair = fileIterator.next();
      File textFile = filePair.left;
      File xmlFile = filePair.right;

      String xmlPath = xmlFile.getAbsolutePath();
      String textPath = textFile.getAbsolutePath();

      RaptatDocument textDocument = ta.processDocument(textPath);
      List<AnnotatedPhrase> xmlAnnotations =
          annotationImporter.importAnnotations(xmlPath, textPath, null, baseSchemaPath);

      annotationMutator.remapRelationship(xmlAnnotations, targetedConcept, targetedRelationship,
          remapping);

      AnnotationGroup annotationGroup = new AnnotationGroup(textDocument, null);
      annotationGroup.setRaptatAnnotations(xmlAnnotations);
      exporter.exportRaptatAnnotationGroup(annotationGroup, true, true);
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RunType runType = RunType.SPECIFY_LOCATION_CONCEPTS_LIVER_OR_ABDOMEN;
        MathenyCirrhosis cirrhosisMethodRunner = new MathenyCirrhosis();

        switch (runType) {
          case SPECIFY_LOCATION_CONCEPTS: {
            String textFilesPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingTXT";
            String xmlFilesPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingXML";
            String originalSchemaPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\projectschema.xml";
            String targetedConcept = "10_location";
            String targetedRelationship = "locationlink";
            String newSchemaPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingXML_LocationSpecified\\projectschemaLocationModified_180507.xml";

            HashMap<String, HashMap<ConceptRelationshipKey, String>> remapping =
                getLocationSecificConceptRemapping();

            cirrhosisMethodRunner.remapTargetedConcept(textFilesPath, xmlFilesPath,
                originalSchemaPath, newSchemaPath, targetedConcept, targetedRelationship,
                remapping);
          }
            break;
          case SPECIFY_LOCATION_CONCEPTS_LIVER_OR_ABDOMEN: {
            String textFilesPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingTXT";
            String xmlFilesPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingDocuments_Old_180608\\TrainingXML";
            String originalSchemaPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\\\TrainingDocuments_Old_180608\\projectschema.xml";
            String targetedConcept = "10_location";
            String targetedRelationship = "locationlink";
            String newSchemaPath =
                "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn\\CrossValidation\\TrainingXML_LocationAbdOrLiver_Revised\\projectschemaLocationLivOrAbd_180618.xml";

            HashMap<String, HashMap<ConceptRelationshipKey, String>> remapping =
                getLiverOrAbdomenRemapping();

            cirrhosisMethodRunner.remapTargetedConcept(textFilesPath, xmlFilesPath,
                originalSchemaPath, newSchemaPath, targetedConcept, targetedRelationship,
                remapping);
          }

            break;
          default:
            break;
        }
      }


      /** @return */
      private HashMap<String, HashMap<ConceptRelationshipKey, String>> getLiverOrAbdomenRemapping() {
        HashMap<String, HashMap<ConceptRelationshipKey, String>> remapping = new HashMap<>();

        /*
         * Specify keys allow users to retrieve the new name of the concept and relationship when
         * remapped
         */
        ConceptRelationshipKey conceptKey = ConceptRelationshipKey.CONCEPT;
        ConceptRelationshipKey relationshipKey = ConceptRelationshipKey.RELATIONSHIP;

        /* Specify the remappings */
        HashMap<ConceptRelationshipKey, String> abdomenMapper = new HashMap<>();
        abdomenMapper.put(conceptKey, "10_location_abdomen");
        abdomenMapper.put(relationshipKey, "LocationAbdomenLink");
        HashMap<ConceptRelationshipKey, String> liverMapper = new HashMap<>();
        liverMapper.put(conceptKey, "10_location_liver");
        liverMapper.put(relationshipKey, "LocationLiverLink");

        /*
         * Specify the concepts that when linked to result in the targetedConcept and
         * tagetedRelationship getting remapped
         */
        remapping.put("06_varices", abdomenMapper);
        remapping.put("02_liverlobe", liverMapper);
        remapping.put("05_massfocalarea", liverMapper);
        remapping.put("07_ascites", abdomenMapper);
        remapping.put("01_liver", liverMapper);
        remapping.put("08_fattyliver", liverMapper);
        remapping.put("09_portosystemicshunt", abdomenMapper);
        return remapping;
      }


      /** @return */
      private HashMap<String, HashMap<ConceptRelationshipKey, String>> getLocationSecificConceptRemapping() {
        HashMap<String, HashMap<ConceptRelationshipKey, String>> remapping = new HashMap<>();

        /*
         * Specify keys to retrieve the new name of the concept and relationship when remapped
         */
        ConceptRelationshipKey conceptKey = ConceptRelationshipKey.CONCEPT;
        ConceptRelationshipKey relationshipKey = ConceptRelationshipKey.RELATIONSHIP;

        /* Specify the remappings */
        HashMap<ConceptRelationshipKey, String> varicesMapper = new HashMap<>();
        varicesMapper.put(conceptKey, "10_location_varices");
        varicesMapper.put(relationshipKey, "LocationVaricesLink");
        HashMap<ConceptRelationshipKey, String> shuntMapper = new HashMap<>();
        shuntMapper.put(conceptKey, "10_location_shunt");
        shuntMapper.put(relationshipKey, "LocationShuntLink");
        HashMap<ConceptRelationshipKey, String> ascitesMapper = new HashMap<>();
        ascitesMapper.put(conceptKey, "10_location_ascites");
        ascitesMapper.put(relationshipKey, "LocationAscitesLink");
        HashMap<ConceptRelationshipKey, String> liverMapper = new HashMap<>();
        liverMapper.put(conceptKey, "10_location_liver");
        liverMapper.put(relationshipKey, "LocationLiverLink");

        /*
         * The key in the remapping corresponds to the concept that, if linked TO by the
         * targetedConcept, will result in renaming of that targetedConcept by the value that the
         * key maps to. So, the targetedRelationship should represent the link FROM the
         * targetedConcept TO the key in the remapping HashMap.
         */
        remapping.put("06_varices", varicesMapper);
        remapping.put("02_liverlobe", liverMapper);
        remapping.put("05_massfocalarea", liverMapper);
        remapping.put("07_ascites", ascitesMapper);
        remapping.put("01_liver", liverMapper);
        remapping.put("08_fattyliver", liverMapper);
        remapping.put("09_portosystemicshunt", shuntMapper);
        return remapping;
      }
    });
  }
}
