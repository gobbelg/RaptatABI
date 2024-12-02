package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.attributemapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBPriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;

public class ConceptAttributeMapSolution implements Serializable {

  private static final long serialVersionUID = 1986365528324498600L;

  /*
   * for a given concept and attribute, stores the number of times (occurrences) a phrase with a
   * particular token at position 'k' in the phrase is associated with a given attribute value
   */
  private HashMap<String, HashMap<String, NBConditionalTable[]>> conceptAttributeConditionals;

  /*
   * Provides the occurrences of a given attribute value for a given concept-attribute pair
   */
  private HashMap<String, HashMap<String, NBPriorTable>> conceptAttributeToValuePriors;

  /*
   * Even though our mappings are based on concept-attribute pairs, we keep track only of modified
   * concepts rather than concepts and attributes because, if a concept is modified, then the
   * attributes are also modified because all attributes must have a value and exist for any
   * annotation. A value of DEFAULT is assigned to any annotated concept with a missing attribute.
   */
  private HashSet<String> modifiedConcepts;

  private int maxTokens;


  public ConceptAttributeMapSolution(int numOfTokens) {
    this.conceptAttributeConditionals =
        new HashMap<>(RaptatConstants.MAX_EXPECTED_CONCEPTS_PER_SCHEMA);
    this.conceptAttributeToValuePriors =
        new HashMap<>(RaptatConstants.MAX_EXPECTED_CONCEPTS_PER_SCHEMA);
    this.modifiedConcepts = new HashSet<>(RaptatConstants.MAX_EXPECTED_CONCEPTS_PER_SCHEMA);

    this.maxTokens = numOfTokens;
  }


  public HashMap<String, HashMap<String, NBConditionalTable[]>> getConceptAttributeConditionals() {
    return this.conceptAttributeConditionals;
  }


  public HashMap<String, HashMap<String, NBPriorTable>> getConceptAttributeToValuePriors() {
    return this.conceptAttributeToValuePriors;
  }


  public void initiateLikelihoodCalcs() {
    Iterator<String> conceptIterator = this.conceptAttributeConditionals.keySet().iterator();
    while (conceptIterator.hasNext()) {
      String concept = conceptIterator.next();
      HashMap<String, NBConditionalTable[]> mappedAttributes =
          this.conceptAttributeConditionals.get(concept);
      Set<String> attributeKeys = mappedAttributes.keySet();
      Iterator<String> attributeIterator = attributeKeys.iterator();
      while (attributeIterator.hasNext()) {
        String attribute = attributeIterator.next();
        NBConditionalTable[] conditionalArray = mappedAttributes.get(attribute);
        int[] vocabSizes = new int[conditionalArray.length];

        for (int i = 0; i < conditionalArray.length; i++) {
          vocabSizes[i] = conditionalArray[i].size();
          conditionalArray[i].calcAllProbs();
        }
        this.conceptAttributeToValuePriors.get(concept).get(attribute)
            .calcAllLikelihoods(vocabSizes);
      }
    }
    this.modifiedConcepts.clear();
  }


  public void updateModifiedLikelihoods() {
    int[] vocabSize = new int[this.maxTokens];

    for (String curConcept : this.modifiedConcepts) {
      Set<String> attributeKeys = this.conceptAttributeConditionals.get(curConcept).keySet();
      Iterator<String> attributeIterator = attributeKeys.iterator();

      while (attributeIterator.hasNext()) {
        boolean vocabSizeChanged = false;
        String curAttribute = attributeIterator.next();
        NBPriorTable priors = this.conceptAttributeToValuePriors.get(curConcept).get(curAttribute);
        NBConditionalTable[] conditionals =
            this.conceptAttributeConditionals.get(curConcept).get(curAttribute);
        for (int i = 0; i < conditionals.length; i++) {
          /*
           * If we haven't detected a change in vocabulary size, check the current conditional to
           * see if it's changed; otherwise, skip this
           */
          if (!vocabSizeChanged && conditionals[i].vocabularySizeChanged()) {
            vocabSizeChanged = true;
          }
          vocabSize[i] = conditionals[i].size();
          conditionals[i].calcModifiedLikelihoods();
        }
        if (vocabSizeChanged) {
          /*
           * Note: No need to actually calculate unsmoothed likelihood on unmodified concepts, but
           * there does not appear to be an efficient way to skip these calculations, so we just
           * redo them
           */
          priors.calcAllLikelihoods(vocabSize);
        } else {
          priors.calcModifiedLikelihoods(vocabSize);
        }
      }
    }
    this.modifiedConcepts.clear();
  }


  public void updateTraining(String theConcept, List<RaptatAttribute> attributes,
      String[] trainData) {
    this.modifiedConcepts.add(theConcept);
    HashMap<String, NBConditionalTable[]> attributeConditionalMap =
        this.conceptAttributeConditionals.get(theConcept);
    HashMap<String, NBPriorTable> attributePriorMap =
        this.conceptAttributeToValuePriors.get(theConcept);

    if (attributeConditionalMap == null) {
      createMapsAndUpdate(theConcept, attributes, trainData);
    } else {
      updateConditionalsAndPriors(attributeConditionalMap, attributePriorMap, attributes,
          trainData);
    }
  }


  private void createMapsAndUpdate(String theConcept, List<RaptatAttribute> attributes,
      String[] trainData) {
    /* First create the maps from the concept to the other maps */
    HashMap<String, NBConditionalTable[]> attributeConditionalMap =
        new HashMap<>(RaptatConstants.MAX_EXPECTED_ATTRIBUTES_PER_CONCEPT);
    HashMap<String, NBPriorTable> attributePriorMap =
        new HashMap<>(RaptatConstants.MAX_EXPECTED_ATTRIBUTES_PER_CONCEPT);

    this.conceptAttributeConditionals.put(theConcept, attributeConditionalMap);
    this.conceptAttributeToValuePriors.put(theConcept, attributePriorMap);

    updateConditionalsAndPriors(attributeConditionalMap, attributePriorMap, attributes, trainData);
  }


  private void updateConditionalsAndPriors(
      HashMap<String, NBConditionalTable[]> attributeConditionalMap,
      HashMap<String, NBPriorTable> attributePriorMap, List<RaptatAttribute> attributes,
      String[] trainData) {
    for (RaptatAttribute curAttribute : attributes) {
      String attributeName = curAttribute.getName();
      NBPriorTable valuePriorTable;
      if ((valuePriorTable = attributePriorMap.get(attributeName)) == null) {
        valuePriorTable =
            new NBPriorTable(RaptatConstants.MAX_EXPECTED_VALUES_PER_ATTRIBUTE, this.maxTokens);
        attributePriorMap.put(attributeName, valuePriorTable);
      }

      NBConditionalTable[] conditionalArray;
      if ((conditionalArray = attributeConditionalMap.get(attributeName)) == null) {
        conditionalArray = new NBConditionalTable[this.maxTokens];
        attributeConditionalMap.put(attributeName, conditionalArray);
        for (int i = 0; i < this.maxTokens; i++) {
          conditionalArray[i] =
              new NBConditionalTable(RaptatConstants.MAX_EXPECTED_VALUES_PER_ATTRIBUTE);
        }
      }

      for (String curValue : curAttribute.getValues()) {
        valuePriorTable.increment(curValue);
        for (int i = 0; i < trainData.length; i++) {
          conditionalArray[i].increment(trainData[i], curValue);
        }
      }
    }
  }
}
