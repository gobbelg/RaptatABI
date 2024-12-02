/** */
package src.main.gov.va.vha09.grecc.raptat.gg.sql;

/* Class used to build ConceptRelation object from fields */
public class ConceptRelationFields {

  public final String conceptRelationID;
  public final String conceptRelationName;
  public final String idsOfRelatedAnnotations;


  /**
   * @param conceptRelationID
   * @param conceptRelationName
   * @param idOfRelatedAnnotation
   */
  public ConceptRelationFields(String conceptRelationID, String conceptRelationName,
      String idsOfRelatedAnnotations) {
    super();
    this.conceptRelationID = conceptRelationID;
    this.conceptRelationName = conceptRelationName;
    this.idsOfRelatedAnnotations = idsOfRelatedAnnotations;
  }
}
