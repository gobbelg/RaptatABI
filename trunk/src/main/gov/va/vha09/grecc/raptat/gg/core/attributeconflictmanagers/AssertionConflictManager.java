/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

/** @author Glenn T. Gobbel Jul 22, 2016 */
public class AssertionConflictManager extends AttributeConflictManager implements Serializable {

  /** */
  private static final long serialVersionUID = 7335646491611089562L;


  /**
   * @param attributeName
   */
  public AssertionConflictManager() {
    super("assertionstatus");
  }


  /*
   * For an AssertionConflictManager instance, there are 3 potential methods of negating a phrase,
   * probabilistic (attribute it is most commonly associated with), contextual (sentence context
   * determines, such as allergy section, and cue-scope). We allow for double-negations being
   * actually positive (e.g. a negation in an allergy section is not an allergy). Because of this,
   * we can count how many are present out of probabilistic, contextual, and cue-scope. The
   * assertion value is negation if an only if either only 1 of the 3 conditions for inducing
   * negation is true or all 3 are true.
   *
   * Note that the annotatedPhrase parameter is not used but is required by the interface.
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.
   * AttributeConflictManager#manageConflicts(java.util.Set)
   */
  @Override
  public RaptatAttribute manageConflicts(Set<RaptatAttribute> attributeSet,
      AnnotatedPhrase annotatedPhrase) {

    String assertionValue = getAssertionValue(attributeSet);

    List<String> valueList = new ArrayList<>();
    valueList.add(assertionValue);

    String IDString = "RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique();
    AttributeAssignmentMethod assignmentMethod = AttributeAssignmentMethod.RESOLVED;

    return new RaptatAttribute(IDString, this.attributeName, valueList, assignmentMethod);
  }


  /**
   * @param attributeSet
   * @return
   */
  protected HashMap<AttributeAssignmentMethod, RaptatAttribute> getMappedAssignmentMethods(
      Set<RaptatAttribute> attributeSet) throws IllegalArgumentException {
    HashMap<AttributeAssignmentMethod, RaptatAttribute> resultMap = new HashMap<>();
    for (RaptatAttribute attribute : attributeSet) {
      String curAttributeName = attribute.getName();
      if (!curAttributeName.equals(this.attributeName)) {
        String errorMsg = "All attributes in the attributeSet when calling "
            + "manageConflics must match the attribute name of the AssertionConflictManager instance";
        throw new IllegalArgumentException(errorMsg);
      }

      resultMap.put(attribute.getAssignmentMethod(), attribute);
    }

    return resultMap;
  }


  String getAssertionValue(Set<RaptatAttribute> attributeSet) {
    HashMap<AttributeAssignmentMethod, RaptatAttribute> assignmentMethodMap = new HashMap<>();
    try {
      assignmentMethodMap = getMappedAssignmentMethods(attributeSet);
    } catch (IllegalArgumentException e) {
      System.err.println(
          "Illegal call to AssertionConflictManager manageConflicts() method\n" + e.getMessage());
      System.exit(-1);
    }

    RaptatAttribute attribute;
    int negationCount = 0;

    /*
     * Note that "commonly assigned value" for ContextType.ASSERTIONSTATUS is "negative"
     */
    if ((attribute = assignmentMethodMap.get(AttributeAssignmentMethod.PROBABILISTIC)) != null) {
      if (attribute.getValues().get(0)
          .equalsIgnoreCase(ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue())) {
        negationCount++;
      }
    }

    if ((attribute = assignmentMethodMap.get(AttributeAssignmentMethod.CUE_SCOPE_CRF)) != null) {
      if (attribute.getValues().get(0)
          .equalsIgnoreCase(ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue())) {
        negationCount++;
      }
    }

    if ((attribute = assignmentMethodMap.get(AttributeAssignmentMethod.CONTEXTUAL)) != null) {
      if (attribute.getValues().get(0)
          .equalsIgnoreCase(ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue())) {
        negationCount++;
      }
    }

    String assertionValue = negationCount == 1 || negationCount == 3
        ? ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue()
        : ContextType.ASSERTIONSTATUS.getDefaultValue();

    return assertionValue;
  }
}
