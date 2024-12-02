/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.negation;

import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BackwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Jun 12, 2013 *****************************************************
 */
public class NegationBackwardLogicAnalyzer extends BackwardLogicAnalyzer {

  /** */
  private static final long serialVersionUID = -4241803641441594814L;
  // This is used because if the last token is part of a pre-negating phrase,
  // it may be used as as a post-negating phrase (e.g. "he is running - not")
  boolean lastTokenNegating = false;


  /**
   * ********************************************
   *
   * @param window
   * @author Glenn Gobbel - Jun 12, 2013 ********************************************
   */
  public NegationBackwardLogicAnalyzer(int window) {
    super(window);
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * <p>
   * Precondition: theIndex must be non-negative
   *
   * @param theIndex
   * @param theMarks
   * @author Glenn Gobbel - Jun 12, 2013 ***********************************************************
   */
  @Override
  public void reset(int theIndex, String[] theMarks) {
    this.curIndex = theIndex;
    this.isMarking = false;
    this.lastTokenNegating = false;
    this.remainingContextLength = 0;
    if (LogicAnalyzer.boundaryMarks[this.curIndex] == NegationMark.ContextMark.PRE) {
      this.lastTokenNegating = true;
      this.isMarking = true;
      this.remainingContextLength = this.window;
    } else if (LogicAnalyzer.boundaryMarks[this.curIndex] == NegationMark.ContextMark.POST) {
      this.isMarking = true;
      this.remainingContextLength = this.window;
    }

    if (this.isMarking) {
      theMarks[this.curIndex] = RaptatConstants.NEGATION_TAG;
    }
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];

    if (this.lastTokenNegating) {
      if (curMark == NegationMark.ContextMark.PRE) {
        this.isMarking = true;
        this.remainingContextLength = this.window + 1;
      } else if (curMark == NegationMark.ContextMark.TERM) {
        this.isMarking = false;
        this.remainingContextLength = 0;
        this.lastTokenNegating = false;
      } else if (curMark == NegationMark.ContextMark.POST) {
        this.isMarking = true;
        this.remainingContextLength = this.window + 1;
        this.lastTokenNegating = false;
      } else {
        if (this.remainingContextLength < 1) {
          this.lastTokenNegating = false;
          this.isMarking = false;
        } else {
          --this.remainingContextLength;
        }
      }
    } else if (curMark == NegationMark.ContextMark.POST) {
      this.isMarking = true;
      this.remainingContextLength = this.window + 1;
    } else if (this.remainingContextLength > 0) {
      if (curMark != NegationMark.ContextMark.TERM) {
        --this.remainingContextLength;
      }
      // If there is a termination mark, stop negating until we
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
