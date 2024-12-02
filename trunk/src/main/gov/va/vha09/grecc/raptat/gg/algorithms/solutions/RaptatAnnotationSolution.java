/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions;

import java.awt.Window;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AttributeAnalyzerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.SentenceSectionTaggerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TSTrainingParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

/**
 * Members of this class store the data structures that are a TokenSequenceAnnotator instances uses
 * to identify and generate AnnotatedPhrase instances within a RaptatDocument object. k
 *
 * @author Glenn T. Gobbel Feb 13, 2016
 */
public class RaptatAnnotationSolution implements Serializable {
  private static final long serialVersionUID = -2706352557463022644L;

  private static final Logger LOGGER = Logger.getLogger(RaptatAnnotationSolution.class);


  private TokenSequenceFinderSolution tokenSequenceFinderSolution = null;


  @Deprecated
  private HashMap<ContextType, AttributeTaggerSolution> taggerSolutionMap = new HashMap<>();


  @Deprecated
  private TokenProcessingOptions tokenProcessingOptions = null;


  @Deprecated
  private TokenContextAnalyzer dictionary = null;


  @Deprecated
  private final Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers =
      new HashMap<>();


  @Deprecated
  private HashMap<AttributeAnalyzerType, List<String[]>> ruleBasedAttributeAnalyzerDescriptors;

  @Deprecated
  private Collection<String> attributeConflictManagerDescriptors;

  @Deprecated
  private ContextSifter contextSifter = null;

  private TSTrainingParameterObject trainingParameters = null;

  public RaptatAnnotationSolution() {
    RaptatAnnotationSolution.LOGGER.setLevel(Level.INFO);
  }

  @Deprecated
  public Map<ContextType, AttributeTaggerSolution> getAllAttributeTaggerSolutions() {
    return this.taggerSolutionMap;
  }

  /** @return the attributeAnalyzers */
  @Deprecated
  public HashMap<AttributeAnalyzerType, List<String[]>> getAttributeAnalyzerDescriptors() {
    return this.ruleBasedAttributeAnalyzerDescriptors;
  }

  /** @return the attributeConflictManagers */
  @Deprecated
  public Collection<String> getAttributeConflictManagerDescriptors() {
    return this.attributeConflictManagerDescriptors;
  }

  @Deprecated
  public AttributeTaggerSolution getAttributeTaggerSolution(ContextType contextType) {
    return this.taggerSolutionMap.get(contextType);
  }

  @Deprecated
  public ContextSifter getContextSifter() {
    return this.contextSifter;
  }


  /** @return the dictionary */
  @Deprecated
  public TokenContextAnalyzer getDictionary() {
    return this.dictionary;
  }


  /** @return the sentenceAndSectionMatchers */
  @Deprecated
  public Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> getSentenceAndSectionMatchers() {
    return this.sentenceAndSectionMatchers;
  }


  /** @return the tokenProcessingOptions */
  @Deprecated
  public TokenProcessingOptions getTokenProcessingOptions() {
    return this.tokenProcessingOptions;
  }


  /** @return the annotator */
  public TokenSequenceFinderSolution getTokenSequenceFinderSolution() {
    return this.tokenSequenceFinderSolution;
  }


  /** @return the trainingParameters */
  public TSTrainingParameterObject getTrainingParameters() {
    return this.trainingParameters;
  }


