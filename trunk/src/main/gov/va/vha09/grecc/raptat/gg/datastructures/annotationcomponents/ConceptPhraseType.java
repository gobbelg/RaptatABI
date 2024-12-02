package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Glenn Gobbel
 *         <p>
 *         Stores phrases that are either concept phrases, facilitators, or blockers
 */
public abstract class ConceptPhraseType implements Serializable {
  /** */
  private static final long serialVersionUID = -1807663106385438560L;

  public List<String> phrase;


  public String phraseToString() {
    String[] phraseArray = this.phrase.toArray(new String[0]);
    return Arrays.toString(phraseArray);
  }
}
