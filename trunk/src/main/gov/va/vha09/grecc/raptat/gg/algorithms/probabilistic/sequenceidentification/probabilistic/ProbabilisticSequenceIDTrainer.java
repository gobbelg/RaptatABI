package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.ExtendedLabeledTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.HashTreeInStreamer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.HashTreeOutStreamer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.IterableHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledSubsequenceHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.ModifiedSubsequenceHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeInputStream;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeOutputStream;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeStreamer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnsmoothedLabeledTree;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ReverseStartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.ReverseStartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * ******************************************************* Class uses a datastructure known as a
 * LabeledHashTree to store the likelihood that a given sequence will be annotated. This class is
 * responsible for building the LabeledHashTree for a set of documents.
 *
 * @author Glenn Gobbel - Apr 6, 2012 *******************************************************
 */
public class ProbabilisticSequenceIDTrainer extends SequenceIDTrainer {
  protected LabeledHashTree primaryLabeledTree =
      new LabeledHashTree(RaptatConstants.LABELED_HASH_TREE_SIZE_DEPTH_0_DEFAULT, 0);

  protected List<AnnotatedPhrase> allPhrasesObj;
  protected HashTreeInStreamer<UnlabeledHashTree> unlabeledHashTreeInstream;
  protected HashTreeOutStreamer<UnlabeledHashTree> unlabeledHashTreeOutstream;
  protected String uhtPath;
  protected PhraseIDSmoothingMethod smoothingMethod = PhraseIDSmoothingMethod.UNSMOOTHED;
  private static Logger LOGGER = Logger.getLogger(ProbabilisticSequenceIDTrainer.class);
  private static ReverseStartOffsetPhraseComparator reversePhraseSorter =
      new ReverseStartOffsetPhraseComparator();
  private static ReverseStartOffsetTokenComparator reverseTokenSorter =
      new ReverseStartOffsetTokenComparator();
  private static StartOffsetPhraseComparator phraseSorter = new StartOffsetPhraseComparator();
  private static StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();


  @SuppressWarnings("unchecked")
  public ProbabilisticSequenceIDTrainer(LabeledHashTree primaryTree,
      PhraseIDSmoothingMethod smoothingMethod, UnlabeledHashTreeInputStream uhtInstream,
      UnlabeledHashTreeOutputStream uhtOutstream, String newUHTPath, String previousUHTPath) {
    this();
    this.smoothingMethod = smoothingMethod;
    this.primaryLabeledTree = primaryTree;

    /*
     * uhtinstream and uhtOutstream will be null if we are not keeping the unlabeled hash trees on
     * disk during training
     */
    if (uhtInstream == null || uhtOutstream == null) {
      this.unlabeledHashTreeOutstream = new UnlabeledHashTreeStreamer(newUHTPath, previousUHTPath);
      this.unlabeledHashTreeInstream =
          (HashTreeInStreamer<UnlabeledHashTree>) this.unlabeledHashTreeOutstream;

    } else {
      this.unlabeledHashTreeInstream = uhtInstream;
      this.unlabeledHashTreeOutstream = uhtOutstream;
    }
  }


  @SuppressWarnings("unchecked")
  public ProbabilisticSequenceIDTrainer(String newUHTPath, boolean keepUnlabeledOnDisk,
      PhraseIDSmoothingMethod smoothingMethod) {
    this(smoothingMethod);

    this.uhtPath = newUHTPath;
    this.smoothingMethod = smoothingMethod;
    if (keepUnlabeledOnDisk) {
      this.unlabeledHashTreeOutstream = new UnlabeledHashTreeOutputStream(newUHTPath);
      this.unlabeledHashTreeInstream = new UnlabeledHashTreeInputStream(newUHTPath);
    } else {
      this.unlabeledHashTreeOutstream = new UnlabeledHashTreeStreamer(newUHTPath);

      /*
       * Note that, because unlabeledHashTreeOutstream is an instance of an
       * UnlabeledHashTreeStreamer, it implements the HashTreeInStreamer interface and can be cast
       * to a HashTreeInStreamer
       */
      this.unlabeledHashTreeInstream =
          (HashTreeInStreamer<UnlabeledHashTree>) this.unlabeledHashTreeOutstream;
    }
  }


