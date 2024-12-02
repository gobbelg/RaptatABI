package src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers;

import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;

public class AssertionUncertaintyConflictManager extends AssertionConflictManager {

  /** */
  private static final long serialVersionUID = 8614673141808081758L;


  @Override
  public RaptatAttribute manageConflicts(Set<RaptatAttribute> attributeSet,
      AnnotatedPhrase annotatedPhrase) {

    String assertionValue = getAssertionValue(attributeSet);

    return null;
  }


  public static void main(String[] args) {
    AssertionUncertaintyConflictManager conflictManager = new AssertionUncertaintyConflictManager();
    System.out.println(conflictManager.attributeName);
  }
}
