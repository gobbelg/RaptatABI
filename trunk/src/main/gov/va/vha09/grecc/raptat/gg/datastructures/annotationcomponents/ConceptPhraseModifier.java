package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** @author vhatvhsahas1 */
public class ConceptPhraseModifier extends ConceptPhraseType implements Serializable {
  private static final long serialVersionUID = -2000133615936070747L;
  public final int forwardWindow;

  public final int backwardWindow;


  public ConceptPhraseModifier(List<String> phraseAsList, int backwardWindow, int forwardWindow) {
    this.phrase = phraseAsList;
    this.backwardWindow = backwardWindow;
    this.forwardWindow = forwardWindow;
  }


  public ConceptPhraseModifier(String phrase, int backwardWindow, int forwardWindow) {
    String[] stringArray = phrase.toLowerCase().trim().split("\\s+");
    this.phrase = new ArrayList<>(stringArray.length);

    for (String tokenString : stringArray) {
      this.phrase.add(tokenString);
    }
    this.backwardWindow = backwardWindow;
    this.forwardWindow = forwardWindow;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Phrase", phraseToString())
        .append("ForwardWindow", this.forwardWindow).append("BackwardWindow", this.backwardWindow)
        .toString();
  }
}
