package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.FSMDefinition;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.FSMElementException;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.FSMFeatureData;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.FSMFeatureLabeler;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.IRaptatTokenText;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchDetails;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchPatternValidate;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.MatchPattern.MatchType;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code.RaptatTokenFSM;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities.TokenPhraseMakerGG;

/**
 * TokenPhraseMaker
 *
 * Class to take sentences containing their tokens and identifies and returns
 * the token phrases (RaptatTokenPhrase objects) within those sentences. It does
 * this by being initialized with finite state machines (FSM) that will match
 * regular expressions that are patterned after strings made up by one or more
 * sequences of tokens.
 *
 * @author VHATVHGOBBEG
 * @date May 5, 2019
 *
 */
public class TokenPhraseMaker implements Serializable {

	public interface ITokenPhraseReplacementValidation {
		boolean validate(RaptatTokenPhrase similar,
				RaptatTokenPhrase foundTokenPhrase);
	}

	/**
	 *
	 * @author VHATVHWesteD
	 *
	 */
	private class RaptatTokenPhrases {
		List<RaptatTokenPhrase> tokenPhraseMap;

		public RaptatTokenPhrases() {
			this.tokenPhraseMap = new ArrayList<>();
		}

		public void add(final RaptatTokenPhrase phraseToAdd) {
			this.tokenPhraseMap.add(phraseToAdd);
		}

		public List<RaptatTokenPhrase> getPhrases() {
			return this.tokenPhraseMap;
		}

		/**
		 * TODO -- review this; it makes for cleaner lists, but it may be
		 * preferable to have the unassigned entries.
		 */
		public void removedUnassigned() {
			final List<RaptatTokenPhrase> phrases = getPhrases();
			final List<RaptatTokenPhrase> cleanedPhrases = new ArrayList<>();

			for (int index = 0; index < phrases.size(); index++) {
				final RaptatTokenPhrase raptatTokenPhrase = phrases.get(index);
				if ( (raptatTokenPhrase.hasPhraseLabel("unassigned") == false)
						&& raptatTokenPhrase.hasPhraseLabels() ) {
					cleanedPhrases.add(raptatTokenPhrase);
				}
			}
			this.tokenPhraseMap = cleanedPhrases;
		}

		@Override
		public String toString() {
			return Joiner.on("\n").join(getPhrases());
		}

		/**
		 * Iterate through phrases that overlap with the same labels, removing
		 * all the smaller ones.
		 */
		public void trimLabelsFromOverlappedPhrases() {

			final Comparator<RaptatTokenPhrase> overlapIsEqual = new Comparator<RaptatTokenPhrase>() {

				/**
				 * Comparison to find out of two phrases overlap and thus are
				 * equal, or if not, push the other phrases to end.
				 */
				@Override
				public int compare(RaptatTokenPhrase largerPhrase,
						RaptatTokenPhrase smallerPhrase) {
					if ( smallerPhrase.getPhraseText().length() > largerPhrase
							.getPhraseText().length() ) {
						final RaptatTokenPhrase tmp = largerPhrase;
						largerPhrase = smallerPhrase;
						smallerPhrase = tmp;
					}
					final int smallerPhrase_start = smallerPhrase
							.getStartOffsetAsInt();
					final int smallerPhrase_end = smallerPhrase
							.getEndOffsetAsInt();
					final int largerPhrase_start = largerPhrase
							.getStartOffsetAsInt();
					/**
					 * substract length of smaller from larger in order to find
					 * smaller start between larger endpoints
					 */
					final int largerPhrase_end = largerPhrase
							.getEndOffsetAsInt()
							- (smallerPhrase_end - smallerPhrase_start);

					/**
					 * smaller start is below the larger end
					 */
					if ( largerPhrase_end >= smallerPhrase_start ) {
						/**
						 * smaller start is at or above larger start
						 */
						if ( smallerPhrase_start >= largerPhrase_start ) {
							return 0;
						}
					}
					/**
					 * Not part of an overlap, push to end.
					 */
					return -1;

				}

			};

			/**
			 * Get list of token phrases sorted with all elements that overlap
			 * near the top and all other elements near the bottom.
			 */
			final List<RaptatTokenPhrase> sorted = getPhrases().stream()
					.sorted((l, r) -> r.getPhraseLength()
							.compareTo(l.getPhraseLength()))
					.collect(Collectors.toList());

			/**
			 * Get all the labels in this set, such overlaps can be reviewed on
			 * a per-label basis
			 */
			final Set<Label> allLabels = new HashSet<>();
			for (final RaptatTokenPhrase raptatTokenPhrase : sorted) {
				final Set<Label> phraseLabels = raptatTokenPhrase
						.getPhraseLabels();
				allLabels.addAll(phraseLabels);
			}

			for (final Label labelToReview : allLabels) {

				final Predicate<RaptatTokenPhrase> containsLabel = x -> x
						.getPhraseLabels().contains(labelToReview);

				/**
				 * Filter by label
				 */
				final List<RaptatTokenPhrase> phrasesFilteredByLabel = sorted
						.stream().filter(containsLabel)
						.collect(Collectors.toList());

				for (int index = 0; index < phrasesFilteredByLabel
						.size(); index++) {
					final RaptatTokenPhrase phraseToCheck = phrasesFilteredByLabel
							.get(index);

					/**
					 * Check all the other phrases in this list, which will
					 * always be smaller than that checked.
					 */
					for (final RaptatTokenPhrase phrasesInSet : phrasesFilteredByLabel) {
						/**
						 * Skip phraseToCheck
						 */
						if ( phrasesInSet.equals(phraseToCheck) == false ) {
							/**
							 * if the overlapping phrase
							 */
							final boolean overlaps = overlapIsEqual
									.compare(phrasesInSet, phraseToCheck) == 0;
							if ( overlaps && (phrasesInSet
									.getPhraseLength() < phraseToCheck
											.getPhraseLength()) ) {
								phrasesInSet.removeLabel(labelToReview);
							}
						}
					}
				}
			}
		}

