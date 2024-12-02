/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

/**
 * Used to determine if an attribute of a phrase should be set based on the context of a sentence.
 * Instances of this type are used by Annotator instances during annotation, and the context is
 * usually set by a call to addSectionMatcher or addSentenceMatcher within TextAnalyzer instance
 * before a call to processDocument or processText.
 *
 * <p>
 * As an example, the call SentenceContextAttributeAnalyzer( "assertionstatus", "allergy",
 * "positive", "negative", false ) has been used for determining the attribute 'assertionstatus'
 * based on whether the sentence has the context of 'allergy' (meaning it is in an allergy section).
 * This constructor indicates that an assertionstatus of 'positive' is the default value of a
 * concept, but this default value is not assigned because the setting for returnDefault, 'false',
 * means that a null value will be return from getPhraseAttribute for a given phrase. The only time
 * a value is assigned is if the sentence context indicates that it is in an 'allergy' section, in
 * which case the assigned 'assertionstatus' would be 'negative.'
 *
 * @author Glenn T. Gobbel Jul 19, 2016
 */
public class SentenceContextAttributeAnalyzer implements ContextAttributeAnalyzer {
  private static final long serialVersionUID = 6988071898651488711L;

  private final String attributeName;

  /*
   * This is the concept that must be assigned to the sentence associated with a phrase for the
   * concept to take on the "contextAssignedAttributeValue
   */
  private final String sentenceContextName;

  /*
   * Attribute value assigned if the context of the sentence associated with the concept is NOT
   * present and the value of returnDefault is true
   */
  private final String defaultAttributeValue;

  /*
   * Attribute value assigned if the context of the sentence in which the concept is found if it has
   * the proper sentenceContextName
   */
  private final String contextAssignedAttributeValue;

  /*
   * Used to determine if an attribute with a default value should be returned if the sentence
   * context does not match sentenceContextName.
   */
  private final boolean returnDefault;


  /**
   * @param attributeName
   * @param sentenceContextName
   * @param defaultAttributeValue
   * @param contextAssignedValue
   * @param returnDefault
   */
  public SentenceContextAttributeAnalyzer(String attributeName, String sentenceContextName,
      String defaultAttributeValue, String contextAssignedValue, boolean returnDefault) {
    this.attributeName = attributeName;
    this.sentenceContextName = sentenceContextName;
    this.defaultAttributeValue = defaultAttributeValue;
    this.contextAssignedAttributeValue = contextAssignedValue;
    this.returnDefault = returnDefault;
  }


  /**
   * This method was designed only for testing of this class within the CSAttributeTagger class
   *
   * @param phrases
   */
  public void assignAttributesTest(List<AnnotatedPhrase> phrases) {
    for (AnnotatedPhrase annotatedPhrase : phrases) {
      RaptatAttribute attribute = getPhraseAttribute(annotatedPhrase.getProcessedTokens());
      if (attribute == null) {
        List<String> attributeValues = new ArrayList<>(1);
        attributeValues.add(ContextType.ASSERTIONSTATUS.getDefaultValue());
        attribute =
            new RaptatAttribute("RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique(),
                this.attributeName, attributeValues, AttributeAssignmentMethod.CONTEXTUAL);
      }

      annotatedPhrase.addAttribute(attribute);
    }
  }


  /*
   * We only use the fields attributeName and sentenceContextName for equality and hashCode as we
   * want to prevent using two GeneralContextAtrributeAnalayzer instances that map the same sentence
   * context to the same attribute during annotation
   */
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

    SentenceContextAttributeAnalyzer other = (SentenceContextAttributeAnalyzer) obj;

    return new EqualsBuilder().append(this.sentenceContextName, other.sentenceContextName)
        .append(this.attributeName, other.attributeName).isEquals();
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.
   * contextanalysis.phrasecontext.ContextAttributeAnalyzer#getAttributeName()
   */
  @Override
  public String getAttributeName() {
    return this.attributeName;
  }


  public String[] getFieldsAsArray() {
    return new String[] {this.attributeName, this.sentenceContextName, this.defaultAttributeValue,
        this.contextAssignedAttributeValue, Boolean.toString(this.returnDefault)};
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.
   * contextanalysis.phrasecontext.ContextAttributeAnalyzer#getPhraseAttribute (java.util.List,
   * src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator)
   */
  @Override
  public RaptatAttribute getPhraseAttribute(List<RaptatToken> phraseTokenSequence) {
    AnnotatedPhrase sentenceOfOrigin = phraseTokenSequence.get(0).getSentenceOfOrigin();
    if (sentenceOfOrigin.getSentenceAssociatedConcepts().contains(this.sentenceContextName)) {
      List<String> attributeValues = new ArrayList<>(1);
      attributeValues.add(this.contextAssignedAttributeValue);
      String id = "RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique();
      return new RaptatAttribute(id, this.attributeName, attributeValues,
          AttributeAssignmentMethod.CONTEXTUAL);
    }

    if (this.returnDefault) {
      List<String> attributeValues = new ArrayList<>(1);
      attributeValues.add(this.defaultAttributeValue);
      String id = "RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique();
      return new RaptatAttribute(id, this.attributeName, attributeValues,
          AttributeAssignmentMethod.CONTEXTUAL);
    }
    return null;
  }


  /*
   * We only use the fields attributeName and sentenceContextName for equality and hashCode as we
   * want to prevent using two GeneralContextAtrributeAnalayzer instances that map the same sentence
   * context to the same attribute during annotation
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.attributeName).append(this.sentenceContextName)
        .toHashCode();
  }
}
