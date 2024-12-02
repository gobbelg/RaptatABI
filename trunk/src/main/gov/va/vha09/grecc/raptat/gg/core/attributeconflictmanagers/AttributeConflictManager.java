/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers;

import java.io.Serializable;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;

/**
 * Handles conflicts where two different methods of assigning attributes to an annotated phrase
 * produce conflicting values
 *
 * @author Glenn T. Gobbel Jul 22, 2016
 */
public abstract class AttributeConflictManager implements Serializable {
  /** */
  private static final long serialVersionUID = -1148352761987508649L;

  public final String attributeName;


  public AttributeConflictManager(String attributeName) {
    this.attributeName = attributeName;
  }


  public abstract RaptatAttribute manageConflicts(Set<RaptatAttribute> attributeSet,
      AnnotatedPhrase annotatedPhrase);
}