		/**
		 * Iterate through phrases that overlap with the same labels, removing
		 * all the smaller ones. Unlike
		 * trimLabelsFromOverlappedPhrasesUnfiltered(), this method ignores the
		 * labels. That is, only the long phrase remains if overlapped.
		 */
		public void trimLabelsFromOverlappedPhrasesUnfilteredGG() {

			final Comparator<RaptatTokenPhrase> overlapIsEqual = new Comparator<RaptatTokenPhrase>() {

				/**
				 * Comparison to find out of two phrases overlap and thus are
				 * equal, or if not, push the other phrases to end.
				 */
				@Override
				public int compare(final RaptatTokenPhrase firstPhrase,
						final RaptatTokenPhrase secondPhrase) {
					if ( (secondPhrase.getEndOffsetAsInt() > firstPhrase
							.getStartOffsetAsInt())
							&& (secondPhrase.getStartOffsetAsInt() < firstPhrase
									.getEndOffsetAsInt()) ) {
						return 0;
					}
					return -1;
				}
			};

			/*
			 * Remove any shorter length phrases. Considers phrases sequentially
			 * from first to last. Any phrases with higher indices in the list
			 * that overlap are removed.
			 *
			 * First sort phrases inversely according to length, then loop
			 * through the list.
			 */
			final List<RaptatTokenPhrase> sorted = getPhrases().stream()
					.sorted((l, r) -> r.getPhraseLength()
							.compareTo(l.getPhraseLength()))
					.collect(Collectors.toList());
			final ListIterator<RaptatTokenPhrase> testIterator = sorted
					.listIterator();
			while ( testIterator.hasNext() ) {
				final RaptatTokenPhrase initialPhrase = testIterator.next();
				while ( testIterator.hasNext() ) {
					final RaptatTokenPhrase testPhrase = testIterator.next();
					if ( overlapIsEqual.compare(initialPhrase,
							testPhrase) == 0 ) {
						testIterator.remove();
					}
				}
			}
		}
	}

	private List<FSMFeatureLabeler> phraseLabelerList = new ArrayList<>();

	private static final long serialVersionUID = -1106105209648862442L;

	private static final int ESTIMATED_MAX_TOKEN_PHRASES_IN_DOC = 10000;

	protected static final Logger LOGGER = Logger
			.getLogger(TokenPhraseMakerGG.class);
	static {
		LOGGER.setLevel(Level.INFO);
	}

	public TokenPhraseMaker() {
	}

	public TokenPhraseMaker(final List<FSMFeatureLabeler> phraseLabelerList) {
		this.phraseLabelerList = phraseLabelerList;
	}

