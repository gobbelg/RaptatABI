package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

public class ReasonNoMedsContextAnalyzerFactory {

  private static TokenContextAnalyzer reasonNoMedsContextAnalyzer = null;


  public ReasonNoMedsContextAnalyzerFactory() {}


  public static synchronized TokenContextAnalyzer getInstance() {
    if (ReasonNoMedsContextAnalyzerFactory.reasonNoMedsContextAnalyzer == null) {
      ReasonNoMedsContextAnalyzerFactory.reasonNoMedsContextAnalyzer =
          new TokenContextAnalyzer(ContextType.REASON_NOT_ON_MEDS);
    }
    return ReasonNoMedsContextAnalyzerFactory.reasonNoMedsContextAnalyzer;
  }
}
