package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.reasonnomeds;

import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BackwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class ReasonNoMedsBackwardLogicAnalyzer extends BackwardLogicAnalyzer {

  /** */
  private static final long serialVersionUID = -6637456413515648116L;


  /**
   * ********************************************
   *
   * @param window
   * @author Glenn Gobbel - Jun 12, 2013 ********************************************
   */
  public ReasonNoMedsBackwardLogicAnalyzer(int window) {
    super(window);
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * <p>
   * Precondition - theIndex must be non-negative
   *
   * @param theIndex
   * @param theMarks
   * @author Glenn Gobbel - Jun 16, 2013 ***********************************************************
   */
  @Override
  public void reset(int theIndex, String[] theMarks) {
    this.curIndex = theIndex;
    this.isMarking = false;
    this.remainingContextLength = 0;
    if (LogicAnalyzer.boundaryMarks[this.curIndex] == ReasonNoMedsMark.ContextMark.PRE
        || LogicAnalyzer.boundaryMarks[this.curIndex] == ReasonNoMedsMark.ContextMark.POST) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }

    if (this.isMarking) {
      theMarks[this.curIndex] = RaptatConstants.REASON_NO_MEDS_TAG;
    }
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];

    if (this.remainingContextLength > 0) {
      if (curMark != ReasonNoMedsMark.ContextMark.TERM) {
        --this.remainingContextLength;
      }
      // If there is a termination mark, stop negating until we
      // reach another negator phrase
      else {
        this.isMarking = false;
        this.remainingContextLength = 0;
      }
    } else if (curMark == ReasonNoMedsMark.ContextMark.PRE
        || curMark == ReasonNoMedsMark.ContextMark.POST) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    } else {
      this.isMarking = false;
    }
    return --this.curIndex;
  }
}
