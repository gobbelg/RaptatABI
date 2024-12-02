/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.results;

import java.util.LinkedList;

/**
 * ********************************************************** BayesResult - Holds record-by-record
 * results from a tested Bayes network. Note that this is NOT synchronized and, if used with
 * mulltiple thread accesses, needs to be synchronized at creation time with List list =
 * Collections.synchronizedList(new BayesResult(...));
 *
 * @author Glenn Gobbel, Jul 22, 2010
 *         <p>
 *         *********************************************************
 */
public class NBResult extends LinkedList<String[]> {
  private static final long serialVersionUID = -6427807413971471919L;


  public NBResult() {
    super();
  }
}
