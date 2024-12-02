/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import cc.mallet.types.Instance;

/**
 * Class created to allow for easy use of generic lists as iterators within Mallet for running
 * through a pipe to build features for use with CRF models.
 *
 * @author Glenn T. Gobbel Feb 18, 2016
 */
public class MalletListIterator<T extends Object> implements Iterator<Instance> {

  private final Iterator<T> objectListIterator;


  public MalletListIterator(List<T> objectList) {
    this.objectListIterator = objectList.iterator();
  }


  /*
   * (non-Javadoc)
   *
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return this.objectListIterator.hasNext();
  }


  /*
   * (non-Javadoc)
   *
   * @see java.util.Iterator#next()
   */
  @Override
  public Instance next() {
    T nextObject = null;

    /* Skip nulls */
    while (this.objectListIterator.hasNext()
        && (nextObject = this.objectListIterator.next()) == null) {
    }

    /*
     * We allow the last object in the list to be null and must do a null check in the call
     * hierarchy
     */
    Instance carrier = new Instance(nextObject, null, null, null);
    return carrier;
  }


  /*
   * (non-Javadoc)
   *
   * @see java.util.Iterator#remove()
   */
  @Override
  public void remove() {
    throw new NotImplementedException(
        "TODO - " + this.getClass().getName() + " not yet fully implemented");
  }
}
