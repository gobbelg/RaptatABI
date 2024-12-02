package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OccurrenceSet implements Serializable {
  private static final long serialVersionUID = -2100579591717283379L;
  public int annotatedPhrases;
  public int unannotatedPhrases;
  public double likelihood;
  public boolean probabilitiesUpdated = false;


  public OccurrenceSet(int posOccurrences, int nonOccurrences, double startProbability) {
    this.annotatedPhrases = posOccurrences;
    this.unannotatedPhrases = nonOccurrences;
    this.likelihood = startProbability;
    this.probabilitiesUpdated = true;
  }


  public boolean canEqual(Object other) {
    return other instanceof OccurrenceSet;
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof OccurrenceSet) {
      OccurrenceSet otherSet = (OccurrenceSet) other;
      if (otherSet.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        return eBuilder.append(this.annotatedPhrases, otherSet.annotatedPhrases)
            .append(this.unannotatedPhrases, otherSet.unannotatedPhrases)
            .append(this.probabilitiesUpdated, otherSet.probabilitiesUpdated).isEquals();
      }
    }
    return false;
  }


  @Override
  public int hashCode() {
    int oddNumber = 61;
    int primeNumber = 3;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    return hcBuilder.append(this.annotatedPhrases).append(this.unannotatedPhrases)
        .append(this.probabilitiesUpdated).toHashCode();
  }
}
