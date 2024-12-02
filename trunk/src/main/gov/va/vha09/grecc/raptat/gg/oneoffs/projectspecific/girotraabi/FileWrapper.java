package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import com.google.common.base.Joiner;

public class FileWrapper implements AutoCloseable {

  public enum Include {
    CarriageReturnNewLine
  }


  private File file;


  public FileWrapper(File file) {
    this.file = file;
  }


  public File baseFile() {
    return this.file;
  }

  @Override
  public void close() throws Exception {

  }


  public int getMaxCharacterCountByLine(Include include) {
    int maxSize = 0;
    int additionalLineLength = 0;
    switch (include) {
      case CarriageReturnNewLine:
        additionalLineLength = 2;
        break;
      default:
        additionalLineLength = 0;
        break;

    }
    try (BufferedReader reader = this.getReader()) {
      maxSize = reader.lines().max((l, r) -> l.length() - r.length()).get().length()
          + additionalLineLength;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return maxSize;
  }


  public BufferedReader getReader() throws FileNotFoundException {
    return new BufferedReader(new FileReader(this.file));
  }


  public String getText() throws FileNotFoundException, IOException {
    String rv = null;
    try (BufferedReader reader = this.getReader()) {
      rv = Joiner.on("\r\n").join(reader.lines().toArray());
    }
    return rv;
  }

}
