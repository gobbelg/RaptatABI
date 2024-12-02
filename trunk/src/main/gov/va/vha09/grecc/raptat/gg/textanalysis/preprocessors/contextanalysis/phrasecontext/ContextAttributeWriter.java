/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * ************************************************ Determines the context for a phrase that is
 * being annotated such as whether the phrase is negated. This class keeps a list of context
 * analyzers so multiple contexts can be analyzed. Note that only one of a given type of context
 * analyzer can exist in the list.
 *
 * @author vhatvhgobbeg - Jun 11, 2013 ************************************************
 */
public class ContextAttributeWriter {
  private Set<ContextAttributeAnalyzer> contextAnalyzers = new HashSet<>();


  public ContextAttributeWriter() {}


  public ContextAttributeWriter(List<ContextAttributeAnalyzer> analyzerList) {
    this();
    for (ContextAttributeAnalyzer analyzer : analyzerList) {
      if (!this.contextAnalyzers.contains(analyzer)) {
        this.contextAnalyzers.add(analyzer);
      } else {
        String msg = "An equivalent analyzer of type " + analyzer.getClass().getName()
            + " already exists in the contextAnalyzers";
        throw new IllegalArgumentException(msg);
      }
    }
  }


  public <T extends ContextAttributeAnalyzer> ContextAttributeWriter(Set<T> analyzerSet) {
    this();
    this.contextAnalyzers = (Set<ContextAttributeAnalyzer>) analyzerSet;
  }


  public void addAnalyzer(ContextAttributeAnalyzer analyzer) {
    if (!this.contextAnalyzers.contains(analyzer)) {
      this.contextAnalyzers.add(analyzer);
    } else {
      String msg = "An equivalent analyzer of type " + analyzer.getClass().getName()
          + " already exists in the contextAnalyzers";
      throw new IllegalArgumentException(msg);
    }
  }


  /**
   * @param curTokenSequence
   * @param idGenerator
   * @return
   */
  public List<RaptatAttribute> getSequenceAttributes(List<RaptatToken> curTokenSequence) {
    List<RaptatAttribute> phraseAttributes = new ArrayList<>(this.contextAnalyzers.size());
    RaptatAttribute curAttribute;
    for (ContextAttributeAnalyzer curAnalyzer : this.contextAnalyzers) {
      if ((curAttribute = curAnalyzer.getPhraseAttribute(curTokenSequence)) != null) {
        phraseAttributes.add(curAttribute);
      }
    }
    return phraseAttributes;
  }
}
