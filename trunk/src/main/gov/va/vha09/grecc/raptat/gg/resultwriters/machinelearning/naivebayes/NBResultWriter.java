package src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.machinelearning.naivebayes;

import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.ResultWriter;

/**
 * ********************************************************** NBResultWriter - Class to create a
 * file to write the results from a test of a naive Bayes network. It generates a file to write to
 * at construction, writes a new line of text when given a String array, and closes the file when
 * prompted
 *
 * @author Glenn Gobbel, July 19, 2010
 *         <p>
 *         *********************************************************
 */
public class NBResultWriter extends ResultWriter {
  public NBResultWriter(String fileDirectory, String append, String[] resultTitles,
      String delimiter) {
    super(fileDirectory, append, resultTitles, delimiter);
  }


  /**
   * ***************************************************************
   *
   * @param fileDirectory
   * @param machineLearningResultTitles
   *        ***************************************************************
   */
  public NBResultWriter(String fileDirectory, String[] resultTitles) {
    super(fileDirectory, resultTitles);
  }


  public NBResultWriter(String fileDirectory, String[] resultTitles, String delimiter) {
    super(fileDirectory, resultTitles, delimiter);
  }


  public static void main(String[] args) {
    // BayesResultWriter theWriter = new BayesResultWriter();
  }
}
