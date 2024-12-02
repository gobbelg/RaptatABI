package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;

/**
 * A class that provides state management for a feature builder, maintaining and updating the
 * FeatureData with using RaptatTokenPhrase and optionally, a Label and feature string for a given
 * entry from PhraseListType.
 *
 * @author VHATVHWesteD
 *
 */
final public class FeatureBuilderState {

  public interface FnFeatureTextCall {
    String getText(FeatureData feature);
  }

  private FnFeatureTextCall fnFeatureTextCall;

  Map<PhraseListType, FeatureData> mapPhraseListTypeToLabelTypeAndLabelValue;


  /**
   * Instantiates a new feature builder state.
   */
  public FeatureBuilderState() {
    this.mapPhraseListTypeToLabelTypeAndLabelValue = new HashMap<>();
  }


  public FeatureBuilderState(FnFeatureTextCall fnfeatureTextCall) {
    this();
    this.fnFeatureTextCall = fnfeatureTextCall;
  }


  /**
   * Gets the feature state.
   *
   * @return the feature state
   */
  public Set<String> getFeatureState() {
    Set<String> state = new HashSet<>();
    for (PhraseListType key : this.mapPhraseListTypeToLabelTypeAndLabelValue.keySet()) {
      FeatureData feature = this.mapPhraseListTypeToLabelTypeAndLabelValue.get(key);

      if (this.fnFeatureTextCall == null) {
        this.fnFeatureTextCall = new FnFeatureTextCall() {

          @Override
          public String getText(FeatureData feature) {
            return feature.getFeature();
          }
        };
      }

      String text = this.fnFeatureTextCall == null ? feature.getFeature()
          : this.fnFeatureTextCall.getText(feature);
      if (text != null) {
        state.add(text);
      }
    }
    return state;
  }


  public void reset() {
    this.mapPhraseListTypeToLabelTypeAndLabelValue = new HashMap<>();
  }


  /**
   * Update feature state.
   *
   * @param foundLabel the found label
   * @param featureString the feature string
   * @param phraseType the phrase type
   * @param phrase
   * @return
   */
  public void updateFeatureState(PhraseListType phraseType, Label foundLabel, String featureString,
      RaptatTokenPhrase phrase, int startTokenIndex, int endTokenIndex) {
    String foundLabelName = foundLabel.getCategory();
    String phraseTypeName = phraseType.name();
    FeatureData oldFeature = this.mapPhraseListTypeToLabelTypeAndLabelValue.get(phraseType);

    final FeatureData oldFeatureStr = oldFeature;

    String oldState = oldFeatureStr == null ? "[No Feature Set]" : oldFeatureStr.getFeature();

    String newState = featureString;

    if (oldState.equals(newState) == false) {
      System.out
          .println(MessageFormat.format("State will change for ({0}, {1}) changed from {2} to {3}",
              phraseTypeName, foundLabelName, oldState, newState));
    }

    FeatureData newFeature = new FeatureData(newState, phrase, startTokenIndex, endTokenIndex);

    this.mapPhraseListTypeToLabelTypeAndLabelValue.put(phraseType, newFeature);
  }


  public void updateFeatureState(PhraseListType phraseType, RaptatTokenPhrase phrase,
      int startTokenIndex, int endTokenIndex) {
    FeatureData feature = this.mapPhraseListTypeToLabelTypeAndLabelValue.get(phraseType);
    if (feature != null) {
      feature.update(phrase, startTokenIndex, endTokenIndex);
    }
  }

}
