package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OptionTree;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.TextReader;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Jul 19, 2012 *****************************************************
 */
public class RaptatResultGenerator {
  /**
   * ***********************************************************
   *
   * @param theResultTree
   * @param charArray
   * @param curConcept
   * @param curF
   * @author Glenn Gobbel - Jul 19, 2012 ***********************************************************
   */
  private void addToTree(Hashtable<String, OptionTree> theResultTree, char[] charArray,
      String curConcept, String curF) {
    OptionTree nextTree;
    if (theResultTree.containsKey(curConcept)) {
      nextTree = theResultTree.get(curConcept);
    } else {
      nextTree = new OptionTree();
      theResultTree.put(curConcept, nextTree);
    }
    this.addToTree(nextTree, charArray, curF);
  }


  /**
   * ***********************************************************
   *
   * @param theResultTree
   * @param theChars
   * @param curF
   * @author Glenn Gobbel - Jul 19, 2012 ***********************************************************
   */
  private void addToTree(OptionTree theResultTree, char[] theChars, String curF) {
    char curChar = theChars[0];
    OptionTree newTree;

    if (!theResultTree.containsKey(curChar)) {
      newTree = new OptionTree();
      theResultTree.put(curChar, newTree);
    } else {
      newTree = theResultTree.get(curChar);
    }
    if (theChars.length < 2) {
      newTree.setfMeasure(curF);
    } else {
      this.addToTree(newTree, GeneralHelper.getSubArray(theChars, 1), curF);
    }
  }


  private int getNumberOfColumns(int numberOfFiles) {
    int res = (int) Math.sqrt(numberOfFiles);
    if (numberOfFiles % res == 0) {
      return res;
    }
    while (--res > 1) {
      if (numberOfFiles % res == 0) {
        return res;
      }
    }
    return res;
  }


  public static void main(String[] args) {
    // RaptatResultGenerator theResultReader = new RaptatResultGenerator();
    List<String> resultList = new ArrayList<>();
    // Hashtable<String, OptionTree> theResultTree = new Hashtable<String,
    // OptionTree>();

    File primaryDirectory = GeneralHelper.getDirectory("Choose the directory", "");

    File[] theFolders = primaryDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File arg0) {
        if (arg0.isDirectory() & arg0.getName().startsWith("Result")) {
          return true;
        }
        return false;
      }
    });

    // Put result data in each folder into the ResultTree
    TextReader theReader;
    // int numberOfFiles = 0;
    for (File curFolder : theFolders) {
      System.out.println(curFolder.getAbsolutePath());
      String[] optionSettings = curFolder.getName().split("_");
      String[] optionChars = optionSettings[4].split("");
      String[] allOptions = new String[7];
      for (int i = 0, j = 4; i < 5; i++, j--) {
        if (optionChars[j].matches("[A-Z]")) {
          allOptions[i] = optionChars[j] + "C";
        } else {
          allOptions[i] = optionChars[j];
        }
      }
      allOptions[5] = optionSettings[3];
      allOptions[6] = optionSettings[5].trim();
      String optionString = GeneralHelper.arrayToDelimString(allOptions, ",");
      File resultFile = curFolder.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          if (pathname.getName().contains("Concept")) {
            return true;
          }
          return false;
        }
      })[0];

      if (resultFile != null) {
        // numberOfFiles++ ;
        theReader = new TextReader(resultFile, false);

        String[] curLine;
        while ((curLine = theReader.getNextData()) != null) {
          String curConcept = curLine[0];
          String curPrec = curLine[4];
          String curRecall = curLine[5];
          String curF = curLine[6];
          String tp = curLine[1];
          String fp = curLine[2];
          String fn = curLine[3];
          System.out.println(curConcept + ":" + tp + ", " + fp + ", " + fn + ", " + curPrec + ", "
              + curRecall + ", " + curF);
          String curResult = optionString + "," + tp + ", " + fp + ", " + fn + ", " + curConcept
              + "," + curPrec + "," + curRecall + "," + curF;
          resultList.add(curResult);
          // theResultReader.addToTree( theResultTree,
          // optionString.toCharArray(), curConcept, curF );
        }
      }
    }

    // Now print out the OptionTree associated with each ResultTree
    // to a file
    // int columnsInFile = theResultReader.getNumberOfColumns( numberOfFiles
    // );
    // Enumeration<String> allConcepts = theResultTree.keys();
    // while (allConcepts.hasMoreElements())
    // {
    // String curConcept = allConcepts.nextElement();
    // System.out.println( "\n\n" + curConcept.toUpperCase() + "::" );
    // OptionTree curConceptTree = theResultTree.get( curConcept );
    // curConceptTree.print();
    //
    // String resultFileName = curConcept + "_Result_"
    // + System.currentTimeMillis() + ".csv";
    // resultFileName = primaryDirectory + File.separator + resultFileName;
    // File resultFile = new File( resultFileName );
    //
    // try
    // {
    // PrintWriter resultPW = new PrintWriter( new BufferedWriter(
    // new FileWriter( resultFile, false ) ), true );
    // curConceptTree.printToWriter( resultPW, columnsInFile );
    // }
    // catch (IOException e)
    // {
    // System.out.println( e );
    // e.printStackTrace();
    // }
    // }

    String resultFileName = "Result_" + System.currentTimeMillis() + ".csv";
    resultFileName = primaryDirectory + File.separator + resultFileName;
    File resultFile = new File(resultFileName);

    try {
      PrintWriter resultPW =
          new PrintWriter(new BufferedWriter(new FileWriter(resultFile, false)), true);
      for (String curResult : resultList) {
        resultPW.println(curResult);
      }
      resultPW.close();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
