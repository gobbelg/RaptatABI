package src.main.gov.va.vha09.grecc.raptat.gg.core.deprecated;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.SequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes.BOWSequenceIDSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticSequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ReverseProbabilisticSequenceIDEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationDataIndex;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.LabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.AttributeSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextAttributeWriter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ReasonNoMedsAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

@Deprecated
public class AnnotationFinder {

  private final List<String> annotationResultPaths = new ArrayList<>();
  private ProbabilisticTSFinderSolution theSolution;
  private String saveResultDirectory = null;
  private double sequenceThreshold = RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT;
  private final ContextAttributeWriter contextWriter = new ContextAttributeWriter();
  // This is typically used to sift through and only keep annotations that
  // have the "proper" context, where "proper" depends on the use case
  private ContextSifter contextSifter = null;

  private AttributeSifter attributeSifter = null;

  private final List<String> defaultContexts = Arrays.asList("reason not on meds");
  private TokenContextAnalyzer tokenContextAnalyzer;
  private UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;
  private static StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();
  private static Logger logger = Logger.getLogger(AnnotationFinder.class);


  // Convenience constructor used by Raptat-eHost Integrated project
  public AnnotationFinder(double sequenceThreshold) {
    this.sequenceThreshold = sequenceThreshold;
  }


  public AnnotationFinder(ProbabilisticTSFinderSolution inSolution) {
    this.theSolution = inSolution;
  }


  public AnnotationFinder(ProbabilisticTSFinderSolution inSolution, String fileDirectory) {
    this.theSolution = inSolution;
    this.saveResultDirectory = fileDirectory;
  }


  public AnnotationFinder(ProbabilisticTSFinderSolution inSolution, String fileDirectory,
      double sequenceThreshold) {
    this(inSolution, fileDirectory);
    this.sequenceThreshold = sequenceThreshold;
  }


  /**
   * @param annotationGroups
   * @return
   */
  public int activeLearningCountPhrases(List<AnnotationGroup> annotationGroups) {
    GeneralHelper.errorWriter("Not Implemented - get code from Keaton Morgan");
    return 0;
  }


