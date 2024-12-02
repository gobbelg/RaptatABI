/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.text.StringEscapeUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;

/** @author sahask */
public class SchemaConcept implements Serializable {
  private static final long serialVersionUID = -4590476805174554642L;

  private static final transient ToStringStyle toStringStyle = RaptatStringStyle.getInstance();


  /*
   * attributeValueMap maps an attribute, described by a string, to the list of potential values
   * that the string can take on.
   */
  private final HashMap<String, List<String>> attributeValueMap = new HashMap<>();


  private final HashMap<String, String> attributeDefaultValueMap = new HashMap<>();

  private final String conceptName;

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
  private final HashSet<String> linkedFromConcepts = new HashSet<>();
  private final HashSet<String> linkedToConcepts = new HashSet<>();

  public SchemaConcept(String concept, boolean isRelation) {
    this.conceptName = concept.toLowerCase();
    this.isRelationship = isRelation;
  }

  public void addAttributeValues(String attribute, List<String> values) {
    this.attributeValueMap.put(attribute.toLowerCase(), values);
  }


  public void addLinkedFromConceptsToRelation(List<String> linkedFromConcepts) {
    for (String curConcept : linkedFromConcepts) {
      /*
       * EHost may add escape characters to strings in relationship. We have to unescape them to get
       * the original name of the linked concept
       */
      String unescapedConcept = StringEscapeUtils.unescapeJava(curConcept);
      this.linkedFromConcepts.add(unescapedConcept.toLowerCase());
    }
  }


  public void addLinkedFromConceptToRelation(String left) {
    this.linkedFromConcepts.add(left.toLowerCase());
  }


  public void addLinkedToConceptsToRelation(List<String> linkedToCconcepts) {
    for (String curConcept : linkedToCconcepts) {
      /*
       * EHost may add escape characters to strings in relationship. We have to unescape them to get
       * the original name of the linked concept
       */
      String unescapedConcept = StringEscapeUtils.unescapeJava(curConcept);
      this.linkedToConcepts.add(unescapedConcept.toLowerCase());
    }
  }


  public HashMap<String, List<String>> getAttributeValueMap() {
    return this.attributeValueMap;
  }


  public List<String> getAttributeValues(String attribute) {
    return this.attributeValueMap.get(attribute.toLowerCase());
  }


  public String getConceptName() {
    return this.conceptName;
  }


  public String getDefaultAttributeValue(String attribute) {
    return this.attributeDefaultValueMap.get(attribute);
  }


  /*
   * Glenn comments 2 - 10/27/15 - Refactored name from 'Left' to 'LinkedFrom'
   */
  public HashSet<String> getLinkedFromConcepts() {
    return this.linkedFromConcepts;
  }


  /*
   * Glenn comments 2 - 10/27/15 - Refactored name from 'Right' to 'LinkedTo'
   */
  public HashSet<String> getLinkedToConcepts() {
    return this.linkedToConcepts;
  }


  /*
   * Glenn comment - 10/29/15 - refactored name to better reflect the info the field contains
   */
  public boolean isRelationship() {
    return this.isRelationship;
  }


  public void setDefaultAttributeValue(String attribute, String value) {
    this.attributeDefaultValueMap.put(attribute, value);
  }


  /*
   * Glenn comments 2 - 10/27/15 - added toString method using following code
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, SchemaConcept.toStringStyle)
        .append("Concept", this.conceptName).append("isRelationship", this.isRelationship)
        .append("AttributeValueMap", this.attributeValueMap)
        .append("LinkedFromConcepts", this.linkedFromConcepts)
        .append("LinkedToConcepts", this.linkedToConcepts).toString();
  }


  public static HashSet<String> getAcceptableConceptNames(String conceptsFilePath) {
    HashSet<String> acceptedConcepts = new HashSet<>();

    CSVReader conceptReader = null;
    if (conceptsFilePath != null && !conceptsFilePath.isEmpty()) {
      File conceptsFile = new File(conceptsFilePath);
      if (conceptsFile.exists() && conceptsFile.canRead()) {
        conceptReader = new CSVReader(conceptsFile);
      }
    }

    if (conceptReader == null) {
      conceptReader = new CSVReader("Select text file with concepts to be analyzed");
    }

    if (conceptReader.isValid()) {
      String[] nextDataSet;
      while ((nextDataSet = conceptReader.getNextData()) != null) {
        acceptedConcepts.add(nextDataSet[0].toLowerCase());
      }
      conceptReader.close();
      OptionsManager.getInstance().setLastSelectedDir(new File(conceptReader.getInFileDirectory()));
    }

    return acceptedConcepts;
  }


  public static HashSet<String> getAttributeListFromProjectSchema(
      List<SchemaConcept> schemaConcepts) {
    HashSet<String> attributeSet = new HashSet<>();

    for (SchemaConcept schemaConcept : schemaConcepts) {
      attributeSet.addAll(schemaConcept.getAttributeValueMap().keySet());
    }

    return attributeSet;
  }
}