  public int saveToFile(File solutionFile) {
    try {
      ObjectOutputStream oos =
          new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(solutionFile),
              RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));
      oos.writeObject(this);
      oos.flush();
      oos.close();
    } catch (IOException ex) {
      System.err.println("Exception writing file " + solutionFile + ": " + ex);
      return -1;
    }
    return 0;
  }


  public int saveToFile(String pathToFile) {
    File solutionFile = new File(pathToFile);
    return this.saveToFile(solutionFile);
  }


  /**
   * @param cueScopeModels the cueScopeModels to set
   */
  @Deprecated
  public void setAttributeTaggers(HashMap<ContextType, AttributeTaggerSolution> cueScopeModels) {
    this.taggerSolutionMap = cueScopeModels;
  }


  @Deprecated
  public void setContextSifter(ContextSifter contextSifter) {
    this.contextSifter = contextSifter;
  }


  /**
   * @param tokenProcessingOptions the tokenProcessingOptions to set
   */
  @Deprecated
  public void setTokenProcessingOptions(TokenProcessingOptions tokenProcessingOptions) {
    this.tokenProcessingOptions = tokenProcessingOptions;
  }


  /**
   * @param tokenSequenceFinderSolution the tokenSequenceFinderSolution to set
   */
  public void setTokenSequenceFinderSolution(
      TokenSequenceFinderSolution tokenSequenceFinderSolution) {
    this.tokenSequenceFinderSolution = tokenSequenceFinderSolution;
  }


  /**
   * @param trainingParameters the trainingParameters to set
   */
  public void setTrainingParameters(TSTrainingParameterObject trainingParameters) {
    this.trainingParameters = trainingParameters;
  }


  public boolean validateProcessingOptions(HashMap<ContextType, AttributeTaggerSolution> taggers) {
    return this.validateProcessingOptions(this.tokenSequenceFinderSolution, taggers);
  }


  @SuppressWarnings("unused")
  private TokenSequenceFinderSolution loadTSFinder() {
    File tsSolution = GeneralHelper.getFile("Load TokenSequenceFinder Solution File");

    return TokenSequenceFinderSolution.loadFromFile(tsSolution);
  }


  /**
   * Check when saving that all the settings for TokenProcessingOptions are consistent among all the
   * different solutions
   *
   * @param tsSolution
   * @param attributeTaggers2
   * @param tokenProcessingOptions2
   */
  private boolean validateProcessingOptions(TokenSequenceFinderSolution tsSolution,
      HashMap<ContextType, AttributeTaggerSolution> taggers) {
    TokenProcessingOptions tsOptions = tsSolution.retrieveTokenProcessingOptions();
    if (taggers != null) {
      for (AttributeTaggerSolution curSolution : taggers.values()) {
        if (!tsOptions.equals(curSolution.getTokenProcessingOptions())) {
          return false;
        }
      }
    }

    return true;
  }


  public static RaptatAnnotationSolution loadFromFile(File solutionFile) {
    RaptatAnnotationSolution theSolution = null;
    ObjectInputStream inObject = null;
    System.out.println("Solution file located:" + solutionFile.exists());

    try {
      inObject = new ObjectInputStream(new FileInputStream(solutionFile));
      theSolution = (RaptatAnnotationSolution) inObject.readObject();
    } catch (IOException e) {
      System.out.println("Unable to open and read in solution file.  " + e.getClass().getName()
          + " Error " + e.getMessage());
    } catch (ClassNotFoundException e) {
      System.out.println("Unable to open and read in solution file. " + e.getClass().getName()
          + " Error " + e.getMessage());
    } finally {
      try {
        if (inObject != null) {
          inObject.close();
        }
      } catch (IOException e) {
        System.out.println("Unable to close solution file\n" + e.getMessage());
      }
    }

    return theSolution;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        RaptatAnnotationSolution solution = RaptatAnnotationSolution.loadFromFile(new File(
            "D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\newCrossValidSolutionForComparison_v08_20160809160750.soln"));
        ProbabilisticTSFinderSolution probSolution =
            (ProbabilisticTSFinderSolution) solution.tokenSequenceFinderSolution;
        probSolution.saveToFile(
            new File("D:\\CARTCL-IIR\\Glenn\\AKI_Analysis_160427\\newCrossValidSolution.soln"));
      }
    });
  }


  public static void packageAndSaveSolution(TSTrainingParameterObject parameters,
      TokenSequenceFinderSolution solution, File solutionFile) {
    RaptatAnnotationSolution raSolution = new RaptatAnnotationSolution();
    raSolution.tokenSequenceFinderSolution = solution;
    raSolution.setTrainingParameters(parameters);
    raSolution.saveToFile(solutionFile);
    String message = "The Raptat Trained solution file has been saved to file";
    JOptionPane.showMessageDialog(null, message, "Training Completed",
        JOptionPane.INFORMATION_MESSAGE);
  }


  /*
   * Changed so that all RaptatAnnotationSolution instances are stored using TSParameterObject
   * instance for storing the training parameters used during training and the parameters to use
   * during annotation. This TSTrainingParameterObject instance will always be created at the time
   * of training, whether it's through a property/configuration file or through a gui.
   */
  @Deprecated
  public static void packageAndSaveSolution(Window parent,
      TokenSequenceFinderSolution tsFinderSolution, File solutionFile, TextAnalyzer textAnalyzer) {
    Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers =
        textAnalyzer.getSentenceAndSectionMatchers();
    packageAndSaveSolution(parent, tsFinderSolution, solutionFile, textAnalyzer,
        sentenceAndSectionMatchers, null, null, null, null);
  }


  /*
   * Changed so that all RaptatAnnotationSolution instances are stored using TSParameterObject
   * instance for storing the training parameters used during training and the parameters to use
   * during annotation. This TSTrainingParameterObject instance will always be created at the time
   * of training, whether it's through a property/configuration file or through a gui.
   */
  @Deprecated
  public static void packageAndSaveSolution(Window parent,
      TokenSequenceFinderSolution tsFinderSolution, File solutionFile, TextAnalyzer textAnalyzer,
      Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers,
      Collection<AttributeTaggerSolution> cueScopeBasedTaggers,
      HashMap<AttributeAnalyzerType, List<String[]>> ruleBasedAttributeAnalyzers,
      Collection<String> attributeConflictManagerDescriptors, ContextSifter contextSifter) {
    RaptatAnnotationSolution raSolution = new RaptatAnnotationSolution();

    raSolution.setTrainingParameters(
        convertTrainingParametersToObject(sentenceAndSectionMatchers, cueScopeBasedTaggers,
            ruleBasedAttributeAnalyzers, attributeConflictManagerDescriptors, contextSifter));
    raSolution.tokenSequenceFinderSolution = tsFinderSolution;
    raSolution.tokenProcessingOptions =
        raSolution.tokenSequenceFinderSolution.retrieveTokenProcessingOptions();
    raSolution.contextSifter = contextSifter;
    raSolution.dictionary = textAnalyzer.getFacilitatorBlockerDictionary();

    if (ruleBasedAttributeAnalyzers != null) {
      raSolution.ruleBasedAttributeAnalyzerDescriptors = ruleBasedAttributeAnalyzers;
    }

    if (cueScopeBasedTaggers != null) {
      if (raSolution.taggerSolutionMap == null) {
        raSolution.taggerSolutionMap = new HashMap<>();
      }
      for (AttributeTaggerSolution taggerSolution : cueScopeBasedTaggers) {
        raSolution.taggerSolutionMap.put(taggerSolution.getContext(), taggerSolution);
      }
    }

    if (attributeConflictManagerDescriptors != null) {
      raSolution.attributeConflictManagerDescriptors = attributeConflictManagerDescriptors;
    }

    raSolution.saveToFile(solutionFile);
    String message = "The Raptat Trained solution file has been saved to file";
    JOptionPane.showMessageDialog(null, message, "Training Completed",
        JOptionPane.INFORMATION_MESSAGE);
  }


  private static TSTrainingParameterObject convertTrainingParametersToObject(
      Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers,
      Collection<AttributeTaggerSolution> cueScopeBasedTaggers,
      HashMap<AttributeAnalyzerType, List<String[]>> ruleBasedAttributeAnalyzers,
      Collection<String> attributeConflictManagerDescriptors, ContextSifter contextSifter) {
    // TODO Auto-generated method stub
    return null;
  }
}
