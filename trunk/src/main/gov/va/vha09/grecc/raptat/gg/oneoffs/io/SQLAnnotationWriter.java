/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io;

import java.io.File;
import java.util.Set;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;

/** @author Glenn Gobbel */
public class SQLAnnotationWriter {

  /*
   * Implement DbConnectionObject to store and/or retrieve the information for connecting to the
   * database with annotations of interest
   */
  class DbConnectionObject {
    final Object field_01;
    final Object field_02;
    final Object field_n;


    /**
     * @param field_01
     * @param field_02
     * @param field_n
     */
    public DbConnectionObject(Object field_01, Object field_02, Object field_n) {
      super();
      this.field_01 = field_01;
      this.field_02 = field_02;
      this.field_n = field_n;
    }
  }


  private final XMLExporterRevised exporter;

  public SQLAnnotationWriter(XMLExporterRevised exporter) {
    this.exporter = exporter;
  }


  public void processBatch(Set<AnnotationGroup> annotationGroupBatch) {
    for (AnnotationGroup annotationGroup : annotationGroupBatch) {
      this.exporter.exportReferenceAnnotationGroup(annotationGroup, false, false);
    }
  }


  public void setNextExportDirectory() {
    /* Implement method to generate path to next result folder */
    String resultFolderPath = "";
    File resultFolder = new File(resultFolderPath);
    this.exporter.setResultFolder(resultFolder);
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        File directoryForWritingXML = new File("PATH TO INITIAL DIRECTORY FOR STORING XML");

        /*
         * Set to overwriteExisting 'true' to overwrite any existing files of same name in the
         * directoryForWritingXML file. If set to 'false,' if the file name exists, the application
         * will ask you if you want to do this.
         */
        boolean overwriteExisting = true;
        XMLExporterRevised exporter =
            new XMLExporterRevised(AnnotationApp.EHOST, directoryForWritingXML, overwriteExisting);
        SQLAnnotationWriter writer = new SQLAnnotationWriter(exporter);

        /*
         * Implement DbConnectionObject as inner class with all the fields needed to set up a
         * database connection for reading the annotated phrases
         */
        DbConnectionObject dbConnectionObject = null;
        SQLAnnotationRetriever retriever = new SQLAnnotationRetriever(dbConnectionObject);
        retriever.connectToDatabase();

        Set<AnnotationGroup> annotationGroupBatch;
        while ((annotationGroupBatch = retriever.getNextAnnotationGroupBatch()) != null) {
          writer.processBatch(annotationGroupBatch);
          writer.setNextExportDirectory();
        }

        System.exit(0);
      }
    });
  }
}
