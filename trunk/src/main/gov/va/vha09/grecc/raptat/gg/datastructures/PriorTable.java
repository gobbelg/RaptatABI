package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * ****************************************************** Keeps track of how many phrases are
 * annotated or not annotated, and, based on those numbers, computes the contribution to likelihood
 * when a phrase under consideration has various lengths and various numbers of upstream and
 * downstream tokens for consideration. Prior probability is based entirely on phrase length, number
 * of upstream, and number of downstream tokens and how often, in the training set, a phrase with
 * similar characteristics (length, upstream and downstream tokens) was annotated.
 *
 * @author Glenn Gobbel - Sep 19, 2012 *****************************************************
 */
public class PriorTable implements Serializable {
  public class PhraseCount implements Serializable {
    private static final long serialVersionUID = 669672235035874196L;
    public int annotated;
    public int unannotated;


    public PhraseCount(int annotated, int unannotated) {
      this.annotated = annotated;
      this.unannotated = unannotated;
    }


    public boolean canEqual(Object other) {
      return other instanceof PhraseCount;
    }


    public void clear() {
      this.annotated = 0;
      this.unannotated = 0;
    }


    @Override
    public boolean equals(Object other) {
      if (other instanceof PhraseCount) {
        PhraseCount otherCount = (PhraseCount) other;
        if (otherCount.canEqual(this)) {
          EqualsBuilder eBuilder = new EqualsBuilder();
          return eBuilder.append(this.annotated, otherCount.annotated)
              .append(this.unannotated, otherCount.unannotated).isEquals();
        }
      }
      return false;
    }


    @Override
    public int hashCode() {
      int oddNumber = 111;
      int primeNumber = 31;
      HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

      return hcBuilder.append(this.annotated).append(this.unannotated).toHashCode();
    }
  }

  private static final long serialVersionUID = -2501252293133475061L;

  // phraseSizeCounts keeps track of how often a phrase
  // of a given length is annotated and unannotated in a corpus.
  // The array is zero-index so phraseSizeCounts[0] givens
  // the number of times a phrase of length 1 is annotated or
  // not. A PhraseCount instance is an object that stores
  // number of occurrences of annotated and unannotated phrases.
  public PhraseCount[] phraseSizeCounts;

  // The first index corresponds to the number of
  // upstream tokens, the second to the number of
  // element tokens in the phrase itself, and the third to downstream tokens
  public double[][][] likelihood;

  public boolean probabilitiesUpdated = false;


  public PriorTable(int upstreamTokens, int elements, int downstreamTokens, int maxElements) {
    this.phraseSizeCounts = new PhraseCount[maxElements];
    for (int i = 0; i < maxElements; i++) {
      // We use add one smoothing with phrase counts
      this.phraseSizeCounts[i] = new PhraseCount(1, 1);
    }

    // Note that Java double arrays are automatically initialized to
    // contain positive zero as the default values
    this.likelihood = new double[upstreamTokens][elements][downstreamTokens];

    this.probabilitiesUpdated = true;
  }


  public boolean canEqual(Object other) {
    return other instanceof PriorTable;
  }


  public void clear() {
    for (int i = 0; i < this.phraseSizeCounts.length; i++) {
      // We use add one smoothing with phrase counts
      this.phraseSizeCounts[i] = new PhraseCount(1, 1);
    }

    for (int i = 0; i < this.likelihood.length; i++) {
      for (int j = 0; j < this.likelihood[i].length; j++) {
        Arrays.fill(this.likelihood[i][j], 0.0);
      }
    }

    this.probabilitiesUpdated = true;
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof PriorTable) {
      PriorTable otherTable = (PriorTable) other;
      if (otherTable.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.probabilitiesUpdated, otherTable.probabilitiesUpdated)
            .append(this.phraseSizeCounts, otherTable.phraseSizeCounts).isEquals();
      }
    }
    return false;
  }


  @Override
  public int hashCode() {
    int oddNumber = 107;
    int primeNumber = 29;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.phraseSizeCounts).append(this.probabilitiesUpdated).toHashCode();
  }


  public void incrementUnannotated(int tokensInSentence) {
    for (int i = 0; i < this.phraseSizeCounts.length - 2; i++) {
      // We add one here because phraseSizeCounts is zero indexed, and
      // we want to know how many times a non-overlapping phrase of
      // length i can occur in a sentence. So, given a sentence 10
      // tokens in length, if i == 1, we are interested in phrases
      // of length 2. A total of 5, non-overlapping phrases of length
      // 2 can occur in a sentence of length 10 (10 / 2 = 5).
      // We subtract 2 from tokenInSentence because we have added
      // start and end tokens to sentences, and these cannot be part of
      // an annotated phrase, they can only be upstream or downstream
      // from the phrase.
      int phrasesToAdd = (int) Math.round((tokensInSentence - 2) / (i + 1.0));
      this.phraseSizeCounts[i].unannotated += phrasesToAdd;
    }

    this.probabilitiesUpdated = false;
  }
}
