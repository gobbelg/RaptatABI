package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.util.List;

/**
 * Annotated Concept class stores the concept, attribute and relationship information of an
 * annotated phrase
 *
 * @author Sanjib Saha, created on July 31, 2014
 */
public class AnnotatedConcept {
  private final String concept;
  private final List<String> attributeID;


  /**
   * Constructor of AnnotatedConcept class
   *
   * @param concept concept of the annotated phrase
   * @param ID list of ID's of attribute and relationship
   */
  public AnnotatedConcept(String concept, List<String> ID) {
    this.concept = concept;
    this.attributeID = ID;
  }


  /**
   * Returns the phrase concept
   *
   * @return phrase concept
   */
  public String getConcept() {
    return this.concept;
  }


  /**
   * Returns the list of ID's of attribute and relationship
   *
   * @return ID list
   */
  public List<String> getIDList() {
    return this.attributeID;
  }
}
