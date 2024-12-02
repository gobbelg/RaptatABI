package src.main.gov.va.vha09.grecc.raptat.gg.importer.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/**
 * ********************************************************** Reads in a file containing training
 * data for a naive Bayes network. Reads the file in one line at a time.
 *
 * @author Glenn T. Gobbel, Jul 6, 2010
 *         <p>
 *         *********************************************************
 */
public class CSVReader extends DataImportReader {
  protected BufferedReader inReader = null;
  protected File inFile = null;


  public CSVReader() {}


  public CSVReader(boolean getStartTokensPosition) {
    this(GeneralHelper.getFile("Select .csv file for input"), getStartTokensPosition);
  }


  /**
   * *************************************************************** Constructor to generate an
   * object for reading in comma separated value (.csv) files. Finds the first line in file
   * containing valid data and assumes that line to contain the header. It sets the file reading
   * pointer to the line after the header so that the first call to getNextData( ) will return the
   * first line of data. Leaves inReader set to null if unable to create this, so caller should
   * check isValid() status after constructor call.
   *
   * @param inFile - File to read data from
   *        ***************************************************************
   */
  public CSVReader(File readFile) {
    super();
    setFileDirectory(readFile.getParent());

    if (readFile != null) {
      try {
        this.inFile = readFile;
        this.inReader = new BufferedReader(new FileReader(readFile));
      } catch (IOException e) {
        System.err.println("Unable to create CSV Reader - " + "             File does not exist");
        this.inReader = null;
      }
    }

    // Read first line of file to determine how many elements there are
    // in the file
    if (this.inReader != null) {
      String[] allData;
      try {
        this.readerLength = GeneralHelper.countNonEmptyFileLines(this.inFile);

        // Keep reading until we find a line of at least 1 element
        // containing data
        long linesRead = 0;
        do {
          allData = this.inReader.readLine().split(",");
          linesRead++;
        } while (allData == null || allData.length < 1);

        if (allData != null) {
          // Correct readerLength for empty lines
          this.readerLength -= linesRead;
          this.numDataElements = allData.length;
          this.startTokensPosition = 0; // Default setting
        }

      } catch (IOException e) {
        System.out.println("Unable to read data");
        e.printStackTrace();
        this.inReader = null;
      }
    }

    if (!isValid()) {
      this.inReader = null;
    }
  }


  /**
   * *************************************************************** Overloaded constructor to allow
   * user to input the column number of the input file that contains the first token in the phrase.
   * This is converted to a zero indexed value. Leaves inReader set to null if unable to create
   * this, so caller should check isValid() status after constructor call.
   *
   * @param inFile - File to read data from
   *        ***************************************************************
   */
  public CSVReader(File readFile, boolean getStartTokensPosition) {
    this(readFile);

    if (isValid()) {
      if (getStartTokensPosition) {
        this.startTokensPosition = getGroupingColumns();
        if (this.startTokensPosition < 0) // Indicates invalid entry
        {
          this.inReader = null;
        }
      }
      // Set startTokensPosition to default if we are not retrieving it
      else {
        this.startTokensPosition = 0;
      }
    }
  }


  public CSVReader(String inputText) {
    this(GeneralHelper.getFile(inputText), false);
  }


  public CSVReader(String baseFileDirectory, boolean getStartTokensPosition, String textPrompt) {
    this(GeneralHelper.getFile(textPrompt, baseFileDirectory), getStartTokensPosition);
  }


  @Override
  public void close() {
    try {
      if (this.inReader != null) {
        this.inReader.close();
      }
    } catch (IOException e) {
      System.out.println("Unable to close CSVReader inReader");
      e.printStackTrace();
    }
  }


  public int getGroupingColumns() {
    int i = -1;

    // Note 2 is the warning message icon parameter as the
    // QUESTION_MESSAGE icon does not seem to work.
    String s = JOptionPane.showInputDialog(null,
        "<html>How many grouping columns" + "<br>precede first token?</html>",
        "Number of Grouping Columns", 2);

    if (s == null) {
      return -1;
    }
    try {
      i = Integer.parseInt(s.trim());
    } catch (NumberFormatException nfe) {
      System.out.println("NumberFormatException: " + nfe.getMessage());
      JOptionPane.showMessageDialog(null, "Enter a valid non-negative integer");
      getGroupingColumns();
    }
    return i;
  }


  public String getInFileDirectory() {
    if (this.inFile != null) {
      return this.inFile.getParent();
    }

    return null;
  }


  /**
   * ************************************************************** Reads in the next line of data
   * in the file with each element generating one element of a String array.
   *
   * @return String [] - Array containing elements from the next line
   *         **************************************************************
   */
  @Override
  public String[] getNextData() {
    String allData;
    try {
      allData = this.inReader.readLine();
      if (allData != null) {
        return allData.split(",");
      }
    } catch (Exception e) {
      System.out.println("Unable to read line of data");
      e.printStackTrace();
    }

    return null;
  }


  /**
   * ************************************************************** Reads in the next line of data
   * in the file with each element generating one element of a String array. The elements before
   * startColumn from position 0 to startColumn - 1 are not returned.
   *
   * @return String [] - Array containing elements from the next line
   *         **************************************************************
   */
  @Override
  public String[] getNextData(int startElement) {
    String allData;
    try {
      allData = this.inReader.readLine();
      if (allData != null) {
        // allData = allData.toUpperCase(Locale.ENGLISH);
        return GeneralHelper.getSubArray(allData.split(","), startElement);
      }
    } catch (Exception e) {
      System.out.println("Unable to read line of data");
      e.printStackTrace();
    }

    return null;
  }


  @Override
  public boolean isValid() {
    return this.inReader != null && super.isValid();
  }


  /**
   * *************************************************************** Resets the reader to the
   * beginning of the data. Note that it skips the non-data lines preceding the actual data.
   * ***************************************************************
   */
  @Override
  public void reset() {
    try {
      if (this.inReader != null) {
        this.inReader.close();
        this.inReader = new BufferedReader(new FileReader(this.inFile));
      }
    } catch (IOException e) {
      System.out.println("Problem with reset of buffered reader in CSV reader");
      System.out.println("Unable to close inReader");
      e.printStackTrace();
    }

    if (this.inReader != null) {
      try {
        // Keep reading until we find a line of at least 1 element
        // containing data
        String[] allData;
        do {
          allData = this.inReader.readLine().split(",");
        } while (allData == null || allData.length < 1);
      } catch (IOException e) {
        System.out.println("Unable to read data after reset of CSV reader");
        e.printStackTrace();
        this.inReader = null;
      }
    }
  }
}
