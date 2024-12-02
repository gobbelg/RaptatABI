/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate.PhraseIDCrossValidationAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/**
 * Class to modify attributes - it takes the values of attributes and promotes them to concepts.
 * Also can take concepts and remap them to other concepts.
 *
 * @author glenn
 */
/**
 * @author VHATVHGOBBEG
 *
 */
/**
 * @author VHATVHGOBBEG
 *
 */
public class AnnotationMutator {

  public enum ConceptRelationshipKey {
    CONCEPT, RELATIONSHIP;
  }

  private enum RunType {
    TEST;
  }

  private static final Logger LOGGER = Logger.getLogger(AnnotationMutator.class);

  private static StartOffsetPhraseComparator phraseSorter = new StartOffsetPhraseComparator();

  public AnnotationMutator() {
    AnnotationMutator.LOGGER.setLevel(Level.DEBUG);
  }


  /**
   * @param xmlSourceFolderPath
   * @param textSourceFolderPath
   * @param targetFolderPath
   * @param schemaConceptFilePath
   * @param acceptedConceptsPath
   * @param conceptNamePositions
   * @param mappedAttributeValue
   * @param prependString
   */
  public void combineAttributeValuesIntoConcepts(final String xmlSourceFolderPath,
      final String textSourceFolderPath, final String targetFolderPath,
      final String schemaConceptFilePath, final String acceptedConceptsPath,
      final Map<String, Set<String>> conceptNamePositions,
      final Map<String, String> mappedAttributeValue, final String prependString) {

    XMLExporterRevised exporter = XMLExporterRevised.getExporter(new File(targetFolderPath), true);

    List<SchemaConcept> schemaConcepts = SchemaImporter.importSchemaConcepts(schemaConceptFilePath);

    HashSet<String> acceptedConcepts =
        SchemaConcept.getAcceptableConceptNames(acceptedConceptsPath);

    AnnotationImporter importer = new AnnotationImporter(AnnotationApp.EHOST);

    List<RaptatPair<File, File>> txtAndXmlFiles =
        GeneralHelper.getXmlFilesAndMatchTxt(textSourceFolderPath, xmlSourceFolderPath);

    Iterator<RaptatPair<File, File>> fileIterator = txtAndXmlFiles.iterator();

    while (fileIterator.hasNext()) {
      RaptatPair<File, File> txtAndXmlFilePair = fileIterator.next();
      String txtFilePath = txtAndXmlFilePair.left.getAbsolutePath();
      String xmlFilePath = txtAndXmlFilePair.right.getAbsolutePath();

      List<AnnotatedPhrase> annotations = importer.importAnnotations(xmlFilePath, txtFilePath,
          acceptedConcepts, schemaConcepts, null, false, false, false);

      abiProjectCombineAttributesForConcepts2(annotations, conceptNamePositions,
          mappedAttributeValue, prependString);

      RaptatDocument doc = new RaptatDocument();
      doc.setTextSourcePath(Optional.of(txtFilePath));
      AnnotationGroup annotationGroup = new AnnotationGroup(doc, null);
      annotationGroup.setRaptatAnnotations(annotations);

      System.out.println("Exporting:" + xmlFilePath);
      exporter.exportRaptatAnnotationGroup(annotationGroup, false, false);
    }

  }

  public List<AnnotationGroup> filterAnnotations(final File targetAnnotationsDirectory,
      final File targetAnnotationsSchemaFile, final HashSet<String> targetConcepts,
      final File filteringAnnotationsDirectory, final File filteringAnnotationsSchemaFile,
      final HashSet<String> filteringConcepts, final File textFileDirectory) {
    List<SchemaConcept> targetSchemaConcepts =
        SchemaImporter.importSchemaConcepts(targetAnnotationsSchemaFile);
    List<SchemaConcept> filteringSchemaConcepts =
        SchemaImporter.importSchemaConcepts(filteringAnnotationsSchemaFile);

    List<File> targetFiles = Arrays.asList(targetAnnotationsDirectory.listFiles());
    List<File> filteringFiles = Arrays.asList(filteringAnnotationsDirectory.listFiles());
    List<File> textFiles = Arrays.asList(textFileDirectory.listFiles());

    Collections.sort(targetFiles);
    Collections.sort(filteringFiles);
    Collections.sort(textFiles);

    List<RaptatPair<AnnotationGroup, List<AnnotatedPhrase>>> filterPairs =
        getFilteringPairs(targetConcepts, filteringConcepts, targetSchemaConcepts,
            filteringSchemaConcepts, targetFiles, filteringFiles, textFiles);

    List<AnnotationGroup> filteredAnnotationGroups = this.filterAnnotations(filterPairs);

    return filteredAnnotationGroups;
  }

  public List<AnnotatedPhrase> filterForAnnotators(final List<AnnotatedPhrase> annotatedPhrases,
      final Set<String> acceptableAnnotators) {
    if (acceptableAnnotators == null || acceptableAnnotators.isEmpty()) {
      return new ArrayList<>(annotatedPhrases);
    }
    List<AnnotatedPhrase> resultList =
        annotatedPhrases.stream()
            .filter(annotatedPhrase -> acceptableAnnotators
                .contains(annotatedPhrase.getAnnotatorID().toLowerCase()))
            .collect(Collectors.toList());
    return resultList;
  }

