package src.main.gov.va.vha09.grecc.raptat.gg.analysis.crossvalidate;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScoreObject;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorer;

/**
 * ****************************************************** This is a convenience class generated to
 * hold the results of doing a leave one out analysis of phrase identification for a particular
 * setting of training options and the sequence probability threshold
 *
 * @author Glenn Gobbel - Mar 25, 2013 *****************************************************
 */
public class PhraseIDCrossValidationResult {
  private double sequenceThreshold;
  private String trainingOptions;
  private PerformanceScorer resultScore;
  private String baseDirectory;


  public PhraseIDCrossValidationResult() {
    // TODO Auto-generated constructor stub
  }


  public PhraseIDCrossValidationResult(double sequenceThreshold, PerformanceScorer resultScore,
      String trainingOptions, String baseDirectory) {
    super();
    this.sequenceThreshold = sequenceThreshold;
    this.baseDirectory = baseDirectory;
    this.trainingOptions = trainingOptions;
    this.resultScore = resultScore;
  }


  public Hashtable<PerformanceScoreObject, int[]> getConceptMatchScore() {
    if (this.resultScore != null) {
      return this.resultScore.getRunningSummedResult();
    }

    return null;
  }


  public String getResultDirectory() throws IOException {
    String resultDirectoryName = "Results_" + this.trainingOptions + "_"
        + Double.toString(this.sequenceThreshold).replace(".", "p") + "_"
        + System.currentTimeMillis();
    File resultDirectory = new File(this.baseDirectory, resultDirectoryName);
    if (!resultDirectory.exists() && !resultDirectory.mkdir()) {
      throw new IOException("Unable to create directory to store results");
    }
    return resultDirectory.getAbsolutePath();
  }


  /** @return the resultScore */
  public PerformanceScorer getResultScore() {
    return this.resultScore;
  }


  /** @return the sequenceThreshold */
  public double getSequenceThreshold() {
    return this.sequenceThreshold;
  }
}
