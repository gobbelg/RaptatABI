package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.OptimismCorrectionMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TSTrainingParameterObject.TrainingAndAnnotationParameterObject;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** OptionManager - Singleton class that
 * keeps track of which options have been set and allows for changing of options
 *
 * @author Glenn Gobbel, Aug 19, 2010
 *         <p>
 *         *********************************************************
 */
public final class OptionsManager {
  private final class AnalysisOptionsWindow extends JFrame implements ActionListener {
    private static final long serialVersionUID = -2504290679050972929L;
    JComboBox groupByColumnSelector, optimismSelector;
    JButton analysisSaveButton;
    JTextField sampleFractionField = new JTextField(10);
    JTextField iterationsFld = new JTextField(10);
    boolean windowInitiated = false;


    private AnalysisOptionsWindow(Point position, Dimension dim) {
      int windowWidth = (int) dim.getWidth() * 2 / 3;
      int windowHeight = (int) dim.getHeight() * 2 / 3;

      // Make sure window is big enough
      windowWidth = windowWidth < RaptatConstants.ANALYSIS_OPTIONS_MIN_WIDTH ? windowWidth
          : RaptatConstants.ANALYSIS_OPTIONS_MIN_WIDTH;
      windowHeight = windowHeight < RaptatConstants.ANALYSIS_OPTIONS_MIN_HEIGHT ? windowHeight
          : RaptatConstants.ANALYSIS_OPTIONS_MIN_HEIGHT;

      this.setSize(windowWidth, windowHeight);
      setTitle("Set Analysis Options");
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setResizable(false);

      position.translate(40, 40); // Move option window slightly down
      // and
      // to right
      this.setLocation(position);
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          OptionsManager.this.analysisOptWindowOpen = false;
        }
      });

      Box mainPanel = Box.createVerticalBox();
      mainPanel.add(buildGroupBySelectionPanel());

      JPanel textFldPanel_2 = new JPanel();
      Border b3 = BorderFactory.createTitledBorder("Fraction of Sample Size to Use for Training");
      textFldPanel_2.setBorder(b3);
      this.sampleFractionField.setText(Double.toString(OptionsManager.this.sampleFraction));
      this.sampleFractionField.setEditable(false);
      textFldPanel_2.add(this.sampleFractionField);

      JPanel textFldPanel_1 = new JPanel();
      Border b2 = BorderFactory.createTitledBorder("Number of Iterations");
      textFldPanel_1.setBorder(b2);
      this.iterationsFld.setText(Integer.toString(OptionsManager.this.bootstrapIterations));
      textFldPanel_1.add(this.iterationsFld);

      mainPanel.add(textFldPanel_2);
      mainPanel.add(textFldPanel_1);
      mainPanel.add(buildOptimismSelectionPanel());
      mainPanel.add(buildSavePanel());
      this.add(mainPanel);

      setVisible(true);
      this.windowInitiated = true;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
      if (this.windowInitiated) // Only respond once window created
      {
        Object eventSource = e.getSource();

        if (eventSource == this.analysisSaveButton) {
          processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
          saveAnalysisOptions();
        }
      }
    }


    private JPanel buildGroupBySelectionPanel() {
      JPanel gBYSelectionPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder("Select group by criteria");
      gBYSelectionPanel.setBorder(b1);
      this.groupByColumnSelector = new JComboBox(RaptatConstants.GROUP_BY_COLUMN_OPTIONS);

      this.groupByColumnSelector.setSelectedItem(
          RaptatConstants.GROUP_BY_COLUMN_OPTIONS[OptionsManager.this.grpByColNum]);

      this.groupByColumnSelector.addActionListener(this);
      gBYSelectionPanel.add(this.groupByColumnSelector);
      return gBYSelectionPanel;
    }


    private JPanel buildOptimismSelectionPanel() {
      JPanel optimismSelectionPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder("Select optimism for low prevalence instances");
      optimismSelectionPanel.setBorder(b1);
      this.optimismSelector = new JComboBox(OptimismCorrectionMethod.values());
      this.optimismSelector.setSelectedItem(OptionsManager.this.optimismCorrection);
      this.optimismSelector.addActionListener(this);
      optimismSelectionPanel.add(this.optimismSelector);
      return optimismSelectionPanel;
    }


    /**
     * ************************************************************** Build panel to save options
     * settings
     *
     * @return Component **************************************************************
     */
    private JPanel buildSavePanel() {
      JPanel savePanel = new JPanel();
      this.analysisSaveButton = new JButton("Save & Close");
      this.analysisSaveButton.addActionListener(this);
      savePanel.add(this.analysisSaveButton);
      return savePanel;
    }


    /**
     * ************************************************************** //Save all the settings
     * currently set in the options window dialog
     *
     * <p>
     * **************************************************************
     */
    private void saveAnalysisOptions() {
      String s1 = (String) this.groupByColumnSelector.getSelectedItem();

      {
        if (s1.equals("None")) {
          OptionsManager.this.grpByColNum = 0;
        } else {
          OptionsManager.this.grpByColNum = Integer.parseInt(s1);
        }
      }

      OptionsManager.this.optimismCorrection =
          (OptimismCorrectionMethod) this.optimismSelector.getSelectedItem();

      try {
        OptionsManager.this.sampleFraction = Double.parseDouble(this.sampleFractionField.getText());
        if (OptionsManager.this.sampleFraction < 0.0 || OptionsManager.this.sampleFraction > 1.0) {
          throw new NumberFormatException();
        }

      } catch (NumberFormatException e) {
        System.err.println(e.getMessage());
        GeneralHelper.errorWriter("<html><b>Sample Fraction Input Error:</b><br>"
            + "Please use a fraction between 0.0 and 1.0<html>");
        GeneralHelper.errorWriter("Sample fraction reset to " + OptionsManager.this.sampleFraction);
      }

      try {
        OptionsManager.this.bootstrapIterations = Integer.parseInt(this.iterationsFld.getText());
        if (OptionsManager.this.bootstrapIterations < 0) {
          throw new NumberFormatException();
        }
      } catch (NumberFormatException e) {
        System.err.println(e.getMessage());
        GeneralHelper.errorWriter("<html><b>Number of Iterations Input Error:</b><br>"
            + "Please use a positive integer<html>");
        GeneralHelper.errorWriter("Sample fraction reset to " + OptionsManager.this.sampleFraction);
      }
    }
  }

  private final class ConceptMappingOptionsWindow extends JFrame implements ActionListener {

    private static final long serialVersionUID = 8011321239003021191L;
    JButton mappingSaveButton;
    JComboBox exportTargetSelector;
    boolean windowInitiated = false;


    public ConceptMappingOptionsWindow(Point position, Dimension dim) {
      int windowWidth = (int) dim.getWidth() * 2 / 3;
      int windowHeight = (int) dim.getHeight() * 2 / 3;
      windowWidth = windowWidth < RaptatConstants.MAP_OPTIONS_MIN_WIDTH ? windowWidth
          : RaptatConstants.MAP_OPTIONS_MIN_WIDTH;
      windowHeight = windowHeight < RaptatConstants.MAP_OPTIONS_MIN_HEIGHT ? windowHeight
          : RaptatConstants.MAP_OPTIONS_MIN_HEIGHT;
      this.setSize(windowWidth, windowHeight);
      setTitle("Set Concept Mapping Options");
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setResizable(false);
      setBackground(new Color(100, 149, 237));
      position.translate(40, 40); // Move option window slightly down
      // and
      // to right
      this.setLocation(position);

      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          OptionsManager.this.mapOptWindowOpen = false;
        }
      });

      Box mainPanel = Box.createVerticalBox();
      mainPanel.add(buildExportTargetPanel());
      mainPanel.add(buildSavePanel());
      mainPanel.setBackground(new Color(100, 149, 237));
      this.add(mainPanel);

      setVisible(true);
      this.windowInitiated = true;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
      if (this.windowInitiated) // Only respond once window created
      {
        Object eventSource = e.getSource();
        if (eventSource == this.mappingSaveButton) {
          processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
          saveMappingOptions();
        }
      }
    }


    public void saveMappingOptions() {
      OptionsManager.this.exportFormat = (String) this.exportTargetSelector.getSelectedItem();
    }


    private JPanel buildExportTargetPanel() {
      JPanel exportTargetPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder("Select export format");
      exportTargetPanel.setBorder(b1);
      this.exportTargetSelector = new JComboBox(RaptatConstants.EXPORTABLE_FILETYPES);

      this.exportTargetSelector.addActionListener(this);
      this.exportTargetSelector.setSelectedItem(OptionsManager.this.exportFormat);
      exportTargetPanel.add(this.exportTargetSelector);
      return exportTargetPanel;
    }


    /**
     * ************************************************************** Build panel to save options
     * settings
     *
     * @return Component **************************************************************
     */
    private JPanel buildSavePanel() {
      JPanel savePanel = new JPanel();
      this.mappingSaveButton = new JButton("Save & Close");
      this.mappingSaveButton.addActionListener(this);
      savePanel.add(this.mappingSaveButton);
      savePanel.setBackground(new Color(100, 149, 237));

      return savePanel;
    }
  }

  private final class TestOptionsWindow extends JFrame implements ActionListener {
    private static final long serialVersionUID = -2504190679050972929L;
    JComboBox learningMethods;
    ButtonGroup smoothGroup, expGroup, cmpGroup, summaryGroup;
    JButton testSaveButton;
    boolean windowInitiated = false;


    private TestOptionsWindow(Point position, Dimension dim) {
      int windowWidth = (int) dim.getWidth() * 2 / 3;
      int windowHeight = (int) dim.getHeight() * 1 / 3;

      // Make sure window is big enough
      windowWidth = windowWidth < RaptatConstants.TEST_OPTIONS_MIN_WIDTH
          ? RaptatConstants.TEST_OPTIONS_MIN_WIDTH
          : windowWidth;
      windowHeight = windowHeight < RaptatConstants.TEST_OPTIONS_MIN_HEIGHT
          ? RaptatConstants.TEST_OPTIONS_MIN_HEIGHT
          : windowHeight;

      this.setSize(windowWidth, windowHeight);
      setTitle("Set Testing Options");
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setResizable(false);
      position.translate(40, 40); // Move option window slightly down
      // and
      // to right
      this.setLocation(position);
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          OptionsManager.this.testOptWindowOpen = false;
        }
      });
      Box mainPanel = Box.createVerticalBox();

      mainPanel.add(buildTopPanel());
      // mainPanel.add(buildStemsPanel());
      mainPanel.add(buildExpCmpPanel());
      mainPanel.add(buildSavePanel());

      this.add(mainPanel);

      // pack();
      setVisible(true);
      this.windowInitiated = true;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
      if (this.windowInitiated) // Only respond once window created
      {
        Object eventSource = e.getSource();
        if (eventSource == this.learningMethods) {
          String s1 = (String) this.learningMethods.getSelectedItem();
          if (s1.equals("Decision Tree")) {
            inActivate(this.smoothGroup);
          } else {
            activate(this.smoothGroup);
          }
        } else if (eventSource == this.testSaveButton) {
          processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
          saveTestOptions();
        }
      }
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException("Clone is not allowed.");
    }


    /**
     * ************************************************************** Set a button group so all
     * elements are not selected and disabled
     *
     * @param theGroups **************************************************************
     */
    private void activate(ButtonGroup theGroup) {
      AbstractButton curButton;
      for (Enumeration<AbstractButton> e = theGroup.getElements(); e.hasMoreElements();) {
        curButton = e.nextElement();
        curButton.setEnabled(true);
      }
    }


    /**
     * ************************************************************** buildCmpPanel - deletes words
     * from a phrase during testing to see if the matching improves
     *
     * @return JPanel **************************************************************
     */
    private JPanel buildCmpPanel(String panelTitle) {
      JPanel buttonPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder(panelTitle);
      buttonPanel.setBorder(b1);

      this.cmpGroup = new ButtonGroup();
      JRadioButton curButton;

      for (int i = 0; i <= RaptatConstants.MAX_WORD_CHANGE; i++) {
        curButton = new JRadioButton(RaptatConstants.NUMSTRINGS[i]);
        curButton.setEnabled(false); // Set to true once implemented
        if (OptionsManager.this.numToCompress == i) {
          curButton.setSelected(true);
        } else {
          curButton.setSelected(false);
        }
        this.cmpGroup.add(curButton);
        buttonPanel.add(curButton);
      }

      return buttonPanel;
    }


    /**
     * ************************************************************** Build panel to determine how
     * many virtual words to expand or delete from a phrase during testing
     *
     * @return JPanel **************************************************************
     */
    private JPanel buildExpCmpPanel() {
      JPanel expCmpPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder("Phrase Expansion/Compression");
      expCmpPanel.setBorder(b1);

      expCmpPanel.add(buildExpPanel(RaptatConstants.COMPRESSION_TITLE));
      expCmpPanel.add(buildCmpPanel(RaptatConstants.EXPANSION_TITLE));

      return expCmpPanel;
    }


    /**
     * ************************************************************** buildExpPanel - helper method
     * for buildExpCmpPanel() method - adds or deletes words from a phrase during testing to see if
     * the matching improves
     *
     * @return JPanel **************************************************************
     */
    private JPanel buildExpPanel(String panelTitle) {
      JPanel buttonPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder(panelTitle);
      buttonPanel.setBorder(b1);

      this.expGroup = new ButtonGroup();
      JRadioButton curButton;

      for (int i = 0; i <= RaptatConstants.MAX_WORD_CHANGE; i++) {
        curButton = new JRadioButton(RaptatConstants.NUMSTRINGS[i]);
        curButton.setEnabled(false);
        if (OptionsManager.this.numToExpand == i) {
          curButton.setSelected(true);
        } else {
          curButton.setSelected(false); // Set to true once
        }
        // implemented
        this.expGroup.add(curButton);
        buttonPanel.add(curButton);
      }

      return buttonPanel;
    }


    // Modified by Glenn on Feb 27, 2012
    // No longer used as we now get the stemming based
    // on the training of the solution file
    // /****************************************************************
    // * Create JPanel to hold the buttons to allow for either standard or
    // no
    // * smoothing during testing
    // *
    // * @return JPanel
    // ****************************************************************/
    // private JPanel buildStemsPanel() {
    // useStemsForTestingGroup = new ButtonGroup();
    // String panelTitle = "Use Token Stems for Testing?";
    // return buildYesNoPanel(panelTitle, useStemsForTestingGroup,
    // useStemsForTesting);
    // }

    /**
     * ************************************************************** Build panel to save options
     * settings
     *
     * @return Component **************************************************************
     */
    private JPanel buildSavePanel() {
      JPanel savePanel = new JPanel();
      this.testSaveButton = new JButton("Save & Close");
      this.testSaveButton.addActionListener(this);
      savePanel.add(this.testSaveButton);
      return savePanel;
    }


    /**
     * ************************************************************** Create JPanel to hold the
     * buttons to allow for either standard or no smoothing during testing
     *
     * @return JPanel **************************************************************
     */
    private JPanel buildSmoothPanel() {
      JPanel smoothPanel = new JPanel();
      Border b1 = BorderFactory.createTitledBorder("Zero Occurrence Filtering");
      smoothPanel.setBorder(b1);
      this.smoothGroup = new ButtonGroup();
      JRadioButton curButton;

      for (String theFilter : RaptatConstants.FILTER_STRINGS) {
        curButton = new JRadioButton(theFilter);
        if (theFilter.equals(OptionsManager.this.curFilter)) {
          curButton.setSelected(true);
        } else {
          curButton.setSelected(false);
        }

        if (OptionsManager.this.curLearnMethod.equals("Decision Tree")) {
          curButton.setEnabled(false);
        } else {
          curButton.setEnabled(true);
        }

        this.smoothGroup.add(curButton);
        smoothPanel.add(curButton);
      }
      return smoothPanel;
    }


    /**
     * ************************************************************** Create and return a panel that
     * allows user to select method of machine learning used during training.
     *
     * @return JPanel **************************************************************
     */
    private Box buildTopPanel() {
      Box topPanel = Box.createHorizontalBox();
      topPanel.add(buildSmoothPanel());

      this.summaryGroup = new ButtonGroup();
      topPanel.add(OptionsManager.this.buildYesNoPanel("Generate Summaries?", this.summaryGroup,
          OptionsManager.this.createSummary));

      return topPanel;
    }


    /**
     * ************************************************************** Set a button group so all
     * elements are not selected and disabled
     *
     * @param theGroups **************************************************************
     */
    private void inActivate(ButtonGroup theGroup) {
      AbstractButton curButton;
      for (Enumeration<AbstractButton> e = theGroup.getElements(); e.hasMoreElements();) {
        curButton = e.nextElement();
        curButton.setEnabled(false);
      }
    }


    /**
     * ************************************************************** Save all the settings
     * currently set in the options window dialog.
     *
     * <p>
     * **************************************************************
     */
    private void saveTestOptions() {
      AbstractButton curButton;
      int i;

      Enumeration<AbstractButton> e = this.smoothGroup.getElements();
      while (e.hasMoreElements()) {
        curButton = e.nextElement();
        if (curButton.isSelected()) {
          OptionsManager.this.curFilter = curButton.getText();
          break;
        }
      }

      e = this.expGroup.getElements();
      i = 0;
      while (e.hasMoreElements()) {
        curButton = e.nextElement();
        if (curButton.isSelected()) {
          OptionsManager.this.numToExpand = i;
          break;
        }
        i++;
      }

      e = this.cmpGroup.getElements();
      i = 0;
      while (e.hasMoreElements()) {
        curButton = e.nextElement();
        if (curButton.isSelected()) {
          OptionsManager.this.numToCompress = i;
          break;
        }
        i++;
      }

      e = this.summaryGroup.getElements();
      i = 0;
      while (e.hasMoreElements()) {
        curButton = e.nextElement();
        if (curButton.isSelected()) {
          if (curButton.getText().equals("Yes")) {
            OptionsManager.this.createSummary = true;
          } else {
            OptionsManager.this.createSummary = false;
          }

          break;
        }
        i++;
      }
    }
  }

  private static OptionsManager optMgrInstance = null;


  boolean trainOptWindowOpen = false;

  private boolean testOptWindowOpen = false;
  private boolean mapOptWindowOpen = false;
  // Malini:Added parameters for setting Analysis options
  private boolean analysisOptWindowOpen = false;
  protected int grpByColNum = RaptatConstants.GROUPBY_COLUMN_NUMBER_DEFAULT;

  protected int sampleSize = RaptatConstants.BOOTSTRAP_SAMPLE_SIZE_DEFAULT;
  protected double sampleFraction = RaptatConstants.BOOTSTRAP_SAMPLE_FRACTION_DEFAULT;
  private final double sequenceThreshold = RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT;
  // Set default parameters
  private String curFilter = RaptatConstants.FILTER_START;
  protected String curLearnMethod = RaptatConstants.CONCEPT_MAPPING_METHOD_DEFAULT;
  private int numToCompress = RaptatConstants.NUM_COMPRESS_START;
  private int numToExpand = RaptatConstants.NUM_EXPAND_START;
  private boolean createSummary = RaptatConstants.CREATE_SUMMARY_START;
  boolean includeNulls = RaptatConstants.USE_NULLS_START;
  private int bootstrapIterations = RaptatConstants.BOOTSTRAP_ITERATIONS_DEFAULT;
  private String exportFormat =
      RaptatConstants.EXPORTABLE_FILETYPES[RaptatConstants.EXPORT_FORMAT_START];
  boolean useStems = RaptatConstants.USE_TOKEN_STEMMING;
  boolean usePOS = RaptatConstants.USE_PARTS_OF_SPEECH;
  boolean invertTokenSequence = RaptatConstants.INVERT_TOKEN_SEQUENCE;
  boolean removeStopWords = RaptatConstants.REMOVE_STOP_WORDS;
  private int trainingTokenNumber = RaptatConstants.MAX_TOKENS_DEFAULT;
  protected ContextHandling reasonNoMedsProcessing =
      RaptatConstants.REASON_NO_MEDS_PROCESSING_DEFAULT;
  protected ContextHandling negationProcessing = RaptatConstants.NEGATION_HANDLING_DEFAULT;
  private java.util.List<String> annotatedFileNames = RaptatConstants.RPT_ANNOTATED_FILENAMES;
  private java.util.List<String> annotatedFilePaths = RaptatConstants.RPT_ANNOTATED_FILEPATHS;
  private File lastSelectedDir = RaptatConstants.LAST_SELECTED_DIR;
  private boolean keepUnlabeledOnDisk = RaptatConstants.STORE_UNLABELED_ON_DISK_DURING_TRAINING;
  private boolean saveUnlabeledToDisk = RaptatConstants.SAVE_UNLABELED_TO_DISK_AFTER_TRAINING;

  protected TSFinderMethod phraseIDTrainingMethod =
      RaptatConstants.PHRASE_ID_TRAINING_METHOD_DEFAULT;

  protected PhraseIDSmoothingMethod phraseIDSmoothingMethod =
      RaptatConstants.SMOOTHING_FOR_PHRASE_ID_DEFAULT;

  protected TSFinderMethod postTokensTrainingMethod = this.phraseIDTrainingMethod;
  protected TSFinderMethod priorTokensTrainingMethod = this.phraseIDTrainingMethod;
  protected boolean linkPhraseToPriorTokens = false;
  protected boolean linkPhraseToPostTokens = false;

  protected int annotationSurroundingTokens = RaptatConstants.PHRASE_ID_SURROUNDING_TOKENS_DEFAULT;
  private File scoreDir = null;
  private java.util.List<String> annotationResultPaths = new Vector<>();
  private Point mainWindowPosition;
  private Dimension mainWindowSize;
  protected boolean annotationTrainOptWindowOpen;
  protected boolean attributeTaggerWindowOpen = false;

  private OptimismCorrectionMethod optimismCorrection = RaptatConstants.OPTIMISM_CORRECTION_DEFAULT;

  /**
   * *************************************************************** Private so that it cannot be
   * instantiated more than once ***************************************************************
   */
  private OptionsManager() {}


  public java.util.List<String> getAnnotatedFileNames() {
    return this.annotatedFileNames;
  }


  public java.util.List<String> getAnnotatedFilePaths() {
    return this.annotatedFilePaths;
  }


  public java.util.List<String> getAnnotationResultPaths() {
    return this.annotationResultPaths;
  }


  // returns the number of Iterations to be performed by the BootStrap class
  public int getBootstrapIterations() {
    return this.bootstrapIterations;
  }


  /**
   * **********************************************************
   *
   * @return createSummary **********************************************************
   */
  public boolean getCreateSummary() {
    return this.createSummary;
  }


  /**
   * **************************************************************
   *
   * @return the curFilter
   * @uml.property name="curFilter" ***************************************************************
   */
  public String getCurFilter() {
    return this.curFilter;
  }


  /**
   * **************************************************************
   *
   * @return the curLearnMethod
   * @uml.property name="curLearnMethod"
   *               ***************************************************************
   */
  public String getCurLearnMethod() {
    return this.curLearnMethod;
  }


  // Return file type format for exporting mapping of phrases to concepts
  public String getExportFormat() {
    return this.exportFormat;
  }


  public int getGrpByColNum() {
    return this.grpByColNum;
  }


  public boolean getInvertTokenSequence() {
    return this.invertTokenSequence;
  }


  /** @return the keepUnlabeledOnDisk */
  public boolean getKeepUnlabeledOnDisk() {
    return this.keepUnlabeledOnDisk;
  }


  public File getLastSelectedDir() {
    return this.lastSelectedDir;
  }


  /** @return the linkPhraseToPostTokens */
  public boolean getLinkPhraseToPostTokens() {
    return this.linkPhraseToPostTokens;
  }


  /** @return the linkPhraseToPriorTokens */
  public boolean getLinkPhraseToPriorTokens() {
    return this.linkPhraseToPriorTokens;
  }


  public ContextHandling getNegationProcessing() {
    return this.negationProcessing;
  }


  /**
   * **************************************************************
   *
   * @return the numToCompress
   * @uml.property name="numToCompress"
   *               ***************************************************************
   */
  public int getNumToCompress() {
    return this.numToCompress;
  }


  // Deprecated by Glenn on Feb 27, 2012
  // We now use on useStems which can only be set via Training
  // so we no longer differentiate between trainingStems and
  // testing stems
  // /************************************************************
  // * @return useStemsForTraining
  // ***********************************************************/
  // public boolean getTrainingStems() {
  // return useStems;
  // }
  //
  // /************************************************************
  // * @return useStemsForTesting
  // ************************************************************/
  // public boolean getTestingStems() {
  // return useStems;
  // }

  /**
   * **************************************************************
   *
   * @return the numToExpand
   * @uml.property name="numToExpand"
   *               ***************************************************************
   */
  public int getNumToExpand() {
    return this.numToExpand;
  }


  public OptimismCorrectionMethod getOptimismCorrection() {
    return this.optimismCorrection;
  }


  public TSFinderMethod getPhraseIDTrainingMethod() {
    return this.phraseIDTrainingMethod;
  }


  /** @return the postTokensTrainingMethod */
  public TSFinderMethod getPostTokensTrainingMethod() {
    return this.postTokensTrainingMethod;
  }


  /** @return the priorTokensTrainingMethod */
  public TSFinderMethod getPriorTokensTrainingMethod() {
    return this.priorTokensTrainingMethod;
  }


  public ContextHandling getReasonNoMedsProcessing() {
    return this.reasonNoMedsProcessing;
  }


  public boolean getRemoveStopWords() {
    return this.removeStopWords;
  }


  public double getSampleFraction() {
    return this.sampleFraction;
  }


  public int getSampleSize() {
    return this.sampleSize;
  }


  public boolean getSaveUnlabeledToDisk() {
    return this.saveUnlabeledToDisk;
  }


  public File getScoreDirectory() {
    return this.scoreDir;
  }


  public PhraseIDSmoothingMethod getSmoothingMethod() {
    return this.phraseIDSmoothingMethod;
  }


  /** @return the annotationSurroundingTokens */
  public int getSurroundingTokenNumber() {
    return this.annotationSurroundingTokens;
  }


  /** @return the trainingTokenNumber */
  public int getTrainingTokenNumber() {
    return this.trainingTokenNumber;
  }


  /** @return */
  public boolean getUseNegationContext() {
    return this.negationProcessing != ContextHandling.NONE;
  }


  /**
   * **********************************************************
   *
   * @return includeNulls **********************************************************
   */
  public boolean getUseNulls() {
    return this.includeNulls;
  }


  public boolean getUsePOS() {
    return this.usePOS;
  }


  /** @return */
  public boolean getUseReasonNoMedsContext() {
    return this.reasonNoMedsProcessing != ContextHandling.NONE;
  }


  public boolean getUseStems() {
    return this.useStems;
  }


  /**
   * Feb 20, 2018
   *
   * @param taParameters
   */
  public void initializeTrainingAndAnnotationTokenOptions(
      TrainingAndAnnotationParameterObject taParameters) {
    this.useStems = taParameters.useStems;
    this.invertTokenSequence = taParameters.invertTokenSequence;
    this.usePOS = taParameters.usePOS;
    this.removeStopWords = taParameters.removeStopWords;
  }


  /** @return the attributeTaggerWindowOpen */
  public boolean isAttributeTaggerWindowOpen() {
    return this.attributeTaggerWindowOpen;
  }


  // Malini: Set options for Bootstrap analysis
  public void setAnalysisOptions(Point location, Dimension size) {
    if (!this.analysisOptWindowOpen) {
      this.analysisOptWindowOpen = true;
      new AnalysisOptionsWindow(location, size);
    }
  }


  public void setAnnotatedFileNames(java.util.List<String> files) {
    this.annotatedFileNames = files;
  }


  public void setAnnotatedFilePaths(java.util.List<String> paths) {
    this.annotatedFilePaths = paths;
  }


  public void setAnnotationResultPaths(java.util.List<String> thePaths) {
    this.annotationResultPaths = thePaths;
  }


  public void setAnnotationTrainOptions() {
    this.setAnnotationTrainOptions(this.mainWindowPosition, this.mainWindowSize);
  }


  public void setAnnotationTrainOptions(Point position, Dimension size) {
    if (!this.annotationTrainOptWindowOpen) {
      this.annotationTrainOptWindowOpen = true;

      // Use defaults if position or size are null
      if (position == null) {
        position = new Point(0, 0);
      }
      if (size == null) {
        size = new Dimension(0, 0);
      }
      new AnnotationTrainOptionsWindow(position, size);
    }
  }


  /**
   * @param attributeTaggerWindowOpen the attributeTaggerWindowOpen to set
   */
  public void setAttributeTaggerWindowOpen(boolean attributeTaggerWindowOpen) {
    this.attributeTaggerWindowOpen = attributeTaggerWindowOpen;
  }


  /**
   * ************************************************************** Set the options used for mapping
   * phrases to concepts using a loaded solution. This is not currently used.
   *
   * @param location
   * @param size **************************************************************
   */
  public void setConceptMappingOptions(Point location, Dimension size) {
    if (!this.mapOptWindowOpen) {
      this.mapOptWindowOpen = true;
      new ConceptMappingOptionsWindow(location, size);
    }
  }


  public void setConceptMapTrainOptions() {
    this.setConceptMapTrainOptions(this.mainWindowPosition, this.mainWindowSize);
  }


  public void setConceptMapTrainOptions(Point position, Dimension size) {
    if (!this.trainOptWindowOpen) {
      this.trainOptWindowOpen = true;

      // Use defaults if position or size are null
      if (position == null) {
        position = new Point(0, 0);
      }
      if (size == null) {
        size = new Dimension(0, 0);
      }
      new ConceptMapTrainOptionsWindow(position, size);
    }
  }


  // Set file type format for exporting mapping of phrases to concepts
  public void setExportFormat(String exportFormat) {
    this.exportFormat = exportFormat;
  }


  public void setInvertTokenSequence(boolean invertTokenSequence) {
    this.invertTokenSequence = invertTokenSequence;
  }


  /**
   * @param keepUnlabeledOnDisk the keepUnlabeledOnDisk to set
   */
  public void setKeepUnlabeledOnDisk(boolean keepUnlabeledOnDisk) {
    this.keepUnlabeledOnDisk = keepUnlabeledOnDisk;
  }


  public void setLastSelectedDir(File dir) {
    if (!dir.isDirectory()) {
      dir = dir.getParentFile();
    }
    this.lastSelectedDir = dir;
  }


  /**
   * @param phraseIDSmoothingMethod the phraseIDSmoothingMethod to set
   */
  public void setPhraseIDSmoothingMethod(PhraseIDSmoothingMethod phraseIDSmoothingMethod) {
    this.phraseIDSmoothingMethod = phraseIDSmoothingMethod;
  }


  public void setPhraseIDTrainingMethod(TSFinderMethod phraseIDTrainingMethod) {
    this.phraseIDTrainingMethod = phraseIDTrainingMethod;
  }


  public void setRemoveStopWords(boolean removeStopWords) {
    this.removeStopWords = removeStopWords;
  }


  public void setSaveUnlabeledToDisk(boolean saveUnlabeledToDisk) {
    this.saveUnlabeledToDisk = saveUnlabeledToDisk;
  }


  public void setScoreDir(File scoreSavelocation) {
    this.scoreDir = scoreSavelocation;
  }


  public void setStemmingOption(boolean value) {
    this.useStems = value;
  }


  /**
   * ************************************************************** Set the options used for testing
   * the current system
   *
   * @param location
   * @param size **************************************************************
   */
  public void setTestOptions(Point location, Dimension size) {
    if (!this.testOptWindowOpen) {
      this.testOptWindowOpen = true;
      new TestOptionsWindow(location, size);
    }
  }


  /**
   * @param trainingTokenNumber the trainingTokenNumber to set
   */
  public void setTrainingTokenNumber(int trainingTokenNumber) {
    this.trainingTokenNumber = trainingTokenNumber;
  }


  public void setUseNullsOption(boolean useNulls) {
    this.includeNulls = useNulls;
  }


  public void setUsePOS(boolean value) {
    this.usePOS = value;
  }


  public void storeMainWindowPositionSize(Point position, Dimension size) {
    this.mainWindowPosition = position;
    this.mainWindowSize = size;
  }


  /**
   * ************************************************************** Create JPanel to hold the
   * buttons to allow or disallow some feature
   *
   * @return JPanel **************************************************************
   */
  protected JPanel buildYesNoPanel(String panelTitle, ButtonGroup theButtons, boolean startValue,
      int titleJustification, Border panelBorder) {
    JPanel thePanel = new JPanel();
    Border b1 = BorderFactory.createTitledBorder(panelBorder, panelTitle, titleJustification,
        TitledBorder.DEFAULT_POSITION);
    thePanel.setBorder(b1);
    JRadioButton curButton;

    String[] potentialAnswers = {"Yes", "No"};

    for (int i = 0; i < potentialAnswers.length; i++) {
      curButton = new JRadioButton(potentialAnswers[i]);
      curButton.setEnabled(true);

      if (potentialAnswers[i].equals("Yes")) {
        if (startValue) {
          curButton.setSelected(true);
        } else {
          curButton.setSelected(false);
        }
      } else {
        if (startValue) {
          curButton.setSelected(false);
        } else {
          curButton.setSelected(true);
        }
      }

      theButtons.add(curButton);
      thePanel.add(curButton);
    }
    return thePanel;
  }


  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException("Clone is not allowed.");
  }


  /**
   * ************************************************************** Create JPanel to hold the
   * buttons to allow or disallow some feature
   *
   * @return JPanel **************************************************************
   */
  JPanel buildYesNoPanel(String panelTitle, ButtonGroup theButtons, boolean startValue) {
    return this.buildYesNoPanel(panelTitle, theButtons, startValue,
        TitledBorder.DEFAULT_JUSTIFICATION, BorderFactory.createEtchedBorder());
  }


  public static synchronized OptionsManager getInstance() {
    if (OptionsManager.optMgrInstance == null) {
      OptionsManager.optMgrInstance = new OptionsManager();
    }
    return OptionsManager.optMgrInstance;
  }
}
