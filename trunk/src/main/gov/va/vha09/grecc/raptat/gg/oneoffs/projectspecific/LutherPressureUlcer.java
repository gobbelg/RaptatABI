package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation.AnnotationMutator;

public class LutherPressureUlcer {
  private enum RunType {
    OTHER, COMBINE;
  }


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    if (lvgSetting > 0) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          RunType runType = RunType.COMBINE;

          switch (runType) {
            case COMBINE:
              String textFileDirectoryName = "";
              File textFileDirectory = new File(textFileDirectoryName);

              String targetAnnotationsDirectoryName = "";
              File targetAnnotationsDirectory = new File(targetAnnotationsDirectoryName);

              HashSet<String> targetConcepts = new HashSet<>();
              targetConcepts.add("livingsituation");

              String targetAnnotationsSchemaFilePath = "";
              File targetAnnotationsSchemaFile = new File(targetAnnotationsSchemaFilePath);

              String filteringAnnotationsDirectoryName = "";
              File filteringAnnotationsDirectory = new File(filteringAnnotationsDirectoryName);

              HashSet<String> filteringConcepts = new HashSet<>();
              filteringConcepts.add("livingsituation");

              String filteringAnnotationsSchemaFilePath = "";
              File filteringAnnotationsSchemaFile = new File(filteringAnnotationsSchemaFilePath);

              AnnotationMutator mutator = new AnnotationMutator();
              List<AnnotationGroup> resultGroups =
                  mutator.filterAnnotations(targetAnnotationsDirectory, targetAnnotationsSchemaFile,
                      targetConcepts, filteringAnnotationsDirectory, filteringAnnotationsSchemaFile,
                      filteringConcepts, textFileDirectory);

              String saveDirectoryPath = targetAnnotationsSchemaFile.getParent() + File.separator
                  + "FilteredXML_" + GeneralHelper.getTimeStamp();
              saveResultGroups(resultGroups, saveDirectoryPath);

              break;

            case OTHER:
              break;
          }
        }


        private void saveResultGroups(List<AnnotationGroup> resultGroups,
            String saveDirectoryPath) {
          File saveDirectory = new File(saveDirectoryPath);
          try {
            FileUtils.forceMkdir(saveDirectory);
          } catch (IOException e) {
            e.printStackTrace();
          }
          boolean ignoreOverwrites = true;
          boolean correctOffsets = true;
          boolean insertPhraseStrings = true;
          XMLExporterRevised exporter =
              new XMLExporterRevised(AnnotationApp.EHOST, saveDirectory, ignoreOverwrites);
          for (AnnotationGroup annotationGroup : resultGroups) {
            exporter.exportRaptatAnnotationGroup(annotationGroup, correctOffsets,
                insertPhraseStrings);
          }
        }
      });
    }
  }
}
