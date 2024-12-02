package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

public class OptionTree extends Hashtable<Character, OptionTree> {
  private static final long serialVersionUID = -5101143536214674860L;
  private static int numberOfColumns;
  private static int curColumnNumber;
  private String fMeasure = "";


  /** @return the fMeasure */
  public String getfMeasure() {
    return this.fMeasure;
  }


  public void print() {
    Enumeration<Character> theKeys = keys();
    while (theKeys.hasMoreElements()) {
      Character curChar = theKeys.nextElement();
      System.out.print(curChar);
      OptionTree nextTree = get(curChar);
      if (nextTree != null) {
        if (nextTree.fMeasure.length() > 0) {
          System.out.println(":" + nextTree.fMeasure);
        } else {
          nextTree.print();
        }
      }
    }
  }


  public void printToWriter(PrintWriter resultPW, int numberOfColumns) {
    OptionTree.numberOfColumns = numberOfColumns;
    OptionTree.curColumnNumber = 0;
    printToWriterHelper(resultPW);
  }


  /**
   * @param fMeasure the fMeasure to set
   */
  public void setfMeasure(String fMeasure) {
    this.fMeasure = fMeasure;
  }


  private void printToWriterHelper(PrintWriter resultPW) {
    Enumeration<Character> theKeys = keys();
    while (theKeys.hasMoreElements()) {
      Character curChar = theKeys.nextElement();
      OptionTree nextTree = get(curChar);
      if (nextTree != null) {
        if (nextTree.fMeasure.length() > 0) {
          resultPW.print(nextTree.fMeasure);
          if (++OptionTree.curColumnNumber != OptionTree.numberOfColumns) {
            resultPW.print(",");
          } else {
            resultPW.println();
            OptionTree.curColumnNumber = 0;
          }
        } else {
          nextTree.printToWriterHelper(resultPW);
        }
      }
    }
  }
}
