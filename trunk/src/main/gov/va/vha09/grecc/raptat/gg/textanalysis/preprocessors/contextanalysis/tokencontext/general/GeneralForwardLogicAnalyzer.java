package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.general;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptPhraseModifier;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.ForwardLogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.LogicAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class GeneralForwardLogicAnalyzer extends ForwardLogicAnalyzer {
  /** */
  private static final long serialVersionUID = 2754842346358752717L;

  ConceptPhraseModifier[] windows;
  int blockerWindowRemaining = 0;
  int facilitatorWindowRemaining = 0;


  public GeneralForwardLogicAnalyzer(ConceptPhraseModifier[] windows) {
    super(0);
    this.windows = windows;
  }


  public void updateBlockingStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];
    this.isMarking = false;

    if (curMark == GeneralMarks.ContextMark.BLOCKER) {
      this.blockerWindowRemaining = this.windows[this.curIndex].forwardWindow;
      this.isMarking = true;
    } else if (this.blockerWindowRemaining > 0) {
      this.isMarking = true;
      this.blockerWindowRemaining--;
    }

    ++this.curIndex;
  }


  public void updateFacilitatingStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];
    this.isMarking = false;

    if (curMark == GeneralMarks.ContextMark.FACILITATOR) {
      this.facilitatorWindowRemaining = this.windows[this.curIndex].forwardWindow;
      this.isMarking = true;
    } else if (this.facilitatorWindowRemaining > 0) {
      this.isMarking = true;
      this.facilitatorWindowRemaining--;
    }

    ++this.curIndex;
  }


  @Override
  @SuppressWarnings("UnusedAssignment")
  public int updateStatus() {
    Mark curMark = LogicAnalyzer.boundaryMarks[this.curIndex];
    this.isMarking = false;

    if (curMark == GeneralMarks.ContextMark.FACILITATOR) {
      this.facilitatorWindowRemaining = this.windows[this.curIndex].forwardWindow;
      this.isMarking = true;
    } else if (this.facilitatorWindowRemaining > 0) {
      this.isMarking = true;
      this.facilitatorWindowRemaining--;
    }

    return ++this.curIndex;
  }
}
