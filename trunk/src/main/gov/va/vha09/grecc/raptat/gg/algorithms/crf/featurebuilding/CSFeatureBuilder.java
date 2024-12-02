/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Level;
import com.google.common.primitives.Ints;
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
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/** @author Glenn T. Gobbel Feb 18, 2016 */
public class CSFeatureBuilder extends FeatureBuilder {
  /**
   * Convenience class for passing results between methods
   *
   * @author Glenn T. Gobbel Mar 8, 2016
   */
  private class CueScopeFeaturesAndLabels {
    private final ItemSequence cueFeatures;
    private final StringList cueLabels;
    private final ItemSequence scopeFeatures;
    private final StringList scopeLabels;


    /**
     * @param cueFeatures
     * @param cueLabels
     * @param scopeFeatures
     * @param scopeLabels
     */
    private CueScopeFeaturesAndLabels(ItemSequence cueFeatures, StringList cueLabels,
        ItemSequence scopeFeatures, StringList scopeLabels) {
      super();
      this.cueFeatures = cueFeatures;
      this.cueLabels = cueLabels;
      this.scopeFeatures = scopeFeatures;
      this.scopeLabels = scopeLabels;
    }
  }

  private static final long serialVersionUID = -668469113480458214L;

  private static final int MAX_TAG_LENGTH = 5;

  /*
   * The re-mapping variables listed below allow use of a single pipeline to create the initial
   * features used for cue-scope training. Certain features and labels are re-mapped for cue or
   * scope specific training. This re-mapping is performed when features from Mallet are converted
   * to ItemSequence instances for the CRFSuite tool
   */

  /*
   * The scopeToCueFeatureRemappings field keeps track of how scope specific features are re-mapped
   * to cue specific features
   */
  private static Map<String, String> scopeFeatureToCueFeatureRemap = new HashMap<>();

  /*
   * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
   */
  // /*
  // * The cueLabeledToScopeFeatureRemappings variable indicates how tokens
  // * labeled as the cue should have features added to them to indicate that
  // * the token is a cue for use during scope training and determination
  // */
  // private static Map<String, Map<String, String>>
  // cueLabelToScopeFeatureRemap = new HashMap<String, Map<String, String>>();

  /*
   * The scopeToCueLabelRemappings variable indicates how tokens labeled as in scope should be
   * labeled when used for cue training or evaluation
   */
  private static Map<String, String> scopeLabelToCueLabelRemap = new HashMap<>();

  /*
   * The cueToScopeLabelRemappings variable indicates how tokens labeled as the cue should be
   * labeled when used for scope training or evaluation
   */
  private static Map<String, String> cueLabelToScopeLabelRemap = new HashMap<>();

  /*
   * The labelToCueFeaturesMap variable maps each cue label to HashSet of features. If 1) the
   * HashSet is not null or empty, 2) a token has a label that is a key in the HashMap, and 3) only
   * features that are part of the the HashSet will be included for training and evaluation
   */
  private final HashSet<String> includedCueFeaturesSet = new HashSet<>(1028 * 64);

  /*
   * The cueToScopeLabelRemappings variable indicates how tokens labeled as the cue should be
   * labeled when used for scope training or evaluation
   */
  private final HashSet<String> includedScopeFeaturesSet = new HashSet<>(1028 * 64);

  /*
   * Keeps track of where the modifiable attributes are during tagging in terms of sentence number,
   * token number, and attribute number so its value can be quickly modified when switching from cue
   * to scope tagging
   */
  private transient Map<Integer, Map<Integer, Integer>> taggingAttributesForModification =
      new HashMap<>(RaptatConstants.DEFAULT_MODIFIABLE_ATTRIBUTES_AND_LABELS);

  /*
   * Determines how many tokens away a cue can be from another token to be considered a feature with
   * regard to scope. Tokens further than maxCueOffset froma cue will not have a feature added.
   * Those that are at position maxCueOffset or closer will have a featured added in a form like
   * 'LabeledCue@OFFSET' where OFFSET is the distance in tokens from the token to the cue/
   */
  private final int maxCueOffset;


