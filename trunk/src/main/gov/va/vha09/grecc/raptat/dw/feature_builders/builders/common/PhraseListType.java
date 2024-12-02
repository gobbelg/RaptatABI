package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common;

/**
 * An enum to help indicate Row, Column, or Document (all lines as one row of text) for scanning for
 * features.
 */
public enum PhraseListType {

  /** Indicates that the feature building is occurring at a column level **/
  Column("COL_"),
  /** Indicates that the feature building is occurring at a document level **/
  Document("DOC_"),
  /** Indicates that the feature building is occurring at a row level **/
  Row("ROW_");

  /** The prefix string. */
  private String prefixString;


  /**
   * Instantiates a new phrase type.
   *
   * @param prefixString the prefix string
   */
  PhraseListType(String prefixString) {
    this.prefixString = prefixString;
  }


  /**
   * Gets the prefix string.
   *
   * @return the prefix string
   */
  public String getPrefixString() {
    return this.prefixString;
  }
}
