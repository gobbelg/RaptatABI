/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated.RaptatSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Classes implementing this interface should be able to take an AnnotatedPhrase, such as a
 * sentence, or a list of AnnotatedPhrase instances and return the AnnotatedPhrase instances that
 * should be annotated according to an instance of a {@link RaptatSolution}.
 *
 * @author Glenn T. Gobbel Feb 13, 2016
 */
public abstract class TokenSequenceFinder {
  protected static StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();
  protected HashSet<String> acceptedConcepts;
  private final UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;


  public abstract List<AnnotatedPhrase> identifySequences(RaptatDocument processedDocument);


  /**
   * @param annotations
   * @param proposedAnnotations
   */
  public abstract void setMappedAttributes(List<AnnotatedPhrase> annotations);


  /**
   * @param raptatSequences
   * @return The sequences annotated with concepts, returned as a list of annotated phrases.
   * @author Glenn Gobbel - Jun 16, 2012
   * @param invertTokenSequence
   */
  protected List<AnnotatedPhrase> convertToAnnotations(
      List<RaptatPair<List<RaptatToken>, String>> phrasesAndConcepts, boolean invertTokenSequence) {
    String annotationData[] = new String[6];
    List<AnnotatedPhrase> theResults = new ArrayList<>(phrasesAndConcepts.size());

    if (invertTokenSequence) {
      for (RaptatPair<List<RaptatToken>, String> phraseAndConcept : phrasesAndConcepts) {
        List<RaptatToken> tokenSequence = phraseAndConcept.left;
        RaptatToken.markRaptatConceptAssigned(tokenSequence, true);
        Collections.sort(tokenSequence, TokenSequenceFinder.tokenSorter);
        annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = this.idGenerator.getUnique();
        annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] =
            tokenSequence.get(0).getStartOffset();
        annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] =
            tokenSequence.get(tokenSequence.size() - 1).getEndOffset();
        annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = phraseAndConcept.right;
        theResults.add(new AnnotatedPhrase(annotationData, tokenSequence));
      }
    } else {
      for (RaptatPair<List<RaptatToken>, String> phraseAndConcept : phrasesAndConcepts) {
        List<RaptatToken> tokenSequence = phraseAndConcept.left;
        RaptatToken.markRaptatConceptAssigned(tokenSequence, true);
        annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = this.idGenerator.getUnique();
        annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] =
            tokenSequence.get(0).getStartOffset();
        annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] =
            tokenSequence.get(tokenSequence.size() - 1).getEndOffset();
        annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = phraseAndConcept.right;
        theResults.add(new AnnotatedPhrase(annotationData, tokenSequence));
      }
    }
    return theResults;
  }
}
