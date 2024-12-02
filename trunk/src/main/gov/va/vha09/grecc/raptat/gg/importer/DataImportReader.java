/** */
package src.main.gov.va.vha09.grecc.raptat.gg.importer;

import javax.swing.JOptionPane;

/**
 * ********************************************************** DataImportReader -
 *
 * @author Glenn Gobbel, Jan 30, 2011
 *         <p>
 *         *********************************************************
 */
public abstract class DataImportReader {

  // Number of items in data including grouping variables
  // and concept
  protected int numDataElements = 0;

  // The data element at which the phrase tokens start - this is after
  // the columns containing grouping variables. Note that this is
  // zero indexed
  protected int startTokensPosition = -1;
  protected long readerLength;
  private String fileDirectory;
  private String fileName;
  private String fileType;


  public DataImportReader() {}


  public abstract void close(); // Close the file reader if associated with
  // this


  public String getFileDirectory() {
    return this.fileDirectory;
  }


  public String getFileName() {
    return this.fileName;
  }


  public String getFileType() {
    return this.fileType;
  }


  public abstract String[] getNextData();


  // Returns data starting from position startElement; elements
  // before that are not returned
  public abstract String[] getNextData(int startElement);


  /**
   * **********************************************************
   *
   * @return the numDataElements **********************************************************
   */
  public int getNumDataElements() {
    return this.numDataElements;
  }


  /**
   * ************************************************************** Returns the number of tokens in
   * the phrase if the position in the data where phrase tokens starts is known. Otherwise, returns
   * -1
   *
   * @return int **************************************************************
   */
  public int getNumPhraseTokens() {
    if (this.startTokensPosition >= 0) {
      return this.numDataElements - this.startTokensPosition - 1;
    }
    return -1;
  }


  /**
   * **********************************************************
   *
   * @return the readerLength **********************************************************
   */
  public long getReaderLength() {
    return this.readerLength;
  }


  /**
   * **********************************************************
   *
   * @return the startTokensPosition **********************************************************
   */
  public int getStartTokensPosition() {
    return this.startTokensPosition;
  }


  /**
   * ************************************************************** Determines if the inReader has
   * sufficient data elements to have a valid file
   *
   * @return boolean **************************************************************
   */
  public boolean isValid() {
    int numTokens = this.numDataElements - this.startTokensPosition;
    if (numTokens <= 0) {
      JOptionPane.showMessageDialog(null, "Insufficient data elements in file", "File Error",
          JOptionPane.WARNING_MESSAGE);
    }
    if (this.startTokensPosition < 0) {
      JOptionPane.showMessageDialog(null, "Grouping column must be non-negative integer",
          "File Error", JOptionPane.WARNING_MESSAGE);
    }
    return numTokens > 0 && this.startTokensPosition > -1;
  }


  public abstract void reset(); // Reset to beginning of data


  public void setFileDirectory(String theDirectory) {
    this.fileDirectory = theDirectory;
  }


  public void setFileName(String fileName) {
    this.fileName = fileName;
  }


  public void setFileType(String fileType) {
    this.fileType = fileType;
  }
}
