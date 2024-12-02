/** */
package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;

/**
 * ****************************************************** Determines the amount of offset from the
 * beginning (offset=0) of a text file for various character formats corresponding to openNLP,
 * eHost, and Knowtator. Each of these create there own offset method. The baseline method is where
 * every byte counts as a character.
 *
 * <p>
 * The method uses finite state machines, stored as FSM objects to determine how much to increment
 * the character offset for each byte read into the file. The class can be expanded by including
 * additional format specific FSMs.
 *
 * @author Glenn Gobbel - Jun 4, 2012 *****************************************************
 */
public class OffsetParser {
  /*
   * Input characters that determine the transition of a finite state machine where OC is other
   * character, CR is carriage return, LF is line feed, UTF1 is a UTF code indicating the next
   * single byte indicates a character, UTF2 indicates the next two bytes correspond to a single
   * character, and UTF3 indicates the next 3 bytes indicate a single character
   */
  public enum CharacterInput implements FSInput {
    OC, CR, LF, UTF1, UTF2, UTF3
  }

  /*
   * These are the potential states for the finite state machines, Start, CarriageReturn, and
   * various UTF character states
   */
  private enum FsmState {
    START, CR, UTF1, UTF2, UTF3
  }

  private CharacterInput input;

  {
    OffsetParser.LOGGER.setLevel(Level.INFO);
  }

  /*
   * These specify the indices in the settings array to determine next state and offset to advance
   */
  private static int STATE_INDEX = 0;

  private static int OFFSET_CHANGE_INDEX = 1;

  /*
   * We have different FSMs for different systems so can convert offsets among multiple types of
   * systems
   */
  private static FSM knowtatorFSM;

  private static FSM ehostFSM;

  private static FSM openNLPFSM;


  static {
    int[][][] knowtatorFsmSettings = getKnowtatorFsmSettings();
    OffsetParser.knowtatorFSM = new FSM(knowtatorFsmSettings);

    /*
     * The only difference for OpenNLP from Knowtator is that it counts a carriage return followed
     * by a line feed as 2 rather than 1 character, and this change accounts for that difference in
     * offset for a given input when in the carriage return state
     */
    int[][][] openNlpFsmSettings = GeneralHelper.threeDimIntArrayCopy(knowtatorFsmSettings);
    openNlpFsmSettings[FsmState.CR.ordinal()][CharacterInput.LF
        .ordinal()][OffsetParser.OFFSET_CHANGE_INDEX] = 1;
    OffsetParser.openNLPFSM = new FSM(openNlpFsmSettings);

    /*
     * Modify fsmSetttings for creating ehostFSM. Like Knowtator, carriage return followed by a line
     * feed counts as just 1 character. In addition, it does not recognize UTF-8 characters and
     * counts them all as individual characters
     */
    int[][][] eHostFsmSettings = GeneralHelper.threeDimIntArrayCopy(knowtatorFsmSettings);
    for (int i = FsmState.UTF1.ordinal(); i <= FsmState.UTF3.ordinal(); i++) {
      for (int j = CharacterInput.OC.ordinal(); j < CharacterInput.UTF3.ordinal(); j++) {
        eHostFsmSettings[i][j][OffsetParser.OFFSET_CHANGE_INDEX] = 1;
      }
    }
    OffsetParser.ehostFSM = new FSM(eHostFsmSettings);
  }


  protected static Logger LOGGER = Logger.getLogger(OffsetParser.class);


