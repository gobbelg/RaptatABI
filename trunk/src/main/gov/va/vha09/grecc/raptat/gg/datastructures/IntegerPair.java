package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

/**
 * ************************************************************ Note: this class has a natural
 * ordering that is inconsistent with equals."
 *
 * @author Glenn Gobbel - Apr 26, 2012 ***********************************************************
 */
public class IntegerPair extends RaptatPair<Integer, Integer> implements Comparable<IntegerPair> {

  /** */
  private static final long serialVersionUID = 6915057131664170094L;


  public IntegerPair(Integer left, Integer right) {
    super(left, right);
  }


  @Override
  public int compareTo(IntegerPair ip) {
    if (this.left < ip.left) {
      return -1;
    } else if (this.left > ip.left) {
      return 1;
    }
    return 0;
  }


  public boolean equals(IntegerPair otherPair) {
    return this.left.equals(this.left) && this.right.equals(this.right);
  }


  @Override
  public String toString() {
    return "(" + this.left.toString() + ", " + this.right.toString() + ")";
  }
}
