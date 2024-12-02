package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.featurematchers.RaptatFeatureMatcher;

public class ReasonNoMedsFeatureBuilder extends ConceptFeatureBuilder implements Serializable {

  private static final long serialVersionUID = 6325486388268806447L;


  public ReasonNoMedsFeatureBuilder(int[] wordOffsetAndSize, int[] posOffsetAndSize,
      int[] wordAndConceptOffsetAndSize, String includedFeaturesFilePath) {
    super();
    int[][] conjunctionArrays =
        new int[][] {wordOffsetAndSize, posOffsetAndSize, wordAndConceptOffsetAndSize};
    this.featureBuilderPipe = prepareFeatureBuilderPipe(conjunctionArrays);
    initializeIncludedFeatures(includedFeaturesFilePath, this.includedFeatures);
  }


  private Pipe getPosConjunctionPipe(int[][] conjunctionArray) {
    return new OffsetConjunctionsNew(true, conjunctionArray, new RaptatFeatureMatcher() {
      /** */
      private static final long serialVersionUID = 7753734464524586345L;


      @Override
      public boolean matches(String inputString) {
        Matcher posMatcher = Pattern.compile("POS=.*").matcher(inputString);
        return posMatcher.matches();
      }
    });
  }


  private Pipe getWordAndConceptPipe(int[][] conjunctionArray) {
    return new OffsetConjunctionsNew(true, conjunctionArray, new RaptatFeatureMatcher() {
      /** */
      private static final long serialVersionUID = -3578968829903122020L;


      @Override
      public boolean matches(String inputString) {
        Matcher wMatcher = Pattern.compile("W=.*").matcher(inputString);
        Matcher conceptMatcher = Pattern.compile("CONCEPT=.*").matcher(inputString);
        Matcher nonConjoinedMatcher = Pattern.compile(".+@-?\\d+.*").matcher(inputString);
        return !nonConjoinedMatcher.matches() && (wMatcher.matches() || conceptMatcher.matches());
      }
    });
  }


  private Pipe getWordConjunctionPipe(int[][] conjunctionArray) {
    return new OffsetConjunctionsNew(true, conjunctionArray, new RaptatFeatureMatcher() {
      /** */
      private static final long serialVersionUID = -1089939295938910413L;


      @Override
      public boolean matches(String inputString) {
        Matcher wMatcher = Pattern.compile("W=.*").matcher(inputString);
        return wMatcher.matches();
      }
    });
  }


  /**
   * @param wordConjunctionOffset
   * @param wordConjunctionWidth
   * @param posNgramSize
   * @param posConjunctionOffset
   * @return
   */
  private Pipe prepareFeatureBuilderPipe(int[][] conjunctionParameters) {
    Pipe pipe = null;
    final int WORD_ROW = 0;
    final int POS_ROW = 1;
    final int CONCEPT_WORD_ROW = 2;

    int[][] wordConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        conjunctionParameters[WORD_ROW][0], conjunctionParameters[WORD_ROW][0],
        conjunctionParameters[WORD_ROW][1], true);

    int[][] posConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        conjunctionParameters[POS_ROW][0], conjunctionParameters[POS_ROW][0],
        conjunctionParameters[POS_ROW][1], true);

    int[][] conceptConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        conjunctionParameters[CONCEPT_WORD_ROW][0], conjunctionParameters[CONCEPT_WORD_ROW][0],
        conjunctionParameters[CONCEPT_WORD_ROW][1], true);

    try {
      Pipe wordConjunctionPipe = getWordConjunctionPipe(wordConjunctionArray);
      Pipe posConjunctionPipe = getPosConjunctionPipe(posConjunctionArray);
      Pipe wordAndConceptPipe = getWordAndConceptPipe(conceptConjunctionArray);

      Pipe[] pipeArray = new Pipe[] {new ReasonNoMedsFeaturePipe(),
          new RegexMatches("ALPHABETIC", Pattern.compile("[A-Za-z][a-z]*")),
          new RegexMatches("NONALPHABETIC", Pattern.compile(".*[^A-Za-z].*")),
          new TokenTextCharPrefix("PREFIX=", 3), new TokenTextCharPrefix("PREFIX=", 4),
          new TokenTextCharSuffix("SUFFIX=", 3), new TokenTextCharSuffix("SUFFIX=", 4),
          wordConjunctionPipe, posConjunctionPipe, wordAndConceptPipe,
          new RegexMatches("ALPHANUMERIC", Pattern.compile(".*[A-Za-z].*[0-9].*")),
          new RegexMatches("PUNCTUATION", Pattern.compile("[,.;:?!-+]"))};

      pipe = new SerialPipes(pipeArray);

    } catch (InstantiationException e) {
      System.out.println(e);
    } catch (IllegalAccessException e) {
      System.out.println(e);
    }

    return pipe;
  }


  public static void main(String[] args) {
    Pattern regex = Pattern.compile("W=(?!.+W= )");
    Matcher regexMatcher = regex.matcher("W=");
    System.out.println("Matches:" + regexMatcher.matches());
  }
}
