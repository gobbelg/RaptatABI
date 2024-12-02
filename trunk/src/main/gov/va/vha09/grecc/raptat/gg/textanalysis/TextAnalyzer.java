/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import opennlp.tools.postag.POSTagger;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase.Label;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.TokenPhraseMaker;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.VerticalColumnAlgorithm;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.common.IndexValueConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.SentenceSectionTaggerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree.IndexedTreeIterator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.Stopwatch;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;
import src.main.gov.va.vha09.grecc.raptat.gg.lexicaltools.Lemmatiser;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities.TokenPhraseMakerGG;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.FacilitatorBlockerDictionaryFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.LemmatiserFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.MajorSectionMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.MedListSectionMarker;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.NegationContextAnalyzerFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.POSTaggerFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.ReasonNoMedsContextAnalyzerFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.RegexTextSplitter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.TokenizerFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.UncertaintyContextAnalyzerFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

/**
 * Singleton that analyzes text documents and breaks them down to sentences and
 * their tokens. These along with a String indicating the text source path are
 * returned in a RaptatDocument instance by a call to the primary method in this
 * class, processDocument().
 *
 * @author Glenn Gobbel - Jun 27, 2012
 */
public class TextAnalyzer {
	private enum RunType {
		RUN_SENTENCES, CHECK_LEMMATIZATION, ANALYZE_DOCUMENT, CHECK_GETPROCESSEDTOKENSTRINGS_METHOD;
	}

	private RegexTextSplitter textSplitter = null;

	private Tokenizer tokenizer;

	private POSTagger posTagger;

	private TokenContextAnalyzer negationContextAnalyzer;

	private TokenContextAnalyzer uncertainContextAnalyzer;

	private TokenContextAnalyzer reasonNoMedsContextAnalyzer;

	private TokenContextAnalyzer facilitatorBlockerDictionary;
	private Lemmatiser stemmer;

	private boolean useStems;

	private boolean usePOS;

	private boolean removeStopWords;

	private boolean generateTokenPhrases = true;

	private ContextHandling reasonNoMedsProcessing;

	private int maxPhraseTokens = RaptatConstants.MAX_TOKENS_DEFAULT;

	private Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers;

	private MajorSectionMarker medListSectionMarker;

	private TokenPhraseMaker tokenPhraseMaker;

	// Synchronized as this is reported to be necessary according to UIMA
	private static final Collection<String> stopWordSet = Collections
			.unmodifiableCollection(
					new HashSet<>(Arrays.asList(RaptatConstants.STOP_WORDS)));
	/*
	 * Create a hash map to go from OpenNLP parts of speech to an LVG part of
	 * speech
	 */
	private static final Hashtable<String, Integer> openNLPToLVGMap = new Hashtable<>(
			20);

	private static final StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();
	/* Regex to identify tokens that start with non-alphanumeric characters */
	private static final Matcher NON_ALPHA_START = Pattern.compile("(^\\W+)")
			.matcher("");
	private static final Matcher ALPHA_NON_ALPHA_ALTERNATION_MATCHER = Pattern
			.compile("(([^\\w\\s])\\2*)|(\\w+)").matcher("");
	private static final Matcher SIMPLE_LINE_BREAK = Pattern
			.compile("\\r\\n|\\r|\\n").matcher("");
	private static final PhraseTokenIntegrator tokenIntegrator = new PhraseTokenIntegrator();

	private static final Logger LOGGER = Logger.getLogger(TextAnalyzer.class);

	private static final long serialVersionUID = -2052454515695348353L;

	/*
	 * Generate map from OpenNLP parts of speech to LVG. This is used for LVG
	 * stemmming. Note that some parts of speech, such as prepositions, are not
	 * included because they do not have inflection variants.
	 */
	static {
		String[] curArray = RaptatConstants.OPEN_NLP_NOUNS;
		for (final String curString : curArray) {
			TextAnalyzer.openNLPToLVGMap.put(curString,
					RaptatConstants.LVG_NOUN_BITFLAG);
		}
		curArray = RaptatConstants.OPEN_NLP_PRONOUNS;
		for (final String curString : curArray) {
			TextAnalyzer.openNLPToLVGMap.put(curString,
					RaptatConstants.LVG_PRONOUN_BITFLAG);
		}
		curArray = RaptatConstants.OPEN_NLP_ADJECTIVES;
		for (final String curString : curArray) {
			TextAnalyzer.openNLPToLVGMap.put(curString,
					RaptatConstants.LVG_ADJECTIVE_BITFLAG);
		}
		curArray = RaptatConstants.OPEN_NLP_MODALS;
		for (final String curString : curArray) {
			TextAnalyzer.openNLPToLVGMap.put(curString,
					RaptatConstants.LVG_MODAL_BITFLAG);
		}
		curArray = RaptatConstants.OPEN_NLP_ADVERBS;
		for (final String curString : curArray) {
			TextAnalyzer.openNLPToLVGMap.put(curString,
					RaptatConstants.LVG_ADVERB_BITFLAG);
		}
		curArray = RaptatConstants.OPEN_NLP_VERBS;
		for (final String curString : curArray) {
			TextAnalyzer.openNLPToLVGMap.put(curString,
					RaptatConstants.LVG_VERB_BITFLAG);
		}
	}

	// private List<TextDocumentLine> textDocumentLines;

	private final static UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;

	// Make class a singleton because creating
	// these processors is expensive, so we only
	// want to do it once and then reuse them
	// until no longer needed
	public TextAnalyzer() {
		initialize();

		TextAnalyzer.LOGGER.setLevel(Level.INFO);
	}

	public TextAnalyzer(final String dictionaryPath,
			final HashSet<String> acceptedConcepts) {
		this();
		createDictionary(dictionaryPath, acceptedConcepts);
	}

	public TextAnalyzer(final TokenContextAnalyzer dictionary) {
		this();
		this.facilitatorBlockerDictionary = dictionary;
	}

	public void addSectionMatcher(final String patternName,
			final String patternToMatch) {
		this.textSplitter.addSectionMatcher(patternName, patternToMatch);
	}

	public void addSentenceAndSectionMatchers(
			final List<RaptatPair<String, String>> sentenceMatchers,
			final List<RaptatPair<String, String>> sectionMatchers) {
		if ( sentenceMatchers != null ) {
			for (final RaptatPair<String, String> matcherPair : sentenceMatchers) {
				addSentenceMatcher(matcherPair.left, matcherPair.right);
			}
		}

		if ( sectionMatchers != null ) {
			for (final RaptatPair<String, String> matcherPair : sectionMatchers) {
				addSectionMatcher(matcherPair.left, matcherPair.right);
			}

		}
	}

	public void addSentenceAndSectionMatchers(
			final Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> matchers) {
		if ( matchers != null ) {
			for (final SentenceSectionTaggerType type : matchers.keySet()) {
				if ( type.equals(SentenceSectionTaggerType.SENTENCE) ) {
					for (final RaptatPair<String, String> matcherPair : matchers
							.get(type)) {
						addSentenceMatcher(matcherPair.left, matcherPair.right);
					}
				}
				if ( type.equals(SentenceSectionTaggerType.SECTION) ) {
					for (final RaptatPair<String, String> matcherPair : matchers
							.get(type)) {
						addSectionMatcher(matcherPair.left, matcherPair.right);
					}
				}
			}
		}
	}

