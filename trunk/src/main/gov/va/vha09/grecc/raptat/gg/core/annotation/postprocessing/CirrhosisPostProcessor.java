/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing;

import java.util.ListIterator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;

/**
 * Takes annotations from an AnnotationGroup, and any that are assigned the concept 05_massfocalarea
 * and contain the word stem "cyst" are also assigned to the concept 20_cancerdetermination.
 * Similarly, 20_cancerdetermination concepts with the word "cyst" in them are assigned to
 * 05_massfocalarea.
 *
 * @author VHATVHGOBBEG
 */
public class CirrhosisPostProcessor implements AnnotationGroupPostProcessor {
  private static final long serialVersionUID = -3373981385241259288L;
  public static final String CONCEPT_01 = "05_massfocalarea";
  public static final String CONCEPT_02 = "20_cancerdetermination";
  public static final String CONCEPT_03 = "00_null_02";


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing.
   * AnnotationPostProcessor#postProcess(src.main.gov.va.vha09.grecc.raptat. gg.
   * datastructures.AnnotationGroup)
   */
  @Override
  public void postProcess(AnnotationGroup annotationGroup) {
    ListIterator<AnnotatedPhrase> phraseIterator = annotationGroup.raptatAnnotations.listIterator();
    while (phraseIterator.hasNext()) {
      AnnotatedPhrase annotatedPhrase = phraseIterator.next();
      String conceptName = annotatedPhrase.getConceptName();
      String conceptPhrase = annotatedPhrase.getProcessedTokensAsString().toLowerCase();

      /*
       * For this task, we want phrases with "cyst" or "hemangioma" in them to be assigned as both
       * "massfocalarea" and "cancerdetermination" concepts
       */
      if ((conceptName.contains(CirrhosisPostProcessor.CONCEPT_01)
          || conceptName.contains(CirrhosisPostProcessor.CONCEPT_02))
          && (conceptPhrase.contains("cyst") || conceptPhrase.contains("hemangioma"))) {
        AnnotatedPhrase newPhrase = new AnnotatedPhrase(annotatedPhrase);
        if (conceptName.contains(CirrhosisPostProcessor.CONCEPT_01)) {
          newPhrase.setConceptName(CirrhosisPostProcessor.CONCEPT_02);
        } else {
          newPhrase.setConceptName(CirrhosisPostProcessor.CONCEPT_01);
        }
        phraseIterator.add(newPhrase);
      }

      /*
       * This 'if' reassigns phrases like "hcc" and "hepatocellular carcinoma" to the concept
       * "cancerdetermination." Such phrases are first assigned to a null concept so they are not
       * removed during the preceding context sifting process.
       */
      if (conceptName.contains(CirrhosisPostProcessor.CONCEPT_03)) {
        annotatedPhrase.setConceptName(CirrhosisPostProcessor.CONCEPT_02);
      }
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
}
