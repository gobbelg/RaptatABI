package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.lang3.RandomUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class FeatureFileRandomizer {
  public static void main(String[] args) {

    BufferedReader br = null;
    try {
      int sentence = 0;
      String featureFilePath =
          "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\ReasonNoMedsModelTraining_160516_095811.txt";
      File featureFile = new File(featureFilePath);
      int numberOfWriters = 5;
      PrintWriter[] pws = getPrintWriters(numberOfWriters, featureFile.getParent());
      br = new BufferedReader(new FileReader(featureFile));

      if (br != null) {
        String line;
        StringBuilder sb = new StringBuilder(System.lineSeparator());

        /* Skip first line which should always be empty */
        br.readLine();
        while ((line = br.readLine()) != null) {
          if (line.isEmpty()) {
            System.out.println("Printing sentence " + ++sentence);
            int whichWriter = RandomUtils.nextInt(0, numberOfWriters);
            pws[whichWriter].println(sb.toString());
            pws[whichWriter].flush();
            sb = new StringBuilder("");
          } else {
            sb.append(System.lineSeparator()).append(line);
          }
        }
      }

      for (PrintWriter printWriter : pws) {
        if (printWriter != null) {
          printWriter.println();
          printWriter.close();
        }
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  private static PrintWriter[] getPrintWriters(int numberOfWriters, String pathToDirectory)
      throws FileNotFoundException {
    PrintWriter[] printWriters = new PrintWriter[numberOfWriters];
    String time = GeneralHelper.getTimeStamp();
    for (int i = 0; i < numberOfWriters; i++) {
      String pwFilePath =
          pathToDirectory + File.separator + "splitFeatures_" + i + "_" + time + ".txt";
      printWriters[i] = new PrintWriter(new File(pwFilePath));
    }
    return printWriters;
  }
}
