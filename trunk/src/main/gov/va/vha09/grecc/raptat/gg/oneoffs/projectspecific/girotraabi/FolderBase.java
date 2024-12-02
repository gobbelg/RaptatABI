package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.text.MessageFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class FolderBase.
 */
public abstract class FolderBase {

  /** The directory. */
  final private File directory;


  /**
   * Instantiates a new folder base.
   *
   * @param directoryPath the directory path
   * @throws NotDirectoryException the not directory exception
   */
  public FolderBase(File directoryPath) throws NotDirectoryException {
    this(directoryPath.getAbsolutePath());
  }


  /**
   * Instantiates a new folder base.
   *
   * @param folderBase the folder base
   * @throws NotDirectoryException the not directory exception
   */
  protected FolderBase(FolderBase folderBase) throws NotDirectoryException {
    this(folderBase.getDirectory());
  }


  /**
   * Instantiates a new folder base.
   *
   * @param folderPath the folder path
   * @throws NotDirectoryException the not directory exception
   */
  protected FolderBase(String folderPath) throws NotDirectoryException {
    this.directory = new File(folderPath);
    if (this.getDirectory().isDirectory() == false) {
      throw new NotDirectoryException(MessageFormat.format("{0} is not a directory", folderPath));
    }
  }


  /**
   * Gets the directory.
   *
   * @return the directory
   */
  public File getDirectory() {
    return this.directory;
  }

  public String getPath() {
    return this.getDirectory().getAbsolutePath();
  }

  @Override
  public String toString() {
    return this.getDirectory().getAbsolutePath();
  }

}
