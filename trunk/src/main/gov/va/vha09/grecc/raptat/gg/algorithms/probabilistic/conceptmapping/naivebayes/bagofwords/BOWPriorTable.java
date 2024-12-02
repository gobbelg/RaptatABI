package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBPriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbGroup;

public class BOWPriorTable extends NBPriorTable {
  private static final long serialVersionUID = -6031676333750202250L;


  public BOWPriorTable(int startCapacity, int probSetSize) {
    super(startCapacity, probSetSize);
  }


  /**
   * ********************************************************** Calculate the likelihood based on
   * the occurrences within the table.
   *
   * @param vocabularySize *********************************************************
   */
  public void calcAllProbs(int vocabularySize) {
    calcModProbs(vocabularySize);
  }


  public void calcModProbs(int vocabSize) {
    Iterator<String> theConcepts = this.modifiedConcepts.iterator();
    while (theConcepts.hasNext()) {
      String curConcept = theConcepts.next();
      OccurProbGroup curGroup = get(curConcept);
      HashMap<String, Integer> tokenMappings = curGroup.mappedTokens;
      int numMappedTokens = tokenMappings.size();
      int conceptOccurrences = curGroup.occurrences;
      int conceptOccurrencesAugmented = conceptOccurrences + vocabSize - 1;
      double unsmoothedValue = 0.0, smoothedValue = 0.0;

      Set<String> theTokens = curGroup.mappedTokens.keySet();

      // Go through all the tokens that map to the concept, and add the
      // log of the difference between the times the concept occurs (or
      // times concept
      // occurs + vocabularySize - 1 = conceptOccurrencesAugmented) to a
      // cumulative sum.
      for (String curToken : theTokens) {
        int totalTokenMappings = curGroup.mappedTokens.get(curToken);
        // This is a bit of a kludge to avoid calculations of
        // -Infinity that can occur when not using smoothing.
        if (conceptOccurrences != totalTokenMappings) {
          unsmoothedValue += Math.log10(conceptOccurrences - totalTokenMappings);
        }
        smoothedValue += Math.log10(conceptOccurrencesAugmented - totalTokenMappings);
      }
      unsmoothedValue -= (numMappedTokens - 1) * Math.log10(conceptOccurrences);
      curGroup.unsmoothedProbs[0] = unsmoothedValue;

      smoothedValue += (vocabSize - numMappedTokens) * Math.log10(conceptOccurrencesAugmented);
      smoothedValue -= vocabSize * Math.log10(conceptOccurrencesAugmented + 1);
      smoothedValue += Math.log10(conceptOccurrences);
      curGroup.smoothedProbs[0] = smoothedValue;
    }
    this.modifiedConcepts.clear();
  }


  /**
   * ************************************************************** Adds one to the integer within
   * the pair hashed by aKey
   *
   * @param aKey - Key indicating value to increment
   *        **************************************************************
   */
  public void increment(String aKey, String[] tokenData) {
    if (containsKey(aKey)) {
      OccurProbGroup curSet = get(aKey);
      curSet.occurrences++;
      curSet.addTokens(tokenData);
      curSet.probabilitiesUpdated = false;
      this.modifiedConcepts.add(aKey);
    } else {
      put(aKey,
          new OccurProbGroup(1, new double[this.maxTokens], new double[this.maxTokens], tokenData));
    }
  }
}
