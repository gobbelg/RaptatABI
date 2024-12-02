package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchPatternValidate;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchType;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * Manages multiple instances of FSMDefinition and permits invocation on each token via the method
 * identify.
 *
 * @param <T> the generic type
 */
final public class FSMFeatureLabeler {

  /** The fsm array. */
  private FSMDefinition[] __fsm_array;


  /**
   * Instantiates a new FSM feature labeler.
   *
   * @param fsm_array the fsm array
   */
  public FSMFeatureLabeler(FSMDefinition... fsm_array) {
    this.__fsm_array = fsm_array;
  }


  /**
   * Calls the FSM on this tokenmatching IsA by MatchType
   *
   * @param raptat_token_list
   * @param validationCallback
   * @throws FSMElementException
   */
  public void identify(List<RaptatToken> raptat_token_list, MatchPatternValidate validationCallback)
      throws FSMElementException {
    this.identify(raptat_token_list, MatchType.Exact, validationCallback);

  }


  /**
   * Calls the FSM on tokens, either matching IsA or HasA by MatchType
   *
   * @param tokens the tokens
   * @param match_type
   * @param validationCallback
   * @throws FSMElementException the FSM element exception
   */
  public void identify(List<RaptatToken> tokens, MatchType match_type,
      MatchPatternValidate validationCallback) throws FSMElementException {
    for (RaptatToken token : tokens) {
      for (FSMDefinition fsm : this.__fsm_array) {
        if (fsm != null) {
          fsm.label(token, match_type, validationCallback);
        }
      }
    }
  }


  /**
   * Calls the FSM on this token, either matching IsA or HasA by MatchType
   *
   * @param raptat_token
   * @param match_type
   * @param validationCallback
   * @throws FSMElementException
   */
  public void identify(RaptatToken raptat_token, MatchType match_type,
      MatchPatternValidate validationCallback) throws FSMElementException {
    List<RaptatToken> list = new ArrayList<>();
    list.add(raptat_token);
    this.identify(list, match_type, validationCallback);
  }

}
