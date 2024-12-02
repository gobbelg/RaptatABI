package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.reasonnomeds;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.ForwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class ReasonNoMedsForwardLogicAnalyzer extends ForwardLogicAnalyzer {

  /** */
  private static final long serialVersionUID = 5737758717748076224L;


  public ReasonNoMedsForwardLogicAnalyzer(int window) {
    super(window);
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];

    // If boundaryMarks is set to pre-reason no meds for the current
    // index, mark reason no meds for the corresponding token and set up
    // context length to mark tokens within the window
    if (curMark == ReasonNoMedsMark.ContextMark.PRE) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }
    // If not pre-reason no med but there is remainingContextLength due
    // to an earlier reason no meds and the current token is not part of a
    // termination phrase, mark the token.
    else if (this.remainingContextLength > 0) {
      if (curMark != ReasonNoMedsMark.ContextMark.TERM) {
        this.isMarking = true;
        --this.remainingContextLength;
      }
      // If there is a termination mark, stop marking until we
      // reach another pre-marked phrase
      else {
        this.isMarking = false;
        this.remainingContextLength = 0;
      }
    } else {
      this.isMarking = false;
    }
    return ++this.curIndex;
  }
}
