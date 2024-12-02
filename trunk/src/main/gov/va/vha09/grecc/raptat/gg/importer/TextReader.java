package src.main.gov.va.vha09.grecc.raptat.gg.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** Reads in a file containing training
 * data for a naive Bayes network. Reads the file in one line at a time.
 *
 * @author Glenn T. Gobbel, Jul 6, 2010
 *         <p>
 *         *********************************************************
 */
public class TextReader extends DataImportReader {
  protected BufferedReader inReader = null;
  private File inFile = null;
  protected String delimiter = "\t";
  private String[] columnNames;
  private boolean useFirstRowAsColumns;


  public TextReader() {}


  public TextReader(boolean getStartTokensPosition) {
    this(GeneralHelper.getFile("Select .txt file for input"), getStartTokensPosition);
  }


  /**
   * *************************************************************** Constructor to generate an
   * object for reading in tab separated value (.txt) files. Finds the first line in file containing
   * valid data and assumes that line to contain the header. It sets the file reading pointer to the
   * line after the header so that the first call to getNextData( ) will return the first line of
   * data. Leaves inReader set to null if unable to create this, so caller should check isValid()
   * status after constructor call.
   *
   * @param inFile - File to read data from
   *        ***************************************************************
   */
  public TextReader(File readFile) {
    super();
    try {
      this.inFile = readFile;
      this.inReader = new BufferedReader(new FileReader(readFile));
    } catch (IOException e) {
      System.out.println("Unable to create Text Reader - " + "             File does not exist");
      this.inReader = null;
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

          allData = this.inReader.readLine().split(this.delimiter);
          /**
           * 6/12/19 - DAX - Persist the first column with the assumption that it represents the
           * column names. May not always be true, but in this case it serves the purposes of
           * pulling out column names in order to map to eHost file
           */
          this.columnNames = allData;
          linesRead++;
        } while (allData == null || allData.length < 1);

        if (allData != null) {
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
      JOptionPane.showMessageDialog(null, "Unable to find valid line in training file",
          "File Error", JOptionPane.WARNING_MESSAGE);
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
  public TextReader(File readFile, boolean getStartTokensPosition) {
    this(readFile);

    if (isValid() && getStartTokensPosition) {
      this.startTokensPosition = getGroupingColumns();
      if (this.startTokensPosition < 0) // Indicates invalid entry
      {
        this.inReader = null;
      }
    }
  }


  public TextReader(String inputText) {
    this(GeneralHelper.getFile(inputText), false);
  }


  public TextReader(String baseFileDirectory, boolean getStartTokensPosition) {
    this(GeneralHelper.getFile("Select .txt file for input", baseFileDirectory),
        getStartTokensPosition);
  }


  @Override
  public void close() {
    try {
      if (this.inReader != null) {
        this.inReader.close();
      }
    } catch (IOException e) {
      System.out.println("Unable to close TextReader inReader");
      e.printStackTrace();
    }
  }


  /**
   * 6/12/19 - DAX - Supporting methods - Persist the first column with the assumption that it
   * represents the column names. May not always be true, but in this case it serves the purposes of
   * pulling out column names in order to map to eHost file
   */
  public String[] getColumnNames() {
    return this.columnNames;
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
        return allData.split(this.delimiter);
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
        return GeneralHelper.getSubArray(allData.split(this.delimiter), startElement);
      }
    } catch (Exception e) {
      System.out.println("Unable to read line of data");
      e.printStackTrace();
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
  public String[] getNextData(String inputSplitter) {
    String allData;
    try {
      allData = this.inReader.readLine();
      if (allData != null) {
        return allData.split(inputSplitter);
      }
    } catch (Exception e) {
      System.out.println("Unable to read line of data");
      e.printStackTrace();
    }

    return null;
  }


  public boolean isUseFirstRowAsColumns() {
    return this.useFirstRowAsColumns;
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
      System.out.println("Problem with reset of buffered reader in TextReader");
      System.out.println("Unable to close inReader");
      e.printStackTrace();
    }

    if (this.inReader != null) {
      try {
        // Keep reading until we find a line of at least 1 element
        // containing data
        String[] allData;
        do {
          allData = this.inReader.readLine().split(this.delimiter);
        } while (allData == null || allData.length < 1);
      } catch (IOException e) {
        System.out.println("Unable to read data after reset of Text reader");
        e.printStackTrace();
        this.inReader = null;
      }
    }
  }


  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }


  public void setFile(File inFile) {
    this.inFile = inFile;
    try {
      this.inReader = new BufferedReader(new FileReader(this.inFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  public void setUseFirstRowAsColumns(boolean useFirstRowAsColumns) {
    this.useFirstRowAsColumns = useFirstRowAsColumns;
  }


  public void useFirstRowAsColumns(boolean useFirstRowAsColumns) {
    setUseFirstRowAsColumns(true);
  }
}
