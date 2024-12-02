package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.text.WordUtils;

/**
 * PhraseClass - An enum used to store the target that we are trying to annotate, like an index
 * value (generally a number between 0 and 1.4) from an ABI report, along with the potential
 * annotations it can have. For the various values of the enum (e.g. DEFAULT, INDEX_VALUE_TYPE,
 * etc), note that the first string in the constructor parameters accompanying the enum value is the
 * name of the concept being targeted. In the case of the INDEX_VALUE_TYPE enum, we are trying to
 * assign a type to the index value of either 'abi', 'tbi,' or 'noclassassigned' (if a type cannot
 * be identified).
 *
 *
 * @author Glenn Gobbel
 * @date May 26, 2019
 */
public enum PhraseClass implements Serializable {

  DEFAULT("default", "default_value"),

  INDEX_VALUE_TYPE("IndexValue", "abi", "tbi", "noclassassigned"),

  COMPRESSIBILITY("noncompressible", "noncompressibleleft", "compressibleright",
      "noncompressiblebilateral", "!!!!compressionunassigned"),

  NO_CLASS_ASSIGNED("noclassassigned"),

  INDEX_VALUE_LATERALITY("IndexValue", "right", "left", "noclassassigned"),

  INDEX_VALUE_TYPE_AND_LATERALITY("IndexValue", "rightabi", "leftabi", "righttbi", "lefttbi",
      "noindextype", "nolaterality", "notypeorlaterality");

  public final String conceptName;
  private TreeSet<String> classifications;

  private PhraseClass(final String conceptName, final String... classifications) {
    this.conceptName = conceptName;
    this.classifications = new TreeSet<>();
    for (final String classification : classifications) {
      this.classifications.add(classification.toLowerCase());
    }
  }

  public SortedSet<String> getClassifications() {
    return Collections.unmodifiableSortedSet(this.classifications);
  }

  public String getConceptName() {
    return this.conceptName;
  }

  @Override
  public String toString() {
    return WordUtils.capitalizeFully(this.name(), '_');
  }
}
