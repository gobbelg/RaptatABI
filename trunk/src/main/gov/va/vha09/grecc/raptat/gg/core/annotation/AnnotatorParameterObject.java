/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.annotation;

import org.apache.commons.lang3.builder.ToStringBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;

/** @author Glenn Gobbel */
public class AnnotatorParameterObject {
  protected final String solutionFilePath;
  final String textDirectoryPath;
  final String exportDirectoryPath;
  final String conceptsFilePath;
  final AnnotationApp annotationApp;
  final boolean isBatchDirectory;


  public AnnotatorParameterObject(String solutionFilePath, String textDirectoryPath,
      boolean isBatchDirectory, String exportDirectoryPath, String conceptsFilePath,
      AnnotationApp annotationApp) {
    super();
    this.solutionFilePath = solutionFilePath;
    this.textDirectoryPath = textDirectoryPath;
    this.isBatchDirectory = isBatchDirectory;
    this.exportDirectoryPath = exportDirectoryPath;
    this.conceptsFilePath = conceptsFilePath;
    this.annotationApp = annotationApp;
  }


  @Override
  public String toString() {
    ToStringBuilder.setDefaultStyle(RaptatStringStyle.getInstance());

    ToStringBuilder sb = new ToStringBuilder(this).append("solutionFilePath", this.solutionFilePath)
        .append("textDirectoryPath", this.textDirectoryPath)
        .append("isBatchDirectory", this.isBatchDirectory)
        .append("exportDirectoryPath", this.exportDirectoryPath)
        .append("conceptsFilePath", this.conceptsFilePath)
        .append("annotationApp", this.annotationApp);

    return sb.toString();
  }
}
