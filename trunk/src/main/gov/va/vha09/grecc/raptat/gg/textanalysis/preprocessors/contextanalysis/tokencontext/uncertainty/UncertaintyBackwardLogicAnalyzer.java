/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty;

import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BackwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/** @author vhatvhsahas1 */
public class UncertaintyBackwardLogicAnalyzer extends BackwardLogicAnalyzer {

  /** */
  private static final long serialVersionUID = 4241672676047020008L;
  // This is used because if the last token is part of a pre-negating phrase,
  // it may be used as as a post-negating phrase (e.g. "he is running - not")
  boolean lastTokenUncertain = false;


  public UncertaintyBackwardLogicAnalyzer(int window) {
    super(window);
  }


  @Override
  public void reset(int index, String[] theMarks) {
    this.curIndex = index;
    this.isMarking = false;
    this.lastTokenUncertain = false;
    this.remainingContextLength = 0;

    if (LogicAnalyzer.boundaryMarks[this.curIndex] == UncertaintyMark.ContextMark.PRE) {
      this.lastTokenUncertain = true;
      this.isMarking = true;
      this.remainingContextLength = this.window;
    } else if (LogicAnalyzer.boundaryMarks[this.curIndex] == UncertaintyMark.ContextMark.POST) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    } else if (LogicAnalyzer.boundaryMarks[this.curIndex] == UncertaintyMark.ContextMark.PREPOST) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }
    if (this.isMarking) {
      theMarks[this.curIndex] = RaptatConstants.UNCERTAIN_TAG;
    }
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];

    if (this.lastTokenUncertain) {
      if (curMark == UncertaintyMark.ContextMark.PRE) {
        this.isMarking = true;
        this.remainingContextLength = this.window + 1;
      } else if (curMark == UncertaintyMark.ContextMark.TERM) {
        this.isMarking = false;
        this.remainingContextLength = 0;
        this.lastTokenUncertain = false;
      } else if (curMark == UncertaintyMark.ContextMark.POST) {
        this.isMarking = true;
        this.remainingContextLength = this.window + 1;
        this.lastTokenUncertain = false;
      } else if (curMark == UncertaintyMark.ContextMark.PREPOST) {
        this.isMarking = true;
        this.remainingContextLength = this.window + 1;
        this.lastTokenUncertain = false;
      } else {
        if (this.remainingContextLength < 1) {
          this.lastTokenUncertain = false;
          this.isMarking = false;
        } else {
          --this.remainingContextLength;
        }
      }
    } else if (curMark == UncertaintyMark.ContextMark.POST) {
      this.isMarking = true;
      this.remainingContextLength = this.window + 1;
    } else if (curMark == UncertaintyMark.ContextMark.PREPOST) {
      this.isMarking = true;
      this.remainingContextLength = this.window + 1;
    } else if (this.remainingContextLength > 0) {
      if (curMark != UncertaintyMark.ContextMark.TERM) {
        --this.remainingContextLength;
      } // If there is a termination mark, stop negating until we
        // reach another negator phrase
      else {
        this.isMarking = false;
        this.remainingContextLength = 0;
      }
    } else {
      this.isMarking = false;
    }
    return --this.curIndex;
  }
}
