/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import weka.core.Attribute;

/**
 * @author VHATVHGOBBEG
 *
 */
public class AttributeFrequencyFilter implements AttributeFilter {

  private double minimumFrequency = 0.0;
  private Set<String> acceptableAttributeNames = Collections.emptySet();
  private static final String RaptatPair = null;

  /**
   *
   */
  public AttributeFrequencyFilter(final Double frequency) {
    this.minimumFrequency = frequency == null ? 0.00 : frequency;
  }

  @Override
  public boolean acceptAttribute(final Attribute attribute) {
    return this.acceptableAttributeNames.contains(attribute.name().toLowerCase());
  }

  /**
   * Determine which attribute names occur frequently enough relative to all the names in the map to
   * be acceptable and put into the acceptableAttributeNames set
   *
   * @param attributeNameOccurrences
   */
  public void findAcceptableAttributeNames(final Map<String, Integer> attributeNameOccurrences) {


  }

  /**
   * The initializingObject parameter is assumed to be a Map<String,Integer> instance.
   */
  @Override
  public <T> void initialize(final T initializingObject) throws IllegalArgumentException {

    RaptatPair<Map<String, Integer>, Integer> validatedObject =
        validateInitializingObject(initializingObject);

    Map<String, Integer> attributeNameToOccurrencesMap = validatedObject.left;
    Integer totalPhrases = validatedObject.right;
    this.acceptableAttributeNames = new HashSet<>(attributeNameToOccurrencesMap.size());
    for (Entry<String, Integer> namedOccurrenceEntry : attributeNameToOccurrencesMap.entrySet()) {
      double proportionContaining = (double) namedOccurrenceEntry.getValue() / totalPhrases;
      if (proportionContaining > this.minimumFrequency) {
        this.acceptableAttributeNames.add(namedOccurrenceEntry.getKey().toLowerCase());
      }
    }
  }

  /**
   * @param <T>
   * @param initializingObject
   * @return
   * @throws IllegalArgumentException
   */
  private <T> RaptatPair<Map<String, Integer>, Integer> validateInitializingObject(
      final T initializingObject) throws IllegalArgumentException {
    if (!(initializingObject instanceof RaptatPair<?, ?>)) {
      throw new IllegalArgumentException();
    }

    RaptatPair<?, ?> pairedInitializingObject = (RaptatPair<?, ?>) initializingObject;

    if (!(pairedInitializingObject.left instanceof Map<?, ?>)) {
      throw new IllegalArgumentException();
    }

    Map<?, ?> initializingObjectMap = (Map) pairedInitializingObject.left;
    if (!initializingObjectMap.isEmpty()) {
      Entry<?, ?> firstEntry = initializingObjectMap.entrySet().iterator().next();
      if (!(firstEntry.getKey() instanceof String) || !(firstEntry.getValue() instanceof Integer)) {
        throw new IllegalArgumentException();
      }
    }

    if (!(pairedInitializingObject.right instanceof Integer)) {
      throw new IllegalArgumentException();
    }

    return (RaptatPair<Map<String, Integer>, Integer>) initializingObject;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }

}
