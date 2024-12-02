package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.negation;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.ForwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class NegationForwardLogicAnalyzer extends ForwardLogicAnalyzer {

  /** */
  private static final long serialVersionUID = 9013119319094176369L;


  public NegationForwardLogicAnalyzer(int window) {
    super(window);
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];

    /*
     * If boundaryMarks is set to pre-negation for the current index, negate the corresponding token
     * and set up context length to negate tokens within the window
     */
    if (curMark == NegationMark.ContextMark.PRE) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }

    /*
     * If not pre-negation but there is remainingContextLength due to an earlier negator and the
     * current token is not part of a termination phrase, negate the token.
     */
    else if (this.remainingContextLength > 0) {
      if (curMark != NegationMark.ContextMark.TERM) {
        --this.remainingContextLength;
      }

      /*
       * If there is a termination mark, stop negating until we reach another negator phrase
       */
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
