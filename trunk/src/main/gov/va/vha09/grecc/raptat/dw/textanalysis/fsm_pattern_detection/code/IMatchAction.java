package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 *
 * @author westerd
 *
 */
public interface IMatchAction {
  void action(RaptatToken token, String feature_type, String feature_value);
}
