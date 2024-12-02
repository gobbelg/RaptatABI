/** */
package src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.analysis.bootstrap;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScoreObject;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TrueFalseScores;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.resultwriters.ResultWriter;

/**
 * ********************************************************** BootstrapResultWriter -
 *
 * @author Glenn Gobbel, Mar 9, 2011
 *         <p>
 *         *********************************************************
 */
public class BootstrapResultWriter extends ResultWriter {

  public BootstrapResultWriter(String fileDirectoryName, String titleAppend,
      String[] resultTitles) {
    super(fileDirectoryName, titleAppend, resultTitles);
  }


  /**
   * ************************************************************** Flush and close the ResultWriter
   *
   * <p>
   * void **************************************************************
   */
  @Override
  public void close() {
    this.resultPW.flush();
    this.resultPW.close();
  }


  public void writeResults(Hashtable<PerformanceScoreObject, int[]> conceptMatchScores) {
    String[] numericResults = new String[6];

    for (PerformanceScoreObject pso : conceptMatchScores.keySet()) {
      this.write(pso.toString() + "\t");
      int[] trueFalseScores = conceptMatchScores.get(pso);
      numericResults = getNumericResults(trueFalseScores);
      this.writeln(numericResults);
    }
  }


  public void writeResults(Map<String, int[]> resultScores) {
    for (String curConcept : resultScores.keySet()) {
      int[] curScores = resultScores.get(curConcept);
      double precision = GeneralHelper.calcPrecision(curScores[0], curScores[1]);
      double recall = GeneralHelper.calcRecall(curScores[0], curScores[2]);
      double f = GeneralHelper.calcF_Measure(curScores[0], curScores[1], curScores[2]);

      /*
       * Modify spacing when data does not have attributes or values for a concept
       */
      if (!curConcept.contains("ATT") && !curConcept.contains("VAL")) {
        StringBuilder longSeparator = new StringBuilder();
        for (int j = 0; j < 4; j++) {
          longSeparator.append("\t");
        }
        curConcept += longSeparator.toString();
        if (curConcept.startsWith("Total")) {
          curConcept += "\t";
        }
      }

      curConcept = curConcept.replaceAll(RaptatConstants.DELIMITER_PERFORMANCE_SCORE_FIELD, "\t");
      curConcept = curConcept.replaceAll(RaptatConstants.NULL_ATTRIBUTE_VALUE, "NULL");

      StringBuilder sb = new StringBuilder(curConcept);
      String tab = "\t";
      sb.append(tab);
      for (int curScore : curScores) {
        sb.append(Integer.toString(curScore)).append(tab);
      }
      sb.append(precision).append(tab).append(recall).append(tab).append(f);
      try {
        this.writeln(sb.toString());
      } catch (IOException e) {
        GeneralHelper.errorWriter("Unable to write results to file");
        e.printStackTrace();
      }
    }
  }


  // public void writeResults(Hashtable<PerformanceScoreObject, int[]>
  // conceptMatchScores)
  // {
  // final int SCORE_INDEX_START = 10;
  // Enumeration<PerformanceScoreObject> theConcepts =
  // conceptMatchScores.keys();
  // double[] performanceScores = new double[3];
  // while (theConcepts.hasMoreElements())
  // {
  // try
  // {
  // String[] results = new String[12];
  // Arrays.fill( results, "" );
  //
  // PerformanceScoreObject curConcept = theConcepts.nextElement();
  // String[] conceptAttributeValue = curConcept.split(
  // Constants.DELIMITER_PERFORMANCE_SCORE_FIELD );
  // for (int i = 0; i < conceptAttributeValue.length; i++ )
  // {
  // results[i] = conceptAttributeValue[i];
  // }
  //
  // int[] conceptValues = conceptMatchScores.get( curConcept );
  // int numConceptValues = conceptValues.length;
  // for (int i = 0; i < numConceptValues; i++ )
  // {
  // results[i + SCORE_INDEX_START] = Integer.toString( conceptValues[i] );
  // }
  //
  // performanceScores = conceptMatchScores.getPerformanceScores( curConcept
  // );
  // for (int i = 0; i < performanceScores.length; i++ )
  // {
  // results[i + numConceptValues + SCORE_INDEX_START] = Double.toString(
  // performanceScores[i] );
  // }
  // writeln( results );
  // }
  // catch (IOException e)
  // {
  // e.printStackTrace();
  // }
  // }
  // }

  private String[] getNumericResults(int[] trueFalseScores) {
    String[] numericResults = new String[6];
    int tps = trueFalseScores[TrueFalseScores.TP.ordinal()];
    int fps = trueFalseScores[TrueFalseScores.FP.ordinal()];
    int fns = trueFalseScores[TrueFalseScores.FN.ordinal()];

    /* First get the TPs, FPs, and FNs */
    numericResults[TrueFalseScores.TP.ordinal()] = Integer.toString(tps);
    numericResults[TrueFalseScores.FP.ordinal()] = Integer.toString(fps);
    numericResults[TrueFalseScores.FN.ordinal()] = Integer.toString(fns);

    /* Now get the precision, recall, and F-measure */
    double precision = GeneralHelper.calcPrecision(tps, fps);
    double recall = GeneralHelper.calcRecall(tps, fns);
    double fmeasure = GeneralHelper.calcF_Measure(tps, fps, fns);

    int measureOffset = TrueFalseScores.FN.ordinal();
    numericResults[measureOffset + 1] = Double.toString(precision);
    numericResults[measureOffset + 2] = Double.toString(recall);
    numericResults[measureOffset + 3] = Double.toString(fmeasure);

    return numericResults;
  }
}
