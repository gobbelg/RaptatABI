package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.reasonnomeds;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenMarker;

public class ReasonNoMedsTokenMarker extends TokenMarker {

  /** */
  private static final long serialVersionUID = 7085217110940270116L;


  public ReasonNoMedsTokenMarker(int window) {
    super();
    this.backwardAnalyzer = new ReasonNoMedsBackwardLogicAnalyzer(window);
    this.forwardAnalyzer = new ReasonNoMedsForwardLogicAnalyzer(window);
  }
}
