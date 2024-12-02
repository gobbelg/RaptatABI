package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.FileNotFoundException;
import java.io.IOException;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PostArffFileCompleted.PostArffFileResults;


public interface FnPostArffFileCompleted {

  /*
   * Functional interface for writing an arff file to the patth described by arffFilePath
   *
   * @param arffFilePath
   *
   * @return
   *
   * @throws FileNotFoundException
   *
   * @throws IOException
   */
  public PostArffFileResults writeArffFile(String arffFilePath)
      throws FileNotFoundException, IOException;



}
