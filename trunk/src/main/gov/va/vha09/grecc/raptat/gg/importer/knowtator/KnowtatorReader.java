/** */
package src.main.gov.va.vha09.grecc.raptat.gg.importer.knowtator;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.ProjectSettings;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;
import edu.uchsc.ccp.knowtator.textsource.TextSourceIterator;
import edu.uchsc.ccp.knowtator.util.ProjectUtil;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/**
 * ********************************************************** Reads in a Knowtator Project file and
 * determines maximum number of elements within all annotated phrases in the file.
 *
 * @author Glenn Gobbel, Jan 30, 2011
 *         <p>
 *         *********************************************************
 */
public class KnowtatorReader extends DataImportReader {
  private TextSourceIterator textSourceIterator;
  private KnowledgeBase kb;
  private KnowtatorProjectUtil kpu;
  private KnowtatorManager knowtatorManager;
  private AnnotationUtil annotationUtil;
  private MentionUtil mentionUtil;
  private Iterator<SimpleInstance> annotationIterator;
  private TextSource curTextSource;
  private int numPhraseTokens;
  private File knowtatorFile;


  /**
   * ***************************************************************
   *
   * @param selectedFile ***************************************************************
   */
  public KnowtatorReader(File selectedFile) {
    super();
    this.knowtatorFile = selectedFile;
    edu.stanford.smi.protege.model.Project project =
        ProjectUtil.openProject(this.knowtatorFile.getPath());

    this.kb = project.getKnowledgeBase();
    this.kpu = new KnowtatorProjectUtil(this.kb);
    this.knowtatorManager = new KnowtatorManager(this.kpu);
    this.annotationUtil = this.knowtatorManager.getAnnotationUtil();
    this.mentionUtil = this.knowtatorManager.getMentionUtil();

    TextSourceCollection textSourceCollection =
        ProjectSettings.getRecentTextSourceCollection(project);
    this.numPhraseTokens = getMaxTokens(textSourceCollection);
    this.textSourceIterator = textSourceCollection.iterator();

    updateCurTextSource();
    setAnnotationIterator(this.curTextSource);
  }


  @Override
  public void close() // Does nothing - only to conform to interface
  {}


  /**
   * ************************************************************** Supplies the next line of data
   * in the file with each element generating one element of a String array.
   *
   * @return String [] - Array containing data elements with the document source at index 0, the
   *         annotation at the last position and the tokens of the annotated phrase at the
   *         intervening indices. **************************************************************
   */
  @Override
  public String[] getNextData() {
    String[] returnPhrase = new String[this.numPhraseTokens + 2];
    String[] tempArray;
    SimpleInstance curAnnotation;
    String curConcept;

    Pattern p = Pattern.compile(RaptatConstants.SPLITTER_REGEX);
    if (this.annotationIterator != null && this.annotationIterator.hasNext()) {
      curAnnotation = this.annotationIterator.next();
      if (isDirectAnnotation(curAnnotation)) {
        curConcept = getConcept(curAnnotation);
        if (curConcept != null) {
          returnPhrase[0] = this.curTextSource.getName();
          tempArray = p.split(curAnnotation.getBrowserText());
          if (tempArray.length < this.numPhraseTokens) {
            tempArray = padStringArray(tempArray);
          }
          System.arraycopy(tempArray, 0, returnPhrase, 1, this.numPhraseTokens);
          returnPhrase[this.numPhraseTokens + 1] = curConcept;
          return returnPhrase;
        }
      }
      return this.getNextData(); // Go onto next data set if no annotation
      // or
      // concept
    } else {
      updateCurTextSource();
      if (this.curTextSource != null) {
        setAnnotationIterator(this.curTextSource);
        return this.getNextData();
      } else {
        return null;
      }
    }
  }


  /**
   * ************************************************************** Supplies the next line of data
   * in the file with each element generating one element of a String array. This is the same as
   * getNextData() and is included only to conform to the abstract superclass that contains
   * subclasses where this method is needed.
   *
   * @return String [] - Array containing data elements with the document source at index 0, the
   *         annotation at the last position and the tokens of the annotated phrase at the
   *         intervening indices. **************************************************************
   */
  @Override
  public String[] getNextData(int startColumn) {
    return this.getNextData();
  }


