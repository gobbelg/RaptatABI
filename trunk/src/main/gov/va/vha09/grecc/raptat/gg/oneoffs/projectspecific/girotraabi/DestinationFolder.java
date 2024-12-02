package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.File;
import java.nio.file.NotDirectoryException;
import com.google.common.base.Joiner;

public class DestinationFolder extends FolderBase {

  public <T> DestinationFolder(Class<T> cls, String... folderParts) throws NotDirectoryException {
    super(cls.getResource(Joiner.on("/").join(folderParts)).getFile());
  }

  public DestinationFolder(File folder) throws NotDirectoryException {
    super(folder);
  }

  public DestinationFolder(SourceFolder sourceFolder) throws NotDirectoryException {
    super(sourceFolder);
  }

  public DestinationFolder(String folder) throws NotDirectoryException {
    super(folder);
  }

}
