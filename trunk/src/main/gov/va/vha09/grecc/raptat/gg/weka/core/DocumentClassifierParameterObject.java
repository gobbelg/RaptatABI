/** */
package src.main.gov.va.vha09.grecc.raptat.gg.weka.core;

import org.apache.commons.lang3.builder.ToStringBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;

/** @author Glenn Gobbel */
public class DocumentClassifierParameterObject {

  final String textDirectoryPath;
  final String mentionLevelAnnotationDirectoryPath;
  final String documentLevelAnnotationDirectoryPath;
  final String mentionLevelAnnotationSchemaPath;
  final String documentLevelAnnotationSchemaPath;
  final String acceptedMentionLevelConceptsPath;
  final String acceptedDocumentLevelConceptsPath;
  final AnnotationApp annotationApp;


  /**
   * @param textDirectoryPath
   * @param mentionLevelAnnotationDirectoryPath
   * @param documentLevelAnnotationDirectoryPath
   * @param mentionLevelAnnotationSchemaPath
   * @param documentLevelAnnotationSchemaPath
   * @param acceptedMentionLevelConcepts
   * @param acceptedDocumentLevelConcepts
   * @param annotationApp
   */
  public DocumentClassifierParameterObject(String textDirectoryPath,
      String mentionLevelAnnotationDirectoryPath, String documentLevelAnnotationDirectoryPath,
      String mentionLevelAnnotationSchemaPath, String documentLevelAnnotationSchemaPath,
      String acceptedMentionLevelConcepts, String acceptedDocumentLevelConcepts,
      AnnotationApp annotationApp) {
    super();
    this.textDirectoryPath = textDirectoryPath;
    this.mentionLevelAnnotationDirectoryPath = mentionLevelAnnotationDirectoryPath;
    this.documentLevelAnnotationDirectoryPath = documentLevelAnnotationDirectoryPath;
    this.mentionLevelAnnotationSchemaPath = mentionLevelAnnotationSchemaPath;
    this.documentLevelAnnotationSchemaPath = documentLevelAnnotationSchemaPath;
    this.acceptedMentionLevelConceptsPath = acceptedMentionLevelConcepts;
    this.acceptedDocumentLevelConceptsPath = acceptedDocumentLevelConcepts;
    this.annotationApp = annotationApp;
  }


  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder.setDefaultStyle(RaptatStringStyle.getInstance());

    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("textDirectoryPath", this.textDirectoryPath)
        .append("mentionLevelAnnotationDirectoryPath", this.mentionLevelAnnotationDirectoryPath)
        .append("documentLevelAnnotationDirectoryPath", this.documentLevelAnnotationDirectoryPath)
        .append("mentionLevelAnnotationSchemaPath", this.mentionLevelAnnotationSchemaPath)
        .append("documentLevelAnnotationSchemaPath", this.documentLevelAnnotationSchemaPath)
        .append("acceptedMentionLevelConceptPath", this.acceptedMentionLevelConceptsPath)
        .append("acceptedDocumentLevelConceptPath", this.acceptedDocumentLevelConceptsPath)
        .append("annotationApp", this.annotationApp);
    return builder.toString();
  }
}
