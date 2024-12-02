package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

/**
 *
 *
 * @author VHATVHGOBBEG
 *
 */
public class ClosestTagFeatureBuilder extends MetaFeatureBuilder {

  private static final Logger LOGGER = Logger.getLogger(ClosestTagFeatureBuilder.class);
  {
    LOGGER.setLevel(Level.INFO);
  }

  /*
   * TODO: August 18, 2022 - Check to see why there is not TagStringDescriptors is not "After"
   * (which seems like it should be there given the "Before" as one of the TagPatternDescriptor text
   * strings.
   */

  /*
   * Contains the text segments (tags) being searched for to determine shortest distances. A
   * comparison is made among all features containing one of each of the tags identified by each of
   * the TagPatternDescriptors within the tagDescriptors list.
   */
  protected List<TagPatternDescriptor> removalTagDescriptors;
  /*
   * Default list of TagPatternDescriptors instances that describe the tags that will be removed
   * from a feature when identifying the closest feature. Note that a feature is evaluated only if
   * it has exactly one text segment from each of the patterns in the tagDescriptors list. From
   * among the features that match (i.e., have the same text segment from each descriptor), the one
   * with the shortest distance element is chosen to create a new 'closest' feature.
   *
   * Note that it is assumed that only one of the strings in *each* tagDescriptor is found in the
   * feature string for a phrase.
   */
  {
    removalTagDescriptors = new ArrayList<>();
    removalTagDescriptors.add(new TagPatternDescriptor("Row", "Col", "Doc"));
    removalTagDescriptors.add(new TagPatternDescriptor("Before"));
    removalTagDescriptors.add(new TagPatternDescriptor("Phrases"));
  }

  /*
   * Default names within the feature that will be kept after processing for the closest. The
   * strings provided here in the TagPatternDescriptor generally will correspond to the names of the
   * labels provided to the TokenPhraseMaker in a file. For example, the name might be "laterality"
   * with values of "right," "left," and "bilateral." After processing a building features, there
   * should only be one "closest" feature for each RaptatTokenPhrase of interest with each of the
   * strings found in the indexTagDescriptors. So, in the case of laterality, this would pick either
   * "right," "left," or "bilateral" (exclusively) along with "laterality" for the name of the
   * closest feature. That is, the system would choose from right, left, and bilateral to determine
   * which is closest to a phrase of interest, such as index value.
   */
  protected TagPatternDescriptor indexTagDescriptors =
      new TagPatternDescriptor("Anatomy", "Artery", "IndexValueType", "Laterality", "Pressure");


  public ClosestTagFeatureBuilder() {}



  @Override
  public void buildFeatures(final Collection<RaptatTokenPhrase> targetedConceptPhrases) {
    LOGGER.info("\r\nBUILDING CLOSEST TAG FEATURES:\n");

    for (final RaptatTokenPhrase tokenPhrase : targetedConceptPhrases) {

      LOGGER.debug(
          "\r\nPHRASE FOR BUILDING CLOSEST TAG FEATURES:" + tokenPhrase.getPhraseText() + "\n");

      final Set<String> tokenPhraseFeatures = tokenPhrase.getPhraseFeatures();

      /*
       * Stores the TextPatternDescriptor patterns and the closest distance of their mapping
       */
      final Map<Pattern, Integer> patternToDistanceMap = new HashMap<>();

      final Map<Pattern, RaptatPair<String, List<Pattern>>> patternsToFeatureAndRemovalMap =
          new HashMap<>();

      for (final String phraseFeature : tokenPhraseFeatures) {

        if (!DISTANCE_FEATURE_PATTERN.matcher(phraseFeature).find()) {
          continue;
        }

        Optional<Pattern> indexPatternOptional =
            findFeatureSegment(phraseFeature, indexTagDescriptors.patternSet);
        if (!indexPatternOptional.isPresent()) {
          continue;
        }
        Pattern indexPattern = indexPatternOptional.get();

        final Optional<RaptatPair<List<Pattern>, Integer>> tagsForRemoval =
            findRemovalTags(phraseFeature);
        if (!tagsForRemoval.isPresent()) {
          continue;
        }


        LOGGER.debug("\nFeature included - matches found for feature:" + phraseFeature);
        List<Pattern> removalPatterns = tagsForRemoval.get().left;
        int tagDistance = tagsForRemoval.get().right;
        RaptatPair<String, List<Pattern>> featureAndRemovalPatternPair =
            new RaptatPair<>(phraseFeature, removalPatterns);

        if (LOGGER.getLevel().equals(Level.DEBUG)) {
          LOGGER.debug("\nPATTERNS FOUND\n-------------------------------");
          LOGGER.debug("IndexPattern:" + indexPattern.pattern());
          removalPatterns.forEach(pattern -> LOGGER.debug("Pattern:" + pattern.pattern()));
          LOGGER.debug("Distance for feature:" + tagDistance);
          final Integer currentDistance = patternToDistanceMap.get(indexPattern);
          LOGGER.debug("Current distance for feature:" + currentDistance == null ? "NotPresent"
              : currentDistance);
          final RaptatPair<String, List<Pattern>> featureRemovalPair =
              patternsToFeatureAndRemovalMap.get(indexPattern);
          if (featureRemovalPair == null) {
            LOGGER.debug("Current mapped feature not present");
          }
          else {
            LOGGER.debug("Current mapped feature:" + featureRemovalPair.left);
          }
        }

        final int testDistance =
            patternToDistanceMap.computeIfAbsent(indexPattern, k -> tagDistance);
        patternsToFeatureAndRemovalMap.putIfAbsent(indexPattern, featureAndRemovalPatternPair);
        if (tagDistance < testDistance) {
          patternToDistanceMap.put(indexPattern, tagDistance);
          patternsToFeatureAndRemovalMap.put(indexPattern, featureAndRemovalPatternPair);
        }
      } // END FOR LOOP
      final Set<String> closestFeatures =
          getMetaFeaturesFromMappedTags(patternsToFeatureAndRemovalMap);
      tokenPhrase.addPhraseFeatures(closestFeatures);
      LOGGER.debug("\n\tCOMPLETED PROCESSING PHRASE:" + tokenPhrase.getPhraseText() + "\n");
    }
    LOGGER.info("\n\tTOKEN PHRASE PROCESSING COMPLETE\n");

  }