  private ProbabilisticSequenceIDTrainer() {
    ProbabilisticSequenceIDTrainer.LOGGER.setLevel(Level.INFO);
  }


  /**
   * ******************************************** Constructor for ProbabilisticSequenceIDTrainer in
   * which the smoothing method determines the type of LabeledHashTree to use for training and also
   * what tokens are considered "unlabeled."
   *
   * <p>
   * We use the sentence "The big red dog runs fast" as an example, with the annotation going to the
   * phrase "red dog."
   *
   * <p>
   * 1. UNSMOOTHED: When an annotated phrase is added to the "primaryLabeledTree," only when the
   * last token of the annotated phrase is considered labeled. All other phrases, shorter, longer
   * and encompassing the last token of the phrase are unlabeled. So, in the above sentence, the
   * phrase "red dog" would be added to the primaryLabeledTree, but only the token "dog," when it
   * was in the second position following "red" would be marked as labeled and get a labeled score
   * of '1'. The token "red" in the first position would get a labeled score of '0', and an
   * unlabeled score of '1'.
   *
   * <p>
   * 2. SMOOTHED: Unlike the unsmoothed case, every token, when initially added to the
   * primaryLabeledTree, gets a labeled value of 1. If the token is the last token in an annotated
   * phrase, when it is the last token as part of the labeled phrase in the primary tree, its
   * labeled value is further incremented by '1'. Unlabeled scoring is unchanged from the unsmoothed
   * variant. So, for the smoothed variant, "red" in the first position would get added with a
   * labeled score of '1' and an unlabeled score of '1', and "dog" in the second position would get
   * added with a labeled score of '2' and an unlabeled score of '0'. Note that "dog" alone would
   * not be part of the labeled tree built only from the example above.
   *
   * <p>
   * 3. EXTENDED: Handles labeling the same way as the smooth variant, but handles unlabeled phrases
   * differently.For the extended variant, only tokens outside of an annotated phrase,at positions
   * in a phrase that did not start at the beginning of an annotated phrase (such a phrase would not
   * be even considered an annotated phrase), or that extend beyond the end of an annotated phrase
   * are considered unlabeled. So, using our standard example, "red" would get a labeled score of
   * '1' and an unlabeled score of '0', while "dog" would get a labeled score of '2' and an
   * unlabeled score of '0.'
   *
   * <p>
   * 4. AMPLIFIED: This smoothing variant uses an "ExtendedLabeledTree" as the primaryLabeledTree,
   * so what is considered labeled is different from the extended variant but what is considered
   * unlabeled is the same. For the amplified variant, every token in a labeled phrase gets equal
   * weight when it occurs as part of an annotated phrase, and there is *not* any smoothing, so all
   * tokens that occur start with a value of 0 which is immediately increased to '1' (since they
   * will not be in the labeled tree at all unless part of an annotated phrase). Using our previous
   * example, "red" would have a labeled value of '1' and unlabeled value of '0', and "dog" would
   * also have a labeled value of '1' and unlabeled value of '0'.
   *
   * <p>
   * 5. XTREME: This smoothing variant uses a "LabeledSubsequenceHashTree as the primaryLabeledTree,
   * and it also handles scoring for unlabeled tokens differently than all previous variants. As far
   * as determining what is unlabeled, this variant skips over annotated phrases entirely when it
   * first encounters them in terms of determining what is unlabeled. PREVIOUSLY, while the phrases
   * "dog", "dog runs", and "dog runs fast" would all be considered unlabeled and entered as such in
   * the primaryLabeledTree *IF* those phrases had ever been annotated or were part of an annotation
   * beginning with "dog" (*NOT* the case in our example). In the present variant, none of those
   * could be entered into the primayLabeledTree based on our example sentence because the system
   * would jump over this phrase entirely when encountered and only consider phrases starting with
   * tokens entirely outside the annotated phrase.
   *
   * @param smoothingMethod
   * @author Glenn Gobbel - Feb 10, 2013 ********************************************
   */
  private ProbabilisticSequenceIDTrainer(PhraseIDSmoothingMethod smoothingMethod) {
    this();
    switch (smoothingMethod) {
      case UNSMOOTHED:
        this.primaryLabeledTree = new UnsmoothedLabeledTree();
        break;
      case SMOOTHED:
        this.primaryLabeledTree = new LabeledHashTree();
        break;
      case EXTENDED:
        this.primaryLabeledTree = new LabeledHashTree();
        break;
      case AMPLIFIED:
        this.primaryLabeledTree = new ExtendedLabeledTree();
        break;
      case XTREME:
        this.primaryLabeledTree = new LabeledSubsequenceHashTree();
        break;
      case XTREME_MOD:
        this.primaryLabeledTree = new ModifiedSubsequenceHashTree();
        break;
      default:
        this.primaryLabeledTree = new LabeledHashTree();
    }
  }


