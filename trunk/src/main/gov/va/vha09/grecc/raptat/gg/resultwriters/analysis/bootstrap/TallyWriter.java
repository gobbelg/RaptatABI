/** */
package src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.analysis.bootstrap;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ************************************************
 *
 * @author vhatvhgobbeg - Jun 10, 2013 ************************************************
 */
public class TallyWriter extends BootstrapResultWriter {

  private StringBuffer resultStringBuffer = new StringBuffer("");
  private ByteArrayOutputStream outstream = new ByteArrayOutputStream();


  /**
   * @param fileDirectoryName
   * @param titleAppend
   * @param resultTitles
   * @param outputStream
   */
  public TallyWriter(String fileDirectoryName, String titleAppend, String[] resultTitles) {
    super(fileDirectoryName, titleAppend, resultTitles);
    // TODO Auto-generated constructor stub
  }


  @Override
  public void close() {
    flushToString();
    try {
      this.resultPW.flush();
      this.resultPW.close();
      String saveFile = this.fileTitle + ".csv";
      File resFile = new File(saveFile);
      this.resultPW = new PrintWriter(new BufferedWriter(new FileWriter(resFile, false)), true);
      this.resultPW.write(this.resultStringBuffer.toString());
    } catch (IOException e) {
      System.out.println(e + "\nUnable to write bootstrapping results to file");
      e.printStackTrace();
    }
  }


  public void flushToString() {
    this.resultPW.flush();
    this.resultStringBuffer.append(this.outstream.toString());
    this.outstream.reset();
  }
}
