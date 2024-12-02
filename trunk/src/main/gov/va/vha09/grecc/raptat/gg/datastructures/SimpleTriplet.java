package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SimpleTriplet<L> {
  public final L left;

  public final L right;
  public final L middle;

  /**
   * Constructor. Accepts two objects and constructs a pair. Neither can be null.
   *
   * @param left The left object
   * @param right The right object
   */
  public SimpleTriplet(final L left, final L middle, final L right) {
    String errString = "Instance members of the triplet class must be non-null";
    Validate.notNull(left, errString);
    this.left = left;
    Validate.notNull(middle, errString);
    this.middle = middle;
    Validate.notNull(right, errString);
    this.right = right;
  }


  /**
   * Returns true iff the two triplets are equal.
   *
   * @param other some other pair
   * @return true if equal
   */
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof SimpleTriplet)) {
      return false;
    }
    SimpleTriplet<?> that = (SimpleTriplet<?>) other;
    return new EqualsBuilder().append(this.left, that.left).append(this.right, that.right)
        .append(this.middle, that.middle).isEquals();
  }


  /**
   * Returns a hash code for this triplet
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.left).append(this.middle).append(this.right)
        .toHashCode();
  }


  /** Returns the string form of this triplet: "(left,middle, right)" */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("(");
    sb.append(this.left.toString());
    sb.append(",");
    sb.append(this.middle.toString());
    sb.append(",");
    sb.append(this.right.toString());
    sb.append(")");
    return sb.toString();
  }


  public static void main(String[] args) {
    EqualsBuilder myTester = new EqualsBuilder();
    String a = "alpha";
    String b = "beta";
    boolean test = myTester.append(a, b).isEquals();
    System.out.println(test);
  }
}
