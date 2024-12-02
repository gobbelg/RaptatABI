package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.sql.RaptatAttributeFields;

/**
 * Phrase attribute class stores the information of a single attribute of an annotated phrase
 *
 * @author Sanjib Saha, created on July 31, 2014
 */
@SuppressWarnings("FieldMayBeFinal")
public class RaptatAttribute implements Comparable<RaptatAttribute>, Serializable {
  /** */
  private static final long serialVersionUID = -7559764829575009116L;


  private final String id;


  private String typeName;


  private List<String> values = new ArrayList<>(1);

  private final AttributeAssignmentMethod assignmentMethod;

  /* Copy constructor */
  public RaptatAttribute(RaptatAttribute phraseAttribute) {
    this.id = phraseAttribute.id;
    this.typeName = phraseAttribute.typeName;
    this.values = new ArrayList<>(phraseAttribute.values);
    this.assignmentMethod = phraseAttribute.assignmentMethod;
  }

  public RaptatAttribute(RaptatAttributeFields attributeFields) {
    this.id = attributeFields.id;
    this.typeName = attributeFields.typeName;
    this.values = attributeFields.values;
    this.assignmentMethod = AttributeAssignmentMethod.SQL;
  }

  /**
   * Constructor of PhraseAttribute class
   *
   * @param id id of the attribute
   * @param typeName attribute type
   * @param values attribute values
   */
  public RaptatAttribute(String id, String typeName, List<String> values,
      AttributeAssignmentMethod assignmentMethod) {
    this.id = id;
    this.typeName = typeName.toLowerCase();;

    /*
     * Values must be sorted to allow for comparison of the list of values when comparing one
     * PhraseAttribute instance to another.
     *
     * Note that if values is null, we keep the field as assigned in the field list so that
     * getValues() returns an empty list rather than a null value.
     */
    if (values != null) {
      Collections.sort(values);
      this.values = values;
    }

    this.assignmentMethod = assignmentMethod;
  }


  /**
   * Constructor of PhraseAttribute class
   *
   * @param id id of the attribute
   * @param type attribute type
   * @param values attribute values
   */
  public RaptatAttribute(String id, String type, String value,
      AttributeAssignmentMethod assignmentMethod) {
    this.id = id;
    this.typeName = type.toLowerCase();
    /*
     * Values must be sorted to allow for comparison of the list of values when comparing on
     * PhraseAttribute instance to another. If values is null, keep the field as assigned in the
     * field list so that getValues() returns an empty list rather than a null value.
     */
    if (value != null) {
      this.values.add(value);
      Collections.sort(this.values);
    }

    this.assignmentMethod = assignmentMethod;
  }


  @Override
  public int compareTo(RaptatAttribute otherAttribute) {
    CompareToBuilder builtComparison = new CompareToBuilder();
    builtComparison.append(this.typeName, otherAttribute.typeName);
    builtComparison.append(getValues().toArray(new String[1]),
        otherAttribute.getValues().toArray(new String[1]));
    return builtComparison.toComparison();
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    RaptatAttribute other = (RaptatAttribute) obj;

    /*
     * Note that id is not included in equals as this is only used to identify a PhraseAttribute
     * within a given XML file. Also, the equivalence of attributes does not depend on which
     * AnnotatedPhrase instance they are associated with so they can be associated with different
     * AnnotatedPhrase instances and still be equal.
     */
    return new EqualsBuilder().append(this.typeName, other.typeName)
        .append(this.values, other.values).isEquals();
  }


  /** @return the assignmentMethod */
  public AttributeAssignmentMethod getAssignmentMethod() {
    return this.assignmentMethod;
  }


  public String getCanonicalName(String prependString) {
    String tab = "\t";
    StringBuilder sb = new StringBuilder(prependString);

    sb.append("ATT");
    sb.append(tab);
    sb.append(this.typeName);
    sb.append(tab);
    String prependAttribute = sb.toString();

    /*
     * Create new line for each value of attribute
     */
    sb = new StringBuilder("");
    for (String value : this.values) {
      sb = new StringBuilder(prependAttribute);
      sb.append("ATT_VAL");
      sb.append(tab);
      sb.append(value);
      sb.append("\n");
    }
    return sb.toString();
  }


  /**
   * Returns the id of the attribute
   *
   * @return attribute id
   */
  public String getID() {
    return this.id;
  }


  /**
   * Returns the attribute type
   *
   * @return attribute type
   */
  public String getName() {
    return this.typeName;
  }


  /**
   * Returns the attribute values
   *
   * @return attribute values
   */
  public List<String> getValues() {
    return this.values;
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder(53, 29).append(this.typeName).append(this.values)
        .append(this.assignmentMethod).toHashCode();
  }


  /**
   * This method was put in place to enforce storing the typeName field in lower case.
   *
   * @param name the name to set
   */
  public void setTypeName(String name) {
    this.typeName = name.toLowerCase();
  }


  /**
   * This method was put in place to allow the eHOST version of RapTAT to use upper case
   *
   * @param name the name to set
   */
  public void setTypeNameKeepCase(String name) {
    this.typeName = name;
  }


  public void setValues(List<String> values) {
    if (values != null) {
      this.values = values;
    } else {
      values = new ArrayList<>();
    }
  }


  @Override
  public String toString() {
    return this.typeName + ": "
        + GeneralHelper.arrayToDelimString(this.values.toArray(new String[0]), ",");
  }


  /**
   * @param attributeName
   * @param attributeSet
   * @return
   */
  public static RaptatAttribute combineSet(String attributeName,
      Set<RaptatAttribute> attributeSet) {
    HashSet<String> values = new HashSet<>();
    attributeName = attributeName.toLowerCase();
    for (RaptatAttribute curAttribute : attributeSet) {
      if (!curAttribute.typeName.equals(attributeName)) {
        String errorString = "All attributes supplied as a parameter must have the same "
            + "name as the attributeName parameter";
        GeneralHelper.errorWriter(errorString);
        throw new IllegalArgumentException(errorString);
      }
      values.addAll(curAttribute.values);
    }

    RaptatAttribute returnedAttribute =
        new RaptatAttribute("RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique(),
            attributeName, new ArrayList<>(values), AttributeAssignmentMethod.RESOLVED);

    return returnedAttribute;
  }


  public static HashSet<String> getNameSet(List<RaptatAttribute> attributeList) {
    HashSet<String> nameSet = new HashSet<>(attributeList.size());
    for (RaptatAttribute attribute : attributeList) {
      nameSet.add(attribute.typeName);
    }

    return nameSet;
  }


  public static String toStringForDisplay(RaptatAttribute attribute, int minAttributeValues) {
    /*
     * Return a blank set of fields separated by tabs if a null attribute is supplied as a parameter
     */
    if (attribute == null) {
      attribute = new RaptatAttribute("", "", "", null);
    }

    /*
     * Standardize to displaying minAttributeValues values with blanks separated by tabs if there
     * are less than minAttributeValues
     */
    StringBuilder sb = new StringBuilder(attribute.typeName);
    for (int i = 0; i < minAttributeValues; i++) {
      String value = i < attribute.values.size() ? attribute.values.get(i) : "";
      sb.append("\t").append(value);
    }

    return sb.toString();
  }
}
