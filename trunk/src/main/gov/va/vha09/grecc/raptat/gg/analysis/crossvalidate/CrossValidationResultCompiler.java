package src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate;

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
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Jul 19, 2012 *****************************************************
 */
public class CrossValidationResultCompiler {
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


  public static void compileResults(File primaryDirectory) {
    List<String> resultList = new ArrayList<>();

    File[] theFolders = primaryDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File arg0) {
        if (arg0.isDirectory()) {
          return true;
        }
        return false;
      }
    });

    // Put result data in each folder into the ResultTree
    DataImportReader theReader;
    // int numberOfFiles = 0;
    for (File curFolder : theFolders) {
      System.out.println(curFolder.getAbsolutePath());
      String[] optionSettings = curFolder.getName().split("_");
      String[] optionChars = optionSettings[4].split("");
      String[] allOptions = new String[7];

      int endMaxTokensString = maxTokensStringEnd(optionChars);

      try {
        allOptions[4] =
            GeneralHelper.subArrayToDelimString(optionChars, 1, endMaxTokensString - 1, "");
      } catch (Exception e) {
        System.out.println("Error in RaptatResultGenerator");
        System.out.println(e.getMessage());
      }
      allOptions[3] = optionChars[endMaxTokensString++];
      allOptions[2] = optionChars[endMaxTokensString++];
      allOptions[1] = optionChars[endMaxTokensString++];
      allOptions[0] = optionChars[endMaxTokensString++];

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
        theReader = new TabReader(resultFile);
        String[] curLine;
        while ((curLine = theReader.getNextData()) != null) {
          String curConcept = curLine[0];
          String curPrec = curLine[4];
          String curRecall = curLine[5];
          String curF = curLine[6];
          System.out.println(curConcept + ":" + curPrec + ", " + curRecall + ", " + curF);
          String curResult =
              optionString + "," + curConcept + "," + curPrec + "," + curRecall + "," + curF;
          resultList.add(curResult);
          // theResultReader.addToTree( theResultTree,
          // optionString.toCharArray(), curConcept, curF );
        }
      }
    }

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


  public static void main(String[] args) {
    // RaptatResultGenerator theResultReader = new RaptatResultGenerator();
    // Hashtable<String, OptionTree> theResultTree = new Hashtable<String,
    // OptionTree>();

    compileResults(GeneralHelper.getDirectory("Choose the directory", ""));
  }


  /**
   * @param optionChars
   * @return
   */
  private static int maxTokensStringEnd(String[] theChars) {
    int stringEnd = 1;
    try {
      // Note that we compare stringLength to theChars.length -1
      // and add 1 to stringLength for theChars[stringLenght + 1]
      // because of the fact that the first charaacter is nil
      // because of splitting on "", an empty string.
      for (; stringEnd < theChars.length; stringEnd++) {
        Integer.parseInt(theChars[stringEnd]);
      }
    } catch (NumberFormatException e) {
      return stringEnd;
    }
    return stringEnd;
  }
}
