package src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.output;

import java.io.File;
import java.io.IOException;
import java.util.List;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

public class AARFFeatureBuilderWriter extends CSVFeatureBuilderWriter {

  private String aarfOutputFilePath;


  public AARFFeatureBuilderWriter(String outputFilePath) throws IOException {
    super();
    this.aarfOutputFilePath = outputFilePath;
  }


  @Override
  public void close() throws Exception {
    super.close();

  }


  @Override
  protected void derivedWrite(List<List<String>> output) throws Exception {
    super.derivedWrite(output);

    CSVLoader loader = new CSVLoader();

    loader.setSource(this.tempFile);

    String[] options = new String[1];
    options[0] = "-H";
    loader.setOptions(options);

    Instances data = loader.getDataSet();

    boolean convert_to_sparse = true;
    if (convert_to_sparse) {
      NonSparseToSparse sp = new NonSparseToSparse();
      sp.setInputFormat(data);
      data = Filter.useFilter(data, sp);
    }

    ArffSaver saver = new ArffSaver();

    saver.setInstances(data);

    saver.setFile(new File(this.aarfOutputFilePath));
    saver.writeBatch();

  }

}
