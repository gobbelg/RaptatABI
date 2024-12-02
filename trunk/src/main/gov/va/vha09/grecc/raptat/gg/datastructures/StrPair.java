package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

/**
 * ********************************************************** StringPair - Object to hold a pair of
 * strings
 *
 * @author Glenn Gobbel, Jul 8, 2010
 *         <p>
 *         *********************************************************
 */
public class StrPair {
  private int pairHashCode;
  public final String left;
  public final String right;


  /**
   * *************************************************************** Construct a StringPair from two
   * String objects
   *
   * @param first
   * @param second ***************************************************************
   */
  public StrPair(final String first, final String second) {
    this.left = first;
    this.right = second;
    calcHash();
  }


  /**
   * ************************************************************** Determines whether an object is
   * equal to this one. If the objects point to the same location, are of the same class, and
   * contain the same strings, it returns true.
   *
   * @param obj - Object to be compared to this
   * @return A boolean indicating whether the object is equal to this object
   *         **************************************************************
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    StrPair other = (StrPair) obj;

    if (this.left == null) {
      if (other.left != null) {
        return false;
      }
    } else if (!this.left.equals(other.left)) {
      return false;
    }

    if (this.right == null) {
      if (other.right != null) {
        return false;
      }
    } else if (!this.right.equals(other.right)) {
      return false;
    }

    return true;
  }


  /**
   * ************************************************************** Return the hashcode for the
   * object
   *
   * @return hashCode for object **************************************************************
   */
  @Override
  public int hashCode() {
    return this.pairHashCode;
  }


  @Override
  public String toString() {
    return this.left + " + " + this.right;
  }


  /**
   * ************************************************************** Calculates the hash of the pair
   * during construction of the object to speed later evaluation of the hashcode for the object
   * **************************************************************
   */
  private void calcHash() {
    final int prime = 31;
    this.pairHashCode = 1;
    this.pairHashCode = prime * this.pairHashCode + (this.left == null ? 0 : this.left.hashCode());
    this.pairHashCode =
        prime * this.pairHashCode + (this.right == null ? 0 : this.right.hashCode());
  }
}
