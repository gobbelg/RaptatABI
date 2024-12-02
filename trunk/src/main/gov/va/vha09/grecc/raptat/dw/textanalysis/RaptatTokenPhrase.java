package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.common.IComparableSpan;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.common.StringHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.DocumentColumn;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Class stub used in anticipation of upcoming RaptatTokenPhrase from RapTAT.
 *
 * @author westerd
 */
/**
 * @author gtony
 *
 */
public class RaptatTokenPhrase implements IComparableSpan, Serializable {

	/**
	 *
	 * DAX 5/7/19
	 *
	 * Added to create a later common point for any logic dealing with Labels.
	 * At this point just encapsulates a String
	 *
	 * @author westerd
	 *
	 */
	static public class Label implements Comparable<Label>, Serializable {

		/** The feature search pattern. */
		final private String category;
		/** The feature type. */
		final private String subcategory;
		final static public String UNASSIGNED = "unassigned";

		public final static String INDEX_VALUE_STRING = "IndexValue";

		public final static Label LABEL_UNASSIGNED = Label
				.createUnassignedLabel();

		public Label(final String category) {
			this(category, category);
		}

		/**
		 * Instantiates a new label.
		 *
		 * @param category
		 *            the feature search pattern
		 * @param subcategory
		 *            the feature type
		 */
		public Label(final String category, final String subcategory) {
			this.category = category.toLowerCase();
			this.subcategory = subcategory.toLowerCase();
		}

		/**
		 * Compare to.
		 *
		 * @param label
		 *            the label
		 * @return the int
		 */
		@Override
		public int compareTo(final Label label) {
			if ( equals(label) ) {
				return 0;
			}
			return -1;
		}

		/**
		 * Equals.
		 *
		 * @param obj
		 *            the obj
		 * @return true, if successful
		 */
		@Override
		public boolean equals(final Object obj) {
			if ( obj == this ) {
				return true;
			}

			if ( !(obj instanceof Label) ) {
				return false;
			}

			final Label lbl = (Label) obj;

			return new EqualsBuilder().append(this.category, lbl.category)
					.append(this.subcategory, lbl.subcategory).isEquals();
		}

		public String getCategory() {
			return this.category;
		}

		public String getSubcategory() {
			return this.subcategory;
		}

		public boolean hasAssignedCategory() {
			return this.category.equalsIgnoreCase(Label.UNASSIGNED) == false;
		}

		/**
		 * Hash code.
		 *
		 * @return the int
		 */
		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(this.category)
					.append(this.subcategory).toHashCode();
		}

		/**
		 * To string.
		 *
		 * @return the string
		 */
		@Override
		public String toString() {

			return Joiner.on(" ").join(new String[] {
					"(", this.category, ": ", this.subcategory, ")"
			});
		}

		public static Label createIndexValueLabel() {
			return new Label(INDEX_VALUE_STRING);
		}

