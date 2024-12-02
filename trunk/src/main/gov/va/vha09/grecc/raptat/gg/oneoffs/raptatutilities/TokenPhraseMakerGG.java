package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.TokenPhraseMaker;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class TokenPhraseMakerGG extends TokenPhraseMaker {

	private class CandidatePhrase implements Comparable<CandidatePhrase> {
		final String category;
		final String subcategory;
		final String phraseText;
		final int start;
		final int end;

		public CandidatePhrase(final String category, final String subcategory,
				final String phraseText, final int start) {
			super();
			this.category = category;
			this.subcategory = subcategory;
			/*
			 * Just trim front at first to see the amount of white space at the
			 * beginning
			 */
			int shortPhraseSize = phraseText.replaceFirst("^\\s+", "").length();
			this.phraseText = phraseText.trim();
			this.start = (start + phraseText.length()) - shortPhraseSize;
			this.end = this.start + this.phraseText.length();
		}

		/**
		 * Compare two CandidatePhrase instances, first by length, then by start
		 * offset
		 */
		@Override
		public int compareTo(final CandidatePhrase otherCandidatePhrase) {
			final int thisPhraseLength = this.end - this.start;
			final int otherPhraseLength = otherCandidatePhrase.end
					- otherCandidatePhrase.start;
			if ( thisPhraseLength != otherPhraseLength ) {
				return thisPhraseLength - otherPhraseLength;
			}

			return this.start - otherCandidatePhrase.start;
		}

		@Override
		public boolean equals(final Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( this.getClass() != obj.getClass() ) {
				return false;
			}
			final CandidatePhrase other = (CandidatePhrase) obj;
			if ( !getEnclosingInstance()
					.equals(other.getEnclosingInstance()) ) {
				return false;
			}
			if ( this.category == null ) {
				if ( other.category != null ) {
					return false;
				}
			}
			else if ( !this.category.equals(other.category) ) {
				return false;
			}
			if ( this.end != other.end ) {
				return false;
			}
			if ( this.phraseText == null ) {
				if ( other.phraseText != null ) {
					return false;
				}
			}
			else if ( !this.phraseText.equals(other.phraseText) ) {
				return false;
			}
			if ( this.start != other.start ) {
				return false;
			}
			if ( this.subcategory == null ) {
				if ( other.subcategory != null ) {
					return false;
				}
			}
			else if ( !this.subcategory.equals(other.subcategory) ) {
				return false;
			}
			return true;
		}

		public int getStart() {
			return this.start;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getEnclosingInstance().hashCode();
			result = (prime * result)
					+ (this.category == null ? 0 : this.category.hashCode());
			result = (prime * result) + this.end;
			result = (prime * result) + (this.phraseText == null ? 0
					: this.phraseText.hashCode());
			result = (prime * result) + this.start;
			result = (prime * result) + (this.subcategory == null ? 0
					: this.subcategory.hashCode());
			return result;
		}

		public boolean overlaps(final CandidatePhrase otherPhrase) {
			return (otherPhrase.end > this.start)
					&& (otherPhrase.start < this.end);
		}

		@Override
		public String toString() {
			return "CandidatePhrase [category=" + this.category
					+ ", subcategory=" + this.subcategory + ", phraseText="
					+ this.phraseText + ", start=" + this.start + ", end="
					+ this.end + "]";
		}

		private TokenPhraseMakerGG getEnclosingInstance() {
			return TokenPhraseMakerGG.this;
		}

	}

	private enum RunType {
		RUN_PHRASE_MAKER, SIMPLE_TEST
	}

	private Label ignoredLabel = RaptatTokenPhrase.Label
			.createUnassignedLabel();
	{
		LOGGER.setLevel(Level.INFO);
	}
	private HashMap<String, HashMap<String, Set<Pattern>>> categoryToRegexMap = new HashMap<>();
	private HashMap<String, Set<String>> categoryToSubcategoryMap = null;

	private static final Pattern CONTAINS_CHARACTER = Pattern.compile("\\S");

	private static final boolean CATEGORY_CASE_TO_LOWERCASE = true;

	public TokenPhraseMakerGG() {
		super();
	}

	public TokenPhraseMakerGG(final String resourcePathToRegexMap) {
		super();
		final String resourcePath = "/src/main/resources/"
				+ resourcePathToRegexMap;
		URL url = this.getClass().getResource(resourcePath);
		String fullPathToRegexMap;
		try {
			fullPathToRegexMap = url.toURI().getPath();
			final File regexMapFile = new File(fullPathToRegexMap);
			final TabReader tr = new TabReader(regexMapFile);
			createRegexMap(tr);

			LOGGER.debug("RegexMap File:" + regexMapFile.getAbsolutePath());
		}
		catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Determines the RaptatTokenPhrase instances found in a given line of text
	 * from a document. It does this using regex patterns stored in the
	 * categoryToRegexMap, that provides a mapping of categories and
	 * subcategories to a pattern. The RaptatTokenPhrase instances are given a
	 * label with the name set to the category of any matched pattern, and it's
	 * value is the subcategory of the pattern.
	 *
	 * <p>
	 * <b>NOTE:</b> Any tokens that are not assigned to a phrase during the
	 * regex match are each assigned to separate RaptatTokenPhrase instances
	 * with a label name and value of unassigned. This assignment is done in the
	 * call to the method convertToRaptatTokenPhrases().
	 *
	 *
	 * @param documentLine
	 * @param documentTokens
	 * @return
	 */
	public List<RaptatTokenPhrase> findLinePhrases(
			final TextDocumentLine documentLine,
			final IndexedTree<IndexedObject<RaptatToken>> documentTokens) {

		final List<CandidatePhrase> candidatePhrases = new ArrayList<>();
		final String lineText = documentLine.lineText;
		for (final String category : this.categoryToRegexMap.keySet()) {
			final Map<String, Set<Pattern>> subcategoryMap = this.categoryToRegexMap
					.get(category);

			for (final String subcategory : subcategoryMap.keySet()) {
				final Set<Pattern> regexPatterns = subcategoryMap
						.get(subcategory);

				for (final Pattern regexPattern : regexPatterns) {
					final Matcher patternMatcher = regexPattern
							.matcher(lineText);
					while ( patternMatcher.find() ) {
						LOGGER.debug("Found '" + patternMatcher.group()
								+ "' using " + regexPattern.pattern());
						final CandidatePhrase candidate = new CandidatePhrase(
								category, subcategory, patternMatcher.group(),
								patternMatcher.start()
										+ documentLine.startOffset);
						candidatePhrases.add(candidate);
					}
				}
			}
		}

		if ( LOGGER.getLevel().equals(Level.DEBUG) ) {
			System.out.println(
					"Final Regex Phrases for Line:" + documentLine.lineText);
			candidatePhrases
					.sort(Comparator.comparing(CandidatePhrase::getStart));
			candidatePhrases.forEach(phrase -> LOGGER.debug(phrase));
		}
		final List<RaptatTokenPhrase> identifiedPhrases = convertToRaptatTokenPhrases(
				candidatePhrases, documentLine, documentTokens);
		filterForNonOverlapping(identifiedPhrases);
		identifiedPhrases.sort(
				Comparator.comparing(RaptatTokenPhrase::getStartOffsetAsInt));
		setPhraseOffsets(identifiedPhrases);
		return identifiedPhrases;

	}

	/**
	 * Gets the association of categories and its subcategories from the mapping
	 * of categories and subcategories to regular expressions used to identify
	 * phrases within the categories and subcategories.
	 *
	 * @return
	 */
	public HashMap<String, Set<String>> getCategoryToSubcategoryMap() {
		if ( this.categoryToSubcategoryMap == null ) {
			this.categoryToSubcategoryMap = new HashMap<>();
			Function<String, String> caseConversion = TokenPhraseMakerGG.CATEGORY_CASE_TO_LOWERCASE
					? String::toLowerCase
					: inputString -> inputString;
			for (String category : this.categoryToRegexMap.keySet()) {
				HashMap<String, Set<Pattern>> mapping = this.categoryToRegexMap
						.get(category);
				Set<String> subcategories = mapping.keySet().stream()
						.map(caseConversion)
						.collect(Collectors.toCollection(HashSet::new));
				this.categoryToSubcategoryMap
						.put(caseConversion.apply(category), subcategories);
			}
		}
		return this.categoryToSubcategoryMap;
	}

	@Override
	public List<RaptatTokenPhrase> getTokenPhrases(
			final List<TextDocumentLine> textDocumentLines,
			final IndexedTree<IndexedObject<RaptatToken>> rawTokensAsTree,
			final IndexedTree<IndexedObject<RaptatToken>> processedTokensAsTree,
			final boolean skipBlankLines) {
		final List<RaptatTokenPhrase> resultPhrases = new ArrayList<>();

		for (final TextDocumentLine line : textDocumentLines) {
			final Matcher characterFinder = CONTAINS_CHARACTER
					.matcher(line.lineText);
			if ( !skipBlankLines || characterFinder.find() ) {
				/*
				 * Note that findLinePhrases sorts the phrase before returning
				 * them
				 */
				final List<RaptatTokenPhrase> phrasesInLine = findLinePhrases(
						line, rawTokensAsTree);
				int phraseIndexInLine = 0;
				for (RaptatTokenPhrase phrase : phrasesInLine) {
					phrase.setIndexInLine(phraseIndexInLine++);
				}
				line.setTokenPhrases(phrasesInLine);
				resultPhrases.addAll(phrasesInLine);
			}
		}

		return resultPhrases;
	}

	public void print() {
		for (final String category : this.categoryToRegexMap.keySet()) {
			final Map<String, Set<Pattern>> subcategoryMap = this.categoryToRegexMap
					.get(category);
			System.out.println("\nCategory:" + category);
			for (final String subcategory : subcategoryMap.keySet()) {
				System.out.println("\tSubcategory:" + subcategory);
				final Set<Pattern> regexPatterns = subcategoryMap
						.get(subcategory);
				int i = 0;
				for (final Pattern regexPattern : regexPatterns) {
					final String regexPatternString = regexPattern.pattern();
					System.out.println("\t\tCase Senstive:"
							+ ((regexPattern.flags()
									& Pattern.CASE_INSENSITIVE) < 1)
							+ "\tRegexPattern " + i++ + ":"
							+ regexPatternString);
				}
			}
		}
	}

	private List<RaptatTokenPhrase> convertToRaptatTokenPhrases(
			final List<CandidatePhrase> phrases,
			final TextDocumentLine phrasesLine,
			final IndexedTree<IndexedObject<RaptatToken>> documentTokens) {

		final List<RaptatTokenPhrase> resultPhrases = new ArrayList<>();

		for (final CandidatePhrase phrase : phrases) {
			final List<RaptatToken> phraseTokens = RaptatToken
					.getTokensCoveringRange(documentTokens, phrase.start,
							phrase.end);

			final Label phraseLabel = new RaptatTokenPhrase.Label(
					phrase.category, phrase.subcategory);
			final RaptatTokenPhrase resultPhrase = new RaptatTokenPhrase(
					phraseTokens.toArray(new RaptatToken[0]), phraseLabel,
					phrasesLine);
			resultPhrases.add(resultPhrase);
		}
		/*
		 * All tokens that are not part of a labeled phrase are each made a part
		 * of an unlabeled phrase so that the set of resultPhrases will
		 * completely cover the line (i.e. every token in the will be assigned
		 * to a RaptatTokenPhrase instance who beginning and end cover the start
		 * and end of every token.
		 */
		Collection<RaptatTokenPhrase> unlabeledPhrases = getUnlabeledPhrases(
				resultPhrases, phrasesLine, documentTokens);
		resultPhrases.addAll(unlabeledPhrases);

		return resultPhrases;
	}

	private void createRegexMap(final TabReader tabReader) {
		final int expectedColumns = 4;
		String[] regexMappings;
		int dataIndex = 0;
		final Pattern caseMatchPattern = Pattern.compile("y(es)?|t(rue)?");
		while ( (regexMappings = tabReader.getNextData()) != null ) {
			++dataIndex;
			if ( regexMappings.length >= expectedColumns ) {
				LOGGER.debug("DataIndex " + dataIndex + ":"
						+ Arrays.toString(regexMappings));
				final String caseSensitiveProperty = regexMappings[0].trim()
						.toLowerCase();
				final boolean caseSensitive = caseMatchPattern
						.matcher(caseSensitiveProperty).find();
				final HashMap<String, Set<Pattern>> categoryMapping = this.categoryToRegexMap
						.computeIfAbsent(regexMappings[1].trim(),
								category -> new HashMap<>());
				final Set<Pattern> regexPatternSet = categoryMapping
						.computeIfAbsent(regexMappings[2].trim(),
								subcategory -> new HashSet<>());
				final String regexMappingString = regexMappings[3];
				if ( caseSensitive ) {
					regexPatternSet.add(Pattern.compile(regexMappingString,
							Pattern.MULTILINE | Pattern.UNICODE_CASE));
				}
				else {
					regexPatternSet.add(Pattern.compile(regexMappingString,
							Pattern.MULTILINE | Pattern.UNICODE_CASE
									| Pattern.CASE_INSENSITIVE));
				}
			}
		}
		print();
	}

	private void filterForNonOverlapping(List<RaptatTokenPhrase> tokenPhrases) {
		Collections.sort(tokenPhrases);
		Collections.reverse(tokenPhrases);
		int iterationStart = 0;
		while ( iterationStart < tokenPhrases.size() ) {
			final ListIterator<RaptatTokenPhrase> tokenPhraseIterator = tokenPhrases
					.listIterator(iterationStart++);
			final RaptatTokenPhrase startPhrase = tokenPhraseIterator.next();
			while ( tokenPhraseIterator.hasNext() ) {
				final RaptatTokenPhrase nextPhrase = tokenPhraseIterator.next();
				if ( nextPhrase.overlaps(startPhrase) ) {
					tokenPhraseIterator.remove();
				}
			}
		}
	}

	// private void filterForNonOverlapping(
	// final List<TokenPhraseMakerGG.CandidatePhrase> candidatePhrases) {
	// Collections.sort(candidatePhrases);
	// Collections.reverse(candidatePhrases);
	// int iterationStart = 0;
	// while ( iterationStart < candidatePhrases.size() ) {
	// final ListIterator<CandidatePhrase> candidateIterator = candidatePhrases
	// .listIterator(iterationStart++);
	// final CandidatePhrase startCandidate = candidateIterator.next();
	// while ( candidateIterator.hasNext() ) {
	// final CandidatePhrase nextCandidate = candidateIterator.next();
	// if ( nextCandidate.overlaps(startCandidate) ) {
	// candidateIterator.remove();
	// }
	// }
	// }
	// }

	/**
	 * Find the subsequences of tokens in a TextDocument line object that are
	 * unlabeled, create unlabeled/unassigned RaptatTokenPhrase instances with
	 * each sequence, and return the instances as a collection.
	 *
	 * @param resultPhrases
	 * @param lineWithPhrases
	 * @param documentTokens
	 * @return
	 */
	private Collection<RaptatTokenPhrase> getUnlabeledPhrases(
			final List<RaptatTokenPhrase> resultPhrases,
			final TextDocumentLine lineWithPhrases,
			final IndexedTree<IndexedObject<RaptatToken>> documentTokens) {

		List<RaptatToken> lineTokens = lineWithPhrases.getRawTokensList();
		Set<RaptatToken> coveredLineTokens = new HashSet<>();
		resultPhrases.forEach(
				phrase -> coveredLineTokens.addAll(phrase.getTokensAsList()));

		List<RaptatTokenPhrase> unlabeledPhrases = new ArrayList<>();
		ListIterator<RaptatToken> lineTokenIterator = lineTokens.listIterator();

		/*
		 * Find the tokens in the list from the line that are unlabeled, then
		 * add any that immediately follow and are not part of a labeled phrase
		 * to create an unlabeled phrase. Do this repeatedly to find
		 * subsequences of tokens not currently part of any RaptatTokenPhrase.
		 */
		while ( lineTokenIterator.hasNext() ) {
			RaptatToken lineToken = lineTokenIterator.next();
			if ( !coveredLineTokens.contains(lineToken) ) {
				List<RaptatToken> unlabeledPhraseTokens = new ArrayList<>();
				unlabeledPhraseTokens.add(lineToken);
				while ( lineTokenIterator.hasNext() ) {
					lineToken = lineTokenIterator.next();
					if ( coveredLineTokens.contains(lineToken) ) {
						break;
					}
					unlabeledPhraseTokens.add(lineToken);
				}
				RaptatTokenPhrase unlabeledPhrase = new RaptatTokenPhrase(
						unlabeledPhraseTokens,
						RaptatTokenPhrase.Label.LABEL_UNASSIGNED,
						lineWithPhrases);
				unlabeledPhrases.add(unlabeledPhrase);
			}
		}

		return unlabeledPhrases;
	}

	/**
	 * Determines the offsets of each phrase. For phrases on a given line, a
	 * phrase with an assigned label adds 1 to the offset of the next phrase.
	 * Phrases without a label add a number dependent on the setting of the
	 * DIVISOR constant within the RaptatTokenPhrase class because an unassigned
	 * phrase may represent a sequence of several tokens. Initially, DIVISOR was
	 * set to 2, so an unassigned phrase of 6 tokens would add 6/2 or 3 to the
	 * offset.
	 *
	 * @param phrases
	 */
	private void setPhraseOffsets(final List<RaptatTokenPhrase> phrases) {

		int startOffset = 0;
		for (RaptatTokenPhrase phrase : phrases) {
			startOffset = phrase.setLinePhraseOffset(startOffset);
		}
	}

	public static void main(final String[] args) {
		final String lvgPath = System.getenv("LVG_DIR");
		UserPreferences.INSTANCE.setLvgPath(lvgPath);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			@SuppressWarnings("ResultOfObjectAllocationIgnored")
			public void run() {
				final RunType runType = RunType.SIMPLE_TEST;

				switch (runType) {
				case RUN_PHRASE_MAKER:
					runPhraseMaker();
					break;

				case SIMPLE_TEST:
					runTest();
					break;

				}

			}

			private void runPhraseMaker() {
				OptionsManager.getInstance().setRemoveStopWords(false);
				final TextAnalyzer ta = new TextAnalyzer();
				ta.setTokenProcessingOptions(OptionsManager.getInstance());
				ta.createTokenPhraseMaker("ABILabelPatterns_v01_191117.txt");

				final String textString = "the ankle blood pressure, 55 mmhg is lower on the right side where iv is 1.2, 0.6, 2.3, 0.99, .651; 4) dorsal pedal artery";

				final RaptatDocument raptatDocument = ta.processText(textString,
						Optional.empty());

				System.out.println(raptatDocument);
				// final TextDocumentLine line = new TextDocumentLine(5, 150,
				// "the ankle blood pressure, 55 mmhg is lower on the right
				// side; 4) dorsal pedal artery",
				// null, null);
				//
				// tpm.findLinePhrases(line, null);

			}

			private void runTest() {
				final String testString01 = "aBc";
				String patternString01 = "abc";
				Pattern pattern = Pattern.compile(patternString01,
						Pattern.CASE_INSENSITIVE);
				boolean isMatched = pattern.matcher(testString01).find();
				System.out.println("Test '" + testString01 + "' matches '"
						+ patternString01 + "' - " + isMatched);

				pattern = Pattern.compile(patternString01);
				isMatched = pattern.matcher(testString01).find();
				System.out.println("Test '" + testString01 + "' matches '"
						+ patternString01 + "' - " + isMatched);

				patternString01 = "(?i:abc)";
				pattern = Pattern.compile(patternString01);
				isMatched = pattern.matcher(testString01).find();
				System.out.println("Test '" + testString01 + "' matches '"
						+ patternString01 + "' - " + isMatched);
			}

		});
	}

}
