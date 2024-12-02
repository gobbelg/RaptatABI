/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

/**
 * A type of 'meta' feature builder that uses previously generated features to determine proximity
 * of key concepts and the words that identify them to a concept of interest, like index value or
 * calcification. It is generally used to provide labels based on distances between the target we
 * are trying to label and the feature. This class takes features that may be at different distances
 * and groups them if they are within a range of distances (proximities).
 *
 * @author gtony
 */
public class ProximityDescriptorMetaFeatureBuilder extends MetaFeatureBuilder {
  private static final Logger LOGGER =
      Logger.getLogger(ProximityDescriptorMetaFeatureBuilder.class);
  {
    LOGGER.setLevel(Level.INFO);
  }

  /*
   * Contains the text segments being looked for. If empty, it will process all regardless of text
   * segments present or absent.
   */
  protected List<TagPatternDescriptor> textFeatureDescriptors;
  {
    this.textFeatureDescriptors = new ArrayList<>();
    // this.textFeatureDescriptors.add(new TextDescriptor("ABI", "TBI", "Left", "Right", "Ankle",
    // "Tibial", "Arm", "Posterior_Tibial", "Index", "Thigh", "Digit", "Brachial"));
    // this.textFeatureDescriptors.add(new TextDescriptor("Row", "Col", "Doc"));
  }

  protected DirectionalFeatureTextSegment textSegmentDirections =
      new DirectionalFeatureTextSegment(true);

  /*
   * Provides a mapping of a pair of distances in tokens that describe a range of distances
   * (proximity) to a string describing those distances, such as "PROXIMITY_2TO_5" could describe a
   * distance of 2 to 5 tokens
   */
  private final Map<RaptatPair<Integer, Integer>, String> proximityMap;

  /** */
  public ProximityDescriptorMetaFeatureBuilder(
      final DirectionalFeatureTextSegment textSegmentDirections,
      final List<TagPatternDescriptor> textSegmentDescriptors,
      final List<RaptatPair<Integer, Integer>> proximityDistances) {
    super();
    this.textSegmentDirections = textSegmentDirections;
    this.textFeatureDescriptors = textSegmentDescriptors;
    this.proximityMap = getProximityMap(proximityDistances);
  }


  /**
   * Use default settings except for proximities, which must be specified
   *
   * @param proximityMap
   */
  public ProximityDescriptorMetaFeatureBuilder(
      final List<RaptatPair<Integer, Integer>> proximityDistances) {
    super();
    this.proximityMap = getProximityMap(proximityDistances);
  }

  @Override
  public void buildFeatures(final Collection<RaptatTokenPhrase> phrases) {
    LOGGER.info("\n\tPROXIMITY DESCRIPTOR TOKEN PHRASE PROCESSING\n");
    for (final RaptatTokenPhrase tokenPhrase : phrases) {
      LOGGER.debug("\r\nPHRASE FOR BUILDING METAFEATURES:" + tokenPhrase.getPhraseText() + "\n");

      final List<String> metaFeatures = new ArrayList<>();
      final Set<String> tokenPhraseFeatures = tokenPhrase.getPhraseFeatures();
      for (final String phraseFeature : tokenPhraseFeatures) {
        LOGGER.debug("\n\tPHRASE FEATURE:" + phraseFeature + "\n");
        final List<String> phraseMetaFeatures = getSingleDirectionFeatures(phraseFeature);
        metaFeatures.addAll(phraseMetaFeatures);
      }
      if (this.textSegmentDirections.bidirectional) {
        metaFeatures.addAll(getBidirectionalFeatures(tokenPhraseFeatures));
      }

      tokenPhrase.addPhraseFeatures(metaFeatures);

      LOGGER.debug("\n\tCOMPLETED PROCESSING PHRASE:" + tokenPhrase.getPhraseText() + "\n");
    }
    LOGGER.info("\n\tTOKEN PHRASE PROCESSING COMPLETE\n");
  }

  /**
   * Features for either forward or reverse direction (or other?)
   *
   * @param phraseFeature
   * @return
   */
  protected List<String> getSingleDirectionFeatures(final String phraseFeature) {
    final List<String> resultFeatures = new ArrayList<>();

    /*
     * First check for Direction and Distance features, which are needed to determine proximity
     */
    final Optional<Matcher> validMatcher = validTextAndDistanceFeature(phraseFeature);
    if (!validMatcher.isPresent()) {
      return resultFeatures;
    }

    /*
     * Check if the phraseFeatures contains a string (e.g. 'forward' or 'reverse') in the set of
     * strings within the textSegmentDirections field
     */
    for (final Pattern directionPattern : this.textSegmentDirections.patternSet) {
      if (!directionPattern.matcher(phraseFeature).find()) {
        continue;
      }

      final int lastDelimiterIndex = phraseFeature.lastIndexOf(FEATURE_DELIMITER);
      final int distance = Integer.parseInt(validMatcher.get().group(1));
      final List<String> proximityFeatures = getProximityFeatures(distance);

      if (proximityFeatures.isEmpty()) {
        continue;
      }

      final String featurePrefix = phraseFeature.substring(0, lastDelimiterIndex);
      for (final String proximityFeature : proximityFeatures) {
        resultFeatures.add((featurePrefix + proximityFeature).toLowerCase());
      }

    }
    return resultFeatures;
  }

