package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.io.File;

public class WindowHelper {
  /**
   * *********************************************************** Checks to make sure the reference
   * supplied as a parameter refers to a file and not a directory, and, if the operating system is a
   * mac, it checks to make sure that it is not a hidden file (like .DS_Store).
   *
   * @param candidateFile
   * @return
   * @author Glenn Gobbel - Oct 25, 2012 ***********************************************************
   */
  public static boolean validFile(File candidateFile) {
    return candidateFile.isFile() && (!OSUtils.isMac() || !candidateFile.getName().startsWith("."));
  }
}