  /**
   * Merges two lists of annotations, taking the longer one if there is annotation phrase overlap or
   * the first if they are overlapped and equal length. Does not account for concept differences.
   * Both lists will be sorted, which is necessary for the algorithm to work properly.
   *
   * @param annotationListOne
   * @param annotationListTwo
   * @return
   * @throws Exception
   */
  public List<AnnotatedPhrase> mergeAnnotationLists(final List<AnnotatedPhrase> annotationListOne,
      final List<AnnotatedPhrase> annotationListTwo) throws Exception {

    if (annotationListOne.isEmpty()) {
      return new ArrayList<>(annotationListTwo);
    }
    if (annotationListTwo.isEmpty()) {
      return new ArrayList<>(annotationListOne);
    }

    Collections.sort(annotationListOne);
    Collections.sort(annotationListTwo);

    ArrayList<AnnotatedPhrase> overlappedList = new ArrayList<>();
    ListIterator<AnnotatedPhrase> listOneIterator = annotationListOne.listIterator();
    ListIterator<AnnotatedPhrase> listTwoIterator = annotationListTwo.listIterator();
    AnnotatedPhrase listOnePhrase = listOneIterator.next();
    AnnotatedPhrase listTwoPhrase = listTwoIterator.next();
    while (listOnePhrase != null || listTwoPhrase != null) {
      switch (compareOffsets(listOnePhrase, listTwoPhrase)) {

        // listTwoPhrase comes before listOnePhrase
        case -1:
          overlappedList.add(listTwoPhrase);
          listTwoPhrase = listTwoIterator.hasNext() ? listTwoIterator.next() : null;
          break;

        // listOnePhrase comes before listTwoPhrase
        case 1:
          overlappedList.add(listOnePhrase);
          listOnePhrase = listOneIterator.hasNext() ? listOneIterator.next() : null;
          break;

        // listOnePhrase and listTwoPhrase overlap
        case 0:
          if (AnnotatedPhrase.compareLengths(listOnePhrase, listTwoPhrase) <= 0) {
            overlappedList.add(listOnePhrase);
          } else {
            overlappedList.add(listTwoPhrase);
          }
          listOnePhrase = listOneIterator.hasNext() ? listOneIterator.next() : null;
          listTwoPhrase = listTwoIterator.hasNext() ? listTwoIterator.next() : null;
          break;

        // Error - compareOffsets method should return only -1, 0, or 1
        default:
          throw new Exception("Code error - compareOffsets method return illegal value");
      }
    }

    return overlappedList;
  }

  /**
   * Takes a collection of annotatedPhrases, selects the ones corresponding to the
   * targetConceptName, and creates new annotations for each based on other annotations linked to
   * them and the attribute values of those linked annotations. The method takes the targeted
   * concepts and combines their concept name with the attribute values of the related concepts. So,
   * if we are targeting "IndexValue" concepts, and the concepts related to them are "Laterality"
   * (with a "LeftOrRight" attribute of value "left") and "IndexTypeOfIndexValue" (with an
   * "ABIOrTBI" attribute with value "ABI"), the new concept would be given the conceptName of
   * "indexValue_abi_left". Note that the attribute values are added to the name of the concept with
   * "_" delimiters. The order of the attribute values is the lexicographic ordering of the
   * attribute names, so that the "ABIorTBI" attribute would come before the "laterality" attribute.
   *
   * @param annotatedPhrases
   * @param targetConceptName
   * @param relationshipToAttributeNames
   * @return
   */
  public List<AnnotatedPhrase> promoteRelationshipValuesToConcepts(
      final Collection<AnnotatedPhrase> annotatedPhrases, final String targetConceptName,
      final Map<String, String> relationshipToAttributeNames) {

    List<AnnotatedPhrase> relatedAnnotations = new ArrayList<>();
    Map<String, AnnotatedPhrase> idToTargetedAnnotationMap = new HashMap<>();

    /*
     * Populate targetedAnnotations, relatedAnnotations, and idToTargetedAnnotationMap
     */
    for (AnnotatedPhrase annotatedPhrase : annotatedPhrases) {
      if (annotatedPhrase.getConceptName().equalsIgnoreCase(targetConceptName)) {
        idToTargetedAnnotationMap.put(annotatedPhrase.getMentionId(), annotatedPhrase);
        continue;
      }

      if (relationshipToAttributeNames
          .containsKey(annotatedPhrase.getConceptName().toLowerCase())) {
        relatedAnnotations.add(annotatedPhrase);
      }
    }

    Map<AnnotatedPhrase, Map<String, String>> annotationToRelatedValuesMap =
        getAnnotationToRelatedValuesMap(relatedAnnotations, idToTargetedAnnotationMap,
            relationshipToAttributeNames);
    List<AnnotatedPhrase> resultList =
        createRelationshipValuePromotedAnnotations(annotationToRelatedValuesMap);
    return resultList;

  }

