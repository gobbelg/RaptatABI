package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

import java.io.Serializable;

public abstract class LogicAnalyzer implements Serializable {
  /** */
  private static final long serialVersionUID = -4185771073049044015L;

  public static Mark[] boundaryMarks;
  public boolean isMarking;
  public int curIndex;
  public int remainingContextLength;
  public int window;


  public LogicAnalyzer(int window) {
    this.window = window;
  }


  public abstract int updateStatus();
}
