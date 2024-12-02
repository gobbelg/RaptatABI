/**
 *
 */
package src.main.resources.config.ggpatternconfig.grouped

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.StateAndPattern

/**
 * @author VHATVHGOBBEG
 *
 */
class ArteryFSM extends FSMDefinitionBase {

  String ARTERY_ANKLE;
  String ARTERY_ARM;
  String ARTERY_UNSPECIFIED;

  @Override
  public String get_fsm_name() {
    return "Artery";
  }

  @Override
  protected void initialize_state_and_pattern_names() {
    ARTERY_ANKLE = "ArteryAnkle";
    ARTERY_ARM = "ArteryArtm";
    ARTERY_UNSPECIFIED = "ArteryUnspecified";
  }

  @Override
  protected void set_match_map() {
    map[ARTERY_ANKLE] = [
      '\\(pta?\\)|(?<!\\()pta?(?!\\))',
      'posterior[ \t]*tib(ial(is)?)?[ \t]*(art(ery|eries)?)?',
      'p(ost(erior)?)?\\.?[ \t]*t(ib(ial(is)?)?)?\\.?[ \t]*(a(rt(ery|eries))?)?\\.?',
      'anterio[ \t]*tib(ial(is)?)?[ \t]*(art(ery|eries)?)?',
      'a(nt(erior)?)?\\.?[ \t]*t(ib(ial(is)?)?)?\\.?[ \t]*(a(rt(ery|eries))?)?\\.?',
      'dpa?',
      '\\(dpa?\\)|(?<!\\()dpa?(?!\\))',
      'dors(al)?[ \t]*ped(al)?[ \t]*(a(rt(ery|eries)?)?)?\\.?'
    ];
    map[ARTERY_ARM] = ['brach((ial)[ \t]*(art(ery)?)?)?'];

    map[ARTERY_UNSPECIFIED] = ['art(er(y|ies))?'];
  }

  @Override
  public String[][] get_fsm_definition_table() {

    StateAndPattern arteryAnkle = new StateAndPattern(ARTERY_ANKLE, ARTERY_ANKLE);
    StateAndPattern arteryArm = new StateAndPattern(ARTERY_ARM, ARTERY_ARM);
    StateAndPattern arteryUnspecified = new StateAndPattern(ARTERY_UNSPECIFIED, ARTERY_UNSPECIFIED);

    return generate_categorical_detection_fsm(arteryAnkle, arteryArm, arteryUnspecified);
  }

  @Override
  public Boolean should_reset_labeling_on_next_not_found() {
    return true;
  }
}
