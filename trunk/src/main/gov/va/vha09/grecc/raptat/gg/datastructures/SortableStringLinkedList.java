package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.LinkedList;
import java.util.List;

public class SortableStringLinkedList extends LinkedList<String>
    implements Comparable<SortableStringLinkedList>, List<String> {
  private static final long serialVersionUID = 1L;


  public SortableStringLinkedList() {}


  public SortableStringLinkedList(List<String> asList) {
    for (String item : asList) {
      this.add(item);
    }
  }


  @Override
  public int compareTo(SortableStringLinkedList o) {
    for (int i = 0; i < Math.min(size(), o.size()); i++) {
      int c = get(i).compareTo(o.get(i));
      if (c != 0) {
        return c;
      }
    }
    return Integer.compare(size(), o.size());
  }
}
