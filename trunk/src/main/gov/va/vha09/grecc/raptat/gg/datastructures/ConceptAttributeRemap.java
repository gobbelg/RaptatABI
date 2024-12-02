package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

/**
 * A simple java datastructure to hold the elements that determine a remapping wherein a certain
 * attribute value of a given concept is remapped to another value. This is used when attribute
 * values are combined to reduce the complexity of the annotation schema.
 *
 * @author VHATVHGOBBEG
 */
public class ConceptAttributeRemap {

  private String concept;
  private String attribute;
  private String attributeValue;
  private String remappedValue;


  public ConceptAttributeRemap(String concept, String attribute, String attributeValue,
      String remappedValue) {
    super();
    this.concept = concept;
    this.attribute = attribute;
    this.attributeValue = attributeValue;
    this.remappedValue = remappedValue;
  }


  public String getAttribute() {
    return this.attribute;
  }


  public String getAttributeValue() {
    return this.attributeValue;
  }


  public String getConcept() {
    return this.concept;
  }


  public String getRemappedValue() {
    return this.remappedValue;
  }
}
