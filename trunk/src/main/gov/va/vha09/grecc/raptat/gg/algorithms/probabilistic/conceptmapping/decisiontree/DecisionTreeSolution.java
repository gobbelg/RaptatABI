package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.decisiontree;

import java.io.File;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ********************************************************** DecisionTreeSolution -
 *
 * @author Glenn Gobbel, Sep 19, 2010
 *         <p>
 *         *********************************************************
 */
public class DecisionTreeSolution extends ConceptMapSolution {
  private static final long serialVersionUID = 1L;


  @Override
  public NBConditionalTable[] getConditionals() {
    // TODO Auto-generated method stub
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution# initiateProbCalcs ()
   */
  @Override
  public void initiateProbCalcs() {
    // TODO Auto-generated method stub

  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution#saveToFile( java.io.File)
   */
  @Override
  public void saveToFile(File solutionFile) {
    // TODO Auto-generated method stub

  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution#updateProbs()
   */
  @Override
  public void updateLikelihood() {
    // TODO Auto-generated method stub

  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.conceptmapping.
   * ConceptMapSolution#updateTraining(java.lang.String[],
   * src.main.gov.va.vha09.grecc.raptat.gg.datastructures.AnnotatedPhrase)
   */
  @Override
  public void updateTraining(String[] trainData, AnnotatedPhrase phrase) {
    String theConcept = phrase.getConceptName();
    this.updateTraining(trainData, theConcept);
    this.conceptAttributeSolution.updateTraining(theConcept, phrase.getPhraseAttributes(),
        trainData);
  }


  @Override
  public void updateTraining(String[] trainData, String theConcept) {
    // TODO Auto-generated method stub
  }
}