  public void addUnlabeledToLabeledTree() {
    Iterator<UnlabeledHashTree> unlabeledTreeIterator = this.unlabeledHashTreeInstream.iterator();
    while (unlabeledTreeIterator.hasNext()) {
      UnlabeledHashTree curUnlabeledTree = unlabeledTreeIterator.next();
      for (RaptatPair<List<String>, LabeledHashTree> curPair : this.primaryLabeledTree) {
        long numUnlabeled = curUnlabeledTree.getUnlabeledForStringSequence(curPair.left);
        if (numUnlabeled > 0) {
          this.primaryLabeledTree.incrementStringSequenceUnlabeled(curPair.left, numUnlabeled);
        }
      }
    }
  }


  /**
   * *********************************************************** Create an unlabledHashTree for the
   * current document, which will contain all unlabeled phrases up to MAXIMUM_ELEMENTS_DEFAULT in
   * length. The algorithm works by evaluating each token in the document to see whether it is the
   * start of a labeled phrase. By going from the beginning of the document to the end, and by
   * storing the positions of the labeled phrases in a sorted array, we can quickly determine which
   * tokens are at the start of a labeled phrase and which are not. If a token is not the start of a
   * labeled phrase, we can just add the sequence starting with that token to the unlabeledHashTree
   * for the document.
   *
   * <p>
   * Postcondition: An UnlabeledHashTree instance is created that contains all the unlabeled token
   * sequences in the document that are shorter than MAXIMUM_ELEMENTS_DEFAULT in length.
   *
   * @param docAnnotations
   * @param docSentences
   * @param docName
   * @author Glenn Gobbel - Apr 6, 2012 ***********************************************************
   */
  @Override
  public UnlabeledHashTree buildUnlabeledHashTree(List<AnnotatedPhrase> docAnnotations,
      List<AnnotatedPhrase> docSentences, String docName, int maxTokens) {
    UnlabeledHashTree theTree = new UnlabeledHashTree(docName);
    int curAnnotationLength = 0;

    // Sort annotations according to start offset - this has to be done as
    // it was not done in
    // processing done to this point
    Collections.sort(docAnnotations, ProbabilisticSequenceIDTrainer.phraseSorter);

    // Sort sentences according to startOffset of first token - this has to
    // be done as it was
    // not done in processing done to this point
    Collections.sort(docSentences, ProbabilisticSequenceIDTrainer.phraseSorter);

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

      if (sentenceTokens != null) {
        Collections.sort(sentenceTokens, ProbabilisticSequenceIDTrainer.tokenSorter);

        for (int tokenIndex = 0; tokenIndex < sentenceTokens.size(); tokenIndex++) {
          RaptatToken curToken = sentenceTokens.get(tokenIndex);

          /*
           * If a current annotation exists and the current token overlaps it in anyway then process
           * it as below depending on the type of smoothing we are using. The code and "if"
           * statement actually tests whether the token does *not* "not overlap" (note that the two
           * "nots" are intentional). It examines whether the token starts after the annotation or
           * the token ends before the annotation. If either is true, the token does not overlap. If
           * neither is true, it does overlap.
           */
          if (curAnnotation != null
              && Integer.parseInt(curToken.getStartOffset()) < Integer
                  .parseInt(curAnnotation.getProcessedTokensEndOffset())
              && Integer.parseInt(curToken.getEndOffset()) > Integer
                  .parseInt(curAnnotation.getProcessedTokensStartOffset())) {
            /*
             * If we are using "xtreme" smoothing, *none* of the tokens within an annotation are
             * part of an unlabeled phrase. For example, if the sentence was
             * "The big red dog runs fast," and "red dog" was an annotated phrase, neither "red,"
             * nor "red dog," nor "red dog runs", nor "red dog runs fast" would be entered into the
             * unlabeled hash tree
             */
            if (this.smoothingMethod == PhraseIDSmoothingMethod.XTREME) {
              // tokenIndex += curAnnotation.getRawTokenNumber() -
              // 1;

              /*
               * Changed code from above line on 2/10/13 because the document should consist only of
               * processed tokens, so we should only be considering the number of processed tokens
               * in an annotation, not raw tokens
               */
              tokenIndex += curAnnotation.getProcessedTokens().size() - 1;
            }
            /*
             * If using "extended" or "amplified" phrase identification smoothing, all tokens in a
             * sequence are marked as labeled, so none are considered unlabeled. However, unlike
             * XTREME smoothing, the annotated phrase tokens do become a part of the tree, but they
             * are *not* marked as unlabeled and only phrases that extend beyond the phrase are
             * marked as unlabeled. As above, if the sentence was "The big red dog runs fast," and
             * "red dog" was an annotated phrase, the phrase "red dog runs fast" would be entered,
             * but only the token "runs" and "fast" would be marked as unlabeled. Also,
             * "dog  runs fast" would be entered, but again, only would be entered into the
             * unlabeled hash tree
             */
            else if (this.smoothingMethod == PhraseIDSmoothingMethod.EXTENDED
                || this.smoothingMethod == PhraseIDSmoothingMethod.AMPLIFIED) {
              theTree.addUnlabeledSequenceFromIndex(sentenceTokens, tokenIndex,
                  tokenIndex + curAnnotationLength, maxTokens);
            }
            /*
             * If using unsmoothed or smoothed phrase identification smoothing, only the last token
             * in the sequence, representing the entire sequence, is considered labeled and
             * therefore is *not* marked as unlabeled.
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
             * If annotation is between two tokens, then scanning through all the tokens in a
             * document can skip by these due to lack of any overlap, which can cause the downstream
             * annotations to not get processed. This handles such a case and moves on to the next
             * annotation
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
    }
    return theTree;
  }


  @Override
  public void clearTraining() {
    this.unlabeledHashTreeOutstream.clear();
    this.primaryLabeledTree.clear();
  }


  public void closeTreeStreams() {
    this.unlabeledHashTreeOutstream.writeTreesToDisk();
  }


  public void generateUnlabeledHashTree(AnnotationGroup trainingGroup) {
    RaptatDocument raptatDocument = trainingGroup.getRaptatDocument();
    ProbabilisticSequenceIDTrainer.LOGGER
        .debug("\n\n==============================================================="
            + "\nBuilding unlabeledhash for document:\n\t" + raptatDocument.getTextSourcePath()
            + "\n================================================================\n\n");
    List<AnnotatedPhrase> docSentences = trainingGroup.getRaptatDocument().getActiveSentences();
    List<AnnotatedPhrase> docAnnotations = trainingGroup.referenceAnnotations;
    String docTitle = raptatDocument.getTextSourcePath().orElse("NoTextSourcePath");
    int maxTokens = trainingGroup.getMaxTokens();
    UnlabeledHashTree unlabeledPhraseTree =
        buildUnlabeledHashTree(docAnnotations, docSentences, docTitle, maxTokens);
    trainingGroup.setBaseUnlabeledPhrases(unlabeledPhraseTree);
  }


  public void generateUnlabeledHashTrees(List<AnnotationGroup> theGroups) {
    for (AnnotationGroup curGroup : theGroups) {
      generateUnlabeledHashTree(curGroup);
    }
  }


  /**
   * @author Shrimalini Jayaramaraja
   * @return
   */
  public LabeledHashTree getLabeledTree() {
    return this.primaryLabeledTree;
  }


  @Override
  public PhraseIDSmoothingMethod getPhraseIDSmoothingMethod() {
    return this.smoothingMethod;
  }


  /**
   * *********************************************************** Method added to comply with
   * SequenceIDTrainer interface
   *
   * @return
   * @author Glenn Gobbel - Sep 7, 2012 ***********************************************************
   */
  @Override
  public LabeledHashTree getSequenceIDStructure() {
    return this.primaryLabeledTree;
  }


  /**
   * *********************************************************** Takes the annotated phrases in a
   * document and puts them in reverse order according to start offset. Also takes the tokens within
   * each phrase and puts them in reverse order according to start offset.
   *
   * <p>
   * Precondition: None Postcondition: The annotationsInDocument are in reverse order and the tokens
   * in those annotations are also in reverse order
   *
   * @param annotationsInDocument
   * @author Glenn Gobbel - Jun 15, 2012 ***********************************************************
   */
  @Override
  public void reversePhraseAndTokenOrder(List<AnnotatedPhrase> annotationsInDocument) {
    List<RaptatToken> curTokens;
    Collections.sort(annotationsInDocument, ProbabilisticSequenceIDTrainer.reversePhraseSorter);
    for (AnnotatedPhrase curPhrase : annotationsInDocument) {
      curTokens = curPhrase.getProcessedTokens();
      Collections.sort(curTokens, ProbabilisticSequenceIDTrainer.reverseTokenSorter);
    }
  }


  /**
   * *********************************************************** Method used to update an existing
   * training solution. It is called by a SolutionUpdateProcessor instance.
   *
   * @param inputGroup
   * @param previousUnlabeledPhrases
   * @author Glenn Gobbel - Oct 14, 2012 ***********************************************************
   */
  @Override
  public void updatePreviousTraining(AnnotationGroup inputGroup) {
    UnlabeledHashTree activeUnlabeledPhrases = inputGroup.getActiveUnlabeledPhrases();

    // First we add the labeled sequences within the new group to the
    // current primaryTree
    LabeledHashTree newSequences = updatePrimaryTree(inputGroup.referenceAnnotations);

    // Next we add *all" the unlabeled values from the new group to the
    // updated primaryTree
    // where labels exist
    this.addUnlabeledSequencesToPrimaryTree(this.primaryLabeledTree, activeUnlabeledPhrases);

    /*
     * Update the primary tree to include unlabeled instances of the annotated phrases within
     * previous documents. We only need to search for newSequences as old ones should have already
     * been analyzed and added. That is, if the sequence was labeled in a previous document, the
     * program would have looked in all the unlabeled trees up to that point to find all unlabeled
     * instances of the sequence up to that point.
     */
    if (!newSequences.isEmpty()) {
      this.addUnlabeledSequencesToPrimaryTree(newSequences);
    }

    this.unlabeledHashTreeOutstream.append(activeUnlabeledPhrases);
  }


  @Override
  public void updateProbabilities() {
    this.primaryLabeledTree.updateProbabilities();
  }


  @Override
  public void updateTraining(AnnotationGroup curGroup) {
    updatePrimaryTree(curGroup.referenceAnnotations);

    UnlabeledHashTree activeUnabeledPhrases = curGroup.getActiveUnlabeledPhrases();
    this.unlabeledHashTreeOutstream.append(activeUnabeledPhrases);

    if (ProbabilisticSequenceIDTrainer.LOGGER.isDebugEnabled()) {
      for (UnlabeledHashTree curTree : (UnlabeledHashTreeStreamer) this.unlabeledHashTreeOutstream) {
        System.out.print("Document:" + curGroup.getRaptatDocument().getTextSource());
        if (curTree.containsKey("lisinopril_NNP")) {
          long unlabeled =
              activeUnabeledPhrases.getUnlabeledForStringSequence(Arrays.asList("lisinopril_NNP"));
          System.out.println("\tUnlabeled:" + unlabeled);
        } else {
          System.out.println("\tUnlabeled:0");
        }
      }
    }
  }


  /**
   * *********************************************************** Add how many times a phrase within
   * a list of phrases occurs in an UnlabeledHashTree and add this number to the unlabeled field of
   * the primary tree for the sequence.
   *
   * <p>
   * Precondition: None.
   *
   * <p>
   * Postcondition: primaryLabeledTree is updated with the number of times a phrase labeled in the
   * current document also occurs but is unlabeled.
   *
   * @param sequencesToCheck
   * @param unlabeledTree
   * @param useStems
   * @author Glenn Gobbel - Apr 7, 2012 ***********************************************************
   */
  protected void addUnlabeledSequencesToPrimaryTree(
      IterableHashTree<LabeledHashTree> sequencesToCheck, UnlabeledHashTree unlabeledTree) {
    List<String> curSequence = null;
    long numberUnlabeled;
    Iterator<RaptatPair<List<String>, LabeledHashTree>> theSequences = sequencesToCheck.iterator();
    while (theSequences.hasNext()) {
      curSequence = theSequences.next().left;

      numberUnlabeled = unlabeledTree.getUnlabeledForStringSequence(curSequence);
      if (numberUnlabeled > 0) {
        this.primaryLabeledTree.incrementStringSequenceUnlabeled(curSequence, numberUnlabeled);
      }
    }
  }


  /**
   * *********************************************************** Determine how many times a token
   * sequence within a list of token sequences has occurred in a set of unlabeledHashTrees and add
   * that number to the primaryHashTree.
   *
   * <p>
   * Precondition: All the sequences supplied must truly be new, and the Integer value paired with
   * them must be non-negative and less than the length of the list representing the sequence.
   *
   * <p>
   * Postcondition: The primaryHashTree is updated with regard to the number of times a phrase was
   * unlabeled in previous documents based on the set of unlabeledHashTrees.
   *
   * @param labeledSequences
   * @param useTokenStems
   * @author Glenn Gobbel - Apr 8, 2012 ***********************************************************
   */
  protected void addUnlabeledSequencesToPrimaryTree(LabeledHashTree labeledSequences) {
    Iterator<UnlabeledHashTree> unlabeledTreeIterator = this.unlabeledHashTreeInstream.iterator();
    Iterator<RaptatPair<List<String>, LabeledHashTree>> newSequenceIterator =
        labeledSequences.iterator();
    UnlabeledHashTree curTree;
    long numberUnlabeled;
    List<String> curTokenSequence;
    while (unlabeledTreeIterator.hasNext()) {
      curTree = unlabeledTreeIterator.next();

      while (newSequenceIterator.hasNext()) {
        curTokenSequence = newSequenceIterator.next().left;

        numberUnlabeled = curTree.getUnlabeledForStringSequence(curTokenSequence);
        if (numberUnlabeled > 0) {
          this.primaryLabeledTree.incrementStringSequenceUnlabeled(curTokenSequence,
              numberUnlabeled);
        }
      }
    }
  }


  /**
   * *********************************************************** Get the phrases that were annotated
   * by RapTAT but not by the human annotator. A phrase is considered annotated by both if there is
   * overlap by at least one token (i.e. the start and end offsets of a token from the RapTAT phrase
   * is within the start and end offsets of the human annotated phrase).
   *
   * @param humanAnnotations
   * @param pathToRaptatXML
   * @return List<AnnotatedPhrase> corresponding to deleted RapTAT-annotated phrases.
   * @author Glenn Gobbel - Jun 1, 2012
   * @param pathToDocument ***********************************************************
   */
  protected List<AnnotatedPhrase> getDeletedPhrases(List<AnnotatedPhrase> humanAnnotations,
      List<AnnotatedPhrase> raptatAnnotations, String pathToDocument) {
    // Basic idea will be to create a list of RapTAT-generated annotations
    // and then see if any
    // of the tokens within each annotated phrase corresponds to one of the
    // tokens within the
    // human annotated phrases. This will occur if the token start offset
    // from the raptat phrase
    // is >= to the human annotated phrase start offset and if the token end
    // offset from the
    // raptat phrase is <= to the human annotated phrase end offset.
    List<AnnotatedPhrase> deletedPhrases = new Vector<>();

    // First build an IndexedTree of the human annotated phrases but with
    // just start offset as
    // the index and end offset as the object stored at the index
    List<IndexedObject<Integer>> annotationsAsIndices = new Vector<>(humanAnnotations.size());
    for (AnnotatedPhrase curPhrase : humanAnnotations) {
      annotationsAsIndices
          .add(new IndexedObject<>(Integer.parseInt(curPhrase.getRawTokensStartOff()),
              Integer.parseInt(curPhrase.getRawTokensEndOff())));
    }
    IndexedTree<IndexedObject<Integer>> searchTree = new IndexedTree<>(annotationsAsIndices);

    List<RaptatToken> phraseTokens;
    IndexedObject<Integer> closestReferencePhrase;
    for (AnnotatedPhrase curRaptatPhrase : raptatAnnotations) {
      phraseTokens = curRaptatPhrase.getProcessedTokens();
      for (RaptatToken curToken : phraseTokens) {
        closestReferencePhrase =
            searchTree.getClosestLesserOrEqualObject(Integer.parseInt(curToken.getStartOffset()));
        if (closestReferencePhrase != null && Integer
            .parseInt(curToken.getEndOffset()) <= closestReferencePhrase.getIndexedObject()) {
          break;
        }
      }
      deletedPhrases.add(curRaptatPhrase);
    }
    return deletedPhrases;
  }


  /**
   * ***********************************************************
   *
   * @param inputPhrase
   * @param useStems
   * @return List of new phrases stored as a List of lists of tokens Precondition: None.
   *         Postcondition: All the phrases within the inputPhrases list are added as labeled to the
   *         field primaryHashTree, and the phrases not previously in the tree are selected and
   *         returned as a new list.
   * @author Glenn Gobbel - Apr 6, 2012 ***********************************************************
   */
  protected void updateDeletedInPrimaryTree(List<AnnotatedPhrase> deletedPhrases) {
    List<RaptatToken> tokensInPhrase = null;
    for (AnnotatedPhrase curPhrase : deletedPhrases) {
      tokensInPhrase = curPhrase.getProcessedTokens();
      this.primaryLabeledTree.addDeletedPhrase(tokensInPhrase);
    }
  }


  /**
   * ***********************************************************
   *
   * @param inputPhrase
   * @param useStems
   * @return LabeledHashTree of new sequences where the mark of labeled > 0 indicates a new
   *         sequences
   *         <p>
   *         Precondition: None. Postcondition: All the phrases within the inputPhrases list are
   *         added as labeled to the field primaryHashTree, and the phrases not previously in the
   *         tree are selected and returned within a new LabeledHashTree instance.
   * @author Glenn Gobbel - Apr 6, 2012 ***********************************************************
   */
  protected LabeledHashTree updatePrimaryTree(List<AnnotatedPhrase> inputPhrases) {
    LabeledHashTree newSequenceTree = new LabeledHashTree();
    for (AnnotatedPhrase curPhrase : inputPhrases) {
      List<RaptatToken> curTokenSequence = curPhrase.getProcessedTokens();
      if (curTokenSequence != null && curTokenSequence.size() > 0) {
        List<RaptatPair<List<RaptatToken>, Integer>> newSequences =
            this.primaryLabeledTree.addLabeledPhrase(curTokenSequence);
        for (RaptatPair<List<RaptatToken>, Integer> curPair : newSequences) {
          newSequenceTree.addPhraseLabelFromIndex(curPair.left, curPair.right);
        }
      }
    }
    return newSequenceTree;
  }
}