	public void addSentenceMatcher(final String patternName,
			final String patternToMatch) {
		this.textSplitter.addSentenceMatcher(patternName, patternToMatch);
	}

	public void createDictionary(final String dictionaryPath,
			final HashSet<String> acceptedConcepts) {
		if ( (dictionaryPath != null) && !dictionaryPath.isEmpty() ) {

			if ( !new File(dictionaryPath).exists() ) {
				GeneralHelper.errorWriter(
						"No dictionary found at:\n\n    " + dictionaryPath);
				System.exit(-1);
			}
			this.facilitatorBlockerDictionary = FacilitatorBlockerDictionaryFactory
					.getInstance(dictionaryPath, acceptedConcepts);

		}
	}

	public void createTokenPhraseMaker(final String ResourcePathToRegexMap) {
		this.tokenPhraseMaker = new TokenPhraseMakerGG(ResourcePathToRegexMap);
	}

	/**
	 * @return the generateTokenPhrases
	 */
	public boolean generateTokenPhrases() {
		return this.generateTokenPhrases;
	}

	/**
	 * @return the facilitatorBlockerDictionary
	 */
	public TokenContextAnalyzer getFacilitatorBlockerDictionary() {
		return this.facilitatorBlockerDictionary;
	}

	public String[] getNegatedSentenceCue(final String sentenceText) {
		String[] tokenStrings = this.tokenizer.tokenize(sentenceText);
		final Span[] tokenPositions = this.tokenizer.tokenizePos(sentenceText);

		final ArrayList<String> processedTokenStrings = new ArrayList<>();

		for (int i = 0; i < tokenStrings.length; i++) {
			final RaptatPair<List<String>, List<Integer>> processedList = splitByAlphanumericRevised(
					tokenStrings[i], tokenPositions[i].getStart());

			processedTokenStrings.addAll(processedList.left);
		}

		tokenStrings = processedTokenStrings.toArray(tokenStrings);
		final String[] negationContext = this.negationContextAnalyzer
				.getContextCue(tokenStrings);

		return negationContext;
	}

	public String[] getNegatedSentenceTokensTest(final String sentenceText) {
		final String[] tokenStrings = this.tokenizer.tokenize(sentenceText);
		final String[] negationContext = this.negationContextAnalyzer
				.getContext(tokenStrings, RaptatConstants.NEGATION_TAG);

		return negationContext;
	}

	public String[] getNegatedSentenceTokensTest(
			final String[] sentenceTokens) {
		final String[] negationContext = this.negationContextAnalyzer
				.getContext(sentenceTokens, RaptatConstants.NEGATION_TAG);

		return negationContext;
	}

	/**
	 * @return the sentenceAndSectionMatchers
	 */
	public Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> getSentenceAndSectionMatchers() {
		return this.sentenceAndSectionMatchers;
	}

	/**
	 * This method is written to replace the original getSentenceTokens method.
	 * Here the processed token list will be used for further processing rather
	 * than the raw token list.
	 *
	 * @ author Sanjib Saha
	 *
	 * @param sentenceText
	 * @param sentenceSpan
	 * @param tokenIndex
	 * @return
	 */
	public RaptatPair<List<RaptatToken>, List<RaptatToken>> getSentenceTokens(
			final String sentenceText, final Span sentenceSpan,
			int tokenIndex) {
		String stemString, augmentedTokenString;
		RaptatToken newToken;

		final Span[] tokenPositions = this.tokenizer.tokenizePos(sentenceText);
		String[] baseTokenStrings = Span.spansToStrings(tokenPositions,
				sentenceText);

		// REGION: Logging Region
		if ( TextAnalyzer.LOGGER.isDebugEnabled() ) {
			System.out.println("SentenceSpan:" + sentenceSpan + ", Sentence:"
					+ sentenceText);
			for (int i = 0; i < tokenPositions.length; i++) {
				System.out.println("TokenSpan:" + tokenPositions[i] + ", Token:"
						+ baseTokenStrings[i]);
			}
		}
		// ENDREGION

		final ArrayList<String> alphanumSplitStrings = new ArrayList<>();
		final ArrayList<Integer> alphanumSplitStringPositions = new ArrayList<>();
		for (int i = 0; i < baseTokenStrings.length; i++) {
			final RaptatPair<List<String>, List<Integer>> alphaNumericSplitList = splitByAlphanumericRevised(
					baseTokenStrings[i], tokenPositions[i].getStart());

			alphanumSplitStrings.addAll(alphaNumericSplitList.left);
			alphanumSplitStringPositions.addAll(alphaNumericSplitList.right);
		}
		baseTokenStrings = alphanumSplitStrings.toArray(new String[0]);

		final String[] partsOfSpeech = this.posTagger.tag(baseTokenStrings);
		final String[] preProcessedTokenStrings = this.stemmer
				.preProcess(baseTokenStrings);
		final String[] reasonNoMedsContext = this.reasonNoMedsContextAnalyzer
				.getContext(preProcessedTokenStrings,
						RaptatConstants.REASON_NO_MEDS_TAG);

		/*
		 * Note that this call which creates negationContextAndMarks is
		 * performed before removal of stop words, so all words in the text are
		 * used for getting context
		 */
		final RaptatPair<String[], Mark[]> negationContextAndMarks = this.negationContextAnalyzer
				.getContextAndContextMarks(preProcessedTokenStrings,
						RaptatConstants.NEGATION_TAG);

		/*
		 * Negation context tracks which tokens are negated in the
		 * preProcessedTokenString array
		 */
		final String[] negationContext = negationContextAndMarks.left;

		/*
		 * The negationMarks array tracks tokens that are part of pre-, post-,
		 * pseudo-, or term- negation phrases. Elements in this array may be
		 * used by other systems, such as a CRF cue-scope marker for negation.
		 */
		final Mark[] negationMarks = negationContextAndMarks.right;

		/*
		 * Create uncertain context just like negation context above
		 */
		/*
		 * Note that this call which creates negationContextAndMarks is
		 * performed before removal of stop words, so all words in the text are
		 * used for getting context
		 */
		final RaptatPair<String[], Mark[]> uncertainContextAndMarks = this.uncertainContextAnalyzer
				.getContextAndContextMarks(preProcessedTokenStrings,
						RaptatConstants.UNCERTAIN_TAG);

		/*
		 * Negation context tracks which tokens are uncertain in the
		 * preProcessedTokenString array
		 */
		final String[] uncertainContext = uncertainContextAndMarks.left;

		final int sentenceStart = sentenceSpan.getStart();
		int tokenStart, tokenEnd;
		RaptatToken previousToken = null;

		final List<RaptatToken> rawTokenList = new ArrayList<>(
				baseTokenStrings.length);
		final List<RaptatToken> processedTokenList = new ArrayList<>(
				baseTokenStrings.length);

		for (int i = 0; i < preProcessedTokenStrings.length; i++) {
			tokenStart = alphanumSplitStringPositions.get(i) + sentenceStart;
			tokenEnd = tokenStart + baseTokenStrings[i].length();

			/*
			 * Only stem tokens that have a part of speech within the
			 * openNLPToLVGMap - other parts of speech are unlikely to change
			 */
			if ( TextAnalyzer.openNLPToLVGMap.containsKey(partsOfSpeech[i]) ) {
				stemString = this.stemmer.lemmatize(preProcessedTokenStrings[i],
						TextAnalyzer.openNLPToLVGMap.get(partsOfSpeech[i]));
			}
			else {
				stemString = preProcessedTokenStrings[i];
			}

			if ( this.useStems ) {
				augmentedTokenString = stemString;
			}
			else {
				augmentedTokenString = preProcessedTokenStrings[i];
			}

			if ( this.usePOS ) {
				augmentedTokenString += "_" + partsOfSpeech[i];
			}

			if ( this.reasonNoMedsProcessing == ContextHandling.PROBABILISTIC ) {
				augmentedTokenString += "_" + reasonNoMedsContext[i];
			}

			newToken = new RaptatToken(baseTokenStrings[i],
					preProcessedTokenStrings[i], stemString,
					augmentedTokenString, partsOfSpeech[i],
					String.valueOf(tokenStart), String.valueOf(tokenEnd),
					previousToken, reasonNoMedsContext[i],
					negationMarks[i].toString(),
					negationContext[i].equals(RaptatConstants.NEGATION_TAG),
					uncertainContext[i].equals(RaptatConstants.UNCERTAIN_TAG),
					tokenIndex++);
			previousToken = newToken;

			rawTokenList.add(newToken);

			if ( !this.removeStopWords || !TextAnalyzer.stopWordSet
					.contains(preProcessedTokenStrings[i]) ) {
				processedTokenList.add(newToken);
				newToken.setProcessed(true);
			}
		}

		Collections.sort(processedTokenList, TextAnalyzer.tokenSorter);

		/*
		 * Note that the only difference between the rawTokenList and the
		 * processedTokenList is that the processedTokenList has stop words
		 * removed (if removeStopWords field is set to true)
		 */
		return new RaptatPair<>(rawTokenList, processedTokenList);
	}