  public CSFeatureBuilder(int wordConjunctionOffset, int wordNgramSize, int posConjunctionOffset,
      int posNgramSize, int maxCueOffset, String cueIncludedFeaturesFilePath,
      String scopeIncludedFeaturesFilePath) {
    FeatureBuilder.LOGGER.setLevel(Level.INFO);
    this.maxCueOffset = maxCueOffset;
    this.featureBuilderPipe = prepareFeatureBuilderPipe(wordConjunctionOffset, wordNgramSize,
        posConjunctionOffset, posNgramSize);
    initializeRemappings(cueIncludedFeaturesFilePath, scopeIncludedFeaturesFilePath);
  }


  @Override
  public CrfFeatureSet buildFeatures(List<AnnotatedPhrase> sentences) {
    throw new NotImplementedException("Not yet implemented: To be done");
  }


  /**
   * Take a list of annotated phrases, where each phrase generally represents a sentence, and modify
   * a list of ItemSequence instances (supplied as a parameter) and StringList instances (also
   * supplied as a parameter used to tag sequences using JCrfSuite or to train models using
   * JCrfSuite. The returned modifiableAttribues is a list of lists, with each inner list
   * corresponding to one sentence, and each Attribute in the list corresponding to one token.
   * Attributes in this List of lists objects are also part of the List of ItemSequence instances
   * returned by the method. Because of this, modifications to the modifiableAttribues will result
   * in changes to the ItemSequence instances returned by the method. Only attributes that have a
   * feature matching featureToMatch and that are part of a token with labeled labelToMatch will be
   * returned.
   *
   * @param sentences
   * @return
   */
  public RaptatPair<List<ItemSequence>, List<ItemSequence>> buildTaggingFeatures(
      List<AnnotatedPhrase> sentences) {
    Iterator<Instance> sentenceIterator = new MalletListIterator<>(sentences);
    List<ItemSequence> cueSentenceFeatureList = new ArrayList<>(sentences.size());
    List<ItemSequence> scopeSentenceFeatureList = new ArrayList<>(sentences.size());

    /* Create sentence features and add them to the list via the pipe */
    InstanceList sentenceFeatureInstances = new InstanceList(this.featureBuilderPipe);
    sentenceFeatureInstances.addThruPipe(sentenceIterator);

    /*
     * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
     */
    // /*
    // * This is used to indicate a feature that will be stored for easy
    // * modification when switching from cue to scope tagging
    // */
    // String featureToMatchForStorage = Constants.MODIFIABLE_CUE_ATTRIBUTE;
    //
    // int sentenceNumber = 0;

    for (Instance curSentenceFeatures : sentenceFeatureInstances) {
      TokenSequence tokens = (TokenSequence) curSentenceFeatures.getData();

      /*
       * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
       */
      // RaptatPair<ItemSequence, ItemSequence> cueAndScopeItems = this
      // .taggingInstanceFeaturesToItemSequence( tokens,
      // featureToMatchForStorage, sentenceNumber++ );

      /*
       * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
       * commented out above on 7/12/16
       */
      RaptatPair<ItemSequence, ItemSequence> cueAndScopeItems =
          taggingInstanceFeaturesToItemSequence(tokens);
      cueSentenceFeatureList.add(cueAndScopeItems.left);
      scopeSentenceFeatureList.add(cueAndScopeItems.right);
    }

    return new RaptatPair<>(cueSentenceFeatureList, scopeSentenceFeatureList);
  }


