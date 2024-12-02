package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes;

import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;

public class NBSequenceIDTrainer extends SequenceIDTrainer {
  protected NBSequenceIDSolution theSolution;


  public NBSequenceIDTrainer() {}


  public void addUnlabeledToLabeledTree() {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public void clearTraining() {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  public void closeTreeStreams() {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  public void createSolution() {
    this.theSolution = new NBSequenceIDSolution(RaptatConstants.MAX_TOKENS_DEFAULT);
  }


  @Override
  public PhraseIDSmoothingMethod getPhraseIDSmoothingMethod() {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public SequenceIDStructure getSequenceIDStructure() {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public void reversePhraseAndTokenOrder(List<AnnotatedPhrase> sentences) {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public void updatePreviousTraining(AnnotationGroup theGroup) {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public void updateProbabilities() {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }


  @Override
  public void updateTraining(AnnotationGroup curGroup) {
    throw new NotImplementedException(
        "Updating of solutions that use BOWSequenceIDTrainers has not been implemented");
  }
}
