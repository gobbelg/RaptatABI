/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ************************************************ Removes phrases that do not have the proper
 * context. Note that any item in the requiredContexts field must also be in the forbiddenContexts
 * field to be logically consistent. Also, the concept to be tested ('concept' field) must be an
 * allowedContext for itself. These logical consistencies are enforced by this class.
 *
 * @author vhatvhgobbeg - Jun 11, 2013 ************************************************
 */
public class ContextSifter implements Serializable {
  /** */
  private static final long serialVersionUID = -1628786734385500500L;

  /*
   * Sorts the entries of the contextSensitiveConceptsToCheckerMap first by the name of the sifters
   * and then by the concepts being sifted by that sifter. The names are sorted in lexicographical
   * order so that precedence can be established in the order that the sifting is carried out.
   */
  private static TreeMap<RaptatPair<String, String>, SentenceContextChecker> contextSensitiveConceptsToCheckerMap;

  static {
    ContextSifter.contextSensitiveConceptsToCheckerMap =
        new TreeMap<>(new Comparator<RaptatPair<String, String>>() {
          @Override
          public int compare(RaptatPair<String, String> pair1, RaptatPair<String, String> pair2) {
            int leftCompare = pair1.left.compareTo(pair2.left);
            if (leftCompare != 0) {
              return leftCompare;
            }

            return pair1.right.compareTo(pair2.right);
          }
        });
  }


  public ContextSifter() {}


  /**
   * Nov 20, 2017
   *
   * @param concept
   * @param contextDescriptor
   * @param forbiddenDistances
   * @param requiredDistances
   */
  public void addContextChecker(String sifterName, String conceptToSift, String contextDescriptor,
      RaptatPair<Integer, Integer> forbiddenDistances,
      RaptatPair<Integer, Integer> requiredDistances) {
    SentenceContextChecker checker =
        new SentenceContextChecker(contextDescriptor, requiredDistances, forbiddenDistances);

    ContextSifter.contextSensitiveConceptsToCheckerMap
        .put(new RaptatPair<>(sifterName.toLowerCase(), conceptToSift.toLowerCase()), checker);
  }


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

    ContextSifter other = (ContextSifter) obj;

    return new EqualsBuilder().append(ContextSifter.contextSensitiveConceptsToCheckerMap,
        ContextSifter.contextSensitiveConceptsToCheckerMap).isEquals();
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(ContextSifter.contextSensitiveConceptsToCheckerMap)
        .toHashCode();
  }


  public void siftUsingContext(List<AnnotatedPhrase> thePhrases) {
    Iterator<RaptatPair<String, String>> mapIterator =
        ContextSifter.contextSensitiveConceptsToCheckerMap.keySet().iterator();

    while (mapIterator.hasNext()) {
      RaptatPair<String, String> mapKey = mapIterator.next();
      Iterator<AnnotatedPhrase> phraseIterator = thePhrases.iterator();

      while (phraseIterator.hasNext()) {
        AnnotatedPhrase annotatedPhrase = phraseIterator.next();
        String conceptName = annotatedPhrase.getConceptName();

        if (conceptName.equalsIgnoreCase(mapKey.right)) {
          SentenceContextChecker checker =
              ContextSifter.contextSensitiveConceptsToCheckerMap.get(mapKey);
          if (!checker.isContextCompliant(annotatedPhrase)) {
            phraseIterator.remove();
          }
        }
      }
    }
  }
}
