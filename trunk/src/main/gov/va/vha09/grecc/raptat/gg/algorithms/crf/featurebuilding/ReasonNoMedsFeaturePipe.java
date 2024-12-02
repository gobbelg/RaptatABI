package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.TokenSequence;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class ReasonNoMedsFeaturePipe extends FeaturePipe {
  /** */
  private static final long serialVersionUID = 1682902457041888048L;

  private static final HashSet<String> reasonNoMedSubcategories =
      new HashSet<>(Arrays.asList(new String[] {"bp", "br", "compliance", "drug", "ex", "fluid",
          "hb", "hf", "hr", "npo", "rhythm", "vestibular", "tolerance", "vision"}));


  public ReasonNoMedsFeaturePipe() throws InstantiationException, IllegalAccessException {
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
       * Here's where we add features that are not part of the ConceptFeaturePipe pipe() method
       */
      RaptatToken parentToken;
      if ((parentToken = curToken.getParentToken()) == null) {
        parentToken = curToken;
      }
      Collection<String> tokenConcepts = parentToken.getAssignedConcepts();
      String negationStatus = parentToken.getContextValue(ContextType.NEGATION);
      for (String curConcept : tokenConcepts) {
        malletToken.setFeatureValue("CONCEPT=" + curConcept, 1);
        malletToken.setFeatureValue("CONCEPT=" + curConcept + "_" + negationStatus, 1);

        if (ReasonNoMedsFeaturePipe.reasonNoMedSubcategories.contains(curConcept.toLowerCase())) {
          malletToken.setFeatureValue("CONCEPT=RNM_Subcategory", 1);
          malletToken.setFeatureValue("CONCEPT=RNM_Subcategory_" + negationStatus, 1);
        }
      }

      data.add(malletToken);
      target.add(label);
    }

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
}
