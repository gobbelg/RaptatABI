/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.BorderLayout;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.WindowHelper;

/** @author Glenn T. Gobbel Mar 29, 2016 */
public class AttributeTaggerChooser extends BaseClassWindows {
  /**
   * Creates the action listener to add text file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  private class AddTaggerFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the tagger solution file location.
     *
     * @param ev Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
      JFileChooser fileChooser = new JFileChooser("File Dialog");

      if (OptionsManager.getInstance().getLastSelectedDir() != null) {
        fileChooser.setCurrentDirectory(OptionsManager.getInstance().getLastSelectedDir());
      }

      fileChooser.setMultiSelectionEnabled(true);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setFileFilter(new FileNameExtensionFilter(".atsn files only", "atsn"));

      int returnVal = fileChooser.showOpenDialog(null);
      File[] dirFiles;

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());

        for (File selectedFile : selectedFiles) {
          if (WindowHelper.validFile(selectedFile)
              && !AttributeTaggerChooser.this.taggerFileListModel
                  .contains(selectedFile.getName())) {
            AttributeTaggerChooser.this.taggerFileListModel.addElement(selectedFile.getName());
            AttributeTaggerChooser.this.taggerFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !AttributeTaggerChooser.this.taggerFileListModel.contains(dirFile.getName())) {
                AttributeTaggerChooser.this.taggerFileListModel.addElement(dirFile.getName());
                AttributeTaggerChooser.this.taggerFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  /**
   * Creates the action listener to remove text file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  private class RemoveTaggerFileListener implements ActionListener {

    /**
     * Removes the selected Text files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = AttributeTaggerChooser.this.taggerFileSelections.length - 1; i >= 0; i--) {
        AttributeTaggerChooser.this.taggerFilesPathList
            .remove(AttributeTaggerChooser.this.textFileSelections[i]);
        AttributeTaggerChooser.this.taggerFileListModel
            .removeElementAt(AttributeTaggerChooser.this.taggerFileSelections[i]);
      }
    }
  }

  /** */
  private static final long serialVersionUID = -6936560852038774206L;

  private static AttributeTaggerChooser attributeTaggerWindowInstance;

  private static final Logger LOGGER = Logger.getLogger(AttributeTaggerChooser.class);


  /** List of AttributeTaggerSolution files */
  private JList taggerSolutionList;


  /** List model for AttributeTaggerSolution files */
  private DefaultListModel taggerFileListModel;

  /** List of Text file absolute path */
  private List<String> taggerFilesPathList;

  private HashMap<ContextType, AttributeTaggerSolution> taggerMap = new HashMap<>();

  /** List of selected Tagger files */
  private JList taggerFileSelList;

  /** Indices of selected Tagger files */
  private int taggerFileSelections[];

  private RaptatAnnotationSolution fullRaptatSolution;

  private File solutionFile = null;

  protected boolean savedToFile = false;

  /** */
  private AttributeTaggerChooser(Window parent) {
    super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
    AttributeTaggerChooser.LOGGER.setLevel(Level.INFO);
  }

  private AttributeTaggerChooser(Window parent, RaptatAnnotationSolution mainSolution,
      File solutionFile, Dimension dim) {
    this(parent);
    Point point;
    if (parent != null) {
      int x = parent.getX() + (parent.getWidth() - getWidth()) / 2;
      int y = parent.getY() + (parent.getHeight() - getHeight()) / 2;
      point = new Point(x, y);
    } else {
      point = new Point(100, 100);
    }

    this.taggerSolutionList = new JList();
    this.taggerFileListModel = new DefaultListModel();
    this.taggerFilesPathList = new ArrayList<>();
    this.taggerFileSelList = new JList();
    this.taggerFileSelections = new int[10];
    this.fullRaptatSolution = mainSolution;
    this.solutionFile = solutionFile;

    initializeWindow(point, dim);
  }


