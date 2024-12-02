/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenMarker;

/** @author vhatvhsahas1 */
public class UncertaintyTokenMarker extends TokenMarker {

  /** */
  private static final long serialVersionUID = 281303943612931200L;


  public UncertaintyTokenMarker(int window) {
    super();
    this.backwardAnalyzer = new UncertaintyBackwardLogicAnalyzer(window);
    this.forwardAnalyzer = new UncertaintyForwardLogicAnalyzer(window);
  }
}
