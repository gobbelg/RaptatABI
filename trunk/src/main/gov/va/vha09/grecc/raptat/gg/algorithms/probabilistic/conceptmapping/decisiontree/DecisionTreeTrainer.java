package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.decisiontree;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/**
 * ********************************************************** DecisionTreeTrainer -
 *
 * @author Glenn Gobbel, Sep 9, 2010
 *         <p>
 *         *********************************************************
 */
public class DecisionTreeTrainer extends ConceptMapTrainer {

  /**
   * ***************************************************************
   *
   * @param convertTokensToStems
   * @param fileReader ***************************************************************
   */
  public DecisionTreeTrainer(boolean useNulls, boolean useStems) {
    this.useNulls = useNulls;
    this.convertTokensToStems = useStems;
  }


  @Override
  protected Vector<Long> addToSolution(DataImportReader trainingData, int startTokensPosition,
      int numElementsForAnalysis) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolution(ListIterator<AnnotatedPhrase> dataIterator, int dataSize) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolution(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(DataImportReader trainingData,
      int startTokensPosition, int numElementsForAnalysis) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(ListIterator<AnnotatedPhrase> dataIterator,
      int dataSize) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns) {
    // TODO Auto-generated method stub
    return null;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Trainer#createSolution
   * (src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader)
   */
  @Override
  protected void createSolution(DataImportReader theData) {
    // TODO Auto-generated method stub

  }


  @Override
  protected void createSolution(List<AnnotatedPhrase> theAnnotations) {
    // TODO Auto-generated method stub

  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Trainer#createSolution (java.util.List)
   */
  @Override
  protected void createSolution(List<String[]> theData, int startTokensPosition) {
    // TODO Auto-generated method stub

  }
}
