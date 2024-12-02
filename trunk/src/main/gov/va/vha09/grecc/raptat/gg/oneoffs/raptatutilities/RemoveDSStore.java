package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class RemoveDSStore {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        File chosenDirectory =
            GeneralHelper.getDirectory("Select directory for recursive removal of .DS_store files");
        try {
          delete(chosenDirectory, ".DS_Store");
        } catch (IOException e) {
          System.out.println(e);
          e.printStackTrace();
        }
      }
    });
  }


  private static void delete(File f, String name) throws IOException {
    if (f.isDirectory()) {
      for (File curFile : f.listFiles()) {
        if (curFile.isDirectory() || curFile.getName().equals(name)) {
          delete(curFile, name);
        }
      }
    } else {
      f.delete();
    }
  }
}
