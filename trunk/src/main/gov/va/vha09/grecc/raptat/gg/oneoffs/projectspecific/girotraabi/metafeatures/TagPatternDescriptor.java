/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class for building metafeatures from RaptatTokenPhrase instances. Contains the fragments of text
 * (tags) to be found in a feature by a MetaFeatureBuilder instance.
 *
 * @author Glenn Gobbel
 */
public class TagPatternDescriptor {
  public final Set<Pattern> patternSet;

  TagPatternDescriptor(final String... textStrings) {
    this.patternSet = new HashSet<>();
    for (int i = 0; i < textStrings.length; i++) {
      /*
       * The text strings should not be directly preceded by any letters, only an "_" (used as a
       * delimiter in features) or the beginning of the line.
       */
      this.patternSet.add(Pattern.compile("(?<![a-zA-Z])" + textStrings[i] + "(_|$)",
          Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE));
    }
  }
}
