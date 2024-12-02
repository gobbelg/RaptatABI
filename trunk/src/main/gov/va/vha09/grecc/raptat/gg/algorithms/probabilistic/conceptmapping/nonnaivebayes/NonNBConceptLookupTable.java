/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * ********************************************************** NonNaiveBayesConceptLookupTable -
 *
 * @author Glenn Gobbel, Jan 17, 2011
 *         <p>
 *         *********************************************************
 */
public class NonNBConceptLookupTable extends Hashtable<String, NonNBOccurProbSet>
    implements Serializable {
  private static final long serialVersionUID = 8911249169085656372L;


  /**
   * *************************************************************** Construct a ConceptLookupTable
   * with an initial capacity
   *
   * @param startCapacity ***************************************************************
   */
  public NonNBConceptLookupTable(int startCapacity) {
    super(startCapacity);
  }


  @Override
  public String toString() {
    String resString = "";
    NonNBOccurProbSet theSet;
    String curConcept;

    Enumeration<String> theConcepts = keys();
    while (theConcepts.hasMoreElements()) {
      curConcept = theConcepts.nextElement();
      theSet = get(curConcept);
      resString += curConcept + System.getProperty("line.separator") + "\t"
          + theSet.getRawOccurrences() + "\t";
      resString += theSet.getSmoothOccurrences() + "\t";
      resString += theSet.getSmoothProbability() + System.getProperty("line.separator");
    }
    return resString;
  }
}
