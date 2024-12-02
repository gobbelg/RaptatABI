package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.common;

import java.util.Comparator;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * The Class ComparableSpanHelper.
 */
public class ComparableSpanHelper {

  public static Comparator<RaptatTokenPhrase> overlapIsEqual = new Comparator<RaptatTokenPhrase>() {

    /**
     * Comparison to find out of two phrases overlap and thus are equal, or if not, push the other
     * phrases to end.
     */
    @Override
    public int compare(RaptatTokenPhrase largerPhrase, RaptatTokenPhrase smallerPhrase) {
      if (smallerPhrase.getPhraseText().length() > largerPhrase.getPhraseText().length()) {
        RaptatTokenPhrase tmp = largerPhrase;
        largerPhrase = smallerPhrase;
        smallerPhrase = tmp;
      }
      int smallerPhrase_start = smallerPhrase.getStartOffsetAsInt();
      int smallerPhrase_end = smallerPhrase.getEndOffsetAsInt();
      int largerPhrase_start = largerPhrase.getStartOffsetAsInt();
      /**
       * substract length of smaller from larger in order to find smaller start between larger
       * endpoints
       */
      int largerPhrase_end = largerPhrase.getEndOffsetAsInt() - smallerPhrase_start;

      /**
       * smaller start is below the larger end
       */
      if (largerPhrase_end > smallerPhrase_start) {
        /**
         * smaller start is at or above larger start
         */
        if (smallerPhrase_start >= largerPhrase_start) {
          return 0;
        }
      }
      /**
       * Not part of an overlap, push to end.
       */
      return -1;

    }

  };



  public boolean doesLhsPhraseCoverRhsPhrase(final RaptatTokenPhrase lhs,
      final RaptatTokenPhrase rhs) {
    boolean rv = false;
    int rhs_end_offset_as_int = rhs.getEndOffsetAsInt();
    int lhs_end_offset_as_int = lhs.getEndOffsetAsInt();
    if (rhs_end_offset_as_int > lhs_end_offset_as_int) {
      int rhs_start_offset_as_int = rhs.getStartOffsetAsInt();
      int lhs_start_offset_as_int = lhs.getStartOffsetAsInt();
      if (rhs_start_offset_as_int < lhs_start_offset_as_int) {
        rv = true;
      }
    }
    return rv;
  }


  /**
   * Compare end offsets.
   *
   * @param lhs the lhs
   * @param rhs the rhs
   * @return the int
   */
  public static int compare_end_offsets(final RaptatTokenPhrase lhs, final RaptatTokenPhrase rhs) {
    return lhs.getEndOffsetAsInt() - rhs.getEndOffsetAsInt();
  }


  /**
   * Compare lines.
   *
   * @param lhs the lhs
   * @param rhs the rhs
   * @return the int
   */
  public static int compare_lines(final RaptatTokenPhrase lhs, final RaptatTokenPhrase rhs) {
    return lhs.get_line_index() - rhs.get_line_index();
  }


  /**
   * Compare start offsets.
   *
   * @param lhs the lhs
   * @param rhs the rhs
   * @return the int
   */
  public static int compare_start_offsets(final IComparableSpan lhs, final IComparableSpan rhs) {
    return lhs.getStartOffsetAsInt() - rhs.getStartOffsetAsInt();
  }


  public static int compare_start_offsets(final RaptatToken lhs, final RaptatToken rhs) {
    return lhs.get_start_offset_as_int() - rhs.get_start_offset_as_int();
  }
}
