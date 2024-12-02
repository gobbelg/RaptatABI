package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

public abstract class SequenceIDTrainer {

  private static StartOffsetPhraseComparator phraseSorter = new StartOffsetPhraseComparator();
  private static StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();


  /**
   * Creates and adds baseUnlabeledPhrase instance and filteredUnlabeledPhrase instance to an
   * AnnotationGroup provided as a parameter to the method
   *
   * @param trainingGroup
   */
  public void buildAllUnlabeledHashTrees(AnnotationGroup trainingGroup) {
    RaptatDocument raptatDocument = trainingGroup.getRaptatDocument();
    String docTitle = raptatDocument.getTextSourcePath().orElse("NoTextSourcePath");
    int maxTokens = trainingGroup.getMaxTokens();
    List<AnnotatedPhrase> docAnnotations = trainingGroup.referenceAnnotations;

    List<AnnotatedPhrase> baseSentences = raptatDocument.getBaseSentences();
    UnlabeledHashTree baseUnlabeledPhraseTree =
        buildUnlabeledHashTree(docAnnotations, baseSentences, docTitle, maxTokens);
    trainingGroup.setBaseUnlabeledPhrases(baseUnlabeledPhraseTree);

    List<AnnotatedPhrase> filteredSentences;
    UnlabeledHashTree filteredUnlabeledPhraseTree = new UnlabeledHashTree();
    if ((filteredSentences = raptatDocument.getFilteredSentences()) != null) {
      filteredUnlabeledPhraseTree =
          buildUnlabeledHashTree(docAnnotations, filteredSentences, docTitle, maxTokens);
    }
    trainingGroup.setFilteredUnlabeledPhrases(filteredUnlabeledPhraseTree);
  }


