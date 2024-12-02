package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import opennlp.tools.postag.POSTagger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapCalculator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.attributemapping.AttributeEvaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.ConceptMapBootstrapSampler;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.FilterType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.lexicaltools.Lemmatiser;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.LemmatiserFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.POSTaggerFactory;

/**
 * ********************************************************** NBEvaluator -
 *
 * @author Glenn Gobbel, Sep 18, 2010 Modified Jun 20, 2011
 *         *********************************************************
 */
/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Jun 17, 2012 *****************************************************
 */
public class NBEvaluator extends ConceptMapEvaluator {
  protected static Logger logger = Logger.getLogger(NBEvaluator.class);
  protected ConceptMapCalculator conceptCalculator;
  protected AttributeEvaluator attributeEvaluator;
  protected FilterType filter;
  protected boolean countNulls = RaptatConstants.USE_NULLS_START;
  protected boolean convertTokensToStems = RaptatConstants.USE_TOKEN_STEMMING;
  protected boolean addPOSToTokens = RaptatConstants.USE_PARTS_OF_SPEECH;
  protected boolean invertTokenSequence = RaptatConstants.INVERT_TOKEN_SEQUENCE;
  protected boolean removeStopWords = RaptatConstants.REMOVE_STOP_WORDS;
  protected int maxElements = RaptatConstants.MAX_TOKENS_DEFAULT;
  protected POSTagger posTagger = null;
  protected Lemmatiser stemmer = null;

  // Makes sure list is synchronized (see Java 1.5 LinkedList documents)
  protected List<String[]> compiledResults =
      Collections.synchronizedList(new LinkedList<String[]>());


  /**
   * *************************************************************** Constructor for an object that
   * will calculate and use the likelihood of a database for a naive Bayes network
   * ***************************************************************
   */
  public NBEvaluator() {
    super();
    NBEvaluator.logger.setLevel(Level.INFO);
  }


  public NBEvaluator(ConceptMapSolution nbSolution, OptionsManager nbOptions) {
    this(nbOptions);
    this.conceptCalculator = new NBCalculator((NBSolution) nbSolution, this.filter);
    this.attributeEvaluator = new AttributeEvaluator(nbSolution);
    this.maxElements = ((NBSolution) nbSolution).getNumOfTokens();
  }


  /**
   * ******************************************** This is a constructor used by an AnnotationFinder
   * instance which handles all options such as stemming and POS, so these don't need to be set as
   * for other constructors, and not stemming or pos tagging is needed.
   *
   * @param nbSolution
   * @param inFilter
   * @author Glenn Gobbel - Jun 17, 2012 ********************************************
   */
  public NBEvaluator(ConceptMapSolution nbSolution, String inFilter) {
    this();
    setFiltering(inFilter);
    this.conceptCalculator = new NBCalculator((NBSolution) nbSolution, this.filter);
    this.attributeEvaluator = new AttributeEvaluator(nbSolution);
    this.maxElements = ((NBSolution) nbSolution).getNumOfTokens();
  }


  public NBEvaluator(OptionsManager nbOptions) {
    this();
    setFiltering(nbOptions.getCurFilter());
    this.countNulls = nbOptions.getUseNulls();
    this.convertTokensToStems = nbOptions.getUseStems();
    this.addPOSToTokens = nbOptions.getUsePOS();
    this.invertTokenSequence = nbOptions.getInvertTokenSequence();
    this.removeStopWords = nbOptions.getRemoveStopWords();
    if (this.addPOSToTokens) {
      this.posTagger = POSTaggerFactory.getInstance();
    }
    if (this.convertTokensToStems || this.removeStopWords) {
      this.stemmer = LemmatiserFactory.getInstance(this.removeStopWords);
    }
  }


