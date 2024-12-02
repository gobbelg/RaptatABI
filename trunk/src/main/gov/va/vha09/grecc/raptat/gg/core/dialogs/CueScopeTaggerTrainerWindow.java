/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training.CSAttributeTaggerTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.CueScopeTaggerTrainerOptionsWindow;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.TaggerTrainerOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SimpleTriplet;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/** @author vhatvhsahas1 */
public class CueScopeTaggerTrainerWindow extends BaseClassWindows {

  public static enum AssertionStatus {
    POSITIVE, NEGATIVE, UNCERTAIN
  }

  /** @author Glenn T. Gobbel Apr 13, 2016 */
  public class CueScopeTypeActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      // TODO Auto-generated method stub

    }
  }

  /** @author Glenn T. Gobbel Apr 13, 2016 */
  public class TaggingOptionsListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent arg0) {
      Point upperLeftPosition = CueScopeTaggerTrainerWindow.this.getLocation();
      Dimension size = CueScopeTaggerTrainerWindow.this.getSize();
      CueScopeTaggerTrainerOptionsWindow.getInstance(
          CueScopeTaggerTrainerWindow.this.trainingOptions, CueScopeTaggerTrainerWindow.this,
          upperLeftPosition, size);
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
      dispose();
    }
  }

  /**
   * Creates the action listener for update button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 17, 2014
   */
  private class TrainListener implements ActionListener {

    /**
     * Updates the selected solution file with the supplied Reference XML and text corpus.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (CueScopeTaggerTrainerWindow.this.textFilesPathList
          .size() != CueScopeTaggerTrainerWindow.this.conceptXMLFilesPathList.size()) {
        JOptionPane.showMessageDialog(null, "Sizes of Text and XML list do not match");
        return;
      }
      List<File> textFiles = convertToFileList(CueScopeTaggerTrainerWindow.this.textFilesPathList);
      List<File> conceptFiles =
          convertToFileList(CueScopeTaggerTrainerWindow.this.conceptXMLFilesPathList);
      List<File> cueScopeFiles =
          convertToFileList(CueScopeTaggerTrainerWindow.this.cueScopeXMLFilesPathList);
      SimpleTriplet<List<File>> trainingLists =
          new SimpleTriplet<>(textFiles, conceptFiles, cueScopeFiles);

      JFileChooser saveLocChooser = new JFileChooser(
          new File(CueScopeTaggerTrainerWindow.this.textFilesPathList.get(0)).getParent());
      saveLocChooser.setDialogTitle("Enter name for the solution file you are generating");
      int returnVal = saveLocChooser.showSaveDialog(null);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String solutionFileTag = saveLocChooser.getSelectedFile().getName();
        String trainingFilePath = saveLocChooser.getSelectedFile().getParent();
        boolean writeTrainingToFile = true;
        CSAttributeTaggerTrainer csTrainer;
        String typeOfTagger =
            (String) CueScopeTaggerTrainerWindow.this.taggerType.getSelectedItem();

        if (typeOfTagger.equalsIgnoreCase(CueScopeTaggerTrainerWindow.CUE_SCOPE_TAGGER_TYPES[0])) {
          csTrainer = new CSAttributeTaggerTrainer(ContextType.ASSERTIONSTATUS,
              CueScopeTaggerTrainerWindow.this.trainingOptions.getTokenOptions());

          csTrainer.trainNegation(CueScopeTaggerTrainerWindow.this.trainingOptions, solutionFileTag,
              trainingLists, trainingFilePath, writeTrainingToFile);
        } else if (typeOfTagger
            .equalsIgnoreCase(CueScopeTaggerTrainerWindow.CUE_SCOPE_TAGGER_TYPES[1])) {
          csTrainer = new CSAttributeTaggerTrainer(ContextType.UNCERTAINTY,
              CueScopeTaggerTrainerWindow.this.trainingOptions.getTokenOptions());
          throw new NotImplementedException(
              "TODO - " + this.getClass().getName() + " not yet fully implemented");
        } else if (typeOfTagger
            .equalsIgnoreCase(CueScopeTaggerTrainerWindow.CUE_SCOPE_TAGGER_TYPES[2])) {
          csTrainer = new CSAttributeTaggerTrainer(ContextType.TEMPORALITY,
              CueScopeTaggerTrainerWindow.this.trainingOptions.getTokenOptions());
          throw new NotImplementedException(
              "TODO - " + this.getClass().getName() + " not yet fully implemented");
        } else if (typeOfTagger.equals(CueScopeTaggerTrainerWindow.CUE_SCOPE_TAGGER_TYPES[3])) {
          csTrainer = new CSAttributeTaggerTrainer(ContextType.EXPERIENCER,
              CueScopeTaggerTrainerWindow.this.trainingOptions.getTokenOptions());
          throw new NotImplementedException(
              "TODO - " + this.getClass().getName() + " not yet fully implemented");
        }
      }
    }


    /**
     * @param textFilesPathList
     * @return
     */
    private List<File> convertToFileList(List<String> filePathList) {
      List<File> resultList = new ArrayList<>(filePathList.size());
      for (String curFilePath : filePathList) {
        File fileToAdd = new File(curFilePath);
        if (!fileToAdd.exists()) {
          GeneralHelper.errorWriter("Training file:" + curFilePath + " does not exist");
          return null;
        }
        resultList.add(new File(curFilePath));
      }

      return resultList;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  /** Instance of AssertionAnnotationWindow Class */
  public static CueScopeTaggerTrainerWindow cueScopeTrainerTaggerWindowInstance = null;

  /** The logger */
  private static final Logger logger = Logger.getLogger(CueScopeTaggerTrainerWindow.class);

  private static final String[] CUE_SCOPE_TAGGER_TYPES =
      {"assertion", "uncertainty", "temporality", "experiencer"};


  private JComboBox<String> taggerType;


  private TaggerTrainerOptions trainingOptions;

  /**
   * Constructor of AssertionAnnotationWindow Class.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @author Sanjib Saha, July 17, 2014
   * @param trainingOptions
   */
  private CueScopeTaggerTrainerWindow(TaggerTrainerOptions trainingOptions, Point thePoint,
      Dimension theDimension) {
    this.conceptXMLFilesPathList = new ArrayList<>();
    this.textFilesPathList = new ArrayList<>();
    this.cueScopeXMLFilesPathList = new ArrayList<>();

    this.conceptXMLListModel = new DefaultListModel();
    this.textFileListModel = new DefaultListModel();
    this.cueScopeXMLListModel = new DefaultListModel();

    if (trainingOptions == null) {
      this.trainingOptions = new TaggerTrainerOptions();
    }
    initialize(thePoint, theDimension);
  }

  /**
   * ************************************************************** Builds a panel to determine what
   * cue scope training we'll be doing
   *
   * @return JPanel **************************************************************
   */
  private JPanel getCueScopeTypePanel() {
    JPanel cueScopeTypePanel = new JPanel();
    cueScopeTypePanel.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
            "Cue-Scope Tagger Type", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
    this.taggerType = new JComboBox<>();

    for (String curMethod : CueScopeTaggerTrainerWindow.CUE_SCOPE_TAGGER_TYPES) {
      this.taggerType.addItem(curMethod);
    }

    this.taggerType.addActionListener(new CueScopeTypeActionListener());
    cueScopeTypePanel.add(this.taggerType, BorderLayout.CENTER);
    this.taggerType.setEnabled(false);
    return cueScopeTypePanel;
  }


  /**
   * Creates the components for the update panel.
   *
   * @return A JPanel with the components for the update panel.
   * @author Sanjib Saha, July 16, 2014
   */
  private Box getRunPanel() {
    Box runBox = Box.createVerticalBox();
    runBox.add(Box.createRigidArea(new Dimension(0, 2)));

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton trainButton = new JButton(" Train ");
    trainButton.addActionListener(new TrainListener());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new CancelButtonListener());

    buttonPanel.add(trainButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

    runBox.add(buttonPanel);
    runBox.add(Box.createRigidArea(new Dimension(0, 2)));

    return runBox;
  }


  /**
   * Creates the components for adjusting settings for running.
   *
   * @return A JPanel with the components for the Schema addition panel.
   * @author Sanjib Saha, March 12, 2015
   */
  private Box getSettingsPanel() {
    Box settingsPanelHolder = Box.createHorizontalBox();
    Box settingsPanel = Box.createVerticalBox();
    settingsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    JPanel schemaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton loadSchemaButton = new JButton("<html><center>Schema File</center></html>");
    loadSchemaButton.addActionListener(new LoadSchemaFileListener());
    schemaPanel.add(loadSchemaButton);

    this.schemaFilePath = new JTextField(50);
    this.schemaFilePath.setEditable(false);
    this.schemaFilePath.setHorizontalAlignment(SwingConstants.LEFT);
    schemaPanel.add(this.schemaFilePath);

    Box taggerOptionsPanel = Box.createHorizontalBox();
    taggerOptionsPanel.add(Box.createHorizontalStrut(80));
    taggerOptionsPanel.add(getCueScopeTypePanel());
    taggerOptionsPanel.add(Box.createHorizontalStrut(160));
    taggerOptionsPanel.add(getTaggerTypeOptionsPanel(), BorderLayout.EAST);
    taggerOptionsPanel.add(Box.createHorizontalStrut(80));

    settingsPanel.add(Box.createVerticalStrut(5));
    settingsPanel.add(taggerOptionsPanel);
    settingsPanel.add(Box.createVerticalStrut(10));
    settingsPanel.add(schemaPanel);
    settingsPanel.add(Box.createVerticalStrut(5));

    settingsPanelHolder.add(Box.createHorizontalStrut(15));
    settingsPanelHolder.add(settingsPanel);
    settingsPanelHolder.add(Box.createHorizontalStrut(15));

    return settingsPanelHolder;
  }


  /** @return */
  private Box getTaggerTypeOptionsPanel() {
    Box taggerTypeOptionsPanel = Box.createHorizontalBox();
    taggerTypeOptionsPanel.add(Box.createHorizontalStrut(20));
    JButton taggingOptionsButton = new JButton("Tagging Options");
    taggingOptionsButton.addActionListener(new TaggingOptionsListener());
    taggerTypeOptionsPanel.add(taggingOptionsButton);
    taggerTypeOptionsPanel.add(Box.createHorizontalStrut(20));
    return taggerTypeOptionsPanel;
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
  private void initialize(Point thePosition, Dimension theDimension) {
    int windowWidth = (int) (theDimension.width * 1.0);
    int windowHeight = (int) (theDimension.height * 0.8);

    this.setSize(windowWidth, windowHeight);
    setResizable(false);
    thePosition.translate(10, 10);

    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    // Adding the box for file lists
    setTitle("Assertion Training Window");
    setLayout(new BorderLayout());

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        CueScopeTaggerTrainerWindow.cueScopeTrainerTaggerWindowInstance.dispose();
        CueScopeTaggerTrainerWindow.cueScopeTrainerTaggerWindowInstance = null;
      }
    });

    Box listPanel = Box.createHorizontalBox();
    listPanel.setPreferredSize(new Dimension(windowWidth, (int) (windowHeight * 5.0 / 8)));
    Dimension listPanelSize = new Dimension((int) (0.3 * listPanel.getPreferredSize().width),
        listPanel.getPreferredSize().height);
    listPanel.add(buildTextListPanel(listPanelSize));
    listPanel.add(buildConceptXMLListPanel(listPanelSize));
    listPanel.add(buildCueScopeXMLListPanel(listPanelSize));

    JPanel listButtonPanel = new JPanel(new GridLayout(1, 2));

    Dimension listButtonPanelSize = new Dimension((int) (0.30 * listPanel.getPreferredSize().width),
        (int) (0.10 * listPanel.getPreferredSize().height));

    JPanel textButtonsPanel = new JPanel(new FlowLayout());
    textButtonsPanel.add(getAddRemoveButtonPanel(new AddTextFileListener(),
        new RemoveTextFileListener(), "Text Files", listButtonPanelSize));
    listButtonPanel.add(textButtonsPanel);

    JPanel conceptXMLButtonsPanel = new JPanel(new FlowLayout());
    conceptXMLButtonsPanel.add(getAddRemoveButtonPanel(new AddReferenceConceptXMLFileListener(),
        new RemoveReferenceConceptXMLFileListener(), "General Concept XML", listButtonPanelSize));
    listButtonPanel.add(conceptXMLButtonsPanel);

    JPanel cueScopeXMLButtonsPanel = new JPanel(new FlowLayout());
    cueScopeXMLButtonsPanel.add(getAddRemoveButtonPanel(new AddCueScopeXMLFileListener(),
        new RemoveCueScopeXMLFileListener(), "Cue Scope XML", listButtonPanelSize));
    listButtonPanel.add(cueScopeXMLButtonsPanel);

    Box runPanel = getRunPanel();
    Box settingsPanel = getSettingsPanel();

    Box generalButtonPanel = Box.createVerticalBox();
    generalButtonPanel.add(listButtonPanel);
    generalButtonPanel.add(settingsPanel);
    generalButtonPanel.add(runPanel);

    this.add(listPanel, BorderLayout.CENTER);
    this.add(generalButtonPanel, BorderLayout.SOUTH);
    setVisible(true);
    pack();
  }


  private int selectSaveDirectory(File saveDirectory) {
    OptionsManager theOptions = OptionsManager.getInstance();

    int response = JOptionPane.showConfirmDialog(null,
        "Would you like to overwrite existing annotation files?", "Select Save Directory",
        JOptionPane.YES_NO_OPTION);

    if (response == JOptionPane.NO_OPTION) {
      JFileChooser solutionChooser = new JFileChooser();
      solutionChooser.setMultiSelectionEnabled(false);
      solutionChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      if (OptionsManager.getInstance().getLastSelectedDir() != null) {
        solutionChooser.setCurrentDirectory(theOptions.getLastSelectedDir());
      }

      int returnValue = solutionChooser.showDialog(null, "Select Svae Directory");

      if (returnValue == JFileChooser.APPROVE_OPTION) {
        saveDirectory = solutionChooser.getSelectedFile();
        theOptions.setLastSelectedDir(saveDirectory);
      }
    } else if (response == JOptionPane.YES_OPTION) {
      // Do nothing now
    } else {
      JOptionPane.showMessageDialog(null, "Please select a directory to save results");
    }

    return response;
  }


  /**
   * Returns an instance of the AssertionAnnotationWindow class.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @return An instance of the AssertionAnnotationWindow class
   * @author Sanjib Saha, July 22, 2014
   */
  public static synchronized CueScopeTaggerTrainerWindow getInstance(
      TaggerTrainerOptions trainingOptions, Point thePoint, Dimension theDimension) {
    if (CueScopeTaggerTrainerWindow.cueScopeTrainerTaggerWindowInstance == null) {
      CueScopeTaggerTrainerWindow.cueScopeTrainerTaggerWindowInstance =
          new CueScopeTaggerTrainerWindow(trainingOptions, thePoint, theDimension);
    }

    CueScopeTaggerTrainerWindow.cueScopeTrainerTaggerWindowInstance.setVisible(true);
    return CueScopeTaggerTrainerWindow.cueScopeTrainerTaggerWindowInstance;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        CueScopeTaggerTrainerWindow.getInstance(null, new Point(100, 100), new Dimension(600, 800));
      }
    });
  }
}
