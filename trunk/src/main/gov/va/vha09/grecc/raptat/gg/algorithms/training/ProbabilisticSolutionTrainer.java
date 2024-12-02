/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.training;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ReverseProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TokenSequenceSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

/** @author Glenn T. Gobbel Feb 16, 2016 */
public class ProbabilisticSolutionTrainer extends TokenSequenceSolutionTrainer {

  private String uhtSerialPath;
  private SequenceIDTrainer tsiTrainer;
  private NBTrainer conceptMappingTrainer;

  private int maxTokenNumber;

  /*
   * The variable keepUnlabeledOnDisk is used to keep the UnlabeledHashTree in memory during the
   * generation of a single solution. This is not generally used unless memory limits the generation
   * of a solution. The UnlabeledHashTree is often the largest language model data structure.
   */
  private boolean keepUnlabeledOnDisk;

  /*
   * The variable saveUnlabeledToDisk is distinct from keepUnlabeledOnDisk. The variable
   * saveUnlabeledOnDisk determines whether, when training is updated, the UnlabeledHashTree
   * instance is saved to disk, which will allow updating of a probabilistic tsiTrainer solution.
   * Without the UnlabeledHashTree instance , the solution cannot be validly updated.
   */
  private boolean saveUnlabeledToDisk;


  public ProbabilisticSolutionTrainer(TokenProcessingOptions tokenOptions,
      PhraseIDTrainOptions phraseIDOptions, HashSet<String> trainingConcepts, File solutionFile,
      boolean keepUnlabeledOnDisk, boolean saveUnlabeledToDisk, TextAnalyzer textAnalyzer) {
    super(textAnalyzer, trainingConcepts);
    this.uhtSerialPath = solutionFile.getParent() + File.separator
        + GeneralHelper.removeFileExtension(solutionFile.getName(), '.') + "_"
        + GeneralHelper.getTimeStamp() + ".rpt";
    this.maxTokenNumber = tokenOptions.getMaxTokenNumber();
    this.keepUnlabeledOnDisk = keepUnlabeledOnDisk;
    this.saveUnlabeledToDisk = saveUnlabeledToDisk;
    initializeSequenceIDTrainer(phraseIDOptions, tokenOptions.invertTokenSequence(),
        keepUnlabeledOnDisk);
    initializeConceptMappingTrainer(tokenOptions);
  }


  /** */
  public void clearTraining() {
    this.tsiTrainer.clearTraining();
    this.conceptMappingTrainer.clearSolution();
  }


