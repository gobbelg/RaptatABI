package src.main.gov.va.vha09.grecc.raptat.gg.resultwriters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** ResultWriter -
 *
 * @author Glenn Gobbel, Mar 8, 2011
 *         <p>
 *         *********************************************************
 */
public abstract class ResultWriter {
  protected PrintWriter resultPW;
  protected String fileTitle = "";
  protected String delimiter = "\t";


  /**
   * *************************************************************** ResultWriter - Class to create
   * a file to write the results from a test of a machine learning algorithm. It generates a file to
   * write to at construction, writes a new line of text when given a String array, and closes the
   * file when prompted
   *
   * @author Glenn Gobbel, July 19, 2010
   * @param fileDirectoryName
   * @param titleAppend
   * @param resultTitles ***************************************************************
   */
  public ResultWriter(String fileDirectoryName, String titleAppend, String[] resultTitles) {
    this.fileTitle = fileDirectoryName + File.separator + "Res" + titleAppend + "_"
        + GeneralHelper.getTimeStamp();

    initializeWriter(resultTitles);
  }


  public ResultWriter(String fileDirectoryName, String titleAppend, String[] resultTitles,
      String delimiter) {
    this.delimiter = delimiter;
    this.fileTitle =
        fileDirectoryName + "RapTAT_Res_" + titleAppend + "_" + GeneralHelper.getTimeStamp();

    initializeWriter(resultTitles);
  }


  /**
   * *************************************************************** ResultWriter - Class to create
   * a file to write the results from a test of a naive Bayes network. It generates a file to write
   * to at construction, writes a new line of text when given a String array, and closes the file
   * when prompted
   *
   * @author Glenn Gobbel, July 19, 2010
   * @param fileDirectory
   * @param resultTitles ***************************************************************
   */
  public ResultWriter(String fileDirectory, String[] resultTitles) {
    String lastCharacter = fileDirectory.substring(fileDirectory.length() - 1);
    String separator = File.separator.equals(lastCharacter) ? "" : File.separator;

    this.fileTitle = fileDirectory + separator + "RapTAT_Res_" + GeneralHelper.getTimeStamp();

    initializeWriter(resultTitles);
  }


  /**
   * *************************************************************** ResultWriter - Class to create
   * a file to write the results from a test of a naive Bayes network. It generates a file to write
   * to at construction, writes a new line of text when given a String array, and closes the file
   * when prompted
   *
   * @author Glenn Gobbel, July 19, 2010
   * @param fileDirectory
   * @param resultTitles ***************************************************************
   */
  public ResultWriter(String fileDirectory, String[] resultTitles, String inputDelimiter) {
    this.delimiter = inputDelimiter;
    this.fileTitle = fileDirectory + "RapTAT_Res_" + GeneralHelper.getTimeStamp();

    initializeWriter(resultTitles);
  }


  /**
   * *************************************************************** Construct an object that can be
   * used to write results of machine learning evaluation or analysis
   *
   * @param resultTitles ***************************************************************
   */
  public ResultWriter(String[] resultTitles) {
    this.fileTitle =
        JOptionPane.showInputDialog("Enter name of result file", JOptionPane.OK_CANCEL_OPTION);
    if (this.fileTitle == null) {
      System.exit(0);
    }
    if (this.fileTitle.length() < 1) {
      this.fileTitle = "RapTAT_Res_" + GeneralHelper.getTimeStamp();
    }

    initializeWriter(resultTitles);
  }


  protected ResultWriter() {
    super();
  }


  /**
   * ************************************************************** Flush and close the ResultWriter
   *
   * <p>
   * void **************************************************************
   */
  public void close() {
    this.resultPW.flush();
    this.resultPW.close();
  }


  /** @return the delimiter */
  public String getDelimiter() {
    return this.delimiter;
  }


  /**
   * **********************************************************
   *
   * @return the fileTitle **********************************************************
   */
  public String getResultFileTitle() {
    return this.fileTitle;
  }


  /**
   * @param delimiter the delimiter to set
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }


  public void write(String theString) {
    if (theString != null) {
      this.resultPW.print(theString);
      this.resultPW.flush();
    }
  }


  /**
   * ************************************************************** Write data in String [] to the
   * file specified within the out field of the BayesResultWriter object class
   *
   * @param curData Data to be written
   *        **************************************************************
   */
  public void write(String[] curData) {
    if (curData != null) {
      this.resultPW.print(curData[0]);
      for (int i = 1; i < curData.length; i++) {
        this.resultPW.print(this.delimiter + curData[i]);
      }
    }
    this.resultPW.flush();
  }


  /**
   * ************************************************************** Write data in String [] to the
   * file specified within the out field of the BayesResultWriter object class
   *
   * @param curData Data to be written
   *        **************************************************************
   */
  public void write(String[] curData, String separatorString) throws IOException {
    if (curData != null) {
      this.resultPW.print(curData[0]);
      for (int i = 1; i < curData.length; i++) {
        this.resultPW.print(separatorString + curData[i]);
        this.resultPW.flush();
      }
    }
  }


  /**
   * Write a string as a line of text
   *
   * @param curData Data to be written
   */
  public void writeln(String curData) throws IOException {
    if (curData != null) {
      this.write(curData);
      this.resultPW.println();
      this.resultPW.flush();
    }
  }


  /**
   * ************************************************************** Write a line of data to the file
   * specified within the out field of the BayesResultWriter object class
   *
   * @param curData Data to be written
   *        **************************************************************
   */
  public void writeln(String[] curData) {
    if (curData != null) {
      this.write(curData);
      this.resultPW.println();
      this.resultPW.flush();
    }
  }


  /**
   * ************************************************************** Write a line of data to the file
   * specified within the out field of the BayesResultWriter object class
   *
   * @param curData Data to be written
   *        **************************************************************
   */
  public void writeln(String[] curData, String dataSeparator) {
    try {
      if (curData != null) {
        this.write(curData, dataSeparator);
        this.resultPW.flush();
      }
      this.resultPW.println();
    } catch (IOException e) {

    }
  }


  /**
   * **************************************************************
   *
   * @param compiledResults - Line by line listing of the results from each record
   *        **************************************************************
   */
  public void writeResults(List<String[]> compiledResults) {
    if (compiledResults == null) {
      System.out.println("Results are null");
      GeneralHelper.errorWriter("Results are null");
    } else {
      Iterator<String[]> e = compiledResults.iterator();
      while (e.hasNext()) {
        this.writeln(e.next());
      }
    }
  }


  private void initializeWriter(String[] resultTitles) {
    try {
      String saveFile;
      if (this.delimiter.equals("\t")) {
        saveFile = this.fileTitle + ".txt";
      } else {
        saveFile = this.fileTitle + ".csv";
      }
      File resFile = new File(saveFile);
      this.resultPW = new PrintWriter(new BufferedWriter(new FileWriter(resFile, false)), true);
      this.writeln(resultTitles);
    } catch (IOException e) {
      System.out.println("Unable to create result file");
      e.printStackTrace();
      System.exit(0);
    }
  }
}
