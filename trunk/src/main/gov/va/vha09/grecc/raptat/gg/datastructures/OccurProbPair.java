package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;

public class OccurProbPair implements Serializable {
  /** */
  private static final long serialVersionUID = -5328040080339384882L;

  public int occurrences = 0;
  public double probability = 0;


  /**
   * *************************************************************** Construct an OccurProbTriplet
   * from an integer and two doubles
   *
   * @param inInt - the integer input
   * @param firstdbl - the first double input
   * @param secondDbl = the second double input
   *        ***************************************************************
   */
  public OccurProbPair(int occ, double prob) {
    this.occurrences = occ;
    this.probability = prob;
  }


  @Override
  public String toString() {
    return "[" + this.occurrences + ", " + String.format("%.2f", this.probability) + "]";
  }
}
