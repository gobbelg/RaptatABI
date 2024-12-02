package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree.IndexedTreeIterator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Stores the lines making up a text document including offset from the top, start offset in terms
 * of number of characters of the first character in the line, the end offset in terms of number of
 * characters of the last character in the line before a return, linefeed, or newline character, the
 * text within the line, and the tokens within the line (stored as an ordered list from first, i.e.,
 * leftmost, to last, i.e., rightmost).
 *
 * @author Glenn Gobbel, created on April 24, 2019
 */
public class TextDocumentLine extends RaptatTokenSequence implements Comparable<TextDocumentLine> {

  public static final int EMPTY_LINE_INCREMENT = 2;
  public final int startOffset;
  public final int endOffset;
  public final String lineText;
  private final LinkedList<RaptatToken> tokenList = new LinkedList<>();
  private final Set<RaptatToken> rawTokenSet = new HashSet<>();
  private boolean rawTokenListSorted = false;
  private final boolean processedTokenListSorted = false;
  private final List<RaptatToken> rawTokensList = new ArrayList<>();
  private final List<RaptatToken> processedTokensList = new ArrayList<>();
  private int[] columnOffsets;

  /*
   * Create both hashsets and lists of tokens since the downstream algorithms may need one or the
   * other. We will create only a setLineTokens method to change what is in these two fields for a
   * bit of safety. For now, we will allow access to the two fields without copying, which exposes
   * them to changes by the users but reduces the time required for access.
   *
   */
  private final List<RaptatTokenPhrase> tokenPhraseList = new ArrayList<>();

  /*
   * Used for marking sentences, which are generally stored in documents as annotated phrases, with
   * any concepts that may occur in the sentence. The concept maps to the number of times the
   * concept occurs. If the number of times the concept is included is not accurate, the mapped
   * value should be set to -1
   */
  private final HashMap<String, Integer> sequenceAssociatedConcepts = new HashMap<>(8);

  private final Set<RaptatTokenPhrase> tokenPhraseSet = new HashSet<>();
  private int lineIndex;
  private final boolean isBlank;
  private TextDocumentLine priorLine = null;

  private TextDocumentLine nextLine = null;
  public final RaptatDocument sourceDocument;

  /**
   * This method allows construction of a TextLineDocument instance without getting the tokens for
   * the line (processedTokensAsTree and rawTokensAsTree can both be set to null when generating an
   * instance)
   *
   * @param startOffset
   * @param endOffset
   * @param lineText
   * @param processedTokenIterator
   * @param rawTokenIterator
   * @param sourceDocument
   * @param getLineTokens
   */
  public TextDocumentLine(final int startOffset, final int endOffset, final String lineText,
      final IndexedTreeIterator processedTokenIterator, final IndexedTreeIterator rawTokenIterator,
      final TextDocumentLine priorLine, final RaptatDocument sourceDocument) {
    super();
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.priorLine = priorLine;
    this.sourceDocument = sourceDocument;
    if (priorLine != null) {
      priorLine.nextLine = this;
    }

    this.isBlank = lineText.matches("^\\s*$");

    if (processedTokenIterator != null && rawTokenIterator != null && !this.isBlank) {

      assignTokensToLine(this.processedTokensList, startOffset, endOffset, processedTokenIterator);
      assignTokensToLine(this.rawTokensList, startOffset, endOffset, rawTokenIterator);

      /*
       * Every RaptatToken assigned to this line should store a reference to the line in its
       * documentLines field. (A token can span over multiple lines).
       */
      this.rawTokensList.forEach(token -> token.addDocumentLine(this));
    }

    this.lineText = lineText;
  }

  @Override
  public void addSequenceAssociatedConcept(final String conceptName) {
    Integer conceptOccurrences;
    if ((conceptOccurrences = this.sequenceAssociatedConcepts.get(conceptName)) == null) {
      conceptOccurrences = 0;
    }
    this.sequenceAssociatedConcepts.put(conceptName, ++conceptOccurrences);
  }

  /**
   * DAX - 5/7/19
   *
   * <p>
   * Added to compare for use in TreeMap or TreeSet
   */
  @Override
  public int compareTo(final TextDocumentLine rhs) {
    return this.endOffset - rhs.endOffset;
  }

