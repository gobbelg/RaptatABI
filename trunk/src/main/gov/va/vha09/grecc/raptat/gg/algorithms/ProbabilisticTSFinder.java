/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms;

import java.util.HashSet;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticSequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ReverseProbabilisticSequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.TokenSequenceFinder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Instances of this class use a
 *
 * @author Glenn T. Gobbel Feb 16, 2016
 */
public class ProbabilisticTSFinder extends TokenSequenceFinder {
  private SequenceIDEvaluator tsEvaluator;
  private NBEvaluator conceptMapEvaluator;
  private boolean invertTokenSequence;


  /**
   * @param tsFinderSolution
   * @param acceptedConcepts
   * @param threshold
   */
  public ProbabilisticTSFinder(ProbabilisticTSFinderSolution tsFinderSolution,
      HashSet<String> acceptedConcepts, double threshold) {
    this.acceptedConcepts = acceptedConcepts;
    initializeEvaluators(tsFinderSolution, threshold);
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.core.candidates.identification. TSIdentifier
   * #identifySequences(src.main.gov.va.vha09.grecc.raptat.gg.datastructures .AnnotatedPhrase)
   */
  @Override
  public List<AnnotatedPhrase> identifySequences(RaptatDocument processedDocument) {
    List<List<RaptatToken>> raptatTokenSequences =
        this.tsEvaluator.getSequencesForAnnotation(processedDocument.getActiveSentences());

    /*
     * Note that this call to getConcepts() can lead to removal of any sequences that do not map to
     * a concept, so the method call may modify the raptatSequences variable
     */
    List<RaptatPair<List<RaptatToken>, String>> raptatPhrasesAndConcepts =
        this.conceptMapEvaluator.getConcepts(raptatTokenSequences, this.acceptedConcepts, false);

    List<AnnotatedPhrase> proposedAnnotations =
        convertToAnnotations(raptatPhrasesAndConcepts, this.invertTokenSequence);

    return proposedAnnotations;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.candidates.coremodifications. annotation
   * .TokenSequenceFinder#setMappedAttributes(java.util.List)
   */
  @Override
  public void setMappedAttributes(List<AnnotatedPhrase> annotations) {
    this.conceptMapEvaluator.setMappedAttributes(annotations);
  }


  public void setSequenceThreshold(double threshold) throws UnsupportedOperationException {
    if (this.tsEvaluator instanceof ProbabilisticSequenceIDEvaluator) {
      ((ProbabilisticSequenceIDEvaluator) this.tsEvaluator).setSequenceThreshold(threshold);
    } else {
      throw new UnsupportedOperationException(
          "Setting of thresholds for phrase identification is only supported for the ProbabilisticSequenceIDEvaluator subclass of SequenceIDEvaluators");
    }
  }


  /**
   * @param tsFinderSolution
   * @param threshold
   */
  private void initializeEvaluators(ProbabilisticTSFinderSolution tsFinderSolution,
      double threshold) {
    ConceptMapSolution conceptMappingSolution = tsFinderSolution.getConceptMapSolution();
    this.invertTokenSequence = conceptMappingSolution.getInvertTokenSequence();

    this.conceptMapEvaluator =
        new NBEvaluator(conceptMappingSolution, OptionsManager.getInstance());
    SequenceIDStructure sequenceIdentifier = tsFinderSolution.getSequenceIdentifier();

    if (sequenceIdentifier instanceof BOWSequenceIDSolution) {
      this.tsEvaluator = new BOWSequenceIDEvaluator((BOWSequenceIDSolution) sequenceIdentifier);
    } else if (sequenceIdentifier instanceof LabeledHashTree) {
      if (this.invertTokenSequence) {
        this.tsEvaluator = new ReverseProbabilisticSequenceIDEvaluator(
            (LabeledHashTree) sequenceIdentifier, threshold);
      } else {
        this.tsEvaluator =
            new ProbabilisticSequenceIDEvaluator((LabeledHashTree) sequenceIdentifier, threshold);
      }
    }
  }
}
