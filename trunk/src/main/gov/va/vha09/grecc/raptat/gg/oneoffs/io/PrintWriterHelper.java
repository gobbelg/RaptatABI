package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

public class PrintWriterHelper {
  public class StartOffsetPhraseComparator implements Comparator<AnnotatedPhrase> {
    @Override
    public int compare(AnnotatedPhrase a1, AnnotatedPhrase a2) {
      int result =
          Integer.parseInt(a1.getRawTokensStartOff()) - Integer.parseInt(a2.getRawTokensStartOff());

      if (result < 0) {
        return -1;
      }

      if (result == 0) {
        return 0;
      }

      return 1;
    }
  }

  public class StartOffsetTokenComparator implements Comparator<RaptatToken> {
    @Override
    public int compare(RaptatToken t1, RaptatToken t2) {
      int result = Integer.parseInt(t1.getStartOffset()) - Integer.parseInt(t2.getStartOffset());

      if (result < 0) {
        return -1;
      }

      if (result == 0) {
        return 0;
      }

      return 1;
    }
  }


  /**
   * ************************************************************** Generate a dialog to retrieve
   * the path to a file
   *
   * @return The chosen file or null if cancel is pressed
   *         **************************************************************
   */
  public String getFileFromPrompt(String thePrompt, FileFilter theFilter) {
    JFileChooser fc = new JFileChooser();
    if (theFilter != null) {
      fc.addChoosableFileFilter(theFilter);
    }
    fc.setDialogTitle(thePrompt);
    fc.setFileHidingEnabled(true);

    int fileChosen = fc.showOpenDialog(null);
    if (fileChosen == JFileChooser.APPROVE_OPTION) {
      return fc.getSelectedFile().getAbsolutePath();
    } else {
      return null;
    }
  }


  public void printAnnotationPhraseOffsetsAndConcepts(List<AnnotatedPhrase> theAnnotations) {
    Collections.sort(theAnnotations, new StartOffsetPhraseComparator());
    for (AnnotatedPhrase curPhrase : theAnnotations) {
      System.out.println(curPhrase.getConceptName() + ", " + curPhrase.getRawTokensStartOff() + ": "
          + curPhrase.getPhraseStringUnprocessed());
    }
  }


  public void printAnnotations(List<AnnotatedPhrase> theSentences) {
    Collections.sort(theSentences, new StartOffsetPhraseComparator());
    for (AnnotatedPhrase curPhrase : theSentences) {
      System.out
          .println(curPhrase.getPhraseStringUnprocessed() + "\t" + curPhrase.getConceptName());
    }
  }
}
