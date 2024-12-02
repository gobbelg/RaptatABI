package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.HashSet;
import java.util.Iterator;

/**
 * ********************************************************* Holds unique pairs of objects in a set.
 * Uses a hashtable with the value of each key (which is an attribute-concept pair) being a dummy
 * value of "true". Used to store items so that every pair occurs only once in the list. Note that
 * this could be done with a HashSet, but the Hashtable is synchronized unlike HashSet.
 *
 * @author Glenn Gobbel, Jul 6, 2010 *********************************************************
 */
public class StrPairSet extends HashSet<StrPair> {
  // Added to comply with serializable
  private static final long serialVersionUID = -1210947293377452184L;


  /**
   * *************************************************************** Construct a new StrPairSet with
   * size startCapacity
   *
   * @param startCapacity - Starting size of hashtable corresponding to the PairSet
   *        ***************************************************************
   */
  public StrPairSet(int startCapacity) {
    super(startCapacity);
  }


  /**
   * ************************************************************** Convert two strings to a pair
   * and store them in the set
   *
   * @param firstString
   * @param secondString **************************************************************
   */
  public void addPair(String first, String second) {
    add(new StrPair(first, second));
  }


  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }


  @Override
  public int hashCode() {
    return super.hashCode();
  }


  public void print() {
    Iterator<StrPair> firstDimKeys = iterator();
    System.out.println("\nKeys StrPairSet");
    while (firstDimKeys.hasNext()) {
      System.out.println(firstDimKeys.next());
    }
  }
}