  /**
   * Check to see if the phraseFeature under consideration has the proper text substring and matches
   * the DISTANCE_FEATURE_PATTERN. If so, return an optional with the matched matcher object.
   * Otherwise, return an empty optional.
   *
   * @param phraseFeature
   * @return
   */
  protected Optional<Matcher> validTextAndDistanceFeature(String phraseFeature) {

    final Matcher distanceFeatureMatcher = DISTANCE_FEATURE_PATTERN.matcher(phraseFeature);

    /*
     * Check to see if the phraseFeature string object contains one string element from ALL of the
     * textFeatureDescriptors. Note that if textFeatureDescriptors is empty, this will not be check,
     * so all phraseFeature strings will generate a metaFeature
     */
    phraseFeature = phraseFeature.toLowerCase();
    if (this.textFeatureDescriptors != null && !this.textFeatureDescriptors.isEmpty()) {
      final ListIterator<TagPatternDescriptor> descriptorIterator =
          this.textFeatureDescriptors.listIterator();
      if (!findDescribedTextFeatures(phraseFeature, descriptorIterator)) {
        return Optional.empty();
      }
    }

    if (!distanceFeatureMatcher.matches()) {
      return Optional.empty();
    }

    return Optional.of(distanceFeatureMatcher);
  }


  /**
   * Tests whether a feature, generally of the form "AAA_BBBBB_CC_DDD..." has the text segments we
   * are looking for in the string describing the feature. The iterator iterates through a list of
   * TextDescriptor objects that contain the text segments we are searching for. If one text element
   * is found from the TextDescriptor elements provided by the iterator, return true.
   *
   * @param phraseFeature
   * @param iterator
   * @return
   */
  private boolean findDescribedTextFeatures(final String phraseFeature,
      final ListIterator<TagPatternDescriptor> iterator) {
    if (!iterator.hasNext()) {
      return true;
    }

    final TagPatternDescriptor identifier = iterator.next();
    for (final Pattern idPattern : identifier.patternSet) {
      if (!idPattern.matcher(phraseFeature).find()) {
        continue;
      }
      return findDescribedTextFeatures(phraseFeature, iterator);
    }

    return false;
  }

  private Collection<? extends String> getBidirectionalFeatures(
      final Set<String> tokenPhraseFeatures) {
    /*
     * First determine which direction (from this.textSegmentDirections.textSet) is closer, and use
     * this to determine just how close an element is to the element of interest (e.g. index value
     * or compressibility mention), regardless of direction used in parsing.
     */
    final Map<String, Integer> featureToDistanceMap = new HashMap<>();
    for (final Pattern parseDirectionPattern : this.textSegmentDirections.patternSet) {
      for (String feature : tokenPhraseFeatures) {
        if (!parseDirectionPattern.matcher(feature).find()) {
          continue;
        }
        feature = feature.replaceFirst(parseDirectionPattern.pattern(), "");
        final int delimiterIndex = feature.lastIndexOf(FEATURE_DELIMITER);
        final String numberString = feature.substring(delimiterIndex + 1, feature.length()).trim();
        if (!numberString.matches("\\d+")) {
          continue;
        }
        final Integer featureDistance = Integer.parseInt(numberString);
        final String metaMetaFeature = feature.substring(0, delimiterIndex + 1);
        Integer currentMax = 0;
        if ((currentMax = featureToDistanceMap.get(metaMetaFeature)) == null
            || featureDistance > currentMax) {
          featureToDistanceMap.put(metaMetaFeature, featureDistance);
        }
      }
    }

    final List<String> bidirectionalFeatures = new ArrayList<>();
    for (final Entry<String, Integer> mapEntry : featureToDistanceMap.entrySet()) {
      final List<String> proximityFeatures = getProximityFeatures(mapEntry.getValue());
      final String prefix = mapEntry.getKey();
      for (final String proximityFeature : proximityFeatures) {
        bidirectionalFeatures.add((prefix + proximityFeature).toLowerCase());
      }
    }
    return bidirectionalFeatures;
  }

  private List<String> getProximityFeatures(final int distance) {
    final List<String> resultList = new ArrayList<>();
    for (final RaptatPair<Integer, Integer> proximity : this.proximityMap.keySet()) {
      if (distance >= proximity.left && distance < proximity.right) {
        resultList.add(this.proximityMap.get(proximity));
      }
    }
    return resultList;
  }

  /**
   * Creates a mapping for a range of distances between which a feature might be found from a
   * potential label and the name the range will be associated with for all features found within
   * ths range.
   *
   * @param proximityDistances
   * @return
   */
  private Map<RaptatPair<Integer, Integer>, String> getProximityMap(
      final List<RaptatPair<Integer, Integer>> proximityDistances) {
    final Map<RaptatPair<Integer, Integer>, String> resultMap = new HashMap<>();

    final NumberFormat formatter = new DecimalFormat("000");
    for (final RaptatPair<Integer, Integer> proximityPair : proximityDistances) {
      final StringBuilder sb = new StringBuilder("_PROXIMITY_");
      sb.append(formatter.format(proximityPair.left));
      sb.append("_TO_");
      final String maxDistance = proximityPair.right == Integer.MAX_VALUE ? "MAXVALUE"
          : formatter.format(proximityPair.right);
      sb.append(maxDistance);
      resultMap.put(proximityPair, sb.toString());
    }

    return resultMap;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }
}
