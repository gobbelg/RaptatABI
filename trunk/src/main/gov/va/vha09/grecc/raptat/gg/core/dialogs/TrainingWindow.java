/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.training.CRFSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.training.ProbabilisticSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AttributeAnalyzerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AttributeConflictManagerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.SentenceSectionTaggerType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.SchemaSettingsOptionWindow;
import src.main.gov.va.vha09.grecc.raptat.gg.core.training.TokenSequenceSolutionTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.JdomXMLImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;

/** @author Glenn T. Gobbel Mar 23, 2016 */
public class TrainingWindow extends BaseClassWindows {

  /** @author Glenn T. Gobbel Apr 18, 2016 */
  public class FeaturePipesButtonListener implements ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent )
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      GeneralHelper.getFile("Select file describing feature pipe");
    }
  }

  public class LoadDictionaryListener implements ActionListener {
    /**
     * Create the Hash Set for the concepts.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser =
          new JFileChooser(OptionsManager.getInstance().getLastSelectedDir());

      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setFileFilter(new FileNameExtensionFilter("Select Dictionary File", "txt"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
        String chosenFilePath = fileChooser.getSelectedFile().getAbsolutePath();
        TrainingWindow.this.dictionaryFilePath.setText(chosenFilePath);
        TrainingWindow.this.dictionaryFilePath.setToolTipText(chosenFilePath);
        JButton button = (JButton) e.getSource();
        button.setBorder(BorderFactory.createLineBorder(Color.BLUE));
      }
    }
  }

  public class LoadSectionizersListener implements ActionListener {
    /**
     * Create the Hash Set for the concepts.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser =
          new JFileChooser(OptionsManager.getInstance().getLastSelectedDir());

      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser
          .setFileFilter(new FileNameExtensionFilter("Select Sectionizer Descriptor File", "txt"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
        String chosenFilePath = fileChooser.getSelectedFile().getAbsolutePath();
        TrainingWindow.this.sectionAndSentenceMatchersFilePath.setText(chosenFilePath);
        TrainingWindow.this.dictionaryFilePath.setToolTipText(chosenFilePath);
        JButton button = (JButton) e.getSource();
        button.setBorder(BorderFactory.createLineBorder(Color.BLUE));
      }
    }
  }

  public enum TrainingWindowRunType {
    INSTANTIATE_WINDOW, DO_NOTHING, GENERATE_CIRRHOSIS_RADIOLOGY_SOLUTION;
  }

  private class AttributeConflictManagerListener implements ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent )
     */
    @Override
    public void actionPerformed(ActionEvent e) {

      if (e.getActionCommand().equalsIgnoreCase("yes")) {
        String dialogTitle = "Select conflict managers";
        CheckboxQueryDialog cqd = new CheckboxQueryDialog(TrainingWindow.this,
            TrainingWindow.this.conflictManagerCheckBoxes, dialogTitle);
        int queryResult = cqd.showQuery();

        if (queryResult == JOptionPane.OK_OPTION) {
          for (RaptatPair<JCheckBox, Boolean> checkBox : TrainingWindow.this.conflictManagerCheckBoxes) {
            String checkBoxTest = checkBox.left.getText().toLowerCase();
            /*
             * Respond to 2 conditions that need to be addressed: 1) Box was not previously check
             * and is now checked; 2) Box was previously checked and is now unchecked. We do not
             * need to respond if check box is unchanged.
             */
            if (checkBox.left.isSelected()) {
              /*
               * As the variable is a hash set, it will not add anything if it already exists
               */
              TrainingWindow.this.attributeConflictManagers.add(checkBoxTest);
            } else if (!checkBox.left.isSelected()) {
              TrainingWindow.this.attributeConflictManagers.remove(checkBoxTest);
            }
          }
        } else
        /* Reset prior settings of check boxes */
        {
          for (RaptatPair<JCheckBox, Boolean> checkBox : TrainingWindow.this.conflictManagerCheckBoxes) {
            String checkBoxName = checkBox.left.getText().toLowerCase();
            boolean managerExists =
                TrainingWindow.this.attributeConflictManagers.contains(checkBoxName);
            checkBox.left.setSelected(managerExists);
          }
          TrainingWindow.this.nocmButton.setSelected(true);
        }
      } else {
        TrainingWindow.this.conflictManagerCheckBoxes.clear();
        for (RaptatPair<JCheckBox, Boolean> checkBox : TrainingWindow.this.conflictManagerCheckBoxes) {
          checkBox.left.setSelected(false);
        }
      }
    }
  }

  /**
   * Creates the action listener for cancel button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 22, 2014
   */
  private class CancelButtonListener implements ActionListener {

    /**
     * Disposes the current window instance.
     *
     * @param arg0 Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      TrainingWindow.this.activeDialog.dispose();
      dispose();
      TrainingWindow.trainingWindowInstance = null;
    }
  }

  /**
   * Creates the action listener for Concept Mapping button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 22, 2014
   */
  private class ConceptMapOptionsListener implements ActionListener {

    /**
     * Set concept map training options.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      OptionsManager.getInstance().setConceptMapTrainOptions();
    }
  }

  private class CSAttributeListener implements ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent )
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase("yes")) {
        AttributeTaggerChooser taggerChooser =
            AttributeTaggerChooser.getInstance(null, null, null, null);
        Collection<AttributeTaggerSolution> taggers = taggerChooser.getTaggers();
        if (taggers == null || taggers.isEmpty()) {
          TrainingWindow.this.nocsTaggerButton.setSelected(true);
        } else {
          TrainingWindow.this.csTaggers.addAll(taggers);
        }
      } else {
        TrainingWindow.this.csTaggers.clear();
      }
    }
  }

  /*
   * This is for loading a rules-based attribute analyzer that generally looks at context around a
   * concept to assign attributes. This reads in the parameters needed to initialize a
   * ContextAttributeAnalyzer of a particular AttributeAnalyzerType. There should be one
   * ContextAttributeAnalayzer subclass for every AttributeAnalyzerType.
   */
  private class RuleBasedAttributeListener implements ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent )
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase("yes")) {
        String dialogTitle = "Select rule-based analyzers";
        CheckboxQueryDialog cqd = new CheckboxQueryDialog(TrainingWindow.this,
            TrainingWindow.this.rbAttributeCheckBoxes, dialogTitle);
        cqd.showQuery();
        int queryResult = cqd.showQuery();

        if (queryResult == JOptionPane.OK_OPTION) {
          for (RaptatPair<JCheckBox, Boolean> checkBox : TrainingWindow.this.rbAttributeCheckBoxes) {
            AttributeAnalyzerType analyzerType =
                AttributeAnalyzerType.getTypeFromDisplayName(checkBox.left.getText());
            /*
             * Respond to 2 conditions that need to be addressed: 1) Box was not previously check
             * and is now checked; 2) Box is now unchecked. We do not need to respond if check box
             * is unchanged.
             */
            if (checkBox.left.isSelected()
                && !TrainingWindow.this.ruleBasedAttributeAnalyzers.containsKey(analyzerType)) {
              boolean additionValid = addAttributeAnalyzer(checkBox.left.getText());
              if (!additionValid) {
                GeneralHelper
                    .errorWriter("Unable to add " + analyzerType.displayName + " type of analyzer");
                checkBox.left.setSelected(false);
                TrainingWindow.this.ruleBasedAttributeAnalyzers.remove(analyzerType);
              }
            } else if (!checkBox.left.isSelected()) {
              TrainingWindow.this.ruleBasedAttributeAnalyzers.remove(analyzerType);
            }
          }
        } else
        /* Reset prior settings of check boxes */
        {
          for (RaptatPair<JCheckBox, Boolean> checkBox : TrainingWindow.this.rbAttributeCheckBoxes) {
            AttributeAnalyzerType checkBoxType =
                AttributeAnalyzerType.getTypeFromDisplayName(checkBox.left.getText());
            boolean analyzerExists =
                TrainingWindow.this.ruleBasedAttributeAnalyzers.containsKey(checkBoxType);
            checkBox.left.setSelected(analyzerExists);
          }
          TrainingWindow.this.norbTaggerButton.setSelected(true);
        }
      } else {
        TrainingWindow.this.ruleBasedAttributeAnalyzers.clear();
        for (RaptatPair<JCheckBox, Boolean> checkBox : TrainingWindow.this.rbAttributeCheckBoxes) {
          checkBox.left.setSelected(false);
        }
      }
    }


    /** @return */
    private boolean addAttributeAnalyzer(String analyzerName) {
      AttributeAnalyzerType analyzerType;
      if ((analyzerType = AttributeAnalyzerType.getTypeFromDisplayName(analyzerName)) == null) {
        return false;
      }
      String analyzerDescriptorPath;
      JFileChooser fileChooser =
          new JFileChooser(OptionsManager.getInstance().getLastSelectedDir());

      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setFileFilter(
          new FileNameExtensionFilter("Select " + analyzerName + " descriptors file", "txt"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
        return false;
      }
      OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
      analyzerDescriptorPath = fileChooser.getSelectedFile().getAbsolutePath();

      TabReader reader = new TabReader(new File(analyzerDescriptorPath));
      String[] data;

      while ((data = reader.getNextData()) != null) {
        if (this.isValid(data)) {
          List<String[]> analyzerDescriptorArray;
          if ((analyzerDescriptorArray =
              TrainingWindow.this.ruleBasedAttributeAnalyzers.get(analyzerType)) == null) {
            analyzerDescriptorArray = new ArrayList<>();
            TrainingWindow.this.ruleBasedAttributeAnalyzers.put(analyzerType,
                analyzerDescriptorArray);
          }
          analyzerDescriptorArray.add(data);
        } else {
          GeneralHelper.errorWriter("Invalid rule-based attribute description text");
          return false;
        }
      }
      return true;
    }


    /**
     * @param data
     * @return
     */
    private boolean isValid(String[] data) {
      int expectedLength = 5;
      if (data.length != expectedLength) {
        return false;
      }
      String useDefault = data[expectedLength - 1];
      if (!useDefault.equalsIgnoreCase("true") && !useDefault.equalsIgnoreCase("false")) {
        return false;
      }
      return true;
    }
  }

  /**
   * Creates the action listener for Schema Path Settings button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on March 12, 2015
   */
  private class SchemaPathSettingsListener implements ActionListener {

    /**
     * Set annotation training options.
     *
     * @param e Action Event for the listener
     */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void actionPerformed(ActionEvent e) {
      if (TrainingWindow.this.schemaFilePath.getText().equals("")) {
        System.err.println("No file selected");
      }

      new SchemaSettingsOptionWindow(new Point(100, 100), new Dimension(600, 500),
          TrainingWindow.this.schemaFilePath.getText(), TrainingWindow.this.strTable);
    }
  }

  /**
   * Creates the action listener for Schema Path Settings button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on March 12, 2015
   */
  private class SchemaProcessListener implements ActionListener {

    /**
     * Set annotation training options.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {

      printTable();
    }
  }

  /**
   * Creates the action listener for Sequence ID button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 22, 2014
   */
  private class SequenceIDOptionsListener implements ActionListener {

    /**
     * Set annotation training options.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      OptionsManager.getInstance().setAnnotationTrainOptions();
    }
  }

  private class TrainingMethodListener implements ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent )
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (TrainingWindow.this.probabilisticRadioButton.isSelected()) {
        TrainingWindow.this.probabilisticMethodOptions.setEnabled(true);
        TrainingWindow.this.featurePipes.setEnabled(false);

      } else {
        TrainingWindow.this.probabilisticMethodOptions.setEnabled(false);
        TrainingWindow.this.featurePipes.setEnabled(true);
      }
    }
  }

  /**
   * Creates the action listener for Train RapTAT button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 22, 2014
   */
  private class TrainRaptatListener implements ActionListener {
    /**
     * Train RapTAT with the supplied Reference XML and text corpus and creates a solution file .
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      initializeTextAnalyzer();
      List<SchemaConcept> schemaConceptList = AnnotationImporter
          .getSchemaConcepts(TrainingWindow.this, TrainingWindow.this.schemaFilePath.getText());
      String matcherPath = TrainingWindow.this.sectionAndSentenceMatchersFilePath.getText();
      TrainingWindow.this.textAnalyzer.loadSectionAndSentenceMatchers(matcherPath);
      String dictionaryPath = TrainingWindow.this.dictionaryFilePath.getText();
      TrainingWindow.this.textAnalyzer.createDictionary(dictionaryPath, null);
      ContextSifter contextSifter = getContextSifter();

      OptionsManager theOptions = OptionsManager.getInstance();
      JFileChooser saveLocChooser = new JFileChooser(theOptions.getLastSelectedDir());
      saveLocChooser.setDialogTitle("Enter name for the solution file you are generating");
      int returnVal = saveLocChooser.showSaveDialog(null);

      if (returnVal == JFileChooser.APPROVE_OPTION) {

        File solutionFile = saveLocChooser.getSelectedFile().getAbsoluteFile();
        solutionFile = GeneralHelper.correctFileName(solutionFile);
        TokenSequenceFinderSolution solution = null;

        if (solutionFile != null) {
          theOptions.setLastSelectedDir(saveLocChooser.getCurrentDirectory());

          try {
            if (solutionFile.createNewFile()) {
              setVisible(false);
              TrainingWindow.this.activeDialog.setVisible(true);
              TrainingWindow.this.activeDialog
                  .setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
              TrainingWindow.this.activeDialog
                  .update(TrainingWindow.this.activeDialog.getGraphics());

              solution = generateSolution(solutionFile, schemaConceptList,
                  TrainingWindow.this.useDiskDuringTrainingButton.isSelected());
              TrainingWindow.this.activeDialog.setCursor(null);
              TrainingWindow.this.activeDialog.setVisible(false);

              TrainingWindow.this.activeDialog.dispose();
              dispose();
              TrainingWindow.trainingWindowInstance = null;
            } else {
              String message1 =
                  "A file with this name already exists in this folder. Please enter another name.";

              JOptionPane.showMessageDialog(new JFrame(), message1, "Error",
                  JOptionPane.ERROR_MESSAGE);
            }
          } catch (Exception e2) {
            TrainingWindow.LOGGER.info("Unable to complete training: " + e2.getMessage());
            GeneralHelper.errorWriter("Error - unable to complete training");
            e2.printStackTrace();
          }
        } else {
          GeneralHelper.errorWriter("Solution file should have the form XXX.soln");
        }

        /* Writing the solution file */
        if (solution != null) {
          Map<SentenceSectionTaggerType, List<RaptatPair<String, String>>> sentenceAndSectionMatchers =
              TrainingWindow.this.textAnalyzer.getSentenceAndSectionMatchers();
          RaptatAnnotationSolution.packageAndSaveSolution(TrainingWindow.this, solution,
              solutionFile, TrainingWindow.this.textAnalyzer, sentenceAndSectionMatchers,
              TrainingWindow.this.csTaggers, TrainingWindow.this.ruleBasedAttributeAnalyzers,
              TrainingWindow.this.attributeConflictManagers, contextSifter);
        } else {
          GeneralHelper.errorWriter("Unable to create solution");
        }
      }
      TrainingWindow.this.activeDialog.dispose();
      dispose();
      TrainingWindow.trainingWindowInstance = null;
    }


    private ContextSifter getContextSifter() {
      GeneralHelper.errorWriter("ContextSifter input not yet implemented");
      System.exit(-1);
      return null;
    }
  }

  /** */
  private static final long serialVersionUID = 3514412417826883848L;

  public static TrainingWindow trainingWindowInstance;

  private static final Logger LOGGER = Logger.getLogger(TrainingWindow.class);


  public Set<String> attributeConflictManagers;


  public List<RaptatPair<JCheckBox, Boolean>> conflictManagerCheckBoxes;

  private TextAnalyzer textAnalyzer;

  private final JRadioButton crfRadioButton = new JRadioButton("CRF");

  private JRadioButton csTaggerButton;
  private JRadioButton nocsTaggerButton;

  private final JRadioButton probabilisticRadioButton = new JRadioButton("Probabilistic");

  private final JButton stringHandlingOptions =
      new JButton("<html><center>Token String<br>Handling</center></html>");
  private final JButton acceptedConceptsButton =
      new JButton("<html><center>Acceptable<br>Concepts</center></html>");

  private final JButton probabilisticMethodOptions =
      new JButton("<html><center>Probabilistic<br>Options</center></html>");
  private final JButton featurePipes =
      new JButton("<html><center>CRF Feature<br>Pipes</center></html>");

  /*
   * These are taggers that assign attributes to concepts generally using a cue-scope model trained
   * using Conditional Random Field models.
   */
  private final Collection<AttributeTaggerSolution> csTaggers = new ArrayList<>(4);

  private final List<List<String>> strTable = new ArrayList<>();

  /*
   * This button determine whether unlabeledHashTree instances, used fo probabilistic sequence
   * identification, are keep on the disk during training. This may be necessary if memory is
   * limited or the training structure gets to be very large.
   */
  private final JRadioButton useDiskDuringTrainingButton =
      new JRadioButton("Use Disk for Training");

  private ActiveProcessDialog activeDialog;

  private JTextField dictionaryFilePath;

  private JTextField sectionAndSentenceMatchersFilePath;

  private JRadioButton rbTaggerButton;

  private JRadioButton norbTaggerButton;

  private JRadioButton cmButton;

  private JRadioButton nocmButton;

  private List<RaptatPair<JCheckBox, Boolean>> rbAttributeCheckBoxes;

  /*
   * This is a collection of ContextAttributeAnalyzer types that will be used during annotation.
   * This list stores the name of the ContextAttributeAnalyzer type on the left side and a
   * descriptor, used for analyzer type construction, on the right. It can be used to assign
   * attributes based on rules. If multiple values can be assigned by multiple
   * ContextAttributeAnalyzers, they should be resolved with AttributeConflictManager instances
   */
  private HashMap<AttributeAnalyzerType, List<String[]>> ruleBasedAttributeAnalyzers;

  /** */
  private TrainingWindow() {
    TrainingWindow.LOGGER.setLevel(Level.INFO);
  }

  /**
   * Constructor of TrainingWindow Class.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @author Glenn Gobbel March 23, 2016
   */
  private TrainingWindow(Point thePoint, Dimension theDimension, String action) {
    this.conceptXMLFilesPathList = new ArrayList<>();
    this.textFilesPathList = new ArrayList<>();

    this.conceptXMLListModel = new DefaultListModel<String>();
    this.textFileListModel = new DefaultListModel<String>();

    initializeWindow(thePoint, theDimension, action);
  }


  /** */
  private void createActiveProcessDialog() {
    String dialogTitle = "RapTAT Training";
    String dialogText = "Please wait during the training of RapTAT";
    this.activeDialog = new ActiveProcessDialog(TrainingWindow.this,
        RaptatConstants.GEARS_IMAGE_PATH, dialogTitle, dialogText);
  }


  /**
   * @param solutionFile
   * @param schemaConceptList
   * @param keepUnlabeledHashTreeOnDisk
   * @return
   */
  private TokenSequenceFinderSolution generateSolution(File solutionFile,
      List<SchemaConcept> schemaConceptList, boolean keepUnlabeledHashTreeOnDisk) {
    OptionsManager theOptions = OptionsManager.getInstance();

    TokenSequenceSolutionTrainer tsFinderTrainer = null;
    if (!TrainingWindow.this.crfRadioButton.isSelected()) {
      TokenProcessingOptions tokenProcessingOptions = new TokenProcessingOptions(theOptions);
      PhraseIDTrainOptions phraseIDOptions = new PhraseIDTrainOptions(theOptions);
      boolean saveUnlabeledHashTrees = true;
      tsFinderTrainer = new ProbabilisticSolutionTrainer(tokenProcessingOptions, phraseIDOptions,
          this.acceptedConceptsSet, solutionFile, keepUnlabeledHashTreeOnDisk,
          saveUnlabeledHashTrees, this.textAnalyzer);
    } else {
      tsFinderTrainer = new CRFSolutionTrainer(this.textAnalyzer, this.acceptedConceptsSet);
    }

    List<RaptatPair<String, String>> textAndXmlPaths = prepareTrainingSet();
    AnnotationImporter importer = AnnotationImporter.getImporter();
    tsFinderTrainer.trainUsingTrainingSet(textAndXmlPaths, schemaConceptList, new HashSet<>(),
        importer);
    TokenSequenceFinderSolution solution =
        tsFinderTrainer.updateAndReturnSolution(schemaConceptList);

    return solution;
  }


  private JPanel getAttributeTaggerPanel() {
    JPanel attributePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    attributePanel.setBorder(BorderFactory.createTitledBorder("Attribute Tagger Packaging"));

    this.csTaggerButton = new JRadioButton();
    this.nocsTaggerButton = new JRadioButton();
    ActionListener actionListener = new CSAttributeListener();
    String panelString = "<html><i>Cue-Scope<br>Tagger?";
    Component csAttributePanel =
        getYesNoButtonBox(panelString, this.csTaggerButton, this.nocsTaggerButton, actionListener);
    attributePanel.add(csAttributePanel);
    // attributePanel.add( Box.createHorizontalStrut( 120 ) );

    this.rbTaggerButton = new JRadioButton();
    this.norbTaggerButton = new JRadioButton();
    actionListener = new RuleBasedAttributeListener();
    panelString = "<html><i>Rule-Based<br>Tagger?</i></html>";
    Component contextAttributesPanel =
        getYesNoButtonBox(panelString, this.rbTaggerButton, this.norbTaggerButton, actionListener);
    attributePanel.add(contextAttributesPanel);
    // attributePanel.add( Box.createHorizontalStrut( 120 ) );

    this.cmButton = new JRadioButton();
    this.nocmButton = new JRadioButton();
    actionListener = new AttributeConflictManagerListener();
    panelString = "<html><i>Attribute Conflict<br>Manager?</i></html>";
    Component conflictManagerPanel =
        getYesNoButtonBox(panelString, this.cmButton, this.nocmButton, actionListener);
    attributePanel.add(conflictManagerPanel);
    // attributePanel.add( Box.createHorizontalStrut( 25 ) );

    return attributePanel;
  }


  /** @return */
  private Component getFileLoadersPanel() {
    Box loadFilesPanel = Box.createVerticalBox();
    int textPathSize = 28;

    String buttonText = "<html><center>Load Schema</center></html>";
    String tipText = "<html>Select eHOST or Knowtator file "
        + "containing the annotation schema<br>(<i>required for attribute assignment training</i>)</html>";
    String pathText = "Schema";
    ActionListener actionListener = new LoadSchemaFileListener();
    this.schemaFilePath = new JTextField(textPathSize);
    Component schemaPanel =
        getLoadFilePanel(this.schemaFilePath, buttonText, tipText, pathText, actionListener);

    buttonText = "<html><center>Load Sectionizers</center></html>";
    tipText =
        "<html>Select file describing regexes to identify named sections and sentences</html>";
    pathText = "Sectionizer descriptors ";
    actionListener = new LoadSectionizersListener();
    this.sectionAndSentenceMatchersFilePath = new JTextField(textPathSize);
    Component matchersPanel = getLoadFilePanel(this.sectionAndSentenceMatchersFilePath, buttonText,
        tipText, pathText, actionListener);

    buttonText = "<html><center>Load Dictionary</center></html>";
    tipText = "<html>Select file describing concept, facilitator, and blocker phrases</html>";
    pathText = "Dictionary ";
    actionListener = new LoadDictionaryListener();
    this.dictionaryFilePath = new JTextField(textPathSize);
    Component dictionaryPanel =
        getLoadFilePanel(this.dictionaryFilePath, buttonText, tipText, pathText, actionListener);

    loadFilesPanel.add(dictionaryPanel);
    loadFilesPanel.add(schemaPanel);
    loadFilesPanel.add(matchersPanel);
    loadFilesPanel.add(Box.createVerticalStrut(10));

    Box loadFilesContainerPanel = Box.createHorizontalBox();
    loadFilesContainerPanel.add(Box.createHorizontalStrut(20));
    loadFilesContainerPanel.add(loadFilesPanel);

    return loadFilesContainerPanel;
  }


  private Component getLoadFilePanel(JTextField textField, String buttonText, String tipText,
      String pathText, ActionListener buttonListener) {
    JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));

    JButton loadFileButton = new JButton(buttonText);
    loadFileButton.setPreferredSize(new Dimension(160, 30));
    loadFileButton.addActionListener(buttonListener);
    loadFileButton.setToolTipText(tipText);
    filePanel.add(loadFileButton);

    textField.setEditable(false);
    textField.setText(pathText + " file path . . . ");
    textField.setHorizontalAlignment(SwingConstants.LEFT);
    filePanel.add(textField);

    JPanel containerPanel = new JPanel();
    containerPanel.add(filePanel, BorderLayout.WEST);

    return containerPanel;
  }


  /**
   * Creates a new solution file to update the existing file.
   *
   * @param solutionFileToUpdate The existing solution file
   * @return Temporary solution file
   * @author Sanjib Saha, July 17, 2014
   */
  private File getNewSolutionFile(File solutionFileToUpdate) {
    File updatedFileDirectory;
    String updatedFileName;

    if (solutionFileToUpdate == null) {
      String message = "The is not a valid Raptat solution file.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);

      return null;
    } else {
      updatedFileDirectory = solutionFileToUpdate.getParentFile();
      updatedFileName = solutionFileToUpdate.getName();

      int updateNumber = 1; // Default update number
      int indexOfUpdateString = updatedFileName.indexOf("_UPDATE_");

      // File name already contains "_UPDATE_"
      if (indexOfUpdateString > -1) {
        int indexOfDot = updatedFileName.indexOf(".");

        // Substring containing update number starts after "_UPDATE_"
        // (length 8)
        updateNumber =
            Integer.parseInt(updatedFileName.substring(indexOfUpdateString + 8, indexOfDot)) + 1;

        // updatedFileName retains only the part before the update
        // string and sequence no
        updatedFileName = updatedFileName.substring(0, indexOfUpdateString);
      }

      updatedFileName = updatedFileName.replaceAll("\\.soln", "");
      NumberFormat formatter = new DecimalFormat("0000");

      updatedFileName += "_UPDATE_" + formatter.format(updateNumber) + ".soln";
    }

    return new File(updatedFileDirectory, updatedFileName);
  }


  /**
   * Creates the components for the option panel of the training window.
   *
   * @return A JPanel with the components for the option panel.
   * @author Sanjib Saha, July 22, 2014
   */
  private Component getOptionsPanel() {
    JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Concept Mapping"));

    optionsPanel.add(this.acceptedConceptsButton);
    this.acceptedConceptsButton.addActionListener(new AcceptableConceptsButtonListener());

    optionsPanel.add(this.stringHandlingOptions);
    this.stringHandlingOptions.addActionListener(new ConceptMapOptionsListener());

    optionsPanel.add(Box.createVerticalStrut(60));

    return optionsPanel;
  }


  /**
   * Creates the components for adding Schema on the training window.
   *
   * @return A JPanel with the components for the Schema addition panel.
   * @author Sanjib Saha, March 12, 2015
   */
  private JPanel getSchemaPanel() {
    JPanel schemaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 1));

    JButton loadSchemaButton = new JButton("<html><center>Load Schema</center></html>");
    loadSchemaButton.setPreferredSize(new Dimension(130, 30));
    loadSchemaButton.addActionListener(new LoadSchemaFileListener());
    loadSchemaButton.setToolTipText("<html>Select eHOST or Knowtator file "
        + "containing the annotation schema<br>(<i>required for attribute assignment training</i>)</html>");
    schemaPanel.add(loadSchemaButton);

    this.schemaFilePath = new JTextField(30);
    this.schemaFilePath.setEditable(false);
    this.schemaFilePath.setText("Schema file path . . . ");
    this.schemaFilePath.setHorizontalAlignment(SwingConstants.LEFT);
    schemaPanel.add(this.schemaFilePath);

    JButton settingsButton = new JButton("<html><center>Settings</center></html>");
    settingsButton.addActionListener(new SchemaPathSettingsListener());
    schemaPanel.add(settingsButton);

    JButton processButton = new JButton("<html><center>Process</center></html>");
    processButton.addActionListener(new SchemaProcessListener());
    schemaPanel.add(processButton);

    /* Turn the buttons off for now */
    settingsButton.setVisible(false);
    processButton.setVisible(false);
    JPanel schemaContainerPanel = new JPanel();
    schemaContainerPanel.add(schemaPanel, BorderLayout.CENTER);

    return schemaContainerPanel;
  }


  private JPanel getTrainingMethodPanel() {
    JPanel trainingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    trainingPanel.setBorder(BorderFactory.createTitledBorder("Training Method?"));

    Box buttonBox = Box.createVerticalBox();
    trainingPanel.add(buttonBox);

    ButtonGroup trainingSelection = new ButtonGroup();
    trainingSelection.add(this.crfRadioButton);
    trainingSelection.add(this.probabilisticRadioButton);
    this.probabilisticRadioButton.setSelected(true);

    buttonBox.add(this.crfRadioButton);
    buttonBox.add(this.probabilisticRadioButton);
    trainingPanel.add(Box.createHorizontalStrut(15));

    trainingPanel.add(this.probabilisticMethodOptions);
    this.probabilisticMethodOptions.addActionListener(new SequenceIDOptionsListener());

    trainingPanel.add(this.featurePipes);
    this.featurePipes.addActionListener(new FeaturePipesButtonListener());
    this.featurePipes.setEnabled(false);

    trainingPanel.add(Box.createVerticalStrut(60));

    TrainingMethodListener listener = new TrainingMethodListener();
    this.crfRadioButton.addActionListener(listener);
    this.probabilisticRadioButton.addActionListener(listener);

    return trainingPanel;
  }


  /**
   * Creates the components for the training panel.
   *
   * @return A JPanel with the components for the training panel.
   * @author Sanjib Saha, July 22, 2014
   */
  private Component getTrainingPanel() {
    Box runPanel = Box.createVerticalBox();

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton trainButton = new JButton("Train RapTAT");
    trainButton.setPreferredSize(new Dimension(150, 60));
    trainButton.addActionListener(new TrainRaptatListener());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new CancelButtonListener());
    cancelButton.setPreferredSize(trainButton.getPreferredSize());

    buttonPanel.add(trainButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    buttonPanel.add(cancelButton);

    runPanel.add(Box.createVerticalStrut(20));
    runPanel.add(buttonPanel);
    JPanel runContainerPanel = new JPanel();
    runContainerPanel.add(runPanel, BorderLayout.CENTER);

    return runContainerPanel;
  }


  /**
   * Creates the components for the update panel.
   *
   * @return A JPanel with the components for the update panel.
   * @author Sanjib Saha, July 16, 2014
   */
  private Component getUpdatePanel() {
    return null;
  }


  private Component getYesNoButtonBox(String label, JRadioButton yesButton, JRadioButton noButton,
      ActionListener actionListener) {
    Box verticalBox = Box.createVerticalBox();
    verticalBox.setBorder(BorderFactory.createTitledBorder(label));

    ButtonGroup buttonGroup = new ButtonGroup();

    String yesString = "Yes";
    yesButton.addActionListener(actionListener);
    yesButton.setActionCommand(yesString);
    yesButton.setText(yesString);
    yesButton.setSelected(false);
    buttonGroup.add(yesButton);

    String noString = "No";
    noButton.addActionListener(actionListener);
    noButton.setActionCommand(noString);
    noButton.setText(noString);
    noButton.setSelected(true);
    buttonGroup.add(noButton);

    Box horizontalButtonBox = Box.createHorizontalBox();
    horizontalButtonBox.add(yesButton);
    horizontalButtonBox.add(noButton);
    horizontalButtonBox.add(Box.createHorizontalStrut(2 * label.length()));
    verticalBox.add(horizontalButtonBox);

    return verticalBox;
  }


  /** */
  private void initializeAttributeAnalyzers() {
    this.ruleBasedAttributeAnalyzers = new HashMap<>();
    this.rbAttributeCheckBoxes = new ArrayList<>();
    for (AttributeAnalyzerType analyzerType : AttributeAnalyzerType.values()) {
      this.rbAttributeCheckBoxes
          .add(new RaptatPair<>(new JCheckBox(analyzerType.displayName), true));
    }

    /*
     * Block below is temporary and placed her to allow for moving forward on AKI project until full
     * TrainingWindow gui can be completed
     */
    // {
    // List<String[]> initializationArrays = new ArrayList<String[]>();
    // String[] allergyContext = new String[]
    // {
    // "assertionstatus", "allergy", "positive", "negative", "false"
    // };
    // initializationArrays.add( allergyContext );
    // String[] medSectionContext = new String[]
    // {
    // "withinmedlist", Constants.MED_SECTION_TAG, "no", "yes", "true"
    // };
    // initializationArrays.add( medSectionContext );
    // this.rbAttributeAnalyzers.put( AttributeAnalyzerType.GENERAL_CONTEXT,
    // initializationArrays );
    // }
  }


  /** */
  private void initializeAttributeConflictManagers() {
    this.attributeConflictManagers = new HashSet<>();
    this.conflictManagerCheckBoxes = new ArrayList<>();
    for (AttributeConflictManagerType managerType : AttributeConflictManagerType.values()) {
      this.conflictManagerCheckBoxes
          .add(new RaptatPair<>(new JCheckBox(managerType.displayName), true));
    }

    /*
     * Block below is temporary and placed here to allow for moving forward on AKI project until
     * full TrainingWindow gui can be completed
     */
    {
      this.attributeConflictManagers.add("assertion");
    }
  }


  /** */
  private void initializeTextAnalyzer() {
    OptionsManager options = OptionsManager.getInstance();
    this.textAnalyzer = new TextAnalyzer();

    boolean useStems = options.getUseStems();
    boolean usePOS = options.getUsePOS();
    boolean removeStopWords = options.getRemoveStopWords();
    int maxTokenNumber = options.getTrainingTokenNumber();
    ContextHandling reasonNoMedsProcessing = options.getReasonNoMedsProcessing();

    this.textAnalyzer.setTokenProcessingParameters(useStems, usePOS, removeStopWords,
        maxTokenNumber, reasonNoMedsProcessing);
  }


  /**
   * Initializes the update window components and assigns corresponding action listeners to the
   * components.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @param action Action selected from the menu (Train/Update solution)
   * @author Sanjib Saha, July 22, 2014
   */
  private void initializeWindow(Point thePosition, Dimension theDimension, String action) {
    int windowWidth = (int) (theDimension.width * 1.0);
    int windowHeight = (int) (theDimension.height * 0.8);

    windowWidth = windowWidth < RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        ? RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        ? RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        : windowHeight;

    this.setSize(windowWidth, windowHeight);
    setResizable(false);
    thePosition.translate(10, 10);

    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    createActiveProcessDialog();

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        TrainingWindow.trainingWindowInstance.dispose();
        TrainingWindow.trainingWindowInstance = null;
      }
    });

    initializeAttributeAnalyzers();
    initializeAttributeConflictManagers();
    // Creates the Box
    Box listPanel = Box.createHorizontalBox();

    JPanel listButtonPanel = new JPanel(new GridLayout(1, 2));
    Box generalButtonPanel = Box.createVerticalBox();
    JPanel loadFilesAndTrainingPanel = new JPanel(new GridLayout(1, 2));

    listPanel.setPreferredSize(new Dimension(windowWidth, (int) (windowHeight * 3.0 / 8)));

    // Adding the box for file lists
    setLayout(new BorderLayout());
    this.add(listPanel, BorderLayout.NORTH);

    Dimension panelSize = new Dimension((int) (0.40 * listPanel.getPreferredSize().width),
        listPanel.getPreferredSize().height);

    listPanel.add(buildConceptXMLListPanel(panelSize));
    listPanel.add(buildTextListPanel(panelSize));

    // Adding the add and remove file buttons
    this.add(generalButtonPanel, BorderLayout.SOUTH);
    generalButtonPanel.add(listButtonPanel);

    panelSize = new Dimension((int) (0.30 * listPanel.getPreferredSize().width),
        (int) (0.10 * listPanel.getPreferredSize().height));

    JPanel refXMLButtonsPanel = new JPanel(new FlowLayout());
    refXMLButtonsPanel.add(getAddRemoveButtonPanel(new AddReferenceConceptXMLFileListener(),
        new RemoveReferenceConceptXMLFileListener(), "Reference XML", panelSize));
    listButtonPanel.add(refXMLButtonsPanel);

    JPanel textButtonsPanel = new JPanel(new FlowLayout());
    textButtonsPanel.add(getAddRemoveButtonPanel(new AddTextFileListener(),
        new RemoveTextFileListener(), "Text", panelSize));
    listButtonPanel.add(textButtonsPanel);

    // Adding score button
    if (action.equals("update")) {
      setTitle("Update Existing Solution");

      loadFilesAndTrainingPanel.add(getUpdatePanel());
    } else {
      setTitle("RapTAT Training Window");

      loadFilesAndTrainingPanel.add(getFileLoadersPanel());
      loadFilesAndTrainingPanel.add(getTrainingPanel());
    }

    loadFilesAndTrainingPanel.setBorder(BorderFactory.createEmptyBorder());
    JPanel loadFilesAndTrainingPanelContainer = new JPanel();
    loadFilesAndTrainingPanelContainer.add(loadFilesAndTrainingPanel, BorderLayout.CENTER);
    loadFilesAndTrainingPanelContainer.add(Box.createRigidArea(new Dimension(0, 60)),
        BorderLayout.NORTH);

    JPanel trainingAndOptionsPanel = new JPanel(new FlowLayout());

    Font borderFont = new Font(trainingAndOptionsPanel.getFont().getFontName(), Font.ITALIC,
        trainingAndOptionsPanel.getFont().getSize());
    Border trainingAndOptionsBorder = BorderFactory.createTitledBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)),
        "Options", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont);
    trainingAndOptionsPanel.setBorder(trainingAndOptionsBorder);

    trainingAndOptionsPanel.add(getTrainingMethodPanel());
    trainingAndOptionsPanel.add(getOptionsPanel());
    trainingAndOptionsPanel.add(getAttributeTaggerPanel());
    JPanel trainingAndOptionsPanelContainer = new JPanel();
    trainingAndOptionsPanelContainer.add(trainingAndOptionsPanel, BorderLayout.CENTER);
    trainingAndOptionsPanelContainer.add(Box.createRigidArea(new Dimension(20, 0)),
        BorderLayout.WEST);

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(trainingAndOptionsPanelContainer, BorderLayout.NORTH);
    buttonPanel.add(loadFilesAndTrainingPanelContainer, BorderLayout.SOUTH);

    generalButtonPanel.add(buttonPanel);

    this.useDiskDuringTrainingButton.setSelected(false);
    setVisible(true);
    pack();
  }


  private List<RaptatPair<String, String>> prepareTrainingSet() {
    List<RaptatPair<String, String>> resultList = new ArrayList<>(this.textFilesPathList.size());

    /*
     * Build a hashtable to quickly look up file paths from text file names
     */
    Hashtable<String, String> textPathSet = new Hashtable<>(this.textFilesPathList.size());
    for (String curTextPath : this.textFilesPathList) {
      textPathSet.put(new File(curTextPath).getName(), curTextPath);
    }

    for (String curXMLPath : this.conceptXMLFilesPathList) {
      String curXMLSource = JdomXMLImporter.getTextSource(curXMLPath);

      /*
       * Only process XML documents where text source document is contained with the set of
       * documents being processed (i.e. in textPathSet)
       */
      if (textPathSet.containsKey(curXMLSource)) {
        String curTextPath = textPathSet.get(curXMLSource);
        RaptatPair<String, String> textXmlPair = new RaptatPair<>(curTextPath, curXMLPath);
        resultList.add(textXmlPair);
      }
    }
    return resultList;
  }


  private void printTable() {
    for (List<String> list : this.strTable) {
      for (String str : list) {
        System.out.print(str + "\t");
      }
      System.out.println("");
    }
  }


  /**
   * Returns an instance of the UpdateSolutionWindow class.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @param action Action selected from the menu (Train/Update solution)
   * @return An instance of the UpdateSolutionWindow class
   * @author Sanjib Saha, July 22, 2014
   */
  public static synchronized TrainingWindow getInstance(Point thePoint, Dimension theDimension,
      String action) {
    if (TrainingWindow.trainingWindowInstance == null) {
      TrainingWindow.trainingWindowInstance = new TrainingWindow(thePoint, theDimension, action);
    }

    return TrainingWindow.trainingWindowInstance;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        TrainingWindowRunType runType = TrainingWindowRunType.INSTANTIATE_WINDOW;

        switch (runType) {
          case GENERATE_CIRRHOSIS_RADIOLOGY_SOLUTION:
            generateCirrhosisRadiologySolution();
            break;
          case INSTANTIATE_WINDOW:
            instantiateWindow();
            break;
          default:
            break;
        }
      }


      private void generateAKISolution() {
        /*
         * Create text analyzer and instantiate it so that options are properly set
         */
        TrainingWindow tw = new TrainingWindow();
        tw.initializeTextAnalyzer();

        /*
         * Get where to write solution file
         */
        String solutionFileName = "testRaptatSolution_" + GeneralHelper.getTimeStamp() + ".soln";
        String solutionFilePath =
            "D:\\CARTCL-IIR\\Glenn\\RaptatTesting_160328" + File.separator + solutionFileName;
        File solutionFile = new File(solutionFilePath);

        /*
         * Determine options for running
         */
        OptionsManager theOptions = OptionsManager.getInstance();
        TokenProcessingOptions tokenProcessingOptions = new TokenProcessingOptions(theOptions);
        PhraseIDTrainOptions phraseIDOptions = new PhraseIDTrainOptions(theOptions);

        /*
         * Load schema
         */
        File schemaFile = new File(
            "D:\\CARTCL-IIR\\Glenn\\AKI_FitzHenryAdjudication\\AKI_Block_02_Fern\\config\\projectschema.xml");
        List<SchemaConcept> schemaConceptList = SchemaImporter.importSchemaConcepts(schemaFile);
        String matcherPath = tw.sectionAndSentenceMatchersFilePath.getText();
        tw.textAnalyzer.loadSectionAndSentenceMatchers(matcherPath);
        String dictionaryPath = tw.dictionaryFilePath.getText();
        tw.textAnalyzer.createDictionary(dictionaryPath, null);

        /*
         * Get the files used for training
         */
        FileFilter filter = GeneralHelper.getFileFilter(".txt", ".xml");
        List<File> fileList =
            Arrays.asList(new File("D:\\CARTCL-IIR\\Glenn\\RaptatTestingGroups\\Training\\corpus")
                .listFiles(filter));
        tw.textFilesPathList = GeneralHelper.fileToPathList(fileList);
        fileList = Arrays
            .asList(new File("D:\\CARTCL-IIR\\Glenn\\RaptatTestingGroups\\Training\\adjudication")
                .listFiles(filter));
        tw.conceptXMLFilesPathList = GeneralHelper.fileToPathList(fileList);
        List<RaptatPair<String, String>> textAndXmlPaths = tw.prepareTrainingSet();

        /*
         * Instantiate trainer and train
         */
        boolean saveUnlabeledHashTrees = true;
        TokenSequenceSolutionTrainer tsFinderTrainer =
            new ProbabilisticSolutionTrainer(tokenProcessingOptions, phraseIDOptions, null,
                solutionFile, false, saveUnlabeledHashTrees, tw.textAnalyzer);
        AnnotationImporter importer = AnnotationImporter.getImporter();
        tsFinderTrainer.trainUsingTrainingSet(textAndXmlPaths, schemaConceptList, new HashSet<>(),
            importer);
        TokenSequenceFinderSolution solution =
            tsFinderTrainer.updateAndReturnSolution(schemaConceptList);

        /*
         * Write the solution file to disk
         */
        if (solution != null) {
          RaptatAnnotationSolution.packageAndSaveSolution(tw, solution, solutionFile,
              tw.textAnalyzer);

        } else {
          GeneralHelper.errorWriter("Unable to create solution");
        }
      }


      private void generateCirrhosisRadiologySolution() {
        /*
         * Create text analyzer and instantiate it so that options are properly set. Note that this
         * does not actually instantiate a Java window. It does instantiate a Raptat-based,
         * TrainingWindow object.
         */
        TrainingWindow tw = new TrainingWindow();
        tw.initializeTextAnalyzer();

        /*
         * Get where to write solution file
         */
        String solutionFileName =
            "RapataCirrhosisRadiologySolution_" + GeneralHelper.getTimeStamp() + ".soln";
        String solutionFilePath =
            "D:\\Cirrhosis\\Glenn\\RadiologyAdjudicatedBlocks" + File.separator + solutionFileName;
        File solutionFile = new File(solutionFilePath);

        /*
         * Determine options for running
         */
        OptionsManager theOptions = OptionsManager.getInstance();
        TokenProcessingOptions tokenProcessingOptions = new TokenProcessingOptions(theOptions);
        PhraseIDTrainOptions phraseIDOptions = new PhraseIDTrainOptions(theOptions);

        /*
         * Load schema
         */
        File schemaFile = new File(
            "D:\\Cirrhosis\\Glenn\\RadiologyAdjudicatedBlocks\\TrainingBlocks\\config\\projectschema.xml");
        List<SchemaConcept> schemaConceptList = SchemaImporter.importSchemaConcepts(schemaFile);

        /*
         * Get the files used for training
         */
        FileFilter filter = GeneralHelper.getFileFilter(".txt", ".xml");
        List<File> fileList = Arrays.asList(
            new File("D:\\Cirrhosis\\Glenn\\RadiologyAdjudicatedBlocks\\TrainingBlocks\\corpus")
                .listFiles(filter));
        tw.textFilesPathList = GeneralHelper.fileToPathList(fileList);
        fileList = Arrays.asList(
            new File("D:\\Cirrhosis\\Glenn\\RadiologyAdjudicatedBlocks\\TrainingBlocks\\saved")
                .listFiles(filter));
        tw.conceptXMLFilesPathList = GeneralHelper.fileToPathList(fileList);
        List<RaptatPair<String, String>> textAndXmlPaths = tw.prepareTrainingSet();

        /*
         * Instantiate trainer and train
         */
        boolean saveUnlabeledHashTrees = true;
        TokenSequenceSolutionTrainer tsFinderTrainer =
            new ProbabilisticSolutionTrainer(tokenProcessingOptions, phraseIDOptions, null,
                solutionFile, false, saveUnlabeledHashTrees, tw.textAnalyzer);

        AnnotationImporter importer = AnnotationImporter.getImporter();
        tsFinderTrainer.trainUsingTrainingSet(textAndXmlPaths, schemaConceptList, new HashSet<>(),
            importer);
        TokenSequenceFinderSolution solution =
            tsFinderTrainer.updateAndReturnSolution(schemaConceptList);

        /*
         * Write the solution file to disk
         */
        if (solution != null) {
          RaptatAnnotationSolution.packageAndSaveSolution(tw, solution, solutionFile,
              tw.textAnalyzer);

        } else {
          GeneralHelper.errorWriter("Unable to create solution");
        }
      }


      private void instantiateWindow() {
        TrainingWindow tw =
            TrainingWindow.getInstance(new Point(100, 100), new Dimension(600, 500), "training");
      }
    });
  }
}
