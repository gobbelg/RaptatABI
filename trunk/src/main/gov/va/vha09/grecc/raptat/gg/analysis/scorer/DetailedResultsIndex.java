package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

// @formatter:off
enum DetailedResultsIndex {
  DOC("Document"),
  CONCEPT_MATCH("Concept Match"),
  PARTIAL_MATCH("Partial Phrase Match"),
  FULL_MATCH("Full Phrase Match"),
  TRUE_POSITIVE_PARTIAL("True Positive(Partial Match)"),
  FALSE_POSITIVE_PARTIAL("False Positive(Partial Match)"),
  FALSE_NEGATIVE_PARTIAL("False Negative(Partial Match"),
  TRUE_POSITIVE_FULL("True Positive(Full Match)"),
  FALSE_POSITIVE_FULL("False Positive(Full Match)"),
  FALSE_NEGATIVE_FULL("False Negative(Full Match)"),
  TRUE_POSITIVE_ATTRIBUTE_VALUE("True Positive Attribute Value"),
  FALSE_POSITIVE_ATTRIBUTE_VALUE("False PositiveAttribute Value"),
  FALSE_NEGATIVE_ATTRIBUTE_VALUE("False Negative Attribute Value"),
  TRUE_POSITIVE_RELATIONSHIP("True Positive Relationship Name"),
  FALSE_POSITIVE_RELATIONSHIP("False Positive Relationship Name"),
  FALSE_NEGATIVE_RELATIONSHIP("False Negative Relationship Name"),
  TRUE_POSITIVE_RELATIONSHIP_ATTRIBUTE_VALUE("True Positive Relationship Attribute Value"),
  FALSE_POSITIVE_RELATIONSHIP_ATTRIBUTE_VALUE("False Positive Relationship Attribute Value"),
  FALSE_NEGATIVE_RELATIONSHIP_ATTRIBUTE_VALUE("False Negative Relationship Attribute Value"),
  RAPTAT_MENTION_ID("Raptat MentionID"),
  RAPTAT_START("Raptat Phrase Offset"),
  RAPTAT_PHRASE("Raptat Phrase"),
  RAPTAT_CONCEPT("Rapate Concept"),
  RAPTAT_ATTRIBUTE("Raptat Attribute"),
  RAPTAT_ATTRIBUTE_VALUE("Raptat Attribute Value"),
  RAPTAT_RELATIONSHIP_NAME("Raptat Relationship"),
  RAPTAT_RELATIONSHIP_LINKED_TO_CONCEPT("Raptat Relationship Linked To Concept"),
  RAPTAT_RELATIONSHIP_LINKED_TO_MENTIONID("Raptat Relationship Linked To MentionID"),
  RAPTAT_RELATIONSHIP_ATTRIBUTE("Raptat Relationship Attribute"),
  RAPTAT_RELATIONSHIP_ATTRIBUTE_VALUE("Raptat Relationship Attribute Value"),
  REFERENCE_MENTION_ID("Reference Mention ID"),
  REFERENCE_START("Reference Offset"),
  REFERENCE_PHRASE("Reference Phrase"),
  REFERENCE_CONCEPT("Reference Concept"),
  REFERENCE_ATTRIBUTE("Reference Attribute"),
  REFERENCE_ATTRIBUTE_VALUE("Reference Attribute Value"),
  REFERENCE_RELATIONSHIP_NAME("Reference Relationship"),
  REFERENCE_RELATIONSHIP_LINKED_TO_CONCEPT("Reference Relationship Linked to Concept"),
  REFERENCE_RELATIONSHIP_LINKED_TO_MENTIONID("Reference Relationship Linked To MentionID"),
  REFERENCE_RELATIONSHIP_ATTRIBUTE("Reference Relationship Attribute"),
  REFERENCE_RELATIONSHIP_ATTRIBUTE_VALUE("Reference Relationship Attribute Value"),
  SENTENCE("Sentence Containing Phrase");

  private String stringRepresentation;

  private DetailedResultsIndex() {
    this.stringRepresentation = "UNDEFINED";
  }

  private DetailedResultsIndex(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  @Override
  public String toString() {
    return this.stringRepresentation;
  }
}
// @formatter:on
