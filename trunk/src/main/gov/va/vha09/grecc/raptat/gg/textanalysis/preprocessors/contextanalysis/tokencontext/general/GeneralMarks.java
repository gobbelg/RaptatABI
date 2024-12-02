package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.general;

import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.Mark;

public class GeneralMarks implements Mark {

  public enum ContextMark implements Mark {
    CONCEPTWORD, FACILITATOR, BLOCKER, BLOCKED, FACILITATED
  }

  /** */
  private static final long serialVersionUID = 4176901685971567248L;
}
