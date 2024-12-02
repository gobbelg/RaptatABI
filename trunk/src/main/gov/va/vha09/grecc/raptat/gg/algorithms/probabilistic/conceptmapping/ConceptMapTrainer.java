package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import opennlp.tools.postag.POSTagger;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.ConceptMapBootstrapSampler;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.lexicaltools.Lemmatiser;

/**
 * ************************************************************ Interface to assure that all
 * trainers of machine learning systems contain a getSolution method.
 *
 * @author Glenn Gobbel, Sep 9, 2010 ***********************************************************
 */
public abstract class ConceptMapTrainer {
  protected ConceptMapSolution theSolution;
  protected boolean solutionInitiated = false;
  protected boolean useNulls = RaptatConstants.USE_NULLS_START;
  protected boolean convertTokensToStems = RaptatConstants.USE_TOKEN_STEMMING;
  protected boolean addPOSToTokens = RaptatConstants.USE_PARTS_OF_SPEECH;
  protected boolean invertTokenSequence = RaptatConstants.INVERT_TOKEN_SEQUENCE;
  protected boolean removeStopWords = RaptatConstants.REMOVE_STOP_WORDS;
  protected int maxElements = RaptatConstants.MAX_TOKENS_DEFAULT; // maximum number of tokens

  protected ContextHandling reasonNoMedsProcessing =
      OptionsManager.getInstance().getReasonNoMedsProcessing();
  protected POSTagger posTagger = null;
  protected Lemmatiser stemmer = null;


  public ConceptMapTrainer() {}


  public ConceptMapTrainer(boolean useNulls, boolean useStems) {
    this();
    this.useNulls = useNulls;
    this.convertTokensToStems = useStems;
  }


  public ConceptMapTrainer(boolean useNulls, boolean useStems, boolean usePOS) {
    this(useNulls, useStems);
    this.addPOSToTokens = usePOS;
  }


  public ConceptMapTrainer(boolean useNulls, boolean useStems, boolean usePOS,
      boolean invertTokenSequence, boolean removeStopWords,
      ContextHandling reasonNoMedsProcessing) {
    this();
    this.useNulls = useNulls;
    this.convertTokensToStems = useStems;
    this.addPOSToTokens = usePOS;
    this.invertTokenSequence = invertTokenSequence;
    this.removeStopWords = removeStopWords;
    this.reasonNoMedsProcessing = reasonNoMedsProcessing;
  }


  /**
   * @param inputSolution
   * @author Glenn Gobbel - Jun 10, 2012
   */
  public ConceptMapTrainer(ConceptMapSolution inputSolution) {
    this.theSolution = inputSolution;
    this.solutionInitiated = true;
    this.useNulls = inputSolution.getUseNulls();
    this.convertTokensToStems = inputSolution.getUseStems();
    this.addPOSToTokens = inputSolution.getUsePOS();
    this.invertTokenSequence = inputSolution.getInvertTokenSequence();
    this.removeStopWords = inputSolution.getRemoveStopWords();
  }


  public void clearSolution() {
    this.solutionInitiated = false;
    this.theSolution = null;
  }


  public ConceptMapSolution getCurrentSolution() {
    return this.theSolution;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Trainer#getSolution(java .util.List)
   */
  public ConceptMapSolution getSolution(ConceptMapBootstrapSampler theAnnotations) {
    if (!this.solutionInitiated) {
      int startTokensPosition = theAnnotations.myReader.getStartTokensPosition();
      this.createSolution(theAnnotations.getAnnotationData(), startTokensPosition);
    }

    return this.theSolution;
  }


  /**
   * Get the database created for the training data, which represents the solution
   *
   * @param theData
   * @return NaiveBayesSolution
   */
  public ConceptMapSolution getSolution(DataImportReader theData) {
    if (!this.solutionInitiated) {
      this.createSolution(theData);
    }

    return this.theSolution;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Trainer#getSolution(java .util.List)
   */
  public ConceptMapSolution getSolution(List<String[]> theData, int startTokensPosition) {
    if (!this.solutionInitiated) {
      this.createSolution(theData, startTokensPosition);
    }

    return this.theSolution;
  }


  public boolean getUseNulls() {
    return this.useNulls;
  }


  public boolean getUseStems() {
    return this.convertTokensToStems;
  }


  public void setSolution(ConceptMapSolution curSolution) {
    this.theSolution = curSolution;
    this.solutionInitiated = true;
  }


  public ConceptMapSolution updateSolution(DataImportReader theData) {
    if (!this.solutionInitiated) {
      this.createSolution(theData);
    } else {
      if (this.useNulls) {
        this.addToSolutionUseNulls(theData, theData.getStartTokensPosition(),
            theData.getNumPhraseTokens() + 1);
      } else {
        this.addToSolution(theData, theData.getStartTokensPosition(),
            theData.getNumPhraseTokens() + 1);
      }
    }
    return this.theSolution;
  }


  public ConceptMapSolution updateTraining(List<AnnotatedPhrase> theAnnotations) {
    if (!this.solutionInitiated) {
      this.createSolution(theAnnotations);
    } else {
      if (this.useNulls) {
        this.addToSolutionUseNulls(theAnnotations.listIterator(), theAnnotations.size());
      } else {
        this.addToSolution(theAnnotations.listIterator(), theAnnotations.size());
      }
    }
    return this.theSolution;
  }


  protected abstract Vector<Long> addToSolution(DataImportReader trainingData,
      int startTokensPosition, int numElementsForAnalysis);


  protected abstract Vector<Long> addToSolution(ListIterator<AnnotatedPhrase> dataIterator,
      int dataSize);


  protected abstract Vector<Long> addToSolution(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns);


  protected abstract Vector<Long> addToSolutionUseNulls(DataImportReader trainingData,
      int startTokensPosition, int numElementsForAnalysis);


  protected abstract Vector<Long> addToSolutionUseNulls(ListIterator<AnnotatedPhrase> dataIterator,
      int dataSize);


  protected abstract Vector<Long> addToSolutionUseNulls(ListIterator<String[]> dataIterator,
      int dataSize, int numGroupingColumns);


  protected abstract void createSolution(DataImportReader theData);


  protected abstract void createSolution(List<AnnotatedPhrase> theAnnotations);


  protected abstract void createSolution(List<String[]> theData, int groupingColumns);
}
