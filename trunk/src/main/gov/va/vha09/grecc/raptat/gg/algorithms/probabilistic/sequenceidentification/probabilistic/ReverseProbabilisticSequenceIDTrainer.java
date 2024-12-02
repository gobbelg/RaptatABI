/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic;

import java.util.Iterator;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeInputStream;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeOutputStream;

/**
 * ****************************************************** This is a subclass of TokenSequnceID. This
 * class is designed to carry out its training using reverse ordering of the tokens in an annotated
 * sequence to build a LabeledHashTree, so tokens at the end of the annotated sequence are the
 * first, rather than the last, elements in the LabeledHashTree instance.
 *
 * @author Glenn Gobbel - Jun 15, 2012 *****************************************************
 */
public class ReverseProbabilisticSequenceIDTrainer extends ProbabilisticSequenceIDTrainer {
  /**
   * ********************************************
   *
   * @param theLabeledTree
   * @param conceptMapSolution
   * @param updatedUHTInstream
   * @param updatedUHTOutstream
   * @author Glenn Gobbel - Jun 15, 2012
   * @param phraseIDSmoothing
   * @param previousUHTSerialPath
   * @param previousUHTSerialPath2 ********************************************
   */
  public ReverseProbabilisticSequenceIDTrainer(LabeledHashTree theTree,
      PhraseIDSmoothingMethod smoothingMethod, UnlabeledHashTreeInputStream uhtInstream,
      UnlabeledHashTreeOutputStream uhtOutstream, String newUHTSerialPath,
      String previousUHTSerialPath) {
    super(theTree, smoothingMethod, uhtInstream, uhtOutstream, newUHTSerialPath,
        previousUHTSerialPath);
  }


  /**
   * @param uhtPath
   * @param useDiskForUnlabeledHashTree
   * @param smoothingMethod
   * @author Glenn Gobbel - Jun 15, 2012
   */
  public ReverseProbabilisticSequenceIDTrainer(String uhtPath, boolean useDiskForUnlabeledHashTree,
      PhraseIDSmoothingMethod smoothingMethod) {
    super(uhtPath, useDiskForUnlabeledHashTree, smoothingMethod);
  }


  /**
   * *********************************************************** Create an unlabledHashTree for the
   * current document, which will contain all unlabeled phrases up to MAXIMUM_ELEMENTS_DEFAULT in
   * length. Everything is stored in reverse order. The algorithm works by evaluating each token in
   * the document to see whether it is the end of a labeled phrase. By going from the end of the
   * document to the beginning, and by storing the positions of the labeled phrases in a sorted
   * array, we can quickly determine which tokens are at the end of a labeled phrase and which are
   * not. If a token is not the end of a labeled phrase, we can just add the sequence ending with
   * that token to the unlabeledHashTree for the document.
   *
   * <p>
   * Precondition: The documentAnnotations are sorted in reverse order with regard to startOffset as
   * well as their tokens. Postcondition: An UnlabeledHashTree instance is created that contains all
   * the unlabeled token sequences in the document that are shorter than MAXIMUM_ELEMENTS_DEFAULT in
   * length.
   *
   * @param documentAnnotations
   * @param docSentences
   * @param documentName
   * @author Glenn Gobbel - Apr 6, 2012
   * @param useStems2 ***********************************************************
   */
  @Override
  public UnlabeledHashTree buildUnlabeledHashTree(List<AnnotatedPhrase> documentAnnotations,
      List<AnnotatedPhrase> docSentences, String documentName, int maxTokens) {
    UnlabeledHashTree theTree = new UnlabeledHashTree(documentName);
    int curAnnotationLength = 0;

    // Sort sentences in reverse order according to startOffset of first
    // token
    reversePhraseAndTokenOrder(docSentences);

    // Iterate through phrases and add token sequences in the document
    // to unlabeled hash tree until we reach a token end offset
    // that matches the phrase.
    AnnotatedPhrase curAnnotation = null;
    Iterator<AnnotatedPhrase> annotationIterator = documentAnnotations.iterator();
    if (annotationIterator.hasNext()) {
      curAnnotation = annotationIterator.next();
      curAnnotationLength = curAnnotation.getProcessedTokens().size();
    }

    for (AnnotatedPhrase curSentence : docSentences) {
      // Get sentence tokens
      List<RaptatToken> sentenceTokens = curSentence.getProcessedTokens();
      int curTokenPosition = 0;

      for (RaptatToken curToken : sentenceTokens) {
        if (curAnnotation != null
            && curToken.getEndOffset().equals(curAnnotation.getProcessedTokensEndOffset())) {
          if (this.smoothingMethod == PhraseIDSmoothingMethod.EXTENDED
              || this.smoothingMethod == PhraseIDSmoothingMethod.AMPLIFIED
              || this.smoothingMethod == PhraseIDSmoothingMethod.XTREME) {
            theTree.addUnlabeledSequenceFromIndex(sentenceTokens, curTokenPosition,
                curTokenPosition + curAnnotationLength, maxTokens);
          } else {
            theTree.addUnlabeledSequence(sentenceTokens, curTokenPosition,
                curTokenPosition + curAnnotationLength - 1, maxTokens);
          }

          curTokenPosition++;
          if (annotationIterator.hasNext()) {
            curAnnotation = annotationIterator.next();
            curAnnotationLength = curAnnotation.getProcessedTokens().size();
          } else {
            curAnnotation = null;
          }
        } else {
          theTree.addUnlabeledSequence(sentenceTokens, curTokenPosition, -1, maxTokens);
          curTokenPosition++;
          // If annotation only encompasses part of a word, the
          // scanning through all the words in a
          // document can skip by annotations that have an end offset
          // never equal to any token. This
          // handles such a case.
          if (annotationIterator.hasNext() && curAnnotation != null
              && Integer.parseInt(curToken.getEndOffset()) < Integer
                  .parseInt(curAnnotation.getProcessedTokensEndOffset())) {
            curAnnotation = annotationIterator.next();
            curAnnotationLength = curAnnotation.getProcessedTokens().size();
          }
        }
      }
    }
    return theTree;
  }


  @Override
  public void updateTraining(AnnotationGroup annotationGroup) {

    updatePrimaryTree(annotationGroup.referenceAnnotations);
    this.unlabeledHashTreeOutstream.append(annotationGroup.getActiveUnlabeledPhrases());
  }
}