  /**
   * s Create an unlabledHashTree for the current document, which will contain all unlabeled phrases
   * up to MAXIMUM_ELEMENTS_DEFAULT in length. The algorithm works by evaluating each token in the
   * document to see whether it is the start of a labeled phrase. By going from the beginning of the
   * document to the end, and by storing the positions of the labeled phrases in a sorted array, we
   * can quickly determine which tokens are at the start of a labeled phrase and which are not. If a
   * token is not the start of a labeled phrase, we can just add the sequence starting with that
   * token to the unlabeledHashTree for the document.
   *
   * <p>
   * Postcondition: An UnlabeledHashTree instance is created that contains all the unlabeled token
   * sequences in the document that are shorter than MAXIMUM_ELEMENTS_DEFAULT in length.
   *
   * @param docAnnotations
   * @param docSentences
   * @param docName
   * @author Glenn Gobbel - Apr 6, 2012
   * @param maxTokens
   * @param useStems2
   * @return
   */
  public UnlabeledHashTree buildUnlabeledHashTree(List<AnnotatedPhrase> docAnnotations,
      List<AnnotatedPhrase> docSentences, String docName, int maxTokens) {
    PhraseIDSmoothingMethod smoothingMethod = OptionsManager.getInstance().getSmoothingMethod();
    UnlabeledHashTree theTree = new UnlabeledHashTree(docName);
    int curAnnotationLength = 0;

    /*
     * Sort annotations according to start offset - this has to be done as it was not done in
     * processing done to this point
     */
    Collections.sort(docAnnotations, SequenceIDTrainer.phraseSorter);

    // Sort sentences according to startOffset of first token - this has to
    // be done as it was
    // not done in processing done to this point
    Collections.sort(docSentences, SequenceIDTrainer.phraseSorter);

    // Get first annotated phrase and its length
    AnnotatedPhrase curAnnotation = null;
    Iterator<AnnotatedPhrase> annotationIterator = docAnnotations.iterator();
    if (annotationIterator.hasNext()) {
      curAnnotation = annotationIterator.next();
      curAnnotationLength = curAnnotation.getProcessedTokens().size();
    }

    // Iterate through sentences and add token sequences in the document to
    // unlabeled hash tree
    // until we reach a token start offset that matches the phrase.
    for (AnnotatedPhrase curSentence : docSentences) {

      // Get sentence tokens and sort them by offset
      List<RaptatToken> sentenceTokens = curSentence.getProcessedTokens();
      Collections.sort(sentenceTokens, SequenceIDTrainer.tokenSorter);

      for (int tokenIndex = 0; tokenIndex < sentenceTokens.size(); tokenIndex++) {
        RaptatToken curToken = sentenceTokens.get(tokenIndex);

        /*
         * If a current annotation exists and the current token overlaps it in anyway then process
         * it as below depending on the type of smoothing we are using. The code and "if" statement
         * actually tests whether the token does *not* "not overlap" (note that the two "nots" are
         * intentional). It examines whether the token starts after the annotation or the token ends
         * before the annotation. If either is true, the token does not overlap. If neither is true,
         * it does overlap.
         */
        if (curAnnotation != null && !(Integer.parseInt(curToken.getStartOffset()) > Integer
            .parseInt(curAnnotation.getProcessedTokensEndOffset())
            || Integer.parseInt(curToken.getEndOffset()) < Integer
                .parseInt(curAnnotation.getProcessedTokensStartOffset()))) {
          /*
           * If we are using "xtreme" smoothing, *none* of the tokens within an annotation are part
           * of an unlabeled phrase. For example, if the sentence was "The big red dog runs fast,"
           * and "red dog" was an annotated phrase, neither "red," nor "red dog," nor
           * "red dog runs", nor "red dog runs fast" would be entered into the unlabeled hash tree
           */
          if (smoothingMethod == PhraseIDSmoothingMethod.XTREME) {
            // tokenIndex += curAnnotation.getRawTokenNumber() - 1;

            /*
             * Changed code from above line on 2/10/13 because the document should consist only of
             * processed tokens, so we should only be considering the number of processed tokens in
             * an annotation, not raw tokens
             */
            tokenIndex += curAnnotation.getProcessedTokens().size() - 1;
          }
          /*
           * If using "extended" or "amplified" phrase identification smoothing, all tokens in a
           * sequence are marked as labeled, so none are considered unlabeled. However, unlike
           * XTREME smoothing, the annotated phrase tokens do become a part of the tree, but they
           * are *not* marked as unlabeled and only phrases that extend beyond the phrase are marked
           * as unlabeled. As above, if the sentence was "The big red dog runs fast," and "red dog"
           * was an annotated phrase, the phrase "red dog runs fast" would be entered, but only the
           * token "runs" and "fast" would be marked as unlabeled. Also, "dog  runs fast" would be
           * entered, but again, only would be entered into the unlabeled hash tree
           */
          else if (smoothingMethod == PhraseIDSmoothingMethod.EXTENDED
              || smoothingMethod == PhraseIDSmoothingMethod.AMPLIFIED) {
            theTree.addUnlabeledSequenceFromIndex(sentenceTokens, tokenIndex,
                tokenIndex + curAnnotationLength, maxTokens);
          }
          /*
           * If using unsmoothed or smoothed phrase identification smoothing, only the last token in
           * the sequence, representing the entire sequence, is considered labeled and therefore is
           * *not* marked as unlabeled.
           */
          else {
            theTree.addUnlabeledSequence(sentenceTokens, tokenIndex,
                tokenIndex + curAnnotationLength - 1, maxTokens);
          }

          if (annotationIterator.hasNext()) {
            curAnnotation = annotationIterator.next();
            curAnnotationLength = curAnnotation.getProcessedTokens().size();
          } else {
            curAnnotation = null;
          }
        } else {
          theTree.addUnlabeledSequence(sentenceTokens, tokenIndex, -1, maxTokens);

          /*
           * If annotation is between two tokens, then scanning through all the tokens in a document
           * can skip by these, which can cause the downstream annotations to not get processed.
           * This handles such a case and moves on to the next annotation
           */
          if (annotationIterator.hasNext() && curAnnotation != null
              && Integer.parseInt(curToken.getStartOffset()) > Integer
                  .parseInt(curAnnotation.getProcessedTokensEndOffset())) {
            curAnnotation = annotationIterator.next();

            curAnnotationLength = curAnnotation.getProcessedTokens().size();
          }
        }
      }
    }
    return theTree;
  }


  public abstract void clearTraining();


  public abstract PhraseIDSmoothingMethod getPhraseIDSmoothingMethod();


  public abstract SequenceIDStructure getSequenceIDStructure();


  public abstract void reversePhraseAndTokenOrder(List<AnnotatedPhrase> sentences);


  public abstract void updatePreviousTraining(AnnotationGroup theGroup);


  public abstract void updateProbabilities();


  public abstract void updateTraining(AnnotationGroup theGroup);
}