  /**
   * Return the first pattern from a pattern set that is found in the phraseFeature string. Return
   * an empty option if none of the patterns in the set match phraseFeature.
   *
   * @param phraseFeature
   * @param patternSet
   * @return
   */
  private Optional<Pattern> findFeatureSegment(final String phraseFeature,
      final Set<Pattern> patternSet) {
    for (final Pattern segmentPattern : patternSet) {
      if (segmentPattern.matcher(phraseFeature).find()) {
        return Optional.of(segmentPattern);
      }
    }
    return Optional.empty();
  }

  private Optional<Pattern> findIndexTag(final String phraseFeature) {
    return findFeatureSegment(phraseFeature, indexTagDescriptors.patternSet);
  }

  /**
   * Determine if there is a list of strings, one from each of the list of TextDescriptor instances
   * in the tagDescriptors field, that matches the phraseFeatue string supplied as a parameter. If
   * so, return the list of patterns and the distance value, which should be an integer added to the
   * end of the phraseFeature string.
   *
   * @param phraseFeature
   * @return
   */
  private Optional<RaptatPair<List<Pattern>, Integer>> findRemovalTags(String phraseFeature) {
    phraseFeature = phraseFeature.toLowerCase();
    final List<Pattern> resultList = new ArrayList<>();
    for (final TagPatternDescriptor textDescriptor : removalTagDescriptors) {
      final Optional<Pattern> foundPattern =
          findFeatureSegment(phraseFeature, textDescriptor.patternSet);
      if (!foundPattern.isPresent()) {
        return Optional.empty();
      }
      resultList.add(foundPattern.get());
    }
    final int delimiterIndex = phraseFeature.lastIndexOf(FEATURE_DELIMITER);
    final String numberString =
        phraseFeature.substring(delimiterIndex + 1, phraseFeature.length()).trim();
    final Integer featureDistance = Integer.parseInt(numberString);
    return Optional.of(new RaptatPair<>(resultList, featureDistance));
  }

  private Set<String> getMetaFeaturesFromMappedTags(
      final Map<Pattern, RaptatPair<String, List<Pattern>>> patternsToFeatureAndRemovalMap) {

    LOGGER.debug("Getting closest metafeatures from mapped tags");

    final Set<String> resultSet = new HashSet<>();
    for (final RaptatPair<String, List<Pattern>> featurePatternPair : patternsToFeatureAndRemovalMap
        .values()) {
      List<Pattern> patternsForRemoval = featurePatternPair.right;
      String feature = featurePatternPair.left;
      if (LOGGER.getLevel().equals(Level.DEBUG)) {
        LOGGER.debug("\nPATTERNS FOR REMOVAL\n-------------------------------");
        patternsForRemoval.forEach(pattern -> LOGGER.debug("Pattern:" + pattern.pattern()));
        LOGGER.debug("Feature mapped by pattern:" + feature == null ? "NotPresent" : feature);
      }
      for (final Pattern removalPattern : patternsForRemoval) {

        LOGGER.debug("Removing patterns from:" + feature);
        feature = feature.replaceFirst(removalPattern.pattern().toLowerCase(), "");
      }
      LOGGER.debug("Cleaning feature after pattern removal:" + feature);
      feature = feature.replaceAll("\\d+$", "");
      feature = feature.replaceAll("^_+|_+$", "");
      feature = feature.replaceAll("_+", "_");
      resultSet.add(feature + "_closest");
      LOGGER.debug("Feature cleaning and tagging complete:" + feature);
    }
    return resultSet;
  }


  public static void main(final String[] args) {

  }

}
