package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

/**
 * ********************************************************** AttributeDBMismatchException - Allows
 * us to specify a particular type of exception when the number of attributes fed into a
 * NaiveBayesCalculator does not match the number expected based on the training data.
 *
 * @author Glenn Gobbel, Jul 12, 2010
 *         <p>
 *         *********************************************************
 */
public class AttributeDBMismatchException extends Exception {
  private static final long serialVersionUID = 3344373765907360370L;


  public AttributeDBMismatchException(String msg) {
    super(msg);
  }
}
