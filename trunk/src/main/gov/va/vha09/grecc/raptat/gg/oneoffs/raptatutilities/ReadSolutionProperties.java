package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.File;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;

public class ReadSolutionProperties {

  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        // String solutionFilePath =
        // "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_AnalysisForManuscript_161108\\RaptatTrainGroups2_9_WithDictionary_170120_20170120193603.soln";
        String solutionFilePath =
            "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_Retraining_170920\\SampleSolution_v01_20170920144401.soln";
        RaptatAnnotationSolution solution =
            RaptatAnnotationSolution.loadFromFile(new File(solutionFilePath));
        System.out.println("Solution loaded");
      }
    });
  }
}
