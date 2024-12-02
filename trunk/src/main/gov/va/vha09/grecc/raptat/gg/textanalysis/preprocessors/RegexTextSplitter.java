package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

public class RegexTextSplitter {
  /*
   * Convenience class for return set of information from a call to detectSentenceSpans()
   */
  public class SentenceSpanObject {
    /*
     * Maps a string used for identifying sections to the spans that contain the matched string
     */
    public final Map<Span, Set<String>> matchedSections;

    /*
     * Maps a string used for identifying sentences to the spans that contain the matched string
     */
    public final Map<Span, Set<String>> matchedSentences;

    /* The span of the sentences identified */
    public final Span[] sentenceSpans;


    /* Constructor used for returning an empty object */
    private SentenceSpanObject() {
      this.sentenceSpans = new Span[0];
      this.matchedSections = new HashMap<>();
      this.matchedSentences = new HashMap<>();
    }


    /**
     * @param sentenceSpans
     * @param sectionSpanMatches
     * @param sentenceSpanMatches
     */
    private SentenceSpanObject(Span[] sentenceSpans, Map<Span, Set<String>> sentenceSpanMatches,
        Map<Span, Set<String>> sectionSpanMatches) {
      this.sentenceSpans = sentenceSpans;
      this.matchedSentences = sentenceSpanMatches;
      this.matchedSections = sectionSpanMatches;
    }
  }

  private static final Pattern lineSplitPattern =
      Pattern.compile("(?:\r\n)|(?:\r)|(?:\n)|(?:$)", Pattern.MULTILINE);

  private static double log2 = Math.log(2.0);

  private static Logger logger = Logger.getLogger(RegexTextSplitter.class);

  private static double logMax = Math.log(RaptatConstants.MAX_SENTENCE_TOKENS);

  private static Pattern nonWhiteSpacePattern = Pattern.compile("\\S+", Pattern.MULTILINE);

  private static List<RaptatPair<String, Matcher>> sectionMatchers = new ArrayList<>();

  private static Pattern semicolonDividerPattern =
      Pattern.compile(";(?=(?:\\s+\\w+)|(?:\\s*$))", Pattern.MULTILINE);

  private static List<RaptatPair<String, Matcher>> sentenceMatchers = new ArrayList<>();

  /*
   * This pattern finds sentence terminators including periods, exclamation points, and question
   * marks indicating the end of a sentence. The specification is that if the character is a true
   * character, it will be followed by another capitalized sentence or a number-period combo (e.g.
   * '1.' or '2.') It tries to exclude "pseudo-terminators" in common acronyms such as 'Dr.' and
   * 'Mr.' and 'Pt.' and 'Vs.' and 'Appt.' and 'St.'.
   */
  private static Pattern sentenceTerminatorPattern = Pattern.compile(
      "(?<!(?:^|\\s)(?:[Mm][Rr]|[Dd][Rr]|[Mm][Rr][Ss]|[Mm][Ss]|[Pp][Tt]|[Vv][Ss]|[Aa][Pp][Pp][Tt]|[Ss][Tt]|[A-Z1-9]))"
          + "[.?!](?=(?:\\s+[A-Za-z\\({\\[])|(?:\\s*$)|(?:\\s+[1-9]\\.))",
      Pattern.MULTILINE);

  private static Comparator<Span> spanComparator = new Comparator<Span>() {
    @Override
    public int compare(Span span1, Span span2) {
      if (span1.getStart() > span2.getStart()) {
        return 1;
      }
      if (span1.getStart() == span2.getStart()) {
        if (span1.getEnd() > span2.getEnd()) {
          return 1;
        }
        if (span1.getEnd() < span2.getEnd()) {
          return -1;
        }
        return 0;
      }
      return -1;
    }
  };

  /**
   * ******************************************************** Start by capturing "sections" divided
   * by 2 or more line breaks ********************************************************
   */
  private static Pattern startPattern = Pattern.compile("((?:\r\n)|(?:\r)|(?:\n))\\s*\\1+");

  private static Pattern[] thePatterns;

  private static Tokenizer tokenizer = TokenizerFactory.getInstance();

