/** */
package src.main.gov.va.vha09.grecc.raptat.gg.sql;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;

/** @author Glenn Gobbel */
public class AnnotatedPhraseFields {
  public final String annotatorName;
  public final String annotatorID;
  public final String conceptName;
  public final String mentionID;
  public final String startOffset;
  public final String endOffset;
  public final String phraseText;
  public final List<ConceptRelation> conceptRelations;
  public final List<RaptatAttribute> attributes;
  public final String creationDate;


  /**
   * Constructor created for Virani Statin project to allow creation of annotated phrase without all
   * the fields present
   */
  public AnnotatedPhraseFields(String conceptName, String startOffset, String endOffset,
      String phraseText) {
    this("", "", conceptName, "", startOffset, endOffset, phraseText,
        new ArrayList<ConceptRelation>(), new ArrayList<RaptatAttribute>(), "");
  }


  public AnnotatedPhraseFields(String annotatorName, String annotatorID, String conceptName,
      String mentionID, String startOffset, String endOffset, String phraseText,
      List<ConceptRelation> conceptRelations, List<RaptatAttribute> attributes,
      String creationDate) {
    super();
    this.annotatorName = annotatorName;
    this.annotatorID = annotatorID;
    this.conceptName = conceptName;
    this.mentionID = mentionID;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.phraseText = phraseText;
    this.conceptRelations = conceptRelations;
    this.attributes = attributes;
    this.creationDate = creationDate;
  }
}
