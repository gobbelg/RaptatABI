package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.sql.ConceptRelationFields;

/**
 * ConceptRelation objects are stored within AnnotatedPhrase objects. There is one ConceptRelation
 * Object for each distinct AnnotatedPhrase object that a separate, other AnnotatedPhrase object is
 * related to. For example, if AnnotatedPhrase 'A' is linked to AnnotatedPhrase 'B', AnnotatedPhrase
 * 'A' would have one ConceptRelation object for that link, and the 'mentionID' field of
 * AnnotatedPhrase 'B' would be stored in the field 'idsOfRelatedAnnotations' for this
 * ConceptRelationObject.
 *
 * @author Sanjib Saha, created on July 31, 2014
 */
@SuppressWarnings("FieldMayBeFinal")
public class ConceptRelation implements Comparable<Object>, Serializable {
  /** */
  private static final long serialVersionUID = 5684985584343772973L;


  /*
   * Glenn comment - 11/03/15 - Refactored from relationID to conceptRelationID.
   */
  private final String conceptRelationID;

  /*
   * Glenn comment - 11/03/15 - Refactored from relation to conceptRelationName. Note that schema
   * relationship types are stored in RapTAT as SchemaConcept objects. The conceptRelationName field
   * stores the 'conceptName' field of the SchemaConcept relationship corresponding to the
   * ConceptRelation object.
   */
  private String conceptRelationName;

  /*
   * Glenn comment - 11/03/15 - Refactored from annotationID to idsOfRelatedAnnotations.
   */
  private String idOfRelatedAnnotation = "";

  /*
   * Note that SchemaConcept objects that are relationships can have attributes and values of those
   * attributes. This list stores the attributes and values for a specific ConceptRelation object
   */
  private List<RaptatAttribute> attributes = new ArrayList<>();

  /** This is a copy constructor except that the unique id is not retained */
  public ConceptRelation(final ConceptRelation conceptRelation) {
    this.conceptRelationID = UniqueIDGenerator.INSTANCE.getUnique();
    this.conceptRelationName = conceptRelation.conceptRelationName;

    if (conceptRelation.idOfRelatedAnnotation != null) {
      this.idOfRelatedAnnotation = conceptRelation.idOfRelatedAnnotation;
    } else {
      this.idOfRelatedAnnotation = "";
    }

    if (conceptRelation.attributes != null) {
      this.attributes = conceptRelation.attributes;
    }
  }


  /**
   * @param relationFields
   */
  public ConceptRelation(final ConceptRelationFields relationFields) {
    this.conceptRelationID = relationFields.conceptRelationID;
    this.conceptRelationName = relationFields.conceptRelationName;
    this.idOfRelatedAnnotation = relationFields.idsOfRelatedAnnotations;
  }


  /**
   * Constructor of ConceptRelation class
   *
   * @param conceptRelationID id of the relation
   * @param relationName the relationship
   * @param id list of the annotations related with this relationship
   */
  public ConceptRelation(final String conceptRelationID, final String relationName, final String id,
      final List<RaptatAttribute> attributes) {
    this.conceptRelationID = conceptRelationID;
    this.conceptRelationName = relationName.toLowerCase();

    this.idOfRelatedAnnotation = id;

    if (attributes != null) {
      this.attributes = attributes;
    }
  }


  @Override
  public int compareTo(final Object o) {
    ConceptRelation other = (ConceptRelation) o;
    return new CompareToBuilder().append(this.conceptRelationName, other.conceptRelationName)
        .toComparison();
  }


  /*
   * Note that equals method does not include the field corresponding to the list of annotations
   * being equal as a requirement. This is done to avoid circular logic and an infinite loop when a
   * call to equals for relations would result in a call to equals for the associated
   * AnnotatedPhrase instances which could then call equals for the same ConceptRelation instances,
   * etc
   *
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    ConceptRelation other = (ConceptRelation) obj;

    return new EqualsBuilder().append(this.conceptRelationID, other.conceptRelationID)
        .append(this.conceptRelationName, other.conceptRelationName)
        .append(this.idOfRelatedAnnotation, other.idOfRelatedAnnotation)
        .append(this.attributes, other.attributes).isEquals();
  }


  /**
   * Returns the attributes of the relationship
   *
   * @return list of attributes
   */
  public List<RaptatAttribute> getAttributes() {
    return this.attributes;
  }


  public String getCanonicalName(final String prependString) {
    String tab = "\t";
    StringBuilder sb = new StringBuilder(prependString);
    /*
     * Offset relations from concept attributes
     */
    sb.append(tab).append(tab).append(tab).append(tab);

    sb.append("REL");
    sb.append(tab);
    sb.append(this.conceptRelationName);
    sb.append(tab);
    sb.append("RELSHP_ID");
    sb.append(tab);
    sb.append(this.conceptRelationID);
    sb.append(tab);
    String prependRelation = sb.toString();
    sb = new StringBuilder("");

    for (RaptatAttribute attribute : this.attributes) {
      sb.append(attribute.getCanonicalName(prependRelation));
    }

    sb.append(prependRelation);

    /*
     * Offset relationship info from relationship attributes and values
     */
    sb.append(tab).append(tab).append(tab).append(tab);
    sb.append("RELANN_ID");
    sb.append(tab);
    sb.append(this.idOfRelatedAnnotation);
    sb.append("\n");

    return sb.toString();
  }


  /**
   * Returns the relationship
   *
   * @return relationship
   */
  public String getConceptRelationName() {
    return this.conceptRelationName;
  }


  /**
   * Returns the id of the relation
   *
   * @return relation id
   */
  public String getID() {
    return this.conceptRelationID;
  }


  /**
   * Returns the id of the related annotation
   *
   * @return annotation list
   */
  public String getRelatedAnnotationID() {
    return this.idOfRelatedAnnotation;
  }


  public void setConceptRelationName(final String conceptRelationName) {
    this.conceptRelationName = conceptRelationName;
  }


  public void setRelatedAnnotationID(final String idOfRelatedAnnotation) {
    this.idOfRelatedAnnotation = idOfRelatedAnnotation;
  }


  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this, RaptatStringStyle.getInstance());
    tsb.append("ConceptRelationName", this.conceptRelationName);
    tsb.append("ConceptRelationID", this.conceptRelationID);
    tsb.append("IDOfRelationAnnotation", this.idOfRelatedAnnotation);
    tsb.append("Attributes", this.attributes);

    return tsb.toString();
  }


  public static String toStringForDisplay(ConceptRelation relation,
      final Set<String> sentenceAssociatedPhrases, final int minRelated) {
    if (relation == null) {
      relation = new ConceptRelation("", "", null, null);
    }
    /*
     * Standardize to displaying minRelated values with blanks separated by tabs if there are less
     * than minRelated
     */
    StringBuilder sb = new StringBuilder(relation.conceptRelationName);
    sb.append("\t").append(relation.conceptRelationID);
    String value = relation.idOfRelatedAnnotation;
    sb.append("\t").append(value);
    String relationInSameSentence =
        value.isEmpty() ? "" : Boolean.toString(sentenceAssociatedPhrases.contains(value));
    sb.append("\t").append(relationInSameSentence);

    return sb.toString();
  }
}