	public TokenPhraseMaker getTokenPhraseMaker() {
		return this.tokenPhraseMaker;
	}

	public TokenProcessingOptions getTokenProcessingOptions() {
		boolean invertTokenSequence = false;
		return new TokenProcessingOptions(this.useStems, this.usePOS,
				this.removeStopWords, invertTokenSequence,
				this.reasonNoMedsProcessing, this.reasonNoMedsProcessing,
				this.maxPhraseTokens);
	}

	public String[] getUncertainSentenceCue(final String sentenceText) {
		String[] tokenStrings = this.tokenizer.tokenize(sentenceText);
		final Span[] tokenPositions = this.tokenizer.tokenizePos(sentenceText);

		final ArrayList<String> processedTokenStrings = new ArrayList<>();

		for (int i = 0; i < tokenStrings.length; i++) {
			final RaptatPair<List<String>, List<Integer>> processedList = splitByAlphanumericRevised(
					tokenStrings[i], tokenPositions[i].getStart());

			processedTokenStrings.addAll(processedList.left);
		}

		tokenStrings = processedTokenStrings.toArray(tokenStrings);
		final String[] uncertainContext = this.uncertainContextAnalyzer
				.getContextCue(tokenStrings);

		return uncertainContext;
	}

	// /////// Uncertainty detection... Sanjib June 25, 2015
	public String[] getUncertainSentenceTokensTest(final String sentenceText) {
		final String[] tokenStrings = this.tokenizer.tokenize(sentenceText);
		final String[] uncertainContext = this.uncertainContextAnalyzer
				.getContext(tokenStrings, RaptatConstants.UNCERTAIN_TAG);

		return uncertainContext;
	}

	public String[] getUncertainSentenceTokensTest(
			final String[] sentenceTokens) {
		final String[] uncertainContext = this.uncertainContextAnalyzer
				.getContext(sentenceTokens, RaptatConstants.UNCERTAIN_TAG);

		return uncertainContext;
	}

	/**
	 * @param matcherPath
	 */
	public void loadSectionAndSentenceMatchers(final String matcherPath) {
		if ( (matcherPath != null) && !matcherPath.isEmpty()
				&& new File(matcherPath).exists() ) {
			final TabReader reader = new TabReader(new File(matcherPath));
			String[] data;
			if ( this.sentenceAndSectionMatchers == null ) {
				this.sentenceAndSectionMatchers = new HashMap<>();
			}

			while ( (data = reader.getNextData()) != null ) {
				if ( isValidMatcher(data) ) {
					List<RaptatPair<String, String>> matchers;
					if ( data[0].equalsIgnoreCase("section") ) {
						addSectionMatcher(data[1], data[2]);
						if ( (matchers = this.sentenceAndSectionMatchers.get(
								SentenceSectionTaggerType.SECTION)) == null ) {
							matchers = new ArrayList<>();
							this.sentenceAndSectionMatchers.put(
									SentenceSectionTaggerType.SECTION,
									matchers);
						}
						matchers.add(new RaptatPair<>(data[1], data[2]));
					}
					else if ( data[0].equalsIgnoreCase("sentence") ) {
						addSentenceMatcher(data[1], data[2]);
						if ( (matchers = this.sentenceAndSectionMatchers.get(
								SentenceSectionTaggerType.SENTENCE)) == null ) {
							matchers = new ArrayList<>();
							this.sentenceAndSectionMatchers.put(
									SentenceSectionTaggerType.SENTENCE,
									matchers);
						}
						matchers.add(new RaptatPair<>(data[1], data[2]));
					}
				}
			}
		}
	}

	/**
	 * Given a path to a document, break it down to sentences and tokens. Return
	 * it as a RaptatDocument. Each sentence is represented as an annotated
	 * phrase. Tokens in the sentence are stored in "raw" form as found in the
	 * document and in processed form, where they are stemmed, tagged with POS,
	 * or with stop words removed according to the booleans useStems, usePOS,
	 * and removeStopWrods.
	 *
	 * @param pathToTextDocument
	 * @return
	 *
	 * @author Glenn Gobbel - Sep 10, 2012
	 *
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public RaptatDocument processDocument(File docFile) {
		String inputText = null;
		Stopwatch timer = new Stopwatch();

		try {
			// timer.tic( "Reading in file from disk" );
			inputText = FileUtils.readFileToString(docFile, "UTF-8");
			// timer.toc( "File read complete" );
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}

		if ( inputText == null ) {
			return null;
		}

		timer.tic("Processing sentences and tokens in document "
				+ docFile.getName());
		final RaptatDocument resultDocument = processText(inputText,
				Optional.of(docFile.getAbsolutePath()));
		timer.toc("Document processed");
		return resultDocument;

	}

	/**
	 * Given a path to a document, break it down to sentences and tokens. Return
	 * it as a RaptatDocument. Each sentence is represented as an annotated
	 * phrase. Tokens in the sentence are stored in "raw" form as found in the
	 * document and in processed form, where they are stemmed, tagged with POS,
	 * or with stop words removed according to the booleans useStems, usePOS,
	 * and removeStopWrods.
	 *
	 * @param pathToTextDocument
	 * @return
	 *
	 * @author Glenn Gobbel - Sep 10, 2012
	 *
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public RaptatDocument processDocument(String pathToTextDocument) {
		return processDocument(new File(pathToTextDocument));
	}

	// /**
	// * Given a path to a document, break it down to sentences and tokens.
	// Return it as a
	// * RaptatDocument. Each sentence is represented as an annotated phrase.
	// Tokens in the sentence
	// are
	// * stored in "raw" form as found in the document and in processed form,
	// where they are stemmed,
	// * tagged with POS, or with stop words removed according to the booleans
	// useStems, usePOS, and
	// * removeStopWrods.
	// *
	// * @param pathToTextDocument
	// * @return
	// * @author Glenn Gobbel - Sep 10, 2012
	// */
	// @SuppressWarnings("CallToPrintStackTrace")
	// public RaptatDocument processDocument(final String pathToTextDocument) {
	// String inputText = null;
	// final Stopwatch timer = new Stopwatch();
	//
	// try {
	// // timer.tic( "Reading in file from disk" );
	// inputText = FileUtils.readFileToString(new File(pathToTextDocument),
	// "UTF-8");
	// // timer.toc( "File read complete" );
	// } catch (final IOException e1) {
	// e1.printStackTrace();
	// }
	//
	// if (inputText != null) {
	// timer.tic(
	// "Processing sentences and tokens in document " + new
	// File(pathToTextDocument).getName());
	// final RaptatDocument resultDocument = processText(inputText,
	// pathToTextDocument);
	// timer.toc("Document processed");
	// return resultDocument;
	// }
	// return null;
	// }

