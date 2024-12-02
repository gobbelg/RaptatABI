package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.ArrayList;
import java.util.List;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.TokenSequence;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class CSFeaturePipe extends FeaturePipe {
  /** */
  private static final long serialVersionUID = 6362879109859688072L;


  public CSFeaturePipe() throws InstantiationException, IllegalAccessException {
    super();
  }


  @Override
  public Instance pipe(Instance carrier) {
    AnnotatedPhrase sentence = (AnnotatedPhrase) carrier.getData();

    if (FeaturePipe.LOGGER.isDebugEnabled()) {
      if (this.logWriter != null) {
        this.logWriter.println("\nFinding features for sentence " + this.sentenceCounter + ":\n"
            + sentence.getPhraseStringUnprocessed());
        this.logWriter.flush();
      }
      FeaturePipe.LOGGER
          .debug("\nFinding features for sentence " + this.sentenceCounter + ":\n" + sentence);
    }

    this.sentenceCounter++;

    List<RaptatToken> phraseTokens = sentence.getProcessedTokens();

    TokenSequence data = new TokenSequence(phraseTokens.size());
    LabelSequence target =
        new LabelSequence((LabelAlphabet) getTargetAlphabet(), phraseTokens.size());

    StringBuffer source = new StringBuffer();

    String label;
    int position = 0;

    List<cc.mallet.types.Token> malletTokenList = new ArrayList<>();
    List<Integer> negexCuePositions = new ArrayList<>();
    List<Integer> annotatedCuePositions = new ArrayList<>();

    for (RaptatToken curToken : phraseTokens) {
      String word, wc, bwc;

      FeaturePipe.LOGGER.debug("ProcessingToken:" + curToken);
      word = curToken.getTokenStringUnprocessed();
      wc = word;
      bwc = word;
      label = curToken.getTokenStringTagged();
      if (label == null || label.equals("")) {
        label = RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING;
      }

      if (this.doDigitCollapses) {
        if (word.matches("19\\d\\d")) {
          word = "<YEAR>";
        } else if (word.matches("19\\d\\ds")) {
          word = "<YEARDECADE>";
        } else if (word.matches("19\\d\\d-\\d+")) {
          word = "<YEARSPAN>";
        } else if (word.matches("\\d+\\\\/\\d")) {
          word = "<FRACTION>";
        } else if (word.matches("\\d[\\d,\\.]*")) {
          word = "<DIGITS>";
        }
      }

      /* Determine word class before downcasing */
      if (this.doWordClass) {
        wc = word;
        wc = wc.replaceAll("[A-Z]", "A");
        wc = wc.replaceAll("[a-z]", "a");
        wc = wc.replaceAll("[0-9]", "0");
        wc = wc.replaceAll("[^A-Za-z0-9]", "x");
      }
      if (this.doBriefWordClass) {
        bwc = word;
        bwc = bwc.replaceAll("[A-Z]+", "A");
        bwc = bwc.replaceAll("[a-z]+", "a");
        bwc = bwc.replaceAll("[0-9]+", "0");
        bwc = bwc.replaceAll("[^A-Za-z0-9]+", "x");
      }

      String malletWord = word;
      if (this.doDowncasing) {
        malletWord = word.toLowerCase();
      }
      cc.mallet.types.Token malletToken = new cc.mallet.types.Token(malletWord);
      malletToken.setFeatureValue("W=" + malletWord, 1);

      if (this.initCapsAlphaPattern.matcher(word).matches()) {
        malletToken.setFeatureValue("INITCAPS", 1);
      }

      if (this.allCaps.matcher(word).matches()) {
        malletToken.setFeatureValue("ALLCAPS", 1);
      }

      if (this.doWordClass) {
        malletToken.setFeatureValue("WC=" + wc, 1);
      }

      if (this.doBriefWordClass) {
        malletToken.setFeatureValue("BWC=" + bwc, 1);
      }

      if (this.saveSource) {
        source = source.append(word).append(" ");
      }

      if (this.usePOS) {
        malletToken.setFeatureValue("POS=" + curToken.getPOS(), 1);
      }

      if (this.useStems) {
        malletToken.setFeatureValue("STEM=" + curToken.getStem(), 1);
      }

      /*
       * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
       */
      // /*
      // * Create a feature value that can be changed when doing linked
      // CRF
      // * tagging, such as with cue and scope. This is essentially a
      // * placeholder that is tagged with the constant
      // * Constants.MODIFIABLE_CUE_ATTRIBUTE so that it can be used later
      // * to add features.
      // */
      // malletToken.setFeatureValue( Constants.MODIFIABLE_CUE_ATTRIBUTE,
      // 1 );

      String negexTag = curToken.getNegexNegationTag();
      if (this.markNegexTags) {
        malletToken.setFeatureValue("NEGEXTAG=" + negexTag, 1);
      }

      if (negexTag.equalsIgnoreCase("pre") || negexTag.equalsIgnoreCase("post")) {
        negexCuePositions.add(position);
      }

      if (label.equalsIgnoreCase(RaptatConstants.BEGIN_CUE_STRING)
          || label.equalsIgnoreCase(RaptatConstants.INSIDE_CUE_STRING)) {
        annotatedCuePositions.add(position);
      }

      data.add(malletToken);
      target.add(label);
      malletTokenList.add(malletToken);
      position++;
    }

    addMitreScopeFeatures(malletTokenList, negexCuePositions, "NEGEXCUE");

    carrier.setData(data);
    carrier.setTarget(target);

    if (this.saveSource) {
      carrier.setSource(source);
    }

    if (FeaturePipe.LOGGER.isDebugEnabled()) {
      printPipeInfo(data, target);
    }

    return carrier;
  }


  private void addMitreScopeFeatures(List<cc.mallet.types.Token> tokenList,
      List<Integer> cuePositionList, String featureTag) {
    for (int i = 0; i < cuePositionList.size(); i++) {
      int cueStart = cuePositionList.get(i);
      StringBuilder cuePhrase = new StringBuilder(tokenList.get(cueStart).getText());

      /*
       * Add to cue if multi-word cue and determine end of cue phrase. For multi-word cues, the
       * position of the next cue should be just one more than the position of the previous cue.
       */
      int cueEnd = cueStart;
      while (i + 1 < cuePositionList.size()
          && cuePositionList.get(i) + 1 == cuePositionList.get(i + 1)) {
        i++;
        cueEnd = cuePositionList.get(i);
        cuePhrase.append("_").append(tokenList.get(cueEnd).getText());
      }

      for (int j = 0; j < tokenList.size(); j++) {
        if (cuePositionList.contains(j)) {
          continue;
        }

        String cueStringFeature =
            RaptatConstants.SCOPE_FEATURE_TAG + "_" + featureTag + "_" + "CUE=" + cuePhrase;
        tokenList.get(j).setFeatureValue(cueStringFeature, 1);

        int distanceToCue;
        if (Math.abs(j - cueStart) > Math.abs(j - cueEnd)) {
          distanceToCue = j - cueEnd;
        } else {
          distanceToCue = j - cueStart;
        }

        String cuePositionFeature = RaptatConstants.SCOPE_FEATURE_TAG + "_" + featureTag + "_"
            + "CUE_POSITION=" + distanceToCue;
        tokenList.get(j).setFeatureValue(cuePositionFeature, 1);

        String cueDistanceFeature = RaptatConstants.SCOPE_FEATURE_TAG + "_" + featureTag + "_"
            + "CUE_DISTANCE=" + Math.abs(distanceToCue);
        tokenList.get(j).setFeatureValue(cueDistanceFeature, 1);

        String cueCloseFeature = RaptatConstants.SCOPE_FEATURE_TAG + "_" + featureTag + "_"
            + "CLOSE_CUE=" + (Math.abs(distanceToCue) < 3);
        tokenList.get(j).setFeatureValue(cueCloseFeature, 1);

        String cueUpStreamFeature = RaptatConstants.SCOPE_FEATURE_TAG + "_" + featureTag + "_"
            + "UPSTREAM_CUE=" + (distanceToCue > 0);
        tokenList.get(j).setFeatureValue(cueUpStreamFeature, 1);

        tokenList.get(j).setFeatureValue(cueStringFeature + "_" + cueCloseFeature, 1);
        tokenList.get(j).setFeatureValue(
            cueStringFeature + "_" + cueCloseFeature + "_" + cueUpStreamFeature, 1);

        int betweenWordIndex;
        int betweenEnd;
        if (cueStart > j) {
          betweenWordIndex = j + 1;
          betweenEnd = cueStart;
        } else {
          betweenWordIndex = cueEnd + 1;
          betweenEnd = j;
        }

        for (; betweenWordIndex < betweenEnd; betweenWordIndex++) {
          tokenList.get(j).setFeatureValue(RaptatConstants.SCOPE_FEATURE_TAG + "_" + featureTag
              + "_" + "BETWEENWORD=" + tokenList.get(betweenWordIndex).getText(), 1);
        }
      }
    }
  }
}