  /**
   * @return the columnOffsets
   */
  public int[] getColumnOffsets() {
    return this.columnOffsets;
  }

  public int getLineIndex() {
    return this.lineIndex;
  }

  public TextDocumentLine getNextLine() {
    return this.nextLine;
  }

  public TextDocumentLine getPriorLine() {
    return this.priorLine;
  }

  public List<RaptatToken> getProcessedTokensList() {
    return this.processedTokensList;
  }

  @Override
  public String getRawTokensEndOff() {
    return Integer.toString(this.endOffset);
  }

  public List<RaptatToken> getRawTokensList() {
    return this.rawTokensList;
  }

  @Override
  public String getRawTokensStartOff() {
    return Integer.toString(this.startOffset);
  }

  @Override
  public Set<String> getSequenceAssociatedConcepts() {
    return this.sequenceAssociatedConcepts.keySet();
  }

  @Override
  public String getSequenceStringUnprocessed() {
    return this.lineText;
  }

  /** @return the tokenPhraseList */
  public List<RaptatTokenPhrase> getTokenPhraseList() {
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

  public void set_line_index(final int line_index) {
    this.lineIndex = line_index;
  }



  /**
   * Set the offset in phrases to each column crossing this TextDocumentLine, which is all the
   * columns in the document. Any associated with a phrase are set to the start offset of the
   * phrase. Any not crossing a phrase are set to the end offset of the preceding phrase.
   *
   * @param columnsInDocument
   */
  public void setColumnOffsetsAlongLine(final int columnsInDocument) {

    this.columnOffsets = new int[columnsInDocument];
    int columnIndex = 0;
    int endOffset = 0;
    List<RaptatTokenPhrase> phrases = getTokenPhraseList();

    for (RaptatTokenPhrase phrase : phrases) {
      List<Integer> columnIndices = phrase.getColumnIndices();
      int startColumnIndex = columnIndices.get(0);
      int startOffset = phrase.getLineStartPhraseOffset();
      while (columnIndex < startColumnIndex) {
        this.columnOffsets[columnIndex++] = endOffset;
      }
      int endColumnIndex = columnIndices.get(columnIndices.size() - 1);
      while (columnIndex <= endColumnIndex) {
        this.columnOffsets[columnIndex++] = startOffset;
      }
      endOffset = phrase.getLineEndPhraseOffset();
    }
    while (columnIndex < columnsInDocument) {
      this.columnOffsets[columnIndex++] = endOffset;
    }
  }

  public void setTokenPhrases(final List<RaptatTokenPhrase> inputTokenPhrases) {

    this.tokenPhraseList.clear();
    this.tokenList.clear();
    this.rawTokenListSorted = false;

    this.tokenPhraseSet.clear();
    this.rawTokenSet.clear();

    this.tokenPhraseList.addAll(inputTokenPhrases);

    for (final RaptatTokenPhrase tokenPhrase : inputTokenPhrases) {
      this.tokenList.addAll(tokenPhrase.getTokensAsList());
    }

    this.tokenPhraseSet.addAll(inputTokenPhrases);
  }

  @Override
  public String toString() {
    return this.lineText;
  }

  /**
   * @param startOffset
   * @param endOffset
   * @param tokenIterator
   */
  private void assignTokensToLine(final List<RaptatToken> tokenList, final int startOffset,
      final int endOffset, final IndexedTreeIterator tokenIterator) {
    /*
     * Make sure the start offset of the tokens is equal to or greater than the startOfffset of the
     * line. RaptatToken instances that have a startOffset less than the startOffset of the line
     * should be added to one of the previous lines. Note that this means that RaptatToken instance
     * that occurs on two lines will only be added to the first line.
     */
    while (tokenIterator.hasNext() && tokenIterator.peek().getNode().getIndex() < startOffset) {
      tokenIterator.next();
    }
    while (tokenIterator.hasNext() && tokenIterator.peek().getNode().getIndex() < endOffset) {
      tokenList.add((RaptatToken) tokenIterator.next().getNode().getIndexedObject());
    }
  }
}