	public RaptatDocument processDocumentText(final String inputText,
			final Optional<String> documentSource) {
		final Stopwatch timer = new Stopwatch();
		if ( inputText != null ) {
			timer.tic("Processing sentences and tokens in document "
					+ documentSource);
			final RaptatDocument resultDocument = processText(inputText,
					documentSource);
			timer.toc("Document processed");
			return resultDocument;
		}
		else {
			return null;
		}
	}

	public RaptatDocument processText(final String inputText,
			final Optional<String> documentSource) {
		final boolean addTokensAsSentence = true;

		String sentenceText;

		IndexedTree<IndexedObject<RaptatToken>> processedTokensAsTree = null;
		IndexedTree<IndexedObject<RaptatToken>> rawTokensAsTree = null;
		final List<AnnotatedPhrase> dictionaryAnnotations = new ArrayList<>();

		RaptatPair<List<RaptatToken>, List<RaptatToken>> sentenceTokens;

		final Map<Span, Set<String>> sentenceSpansAndContext = this.textSplitter
				.detectSentenceSpans(inputText);

		final List<AnnotatedPhrase> theSentences = new ArrayList<>(
				sentenceSpansAndContext.size());
		final RaptatDocument theDocument = new RaptatDocument();
		theDocument.setTextSourcePath(documentSource);

		final List<RaptatToken> processedTokensAsList = new ArrayList<>(
				RaptatConstants.TOKENS_PER_DOCUMENT);
		final List<RaptatToken> rawTokensAsList = new ArrayList<>(
				RaptatConstants.TOKENS_PER_DOCUMENT);

		if ( !sentenceSpansAndContext.isEmpty() ) {

			AnnotatedPhrase previousSentence = null;

			final List<Span> sentenceSpans = new ArrayList<>(
					sentenceSpansAndContext.keySet());
			Collections.sort(sentenceSpans);
			int tokenIndex = 0;
			for (final Span curSpan : sentenceSpans) {
				sentenceText = curSpan.getCoveredText(inputText).toString();

				TextAnalyzer.LOGGER.debug("Sentence:" + sentenceText);

				/*
				 * getSentenceTokens() returns a pair of lists, with the raw
				 * tokens (no stop words removed) on the left side of the pair
				 * and the processed tokens (stop words removed if indicated))
				 * on the right.
				 */
				// Added by Sanjib to replace the previous method
				sentenceTokens = getSentenceTokens(sentenceText, curSpan,
						tokenIndex);
				tokenIndex += sentenceTokens.left.size();

				final Set<String> sentenceConcepts = sentenceSpansAndContext
						.get(curSpan);
				final String annotatorID = "RaptatSentence_"
						+ UniqueIDGenerator.INSTANCE.getUnique();
				final String annotatorName = "RaptatSentence";
				final AnnotatedPhrase newSentence = new AnnotatedPhrase(
						sentenceText, curSpan, sentenceTokens,
						addTokensAsSentence, previousSentence, theDocument,
						sentenceConcepts, annotatorID, annotatorName);
				theSentences.add(newSentence);

				previousSentence = newSentence;

				/*
				 * The generalTokenContextAnalyzer is a dictionary-based
				 * annotator that adds annotations to the document so they can
				 * be added later to the list of annotations found. This is
				 * generally done if they do not overlap probabilistic
				 * annotations (probabilistic annotations take precedence)
				 */
				if ( this.facilitatorBlockerDictionary != null ) {
					final List<AnnotatedPhrase> dictionaryAnnotationsForSentence = this.facilitatorBlockerDictionary
							.getSentenceAnnotations(newSentence);
					dictionaryAnnotations
							.addAll(dictionaryAnnotationsForSentence);
				}

				processedTokensAsList.addAll(sentenceTokens.right);
				rawTokensAsList.addAll(sentenceTokens.left);
			}

			processedTokensAsTree = RaptatToken
					.generateIndexedTree(processedTokensAsList);
			rawTokensAsTree = RaptatToken.generateIndexedTree(rawTokensAsList);
		}

		List<TextDocumentLine> documentLines = getTextDocumentLines(inputText,
				processedTokensAsTree, rawTokensAsTree, theDocument);
		theDocument.setDocumentLines(documentLines);

		if ( LOGGER.getLevel().equals(Level.DEBUG) ) {
			LOGGER.debug("LINES in " + theDocument.getTextSource());
			for (TextDocumentLine line : theDocument.getTextDocumentLines()) {
				System.out.println("LINE:" + line);
			}
		}

		if ( this.generateTokenPhrases ) {
			final boolean skipBlankLines = true;
			final List<RaptatTokenPhrase> tokenPhrases = this.tokenPhraseMaker
					.getTokenPhrases(documentLines, rawTokensAsTree,
							processedTokensAsTree, skipBlankLines);
			theDocument.setTokenPhrases(tokenPhrases);

			if ( LOGGER.getLevel().equals(Level.DEBUG) ) {
				LOGGER.debug("RAPTAT TOKEN PHRASES in "
						+ theDocument.getTextSource());
				for (RaptatTokenPhrase phrase : tokenPhrases) {
					StringBuilder sb = new StringBuilder();
					sb.append("START:" + phrase.getStartOffsetAsInt());
					sb.append("\t").append("STARTLINE:"
							+ phrase.get_start_offset_relative_to_line());
					sb.append("\t").append("END:" + phrase.getEndOffsetAsInt());
					sb.append("\t").append("ENDLINE:"
							+ phrase.get_end_offset_relative_to_line());
					sb.append("\t")
							.append("STRING:" + RaptatToken
									.getUnprocessedStringFromTokens(
											phrase.getTokensAsList()));
					System.out.println(sb.toString());
				}
			}

			final List<DocumentColumn> documentColumns = VerticalColumnAlgorithm
					.invoke(tokenPhrases, false);
			theDocument.setListOfColumnsOfPhrases(documentColumns);
			for (RaptatTokenPhrase phrase : tokenPhrases) {
				phrase.setDistancesToLines();
			}
			int columnsInDocument = theDocument.getDocumentColumns().size();
			for (TextDocumentLine line : theDocument.getTextDocumentLines()) {
				line.setColumnOffsetsAlongLine(columnsInDocument);
			}
		}
		/*
		 * This is where to place checklist section marker for use in extracting
		 * semi-structured elements within the text
		 */
		this.medListSectionMarker.markDocumentSentences(theSentences,
				inputText);
		theDocument.setBaseSentences(theSentences);
		theDocument.setDictionaryAnnotations(dictionaryAnnotations);
		theDocument.setProcessedTokens(processedTokensAsTree);
		theDocument.setRawTokens(rawTokensAsTree);

		if ( LOGGER.getLevel().equals(Level.DEBUG) ) {
			LOGGER.debug("COLUMNS in " + theDocument.getTextSource());
			int columnNumber = 0;
			for (DocumentColumn documentColumn : theDocument
					.getDocumentColumns()) {
				LOGGER.debug("\n\n\n----------------- COLUMN:" + columnNumber++
						+ " ---------------------");
				for (RaptatTokenPhrase phrase : documentColumn.columnPhrases) {
					System.out.println("============== PHRASE STRING:  "
							+ phrase.getPhraseText() + " ==============");
					System.out.println("PHRASE:" + phrase);
				}
			}
		}

		return theDocument;
	}

