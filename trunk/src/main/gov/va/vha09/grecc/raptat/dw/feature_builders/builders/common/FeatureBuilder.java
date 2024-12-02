package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces.IColumnFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces.IDocumentFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.interfaces.IRowFeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.FeatureBuilderRunner;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.DocumentColumn;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Base class for all Feature Builders, supplying the common method to accept a RaptatDocument, then
 * iterate through each token to run the logic of the derived Builder.
 *
 * @author westerd
 */
public abstract class FeatureBuilder
    implements IColumnFeatureBuilder, IRowFeatureBuilder, IDocumentFeatureBuilder {

  /** The Constant STOP_LABEL. */
  final static Label STOP_LABEL = new Label("IndexValue");

  /** The Constant COL_PREFIX. */
  private static final String COL_PREFIX = "COL_";

  /** The Constant ROW_PREFIX. */
  private static final String ROW_PREFIX = "ROW_";

  private static final Logger LOGGER = Logger.getLogger(FeatureBuilder.class);
  {
    LOGGER.setLevel(Level.INFO);
  }

  /** The state. */
  private final FeatureBuilderState state;

  /**
   * A reference to the running currently controlling this feature builder
   **/
  private FeatureBuilderRunner featureBuilderRunner;

  /**
   * Instantiates a new feature builder.
   */
  protected FeatureBuilder() {
    this(new FeatureBuilderState());
  }

  protected FeatureBuilder(final FeatureBuilderState state) {
    this.state = state;
  }

  /**
   * publicly exposed call to enact feature builder over RaptatDocument.
   *
   * @param document the document
   */
  @Override
  public void process(final RaptatDocument document) {
    final List<RaptatTokenPhrase> phrase_list = document.getTokenPhrases();
    for (int index = 0; index < phrase_list.size(); index++) {
      final RunDirection forward = RunDirection.Forward;
      final IndexedTree<IndexedObject<RaptatToken>> processedTokens = document.getProcessedTokens();

      processListItem(phrase_list, index, PhraseListType.Row, forward, processedTokens);
      processListItem(phrase_list, index, PhraseListType.Column, forward, processedTokens);
    }
  }

  /**
   * Process list as column of token phrases
   *
   * @param phrase_list the phrase list
   * @param runDirection the run direction
   * @param processedTokens the processed tokens
   */
  @Override
  public void process_column(final List<RaptatTokenPhrase> phrase_list,
      final RunDirection runDirection,
      final IndexedTree<IndexedObject<RaptatToken>> processedTokens) {

    process_list(phrase_list, PhraseListType.Column, runDirection, processedTokens);

  }

  /**
   * Process a list as a document of token phrases (one long row)
   *
   * @param tokenPhraseList the token phrase list
   * @param direction the direction
   * @param processedTokens the processed tokens
   */
  public void process_document(final List<RaptatTokenPhrase> tokenPhraseList,
      final RunDirection direction, final IndexedTree<IndexedObject<RaptatToken>> processedTokens) {
    process_list(tokenPhraseList, PhraseListType.Document, direction, processedTokens);
  }

  /**
   * Process a list as a row of token phrases
   *
   * @param phrase_list the phrase list
   * @param runDirection the run direction
   * @param processedTokens the processed tokens
   */
  @Override
  public void process_row(final List<RaptatTokenPhrase> phrase_list,
      final RunDirection runDirection,
      final IndexedTree<IndexedObject<RaptatToken>> processedTokens) {
    process_list(phrase_list, PhraseListType.Row, runDirection, processedTokens);
  }

  public void reset() {
    this.state.reset();

  }

  public void setRunner(final FeatureBuilderRunner featureBuilderRunner) {
    this.featureBuilderRunner = featureBuilderRunner;
  }

  /**
   * Derived class to provide a generated String feature.
   *
   * @param foundLabel the found label
   * @param runDirection
   * @return the string
   */
  protected String generatePhraseFeature(final Label foundLabel, final RunDirection direction) {
    return foundLabel.getCategory() + "_" + foundLabel.getSubcategory() + "_" + direction.name();
  }

  /**
   * Derived class to provide a String corresponding to the Label name to check.
   *
   * @return the label to check
   */
  protected abstract String getLabelToCheck();

  /**
   * Provides a check against a build to see if it can run on rows and columns, or should this only
   * be run against columns (e.g. don't calc line distance when looking at a row. Default is a
   * feature builder can run both on a row and a column (e.g., Token Distance)
   *
   * @return
   */
  protected boolean isColumnOnlyBuilder() {
    return false;
  }

  /**
   * Checks if is label of interest found.
   *
   * @param phrase the phrase
   * @return the label
   */
  protected Label isLabelOfInterestFound(final RaptatTokenPhrase phrase) {
    Label found = null;
    for (final Label label : phrase.getPhraseLabels()) {
      if (label.getCategory().equalsIgnoreCase(getLabelToCheck())) {
        found = label;
        break;
      }
    }
    return found;
  }

  /**
   * Modify generated feature string; override to enhance the feature string based on other contexts
   *
   * @param transaction
   *
   * @param phraseType the phrase type
   * @param phrase the phrase
   * @param featureString the feature string
   * @param foundLabel
   * @return the string
   */
  protected void modifyGeneratedFeatureString(final IFeatureBuilderState transaction,
      final PhraseListType phraseType, final RaptatTokenPhrase phrase, final String featureString,
      final Label foundLabel) {

  }

  /**
   * Process a token phrase to check if the label is found for this feature builder
   *
   * @param phrase_list
   *
   * @param phrase the phrase
   * @param phraseType the phrase type
   * @param runDirection
   * @param phrase_list the phrase list
   * @param processedTokens
   */
  protected void processListItem(final List<RaptatTokenPhrase> phrase_list, final int phrase_index,
      final PhraseListType phraseType, final RunDirection runDirection,
      final IndexedTree<IndexedObject<RaptatToken>> processedTokens) {
    if (isColumnOnlyBuilder() && phraseType.equals(PhraseListType.Column) == false) {
      return;
    }

    /*
     * Commented out by Glenn - 10/8/19
     */
    // ArrayList<IndexedObject<RaptatToken>> allNodes = processedTokens.getAllNodes();


    final RaptatTokenPhrase phrase = phrase_list.get(phrase_index);

    final int phrase_start = phrase.getStartOffsetAsInt();
    final int phrase_end = phrase.getEndOffsetAsInt();

    /*
     * Added by Glenn - 10/8/19
     */
    final RaptatToken startToken =
        processedTokens.getClosestLesserOrEqualObject(phrase_start).getIndexedObject();
    final RaptatToken endToken =
        processedTokens.getClosestLesserOrEqualObject(phrase_end).getIndexedObject();
    // End add by Glenn

    /*
     * Commented out by Glenn - 10/8/19
     */
    // List<IndexedObject<RaptatToken>> actual_list_of_tokens = new ArrayList<>();
    //
    // // TODO -- This needs to be refactored, such that a tree of these nodes,
    // // keyed to start offset, is passed from the top-most levels
    // /**
    // * Iterate through all the nodes looking for this phrase
    // */
    // outer: for (int index = 0; index < allNodes.size(); index++ )
    // {
    // IndexedObject<RaptatToken> node = allNodes.get( index );
    // RaptatToken actualRaptatToken = node.getIndexedObject();
    //
    // int actualRaptatTokenStart = actualRaptatToken.get_start_offset_as_int();
    // if ( actualRaptatTokenStart == phrase_start )
    // {
    // /**
    // * Once the phrase start is found, start looking for the ending token
    // */
    // actual_list_of_tokens.add( node );
    // index++ ;
    // while (index < allNodes.size())
    // {
    // node = allNodes.get( index );
    // actualRaptatToken = node.getIndexedObject();
    // int actualRaptatTokenEnd = actualRaptatToken.get_end_offset_as_int();
    // if ( actualRaptatTokenEnd <= phrase_end )
    // {
    // actual_list_of_tokens.add( node );
    // }
    // else
    // {
    // break outer;
    // }
    // index++ ;
    // }
    // }
    // }
    // End commented out by Glenn

    // Stream<IndexedObject<RaptatToken>> streamed =
    // allNodes.stream().filter((Predicate<IndexedObject<RaptatToken>>) t ->
    // {
    // RaptatToken indexedObject = t.getIndexedObject();
    //
    // int comp_start = indexedObject.get_start_offset_as_int();
    // String comp_text = indexedObject.getTokenStringUnprocessed();
    //
    // RaptatTokenPhrase inner_ref_phrase = phrase;
    //
    // int phrase_start = inner_ref_phrase.get_start_offset_as_int();
    // String phrase_text = inner_ref_phrase.getTokensAsList().get( 0
    // ).getTokenStringPreprocessed();
    //
    // boolean found = comp_start == phrase_start &&
    // comp_text.trim().compareToIgnoreCase( phrase_text.trim() ) == 0;
    // return found;
    //
    // } );
    //
    // Optional<IndexedObject<RaptatToken>> findFirst =
    // streamed.findFirst();
    //
    // if (findFirst.isPresent() == false) {
    // throw new NullArgumentException( "findFirst" );
    // }

    // IndexedObject<RaptatToken> raptatObject = findFirst.get();

    final Label foundLabel = isLabelOfInterestFound(phrase);

    // int startIndex = actual_list_of_tokens.get(0).getIndexedObject().getTokenIndexInDocument();
    // int endIndex = actual_list_of_tokens.get(actual_list_of_tokens.size() - 1).getIndexedObject()
    // .getTokenIndexInDocument();

    /*
     * Added by Glenn - 10/8/19
     */
    final int startIndex = startToken.getTokenIndexInDocument();
    final int endIndex = endToken.getTokenIndexInDocument();
    // End add by Glenn

    if (foundLabel != null) {
      final String featureString =
          phraseType.getPrefixString() + generatePhraseFeature(foundLabel, runDirection);
      this.state.updateFeatureState(phraseType, foundLabel, featureString, phrase, startIndex,
          endIndex);
    } else {
      this.state.updateFeatureState(phraseType, phrase, startIndex, endIndex);
    }

    phrase.addPhraseFeatures(this.state.getFeatureState());

    if (LOGGER.getLevel().equals(Level.DEBUG)) {
      log_current_featuring(phrase);
    }
  }

  /**
   * Derived method that performs the class-specific evaluation.
   *
   * @param phrase the token phrases
   * @param previous_row_features the previous row features
   * @param previous_column_features the previous column features
   */
  protected void update_with_previous_phrase(final RaptatTokenPhrase phrase,
      final List<String> previous_row_features, final List<String> previous_column_features) {

    if (previous_row_features != null) {
      phrase.addPhraseFeatures(previous_row_features);
    }
    if (previous_column_features != null) {
      phrase.addPhraseFeatures(previous_column_features);
    }

  }

  /**
   * Gets the previous column labels. Will include from any columns to which the phrase is a member
   *
   * @param raptatTokenPhrase the raptat token phrase
   * @return the previous column labels
   */
  @Deprecated
  private List<String> get_previous_column_features(final RaptatTokenPhrase raptatTokenPhrase) {

    final List<DocumentColumn> sorted_column_list = raptatTokenPhrase.getColumnsContaining();
    final List<String> phraseFeatures = new ArrayList<>();
    for (final DocumentColumn column_of_phrases : sorted_column_list) {
      final int index = column_of_phrases.columnPhrases.indexOf(raptatTokenPhrase);
      if (index > 0) {
        final RaptatTokenPhrase previous_phrase_in_column =
            column_of_phrases.columnPhrases.get(index - 1);
        final Set<String> currentFeatures = previous_phrase_in_column.getPhraseFeatures();
        for (final String feature : currentFeatures) {
          /*
           * It is possible that a phrase above this current phrase may be referred to as a ROW_
           * element, which should be dropped.
           */
          if (feature.contains(ROW_PREFIX) == false) {
            String feature_str = feature;

            if (feature_str.indexOf(COL_PREFIX) != 0) {
              feature_str = COL_PREFIX + feature;
            }
            phraseFeatures.add(feature_str);
          }
        }
      }
    }

    return phraseFeatures;
  }

  /**
   * Gets the previous row labels.
   *
   * @param raptatTokenPhrase the raptat token phrase
   * @return the previous row labels
   */
  @Deprecated
  private List<String> get_previous_row_features(final RaptatTokenPhrase raptatTokenPhrase) {
    final List<RaptatTokenPhrase> tokenPhraseList =
        raptatTokenPhrase.get_parent_line().getTokenPhraseList();
    tokenPhraseList.sort((o1, o2) -> o1.getStartOffsetAsInt() - o2.getStartOffsetAsInt());
    final int index = tokenPhraseList.indexOf(raptatTokenPhrase);
    if (index == 0) {
      return null;
    }
    final RaptatTokenPhrase previous_row_phrase = tokenPhraseList.get(index - 1);
    final Set<String> phraseFeatures = previous_row_phrase.getPhraseFeatures();
    final List<String> row_features = new ArrayList<>();
    for (final String feature : phraseFeatures) {

      if (feature.indexOf(COL_PREFIX) == -1) {

        String feature_str = feature;
        if (feature.indexOf(ROW_PREFIX) != 0) {
          feature_str = ROW_PREFIX + feature;
        }
        row_features.add(feature_str);

      }

    }
    return row_features;
  }

  /**
   * Log previous and current featuring.
   *
   * @param phrase_list the phrase list
   * @param phrase the phrase
   */
  private void log_current_featuring(final RaptatTokenPhrase phrase) {
    System.out.println("\n***************************************");
    System.out.println("Current Features: " + phrase);
    System.out.println("***************************************\n");
  }

  /**
   * Check the labels for phraseToken to see if it contains the stop label.
   *
   * @param phrase the phrase
   * @return true, if successful
   */
  @Deprecated
  private boolean phraseContainsStopLabel(final RaptatTokenPhrase phrase) {
    return phrase.getPhraseLabels().contains(STOP_LABEL);
  }

  /**
   * Generic method to process a list based on PhraseListType
   *
   * @param phrase_list the phrase list
   * @param phraseListType the phrase list type
   * @param runDirection the run direction
   * @param processedTokens the processed tokens
   */
  private void process_list(final List<RaptatTokenPhrase> phrase_list,
      final PhraseListType phraseListType, RunDirection runDirection,
      final IndexedTree<IndexedObject<RaptatToken>> processedTokens) {

    runDirection = runDirection == null ? RunDirection.Forward : runDirection;

    final int directionMultiplier = runDirection.equals(RunDirection.Forward) ? 1 : -1;
    phrase_list.sort((lhs, rhs) -> directionMultiplier
        * (lhs.getStartOffsetAsInt() - rhs.getStartOffsetAsInt()));

    for (int index = 0; index < phrase_list.size(); index++) {
      processListItem(phrase_list, index, phraseListType, runDirection, processedTokens);
    }
  }

}
