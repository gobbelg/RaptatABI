/** */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.ArrayList;
import java.util.List;

public class SortableStringArrayList extends ArrayList<String>
    implements Comparable<SortableStringArrayList>, List<String> {
  private static final long serialVersionUID = 1L;


  public SortableStringArrayList() {}


  public SortableStringArrayList(List<String> asList) {
    for (String item : asList) {
      this.add(item);
    }
  }


  @Override
  public int compareTo(SortableStringArrayList o) {
    for (int i = 0; i < Math.min(size(), o.size()); i++) {
      int c = get(i).compareTo(o.get(i));
      if (c != 0) {
        return c;
      }
    }
    return Integer.compare(size(), o.size());
  }


  /**
   * Jun 11, 2018
   *
   * @param tokenStrings
   * @param tokenIndex
   * @param foundPhraseLength
   * @return
   */
  public static SortableStringArrayList buildList(String[] tokenStrings, int tokenIndex,
      int foundPhraseLength) {
    List<String> resultList = new ArrayList<>(foundPhraseLength);
    for (int i = tokenIndex; i < tokenIndex + foundPhraseLength; i++) {
      resultList.add(tokenStrings[i]);
    }

    return new SortableStringArrayList(resultList);
  }
}
