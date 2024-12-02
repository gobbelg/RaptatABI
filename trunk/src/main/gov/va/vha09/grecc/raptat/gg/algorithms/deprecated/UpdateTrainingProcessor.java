package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ReverseProbabilisticSequenceIDTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeInputStream;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeOutputStream;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * *
 *
 * @author Shrimalini Jayaramaraja VHATVHJAYARS
 */
@Deprecated
public class UpdateTrainingProcessor extends TrainingProcessor {

  // path where the updated list of unlabeledHashtrees are serialized
  private ProbabilisticTSFinderSolution updatedAlgorithmSolution;
  private File newSolutionFile;
  private final String newUHTSerialPath;
  private UnlabeledHashTreeInputStream newUHTInstream;
  private UnlabeledHashTreeOutputStream newUHTOutstream;


  /**
   * Constructor for an object to update an existing AlgorithmSolution with new data. Note that all
   * training options are set via the existing AlgorithmSolution instance
   *
   * @author Glenn Gobbel - Jun 22, 2012 modified by Sanjib Saha - July 21, 2014
   * @param solutionToUpdate
   * @param newSolution
   */
  public UpdateTrainingProcessor(File newSolution, ProbabilisticTSFinderSolution solutionToUpdate) {
    this.theAlgorithmSolution = solutionToUpdate;
    this.newSolutionFile = newSolution;
    this.newUHTInstream = null;
    this.newUHTOutstream = null;

    this.keepUnlabeledOnDisk = OptionsManager.getInstance().getKeepUnlabeledOnDisk();

    this.newUHTSerialPath = this.newSolutionFile.getParent() + File.separator
        + GeneralHelper.removeFileExtension(this.newSolutionFile.getName(), '.') + "_"
        + System.currentTimeMillis() + ".rpt";

    if (this.keepUnlabeledOnDisk) {
      this.newUHTInstream = new UnlabeledHashTreeInputStream(this.newUHTSerialPath);
      this.newUHTOutstream = new UnlabeledHashTreeOutputStream(this.newUHTSerialPath);
    }

    // Get the previous solution to be updated and set options based on it
    this.maxTokenNumber = this.theAlgorithmSolution.getTrainingTokenNumber();
    this.acceptedConcepts = this.theAlgorithmSolution.getAcceptableConcepts();

    NBSolution conceptMapSolution = (NBSolution) this.theAlgorithmSolution.getConceptMapSolution();
    /*
     * Commented out code below as it seemed to no longer be needed after adding attribute mapping
     * in Glenn's base code
     */
    // conceptMapSolution.initializeTransients();
    setOptionFields(conceptMapSolution);

    TrainingProcessor.theTextAnalyzer.setTokenProcessingParameters(this.useStems, this.usePOS,
        this.removeStopWords, this.maxTokenNumber, this.reasonNoMedsProcessing);

    String previousUHTSerialPath = this.theAlgorithmSolution.getUnlabeledHashTreePath();

    /*
     * Consider whether previousUHTSerialPath associated file may have moved along with solution
     */
    File previousUHTFile = new File(previousUHTSerialPath);
    if (!previousUHTFile.exists()) {
    }

    SequenceIDStructure sequenceIDStructure = this.theAlgorithmSolution.getSequenceIdentifier();
    if (this.invertTokenSequence) {
      if (sequenceIDStructure instanceof LabeledHashTree) {
        this.tsiTrainer =
            new ReverseProbabilisticSequenceIDTrainer((LabeledHashTree) sequenceIDStructure,
                this.theAlgorithmSolution.getSmoothingMethod(), this.newUHTInstream,
                this.newUHTOutstream, this.newUHTSerialPath, previousUHTSerialPath);
      } else if (sequenceIDStructure instanceof BOWSequenceIDSolution) {
        this.tsiTrainer = new BOWSequenceIDTrainer((BOWSequenceIDSolution) sequenceIDStructure);
      }
    } else {
      if (sequenceIDStructure instanceof LabeledHashTree) {
        this.tsiTrainer = new ProbabilisticSequenceIDTrainer((LabeledHashTree) sequenceIDStructure,
            this.theAlgorithmSolution.getSmoothingMethod(), this.newUHTInstream,
            this.newUHTOutstream, this.newUHTSerialPath, previousUHTSerialPath);
      } else if (sequenceIDStructure instanceof BOWSequenceIDSolution) {
        this.tsiTrainer = new BOWSequenceIDTrainer((BOWSequenceIDSolution) sequenceIDStructure);
      }
    }

    if (sequenceIDStructure instanceof LabeledHashTree && this.keepUnlabeledOnDisk) {
      UnlabeledHashTreeInputStream previousUHTInstream =
          new UnlabeledHashTreeInputStream(previousUHTSerialPath);
      previousUHTInstream.copyTo(this.newUHTOutstream);
    }

    this.conceptMappingTrainer = new NBTrainer(conceptMapSolution, true);
  }


