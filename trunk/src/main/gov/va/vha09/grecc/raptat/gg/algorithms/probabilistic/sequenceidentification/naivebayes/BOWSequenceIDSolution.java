package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurrenceSet;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class BOWSequenceIDSolution implements SequenceIDStructure, Serializable {
  private static final long serialVersionUID = 5849146571605575622L;
  protected static Logger logger = Logger.getLogger(BOWSequenceIDSolution.class);

  private static final int ANNOTATED = 1;

  private static final int NOT_ANNOTATED = 0;

  // This is used to store how many times a token occurs upstream, downstream
  // or within a phrase (reason for the 3-element array) when the phrase is
  // annotated or not annotated.
  private final ConcurrentHashMap<String, OccurrenceSet>[] tokenConditionals;

  // This is used to store the likelihood that a token string that has
  // never been seen before is upstream, within a phrase, or downstream
  // of an annotated phrase
  private final double missingTokenLikelihood[] = {0.0, 0.0, 0.0};

  // This is keeps track of how many times a phrase is annotated or not
  // annotated and the likelihood used to calculate likelihood of
  // annotation. It is similar to the NBPriorTable used with NBTrainers for
  // concept mapping,but, in this case, we are only interested in two
  // concepts, annotated and
  // not annotated. The likelihood of various numbers of upstream, phrase,
  // and downstream tokens is stored in the double [] that is within the
  // "likelihood" field of the priors. The first index corresponds to how many
  // upstream tokens, the second to the number of downstream, and the third
  // to the number of phrase tokens. Note that the number of upstream and
  // downstream
  // tokens can be zero, so the zero index in the array provides the
  // likelihood
  // contributed when there are zero upstream or downstream tokens.
  private final PriorTable priors;

  // The variable "totalTokens" stores the number of times any token
  // occurs within, upstream, or downstream of an annotated or non-
  // annotated sequence. Unlike with vocabSize, which keeps track
  // of unique tokens, this variable counts a token each time it occurs,
  // even if it has occurred and been counted previously. The first index
  // selects the row corresponding to whether it is count non-annotated
  // tokens (index = 0) or annotated tokens (index == 1). The second index
  // indicates whether we are looking at the total for the phrase itself
  // (index == BOW_SEQUENCE_ID_PHRASE_INDEX == 1), tokens upstream of the
  // phrase (index == BOW_SEQUENCE_ID_PRIOR_INDEX == 0), or tokens that
  // are downstream (index == BOW_SEQUENCE_ID_POST_INDEX == 2)
  private final int[][] totalTokens = {{0, 0, 0}, {0, 0, 0}};;
  // These settings are stored with the solution so they can be used
  // when an instance of this class is updated
  private final PhraseIDTrainOptions trainingOptions;

  private boolean likelihoodsUpdated = true;
  // We don't need to store tokenWindowWidth because it is contained
  // in trainingOptions - we separate it out here so it is readily available
  // during training
  private transient int tokenWindowWidth = 0;


  @SuppressWarnings("unchecked")
  public BOWSequenceIDSolution(PhraseIDTrainOptions trainingOptions) {
    super();
    this.trainingOptions = trainingOptions;

    this.tokenConditionals = new ConcurrentHashMap[3];
    for (int i = 0; i < this.tokenConditionals.length; i++) {
      this.tokenConditionals[i] =
          new ConcurrentHashMap<>(RaptatConstants.ESTIMATED_VOCABULARY_SIZE);
    }

    this.tokenWindowWidth = trainingOptions.getSurroundingTokensToUse();
    this.priors = new PriorTable(this.tokenWindowWidth + 1, RaptatConstants.MAX_TOKENS_DEFAULT + 1,
        this.tokenWindowWidth + 1, RaptatConstants.MAX_TOKENS_DEFAULT);
  }


  @Override
  public boolean canEqual(Object other) {
    return other instanceof BOWSequenceIDSolution;
  }


  public void clear() {
    this.priors.clear();

    for (int i = 0; i < this.tokenConditionals.length; i++) {
      this.tokenConditionals[i].clear();
    }

    for (int i = 0; i < this.missingTokenLikelihood.length; i++) {
      this.missingTokenLikelihood[i] = 0.0;
    }

    for (int i = 0; i < this.totalTokens.length; i++) {
      for (int j = 0; j < this.totalTokens[0].length; j++) {
        this.totalTokens[i][j] = 0;
      }
    }

    this.likelihoodsUpdated = true;
  }


  @Override
  public void compareTo(Object other) {
    System.out.println("CompareTo() method not implemented for BOWSequenceIDStructures");
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof BOWSequenceIDSolution) {
      BOWSequenceIDSolution otherSolution = (BOWSequenceIDSolution) other;
      if (otherSolution.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.likelihoodsUpdated, otherSolution.likelihoodsUpdated)
            .append(this.tokenConditionals, otherSolution.tokenConditionals)
            .append(this.priors, otherSolution.priors)
            .append(this.totalTokens, otherSolution.totalTokens)
            .append(this.trainingOptions, otherSolution.trainingOptions).isEquals();
      }
    }
    return false;
  }


  public double[] getMissingTokenLikelihood() {
    return this.missingTokenLikelihood;
  }


  /** @return the priors */
  public PriorTable getPriors() {
    return this.priors;
  }


  /** @return the tokenConditionals */
  public ConcurrentHashMap<String, OccurrenceSet>[] getTokenConditionals() {
    return this.tokenConditionals;
  }


  public int getTokenWindowWidth() {
    return this.tokenWindowWidth;
  }


  public int[][] getTotalTokens() {
    return this.totalTokens;
  }


  /** @return the trainingOptions */
  public PhraseIDTrainOptions getTrainingOptions() {
    return this.trainingOptions;
  }


  @Override
  public int hashCode() {
    int oddNumber = 93;
    int primeNumber = 5;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.tokenConditionals).append(this.likelihoodsUpdated)
        .append(this.priors).append(this.totalTokens).append(this.trainingOptions).toHashCode();
  }


  public void incrementUnannotated(String tokenString, int incrementAmount,
      int conditionalToUpdate) {
    OccurrenceSet curOccurrences;
    if ((curOccurrences = this.tokenConditionals[conditionalToUpdate].get(tokenString)) != null) {
      curOccurrences.unannotatedPhrases += incrementAmount;
    } else {
      this.tokenConditionals[conditionalToUpdate].put(tokenString,
          new OccurrenceSet(0, incrementAmount, 0.0));
    }
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure #print()
   */
  @Override
  public void print() {
    // TODO Auto-generated method stub
    System.err.print("Print not currently implemented for BOWSequenceIDSolution");
  }


  public void printTokenConditional(int conditionalSet) {
    Set<String> theKeys = this.tokenConditionals[conditionalSet].keySet();
    for (String curString : theKeys) {
      OccurrenceSet curSet = this.tokenConditionals[conditionalSet].get(curString);
      System.out.println(curString + "\t" + curSet.annotatedPhrases + "\t"
          + curSet.unannotatedPhrases + "\t" + curSet.likelihood);
    }

    theKeys = null;
  }


  @Override
  public void showComparison(Object other) {
    System.out.println("showComparison() method not implemented for BOWSequenceIDStructures");
  }


  public void updateAnnotated(HashSet<RaptatToken> annotatedTokens, int conditionalToUpdate) {
    this.totalTokens[BOWSequenceIDSolution.ANNOTATED][conditionalToUpdate] +=
        annotatedTokens.size();
    for (RaptatToken curToken : annotatedTokens) {
      String tokenString = curToken.getTokenStringAugmented();
      OccurrenceSet curOccurrences;
      if ((curOccurrences = this.tokenConditionals[conditionalToUpdate].get(tokenString)) != null) {
        curOccurrences.annotatedPhrases++;
        curOccurrences.probabilitiesUpdated = false;
      } else {
        this.tokenConditionals[conditionalToUpdate].put(tokenString, new OccurrenceSet(1, 0, 0.0));
      }
    }
  }


  public void updateConditionalProbabilities() {
    double[] notToAnnotatedRatios = new double[this.priors.phraseSizeCounts.length];
    for (int i = 0; i < this.priors.phraseSizeCounts.length; i++) {
      notToAnnotatedRatios[i] = (double) this.priors.phraseSizeCounts[i].annotated
          / this.priors.phraseSizeCounts[i].unannotated;
    }

    for (int i = 0; i < this.tokenConditionals.length; i++) {
      Set<String> conditionalKeys = this.tokenConditionals[i].keySet();
      for (String curKey : conditionalKeys) {
        OccurrenceSet curPair = this.tokenConditionals[i].get(curKey);

        // We have no null check because modifiedConditionals
        // should only contain strings that are keys of
        // tokenConditionals;
        if (curPair.annotatedPhrases > 0 || curPair.unannotatedPhrases > 0) {
          curPair.likelihood = Math.log10(curPair.annotatedPhrases + notToAnnotatedRatios[i]);
          curPair.likelihood -= Math.log10(curPair.unannotatedPhrases + notToAnnotatedRatios[i]);
        } else {
          curPair.likelihood = this.missingTokenLikelihood[i];
        }
        curPair.probabilitiesUpdated = true;
      }
    }
  }


  public void updateNonAnnotated(List<String> tokenStrings, int tokenWindowWidth) {
    int sentenceSize = tokenStrings.size();
    this.priors.incrementUnannotated(sentenceSize);
    // The last token cannot be upstream
    this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX] +=
        sentenceSize - 1;

    // The first and last tokens are added by the program and will never be
    // annotated
    // so they are subtracted from sentence size
    this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX] +=
        sentenceSize - 2;

    // The first token cannot be downstream of a phrase
    this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX] +=
        sentenceSize - 1;

    // First add token at beginning of sentence - it can only be added as an
    // upstream tokens as it can't be downstream and, because the program
    // adds a "start token" to the beginning, it can't be part of an
    // annotated
    // phrase
    int i = 0;
    String curTokenString = tokenStrings.get(i);
    incrementUnannotated(curTokenString, 1, RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX);

    for (i = 1; i < sentenceSize - 1; i++) {
      curTokenString = tokenStrings.get(i);
      incrementUnannotated(curTokenString, 1, RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX);
      incrementUnannotated(curTokenString, 1, RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX);
      incrementUnannotated(curTokenString, 1, RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX);
    }

    // Now add the last token to as a only a downstream token. The program
    // adds the last token as an "end token," so it will never be part of an
    // annotation, only downstream of an annotation).
    System.out.println("Adding last token, " + i + " to sentence of size " + sentenceSize);
    incrementUnannotated(curTokenString, 1, RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX);
  }


  public void updatePriorProbabilities() {
    // Now fill in the likelihood for determining contribution of P(N)/P(A)
    // for each occurrence
    // of an upstream, phrase, or downstream token. Each token contributes a
    // probability
    // multiplied
    // by P(N)/P(A), the log of which is equal to -log(baseValue) (i.e
    // log(unannotated) -
    // log(annotated).
    // So, essentially, for every token in the upstream, phrase, or
    // downstream, we subtract
    // baseValue
    for (int i = 0; i < this.priors.likelihood.length; i++) {
      for (int j = 1; j < this.priors.likelihood[0].length; j++) {
        // Calculate value contributed by P(A)/P(N), note that
        // phraseSizeCounts
        // are zero-indexed, so, for example, index 0 has phrases 1
        // token in length,
        // index 2 has phrases 2 tokens in length, etc. But the
        // likelihoods are not
        // zero indexed (the zero actually indicates zero elements, 1
        // indicates 1 element,
        // etc, so we need to subtract 1 from j here.
        double baseValue = Math.log10(this.priors.phraseSizeCounts[j - 1].annotated);
        baseValue -= Math.log10(this.priors.phraseSizeCounts[j - 1].unannotated);

        for (int k = 0; k < this.priors.likelihood[0][0].length; k++) {
          this.priors.likelihood[i][j][k] = baseValue - (i + j + k) * baseValue;
        }
      }
    }
    this.priors.probabilitiesUpdated = true;
  }


  public void updateProbabilities() {
    updatePriorProbabilities();
    updateMissingTokenLikelihood();
    updateConditionalProbabilities();
  }


  /**
   * *********************************************************** Calculate missingTokenLikelihood,
   * which determines contribution of tokens that are absent in the training set but appear in the
   * test set.
   *
   * @author Glenn Gobbel - Sep 28, 2012 ***********************************************************
   */
  private void updateMissingTokenLikelihood() {
    int curIndex = RaptatConstants.BOW_SEQUENCE_ID_PHRASE_INDEX;
    if (this.totalTokens[BOWSequenceIDSolution.ANNOTATED][curIndex] > 0
        && this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][curIndex] > 0) {
      this.missingTokenLikelihood[curIndex] =
          Math.log10(this.totalTokens[BOWSequenceIDSolution.ANNOTATED][curIndex])
              - Math.log10(this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][curIndex]);
    } else {
      this.missingTokenLikelihood[curIndex] = 0;
    }

    if (this.tokenWindowWidth > 0) {
      curIndex = RaptatConstants.BOW_SEQUENCE_ID_PRIOR_INDEX;
      if (this.totalTokens[BOWSequenceIDSolution.ANNOTATED][curIndex] > 0
          && this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][curIndex] > 0) {
        this.missingTokenLikelihood[curIndex] =
            Math.log10(this.totalTokens[BOWSequenceIDSolution.ANNOTATED][curIndex])
                - Math.log10(this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][curIndex]);
      } else {
        this.missingTokenLikelihood[curIndex] = 0;
      }

      curIndex = RaptatConstants.BOW_SEQUENCE_ID_POST_INDEX;
      if (this.totalTokens[BOWSequenceIDSolution.ANNOTATED][curIndex] > 0
          && this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][curIndex] > 0) {
        this.missingTokenLikelihood[curIndex] =
            Math.log10(this.totalTokens[BOWSequenceIDSolution.ANNOTATED][curIndex])
                - Math.log10(this.totalTokens[BOWSequenceIDSolution.NOT_ANNOTATED][curIndex]);
      } else {
        this.missingTokenLikelihood[curIndex] = 0;
      }
    }
    this.likelihoodsUpdated = true;
  }
}