  /**
   * Take a list of annotated phrases, where each phrase generally represents a sentence, and
   * creates ItemSequences and StringList instances for training cue and scope labeling for each
   * sentence.
   *
   * @param cueSentences
   * @param scopeSentences
   * @param featureToMatch
   * @param labelSelectors
   * @param labelsToTurnOff
   * @param labelsToStore
   * @return
   */
  public CrfFeatureSet buildTrainingFeatures(List<AnnotatedPhrase> cueSentences,
      HashSet<AnnotatedPhrase> scopeSentences) {
    List<ItemSequence> cueSentenceFeatureList = new ArrayList<>(cueSentences.size());
    List<StringList> cueSentenceTokenLabels = new ArrayList<>(cueSentences.size());
    List<ItemSequence> scopeSentenceFeatureList = new ArrayList<>(scopeSentences.size());
    List<StringList> scopeSentenceTokenLabels = new ArrayList<>(scopeSentences.size());

    /* Create sentence features and add them to the list via the pipe */
    InstanceList sentenceFeatureInstances = new InstanceList(this.featureBuilderPipe);
    Iterator<Instance> malletSentenceIterator = new MalletListIterator<>(cueSentences);
    sentenceFeatureInstances.addThruPipe(malletSentenceIterator);

    /*
     * We will simultaneously iterate through the sentenceFeatureInstances and the cue sentences to
     * identify the cue sentences that are also scope sentences. For those identified, we will add
     * sentence features and token labels to both the cueSentenceFeatureList and
     * scopeSentenceFeatureList as well as the lists of both cueSentenceTokenLabels
     * scopeSentenceTokenLabels. Otherwise, we will only add sentence features to the
     * cueSentenceFeatureList and the cueSentenceTokenLabels.
     */
    Iterator<AnnotatedPhrase> cueSentenceListIterator = cueSentences.iterator();

    for (Instance curSentenceFeatures : sentenceFeatureInstances) {
      TokenSequence tokens = (TokenSequence) curSentenceFeatures.getData();
      LabelSequence labels = (LabelSequence) curSentenceFeatures.getTarget();

      /*
       * Call different conversion methods depending on whether the sentence is one of the scope
       * sentences or not
       */
      if (scopeSentences.contains(cueSentenceListIterator.next())) {
        CueScopeFeaturesAndLabels featuresAndLabels =
            trainingInstanceFeaturesToItemSequence(tokens, labels);
        cueSentenceFeatureList.add(featuresAndLabels.cueFeatures);
        cueSentenceTokenLabels.add(featuresAndLabels.cueLabels);
        scopeSentenceFeatureList.add(featuresAndLabels.scopeFeatures);
        scopeSentenceTokenLabels.add(featuresAndLabels.scopeLabels);
      } else {
        RaptatPair<ItemSequence, StringList> cueFeaturesAndLabels =
            trainingInstanceFeaturesToItemSequenceCueOnly(tokens, labels);
        cueSentenceFeatureList.add(cueFeaturesAndLabels.left);
        cueSentenceTokenLabels.add(cueFeaturesAndLabels.right);
      }
    }

    CueScopeFeatureSet finalFeatureSet = new CueScopeFeatureSet(cueSentenceFeatureList,
        cueSentenceTokenLabels, scopeSentenceFeatureList, scopeSentenceTokenLabels);

    return finalFeatureSet;
  }


  public HashMap<Integer, List<Integer>> createMapFromCueLocations(int[] cueLocations,
      int numberOfTokens) {
    HashMap<Integer, List<Integer>> resultMap = new HashMap<>(cueLocations.length);
    if (cueLocations.length > 0) {
      for (int tokenPosition = 0; tokenPosition < numberOfTokens; tokenPosition++) {
        for (int cueLocationIndex = 0; cueLocationIndex < cueLocations.length; cueLocationIndex++) {
          int offset = cueLocations[cueLocationIndex] - tokenPosition;
          if (Math.abs(offset) <= this.maxCueOffset) {
            List<Integer> offsetList;
            if ((offsetList = resultMap.get(tokenPosition)) == null) {
              offsetList = new ArrayList<>(numberOfTokens);
              resultMap.put(tokenPosition, offsetList);
            }
            offsetList.add(offset);
          }
        }
      }
    }
    return resultMap;
  }


  /** @return the includedScopeFeaturesSet */
  public HashSet<String> getIncludedScopeFeaturesSet() {
    return this.includedScopeFeaturesSet;
  }


  /** @return the taggingAttributesForModification */
  public Map<Integer, Map<Integer, Integer>> getTaggingAttributesForModification() {
    return this.taggingAttributesForModification;
  }