	public List<RaptatTokenPhrase> getTokenPhrases(
			final List<TextDocumentLine> textDocumentLines,
			final IndexedTree<IndexedObject<RaptatToken>> rawTokensAsTree,
			final IndexedTree<IndexedObject<RaptatToken>> processedTokensAsTree,
			final boolean skipBlankLines) {
		GeneralHelper.errorWriter(
				"getTokenPhrases() not implemented in TokenPhraseMaker Class");
		System.exit(-1);
		return null;
	}

	private void add_matched_labels(final RaptatTokenPhrase foundTokenPhrase,
			final List<RaptatTokenPhrase.Label> labels) {
		foundTokenPhrase.addPhraseLabels(labels);
	}

	private void add_unassigned_label(
			final RaptatTokenPhrase foundTokenPhrase) {
		foundTokenPhrase.addPhraseLabel(
				RaptatTokenPhrase.Label.createUnassignedLabel());

	}

	/**
	 * The object should have a set of finite state machines (see
	 * phraseLabelerList) assigned to it so that it returns all labels
	 * corresponding to the phrase.
	 *
	 * @param fsm_feature_labeler
	 * @param validationCallback
	 *
	 * @param phraseString)
	 * @return
	 */
	private List<RaptatTokenPhrase.Label> findLabelsForTokenPhrase(
			final RaptatTokenPhrase found_token_phrase,
			final IRaptatTokenText string_eval,
			final FSMFeatureLabeler fsm_feature_labeler,
			final MatchPatternValidate validationCallback) {
		// /*
		// * Leaving in as place holder, doesn't actually do anything right now,
		// * but may be a useful mechanism later.
		// */
		// RaptatTokenFSM potential_phrase = RaptatToken.getStringFromTokens(
		// foundTokenPhrase, string_eval, x -> {
		// return x;
		// } );

		/**
		 * if this ends with a period, drop that
		 */
		String text = found_token_phrase.getPhraseText();
		if ( text.endsWith(".") ) {
			final RaptatToken raptatToken = found_token_phrase.getTokensAsList()
					.get(found_token_phrase.getTokensAsList().size() - 1);
			String tokenStringPreprocessed = raptatToken
					.getTokenStringPreprocessed();
			tokenStringPreprocessed = tokenStringPreprocessed.substring(0,
					tokenStringPreprocessed.length() - 1);
			raptatToken.setTokenStringPreprocessed(tokenStringPreprocessed);
			raptatToken.setEndOff(String
					.valueOf(Integer.parseInt(raptatToken.getEndOffset()) - 1));
			text = found_token_phrase.getPhraseText();
		}

		final RaptatTokenFSM potential_phrase = new RaptatTokenFSM(text);

		if ( fsm_feature_labeler != null ) {
			try {
				fsm_feature_labeler.identify(potential_phrase, MatchType.Exact,
						validationCallback);
			}
			catch (final FSMElementException e) {
				e.printStackTrace();
				System.out.println(
						"Error attempting to identify: " + e.getMessage());
			}
		}

		final List<RaptatTokenPhrase.Label> labels_found = new ArrayList<>();

		final Map<String, FSMFeatureData> map = potential_phrase
				.get_fsm_feature_map();
		for (final String key : map.keySet()) {
			final FSMFeatureData label = map.get(key);
			if ( label.get_feature_value() != MatchPattern.None
					.get_match_pattern_name() ) {
				/*
				 * e.g., IndexType, ABI
				 */
				final RaptatTokenPhrase.Label found_label = new RaptatTokenPhrase.Label(
						label.get_feature_type(), label.get_feature_value());
				labels_found.add(found_label);
			}
		}

		return labels_found;
	}

