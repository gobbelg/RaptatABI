package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

/**
 * ****************************************************** Provides common interface for appending
 * another HashTree instance onto an existing group of one or more HashTrees (that can be
 * subsequently accessed via a HashTreeInStreamer instance
 *
 * @author Glenn Gobbel - Jul 7, 2012 *****************************************************
 */
public interface HashTreeOutStreamer<V extends HashTree<V>> {
  public boolean append(HashTree<V> theTree);


  public void clear();


  public void writeTreesToDisk();
}
