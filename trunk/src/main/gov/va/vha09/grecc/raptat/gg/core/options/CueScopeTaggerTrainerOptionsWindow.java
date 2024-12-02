/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;

/**
 * ************************************************
 *
 * @author vhatvhgobbeg - Jul 11, 2013 ************************************************
 */
public final class CueScopeTaggerTrainerOptionsWindow extends JDialog implements ActionListener {
  /** @author Glenn T. Gobbel Apr 13, 2016 */
  private class CancelButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      CueScopeTaggerTrainerOptionsWindow.taggerTrainerWindowInstance.dispose();
    }
  }

  /** @author Glenn T. Gobbel Apr 13, 2016 */
  private class FeatureFileButtonListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent event) {
      String dialogTitle;
      File featureFile;
      JTextField featureFilePath;

      if (event.getSource() == CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesButton) {
        dialogTitle = "Select Cue Features File";
        featureFilePath = CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFilePath;
      } else {
        dialogTitle = "Select Scope Features File";
        featureFilePath = CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFilePath;
      }
      JFileChooser fileChooser =
          new JFileChooser(OptionsManager.getInstance().getLastSelectedDir());

      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setFileFilter(new FileNameExtensionFilter(dialogTitle, "txt"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        featureFile = fileChooser.getSelectedFile();
        featureFilePath.setText(featureFile.getAbsolutePath());
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
      }
    }
  }

  /** @author Glenn T. Gobbel Apr 14, 2016 */
  private class SaveButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions
          .setCueFeaturesFile(CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFile);
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions
          .setScopeFeaturesFile(CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFile);
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions.setWordOffsetDistance(
          Integer.parseInt(CueScopeTaggerTrainerOptionsWindow.this.wordOffsetDistance.getText()));
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions.setWordConjunctionSize(
          Integer.parseInt(CueScopeTaggerTrainerOptionsWindow.this.wordNgramSize.getText()));
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions.setPosOffsetDistance(
          Integer.parseInt(CueScopeTaggerTrainerOptionsWindow.this.posOffsetDistance.getText()));
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions.setPosConjunctionSize(
          Integer.parseInt(CueScopeTaggerTrainerOptionsWindow.this.posNgramSize.getText()));
      CueScopeTaggerTrainerOptionsWindow.this.trainingOptions
          .setTokenOptions(CueScopeTaggerTrainerOptionsWindow.this.tokenOptionsCopy);

      dispose();
    }
  }

  private class SpecifyFeaturesListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (CueScopeTaggerTrainerOptionsWindow.this.specifyFeaturesButton.isSelected()) {
        CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesButton.setEnabled(true);
        CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFilePath.setEnabled(true);
        if (CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFile != null) {
          CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFilePath
              .setText(CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFile.getAbsolutePath());
        } else {
          CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFilePath
              .setText("Cue features file path . . .");
        }

        CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesButton.setEnabled(true);
        CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFilePath.setEnabled(true);
        if (CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFile != null) {
          CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFilePath
              .setText(CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFile.getAbsolutePath());
        } else {
          CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFilePath
              .setText("Scope features file path . . .");
        }
      } else {
        CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesButton.setEnabled(false);
        CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFilePath.setEnabled(false);
        CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFilePath
            .setText("Facilitator/Blocker dictionary path . . .");
        CueScopeTaggerTrainerOptionsWindow.this.cueFeaturesFile = null;

        CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesButton.setEnabled(false);
        CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFilePath.setEnabled(false);
        CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFilePath
            .setText("Facilitator/Blocker dictionary path . . .");
        CueScopeTaggerTrainerOptionsWindow.this.scopeFeaturesFile = null;
      }
    }
  }

  /** @author Glenn T. Gobbel Apr 14, 2016 */
  private class TokenOptionsListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent arg0) {
      Point upperLeftPosition = CueScopeTaggerTrainerOptionsWindow.this.getLocation();
      Dimension size = CueScopeTaggerTrainerOptionsWindow.this.getSize();
      TokenOptionsWindow.getInstance(CueScopeTaggerTrainerOptionsWindow.this.tokenOptionsCopy,
          CueScopeTaggerTrainerOptionsWindow.this, upperLeftPosition, size);
    }
  }

  /** */
  private static final long serialVersionUID = -6710586254440798888L;

  private static CueScopeTaggerTrainerOptionsWindow taggerTrainerWindowInstance;
  private static final Logger LOGGER = Logger.getLogger(CueScopeTaggerTrainerOptionsWindow.class);


  private File cueFeaturesFile = null;


  private File scopeFeaturesFile = null;

  private JTextField wordOffsetDistance = new JTextField(2);
  private JTextField wordNgramSize = new JTextField(2);
  private JTextField posOffsetDistance = new JTextField(2);
  private JTextField posNgramSize = new JTextField(2);

  private JTextField cueFeaturesFilePath;
  private JTextField scopeFeaturesFilePath;
  private AbstractButton specifyFeaturesButton = new JRadioButton("Yes");
  private AbstractButton doNotSpecifyFeaturesButton = new JRadioButton("No");
  private AbstractButton cancelButton;
  private AbstractButton tokenOptionsButton;
  private AbstractButton cueFeaturesButton = new JButton("Cue Features");

  private AbstractButton scopeFeaturesButton = new JButton("Scope Features");
  private AbstractButton saveButton;

  private boolean windowInitiated = false;

  private TaggerTrainerOptions trainingOptions;
  /*
   * Use a copy of the tokenOptions from the trainingOptions for assessing and getting changes to
   * the tokenOptions via the TokenOptionsListener. Using this approach allows one to cancel this
   * window and not change the tokenOptions within trainingOptions. They are only changed if the
   * "save" button is selected.
   */
  private TokenProcessingOptions tokenOptionsCopy;

  private CueScopeTaggerTrainerOptionsWindow(TaggerTrainerOptions trainingOptions, Window parent,
      Point position, Dimension dim) {
    this(parent, trainingOptions);
    Point point;
    if (parent != null) {
      int x = parent.getX() + (parent.getWidth() - getWidth()) / 2;
      int y = parent.getY() + (parent.getHeight() - getHeight()) / 2;
      point = new Point(x, y);
    } else {
      point = new Point(0, 0);
    }

    initializeWindow(point, dim);
  }

  /**
   * @param trainingOptions
   */
  private CueScopeTaggerTrainerOptionsWindow(Window parent, TaggerTrainerOptions trainingOptions) {
    super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
    setOptions(trainingOptions);
    CueScopeTaggerTrainerOptionsWindow.LOGGER.setLevel(Level.INFO);
  }


  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (this.windowInitiated) // Only respond once window created
    {
      Object eventSource = e.getSource();
      if (eventSource == this.saveButton) {
        processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        saveTrainOptions();
      }
    }
  }


  /** @return */
  private Component buildOffsetNgramPanel() {
    Box offsetNgramButtons = Box.createVerticalBox();
    offsetNgramButtons.add(getConjunctionSettingsPanel("Word Conjunctions", this.wordOffsetDistance,
        this.wordNgramSize, this.trainingOptions.getWordOffsetDistance(),
        this.trainingOptions.getWordConjunctionSize()));
    offsetNgramButtons.add(Box.createVerticalStrut(5));
    offsetNgramButtons.add(getConjunctionSettingsPanel("POS Conjunctions", this.posOffsetDistance,
        this.posNgramSize, this.trainingOptions.getPosOffsetDistance(),
        this.trainingOptions.getPosConjunctionSize()));
    return offsetNgramButtons;
  }


  /** @return */
  private Component buildOptionsAndSavePanel() {
    Box optionsAndSavePanel = Box.createHorizontalBox();

    this.tokenOptionsButton = new JButton("Token Options");
    this.tokenOptionsButton.addActionListener(new TokenOptionsListener());
    Box tokenOptionsButtonPanel = Box.createVerticalBox();
    tokenOptionsButtonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

    Box innerPanel = Box.createHorizontalBox();
    innerPanel.add(Box.createHorizontalStrut(10));
    innerPanel.add(this.tokenOptionsButton);
    innerPanel.add(Box.createHorizontalStrut(10));

    tokenOptionsButtonPanel.add(Box.createVerticalStrut(5));
    tokenOptionsButtonPanel.add(innerPanel);
    tokenOptionsButtonPanel.add(Box.createVerticalStrut(5));

    optionsAndSavePanel.add(Box.createHorizontalStrut(15));
    optionsAndSavePanel.add(tokenOptionsButtonPanel);
    optionsAndSavePanel.add(Box.createHorizontalStrut(15));
    optionsAndSavePanel.add(buildSavePanel());

    return optionsAndSavePanel;
  }


  /**
   * ************************************************************** Create JPanel to save the
   * settings for the training options
   *
   * @return Component **************************************************************
   */
  private Component buildSavePanel() {
    Box savePanel = Box.createHorizontalBox();
    savePanel.add(Box.createVerticalStrut(50));
    this.saveButton = new JButton("Save");
    this.cancelButton = new JButton("Cancel");
    this.saveButton.addActionListener(new SaveButtonListener());
    this.cancelButton.addActionListener(new CancelButtonListener());

    savePanel.add(this.saveButton);
    savePanel.add(Box.createHorizontalStrut(10));
    savePanel.add(this.cancelButton);
    savePanel.add(Box.createHorizontalStrut(10));

    return savePanel;
  }


  /**
   * @param size
   * @param offset
   * @return
   */
  private Component getConjunctionSettingsPanel(String title, JTextField offsetField,
      JTextField conjunctionSizeField, int offset, int size) {
    Box conjunctionSettingsPanel = Box.createHorizontalBox();
    conjunctionSettingsPanel.setBorder(BorderFactory.createTitledBorder(title));

    offsetField.setText(Integer.toString(offset));
    offsetField.setBorder(BorderFactory.createTitledBorder("Offset Distance"));
    offsetField.setHorizontalAlignment(SwingConstants.CENTER);
    conjunctionSettingsPanel.add(Box.createHorizontalStrut(20));
    conjunctionSettingsPanel.add(offsetField);

    conjunctionSettingsPanel.add(Box.createHorizontalStrut(50));

    conjunctionSizeField.setText(Integer.toString(size));
    conjunctionSizeField.setBorder(BorderFactory.createTitledBorder("Conjunction Size"));
    conjunctionSizeField.setHorizontalAlignment(SwingConstants.CENTER);
    conjunctionSettingsPanel.add(conjunctionSizeField);
    conjunctionSettingsPanel.add(Box.createHorizontalStrut(20));

    return conjunctionSettingsPanel;
  }


  /**
   * Creates panel used to load cue and scope features to include in a CRF model. If not specified,
   * all features created by the feature builder are included
   *
   * @return
   */
  private Component getLoadFeaturesPanel() {
    this.scopeFeaturesButton.addActionListener(new FeatureFileButtonListener());
    this.scopeFeaturesButton.setEnabled(false);
    this.scopeFeaturesFilePath = new JTextField(10);
    this.scopeFeaturesFilePath.setEditable(false);
    this.scopeFeaturesFilePath.setText("Scope features file path . . . ");
    this.scopeFeaturesFilePath.setEnabled(false);
    this.scopeFeaturesFilePath.setHorizontalAlignment(SwingConstants.LEFT);

    Box scopeFeaturesPanel = Box.createHorizontalBox();
    scopeFeaturesPanel.add(this.scopeFeaturesButton);
    scopeFeaturesPanel.add(Box.createHorizontalStrut(2));
    scopeFeaturesPanel.add(this.scopeFeaturesFilePath);

    this.cueFeaturesButton.addActionListener(new FeatureFileButtonListener());
    this.cueFeaturesButton.setEnabled(false);
    this.cueFeaturesFilePath = new JTextField(10);
    this.cueFeaturesFilePath.setEditable(false);
    this.cueFeaturesFilePath.setText("Cue features file path . . . ");
    this.cueFeaturesFilePath.setEnabled(false);
    this.cueFeaturesFilePath.setHorizontalAlignment(SwingConstants.LEFT);

    Box cueFeaturesPanel = Box.createHorizontalBox();
    this.cueFeaturesButton.setPreferredSize(this.scopeFeaturesButton.getPreferredSize());
    cueFeaturesPanel.add(this.cueFeaturesButton);
    cueFeaturesPanel.add(Box.createHorizontalStrut(2));
    cueFeaturesPanel.add(this.cueFeaturesFilePath);

    Box loadFeaturesPanel = Box.createVerticalBox();
    loadFeaturesPanel.add(cueFeaturesPanel);
    loadFeaturesPanel.add(Box.createVerticalStrut(10));
    loadFeaturesPanel.add(scopeFeaturesPanel);

    return loadFeaturesPanel;
  }


  private Component getSpecifyFeaturesPanel() {
    Box specifyFeaturesPanel = Box.createHorizontalBox();
    specifyFeaturesPanel.setBorder(BorderFactory.createTitledBorder("Cue-Scope Features"));
    JPanel yesNoPanel = new JPanel(new FlowLayout());

    JLabel useDictionaryLabel =
        new JLabel("<html><center>Specify Cue<br>and Scope<br>Features?</center></html>");
    yesNoPanel.add(useDictionaryLabel);

    ButtonGroup trainingSelection = new ButtonGroup();
    trainingSelection.add(this.specifyFeaturesButton);
    trainingSelection.add(this.doNotSpecifyFeaturesButton);
    this.doNotSpecifyFeaturesButton.setSelected(true);

    Box buttonPanel = Box.createVerticalBox();
    buttonPanel.add(this.specifyFeaturesButton);
    buttonPanel.add(this.doNotSpecifyFeaturesButton);
    yesNoPanel.add(buttonPanel);

    ActionListener listener = new SpecifyFeaturesListener();
    this.specifyFeaturesButton.addActionListener(listener);
    this.doNotSpecifyFeaturesButton.addActionListener(listener);

    specifyFeaturesPanel.add(Box.createHorizontalStrut(10));
    specifyFeaturesPanel.add(yesNoPanel);
    specifyFeaturesPanel.add(Box.createHorizontalStrut(10));
    specifyFeaturesPanel.add(getLoadFeaturesPanel());
    specifyFeaturesPanel.add(Box.createHorizontalStrut(10));

    return specifyFeaturesPanel;
  }


  /**
   * @param point
   * @param dim
   */
  private void initializeWindow(Point position, Dimension dim) {
    setTitle("Cue-Scope Training Options");
    int windowWidth = (int) (4 * dim.getWidth() / 5);
    int windowHeight = (int) dim.getHeight() * 2 / 3;
    windowWidth = windowWidth < RaptatConstants.TAGGER_TRAIN_OPTIONS_MIN_WIDTH
        ? RaptatConstants.TAGGER_TRAIN_OPTIONS_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        ? RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        : windowHeight;
    this.setSize(windowWidth, windowHeight);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    this.setLocation(position);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        CueScopeTaggerTrainerOptionsWindow.this.dispose();
      }
    });
    Box mainPanel = Box.createVerticalBox();
    mainPanel.add(buildOffsetNgramPanel());
    mainPanel.add(Box.createVerticalStrut(15));
    mainPanel.add(getSpecifyFeaturesPanel());
    mainPanel.add(Box.createVerticalStrut(15));
    mainPanel.add(buildOptionsAndSavePanel());
    mainPanel.add(Box.createVerticalStrut(10));

    this.add(mainPanel);
    pack();
    this.windowInitiated = true;
  }


  /**
   * ************************************************************** Save all the settings currently
   * set in the options window dialog.
   *
   * <p>
   * **************************************************************
   */
  private void saveTrainOptions() {}


  /**
   * @param trainingOptions2
   */
  private void setOptions(TaggerTrainerOptions inputOptions) {
    this.trainingOptions = inputOptions;
    this.tokenOptionsCopy = new TokenProcessingOptions(inputOptions.getTokenOptions());
  }


  /**
   * Returns an instance of the ScorePerformanceWindow class. * @param mainSolution
   *
   * @param parent
   * @param mainSolution
   * @param solutionFile
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @return An instance of the AttributeTaggerWindow class
   */
  public static synchronized CueScopeTaggerTrainerOptionsWindow getInstance(
      TaggerTrainerOptions trainingOptions, Window parent, Point thePoint, Dimension theDimension) {
    if (trainingOptions == null) {
      throw new IllegalArgumentException("The TaggerTrainerOptions parameter can not be null");
    }
    if (CueScopeTaggerTrainerOptionsWindow.taggerTrainerWindowInstance == null) {
      CueScopeTaggerTrainerOptionsWindow.taggerTrainerWindowInstance =
          new CueScopeTaggerTrainerOptionsWindow(trainingOptions, parent, thePoint, theDimension);
    }

    CueScopeTaggerTrainerOptionsWindow.taggerTrainerWindowInstance.setOptions(trainingOptions);
    CueScopeTaggerTrainerOptionsWindow.taggerTrainerWindowInstance.setVisible(true);
    return CueScopeTaggerTrainerOptionsWindow.taggerTrainerWindowInstance;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        TaggerTrainerOptions ttOptions = new TaggerTrainerOptions();
        CueScopeTaggerTrainerOptionsWindow.getInstance(ttOptions, null, new Point(0, 0),
            new Dimension(0, 0));
      }
    });
  }
}