	/**
	 * @param line
	 * @param index_value_fsm_defn
	 * @param fsm_feature_labeler
	 * @param index_range_fsm_defn
	 *            -- Element to ignore (e.g., 1.0-1.5 or 1.0 to 1.5)
	 * @param site_fsm_defn
	 * @return
	 */
	private List<RaptatTokenPhrase> getLineTokenPhrasesGG(
			final TextDocumentLine line,
			final FSMFeatureLabeler fsm_feature_labeler) {

		/*
		 * x -> x.getTokenStringPreprocessed() is a lambda method defined in
		 * IRaptatTokenStringEval that permits specification of which string
		 * method to use on the RapTAT token. Included here to demonstrate how
		 * to generalize approach
		 */
		final IRaptatTokenText getTokenText = x -> {
			final String text = x.getTokenStringPreprocessed();
			return text;
		};

		/*
		 * Changed to use pre-existing raw tokens
		 */
		final List<RaptatToken> tokens = line.getRawTokensList();

		final RaptatTokenPhrases resultPhrases = new RaptatTokenPhrases();

		/**
		 * Create a validation against the entire line. The goal here is to
		 * permit a check for back lookups, comparing what was found as an item
		 * against what exists in the line. If something is found in the phrase
		 * search that does not exist in the line itself, due to negative
		 * lookup, then it's not valid.
		 */
		final RaptatTokenFSM original_line = new RaptatTokenFSM(line.lineText);

		/**
		 * Loop through and attempt to detect minimal covering tokens that
		 * constitute a token phrase, resulting in 1) a series of matched
		 * labels, 2) an IndexValue label, or 3) an "unassigned" label.
		 */
		for (int startIndex = 0; startIndex < tokens.size(); startIndex++) {

			int endIndex = tokens.size();
			if ( (startIndex
					+ RaptatConstants.MAX_TOKENS_DEFAULT) < endIndex ) {
				endIndex = startIndex + RaptatConstants.MAX_TOKENS_DEFAULT;
			}

			while ( endIndex > startIndex ) {
				final List<RaptatToken> candidates = tokens.subList(startIndex,
						endIndex);
				final RaptatTokenPhrase foundTokenPhrase = new RaptatTokenPhrase(
						candidates);

				final MatchPatternValidate validationCallback = new MatchPatternValidate() {

					/**
					 * Use the matching details of the token phrase to review
					 * the original sentence, looking to see if the found phrase
					 * exists in the same place as in the sentence. This is
					 * primarily for patterns with lookbehind strategies
					 *
					 * @param foundMatchDetails
					 * @return
					 */
					@Override
					public boolean validate(
							final MatchDetails<RaptatToken> foundMatchDetails) {
						final String tokenStringPreprocessed = original_line
								.getTokenStringPreprocessed();

						final Pattern pattern = foundMatchDetails.getMatcher()
								.pattern();
						final Matcher matcher = pattern
								.matcher(tokenStringPreprocessed
										.replaceAll("^\\s*", ""));

						boolean validated = false;

						while ( matcher.find() ) {
							final int foundEndOffset = foundMatchDetails
									.get_end();
							final int matcher_end = matcher.end()
									- matcher.start();
							// && foundMatchDetails.get_start() == 0
							if ( validated = foundEndOffset == matcher_end ) {
								break;
							}

						}
						return validated;
					}

				};

				final List<RaptatTokenPhrase.Label> labels = findLabelsForTokenPhrase(
						foundTokenPhrase, getTokenText, fsm_feature_labeler,
						validationCallback);

				/**
				 * Were labels found, and if so add the phrase, otherwise check
				 * for indexValue
				 */
				if ( (labels != null) && (labels.isEmpty() == false) ) {

					/**
					 * BEGIN - 8/12/19 - DAX - Added mechanism to compare new
					 * "found" token with what's already in the collection. If
					 * the new one covers the old one, replace
					 *
					 * https://github.com/CPHI-TVHS/ABIGirotra/issues/13
					 *
					 */
					add_matched_labels(foundTokenPhrase, labels);

					resultPhrases.add(foundTokenPhrase);
				}

				endIndex--;
			}

			if ( endIndex == startIndex ) {
				final List<RaptatToken> token_sublist = tokens
						.subList(startIndex, startIndex + 1);
				final RaptatTokenPhrase foundTokenPhrase = new RaptatTokenPhrase(
						token_sublist);
				add_unassigned_label(foundTokenPhrase);
				resultPhrases.add(foundTokenPhrase);

			}
		}

		/**
		 * Clear out overlapping phrases per label type
		 */
		resultPhrases.trimLabelsFromOverlappedPhrasesUnfilteredGG();

		resultPhrases.removedUnassigned();

		return new ArrayList<>(resultPhrases.getPhrases());
	}

