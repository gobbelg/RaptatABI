package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.LemmatiserFactory;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.POSTaggerFactory;

/** @author Glenn T. Gobbel Mar 23, 2016 */
public class NBTrainer extends ConceptMapTrainer {

  private static Logger logger = Logger.getLogger(NBTrainer.class);


  public NBTrainer() {
    super();
  }


  /**
   * Constructor for an object that will calculate and use the likelihood of a database for a naive
   * Bayes network
   *
   * @param useNulls
   * @param useStems
   * @param usePOS
   * @param invertTokenSequence
   * @param removeStopWords
   * @param reasonNoMedsProcessing
   */
  public NBTrainer(boolean useNulls, boolean useStems, boolean usePOS, boolean invertTokenSequence,
      boolean removeStopWords, ContextHandling reasonNoMedsProcessing) {
    super(useNulls, useStems, usePOS, invertTokenSequence, removeStopWords, reasonNoMedsProcessing);
    if (usePOS) {
      this.posTagger = POSTaggerFactory.getInstance();
    }
    if (useStems || removeStopWords) {
      this.stemmer = LemmatiserFactory.getInstance(removeStopWords);
    }
  }


  /**
   * Constructor used when the caller wants an NBTrainer that will update a previously generated
   * ConceptMapSolution instance. The parameters for training are taken from the inputSolution
   * instance.
   *
   * @param inputSolution
   * @param ignoreOptions
   */
  public NBTrainer(ConceptMapSolution inputSolution, boolean ignoreOptions) {
    super(inputSolution);

    /*
     * If we are not ignoring options, we will need to handle POS tagging and lemmatization
     * ourselves
     */
    if (!ignoreOptions) {
      /*
       * Note that these options are set from the inputSolution by the initial call to the
       * superclass constructor
       */
      if (this.addPOSToTokens) {
        this.posTagger = POSTaggerFactory.getInstance();
      }
      if (this.convertTokensToStems || this.removeStopWords) {
        this.stemmer = LemmatiserFactory.getInstance(this.removeStopWords);
      }
    }
  }


