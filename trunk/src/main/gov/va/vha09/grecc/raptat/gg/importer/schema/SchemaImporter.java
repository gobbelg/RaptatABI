package src.main.gov.va.vha09.grecc.raptat.gg.importer.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public abstract class SchemaImporter {
  protected static AnnotationApp schemaApp;


  public File getSchemaFile() {
    String startDirectory = System.getProperty("user.dir");
    String[] fileTypes = {".pont", ".xml"};
    File schemaFile = GeneralHelper.getFileByType("Select schema file", startDirectory, fileTypes);
    return schemaFile;
  }


  /**
   * Remove reference in schema to concepts that are not in the set of accepted concepts and add
   * concepts in accepted concepts not in list of schema concepts
   *
   * @param schemaConcepts
   * @param allowableConceptNames
   * @return
   */
  public static List<SchemaConcept> filterSchemaConcepts(List<SchemaConcept> schemaConcepts,
      HashSet<String> allowableConceptNames) {
    List<SchemaConcept> resultConcepts = new ArrayList<>(allowableConceptNames.size());

    for (SchemaConcept schemaConcept : schemaConcepts) {
      String conceptName = schemaConcept.getConceptName();
      if (allowableConceptNames.contains(conceptName)) {
        if (schemaConcept.isRelationship()) {
          filterForAllowableConcepts(allowableConceptNames, schemaConcept.getLinkedFromConcepts());
          filterForAllowableConcepts(allowableConceptNames, schemaConcept.getLinkedToConcepts());
          if (!schemaConcept.getLinkedFromConcepts().isEmpty()
              && !schemaConcept.getLinkedToConcepts().isEmpty()) {
            resultConcepts.add(schemaConcept);
          }
        } else {
          resultConcepts.add(schemaConcept);
        }
        allowableConceptNames.remove(conceptName);
      }
    }

    /* Add any concepts that are allowable but not in original schema */
    for (String conceptToAdd : allowableConceptNames) {
      resultConcepts.add(new SchemaConcept(conceptToAdd, false));
    }

    return resultConcepts;
  }


  public static AnnotationApp getSchemaApp() {
    return SchemaImporter.schemaApp;
  }


  public static List<SchemaConcept> importSchemaConcepts(File schemaFile) {
    return importSchemaConcepts(schemaFile.getAbsolutePath());
  }


  public static List<SchemaConcept> importSchemaConcepts(String schemaFilePath) {
    if (schemaFilePath.endsWith(".pont")) {
      SchemaImporter.schemaApp = AnnotationApp.KNOWTATOR;
      return KnowtatorSchemaImporter.importSchemaConcepts(schemaFilePath);
    } else {
      SchemaImporter.schemaApp = AnnotationApp.EHOST;
      return EhostSchemaImporter.importSchemaConcepts(schemaFilePath);
    }
  }


  private static void filterForAllowableConcepts(HashSet<String> allowableConcepts,
      Set<String> conceptList) {
    Iterator<String> iterator = conceptList.iterator();
    while (iterator.hasNext()) {
      String listConcept = iterator.next();
      if (!allowableConcepts.contains(listConcept)) {
        iterator.remove();
      }
    }
  }
}
