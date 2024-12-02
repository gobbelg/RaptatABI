/** */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;

/**
 * ****************************************************** Used with a Indexed tree to order objects
 * according to an index
 *
 * @author Glenn Gobbel - May 3, 2012 *****************************************************
 */
public class IndexedObject<T extends Serializable>
    implements Comparable<IndexedObject<T>>, Serializable {
  private static final long serialVersionUID = -1398208828870486703L;
  protected int index;
  protected T indexedObject;


  /**
   * ********************************************
   *
   * @param index
   * @param objectToBeIndexed
   * @author Glenn Gobbel - May 3, 2012 ********************************************
   */
  public IndexedObject(int index, T objectToBeIndexed) {
    this.index = index;
    this.indexedObject = objectToBeIndexed;
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @param otherObject
   * @return
   * @author Glenn Gobbel - May 3, 2012 ***********************************************************
   */
  @Override
  public int compareTo(IndexedObject<T> otherObject) {
    return this.index - otherObject.index;
  }


  public int getIndex() {
    return this.index;
  }


  public T getIndexedObject() {
    return this.indexedObject;
  }


  @Override
  public String toString() {
    return "[" + this.index + ", " + this.indexedObject + "]";
  }
}