  public void promoteValuesToConcepts(final String xmlSourceFolderPath,
      final String textSourceFolderPath, final String targetFolderPath,
      final String schemaConceptFilePath, final String acceptedConceptsPath) {
    XMLExporterRevised exporter = XMLExporterRevised.getExporter(new File(targetFolderPath), true);

    List<SchemaConcept> schemaConcepts = SchemaImporter.importSchemaConcepts(schemaConceptFilePath);

    HashSet<String> acceptedConcepts =
        SchemaConcept.getAcceptableConceptNames(acceptedConceptsPath);

    AnnotationImporter importer = new AnnotationImporter(AnnotationApp.EHOST);

    String targetConceptName = "ptsd_sign_symptom";
    String targetAttributeName = "symptomcategory";

    List<RaptatPair<File, File>> txtAndXmlFiles =
        GeneralHelper.getXmlFilesAndMatchTxt(textSourceFolderPath, xmlSourceFolderPath);

    Iterator<RaptatPair<File, File>> fileIterator = txtAndXmlFiles.iterator();

    while (fileIterator.hasNext()) {
      RaptatPair<File, File> txtAndXmlFilePair = fileIterator.next();
      String txtFilePath = txtAndXmlFilePair.left.getAbsolutePath();
      String xmlFilePath = txtAndXmlFilePair.right.getAbsolutePath();

      List<AnnotatedPhrase> annotations = importer.importAnnotations(xmlFilePath, txtFilePath,
          acceptedConcepts, schemaConcepts, null);

      promoteAttributeValuesToConcepts(annotations, targetConceptName, targetAttributeName);

      RaptatDocument doc = new RaptatDocument();
      doc.setTextSourcePath(Optional.of(txtFilePath));
      AnnotationGroup annotationGroup = new AnnotationGroup(doc, null);
      annotationGroup.setRaptatAnnotations(annotations);

      System.out.println("Exporting:" + xmlFilePath);
      exporter.exportRaptatAnnotationGroup(annotationGroup, true, false);
    }
  }