	public void setFacilitatorBlockerDictionary(
			final TokenContextAnalyzer dictionary) {
		this.facilitatorBlockerDictionary = dictionary;
	}

	/**
	 * @param generateTokenPhrases
	 *            the generateTokenPhrases to set
	 */
	public void setGenerateTokenPhrases(final boolean generateTokenPhrases) {
		this.generateTokenPhrases = generateTokenPhrases;
	}

	public void setTokenPhraseMaker(final TokenPhraseMaker tokenPhraseMaker) {
		this.tokenPhraseMaker = tokenPhraseMaker;
	}

	public synchronized void setTokenProcessingOptions(
			final OptionsManager optionsManager) {
		this.useStems = optionsManager.getUseStems();
		this.usePOS = optionsManager.getUsePOS();
		this.removeStopWords = optionsManager.getRemoveStopWords();
		this.maxPhraseTokens = optionsManager.getTrainingTokenNumber();
		this.reasonNoMedsProcessing = optionsManager
				.getReasonNoMedsProcessing();
	}

	/**
	 * @param useStems
	 * @param usePOS
	 * @param removeStopWords
	 * @author Glenn Gobbel - Jun 28, 2012
	 * @param maxElements
	 * @param reasonNoMedsProcessing
	 */
	public synchronized void setTokenProcessingParameters(
			final boolean useStems, final boolean usePOS,
			final boolean removeStopWords, final int maxElements,
			final ContextHandling reasonNoMedsProcessing) {
		this.useStems = useStems;
		this.usePOS = usePOS;
		this.removeStopWords = removeStopWords;
		this.maxPhraseTokens = maxElements;
		this.reasonNoMedsProcessing = reasonNoMedsProcessing;
	}

	public synchronized void setTokenProcessingParameters(
			final TokenProcessingOptions theOptions) {
		this.useStems = theOptions.useStems();
		this.usePOS = theOptions.usePOS();
		this.removeStopWords = theOptions.removeStopWords();
		this.maxPhraseTokens = theOptions.getMaxTokenNumber();
		this.reasonNoMedsProcessing = theOptions.getReasonNoMedsProcessing();
	}

	/**
	 * This method is extracted from the original getTokensAndOffsets method.
	 * This method is designed to deliver only the list of the modified token
	 * list, so that the number of tokens in Raw Token List and in the Negex
	 * String Array size is same.
	 *
	 * @param stringToSplit
	 * @return
	 */
	public RaptatPair<List<String>, List<Integer>> splitByAlphanumeric(
			String stringToSplit, int curTokenPosition) {
		final ArrayList<String> tokenStringList = new ArrayList<>();
		final List<Integer> tokenPosList = new ArrayList<>();

		/*
		 * First get the non-alphanumeric characters that begin the string if
		 * they exist, add them to the list, and remove them from the string so
		 * we only have to deal with strings that start with alphanumeric
		 * characters followed by non-alphanumeric characters
		 */
		TextAnalyzer.NON_ALPHA_START.reset(stringToSplit);

		if ( TextAnalyzer.NON_ALPHA_START.find() ) {
			final String startString = TextAnalyzer.NON_ALPHA_START.group(1);
			stringToSplit = TextAnalyzer.NON_ALPHA_START.replaceFirst("");
			tokenStringList.add(startString);
			tokenPosList.add(curTokenPosition);
			curTokenPosition += startString.length();
		}

		TextAnalyzer.ALPHA_NON_ALPHA_ALTERNATION_MATCHER.reset(stringToSplit);
		while ( TextAnalyzer.ALPHA_NON_ALPHA_ALTERNATION_MATCHER.find() ) {
			for (int i = 1; i < 3; i++) {
				final String curGroupString = TextAnalyzer.ALPHA_NON_ALPHA_ALTERNATION_MATCHER
						.group(i);

				if ( (curGroupString != null)
						&& (curGroupString.length() > 0) ) {
					tokenStringList.add(curGroupString);
					tokenPosList.add(
							TextAnalyzer.ALPHA_NON_ALPHA_ALTERNATION_MATCHER
									.start(i) + curTokenPosition);
				}
			}
		}

		return new RaptatPair<>(tokenStringList, tokenPosList);
	}

	/**
	 * Method to split any strings that mix alphanumeric strings with
	 * non-alphanumeric characters. Non-alphanumeric characters that repeat are
	 * kept together to capture delimiters of sections and similar
	 * constructions.
	 *
	 * @param stringToSplit
	 * @return
	 */
	public RaptatPair<List<String>, List<Integer>> splitByAlphanumericRevised(
			String stringToSplit, int curTokenPosition) {
		final ArrayList<String> tokenStringList = new ArrayList<>();
		final List<Integer> tokenPosList = new ArrayList<>();

		Matcher matcher = TextAnalyzer.ALPHA_NON_ALPHA_ALTERNATION_MATCHER
				.reset(stringToSplit);
		final int repeatedNonAlphaStringCaptureGroup = 1;
		final int alphanumericStringCaptureGroup = 3;

		String stringMatch;
		while ( matcher.find() ) {
			stringMatch = matcher.group(repeatedNonAlphaStringCaptureGroup);
			if ( (stringMatch != null) && (stringMatch.length() > 0) ) {
				tokenStringList.add(stringMatch);
				tokenPosList
						.add(matcher.start(repeatedNonAlphaStringCaptureGroup)
								+ curTokenPosition);
			}

			stringMatch = matcher.group(alphanumericStringCaptureGroup);
			if ( (stringMatch != null) && (stringMatch.length() > 0) ) {
				tokenStringList.add(stringMatch);
				tokenPosList.add(matcher.start(alphanumericStringCaptureGroup)
						+ curTokenPosition);
			}
		}

		return new RaptatPair<>(tokenStringList, tokenPosList);
	}