  /** */
  public void initializeTransients() {
    this.taggingAttributesForModification =
        new HashMap<>(RaptatConstants.DEFAULT_MODIFIABLE_ATTRIBUTES_AND_LABELS);
  }


  /**
   * @param malletLabels
   * @return
   */
  private HashMap<Integer, List<Integer>> getCueFeatureMap(LabelSequence malletLabels) {
    int[] cueLocations = getCueLocations(malletLabels);
    return createMapFromCueLocations(cueLocations, malletLabels.size());
  }


  /**
   * Determines which labels correspond to a cue
   *
   * @param malletLabels
   * @return
   */
  private int[] getCueLocations(LabelSequence malletLabels) {
    LabelSequence.Iterator labelIterator = malletLabels.iterator();
    List<Integer> cuePositions = new ArrayList<>(malletLabels.size());

    int currentPosition = 0;

    while (labelIterator.hasNext()) {
      String curLabel = labelIterator.next().toString();
      if (curLabel.equals(RaptatConstants.BEGIN_CUE_STRING)
          || curLabel.equals(RaptatConstants.INSIDE_CUE_STRING)) {
        cuePositions.add(currentPosition);
      }
      currentPosition++;
    }

    return Ints.toArray(cuePositions);
  }


  /**
   * Creates remappings for features and labels so that they can be changed from generalized
   * features or labels to cue or scope specific features or labels
   *
   * @param IncludedFeaturesFilePath
   */
  private void initializeRemappings(String cueIncludedFeaturesFilePath,
      String scopeIncludedFeaturesFilePath) {
    /*
     * Note that the Constants.SCOPE_FEATURE_TAG was originally only 5 characters, and we will only
     * be mapping features whose first five characters map to strings within cueFeatureRemappings.
     * This remapping gets rid of features intended only for determining scope, which involves MITRE
     * scope features and certain features for conjunctions.
     */
    String featureTag = RaptatConstants.SCOPE_FEATURE_TAG;
    if (featureTag.length() > CSFeatureBuilder.MAX_TAG_LENGTH) {
      featureTag = featureTag.substring(0, CSFeatureBuilder.MAX_TAG_LENGTH);
    }
    CSFeatureBuilder.scopeFeatureToCueFeatureRemap.put(featureTag, "");

    /*
     * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
     */
    // /*
    // * The scopeLabeledFeatureRemappings allow us to determine where the
    // cue
    // * is and change an attribute that indicates this before starting
    // scope
    // * training. Using this variable, we can identify labels indicating
    // that
    // * the token is part of a cue and then and add that fact as a feature
    // * that can be used during scope training. During feature building, we
    // * create an attribute with the name
    // Constants.MODIFIABLE_CUE_ATTRIBUTE
    // * specifically to allow the addition of this feature.
    // */
    // Map<String, String> featureRemap = new HashMap<String, String>();
    // featureRemap.put( Constants.MODIFIABLE_CUE_ATTRIBUTE,
    // Constants.CUENEGATION_FEATURE_STRING );
    // cueLabelToScopeFeatureRemap.put( Constants.BEGIN_CUE_STRING,
    // featureRemap );
    // cueLabelToScopeFeatureRemap.put( Constants.INSIDE_CUE_STRING,
    // featureRemap );

    CSFeatureBuilder.scopeLabelToCueLabelRemap.put(RaptatConstants.BEGIN_SCOPE_STRING,
        RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING);
    CSFeatureBuilder.scopeLabelToCueLabelRemap.put(RaptatConstants.INSIDE_SCOPE_STRING,
        RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING);

    CSFeatureBuilder.cueLabelToScopeLabelRemap.put(RaptatConstants.BEGIN_CUE_STRING,
        RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING);
    CSFeatureBuilder.cueLabelToScopeLabelRemap.put(RaptatConstants.INSIDE_CUE_STRING,
        RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING);

    initializeIncludedFeatures(cueIncludedFeaturesFilePath, this.includedCueFeaturesSet);
    initializeIncludedFeatures(scopeIncludedFeaturesFilePath, this.includedScopeFeaturesSet);
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

    /*
     * The maxCueTokenOffset and maxCueConjunctionSize variables are used in the
     * OffsetConjunctionsRaptat constructor. When used for cue and scope feature building, they are
     * used to identify conjunction features that are differentially labeled for cue and scope. This
     * reduces the need for separate feature building for cue and scope.
     */
    int maxCueTokenOffset = 2;
    int maxCueConjunctionSize = 2;

    int[][] wordConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        wordConjunctionOffset, wordConjunctionOffset, wordConjunctionWidth, true);

