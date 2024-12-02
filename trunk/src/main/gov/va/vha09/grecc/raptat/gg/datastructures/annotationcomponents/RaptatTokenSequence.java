/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.util.Set;

/**
 * Interface created to allow document sentences, which are AnnotatedPhrase objects, and the lines
 * of a document, which are TexDocumentLine objects, to be interchanged when identifying sections,
 * such as with the MedListSectionMarker. The interface corresponds to any ordered sequence of
 * tokens.
 *
 * @author VHATVHGOBBEG
 *
 */
abstract public class RaptatTokenSequence {

  public abstract void addSequenceAssociatedConcept(String conceptName);

  public abstract String getRawTokensEndOff();

  public abstract String getRawTokensStartOff();

  public abstract Set<String> getSequenceAssociatedConcepts();

  public abstract String getSequenceStringUnprocessed();
}
