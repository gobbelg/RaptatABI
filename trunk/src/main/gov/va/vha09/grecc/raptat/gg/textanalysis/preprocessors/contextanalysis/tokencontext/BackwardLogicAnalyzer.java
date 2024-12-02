/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

/**
 * Class Description - Instances of this class essentially "walk" from the end to the beginning of a
 * phrase and analyze "marks" that represent contextual boundaries to determine the context of each
 * token analyzed. Members of this class may be paired with instances of the ForwardLogicAnalyzer
 * class.
 *
 * @author Glenn Gobbel - Jul 17, 2014
 */
public abstract class BackwardLogicAnalyzer extends LogicAnalyzer {

  /** */
  private static final long serialVersionUID = -7375882801590458077L;


  /**
   * @param window
   */
  public BackwardLogicAnalyzer(int window) {
    super(window);
  }


  public abstract void reset(int startIndex, String[] theMarks);
}
