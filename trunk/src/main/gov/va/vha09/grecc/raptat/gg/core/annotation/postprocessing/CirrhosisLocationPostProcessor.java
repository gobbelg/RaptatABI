/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/** @author glenn */
public class CirrhosisLocationPostProcessor implements AnnotationGroupPostProcessor {
  private enum RunType {
    POST_PROCESS;
  }

  private HashMap<String, File> xmlLocationFileMap = new HashMap<>();

  private HashMap<String, File> textLocationFileMap = new HashMap<>();

  private final HashSet<String> linkableConcepts = new HashSet<>();
  private final boolean usePOS = OptionsManager.getInstance().getUsePOS();
  private final int previousSentences;


  private final int nextSentences;

  private String relationshipName;

  /** */
  private static final long serialVersionUID = 1910492262668370058L;

  protected static final Logger LOGGER = Logger.getLogger(CirrhosisLocationPostProcessor.class);
  private static AnnotationImporter annotationImporter = null;
  private static List<SchemaConcept> schemaConcepts = null;
  private static PhraseTokenIntegrator phraseTokenIntegrator = null;

  // String xmlDirectory, String textDirectory, String schemaFilePath,String
  // annotationAppString
  public CirrhosisLocationPostProcessor(List<String> constructorParameters) {
    this(constructorParameters.get(0), constructorParameters.get(1), constructorParameters.get(2),
        constructorParameters.get(3),
        AnnotationApp.valueOf(constructorParameters.get(4).toUpperCase()),
        constructorParameters.get(5), constructorParameters.get(6));
  }


  /**
   * @param xmlDirectory - Path to directory story the XML for location annotations that will be
   *        used for determining the location relationships for other concepts in the cirrhosis
   *        schema.
   */
  public CirrhosisLocationPostProcessor(String relationshipName, String xmlDirectory,
      String textDirectory, String schemaFilePath, AnnotationApp annotationApp,
      String previousSentences, String nextSentences) {
    CirrhosisLocationPostProcessor.LOGGER.setLevel(Level.DEBUG);

    boolean removeFileExtension = true;
    this.xmlLocationFileMap = getFiles(xmlDirectory, removeFileExtension, new String[] {"xml"});

    removeFileExtension = false;
    this.textLocationFileMap = getFiles(textDirectory, removeFileExtension, new String[] {"txt"});

    if (CirrhosisLocationPostProcessor.annotationImporter == null) {
      CirrhosisLocationPostProcessor.annotationImporter = new AnnotationImporter(annotationApp);
    }

    if (CirrhosisLocationPostProcessor.schemaConcepts == null) {
      CirrhosisLocationPostProcessor.schemaConcepts =
          SchemaImporter.importSchemaConcepts(schemaFilePath);
    }

    if (CirrhosisLocationPostProcessor.phraseTokenIntegrator == null) {
      CirrhosisLocationPostProcessor.phraseTokenIntegrator = new PhraseTokenIntegrator();
    }

    this.previousSentences = Integer.parseInt(previousSentences);
    this.nextSentences = Integer.parseInt(nextSentences);

    this.relationshipName = relationshipName;
    getLinkableConcepts();
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing.
   * AnnotationGroupPostProcessor#postProcess(src.main.gov.va.vha09.grecc. raptat.
   * gg.datastructures.AnnotationGroup)
   */
  @Override
  public void postProcess(AnnotationGroup annotationGroup) {
    List<AnnotatedPhrase> locationAnnotations = getLocationAnnotations(annotationGroup);

    if (locationAnnotations == null || locationAnnotations.isEmpty()) {
      return;
    }

    HashMap<AnnotatedPhrase, Set<AnnotatedPhrase>> sentenceToPhraseMap = new HashMap<>();

    /*
     * Find sentences where all the annotated phrases are and any sentence 'previousSentences'
     * upstream or 'nextSentences' downstream. Create map from sentences to all annotations that
     * could be linked (based on the schema) to a phrase with the 'conceptToLink' provided in the
     * constructor.
     */
    for (AnnotatedPhrase annotatedPhrase : annotationGroup.raptatAnnotations) {
      if (this.linkableConcepts.contains(annotatedPhrase.getConceptName().toLowerCase())) {
        AnnotatedPhrase associatedSentence = annotatedPhrase.getAssociatedSentence();
        addToSentenceToPhraseMap(sentenceToPhraseMap, annotatedPhrase, associatedSentence);
        AnnotatedPhrase previousSentence = associatedSentence;
        for (int i = 1; i <= this.previousSentences; i++) {
          previousSentence = previousSentence.getPreviousSentence();
          addToSentenceToPhraseMap(sentenceToPhraseMap, annotatedPhrase, previousSentence);
        }
        AnnotatedPhrase nextSentence = associatedSentence;
        for (int i = 1; i <= this.nextSentences; i++) {
          nextSentence = nextSentence.getNextSentence();
          addToSentenceToPhraseMap(sentenceToPhraseMap, annotatedPhrase, nextSentence);
        }
      }
    }

    /*
     * Find the location annotations close to a potentially linked annotation and add them to the
     * raptatAnnotations within the annotationGroup parameter of this method
     */
    for (AnnotatedPhrase locationAnnotation : locationAnnotations) {
      AnnotatedPhrase linkedAnnotation = null;
      if ((linkedAnnotation =
          findAnnotationToLink(locationAnnotation, sentenceToPhraseMap)) != null) {
        annotationGroup.raptatAnnotations.add(locationAnnotation);
        linkViaRelationship(locationAnnotation, linkedAnnotation);
      }
    }
  }


