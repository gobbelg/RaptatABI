package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.MenuResponseDispatcher;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.Annotator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporterRevised;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.WindowHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;

public class AnnotationWindow extends JDialog {

  private class AnnotateButtonListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      double threshold = RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT;
      boolean thresholdInRange = true;

      try {
        threshold = Double.parseDouble(AnnotationWindow.this.thresholdField.getText());
        if (threshold < 0.0 || threshold > 1.0) {
          throw new NumberFormatException();
        }

      } catch (NumberFormatException e1) {
        thresholdInRange = false;
        System.err.println(e1.getMessage());
        GeneralHelper.errorWriter("<html><b>Threshold Input Error:</b><br>"
            + "Please use a fraction between 0.0 and 1.0<html>");
        GeneralHelper.errorWriter("Threshold reset to default value of "
            + RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT);
        AnnotationWindow.this.thresholdField
            .setText(Double.toString(RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT));
      }
      if (thresholdInRange) {
        OptionsManager options = OptionsManager.getInstance();
        String startDirectory = options.getLastSelectedDir().getAbsolutePath();
        RaptatPair<RaptatAnnotationSolution, String> solutionAndDirectory =
            new MenuResponseDispatcher().loadSolution(startDirectory);

        File exportLocation;
        if (solutionAndDirectory != null && (exportLocation = GeneralHelper.getDirectory(
            "Choose directory" + " for exporting results to XML", startDirectory)) != null) {
          boolean ignoreOverwrites = true;
          XMLExporterRevised xmlExporter =
              XMLExporterRevised.getExporter(exportLocation, ignoreOverwrites);
          RaptatAnnotationSolution theSolution = solutionAndDirectory.left;
          String pathToDictionary = null;
          if (AnnotationWindow.this.fbDictionaryFile != null) {
            pathToDictionary = AnnotationWindow.this.fbDictionaryFile.getAbsolutePath();
          }
          HashSet<String> acceptedConcepts = AnnotationWindow.this.acceptedConceptsSet;

          Annotator annotator =
              new Annotator(theSolution, pathToDictionary, acceptedConcepts, threshold);

          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          int docNumber = 1;
          int corpusSize = AnnotationWindow.this.textFilesList.size();
          for (String curTextPath : AnnotationWindow.this.textFilesList) {
            System.out.println("Annotating document " + docNumber++ + " of " + corpusSize);
            AnnotationGroup curAnnotationGroup = annotator.annotate(curTextPath);
            if (curAnnotationGroup.raptatAnnotations != null) {
              boolean correctOffsets = true;
              boolean insertPhraseStrings = true;
              xmlExporter.exportRaptatAnnotationGroup(curAnnotationGroup, correctOffsets,
                  insertPhraseStrings);
            }
            if (curAnnotationGroup.raptatAnnotations == null
                || curAnnotationGroup.raptatAnnotations.size() == 0) {
              System.out.println("No annotations for export");
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          setVisible(false);
          dispose();
        }
      }
    }
  }

  private class GetFacilitatorBlockerListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser =
          new JFileChooser(OptionsManager.getInstance().getLastSelectedDir());
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setFileFilter(
          new FileNameExtensionFilter("Select Facilitator/Blocker Dictionary File", "dct"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        AnnotationWindow.this.fbDictionaryFile = fileChooser.getSelectedFile();
        AnnotationWindow.this.fbDictionaryFilePath
            .setText(AnnotationWindow.this.fbDictionaryFile.getAbsolutePath());

        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
      } else {
        AnnotationWindow.this.fbButton.setEnabled(false);
        AnnotationWindow.this.fbDictionaryFilePath.setEnabled(false);
        AnnotationWindow.this.doNotLoadDictionaryButton.setSelected(true);
      }
    }
  }

  private class LoadDictionaryListener implements ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent )
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (AnnotationWindow.this.loadDictionaryButton.isSelected()) {
        AnnotationWindow.this.fbButton.setEnabled(true);
        AnnotationWindow.this.fbDictionaryFilePath.setEnabled(true);
        if (AnnotationWindow.this.fbDictionaryFile != null) {
          AnnotationWindow.this.fbDictionaryFilePath
              .setText(AnnotationWindow.this.fbDictionaryFile.getAbsolutePath());
        } else {
          AnnotationWindow.this.fbDictionaryFilePath
              .setText("Facilitator/Blocker dictionary path . . .");
          new GetFacilitatorBlockerListener().actionPerformed(null);
        }
      } else {
        AnnotationWindow.this.fbButton.setEnabled(false);
        AnnotationWindow.this.fbDictionaryFilePath.setEnabled(false);
        AnnotationWindow.this.fbDictionaryFilePath
            .setText("Facilitator/Blocker dictionary path . . .");
        AnnotationWindow.this.fbDictionaryFile = null;
      }
    }
  }

  private class RemoveConceptsListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = AnnotationWindow.this.selectedConceptIndices.length - 1; i >= 0; i--) {
        AnnotationWindow.this.acceptedConceptsSet.remove(AnnotationWindow.this.conceptListModel
            .elementAt(AnnotationWindow.this.selectedConceptIndices[i]).toString());
        AnnotationWindow.this.conceptListModel
            .removeElementAt(AnnotationWindow.this.selectedConceptIndices[i]);
      }
    }
  }

  private class RemoveTextFileListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = AnnotationWindow.this.textSrcselections.length - 1; i >= 0; i--) {
        AnnotationWindow.this.textFilesList.remove(AnnotationWindow.this.textSrcselections[i]);
        AnnotationWindow.this.docListModel
            .removeElementAt(AnnotationWindow.this.textSrcselections[i]);
      }
    }
  }

  private class SetConceptsListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      AnnotationWindow.this.acceptedConceptsSet = new HashSet<>();
      String baseDirectory = OptionsManager.getInstance().getLastSelectedDir().getAbsolutePath();
      CSVReader conceptReader =
          new CSVReader(baseDirectory, false, "Select file listing concepts to be analyzed");
      if (conceptReader.isValid()) {
        String[] nextDataSet;
        while ((nextDataSet = conceptReader.getNextData()) != null) {
          AnnotationWindow.this.acceptedConceptsSet.add(nextDataSet[0].toLowerCase());
          AnnotationWindow.this.conceptListModel.addElement(nextDataSet[0]);
        }
      }
    }
  }

  private class SetTextFilesListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ev) {
      JFileChooser fileChooser = new JFileChooser("File Dialog");
      if (OptionsManager.getInstance().getLastSelectedDir() != null) {
        fileChooser.setCurrentDirectory(OptionsManager.getInstance().getLastSelectedDir());
      }
      fileChooser.setMultiSelectionEnabled(true);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      FileNameExtensionFilter extFilter = new FileNameExtensionFilter("Text files only", "txt");
      fileChooser.setFileFilter(extFilter);
      int returnVal = fileChooser.showOpenDialog(null);
      File[] dirFiles;

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
        AnnotationWindow.this.docListModel.clear();
        AnnotationWindow.this.textFilesList.clear();

        for (File selectedFile : selectedFiles) {
          if (WindowHelper.validFile(selectedFile)) {
            if (!AnnotationWindow.this.docListModel.contains(selectedFile.getName())) {
              AnnotationWindow.this.docListModel.addElement(selectedFile.getName());
              AnnotationWindow.this.textFilesList.add(selectedFile.getAbsolutePath());
            } else {
              String message = "\"File already exists in the list!\n";

              JOptionPane.showMessageDialog(new JFrame(), message, "Error",
                  JOptionPane.ERROR_MESSAGE);
            }
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)) {
                if (!AnnotationWindow.this.docListModel.contains(selectedFile.getName())) {
                  AnnotationWindow.this.docListModel.addElement(dirFile.getName());
                  AnnotationWindow.this.textFilesList.add(dirFile.getAbsolutePath());
                } else {
                  String message = "\"File already exists in the list!\n";
                  JOptionPane.showMessageDialog(new JFrame(), message, "Error",
                      JOptionPane.ERROR_MESSAGE);
                }
              }
            }
          }
        }
      }
    }
  }

  private static final long serialVersionUID = 35601560859006230L;

  public static AnnotationWindow annotationFinderWindowInstance = null;


  private final DefaultListModel<String> docListModel;


  private final List<String> textFilesList = new Vector<>();

  private final DefaultListModel<String> conceptListModel;
  private JList<String> textList;

  private JList<String> conceptList;

  private JList<?> textSrclist;;
  private int textSrcselections[], selectedConceptIndices[];

  private HashSet<String> acceptedConceptsSet;

  private final JTextField thresholdField = new JTextField(5);

  private final double threshold = RaptatConstants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT;

  private File fbDictionaryFile = null;

  private JTextField fbDictionaryFilePath;

  private JRadioButton loadDictionaryButton = new JRadioButton("Yes");

  private final JRadioButton doNotLoadDictionaryButton = new JRadioButton("No");

  private JButton fbButton;

  public AnnotationWindow() {
    this.docListModel = new DefaultListModel<>();
    this.conceptListModel = new DefaultListModel<>();
  }

  /**
   * ********************************************
   *
   * @param thePosition
   * @param theDimension
   * @author Glenn Gobbel - Jun 12, 2012 ********************************************
   */
  public AnnotationWindow(Point thePosition, Dimension theDimension) {
    this.docListModel = new DefaultListModel<>();
    this.conceptListModel = new DefaultListModel<>();
    initialize(thePosition, theDimension);
  }


  private JPanel buildConceptListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.conceptList = new JList<>(this.conceptListModel);
    scrollPane.setViewportView(this.conceptList);
    this.conceptList.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, null, null),
        "Concepts for Annotation", TitledBorder.LEADING, TitledBorder.TOP, null,
        new Color(0, 0, 0)));

    ListSelectionListener conceptSelectionListener = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          JList<?> conceptListSource = (JList<?>) listSelectionEvent.getSource();
          AnnotationWindow.this.selectedConceptIndices = conceptListSource.getSelectedIndices();
        }
      }
    };

    this.conceptList.addListSelectionListener(conceptSelectionListener);
    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    JPanel thePanel = new JPanel();
    scrollPane.setPreferredSize(panelSize);
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane, BorderLayout.NORTH);
    return thePanel;
  }


  private JPanel buildTextListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.textList = new JList<>(this.docListModel);
    this.textList.setBorder(
        new TitledBorder(null, "Text Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    scrollPane.setViewportView(this.textList);

    ListSelectionListener ListSelLis = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          AnnotationWindow.this.textSrclist = (JList<?>) listSelectionEvent.getSource();
          AnnotationWindow.this.textSrcselections =
              AnnotationWindow.this.textSrclist.getSelectedIndices();
        }
      }
    };
    this.textList.addListSelectionListener(ListSelLis);
    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);
    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane, BorderLayout.NORTH);
    return thePanel;
  }


  /**
   * ***********************************************************
   *
   * @param addTextFileListener
   * @param removeTextFileListener
   * @return
   * @author Glenn Gobbel - Jun 12, 2012 modified by Sanjib Saha - July 24, 2014
   * @param chooseButtonString ***********************************************************
   */
  private Component getChooseRemoveButtonPanel(ActionListener addFileListener,
      ActionListener removeFileListener, Dimension panelSize) {
    // JButton chooseButton = new JButton( "Choose " + chooseButtonString );
    JButton chooseButton = new JButton("Choose ");
    chooseButton.addActionListener(addFileListener);

    // JButton removeButton = new JButton( "Remove " + chooseButtonString);
    JButton removeButton = new JButton("Remove ");
    removeButton.addActionListener(removeFileListener);
    // removeButton.setPreferredSize( chooseButton.getPreferredSize() );

    // JPanel theButtonPanel = new JPanel( new FlowLayout() );
    // theButtonPanel.add( chooseButton );
    // theButtonPanel.add( removeButton );
    // Buttons are aligned with the border
    JPanel theButtonPanel = new JPanel(new GridBagLayout());
    theButtonPanel.setPreferredSize(panelSize);

    GridBagConstraints c = new GridBagConstraints();

    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    theButtonPanel.add(chooseButton, c);

    c.weightx = 0.5;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    theButtonPanel.add(removeButton, c);

    return theButtonPanel;
  }


  private JPanel getFacilitatorBlockerPanel() {
    JPanel fbPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

    fbPanel.add(getUseDictionaryPanel());
    this.fbButton = new JButton("<html><center>Load Dictionary</center></html>");
    this.fbButton.setEnabled(false);
    this.fbButton.addActionListener(new GetFacilitatorBlockerListener());
    fbPanel.add(this.fbButton);

    this.fbDictionaryFilePath = new JTextField(30);
    this.fbDictionaryFilePath.setEditable(false);
    this.fbDictionaryFilePath.setText("Facilitator/Blocker dictionary path . . . ");
    this.fbDictionaryFilePath.setEnabled(false);
    this.fbDictionaryFilePath.setHorizontalAlignment(SwingConstants.LEFT);
    fbPanel.add(this.fbDictionaryFilePath);

    return fbPanel;
  }


  /**
   * @return
   * @author Glenn Gobbel - Jun 12, 2012
   */
  private Component getRunPanel() {
    Box runPanel = Box.createVerticalBox();
    runPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton runButton = new JButton("Annotate");
    runButton.addActionListener(new AnnotateButtonListener());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        AnnotationWindow.this.setVisible(false);
        AnnotationWindow.this.dispose();
      }
    });
    cancelButton.setPreferredSize(runButton.getPreferredSize());
    buttonPanel.add(runButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    buttonPanel.add(cancelButton);
    runPanel.add(buttonPanel);
    return runPanel;
  }


  private JPanel getUseDictionaryPanel() {
    JPanel useDictionaryPanel = new JPanel();
    JPanel yesNoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));

    JLabel useDictionaryLabel = new JLabel("<html><center>Load Dictionary?</center></html>");
    yesNoPanel.add(useDictionaryLabel);

    ButtonGroup trainingSelection = new ButtonGroup();
    trainingSelection.add(this.loadDictionaryButton);
    trainingSelection.add(this.doNotLoadDictionaryButton);
    this.doNotLoadDictionaryButton.setSelected(true);

    yesNoPanel.add(this.loadDictionaryButton);
    yesNoPanel.add(this.doNotLoadDictionaryButton);

    LoadDictionaryListener listener = new LoadDictionaryListener();
    this.loadDictionaryButton.addActionListener(listener);
    this.doNotLoadDictionaryButton.addActionListener(listener);

    useDictionaryPanel.add(yesNoPanel, BorderLayout.CENTER);

    return useDictionaryPanel;
  }


  private void initialize(Point thePosition, Dimension theDimension) {
    int windowWidth = (int) (theDimension.width * 1.0);
    int windowHeight = (int) (theDimension.height * 0.5);
    windowWidth = windowWidth < RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        ? RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.ANNOTATE_WINDOW_MIN_HEIGHT
        ? RaptatConstants.ANNOTATE_WINDOW_MIN_HEIGHT
        : windowHeight;

    this.setSize(windowWidth, windowHeight);
    setTitle("Annotate Documents Window");
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    thePosition.translate(40, 40);
    this.setLocation(thePosition);

    Box listPanel = Box.createHorizontalBox();
    listPanel.setPreferredSize(new Dimension(windowWidth, (int) (windowHeight * 5.0 / 8)));

    Container mainWindowContainer = getContentPane();
    mainWindowContainer.setLayout(new BorderLayout());
    mainWindowContainer.add(listPanel, BorderLayout.NORTH);

    JPanel textListPanel =
        buildTextListPanel(new Dimension((int) (0.45 * listPanel.getPreferredSize().width),
            (int) (0.85 * listPanel.getPreferredSize().height)));
    JPanel textButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
    Dimension panelSize = new Dimension((int) (0.9 * textListPanel.getPreferredSize().width),
        (int) (0.10 * textListPanel.getPreferredSize().height));
    textButtonsPanel.add(getChooseRemoveButtonPanel(new SetTextFilesListener(),
        new RemoveTextFileListener(), panelSize));
    textListPanel.add(textButtonsPanel, BorderLayout.SOUTH);

    JPanel conceptListPanel =
        buildConceptListPanel(new Dimension((int) (0.25 * listPanel.getPreferredSize().width),
            (int) (0.85 * listPanel.getPreferredSize().height)));
    JPanel conceptButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
    panelSize = new Dimension((int) (0.8 * conceptListPanel.getPreferredSize().width),
        (int) (0.10 * conceptListPanel.getPreferredSize().height));
    conceptButtonsPanel.add(getChooseRemoveButtonPanel(new SetConceptsListener(),
        new RemoveConceptsListener(), panelSize));
    conceptListPanel.add(conceptButtonsPanel, BorderLayout.SOUTH);

    listPanel.add(textListPanel);
    listPanel.add(conceptListPanel);

    JPanel generalButtonPanel = new JPanel(new BorderLayout());
    JPanel optionsRunPanel = new JPanel(new GridLayout(1, 2));
    mainWindowContainer.add(generalButtonPanel, BorderLayout.SOUTH);

    JPanel thresholdFieldPanel = new JPanel();
    Border b3 = BorderFactory.createTitledBorder(
        BorderFactory.createBevelBorder(BevelBorder.RAISED),
        "Phase Identification Threshold (0.0 to 1.0)", TitledBorder.CENTER, TitledBorder.BELOW_TOP);
    thresholdFieldPanel.setBorder(b3);
    this.thresholdField.setText(Double.toString(this.threshold));
    this.thresholdField.setEditable(true);
    this.thresholdField.setHorizontalAlignment(SwingConstants.CENTER);
    thresholdFieldPanel.add(this.thresholdField);
    optionsRunPanel.add(thresholdFieldPanel, BorderLayout.CENTER);

    optionsRunPanel.add(getRunPanel());
    optionsRunPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createLineBorder(Color.GRAY)));

    generalButtonPanel.add(getFacilitatorBlockerPanel(), BorderLayout.CENTER);
    generalButtonPanel.add(optionsRunPanel, BorderLayout.SOUTH);

    setVisible(true);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    pack();
  }


  public static synchronized AnnotationWindow getInstance(Point thePoint, Dimension theDimension) {
    if (AnnotationWindow.annotationFinderWindowInstance == null) {
      AnnotationWindow.annotationFinderWindowInstance =
          new AnnotationWindow(thePoint, theDimension);
    }

    return AnnotationWindow.annotationFinderWindowInstance;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        AnnotationWindow.getInstance(new Point(0, 0), new Dimension(100, 500));
      }
    });
  }
}
