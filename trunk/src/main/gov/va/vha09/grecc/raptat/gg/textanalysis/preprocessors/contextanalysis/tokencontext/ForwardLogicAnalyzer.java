package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

/**
 * Class Description - Instances of this class essentially "walk" from the beginning to the end of a
 * phrase and analyze "marks" that represent contextual boundaries to determine the context of each
 * token analyzed.
 *
 * @author Glenn Gobbel - Jul 17, 2014
 */
public abstract class ForwardLogicAnalyzer extends LogicAnalyzer {
  /** */
  private static final long serialVersionUID = -3092588372947592741L;


  public ForwardLogicAnalyzer(int window) {
    super(window);
  }


  public void reset(Mark[] tokenBoundaryMarks) {
    LogicAnalyzer.boundaryMarks = tokenBoundaryMarks;
    this.isMarking = false;
    this.remainingContextLength = 0;
    this.curIndex = 0;
  }
}