  /**
   * *********************************************************** Determine exact location of all
   * annotated phrases within the document as determined by Open NLP and by a file reader. This
   * method creates a set of IntegerPairComparators that can be used to quickly look up how to
   * convert from an annotator (eHOST or Knowtator) XML offset to an OpenNLP offset.
   *
   * @param pathToDocument
   * @author Glenn Gobbel - Apr 25, 2012 ***********************************************************
   */
  public static IndexedTree<IndexedObject<Integer>> calculateOffsetCorrections(
      String pathToDocument) {
    InputStream is = null;
    List<IndexedObject<Integer>> annotatorToOpenNLPOffsetCorrections = new ArrayList<>();
    IndexedTree<IndexedObject<Integer>> annotatorToOpenNLPOffsetComparer;
    byte[] curBytes = new byte[1024];
    boolean foundCRChar = false, foundUTFStartChar = false;
    int charsPrinted = 0;

    int annotatorOffset = 0, openNLPOffset = 0;
    int startLoopIndex = 0, extraUTFChars = 0, curCharsRead = 0;
    // int totalChars = 0; /* For debugging only */

    if (hasByteOrderMark(pathToDocument)) {
      // openNLPOffset remains at 0
      annotatorOffset = 1;
      annotatorToOpenNLPOffsetCorrections
          .add(new IndexedObject<>(0, openNLPOffset - annotatorOffset));
      startLoopIndex = 3;
    }

    try {
      is = new BufferedInputStream(new FileInputStream(pathToDocument));
      while ((curCharsRead = is.read(curBytes)) != -1) {
        /*
         * Print out binary representations of each set of 16 characters that were just read in
         */
        if (OffsetParser.LOGGER.isDebugEnabled()) {
          int k = 0;
          while (k < curCharsRead) {
            System.out.println();
            System.out.print(StringUtils.leftPad(Integer.toHexString(charsPrinted), 8, "0") + ":");
            do {
              System.out.print(
                  StringUtils.leftPad(Integer.toBinaryString(curBytes[k++] & 0xFF), 8, "0") + " ");
            } while (++charsPrinted % 16 != 0);
          }
          System.out.println("\n------------------\n");
        }
        // totalChars += startLoopIndex;

        /*
         * Note that startLoopIndex only considers the ByteOrderMark for the first time through the
         * loop. It is set to zero for subsequent iterations.
         */
        for (int i = startLoopIndex; i < curCharsRead; i++) {
          // totalChars++;
          if (foundCRChar) {
            /*
             * CR (0D in hex) followed by LF (0A in hex) treated as 1 character by Knowtator and 2
             * by OpenNLP - we adjust for that here once LF found after finding CR
             */
            if (curBytes[i] == RaptatConstants.LINE_FEED_CHAR) {
              foundCRChar = false;
              openNLPOffset++;
              // System.out.println("Total: " + totalChars +"\t" +
              // "Correction: " +
              // (openNLPOffset-knowtatorOffset));
              annotatorToOpenNLPOffsetCorrections
                  .add(new IndexedObject<>(annotatorOffset, openNLPOffset - annotatorOffset));
            } else {
              foundCRChar = false;
              if (curBytes[i] == RaptatConstants.CARRIAGE_RETURN_CHAR) {
                foundCRChar = true;
                openNLPOffset++;
                annotatorOffset++;
              } else if (curBytes[i] < 0) {
                foundUTFStartChar = true;
                openNLPOffset++;
                annotatorOffset++;
                // Bytes from -1 to -16 correspond to F0 to FF
                // in hex, so top 4 bits set for UTF-8
                if (curBytes[i] > -17) {
                  extraUTFChars = 3;
                }
                // Bytes from -32 to -17 correspond to E0 to EF
                // in hex, so top 3 bits set for UTF-8
                else if (curBytes[i] > -33) {
                  extraUTFChars = 2;
                }
                // Bytes from -64 to -49 correspond to C0 to CF
                // in hex, so top 2 bits set for UTF-8
                else if (curBytes[i] > -65 && curBytes[i] < -48) {
                  extraUTFChars = 1;
                }
              } else {
                openNLPOffset++;
                annotatorOffset++;
              }
            }
          }

          // No previous carriage return found
          else if (foundUTFStartChar) {
            extraUTFChars--;
            annotatorOffset++;
            if (extraUTFChars < 1) {
              foundUTFStartChar = false;
              annotatorToOpenNLPOffsetCorrections
                  .add(new IndexedObject<>(annotatorOffset, openNLPOffset - annotatorOffset));
            }
          } else if (curBytes[i] == RaptatConstants.CARRIAGE_RETURN_CHAR) {
            foundCRChar = true;
            openNLPOffset++;
            annotatorOffset++;
          } else if (curBytes[i] < 0) {
            foundUTFStartChar = true;
            openNLPOffset++;
            annotatorOffset++;
            // Bytes from -1 to -16 correspond to F0 to FF
            // in hex, so top 4 bits set for UTF-8
            if (curBytes[i] > -17) {
              extraUTFChars = 3;
            }
            // Bytes from -32 to -17 correspond to E0 to EF
            // in hex, so top 3 bits set for UTF-8
            else if (curBytes[i] > -33) {
              extraUTFChars = 2;
            }
            // Bytes from -64 to -49 correspond to C0 to CF
            // in hex, so top 2 bits set for UTF-8
            else if (curBytes[i] > -65 && curBytes[i] < -48) {
              extraUTFChars = 1;
            }
          } else {
            openNLPOffset++;
            annotatorOffset++;
          }
        }
        startLoopIndex = 0;
      }

      annotatorToOpenNLPOffsetComparer = new IndexedTree<>(annotatorToOpenNLPOffsetCorrections);
    } catch (FileNotFoundException e1) {
      System.out.println(e1);
      e1.printStackTrace();
      annotatorToOpenNLPOffsetComparer = null;
    } catch (IOException e) {
      System.out.println("Unable to read file for correcting XML offsets\n" + e);
      e.printStackTrace();
      annotatorToOpenNLPOffsetComparer = null;

    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        System.out.println("Unable to close file for correcting XML offsets\n" + e);
        e.printStackTrace();
        annotatorToOpenNLPOffsetComparer = null;
      }
    }

