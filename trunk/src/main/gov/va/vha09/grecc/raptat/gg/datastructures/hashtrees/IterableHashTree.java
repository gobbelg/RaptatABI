package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

/**
 * ****************************************************** Provides a common interface for
 * implementations of HashTress that can iterate over the tree and iteratively return lists of
 * strings within the tree.
 *
 * @author Glenn Gobbel - Jul 3, 2012 *****************************************************
 */
public interface IterableHashTree<V extends HashTree<V>>
    extends Iterable<RaptatPair<List<String>, V>> {
}
