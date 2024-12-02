package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ****************************************************** Interface for converting character offsets
 * between two different formats of text. Classes implementing this interface will define the format
 * being converted from and to.
 *
 * @author Glenn Gobbel - Oct 21, 2013 *****************************************************
 */
public interface OffsetConverter {
  public void convertAnnotatedPhraseOffsetList(List<AnnotatedPhrase> allAnnData);


  public RaptatPair<Integer, Integer> convertBeginAndEndOffsets(int begin, int end);


  public List<RaptatPair<Integer, Integer>> convertOffsetList(
      List<RaptatPair<Integer, Integer>> offsetList);


  public RaptatPair<Integer, Integer> convertOffsetPair(RaptatPair<Integer, Integer> theOffsets);
}
