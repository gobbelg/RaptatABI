/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.io.Serializable;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * Interface for a set of classes that are tasked with taking a tagged sequence of tokens and
 * determining the attribute of the phrase based on the tags. Different logic can be incorporated
 * into the method getPhraseAttribute() to determine the value(s) of the attribute. Each
 * AttributeAnalyzerType, an enum, will generally have its own implementation of the
 * ContextAttributeAnalyzer interface.
 *
 * <p>
 * Note that any classes implementing this interface must be seralizable, so all fields that are not
 * transient or static must be serializable
 *
 * @author vhatvhgobbeg - Jun 11, 2013
 */
public interface ContextAttributeAnalyzer extends Serializable {
  String getAttributeName();


  /**
   * @param phraseTokenSequence
   * @return
   */
  RaptatAttribute getPhraseAttribute(List<RaptatToken> phraseTokenSequence);
}
