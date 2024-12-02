package src.main.gov.va.vha09.grecc.raptat.gg.importer;

import java.io.File;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import opennlp.tools.tokenize.Tokenizer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.TokenizerFactory;

/**
 * ****************************************************** Class created to extend the CSVReader
 * class. This one will tokenize the column that contains a string that is untokenized. Its use is
 * with training or running the concept mappiing system, where the phrase is not already tokenized.
 *
 * @author Glenn Gobbel - Aug 16, 2013 *****************************************************
 */
public class TokenizingTextReader extends TextReader {

  Tokenizer tokenizer = TokenizerFactory.getInstance();

  int maxTokens = OptionsManager.getInstance().getTrainingTokenNumber();

  public TokenizingTextReader() {
    super();
  }


  public TokenizingTextReader(File readFile, boolean getStartTokensPosition) {
    super(readFile, getStartTokensPosition);
  }


  @Override
  public void close() {
    // Unregister use of tokenizer
    // TokenizerFactory.release();
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
        return tokenize(allData.split(this.delimiter));
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
        return tokenize(GeneralHelper.getSubArray(allData.split(this.delimiter), startElement));
      }
    } catch (Exception e) {
      System.out.println("Unable to read line of data");
      e.printStackTrace();
    }

    return null;
  }


  /**
   * ************************************************************** Returns the number of tokens in
   * the phrase if the position in the data where phrase tokens starts is known. This overrides the
   * method iin DataImportReader, because with the CSVReaderAndTokenizer, the nunber of phrase
   * tokens actually in the data depends on the line, but an instance of this class always returns a
   * phrase with maxTokens elements when getNextData() is called. However, returns -1.
   *
   * @return int **************************************************************
   */
  @Override
  public int getNumPhraseTokens() {
    if (this.startTokensPosition >= 0) {
      return this.maxTokens;
    }
    return -1;
  }


  private String[] tokenize(String[] dataItems) {
    if (dataItems.length > 0) {
      String[] result = new String[dataItems.length - 1 + this.maxTokens];

      int tokensIndex = this.startTokensPosition > 0 ? this.startTokensPosition : 0;
      String[] tokens = this.tokenizer.tokenize(dataItems[tokensIndex]);
      int i = 0;

      // Get items before tokens
      while (i < tokensIndex) {
        result[i] = dataItems[i++];
      }

      // Get the tokens
      i = 0;
      while (i < tokens.length && i < tokensIndex + this.maxTokens) {
        result[i + tokensIndex] = tokens[i++];
      }

      // Fill array if necessary with null elements
      while (i < tokensIndex + this.maxTokens) {
        result[i++] = RaptatConstants.NULL_ELEMENT;
      }

      // Put the last item into the array if there is room (this is the
      // case
      // if the last item is a concept
      if (i < result.length) {
        result[result.length - 1] = dataItems[dataItems.length - 1];
      }

      return result;
    }

    // Return null if there is no data in the parameter pass to this
    // function
    return new String[0];
  }


  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        DataImportReader fileReader = new DataImporter("Select Training Data", true).getReader();
        String[] curData;

        if (fileReader != null && fileReader.isValid()) {
          while ((curData = fileReader.getNextData()) != null) {
            System.out.println(Arrays.toString(curData));
          }
        }
      }
    });
  }
}
