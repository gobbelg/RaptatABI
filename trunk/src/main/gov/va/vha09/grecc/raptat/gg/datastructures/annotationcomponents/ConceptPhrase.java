/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Stores the information of Context phrases.
 *
 * @author vhatvhsahas1
 */
public class ConceptPhrase extends ConceptPhraseType implements Serializable {
  /** */
  private static final long serialVersionUID = 6387245961155967093L;
  /*
   * Glenn comment - 10/22/15 - To better show what these fields mean, better names might be
   * 'requiresFacilitation' and 'blockable'. Also, if they are made final, and public, they can be
   * retrieved quickly with conceptPhrase.blockable rather than a call to the method
   */
  public final boolean requiresFacilitation;
  public final boolean blockable;

  /*
   * Indicates whether the specific concept phrase if found and properly facilitated and not blocked
   * should override any overlapping annotations by probabilistic methods within Raptat
   */
  public final boolean overridesRaptat;


  public ConceptPhrase(List<String> phraseAsList, boolean facilitated, boolean blocked,
      boolean overridesRaptat) {
    this.phrase = phraseAsList;
    this.requiresFacilitation = facilitated;
    this.blockable = blocked;
    this.overridesRaptat = overridesRaptat;
  }


  public ConceptPhrase(String phrase, boolean facilitated, boolean blocked,
      boolean overridesRaptat) {
    String[] stringArray = phrase.toLowerCase().trim().split("\\s+");
    this.phrase = new ArrayList<>(stringArray.length);

    for (String tokenString : stringArray) {
      this.phrase.add(tokenString);
    }

    this.requiresFacilitation = facilitated;
    this.blockable = blocked;
    this.overridesRaptat = overridesRaptat;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Phrase", phraseToString())
        .append("RequiresFacilitation", this.requiresFacilitation)
        .append("Blockable", this.blockable).append("OverridesRaptat", this.overridesRaptat)
        .toString();
  }
}
