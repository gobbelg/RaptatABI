package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords;

import java.util.Enumeration;
import java.util.Iterator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConceptLookupTable;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbTriplet;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.StrPair;

public class BOWConditionalTable extends NBConditionalTable {
  private static final long serialVersionUID = 8660150340930655042L;


  public BOWConditionalTable(int startCapacity) {
    super(startCapacity);
    // TODO Auto-generated constructor stub
  }


  public void calcAllProbs(BOWPriorTable priors) {
    // Run through all the attributes and their
    // associated concepts and calculate the values
    // used for calculating likelihood of a set of attributes
    // occurring with a particular concept
    Enumeration<String> theAttributes = keys();
    while (theAttributes.hasMoreElements()) {
      String curAttribute = theAttributes.nextElement();
      NBConceptLookupTable curConcepts = get(curAttribute);
      Enumeration<String> theConcepts = curConcepts.keys();
      while (theConcepts.hasMoreElements()) {
        String curConcept = theConcepts.nextElement();
        OccurProbTriplet curTokenConceptMapping = curConcepts.get(curConcept);
        int mappingOccurrences = curTokenConceptMapping.occurrences;
        int conceptOccurrences = priors.get(curConcept).occurrences;
        curTokenConceptMapping.unsmoothedProb = Math.log10(mappingOccurrences);
        // This is a bit of a kludge to avoid calculations of
        // -Infinity
        if (conceptOccurrences != mappingOccurrences) {
          curTokenConceptMapping.unsmoothedProb -=
              Math.log10(conceptOccurrences - mappingOccurrences);
        }
        ++mappingOccurrences;
        curTokenConceptMapping.smoothedProb = Math.log10(mappingOccurrences);
        curTokenConceptMapping.smoothedProb -=
            Math.log10(conceptOccurrences + size() + mappingOccurrences);
        curTokenConceptMapping.probabilitiesUpdated = true;
      }
    }
    this.modifiedElements.clear();
  }


  public void calcModProbs(BOWPriorTable priors) {
    Iterator<StrPair> attributeConceptPairs = this.modifiedElements.iterator();
    while (attributeConceptPairs.hasNext()) {
      StrPair curPair = attributeConceptPairs.next();
      OccurProbTriplet curValue = getValue(curPair.left, curPair.right);
      int mappingOccurrences = curValue.occurrences;
      int conceptOccurrences = priors.get(curPair.right).occurrences;
      curValue.unsmoothedProb = Math.log10(mappingOccurrences);
      // This is a bit of a kludge to avoid calculations of
      // -Infinity
      if (conceptOccurrences != mappingOccurrences) {
        curValue.unsmoothedProb -= Math.log10(conceptOccurrences - mappingOccurrences);
      }
      ++mappingOccurrences;
      curValue.smoothedProb = Math.log10(mappingOccurrences);
      curValue.smoothedProb -= Math.log10(conceptOccurrences + size() + mappingOccurrences);
      curValue.probabilitiesUpdated = true;
    }
    this.modifiedElements.clear();
  }
}
