/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import third_party.org.chokkan.crfsuite.ItemSequence;

/** @author Glenn T. Gobbel Mar 21, 2016 */
public interface CRFTagger {
  /*
   * @param features - Generally consists of a list representing sentences, with each Item instance
   * in each ItemSequence object representing a token.
   *
   * @return - A list of pairs where the 2 numbers stored in each pair correspond to a sentence and
   * token that is positive for the attribute of interest. So if the 10th sentence in the features
   * sentences had a positive token at position k, there would be an entry (n,k) in the returned
   * list.
   */
  public List<RaptatPair<Integer, Integer>> getLabels(List<ItemSequence> features);
}