  @Override
  public void close() {
    // if ( posTagger != null )
    // {
    // POSTaggerFactory.release();
    // }
    // if ( stemmer != null )
    // {
    // LemmatiserFactory.release( stemmer );
    // }
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Evaluator# evaluateSolution
   * (src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap.AnnotationSet)
   */
  @Override
  public List<String[]> evaluateSolution(ConceptMapBootstrapSampler testData)
      throws AttributeDBMismatchException {
    this.evaluateSolution(testData.getAnnotationData(), testData.myReader.getStartTokensPosition());
    return this.compiledResults;
  }


  /**
   * ************************************************************** Test the Naive Bayes network by
   * seeing how well it maps test data to already mapped concepts
   *
   * @throws AttributeDBMismathException
   *         **************************************************************
   */
  @Override
  public List<String[]> evaluateSolution(DataImportReader testDataReader)
      throws AttributeDBMismatchException {
    Vector<Long> durationList = null;

    if (this.countNulls) {
      durationList = this.getMatchesUseNulls(testDataReader);
    } else {
      durationList = this.getMatches(testDataReader);
    }

    GeneralHelper.errorWriter("Completed evaluation: writing track duration");
    if (durationList != null) {
      String evalTimeFileName = testDataReader.getFileDirectory() + "EvaluationTime_"
          + GeneralHelper.getTimeStamp() + ".csv";
      GeneralHelper.writeVectorAtInterval(evalTimeFileName, durationList,
          RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    return this.compiledResults;
  }


  /**
   * ************************************************************** Test the Naive Bayes network by
   * seeing how well it maps test data to already mapped concepts
   *
   * @param testDataSource - a List<String[]> parameter holding the data to be evaluated. There can
   *        be any number of elements before the location of the first token or attribute in the
   *        data. The last element contains a concept.
   * @param startTokenPosition - position within each string array in the list where the attributes
   *        (tokens) of data start using zero indexing
   * @throws AttributeDBMismathException
   *         **************************************************************
   */
  @Override
  public List<String[]> evaluateSolution(List<String[]> testDataSource, int startTokensPosition)
      throws AttributeDBMismatchException {
    // Check to make sure we have at least one attribute (word) for testing
    // We assume all string arrays in the list have the same number of
    // elements.
    if (testDataSource.size() > 0 && testDataSource.get(0).length - startTokensPosition > 1) {
      Vector<Long> durationList = null;

      if (this.countNulls) {
        durationList = this.getMatchesUseNulls(testDataSource, startTokensPosition);
      } else {
        durationList = this.getMatches(testDataSource, startTokensPosition);
      }

      // if ( durationList != null )
      // {
      // GeneralHelper.errorWriter("Completed evaluation");
      // GeneralHelper.errorWriter("Writing track duration");
      // String evalTimeFileName = "EvaluationTime_" +
      // GeneralHelper.getDateTime() + ".csv";
      // GeneralHelper.writeVectorAtInterval(evalTimeFileName,
      // durationList, PHRASE_REPORT_INTERVAL);
      // }
    }
    return this.compiledResults;
  }


  /**
   * ***********************************************************
   *
   * @param raptatSequences
   * @return
   * @author Glenn Gobbel - Jun 16, 2012 ***********************************************************
   */
  @Override
  public List<RaptatPair<List<RaptatToken>, String>> getConcepts(
      List<List<RaptatToken>> theSequences, HashSet<String> acceptableConcepts,
      boolean includeNulls) {
    List<RaptatPair<List<RaptatToken>, String>> theResults = new ArrayList<>();
    String[] tokensAsStringArray;
    String theConcept;
    try {
      /*
       * We use an iterator so we can quickly remove a sequence that doesn't map to anything with
       * O(1) complexity as theSequences should be a linked list.
       */
      Iterator<List<RaptatToken>> sequenceIterator = theSequences.iterator();
      while (sequenceIterator.hasNext()) {
        List<RaptatToken> curSequence = sequenceIterator.next();
        tokensAsStringArray = getAugmentedStringsAsArray(curSequence);
        theConcept =
            this.conceptCalculator.calcBestConcept(tokensAsStringArray, acceptableConcepts)[0];
        NBEvaluator.logger.debug(Arrays.toString(curSequence.toArray()) + " MAPS TO:" + theConcept);
        if (!theConcept.equals(RaptatConstants.NULL_ELEMENT) || includeNulls) {
          theResults.add(new RaptatPair<>(curSequence, theConcept));
        } else {
          sequenceIterator.remove();
        }
      }
    } catch (Exception e) {
      GeneralHelper
          .errorWriter("Exception" + e + "\nIncorrect attribute" + "length for concept matching");
    }
    return theResults;
  }


  @Override
  public List<List<RaptatAttribute>> getMappedAttributes(List<String[]> phrases,
      List<String> concepts) {
    List<List<RaptatAttribute>> resultAttributes =
        new ArrayList<>(phrases.size() * RaptatConstants.MAX_EXPECTED_ATTRIBUTES_PER_CONCEPT);
    Iterator<String[]> phraseIterator = phrases.iterator();
    Iterator<String> conceptIterator = concepts.iterator();
    while (phraseIterator.hasNext()) {
      String[] phraseStrings = phraseIterator.next();
      String concept = conceptIterator.next();
      List<RaptatAttribute> annotationAttributes =
          this.attributeEvaluator.getAttributes(phraseStrings, concept);
      resultAttributes.add(annotationAttributes);
    }

    return resultAttributes;
  }


  @Override
  public List<String[]> mapSolution(DataImportReader testDataReader) {
    List<String[]> resultList = new ArrayList<>(1000);
    String[] curData, phraseTokens, preProcessedData, posTags;
    int startTokensPosition = testDataReader.getStartTokensPosition();
    int lineRead = 0;

    try {
      while ((curData = testDataReader.getNextData()) != null) {
        if (curData.length > 1) {
          // Get the current data string up until we reach a value
          // of NULL_ELEMENT or up until we come to the concept value
          // column
          int i = startTokensPosition;
          while (i < curData.length - 1 && !curData[i].equals(RaptatConstants.NULL_ELEMENT)) {
            i++;
          }
          // We'll use elements from the startTokensPosition and the
          // first null to calculate best concept
          phraseTokens =
              GeneralHelper.getSubArray(curData, startTokensPosition, i - startTokensPosition);

          if (phraseTokens != null && phraseTokens.length > 0) {
            phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);

            // This automatically handles removeStopWords
            if (this.convertTokensToStems) {
              // This will convert data to lower case and remove
              // stop words if set to do so during initialization
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

              if (this.addPOSToTokens) {
                // Find out which POS tags to keep when we get
                // them as some stop words may have been
                // removed, but we want to do POS tagging on
                // complete phrase
                int[] keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                posTags = this.posTagger.tag(phraseTokens);
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
                GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
              } else {
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
              }

            } else {
              if (this.removeStopWords) {
                if (this.addPOSToTokens) {
                  // Find out which POS tags to keep when we
                  // get them as some stop words may have been
                  // removed, but we want to do POS tagging on
                  // complete phrase

                  // This will convert data to lower case and
                  // remove stop words if set to do so during
                  // initialization
                  preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                  int[] keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                  posTags = this.posTagger.tag(phraseTokens);
                  phraseTokens = preProcessedData;
                  GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
                } else {
                  // This will convert data to lower case and
                  // remove stop words if set to do so during
                  // initialization
                  phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                }
              } else
              // (if !convertTokensToStems &&
              // !removeStopWords )
              {
                if (this.addPOSToTokens) {
                  posTags = this.posTagger.tag(phraseTokens);
                  GeneralHelper.concatenateArrays(phraseTokens, posTags, "_");
                }
              }
            }

            if (this.invertTokenSequence) {
              ArrayUtils.reverse(phraseTokens);
            }

            // System.out.println(curPhrase + ": " +
            // Arrays.toString(testData));
            String[] evaluationResults =
                this.conceptCalculator.calcBestConcept(phraseTokens, this.acceptedConcepts);

            NBEvaluator.logger.debug(
                ++lineRead + ") " + evaluationResults[0] + ":" + Arrays.toString(phraseTokens));;

            resultList.add(ArrayUtils.addAll(evaluationResults, phraseTokens));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return resultList;
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Evaluator#reset()
   */
  @Override
  public void resetResults() {
    this.compiledResults.clear();
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.algorithms .Evaluator#setSolution(
   * src.main.gov.va.vha09.grecc.raptat.gg.algorithms.Solution)
   */
  @Override
  public void setFieldsFromSolution(ConceptMapSolution theSolution) {
    this.conceptCalculator = new NBCalculator((NBSolution) theSolution, this.filter);
    this.maxElements = ((NBSolution) theSolution).getNumOfTokens();
    this.countNulls = theSolution.getUseNulls();
    this.convertTokensToStems = theSolution.getUseStems();
    this.addPOSToTokens = theSolution.getUsePOS();
    this.invertTokenSequence = theSolution.getInvertTokenSequence();
    this.removeStopWords = theSolution.getRemoveStopWords();

    if (this.addPOSToTokens) {
      this.posTagger = POSTaggerFactory.getInstance();
    }
    if (this.convertTokensToStems || this.removeStopWords) {
      this.stemmer = LemmatiserFactory.getInstance(this.removeStopWords);
    }
  }


  @Override
  public void setMappedAttributes(List<AnnotatedPhrase> annotations) {
    for (AnnotatedPhrase curPhrase : annotations) {
      List<RaptatAttribute> curAttributes = curPhrase.getPhraseAttributes();
      String[] phraseStrings =
          curPhrase.getProcessedTokensAugmentedStrings().toArray(new String[0]);
      String phraseConcept = curPhrase.getConceptName();
      List<RaptatAttribute> additionalAttributes =
          this.attributeEvaluator.getAttributes(phraseStrings, phraseConcept);
      curAttributes.addAll(additionalAttributes);
    }
  }


  protected void setFiltering(String inFilter) {
    if (inFilter.equals("Standard")) {
      this.filter = FilterType.STANDARD;
    } else if (inFilter.equals("Smoothed")) {
      this.filter = FilterType.SMOOTHED;
    } else {
      this.filter = FilterType.SMOOTHED;
    }
  }


  /**
   * ***********************************************************
   *
   * @param theSequence
   * @return
   * @author Glenn Gobbel - Jun 16, 2012 ***********************************************************
   */
  private String[] getAugmentedStringsAsArray(List<RaptatToken> theSequence) {
    List<String> theArray = new ArrayList<>(theSequence.size());
    String curTokenString;

    for (RaptatToken curToken : theSequence) {
      // Stop words are represented by empty strings,
      // so we remove them
      curTokenString = curToken.getTokenStringAugmented();
      if (!curTokenString.equals("")) {
        theArray.add(curTokenString);
      }
    }
    return theArray.toArray(new String[0]);
  }


  private Vector<Long> getMatches(DataImportReader testDataReader)
      throws AttributeDBMismatchException {
    String[] phraseTokens, evaluationResults;
    String[][] allResults = new String[(int) testDataReader.getReaderLength()][6];
    String expectedConcept;
    int curPhrase = 0;

    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    int startTokensPosition = testDataReader.getStartTokensPosition();

    // Development - Track time to perform concept matching for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;

    if (RaptatConstants.TRACK_DURATION) {
      // Add 1 to calculated size for easy handling of uneven divisions
      durationList = new Vector<>(
          1 + (int) testDataReader.getReaderLength() / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    String[] curData = testDataReader.getNextData();

    try {
      while (curData != null) {
        if (curData.length > 1) {
          // System.out.println("Analyzing data element " +
          // dataCounter++);
          expectedConcept = curData[curData.length - 1].toLowerCase();

          // Get the current data string up until we reach a value
          // of NULL_ELEMENT or up until we come to the concept value
          // column
          int i = startTokensPosition;
          while (i < curData.length - 1 && !curData[i].equals(RaptatConstants.NULL_ELEMENT)) {
            i++;
          }
          // We'll use elements from the startTokensPosition and the
          // first null to calculate best concept
          phraseTokens =
              GeneralHelper.getSubArray(curData, startTokensPosition, i - startTokensPosition);

          if (phraseTokens != null && phraseTokens.length > 0) {
            phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);
            if (this.convertTokensToStems) // This automatically
            // handles
            // removeStopWords
            {
              // This will convert data to lower case and remove
              // stop words if set to do so during initialization
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

              if (this.addPOSToTokens) {
                // Find out which POS tags to keep when we get
                // them
                // as some stop words may have been removed, but
                // we
                // want to do POS tagging on complete phrase
                keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                posTags = this.posTagger.tag(phraseTokens);
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
                GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
              } else {
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
              }

            } else {
              if (this.removeStopWords) {
                if (this.addPOSToTokens) {
                  // Find out which POS tags to keep when we
                  // get them
                  // as some stop words may have been removed,
                  // but we
                  // want to do POS tagging on complete phrase

                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                  keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                  posTags = this.posTagger.tag(phraseTokens);
                  phraseTokens = preProcessedData;
                  GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
                } else {
                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                }
              } else
              // (if !convertTokensToStems &&
              // !removeStopWords )
              {
                if (this.addPOSToTokens) {
                  posTags = this.posTagger.tag(phraseTokens);
                  GeneralHelper.concatenateArrays(phraseTokens, posTags, "_");
                }
              }
            }

            if (this.invertTokenSequence) {
              ArrayUtils.reverse(phraseTokens);
            }

            // System.out.println(curPhrase + ": " +
            // Arrays.toString(testData));
            evaluationResults =
                this.conceptCalculator.calcBestConcept(phraseTokens, this.acceptedConcepts);

            // testResults[0] contains the most probable concept
            // testResults[1] contains the number of potential
            // concepts tested
            // testResults[2] contains the type of smoothing used
            allResults[curPhrase][0] = GeneralHelper.arrayToString(phraseTokens);
            allResults[curPhrase][1] = Integer.toString(phraseTokens.length);
            allResults[curPhrase][2] = expectedConcept;
            allResults[curPhrase][3] = evaluationResults[0];
            allResults[curPhrase][4] = evaluationResults[1];
            allResults[curPhrase][5] = evaluationResults[2];
            this.compiledResults.add(allResults[curPhrase]);
            curPhrase++;

            if (RaptatConstants.TRACK_DURATION
                && curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
              // For development purposes only
              // curTime = System.currentTimeMillis();
              // curDuration = curTime - startTime;
              // System.out.println(curPhrase + "\t" +
              // curDuration);
              durationList.add(System.currentTimeMillis() - startTime);
            }
          }
        }
        curData = testDataReader.getNextData();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return durationList;
  }


  private Vector<Long> getMatches(List<String[]> testDataSource, int startTokensPosition)
      throws AttributeDBMismatchException {
    String[] phraseTokens, evaluationResults;
    String expectedConcept;
    int curPhrase = 0;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    String[] curData;
    String[][] allResults = new String[testDataSource.size()][6];
    ListIterator<String[]> dataIterator = testDataSource.listIterator();

    // Development - Track time to perform concept matching for each phrase
    long startTime = System.currentTimeMillis();
    long curTime, curDuration;
    Vector<Long> durationList = null;

    if (RaptatConstants.TRACK_DURATION) {
      // Add 1 to calculated size fo easy handling of division
      durationList =
          new Vector<>(1 + testDataSource.size() / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    try {
      while (dataIterator.hasNext()) {
        curData = dataIterator.next();
        if (curData.length > 0) {
          // System.out.println( "Analyzing data element " +
          // dataCounter++ );
          expectedConcept = curData[curData.length - 1].toLowerCase();

          // Get the current data string up until we reach a value
          // of NULL_ELEMENT or up until we come to the concept value
          // column
          int i = startTokensPosition;
          while (!curData[i].equals(RaptatConstants.NULL_ELEMENT) && i < curData.length - 1) {
            i++;
          }

          // We'll use elements from the startTokensPosition and the
          // first null to calculate best concept
          phraseTokens =
              GeneralHelper.getSubArray(curData, startTokensPosition, i - startTokensPosition);

          if (phraseTokens != null && phraseTokens.length > 0) {
            phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);
            if (this.convertTokensToStems) // This automatically
            // handles
            // removeStopWords
            {
              // This will convert data to lower case and remove
              // stop words if set to do so during initialization
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

              if (this.addPOSToTokens) {
                // Find out which POS tags to keep when we get
                // them
                // as some stop words may have been removed, but
                // we
                // want to do POS tagging on complete phrase
                keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                posTags = this.posTagger.tag(phraseTokens);
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
                GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
              } else {
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
              }

            } else {
              if (this.removeStopWords) {
                if (this.addPOSToTokens) {
                  // Find out which POS tags to keep when we
                  // get them
                  // as some stop words may have been removed,
                  // but we
                  // want to do POS tagging on complete phrase

                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                  keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                  posTags = this.posTagger.tag(phraseTokens);
                  phraseTokens = preProcessedData;
                  GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
                } else {
                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                }
              } else
              // (if !convertTokensToStems &&
              // !removeStopWords )
              {
                if (this.addPOSToTokens) {
                  posTags = this.posTagger.tag(phraseTokens);
                  GeneralHelper.concatenateArrays(phraseTokens, posTags, "_");
                }
              }
            }

            if (this.invertTokenSequence) {
              ArrayUtils.reverse(phraseTokens);
            }

            evaluationResults =
                this.conceptCalculator.calcBestConcept(phraseTokens, this.acceptedConcepts);
            // System.out.println(Arrays.toString(testData));

            // testResults[0] contains the most probable concept
            // testResults[1] contains the number of potential
            // concepts tested
            // testResults[2] contains the type of smoothing used
            allResults[curPhrase][0] = GeneralHelper.arrayToString(phraseTokens);
            allResults[curPhrase][1] = Integer.toString(phraseTokens.length);
            allResults[curPhrase][2] = expectedConcept;
            allResults[curPhrase][3] = evaluationResults[0];
            allResults[curPhrase][4] = evaluationResults[1];
            allResults[curPhrase][5] = evaluationResults[2];
            this.compiledResults.add(allResults[curPhrase]);
            curPhrase++;

            if (RaptatConstants.TRACK_DURATION
                && curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
              // For development purposes only
              curTime = System.currentTimeMillis();
              curDuration = curTime - startTime;
              System.out.println(curPhrase + "\t" + curDuration);
              durationList.add(System.currentTimeMillis() - startTime);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return durationList;
  }


  private Vector<Long> getMatchesUseNulls(DataImportReader testDataReader)
      throws AttributeDBMismatchException {
    String[] phraseTokens;
    String expectedConcept;
    String[] evaluationResults;
    String[][] allResults = new String[(int) testDataReader.getReaderLength()][6];
    int curPhrase = 0;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    int startTokensPosition = testDataReader.getStartTokensPosition();

    // Development - Track time to perform concept matching for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;

    if (RaptatConstants.TRACK_DURATION) {
      // Add 1 to calculated size for easy handling of uneven divisions
      durationList = new Vector<>(
          1 + (int) testDataReader.getReaderLength() / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    String[] curData = testDataReader.getNextData();
    try {
      while (curData != null) {
        if (curData.length > 1) {
          // System.out.println("Analyzing data element " +
          // dataCounter++);
          expectedConcept = curData[curData.length - 1].toLowerCase();

          phraseTokens = GeneralHelper.getSubArray(curData, startTokensPosition,
              curData.length - startTokensPosition - 1);

          if (phraseTokens != null && phraseTokens.length > 0) {
            phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);
            if (this.convertTokensToStems) // This automatically
            // handles
            // removeStopWords
            {
              // This will convert data to lower case and remove
              // stop words if set to do so during initialization
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

              if (this.addPOSToTokens) {
                // Find out which POS tags to keep when we get
                // them as some stop words may have been
                // removed, but we want to do POS tagging on
                // complete phrase
                keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                posTags = this.posTagger.tag(phraseTokens);
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
                GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
              } else {
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
              }

            } else {
              if (this.removeStopWords) {
                if (this.addPOSToTokens) {
                  // Find out which POS tags to keep when we
                  // get them
                  // as some stop words may have been removed,
                  // but we
                  // want to do POS tagging on complete phrase

                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                  keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                  posTags = this.posTagger.tag(phraseTokens);
                  phraseTokens = preProcessedData;
                  GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
                } else {
                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                }
              } else
              // (if !convertTokensToStems &&
              // !removeStopWords )
              {
                if (this.addPOSToTokens) {
                  posTags = this.posTagger.tag(phraseTokens);
                  GeneralHelper.concatenateArrays(phraseTokens, posTags, "_");
                }
              }
            }

            if (this.invertTokenSequence) {
              ArrayUtils.reverse(phraseTokens);
            }

            evaluationResults =
                this.conceptCalculator.calcBestConcept(phraseTokens, this.acceptedConcepts);

            // testResults[0] contains the most probable concept
            // testResults[1] contains the number of potential
            // concepts tested
            // testResults[2] contains the type of smoothing used
            allResults[curPhrase][0] = GeneralHelper.arrayToString(phraseTokens);
            allResults[curPhrase][1] = Integer.toString(phraseTokens.length);
            allResults[curPhrase][2] = expectedConcept;
            allResults[curPhrase][3] = evaluationResults[0];
            allResults[curPhrase][4] = evaluationResults[1];
            allResults[curPhrase][5] = evaluationResults[2];
            this.compiledResults.add(allResults[curPhrase]);
            curPhrase++;

            if (RaptatConstants.TRACK_DURATION
                && curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
              // For development only
              // curTime = System.currentTimeMillis();
              // curDuration = curTime - startTime;
              // System.out.println(curPhrase + "\t" +
              // curDuration);
              durationList.add(System.currentTimeMillis() - startTime);
            }
          }
        }
        curData = testDataReader.getNextData();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return durationList;
  }


  private Vector<Long> getMatchesUseNulls(List<String[]> testDataSource, int startTokensPosition)
      throws AttributeDBMismatchException {
    String[] phraseTokens;
    String expectedConcept;
    String[] evaluationResults;
    String[][] allResults = new String[testDataSource.size()][6];
    int curPhrase = 0;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    String[] curData;
    ListIterator<String[]> dataIterator = testDataSource.listIterator();

    // Development - Track time to perform concept matching for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;

    if (RaptatConstants.TRACK_DURATION) {
      durationList =
          new Vector<>(1 + testDataSource.size() / RaptatConstants.PHRASE_REPORT_INTERVAL); // Add
      // 1
      // to
      // calculated
      // size
      // for
      // easy
      // handling
      // of
      // uneven
      // divisions
    }

    try {

      while (dataIterator.hasNext()) {
        curData = dataIterator.next();
        if (curData.length > 0) {
          // System.out.println( "Analyzing data element " +
          // dataCounter++ );
          expectedConcept = curData[curData.length - 1].toLowerCase();

          // We'll use elements from the startTokensPosition and
          // beyond
          // to determine the most probable concept
          phraseTokens = GeneralHelper.getSubArray(curData, startTokensPosition,
              curData.length - startTokensPosition - 1);

          if (phraseTokens != null && phraseTokens.length > 0) {
            phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);
            if (this.convertTokensToStems) // This automatically
            // handles
            // removeStopWords
            {
              // This will convert data to lower case and remove
              // stop words if set to do so during initialization
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

              if (this.addPOSToTokens) {
                // Find out which POS tags to keep when we get
                // them
                // as some stop words may have been removed, but
                // we
                // want to do POS tagging on complete phrase
                keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                posTags = this.posTagger.tag(phraseTokens);
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
                GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
              } else {
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
              }

            } else {
              if (this.removeStopWords) {
                if (this.addPOSToTokens) {
                  // Find out which POS tags to keep when we
                  // get them
                  // as some stop words may have been removed,
                  // but we
                  // want to do POS tagging on complete phrase

                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                  keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                  posTags = this.posTagger.tag(phraseTokens);
                  phraseTokens = preProcessedData;
                  GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
                } else {
                  // This will convert data to lower case and
                  // remove
                  // stop words if set to do so during
                  // initialization
                  phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                }
              } else
              // (if !convertTokensToStems &&
              // !removeStopWords )
              {
                if (this.addPOSToTokens) {
                  posTags = this.posTagger.tag(phraseTokens);
                  GeneralHelper.concatenateArrays(phraseTokens, posTags, "_");
                }
              }
            }

            if (this.invertTokenSequence) {
              ArrayUtils.reverse(phraseTokens);
            }

            evaluationResults =
                this.conceptCalculator.calcBestConcept(phraseTokens, this.acceptedConcepts);
            // System.out.println(Arrays.toString(testData));

            // testResults[0] contains the most probable concept
            // testResults[1] contains the number of potential
            // concepts tested
            // testResults[2] contains the type of smoothing used
            allResults[curPhrase][0] = GeneralHelper.arrayToString(phraseTokens);
            allResults[curPhrase][1] = Integer.toString(phraseTokens.length);
            allResults[curPhrase][2] = expectedConcept;
            allResults[curPhrase][3] = evaluationResults[0];
            allResults[curPhrase][4] = evaluationResults[1];
            allResults[curPhrase][5] = evaluationResults[2];
            this.compiledResults.add(allResults[curPhrase]);
            curPhrase++;

            if (RaptatConstants.TRACK_DURATION
                && curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
              // For development purposes only
              // curTime = System.currentTimeMillis();
              // curDuration = curTime - startTime;
              // System.out.println(curPhrase + "\t" +
              // curDuration);
              durationList.add(System.currentTimeMillis() - startTime);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return durationList;
  }
}
