package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.negation;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenMarker;

public class NegationTokenMarker extends TokenMarker {

  /** */
  private static final long serialVersionUID = -8094856716309085050L;


  public NegationTokenMarker(int window) {
    super();
    this.backwardAnalyzer = new NegationBackwardLogicAnalyzer(window);
    this.forwardAnalyzer = new NegationForwardLogicAnalyzer(window);
  }
}
