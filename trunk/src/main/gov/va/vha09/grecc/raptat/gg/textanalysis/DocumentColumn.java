/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;

/**
 * Class to store a column of RaptatTokenPhrase instances within a document. By definition, all
 * RaptatTokenPhrase instances within a column overlap all other RaptatTokenPhrase instances within
 * a document. The RaptatTokenPhrase instances are store in order.
 *
 * @author gtony
 *
 */
public class DocumentColumn {

  final private Comparator<RaptatTokenPhrase> phraseSorter = new Comparator<RaptatTokenPhrase>() {
    /**
     * Sort based on line unless on same line, then sort based on overlap with this
     * RaptatTokenPhrase instance
     *
     * @author gobbelgt
     *
     */
    @Override
    public int compare(final RaptatTokenPhrase left, final RaptatTokenPhrase right) {
      return left.get_line_index() - right.get_line_index();
    }
  };

  final public List<RaptatTokenPhrase> columnPhrases;
  final public int indexInDocument;

  public DocumentColumn(List<RaptatTokenPhrase> phrases, int indexInDocument) {
    super();
    Collections.sort(phrases, this.phraseSorter);
    this.columnPhrases = phrases;
    this.indexInDocument = indexInDocument;
  }
}
