package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import org.apache.commons.collections4.trie.PatriciaTrie;
import com.mifmif.common.regex.Generex;
import com.mifmif.common.regex.util.Iterator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

/**
 * Like a phrase tree but uses a PatriciaTrie to store strings and uses regular expressions to
 * populate the tree with phrases for matching.
 *
 * @author VHATVHGOBBEG
 * @version - Created Oct 25, 2019
 *
 */
public class RegexPhraseTree extends PatriciaTrie<Mark> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  /**
   * Determines whether phrases put into the tree and compare to the tree should consider character
   * case. This is done to add flexibility to the class. We also have included a method
   * phraseLengthAtIndexIgnoreCase() which speeds up processing by avoiding an "if" test.
   */
  private boolean ignoreCase = true;
  private int maxGeneratedStrings = Integer.MAX_VALUE;


  public RegexPhraseTree() {
    super();
  }


  public RegexPhraseTree(boolean ignoreCase) {
    this();
    this.ignoreCase = ignoreCase;
  }

  /**
   * <b>Method Description</b> - Adds the phrases in the list, thePhrases, to the phrase tree, with
   * the marker, marker, at the end of the String arrays that make up the each phrase in the list
   *
   * @param thePhrases
   * @param marker
   *
   * @author Glenn Gobbel - Jul 17, 2014
   */
  public void addPhrases(String[] thePhrases, Mark marker) {
    for (String curPhrase : thePhrases) {
      Generex generex = new Generex(curPhrase);
      Iterator iterator = generex.iterator();
      int generatedStrings = 0;
      while (iterator.hasNext() && generatedStrings < this.maxGeneratedStrings) {
        String generatedString = iterator.next();
        System.out.println("GeneratedString " + generatedStrings + ":" + generatedString);
        generatedStrings++;
      }
    }
  }


  public static void main(String[] args) {
    RegexPhraseTree rpt = new RegexPhraseTree();
    String[] testRegexes = {"not (us|tak|comply)ing"};
    rpt.addPhrases(testRegexes, null);
  }
}