  /*
   * Set this up with regexes so that we can add additional ones at a later point to improve
   * sentence splitting.
   */
  static {
    String[] regexStrings = {
        /**
         * ******************************************************** 0. Capture 3 or more equals
         * signs in a row ********************************************************
         */
        "[ \t]*={3,}[ \t]*",

        /**
         * ******************************************************** 1. Capture 3 or more hyphens in
         * a row ********************************************************
         */
        "[ \t]*-{3,}[ \t]*",

        /**
         * ******************************************************** 2. Capture 3 or more asterisks
         * signs in a row ********************************************************
         */
        "[ \t]*\\*{3,}[ \t]",

        /**
         * ******************************************************** 3. Cap word followed by 2 or
         * more indented, numbered lines with matching indents and number patterns
         * ********************************************************
         */
        "^[A-Z]\\S{2,}.*?(?:\r\n?|\n)(?:([ \t]+\\(*)\\d{1,2}([.)])[ \t]+\\S.*?(?:\r\n?|\n))"
            + "(?:^\\1\\d{1,2}\\2[ \t]+((\\S.*?(?:\r\n?|\n))|(\\S.*)))+",

        /**
         * ******************************************************** 4. Word all caps followed by 1
         * or more hyphen or starred lines with matching hyphens or asterisks (spacing before hyphen
         * or star may vary) ********************************************************
         */
        "^[A-Z]{2,}.*?(?:\r\n?|\n)^[ \t]*([-*]+[ \t]*)\\S+.*(?:\r\n?|\n)(?:^[ \t]*\\1\\S+.*(?:\r\n?|\n)*)*",

        /**
         * ******************************************************** 5. Cap start line followed by
         * offset Roman numerals ********************************************************
         */
        "^[A-Z]\\S{2,}.*?(?:\r\n?|\n)"
            + "(?:([ \t]+\\(*)X{0,3}(?:IX|IV|V?I{0,3})([.)])[ \t]+\\S.*?(?:\r\n?|\n))"
            + "(?:^\\1X{0,3}(?:IX|IV|V?I{0,3})\\2[ \t](?:(?:\\S.*?(?:\r\n?|\n))|(?:\\S.*)))+",

        /**
         * ******************************************************** 6. Word all caps followed by
         * lines starting with multiple asterisks or hyphens
         * ********************************************************
         */
        "^[A-Z]{2,}.*(?:\r\n?|\n)^((?:[-*]+)[ \t]*)\\S+.*?((?:\r\n?|\n))"
            + "(?:^\\1(?:(?:\\S.*?\\2)|(?:\\S.*)))+",

        /**
         * ******************************************************** 7. Capitalized word or words
         * followed by colon followed by 2 or more lines of capitalized words or numbers (at least
         * 2) with similar indentation ********************************************************
         */
        "^[ \t]*[A-Z](?:[ \t]*[A-Za-z0-9/])*[ \t]{0,3}:[ \t]*(?:\r\n?|\n)"
            + "(^[ \t]+)(?:[A-Z]|(?:[0-9]{2,}))\\S.*?(?:\r\n?|\n)"
            + "(\\1(?:[A-Z]|(?:[0-9]{2,}))\\S.*?(?:\r\n?|\n|$))+",

        /**
         * ******************************************************** 8. Digit followed by parenthesis
         * space or period-space followed by capitalized word followed by another
         * ********************************************************
         */
        "(^[ \t]*)\\d{1,2}([.)][ \t]+)[A-Z]\\S+.*?((?:\r\n?|\n))"
            + "(?:^\\1\\d{1,2}\\2(?:[A-Z]\\S+.*?\\3)|(?:[A-Z]\\S.*?))+",

        /**
         * ******************************************************** 9. Roman numeral followed by
         * parenthesis space or period-space followed by capitalized word followed by another
         * ********************************************************
         */
        "(?:([ \t]+\\(*)X{0,3}(?:IX|IV|V?I{0,3})([.)])[ \t]+[A-Z]\\S.*?(?:\r\n?|\n))"
            + "(?:^\\1X{0,3}(?:IX|IV|V?I{0,3})\\2[ \t](?:(?:[A-Z]\\S.*?(?:\r\n?|\n))|(?:[A-Z]\\S.*)))+",

        /**
         * ******************************************************** 10. Word all caps followed by 2
         * or more indented lines that are capitalized and are not preceded by a hyphen or asterisk.
         * ********************************************************
         */
        "^[A-Z]{2,}.*?(?:\r\n?|\n)^([ \t]+(?![-*]))[A-Z]\\S+.*((?:\r\n?|\n))"
            + "(?:^\\1(?:[A-Z]\\S+.*(?:\\2|$)))+",

        /**
         * ******************************************************** 11. Indented first word all caps
         * followed by 2 or more indented lines that are capitalized and are not preceded by a
         * hyphen or asterisk. ********************************************************
         */
        "^[ \t]+[A-Z]{2,}.*?(?:\r\n?|\n)^([ \t]+(?![-*]))[A-Z]\\S+.*((?:\r\n?|\n))"
            + "(?:^\\1(?:[A-Z]\\S+.*(?:\\2|$)))+",};

    RegexTextSplitter.thePatterns = new Pattern[regexStrings.length];

    for (int i = 0; i < regexStrings.length; i++) {
      RegexTextSplitter.thePatterns[i] = Pattern.compile(regexStrings[i], Pattern.MULTILINE);
    }
  }


