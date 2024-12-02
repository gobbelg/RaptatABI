/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing;

import java.io.Serializable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;

/**
 * Creates common interface for handling annotations after creation and filtering by an Annotator
 * class instance.
 *
 * @author VHATVHGOBBEG
 */
public interface AnnotationGroupPostProcessor extends Serializable {
  public void postProcess(AnnotationGroup annotationGroup);
}
