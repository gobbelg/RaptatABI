/** */
package src.main.gov.va.vha09.grecc.raptat.gg.importer;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.MultiFileFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.knowtator.KnowtatorReader;

/**
 * ********************************************************** DataImporter - Creates a
 * DataImportReader that can be used to import data from a data source into RapTAT. We include a
 * DataImportReader as a way of allowing this DataImporter to import data of different types of data
 * sources.
 *
 * @author Glenn Gobbel, Jan 30, 2011 *********************************************************
 */
public class DataImporter {
  private DataImportReader theReader = null;
  private String importFileType = null;


  public DataImporter() {
    super();
  }


  public DataImporter(String thePrompt) {
    this(thePrompt, false);
  }


  public DataImporter(String thePrompt, boolean getStartTokensPostion) {
    JFileChooser fc = new JFileChooser();
    fc.addChoosableFileFilter(new MultiFileFilter(RaptatConstants.IMPORTABLE_FILETYPES));
    fc.setDialogTitle(thePrompt);
    fc.setFileHidingEnabled(true);
    int fileChosen = fc.showOpenDialog(null);
    if (fileChosen == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fc.getSelectedFile();
      String fileName = selectedFile.getName();
      if (fileName.endsWith(RaptatConstants.IMPORTABLE_FILETYPES[0])) {
        int tokenizeOption =
            JOptionPane.showConfirmDialog(null, "Tokenize data?", "", JOptionPane.YES_NO_OPTION);
        if (tokenizeOption == JOptionPane.YES_OPTION) {
          this.theReader = new TokenizingTextReader(selectedFile, getStartTokensPostion);
          ((TokenizingTextReader) this.theReader).setDelimiter(",");
        } else {
          this.theReader = new CSVReader(selectedFile, getStartTokensPostion);
        }
        this.theReader.setFileType(RaptatConstants.IMPORTABLE_FILETYPES[0]);
      } else if (fileName.endsWith(RaptatConstants.IMPORTABLE_FILETYPES[1])) {
        int tokenizeOption =
            JOptionPane.showConfirmDialog(null, "Tokenize data?", "", JOptionPane.YES_NO_OPTION);
        if (tokenizeOption == JOptionPane.YES_OPTION) {
          this.theReader = new TokenizingTextReader(selectedFile, getStartTokensPostion);
        } else {
          this.importFileType = RaptatConstants.IMPORTABLE_FILETYPES[2];
          this.theReader = new TextReader(selectedFile, getStartTokensPostion);
        }
        this.theReader.setFileType(RaptatConstants.IMPORTABLE_FILETYPES[1]);
      } else if (fileName.endsWith(RaptatConstants.IMPORTABLE_FILETYPES[2])) {
        this.theReader = new KnowtatorReader(selectedFile);
        this.theReader.setFileType(RaptatConstants.IMPORTABLE_FILETYPES[2]);
      }
      this.theReader
          .setFileDirectory(GeneralHelper.getDirectoryOfFile(selectedFile.getAbsolutePath()));
      this.theReader.setFileName(fileName);
    }
    if (this.theReader != null) {
      this.theReader.close();
    }
  }


  public String getImportFileType() {
    return this.importFileType;
  }


  public DataImportReader getReader() {
    return this.theReader;
  }
}
