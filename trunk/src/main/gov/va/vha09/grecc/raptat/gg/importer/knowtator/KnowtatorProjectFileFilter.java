package src.main.gov.va.vha09.grecc.raptat.gg.importer.knowtator;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * ********************************************************** KnowtatorProjectFileFilter - Allows
 * only selection of Knowtator Project files by file chooser
 *
 * @author Glenn Gobbel, Jan 25, 2011
 *         <p>
 *         *********************************************************
 */
public class KnowtatorProjectFileFilter extends FileFilter {
  @Override
  public boolean accept(File file) {
    // Convert to lower case before checking extension
    return file.getName().toLowerCase().endsWith(".pprj") || file.isDirectory();
  }


  @Override
  public String getDescription() {
    return "Knowtator Project (*.pprj)";
  }
}