  /**
   * Creates the text file list panel.
   *
   * @param panelSize Dimension of the panel
   * @return A JPanel for the text file list.
   * @author Glenn Gobbel, March 29, 2016
   */
  public JPanel buildTaggerListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.taggerSolutionList = new JList(this.taggerFileListModel);
    this.taggerSolutionList.setBorder(new TitledBorder(null, "AttributeTagger Solution Files",
        TitledBorder.LEADING, TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.taggerSolutionList);

    ListSelectionListener selectionListener = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          AttributeTaggerChooser.this.taggerFileSelList = (JList) listSelectionEvent.getSource();
          AttributeTaggerChooser.this.taggerFileSelections =
              AttributeTaggerChooser.this.taggerFileSelList.getSelectedIndices();
        }
      }
    };

    this.taggerSolutionList.addListSelectionListener(selectionListener);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  /** @return the taggerMap */
  public Collection<AttributeTaggerSolution> getTaggers() {
    Collection<AttributeTaggerSolution> taggers = this.taggerMap.values();

    return taggers == null ? new ArrayList<>() : taggers;
  }


  /**
   * Used to determine if an instance of this class has already saved a solution to a file so that
   * saving is no longer necessary.
   *
   * @return the savedToFile
   */
  public boolean isSavedToFile() {
    return this.savedToFile;
  }


  /**
   * Creates the components for the run panel.
   *
   * @return A JPanel with the components for the run panel.
   * @author Sanjib Saha, July 15, 2014
   */
  private Component getSaveOrCancelPanel() {
    Box runPanel = Box.createVerticalBox();

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton saveButon = new JButton("Save");
    saveButon.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        AttributeTaggerChooser.this.taggerMap =
            new HashMap<>(AttributeTaggerChooser.this.taggerFilesPathList.size());
        for (String curFilePath : AttributeTaggerChooser.this.taggerFilesPathList) {
          AttributeTaggerSolution curSolution =
              AttributeTaggerSolution.loadFromFile(new File(curFilePath));
          AttributeTaggerChooser.this.taggerMap.put(curSolution.getContext(), curSolution);
        }

        if (AttributeTaggerChooser.this.fullRaptatSolution != null) {
          if (AttributeTaggerChooser.this.fullRaptatSolution
              .validateProcessingOptions(AttributeTaggerChooser.this.taggerMap)) {
            AttributeTaggerChooser.this.fullRaptatSolution
                .setAttributeTaggers(AttributeTaggerChooser.this.taggerMap);
            if (AttributeTaggerChooser.this.solutionFile == null) {
              AttributeTaggerChooser.this.solutionFile = getSolutionFile();
            }

            if (AttributeTaggerChooser.this.solutionFile != null) {
              AttributeTaggerChooser.this.fullRaptatSolution
                  .saveToFile(AttributeTaggerChooser.this.solutionFile);
              AttributeTaggerChooser.this.savedToFile = true;
              AttributeTaggerChooser.LOGGER.info("RaptatAnnotationSolution instance saved to file");
              String message = "The Raptat Trained solution file has been saved to file";
              JOptionPane.showMessageDialog(new JFrame(), message, "Training Completed",
                  JOptionPane.INFORMATION_MESSAGE);
            }
          } else {
            GeneralHelper.errorWriter(
                "Token processing options are not consistent across all solution files");
          }
        }
        /*
         * This dialog box is used to validate attribute taggers used for cross-validation which do
         * not have a single RaptatAnnotationSolution instance so this 'else if' block is called
         * instead
         */
        else if (!validateProcessingOptions(AttributeTaggerChooser.this.taggerMap)) {
          GeneralHelper
              .errorWriter("Token processing options are not consistent across all solution files");
        }

        AttributeTaggerChooser.this.dispose();
      }


      private File getSolutionFile() {
        File solutionFile = null;
        JFileChooser saveLocChooser = new JFileChooser();
        saveLocChooser.setDialogTitle("Enter name for solution file");
        OptionsManager theOptions = OptionsManager.getInstance();

        if (theOptions.getLastSelectedDir() != null) {
          saveLocChooser.setCurrentDirectory(theOptions.getLastSelectedDir());
        }

        int returnVal = saveLocChooser.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

          solutionFile = saveLocChooser.getSelectedFile().getAbsoluteFile();
          solutionFile = GeneralHelper.correctFileName(solutionFile);
        }

        return solutionFile;
      }


      private boolean validateProcessingOptions(
          HashMap<ContextType, AttributeTaggerSolution> taggerMap) {
        if (taggerMap != null & taggerMap.size() > 1) {
          Iterator<AttributeTaggerSolution> taggerIterator = taggerMap.values().iterator();
          TokenProcessingOptions tsOptions = taggerIterator.next().getTokenProcessingOptions();

          while (taggerIterator.hasNext()) {
            if (!tsOptions.equals(taggerIterator.next().getTokenProcessingOptions())) {
              return false;
            }
          }
        }

        return true;
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        AttributeTaggerChooser.this.dispose();
      }
    });

    buttonPanel.add(Box.createRigidArea(new Dimension(5, 35)));
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 35)));
    buttonPanel.add(saveButon);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 35)));
    runPanel.add(buttonPanel);
    runPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    return runPanel;
  }


  /** */
  private void initializeWindow(Point position, Dimension dim) {
    setTitle("Add or Remove Attribute Tagger Solutions");
    int windowWidth = 0;
    int windowHeight = 0;
    if (dim != null) {
      windowWidth = (int) dim.getWidth() / 2;
      windowHeight = (int) dim.getHeight() * 2 / 3;
    }
    windowWidth = windowWidth < RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        ? RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
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
        AttributeTaggerChooser.this.dispose();
      }
    });
    JPanel listPanel = new JPanel();
    listPanel.setPreferredSize(new Dimension(windowWidth, (int) (0.6 * windowHeight)));
    Dimension taggerListDimensions = new Dimension((int) (0.8 * listPanel.getPreferredSize().width),
        (int) (0.9 * listPanel.getPreferredSize().height));

    listPanel.add(buildTaggerListPanel(taggerListDimensions), BorderLayout.CENTER);
    this.add(listPanel, BorderLayout.NORTH);

    JPanel addRemoveButtonPanel =
        getAddRemoveButtonPanel(new AddTaggerFileListener(), new RemoveTextFileListener(), "Text",
            new Dimension((int) (0.8 * taggerListDimensions.width), 30));
    JPanel addRemoveButtonPanelContainer = new JPanel();
    addRemoveButtonPanelContainer.add(Box.createRigidArea(new Dimension(20, 0)), BorderLayout.WEST);
    addRemoveButtonPanelContainer.add(addRemoveButtonPanel, BorderLayout.CENTER);
    this.add(addRemoveButtonPanelContainer, BorderLayout.CENTER);

    JPanel cancelSavePanel = new JPanel();
    cancelSavePanel.add(Box.createRigidArea(new Dimension(140, 0)), BorderLayout.WEST);
    cancelSavePanel.add(getSaveOrCancelPanel(), BorderLayout.EAST);
    this.add(cancelSavePanel, BorderLayout.SOUTH);

    setVisible(true);
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    pack();
  }


  /**
   * Returns an instance of the AttributeTaggerWindow class. * @param mainSolution
   *
   * @param parent
   * @param mainSolution
   * @param solutionFile
   * @param thePoint Upper-left coordinate of the window
   * @param theDimension Dimension of the window
   * @return An instance of the AttributeTaggerWindow class
   */
  public static synchronized AttributeTaggerChooser getInstance(Window parent,
      RaptatAnnotationSolution mainSolution, File solutionFile, Dimension theDimension) {
    if (AttributeTaggerChooser.attributeTaggerWindowInstance == null) {
      AttributeTaggerChooser.attributeTaggerWindowInstance =
          new AttributeTaggerChooser(parent, mainSolution, solutionFile, theDimension);
    } else {
      AttributeTaggerChooser.attributeTaggerWindowInstance.setVisible(true);
    }
    return AttributeTaggerChooser.attributeTaggerWindowInstance;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        AttributeTaggerChooser.getInstance(null, null, null, new Dimension(0, 0));

        System.exit(0);
      }
    });
  }
}