	/**
	 * Adjust the token lists in the document sentences and annotations
	 * according to the options within this instance of the TextAnalyzer. The
	 * method regenerates "processed tokens," which are those that are not in
	 * the stopWord list. Also, the "augmentedString" field of the processed
	 * tokens is updated according to useStems, usePOS, and reasonNoMeds tagging
	 * set for this TextAnalyzer instance. The processed tokens are added to the
	 * annotations in the AnnotationGroup, and the annotations are split to
	 * conform to the field maxTokens for this TextAnalyzer instance.
	 *
	 * @author Glenn Gobbel - Jul 13, 2012
	 * @param inputGroup
	 */
	public void updateTrainingGroup(final AnnotationGroup inputGroup) {
		final PhraseTokenIntegrator theIntegrator = new PhraseTokenIntegrator();

		final RaptatDocument raptatDocument = inputGroup.getRaptatDocument();
		TextAnalyzer.LOGGER.debug("Regenerating processed tokens for document:"
				+ raptatDocument.getTextSource());
		final List<RaptatToken> processedTokens = raptatDocument
				.regenerateProcessedTokens(this.removeStopWords, this.useStems,
						this.usePOS,
						this.reasonNoMedsProcessing == ContextHandling.PROBABILISTIC,
						TextAnalyzer.stopWordSet);

		TextAnalyzer.LOGGER.debug("Adding processed tokens to document");
		raptatDocument.setProcessedTokens(
				RaptatToken.generateIndexedTree(processedTokens));

		TextAnalyzer.LOGGER
				.debug("Adding processed tokens to document annotations");

		inputGroup.restoreSplitPhrases();
		final Hashtable<Integer, AnnotatedPhrase> splitPhrases = theIntegrator
				.addProcessedTokensToAnnotations(
						inputGroup.referenceAnnotations, raptatDocument,
						this.usePOS, this.maxPhraseTokens);

		// Split phrases contains the processed tokens.
		inputGroup.setSplitPhrases(splitPhrases);
		inputGroup.setMaxTokens(this.maxPhraseTokens);
	}

	/**
	 * Adjust the token lists in the document sentences and annotations
	 * according to the options within this instance of the TextAnalyzer
	 *
	 * @param documentGroups
	 * @author Glenn Gobbel - Jul 13, 2012
	 */
	public void updateTrainingGroups(
			final List<AnnotationGroup> documentGroups) {
		for (final AnnotationGroup curGroup : documentGroups) {
			updateTrainingGroup(curGroup);
		}
	}

	/**
	 * DAX 5/7/19 Iterate through phrases and tokens, adding into the lines
	 *
	 * @param lines
	 * @param phrases
	 * @param tokens
	 */
	@Deprecated
	private void assign_token_phrases_and_tokens_to_lines(
			final LinkedList<TextDocumentLine> lines,
			final List<RaptatTokenPhrase> phrases,
			final IndexedTree<IndexedObject<RaptatToken>> tokens) {

		/*
		 * Load all the doc lines into a tree set ordered by end offset. This
		 * will permit searching for keys based on the floor or ceiling of the
		 * phrase or token offset
		 */
		final TreeMap<Integer, TextDocumentLine> map_end_offset_to_line = new TreeMap<>();
		final TreeMap<TextDocumentLine, List<RaptatTokenPhrase>> map_line_to_tokens = new TreeMap<>();

		for (final TextDocumentLine textDocumentLine : lines) {
			map_end_offset_to_line.put(textDocumentLine.endOffset,
					textDocumentLine);
		}

		for (final RaptatTokenPhrase raptatTokenPhrase : phrases) {
			/*
			 * Get the end offset from the phrase
			 */
			final int phrase_end_offset = raptatTokenPhrase.getEndOffsetAsInt();
			/*
			 * Get the next line, which in reality is the current line of this
			 * phrase
			 */
			final TextDocumentLine current_line = map_end_offset_to_line
					.ceilingEntry(phrase_end_offset).getValue();
			/*
			 * set the reference to current line
			 */
			raptatTokenPhrase.set_containing_text_document_line(current_line);

			if ( map_line_to_tokens.containsKey(current_line) == false ) {
				map_line_to_tokens.put(current_line,
						new ArrayList<RaptatTokenPhrase>());
			}
			final List<RaptatTokenPhrase> phrases_in_current_line = map_line_to_tokens
					.get(current_line);
			phrases_in_current_line.add(raptatTokenPhrase);
		}

		/**
		 * Add the phrases to the line
		 */
		for (final TextDocumentLine line : map_line_to_tokens.keySet()) {
			line.setTokenPhrases(map_line_to_tokens.get(line));
		}

		/**
		 * The following just reviews the list of tokens and assigns the line to
		 * them; this may not be needed, but it can be torn out easily
		 */
		if ( tokens != null ) {
			for (final IndexedObject<RaptatToken> indexedObject : tokens
					.getAllNodes()) {
				/*
				 * Get the end offset from the phrase
				 */
				final RaptatToken indexed_token = indexedObject
						.getIndexedObject();
				/*
				 * Get the current line of this phrase
				 */
				final int end_offset = indexed_token.get_end_offset_as_int();
				final TextDocumentLine current_line = map_end_offset_to_line
						.ceilingEntry(end_offset).getValue();
				/*
				 * set the reference to current line
				 */
				indexed_token.set_containing_text_document_line(current_line);

			}
		}

	}

	/**
	 * This is a method refactored from assign_token_phrases_and_tokens_to_lines
	 *
	 * @param textDocumentLines2
	 * @param phrases
	 * @param tokens
	 */
	private void assignTokenPhrasesToLines(
			final List<TextDocumentLine> textDocumentLines2,
			final List<RaptatTokenPhrase> phrases) {

		/*
		 * Load all the doc lines into a tree set ordered by end offset. This
		 * will permit searching for keys based on the floor or ceiling of the
		 * phrase or token offset
		 */
		final TreeMap<Integer, TextDocumentLine> map_end_offset_to_line = new TreeMap<>();
		final TreeMap<TextDocumentLine, List<RaptatTokenPhrase>> map_line_to_tokens = new TreeMap<>();

		for (final TextDocumentLine textDocumentLine : textDocumentLines2) {
			map_end_offset_to_line.put(textDocumentLine.endOffset,
					textDocumentLine);
		}

		for (final RaptatTokenPhrase raptatTokenPhrase : phrases) {
			/*
			 * Get the end offset from the phrase
			 */
			final int phrase_end_offset = raptatTokenPhrase.getEndOffsetAsInt();
			/*
			 * Get the next line, which in reality is the current line of this
			 * phrase
			 */
			final TextDocumentLine current_line = map_end_offset_to_line
					.ceilingEntry(phrase_end_offset).getValue();
			/*
			 * set the reference to current line
			 */
			raptatTokenPhrase.set_containing_text_document_line(current_line);

			if ( map_line_to_tokens.containsKey(current_line) == false ) {
				map_line_to_tokens.put(current_line,
						new ArrayList<RaptatTokenPhrase>());
			}
			final List<RaptatTokenPhrase> phrases_in_current_line = map_line_to_tokens
					.get(current_line);
			phrases_in_current_line.add(raptatTokenPhrase);
		}

		/**
		 * Add the phrases to the line
		 */
		for (final TextDocumentLine line : map_line_to_tokens.keySet()) {
			line.setTokenPhrases(map_line_to_tokens.get(line));
		}

	}