  /**
   * @param phrasesAndConcepts
   * @param invertTokenSequence
   * @param markAsAssigned
   * @return The sequences annotated with concepts, returned as a list of annotated phrases. *
   * @author Glenn Gobbel - Jun 16, 2012
   */
  public List<AnnotatedPhrase> convertToAnnotations(
      List<RaptatPair<List<RaptatToken>, String>> phrasesAndConcepts, boolean invertTokenSequence,
      boolean markAsAssigned) {
    String annotationData[] = new String[6];
    List<AnnotatedPhrase> theResults = new ArrayList<>(phrasesAndConcepts.size());

    if (invertTokenSequence) {
      for (RaptatPair<List<RaptatToken>, String> curPhrase : phrasesAndConcepts) {
        List<RaptatToken> curTokenSequence = curPhrase.left;
        RaptatToken.markRaptatConceptAssigned(curTokenSequence, markAsAssigned);
        Collections.sort(curTokenSequence, AnnotationFinder.tokenSorter);
        annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = this.idGenerator.getUnique();
        annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] =
            curTokenSequence.get(0).getStartOffset();
        annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] =
            curTokenSequence.get(curTokenSequence.size() - 1).getEndOffset();
        annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = curPhrase.right;
        theResults.add(new AnnotatedPhrase(annotationData, curTokenSequence));
      }
    } else {
      for (RaptatPair<List<RaptatToken>, String> curPhrase : phrasesAndConcepts) {
        List<RaptatToken> curTokenSequence = curPhrase.left;
        RaptatToken.markRaptatConceptAssigned(curTokenSequence, markAsAssigned);
        annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = this.idGenerator.getUnique();
        annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] =
            curTokenSequence.get(0).getStartOffset();
        annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] =
            curTokenSequence.get(curTokenSequence.size() - 1).getEndOffset();
        annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = curPhrase.right;
        theResults.add(new AnnotatedPhrase(annotationData, curTokenSequence));
      }
    }
    return theResults;
  }


  public void createDefaultContextAnalyzer() {
    this.contextWriter.addAnalyzer(new ReasonNoMedsAnalyzer());
  }


  public void createDefaultContextSifter() {
    GeneralHelper.errorWriter("Not implemented - generally used for ReasonNoMeds annotation");
    System.exit(-1);
  }


  public List<String> getPathsToAnnotations() {
    return this.annotationResultPaths;
  }


  /**
   * @param annotationGroups
   * @param acceptableConcepts
   * @param writeToDisk
   * @return boolean - indicates if the phrase identification was completed successfully
   * @author Glenn Gobbel - Jun 17, 2012
   */
  public boolean identifyPhrasesAndAnnotations(List<AnnotationGroup> annotationGroups,
      HashSet<String> acceptableConcepts, boolean writeToDisk) {
    boolean invertTokenSequence;
    SequenceIDEvaluator tsEvaluator = null;

    OptionsManager theOptions = OptionsManager.getInstance();
    this.annotationResultPaths.clear();
    theOptions.setAnnotationResultPaths(this.annotationResultPaths);

    ConceptMapSolution conceptMappingSolution = this.theSolution.getConceptMapSolution();
    invertTokenSequence = conceptMappingSolution.getInvertTokenSequence();

    ConceptMapEvaluator theConceptMapper =
        new NBEvaluator(conceptMappingSolution, theOptions.getCurFilter());
    SequenceIDStructure sequenceIdentifier = this.theSolution.getSequenceIdentifier();

    if (sequenceIdentifier instanceof BOWSequenceIDSolution) {
      tsEvaluator = new BOWSequenceIDEvaluator((BOWSequenceIDSolution) sequenceIdentifier);
    } else if (sequenceIdentifier instanceof LabeledHashTree) {
      if (invertTokenSequence) {
        tsEvaluator = new ReverseProbabilisticSequenceIDEvaluator(
            (LabeledHashTree) sequenceIdentifier, this.sequenceThreshold);
      } else {
        tsEvaluator = new ProbabilisticSequenceIDEvaluator((LabeledHashTree) sequenceIdentifier,
            this.sequenceThreshold);
      }
    }

    if (tsEvaluator != null) {
      int fileIndex = 0;
      for (AnnotationGroup group : annotationGroups) {
        RaptatDocument raptatDocument = group.getRaptatDocument();
        String docPath = raptatDocument.getTextSourcePath().get();

        List<List<RaptatToken>> raptatTokenSequences =
            tsEvaluator.getSequencesForAnnotation(raptatDocument.getActiveSentences());

        /*
         * Note that this call to getConcepts() can lead to removal of any sequences that do not map
         * to a concept, so the method call may modify the raptatSequences variable
         */
        List<RaptatPair<List<RaptatToken>, String>> raptatPhrasesAndConcepts =
            theConceptMapper.getConcepts(raptatTokenSequences, acceptableConcepts, false);

        List<AnnotatedPhrase> proposedAnnotations =
            this.convertToAnnotations(raptatPhrasesAndConcepts, invertTokenSequence);

        /*
         * This adds dictionary annotations to the proposed annotations for each dictionary
         * annotation that does not overlap one of the proposed annotations. That is, proposed
         * annotations take precedence over dictionary annotations.
         */
        boolean clearPreviousAttributes = true;
        addDictionaryAnnotations(raptatDocument.getDictionaryAnnotations(clearPreviousAttributes),
            proposedAnnotations);

        if (this.contextSifter != null) {
          this.contextSifter.siftUsingContext(proposedAnnotations);
        }

        /*
         * Add attributes to the annotations that were found and not removed by the contextSifter
         */
        theConceptMapper.setMappedAttributes(proposedAnnotations);
        setContextualAttributes(proposedAnnotations);

        if (this.attributeSifter != null) {
          this.attributeSifter.sift(proposedAnnotations);
        }

        group.setRaptatAnnotations(proposedAnnotations);

        if (writeToDisk) {
          writeAnnotationsToDisk(docPath, fileIndex++, group.raptatAnnotations);
        }
      }
      return true;
    } else {
      GeneralHelper.errorWriter("Unknown type of sequence identifier");
      return false;
    }
  }


  public void setSaveResultDirectory(String theDirectory) {
    this.saveResultDirectory = theDirectory;
  }


  /**
   * @param sequenceThreshold the sequenceThreshold to set
   */
  public void setSequenceThreshold(double sequenceThreshold) {
    this.sequenceThreshold = sequenceThreshold;
  }


  public void setSolution(ProbabilisticTSFinderSolution inSolution) {
    this.theSolution = inSolution;
  }


  /**
   * Adds dictionary annotations that have not already been found by RapTAT to the list,
   * annotations. Also finds and adds attributes for the phrases.
   *
   * @param dictionaryAnnotations
   * @param annotations
   */
  private void addDictionaryAnnotations(List<AnnotatedPhrase> dictionaryAnnotations,
      List<AnnotatedPhrase> annotations) {
    annotatedGroupLoop: for (AnnotatedPhrase curAnnotation : dictionaryAnnotations) {
      List<RaptatToken> annotationTokenSequence = curAnnotation.getProcessedTokens();
      for (RaptatToken curToken : annotationTokenSequence) {
        if (curToken.raptatConceptAssigned()) {
          break annotatedGroupLoop;
        }
      }

      annotations.add(curAnnotation);
    }
  }


  /**
   * @param raptatSequences
   * @return The sequences annotated with concepts, returned as a list of annotated phrases.
   * @author Glenn Gobbel - Jun 16, 2012
   * @param invertTokenSequence
   */
  private List<AnnotatedPhrase> convertToAnnotations(
      List<RaptatPair<List<RaptatToken>, String>> phrasesAndConcepts, boolean invertTokenSequence) {
    String annotationData[] = new String[6];
    List<AnnotatedPhrase> theResults = new ArrayList<>(phrasesAndConcepts.size());

    if (invertTokenSequence) {
      for (RaptatPair<List<RaptatToken>, String> curPhrase : phrasesAndConcepts) {
        List<RaptatToken> curTokenSequence = curPhrase.left;
        Collections.sort(curTokenSequence, AnnotationFinder.tokenSorter);
        annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = this.idGenerator.getUnique();
        annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] =
            curTokenSequence.get(0).getStartOffset();
        annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] =
            curTokenSequence.get(curTokenSequence.size() - 1).getEndOffset();
        annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = curPhrase.right;
        theResults.add(new AnnotatedPhrase(annotationData, curTokenSequence));
      }
    } else {
      for (RaptatPair<List<RaptatToken>, String> curPhrase : phrasesAndConcepts) {
        List<RaptatToken> curTokenSequence = curPhrase.left;
        annotationData[AnnotationDataIndex.MENTION_ID.ordinal()] = this.idGenerator.getUnique();
        annotationData[AnnotationDataIndex.START_OFFSET.ordinal()] =
            curTokenSequence.get(0).getStartOffset();
        annotationData[AnnotationDataIndex.END_OFFSET.ordinal()] =
            curTokenSequence.get(curTokenSequence.size() - 1).getEndOffset();
        annotationData[AnnotationDataIndex.CONCEPT_NAME.ordinal()] = curPhrase.right;
        theResults.add(new AnnotatedPhrase(annotationData, curTokenSequence));
      }
    }
    return theResults;
  }


  private void setContextualAttributes(List<AnnotatedPhrase> proposedAnnotations) {
    for (AnnotatedPhrase curPhrase : proposedAnnotations) {
      List<RaptatAttribute> curAttributes = curPhrase.getPhraseAttributes();
      List<RaptatToken> curTokenSequence = curPhrase.getProcessedTokens();
      List<RaptatAttribute> phraseAttributes =
          this.contextWriter.getSequenceAttributes(curTokenSequence);
      curAttributes.addAll(phraseAttributes);
    }
  }


  private boolean writeAnnotationsToDisk(String pathToTextSource, int fileIndex,
      List<AnnotatedPhrase> raptatPhrases) {
    NumberFormat formatter = new DecimalFormat("0000");

    // Serialize the results to same location as the Solution file.
    // Make sure that each file in this batch has a unique name because
    // some text files may be repeated during bootstrapping.
    String textFileName =
        GeneralHelper.removeFileExtension(new File(pathToTextSource).getName(), '.');
    String curFileIndex = formatter.format(fileIndex++);
    String pathToResultForTextFile = new File(this.saveResultDirectory).getAbsolutePath()
        + File.separator + textFileName + "_" + curFileIndex + ".ser";
    try {
      ObjectOutputStream outStream = new ObjectOutputStream(
          new BufferedOutputStream(new FileOutputStream(new File(pathToResultForTextFile)),
              RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
      outStream.writeObject(raptatPhrases);
      outStream.writeObject(pathToTextSource);
      outStream.close();
      this.annotationResultPaths.add(pathToResultForTextFile);
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
      return false;
    }

    return true;
  }
}
