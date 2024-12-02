/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Stores the information of Context phrases.
 *
 * @author vhatvhsahas1
 */
public class ConceptPhrase {
  private String phrase;

  /*
   * Glenn comment - 10/22/15 - To better show what these fields mean, better names might be
   * 'requiresFacilitation' and 'blockable'. Also, if they are made final, and public, they can be
   * retrieved quickly with conceptPhrase.blockable rather than a call to the method
   */
  private boolean requiresFacilitation;
  private boolean blockable;


  public ConceptPhrase(String phrase, boolean facilitated, boolean blocked) {
    this.phrase = phrase;
    this.requiresFacilitation = facilitated;
    this.blockable = blocked;
  }


  public String getPhrase() {
    return this.phrase;
  }


  public boolean isBlockable() {
    return this.blockable;
  }


  public boolean requiresFacilitation() {
    return this.requiresFacilitation;
  }


  public void setBlockable(boolean blocked) {
    this.blockable = blocked;
  }


  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }


  public void setRequiresFacilitation(boolean facilitated) {
    this.requiresFacilitation = facilitated;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Phrase", this.phrase)
        .append("RequiresFacilitation", this.requiresFacilitation)
        .append("Blockable", this.blockable).toString();
  }
}