  /**
   * Remaps concepts and relationships to new names. The method examines all the annotations in the
   * baseAnnotations list. If the examined annotation concept name is within the remapping HashMap
   * and it contains the relationship relationshipName, it remaps the relationshipName to the name
   * retrieved using the name of the concept as a key.
   *
   * <p>
   * May 7, 2018
   *
   * @param annotations
   * @param targetedRelationship
   * @param remapping
   * @return
   */
  public void remapRelationship(final List<AnnotatedPhrase> annotations,
      final String targetedConcept, final String targetedRelationship,
      final HashMap<String, HashMap<ConceptRelationshipKey, String>> remapping) {

    HashMap<String, AnnotatedPhrase> mentionIdToAnnotatedPhraseMap = new HashMap<>();
    List<AnnotatedPhrase> targetedAnnotations = new ArrayList<>();
    List<AnnotatedPhrase> targetedAnnotationsUpdated = new ArrayList<>();

    // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
    // AnnotationMutator.LOGGER.debug("Start of remapRelationship for concept:" + targetedConcept
    // + " relationship:" + targetedRelationship);
    // AnnotationMutator.LOGGER.debug("\n\nStarting " + annotations.size() + " annotations:");
    // for (int i = 0; i < annotations.size(); i++) {
    // System.out.println("\n\nAnnotation " + i + ":\n" + annotations.get(i));
    // }
    // }
    /*
     * Select all phrases that have targetedConcept, remove them, and put them in another list.
     * We'll need them later to remap concept and relationship names.
     *
     * Also, while iterating through the concepts, store a map from id to each annotated phrase. We
     * need to find the concept the targetedConcept is linked to in order to determine how to remap
     * the concept and relationship name
     */
    ListIterator<AnnotatedPhrase> annotationIterator = annotations.listIterator();
    while (annotationIterator.hasNext()) {
      AnnotatedPhrase annotatedPhrase = annotationIterator.next();
      AnnotationMutator.LOGGER.debug("Processsing annotation:\n" + annotatedPhrase + "\n\n");
      mentionIdToAnnotatedPhraseMap.put(annotatedPhrase.getMentionId(), annotatedPhrase);

      if (annotatedPhrase.getConceptName().equalsIgnoreCase(targetedConcept)) {
        targetedAnnotations.add(annotatedPhrase);
        annotationIterator.remove();

        AnnotationMutator.LOGGER
            .debug("\n\nPhrase:" + annotatedPhrase + " added to targetedAnnotations\n\n");
      }
    }

    // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
    // AnnotationMutator.LOGGER
    // .debug("Completed Transfer of targetedAnnotations\nCurrent xmlAnnotations:");
    // for (int i = 0; i < annotations.size(); i++) {
    // System.out.println("\n\nAnnotation " + i + ":" + annotations.get(i));
    // }
    // AnnotationMutator.LOGGER.debug("targetedAnnotations:");
    // for (int i = 0; i < targetedAnnotations.size(); i++) {
    // System.out.println("\n\nAnnotation " + i + ":" + targetedAnnotations.get(i));
    // }
    // }
    ListIterator<AnnotatedPhrase> targetedAnnotationIterator = targetedAnnotations.listIterator();

    /*
     * Process all the targeted concept annotations found in the previous loop. For every concept
     * relation found that has the name targetedRelationship, create a new annotation with a new
     * concept name specified by the name of the concept that the relation links to and the
     * remapping map supplied as a parameter.
     */
    while (targetedAnnotationIterator.hasNext()) {
      AnnotatedPhrase targetedAnnotation = targetedAnnotationIterator.next();
      List<ConceptRelation> potentialTargetedRelations = targetedAnnotation.getConceptRelations();

      // AnnotationMutator.LOGGER.debug("\n\n=====================================\n"
      // + "Processing targeted annotation:\n" + targetedAnnotation + "\n\n");
      // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
      // AnnotationMutator.LOGGER.debug("Finding targeted relations from all relations:\n\n");
      // for (int i = 0; i < potentialTargetedRelations.size(); i++) {
      // System.out.println("Relation " + i + ":" + potentialTargetedRelations.get(i) + "\n\n");
      // }
      // System.out.print("\n\n");
      // }
      //
      // AnnotationMutator.LOGGER
      // .debug("Finding targeted relations with name:" + targetedRelationship);
      List<ConceptRelation> targetedRelations =
          extractTargetedRelations(targetedRelationship, potentialTargetedRelations);

      // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
      // AnnotationMutator.LOGGER.debug("Completed find targeted relations\nRemaining relations:");
      // for (ConceptRelation conceptRelation : potentialTargetedRelations) {
      // System.out.println(conceptRelation);
      // }
      //
      // AnnotationMutator.LOGGER
      // .debug("Targeted Relations with name " + targetedRelationship + ":");
      // for (ConceptRelation conceptRelation : targetedRelations) {
      // System.out.println(conceptRelation);
      // }
      // }

      /*
       * If there are no targeted relationships, just place the annotation, as is, into the list of
       * new annotations
       */
      if (targetedRelations.isEmpty()) {
        AnnotationMutator.LOGGER.debug("No targeted relations found for current concept");
        targetedAnnotationsUpdated.add(targetedAnnotation);
      }
      /*
       * Else create a new annotation for each of the targeted relations stored in
       * targetedRelationsNew and rename them according to the remapping HashMap
       */
      else {
        ListIterator<ConceptRelation> relationsIterator = targetedRelations.listIterator();

        AnnotationMutator.LOGGER.debug("Targeted relations found\nProcessing targeted relations");

        while (relationsIterator.hasNext()) {
          ConceptRelation targetedRelation = relationsIterator.next();

          AnnotationMutator.LOGGER
              .debug("Processing linked concept ID of targeted relation:" + targetedRelation);

          /*
           * We add one replacementRelation for every linkedConcept because of the remapping
           */
          String linkedConceptID = targetedRelation.getRelatedAnnotationID();

          AnnotationMutator.LOGGER.debug("Processing linked concept ID:" + linkedConceptID);
          /*
           * Note that the copy constructor for AnnotatedPhrase does NOT do a deep copy of the
           * conceptRelationList, so the relationsIterator, instantiated above, will apply also to
           * the new AnnotatedPhrase instance, annotatedPhraseUpdated
           */
          AnnotationMutator.LOGGER
              .debug("Creating copy of targeted annotation:" + targetedAnnotation);
          AnnotatedPhrase annotatedPhraseUpdated = new AnnotatedPhrase(targetedAnnotation);

          // AnnotationMutator.LOGGER
          // .debug("Completed copy of TargetedAnnotation:" + targetedAnnotation);
          // AnnotationMutator.LOGGER
          // .debug("Coped to updated Annotated Phrase:" + annotatedPhraseUpdated);

          ConceptRelation replacementRelation = new ConceptRelation(targetedRelation);

          AnnotationMutator.LOGGER.debug("Copied targeted relation to:" + replacementRelation);

          replacementRelation.setRelatedAnnotationID(linkedConceptID);

          // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
          // AnnotationMutator.LOGGER.debug("Adding relation " + replacementRelation + "to:");
          // for (ConceptRelation conceptRelation : potentialTargetedRelations) {
          // AnnotationMutator.LOGGER.debug("\tPotentialTargetedRelation:" + conceptRelation);
          // }
          // }

          potentialTargetedRelations.add(replacementRelation);

          // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
          // AnnotationMutator.LOGGER
          // .debug("Completed adding relation " + replacementRelation + "to:");
          // for (ConceptRelation conceptRelation : potentialTargetedRelations) {
          // AnnotationMutator.LOGGER.debug("\tPotentialTargetedRelation:" + conceptRelation);
          // }
          // }

          AnnotatedPhrase linkedAnnotation = mentionIdToAnnotatedPhraseMap.get(linkedConceptID);
          HashMap<ConceptRelationshipKey, String> conceptAndRelationNameMap =
              remapping.get(linkedAnnotation.getConceptName());

          /*
           * If the conceptAndRelationNameMap has the concept name, then do the remapping of concept
           * and relationship name
           */
          if (conceptAndRelationNameMap != null) {
            String remappedConceptName =
                conceptAndRelationNameMap.get(ConceptRelationshipKey.CONCEPT);
            annotatedPhraseUpdated.setConceptName(remappedConceptName);
            targetedAnnotationsUpdated.add(annotatedPhraseUpdated);

            String replacementRelationName =
                conceptAndRelationNameMap.get(ConceptRelationshipKey.RELATIONSHIP);
            replacementRelation.setConceptRelationName(replacementRelationName);

            // AnnotationMutator.LOGGER.debug("Completed remapping of concept relation:"
            // + replacementRelation + "\n\tfor conceptID:" + linkedConceptID
            // + "\n\tfor targeted relation:" + targetedRelation);
          }

          // AnnotationMutator.LOGGER
          // .debug("Completed processing targetedRelation:" + targetedRelation);
        }

        // AnnotationMutator.LOGGER
        // .debug("Completed remapping of targeted annotation:" + targetedAnnotation);
      }
      // if (AnnotationMutator.LOGGER.isDebugEnabled()) {
      // AnnotationMutator.LOGGER.debug("Updated list of targeted Annotations");
      // for (int i = 0; i < targetedAnnotationsUpdated.size(); i++) {
      // System.out.println("Annotation " + i + ":" + targetedAnnotationsUpdated.get(i));
      // }
      // }
    }

    annotations.addAll(targetedAnnotationsUpdated);

    if (AnnotationMutator.LOGGER.isDebugEnabled()) {
      System.out.println("Completed xml annotations:\n\n");
      AnnotationMutator.LOGGER.debug("Final " + annotations.size() + " annotations:");
      for (int i = 0; i < annotations.size(); i++) {
        System.out.println("\n\nAnnotation " + i + ":\n" + annotations.get(i));
      }
    }

    Collections.sort(annotations, AnnotationMutator.phraseSorter);
  }

