package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import com.google.common.base.Joiner;



// TODO: Auto-generated Javadoc
/**
 * Wrapper class to expose common functionality for directory activities.
 *
 * @author VHATVHWesteD
 */
public class SourceFolder extends FolderBase implements Iterator<FileWrapper> {
  /** The extensions. */
  final private String[] extensions;

  /** The iterator. */
  private Iterator<File> iterator;


  /**
   * Instantiates a new source folder.
   *
   * @param <T> the generic type
   * @param cls the cls
   * @param subFolder the sub folder
   * @param extensions the extensions
   * @throws NotDirectoryException the not directory exception
   */
  public <T> SourceFolder(Class<T> cls, String subFolder, String... extensions)
      throws NotDirectoryException {
    this(cls.getResource(subFolder).getFile(), extensions);
  }


  public SourceFolder(File folder) throws NotDirectoryException {
    super(folder);
    this.extensions = null;
  }


  /**
   * Instantiates a new source folder.
   *
   * @param baseDir the base dir
   * @param folderSubPath the folder sub path
   * @param extensions the extensions
   * @throws NotDirectoryException the not directory exception
   */
  public SourceFolder(File baseDir, String folderSubPath, String[] extensions)
      throws NotDirectoryException {
    this(Joiner.on("\\").join(baseDir.getAbsolutePath(), folderSubPath), extensions);
  }


  /**
   * Instantiates a new source folder.
   *
   * @param sourceFolderPath the source folder path
   * @param extensions the extensions
   * @throws NotDirectoryException the not directory exception
   */
  public SourceFolder(String sourceFolderPath, String... extensions) throws NotDirectoryException {
    super(sourceFolderPath);
    this.extensions = extensions;
  }


  /**
   * As destination.
   *
   * @return the destination folder
   * @throws NotDirectoryException
   */
  public DestinationFolder asDestination() throws NotDirectoryException {
    return new DestinationFolder(this);
  }


  public List<FileWrapper> getAll() {
    return this.collectFiles(null, null);
  }


  public List<FileWrapper> getBy(Pattern pattern) {
    IOFileFilter fileFilter = new IOFileFilter() {

      @Override
      public boolean accept(File file) {
        return this.doesNameExist(pattern, file.getName());
      }

      @Override
      public boolean accept(File dir, String name) {
        return this.doesNameExist(pattern, name);
      }

      private boolean doesNameExist(Pattern pattern, String name) {
        return pattern.matcher(name).find();
      }
    };
    IOFileFilter dirFilter = new IOFileFilter() {

      @Override
      public boolean accept(File file) {
        return true;
      }

      @Override
      public boolean accept(File dir, String name) {
        return true;
      }
    };

    return this.collectFiles(fileFilter, dirFilter);
  }


  /**
   * Gets the by name.
   *
   * @param fileName the file name
   * @return the by name
   */
  public FileWrapper getByName(String fileName) {
    File file = new File(this.getDirectory().getAbsolutePath() + "\\" + fileName);
    if (file.exists()) {
      return new FileWrapper(file);
    }
    return null;
  }


  /**
   * Checks for next.
   *
   * @return true, if successful
   */
  @Override
  public boolean hasNext() {
    this.initializeIterator();
    return this.iterator.hasNext();
  }


  /**
   * Next.
   *
   * @return the file
   */
  @Override
  public FileWrapper next() {
    this.initializeIterator();
    return new FileWrapper(this.iterator.next());
  }


  /**
   * Sub folder.
   *
   * @param folderPieces the folder pieces
   * @return the source folder
   * @throws NotDirectoryException the not directory exception
   */
  public SourceFolder subFolder(String... folderPieces) throws NotDirectoryException {
    String folderSubPath = Joiner.on("\\").join(folderPieces);
    return new SourceFolder(this.getDirectory(), folderSubPath, this.extensions);
  }


  private List<FileWrapper> collectFiles(IOFileFilter fileFilter, IOFileFilter dirFilter) {
    return FileUtils.listFiles(this.getDirectory(), fileFilter, dirFilter).stream()
        .map(x -> new FileWrapper(x)).collect(Collectors.toList());
  }


  /**
   * Initialize iterator.
   */
  private void initializeIterator() {
    if (this.iterator == null) {
      this.iterator = FileUtils.iterateFiles(this.getDirectory(), this.extensions, false);
    }
  }

}