    return annotatorToOpenNLPOffsetComparer;
  }

  /**
   * *********************************************************** Determine exact location of all
   * annotated phrases within the document as determined by Open NLP and by a file reader. This
   * method creates a set of IntegerPairComparators that can be used to quickly look up how to
   * convert from an annotator (eHOST or Knowtator) XML offset to an GOpenNLP offset and from an
   * OpenNLP offset to a file reader offset.
   *
   * @param textDocumentPath
   * @param annotationSource
   * @author Glenn Gobbel - Apr 25, 2012 ***********************************************************
   */
  public static IndexedTree<IndexedObject<Integer>> calculateOffsetCorrectionsNew(
      Path textDocumentPath, AnnotationApp annotationSource) {
    List<IndexedObject<Integer>> annotatorToOpenNLPOffsetCorrections = new ArrayList<>();
    IndexedTree<IndexedObject<Integer>> offsetComparer;
    byte[] curBytes = new byte[1024];

    /*
     * We set the FSM to work on eHOST annotations by default if null or not some other source of
     * annotation (like Knowtator)
     */
    FSM annotatorFSM;
    if (annotationSource != null && annotationSource.equals(AnnotationApp.KNOWTATOR)) {
      annotatorFSM = OffsetParser.knowtatorFSM;
    } else {
      annotatorFSM = OffsetParser.ehostFSM;
    }
    annotatorFSM.setState(FsmState.START.ordinal());
    OffsetParser.openNLPFSM.setState(FsmState.START.ordinal());

    int annotatorOffset = 0, openNLPOffset = 0;
    int readLoopIndex = 0, curCharsRead = 0;
    int currentOffsetDifference = 0;

    if (hasByteOrderMark(textDocumentPath)) {
      // openNLPOffset remains at 0
      annotatorOffset = 1;
      currentOffsetDifference = openNLPOffset - annotatorOffset;
      annotatorToOpenNLPOffsetCorrections.add(new IndexedObject<>(0, currentOffsetDifference));
      readLoopIndex = 3;
    }

    try (BufferedInputStream characterReader =
        new BufferedInputStream(Files.newInputStream(textDocumentPath, StandardOpenOption.READ))) {

      int totalBytes = 0;
      while ((curCharsRead = characterReader.read(curBytes)) != -1) {
        /*
         * Note that readLoopIndex only considers the ByteOrderMark for the first time through the
         * loop. It is set to zero for subsequent iterations.
         */
        for (int i = readLoopIndex; i < curCharsRead; i++) {
          OffsetParser.LOGGER
              .debug("\ni: " + totalBytes + "  x" + Integer.toHexString(totalBytes++));
          CharacterInput character = getCharacter(curBytes[i]);
          int offsetUpdate = annotatorFSM.update(character.ordinal());
          annotatorOffset += offsetUpdate;
          OffsetParser.LOGGER
              .debug("AnnotatorOffset: " + annotatorOffset + ", offsetUpdate: " + offsetUpdate);

          offsetUpdate = OffsetParser.openNLPFSM.update(character.ordinal());
          openNLPOffset += offsetUpdate;
          OffsetParser.LOGGER
              .debug("openNLPOffset: " + openNLPOffset + ", offsetUpdate: " + offsetUpdate);

          int newOffsetDifference = openNLPOffset - annotatorOffset;
          if (newOffsetDifference != currentOffsetDifference) {
            currentOffsetDifference = newOffsetDifference;
            annotatorToOpenNLPOffsetCorrections
                .add(new IndexedObject<>(annotatorOffset, currentOffsetDifference));
            OffsetParser.LOGGER.debug("NEW OFFSET DIFFERENCE: " + newOffsetDifference);
          }
        }
        readLoopIndex = 0;
      }

      offsetComparer = new IndexedTree<>(annotatorToOpenNLPOffsetCorrections);
    } catch (FileNotFoundException e1) {
      System.out.println(e1);
      e1.printStackTrace();
      offsetComparer = null;
    } catch (IOException e) {
      System.out.println("Unable to read file for correcting XML offsets\n" + e);
      e.printStackTrace();
      offsetComparer = null;

    }

    return offsetComparer;
  }


  /**
   * *********************************************************** Determine exact location of all
   * annotated phrases within the document as determined by Open NLP and by a file reader. This
   * method creates a set of IntegerPairComparators that can be used to quickly look up how to
   * convert from an annotator (eHOST or Knowtator) XML offset to an GOpenNLP offset and from an
   * OpenNLP offset to a file reader offset.
   *
   * @param documentPathString
   * @param annotationSource
   * @author Glenn Gobbel - Apr 25, 2012 ***********************************************************
   */
  public static IndexedTree<IndexedObject<Integer>> calculateOffsetCorrectionsNew(
      String documentPathString, AnnotationApp annotationSource) {
    return calculateOffsetCorrectionsNew(Path.of(documentPathString), annotationSource);
  }

  /**
   * *********************************************************** Determine if the first 3 characters
   * of a file are consistent with the bytes offset marker (BOM) for a UTF-8 file
   *
   * @param filePath
   * @return boolean
   * @author Glenn Gobbel - Apr 18, 2012 ***********************************************************
   */
  public static boolean hasByteOrderMark(Path filePath) {
    BufferedInputStream characterReader = null;
    try {
      characterReader =
          new BufferedInputStream(Files.newInputStream(filePath, StandardOpenOption.READ));
      if (characterReader.read() == 239 && characterReader.read() == 187
          && characterReader.read() == 191) {
        characterReader.close();
        return true;
      } else {
        if (characterReader != null) {
          characterReader.close();
        }
        return false;
      }
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    } finally {
      if (characterReader != null) {
        try {
          characterReader.close();
        } catch (IOException e) {
          System.out.println(e);
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  /**
   * *********************************************************** Determine if the first 3 characters
   * of a file are consistent with the bytes offset marker (BOM) for a UTF-8 file
   *
   * @param filePathString
   * @return boolean
   * @author Glenn Gobbel - Apr 18, 2012 ***********************************************************
   */
  public static boolean hasByteOrderMark(String filePathString) {
    return hasByteOrderMark(Path.of(filePathString));
  }


  public static void main(String[] args) {}

  /*
   * Determines the input value to a finite state machine based on the value of the character read
   * in as a byte
   */
  private static CharacterInput getCharacter(byte readChar) {
    OffsetParser.LOGGER.debug("Character is:" + readChar);

    if (readChar == RaptatConstants.CARRIAGE_RETURN_CHAR) {
      OffsetParser.LOGGER.debug("Carriage return character");
      return CharacterInput.CR;
    }
    if (readChar == RaptatConstants.LINE_FEED_CHAR) {
      OffsetParser.LOGGER.debug("Line feed character");
      return CharacterInput.LF;
    }

    if (readChar < 0) {
      OffsetParser.LOGGER.debug("UTF character");
      // Bytes from -1 to -16 correspond to F0 to FF
      // in hex, so top 4 bits set for UTF-8
      if (readChar > -17) {
        return CharacterInput.UTF3;
      }
      // Bytes from -32 to -17 correspond to E0 to EF
      // in hex, so top 3 bits set for UTF-8
      if (readChar > -33) {
        return CharacterInput.UTF2;
      }
      // Bytes from -64 to -49 correspond to C0 to CF
      // in hex, so top 2 bits set for UTF-8
      if (readChar > -65 && readChar < -48) {
        return CharacterInput.UTF1;
      }
    }

    OffsetParser.LOGGER.debug("Other character");
    return CharacterInput.OC;
  }

  private static int[][][] getKnowtatorFsmSettings() {
    int[][][] settings = new int[][][] {
        /*
         * Each of the below lists indicates the change in character offset (emission) when
         * transitioning from one state to another, given a particular input as a character. The
         * input is translated to a CharacterInput enum, either OC, CR, LF, UTF1, UTF2, or UTF3,
         * where OC stands for "OtherCharacter", CR="CarriageReturn", LF="LineFeed", and UTF1 is a
         * UTF character with 1 byte encoding (UTF2 and UTF3 and 2 and 3 byte encoding)
         */
        /* START_STATE Transitions and Emissions */
        {{FsmState.START.ordinal(), 1}, {FsmState.CR.ordinal(), 1}, {FsmState.START.ordinal(), 1},
            {FsmState.UTF1.ordinal(), 1}, {FsmState.UTF2.ordinal(), 1},
            {FsmState.UTF3.ordinal(), 1}},
        /* CR_STATE Transitions and Emissions */
        {{FsmState.START.ordinal(), 1}, {FsmState.CR.ordinal(), 1}, {FsmState.START.ordinal(), 0},
            {FsmState.UTF1.ordinal(), 1}, {FsmState.UTF2.ordinal(), 1},
            {FsmState.UTF3.ordinal(), 1}},
        /* UTF1_STATE Transitions and Emissions */
        {{FsmState.START.ordinal(), 0}, {FsmState.START.ordinal(), 0},
            {FsmState.START.ordinal(), 0}, {FsmState.START.ordinal(), 0},
            {FsmState.START.ordinal(), 0}, {FsmState.START.ordinal(), 0}},
        /* UTF2_STATE Transitions and Emissions */
        {{FsmState.UTF1.ordinal(), 0}, {FsmState.UTF1.ordinal(), 0}, {FsmState.UTF1.ordinal(), 0},
            {FsmState.UTF1.ordinal(), 0}, {FsmState.UTF1.ordinal(), 0},
            {FsmState.UTF1.ordinal(), 0}},
        /* UTF3_STATE Transitions and Emissions */
        {{FsmState.UTF2.ordinal(), 0}, {FsmState.UTF2.ordinal(), 0}, {FsmState.UTF2.ordinal(), 0},
            {FsmState.UTF2.ordinal(), 0}, {FsmState.UTF2.ordinal(), 0},
            {FsmState.UTF2.ordinal(), 0}}};
    return settings;
  }
}
