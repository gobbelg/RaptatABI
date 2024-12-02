package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

import java.util.Collection;
import java.util.regex.Pattern;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;

/**
 * Generates features for a given RaptatTokenPhrase instance based on existing features. That is, it
 * builds features of features.
 *
 * @author VHATVHGOBBEG
 *
 */
public abstract class MetaFeatureBuilder {

  /*
   * Pattern to find feature names ending with "...distance_integer" where integer is a number from
   * 0 to infinity.
   */
  protected static final Pattern DISTANCE_FEATURE_PATTERN =
      Pattern.compile("^[^0-9]+_distance_(\\d+)$",
          Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE);

  protected static final String FEATURE_DELIMITER = "_";

  public abstract void buildFeatures(Collection<RaptatTokenPhrase> phrases);
}