	/**
	 * @param line
	 * @param index_value_fsm_defn
	 * @param fsm_feature_labeler
	 * @param index_range_fsm_defn
	 *            -- Element to ignore (e.g., 1.0-1.5 or 1.0 to 1.5)
	 * @param site_fsm_defn
	 * @return
	 */
	private List<RaptatTokenPhrase> getTokenPhrasesFromAnnotatedPhrase(
			final TextDocumentLine line,
			final FSMFeatureLabeler fsm_feature_labeler) {

		/*
		 * x -> x.getTokenStringPreprocessed() is a lambda method defined in
		 * IRaptatTokenStringEval that permits specification of which string
		 * method to use on the RapTAT token. Included here to demonstrate how
		 * to generalize approach
		 */
		final IRaptatTokenText getTokenText = x -> {
			final String text = x.getTokenStringPreprocessed();
			return text;
		};

		/*
		 * 7/29/19 - DAX - get list of tokens from TextDocumentLine Trying at
		 * first a simple split, since the line does not have the immediate
		 * benefit of the tokenizing from the sentence detection.
		 */
		final List<RaptatToken> tokens = generateRaptatTokensForLine(line);

		final RaptatTokenPhrases resultPhrases = new RaptatTokenPhrases();

		/**
		 * Create a validation against the entire line. The goal here is to
		 * permit a check for back lookups, comparing what was found as an item
		 * against what exists in the line. If somehting is found in the phrase
		 * search that does not exist in the line itself, due to negative
		 * lookup, then it's not valid.
		 */
		final RaptatTokenFSM original_line = new RaptatTokenFSM(line.lineText);

		/**
		 * Loop through and attempt to detect minimal covering tokens that
		 * constitute a token phrase, resulting in 1) a series of matched
		 * labels, 2) an IndexValue label, or 3) an "unassigned" label.
		 */
		for (int startIndex = 0; startIndex < tokens.size(); startIndex++) {

			int endIndex = tokens.size();
			if ( (startIndex
					+ RaptatConstants.MAX_TOKENS_DEFAULT) < endIndex ) {
				endIndex = startIndex + RaptatConstants.MAX_TOKENS_DEFAULT;
			}

			while ( endIndex > startIndex ) {
				final List<RaptatToken> candidates = tokens.subList(startIndex,
						endIndex);
				final RaptatTokenPhrase foundTokenPhrase = new RaptatTokenPhrase(
						candidates);

				final MatchPatternValidate validationCallback = new MatchPatternValidate() {

					/**
					 * Use the matching details of the token phrase to review
					 * the original sentence, looking to see if the found phrase
					 * exists in the same place as in the sentence. This is
					 * primarily for patterns with lookbehind strategies
					 *
					 * @param foundMatchDetails
					 * @return
					 */
					@Override
					public boolean validate(
							final MatchDetails<RaptatToken> foundMatchDetails) {
						final String tokenStringPreprocessed = original_line
								.getTokenStringPreprocessed();

						final Pattern pattern = foundMatchDetails.getMatcher()
								.pattern();
						final Matcher matcher = pattern
								.matcher(tokenStringPreprocessed
										.replaceAll("^\\s*", ""));

						boolean validated = false;

						while ( matcher.find() ) {
							final int foundEndOffset = foundMatchDetails
									.get_end();
							final int matcher_end = matcher.end()
									- matcher.start();
							// && foundMatchDetails.get_start() == 0
							if ( validated = foundEndOffset == matcher_end ) {
								break;
							}

						}
						return validated;
					}

				};

				final List<RaptatTokenPhrase.Label> labels = findLabelsForTokenPhrase(
						foundTokenPhrase, getTokenText, fsm_feature_labeler,
						validationCallback);

				/**
				 * Were labels found, and if so add the phrase, otherwise check
				 * for indexValue
				 */
				if ( (labels != null) && (labels.isEmpty() == false) ) {

					/**
					 * BEGIN - 8/12/19 - DAX - Added mechanism to compare new
					 * "found" token with what's already in the collection. If
					 * the new one covers the old one, replace
					 *
					 * https://github.com/CPHI-TVHS/ABIGirotra/issues/13
					 *
					 */
					add_matched_labels(foundTokenPhrase, labels);

					resultPhrases.add(foundTokenPhrase);
				}

				endIndex--;
			}

			if ( endIndex == startIndex ) {
				final List<RaptatToken> token_sublist = tokens
						.subList(startIndex, startIndex + 1);
				final RaptatTokenPhrase foundTokenPhrase = new RaptatTokenPhrase(
						token_sublist);
				add_unassigned_label(foundTokenPhrase);
				resultPhrases.add(foundTokenPhrase);

			}
		}

		/**
		 * Clear out overlapping phrases per label type
		 */
		resultPhrases.trimLabelsFromOverlappedPhrases();

		resultPhrases.removedUnassigned();

		return new ArrayList<>(resultPhrases.getPhrases());
	}

