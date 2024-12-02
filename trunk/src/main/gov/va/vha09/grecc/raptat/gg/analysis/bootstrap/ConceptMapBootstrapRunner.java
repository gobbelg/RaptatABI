/** */
package src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.decisiontree.DecisionTreeEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.decisiontree.DecisionTreeTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords.BOWConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords.BOWConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes.NonNBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes.NonNBTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;

/**
 * ********************************************************** ConceptMapBootstrapRunner - Runs a
 * bootstrap analysis on the mapping of a set of annotation data to concepts
 *
 * @author Glenn Gobbel, Mar 6, 2011 *********************************************************
 */
public class ConceptMapBootstrapRunner {

  protected boolean usePOS = RaptatConstants.USE_PARTS_OF_SPEECH;
  protected boolean invertTokenSequence = RaptatConstants.INVERT_TOKEN_SEQUENCE;
  protected boolean removeStopWords = RaptatConstants.REMOVE_STOP_WORDS;
  private boolean useNulls = RaptatConstants.USE_NULLS_START;
  private boolean useTrainingStems = RaptatConstants.USE_TOKEN_STEMMING;
  private ContextHandling reasonNoMedsProcessing =
      OptionsManager.getInstance().getReasonNoMedsProcessing();
  protected ConceptMapEvaluator curEvaluator;
  protected ConceptMapTrainer curTrainer;
  protected String resultFileDirectory;
  private int groupByColumn;

  // Iterations is the number of times to get and fit sample to determine
  // precision, recall, and fMeasure;
  private int iterations;

  // theBootStrapper returns random samples of data
  // from a stored data set
  protected ConceptMapBootstrapAnalyzer theAnalyzer;


  public ConceptMapBootstrapRunner(ConceptMapBootstrapAnalyzer inputAnalyzer,
      OptionsManager theOptions, String resultFileDirectory) {
    this.useNulls = theOptions.getUseNulls();
    this.usePOS = theOptions.getUsePOS();
    this.useTrainingStems = theOptions.getUseStems();
    this.invertTokenSequence = theOptions.getInvertTokenSequence();
    this.removeStopWords = theOptions.getRemoveStopWords();

    this.theAnalyzer = inputAnalyzer;
    this.iterations = theOptions.getBootstrapIterations();
    this.groupByColumn = theOptions.getGrpByColNum();
    this.useTrainingStems = theOptions.getUseStems();
    this.resultFileDirectory = resultFileDirectory;

    String curLearnMethod = theOptions.getCurLearnMethod();
    if (curLearnMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_METHODS[0])) {
      this.curTrainer = new BOWConceptMapTrainer(this.useNulls, this.useTrainingStems, this.usePOS,
          this.invertTokenSequence, this.removeStopWords, this.reasonNoMedsProcessing);
      this.curEvaluator = new BOWConceptMapEvaluator(theOptions);
    }
    if (curLearnMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_METHODS[1])) {
      this.curTrainer = new NBTrainer(this.useNulls, this.useTrainingStems, this.usePOS,
          this.invertTokenSequence, this.removeStopWords, this.reasonNoMedsProcessing);
      this.curEvaluator = new NBEvaluator(theOptions);
    } else if (curLearnMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_METHODS[2])) {
      this.curTrainer = new NonNBTrainer(this.useNulls, this.useTrainingStems);
      this.curEvaluator = new NonNBEvaluator();
    } else if (curLearnMethod.endsWith(RaptatConstants.CONCEPT_MAPPING_METHODS[3])) {
      this.curTrainer = new DecisionTreeTrainer(this.useNulls, this.useTrainingStems);
      this.curEvaluator = new DecisionTreeEvaluator();
    }
  }
}
