/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * @author glenn
 *         <p>
 *         Sets the context of a sentence, token, or annotated phrase based on any AnnotatedPhrase
 *         instances it may contain or be related to. What is considered "related" depend on how the
 *         interface is implemented.
 */
interface ContextChecker {
  boolean isContextCompliant(AnnotatedPhrase annotatedPhrase);
}
