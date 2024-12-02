package src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.ConceptMapBootstrapAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.ConceptMapBootstrapSampler;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

public class ConceptMapCrossValidationAnalyzer extends ConceptMapBootstrapAnalyzer
    implements Iterable<RaptatPair<List<String[]>, List<String[]>>> {
  private class CrossValidationIterator
      implements Iterator<RaptatPair<List<String[]>, List<String[]>>> {
    private int positionInList = 0;
    List<String> groupingKeys;


    private CrossValidationIterator() {
      this.groupingKeys =
          new ArrayList<>(ConceptMapCrossValidationAnalyzer.this.annotationGroups.keySet());
    }


    @Override
    public boolean hasNext() {
      return this.positionInList < this.groupingKeys.size();
    }


    @Override
    public RaptatPair<List<String[]>, List<String[]>> next() {
      String testGroupKey = this.groupingKeys.get(this.positionInList++);
      List<String[]> testingSet =
          ConceptMapCrossValidationAnalyzer.this.annotationGroups.get(testGroupKey);
      Set<String> subGroupKeys = ConceptMapCrossValidationAnalyzer.this.annotationGroups.keySet();
      List<String[]> trainingSet =
          new ArrayList<>(ConceptMapCrossValidationAnalyzer.this.annotationGroups.values().size());
      for (String curKey : subGroupKeys) {
        if (!curKey.equals(testGroupKey)) {
          trainingSet.addAll(ConceptMapCrossValidationAnalyzer.this.annotationGroups.get(curKey));
        }
      }
      return new RaptatPair<>(trainingSet, testingSet);
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private HashMap<String, List<String[]>> annotationGroups;

  private int tokenDataStart;


  public ConceptMapCrossValidationAnalyzer(ConceptMapBootstrapSampler dataSet) {
    this.annotationGroups = dataSet.getGroupByColumnList();
    this.tokenDataStart = dataSet.myReader.getStartTokensPosition();
  }


  public int getNumberOfGroups() {
    return this.annotationGroups.size();
  }


  public int getTokenDataStart() {
    return this.tokenDataStart;
  }


  @Override
  public Iterator<RaptatPair<List<String[]>, List<String[]>>> iterator() {
    return new CrossValidationIterator();
  }
}
