/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

/**
 * Builds arrays of ints that represent how far upstream or downstream to combine features with a
 * given token in a token sequence to create additional features for that token. The features can be
 * ngrams of width one to 'width'.
 *
 * @author Glenn T. Gobbel Feb 18, 2016
 */
public class ConjunctionOffsetArrayBuilder {
  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // int[][] offsetArray = generateConjunctionOffsetArray( 5, 5,
        // 4, true );
        //
        // for (int i = 0; i < offsetArray.length; i++ )
        // {
        // System.out.println( Arrays.toString( offsetArray[i] ) );
        // }

        int[][] offsetArray = generateConjunctionOffsetArray(-7, 5, 2, true);

        for (int i = 0; i < offsetArray.length; i++) {
          System.out.println(Arrays.toString(offsetArray[i]));
        }
      }
    });
  }


  /**
   * Creates a two-dimensional array that indicate what features from distant tokens may be combined
   * to form a combined feature. The distance is given by positive and negative offsets and the
   * number of features combined is given by the width.
   *
   * @param negOffset - How far to the left (upstream) of the current token to go to get features
   * @param posOffset - How far to the right (downstream) of the current token to go to get features
   * @param width - Highest number of sequential tokens whose features are joined together to form
   *        unigram to ngrams where n=width.
   * @param includeOrigin - Include features of the token itself (0 offset) when combining features
   * @return - An array to use in the OffsetConjunctions constructor
   */
  protected static int[][] generateConjunctionOffsetArray(int negOffset, int posOffset, int width,
      boolean includeOrigin) {
    if (width == 0) {
      return new int[0][0];
    }

    /*
     * Correct any sign errors
     */
    negOffset = -Math.abs(negOffset);
    posOffset = Math.abs(posOffset);
    width = Math.abs(width);

    /*
     * Create temporary list of big enough size to hold all temporary results;
     */
    List<int[]> resultList = new ArrayList<>(width * (-negOffset + posOffset));
    int[][] offsetArrayResults;

    if (includeOrigin) {
      /*
       * We first run through single features at offsets, excluding singleton features of just the
       * origin itself as this would create repetitive features for to token at the origin
       */
      offsetArrayResults = generateConjunctionOffsetArray(negOffset, posOffset, 1, false);
      for (int[] curOffset : offsetArrayResults) {
        resultList.add(curOffset);
      }

      for (int i = 2; i <= width; i++) {
        offsetArrayResults = generateSingleOffsets(negOffset, posOffset, i);
        for (int j = 0; j < offsetArrayResults.length; j++) {
          resultList.add(offsetArrayResults[j]);
        }
      }
    } else {
      for (int i = 1; i <= width; i++) {
        offsetArrayResults = generateSingleOffsets(negOffset, -1, i);
        for (int j = 0; j < offsetArrayResults.length; j++) {
          resultList.add(offsetArrayResults[j]);
        }
      }

      for (int i = 1; i <= width; i++) {
        offsetArrayResults = generateSingleOffsets(1, posOffset, i);
        for (int j = 0; j < offsetArrayResults.length; j++) {
          resultList.add(offsetArrayResults[j]);
        }
      }
    }

    int[][] resultArray = new int[resultList.size()][];
    Iterator<int[]> arrayIterator = resultList.iterator();
    for (int i = 0; i < resultList.size(); i++) {
      resultArray[i] = arrayIterator.next();
    }

    return resultArray;
  }


  /**
   * @param startOffset - where to start making offsets (inclusive)
   * @param endOffset - where to end making offsets (inclusive)
   * @param arraySize - number of offset indices to include in each returned array
   * @return
   */
  private static int[][] generateSingleOffsets(int startOffset, int endOffset, int arraySize) {
    int offsetWidth = endOffset - startOffset + 1;
    int numberOfArrays = offsetWidth - arraySize + 1;
    if (arraySize < 1 || numberOfArrays < 1) {
      return new int[0][0];
    }
    int[][] resultArray = new int[numberOfArrays][arraySize];

    for (int i = 0; i < numberOfArrays; i++) {
      for (int j = 0; j < arraySize; j++) {
        resultArray[i][j] = startOffset + i + j;
      }
    }
    return resultArray;
  }
}