	private List<TextDocumentLine> getTextDocumentLines(final String inputText,
			final IndexedTree<IndexedObject<RaptatToken>> processedTokensAsTree,
			final IndexedTree<IndexedObject<RaptatToken>> rawTokensAsTree,
			final RaptatDocument sourceDocument) {

		final List<TextDocumentLine> documentLines = new ArrayList<>();
		final Matcher lineBreakMatcher = TextAnalyzer.SIMPLE_LINE_BREAK
				.reset(inputText);
		int startOffset = 0;
		/**
		 * 7/8/19 - DAX - Added line number reference
		 */
		int lineNumber = 0;
		TextDocumentLine priorLine = null;
		IndexedTreeIterator processedTokenIterator = processedTokensAsTree
				.iterator();
		IndexedTreeIterator rawTokenIterator = rawTokensAsTree.iterator();

		while ( lineBreakMatcher.find() ) {
			final int endOffset = lineBreakMatcher.start();
			final String lineString = inputText.substring(startOffset,
					endOffset);
			LOGGER.debug("Processing " + lineString);
			final TextDocumentLine line = new TextDocumentLine(startOffset,
					endOffset, lineString, processedTokenIterator,
					rawTokenIterator, priorLine, sourceDocument);
			line.set_line_index(lineNumber);
			documentLines.add(line);

			startOffset = lineBreakMatcher.end();
			lineNumber++;
		}

		/*
		 * The last bit of the text
		 */
		if ( startOffset < inputText.length() ) {
			final int endOffset = inputText.length();
			final String lineString = inputText.substring(startOffset,
					endOffset);
			final TextDocumentLine line = new TextDocumentLine(startOffset,
					endOffset, lineString, processedTokenIterator,
					rawTokenIterator, priorLine, sourceDocument);
			line.set_line_index(lineNumber);
			documentLines.add(line);
		}

		return documentLines;
	}

	private void initialize() {
		final OptionsManager optionsManager = OptionsManager.getInstance();
		setTokenProcessingOptions(optionsManager);

		/*
		 * Leave textSplitter set to null to use sentenceSplitter instead for
		 * splitting sentences
		 */
		this.textSplitter = new RegexTextSplitter();
		this.posTagger = POSTaggerFactory.getInstance();
		this.tokenizer = TokenizerFactory.getInstance();
		this.negationContextAnalyzer = NegationContextAnalyzerFactory
				.getInstance();
		this.uncertainContextAnalyzer = UncertaintyContextAnalyzerFactory
				.getInstance();
		this.reasonNoMedsContextAnalyzer = ReasonNoMedsContextAnalyzerFactory
				.getInstance();
		this.medListSectionMarker = new MedListSectionMarker();

		/*
		 * Note that we changed the line below so that the stemmer object never
		 * removes stop words - this is handled elsewhere in the code by looking
		 * up words in the stopWordSet object.
		 */
		this.stemmer = LemmatiserFactory.getInstance(false);
	}

