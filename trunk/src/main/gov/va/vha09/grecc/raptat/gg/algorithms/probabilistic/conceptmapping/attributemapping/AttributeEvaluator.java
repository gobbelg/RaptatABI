package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.attributemapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBCalculator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBPriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

public class AttributeEvaluator {
  private HashMap<String, HashMap<String, NBCalculator>> calculators;
  private UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;
  private Map<String, SchemaConcept> conceptToSchemaMap;


  public AttributeEvaluator(ConceptMapSolution cmSolution) {
    ConceptAttributeMapSolution solution = cmSolution.getConceptAttributeMapSolution();
    this.conceptToSchemaMap = cmSolution.getSchemaConcepts();
    HashMap<String, HashMap<String, NBPriorTable>> priorsMap =
        solution.getConceptAttributeToValuePriors();
    HashMap<String, HashMap<String, NBConditionalTable[]>> conditionalsMap =
        solution.getConceptAttributeConditionals();
    this.calculators = getCalculators(priorsMap, conditionalsMap);
  }


  public List<RaptatAttribute> getAttributes(String[] tokenStrings, String concept) {
    List<RaptatAttribute> resultAttributes =
        new ArrayList<>(RaptatConstants.MAX_EXPECTED_ATTRIBUTES_PER_CONCEPT);

    HashMap<String, NBCalculator> attributeToCalculatorMap = this.calculators.get(concept);

    if (attributeToCalculatorMap != null) {
      Set<String> attributeNames = attributeToCalculatorMap.keySet();
      for (String curAttributeName : attributeNames) {
        NBCalculator curCalculator = attributeToCalculatorMap.get(curAttributeName);
        String attributeValue = curCalculator.calcBestConcept(tokenStrings, null)[0];
        String uniqueID = "RapTAT_Attribute_" + this.idGenerator.getUnique();
        RaptatAttribute assignedAttribute = new RaptatAttribute(uniqueID, curAttributeName,
            attributeValue, AttributeAssignmentMethod.PROBABILISTIC);
        resultAttributes.add(assignedAttribute);
      }

    }

    /*
     * attributeToCalculatorMap may not exist if the concept was assigned by the dictionary (e.g.,
     * if concepts in the dictionary have not occurred during training)
     */
    else {
      HashMap<String, List<String>> attributes =
          this.conceptToSchemaMap.get(concept).getAttributeValueMap();

      for (String attributeName : attributes.keySet()) {
        String uniqueID = "RapTAT_Attribute_" + this.idGenerator.getUnique();
        RaptatAttribute assignedAttribute = new RaptatAttribute(uniqueID, attributeName,
            RaptatConstants.NULL_ATTRIBUTE_VALUE, AttributeAssignmentMethod.PROBABILISTIC);
        resultAttributes.add(assignedAttribute);
      }
    }

    return resultAttributes;
  }


  private HashMap<String, HashMap<String, NBCalculator>> getCalculators(
      HashMap<String, HashMap<String, NBPriorTable>> priorsMap,
      HashMap<String, HashMap<String, NBConditionalTable[]>> conditionalsMap) {
    HashMap<String, HashMap<String, NBCalculator>> calculators =
        new HashMap<>(RaptatConstants.MAX_EXPECTED_CONCEPTS_PER_SCHEMA);
    Iterator<String> concepts = priorsMap.keySet().iterator();

    while (concepts.hasNext()) {
      HashMap<String, NBCalculator> attributeToCalculatorMap =
          new HashMap<>(RaptatConstants.MAX_EXPECTED_ATTRIBUTES_PER_CONCEPT);
      String curConcept = concepts.next();
      HashMap<String, NBPriorTable> priors = priorsMap.get(curConcept);
      Iterator<String> attributes = priors.keySet().iterator();
      while (attributes.hasNext()) {
        String curAttribute = attributes.next();
        NBPriorTable curPrior = priors.get(curAttribute);
        NBConditionalTable[] curConditionals = conditionalsMap.get(curConcept).get(curAttribute);
        attributeToCalculatorMap.put(curAttribute,
            new NBCalculator(curPrior, curConditionals, RaptatConstants.FilterType.SMOOTHED));
      }
      calculators.put(curConcept, attributeToCalculatorMap);
    }

    return calculators;
  }
}
