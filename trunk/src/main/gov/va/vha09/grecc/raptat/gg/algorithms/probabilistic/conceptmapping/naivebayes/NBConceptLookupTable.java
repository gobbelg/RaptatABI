package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbTriplet;

/**
 * ********************************************************** NBConceptLookupTable - Used by
 * ConditionalTable for creating essentially a two-dimensional hashtable (hash into a 2nd hash).
 * This hash takes a string which keys into an OccurProbTriplet containing the number of occurrences
 * and likelihoods for the concept, which is provided in the form of a string representing the
 * concept.
 *
 * @author Glenn Gobbel, Jul 18, 2010
 *         <p>
 *         *********************************************************
 */
public class NBConceptLookupTable extends Hashtable<String, OccurProbTriplet>
    implements Serializable {

  // Used to make class serializable
  private static final long serialVersionUID = 8758246083652832933L;


  /**
   * *************************************************************** Construct a Naive Bayes (NB)
   * ConceptLookupTable with an initial capacity
   *
   * @param startCapacity ***************************************************************
   */
  public NBConceptLookupTable(int startCapacity) {
    super(startCapacity);
  }


  @Override
  public String toString() {
    String resString = "{";
    OccurProbTriplet theTriplet;
    String curConcept;

    Enumeration<String> theConcepts = keys();
    while (theConcepts.hasMoreElements()) {
      curConcept = theConcepts.nextElement();
      theTriplet = get(curConcept);
      if (theTriplet != null) {
        resString += curConcept + " => " + theTriplet.toString() + ", ";
      } else {
        resString += curConcept + " =>  " + ", ";
      }
    }
    if (resString.endsWith(", ")) {
      resString = resString.substring(0, resString.length() - 2);
    }
    resString += "}";
    return resString;
  }
}
