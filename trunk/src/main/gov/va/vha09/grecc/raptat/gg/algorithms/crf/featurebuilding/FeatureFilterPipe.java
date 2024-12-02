/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.ListIterator;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.PropertyList;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.featurematchers.RaptatFeatureMatcher;

/** @author vhatvhgobbeg */
public class FeatureFilterPipe extends Pipe {
  private static final long serialVersionUID = 9123687589383759417L;


  private RaptatFeatureMatcher featureMatcher;

  public FeatureFilterPipe(RaptatFeatureMatcher featureMatcher) {
    this.featureMatcher = featureMatcher;
  }


  @Override
  public Instance pipe(Instance carrier) {
    TokenSequence ts = (TokenSequence) carrier.getData();

    ListIterator<cc.mallet.types.Token> tokenIterator = ts.listIterator();

    while (tokenIterator.hasNext()) {
      cc.mallet.types.Token curToken = tokenIterator.next();

      PropertyList oldProperties = curToken.getProperties();
      PropertyList newProperties = null;

      cc.mallet.util.PropertyList.Iterator propertyIterator = oldProperties.iterator();

      while (propertyIterator.hasNext()) {
        propertyIterator.next();
        String feature = propertyIterator.getKey();

        if (this.featureMatcher != null && !this.featureMatcher.matches(feature)) {
          newProperties =
              PropertyList.add(feature, propertyIterator.getNumericValue(), newProperties);
        }
      }

      curToken.setFeatures(newProperties);
    }

    return null;
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
}