  /** @return the tsiTrainer */
  @Override
  public SequenceIDTrainer getTsiTrainer() {
    return this.tsiTrainer;
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
    TokenSequenceSolutionTrainer.LOGGER.info("Updating token sequence finder probabilities");
    this.tsiTrainer.updateProbabilities();

    /*
     * If tsiTrainer is a ProbabilisticSequenceIDTrainer, save the UnlabeledHashTree data structure
     * of the ProbabilisticSequenceIDTrainer instance to disk so that it can be updated later with
     * new training (if desired).
     */
    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer & this.saveUnlabeledToDisk) {
      /*
       * This does the actual writing to disk
       */
      ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).closeTreeStreams();
    }

    TokenSequenceSolutionTrainer.LOGGER
        .info("Creating probabilistic token sequence finder solution object");
    TokenSequenceFinderSolution solution = new ProbabilisticTSFinderSolution(this.uhtSerialPath,
        this.tsiTrainer.getSequenceIDStructure(), this.conceptMappingTrainer.getCurrentSolution(),
        this.trainingConcepts, this.maxTokenNumber, this.tsiTrainer.getPhraseIDSmoothingMethod(),
        schemaConceptList);
    TokenSequenceSolutionTrainer.LOGGER.info("Solution object creation complete");

    return solution;
  }


  /*
   * Note that a call to this method does NOT add unlabeled occurrences from the UnlabeledHashTree
   * to the LabeledHashTree associated with training (if the tsiTrainer instance is a
   * ProbabilisticSequenceIDTrainer). So, this adding of unlabeled to labeled has to be handled by
   * the caller.
   */
  @Override
  public void updateTraining(AnnotationGroup trainingGroup) {
    TokenSequenceSolutionTrainer.LOGGER.debug(
        "Updating sequence training for:" + trainingGroup.getRaptatDocument().getTextSource());
    this.tsiTrainer.updateTraining(trainingGroup);

    TokenSequenceSolutionTrainer.LOGGER
        .debug("Updating concept mapping for:" + trainingGroup.getRaptatDocument().getTextSource());
    this.conceptMappingTrainer.updateTraining(trainingGroup.referenceAnnotations);

    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
      /*
       * Write any unsaved UnlabeledHashTree instance to disk if indicated. Turn saving off if you
       * will not be updating the solution files
       */
      if (this.keepUnlabeledOnDisk) {
        ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).closeTreeStreams();
      }
    }
  }


  /**
   * Updates occurrences of labeled (and unlabeled tokens) for the corresponding data structures,
   * but does not update probabilities calculated from those occurrences.
   *
   * @param trainingGroups
   */
  public void updateTraining(List<AnnotationGroup> trainingGroups) {
    for (AnnotationGroup annotationGroup : trainingGroups) {
      this.updateTraining(annotationGroup);
    }

    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
      ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).addUnlabeledToLabeledTree();
    }
  }


  /**
   * @param tokenOptions
   */
  private void initializeConceptMappingTrainer(TokenProcessingOptions tokenOptions) {

    this.conceptMappingTrainer = new NBTrainer(RaptatConstants.USE_NULLS_START,
        tokenOptions.useStems(), tokenOptions.usePOS(), tokenOptions.invertTokenSequence(),
        tokenOptions.removeStopWords(), tokenOptions.getReasonNoMedsProcessing());
  }


  /**
   * @param phraseIDOptions
   * @param invertTokenSequence
   * @param keepUnlabeledOnDisk
   */
  private void initializeSequenceIDTrainer(PhraseIDTrainOptions phraseIDOptions,
      boolean invertTokenSequence, boolean keepUnlabeledOnDisk) {
    TSFinderMethod identificationMethod = phraseIDOptions.getPhraseIDMethod();
    PhraseIDSmoothingMethod smoothingMethod = phraseIDOptions.getSmoothingMethod();

    if (invertTokenSequence) {
      /*
       * Note that invertTokenSequence option not implemented for BAGOFWORDS
       */
      if (identificationMethod.equals(TSFinderMethod.BAGOFWORDS)) {
        TokenSequenceSolutionTrainer.LOGGER.info("Using BOW and inverted sequence for sequence ID");
        this.tsiTrainer = new BOWSequenceIDTrainer(phraseIDOptions);
      } else if (identificationMethod.equals(TSFinderMethod.PROBABILISTIC)) {
        TokenSequenceSolutionTrainer.LOGGER
            .info("Using Probabilistic method and inverted sequence for sequence ID");
        this.tsiTrainer = new ReverseProbabilisticSequenceIDTrainer(this.uhtSerialPath,
            keepUnlabeledOnDisk, smoothingMethod);
      }
    } else {
      if (identificationMethod.equals(TSFinderMethod.BAGOFWORDS)) {
        TokenSequenceSolutionTrainer.LOGGER.info("Using BOW and standard sequence for sequence ID");
        this.tsiTrainer = new BOWSequenceIDTrainer(phraseIDOptions);
      } else if (identificationMethod.equals(TSFinderMethod.PROBABILISTIC)) {
        TokenSequenceSolutionTrainer.LOGGER
            .info("Using Probabilistic method and standard sequence for sequence ID");
        this.tsiTrainer = new ProbabilisticSequenceIDTrainer(this.uhtSerialPath,
            keepUnlabeledOnDisk, smoothingMethod);
      }
    }
  }
}
