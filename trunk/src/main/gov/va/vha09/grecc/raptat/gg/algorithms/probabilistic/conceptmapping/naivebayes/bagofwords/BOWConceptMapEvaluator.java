/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.bagofwords;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.LemmatiserFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.POSTaggerFactory;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - May 16, 2012 *****************************************************
 */
public class BOWConceptMapEvaluator extends NBEvaluator {

  /**
   * ********************************************
   *
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public BOWConceptMapEvaluator() {
    super();
  }


  /**
   * ********************************************
   *
   * @param nbSolution
   * @param nbOptions
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public BOWConceptMapEvaluator(ConceptMapSolution bowSolution, OptionsManager bowOptions) {
    setFiltering(bowOptions.getCurFilter());
    setFieldsFromSolution(bowSolution);
  }


  /**
   * ********************************************
   *
   * @param nbSolution
   * @param nbOptions
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public BOWConceptMapEvaluator(ConceptMapSolution bowSolution, String inFilter) {
    setFiltering(inFilter);
    setFieldsFromSolution(bowSolution);
  }


  /**
   * ********************************************
   *
   * @param nbOptions
   * @author Glenn Gobbel - May 16, 2012 ********************************************
   */
  public BOWConceptMapEvaluator(OptionsManager bowOptions) {
    super(bowOptions);
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms .Evaluator#setSolution(
   * src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution)
   */
  @Override
  public void setFieldsFromSolution(ConceptMapSolution theSolution) {
    this.conceptCalculator =
        new BOWConceptMapCalculator((BOWConceptMapSolution) theSolution, this.filter);
    this.maxElements = ((BOWConceptMapSolution) theSolution).getNumOfTokens();
    this.countNulls = theSolution.getUseNulls();
    this.convertTokensToStems = theSolution.getUseStems();
    this.addPOSToTokens = theSolution.getUsePOS();
    this.invertTokenSequence = theSolution.getInvertTokenSequence();
    this.removeStopWords = theSolution.getRemoveStopWords();

    if (this.addPOSToTokens) {
      this.posTagger = POSTaggerFactory.getInstance();
    }
    if (this.convertTokensToStems || this.removeStopWords) {
      this.stemmer = LemmatiserFactory.getInstance(this.removeStopWords);
    }
  }
}
