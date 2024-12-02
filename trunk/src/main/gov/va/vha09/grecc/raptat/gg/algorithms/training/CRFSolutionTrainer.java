/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.training;

import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TokenSequenceSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/** @author Glenn T. Gobbel Feb 16, 2016 */
public class CRFSolutionTrainer extends TokenSequenceSolutionTrainer {
  /**
   * @param textAnalyzer
   * @param acceptedConceptsSet
   */
  public CRFSolutionTrainer(TextAnalyzer textAnalyzer, HashSet<String> acceptedConceptsSet) {
    super(textAnalyzer, acceptedConceptsSet);
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.core.TokenSequenceSolutionTrainer #getTsiTrainer()
   */
  @Override
  public SequenceIDTrainer getTsiTrainer() {
    /* A CRFSolutionTrainer has no SequenceIDTrainer, so return null */
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.candidates.coremodifications. training
   * .TokenSequenceSolutionTrainer#generateSolution(java.util.List)
   */
  @Override
  public TokenSequenceFinderSolution updateAndReturnSolution(
      List<SchemaConcept> schemaConceptList) {
    // TODO Auto-generated method stub
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.candidates.coremodifications. training
   * .TokenSequenceSolutionTrainer#updateTraining(src.main.gov.va.vha09.grecc.
   * raptat.gg.datastructures.AnnotationGroup)
   */
  @Override
  public void updateTraining(AnnotationGroup trainingGroup) {
    throw new NotImplementedException("Not yet implemented");
  }
}
