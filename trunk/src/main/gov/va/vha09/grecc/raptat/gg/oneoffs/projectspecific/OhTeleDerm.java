package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

public class OhTeleDerm {
  private enum RunType {
    CONVERT_FILE, TEST
  }


  /**
   * @param args
   */
  public static void main(String[] args) {

    String visitsString = "Visits:";
    String patientIDString = "PatientICN:";
    String documentIDString = "TIUDocumentSID:";
    String timeDateString = "EntryDate:";

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RunType runType = RunType.CONVERT_FILE;

        switch (runType) {

          case CONVERT_FILE: {
            String filePath =
                "P:\\ORD_Oh_PTR_201903029D\\GobbelWorkspace\\SmallPatientSet\\PatientRecords_10Docs_190721.txt";
            String documentSeparator = "********_RECORD_SEPARATOR_********";
            String[] labelsArray =
                new String[] {visitsString, patientIDString, documentIDString, timeDateString};
            Set<String> sqlLabels = new HashSet<>(Arrays.asList(labelsArray));
            convertFile(filePath, documentSeparator, sqlLabels);
          }
            break;
          case TEST: {
            String dateTime = "JAN-05-2005";
            System.out.println("InputDate:" + dateTime);

            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date inputDate;
            String outputDate = "UnableToParseDate";
            try {
              inputDate = inputFormatter.parse(dateTime);
              DateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");
              outputDate = outputFormat.format(inputDate);
            } catch (ParseException e) {

              e.printStackTrace();
            }
            System.out.println("OutputDate:" + outputDate);
          }
          default:
            break;
        }
      }


      private void convertFile(String filePath, String documentSeparator, Set<String> sqlLabels) {
        File file = new File(filePath);
        String parentDirectoryPath = file.getParent();

        try (BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
          Map<String, String> labelToValueMap = new HashMap<>();
          List<String> fileLines = new ArrayList<>(400);

          String line;
          int fileIndex = 0;
          boolean textFound = false;

          while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!textFound && !line.isEmpty()) {
              textFound = true;
            }

            if (line.equals(documentSeparator)) {
              if (textFound) {
                writeToFile(fileLines, labelToValueMap, parentDirectoryPath, fileIndex++);
              }
              textFound = false;
              fileLines.clear();
              textFound = false;
              continue;
            }
            String[] lineElements = line.split("\t");
            if (sqlLabels.contains(lineElements[0])) {
              labelToValueMap.put(lineElements[0], lineElements[1]);
            }
            fileLines.add(line);
          }
        } catch (IOException e) {
          System.err.println("Unable to create buffered reader\n" + e.getLocalizedMessage());
          e.printStackTrace();
        }
      }


      private String getFileName(Map<String, String> labelToValueMap) {
        String dateTime =
            labelToValueMap.containsKey(timeDateString) ? labelToValueMap.get(timeDateString)
                : "NoDateTime";
        dateTime = dateTime.split(" ")[0];
        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date inputDate;
        String outputDate = "UnableToParseDate";
        try {
          inputDate = inputFormatter.parse(dateTime);
          DateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");
          outputDate = outputFormat.format(inputDate);
        } catch (ParseException e) {

          e.printStackTrace();
        }

        String docID =
            labelToValueMap.containsKey(documentIDString) ? labelToValueMap.get(documentIDString)
                : "NoDocumentID";

        return outputDate + "_" + docID;
      }


      private void writeToFile(List<String> fileLines, Map<String, String> labelToValueMap,
          String parentDirectoryPath, int fileIndex) throws FileNotFoundException {
        if (fileLines == null || fileLines.isEmpty()) {
          return;
        }

        String visits =
            labelToValueMap.containsKey(visitsString) ? labelToValueMap.get(visitsString) : "000";
        NumberFormat format = new DecimalFormat("000");
        visits = format.format(Integer.parseInt(visits));
        String visitDirectoryPath = parentDirectoryPath + File.separator + visits + "_PatientDocs";
        File visitDirectory = new File(visitDirectoryPath);
        if (!visitDirectory.exists()) {
          visitDirectory.mkdir();
        }
        String patientID =
            labelToValueMap.containsKey(patientIDString) ? labelToValueMap.get(patientIDString)
                : "NoPatientICN";
        String patientDirectoryPath =
            visitDirectoryPath + File.separator + "PatientICN_" + patientID;
        File patientDirectory = new File(patientDirectoryPath);
        if (!patientDirectory.exists()) {
          patientDirectory.mkdir();
        }

        String fileName = getFileName(labelToValueMap);
        String outputFilePath = patientDirectoryPath + File.separator + fileName + ".txt";

        System.out.println("Writing File " + fileIndex + ":" + outputFilePath);
        PrintWriter pw = new PrintWriter(outputFilePath);
        fileLines.forEach(line -> {
          pw.println(line);
          pw.flush();
        });
        pw.println();
        pw.flush();
        pw.close();
        System.out.println();
      }

    });

  }

}