  /**
   * @param associatedSentences
   * @param annotatedPhrase
   * @return
   */
  private void addToSentenceToPhraseMap(
      HashMap<AnnotatedPhrase, Set<AnnotatedPhrase>> associatedSentences,
      AnnotatedPhrase annotatedPhrase, AnnotatedPhrase associatedSentence) {
    Set<AnnotatedPhrase> associatedPhrases;
    if ((associatedPhrases = associatedSentences.get(associatedSentence)) == null) {
      associatedPhrases = new HashSet<>();
      associatedSentences.put(associatedSentence, associatedPhrases);
    }
    associatedPhrases.add(annotatedPhrase);
  }


  /**
   * Remove an annotation in the sentenceToPhraseMap HashMap. This is done once the annotation is
   * linked to a location so that it is not used twice to link to two different locations.
   *
   * @param sentenceToPhraseMap
   * @param linkedAnnotation
   */
  private void cleanSentenceToPhraseMap(
      HashMap<AnnotatedPhrase, Set<AnnotatedPhrase>> sentenceToPhraseMap,
      AnnotatedPhrase linkedAnnotation) {
    AnnotatedPhrase associatedSentence = linkedAnnotation.getAssociatedSentence();
    removePhraseFromMap(linkedAnnotation, associatedSentence, sentenceToPhraseMap);

    AnnotatedPhrase adjacentSentence = null;
    if ((adjacentSentence = associatedSentence.getPreviousSentence()) != null) {
      removePhraseFromMap(linkedAnnotation, adjacentSentence, sentenceToPhraseMap);
    }

    if ((adjacentSentence = associatedSentence.getNextSentence()) != null) {
      removePhraseFromMap(linkedAnnotation, adjacentSentence, sentenceToPhraseMap);
    }
  }


  /**
   * @param annotationGroup
   * @param sentenceToPhraseMap
   * @param locationAnnotation
   */
  private AnnotatedPhrase findAnnotationToLink(AnnotatedPhrase locationAnnotation,
      HashMap<AnnotatedPhrase, Set<AnnotatedPhrase>> sentenceToPhraseMap) {
    AnnotatedPhrase locationSentence = locationAnnotation.getAssociatedSentence();
    Set<AnnotatedPhrase> candidateLinks = null;
    if ((candidateLinks = sentenceToPhraseMap.get(locationSentence)) != null
        && !candidateLinks.isEmpty()) {
      AnnotatedPhrase linkedAnnotation = getClosestLink(locationAnnotation, candidateLinks);
      return linkedAnnotation;
    }

    return null;
  }


  private AnnotatedPhrase getClosestLink(AnnotatedPhrase locationAnnotation,
      Set<AnnotatedPhrase> candidateLinks) {
    AnnotatedPhrase closestLink = null;

    List<RaptatToken> locationTokens = locationAnnotation.getProcessedTokens();
    int startLocationToken = locationTokens.get(0).getTokenIndexInDocument();
    int endLocationToken = locationTokens.get(locationTokens.size() - 1).getTokenIndexInDocument();
    int distance = Integer.MAX_VALUE;

    for (AnnotatedPhrase candidateLink : candidateLinks) {
      List<RaptatToken> linkTokens = candidateLink.getProcessedTokens();
      int startLinkToken = linkTokens.get(0).getTokenIndexInDocument();
      int endLinkToken = linkTokens.get(linkTokens.size() - 1).getTokenIndexInDocument();

      int candidateDistance;
      if (startLocationToken < startLinkToken) {
        candidateDistance = Math.abs(startLinkToken - endLocationToken);
      } else {
        candidateDistance = Math.abs(startLocationToken - endLinkToken);
      }

      if (candidateDistance < distance) {
        distance = candidateDistance;
        closestLink = candidateLink;
      }
    }

    return closestLink;
  }