  public void removeAttributesFromPhrases(final String targetAttribute,
      final String acceptedConceptsPath, final String xmlSourceFolderPath,
      final String txtSourceFolderPath, final String targetXmlFolderPath) {
    AnnotationImporter xmlImporter = new AnnotationImporter(AnnotationApp.EHOST);
    TextAnalyzer ta = new TextAnalyzer();
    PhraseTokenIntegrator integrator = new PhraseTokenIntegrator();
    File targetFolderFile = new File(targetXmlFolderPath);
    XMLExporterRevised xmlExporter =
        new XMLExporterRevised(AnnotationApp.EHOST, targetFolderFile, true);

    HashSet<String> acceptedConcepts =
        PhraseIDCrossValidationAnalyzer.getConcepts(acceptedConceptsPath);

    List<RaptatPair<File, File>> txtAndXmlFiles =
        GeneralHelper.getXmlFilesAndMatchTxt(txtSourceFolderPath, xmlSourceFolderPath);

    int numberOfFiles = txtAndXmlFiles.size();
    int fileCounter = 1;
    Iterator<RaptatPair<File, File>> fileIterator = txtAndXmlFiles.iterator();

    while (fileIterator.hasNext()) {
      RaptatDocument raptatDocument = null;
      List<AnnotatedPhrase> thePhrases = null;

      try {
        RaptatPair<File, File> txtAndXmlFilePair = fileIterator.next();
        String txtFilePath = txtAndXmlFilePair.left.getAbsolutePath();
        String xmlFilePath = txtAndXmlFilePair.right.getAbsolutePath();

        System.out.println("Importing file " + fileCounter++ + " of " + numberOfFiles + ":"
            + txtAndXmlFilePair.left.getName());

        /* Import the annotations */
        thePhrases = xmlImporter.importAnnotations(xmlFilePath, txtFilePath, acceptedConcepts);
        if (thePhrases == null) {
          thePhrases = new ArrayList<>();
        }

        /*
         * If we have a valid TextAnalyzer and PhraseTokenIntegrator instances, add tokens to
         * annotations so we can get the sentences associated with annotations
         */
        if (ta != null && integrator != null) {
          raptatDocument = ta.processDocument(txtFilePath);
          integrator.addProcessedTokensToAnnotations(thePhrases, raptatDocument, true, 0);
        }

        filterOutAttribute(targetAttribute, thePhrases);

      } catch (Exception e) {
        System.out.println(e);
        e.printStackTrace();
      }

      AnnotationGroup annotationGroup = new AnnotationGroup(raptatDocument, null);
      annotationGroup.setRaptatAnnotations(thePhrases);
      xmlExporter.exportRaptatAnnotationGroup(annotationGroup, true, true);
    }
  }

