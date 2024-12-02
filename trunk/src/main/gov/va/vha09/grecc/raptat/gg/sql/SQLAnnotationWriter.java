/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.sql;

import java.io.File;
import java.util.Set;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.ExportAnnonationGroupParameter;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.Exporter;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.SQLExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.SqlExportAnnotationGroupParameter;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriverException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/**
 * @author Glenn Gobbel
 *
 */
public class SQLAnnotationWriter {
  

  public static final int ANNOTATION_GROUP_BATCH_SIZE = 100;


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        String pathForWritingXML = "D:/KarunaWorkspace/Temp/";
        String pathToSourceXMLFilesFolder =
            "D:\\KarunaWorkspace\\RapTAT\\ExamplesForKaruna\\ExamplesForKaruna\\XmlFiles\\";
        File directoryForWritingXML = new File(pathForWritingXML);

        
        String server = "vhatvhressql001.v09.med.va.gov";
        String database = "ORD_Matheny_201312053D_RES1";
        String username = "RapTAT_user";
        String password = "raptat_USER";
        
DbConnectionObject dbConnectionObject = new DbConnectionObject(server, database, username,password);
        
        boolean overwriteExisting = true;
        XMLExporterRevised exporter =
            new XMLExporterRevised(AnnotationApp.EHOST, directoryForWritingXML, overwriteExisting);
        SQLAnnotationWriter writer =
            new SQLAnnotationWriter(pathForWritingXML, pathToSourceXMLFilesFolder, exporter, dbConnectionObject);

        try {
          writer.retrieveXMl();
        } catch (SQLDriverException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        System.exit(0);
      }

    });
  }

  String pathForWritingXML;

  String pathToSourceXMLFilesFolder;

  private final XMLExporterRevised exporter;

  private DbConnectionObject __dbConnectionObject;


  public SQLAnnotationWriter(String pathForWritingXML, String pathToSourceXMLFilesFolder,
      XMLExporterRevised exporter, DbConnectionObject dbConnectionObject) {
    this.pathForWritingXML = pathForWritingXML;
    this.pathToSourceXMLFilesFolder = pathToSourceXMLFilesFolder;
    this.exporter = exporter;
    __dbConnectionObject = dbConnectionObject;
  }


  @Deprecated
  public SQLAnnotationWriter(XMLExporterRevised exporter) {
    this.exporter = exporter;
  }


  public void retrieveXMl() throws SQLDriverException {


    int groupBatchSize = SQLAnnotationWriter.ANNOTATION_GROUP_BATCH_SIZE;
    SQLAnnotationRetriever retriever = new SQLAnnotationRetriever(__dbConnectionObject, groupBatchSize);

    if (retriever.connectToDatabase() == null) {
      System.out.println("Check database connection parameters!");
      return;
    } ;

    Set<AnnotationGroup> annotationGroupBatch;
    int idIndex = 0;
    try {
      while ((annotationGroupBatch = retriever.getNextAnnotationGroupBatch(idIndex)) != null) {
        this.writeBatch(annotationGroupBatch);
        this.setNextExportDirectory();
        idIndex += groupBatchSize;
      }
    } catch (NoConnectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoDataException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  public void setNextExportDirectory() {
    /* Implement method to generate path to next result folder */
    String resultFolderPath = "";
    File resultFolder = new File(resultFolderPath);
    this.exporter.setResultFolder(resultFolder);

  }


  public void writeBatch(Set<AnnotationGroup> annotationGroupBatch) {
    for (AnnotationGroup annotationGroup : annotationGroupBatch) {
      boolean correctOffsets = true;
      boolean insertPhraseStrings = false;
      ExportAnnonationGroupParameter exportAnnonationGroupParameter =
          new SqlExportAnnotationGroupParameter(annotationGroup, correctOffsets,
              insertPhraseStrings, this.pathToSourceXMLFilesFolder);
      this.exporter.exportAnnotationGroup(exportAnnonationGroupParameter);
    }

  }
}