		public static Label createUnassignedLabel() {
			return new Label(UNASSIGNED);
		}

	}

	/**
	 * Sort based on line unless on same line, then sort based on overlap with
	 * this RaptatTokenPhrase instance. If on the same line and the left
	 * overlaps more, it is "bigger."
	 *
	 * @author gobbelgt
	 *
	 */
	public class PhraseLineComparator implements Comparator<RaptatTokenPhrase> {

		@Override
		public int compare(final RaptatTokenPhrase left,
				final RaptatTokenPhrase right) {
			int lineDifference = left.get_line_index() - right.get_line_index();

			return lineDifference != 0 ? lineDifference
					: left.getOverlap(RaptatTokenPhrase.this)
							- right.getOverlap(RaptatTokenPhrase.this);
		}
	}

	/**
	 * Indicates how to carry out a search for phrases surround this one that
	 * indicate features. A "ROW" search stays within the same row of the
	 * document that this one is found in. A "DOCUMENT" search searches the row
	 * containing this phrase and the rows above or below that row. Specifying
	 * an a SearchScope enum value in some of the method calls allows us to
	 * re-use some of the same code.
	 *
	 * @author gtony
	 *
	 */
	public enum SearchScope {
		ROW, DOCUMENT;
	}

	static private class PhraseEndComparator
			implements Comparator<RaptatTokenPhrase> {

		@Override
		public int compare(final RaptatTokenPhrase left,
				final RaptatTokenPhrase right) {
			return left.endOffset - right.endOffset;
		}
	}

	static private class PhraseStartComparator
			implements Comparator<RaptatTokenPhrase> {
		@Override
		public int compare(final RaptatTokenPhrase left,
				final RaptatTokenPhrase right) {
			return left.startOffset - right.startOffset;
		}
	}

	{
		LOGGER.setLevel(Level.INFO);
	}

	// private final static Label INDEX_VALUE = new Label( INDEX_VALUE_STRING );

	/* The start offset. */
	private final int startOffset;

	/* The end offset. */
	private final int endOffset;

	/* The tokens as list. */
	private final List<RaptatToken> tokensAsList = new ArrayList<>();

	/*
	 * phraseLabels stores associations of the string indicated by the list of
	 * tokens in the phrase. We may expand this in the future to handle multiple
	 * labels.
	 */
	private final HashSet<Label> phraseLabels = new HashSet<>();
	/** The current line. */
	private TextDocumentLine parentLine;

	private int indexInLine;

	private Set<String> phraseFeatures = new HashSet<>();

	private PhraseLineComparator phraseLineComparator = new PhraseLineComparator();

	/*
	 * Field for storing the list of columns of RaptatTokenPhrase instances that
	 * also contain this particular RaptatTokenPhrase instance. Note that a
	 * RaptatTokenPhrase instance may be found in multiple columns.
	 */
	private List<DocumentColumn> columnsContaining = new ArrayList<>(128);

	private int lineStartOffsetByPhrase;

	private int lineEndOffsetByPhrase;

	private int phraseLength;

	/**
	 * Stores minimum distances to a line in phrases and is based on the columns
	 * the phrase is contained in. Generally, a line is one phrase in height. If
	 * this phrase is on line 1, the distance to line 3 is 2. However, if the
	 * line 2 is empty, the distance will by multiplied by a multiple specified
	 * by the constant EMPTY_LINE_HEIGHT within the TextDocumentLine class. The
	 * index into the array will correspond to the line, and the value stored at
	 * the index will contain the distance.
	 */
	private int[] minimumDistancesToLines;

	/**
	 * Used to determine length in terms of phrase distance of RaptatTokenPhrase
	 * instances that are unassigned. An unassigned phrase can often consist of
	 * multiple tokens, whereas most assigned ones are 1-3 tokens, so we divide
	 * the phrase length in tokens by this DIVISOR constant to get comparable
	 * lengths.
	 */
	private static final double DIVISOR_FOR_UNASSIGNED_PHRASE_LENGTH = 2.0;

	private static PhraseEndComparator phraseEndComparator = new PhraseEndComparator();

	private static PhraseStartComparator phraseStartComparator = new PhraseStartComparator();

	public final static String FEATURE_DELIMITER = "_";

	private final static Logger LOGGER = Logger
			.getLogger(RaptatTokenPhrase.class);

	/**
	 * Instantiates a new raptat token phrase.
	 *
	 * @param tokens
	 *            the tokens
	 */
	public RaptatTokenPhrase(final List<RaptatToken> tokens) {
		this.tokensAsList.addAll(tokens);
		Collections.sort(this.tokensAsList);
		RaptatToken startToken = this.tokensAsList.get(0);
		this.startOffset = startToken.get_start_offset_as_int();
		this.endOffset = this.tokensAsList.get(this.tokensAsList.size() - 1)
				.get_end_offset_as_int();
		this.minimumDistancesToLines = new int[startToken.getDocumentLines()
				.size()];
	}

	/**
	 * Instantiates a new raptat token phrase.
	 *
	 * @param tokens
	 *            the tokens
	 */
	public RaptatTokenPhrase(final List<RaptatToken> tokens, final Label label,
			final TextDocumentLine lineContainingPhrase) {
		this(tokens);
		this.phraseLabels.add(label);
		this.parentLine = lineContainingPhrase;
	}

	/**
	 * Instantiates a new raptat token phrase.
	 *
	 * @param token
	 *            the token
	 * @param label
	 *            the label
	 */
	public RaptatTokenPhrase(final RaptatToken token, final Label label) {
		this(new RaptatToken[] {
				token
		}, label);
	}

	/**
	 * Instantiates a new RaptatTokenPhrase instance.
	 *
	 * @param raptatTokens
	 *            the raptat tokens
	 * @param labels
	 *            the labels
	 */
	public RaptatTokenPhrase(final RaptatToken[] raptatTokens,
			final Label... labels) {
		this(Arrays.asList(raptatTokens));
		final List<Label> labelList = Arrays.asList(labels);
		this.phraseLabels.addAll(labelList);
	}

	public RaptatTokenPhrase(final RaptatToken[] tokenArray,
			final Label phraseLabel, final TextDocumentLine lineContaining) {
		this(tokenArray, phraseLabel);
		this.parentLine = lineContaining;
	}

	/**
	 * Add features to this RaptatTokenPhrase instance based on the labeling of
	 * RaptatTokenPhrase instances that are in the same column within a
	 * document, either above (before) or below (after) this one. Note that the
	 * RaptatTokenPhrase instance is generally the one we are trying to assign
	 * (when testing) or has been assigned (during training) to a concept.
	 *
	 * @param categoryToSubcategoryMap
	 */
	public void addColumnFeatures(
			final Map<String, Set<String>> categoryToSubcategoryMap) {

		String abovePhraseTag = "col_phrases_before";
		String aboveLineTag = "col_lines_before";
		addColumnFeaturesHelper(categoryToSubcategoryMap,
				Collections.reverseOrder(this.phraseLineComparator),
				abovePhraseTag, aboveLineTag);

		String belowPhraseTag = "col_phrases_after";
		String belowLineTag = "col_lines_after";
		addColumnFeaturesHelper(categoryToSubcategoryMap,
				this.phraseLineComparator, belowPhraseTag, belowLineTag);
	}

	/**
	 * DAX - 5/20/19 Added to create local reference to all column-space
	 * membership
	 */
	public void addContainingColumn(final DocumentColumn column) {
		this.columnsContaining.add(column);
	}

	/**
	 * Add features to this RaptatTokenPhrase instance based on the labeling of
	 * RaptatTokenPhrase instances that are in the same document as this one.
	 * Note that the RaptatTokenPhrase instance is generally the one we are
	 * trying to assign (when testing) or has been assigned (during training) to
	 * a concept.
	 *
	 * @param categoryToSubcategoryMap
	 */
	public void addDocumentFeatures(
			final Map<String, Set<String>> categoryToSubcategoryMap) {
		List<RaptatTokenPhrase> associatedPhrases = this.parentLine.sourceDocument
				.getTokenPhrases();
		addHorizontalFeatures(categoryToSubcategoryMap, associatedPhrases,
				SearchScope.DOCUMENT);
	}

	/**
	 * Add an individual feature, usually one discovered from review of the
	 * immediate phrase's label
	 *
	 * @param phraseFeature
	 */
	public void addPhraseFeature(final String phraseFeature) {
		if ( this.phraseFeatures.contains(phraseFeature) == false ) {
			this.phraseFeatures.add(phraseFeature);
		}
	}

	/**
	 * Add a List of features, usually already discovered from a row or column
	 *
	 * @param features
	 *            the features
	 */
	public void addPhraseFeatures(final List<String> features) {
		if ( features != null ) {
			for (final String feature : features) {
				addPhraseFeature(feature);
			}
		}
	}

	/*
	 * Modified by Glenn 9/11/19 - added this method which is just a renaming of
	 * the method setPhraseFeatures()
	 */
	public void addPhraseFeatures(final Set<String> featureState) {
		this.phraseFeatures.addAll(featureState);
	}

	/**
	 * Adds the phrase label.
	 *
	 * @param label
	 *            the label
	 */
	public void addPhraseLabel(final Label label) {
		this.phraseLabels.add(label);
	}

	/**
	 * Adds the phrase labels.
	 *
	 * @param labels
	 *            the labels
	 */
	public void addPhraseLabels(final List<Label> labels) {
		this.phraseLabels.addAll(labels);
	}

	/**
	 * Add features to this RaptatTokenPhrase instance based on the labeling of
	 * RaptatTokenPhrase instances that are in the same row to the left or right
	 * of this one. Note that the RaptatTokenPhrase instance is generally the
	 * one we are trying to assign (when testing) or has been assigned (during
	 * training) to a concept.
	 *
	 * @param categoryToSubcategoryMap
	 */
	public void addRowFeatures(
			final Map<String, Set<String>> categoryToSubcategoryMap) {

		/*
		 * Get all RaptatTokenPhrase instances that share the line with this one
		 * - they will be the source for the features assigned to *this*
		 * particular RaptatTokenPhrase instance.
		 */
		List<RaptatTokenPhrase> lineAssociatedPhrases = new ArrayList<>(
				this.parentLine.getTokenPhraseSet());

		addHorizontalFeatures(categoryToSubcategoryMap, lineAssociatedPhrases,
				SearchScope.ROW);

	}

	/**
	 * Compare to.
	 *
	 * @param rhs
	 *            the rhs
	 * @return the int
	 */
	@Override
	public int compareTo(final IComparableSpan rhs) {
		final IComparableSpan lhs = this;
		if ( lhs.getEndOffsetAsInt() > rhs.getStartOffsetAsInt() ) {
			if ( lhs.getStartOffsetAsInt() < rhs.getEndOffsetAsInt() ) {
				/*
				 * lhs and rhs overlap
				 */
				return 0;
			}
			/*
			 * lhs comes after (in terms of character offset) rhs.
			 */
			return 1;
		}

		/*
		 * lhs must come before rhs
		 */
		return -1;
	}

	/**
	 * Determine if this RaptatTokenPhrase instance contains a Label equal to
	 * the provided parameter, targetLabel.
	 *
	 * @param targetLabel
	 * @return
	 */
	public boolean containsLabelName(final Label targetLabel) {

		for (final Label tokenPhraseLabel : this.phraseLabels) {
			if ( targetLabel.getCategory()
					.equalsIgnoreCase(tokenPhraseLabel.getCategory()) ) {
				return true;
			}
		}
		return false;
	}

	public String debug_tokenPhraseDetails() {
		return String.format("[%s, %s] - %s %s", this.startOffset,
				this.endOffset, getPhraseText(),
				get_line_index() > -1
						? String.format("(line %s)", get_line_index())
						: "");
	}

	/**
	 * Equals.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(final Object obj) {
		if ( obj == this ) {
			return true;
		}
		if ( !(obj instanceof RaptatTokenPhrase) ) {
			return false;
		}
		final RaptatTokenPhrase rtp = (RaptatTokenPhrase) obj;

		/*
		 * Going to try this less complete definition of equals, discounting
		 * inner tokens for now, for it should be reasonable that
		 * one-and-only-one phrase exists in the space defined by start, end,
		 * line index, and token count.
		 */
		return new EqualsBuilder().append(this.startOffset, rtp.startOffset)
				.append(this.endOffset, rtp.endOffset)
				.append(get_line_index(), rtp.get_line_index())
				.append(this.tokensAsList.size(), rtp.tokensAsList.size())
				.isEquals();
	}

	/**
	 * Gets the end offset relative to line.
	 *
	 * @return the end offset relative to line
	 */
	public int get_end_offset_relative_to_line() {
		return getEndOffsetAsInt() - this.parentLine.startOffset;
	}

	/**
	 * Gets the line index.
	 *
	 * @return the line index
	 */
	@Override
	public int get_line_index() {
		return this.parentLine == null ? -1 : this.parentLine.getLineIndex();
	}

	/**
	 * Gets the parent line.
	 *
	 * @return the parent line
	 */
	public TextDocumentLine get_parent_line() {
		return this.parentLine;
	}

	/**
	 * Gets the start offset relative to line.
	 *
	 * @return the start offset relative to line
	 */
	public Integer get_start_offset_relative_to_line() {
		return getStartOffsetAsInt() - this.parentLine.startOffset;
	}

	/**
	 * Find the relative distance (lhs on left, rhs on right) of two token
	 * phrases. Comparison across lines results in an exception
	 *
	 * @param lhs
	 *            the lhs
	 * @param rhs
	 *            the rhs
	 * @return the token phrase distance
	 * @throws RaptatTokenPhraseException
	 *             the raptat token phrase exception
	 */
	public int get_token_phrase_distance(final RaptatTokenPhrase lhs,
			final RaptatTokenPhrase rhs) throws RaptatTokenPhraseException {
		if ( lhs.parentLine.equals(rhs.parentLine) == false ) {
			throw new RaptatTokenPhraseException(String.format(
					"Cannot obtain token phrase distance from different lines: lhs = %s, rhs = %s",
					lhs, rhs));
		}
		int lhs_index = 0;
		int rhs_index = 0;
		for (int index = 0; index < this.parentLine.getTokenPhraseList()
				.size(); index++) {
			final RaptatTokenPhrase raptatTokenPhrase = this.parentLine
					.getTokenPhraseList().get(index);
			if ( raptatTokenPhrase.equals(lhs) ) {
				lhs_index = index;
			}
			if ( raptatTokenPhrase.equals(rhs) ) {
				rhs_index = index;
			}
		}
		return rhs_index - lhs_index;

	}

	public List<Integer> getColumnIndices() {
		List<Integer> columnIndices = new ArrayList<>(
				this.columnsContaining.size());
		this.columnsContaining
				.forEach(column -> columnIndices.add(column.indexInDocument));
		Collections.sort(columnIndices);
		return columnIndices;
	}

	/**
	 * DAX - 5/20/19 Added to create local reference to all column-space
	 * membership
	 */
	public List<DocumentColumn> getColumnsContaining() {
		return this.columnsContaining;
	}

	/**
	 * Gets the end offset as int.
	 *
	 * @return the end offset as int
	 */
	@Override
	public int getEndOffsetAsInt() {
		return this.endOffset;
	}

	/**
	 * @return the lineEndPhraseOffset
	 */
	public int getLineEndPhraseOffset() {
		return this.lineEndOffsetByPhrase;
	}

	public int getLineIndexValue() {
		return this.parentLine.getLineIndex();
	}

	public int getLinePosition() {
		return this.indexInLine;
	}

	/**
	 * @return the lineStartPhraseOffset
	 */
	public int getLineStartPhraseOffset() {
		return this.lineStartOffsetByPhrase;
	}

	/**
	 * Determine the number of tokens in this phrase that overlap those in
	 * otherPhrase
	 *
	 * @param otherPhrase
	 * @return
	 */
	public int getOverlap(final RaptatTokenPhrase otherPhrase) {
		Iterator<RaptatToken> tokenIterator = this.tokensAsList.iterator();
		int overlap = 0;

		while ( tokenIterator.hasNext() ) {
			RaptatToken token = tokenIterator.next();
			if ( (token.get_start_offset_as_int() < otherPhrase.endOffset)
					&& (token
							.get_end_offset_as_int() > otherPhrase.startOffset) ) {
				++overlap;
			}
		}
		return overlap;
	}

	public Set<String> getPhraseFeatures() {
		return this.phraseFeatures;
	}

	public Set<Label> getPhraseLabels() {
		return Collections.unmodifiableSet(this.phraseLabels);
	}

	public Integer getPhraseLength() {
		return getPhraseText().length();
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getPhraseText() {
		String joinDelimeter = " ";
		if ( this.phraseLabels.contains(Label.createIndexValueLabel()) ) {
			joinDelimeter = "";
		}
		return Joiner.on(joinDelimeter).join(this.tokensAsList.stream()
				.map(x -> x.getTokenStringPreprocessed()).iterator());
	}

	/**
	 * Gets the start offset as int.
	 *
	 * @return the start offset as int
	 */
	@Override
	public int getStartOffsetAsInt() {
		return this.startOffset;
	}

	public String getTextSource() {
		return this.tokensAsList.get(0).getTextSource();
	}

	/**
	 * Gets the tokens as list.
	 *
	 * @return the tokens as list
	 */
	public List<RaptatToken> getTokensAsList() {
		return this.tokensAsList;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this.startOffset)
				.append(this.endOffset).append(get_line_index())
				.append(this.tokensAsList.size()).toHashCode();
	}

	public boolean hasPhraseLabel(final String phraseLabelName) {
		boolean rv = false;
		for (final Label label : this.phraseLabels) {
			if ( rv = label.getCategory().equalsIgnoreCase(phraseLabelName) ) {
				break;
			}
		}
		return rv;
	}

	public boolean hasPhraseLabel(final String phraseLabelName,
			final String phraseLabelValue) {
		boolean rv = false;
		for (final Label label : this.phraseLabels) {
			final boolean hasName = label.getCategory()
					.equalsIgnoreCase(phraseLabelName);
			final boolean hasValue = label.getSubcategory()
					.equalsIgnoreCase(phraseLabelValue);
			if ( rv = hasName && hasValue ) {
				break;
			}
		}
		return rv;
	}

	public boolean hasPhraseLabels() {
		return (this.phraseLabels != null) && (this.phraseLabels.size() > 0);
	}

	/**
	 * Checks if is in column index.
	 *
	 * @param column_index
	 *            the column index
	 * @return true, if is in column index
	 */
	@Override
	public boolean is_in_column_index(final int column_index) {
		return (get_start_offset_relative_to_line() <= column_index)
				&& (column_index <= (get_end_offset_relative_to_line() - 1));
	}

	public boolean isIndexValue() {
		return this.hasPhraseLabel(RaptatTokenPhrase.Label.INDEX_VALUE_STRING);
	}

	public RaptatTokenPhrase leftTrim() {
		final RaptatToken firstToken = this.tokensAsList.get(0);
		final String text = firstToken.getTokenStringPreprocessed();
		final String newText = StringHelper.leftTrim(text);
		final int delta = text.length() - newText.length();
		if ( delta != 0 ) {
			final Integer newStart = Integer
					.parseInt(firstToken.getStartOffset()) + delta;
			firstToken.setTokenStringPreprocessed(newText);
			firstToken.setStartOff(newStart.toString());
		}
		return this;
	}

	public boolean overlaps(RaptatTokenPhrase otherPhrase) {
		return (otherPhrase.endOffset > this.startOffset)
				&& (otherPhrase.startOffset < this.endOffset);
	}

	public void removeLabel(final Label labelToReview) {
		this.phraseLabels.remove(labelToReview);
	}

	/*
	 * Added by Glenn 9/11/19
	 *
	 */public void resetPhraseFeatures(final Set<String> featureSet) {
		this.phraseFeatures = featureSet;
	}

	public RaptatTokenPhrase rightTrim() {
		final RaptatToken lastToken = this.tokensAsList
				.get(this.tokensAsList.size() - 1);
		final String text = lastToken.getTokenStringPreprocessed();
		final String newText = StringHelper.rightTrim(text);
		final int delta = text.length() - newText.length();
		if ( delta != 0 ) {
			final Integer newStart = Integer.parseInt(lastToken.getEndOffset())
					- delta;
			lastToken.setTokenStringPreprocessed(newText);
			lastToken.setEndOff(newStart.toString());
		}
		return this;
	}

	/**
	 * Sets the containing text document line.
	 *
	 * @param current_line
	 *            the new containing text document line
	 */
	public void set_containing_text_document_line(
			final TextDocumentLine current_line) {
		this.parentLine = current_line;
	}

	/**
	 * Create an array holding the distance from this phrase, which may span one
	 * or more column, to any other column in the document;
	 */
	public void setDistancesToColumns() {
		// TODO Auto-generated method stub

	}

	/**
	 * Determine the distance to all other lines in the document containing a
	 * phrase that overlaps this phrase.
	 */
	public void setDistancesToLines() {
		/*
		 * Get all phrases along one of the same columns as this one
		 */
		Set<RaptatTokenPhrase> sharedColumnPhrases = new HashSet<>();
		this.columnsContaining.forEach(
				column -> sharedColumnPhrases.addAll(column.columnPhrases));

		if ( LOGGER.isDebugEnabled() ) {
			List<RaptatTokenPhrase> sharedColumnPhrasesList = new ArrayList<>(
					sharedColumnPhrases);
			sharedColumnPhrasesList.sort(
					Comparator.comparing(RaptatTokenPhrase::getLineIndexValue));
			sharedColumnPhrasesList.forEach(phrase -> System.out.println(
					phrase.getLineIndexValue() + ":" + phrase.getPhraseText()));
		}

		/*
		 * Get all the document lines of phrases sharing columns with this one
		 * and put them into a sorted list
		 */
		Set<Integer> linesWithSharedPhrasesSet = new HashSet<>();
		sharedColumnPhrases.forEach(phrase -> linesWithSharedPhrasesSet
				.add(phrase.parentLine.getLineIndex()));
		List<Integer> indexedLines = new ArrayList<>(linesWithSharedPhrasesSet);
		Collections.sort(indexedLines);

		if ( LOGGER.isDebugEnabled() ) {
			LOGGER.debug("");
			indexedLines.forEach(length -> System.out.println(length));
		}

		/*
		 * Iterate through the list of integers to determine and set distances;
		 */
		int currentLine = 0;
		int lineDistance = 0;
		for (int indexedLine : indexedLines) {
			while ( currentLine < indexedLine ) {

				/*
				 * We increment lineDistance this way, before and after
				 * assigning line distance, because, in the first iteration of
				 * this loop, lineDistance has already been incremented by 1;
				 */
				this.minimumDistancesToLines[currentLine] = lineDistance;
				++currentLine;
				lineDistance += TextDocumentLine.EMPTY_LINE_INCREMENT;
			}
			this.minimumDistancesToLines[currentLine] = lineDistance;
			++currentLine;
			++lineDistance;
		}
		int linesInPhraseDocument = this.tokensAsList.get(0).getDocumentLines()
				.size();
		while ( currentLine < linesInPhraseDocument ) {
			lineDistance += TextDocumentLine.EMPTY_LINE_INCREMENT;
			this.minimumDistancesToLines[currentLine] = lineDistance;
			++currentLine;
			++lineDistance;
		}

		int thisPhraseLineDistance = this.minimumDistancesToLines[this.parentLine
				.getLineIndex()];
		for (int i = 0; i < this.minimumDistancesToLines.length; i++) {
			this.minimumDistancesToLines[i] = Math.abs(
					this.minimumDistancesToLines[i] - thisPhraseLineDistance);
		}
	}

	public void setIndexInLine(final int linePosition) {
		this.indexInLine = linePosition;
	}

	/**
	 * Sets the start offset, end offset, and length of a phrase. Instances with
	 * an assigned label have a length of 1. Otherwise, length is dependent on
	 * number of tokens in phrase.
	 *
	 * @param startPhraseOffset
	 * @return
	 */
	public int setLinePhraseOffset(final int startPhraseOffset) {
		this.lineStartOffsetByPhrase = startPhraseOffset;
		if ( !this.phraseLabels.contains(Label.LABEL_UNASSIGNED) ) {
			this.phraseLength = 1;
		}
		else {
			this.phraseLength = (int) Math.ceil(this.tokensAsList.size()
					/ RaptatTokenPhrase.DIVISOR_FOR_UNASSIGNED_PHRASE_LENGTH);
		}
		this.lineEndOffsetByPhrase = this.lineStartOffsetByPhrase
				+ this.phraseLength;
		return this.lineEndOffsetByPhrase;
	}

	/*
	 * Modified by Glenn 9/11/19 - Deprecated - Refactored name to addFeatures
	 * to expose what the method is doing
	 */
	@Deprecated
	public void setPhraseFeatures(final Set<String> featureState) {
		this.phraseFeatures.addAll(featureState);
	}

	/**
	 * Sets the token list.
	 *
	 * @param raptatTokens
	 *            the new token list
	 */
	public void setTokenList(final List<RaptatToken> raptatTokens) {
		this.tokensAsList.addAll(raptatTokens);
		Collections.sort(this.tokensAsList);
	}

	// /**
	// * Helper method for addFeatures. Used when both phrase distance and line
	// distance between this
	// * and associated RaptatTokenPhrase instances (provided by phraseIterator)
	// must be calculated to
	// * generate features. The other addFeatures helper method only calculates
	// phrase distance.
	// *
	// * @param featureLabelMap
	// * @param phraseIterator
	// * @param phraseDistanceTag
	// * @param lineDistanceTag
	// */
	// private void addFeaturesHelper(final Map<String, Set<String>>
	// featureLabelMap,
	// final ListIterator<RaptatTokenPhrase> phraseIterator, final String
	// phraseDistanceTag,
	// final String lineDistanceTag) {
	//
	// RaptatPair<Map<String, RaptatPair<String, Integer>>, Map<String,
	// RaptatPair<String, Integer>>>
	// dualFeatures =
	// getPhraseAndLineDistanceFeaturesMaps(phraseIterator, featureLabelMap);
	//
	// Set<String> phraseDistanceFeatures =
	// getFeaturesFromMap(dualFeatures.left, phraseDistanceTag);
	// this.phraseFeatures.addAll(phraseDistanceFeatures);
	// Set<String> lineDistanceFeatures = getFeaturesFromMap(dualFeatures.right,
	// lineDistanceTag);
	// this.phraseFeatures.addAll(lineDistanceFeatures);
	// }

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this,
				ToStringStyle.MULTI_LINE_STYLE)
						.append("Start", this.startOffset)
						.append("End", this.endOffset)
						.append("PhraseText", getPhraseText())
						.append("IndexInLine", this.indexInLine)
						.append("Line", this.parentLine.getLineIndex())
						.append("Labels", this.phraseLabels)
						.append("Features", this.phraseFeatures);

		return tsb.toString();
	}

	public RaptatTokenPhrase trim() {
		return leftTrim().rightTrim();

	}

	public void trimEndingPeriod() {
		final List<RaptatToken> tokens = getTokensAsList();
		final RaptatToken raptatToken = tokens.get(tokens.size() - 1);
		final String tokenStringPreprocessed = raptatToken
				.getTokenStringPreprocessed();
		if ( tokenStringPreprocessed.endsWith(".") ) {
			final String substring = tokenStringPreprocessed.substring(0,
					tokenStringPreprocessed.length() - 1);
			raptatToken.setTokenStringPreprocessed(substring);
		}

	}

	/**
	 * Helper for adding column features in either the reverse direction (up
	 * from label of interest) or forward direction (down from RaptatTokenPhrase
	 * of interest). The direction is generated by sorting via the comparator
	 * parameter, and phraseTag and lineTag provide indications of the location
	 * of the RaptatTokenPhrase of interest relative to the features (e.g.
	 * 'phrases above' or 'lines below').
	 *
	 * @param featureLabelMap
	 */
	private void addColumnFeaturesHelper(
			final Map<String, Set<String>> featureLabelMap,
			final Comparator<RaptatTokenPhrase> comparator,
			final String phraseTag, final String lineTag) {

		/*
		 * These two variables, phraseDistanceMap and lineDistanceMap, track the
		 * label name and associated value and distance to other
		 * RaptatTokenPhrase instances in the same column as this one. It only
		 * stores the closest one found as it iterates through all the columns
		 * that this RaptatTokenPhrase instance is found in.
		 */
		Map<String, RaptatPair<String, Integer>> phraseDistanceMap = new HashMap<>();
		Map<String, RaptatPair<String, Integer>> lineDistanceMap = new HashMap<>();

		for (DocumentColumn column : this.columnsContaining) {
			Collections.sort(column.columnPhrases, comparator);
			int startIndex = column.columnPhrases.indexOf(this);
			if ( startIndex < (column.columnPhrases.size() - 1) ) {
				/*
				 * Start iterating AFTER the phrase of interest, which is
				 * **this** RaptatTokenPhrase
				 */
				ListIterator<RaptatTokenPhrase> columnIterator = column.columnPhrases
						.listIterator(startIndex + 1);
				updatePhraseAndLineDistanceMaps(columnIterator, featureLabelMap,
						phraseDistanceMap, lineDistanceMap);
			}
		}

		Set<String> phraseDistanceFeatures = getFeaturesFromMap(
				phraseDistanceMap, phraseTag);
		this.phraseFeatures.addAll(phraseDistanceFeatures);

		Set<String> lineDistanceFeatures = getFeaturesFromMap(lineDistanceMap,
				lineTag);
		this.phraseFeatures.addAll(lineDistanceFeatures);
	}

	// /**
	// * Finds features based on a list of phrases and a feature label map.
	// Features are created based
	// * on the labels of the phrases in the list. Only the labels have their
	// name as a key and their
	// * value within the set of string mapped to by that key (within the
	// featureLabelMap parameter)
	// are
	// * added as a feature.
	// *
	// * @param adjacentPhrasesList
	// * @param featureLabelMap
	// * @return
	// */
	// private Map<String, RaptatPair<String, Integer>> getRowFeatureMap(
	// final List<RaptatTokenPhrase> adjacentPhrasesList,
	// final Map<String, Set<String>> featureLabelMap, final boolean
	// forwardSearch) {
	//
	// Map<String, RaptatPair<String, Integer>> resultMap = new HashMap<>();
	// for (RaptatTokenPhrase phrase : adjacentPhrasesList) {
	// for (Label phraseLabel : phrase.phraseLabels) {
	// String labelName = phraseLabel.name.toLowerCase();
	// Set<String> acceptableValues;
	// if ((acceptableValues = featureLabelMap.get(labelName)) != null) {
	// String labelValue = phraseLabel.value.toLowerCase();
	// if (acceptableValues.contains(labelValue)) {
	// Set<String> foundValues;
	// if ((foundValues = featureLabelMap.get(labelName)) != null
	// && foundValues.contains(labelValue)) {
	// continue;
	// }
	// int phraseDistance = tokenDistance(phrase, forwardSearch);
	// resultMap.put(labelName, new RaptatPair<>(labelValue, phraseDistance));
	// }
	// }
	// }
	// }
	// return resultMap;
	// }

	/*
	 * Calculate the distance from this instance of a RaptatTokenPhrase to
	 * another in tokens. If it's a forward search, we are trying to determine
	 * how far before this phrase the other phrase occurs measured in tokens,
	 * and the distance is from the first token of this instance to the last of
	 * the otherPhrase. If not, we are trying to determine how far after the
	 * other phrase occurs after this one measured in tokens, and the distance
	 * is from the last token of this instance of a phrase to the first token of
	 * the other.
	 *
	 * The value returned is an absolute value.
	 */
	// private int tokenDistance(final RaptatTokenPhrase otherPhrase, final
	// boolean forwardSearch) {
	//
	// int expectedSmallestIndex;
	// int expectedLargestIndex;
	// if (forwardSearch) {
	//
	// expectedSmallestIndex =
	// otherPhrase.tokensAsList.get(otherPhrase.tokensAsList.size() - 1)
	// .getTokenIndexInDocument();
	// expectedLargestIndex =
	// this.tokensAsList.get(0).getTokenIndexInDocument();
	//
	// } else {
	//
	// expectedSmallestIndex =
	// this.tokensAsList.get(this.tokensAsList.size() -
	// 1).getTokenIndexInDocument();
	// expectedLargestIndex =
	// otherPhrase.tokensAsList.get(0).getTokenIndexInDocument();
	//
	// }
	//
	// int distance = expectedLargestIndex - expectedSmallestIndex;
	//
	// return distance > 0 ? distance : 0;
	// }

	private void addDocumentFeaturesHelper(
			final Map<String, Set<String>> categoryToSubcategoryMap,
			final ListIterator<RaptatTokenPhrase> associatedPhraseIterator,
			final String featureTag) {
		Map<String, RaptatPair<String, Integer>> featuresMap = getDocumentPhraseDistanceFeaturesMap(
				associatedPhraseIterator, categoryToSubcategoryMap);
		Set<String> phraseDistanceFeatures = getFeaturesFromMap(featuresMap,
				"doc" + featureTag);
		this.phraseFeatures.addAll(phraseDistanceFeatures);

	}

	/**
	 * Sort the phrases associated with this RaptatTokenPhrase instance and add
	 * features. The firstTag parameter generally stores a phrase distance
	 * description, such as "row_phrasesbefore", and the second generally stores
	 * a line distance phrase, such as "col_linesabove."
	 *
	 * @param categoryToSubcategoryMap
	 * @param associatedPhrases
	 * @param sorter
	 * @param tag
	 * @param searchScope
	 * @param secondTag
	 */
	private void addFeatures(
			final Map<String, Set<String>> categoryToSubcategoryMap,
			final List<RaptatTokenPhrase> associatedPhrases,
			final Comparator<RaptatTokenPhrase> sorter, final String tag,
			final SearchScope searchScope) {
		Collections.sort(associatedPhrases, sorter);
		int thisPhraseIndex = associatedPhrases.indexOf(this);
		if ( thisPhraseIndex < (associatedPhrases.size() - 1) ) {
			ListIterator<RaptatTokenPhrase> associatedPhraseIterator = associatedPhrases
					.listIterator(thisPhraseIndex + 1);
			if ( searchScope.equals(SearchScope.ROW) ) {
				addRowFeaturesHelper(categoryToSubcategoryMap,
						associatedPhraseIterator, tag);
			}
			if ( searchScope.equals(SearchScope.DOCUMENT) ) {
				addDocumentFeaturesHelper(categoryToSubcategoryMap,
						associatedPhraseIterator, tag);
			}
		}
	}

	/**
	 * Add features for phrases that occur before and after the
	 * RaptatTokenPhrase instance in a row *or* document. The document is
	 * treated as a single row of phrases provided by the parameter
	 * associatedPhrases.
	 *
	 * @param categoryToSubcategoryMap
	 * @param associatedPhrases
	 * @param row
	 */
	private void addHorizontalFeatures(
			final Map<String, Set<String>> categoryToSubcategoryMap,
			final List<RaptatTokenPhrase> associatedPhrases,
			final SearchScope searchScope) {
		/*
		 * First search before for the features of RaptatTokenPhrase instances
		 * that are before this one.
		 */
		String beforePhraseTag = "_phrases_before";
		addFeatures(categoryToSubcategoryMap, associatedPhrases,
				Collections.reverseOrder(RaptatTokenPhrase.phraseEndComparator),
				beforePhraseTag, searchScope);

		/*
		 *
		 * Now search for the features of RaptatTokenPhrase instances that are
		 * after this one.
		 */
		String afterPhraseTag = "_phrases_after";
		addFeatures(categoryToSubcategoryMap, associatedPhrases,
				RaptatTokenPhrase.phraseStartComparator, afterPhraseTag,
				searchScope);
	}

	private void addRowFeaturesHelper(
			final Map<String, Set<String>> categoryToSubcategoryMap,
			final ListIterator<RaptatTokenPhrase> phraseIterator,
			final String featureTag) {
		Map<String, RaptatPair<String, Integer>> featuresMap = getRowPhraseDistanceFeaturesMap(
				phraseIterator, categoryToSubcategoryMap);
		Set<String> phraseDistanceFeatures = getFeaturesFromMap(featuresMap,
				"row" + featureTag);
		this.phraseFeatures.addAll(phraseDistanceFeatures);

	}

	/**
	 * Determine distance between this phrase and the other phrase in terms of
	 * horizontal and vertical distance. Note that for document distance, the
	 * otherPhrase may be in different columns than this one. Because of this,
	 * the vertical 'phraseDistance' between this one and otherPhrase may differ
	 * in distance due to the presence or absence of phrases at a given line
	 * within a given column. To account for this, we take the average of
	 * distances from this to the otherPhrase instance and from the otherPhrase
	 * instance to this one.
	 *
	 * @param otherPhrase
	 */
	private int getDocumentPhraseDistance(final RaptatTokenPhrase otherPhrase) {

		/*
		 * First get vertical distance
		 */
		int verticalTotalDistance = otherPhrase.minimumDistancesToLines[this.parentLine
				.getLineIndex()];
		verticalTotalDistance += this.minimumDistancesToLines[otherPhrase.parentLine
				.getLineIndex()];
		int verticalAverageDistance = (int) Math
				.ceil((double) verticalTotalDistance / 2);

		int horizontalDistance = getHorizontalDistance(otherPhrase);

		return verticalAverageDistance + horizontalDistance;
	}

	/**
	 *
	 * @param phraseIterator
	 * @param categoryToSubcategoryMap
	 * @return
	 */
	private Map<String, RaptatPair<String, Integer>> getDocumentPhraseDistanceFeaturesMap(
			final ListIterator<RaptatTokenPhrase> phraseIterator,
			final Map<String, Set<String>> categoryToSubcategoryMap) {
		Map<String, RaptatPair<String, Integer>> phraseDistanceMap = new HashMap<>();
		Set<String> foundCategoriesSet = new HashSet<>();

		while ( phraseIterator.hasNext() ) {
			RaptatTokenPhrase phrase = phraseIterator.next();
			for (Label phraseLabel : phrase.phraseLabels) {
				/*
				 * Only add to the phraseDistance map if the name of the label
				 * has not already been found and if the phrase name and value
				 * are within the featureLabelMap of names and values.
				 */
				String phraseCategory = phraseLabel.category.toLowerCase();
				if ( !foundCategoriesSet.contains(phraseCategory) ) {
					Set<String> mappedSubcategories;
					String phraseSubcategory = phraseLabel.subcategory
							.toLowerCase();
					if ( ((mappedSubcategories = categoryToSubcategoryMap
							.get(phraseCategory)) != null)
							&& mappedSubcategories
									.contains(phraseSubcategory) ) {
						int phraseDistance = getDocumentPhraseDistance(phrase);
						phraseDistanceMap.put(phraseCategory, new RaptatPair<>(
								phraseSubcategory, phraseDistance));
						foundCategoriesSet.add(phraseCategory);
					}
				}
			}
		}
		return phraseDistanceMap;
	}

	private Set<String> getFeaturesFromMap(
			final Map<String, RaptatPair<String, Integer>> featureMap,
			final String prependTag) {
		Set<String> resultSet = new HashSet<>();

		for (String tokenPhraseCategory : featureMap.keySet()) {
			RaptatPair<String, Integer> subCategoryDistancePair = featureMap
					.get(tokenPhraseCategory);
			StringBuilder sb = new StringBuilder(prependTag);
			sb.append(FEATURE_DELIMITER).append(tokenPhraseCategory);
			sb.append(FEATURE_DELIMITER).append(subCategoryDistancePair.left);
			sb.append(FEATURE_DELIMITER).append("distance");
			sb.append(FEATURE_DELIMITER).append(subCategoryDistancePair.right);
			resultSet.add(sb.toString());
		}
		return resultSet;
	}

	/**
	 * Determine horizontal distance between two phrases, this one and
	 * otherPhrase, that may be on different lines within the document. We use
	 * start and end offsets on the line of the phrase, otherwise we use the
	 * offsets of the columns as they intersect the line.
	 *
	 * @param otherPhrase
	 * @return
	 */
	private int getHorizontalDistance(final RaptatTokenPhrase otherPhrase) {

		RaptatTokenPhrase linePhrase = this;
		int thisLinePhraseDifference = getOffsetDifference(linePhrase,
				otherPhrase);
		int otherLinePhraseDifference = getOffsetDifference(linePhrase,
				otherPhrase);

		return (int) Math.ceil(
				(double) (thisLinePhraseDifference + otherLinePhraseDifference)
						/ 2);
	}

	/**
	 * @param linePhrase
	 * @param otherPhrase
	 */
	private int getOffsetDifference(final RaptatTokenPhrase linePhrase,
			final RaptatTokenPhrase otherPhrase) {
		Set<Integer> linePhraseOffsets = new HashSet<>();
		linePhraseOffsets.add(linePhrase.lineStartOffsetByPhrase);
		linePhraseOffsets.add(linePhrase.lineEndOffsetByPhrase);
		Set<Integer> otherPhraseOffsets = new HashSet<>();
		int[] otherPhraseColumnOffsets = otherPhrase.parentLine
				.getColumnOffsets();
		for (DocumentColumn otherPhraseColumn : otherPhrase.columnsContaining) {
			otherPhraseOffsets.add(
					otherPhraseColumnOffsets[otherPhraseColumn.indexInDocument]);
		}

		return GeneralHelper.findMinimumDifference(linePhraseOffsets,
				otherPhraseOffsets);
	}

	/**
	 * Determine features and their distance (in phrases) for phrases in the
	 * same row as this one and provided by phraseIterator.
	 *
	 * @param phraseIterator
	 * @param categoryToSubcategoryMap
	 * @return
	 */
	private Map<String, RaptatPair<String, Integer>> getRowPhraseDistanceFeaturesMap(
			final Iterator<RaptatTokenPhrase> phraseIterator,
			final Map<String, Set<String>> categoryToSubcategoryMap) {
		Map<String, RaptatPair<String, Integer>> phraseDistanceMap = new HashMap<>();
		Set<String> foundCategoriesSet = new HashSet<>();

		while ( phraseIterator.hasNext() ) {
			RaptatTokenPhrase phrase = phraseIterator.next();

			for (Label phraseLabel : phrase.phraseLabels) {
				String phraseLabelCategory = phraseLabel.category.toLowerCase();

				/*
				 * Only add to the phraseDistance map if the category of the
				 * label has not already been found and if the phrase category
				 * and subcategory are within the categoryToSubcategoryMap.
				 */
				if ( !foundCategoriesSet.contains(phraseLabelCategory) ) {
					Set<String> acceptableSubcategories;
					String phraseLabelSubcategory = phraseLabel.subcategory
							.toLowerCase();
					if ( ((acceptableSubcategories = categoryToSubcategoryMap
							.get(phraseLabelCategory)) != null)
							&& acceptableSubcategories
									.contains(phraseLabelSubcategory) ) {
						int phraseDistance = Math
								.abs(this.lineStartOffsetByPhrase
										- phrase.lineStartOffsetByPhrase);
						phraseDistanceMap.put(phraseLabelCategory,
								new RaptatPair<>(phraseLabelSubcategory,
										phraseDistance));
						foundCategoriesSet.add(phraseLabelCategory);
					}
				}
			}
		}

		return phraseDistanceMap;
	}

	/**
	 * @param nameToValueDistanceMap
	 * @param distance
	 * @param phraseLabelName
	 * @param phraseLabelValue
	 */
	private void updateColumnBasedDistanceMapping(
			final Map<String, RaptatPair<String, Integer>> nameToValueDistanceMap,
			final int distance, final String phraseLabelName,
			final String phraseLabelValue) {
		/*
		 * If foundPhraseNames does not contain the phraseLabelName, it has
		 * never been added, and we can go ahead add it, its associated value,
		 * and the distance.
		 */
		if ( !nameToValueDistanceMap.containsKey(phraseLabelName) ) {
			nameToValueDistanceMap.put(phraseLabelName,
					new RaptatPair<>(phraseLabelValue, distance));
			/*
			 * However, if foundPhraseDistanceNames DOES CONTAIN the
			 * phraseLabelName, we need to check if the new one is closer,
			 * otherwise, we leave things as they are.
			 */
		}
		else {
			RaptatPair<String, Integer> valuePhraseDistancePair = nameToValueDistanceMap
					.get(phraseLabelName);
			if ( distance < valuePhraseDistancePair.right ) {
				nameToValueDistanceMap.put(phraseLabelName,
						new RaptatPair<>(phraseLabelValue, distance));
			}
		}
	}

	/**
	 * Method used to update phrase and line distance maps if a closer phrase is
	 * found with respect to a label name.
	 *
	 * @param phraseIterator
	 * @param featureLabelMap
	 * @param phraseDistanceMap
	 * @param lineDistanceMap
	 */
	private void updatePhraseAndLineDistanceMaps(
			final Iterator<RaptatTokenPhrase> phraseIterator,
			final Map<String, Set<String>> featureLabelMap,
			final Map<String, RaptatPair<String, Integer>> phraseDistanceMap,
			final Map<String, RaptatPair<String, Integer>> lineDistanceMap) {

		while ( phraseIterator.hasNext() ) {
			RaptatTokenPhrase phrase = phraseIterator.next();

			/*
			 * Examine labels associated with the phrase
			 */
			for (Label phraseLabel : phrase.phraseLabels) {
				String phraseLabelName = phraseLabel.category.toLowerCase();
				Set<String> acceptableValues;
				/*
				 * Only add features for phrase label names and values that
				 * exist in the featureLabelMap and only add them the first time
				 * we encounter them (which should be closest matching element)
				 */
				if ( (acceptableValues = featureLabelMap
						.get(phraseLabelName)) != null ) {
					String phraseLabelValue = phraseLabel.subcategory
							.toLowerCase();
					if ( acceptableValues.contains(phraseLabelValue) ) {

						int phraseDistance = this.minimumDistancesToLines[phrase.parentLine
								.getLineIndex()];
						updateColumnBasedDistanceMapping(phraseDistanceMap,
								phraseDistance, phraseLabelName,
								phraseLabelValue);
						/*
						 * Now do the same as the if/else block above but for
						 * line distance
						 */
						int lineDistance = Math.abs(
								phrase.get_line_index() - get_line_index());
						updateColumnBasedDistanceMapping(lineDistanceMap,
								lineDistance, phraseLabelName,
								phraseLabelValue);
					}
				}
			}
		}
	}

	/**
	 * Gets the all tokens.
	 *
	 * @param token_phrases
	 *            the token phrases
	 * @return the all tokens
	 */
	/**
	 * @param token_phrases
	 * @return
	 */
	public static List<RaptatToken> get_all_tokens(
			final List<RaptatTokenPhrase> token_phrases) {
		final List<RaptatToken> ret_val = new ArrayList<>();
		for (final RaptatTokenPhrase raptat_token_phrase : token_phrases) {
			for (final RaptatToken raptat_token : raptat_token_phrase.tokensAsList) {
				ret_val.add(raptat_token);
			}
		}
		return ret_val;
	}

	public static List<RaptatTokenPhrase> getCandidateTokenPhrases(
			RaptatDocument raptatDocument, Label targetConceptLabel) {
		List<RaptatTokenPhrase> candidatePhrases = new ArrayList<>();
		for (RaptatTokenPhrase tokenPhrase : raptatDocument.getTokenPhrases()) {
			if ( tokenPhrase.getPhraseLabels().contains(targetConceptLabel) ) {
				candidatePhrases.add(tokenPhrase);
			}
		}
		return candidatePhrases;
	}

}