  /**
   * Replace any annotations from the baseAnnotations list with overlapping annotations from the
   * replacementAnnotations list
   *
   * @param baseAnnotations
   * @param replacementAnnotations
   * @param conceptMap
   * @return
   */
  public List<AnnotatedPhrase> replaceOverlapped(final List<AnnotatedPhrase> baseAnnotations,
      final List<AnnotatedPhrase> replacementAnnotations, final Map<String, String> conceptMap) {

    Collections.sort(baseAnnotations, phraseSorter);
    Collections.sort(replacementAnnotations, phraseSorter);
    List<AnnotatedPhrase> resultAnnotations = new ArrayList<>();
    int minReplacementIndex = 0;

    BASE_ANNOTATIONS_LOOP: for (int baseIndex = 0; baseIndex < baseAnnotations
        .size(); baseIndex++) {
      AnnotatedPhrase baseAnnotation = baseAnnotations.get(baseIndex);
      int replacementIndex = minReplacementIndex;
      while (replacementIndex < replacementAnnotations.size()) {
        AnnotatedPhrase replacementAnnotation = replacementAnnotations.get(replacementIndex);
        if (AnnotatedPhrase.rawOffsetsOverlap(baseAnnotation, replacementAnnotation)) {
          replaceConceptUsingMap(conceptMap, replacementAnnotation);
          resultAnnotations.add(replacementAnnotation);
          minReplacementIndex = replacementIndex + 1;
          continue BASE_ANNOTATIONS_LOOP;
        }
        replacementIndex++;
      }
      replaceConceptUsingMap(conceptMap, baseAnnotation);
      resultAnnotations.add(baseAnnotation);
    }
    return resultAnnotations;
  }

  private void abiProjectCombineAttributesForConcepts2(final List<AnnotatedPhrase> annotations,
      final Map<String, Set<String>> attributesToValuesMap,
      final Map<String, String> mappedAttributeValue, final String prependString) {

    HashMap<String, String> resultAttributeToValueMap = new HashMap<>();

    for (AnnotatedPhrase annotatedPhrase : annotations) {
      for (RaptatAttribute attribute : annotatedPhrase.getPhraseAttributes()) {
        String attributeName = attribute.getName().toLowerCase();
        Set<String> values;
        if ((values = attributesToValuesMap.get(attributeName)) != null) {
          String attributeValue = attribute.getValues().get(0);
          if (values.contains(attributeValue)) {
            String mappedValue = mappedAttributeValue.getOrDefault(attributeValue, attributeValue);
            resultAttributeToValueMap.put(attributeName, mappedValue);
          }
        }
      }

      StringBuilder assignedConceptName = new StringBuilder(prependString);
      List<String> attributeList = new ArrayList<>(resultAttributeToValueMap.keySet());
      Collections.sort(attributeList);
      for (String attributeName : attributeList) {
        assignedConceptName.append("_").append(resultAttributeToValueMap.get(attributeName));
      }
      annotatedPhrase.setConceptName(assignedConceptName.toString());
      annotatedPhrase.setPhraseAttributes(null);
    }

  }


  /**
   * Compare the offsets of two phrases provided as parameters and return -1 if phraseTwo comes
   * before phraseOne, 0 if they overlap, and 1 if phraseTwo comes after (i.e., phraseTwo offsets
   * are greater than phraseOne offsets).
   *
   * We treat nulls like infinity (infinite offset).
   *
   * @param phraseOne
   * @param listTwoPhrase
   * @return
   */
  private int compareOffsets(final AnnotatedPhrase phraseOne, final AnnotatedPhrase phraseTwo) {

    if (phraseOne == null && phraseTwo != null) {
      return -1;
    }

    if (phraseOne != null && phraseTwo == null) {
      return 1;
    }

    if (phraseOne == null && phraseTwo == null) {
      return 0;
    }


    int phraseOneStart = Integer.parseInt(phraseOne.getRawTokensStartOff());
    int phraseTwoEnd = Integer.parseInt(phraseTwo.getRawTokensEndOff());

    if (phraseTwoEnd <= phraseOneStart) {
      return -1;
    }

    int phraseTwoStart = Integer.parseInt(phraseTwo.getRawTokensStartOff());
    int phraseOneEnd = Integer.parseInt(phraseOne.getRawTokensEndOff());

    if (phraseTwoStart >= phraseOneEnd) {
      return 1;
    }

    return 0;
  }

  private List<AnnotatedPhrase> createRelationshipValuePromotedAnnotations(
      final Map<AnnotatedPhrase, Map<String, String>> annotationToRelatedValuesMap) {

    List<AnnotatedPhrase> resultList = new ArrayList<>();

    for (AnnotatedPhrase annotatedPhrase : annotationToRelatedValuesMap.keySet()) {

      Map<String, String> attributeValueMap = annotationToRelatedValuesMap.get(annotatedPhrase);

      AnnotatedPhrase generatedPhrase = new AnnotatedPhrase(annotatedPhrase);
      StringBuilder phraseNameSb =
          new StringBuilder(generatedPhrase.getConceptName().toLowerCase());
      List<String> attributes = new ArrayList<>(attributeValueMap.keySet());
      Collections.sort(attributes);
      for (String attribute : attributes) {
        phraseNameSb.append("_").append(attributeValueMap.get(attribute));
      }
      generatedPhrase.setConceptName(phraseNameSb.toString());

      /*
       * Make deep copy of concept relations so they are separate from those in annotatedPhrase (the
       * copy constructor only makes shallow copy)
       */
      List<ConceptRelation> generatedRelations = new ArrayList<>();
      for (ConceptRelation relation : generatedPhrase.getConceptRelations()) {
        generatedRelations.add(new ConceptRelation(relation));
      }
      generatedPhrase.setConceptRelations(generatedRelations);

      resultList.add(generatedPhrase);
    }

    return resultList;
  }

