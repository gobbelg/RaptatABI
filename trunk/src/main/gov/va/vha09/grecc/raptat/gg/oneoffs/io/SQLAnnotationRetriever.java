/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/** @author Glenn Gobbel */
public class SQLAnnotationRetriever {
  /* Class used to build AnnotatedPhrase object from fields in SQL database */
  private class AnnotatedPhraseFields {
    private final String mentionID;
    private final String startOffset;
    private final String endOffset;
    private final String phraseText;
    private final List<ConceptRelation> conceptRelations;
    private final List<RaptatAttribute> attributes;


    /**
     * @param mentionID
     * @param startOffset
     * @param endOffset
     * @param phraseText
     * @param conceptRelations
     * @param attributes
     */
    public AnnotatedPhraseFields(String mentionID, String startOffset, String endOffset,
        String phraseText, List<ConceptRelation> conceptRelations,
        List<RaptatAttribute> attributes) {
      super();
      this.mentionID = mentionID;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.phraseText = phraseText;
      this.conceptRelations = conceptRelations;
      this.attributes = attributes;
    }
  }

  /* Class used to build ConceptRelation object from fields */
  private class ConceptRelationFields {
    private final String conceptRelationID;
    private final String conceptRelationName;
    private final List<String> idsOfRelatedAnnotations;


    /**
     * @param conceptRelationID
     * @param conceptRelationName
     * @param idsOfRelatedAnnotations
     */
    public ConceptRelationFields(String conceptRelationID, String conceptRelationName,
        List<String> idsOfRelatedAnnotations) {
      super();
      this.conceptRelationID = conceptRelationID;
      this.conceptRelationName = conceptRelationName;
      this.idsOfRelatedAnnotations = idsOfRelatedAnnotations;
    }
  }

  /* Class used to build RaptatAttribute object from fields */
  private class RaptatAttributeFields {
    private final String id;
    private final String typeName;
    private final List<String> values;


    /**
     * @param id
     * @param typeName
     * @param values
     */
    public RaptatAttributeFields(String id, String typeName, List<String> values) {
      super();
      this.id = id;
      this.typeName = typeName;
      this.values = values;
    }
  }

  /* Class used to build RaptatDocument object from fields in SQL database */
  private class RaptatDocumentFields {
    private final String textSource;


    private RaptatDocumentFields(String textSource) {
      this.textSource = textSource;
    }


    private RaptatDocument getRaptatDocumentFromFields() {
      RaptatDocument document = new RaptatDocument();
      document.setTextSourcePath(Optional.of(this.textSource));

      return document;
    }
  }


  /*
   * General, unspecified object for storing the information used to connect to a database
   */
  private SQLAnnotationWriter.DbConnectionObject databaseConnectionInformation;

  /** */
  public SQLAnnotationRetriever() {}


  /*
   * The databaseConnectionInformation is a placeholder used to represent all the needed information
   * to connect to the database for retrieving annotations
   */
  public SQLAnnotationRetriever(
      SQLAnnotationWriter.DbConnectionObject databaseConnectionInformation) {
    this.databaseConnectionInformation = databaseConnectionInformation;
  }


  /*
   * Implement this method to set up a connection to the database and make sure it is valid. If so,
   * return true. If not, return false;
   */
  public boolean connectToDatabase() {
    boolean validConnection = false;

    return validConnection;
  }


  public Set<AnnotationGroup> getNextAnnotationGroupBatch() {
    /* Set the number of groups you want to return as a set */
    int returnSetSize = 100;
    Set<AnnotationGroup> groupsToReturn = new HashSet<>(returnSetSize);

    for (int groupIndex = 0; groupIndex < returnSetSize; groupIndex++) {
      AnnotationGroup annotationGroup = getNextAnnotationGroup();
      groupsToReturn.add(annotationGroup);
    }

    return groupsToReturn;
  }


  /**
   * May 29, 2018
   *
   * @return
   */
  private String getDocumentTextSource() {
    String textSource = "Name of document that was annotated";
    return textSource;
  }


  /**
   * May 29, 2018
   *
   * @return
   */
  private AnnotationGroup getNextAnnotationGroup() {
    String textSource = getDocumentTextSource();
    RaptatDocument document = new RaptatDocument();
    document.setTextSourcePath(Optional.of(textSource));

    List<AnnotatedPhrase> annotatedPhrases = getNextAnnotationGroupPhrases();
    return null;
  }


  /**
   * May 29, 2018
   *
   * @return
   */
  private List<AnnotatedPhrase> getNextAnnotationGroupPhrases() {
    List<AnnotatedPhrase> annotatedPhrases = new ArrayList<>();

    return annotatedPhrases;
  }


  /**
   * May 29, 2018
   *
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
}
