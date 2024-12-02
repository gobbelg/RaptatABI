/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import weka.core.Attribute;

/**
 * @author VHATVHGOBBEG
 *
 */
public class AttributeNameFilter implements AttributeFilter {

  /*
   * Return false if an attribute has a name containing the String in the excludedNames set
   */
  private Set<Matcher> exclusionMatchers = Collections.emptySet();

  /**
   *
   */
  public AttributeNameFilter(final Collection<String> excludedPatternStrings) {
    this.exclusionMatchers = new HashSet<>(excludedPatternStrings.size());
    for (String patternString : excludedPatternStrings) {
      Pattern exclusionPattern = Pattern.compile(patternString,
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
      this.exclusionMatchers.add(exclusionPattern.matcher(""));
    }
  }

  @Override
  public boolean acceptAttribute(final Attribute attribute) {
    for (Matcher exclusionMatcher : this.exclusionMatchers) {
      if (exclusionMatcher.reset(attribute.name().toLowerCase()).find()) {
        return false;
      }
    }
    return true;
  }

  /**
   * This method is only here to comply with theAttributeFilter interface and does nothing.
   */
  @Override
  public void initialize(final Object instanceInitializer) {
    // Non-functional empty method.
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }

}
