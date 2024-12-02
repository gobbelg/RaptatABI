/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import cc.mallet.util.PropertyList;

/** @author Glenn Gobbel */
public class RaptatPropertyListIterator extends PropertyList.Iterator {
  /** */
  private static final long serialVersionUID = -8793123922174471684L;

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
