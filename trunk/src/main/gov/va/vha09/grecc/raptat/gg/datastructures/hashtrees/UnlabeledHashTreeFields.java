package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.List;

public class UnlabeledHashTreeFields implements HashTreeFields {

  public List<String> tokenSequence = null;
  public long unlabeled = 0;


  // public LabeledHashTreeResultTest()
  // {}

  public UnlabeledHashTreeFields(List<String> tokenSequence, long unlabeled) {
    this.tokenSequence = tokenSequence;
    this.unlabeled = unlabeled;
  }


  @Override
  public String toString() {
    return "Sequence:  " + this.tokenSequence + "\n\t\tUnlabeled: " + this.unlabeled + "\n\n";
  }
}