	private void identifyAllIndexValuesInALine(final TextDocumentLine line,
			final FSMDefinition index_value_fsm_defn,
			final FSMDefinition index_range_fsm_defn,
			final TreeMap<Integer, MatchDetails<RaptatTokenFSM>> index_value_start_offset_to_match) {
		/**
		 * This section seeks to identify all the index values in a line
		 */
		if ( index_value_fsm_defn != null ) {
			final List<MatchPattern> index_elements = index_value_fsm_defn
					.get_match_patterns("INDEX");
			final List<MatchPattern> range_elements = index_range_fsm_defn
					.get_match_patterns("Index_Range_Pattern");

			/*
			 * 7/29/19 - DAX - getting line text from TextDocumentLine
			 */
			String stringToProccess = line.lineText;

			RaptatTokenFSM fsm_token = new RaptatTokenFSM(stringToProccess);

			int startOffset = 0;

			/**
			 * In order to loop through and capture all the IndexValues from a
			 * sentence, keep looping, resetting the token by substring at each
			 * loop, until start offset == end offset
			 */
			while ( fsm_token != null ) {
				final IRaptatTokenText token_text_callback = x -> x
						.getTokenStringPreprocessed();

				/**
				 * DAX - 5/16/19 - Putting in hard check to ensure that we don't
				 * include ranges
				 */
				for (final MatchPattern element : index_elements) {
					MatchDetails<RaptatTokenFSM> match_details = element
							.matches(fsm_token, token_text_callback,
									MatchType.Contains);
					if ( match_details != null ) {

						/**
						 * Place the match into the "lookup", such that later it
						 * can be used to evaluate which RaptatTokenPhrase is
						 * associated by offset
						 */
						MatchDetails<RaptatTokenFSM> cloned_match = match_details
								.clone_to_offset(match_details, startOffset);

						int substring_delta = match_details.get_end();
						/**
						 * Check for patterns matching "range" elements (e.g.,
						 * 1.0 to 2.0) in order to exclude them.
						 */
						for (final MatchPattern range_element : range_elements) {
							final MatchDetails<RaptatTokenFSM> range_match_details = range_element
									.matches(fsm_token, token_text_callback,
											MatchType.Contains);
							if ( range_match_details != null ) {
								/**
								 * Set the match to empty
								 */
								match_details = null;
								cloned_match = range_match_details
										.clone_to_offset(range_match_details,
												startOffset);
								substring_delta = range_match_details.get_end();
								break;
							}
						}

						stringToProccess = stringToProccess
								.substring(substring_delta);

						if ( match_details != null ) {
							index_value_start_offset_to_match.put(
									cloned_match.get_start(), cloned_match);
							/*
							 * re-calculate to get the next piece of the
							 * "sentence"
							 */
							startOffset = cloned_match.get_end();

						}

						fsm_token = stringToProccess.length() > 0
								? new RaptatTokenFSM(stringToProccess)
								: null;

					}
					else {
						fsm_token = null;
					}
				}
			}
		}
	}

	private static List<RaptatToken> generateRaptatTokensForLine(
			final TextDocumentLine line) {
		final List<RaptatToken> retVal = new ArrayList<>();

		final String[] pieces = line.lineText.split("[\\s:]");
		int lastReference = line.startOffset;
		for (int index = 0; index < pieces.length; index++) {
			String piece = pieces[index];
			final int start = lastReference;
			piece = piece.trim();
			final int end = piece.length() + lastReference;
			lastReference = end + 1;
			if ( piece.trim().length() > 0 ) {
				final RaptatToken raptatToken = RaptatTokenHelper
						.AsPreprocessed(piece, start, end, index);
				if ( raptatToken.getTokenStringPreprocessed()
						.isEmpty() == false ) {
					retVal.add(raptatToken);
				}
			}
		}
		return retVal;
	}

}
