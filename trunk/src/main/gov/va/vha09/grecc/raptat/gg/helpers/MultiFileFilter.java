/** */
package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * ********************************************************** MultiFileFilter - allows the creation
 * of a file filter for the open file dialogue so that any combination of file types can be used
 *
 * @author Glenn Gobbel, Jan 30, 2011
 *         <p>
 *         *********************************************************
 */
public class MultiFileFilter extends FileFilter {
  private final String[] acceptableFiles;


  /**
   * ********************************************************** MultiFileFilter - Constructor for
   * file filter
   *
   * @author Glenn Gobbel, Jan 30, 2011
   * @param fileTypes - A String [] with all possible endings (suffixes) that the file filter will
   *        accept *********************************************************
   */
  public MultiFileFilter(String[] fileTypes) {
    this.acceptableFiles = fileTypes;
  }


  @Override
  public boolean accept(File file) {
    // Convert to lower case before checking extension
    String fileName = file.getName().toLowerCase();
    System.out.println("Filename " + fileName);
    boolean result = file.isDirectory();

    for (int i = 0; result == false && i < this.acceptableFiles.length; i++) {
      result = result || fileName.endsWith(this.acceptableFiles[i]);
    }
    System.out.println(":" + result);
    return result;
  }


  @Override
  public String getDescription() {
    int i = 0;
    String resultString = "*" + this.acceptableFiles[i++];
    while (i < this.acceptableFiles.length) {
      resultString += ", *" + this.acceptableFiles[i++];
    }
    return resultString;
  }


  @Override
  public String toString() {
    StringBuilder outString = new StringBuilder(this.acceptableFiles[0]);

    for (int i = 1; i < this.acceptableFiles.length; i++) {
      outString.append(System.getProperty("line.separator") + this.acceptableFiles[i]);
    }

    return outString.toString();
  }
}