	/**
	 * @param data
	 * @return
	 */
	private boolean isValidMatcher(final String[] data) {
		if ( data.length != 3 ) {
			return false;
		}
		for (int i = 0; i < 3; i++) {
			if ( (data[i] == null) || data[i].isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Excludes from the list of index values any phrases that fall within the
	 * bounds of an exclusion phrase
	 *
	 * @param indexValueCandidates
	 * @param exclusionPhrases
	 * @return
	 */
	public static List<RaptatTokenPhrase> filterIndexValueCandidatesByExcludedValues(
			final List<RaptatTokenPhrase> indexValueCandidates,
			final List<RaptatTokenPhrase> exclusionPhrases) {

		final List<RaptatTokenPhrase> rv = new ArrayList<>();

		/**
		 * Check each possible candidate to see if it's contained in an
		 * exclusion
		 */
		for (final RaptatTokenPhrase indexValueCandidate : indexValueCandidates) {
			boolean isWithinPhrase = false;
			for (final RaptatTokenPhrase exclusionPhrase : exclusionPhrases) {
				final boolean candidateStartIsGTEExclusionStart = indexValueCandidate
						.getStartOffsetAsInt() >= exclusionPhrase
								.getStartOffsetAsInt();
				final boolean candidateEndisLTEExclusionEnd = indexValueCandidate
						.getEndOffsetAsInt() <= exclusionPhrase
								.getEndOffsetAsInt();
				isWithinPhrase |= candidateStartIsGTEExclusionStart
						&& candidateEndisLTEExclusionEnd;
				if ( isWithinPhrase ) {
					break;
				}
			}
			if ( isWithinPhrase == false ) {
				rv.add(indexValueCandidate);
			}
		}

		return rv;
	}

	/**
	 * Look for all the index values in the document
	 *
	 * @param inputText
	 * @return
	 */
	public static List<RaptatTokenPhrase> identifyIndexValueTokenPhrases(
			final String inputText) {
		final Pattern pattern = Pattern
				.compile(IndexValueConstants.IndexValueRegEx);

		final Matcher matcher = pattern.matcher(inputText);

		final List<RaptatTokenPhrase> phrases = new ArrayList<>();

		while ( matcher.find() ) {
			final String indexValueKey = IndexValueConstants.IndexValueKey;
			final Integer start = matcher.start(indexValueKey);
			final Integer end = matcher.end(indexValueKey);
			final String text = matcher.group(indexValueKey);
			final RaptatToken raptatToken = new RaptatToken(text,
					start.toString(), end.toString(), null, null);
			final Label label = Label.createUnassignedLabel();
			final RaptatTokenPhrase phrase = new RaptatTokenPhrase(raptatToken,
					label);
			phrase.trim();
			phrase.addPhraseLabel(
					RaptatTokenPhrase.Label.createIndexValueLabel());
			phrases.add(phrase);
		}

		return phrases;
	}

	/**
	 * Look for all the ranges or index values that should be ignored.
	 *
	 * @param inputText
	 * @return
	 */
	public static List<RaptatTokenPhrase> identifyValuesToIgnore(
			final String inputText) {
		final Pattern pattern = Pattern
				.compile(IndexValueConstants.IndexValueIgnoreRegEx);

		final Matcher matcher = pattern.matcher(inputText);

		final List<RaptatTokenPhrase> phrases = new ArrayList<>();

		while ( matcher.find() ) {
			final Integer start = matcher.start();
			final Integer end = matcher.end();
			final String text = matcher.group().trim();
			final RaptatToken raptatToken = new RaptatToken(text,
					start.toString(), end.toString(), null, null);
			final Label label = Label.createUnassignedLabel();
			final RaptatTokenPhrase phrase = new RaptatTokenPhrase(raptatToken,
					label);
			phrases.add(phrase);
		}
		return phrases;
	}

	public static void main(final String[] args) {
		/*
		 * This is new testing code used in Sanjib's new code to test
		 * annotation. To implement, uncomment the two lines below this one and
		 * comment out all other lines in this main method.
		 */
		// TextAnalyzer ta = new TextAnalyzer();
		// ta.testAnnotateFile();

		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			@SuppressWarnings("ResultOfObjectAllocationIgnored")
			public void run() {
				final RunType runType = RunType.ANALYZE_DOCUMENT;

				switch (runType) {
				case CHECK_LEMMATIZATION:
					checkLemmatization();
					break;
				case RUN_SENTENCES:
					checkSentences();
					break;
				case ANALYZE_DOCUMENT:
					final String pathToDocument = "P:/ORD_Girotra_202110052D/Glenn"
							+ "/ConcatedNoteSets/AllRadAbiNotes_Table_rad_abi_111722_WithImpression_230921"
							+ "/DeconcatRoot_230921_161615/deconcat_001_000/deconcat_002_007/PEARLS_TIU_230812_002_007_093.txt";
					final String tokenPhraseLabelerName = "ABILabelPatterns_v18_200513.txt";
					RaptatDocument raptatDocument = analyzeDocument(
							pathToDocument, null, tokenPhraseLabelerName);
					break;
				case CHECK_GETPROCESSEDTOKENSTRINGS_METHOD:
					final String startString = "[: -]";
					checkGetProcessedTokenStrings(startString);
					break;
				// case RUN_
				}
			}

			private RaptatDocument analyzeDocument(final String pathToDocument,
					final String pathToDictionary,
					final String tokenPhraseMakerName) {
				final TextAnalyzer ta = new TextAnalyzer(pathToDictionary,
						null);
				ta.setGenerateTokenPhrases(false);
				if ( (tokenPhraseMakerName != null)
						&& (tokenPhraseMakerName.length() > 0) ) {
					TokenPhraseMaker tpm = new TokenPhraseMakerGG(
							tokenPhraseMakerName);
					ta.setTokenPhraseMaker(tpm);
					ta.setGenerateTokenPhrases(true);
				}
				return ta.processDocument(pathToDocument);

			}

			private void checkGetProcessedTokenStrings(
					final String startString) {
				final TextAnalyzer ta = new TextAnalyzer();
				final RaptatPair<List<String>, List<Integer>> results = ta
						.splitByAlphanumericRevised(startString, 0);

				System.out.println("StartString:" + startString);

				System.out.println("SplittingResults:");
				final Iterator<String> stringIterator = results.left.iterator();
				final Iterator<Integer> integerIterator = results.right
						.iterator();

				while ( stringIterator.hasNext()
						&& integerIterator.hasNext() ) {
					System.out.println("[" + stringIterator.next() + ","
							+ integerIterator.next() + "]");
				}
			}

			private void checkLemmatization() {
				final TextAnalyzer ta = new TextAnalyzer();
				final String inputSentence = "The entities were overwhelming and ran behind the cats and dogs";
				final String[] tokenStrings = inputSentence.split(" ");
				final String[] partsOfSpeech = ta.posTagger.tag(tokenStrings);

				String tokenStem;
				for (int i = 0; i < tokenStrings.length; i++) {
					final String tokenString = tokenStrings[i];
					if ( TextAnalyzer.openNLPToLVGMap
							.containsKey(partsOfSpeech[i]) ) {
						tokenStem = ta.stemmer.lemmatize(tokenString,
								TextAnalyzer.openNLPToLVGMap
										.get(partsOfSpeech[i]));
					}
					else {
						tokenStem = tokenString;
					}
					System.out
							.println("IN:" + tokenString + " OUT:" + tokenStem);
				}
			}

			private void checkSentences() {
				final List<String> sentences = new ArrayList<>();

				{

					/*
					 * "calculus" should NOT be tagged as an
					 * 'anatomicalkidneyfunction'concept because bladder is a
					 * blocker
					 */
					sentences.add(
							"The bladder calculus did not compromise kidney function.");

					/*
					 * "stones" should be tagged as an
					 * 'anatomicalkidneyfunction' concept.
					 */
					sentences.add(
							"His renal stones were compromising his ability to excrete by-products.");

					/* Nothing should be tagged. */
					sentences.add(
							"Her cystic ovaries were what was seen on the images and there were no signs of kidney dysfunction.");

					/*
					 * "atrophic" should be tagged as an
					 * 'anatomicalkidneyfunction' concept.
					 */
					sentences.add(
							"Both her kidney and adrenal gland were atrophic leading to all the problems.");

					/*
					 * "difficulty eating", "restrict fluids", and
					 * "appetite increase" should all be tagged as "intake".
					 */
					sentences.add(
							"The patient did not have difficulty eating, so we decided to restrict fluids and monitor whether his appetite increased.");

					/*
					 * "place an icd" should be tagged as a 'contrastexposure'
					 * concept.
					 */
					sentences.add(
							"The nurse will place an icd line so that contrast can be given during the procedure.");

					/*
					 * "aortofemoral bypass" should be tagged as a
					 * 'potentialcontrastexposure' concept.
					 */
					sentences.add(
							"The aortofemoral bypass was done both with and without contrast.");

					/*
					 * "aortofemoral bypass" should be tagged as
					 * a'potentialcontrastexposure' concept.
					 */
					sentences.add(
							"We decided to go ahead with the aortofemoral bypass, even though the surgeon was not present and the mri image was to be a noncontrast procedure.");

					/*
					 * "cancer" should be marked as an
					 * 'anatomicalkidneyfunction' concept.
					 */
					sentences.add(
							"The surgery CT did not show any cancer left, but the CT taken after kidney surgery did.");
				}

				final TextAnalyzer ta = new TextAnalyzer();

				/*
				 * Change dictionaryPath below to switch from work PC to home
				 * Mac
				 */
				// String dictionaryPath =
				// "/Users/glenn/SubversionMatheny/branches/GlennWorkspace/Resources/FaciliatorBlockerExamples/ConceptDictionaries/dictionary.txt";
				final String dictionaryPath = "C:\\Users\\gobbelgt\\SubversionMatheny\\branches\\GlennWorkspace\\Resources\\FaciliatorBlockerExamples\\ConceptDictionaries\\dictionary.txt";

				/*
				 * This method is better described as generating a context
				 * analyzer
				 */
				TextAnalyzer.LOGGER.debug("Generating context analyzer");
				ta.createDictionary(dictionaryPath, null);
				// ta.generalTokenContextAnalyzer.getGenContextBoundaryMarker().printPhraseTrees();

				// String str = "He is drug free.";
				// str =
				// "Both her kidney stone is leading appetite to all the
				// problems.";
				// str =
				// "The surgery CT did not show any cancer left, but the CT
				// taken after kidney surgery did.";
				int i = 0;
				for (final String curSentence : sentences) {
					final RaptatDocument doc = ta.processText(curSentence,
							Optional.empty());

					final List<AnnotatedPhrase> phrases = doc
							.getActiveSentences();
					final List<RaptatToken> sentenceTokens = phrases.get(0)
							.getProcessedTokens();

					ta.facilitatorBlockerDictionary
							.setTokenConcepts(sentenceTokens);

					final AnnotatedPhrase sentence = phrases.get(0);

					final List<AnnotatedPhrase> annotations = ta.facilitatorBlockerDictionary
							.getSentenceAnnotations(sentence);

					// List<AnnotatedPhrase> annotations =
					// ta.generalTokenContextAnalyzer
					// .getTokenConcepts( phrases.get( 0 ) );

					System.out.println(
							"\n\n\n\n\n\n---------------------------------------------------"
									+ "--------------------------------------------");
					System.out.println("Sentence " + ++i + ") " + curSentence);
					System.out.println(
							"-----------------------------------------------------------------"
									+ "------------------------------\n");
					for (final RaptatToken curToken : sentenceTokens) {
						System.out.print("String:");
						System.out
								.println(curToken.getTokenStringPreprocessed());

						System.out.print("Concept:");
						for (final String s : curToken.getAssignedConcepts()) {
							System.out.print("\u001B[37;46m" + s + "\u001B[0m");
						}

						System.out.print("\nFacilitated:");
						for (final String s : curToken.getFacilitated()) {
							System.out.print(
									" " + "\u001B[32m" + s + "\u001B[0m");
						}

						System.out.print("\nBlocked:");
						for (final String s : curToken.getBlocked()) {
							System.out.print(
									" " + "\u001B[31m" + s + "\u001B[0m");
						}

						System.out.print("\n ---\n");
					}

					int j = 0;
					for (final AnnotatedPhrase curPhrase : annotations) {
						System.out
								.println("Annotation " + j++ + ":" + curPhrase);

					}
				}
			}
		});
	}

}