  private final int maxSentenceLength = RaptatConstants.MAX_SENTENCE_TOKENS;

  // private static int textSplitterUsers = 0;

  public RegexTextSplitter() {
    RegexTextSplitter.logger.setLevel(Level.INFO);
  }


  public void addSectionMatcher(String matchName, String matchString) {
    Pattern matchPattern =
        Pattern.compile(matchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    RaptatPair<String, Matcher> matcherPair = new RaptatPair<>(matchName, matchPattern.matcher(""));
    RegexTextSplitter.sectionMatchers.add(matcherPair);
  }


  public void addSentenceMatcher(String matchName, String matchString) {
    Pattern matchPattern =
        Pattern.compile(matchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    RaptatPair<String, Matcher> matchPatternPair =
        new RaptatPair<>(matchName, matchPattern.matcher(""));
    RegexTextSplitter.sentenceMatchers.add(matchPatternPair);
  }


  public Map<Span, Set<String>> detectSentenceSpans(String inputText) {
    int textLength = inputText.length();
    if (inputText != null && textLength > 0) {
      Span[] sentenceSpans;
      List<Span> workingSpanList = new ArrayList<>(inputText.length() / 10);
      List<Span> startSectionSpanList = new ArrayList<>(64);

      if (RegexTextSplitter.thePatterns.length > 0) {
        Matcher sectionMatcher = RegexTextSplitter.startPattern.matcher(inputText);
        int lastMatchEnd = 0;
        int curMatchStart = 0;
        int curMatchEnd = 0;
        List<Span> subsectionSpanList;

        /*
         * This loop divides the section matcher according to the "startPattern", which, at the time
         * of this comment (5/10/19) consists of multiple blank lines. These are then dividied into
         * subsections.
         */
        while (sectionMatcher.find()) {
          /*
           * The first matched group found by the call to matcher(inputText) (see above) should hold
           * the text we are interested in
           */
          curMatchStart = sectionMatcher.start(0);
          curMatchEnd = sectionMatcher.end(0);
          startSectionSpanList.add(new Span(lastMatchEnd, curMatchStart));

          subsectionSpanList = getInitialSpans(inputText, lastMatchEnd, curMatchStart, 0);
          workingSpanList.addAll(subsectionSpanList);
          lastMatchEnd = curMatchEnd;
        }

        /*
         * Clean up the end section which does not get captured by the previous loop
         */
        if (lastMatchEnd < textLength) {
          startSectionSpanList.add(new Span(lastMatchEnd, textLength));
          subsectionSpanList = getInitialSpans(inputText, lastMatchEnd, textLength, 0);
          workingSpanList.addAll(subsectionSpanList);
        }

        /*
         * Subdivide the spans according to periods marking ends of sentences and semicolons marking
         * major clauses
         */
        workingSpanList = splitSpansWithPattern(inputText, workingSpanList,
            RegexTextSplitter.sentenceTerminatorPattern);
        workingSpanList = splitSpansWithPattern(inputText, workingSpanList,
            RegexTextSplitter.semicolonDividerPattern);

        sentenceSpans = workingSpanList.toArray(new Span[0]);
      }

      /*
       * The else part of the code is un-reachable if patterns are already defined and
       * pattern.length is greater than 0
       *
       * Sorting is not relevant here
       */
      else {
        sentenceSpans = new Span[1];
        sentenceSpans[0] = new Span(0, inputText.length());
      }

      sentenceSpans = limitSpanLength(sentenceSpans, inputText);
      Arrays.sort(sentenceSpans);

      Span[] sectionSpanArray = startSectionSpanList.toArray(new Span[0]);
      Map<Span, Set<String>> sectionSpanMatches =
          getPatternMatches(inputText, sectionSpanArray, RegexTextSplitter.sectionMatchers);
      Map<Span, Set<String>> sentenceSpanMatches =
          getPatternMatches(inputText, sentenceSpans, RegexTextSplitter.sentenceMatchers);

      mergeSectionsWithSentences(sectionSpanMatches, sentenceSpanMatches, sentenceSpans);

      return sentenceSpanMatches;
    }

    return new HashMap<>();
  }


  /**
   * @param inputText - Original text containing subsection to be matched
   * @param startOffset - Beginning offset of subsection being matched within inputText
   * @param endOffset - Ending offset of subsection being matched within inputText
   * @param regexMatcher - Matcher being matched to subtext
   * @param patternIndex - Index of match pattern in the Pattern [], thePatterns
   * @param spanList - Existing spans, will be modified if new spans found
   * @return Index of end offset of last matching by regexMatcher (with respect to inputText)
   */
  private int addPatternMatchingSpansToList(String inputText, final int startOffset,
      final int endOffset, Matcher regexMatcher, final int patternIndex, List<Span> spanList) {
    int lastMatchOffset = startOffset;

    while (regexMatcher.find()) {
      int curMatchStart = startOffset + regexMatcher.start();
      int curMatchEnd = startOffset + regexMatcher.end();

      spanList.add(new Span(curMatchStart, curMatchEnd));

      if (curMatchStart > lastMatchOffset) {
        spanList
            .addAll(getInitialSpans(inputText, lastMatchOffset, curMatchStart, patternIndex + 1));
      }

      lastMatchOffset = curMatchEnd;
    }

    return lastMatchOffset;
  }


  /**
   * Takes a list of spans and determines the number of tokens they contain based on the text
   * parameter. Any spans that have less than maxLength tokens are left unchanged. Ones whose length
   * is greater than maxLength are subdivided so they meet the criteria.
   *
   * @param spans
   * @param text
   * @param maxLength
   * @return
   */
  private List<Span> adjustLength(List<Span> spans, String text) {
    ArrayList<Span> resultSpans = new ArrayList<>();
    String[] spanStrings = Span.spansToStrings(spans.toArray(new Span[0]), text);

    int i = 0;
    for (Span curSpan : spans) {
      Span[] tokenSpans = RegexTextSplitter.tokenizer.tokenizePos(spanStrings[i]);
      if (tokenSpans.length <= this.maxSentenceLength) {
        resultSpans.add(curSpan);
      } else {
        List<Span> splitSpans =
            splitSpanToMaxLength(spanStrings[i], curSpan.getStart(), tokenSpans);
        resultSpans.addAll(splitSpans);
      }
      i++;
    }
    return resultSpans;
  }


  /**
   * Identifies and returns subsections within a text section as a list of spans
   *
   * @param inputText
   * @param startOffset
   * @param endOffset
   * @param patternIndex
   * @return - list of spans of subsections within a section of text
   */
  private List<Span> getInitialSpans(String inputText, final int startOffset, final int endOffset,
      final int patternIndex) {
    // REGION: Logging Region
    if (RegexTextSplitter.logger.getLevel() == Level.DEBUG) {
      System.out.println("\n========== GETTING SPANS ===========");
      System.out.println("Start:End - " + startOffset + ":" + endOffset);
      System.out.print("=== Analyzing pattern index:" + patternIndex + " ===\n");
      if (patternIndex < RegexTextSplitter.thePatterns.length) {
        System.out.println("Pattern Regex:"
            + StringEscapeUtils.escapeJava(RegexTextSplitter.thePatterns[patternIndex].pattern()));
      } else {
        System.out.println("PatternIndex exceeds number of patterns");
      }
    }
    // ENDREGION

    int inLength = endOffset - startOffset;
    List<Span> spanList = new ArrayList<>(inLength / 10);
    String subText = inputText.substring(startOffset, endOffset);

    if (inLength > 0 && !isAllWhitespace(subText)) {
      /*
       * Do not do any further splitting of short sequences
       */
      if (inLength < 10) {
        RegexTextSplitter.logger
            .debug("\nShort sequence (<10 chars) '" + subText + "' added to spanList");
        spanList.add(new Span(startOffset, endOffset));
        return spanList;
      }
      if (patternIndex < RegexTextSplitter.thePatterns.length) {
        try {
          Matcher regexMatcher = RegexTextSplitter.thePatterns[patternIndex].matcher(subText);
          int lastMatchOffset = addPatternMatchingSpansToList(inputText, startOffset, endOffset,
              regexMatcher, patternIndex, spanList);

          /*
           * After getting all matches to the current pattern, if the last match did not come at the
           * very end of the entire input text, then search the remaining part of the input text
           * using the next pattern
           */
          if (lastMatchOffset < endOffset) {
            RegexTextSplitter.logger.debug(
                "\nLastMatchOffset is less than endOffset -- Checking other pattern matches in unmatched part of string ("
                    + startOffset + ":" + endOffset + "\n");
            spanList
                .addAll(getInitialSpans(inputText, lastMatchOffset, endOffset, patternIndex + 1));
          }
        }
        // REGION: Catch Block
        catch (PatternSyntaxException pse) {
          RegexTextSplitter.logger.info("Regular expression problem during text splitting");
          RegexTextSplitter.logger.info("Text is: " + inputText);
          RegexTextSplitter.logger.info("Pattern in question is: " + pse.getPattern());
          RegexTextSplitter.logger.info("\tDescription: " + pse.getDescription());
          RegexTextSplitter.logger.info("\tMessage:  " + pse.getMessage());
          RegexTextSplitter.logger.info("\tIndex: " + pse.getIndex());
        }
        // ENDREGION
      } else {
        RegexTextSplitter.logger
            .debug("\nPattern index exceeds number of patterns - adding remaining span "
                + startOffset + "-" + endOffset + " to spanList \n");
        spanList.add(new Span(startOffset, endOffset));
      }
    }

    return spanList;
  }


  /**
   * Identifies spans of text that match one among a list of patterns, where the list contains a
   * pair, the left a string and the right a Pattern, where the Pattern is the Pattern object
   * compiled from the string. Returns a map where the key specifies the matched span and the value
   * is a set of the names of matched patterns for that span.
   *
   * @return
   */
  private Map<Span, Set<String>> getPatternMatches(String inputText, Span[] spans,
      List<RaptatPair<String, Matcher>> matcherPairList) {

    String[] stringsToMatch = Span.spansToStrings(spans, inputText);
    Map<Span, Set<String>> resultMap = new HashMap<>();

    for (int i = 0; i < spans.length; i++) {
      Set<String> matchedPatternNames;
      for (RaptatPair<String, Matcher> matcherPair : matcherPairList) {
        if (matcherPair.right.reset(stringsToMatch[i]).find()) {
          if ((matchedPatternNames = resultMap.get(spans[i])) == null) {
            matchedPatternNames = new HashSet<>();
            resultMap.put(spans[i], matchedPatternNames);
          }
          matchedPatternNames.add(matcherPair.left);
        }
      }
    }
    return resultMap;
  }


  /**
   * @param sectionSpan
   * @param sentenceSpans
   * @param offsetToPositionMap
   * @return
   */
  private List<Span> getSectionSentences(Span sectionSpan, Span[] sentenceSpans,
      Map<Integer, Integer> offsetToPositionMap) {
    List<Span> resultList = new ArrayList<>();
    int sectionStart = sectionSpan.getStart();
    int sectionEnd = sectionSpan.getEnd();

    int spanIndex = offsetToPositionMap.get(sectionStart);
    while (spanIndex < sentenceSpans.length && sentenceSpans[spanIndex].getEnd() <= sectionEnd) {
      resultList.add(sentenceSpans[spanIndex]);
      spanIndex++;
    }
    return resultList;
  }


  private List<Span> getSpans(Matcher splitterMatcher, int startOffset) {
    int endOffset = startOffset;
    List<Span> resultSpans = new ArrayList<>();
    int foundRegionStart = startOffset;
    while (splitterMatcher.find()) {
      endOffset = startOffset + splitterMatcher.end();
      resultSpans.add(new Span(foundRegionStart, endOffset));
      foundRegionStart = endOffset;
    }

    int matchedTextLength = splitterMatcher.regionEnd();
    if (endOffset - startOffset < matchedTextLength) {
      endOffset = startOffset + splitterMatcher.regionEnd();
      resultSpans.add(new Span(foundRegionStart, endOffset));
    }

    return resultSpans;
  }


  private boolean isAllWhitespace(String inString) {
    Matcher regexMatcher = RegexTextSplitter.nonWhiteSpacePattern.matcher(inString);
    if (regexMatcher.find()) {
      return false;
    }
    return true;
  }


  /**
   * Takes an array of sentence spans corresponding to the input text. The spans are shortened by
   * dividing them up so they are no longer than maxLength.
   *
   * <p>
   * The spans are first checked to see if they have less than or equal to maxLength tokens in them
   * using a splitting pattern that splits on white space. Spans who meet this criteria are kept as
   * is. For spans that exceed this length, the method finds the subspans created by dividing them
   * up according to line splits, which is set to a regex pattern of carriage return or newline
   * characters. The spans are then checked to see if any exceed the maxLength specification. Any
   * that do are further subdivided sufficiently so that their length is less than maxLength.
   *
   * @param sentenceSpans
   * @param inputText
   * @param maxLength
   * @return
   */
  private Span[] limitSpanLength(Span[] sentenceSpans, String inputText) {
    String[] spanStrings = Span.spansToStrings(sentenceSpans, inputText);
    List<Span> resultSpans = new ArrayList<>(2 * sentenceSpans.length);

    for (int i = 0; i < sentenceSpans.length; i++) {
      String[] sentenceTokens = RegexTextSplitter.tokenizer.tokenize(spanStrings[i]);
      if (sentenceTokens.length <= this.maxSentenceLength) {
        resultSpans.add(sentenceSpans[i]);
      }

      /* Sentence too long - needs to be limited in length */
      else {
        int sentenceStartOffset = sentenceSpans[i].getStart();
        Matcher lineSplitMatcher = RegexTextSplitter.lineSplitPattern.matcher(spanStrings[i]);
        int curEndSpan = 0;

        /* Get the spans detected by the line split pattern */
        List<Span> lineSplitSpans = new ArrayList<>();
        while (lineSplitMatcher.find()) {
          int spanStart = curEndSpan + sentenceStartOffset;
          int spanEnd = lineSplitMatcher.start() + sentenceStartOffset;

          /*
           * If the next span starts with a line split set of characters, the start and end of the
           * span may be the same
           */
          if (spanEnd > spanStart) {
            lineSplitSpans.add(new Span(curEndSpan + sentenceStartOffset,
                lineSplitMatcher.start() + sentenceStartOffset));
          }
          curEndSpan = lineSplitMatcher.end();
        }

        /*
         * Check spans resulting from line split for length and subdivide further if needed,
         * returning the final subspans. This is needed in case there are longer single lines in a
         * document with no intervening return or linefeed characters.
         */
        lineSplitSpans = adjustLength(lineSplitSpans, inputText);
        resultSpans.addAll(lineSplitSpans);
      }
    }
    Collections.sort(resultSpans, RegexTextSplitter.spanComparator);
    return resultSpans.toArray(new Span[0]);
  }


  /**
   * Takes all the sentences within the matched sections and adds them to the matchedSentences map
   * unless they already exist.<br>
   *
   * @return
   */
  private void mergeSectionsWithSentences(Map<Span, Set<String>> sectionSpanMatches,
      Map<Span, Set<String>> sentenceSpanMatches, Span[] sentenceSpans) {
    /*
     * Create map from offset to position in array and a map from all sentence spans to context
     * strings
     */
    Map<Integer, Integer> offsetToPositionMap = new HashMap<>(sentenceSpans.length);
    for (int i = 0; i < sentenceSpans.length; i++) {
      Span span = sentenceSpans[i];
      offsetToPositionMap.put(span.getStart(), i);
      if (!sentenceSpanMatches.containsKey(span)) {
        sentenceSpanMatches.put(span, new HashSet<String>());
      }
    }

    /*
     * Now got through and find all sentences within each matched section and add the sentences to
     * sentence span matches if not already there
     */
    for (Span sectionSpan : sectionSpanMatches.keySet()) {
      List<Span> sectionSentences =
          getSectionSentences(sectionSpan, sentenceSpans, offsetToPositionMap);
      Set<String> sectionContextSet = sectionSpanMatches.get(sectionSpan);
      for (Span sectionSentenceSpan : sectionSentences) {
        sentenceSpanMatches.get(sectionSentenceSpan).addAll(sectionContextSet);
      }
    }
  }


  private boolean spansOverlap(Span span1, Span span2) {
    return span1.getStart() > span2.getEnd() && span1.getEnd() < span2.getStart();
  }


  /**
   * @param sentenceSpans
   * @return
   */
  private IndexedTree<IndexedObject<RaptatPair<Integer, Integer>>> spansToIndexedTree(
      Span[] sentenceSpans) {
    List<IndexedObject<RaptatPair<Integer, Integer>>> resultList =
        new ArrayList<>(sentenceSpans.length);
    for (Span curSpan : sentenceSpans) {
      int start = curSpan.getStart();
      resultList.add(new IndexedObject(start, new RaptatPair<>(start, curSpan.getEnd())));
    }

    return new IndexedTree(resultList);
  }


  private List<Span> splitSpansWithPattern(String inputText, List<Span> spans,
      Pattern splitterPattern) {
    List<Span> resultSpans = new ArrayList<>(2 * spans.size());
    String[] textSpans = Span.spansToStrings(spans.toArray(new Span[0]), inputText);
    int i = 0;
    for (Span curSpan : spans) {
      Matcher splitMatcher = splitterPattern.matcher(textSpans[i++]);
      List<Span> foundSpans = getSpans(splitMatcher, curSpan.getStart());
      resultSpans.addAll(foundSpans);
    }
    return resultSpans;
  }


  /**
   * Takes a span of text and splits it up relatively evenly so that the length of the resultant
   * spans in terms of number of tokens they correspond to in the original text is less than or
   * equal to maxLength
   *
   * @param inputSpan
   * @param maxLength
   * @return
   */
  private List<Span> splitSpanToMaxLength(String spanText, int spanStartOffset, Span[] tokenSpans) {
    ArrayList<Span> resultSpans = new ArrayList<>();
    /*
     * (length/(2^x)) must be less than maxLength, so (maxLength/length) must be less than (2^x), so
     * (log(maxLength) - log(length)) must be less than (x�log(2)), so x must be greater than
     * (log(maxLength) - log(length))/log(2). This is how we calculate the number of even splits to
     * achieve spans with token numbers of less than maxLength
     */
    double numberOfSplits = Math
        .ceil((RegexTextSplitter.logMax - Math.log(tokenSpans.length)) / RegexTextSplitter.log2);
    int splitSize = (int) Math.round(Math.ceil(Math.pow(2.0, numberOfSplits)));

    int startToken = 0;
    int endToken = splitSize - 1;
    while (startToken < tokenSpans.length) {
      int startOffset = tokenSpans[startToken].getStart() + spanStartOffset;
      int endOffset = tokenSpans[endToken].getEnd() + spanStartOffset;
      resultSpans.add(new Span(startOffset, endOffset));
      startToken = endToken + 1;
      endToken += splitSize;
      if (endToken > tokenSpans.length - 1) {
        endToken = tokenSpans.length - 1;
      }
    }
    return resultSpans;
  }


  public static void main(String[] args) {
    String[] fileTypes = {"txt", "csv"};
    RegexTextSplitter ts = new RegexTextSplitter();
    ts.addSectionMatcher("allergy", "^\\s*ALLERG.*");
    ts.addSentenceMatcher("allergy", "^\\s*ALLERG.*");

    File textFileDirectory =
        new File("D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\TrainingCorpusXMLForCrossValidation");
    Collection<File> textFiles = FileUtils.listFiles(textFileDirectory, fileTypes, true);

    try {
      for (File inputFile : textFiles) {
        System.out.println("Processing File:" + inputFile.getName());
        String inputText = FileUtils.readFileToString(inputFile, "UTF-8");
        Map<Span, Set<String>> spanObject = ts.detectSentenceSpans(inputText);
        Span[] sentenceSpans = spanObject.keySet().toArray(new Span[0]);
        for (Span curSpan : sentenceSpans) {
          Set<String> matchedPatternNames = spanObject.get(curSpan);
          int i = 1;
          for (String patternName : matchedPatternNames) {
            if (patternName.contains("allergy")) {
              System.out.println("\n\nSentencePattern " + patternName + " " + i++ + ":\n"
                  + curSpan.getCoveredText(inputText) + "\n");
            }
          }
        }
      }

    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
