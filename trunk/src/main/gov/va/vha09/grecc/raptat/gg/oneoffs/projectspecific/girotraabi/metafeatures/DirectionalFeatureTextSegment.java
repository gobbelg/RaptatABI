package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

/**
 * Handles whether to search forward or reverse tells whether search should search both directions
 * at once, for example to tell distance regardless of whether searching forward or reverse
 *
 * @author gtony
 */
public class DirectionalFeatureTextSegment extends TagPatternDescriptor {

  public static final String BEFORE = "before";
  public static final String AFTER = "after";
  public final boolean bidirectional;


  public DirectionalFeatureTextSegment(final boolean bidirectional) {
    super(BEFORE, AFTER);
    this.bidirectional = bidirectional;
  }


  public DirectionalFeatureTextSegment(final boolean bidirectional, final String... textStrings) {
    super(textStrings);
    this.bidirectional = bidirectional;
  }
}
