package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * Stores the lines making up a text document including offset from the top, start offset in terms
 * of number of characters of the first character in the line, the end offset in terms of number of
 * characters of the last character in the line before a return, linefeed, or newline character, the
 * text within the line, and the tokens within the line (stored as an ordered list from first, i.e.,
 * leftmost, to last, i.e., rightmost).
 *
 * Class and methods replaced by methods and class TextDocumentLine
 *
 * @author Glenn Gobbel, created on April 24, 2019
 */
@Deprecated
public class TextDocumentLineArchivedAndDeprecated
    implements Comparable<TextDocumentLineArchivedAndDeprecated> {

  public final int startOffset;
  public final int endOffset;
  public final String lineText;
  private final LinkedList<RaptatToken> tokenList = new LinkedList<>();
  private final Set<RaptatToken> rawTokenSet = new HashSet<>();
  private boolean rawTokenListSorted = false;
  private boolean processedTokenListSorted = false;
  private final List<RaptatToken> rawTokensList = new ArrayList<>();
  private final List<RaptatToken> processedTokensList = new ArrayList<>();
  /*
   * Create both hashsets and lists of tokens since the downstream algorithms may need one or the
   * other. We will create only a setLineTokens method to change what is in these two fields for a
   * bit of safety. For now, we will allow access to the two fields without copying, which exposes
   * them to changes by the users but reduces the time required for access.
   *
   */
  private final LinkedList<RaptatTokenPhrase> tokenPhraseList = new LinkedList<>();

  private final Set<RaptatTokenPhrase> tokenPhraseSet = new HashSet<>();
  private int __line_index;

  @Deprecated
  public TextDocumentLineArchivedAndDeprecated(int startOffset, int endOffset,
      IndexedTree<IndexedObject<RaptatToken>> processedTokensAsTree,
      IndexedTree<IndexedObject<RaptatToken>> rawTokensAsTree) {
    super();
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    Set<IndexedObject<RaptatToken>> processedTokenSet =
        processedTokensAsTree.getObjectsCoveringRange(startOffset, endOffset);
    processedTokenSet.stream()
        .forEach(tokenObject -> this.processedTokensList.add(tokenObject.getIndexedObject()));
    Collections.sort(this.processedTokensList);
    /*
     * Note that the set of "raw" tokens should be a superset of processedTokensList (i.e. the
     * rawTokensList should contain all processed tokens in the processedTokensList), so the code
     * below handles adding this line as the containing line for all tokens in the line.
     */
    Set<IndexedObject<RaptatToken>> rawTokenSet =
        rawTokensAsTree.getObjectsCoveringRange(startOffset, endOffset);
    for (IndexedObject<RaptatToken> tokenAsObject : rawTokenSet) {
      RaptatToken token = tokenAsObject.getIndexedObject();
      this.rawTokensList.add(token);
      // token.set_containing_text_document_line(this);
    }
    Collections.sort(this.rawTokensList);

    this.lineText = RaptatToken.getPreprocessedStringFromTokens(this.rawTokensList);

  }

  /**
   * DAX - 5/7/19
   *
   * <p>
   * Added to compare for use in TreeMap or TreeSet
   */
  @Override
  public int compareTo(TextDocumentLineArchivedAndDeprecated rhs) {
    return this.endOffset - rhs.endOffset;
  }

  public int get_line_index() {
    return this.__line_index;
  }

  public List<RaptatToken> getProcessedTokensList() {
    return this.processedTokensList;
  }

  public List<RaptatToken> getRawTokensList() {
    return this.rawTokensList;
  }

  /** @return the tokenPhraseList */
  public LinkedList<RaptatTokenPhrase> getTokenPhraseList() {
    if (!this.rawTokenListSorted) {
      Collections.sort(this.tokenPhraseList);
      this.rawTokenListSorted = true;
    }
    return this.tokenPhraseList;
  }

  /** @return the tokenPhraseSet */
  public Set<RaptatTokenPhrase> getTokenPhraseSet() {
    return this.tokenPhraseSet;
  }

  public boolean isProcessedTokenListSorted() {
    return this.processedTokenListSorted;
  }

  public boolean isRawTokenListSorted() {
    return this.rawTokenListSorted;
  }

  public boolean isTokenListSorted() {
    return this.rawTokenListSorted;
  }

  public void set_line_index(int line_index) {
    this.__line_index = line_index;
  }

  public void setTokenPhrases(List<RaptatTokenPhrase> inputTokenPhrases) {

    /*
     * TODO - DAX - 5/7/2019
     *
     * Here is where the discussion on phrases that span multiple line should enjoin. Consider a
     * phrase two tokens long which starts on one line and continues on another
     *
     * There is a <blue dog> in the hallway.
     *
     * The idea under review is to make sure that <blue dog> can be used on each line. This likely
     * will involve going back to the TokenPhraseMaker and ensuring that the phrase is picked up
     * while reviewing one long line, then checking phrase start offset against line start offset to
     * ensure on same line, if not then make appropriate adjustments to ensure counted on both.
     */

    this.tokenPhraseList.clear();
    this.tokenList.clear();
    this.rawTokenListSorted = false;

    this.tokenPhraseSet.clear();
    this.rawTokenSet.clear();

    this.tokenPhraseList.addAll(inputTokenPhrases);

    for (RaptatTokenPhrase tokenPhrase : inputTokenPhrases) {
      this.tokenList.addAll(tokenPhrase.getTokensAsList());
    }

    this.tokenPhraseSet.addAll(inputTokenPhrases);
  }

  @Override
  public String toString() {
    return this.lineText;
  }
}
