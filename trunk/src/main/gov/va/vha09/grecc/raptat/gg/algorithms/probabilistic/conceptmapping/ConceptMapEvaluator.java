package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping;

import java.util.HashSet;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.AttributeDBMismatchException;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.ConceptMapBootstrapSampler;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/*
 *
 *
 * @author Glenn Gobbel, Sep 9, 2010
 */
public abstract class ConceptMapEvaluator {
  protected HashSet<String> acceptedConcepts = null;


  public abstract void close();


  public abstract List<String[]> evaluateSolution(ConceptMapBootstrapSampler testData)
      throws AttributeDBMismatchException;


  public abstract List<String[]> evaluateSolution(DataImportReader testingFile)
      throws AttributeDBMismatchException;


  public abstract List<String[]> evaluateSolution(List<String[]> testData, int startTokensPosition)
      throws AttributeDBMismatchException;


  public abstract List<RaptatPair<List<RaptatToken>, String>> getConcepts(
      List<List<RaptatToken>> raptatSequences, HashSet<String> acceptableConcepts,
      boolean includeNulls);


  public abstract List<List<RaptatAttribute>> getMappedAttributes(List<String[]> phrases,
      List<String> concepts);


  // public abstract List<AnnotatedPhrase>
  // evaluateSolution(List<AnnotatedPhrase> testData);

  public abstract List<String[]> mapSolution(DataImportReader theReader);


  public abstract void resetResults();


  public abstract void setFieldsFromSolution(ConceptMapSolution theSolution);


  public abstract void setMappedAttributes(List<AnnotatedPhrase> proposedAnnotations);
}