  /**
   * May 8, 2018
   *
   * @param targetedRelationship
   * @param potentialTargetedRelations
   * @return
   */
  private List<ConceptRelation> extractTargetedRelations(final String targetedRelationship,
      final List<ConceptRelation> potentialTargetedRelations) {
    List<ConceptRelation> targetedRelations = new ArrayList<>();
    ListIterator<ConceptRelation> relationIterator = potentialTargetedRelations.listIterator();
    /*
     * Remove all the concept relations whose name is equal to the targetedRelationship and store
     * them in a separate list. This list, if it is not empty, will be used to generate new
     * concepts.
     */
    while (relationIterator.hasNext()) {
      ConceptRelation conceptRelation = relationIterator.next();
      String conceptRelationName = conceptRelation.getConceptRelationName();

      if (conceptRelationName.equalsIgnoreCase(targetedRelationship)) {
        relationIterator.remove();
        targetedRelations.add(conceptRelation);
      }
    }
    return targetedRelations;
  }

  private AnnotationGroup filterAnnotations(final AnnotationGroup groupForFiltering,
      final List<AnnotatedPhrase> filterPhrases) {
    HashSet<AnnotatedPhrase> filterSentences = new HashSet<>();
    List<AnnotatedPhrase> resultAnnotations = new ArrayList<>();
    for (AnnotatedPhrase filterPhrase : filterPhrases) {
      AnnotatedPhrase filterPhraseSentence = filterPhrase.getAssociatedSentence();
      AnnotatedPhrase priorSentence = filterPhraseSentence.getPreviousSentence();
      AnnotatedPhrase nextSentence = filterPhraseSentence.getNextSentence();

      filterSentences.add(filterPhraseSentence);
      if (priorSentence != null) {
        filterSentences.add(priorSentence);
      }

      if (nextSentence != null) {
        filterSentences.add(nextSentence);
      }
    }

    for (AnnotatedPhrase targetPhrase : groupForFiltering.referenceAnnotations) {
      if (filterSentences.contains(targetPhrase.getAssociatedSentence())) {
        resultAnnotations.add(targetPhrase);
      }
    }

    return new AnnotationGroup(groupForFiltering.getRaptatDocument(), resultAnnotations);
  }

  private List<AnnotationGroup> filterAnnotations(
      final List<RaptatPair<AnnotationGroup, List<AnnotatedPhrase>>> filterPairs) {
    List<AnnotationGroup> resultGroups = new ArrayList<>(filterPairs.size());
    for (RaptatPair<AnnotationGroup, List<AnnotatedPhrase>> raptatPair : filterPairs) {
      AnnotationGroup resultGroup = this.filterAnnotations(raptatPair.left, raptatPair.right);
      resultGroups.add(resultGroup);
    }
    return resultGroups;
  }

  private void filterOutAttribute(final String targetAttribute,
      final List<AnnotatedPhrase> thePhrases) {
    String lowerCaseTargetAttribute = targetAttribute.toLowerCase();
    for (AnnotatedPhrase phrase : thePhrases) {
      List<RaptatAttribute> attributes = phrase.getPhraseAttributes();
      Iterator<RaptatAttribute> attributeIterator = attributes.iterator();
      while (attributeIterator.hasNext()) {
        RaptatAttribute attribute = attributeIterator.next();
        if (attribute.getName().toLowerCase().equals(lowerCaseTargetAttribute)) {
          attributeIterator.remove();
        }
      }
    }
  }

  private Map<AnnotatedPhrase, Map<String, String>> getAnnotationToRelatedValuesMap(
      final List<AnnotatedPhrase> relatedAnnotations,
      final Map<String, AnnotatedPhrase> idToTargetedAnnotationMap,
      final Map<String, String> relationshipToAttributeNames) {

    Map<AnnotatedPhrase, Map<String, String>> resultMap = new HashMap<>();
    for (AnnotatedPhrase relatedAnnotation : relatedAnnotations) {
      for (ConceptRelation relation : relatedAnnotation.getConceptRelations()) {
        String relatedAnnotationID = relation.getRelatedAnnotationID();
        if (idToTargetedAnnotationMap.containsKey(relatedAnnotationID)) {
          String relatedAnnotationName = relatedAnnotation.getConceptName();
          String targetedAttribute = relationshipToAttributeNames.get(relatedAnnotationName);
          for (RaptatAttribute relationshipAttribute : relatedAnnotation.getPhraseAttributes()) {
            String relationshipAttributeName = relationshipAttribute.getName();
            if (relationshipAttributeName.equalsIgnoreCase(targetedAttribute)) {

              /*
               * Ignore that an attribute can have multiple values (created for later expansion of
               * app), we'll only use the first one.
               */
              String attributeValue = relationshipAttribute.getValues().get(0);
              AnnotatedPhrase annotatedPhraseKey =
                  idToTargetedAnnotationMap.get(relatedAnnotationID);
              Map<String, String> attributeValueMap =
                  resultMap.computeIfAbsent(annotatedPhraseKey, key -> new HashMap<>());
              attributeValueMap.put(relationshipAttributeName, attributeValue);
            }
          }
        }
      }
    }
    return resultMap;
  }

