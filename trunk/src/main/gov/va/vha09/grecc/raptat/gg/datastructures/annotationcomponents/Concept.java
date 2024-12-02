/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;

/** @author sahask */
public class Concept implements Serializable {
  /** */
  private static final long serialVersionUID = 2800968090205876L;

  private static final transient ToStringStyle toStringStyle = RaptatStringStyle.getInstance();


  private final HashMap<String, List<String>> attributeValueMap = new HashMap<>();

  private final String concept;

  /*
   * Glenn comment - 10/29/15 - refactored name to better reflect the info the field contains
   */
  private final boolean isRelationship;

  /*
   * Glenn comments 2 - 10/27/15 - Refactored namse from 'Left' to 'LinkedFrom' and 'Right' to
   * 'LinkedTo'. Note that the getters and setters for these fields were also refactored to reflect
   * the name changes.
   */
  // For relations
  private final List<String> linkedFromConcepts = new ArrayList<>();
  private final List<String> linkedToConcepts = new ArrayList<>();

  public Concept(String concept, boolean isRelation) {
    this.concept = concept;
    this.isRelationship = isRelation;
  }


  public void addAttributeValues(String attribute, List<String> values) {
    this.attributeValueMap.put(attribute, values);
  }


  public void addLinkedFromConceptsToRelation(List<String> left) {
    this.linkedFromConcepts.addAll(left);
  }


  public void addLinkedFromConceptToRelation(String left) {
    this.linkedFromConcepts.add(left);
  }


  public void addLinkedToConceptsToRelation(List<String> right) {
    // leftConcept = left;
    this.linkedToConcepts.addAll(right);
  }


  public HashMap<String, List<String>> getAttributeValueMap() {
    return this.attributeValueMap;
  }


  public List<String> getAttributeValues(String attribute) {
    return this.attributeValueMap.get(attribute);
  }


  public String getConcept() {
    return this.concept;
  }


  /*
   * Glenn comments 2 - 10/27/15 - Refactored name from 'Left' to 'LinkedFrom'
   */
  public List<String> getLinkedFromConcepts() {
    return this.linkedFromConcepts;
  }


  /*
   * Glenn comments 2 - 10/27/15 - Refactored name from 'Right' to 'LinkedTo'
   */
  public List<String> getLinkedToConcepts() {
    return this.linkedToConcepts;
  }


  /*
   * Glenn comment - 10/29/15 - refactored name to better reflect the info the field contains
   */
  public boolean isRelationship() {
    return this.isRelationship;
  }


  /*
   * Glenn comments 2 - 10/27/15 - added toString method using following code
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, Concept.toStringStyle).append("Concept", this.concept)
        .append("isRelationship", this.isRelationship)
        .append("AttributeValueMap", this.attributeValueMap)
        .append("LeftConcept", this.linkedFromConcepts)
        .append("RightConcept", this.linkedToConcepts).toString();
  }


  public static HashSet<String> getAttributeListFromProjectSchema(List<Concept> concepts) {
    HashSet<String> attributeSet = new HashSet<>();

    for (Concept concept : concepts) {
      attributeSet.addAll(concept.getAttributeValueMap().keySet());
    }

    return attributeSet;
  }
}
