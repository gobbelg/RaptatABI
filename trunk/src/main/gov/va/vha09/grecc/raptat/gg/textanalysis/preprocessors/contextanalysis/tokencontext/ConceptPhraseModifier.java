/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** @author vhatvhsahas1 */
public class ConceptPhraseModifier implements Serializable {
  private static final long serialVersionUID = -2000133615936070747L;
  /*
   * Glenn comment - 10/25/15 - I would make these fields final and public for quick access. Given
   * the way the code is using them, once they are set, I don't believe they will ever be changed
   */
  private final String phrase;
  private final int forwardWindow;
  private final int backwardWindow;


  public ConceptPhraseModifier(String phrase, int forwardWindow, int backwardWindow) {
    this.phrase = phrase;
    this.forwardWindow = forwardWindow;
    this.backwardWindow = backwardWindow;
  }


  public int getBackwardWindow() {
    return this.backwardWindow;
  }


  public String getBlockerPhrase() {
    return this.phrase;
  }


  public int getForwardWindow() {
    return this.forwardWindow;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Phrase", this.phrase)
        .append("ForwardWindow", this.forwardWindow).append("BackwardWindow", this.backwardWindow)
        .toString();
  }
}