  /**
   * Constructor for an object to update an existing AlgorithmSolution with new data. Note that all
   * training options are set via the existing AlgorithmSolution instance
   *
   * @author Sanjib Saha - July 22, 2014
   * @param solutionToUpdate The existing solution object
   * @param newSolution New solution file
   * @param solutionFileToUpdate Existing solution file
   */
  public UpdateTrainingProcessor(File newSolution, ProbabilisticTSFinderSolution solutionToUpdate,
      File solutionFileToUpdate) {
    this.theAlgorithmSolution = solutionToUpdate;
    this.newSolutionFile = newSolution;
    this.newUHTInstream = null;
    this.newUHTOutstream = null;

    this.keepUnlabeledOnDisk = OptionsManager.getInstance().getKeepUnlabeledOnDisk();

    this.newUHTSerialPath = this.newSolutionFile.getParent() + File.separator
        + GeneralHelper.removeFileExtension(this.newSolutionFile.getName(), '.') + ".rpt";

    if (this.keepUnlabeledOnDisk) {
      this.newUHTInstream = new UnlabeledHashTreeInputStream(this.newUHTSerialPath);
      this.newUHTOutstream = new UnlabeledHashTreeOutputStream(this.newUHTSerialPath);
    }

    // Get the previous solution to be updated and set options based on it
    this.maxTokenNumber = this.theAlgorithmSolution.getTrainingTokenNumber();
    this.acceptedConcepts = this.theAlgorithmSolution.getAcceptableConcepts();

    NBSolution conceptMapSolution = (NBSolution) this.theAlgorithmSolution.getConceptMapSolution();
    /*
     * Commented out code below as it seemed to no longer be needed after adding attribute mapping
     * in Glenn's base code
     */
    // conceptMapSolution.initializeTransients();
    setOptionFields(conceptMapSolution);

    TrainingProcessor.theTextAnalyzer.setTokenProcessingParameters(this.useStems, this.usePOS,
        this.removeStopWords, this.maxTokenNumber, this.reasonNoMedsProcessing);

    // The UHT file is searched in the same directory where the solution
    // file is located
    // instead of the saved locaiton in the AlgorithmSolution class
    String prevSolutionFilePath = solutionFileToUpdate.getAbsolutePath();
    String previousUHTSerialPath = prevSolutionFilePath.replaceAll("\\.soln", "") + ".rpt";

    SequenceIDStructure sequenceIDStructure = this.theAlgorithmSolution.getSequenceIdentifier();
    if (this.invertTokenSequence) {
      if (sequenceIDStructure instanceof LabeledHashTree) {
        this.tsiTrainer =
            new ReverseProbabilisticSequenceIDTrainer((LabeledHashTree) sequenceIDStructure,
                this.theAlgorithmSolution.getSmoothingMethod(), this.newUHTInstream,
                this.newUHTOutstream, this.newUHTSerialPath, previousUHTSerialPath);
      } else if (sequenceIDStructure instanceof BOWSequenceIDSolution) {
        this.tsiTrainer = new BOWSequenceIDTrainer((BOWSequenceIDSolution) sequenceIDStructure);
      }
    } else {
      if (sequenceIDStructure instanceof LabeledHashTree) {
        this.tsiTrainer = new ProbabilisticSequenceIDTrainer((LabeledHashTree) sequenceIDStructure,
            this.theAlgorithmSolution.getSmoothingMethod(), this.newUHTInstream,
            this.newUHTOutstream, this.newUHTSerialPath, previousUHTSerialPath);
      } else if (sequenceIDStructure instanceof BOWSequenceIDSolution) {
        this.tsiTrainer = new BOWSequenceIDTrainer((BOWSequenceIDSolution) sequenceIDStructure);
      }
    }

    if (sequenceIDStructure instanceof LabeledHashTree && this.keepUnlabeledOnDisk) {
      UnlabeledHashTreeInputStream previousUHTInstream =
          new UnlabeledHashTreeInputStream(previousUHTSerialPath);
      previousUHTInstream.copyTo(this.newUHTOutstream);
    }

    this.conceptMappingTrainer = new NBTrainer(conceptMapSolution, true);
  }


  /**
   * @param newSolutionFile the newSolutionFile to set
   */
  public void setNewSolutionFile(File newSolutionFile) {
    this.newSolutionFile = newSolutionFile;
  }


  @Override
  public void updateTraining(List<AnnotationGroup> trainingGroups, boolean saveUnlabeledToDisk) {
    for (AnnotationGroup curGroup : trainingGroups) {
      this.tsiTrainer.updatePreviousTraining(curGroup);
      this.conceptMappingTrainer.updateTraining(curGroup.referenceAnnotations);
    }

    if (this.tsiTrainer instanceof ProbabilisticSequenceIDTrainer) {
      // Write any unsaved UnlabeledHashTree instance to disk
      // if indicated
      if (saveUnlabeledToDisk) {
        ((ProbabilisticSequenceIDTrainer) this.tsiTrainer).closeTreeStreams();
      }
    }

    this.updateAlgorithmSolution(this.newUHTSerialPath, new ArrayList<SchemaConcept>());

    if (this.newSolutionFile != null) {
      this.theAlgorithmSolution.saveToFile(this.newSolutionFile);
    }
  }


  public ProbabilisticTSFinderSolution updateTrainingTest(List<AnnotationGroup> trainingGroups) {
    for (AnnotationGroup curGroup : trainingGroups) {
      this.tsiTrainer.updatePreviousTraining(curGroup);
      this.conceptMappingTrainer.updateTraining(curGroup.referenceAnnotations);
    }

    this.updateAlgorithmSolution(this.newUHTSerialPath, new ArrayList<SchemaConcept>());

    return this.theAlgorithmSolution;
  }


  /**
   * *********************************************************** Used to set the fields that
   * determine options used for training, and the fields are based on whatever is stored in the
   * previous solution file
   *
   * @param conceptMapSolution
   * @author Glenn Gobbel - Jun 15, 2012 *
   *         **********************************************************
   */
  private void setOptionFields(NBSolution conceptMapSolution) {
    this.useStems = conceptMapSolution.getUseStems();
    this.usePOS = conceptMapSolution.getUsePOS();
    this.removeStopWords = conceptMapSolution.getRemoveStopWords();
    this.invertTokenSequence = conceptMapSolution.getInvertTokenSequence();
    this.useNulls = conceptMapSolution.getUseNulls();
    this.reasonNoMedsProcessing = conceptMapSolution.getReasonNotOnMedsProcessing();
  }
}
