package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.AttributeDBMismatchException;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.ConceptMapBootstrapSampler;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/**
 * ********************************************************** NonNaiveBayesTester -
 *
 * @author Glenn Gobbel, Sep 19, 2010
 *         <p>
 *         *********************************************************
 */
public class NonNBEvaluator extends ConceptMapEvaluator {

  // Makes sure list is synchronized (see Java 1.5 LinkedList documents)
  private List<String[]> compiledResults = Collections.synchronizedList(new LinkedList<String[]>());


  public NonNBEvaluator() {
    // TODO Auto-generated constructor stub
  }


  /**
   * ***************************************************************
   *
   * @param curSolution
   * @param annotatorOptions ***************************************************************
   */
  public NonNBEvaluator(ConceptMapSolution curSolution, OptionsManager annotatorOptions) {
    // TODO Auto-generated constructor stub
  }


  // (non-Javadoc)
  // @see
  // org.grecc.nashville.va.annotator.main.Tester#testSolution(org.grecc.nashville.va.annotator.helpers.CSVReader)

  /**
   * ***************************************************************
   *
   * @param theOptions ***************************************************************
   */
  public NonNBEvaluator(OptionsManager theOptions) {
    // TODO Auto-generated constructor stub
  }


  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  // (non-Javadoc)
  // @see
  // src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Evaluator#setSolution(src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution)


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Evaluator# evaluateSolution
   * (src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.AnnotationSet)
   */
  @Override
  public List<String[]> evaluateSolution(ConceptMapBootstrapSampler testData)
      throws AttributeDBMismatchException {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<String[]> evaluateSolution(DataImportReader testDataReader)
      throws AttributeDBMismatchException {
    return null;
    // TODO Auto-generated method stub

  }


  public List<AnnotatedPhrase> evaluateSolution(List<AnnotatedPhrase> testData) {
    // TODO Auto-generated method stub
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Evaluator# evaluateSolution
   * (java.util.List, int)
   */
  @Override
  public List<String[]> evaluateSolution(List<String[]> testData, int startTokensPosition)
      throws AttributeDBMismatchException {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<RaptatPair<List<RaptatToken>, String>> getConcepts(
      List<List<RaptatToken>> theSequences, HashSet<String> acceptableConcepts,
      boolean includeNulls) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<List<RaptatAttribute>> getMappedAttributes(List<String[]> phrases,
      List<String> concepts) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<String[]> mapSolution(DataImportReader theReader) {
    // TODO Auto-generated method stub
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Evaluator#resetResults()
   */
  @Override
  public void resetResults() {
    this.compiledResults.clear();
  }


  @Override
  public void setFieldsFromSolution(ConceptMapSolution theSolution) {
    // TODO Auto-generated method stub

  }


  @Override
  public void setMappedAttributes(List<AnnotatedPhrase> proposedAnnotations) {
    // TODO Auto-generated method stub

  }
}
