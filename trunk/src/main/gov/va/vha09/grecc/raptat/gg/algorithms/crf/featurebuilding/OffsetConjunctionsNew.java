package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.PropertyList;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.featurematchers.RaptatFeatureMatcher;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class OffsetConjunctionsNew extends Pipe implements Serializable {
  static final int maxWindowSize = 50;
  static final PropertyList[] startfs = new PropertyList[OffsetConjunctionsNew.maxWindowSize];
  static final PropertyList[] endfs = new PropertyList[OffsetConjunctionsNew.maxWindowSize];

  private static Logger logger = Logger.getLogger(OffsetConjunctionsRaptat.class);

  static {
    initStartEndFs();
  }

  private static final long serialVersionUID = 1;

  private static final int CURRENT_SERIAL_VERSION = 0;
  private static final int NULL_INTEGER = -1;


  private int[][] conjunctions;

  private boolean includeOriginalSingletons;

  // boolean includeBeginEndBoundaries;
  RaptatFeatureMatcher featureMatcher;

  /*
   * This is added to the beginning of feature names when the conjunction offset is greater than
   * maxOffsetLimit or the number of token features combined exceeds maxConjunctionsLimit. It was
   * created to allow for separate features being created during cue and scope training and tagging
   * with just a single pipeline for feature generation.
   */
  private String maxExceededTag;

  /*
   * If the absolute value of an offset of a conjunction exceeds this value, the feature is tagged
   * with maxExceededTag
   */
  private int maxOffsetLimit;

  /*
   * If the number of features joined to make a conjunction feature exceeds this number, the
   * resulting conjunction feature is tagged at its beginning with the maxExceededTag.
   */
  private int maxConjunctionsLimit;

  public OffsetConjunctionsNew(boolean includeOriginalSingletons, int[][] conjunctions) {
    this(includeOriginalSingletons, conjunctions, null);
  }


  public OffsetConjunctionsNew(boolean includeOriginalSingletons, int[][] conjunctions,
      RaptatFeatureMatcher featureMatcher) {
    this(includeOriginalSingletons, featureMatcher, conjunctions, "", 0, 0);
  }


  /*
   * The variables maxOffsetLimit and maxConjunctions variables are used for cue and scope feature
   * building; they identify conjunction features that are differentially labeled for cue and scope.
   * This reduces the need for separate feature building for cue and scope.
   */
  public OffsetConjunctionsNew(boolean includeOriginalSingletons,
      RaptatFeatureMatcher featureMatcher, int[][] conjunctions, String maxExceededTag,
      int maxOffsetLimit, int maxConjunctions) {
    this.conjunctions = conjunctions;
    this.featureMatcher = featureMatcher;
    this.includeOriginalSingletons = includeOriginalSingletons;
    this.maxExceededTag = maxExceededTag;
    this.maxOffsetLimit = maxOffsetLimit;
    this.maxConjunctionsLimit = maxConjunctions;
    OffsetConjunctionsNew.logger.setLevel(Level.INFO);
    if (OffsetConjunctionsNew.logger.isDebugEnabled()) {
      try {
        RollingFileAppender rfa = new RollingFileAppender(new SimpleLayout(),
            "offsetConjuctionLogger_" + System.currentTimeMillis() + ".txt");
        rfa.setMaximumFileSize(100000000);
        rfa.setMaxBackupIndex(10);
        OffsetConjunctionsNew.logger.addAppender(rfa);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }


  public OffsetConjunctionsNew(int[][] conjunctions) {
    this(true, conjunctions);
  }


  private OffsetConjunctionsNew() {
    /* Used only for debugging within main() */
  }


  @Override
  public Instance pipe(Instance carrier) {

    if (this.conjunctions.length == 0) {
      return carrier;
    }

    TokenSequence ts = (TokenSequence) carrier.getData();
    if (OffsetConjunctionsNew.logger.isDebugEnabled()) {
      OffsetConjunctionsNew.logger.debug(
          "\n-----------------------------\nAdding offset conjunctions:\n-----------------------------\n");
      for (int i = 0; i < this.conjunctions.length; i++) {
        OffsetConjunctionsNew.logger
            .debug("\tOffsetArray " + i + ": " + Arrays.toString(this.conjunctions[i]));
      }

      OffsetConjunctionsNew.logger.debug("SENTENCE:");
      OffsetConjunctionsNew.logger.debug("\n==================================================");
      for (cc.mallet.types.Token curToken : ts) {
        System.out.print(curToken.getText() + " ");
      }
      OffsetConjunctionsNew.logger.debug("\n==================================================");

      OffsetConjunctionsNew.logger.debug("\n\nTOKENS:");
      int i = 0;
      for (cc.mallet.types.Token curToken : ts) {
        OffsetConjunctionsNew.logger
            .debug("Token " + i++ + ")" + curToken.toStringWithFeatureNames());
      }
    }
    int tokenSequenceLength = ts.size();
    PropertyList[] oldfs = null;
    PropertyList[] newfs = null;
    try {
      oldfs = new PropertyList[ts.size()];
    } catch (Exception e) {
      System.err.println("Exception allocating oldfs: " + e);
    }
    try {
      newfs = new PropertyList[ts.size()];
    } catch (Exception e) {
      System.err.println("Exception allocating newfs: " + e);
    }

    for (int i = 0; i < tokenSequenceLength; i++) {
      oldfs[i] = ts.get(i).getFeatures();
    }
    if (this.includeOriginalSingletons) {
      for (int i = 0; i < tokenSequenceLength; i++) {
        newfs[i] = ts.get(i).getFeatures();
      }
    }

    /* Find properties for each token that match the regex */
    PropertyList[] regexMatchingFeatures = new PropertyList[tokenSequenceLength];
    if (this.featureMatcher == null) {
      regexMatchingFeatures = oldfs;
    } else {
      for (int i = 0; i < tokenSequenceLength; i++) {
        PropertyList.Iterator iterator = oldfs[i].iterator();
        regexMatchingFeatures[i] = findMatchingProperties(iterator);
        if (OffsetConjunctionsNew.logger.isDebugEnabled()) {
          if (regexMatchingFeatures[i] != null) {
            OffsetConjunctionsNew.logger.debug("MATCHED FEATURES FOR TOKEN:" + i);
            StringBuilder sb = new StringBuilder();
            iterator = regexMatchingFeatures[i].iterator();
            while (iterator.hasNext()) {
              iterator.next();
              sb.append(" '" + iterator.getKey() + "'");
            }
            OffsetConjunctionsNew.logger.debug(sb.toString());
          }
        }
      }
    }

    /* Find new conjunction features for each token */
    for (int currentTokenIndex = 0; currentTokenIndex < tokenSequenceLength; currentTokenIndex++) {
      OffsetConjunctionsNew.logger
          .debug("Forming conjunctions for token at index " + currentTokenIndex);
      /*
       * Find new conjunction features for each row in the conjunction array
       */
      for (int conjunctionRow = 0; conjunctionRow < this.conjunctions.length; conjunctionRow++) {
        OffsetConjunctionsNew.logger.debug("Adding conjunctions for row " + conjunctionRow + ":"
            + Arrays.toString(this.conjunctions[conjunctionRow]));
        /*
         * Get iterators for the features for each token at each offset specified in the current row
         * of the conjunction array
         */
        PropertyList.Iterator[] propertyListIterators = getOffsetIters(this.conjunctions,
            conjunctionRow, tokenSequenceLength, currentTokenIndex, regexMatchingFeatures);
        if (propertyListIterators == null) {
          continue;
        }
        PropertyList combinedFeatures =
            combineAllFeatures(propertyListIterators, this.conjunctions[conjunctionRow]);
        newfs[currentTokenIndex].last().append(combinedFeatures);
      }
    }
    // Put the new PropertyLists in place
    for (int i = 0; i < ts.size(); i++) {
      ts.get(i).setFeatures(newfs[i]);
    }

    OffsetConjunctionsNew.logger.debug(
        "\n-----------------------------\nCompleted offset conjunctions:\n-----------------------------\n");

    if (OffsetConjunctionsNew.logger.isDebugEnabled()) {
      for (int i = 0; i < this.conjunctions.length; i++) {
        System.out.println("\tOffsetArray " + i + ": " + Arrays.toString(this.conjunctions[i]));
      }
      System.out.println("\n=========================\n");
      System.out.println("SENTENCE:");
      for (cc.mallet.types.Token curToken : ts) {
        System.out.print(curToken.getText() + " ");
      }
      System.out.println("\n\nTOKENS:");
      int i = 0;
      for (cc.mallet.types.Token curToken : ts) {
        System.out.println("Token " + i++ + ")" + curToken.toStringWithFeatureNames());
      }
    }
    OffsetConjunctionsNew.logger.debug("Returning token carrier");

    return carrier;
  }


  private PropertyList combineAllFeatures(PropertyList.Iterator[] propertyListIterators,
      int[] elementOffsets) {
    /*
     * Create tag for any features in which the offset or number of conjunctions exceeds the
     * maxConjunctionsLimit or maxOffsetLimit
     */
    String startTag = "";
    if (this.maxConjunctionsLimit > 0 && elementOffsets.length > this.maxConjunctionsLimit
        || this.maxOffsetLimit > 0
            && GeneralHelper.maxAbsoluteValue(elementOffsets) > this.maxOffsetLimit) {
      startTag = this.maxExceededTag + "_";
    }

    PropertyList resultPropertyList = null;
    if (propertyListIterators != null && propertyListIterators.length > 0) {
      ArrayDeque<PropertyList.Iterator> iteratorQueue =
          new ArrayDeque<>(propertyListIterators.length);

      for (PropertyList.Iterator curIterator : propertyListIterators) {
        /*
         * PropertyList.Iterator seems to be padded with one extra feature at the beginning for an
         * unknown reason, so we always skip this first feature
         */
        curIterator.next();
        iteratorQueue.add(curIterator);
      }

      ArrayDeque<Integer> offsetQueue = new ArrayDeque<>(elementOffsets.length);
      for (int curOffset : elementOffsets) {
        offsetQueue.add(curOffset);
      }

      double combinedFeatureValue = 1.0;
      int iteratorCounter = 0;
      int numberOfIterators = propertyListIterators.length;
      boolean firstIterator = true;
      boolean lastIterator = false;
      boolean getNext = false;
      StringBuilder sb = new StringBuilder(startTag);
      PropertyList.Iterator curIterator = null;

      /*
       * If we just processed the last iterator and determined that we need to call getNext() for
       * the next iterator in the queue, it means we have run through all features for all the
       * iterators in the queue
       */
      while (!lastIterator || !getNext) {
        firstIterator = iteratorCounter % numberOfIterators == 0;
        lastIterator = (iteratorCounter + 1) % numberOfIterators == 0;
        curIterator = iteratorQueue.remove();
        Integer curOffset = offsetQueue.remove();

        String curKey = curIterator.getKey();
        if (!firstIterator) {
          sb.append("_&_");
        }
        sb.append(curKey).append("@").append(curOffset);

        double curFeatureValue = curIterator.getNumericValue();
        combinedFeatureValue *= curFeatureValue;

        if (lastIterator) {
          resultPropertyList =
              PropertyList.add(sb.toString(), combinedFeatureValue, resultPropertyList);
          sb = new StringBuilder();
          combinedFeatureValue = 1.0;
        }

        if (getNext || firstIterator) {
          if (curIterator.hasNext()) {
            curIterator.next();
            getNext = false;
          } else {
            curIterator = curIterator.reset();
            curIterator.next();
            getNext = true;
          }
        }
        iteratorQueue.add(curIterator);
        offsetQueue.add(curOffset);
        iteratorCounter++;
      }
    }

    return resultPropertyList;
  }


  private PropertyList findMatchingProperties(PropertyList.Iterator iterator) {
    PropertyList resultList = null;
    while (iterator.hasNext()) {
      iterator.next();
      String feature = iterator.getKey();
      // logger.debug( "Evaluating feature '" + feature + "' for matching"
      // );
      if (this.featureMatcher != null && this.featureMatcher.matches(feature)) {
        resultList = PropertyList.add(feature, iterator.getNumericValue(), resultList);
      }
    }
    return resultList;
  }


  private PropertyList.Iterator getOffsetIter(int[][] conjunctions, int conjunctionRow,
      int rowElement, int tsSize, int tsIndex, PropertyList[] matchingFeatures) {
    PropertyList.Iterator iter;
    OffsetConjunctionsNew.logger.debug("Getting offset iterator at index:" + rowElement
        + " in offset array for token at absolute index:"
        + (tsIndex + conjunctions[conjunctionRow][rowElement]) + " in sentence");
    if (tsIndex + conjunctions[conjunctionRow][rowElement] < 0) {
      iter =
          OffsetConjunctionsNew.startfs[-(tsIndex + conjunctions[conjunctionRow][rowElement]) - 1]
              .iterator();
    } else if (conjunctions[conjunctionRow][rowElement] + tsIndex > tsSize - 1) {
      iter =
          OffsetConjunctionsNew.endfs[tsIndex + conjunctions[conjunctionRow][rowElement] - tsSize]
              .iterator();
    } else if (matchingFeatures[conjunctions[conjunctionRow][rowElement] + tsIndex] == null) {
      iter = null;
    } else {
      iter = matchingFeatures[tsIndex + conjunctions[conjunctionRow][rowElement]].iterator();
    }
    return iter;
  }


  /** Get iterators for each token in this offset */
  private PropertyList.Iterator[] getOffsetIters(int[][] conjunctions, int conjunctionRow,
      int tsSize, int tsIndex, PropertyList[] matchingFeatures) {
    OffsetConjunctionsNew.logger.debug("Getting offset iterators for OffsetArray " + conjunctionRow
        + ":" + Arrays.toString(conjunctions[conjunctionRow]));
    PropertyList.Iterator[] iterators =
        new PropertyList.Iterator[conjunctions[conjunctionRow].length];
    // get iterators for offsets
    for (int iteratorIndex = 0; iteratorIndex < iterators.length; iteratorIndex++) {
      iterators[iteratorIndex] = getOffsetIter(conjunctions, conjunctionRow, iteratorIndex, tsSize,
          tsIndex, matchingFeatures);
      if (iterators[iteratorIndex] == null) {
        return null;
      }
    }
    return iterators;
  }


  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int size1, size2;
    int version = in.readInt();
    size1 = in.readInt();
    // Deserialization doesn't call the unnamed class initializer, so do it
    // here
    if (OffsetConjunctionsNew.startfs[0] == null) {
      initStartEndFs();
    }
    if (size1 == OffsetConjunctionsNew.NULL_INTEGER) {
      this.conjunctions = null;
    } else {
      this.conjunctions = new int[size1][];
      for (int i = 0; i < size1; i++) {
        size2 = in.readInt();
        if (size2 == OffsetConjunctionsNew.NULL_INTEGER) {
          this.conjunctions[i] = null;
        } else {
          this.conjunctions[i] = new int[size2];
          for (int j = 0; j < size2; j++) {
            this.conjunctions[i][j] = in.readInt();
          }
        }
      }
    }
    this.includeOriginalSingletons = in.readBoolean();
    this.featureMatcher = (RaptatFeatureMatcher) in.readObject(); // add by
    // fuchun

  }


  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(OffsetConjunctionsNew.CURRENT_SERIAL_VERSION);
    int size1, size2;
    size1 =
        this.conjunctions == null ? OffsetConjunctionsNew.NULL_INTEGER : this.conjunctions.length;
    out.writeInt(size1);
    if (size1 != OffsetConjunctionsNew.NULL_INTEGER) {
      for (int i = 0; i < size1; i++) {
        size2 = this.conjunctions[i] == null ? OffsetConjunctionsNew.NULL_INTEGER
            : this.conjunctions[i].length;
        out.writeInt(size2);
        if (size2 != OffsetConjunctionsNew.NULL_INTEGER) {
          for (int j = 0; j < size2; j++) {
            out.writeInt(this.conjunctions[i][j]);
          }
        }
      }
    }
    out.writeBoolean(this.includeOriginalSingletons);

    out.writeObject(this.featureMatcher);
  }


  private static void initStartEndFs() {
    for (int i = 0; i < OffsetConjunctionsNew.maxWindowSize; i++) {
      OffsetConjunctionsNew.startfs[i] = PropertyList.add("W=<START" + i + ">", 1.0, null);
      OffsetConjunctionsNew.endfs[i] = PropertyList.add("W=<END" + i + ">", 1.0, null);
    }
  }
}
