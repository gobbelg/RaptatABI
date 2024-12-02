package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.PerformanceScorer;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * Creates the Score Performance window and gets the RapTAT and Reference XML files for scoring.
 *
 * @author Sanjib Saha, created on July 15, 2014
 */
public class ScorePerformanceWindow extends BaseClassWindows {

  /**
   * Creates the Focus listener to to check input format validity for maxToken field. OVERRIDES
   * PARENT METHOD
   *
   * @author Sanjib Saha, created on July 17, 2014
   */
  private class ChangeMaxTokenNumberListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent fe) {
      ScorePerformanceWindow.this.warned = false;
    }


    @Override
    public void focusLost(FocusEvent fe) {
      if (!ScorePerformanceWindow.this.warned) {
        isMaxTokenNumberValid();
        ScorePerformanceWindow.this.warned = true;
      }
    }
  }

  /**
   * Creates the action listener to score button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  private class ScorePerformanceListener implements ActionListener {

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void actionPerformed(ActionEvent e) {
      if (!isMaxTokenNumberValid()) {
        return;
      }

      if (!isWeightInputValid()) {
        return;
      }

      if (ScorePerformanceWindow.this.schemaFile == null) {
        JOptionPane.showMessageDialog(new JFrame(), "Schema File is not Selected.",
            "Schema file error", JOptionPane.OK_OPTION);
        return;
      }

      if (ScorePerformanceWindow.this.comboBoxScoringMode.getSelectedItem().toString()
          .equals("Concept and Attribute")) {
        ScorePerformanceWindow.this.scoringMode =
            RaptatConstants.ScoringMode.CONCEPT_AND_ATTRIBUTE_SCORING;
      } else if (ScorePerformanceWindow.this.comboBoxScoringMode.getSelectedItem().toString()
          .equals("Concept Only")) {
        ScorePerformanceWindow.this.scoringMode = RaptatConstants.ScoringMode.CONCEPT_ONLY_SCORING;
      } else if (ScorePerformanceWindow.this.comboBoxScoringMode.getSelectedItem().toString()
          .equals("")) {
        ScorePerformanceWindow.this.scoringMode = RaptatConstants.ScoringMode.ATTRIBUTE_ONLY;
      }

      JFileChooser saveLocChooser = new JFileChooser();

      if (OptionsManager.getInstance().getLastSelectedDir() != null) {
        saveLocChooser.setCurrentDirectory(OptionsManager.getInstance().getLastSelectedDir());
      }

      int returnVal = saveLocChooser.showSaveDialog(null);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        ScorePerformanceWindow.this.scoreSavelocation =
            saveLocChooser.getSelectedFile().getAbsoluteFile();
        OptionsManager.getInstance().setLastSelectedDir(saveLocChooser.getCurrentDirectory());

        try {
          if (!ScorePerformanceWindow.this.scoreSavelocation.createNewFile()) {
            String message =
                "A file with this name already exists in this folder.\n" + "Replace this file?";
            int reply = JOptionPane.showConfirmDialog(new JFrame(), message, "Duplicate File",
                JOptionPane.YES_NO_CANCEL_OPTION);

            if (reply != JOptionPane.YES_OPTION) {
              return;
            }
          }
        } catch (IOException e1) {
          e1.printStackTrace();
        }

        ScorePerformanceWindow.this.scoreSavelocation.setWritable(true);
      }

      if (ScorePerformanceWindow.this.scoreSavelocation != null) {
        OptionsManager.getInstance().setScoreDir(ScorePerformanceWindow.this.scoreSavelocation);
      } else {
        String message = "Specify location and file name to save solution file\n";
        JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      try {
        PerformanceScorer scorer =
            new PerformanceScorer(ScorePerformanceWindow.this.acceptedConceptsSet,
                ScorePerformanceWindow.this.maxTokensForScoring,
                ScorePerformanceWindow.this.valueConceptWeight,
                ScorePerformanceWindow.this.valueAttributeWeight,
                ScorePerformanceWindow.this.scoringMode);

        /*
         * Need to fix this and allow as option to check phrase offsets during import for comparison
         */
        boolean removeUnmatched = true;
        boolean scoreGenerated = false;

        /*
         * Removed this call as it is no longer the proper one for scoring - This needs to be hooked
         * up again
         */
        // scorer.compareFiles(
        // ScorePerformanceWindow.this.conceptXMLFilesPathList,
        // ScorePerformanceWindow.this.rapTATXMLFilesPathList,
        // ScorePerformanceWindow.this.textFilesPathList,
        // ScorePerformanceWindow.this.schemaFilePath.getText().trim(),
        // removeUnmatched);

        if (scoreGenerated) {
          String message = "The Raptat performance scores have been saved to file\n";
          JOptionPane.showMessageDialog(new JFrame(), message, "Performance scoring Completed",
              JOptionPane.OK_OPTION);
        } else {
          GeneralHelper
              .errorWriter("Error during performance scoring - check files used for scoring");
        }
      } catch (NumberFormatException e2) {
        e2.printStackTrace();
      } catch (HeadlessException e3) {
        e3.printStackTrace();
      }
    }
  }

  /** */
  private static final long serialVersionUID = 3511180772560310905L;

  /** Instance of ScorePerformanceWindow Class */
  public static ScorePerformanceWindow scoreWindowInstance = null;


  /** File location to save the performance result */
  private File scoreSavelocation;


  /** Text field for the maximum token number */
  private JTextField maxTokensField;

  private int valueConceptWeight;

  private int valueAttributeWeight;

  private JTextField weightOfConcept;

  private JTextField weightOfAttribute;

  private JComboBox comboBoxScoringMode;

  private RaptatConstants.ScoringMode scoringMode;

  /** Flag for the focus listener to check if the warning message is displayed */
  private boolean warned = false;

  /** Maximum number of tokens for scoring */
  private int maxTokensForScoring;

  /**
   * Constructor of ScorePerformanceWindow Class.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @author Sanjib Saha, July 15, 2014
   */
  @SuppressWarnings("Convert2Diamond")
  private ScorePerformanceWindow(Point thePoint, Dimension theDimension) {
    this.conceptXMLFilesPathList = new ArrayList<>();
    this.rapTATXMLFilesPathList = new ArrayList<>();
    this.textFilesPathList = new ArrayList<>();

    this.conceptXMLListModel = new DefaultListModel();
    this.raptatXMLListModel = new DefaultListModel();
    this.textFileListModel = new DefaultListModel();

    initialize(thePoint, theDimension);
  }

  /**
   * Creates the components for the option panel.
   *
   * @return A JPanel with the components for the option panel.
   * @author Sanjib Saha, July 15, 2014
   */
  private JPanel getOptionsPanel() {
    JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));

    JButton acceptableConceptsButton =
        new JButton("<html><center>Acceptable<br>Concepts</center></html>");
    acceptableConceptsButton.addActionListener(new AcceptableConceptsButtonListener());
    optionsPanel.add(acceptableConceptsButton);

    Border b2 = BorderFactory.createTitledBorder("Max Tokens");
    this.maxTokensField = new JTextField(OptionsManager.getInstance().getTrainingTokenNumber());
    this.maxTokensField.setBorder(b2);
    this.maxTokensField
        .setText(Integer.toString(OptionsManager.getInstance().getTrainingTokenNumber()));
    this.maxTokensField.setHorizontalAlignment(SwingConstants.CENTER);
    this.maxTokensField.addFocusListener(new ChangeMaxTokenNumberListener());
    optionsPanel.add(this.maxTokensField);

    this.comboBoxScoringMode = new JComboBox();
    b2 = BorderFactory.createTitledBorder("Scoring Method");
    this.comboBoxScoringMode.setBorder(b2);
    this.comboBoxScoringMode.addItem("Concept and Attribute");
    this.comboBoxScoringMode.addItem("Concept Only");
    this.comboBoxScoringMode.addItem("Attribute Only");
    optionsPanel.add(this.comboBoxScoringMode);

    Font borderFont = new Font(optionsPanel.getFont().getFontName(), Font.ITALIC,
        optionsPanel.getFont().getSize());
    Border b3 =
        BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
            "Option Settings", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont);
    optionsPanel.setBorder(b3);

    return optionsPanel;
  }


  /**
   * Creates the components for the run panel.
   *
   * @return A JPanel with the components for the run panel.
   * @author Sanjib Saha, July 15, 2014
   */
  @SuppressWarnings("Convert2Lambda")
  private Component getRunPanel() {
    Box runPanel = Box.createVerticalBox();
    runPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton runButton = new JButton("Score Performance");
    runButton.addActionListener(new ScorePerformanceListener());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        ScorePerformanceWindow.scoreWindowInstance.dispose();
        ScorePerformanceWindow.scoreWindowInstance = null;
      }
    });

    buttonPanel.add(runButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    buttonPanel.add(cancelButton);
    runPanel.add(buttonPanel);

    return runPanel;
  }


  // protected class GetSchemaFileListerner implements ActionListener {
  //
  // /**
  // * Create the Hash Set for the concepts.
  // *
  // * @param e Action Event for the listener
  // */
  // @Override
  // @SuppressWarnings("Convert2Diamond")
  // public void actionPerformed(ActionEvent e) {
  // JFileChooser fileChooser = new
  // JFileChooser(OptionsManager.getInstance().getLastSelectedDir());
  //
  // fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  // fileChooser.setFileFilter(new
  // FileNameExtensionFilter("Select Schema File", "xml"));
  //
  // if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
  // schemaFile = fileChooser.getSelectedFile();
  // schemaFilePath.setText(schemaFile.getAbsolutePath());
  //
  // OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
  // }
  // }
  // }

  private JPanel getSchemaPanel() {
    JPanel getSchemaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));

    JButton acceptableConceptsButton =
        new JButton("<html><center>Select<br>Schema File</center></html>");
    acceptableConceptsButton.addActionListener(new LoadSchemaFileListener());
    getSchemaPanel.add(acceptableConceptsButton);

    this.schemaFilePath = new JTextField(30);
    this.schemaFilePath.setEditable(false);
    this.schemaFilePath.setText("Schema file path.");
    this.schemaFilePath.setHorizontalAlignment(SwingConstants.LEFT);
    getSchemaPanel.add(this.schemaFilePath);

    Border b2 = BorderFactory.createTitledBorder("Concept Match Weight");
    this.weightOfConcept = new JTextField(14);
    this.weightOfConcept.setBorder(b2);
    this.weightOfConcept.setText("10");
    this.weightOfConcept.setHorizontalAlignment(SwingConstants.CENTER);
    // weightOfConcept.addFocusListener(new ChangeMaxTokenNumberListener());
    getSchemaPanel.add(this.weightOfConcept);

    b2 = BorderFactory.createTitledBorder("Attribute Match Weight");
    this.weightOfAttribute = new JTextField(14);
    this.weightOfAttribute.setBorder(b2);
    this.weightOfAttribute.setText("1");
    this.weightOfAttribute.setHorizontalAlignment(SwingConstants.CENTER);
    // weightOfAttribute.addFocusListener(new
    // ChangeMaxTokenNumberListener());
    getSchemaPanel.add(this.weightOfAttribute);

    return getSchemaPanel;
  }


  /**
   * Initializes the window components and assigns corresponding action listeners to the components.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @author Sanjib Saha, July 15, 2014
   */
  private void initialize(Point thePosition, Dimension theDimension) {
    int windowWidth = (int) (theDimension.width * 1.0);
    int windowHeight = (int) (theDimension.height * 0.8);

    windowWidth = windowWidth < RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        ? RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        ? RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        : windowHeight;

    this.setSize(windowWidth, windowHeight);
    setTitle("Score Performance");
    setResizable(false);
    thePosition.translate(10, 10);

    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        ScorePerformanceWindow.scoreWindowInstance.dispose();
        ScorePerformanceWindow.scoreWindowInstance = null;
      }
    });

    // Creates the Box
    Box listPanel = Box.createHorizontalBox();

    JPanel listButtonPanel = new JPanel(new GridLayout(1, 3));
    JPanel generalButtonPanel = new JPanel(new BorderLayout());
    // JPanel getSchemaPanel = new JPanel(new BorderLayout());
    JPanel optionsRunPanel = new JPanel(new GridLayout(1, 3));

    listPanel.setPreferredSize(new Dimension(windowWidth, (int) (windowHeight * 5.0 / 8)));

    // Adding the box for file lists
    setLayout(new BorderLayout());
    this.add(listPanel, BorderLayout.NORTH);

    Dimension panelSize = new Dimension((int) (0.30 * listPanel.getPreferredSize().width),
        listPanel.getPreferredSize().height);

    listPanel.add(buildConceptXMLListPanel(panelSize));
    listPanel.add(buildRapTATXMLListPanel(panelSize));
    listPanel.add(buildTextListPanel(panelSize));

    // Adding the add and remove file buttons
    this.add(generalButtonPanel, BorderLayout.SOUTH);
    generalButtonPanel.add(listButtonPanel, BorderLayout.NORTH);

    panelSize = new Dimension((int) (0.30 * listPanel.getPreferredSize().width),
        (int) (0.10 * listPanel.getPreferredSize().height));

    JPanel refXMLButtonsPanel = new JPanel(new FlowLayout());
    refXMLButtonsPanel.add(getAddRemoveButtonPanel(new AddReferenceConceptXMLFileListener(),
        new RemoveReferenceConceptXMLFileListener(), "Reference XML", panelSize));
    listButtonPanel.add(refXMLButtonsPanel);

    JPanel rapTATXMLButtonsPanel = new JPanel(new FlowLayout());
    rapTATXMLButtonsPanel.add(getAddRemoveButtonPanel(new AddRapTATXMLFileListener(),
        new RemoveRapTATFileXMLListener(), "RapTAT XML", panelSize));
    listButtonPanel.add(rapTATXMLButtonsPanel);

    JPanel textButtonsPanel = new JPanel(new FlowLayout());
    textButtonsPanel.add(getAddRemoveButtonPanel(new AddTextFileListener(),
        new RemoveTextFileListener(), "Text", panelSize));
    listButtonPanel.add(textButtonsPanel);

    // Adding the option buttons
    optionsRunPanel.add(getOptionsPanel());

    // Adding score button
    optionsRunPanel.add(getRunPanel());
    optionsRunPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createLineBorder(Color.GRAY)));

    generalButtonPanel.add(Box.createRigidArea(new Dimension(0, 40)));
    generalButtonPanel.add(getSchemaPanel(), BorderLayout.CENTER);
    generalButtonPanel.add(optionsRunPanel, BorderLayout.SOUTH);
    setVisible(true);
    pack();
  }


  /**
   * Checks if the Max token number field contains a valid token number.
   *
   * @return true if the input is a non-zero, non-negative integer false otherwise
   * @author Sanjib Saha, created on July 17, 2014
   */
  @SuppressWarnings("CallToPrintStackTrace")
  private boolean isMaxTokenNumberValid() {
    try {
      this.maxTokensForScoring = Integer.parseInt(this.maxTokensField.getText());

      if (this.maxTokensForScoring < 1) {
        return false;
      }
    } catch (NumberFormatException e) {
      // e.printStackTrace();
      String message = "Max Token field does not have a valid Token number.\n"
          + "Please enter a valid non-fraction number as Max Token.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Invalid number",
          JOptionPane.ERROR_MESSAGE);

      return false;
    }
    return true;
  }


  @SuppressWarnings("CallToPrintStackTrace")
  private boolean isWeightInputValid() {
    try {
      this.valueConceptWeight = Integer.parseInt(this.weightOfConcept.getText());

      if (this.valueConceptWeight < 0) {
        return false;
      }
    } catch (NumberFormatException e) {
      // e.printStackTrace();
      String message = "Concept weight field does not have a valid weight.\n"
          + "Please enter a valid non-fraction number as weight.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Invalid weight",
          JOptionPane.ERROR_MESSAGE);

      return false;
    }

    try {
      this.valueAttributeWeight = Integer.parseInt(this.weightOfAttribute.getText());

      if (this.valueAttributeWeight < 0) {
        return false;
      }
    } catch (NumberFormatException e) {
      String message = "Attribute weight field does not have a valid weight.\n"
          + "Please enter a valid non-fraction number as weight.";
      JOptionPane.showMessageDialog(new JFrame(), message, "Invalid weight",
          JOptionPane.ERROR_MESSAGE);

      return false;
    }

    return true;
  }


  /**
   * Returns an instance of the ScorePerformanceWindow class.
   *
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @return An instance of the Score Window class
   * @author Sanjib Saha, July 15, 2014
   */
  public static synchronized ScorePerformanceWindow getInstance(Point thePoint,
      Dimension theDimension) {
    if (ScorePerformanceWindow.scoreWindowInstance == null) {
      ScorePerformanceWindow.scoreWindowInstance =
          new ScorePerformanceWindow(thePoint, theDimension);
    }

    return ScorePerformanceWindow.scoreWindowInstance;
  }


  /**
   * Tests the ScorePerformanceWindow Class
   *
   * @param args
   */
  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        ScorePerformanceWindow.getInstance(new Point(100, 100), new Dimension(600, 800));
      }
    });
  }
}