  /**
   * @param fileDirectoryPath
   * @return
   */
  private HashMap<String, File> getFiles(String fileDirectoryPath, boolean removeNameExtension,
      String[] extensionForFiltering) {
    HashMap<String, File> fileLocationMap = new HashMap<>();
    Iterator<File> fileIterator =
        FileUtils.iterateFiles(new File(fileDirectoryPath), extensionForFiltering, false);

    while (fileIterator.hasNext()) {
      File nextFile = fileIterator.next();
      String textFileName = nextFile.getName();
      if (removeNameExtension) {
        textFileName = GeneralHelper.removeFileExtension(textFileName, '.');
      }
      fileLocationMap.put(nextFile.getName(), nextFile);
    }

    return fileLocationMap;
  }


  /** Jun 21, 2018 */
  private void getLinkableConcepts() {
    for (SchemaConcept schemaConcept : CirrhosisLocationPostProcessor.schemaConcepts) {
      CirrhosisLocationPostProcessor.LOGGER.debug("Concept:" + schemaConcept);
      if (schemaConcept.isRelationship()
          && schemaConcept.getConceptName().equalsIgnoreCase(this.relationshipName)) {
        this.linkableConcepts.addAll(schemaConcept.getLinkedToConcepts());
        return;
      }
    }
  }


  /**
   * @param annotationGroup
   * @return
   */
  private List<AnnotatedPhrase> getLocationAnnotations(AnnotationGroup annotationGroup) {
    RaptatDocument raptatDocument = annotationGroup.getRaptatDocument();
    String textSource = raptatDocument.getTextSource().get();
    String xmlSource = textSource + ".knowtator.xml";

    File xmlLocationFile = null;
    File textLocationFile = null;

    if ((xmlLocationFile = this.xmlLocationFileMap.get(xmlSource)) == null
        || (textLocationFile = this.textLocationFileMap.get(textSource)) == null) {
      System.err.println("Error loading unmatched annotations for location annotation processing ");
      System.exit(-1);
    }

    List<AnnotatedPhrase> locationAnnotations = CirrhosisLocationPostProcessor.annotationImporter
        .importAnnotations(xmlLocationFile.getAbsolutePath(), textLocationFile.getAbsolutePath(),
            null, CirrhosisLocationPostProcessor.schemaConcepts, null);
    CirrhosisLocationPostProcessor.phraseTokenIntegrator
        .addProcessedTokensToAnnotations(locationAnnotations, raptatDocument, this.usePOS, 0);

    return locationAnnotations;
  }


  /**
   * @param locationAnnotation
   * @param linkedAnnotation
   */
  private void linkViaRelationship(AnnotatedPhrase locationAnnotation,
      AnnotatedPhrase linkedAnnotation) {
    String conceptRelationID = UniqueIDGenerator.INSTANCE.getUnique();
    String linkedAnnotationID = linkedAnnotation.getMentionId();

    ConceptRelation linkedRelationship =
        new ConceptRelation(conceptRelationID, this.relationshipName, linkedAnnotationID, null);
    List<ConceptRelation> conceptRelations = locationAnnotation.getConceptRelations();
    conceptRelations.add(linkedRelationship);
  }


  private void removePhraseFromMap(AnnotatedPhrase annotatedPhrase,
      AnnotatedPhrase phraseAssociatedSentence,
      HashMap<AnnotatedPhrase, Set<AnnotatedPhrase>> sentenceToPhraseMap) {
    Set<AnnotatedPhrase> sentenceAnnotations = sentenceToPhraseMap.get(phraseAssociatedSentence);
    if (sentenceAnnotations != null && !sentenceAnnotations.isEmpty()) {
      sentenceAnnotations.remove(annotatedPhrase);
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RunType runType = RunType.POST_PROCESS;

        String conceptToLink = null;
        String xmlConceptsDirectory = null;
        String xmlLocationDirectory = null;
        String textLocationDirectory = null;
        String schemaFilePath = null;
        AnnotationApp annotationApp = AnnotationApp.EHOST;
        String previousSentences = null;
        String nextSentences = null;
        CirrhosisLocationPostProcessor postProcessor = new CirrhosisLocationPostProcessor(
            conceptToLink, xmlLocationDirectory, textLocationDirectory, schemaFilePath,
            annotationApp, previousSentences, nextSentences);

        switch (runType) {
          case POST_PROCESS:
            this.postProcess(postProcessor, xmlConceptsDirectory, xmlLocationDirectory,
                textLocationDirectory, schemaFilePath);
            break;
          default:
            break;
        }

        System.out.println("\nMerging complete.\nExiting annotation filtering program.");
        System.exit(0);
      }


      private void postProcess(CirrhosisLocationPostProcessor postProcessor,
          String xmlConceptsDirectory, String xmlLocationDirectory, String textLocationDirectory,
          String schemaFilePath) {}
    });
  }
}
