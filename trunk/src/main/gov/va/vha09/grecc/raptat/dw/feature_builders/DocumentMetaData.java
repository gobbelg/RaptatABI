package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders;

import org.apache.commons.io.FilenameUtils;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;

public class DocumentMetaData {

  private RaptatTokenPhrase documentPhrase;
  private String textDocumentFilePath;
  private String xmlDocumentFilePath;


  public DocumentMetaData(RaptatTokenPhrase document_phrases, String textDocumentFilePath,
      String xmlDocumentFilePath) {
    this.documentPhrase = document_phrases;
    this.textDocumentFilePath = textDocumentFilePath;
    this.xmlDocumentFilePath = xmlDocumentFilePath;
  }


  public String getDocumentId() {
    return FilenameUtils.getName(this.textDocumentFilePath);
  }


  public RaptatTokenPhrase getDocumentPhrase() {
    return this.documentPhrase;

  }

}
