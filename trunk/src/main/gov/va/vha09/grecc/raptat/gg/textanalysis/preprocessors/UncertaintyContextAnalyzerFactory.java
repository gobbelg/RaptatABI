/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

/** @author vhatvhsahas1 */
public class UncertaintyContextAnalyzerFactory {

  // private static int negationContextUsers = 0;
  private static TokenContextAnalyzer uncertaintyContextAnalyzer = null;


  public UncertaintyContextAnalyzerFactory() {
    // TODO Auto-generated constructor stub
  }


  public static synchronized TokenContextAnalyzer getInstance() {
    // negationContextUsers++ ;
    if (UncertaintyContextAnalyzerFactory.uncertaintyContextAnalyzer == null) {
      UncertaintyContextAnalyzerFactory.uncertaintyContextAnalyzer =
          new TokenContextAnalyzer(ContextType.UNCERTAINTY);
    }
    return UncertaintyContextAnalyzerFactory.uncertaintyContextAnalyzer;
  }
}
