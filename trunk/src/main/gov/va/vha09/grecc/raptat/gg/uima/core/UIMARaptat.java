package src.main.gov.va.vha09.grecc.raptat.gg.uima.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.XMLInputSource;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.cpe.FileSystemCollectionReader;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Aug 24, 2013 *****************************************************
 */
public class UIMARaptat {

  private static final String TEXT_READER_DESC = "/desc/TextCollectionReaderDescriptor.xml";

  private static final String AGGREGATE_ENGINE_DESC = "/desc/UIMAAggregateAnnotator.xml";

  private static Logger logger = Logger.getLogger(UIMARaptat.class);


  /**
   * ********************************************
   *
   * @author Glenn Gobbel - Aug 24, 2013 ********************************************
   */
  public UIMARaptat() {
    // initializeUserPreferences() returns 1 if the chosen directory is
    // good, 0 if user cancels prompt to get LVG directory, and -1 if
    // they put in an LVG directory that doesn't work
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    if (lvgSetting == 0) {
      GeneralHelper.errorWriter("Please install the 2012 Lexical Variant"
          + " Generator (LVG) from the U.S. National Library of Medicine before running RapTAT");
      System.exit(-1);
    }
  }


  /**
   * *********************************************************** Sets directory containing text
   * files to analysis to the first argument in the @param inputArgs array (if there is such an
   * argument). If no argument exists, then the method looks in the FileSystemCollectionReader
   * descriptor for the directory. If it is present, it uses that, if not, it asks for the user to
   * identify the directory.
   *
   * @param cpeSettings
   * @return
   * @author Glenn Gobbel - Aug 24, 2013 ***********************************************************
   */
  private boolean setTextDirectory(String[] inputArgs, CollectionReaderDescription cpeDesc) {
    ConfigurationParameterSettings cpeSettings =
        cpeDesc.getCollectionReaderMetaData().getConfigurationParameterSettings();

    if (inputArgs.length > 0 && inputArgs[0].length() > 0) {
      if (cpeSettings.isModifiable()) {
        cpeSettings.setParameterValue(FileSystemCollectionReader.PARAM_INPUTDIR, inputArgs[0]);
        return true;
      }
      return false;
    }

    String inputDir =
        (String) cpeSettings.getParameterValue(FileSystemCollectionReader.PARAM_INPUTDIR);
    if (inputDir == null || inputDir.length() == 0) {
      if (cpeSettings.isModifiable()) {
        inputDir = GeneralHelper.getDirectory("Select directory containing text files for analysis")
            .getAbsolutePath();
        if (inputDir != null & inputDir.length() > 0) {
          cpeSettings.setParameterValue(FileSystemCollectionReader.PARAM_INPUTDIR, inputDir);
          return true;
        }
      }
      return false;
    }
    return true;
  }


  /**
   * ***********************************************************
   *
   * @param args
   * @author Glenn Gobbel - Aug 24, 2013 ***********************************************************
   */
  public static void main(final String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        runRaptat(args);
      }
    });
  }


  /**
   * ***********************************************************
   *
   * @param args
   * @author Glenn Gobbel - Aug 24, 2013 ***********************************************************
   */
  public static void runRaptat(final String[] args) {
    UIMARaptat raptatInstance = new UIMARaptat();
    String relativePathToDescriptors = "";
    try {
      relativePathToDescriptors = new File(".").getCanonicalPath();
    } catch (IOException e1) {
      System.out.println(e1);
      e1.printStackTrace();
    }

    try {
      URL readerURL = new File(relativePathToDescriptors + TEXT_READER_DESC).toURI().toURL();
      CollectionReaderDescription cpeDesc = UIMAFramework.getXMLParser()
          .parseCollectionReaderDescription(new XMLInputSource(readerURL));

      if (raptatInstance.setTextDirectory(args, cpeDesc)) {
        // instantiate CPE
        CollectionReader collectionReader = UIMAFramework.produceCollectionReader(cpeDesc);

        URL aeURL = new File(relativePathToDescriptors + AGGREGATE_ENGINE_DESC).toURI().toURL();

        ResourceSpecifier aeSpecifier =
            UIMAFramework.getXMLParser().parseResourceSpecifier(new XMLInputSource(aeURL));
        AnalysisEngine aggregateAnnotator = UIMAFramework.produceAnalysisEngine(aeSpecifier);

        CAS aCas = aggregateAnnotator.newCAS();

        while (collectionReader.hasNext()) {
          Progress curProgress = collectionReader.getProgress()[0];
          System.out.println("Processing document " + (curProgress.getCompleted() + 1L) + " of "
              + curProgress.getTotal());
          collectionReader.getNext(aCas);
          aggregateAnnotator.process(aCas);
          aCas.reset();
        }
        collectionReader.destroy();
        aggregateAnnotator.destroy();
      }
    } catch (IOException e) {
      System.out.println(e);
      e.printStackTrace();
    } catch (InvalidXMLException e) {
      System.out.println(e);
      e.printStackTrace();
    } catch (ResourceInitializationException e) {
      System.out.println(e);
      e.printStackTrace();
    } catch (AnalysisEngineProcessException e) {
      System.out.println(e);
      e.printStackTrace();
    } catch (CollectionException e) {
      System.out.println(e);
      e.printStackTrace();
    }

    GeneralHelper.errorWriter("UIMA-based RapTAT analysis complete.");
    System.exit(0);
  }
}
