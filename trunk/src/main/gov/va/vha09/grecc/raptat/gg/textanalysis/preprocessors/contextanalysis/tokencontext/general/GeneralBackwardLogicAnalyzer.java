/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.general;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptPhraseModifier;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.BackwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Jun 12, 2013 *****************************************************
 */
public class GeneralBackwardLogicAnalyzer extends BackwardLogicAnalyzer {
  /** */
  private static final long serialVersionUID = -7087551823827566175L;

  ConceptPhraseModifier[] windows;
  int blockerWindowRemaining = 0;
  int facilitatorWindowRemaining = 0;
  String[] result;


  public GeneralBackwardLogicAnalyzer(ConceptPhraseModifier[] windows) {
    super(0);
    this.windows = windows;
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
    this.result = theMarks;
  }


  public void updateBlockingStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];
    this.isMarking = false;

    if (curMark == GeneralMarks.ContextMark.BLOCKER) {
      this.blockerWindowRemaining = this.windows[this.curIndex].backwardWindow;
      this.isMarking = true;
    } else if (this.blockerWindowRemaining > 0) {
      this.isMarking = true;
      this.blockerWindowRemaining--;
    }

    --this.curIndex;
  }


  public void updateFacilitatingStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];
    this.isMarking = false;

    if (curMark == GeneralMarks.ContextMark.FACILITATOR) {
      this.facilitatorWindowRemaining = this.windows[this.curIndex].backwardWindow;
      this.isMarking = true;
    } else if (this.facilitatorWindowRemaining > 0) {
      this.isMarking = true;
      this.facilitatorWindowRemaining--;
    }

    --this.curIndex;
  }


  @Override
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];
    this.isMarking = false;
    return --this.curIndex;
  }
}
