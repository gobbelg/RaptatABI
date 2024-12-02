/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

/**
 * Takes phrases like "segments 1 and 2 of the liver" and makes sure that numbers are consistent. If
 * the first number is less than the second, then they are inconsistent. This is used for generating
 * phraseVariants of locations in the liver
 *
 * @author VHATVHGOBBEG
 */
public class LiverSegmentConjunctionProcessor {

  private String conjunctionString;

  public LiverSegmentConjunctionProcessor(String conjunctionString) {
    this.conjunctionString = conjunctionString;
  }


  public int getValueOf(String segmentNumber) throws IllegalArgumentException {
    segmentNumber = segmentNumber.toLowerCase().trim();

    switch (segmentNumber) {
      case "1":
        return 1;
      case "2":
        return 2;
      case "3":
        return 3;
      case "4":
        return 4;
      case "5":
        return 5;
      case "6":
        return 6;
      case "7":
        return 7;
      case "8":
        return 8;
      case "one":
        return 1;
      case "two":
        return 2;
      case "three":
        return 3;
      case "four":
        return 4;
      case "five":
        return 5;
      case "six":
        return 6;
      case "seven":
        return 7;
      case "eight":
        return 8;
      case "i":
        return 1;
      case "ii":
        return 2;
      case "iii":
        return 3;
      case "iv":
        return 4;
      case "v":
        return 5;
      case "vi":
        return 6;
      case "vii":
        return 7;
      case "viii":
        return 8;
      case "ia":
        return 1;
      case "iia":
        return 2;
      case "iiia":
        return 3;
      case "iva":
        return 4;
      case "va":
        return 5;
      case "via":
        return 6;
      case "viia":
        return 7;
      case "viiia":
        return 8;
      default:
        throw new IllegalArgumentException();
    }
  }


  public boolean validSegments(String stringWithSegments) throws IllegalArgumentException {
    String[] tokenStrings = stringWithSegments.split("\\s+");
    int segmentConjunction = findIndex(tokenStrings, this.conjunctionString);

    /*
     * segmentConjunction will be less than zero is not found and it should occur somewhere in the
     * middle of the tokenStrings (not first or last)
     */
    if (segmentConjunction > 0 && segmentConjunction < tokenStrings.length - 1) {
      return this.validSegments(tokenStrings[segmentConjunction - 1],
          tokenStrings[segmentConjunction + 1]);
    } else {
      System.err.println("Conjunction string not found or not at correct index");
      throw new IllegalArgumentException();
    }
  }


  public boolean validSegments(String segment01, String segment02) {
    boolean isValid = false;
    try {
      isValid = getValueOf(segment02) > getValueOf(segment01);
    } catch (IllegalArgumentException e) {
      System.err.append("Invalid liver segment name");
      System.exit(-1);
    }

    return isValid;
  }


  private int findIndex(String[] tokenStrings, String matchString) {
    for (int i = 0; i < tokenStrings.length; i++) {
      if (tokenStrings[i].equals(matchString)) {
        return i;
      }
    }
    return -1;
  }


  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
}
