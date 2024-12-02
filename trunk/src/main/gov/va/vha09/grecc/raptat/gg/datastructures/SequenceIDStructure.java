package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

/**
 * ****************************************************** Provides common interface for the
 * structures needed to store information for carrying out sequence identification. It contains no
 * methods.
 *
 * @author Glenn Gobbel - Sep 7, 2012 *****************************************************
 */
public interface SequenceIDStructure {

  boolean canEqual(Object other);


  void compareTo(Object other);


  /** */
  void print();


  void showComparison(Object other);
}
