/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.IRaptatDocumentTokenPhrases;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.ExclusionFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.PhraseBasedSentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.sql.RaptatDocumentFields;

/**
 * ****************************************************** Class to make it easy
 * to pass all the relevant info about a text document after analysis by a
 * TextAnalyzer instance.
 *
 * @author Glenn Gobbel - Jun 28, 2012
 *         *****************************************************
 */
public class RaptatDocument implements Comparable<RaptatDocument>, Serializable,
		IRaptatDocumentTokenPhrases {

	public class MetaData {

		private String name;
		private Object value;

		public MetaData(final String name, final Boolean value) {
			this.name = name;
			this.value = value;
		}

		public Boolean AsBoolean(final Boolean defaultValue) {
			if ( this.value == null ) {
				return defaultValue;
			}
			return (Boolean) this.value;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String toString() {
			return MessageFormat.format("MetaData: ({0},{1})", getName(),
					this.value);
		}

	}

	/* Stores the sentences that are currently being used in NLP processing */
	private List<AnnotatedPhrase> activeSentences;
	private IndexedTree<IndexedObject<RaptatToken>> processedTokens, rawTokens;

	private Optional<String> textSourcePath;
	private Optional<String> textSource;
	private UUID uniqueIdentifier = UUID.randomUUID();
	private List<AnnotatedPhrase> dictionaryAnnotations = new ArrayList<>();
	private LinkedList<TextDocumentLine> textLines;
	/*
	 * The filteredSentences field is intended to store subsets of the
	 * baseSentences field, such as those produced by filtering the set. If it
	 * is null, then the set has not been filtered.
	 */
	private List<AnnotatedPhrase> filteredSentences;
	/*
	 * Stores literally all the sentences in the document as a list of
	 * AnnotatedPhrase instances
	 */
	private List<AnnotatedPhrase> baseSentences;

	private List<RaptatTokenPhrase> tokenPhrases;
	/**
	 * DAX - 5/7/19
	 *
	 * <p>
	 * Added to permit use at the document level of text document lines and list
	 * of columns of phrases.
	 */
	private List<DocumentColumn> documentColumns;

	private List<TextDocumentLine> textDocumentLines;

	private Map<String, MetaData> metaDataMap;

	private static final long serialVersionUID = 5445217626937545623L;

	private static Logger logger = Logger.getLogger(AnnotatedPhrase.class);

	public RaptatDocument() {
		RaptatDocument.logger.setLevel(Level.INFO);
	}

	/**
	 * ********************************************
	 *
	 * @param theSentences
	 * @param allRawTokens
	 * @param allProcessedTokens
	 * @param pathToSource
	 *
	 * @author Glenn Gobbel - Jun 28, 2012
	 *********************************************
	 */
	public RaptatDocument(final List<AnnotatedPhrase> theSentences,
			final IndexedTree<IndexedObject<RaptatToken>> allRawTokens,
			final IndexedTree<IndexedObject<RaptatToken>> allProcessedTokens,
			final Optional<String> pathToSource) {
		this();
		this.baseSentences = theSentences;
		this.activeSentences = theSentences;
		this.rawTokens = allRawTokens;
		this.processedTokens = allProcessedTokens;
		this.textSourcePath = pathToSource;
		if ( this.textSourcePath.isEmpty() ) {
			this.textSource = Optional.empty();
		}
		else {
			this.textSource = Optional
					.ofNullable(new File(this.textSourcePath.get()).getName());
		}
	}

	public RaptatDocument(final RaptatDocumentFields rdf) {
		this.textSource = Optional.ofNullable(rdf.textSource);
	}

	public void activateBaseSentences() {
		this.activeSentences = this.baseSentences;
	}

	public void activateFilteredSentences() throws Exception {
		if ( this.filteredSentences == null ) {
			throw new Exception();
		}
		this.activeSentences = this.filteredSentences;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final RaptatDocument otherDocument) {
		CompareToBuilder builtComparison = new CompareToBuilder();
		builtComparison.append(this.textSource, otherDocument.textSource);
		builtComparison.append(this.textSourcePath,
				otherDocument.textSourcePath);
		return builtComparison.toComparison();
	}

	/**
	 * Takes multiple SentenceFilter instances and creates a set of filtered
	 * sentence. All filters are considered inclusion filters unless they are a
	 * subclass of ExclusionFilter. The method includes only those sentence in
	 * the inclusion filters that are not excluded by one of the excluded
	 * filters.
	 *
	 * @param sentenceFilters
	 * @param annotations
	 */
	public void createFilteredSentences(
			final Collection<SentenceFilter> sentenceFilters,
			final List<AnnotatedPhrase> annotations) {
		if ( (sentenceFilters == null) || sentenceFilters.isEmpty() ) {
			this.filteredSentences = this.baseSentences;
			return;
		}

		List<SentenceFilter> inclusionFilters = new ArrayList<>();
		List<SentenceFilter> exclusionFilters = new ArrayList<>();

		for (SentenceFilter sentenceFilter : sentenceFilters) {
			if ( sentenceFilter instanceof ExclusionFilter ) {
				exclusionFilters.add(sentenceFilter);
			}
			else {
				inclusionFilters.add(sentenceFilter);
			}
		}

		Set<AnnotatedPhrase> inclusionSet = new HashSet<>();

		/*
		 * Add together all those sentence included by any of the
		 * InclusionFilter types
		 */
		for (SentenceFilter inclusionFilter : inclusionFilters) {
			inclusionSet.addAll(inclusionFilter.selectSentences(annotations));
		}

		/*
		 * Exclude any phrases from the inclusionSet that are not also in the
		 * set remaining through exclusion
		 */
		for (SentenceFilter exclusionFilter : exclusionFilters) {
			((PhraseBasedSentenceFilter) exclusionFilter).getPhraseFilter()
					.setCandidateSentences(inclusionSet);
			inclusionSet = exclusionFilter.selectSentences(annotations);
		}
		this.filteredSentences = new ArrayList<>(inclusionSet);
	}

	public void createFilteredSentences(final SentenceFilter sentenceFilter,
			final List<AnnotatedPhrase> annotations) {
		Set<AnnotatedPhrase> filteredSet = sentenceFilter
				.selectSentences(annotations);
		this.filteredSentences = new ArrayList<>(filteredSet);
	}

	/** @return the activeSentences */
	public List<AnnotatedPhrase> getActiveSentences() {
		return this.activeSentences;
	}

	/** @return the baseSentences */
	public List<AnnotatedPhrase> getBaseSentences() {
		return this.baseSentences;
	}

	public AnnotatedPhrase getCloneMarkedPhrase(final String startOffset,
			final String endOffset, final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s.clone();
				break;
			}
		}

		if ( result != null ) {
			int startOffsetToken;
			ArrayList<RaptatToken> tokens = new ArrayList<>();

			for (RaptatToken t : result.getProcessedTokens()) {
				tokens.add(t.clone());
			}

			result.setProcessedTokens(tokens);

			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					if ( !context.equals("") ) {
						t.setTokenStringPreprocessed(
								t.getTokenStringPreprocessed() + "|" + context);
					}
				}
			}
		}

		return result;
	}

	public AnnotatedPhrase getCueMarkedPhrase(final String startOffset,
			final String endOffset) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		int startOffsetToken;
		boolean cueFound = false;

		if ( result != null ) {
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					if ( !cueFound ) {
						t.setTokenStringPreprocessed("####");
						cueFound = true;
					}
					else {
						t.setTokenStringPreprocessed("");
					}
				}
			}
		}

		return result;
	}

	public AnnotatedPhrase getCueMarkedPhrase(final String startOffset,
			final String endOffset, final AnnotatedPhrase source) {
		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetToken;

		if ( source != null ) {
			for (RaptatToken t : source.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					t.setTokenStringPreprocessed("####");
				}
			}
		}

		return source;
	}

	public AnnotatedPhrase getCueMarkedPhraseTest(final String startOffset,
			final String endOffset, final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		int startOffsetToken;
		boolean cueFound = false;

		if ( !context.equals("") ) {
			if ( result != null ) {
				for (RaptatToken t : result.getProcessedTokens()) {
					startOffsetToken = Integer.parseInt(t.getStartOffset());

					if ( (startOffsetToken >= startOffsetInt)
							&& (startOffsetToken <= endOffsetInt) ) {
						if ( !cueFound ) {
							t.setTokenStringPreprocessed("####");
							cueFound = true;
						}
						else {
							t.setTokenStringPreprocessed("");
						}
					}
				}
			}
		}

		return result;
	}

	public String getCueMarkedSentence(final String startOffset,
			final String endOffset, final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		int startOffsetToken;
		boolean cueFound = false;
		StringBuilder sb = new StringBuilder();

		if ( result != null ) {
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					if ( !context.equals("") ) {
						if ( !cueFound ) {
							sb.append("####").append("|").append(context)
									.append(" ");
							cueFound = true;
						}
					}
				}
				else {
					sb.append(t.getTokenStringPreprocessed()).append(" ");
				}
			}
		}

		return sb.toString();
	}

	public AnnotatedPhrase getCuePhraseUnderScored(final String startOffset,
			final String endOffset, final AnnotatedPhrase source) {
		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetToken;

		if ( source != null ) {
			for (RaptatToken t : source.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					t.setTokenStringPreprocessed(
							"_" + t.getTokenStringPreprocessed());
				}
			}
		}

		return source;
	}

	public AnnotatedPhrase getCuePhraseUnderScored(final String startOffset,
			final String endOffset, final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		int startOffsetToken;

		if ( !context.equals("") ) {
			if ( result != null ) {
				for (RaptatToken t : result.getProcessedTokens()) {
					startOffsetToken = Integer.parseInt(t.getStartOffset());

					if ( (startOffsetToken >= startOffsetInt)
							&& (startOffsetToken <= endOffsetInt) ) {
						t.setTokenStringPreprocessed(
								"_" + t.getTokenStringPreprocessed());
					}
				}
			}
		}

		return result;
	}

	public AnnotatedPhrase getCuePhraseUnderScoredWithBILabel(
			final String startOffset, final String endOffset,
			final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		int startOffsetToken;

		if ( !context.equals("") ) {
			if ( result != null ) {
				int count = 1;
				for (RaptatToken t : result.getProcessedTokens()) {
					startOffsetToken = Integer.parseInt(t.getStartOffset());

					if ( (startOffsetToken >= startOffsetInt)
							&& (startOffsetToken <= endOffsetInt) ) {
						if ( count == 1 ) {
							t.setTokenStringPreprocessed(
									"_" + t.getTokenStringPreprocessed() + "|B"
											+ context);
							count++;
						}
						else {
							t.setTokenStringPreprocessed(
									"_" + t.getTokenStringPreprocessed() + "|I"
											+ context);
						}
						// t.setTokenString("_" + t.getTokenString());
					}
				}
			}
		}

		return result;
	}

	public List<AnnotatedPhrase> getDictionaryAnnotations(
			final boolean clearDictionaryAttributes) {
		if ( clearDictionaryAttributes ) {
			for (AnnotatedPhrase annotatedPhrase : this.dictionaryAnnotations) {
				annotatedPhrase.setPhraseAttributes(null);
			}
		}
		return this.dictionaryAnnotations;
	}

	public List<DocumentColumn> getDocumentColumns() {
		return Collections.unmodifiableList(this.documentColumns);
	}

	public List<AnnotatedPhrase> getFilteredSentences() {
		return this.filteredSentences;
	}

	/**
	 * This method was added by Sanjib to mark the
	 *
	 * @param startOffset
	 * @param endOffset
	 * @param context
	 * @return
	 */
	public AnnotatedPhrase getMarkedPhrase(final String startOffset,
			final String endOffset, final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		if ( result != null ) {
			int startOffsetToken;
			int count = 1;
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken < endOffsetInt) ) {
					if ( !context.equals("") ) {
						if ( count == 1 ) {
							t.setTokenStringPreprocessed(
									t.getTokenStringPreprocessed() + "|B"
											+ context);
							count++;
						}
						else {
							t.setTokenStringPreprocessed(
									t.getTokenStringPreprocessed() + "|I"
											+ context);
						}
					}
					else {
						System.out.println("context empty 1");
					}
				}
			}
		}

		return result;
	}

	public AnnotatedPhrase getMarkedPhrase(final String startOffset,
			final String endOffset, final String context,
			final AnnotatedPhrase source) {
		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetToken;
		int endOffsetToken;

		if ( source != null ) {
			int count = 1;

			for (RaptatToken t : source.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());
				endOffsetToken = Integer.parseInt(t.getEndOffset());

				if ( !context.equals("") ) {
					if ( (startOffsetToken >= startOffsetInt)
							&& (endOffsetToken <= endOffsetInt) ) {
						if ( count == 1 ) {
							t.setTokenStringPreprocessed(
									t.getTokenStringPreprocessed() + "|B"
											+ context);
							count++;
						}
						else {
							t.setTokenStringPreprocessed(
									t.getTokenStringPreprocessed() + "|I"
											+ context);
						}
					}
				}
				else {
					System.out.println("context empty 2");
				}
			}
		}

		return source;
	}

	public String getMarkedSentence(final String startOffset,
			final String endOffset, final String context,
			final AnnotatedPhrase source) {
		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetToken;
		StringBuilder sb = new StringBuilder();

		if ( source != null ) {
			for (RaptatToken t : source.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( !context.equals("") ) {
					if ( (startOffsetToken >= startOffsetInt)
							&& (startOffsetToken <= endOffsetInt) ) {
						t.setTokenStringPreprocessed(
								t.getTokenStringPreprocessed() + "|" + context);
					}
				}

				sb.append(t.getTokenStringPreprocessed()).append(" ");
			}
		}

		return sb.toString();
	}

	public MetaData getMetaData(final String metaDataKey) {
		if ( (this.metaDataMap == null)
				|| (this.metaDataMap.containsKey(metaDataKey) == false) ) {
			return new MetaData(metaDataKey, null);
		}

		return this.metaDataMap.get(metaDataKey);
	}

	public AnnotatedPhrase getPhraseSentence(final String startOffset,
			final String endOffset) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);
		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		return result;
	}

	public String getPhraseSentence(final String startOffset,
			final String endOffset, final String concept) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);
		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		int startOffsetToken;

		if ( result != null ) {
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				// if(concept != null){
				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					// t.setTokenString(t.getTokenString() + "|" + concept);
					sb.append(t.getTokenStringPreprocessed()).append("|")
							.append(concept).append(" ");
				} // }
				else {
					sb.append(t.getTokenStringPreprocessed()).append(" ");
				}
			}
		}

		return sb.toString();
	}

	public AnnotatedPhrase getPhraseSentenceWithConceptMarking(
			final AnnotatedPhrase conceptPhrase) {
		AnnotatedPhrase result = null;

		int startOffsetSentence, endOffsetSentence;

		int startOffsetConceptPH = Integer
				.parseInt(conceptPhrase.getRawTokensStartOff());
		int endOffsetConceptPH = Integer
				.parseInt(conceptPhrase.getRawTokensEndOff());

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetConceptPH <= endOffsetSentence)
					&& (startOffsetConceptPH >= startOffsetSentence) ) {
				result = s.clone();
				break;
			}
		}

		if ( result != null ) {
			int startOffsetToken;

			List<RaptatToken> newTokens = new ArrayList<>();

			for (RaptatToken t : result.getProcessedTokens()) {
				newTokens.add(t.clone());
			}
			result.setProcessedTokens(newTokens);

			boolean conceptFound = false;

			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());
				if ( (startOffsetToken >= startOffsetConceptPH)
						&& (startOffsetToken <= endOffsetConceptPH) ) {
					if ( !conceptFound ) {
						t.setTokenStringPreprocessed("^^^^");
						conceptFound = true;
					}
					else {
						t.setTokenStringPreprocessed("");
					}
				}
			}
		}

		return result;
	}

	public AnnotatedPhrase getPhraseSentenceWithConceptMarking(
			final AnnotatedPhrase conceptPhrase,
			final AnnotatedPhrase sentence) {
		AnnotatedPhrase result = sentence;

		int startOffsetConceptPH = Integer
				.parseInt(conceptPhrase.getRawTokensStartOff());
		int endOffsetConceptPH = Integer
				.parseInt(conceptPhrase.getRawTokensEndOff());
		int startOffsetToken;

		boolean conceptFound = false;

		// if (result != null) {
		for (RaptatToken t : result.getProcessedTokens()) {
			startOffsetToken = Integer.parseInt(t.getStartOffset());

			if ( (startOffsetToken >= startOffsetConceptPH)
					&& (startOffsetToken <= endOffsetConceptPH) ) {
				if ( !conceptFound ) {
					t.setTokenStringPreprocessed("^^^^");
					conceptFound = true;
				}
				else {
					t.setTokenStringPreprocessed("");
				}
			}
		}
		// }

		return result;
	}

	public String getPhraseSentenceWithConceptMarking(final String startOffset,
			final String endOffset, final String context,
			final AnnotatedPhrase conceptPhrase) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);
		int startOffsetSentence, endOffsetSentence;

		int startOffsetConceptPH = Integer
				.parseInt(conceptPhrase.getRawTokensStartOff());
		int endOffsetConceptPH = Integer
				.parseInt(conceptPhrase.getRawTokensEndOff());

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		int startOffsetToken;
		boolean conceptFound = false;

		if ( result != null ) {
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());
				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					if ( !context.equals("") ) {
						sb.append(t.getTokenStringPreprocessed()).append("|")
								.append(context).append(" ");
					}
				}
				else if ( (startOffsetToken >= startOffsetConceptPH)
						&& (startOffsetToken <= endOffsetConceptPH) ) {
					if ( !conceptFound ) {
						sb.append("^^^^ ");
						conceptFound = true;
					}
				}
				else {
					sb.append(t.getTokenStringPreprocessed()).append(" ");
				}
			}
		}

		return sb.toString();
	}

	public String getPhraseSentenceWithScopeMarker(final String startOffset,
			final String endOffset, final String context) {
		AnnotatedPhrase result = null;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);
		int startOffsetSentence, endOffsetSentence;

		for (AnnotatedPhrase s : this.activeSentences) {
			startOffsetSentence = Integer.parseInt(s.getRawTokensStartOff());
			endOffsetSentence = Integer.parseInt(s.getRawTokensEndOff());

			if ( (endOffsetInt <= endOffsetSentence)
					&& (startOffsetInt >= startOffsetSentence) ) {
				result = s;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		int startOffsetToken;
		boolean scopeMarked = false;

		if ( result != null ) {
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (startOffsetToken <= endOffsetInt) ) {
					if ( !scopeMarked ) {
						sb.append("^^^^").append("|").append(context)
								.append(" ");
						scopeMarked = true;
					}
				}
				else {
					sb.append(t.getTokenStringPreprocessed()).append(" ");
				}
			}
		}

		return sb.toString();
	}

	public AnnotatedPhrase getPhraseWithScopeMarker(final String startOffset,
			final String endOffset, final String context,
			final AnnotatedPhrase result) {
		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetToken;
		int endOffsetToken;
		boolean scopeMarked = false;

		if ( result != null ) {
			for (RaptatToken t : result.getProcessedTokens()) {
				startOffsetToken = Integer.parseInt(t.getStartOffset());
				endOffsetToken = Integer.parseInt(t.getEndOffset());

				if ( (startOffsetToken >= startOffsetInt)
						&& (endOffsetToken <= endOffsetInt) ) {
					if ( !scopeMarked ) {
						t.setTokenStringPreprocessed("^^^^|" + context);
						scopeMarked = true;
					}
				}
			}
		}

		return result;
	}

	/** @return the processed tokens */
	public IndexedTree<IndexedObject<RaptatToken>> getProcessedTokens() {
		return this.processedTokens;
	}

	public String getRawTextInRange(final int startOffset,
			final int endOffset) {
		if ( !(endOffset > startOffset) ) {
			return "";
		}
		StringBuilder resultBuffer = new StringBuilder(endOffset - startOffset);
		int curOffset = startOffset;
		IndexedObject<RaptatToken> curTokenObject;
		RaptatToken curToken;

		while ( curOffset < endOffset ) {
			if ( ((curTokenObject = this.rawTokens
					.getClosestLesserOrEqualObject(curOffset)) == null)
					&& ((curTokenObject = this.rawTokens
							.getClosestGreaterObject(curOffset)) == null) ) {
				return resultBuffer.toString();
			}

			curToken = curTokenObject.getIndexedObject();
			resultBuffer.append(curToken
					.getTokenPreprocessedSubstring(startOffset, endOffset));
			curOffset = Integer.parseInt(curToken.getEndOffset());
		}

		return resultBuffer.toString();
	}

	/** @return the rawTokens */
	public IndexedTree<IndexedObject<RaptatToken>> getRawTokens() {
		return this.rawTokens;
	}

	/**
	 * Find the processed tokens between start and end offsets within the
	 * document
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public List<RaptatToken> getSpannedTokens(final int start, final int end) {
		GeneralHelper.errorWriter(
				"getSpannedTokens() method in RaptatDocument class not implemented");
		return null;
	}

	public List<TextDocumentLine> getTextDocumentLines() {
		return Collections.unmodifiableList(this.textDocumentLines);
	}

	/**
	 * @return the textLines
	 */
	public LinkedList<TextDocumentLine> getTextLines() {
		return this.textLines;
	}

	public Optional<String> getTextSource() {
		return this.textSource;
	}

	/** @return the textSourcePath */
	public Optional<String> getTextSourcePath() {
		return this.textSourcePath;
	}

	@Override
	public List<RaptatTokenPhrase> getTokenPhrases() {
		return this.tokenPhrases;
	}

	public UUID getUniqueIdentifier() {
		return this.uniqueIdentifier;
	}

	public boolean isPhraseOverlapped(final String startOffset,
			final String endOffset, final AnnotatedPhrase source) {
		boolean marked = false;

		int startOffsetInt = Integer.parseInt(startOffset);
		int endOffsetInt = Integer.parseInt(endOffset);

		int startOffsetSentence, endOffsetSentence;

		startOffsetSentence = Integer.parseInt(source.getRawTokensStartOff());
		endOffsetSentence = Integer.parseInt(source.getRawTokensEndOff());

		if ( (endOffsetInt <= endOffsetSentence)
				&& (startOffsetInt >= startOffsetSentence) ) {
			marked = true;
		}

		return marked;
	}

	public String print() {
		ToStringBuilder tsb = new ToStringBuilder(this,
				ToStringStyle.MULTI_LINE_STYLE).append("TextSource:",
						this.textSourcePath);
		int i = 0;
		for (AnnotatedPhrase curPhrase : this.activeSentences) {
			tsb.append(SystemUtils.LINE_SEPARATOR + "    Sentence " + i++ + ":",
					curPhrase);
		}
		return tsb.toString();
	}

	public void putMetaData(final MetaData metaData) {
		if ( this.metaDataMap == null ) {
			this.metaDataMap = new HashMap<>();
		}
		this.metaDataMap.put(metaData.getName(), metaData);
	}

	/**
	 * Used to regenerate the processed token list in a document. It changes the
	 * tokenStringAugmented for all tokens, unless the option removeStopWords is
	 * set, in which case stop words will not have the augmented string
	 *
	 * @author Glenn Gobbel - Jul 12, 2012
	 * @param removeStopWords
	 * @param useStems
	 * @param usePOS
	 * @param tagReasonNoMeds
	 * @param stopWordSet
	 * @return ***********************************************************
	 */
	public List<RaptatToken> regenerateProcessedTokens(
			final boolean removeStopWords, final boolean useStems,
			final boolean usePOS, final boolean tagReasonNoMeds,
			final Collection<String> stopWordSet) {
		List<RaptatToken> allDocumentTokens = new ArrayList<>(
				RaptatConstants.TOKENS_PER_DOCUMENT);
		for (AnnotatedPhrase curSentence : this.baseSentences) {
			List<RaptatToken> rawTokenList = curSentence.getRawTokens();
			List<RaptatToken> newProcessedTokenList = new ArrayList<>(
					rawTokenList.size());

			for (RaptatToken curRawToken : rawTokenList) {
				String augmentedString;
				String tokenString = curRawToken.getTokenStringPreprocessed();

				/*
				 * Note that unlike in the method
				 * TextAnalyzer.getSentenceTokens()which processes all tokens
				 * and afterwards adds the ones that are not in the stop word
				 * list, here we just need to change the processed tokens, so we
				 * don't need to go through any that are stop words as they will
				 * already be included in raw tokens and don't need to be added
				 * to processed tokens.
				 */
				if ( !removeStopWords || !stopWordSet.contains(tokenString) ) {
					if ( useStems ) {
						augmentedString = curRawToken.getStem();
					}
					else {
						augmentedString = tokenString;
					}

					if ( usePOS ) {
						augmentedString += "_" + curRawToken.getPOS();
					}

					if ( tagReasonNoMeds ) {
						augmentedString += "_"
								+ curRawToken.getReasonNoMedsContext();
					}
					curRawToken.setTokenStringAugmented(augmentedString);
					newProcessedTokenList.add(curRawToken);
				}
			}
			curSentence.setProcessedTokens(newProcessedTokenList);
			allDocumentTokens.addAll(newProcessedTokenList);
		}
		return allDocumentTokens;
	}

	/*
	 * It is possible that this method is never called. It is used primarily as
	 * a reminder for how we tell if activeSentences have been filtered or not
	 */
	public boolean sentencesFiltered() {
		return this.filteredSentences != null;
	}

	/**
	 * @param baseSentences
	 *            the baseSentences to set
	 */
	public void setBaseSentences(final List<AnnotatedPhrase> baseSentences) {
		this.baseSentences = baseSentences;
	}

	public void setDictionaryAnnotations(
			final List<AnnotatedPhrase> dictionaryAnnotations) {
		if ( dictionaryAnnotations != null ) {
			this.dictionaryAnnotations = dictionaryAnnotations;
		}
		else {
			this.dictionaryAnnotations = new ArrayList<>();
		}
	}

	public void setDocumentLines(
			final List<TextDocumentLine> textDocumentLines) {
		this.textDocumentLines = textDocumentLines;
	}

	public void setFilteredSentences(
			final List<AnnotatedPhrase> filteredSentences) {
		this.filteredSentences = filteredSentences;
	}

	public void setListOfColumnsOfPhrases(
			final List<DocumentColumn> documentColumns) {
		this.documentColumns = documentColumns;
	}

	public void setProcessedTokens(
			final IndexedTree<IndexedObject<RaptatToken>> inputTokens) {
		this.processedTokens = inputTokens;
	}

	/**
	 * @param rawTokens
	 *            the rawTokens to set
	 */
	public void setRawTokens(
			final IndexedTree<IndexedObject<RaptatToken>> rawTokens) {
		this.rawTokens = rawTokens;
	}

	/**
	 * @param textSourcePath
	 *            the textSourcePath to set
	 */
	public void setTextSourcePath(final Optional<String> textSourcePath) {
		this.textSourcePath = textSourcePath;
		if ( this.textSourcePath.isEmpty() ) {
			this.textSource = Optional.empty();
		}
		else {
			final File textFile = new File(this.textSourcePath.get());
			if ( textFile.exists() ) {
				this.textSource = Optional.of(textFile.getName());
			}
			else {
				this.textSource = this.textSourcePath;
			}
		}
	}

	/**
	 * @param tokenPhrases
	 */
	public void setTokenPhrases(final List<RaptatTokenPhrase> tokenPhrases) {
		this.tokenPhrases = tokenPhrases;
		boolean index_value_found = false;
		boolean index_type_found = false;

		outer: for (RaptatTokenPhrase raptatTokenPhrase : this.tokenPhrases) {
			Set<Label> phraseLabels = raptatTokenPhrase.getPhraseLabels();
			// IndexValue
			final String INDEX_VALUE_LABEL = "IndexValue";
			final String INDEX_TYPE_LABEL = "IndexType";

			Iterator<Label> iterator = phraseLabels.iterator();
			while ( iterator.hasNext() ) {
				Label next = iterator.next();
				String name = next.getCategory();
				index_value_found |= name.equalsIgnoreCase(INDEX_VALUE_LABEL);
				index_type_found |= name.equalsIgnoreCase(INDEX_TYPE_LABEL);
				if ( index_value_found && index_type_found ) {
					break outer;
				}
			}

		}

		if ( (index_value_found && index_type_found) == false ) {
			putMetaData(new MetaData("NO_INDEX_VALUES_FOUND", true));
		}
	}

	/**
	 * @param uniqueIdentifier
	 *            the uniqueIdentifier to set
	 */
	public void setUniqueIdentifier(final UUID uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	/**
	 * @param activeSentences
	 *            the activeSentences to set
	 */
	private void setActiveSentences(final List<AnnotatedPhrase> sentences) {
		this.activeSentences = sentences;
	}
}
