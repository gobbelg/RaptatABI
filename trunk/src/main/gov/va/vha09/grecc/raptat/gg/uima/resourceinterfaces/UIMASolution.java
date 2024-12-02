/** */
package src.main.gov.va.vha09.grecc.raptat.gg.uima.resourceinterfaces;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Aug 26, 2013 *****************************************************
 */
public interface UIMASolution {
  ConceptMapSolution getConceptMapSolution();


  SequenceIDStructure getSequenceIdentifier();


  TokenProcessingOptions getTokenTrainingOptions();
}