  @Override
  protected Vector<Long> addToSolution(DataImportReader trainingData, int startTokensPosition,
      int numElementsForAnalysis) {
    int curPhrase = 0;
    int i = 0;
    String[] curData, phraseTokens;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    // Track time to perform updates for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;
    if (RaptatConstants.TRACK_DURATION) {
      /*
       * Add 1 for easy handling of uneven divisions
       */
      durationList = new Vector<>(
          1 + (int) trainingData.getReaderLength() / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    // Run through the data line-by-line updating the database
    // after each line. Update the likelihood table whenever
    // curPhrase is an even multiple of PHRASE_REPORT_INTERVAL
    // Find the first valid phrase
    do {
      curData = trainingData.getNextData(startTokensPosition);
    } while (curData != null && curData.length != numElementsForAnalysis);

    try {
      // Keep reading while not at end of data file
      while (curData != null) {
        // Keep updating database as long as the phrase number
        // is not an even multiple of PHRASE_REPORT_INTERVAL
        // and we haven't reached the end of the file (curData != null)
        do {
          // Get the current data string up until we reach a value
          // of NULL_ELEMENT or up until we come to the concept value
          // column
          i = 0;
          while (!curData[i].equals(RaptatConstants.NULL_ELEMENT) && i < curData.length - 1) {
            i++;
          }

          phraseTokens = GeneralHelper.getSubArray(curData, 0, i);

          if (phraseTokens != null && phraseTokens.length > 0) {
            phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);

            /*
             * This automatically handles removeStopWords
             */
            if (this.convertTokensToStems) {
              /*
               * This will convert data to lower case and remove stop words if set to do so during
               * initialization
               */
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

              if (this.addPOSToTokens) {
                /*
                 * Find out which POS tags to keep when we get // them // as some stop words may
                 * have been removed, but // we // want to do POS tagging on complete phrase
                 */
                posTags = this.posTagger.tag(phraseTokens);
                keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
                GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
              } else {
                phraseTokens = this.stemmer.lemmatize(preProcessedData);
              }

            } else {
              if (this.removeStopWords) {
                if (this.addPOSToTokens) {
                  /*
                   * Find out which POS tags to keep when we get them as some stop words may have
                   * been removed, but we want to do POS tagging on complete phrase
                   *
                   * This will convert data to lower case and remove stop words if set to do so
                   * during initialization
                   */
                  posTags = this.posTagger.tag(phraseTokens);
                  preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                  keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
                  phraseTokens = preProcessedData;
                  GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
                } else {
                  /*
                   * This will convert data to lower case and remove stop words if set to do so
                   * during initialization
                   */
                  phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
                }
              } else {
                if (this.addPOSToTokens) {
                  posTags = this.posTagger.tag(phraseTokens);
                  GeneralHelper.concatenateArrays(phraseTokens, posTags, "_");
                }
              }
            }

            if (this.invertTokenSequence) {
              ArrayUtils.reverse(phraseTokens);
            }

            this.theSolution.updateTraining(phraseTokens,
                curData[curData.length - 1].toLowerCase());
          }

          // Get the next valid phrase;
          do {
            curData = trainingData.getNextData(startTokensPosition);
          } while (curData != null && curData.length != numElementsForAnalysis);

          curPhrase++;

        } while (curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL != 0 && curData != null);

        ;
        if (!this.solutionInitiated) {
          this.theSolution.initiateProbCalcs();
          this.solutionInitiated = true;
        } else {
          // Now update to the new document and update the likelihood
          // values with the database based on the data from the old
          // document
          this.theSolution.updateLikelihood();
        }

        if (RaptatConstants.TRACK_DURATION) {
          // For development only
          // curTime = System.currentTimeMillis();
          // curDuration = curTime - startTime;
          // System.out.println(curPhrase + "\t" + curDuration);
          durationList.add(System.currentTimeMillis() - startTime);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure all likelihood have been updated
    if (!this.solutionInitiated) {
      this.theSolution.initiateProbCalcs();
      this.solutionInitiated = true;
    } else {
      this.theSolution.updateLikelihood();
    }

    return durationList;
  }


  /**
   * Iterates through a list of AnnotatedPhrase objects and uses each one to update the Solution
   * instance associated with this NBTrainer object
   *
   * @param dataIterator
   * @param dataSize
   * @return Vector<Long>
   */
  @Override
  protected Vector<Long> addToSolution(ListIterator<AnnotatedPhrase> dataIterator, int dataSize) {
    String[] curData, phraseTokens;
    AnnotatedPhrase curAnnotation;

    int i;
    int curPhrase = 0;

    // Track time to perform updates for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;
    if (RaptatConstants.TRACK_DURATION) {
      /* Add 1 for easy handling of uneven divisions */
      durationList = new Vector<>(1 + dataSize / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    try {
      while (dataIterator.hasNext()) {
        curAnnotation = dataIterator.next();
        curData = GeneralHelper.tokensToStringArray(curAnnotation.getProcessedTokens());
        NBTrainer.logger.debug(Arrays.toString(curData));

        /*
         * Get the current data string up until we reach a value of NULL_ELEMENT or up until we come
         * to the concept value column
         */
        i = 0;
        while (i < curData.length && !curData[i].equals(RaptatConstants.NULL_ELEMENT)) {
          i++;
        }

        phraseTokens = GeneralHelper.getSubArray(curData, 0, i);
        this.theSolution.updateTraining(phraseTokens, curAnnotation);

        curPhrase++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure all likelihood have been updated
    if (!this.solutionInitiated) {
      this.theSolution.initiateProbCalcs();
      this.solutionInitiated = true;
    } else {
      this.theSolution.updateLikelihood();
    }

    return durationList;
  }


  @Override
  protected Vector<Long> addToSolution(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns) {
    String[] curData, phraseTokens;
    int i;
    int curPhrase = 0;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    // Track time to perform updates for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;
    if (RaptatConstants.TRACK_DURATION) {
      durationList = new Vector<>(1 + dataSize / RaptatConstants.PHRASE_REPORT_INTERVAL); // Add
      // 1
      // for
      // easy
      // handling
      // of uneven
      // divisions
    }

    try {
      while (dataIterator.hasNext()) {
        curData = dataIterator.next();

        // Get the current data string up until we reach a value
        // of NULL_ELEMENT or up until we come to the concept value
        // column
        i = numGroupingColumns;
        while (i < curData.length - 1 && !curData[i].equals(RaptatConstants.NULL_ELEMENT)) {
          i++;
        }

        phraseTokens =
            GeneralHelper.getSubArray(curData, numGroupingColumns, i - numGroupingColumns);

        if (phraseTokens != null && phraseTokens.length > 0) {
          phraseTokens = GeneralHelper.allToLowerCase(phraseTokens);
          if (this.convertTokensToStems) {
            // This will convert data to lower case and remove
            // stop words if set to do so during initialization
            preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

            if (this.addPOSToTokens) {
              // Find out which POS tags to keep when we get them
              // as some stop words may have been removed, but we
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
                // Find out which POS tags to keep when we get
                // them
                // as some stop words may have been removed, but
                // we
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
            // (if !convertTokensToStems && !removeStopWords )
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

          this.theSolution.updateTraining(phraseTokens, curData[curData.length - 1].toLowerCase());

          curPhrase++;

          if (curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
            if (!this.solutionInitiated) {
              this.theSolution.initiateProbCalcs();
              this.solutionInitiated = true;
            } else {
              this.theSolution.updateLikelihood();
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure all likelihood have been updated
    if (!this.solutionInitiated) {
      this.theSolution.initiateProbCalcs();
      this.solutionInitiated = true;
    } else {
      this.theSolution.updateLikelihood();
    }

    return durationList;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(DataImportReader trainingData,
      int startTokensPosition, int numElementsForAnalysis) {
    int curPhrase = 0;
    String[] curData, phraseTokens;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    // Track time to perform updates for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;

    if (RaptatConstants.TRACK_DURATION) {
      durationList = new Vector<>(
          1 + (int) trainingData.getReaderLength() / RaptatConstants.PHRASE_REPORT_INTERVAL); // Add
      // 1
      // for
      // easy
      // handling
      // of
      // uneven
      // divisions
    }

    // Run through the data line-by-line updating the database
    // after each line. Update the likelihood table whenever
    // curPhrase is an even multiple of PHRASE_REPORT_INTERVAL
    // Find the first valid phrase
    do {
      curData = trainingData.getNextData(startTokensPosition);
    } while (curData != null && curData.length != numElementsForAnalysis);

    try {
      // Keep reading while not at end of data file
      while (curData != null) {
        // Keep updating database as long as the phrase number
        // is not an even multiple of PHRASE_REPORT_INTERVAL
        // and we haven't reached the end of the file (curData != null)
        do {
          // Only get the tokens before the concept
          phraseTokens = GeneralHelper
              .allToLowerCase(GeneralHelper.getSubArray(curData, 0, curData.length - 1));

          if (this.convertTokensToStems) {
            // This will convert data to lower case and remove
            // stop words if set to do so during initialization
            preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

            if (this.addPOSToTokens) {
              // Find out which POS tags to keep when we get them
              // as some stop words may have been removed, but we
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
                // Find out which POS tags to keep when we get
                // them
                // as some stop words may have been removed, but
                // we
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
            // (if !convertTokensToStems && !removeStopWords )
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

          // System.out.println( Arrays.toString(phraseTokens));
          this.theSolution.updateTraining(phraseTokens, curData[curData.length - 1].toLowerCase());

          // Get the next valid phrase;
          do {
            curData = trainingData.getNextData(startTokensPosition);
          } while (curData != null && curData.length != numElementsForAnalysis);

          curPhrase++;

        } while (curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL != 0 && curData != null);

        // System.out.println("Updating likelihood for phrase " +
        // curPhrase);
        if (!this.solutionInitiated) {
          this.theSolution.initiateProbCalcs();
          this.solutionInitiated = true;
        } else {
          // Now update to the new document and update the likelihood
          // values with the database based on the data from the old
          // document
          this.theSolution.updateLikelihood();
        }

        if (RaptatConstants.TRACK_DURATION) {
          durationList.add(System.currentTimeMillis() - startTime);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure all likelihood have been updated
    if (!this.solutionInitiated) {
      this.theSolution.initiateProbCalcs();
      this.solutionInitiated = true;
    } else {
      this.theSolution.updateLikelihood();
    }

    return durationList;
  }


  /**
   * ********************************************************* Iterates through a list of
   * AnnotatedPhrase objects and uses each one to update the Solution instance associated with this
   * NBTrainer object. This method does not strip out NULL_ELEMENT strings in the list but uses them
   * for training.
   *
   * @param dataIterator
   * @param dataSize
   * @return Vector<Long> **********************************************************
   */
  @Override
  protected Vector<Long> addToSolutionUseNulls(ListIterator<AnnotatedPhrase> dataIterator,
      int dataSize) {
    String[] phraseTokens;
    int curPhrase = 0;
    AnnotatedPhrase curAnnotation;

    // Track time to perform updates for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;
    if (RaptatConstants.TRACK_DURATION) {
      /* Add 1 for easy handling of uneven divisions */
      durationList = new Vector<>(1 + dataSize / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    try {
      while (dataIterator.hasNext()) {
        curAnnotation = dataIterator.next();
        phraseTokens = GeneralHelper.tokensToStringArray(curAnnotation.getProcessedTokens());
        this.theSolution.updateTraining(phraseTokens, curAnnotation);
        curPhrase++;

        if (curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
          if (!this.solutionInitiated) {
            this.theSolution.initiateProbCalcs();
            this.solutionInitiated = true;
          } else {
            this.theSolution.updateLikelihood();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure all likelihood have been updated
    if (!this.solutionInitiated) {
      this.theSolution.initiateProbCalcs();
      this.solutionInitiated = true;
    } else {
      this.theSolution.updateLikelihood();
    }

    return durationList;
  }


  @Override
  protected Vector<Long> addToSolutionUseNulls(ListIterator<String[]> dataIterator, int dataSize,
      int numGroupingColumns) {
    String[] curData, phraseTokens;
    int curPhrase = 0;
    String posTags[] = null;
    int[] keptStrings = null;
    String[] preProcessedData;

    // Track time to perform updates for each phrase
    long startTime = System.currentTimeMillis();
    Vector<Long> durationList = null;
    if (RaptatConstants.TRACK_DURATION) {
      /*
       * Add 1 for easy handling of uneven divisions
       */
      durationList = new Vector<>(1 + dataSize / RaptatConstants.PHRASE_REPORT_INTERVAL);
    }

    try {
      while (dataIterator.hasNext()) {
        curData = dataIterator.next();

        // Only get the tokens before the concept
        phraseTokens = GeneralHelper.allToLowerCase(GeneralHelper.getSubArray(curData,
            numGroupingColumns, curData.length - numGroupingColumns - 1));

        if (this.convertTokensToStems) {
          // This will convert data to lower case and remove
          // stop words if set to do so during initialization
          preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);

          if (this.addPOSToTokens) {
            /*
             * Find out which POS tags to keep when we get them // as some stop words may have been
             * removed, but we // want to do POS tagging on complete phrase
             */
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
              /*
               * Find out which POS tags to keep when we get them as some stop words may have been
               * removed, but we want to do POS tagging on complete phrase
               */

              /*
               * This will convert data to lower case and remove stop words if set to do so during
               * initialization
               */
              preProcessedData = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
              keptStrings = GeneralHelper.sameStrings(phraseTokens, preProcessedData);
              posTags = this.posTagger.tag(phraseTokens);
              phraseTokens = preProcessedData;
              GeneralHelper.concatenateArraysByIndex(phraseTokens, posTags, keptStrings, "_");
            } else {
              // This will convert data to lower case and remove
              // stop words if set to do so during initialization
              phraseTokens = this.stemmer.preProcessAndRemoveDashColons(phraseTokens);
            }
          } else
          // (if !convertTokensToStems && !removeStopWords )
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

        this.theSolution.updateTraining(phraseTokens, curData[curData.length - 1].toLowerCase());
        curPhrase++;

        if (curPhrase % RaptatConstants.PHRASE_REPORT_INTERVAL == 0) {
          if (!this.solutionInitiated) {
            this.theSolution.initiateProbCalcs();
            this.solutionInitiated = true;
          } else {
            this.theSolution.updateLikelihood();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure all likelihood have been updated
    if (!this.solutionInitiated) {
      this.theSolution.initiateProbCalcs();
      this.solutionInitiated = true;
    } else {
      this.theSolution.updateLikelihood();
    }

    return durationList;
  }


  /**
   * Creates "theSolution" to use for likelihood calculations
   *
   * @param trainingData
   */
  @Override
  protected void createSolution(DataImportReader trainingData) {
    Vector<Long> durationList = null;

    /*
     * Each row of data contains a variable number of grouping // columns, followed by a variable
     * set of tokens in a phrase, // finally followed by a single concept. Each set of data //
     * analyzed consists of a string array containing first // the tokens in the phrase followed by
     * the concept at the // end.
     */
    int numTokens = trainingData.getNumPhraseTokens();
    int startTokensPosition = trainingData.getStartTokensPosition();

    this.theSolution = new NBSolution(numTokens, this.useNulls, this.convertTokensToStems,
        this.addPOSToTokens, this.invertTokenSequence, this.removeStopWords);

    if (getUseNulls()) {
      durationList = this.addToSolutionUseNulls(trainingData, startTokensPosition, numTokens + 1);
    } else {
      durationList = this.addToSolution(trainingData, startTokensPosition, numTokens + 1);
    }

    if (RaptatConstants.TRACK_DURATION && durationList != null) {
      String trainTimeFileName =
          trainingData.getFileDirectory() + "TrainTime_" + GeneralHelper.getTimeStamp() + ".csv";
      GeneralHelper.writeVectorAtInterval(trainTimeFileName, durationList,
          RaptatConstants.PHRASE_REPORT_INTERVAL);
    }
  }


  @Override
  protected void createSolution(List<AnnotatedPhrase> trainingData) {
    if (trainingData != null) {
      int dataSize = trainingData.size();

      this.theSolution = new NBSolution(this.maxElements, this.useNulls, this.convertTokensToStems,
          this.addPOSToTokens, this.invertTokenSequence, this.removeStopWords);

      /*
       * Run through the data line-by-line updating the database // after each line. Update the
       * likelihood table whenever there // is a new document number (provided as the first element
       * on the // line
       */
      ListIterator<AnnotatedPhrase> dataIterator = trainingData.listIterator();

      if (getUseNulls()) {
        this.addToSolutionUseNulls(dataIterator, dataSize);
      } else {
        this.addToSolution(dataIterator, dataSize);
      }
    }
  }


  /**
   * This method creates the Naive Bayes solution using a list of data. Unlike the createSolution
   * method for DataImportReaders, there is no checking to make sure that each row in the list has
   * the correct number of element. So, we assume the list is valid in terms of data length.
   *
   * @param trainingData - List<String> of the data. Each row of attributes and concept is preceded
   *        by a given number of elements (groupingColumns) indicating how to group the data when
   *        used for other analyses)
   * @param groupingColumns - Number of columns preceding the actual data (attributes and concept)
   *        being analyzed
   */
  @Override
  protected void createSolution(List<String[]> trainingData, int groupingColumns) {
    if (trainingData != null && trainingData.get(0).length - groupingColumns > 1) {
      /*
       * Make sure the data has at least two elements after the grouping columns
       */

      int dataSize = trainingData.size();

      int expectedNumElements = trainingData.get(0).length;

      this.theSolution = new NBSolution(expectedNumElements - groupingColumns - 1, this.useNulls,
          this.convertTokensToStems, this.addPOSToTokens, this.invertTokenSequence,
          this.removeStopWords);

      /*
       * Run through the data line-by-line updating the database // after each line. Update the
       * likelihood table whenever there // is a new document number (provided as the first element
       * on the // line
       */
      ListIterator<String[]> dataIterator = trainingData.listIterator();

      if (getUseNulls()) {
        this.addToSolutionUseNulls(dataIterator, dataSize, groupingColumns);
      } else {
        this.addToSolution(dataIterator, dataSize, groupingColumns);
      }
    }
  }
}
