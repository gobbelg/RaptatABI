/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.PropertyList;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/** @author Glenn T. Gobbel Apr 19, 2016 */
public class ConceptFeatureBuilder extends FeatureBuilder implements Serializable {
  private static final long serialVersionUID = -1344154651379605178L;


  protected HashSet<String> includedFeatures = new HashSet<>(1028 * 64);

  public ConceptFeatureBuilder() {
    super();
  }


  public ConceptFeatureBuilder(int wordConjunctionOffset, int wordNgramSize,
      int posConjunctionOffset, int posNgramSize, String includedFeaturesFilePath) {
    FeatureBuilder.LOGGER.setLevel(Level.INFO);
    this.featureBuilderPipe = prepareFeatureBuilderPipe(wordConjunctionOffset, wordNgramSize,
        posConjunctionOffset, posNgramSize);

    initializeIncludedFeatures(includedFeaturesFilePath, this.includedFeatures);
  }


  /**
   * @param sentences
   * @return
   */
  @Override
  public CrfFeatureSet buildFeatures(List<AnnotatedPhrase> sentences) {
    List<ItemSequence> sentenceFeatureList = new ArrayList<>(sentences.size());
    List<StringList> sentenceTokenLabels = new ArrayList<>(sentences.size());

    /* Create sentence features and add them to the list via the pipe */
    InstanceList sentenceFeatureInstances = new InstanceList(this.featureBuilderPipe);
    Iterator<Instance> malletSentenceIterator = new MalletListIterator<>(sentences);
    sentenceFeatureInstances.addThruPipe(malletSentenceIterator);

    for (Instance curSentenceFeatures : sentenceFeatureInstances) {
      TokenSequence tokens = (TokenSequence) curSentenceFeatures.getData();
      LabelSequence labels = (LabelSequence) curSentenceFeatures.getTarget();

      RaptatPair<ItemSequence, StringList> featuresAndLabels =
          trainingInstanceFeaturesToItemSequence(tokens, labels);
      sentenceFeatureList.add(featuresAndLabels.left);
      sentenceTokenLabels.add(featuresAndLabels.right);
    }
    return new CrfFeatureSet(sentenceFeatureList, sentenceTokenLabels);
  }


  /**
   * @param wordConjunctionOffset
   * @param wordConjunctionWidth
   * @param posNgramSize
   * @param posConjunctionOffset
   * @return
   */
  private Pipe prepareFeatureBuilderPipe(int wordConjunctionOffset, int wordConjunctionWidth,
      int posConjunctionOffset, int posConjunctionWidth) {
    Pipe pipe = null;

    int[][] wordConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        wordConjunctionOffset, wordConjunctionOffset, wordConjunctionWidth, true);

    int[][] posConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        posConjunctionOffset, posConjunctionOffset, posConjunctionWidth, true);

    try {
      Pipe[] pipeArray = new Pipe[] {new ConceptFeaturePipe(),
          new RegexMatches("ALPHABETIC", Pattern.compile("[A-Za-z][a-z]*")),
          new RegexMatches("NONALPHABETIC", Pattern.compile(".*[^A-Za-z].*")),
          new TokenTextCharPrefix("PREFIX=", 3), new TokenTextCharPrefix("PREFIX=", 4),
          new TokenTextCharSuffix("SUFFIX=", 3), new TokenTextCharSuffix("SUFFIX=", 4),
          new OffsetConjunctionsRaptat(true, wordConjunctionArray, Pattern.compile("W=.*")),
          new OffsetConjunctionsRaptat(true, posConjunctionArray, Pattern.compile("POS=.*")),
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


  /**
   * Takes the sequence of features and labels for a single sentence that were created via a Mallet
   * pipe and transforms them into an ItemSequence and StringList object for compatability with
   * CrfSuite.
   *
   * @param tokens
   * @param labels
   * @return
   */
  private RaptatPair<ItemSequence, StringList> trainingInstanceFeaturesToItemSequence(
      TokenSequence tokens, LabelSequence labels) {
    ItemSequence tokenFeatureSequence = new ItemSequence();
    StringList tokenLabelList = new StringList();

    Iterator<cc.mallet.types.Token> tokenIterator = tokens.iterator();
    LabelSequence.Iterator labelIterator = labels.iterator();

    while (tokenIterator.hasNext()) {
      cc.mallet.types.Token token = tokenIterator.next();
      String curLabel = labelIterator.next().toString();
      tokenLabelList.add(curLabel);

      PropertyList.Iterator featureIterator = token.getFeatures().iterator();
      Item tokenFeatures = new Item();
      while (featureIterator.hasNext()) {
        featureIterator.next();
        String curFeature = escapeCrfSuiteCharacters(featureIterator.getKey());

        HashSet<String> mappedFeatures;
        if (this.includedFeatures.isEmpty() || this.includedFeatures.contains(curFeature)) {
          if (curFeature.length() > 0) {
            tokenFeatures.add(new Attribute(curFeature));
          }
        }
      }
      tokenFeatureSequence.add(tokenFeatures);
    }

    return new RaptatPair<>(tokenFeatureSequence, tokenLabelList);
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {}
    });
  }
}
