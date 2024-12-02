package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

public class NegationContextAnalyzerFactory {

  // private static int negationContextUsers = 0;
  private static TokenContextAnalyzer negationsContextAnalyzer = null;


  public NegationContextAnalyzerFactory() {
    // TODO Auto-generated constructor stub
  }


  public static synchronized TokenContextAnalyzer getInstance() {
    // negationContextUsers++ ;
    if (NegationContextAnalyzerFactory.negationsContextAnalyzer == null) {
      NegationContextAnalyzerFactory.negationsContextAnalyzer =
          new TokenContextAnalyzer(ContextType.NEGATION);
    }
    return NegationContextAnalyzerFactory.negationsContextAnalyzer;
  }

  // public static void release()
  // {
  // negationContextUsers-- ;
  // if ( negationContextUsers < 1 )
  // {
  // negationContextUsers = 0;
  // negationsContextAnalyzer = null;
  // }
  // }

}
