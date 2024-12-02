/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.ForwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/** @author vhatvhsahas1 */
public class UncertaintyForwardLogicAnalyzer extends ForwardLogicAnalyzer {

  /** */
  private static final long serialVersionUID = 4465594012812084495L;


  public UncertaintyForwardLogicAnalyzer(int window) {
    super(window);
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];

    /*
     * If boundaryMarks is set to pre-uncertain for the current index, negate the corresponding
     * token and set up context length to negate tokens within the window
     */
    if (curMark == UncertaintyMark.ContextMark.PRE) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }
    /*
     * If boundaryMarks is set to pre/post-uncertain for the current index, negate the corresponding
     * token and set up context length to negate tokens within the window
     */
    else if (curMark == UncertaintyMark.ContextMark.PREPOST) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }
    /*
     * If not pre-uncertain but there is remainingContextLength due to an earlier negator and the
     * current token is not part of a termination phrase, negate the token.
     */
    else if (this.remainingContextLength > 0) {
      if (curMark != UncertaintyMark.ContextMark.TERM) {
        --this.remainingContextLength;
      }
      /*
       * If there is a termination mark, stop negating until we reach another negator phrase
       */ else {
        this.isMarking = false;
        this.remainingContextLength = 0;
      }
    } else {
      this.isMarking = false;
    }

    return ++this.curIndex;
  }
}
