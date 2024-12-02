package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.PropertyList;
import cc.mallet.util.PropertyList.Iterator;

public class ConjunctionCollapser extends Pipe implements Serializable {
  /** */
  private static final long serialVersionUID = 8768412183964474368L;

  private static final int CURRENT_SERIAL_VERSION = 0;
  private static final String UPSTREAM_TAG = "@UPSTREAM";
  private static final String DOWNSTREAM_TAG = "@DOWNSTREAM";
  private static final int upTagLength = ConjunctionCollapser.UPSTREAM_TAG.length();
  private static final int downTagLength = ConjunctionCollapser.DOWNSTREAM_TAG.length();
  private static Pattern conjunctionPatternA = Pattern.compile(".+_&_.+@-*[0-9]+.*");
  private static Pattern conjunctionPatternB = Pattern.compile(".+@-*[0-9]+_&_.+");
  private static Pattern singletonPattern = Pattern.compile(".+@-*[0-9]+");
  private static Logger logger = Logger.getLogger(ConjunctionCollapser.class);
  private int version;
  private boolean replaceSingletons = false;
  private boolean sortFeatures = false;


  public ConjunctionCollapser() {
    super();
    ConjunctionCollapser.logger.setLevel(Level.INFO);
  }


  public ConjunctionCollapser(boolean replaceSingletons) {
    this();
    this.replaceSingletons = replaceSingletons;
  }


  public ConjunctionCollapser(boolean replaceSingletons, boolean sortFeatures) {
    this(replaceSingletons);
    this.sortFeatures = sortFeatures;
  }


  @Override
  public Instance pipe(Instance carrier) {
    ConjunctionCollapser.logger.debug("Collapsing conjunctions");
    TokenSequence ts = (TokenSequence) carrier.getData();
    ConjunctionCollapser.logger.debug("Token data obtained for collapsing");
    PropertyList revisedProperties;

    for (cc.mallet.types.Token curToken : ts) {
      revisedProperties = null;
      ConjunctionCollapser.logger.debug("Current token:" + curToken);
      Iterator featureIterator = curToken.getFeatures().iterator();
      while (featureIterator.hasNext()) {
        featureIterator.next();
        String featureKey = featureIterator.getKey();
        ConjunctionCollapser.logger.debug("Current feature:" + featureKey);
        if (ConjunctionCollapser.conjunctionPatternA.matcher(featureKey).matches()
            || ConjunctionCollapser.conjunctionPatternB.matcher(featureKey).matches()) {
          String atPattern = "@-[0-9]+";
          featureKey = featureKey.replaceAll(atPattern, ConjunctionCollapser.UPSTREAM_TAG);
          atPattern = "@[0-9]+";
          featureKey = featureKey.replaceAll(atPattern, ConjunctionCollapser.DOWNSTREAM_TAG);
          if (this.sortFeatures) {
            featureKey = doFeatureSort(featureKey);
          }
        } else if (this.replaceSingletons
            && ConjunctionCollapser.singletonPattern.matcher(featureKey).matches()) {
          String atPattern = "@-[0-9]+";
          featureKey = featureKey.replaceAll(atPattern, ConjunctionCollapser.UPSTREAM_TAG);
          atPattern = "@[0-9]+";
          featureKey = featureKey.replaceAll(atPattern, ConjunctionCollapser.DOWNSTREAM_TAG);
        }

        if (featureIterator.isNumeric()) {
          revisedProperties =
              PropertyList.add(featureKey, featureIterator.getNumericValue(), revisedProperties);
        } else {
          revisedProperties =
              PropertyList.add(featureKey, featureIterator.getObjectValue(), revisedProperties);
        }
      }
      curToken.setFeatures(revisedProperties);
      ConjunctionCollapser.logger.debug("Revised token:" + curToken);
    }
    return carrier;
  }


  /**
   * Takes all features that are marked as DOWNSTREAM_TAG or DOWNSTREAM_TAG and combines and sorts
   * them so whenever they occur together downstream or upstream, they appear as the same set rather
   * than dependent on the order they occur in within the sentence. This assumes that all tags are
   * marked with "W="
   *
   * @param featureKey
   * @return
   */
  private String doFeatureSort(String featureKey) {
    String tagSeparator = "_&_";
    String[] tags = StringUtils.splitByWholeSeparator(featureKey, tagSeparator);
    List<String> upTags = new ArrayList<>();
    List<String> downTags = new ArrayList<>();
    StringBuilder sb = new StringBuilder("");

    for (String curTag : tags) {
      if (curTag.endsWith(ConjunctionCollapser.UPSTREAM_TAG)) {
        int startIndex = curTag.indexOf("=") + 1;
        int endIndex = curTag.length() - ConjunctionCollapser.upTagLength;
        upTags.add(curTag.substring(startIndex, endIndex));
      }
      if (curTag.endsWith(ConjunctionCollapser.DOWNSTREAM_TAG)) {
        int startIndex = curTag.indexOf("=") + 1;
        int endIndex = curTag.length() - ConjunctionCollapser.downTagLength;
        downTags.add(curTag.substring(startIndex, endIndex));
      }
    }

    if (!upTags.isEmpty()) {
      Collections.sort(upTags);
      sb.append("W=");
      java.util.Iterator<String> upTagIterator = upTags.iterator();
      sb.append(upTagIterator.next());
      while (upTagIterator.hasNext()) {
        sb.append(",").append(upTagIterator.next());
      }
      sb.append(ConjunctionCollapser.UPSTREAM_TAG);
      if (!downTags.isEmpty()) {
        sb.append(tagSeparator);
      }
    }
    if (!downTags.isEmpty()) {
      Collections.sort(downTags);
      sb.append("W=");
      java.util.Iterator<String> downTagIterator = downTags.iterator();
      sb.append(downTagIterator.next());
      while (downTagIterator.hasNext()) {
        sb.append(",").append(downTagIterator.next());
      }
      sb.append(ConjunctionCollapser.DOWNSTREAM_TAG);
    }

    return sb.toString();
  }


  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.version = in.readInt();
    this.replaceSingletons = in.readBoolean();
    this.sortFeatures = in.readBoolean();
  }


  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(ConjunctionCollapser.CURRENT_SERIAL_VERSION);
    out.writeBoolean(this.replaceSingletons);
    out.writeBoolean(this.sortFeatures);;
  }
}