    int[][] posConjunctionArray = ConjunctionOffsetArrayBuilder.generateConjunctionOffsetArray(
        posConjunctionOffset, posConjunctionOffset, posConjunctionWidth, true);

    try {
      Pipe[] pipeArray = new Pipe[] {new CSFeaturePipe(),
          new RegexMatches("ALPHABETIC", Pattern.compile("[A-Za-z][a-z]*")),
          new RegexMatches("NONALPHABETIC", Pattern.compile(".*[^A-Za-z].*")),
          new TokenTextCharPrefix("PREFIX=", 3), new TokenTextCharPrefix("PREFIX=", 4),
          new TokenTextCharSuffix("SUFFIX=", 3), new TokenTextCharSuffix("SUFFIX=", 4),
          new OffsetConjunctionsRaptat(true, Pattern.compile("W=.*"), wordConjunctionArray,
              RaptatConstants.SCOPE_FEATURE_TAG, maxCueTokenOffset, maxCueConjunctionSize),
          new OffsetConjunctionsRaptat(true, Pattern.compile("POS=.*"), posConjunctionArray,
              RaptatConstants.SCOPE_FEATURE_TAG, maxCueTokenOffset, maxCueConjunctionSize),
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
   * Takes a sequence of tokens (generally corresponding to the tokens of a single sentence) that
   * are provided as a TokenSequence instance (a Mallet object) and returns an ItemSequence instance
   * <br>
   * <br>
   * ItemSequence objects in CRFSuite are similar to TokenSequence objects in Mallet in that they
   * store a sequence of token features for a sequence of tokens from a sentence. <br>
   * <br>
   *
   * @param malletTokens - A TokenSequence object storing the features for a sequence of tokens
   * @param featureToMatchForStorage
   * @param sentenceNumber
   * @return - A pair of ItemSequence instances, with the one on the left for tagging cues, and the
   *         one on the right for tagging scope
   */

  /*
   * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
   */
  // private RaptatPair<ItemSequence, ItemSequence>
  // taggingInstanceFeaturesToItemSequence(TokenSequence malletTokens,
  // String featureToMatchForStorage, int sentenceNumber)
  private RaptatPair<ItemSequence, ItemSequence> taggingInstanceFeaturesToItemSequence(
      TokenSequence malletTokens) {
    /*
     * An Item corresponds to one token and may contain multiple attributes. An ItemSequence
     * contains multiple tokens, generally from a sentence. So an ItemSequence contains all the
     * tokens and attributes/features of those tokens.
     */
    ItemSequence cueFeatureSequence = new ItemSequence();
    ItemSequence scopeFeatureSequence = new ItemSequence();

    Iterator<cc.mallet.types.Token> tokenIterator = malletTokens.iterator();

    /*
     * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
     */
    // Map<Integer, Integer> tokenToFeatureMap = new HashMap<Integer,
    // Integer>( Constants.MAX_SENTENCE_TOKENS * 2 );
    // this.taggingAttributesForModification.put( sentenceNumber,
    // tokenToFeatureMap );

    /*
     * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
     */
    // int tokenNumber = 0;

    /* Loop through the tokens in the sentence */
    while (tokenIterator.hasNext()) {
      cc.mallet.types.Token token = tokenIterator.next();
      PropertyList.Iterator featureIterator = token.getFeatures().iterator();
      Item cueTokenAttributes = new Item();
      Item scopeTokenAttributes = new Item();

      /*
       * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
       */
      // int scopeFeatureNumber = 0;

      /* Loop through the features for each token */
      while (featureIterator.hasNext()) {
        featureIterator.next();
        String curFeature = escapeCrfSuiteCharacters(featureIterator.getKey());
        String matchString = curFeature.length() > CSFeatureBuilder.MAX_TAG_LENGTH
            ? curFeature.substring(0, CSFeatureBuilder.MAX_TAG_LENGTH)
            : curFeature;

        String cueFeature;
        if ((cueFeature =
            CSFeatureBuilder.scopeFeatureToCueFeatureRemap.get(matchString)) == null) {
          cueFeature = curFeature;
        }

        /*
         * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
         */
        // /*
        // * We should not need the featureToMatchForStorage as a
        // feature
        // * to detect cues - it is only used to switch cue tokens to a
        // * scope attribute when going from identifying cues to
        // * identifying scope
        // */
        // if ( !cueFeature.equals( featureToMatchForStorage ) &&
        // this.includedCueFeaturesSet.isEmpty()
        // || this.includedCueFeaturesSet.contains( curFeature ) )
        // {
        // if ( cueFeature.length() > 0 )
        // {
        // cueTokenAttributes.add( new Attribute( cueFeature ) );
        // }
        // }

        /*
         * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
         * commented out above on 7/12/16
         */
        if (this.includedCueFeaturesSet.isEmpty()
            || this.includedCueFeaturesSet.contains(curFeature)) {
          if (cueFeature.length() > 0) {
            cueTokenAttributes.add(new Attribute(cueFeature));
          }
        }

        String scopeFeature = curFeature;
        if (this.includedScopeFeaturesSet.isEmpty()
            || this.includedScopeFeaturesSet.contains(scopeFeature)) {
          if (scopeFeature.length() > 0) {
            scopeTokenAttributes.add(new Attribute(scopeFeature));

            /*
             * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
             */
            // if ( scopeFeature.equals( featureToMatchForStorage )
            // )
            // {
            // tokenToFeatureMap.put( tokenNumber,
            // scopeFeatureNumber );
            // }
            // scopeFeatureNumber++ ;
          }
        }
      }
      cueFeatureSequence.add(cueTokenAttributes);
      scopeFeatureSequence.add(scopeTokenAttributes);
      /*
       * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
       */
      // tokenNumber++ ;
    }

    return new RaptatPair<>(cueFeatureSequence, scopeFeatureSequence);
  }


  /**
   * The method serves as an extension to the other method with a simpler signature,
   * instanceFeaturesToItemsSequence(TokenSequence, LabelSequence). This method is called during
   * model training, and it enhances that method by also storing a list of attributes for tokens
   * that have a feature and label matching featureToMatch and labelToMatch in the field,
   * attributesForModification. It also stores certain labels that will be turned on or turned off
   * after cue training and before scope training.
   *
   * @param malletTokens - A TokenSequence object storing the features for a sequence of tokens
   * @param malletLabels - A LabelSequence object containing the labels for the tokens in the
   *        malletTokens object
   * @return - A pair consisting of the ItemSequence and StringList for the sentence
   */
  private CueScopeFeaturesAndLabels trainingInstanceFeaturesToItemSequence(
      TokenSequence malletTokens, LabelSequence malletLabels) {

    /*
     * An Item corresponds to one token and may contain multiple attributes. An ItemSequence
     * contains multiple tokens, generally from a sentence. So an ItemSequence contains all the
     * tokens and attributes of those tokens.
     */
    ItemSequence cueFeatureSequence = new ItemSequence();
    StringList cueLabelList = new StringList();

    ItemSequence scopeFeatureSequence = new ItemSequence();
    StringList scopeLabelList = new StringList();

    Iterator<cc.mallet.types.Token> tokenIterator = malletTokens.iterator();
    LabelSequence.Iterator labelIterator = malletLabels.iterator();

    HashMap<Integer, List<Integer>> cueOffsetFeatureMap = getCueFeatureMap(malletLabels);
    int tokenIndex = 0;
    while (tokenIterator.hasNext()) {
      String cueLabel;
      String scopeLabel;

      cc.mallet.types.Token token = tokenIterator.next();

      String curLabel = labelIterator.next().toString();

      if ((cueLabel = CSFeatureBuilder.scopeLabelToCueLabelRemap.get(curLabel)) == null) {
        cueLabel = curLabel;
      }
      cueLabelList.add(cueLabel);

      if ((scopeLabel = CSFeatureBuilder.cueLabelToScopeLabelRemap.get(curLabel)) == null) {
        scopeLabel = curLabel;
      }
      scopeLabelList.add(scopeLabel);

      PropertyList.Iterator featureIterator = token.getFeatures().iterator();

      /*
       * Now translate all the features for the current token to item attributes
       */
      Item tokenCueFeatures = new Item();
      Item tokenScopeFeatures = new Item();
      while (featureIterator.hasNext()) {
        featureIterator.next();
        String curFeature = escapeCrfSuiteCharacters(featureIterator.getKey());
        String matchStringForRemapping = curFeature.length() > CSFeatureBuilder.MAX_TAG_LENGTH
            ? curFeature.substring(0, CSFeatureBuilder.MAX_TAG_LENGTH)
            : curFeature;

        String cueFeature;
        if ((cueFeature =
            CSFeatureBuilder.scopeFeatureToCueFeatureRemap.get(matchStringForRemapping)) == null) {
          cueFeature = curFeature;
        }

        /*
         * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
         */
        /*
         * We should not need the MODIFIABLE_CUE_ATTRIBUTE as a feature to train to detect cues - it
         * is only used to switch cue tokens to a scope attribute when going from identifying cues
         * to identifying scope
         */
        // if ( !cueFeature.equals( Constants.MODIFIABLE_CUE_ATTRIBUTE )
        // && this.includedCueFeaturesSet.isEmpty()
        // || this.includedCueFeaturesSet.contains( cueFeature ) )
        // {
        // if ( cueFeature.length() > 0 )
        // {
        // tokenCueFeatures.add( new Attribute( cueFeature ) );
        // }
        // }

        /*
         * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
         * commented out above on 7/12/16
         */
        if (this.includedCueFeaturesSet.isEmpty()
            || this.includedCueFeaturesSet.contains(cueFeature)) {
          if (cueFeature.length() > 0) {
            tokenCueFeatures.add(new Attribute(cueFeature));
          }
        }

        /*
         * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
         */
        // Map<String, String> featureRemap;
        // String scopeFeature;
        // if ( ( featureRemap =
        // CSFeatureBuilder.cueLabelToScopeFeatureRemap.get( cueLabel )
        // ) == null
        // || ( scopeFeature = featureRemap.get( curFeature ) ) == null
        // )
        // {
        // scopeFeature = curFeature;
        // }
        //
        // if ( this.includedScopeFeaturesSet.isEmpty() ||
        // this.includedScopeFeaturesSet.contains( scopeFeature ) )
        // {
        // if ( scopeFeature.length() > 0 )
        // {
        // tokenScopeFeatures.add( new Attribute( scopeFeature ) );
        // }
        // }

        /*
         * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
         * commented out above on 7/12/16
         */
        if (this.includedScopeFeaturesSet.isEmpty()
            || this.includedScopeFeaturesSet.contains(curFeature)) {
          if (curFeature.length() > 0) {
            tokenScopeFeatures.add(new Attribute(curFeature));
          }
        }
      }

      /*
       * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
       * commented out above on 7/12/16
       */
      List<Integer> cueOffsets;
      if ((cueOffsets = cueOffsetFeatureMap.get(tokenIndex)) != null) {
        for (Integer currentOffset : cueOffsets) {
          String candidateFeature = RaptatConstants.CUE_OFFSET_STRING + "@" + currentOffset;
          if (this.includedScopeFeaturesSet.isEmpty()
              || this.includedScopeFeaturesSet.contains(candidateFeature)) {
            tokenScopeFeatures
                .add(new Attribute(RaptatConstants.CUE_OFFSET_STRING + "@" + currentOffset));
          }
        }
      }
      cueFeatureSequence.add(tokenCueFeatures);
      scopeFeatureSequence.add(tokenScopeFeatures);
      tokenIndex++;
    }

    return new CueScopeFeaturesAndLabels(cueFeatureSequence, cueLabelList, scopeFeatureSequence,
        scopeLabelList);
  }


  /**
   * The method serves as an extension to the other method with a simpler signature,
   * instanceFeaturesToItemsSequence(TokenSequence, LabelSequence). This method is called during
   * model training, and it enhances that method by also storing a list of attributes for tokens
   * that have a feature and label matching featureToMatch and labelToMatch in the field,
   * attributesForModification. It also stores certain labels that will be turned on or turned off
   * after cue training and before scope training.
   *
   * @param malletTokens - A TokenSequence object storing the features for a sequence of tokens
   * @param malletLabels - A LabelSequence object containing the labels for the tokens in the
   *        malletTokens object
   * @return - A pair consisting of the ItemSequence and StringList for the sentence
   */
  private RaptatPair<ItemSequence, StringList> trainingInstanceFeaturesToItemSequenceCueOnly(
      TokenSequence malletTokens, LabelSequence malletLabels) {

    /*
     * An Item corresponds to one token and may contain multiple attributes. An ItemSequence
     * contains multiple tokens, generally from a sentence. So an ItemSequence contains all the
     * tokens and attributes of those tokens.
     */
    ItemSequence cueFeatureSequence = new ItemSequence();
    StringList cueLabelList = new StringList();

    Iterator<cc.mallet.types.Token> tokenIterator = malletTokens.iterator();
    LabelSequence.Iterator labelIterator = malletLabels.iterator();

    while (tokenIterator.hasNext()) {
      String cueLabel;

      cc.mallet.types.Token token = tokenIterator.next();

      String curLabel = labelIterator.next().toString();

      if ((cueLabel = CSFeatureBuilder.scopeLabelToCueLabelRemap.get(curLabel)) == null) {
        cueLabel = curLabel;
      }
      cueLabelList.add(cueLabel);

      PropertyList.Iterator featureIterator = token.getFeatures().iterator();
      Item tokenCueFeatures = new Item();
      while (featureIterator.hasNext()) {
        featureIterator.next();
        String curFeature = escapeCrfSuiteCharacters(featureIterator.getKey());
        /*
         * We're only checking for a tag at the beginning of the feature, so we only take the first
         * MAX_TAG_LENGTH characters to look in cueFeatureRemappings for curFeature in the HashMap
         */
        String matchString = curFeature.length() > CSFeatureBuilder.MAX_TAG_LENGTH
            ? curFeature.substring(0, CSFeatureBuilder.MAX_TAG_LENGTH)
            : curFeature;

        String cueFeature;
        if ((cueFeature =
            CSFeatureBuilder.scopeFeatureToCueFeatureRemap.get(matchString)) == null) {
          cueFeature = curFeature;
        }

        /*
         * Modified by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references on 7/12/16
         */
        // /*
        // * We should not need the MODIFIABLE_CUE_ATTRIBUTE as a
        // feature
        // * to train to detect cues - it is only used to switch cue
        // * tokens to a scope attribute when going from identifying
        // cues
        // * to identifying scope
        // */
        // if ( !cueFeature.equals( Constants.MODIFIABLE_CUE_ATTRIBUTE )
        // && this.includedCueFeaturesSet.isEmpty()
        // || this.includedCueFeaturesSet.contains( cueFeature ) )
        // {
        // if ( cueFeature.length() > 0 )
        // {
        // tokenCueFeatures.add( new Attribute(
        // escapeCrfSuiteCharacters( cueFeature ) ) );
        // }
        // }

        /*
         * Added by Glenn to remove Constants.MODIFIABLE_CUE_ATTRIBUTE references and replace code
         * commented out above on 7/12/16
         */
        if (this.includedCueFeaturesSet.isEmpty()
            || this.includedCueFeaturesSet.contains(cueFeature)) {
          if (cueFeature.length() > 0) {
            tokenCueFeatures.add(new Attribute(escapeCrfSuiteCharacters(cueFeature)));
          }
        }
      }
      cueFeatureSequence.add(tokenCueFeatures);
    }

    return new RaptatPair<>(cueFeatureSequence, cueLabelList);
  }
}
