package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

/**
 * ****************************************************** Provides common interface for iterating
 * through a series of UnlabeledHashTree instances whether stored on disk or in memory
 *
 * @author Glenn Gobbel - Jul 7, 2012 *****************************************************
 */
public interface HashTreeInStreamer<V extends HashTree<V>> extends Iterable<V> {
}
