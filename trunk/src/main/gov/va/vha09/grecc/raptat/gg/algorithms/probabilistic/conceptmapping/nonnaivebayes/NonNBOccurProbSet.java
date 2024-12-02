/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

/**
 * ********************************************************** Holds the occurrences of a concept
 * with a given word, the number of occurrences when smoothing is used, and the likelihood
 * associated with these smoothed values. Note that no likelihood are supplied with the raw
 * occurrence values because they are not needed for calculation of likelihood.
 *
 * @author Glenn Gobbel, Jan 15, 2011
 *         <p>
 *         *********************************************************
 */
public class NonNBOccurProbSet {
  private int rawOccurrences = 0;
  private int smoothOccurrences = 0;
  private double smoothProbability = 0.0;


  public NonNBOccurProbSet() {}


  public NonNBOccurProbSet(int rawValue, int smoothValue) {
    assert rawValue > -1 && smoothValue > -1;
    this.rawOccurrences = rawValue;
    this.smoothOccurrences = smoothValue;
  }


  /**
   * **********************************************************
   *
   * @return the rawOccurrences as int **********************************************************
   */
  public int getRawOccurrences() {
    return this.rawOccurrences;
  }


  /**
   * **********************************************************
   *
   * @return the smoothOccurrences as int **********************************************************
   */
  public int getSmoothOccurrences() {
    return this.smoothOccurrences;
  }


  /**
   * **********************************************************
   *
   * @return the smoothProbality as double
   *         **********************************************************
   */
  public double getSmoothProbability() {
    return this.smoothProbability;
  }


  public void incrementAll(boolean newPhrase) {
    ++this.rawOccurrences;
    if (newPhrase) {
      this.smoothOccurrences += 2;
    } else {
      ++this.smoothOccurrences;
    }
    this.smoothProbability = Math.log10(this.smoothOccurrences);
  }


  /**
   * ********************************************************** Increment rawOccurrences and
   * smoothOccurrences but do not update the calculated value of smoothProbability
   * **********************************************************
   */
  public void incrementOccurrences(boolean newPhrase) {
    ++this.rawOccurrences;
    if (newPhrase) {
      this.smoothOccurrences += 2;
    } else {
      ++this.smoothOccurrences;
    }
    this.smoothProbability = Math.log10(this.smoothOccurrences);
  }


  public void updateProbability() {
    this.smoothProbability = Math.log10(this.smoothOccurrences);
  }
}
