/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.TokenSequence;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;

/** @author Glenn T. Gobbel Apr 19, 2016 */
public abstract class FeaturePipe extends Pipe {
  /** */
  private static final long serialVersionUID = -2638492154914362316L;

  protected static final Logger LOGGER = Logger.getLogger(FeaturePipe.class);

  protected boolean saveSource = true;

  protected boolean doDigitCollapses = true;

  protected boolean doDowncasing = true;

  protected boolean doWordClass = true;

  protected boolean usePOS = true;

  protected boolean useStems = true;

  protected boolean markNegexTags = true;

  protected boolean doBriefWordClass = true;
  protected Pattern initCapsAlphaPattern = Pattern.compile("[A-Z][a-z].*");
  protected Pattern allCaps = Pattern.compile("[A-Z]+");
  protected int sentenceCounter = 0;
  protected String logFilePath;
  protected String logFileName = "Input2TokenLog" + System.currentTimeMillis() + ".txt";
  protected transient PrintWriter logWriter = null;


  public FeaturePipe() throws InstantiationException, IllegalAccessException {
    super(null, LabelAlphabet.class.newInstance());
    this.sentenceCounter = 0;
    FeaturePipe.LOGGER.setLevel(Level.DEBUG);
  }


  public FeaturePipe(boolean logToFile) throws InstantiationException, IllegalAccessException {
    this();

    if (logToFile) {
      OutputStream os;
      try {
        this.logFilePath = OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath()
            + File.separator + this.logFileName;
        os = new BufferedOutputStream(new FileOutputStream(this.logFilePath, true));
        this.logWriter = new PrintWriter(os, true);
      } catch (FileNotFoundException e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }
  }


  /** @return the doDigitCollapses */
  public boolean doDigitCollapses() {
    return this.doDigitCollapses;
  }


  /**
   * @param doBriefWordClass the doBriefWordClass to set
   * @return
   */
  public FeaturePipe setDoBriefWordClass(boolean doBriefWordClass) {
    this.doBriefWordClass = doBriefWordClass;
    return this;
  }


  public FeaturePipe setDoDigitCollapses(boolean doDigitCollapses) {
    this.doDigitCollapses = doDigitCollapses;
    return this;
  }


  /**
   * @param doDowncasing the doDowncasing to set
   * @return
   */
  public FeaturePipe setDoDowncasing(boolean doDowncasing) {
    this.doDowncasing = doDowncasing;
    return this;
  }


  /**
   * @param doWordClass the doWordClass to set
   * @return
   */
  public FeaturePipe setDoWordClass(boolean doWordClass) {
    this.doWordClass = doWordClass;
    return this;
  }


  /**
   * @param markNegexTags the markNegexTags to set
   * @return
   */
  public FeaturePipe setMarkNegexTags(boolean markNegexTags) {
    this.markNegexTags = markNegexTags;
    return this;
  }


  /**
   * @param saveSource the saveSource to set
   * @return
   */
  public FeaturePipe setSaveSource(boolean saveSource) {
    this.saveSource = saveSource;
    return this;
  }


  /**
   * @param usePOS the usePOS to set
   * @return
   */
  public FeaturePipe setUsePOS(boolean usePOS) {
    this.usePOS = usePOS;
    return this;
  }


  /**
   * @param useStems the useStems to set
   * @return
   */
  public FeaturePipe setUseStems(boolean useStems) {
    this.useStems = useStems;
    return this;
  }


  protected void printPipeInfo(TokenSequence data, LabelSequence target) {
    cc.mallet.types.LabelSequence.Iterator targetIterator = target.iterator();
    for (cc.mallet.types.Token curToken : data) {
      StringBuilder tokenSB = new StringBuilder("String:");
      tokenSB.append(curToken).append(", Target:").append(targetIterator.next());
      FeaturePipe.LOGGER.debug(tokenSB);
      if (this.logWriter != null) {
        this.logWriter.println(tokenSB);
      }
    }
  }
}
