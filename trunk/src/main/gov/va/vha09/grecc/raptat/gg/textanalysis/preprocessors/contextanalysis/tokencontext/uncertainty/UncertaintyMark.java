/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.uncertainty;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/** @author vhatvhsahas1 */
public class UncertaintyMark implements Mark {

  public enum ContextMark implements Mark {
    PSEUDO, PRE, TERM, POST, PREPOST
  }

  /** */
  private static final long serialVersionUID = 8248165574971579889L;
}
