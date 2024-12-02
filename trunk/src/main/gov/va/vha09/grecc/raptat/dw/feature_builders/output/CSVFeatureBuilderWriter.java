package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVFeatureBuilderWriter extends FeatureBuilderWriter {


  public CSVFeatureBuilderWriter() throws IOException {
    super("csv_feature_builder_writer", ".csv");
  }


  public CSVFeatureBuilderWriter(String outputFilePath) {
    super(outputFilePath);
  }


  @Override
  public void close() throws Exception {
    super.close();

  }


  @Override
  protected void derivedWrite(List<List<String>> output) throws Exception {

    // assumes top row is header
    List<String> header = output.get(0);
    String[] buffer = new String[header.size()];
    header.toArray(buffer);

    Path path = null;

    if (this.tempFile != null) {
      path = Paths.get(this.tempFile.getPath());
    } else {
      path = Paths.get(getOutputFilePath());
    }

    try (BufferedWriter writer = Files.newBufferedWriter(path);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(buffer));) {
      int index = 1;
      while (index < output.size()) {
        csvPrinter.printRecord(output.get(index).toArray());
        index++;
      }
      csvPrinter.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