  /**
   * @param targetConcepts
   * @param filteringConcepts
   * @param targetSchemaConcepts
   * @param filteringSchemaConcepts
   * @param targetFiles
   * @param filteringFiles
   * @param textFiles
   * @return
   */
  private List<RaptatPair<AnnotationGroup, List<AnnotatedPhrase>>> getFilteringPairs(
      final HashSet<String> targetConcepts, final HashSet<String> filteringConcepts,
      final List<SchemaConcept> targetSchemaConcepts,
      final List<SchemaConcept> filteringSchemaConcepts, final List<File> targetFiles,
      final List<File> filteringFiles, final List<File> textFiles) {
    List<RaptatPair<AnnotationGroup, List<AnnotatedPhrase>>> resultPairList =
        new ArrayList<>(textFiles.size());

    TextAnalyzer ta = new TextAnalyzer();
    PhraseTokenIntegrator pti = new PhraseTokenIntegrator();

    /*
     * Assume files are in such an order that they correspond to one another and process lists.
     */
    Iterator<File> targetFileIterator = targetFiles.iterator();
    Iterator<File> filteringFileIterator = filteringFiles.iterator();
    Iterator<File> textIterator = textFiles.iterator();

    AnnotationImporter annotationImporter = AnnotationImporter.getImporter();
    while (targetFileIterator.hasNext() && filteringFileIterator.hasNext()
        && textIterator.hasNext()) {
      File targetFile = targetFileIterator.next();
      File filteringFile = filteringFileIterator.next();
      File textFile = textIterator.next();

      /*
       * Check to be that files correspond (both reference and raptat files should refer to text
       * file)
       */
      String textFileName = textFile.getName();
      if (!targetFile.getName().contains(textFileName)
          || !filteringFile.getName().contains(textFileName)) {
        GeneralHelper.errorWriter("Unmatched text file");
        System.exit(-1);
      }

      String targetPath = targetFile.getAbsolutePath();
      String filteringPath = filteringFile.getAbsolutePath();
      String textPath = textFile.getAbsolutePath();

      RaptatDocument textDocument = ta.processDocument(textPath);
      List<AnnotatedPhrase> targetAnnotations = annotationImporter.importAnnotations(targetPath,
          textPath, targetConcepts, targetSchemaConcepts);
      List<AnnotatedPhrase> filteringAnnotations = annotationImporter
          .importAnnotations(filteringPath, textPath, filteringConcepts, filteringSchemaConcepts);
      pti.addProcessedTokensToAnnotations(targetAnnotations, textDocument, false, 7);
      pti.addProcessedTokensToAnnotations(filteringAnnotations, textDocument, false, 7);

      AnnotationGroup annotationGroup = new AnnotationGroup(textDocument, targetAnnotations);

      RaptatPair<AnnotationGroup, List<AnnotatedPhrase>> resultPair =
          new RaptatPair<>(annotationGroup, filteringAnnotations);

      resultPairList.add(resultPair);
    }

    return resultPairList;
  }

  private void promoteAttributeValuesToConcepts(final List<AnnotatedPhrase> annotations,
      final String targetConceptName, final String targetAttributeName) {
    for (AnnotatedPhrase annotatedPhrase : annotations) {
      if (annotatedPhrase.getConceptName().equalsIgnoreCase(targetConceptName)) {
        List<RaptatAttribute> replacementAttributes = new ArrayList<>();
        for (RaptatAttribute attribute : annotatedPhrase.getPhraseAttributes()) {
          if (attribute.getName().equalsIgnoreCase(targetAttributeName)) {
            annotatedPhrase.setConceptName(attribute.getValues().get(0));
          } else {
            replacementAttributes.add(attribute);
          }
        }
        annotatedPhrase.setPhraseAttributes(replacementAttributes);
      }
    }
  }

  /**
   * @param conceptMap
   * @param annotatedPhrase
   */
  private void replaceConceptUsingMap(final Map<String, String> conceptMap,
      final AnnotatedPhrase annotatedPhrase) {
    if (conceptMap != null && !conceptMap.isEmpty()) {
      String mappedName;
      String conceptName = annotatedPhrase.getConceptName().toLowerCase();
      if ((mappedName = conceptMap.get(conceptName)) != null) {
        annotatedPhrase.setConceptName(mappedName);
      }
    }
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RunType runType = RunType.TEST;
        AnnotationMutator mutator = new AnnotationMutator();

        switch (runType) {
          case TEST:
            break;
          default:
            break;
        }
      }
    });
  }
}