  /**
   * ************************************************************** Resets reader so user can read
   * starting at the first text source in the knowtator file
   * **************************************************************
   */
  @Override
  public void reset() {
    edu.stanford.smi.protege.model.Project project =
        ProjectUtil.openProject(this.knowtatorFile.getPath());

    TextSourceCollection textSourceCollection =
        ProjectSettings.getRecentTextSourceCollection(project);
    this.textSourceIterator = textSourceCollection.iterator();

    updateCurTextSource();
    setAnnotationIterator(this.curTextSource);
  }


  /**
   * **************************************************************
   *
   * @param curAnnotation
   * @return String **************************************************************
   */
  private String getConcept(SimpleInstance theAnnotation) {
    SimpleInstance mention = this.annotationUtil.getMention(theAnnotation);
    if (mention != null) {
      Cls theCls = this.mentionUtil.getMentionCls(mention);
      if (theCls != null) {
        String theName = this.mentionUtil.getMentionCls(mention).getName();
        if (theName != null) {
          return theName;
        }
      }
    }
    return null;
  }


  /**
   * **************************************************************
   *
   * @return int **************************************************************
   */
  private int getMaxTokens(TextSourceCollection theSources) {
    int curNumTokens;
    int maxNumTokens = 0;
    TextSourceIterator theSourcesIterator = theSources.iterator();

    while (theSourcesIterator.hasNext()) {
      TextSource textSource;
      Pattern p = Pattern.compile(RaptatConstants.SPLITTER_REGEX);
      try {
        textSource = theSourcesIterator.next();
        Collection<SimpleInstance> annotations = this.annotationUtil.getAnnotations(textSource);
        if (annotations != null) {
          for (SimpleInstance curAnnotation : annotations) {
            if (isDirectAnnotation(curAnnotation)) {
              if (getConcept(curAnnotation) != null) {
                curNumTokens = p.split(curAnnotation.getBrowserText()).length;
                if (curNumTokens > maxNumTokens) {
                  maxNumTokens = curNumTokens;
                }
              }
            }
          }
        }
      } catch (TextSourceAccessException e) {
        JOptionPane.showMessageDialog(null,
            "Unable to access annotations for " + "current text source",
            "Text Source Not Available", JOptionPane.WARNING_MESSAGE);
        e.printStackTrace();
      }
    }
    return maxNumTokens;
  }


  private boolean isDirectAnnotation(SimpleInstance theAnnotation) {
    return !this.annotationUtil.getSpans(theAnnotation).isEmpty();
  }


  private String[] padStringArray(String[] inArray) {
    if (inArray.length >= this.numPhraseTokens + 2) {
      return inArray;
    }

    int inArrayLength = inArray.length;
    String[] returnArray = new String[this.numPhraseTokens + 2];
    System.arraycopy(inArray, 0, returnArray, 0, inArrayLength);
    for (int i = inArrayLength; i < this.numPhraseTokens + 2; i++) {
      returnArray[i] = RaptatConstants.NULL_ELEMENT;
    }
    return returnArray;
  }


  /**
   * **************************************************************
   *
   * @param curTextSource2
   * @return Iterator<SimpleInstance> **************************************************************
   */
  private void setAnnotationIterator(TextSource theTextSource) {
    if (theTextSource != null) {
      Collection<SimpleInstance> annotations = this.annotationUtil.getAnnotations(theTextSource);
      if (annotations != null) {
        this.annotationIterator = annotations.iterator();
      } else {
        this.annotationIterator = null;
      }
    } else {
      this.annotationIterator = null;
    }
  }


  /**
   * **************************************************************
   *
   * @param textSourceIterator2
   * @return TextSource **************************************************************
   */
  private void updateCurTextSource() {
    if (this.textSourceIterator.hasNext()) {
      try {
        this.curTextSource = this.textSourceIterator.next();
      } catch (TextSourceAccessException e) {
        JOptionPane.showMessageDialog(null, "Unable to access next text source",
            "Knowator File Reader Error", JOptionPane.WARNING_MESSAGE);
        e.printStackTrace();
        this.curTextSource = null;
      }
    } else {
      this.curTextSource = null;
    }
  }
}
