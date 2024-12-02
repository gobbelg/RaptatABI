package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.List;

public class LabeledHashTreeFields implements HashTreeFields {

  public List<String> tokenStringSequence = null;
  public long labeled = 0;
  public long unlabeled = 0;
  public long deleted = 0;
  public double labeledProbability = 0;


  // public LabeledHashTreeResultTest()
  // {}

  public LabeledHashTreeFields(List<String> tokenSequence) {
    this.tokenStringSequence = tokenSequence;
  }


  public LabeledHashTreeFields(List<String> tokenSequence, long labeled, long unlabeled,
      long deleted, double labeledProbability) {
    this.tokenStringSequence = tokenSequence;
    this.labeled = labeled;
    this.unlabeled = unlabeled;
    this.deleted = deleted;
    this.labeledProbability = labeledProbability;
  }


  @Override
  public String toString() {
    return "Sequence:  " + this.tokenStringSequence + "\n\tLabeled: " + this.labeled
        + ",\tUnlabeled: " + this.unlabeled + ",\tDeleted: " + this.deleted + ",\tProbability: "
        + this.labeledProbability + "\n\n";
  }
}
