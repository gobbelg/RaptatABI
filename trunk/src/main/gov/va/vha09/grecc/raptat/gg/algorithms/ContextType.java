package src.main.gov.va.vha09.grecc.raptat.gg.algorithms;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This is used in TWO DISTINCT ways. One use is to track the "context" of a token and its value(s).
 * In terms of the ContextType ASSERTIONSTATUS, it can have values of positive or negative. There
 * may be multiple potential values, but, for a given ContextType, a token can only take one value.
 * There is a 'contexts' field in RaptatToken objects that stores a mapping from the name of one of
 * the ContextType Enum values to the value of the token for that ContextType value.
 *
 * <p>
 * The other use is for setting up a TokenContextAnalyzer which uses rules like those in Negex to
 * determine the context of a token.
 *
 * @author Glenn T. Gobbel Mar 22, 2016
 */
public enum ContextType implements Serializable {
  /*
   * These are context types. The first string gives the name of the context, the second is the
   * default value, and the third is the commonly assigned value. If there are more strings, they
   * represent potential values.
   */
  // @formatter:off
  NEGATION("negexassertionstatus", "positive", "negative"),
  REASON_NOT_ON_MEDS("reasonnotonmeds", "notreason", "isreason"),
  UNCERTAINTY("uncertainty", "positive", "uncertain"),
  GENERAL("general", "unassigned", "assigned"),
  ASSERTIONSTATUS("assertionstatus", "positive", "negative"),
  EXPERIENCER("experiencer", "patient", "notpatient"),
  TEMPORALITY("temporality", "past", "present", "future"),
  ALLERGYSECTION("allergysection", "yes", "no"),
  MEDLISTSECTION("medlistsection", "yes", "no");
  //	@formatter:on

  private final String name;

  private final String defaultValue;

  private final String commonlyAssignedValue;

  private final Set<String> potentialValues = new HashSet<>();


  private ContextType(String name, String defaultValue, String commonlyAssignedValue,
      String... potentialValues) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.commonlyAssignedValue = commonlyAssignedValue;
    for (String curValue : potentialValues) {
      this.potentialValues.add(curValue);
    }
    this.potentialValues.add(this.defaultValue);
    this.potentialValues.add(this.commonlyAssignedValue);
  }


  /** @return the commonlyAssignedValue */
  public String getCommonlyAssignedValue() {
    return this.commonlyAssignedValue;
  }


  /** @return the defaultValue */
  public String getDefaultValue() {
    return this.defaultValue;
  }


  /** @return the potentialValues */
  public Set<String> getPotentialValues() {
    return this.potentialValues;
  }


  /** @return the name */
  public String getTypeName() {
    return this.name;
  }
}
