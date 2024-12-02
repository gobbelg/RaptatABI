/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import cc.mallet.util.PropertyList;

/** @author Glenn Gobbel */
public class RaptatPropertyList extends PropertyList {

  public class RaptatPropertyListIterator extends PropertyList.Iterator {
    /** */
    private static final long serialVersionUID = 8185214942669248715L;

    PropertyList raptatIteratorPropertyList;


    /**
     * @param propertyList
     * @param pl
     */
    public RaptatPropertyListIterator(PropertyList propertyList, PropertyList pl) {
      propertyList.super(pl);
      this.raptatIteratorPropertyList = pl;
    }


    @Override
    public RaptatPropertyListIterator reset() {
      return new RaptatPropertyListIterator(this.raptatIteratorPropertyList,
          this.raptatIteratorPropertyList);
    }
  }

  /** */
  private static final long serialVersionUID = 5409214716630255640L;


  /**
   * @param key
   * @param rest
   */
  public RaptatPropertyList(String key, PropertyList rest) {
    super(key, rest);
  }
}
