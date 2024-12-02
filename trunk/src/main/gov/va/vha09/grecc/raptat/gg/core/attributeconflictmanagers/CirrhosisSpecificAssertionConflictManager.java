package src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

public class CirrhosisSpecificAssertionConflictManager extends AssertionConflictManager {

  private static final long serialVersionUID = -675919418670903230L;

  private static final Set<String> nonprobabilisticConcepts = new HashSet<>(Arrays.asList(
      new String[] {"07_ascites", "01_liver", "05_massfocalarea", "20_cancerdetermination"}));


  public CirrhosisSpecificAssertionConflictManager() {
    super();
  }


  /*
   * This is a modification of the AssertionConflictManager class method that excludes certain
   * phrases that are mapped to particular concepts from probabilistic attribute mapping
   * consideration.
   */
  @Override
  public RaptatAttribute manageConflicts(Set<RaptatAttribute> attributeSet,
      AnnotatedPhrase annotatedPhrase) {
    String IDString = "RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique();
    AttributeAssignmentMethod assignmentMethod = AttributeAssignmentMethod.RESOLVED;

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
    /*
     * We exclude concepts with ascites in the name from consideration for probabilistic mapping.
     * These are commonly negated by external negation terms, but this makes them also
     * probabilistically mapped to negation.
     */
    if (!CirrhosisSpecificAssertionConflictManager.nonprobabilisticConcepts
        .contains(annotatedPhrase.getConceptName().toLowerCase())
        && (attribute = assignmentMethodMap.get(AttributeAssignmentMethod.PROBABILISTIC)) != null) {
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

    List<String> valueList = new ArrayList<>();
    String assertionValue = negationCount == 1 || negationCount == 3
        ? ContextType.ASSERTIONSTATUS.getCommonlyAssignedValue()
        : ContextType.ASSERTIONSTATUS.getDefaultValue();
    valueList.add(assertionValue);
    return new RaptatAttribute(IDString, this.attributeName, valueList, assignmentMethod);
  }
}
