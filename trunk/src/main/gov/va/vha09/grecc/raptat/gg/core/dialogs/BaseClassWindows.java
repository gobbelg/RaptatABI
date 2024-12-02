package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.WindowHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;

/**
 * Abstract Class providing the components and action listeners to setup the window for file
 * management for score performance window and update solution window.
 *
 * @author Sanjib Saha, created on July 17, 2014
 */
public abstract class BaseClassWindows extends JDialog {

  /**
   * Creates the action listener to acceptable concept button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class AcceptableConceptsButtonListener implements ActionListener {

    /**
     * Create the Hash Set for the concepts.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      CSVReader conceptReader = new CSVReader("Select text file with concepts to be analyzed");

      if (conceptReader.isValid()) {
        BaseClassWindows.this.acceptedConceptsSet = new HashSet<>();
        String[] nextDataSet;

        while ((nextDataSet = conceptReader.getNextData()) != null) {
          BaseClassWindows.this.acceptedConceptsSet.add(nextDataSet[0].toLowerCase());
        }
      }
    }
  }

  /**
   * Creates the action listener to add cueScope XML file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class AddCueScopeXMLFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the cueScope XML file location.
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
      fileChooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));

      int returnVal = fileChooser.showOpenDialog(null);
      File[] dirFiles;

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());

        for (File selectedFile : selectedFiles) {
          if (WindowHelper.validFile(selectedFile)
              && !BaseClassWindows.this.cueScopeXMLListModel.contains(selectedFile.getName())) {
            BaseClassWindows.this.cueScopeXMLListModel.addElement(selectedFile.getName());
            BaseClassWindows.this.cueScopeXMLFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !BaseClassWindows.this.cueScopeXMLListModel.contains(dirFile.getName())) {
                BaseClassWindows.this.cueScopeXMLListModel.addElement(dirFile.getName());
                BaseClassWindows.this.cueScopeXMLFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  /**
   * Creates the action listener to add RapTAT XML file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class AddRapTATXMLFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the RapTAT XML file location.
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
      fileChooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));

      int returnVal = fileChooser.showOpenDialog(null);
      File[] dirFiles;

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());

        for (File selectedFile : selectedFiles) {
          if (WindowHelper.validFile(selectedFile)
              && !BaseClassWindows.this.raptatXMLListModel.contains(selectedFile.getName())) {
            BaseClassWindows.this.raptatXMLListModel.addElement(selectedFile.getName());
            BaseClassWindows.this.rapTATXMLFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !BaseClassWindows.this.raptatXMLListModel.contains(dirFile.getName())) {
                BaseClassWindows.this.raptatXMLListModel.addElement(dirFile.getName());
                BaseClassWindows.this.rapTATXMLFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  /**
   * Creates the action listener to add reference XML file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class AddReferenceConceptXMLFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the Reference XML file location.
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
      fileChooser.setFileFilter(new FileNameExtensionFilter("XML files only", "xml"));

      int returnVal = fileChooser.showOpenDialog(null);
      File[] dirFiles;

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());

        for (File selectedFile : selectedFiles) {
          if (WindowHelper.validFile(selectedFile)
              && !BaseClassWindows.this.conceptXMLListModel.contains(selectedFile.getName())) {
            BaseClassWindows.this.conceptXMLListModel.addElement(selectedFile.getName());
            BaseClassWindows.this.conceptXMLFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !BaseClassWindows.this.conceptXMLListModel.contains(dirFile.getName())) {
                BaseClassWindows.this.conceptXMLListModel.addElement(dirFile.getName());
                BaseClassWindows.this.conceptXMLFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  /**
   * Creates the action listener to add text file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class AddTextFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the Text file location.
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
      fileChooser.setFileFilter(new FileNameExtensionFilter("Text files only", "txt"));

      int returnVal = fileChooser.showOpenDialog(null);
      File[] dirFiles;

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());

        for (File selectedFile : selectedFiles) {
          if (WindowHelper.validFile(selectedFile)
              && !BaseClassWindows.this.textFileListModel.contains(selectedFile.getName())) {
            BaseClassWindows.this.textFileListModel.addElement(selectedFile.getName());
            BaseClassWindows.this.textFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !BaseClassWindows.this.textFileListModel.contains(dirFile.getName())) {
                BaseClassWindows.this.textFileListModel.addElement(dirFile.getName());
                BaseClassWindows.this.textFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  public class LoadSchemaFileListener implements ActionListener {
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
      fileChooser.setFileFilter(new FileNameExtensionFilter("Select Schema File", "xml", "pont"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
        BaseClassWindows.this.schemaFile = fileChooser.getSelectedFile();
        String chosenFilePath = BaseClassWindows.this.schemaFile.getAbsolutePath();
        BaseClassWindows.this.schemaFilePath.setText(chosenFilePath);
        BaseClassWindows.this.schemaFilePath.setToolTipText(chosenFilePath);
        JButton button = (JButton) e.getSource();
        button.setBorder(BorderFactory.createLineBorder(Color.BLUE));
      }
    }
  }

  /**
   * Creates the action listener to remove concept XML file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class RemoveCueScopeXMLFileListener implements ActionListener {

    /**
     * Removes the selected Reference XML files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = BaseClassWindows.this.conceptXMLSelections.length - 1; i >= 0; i--) {
        BaseClassWindows.this.cueScopeXMLFilesPathList
            .remove(BaseClassWindows.this.cueScopeXMLSelections[i]);
        BaseClassWindows.this.cueScopeXMLListModel
            .removeElementAt(BaseClassWindows.this.cueScopeXMLSelections[i]);
      }
    }
  }

  /**
   * Creates the action listener to remove RapTAT XML file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class RemoveRapTATFileXMLListener implements ActionListener {

    /**
     * Removes the selected RapTAT XML files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = BaseClassWindows.this.rapTATXMLSelections.length - 1; i >= 0; i--) {
        BaseClassWindows.this.rapTATXMLFilesPathList
            .remove(BaseClassWindows.this.rapTATXMLSelections[i]);
        BaseClassWindows.this.raptatXMLListModel
            .removeElementAt(BaseClassWindows.this.rapTATXMLSelections[i]);
      }
    }
  }

  /**
   * Creates the action listener to remove reference XML file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class RemoveReferenceConceptXMLFileListener implements ActionListener {

    /**
     * Removes the selected Reference XML files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = BaseClassWindows.this.conceptXMLSelections.length - 1; i >= 0; i--) {
        BaseClassWindows.this.conceptXMLFilesPathList
            .remove(BaseClassWindows.this.conceptXMLSelections[i]);
        BaseClassWindows.this.conceptXMLListModel
            .removeElementAt(BaseClassWindows.this.conceptXMLSelections[i]);
      }
    }
  }

  /**
   * Creates the action listener to remove text file button. OVERRIDES PARENT METHOD
   *
   * @author Sanjib Saha, created on July 15, 2014
   */
  public class RemoveTextFileListener implements ActionListener {

    /**
     * Removes the selected Text files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = BaseClassWindows.this.textFileSelections.length - 1; i >= 0; i--) {
        BaseClassWindows.this.textFilesPathList.remove(BaseClassWindows.this.textFileSelections[i]);
        BaseClassWindows.this.textFileListModel
            .removeElementAt(BaseClassWindows.this.textFileSelections[i]);
      }
    }
  }

  /** */
  private static final long serialVersionUID = -3501812211726668796L;

  protected List<String> cueScopeXMLFilesPathList;

  /** List model for Reference XML files */
  public DefaultListModel conceptXMLListModel;

  /** List model for RapTAT XML files */
  public DefaultListModel raptatXMLListModel;

  /** List model for Text files */
  public DefaultListModel textFileListModel;

  /** List of Reference XML file absolute path */
  public List<String> conceptXMLFilesPathList;

  /** List of RapTAT XML file absolute path */
  public List<String> rapTATXMLFilesPathList;

  /** List of Text file absolute path */
  public List<String> textFilesPathList;

  /** List of Reference XML files */
  public JList conceptXMLList;

  /** List of RapTAT XML files */
  public JList rapTATXMLList;

  /** List of Text files */
  public JList textFileList;

  /** List of selected Reference XML files */
  public JList refXMLSelList;

  /** List of selected RapTAT XML files */
  public JList rapTATXMLSelList;

  /** List of selected Text files */
  public JList textFileSelList;

  /** Indices of selected Reference XML files */
  public int conceptXMLSelections[];

  /** Indices of selected RapTAT XML files */
  public int rapTATXMLSelections[];

  /** Indices of selected Text files */
  public int textFileSelections[];

  /** Set of accepted concepts */
  public HashSet<String> acceptedConceptsSet = null;

  public File schemaFile = null;

  public JTextField schemaFilePath;

  private JList cueScopeXMLFileList;

  protected DefaultListModel cueScopeXMLListModel;

  protected JList conceptXMLSelList;

  protected JList cueScopeXMLSelList;

  protected int[] cueScopeXMLSelections;


  protected BaseClassWindows() {
    super();
  }


  /**
   * @param parent
   * @param documentModal
   */
  protected BaseClassWindows(Window parent, ModalityType modalityType) {
    super(parent, modalityType);
  }


  /**
   * Creates the Reference XML file list panel.
   *
   * @param panelSize Dimension of the panel
   * @return A JPanel for the Reference XML file list.
   * @author Sanjib Saha, July 15, 2014
   */
  public JPanel buildConceptXMLListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.conceptXMLList = new JList(this.conceptXMLListModel);
    this.conceptXMLList.setBorder(new TitledBorder(null, "XML Training Files", TitledBorder.LEADING,
        TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.conceptXMLList);

    @SuppressWarnings("Convert2Lambda")
    ListSelectionListener selectionListener = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          BaseClassWindows.this.conceptXMLSelList = (JList) listSelectionEvent.getSource();
          BaseClassWindows.this.conceptXMLSelections =
              BaseClassWindows.this.conceptXMLSelList.getSelectedIndices();
        }
      }
    };

    this.conceptXMLList.addListSelectionListener(selectionListener);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  /**
   * Creates the text file list panel.
   *
   * @param panelSize Dimension of the panel
   * @return A JPanel for the text file list.
   * @author Sanjib Saha, July 15, 2014
   */
  public JPanel buildCueScopeXMLListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.cueScopeXMLFileList = new JList(this.cueScopeXMLListModel);
    this.cueScopeXMLFileList.setBorder(new TitledBorder(null, "Cue Scope XML Files",
        TitledBorder.LEADING, TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.cueScopeXMLFileList);

    @SuppressWarnings("Convert2Lambda")
    ListSelectionListener selectionListener = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          BaseClassWindows.this.cueScopeXMLSelList = (JList) listSelectionEvent.getSource();
          BaseClassWindows.this.cueScopeXMLSelections =
              BaseClassWindows.this.cueScopeXMLSelList.getSelectedIndices();
        }
      }
    };

    this.cueScopeXMLFileList.addListSelectionListener(selectionListener);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  /**
   * Creates the RapTAT XML file list panel.
   *
   * @param panelSize Dimension of the panel
   * @return A JPanel for the RapTAT XML file list.
   * @author Sanjib Saha, July 15, 2014
   */
  public JPanel buildRapTATXMLListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.rapTATXMLList = new JList(this.raptatXMLListModel);
    this.rapTATXMLList.setBorder(new TitledBorder(null, "RapTAT XML Files", TitledBorder.LEADING,
        TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.rapTATXMLList);

    @SuppressWarnings("Convert2Lambda")
    ListSelectionListener selectionListener = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          BaseClassWindows.this.rapTATXMLSelList = (JList) listSelectionEvent.getSource();
          BaseClassWindows.this.rapTATXMLSelections =
              BaseClassWindows.this.rapTATXMLSelList.getSelectedIndices();
        }
      }
    };

    this.rapTATXMLList.addListSelectionListener(selectionListener);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  /**
   * Creates the text file list panel.
   *
   * @param panelSize Dimension of the panel
   * @return A JPanel for the text file list.
   * @author Sanjib Saha, July 15, 2014
   */
  public JPanel buildTextListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.textFileList = new JList(this.textFileListModel);
    this.textFileList.setBorder(
        new TitledBorder(null, "Text Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.textFileList);

    @SuppressWarnings("Convert2Lambda")
    ListSelectionListener selectionListener = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          BaseClassWindows.this.textFileSelList = (JList) listSelectionEvent.getSource();
          BaseClassWindows.this.textFileSelections =
              BaseClassWindows.this.textFileSelList.getSelectedIndices();
        }
      }
    };

    this.textFileList.addListSelectionListener(selectionListener);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  /**
   * Creates the components for the add and remove file buttons panel.
   *
   * @param addFileListener
   * @param removeFileListener
   * @param chooseButtonString
   * @param panelSize
   * @return A JPanel with the components for the add and remove file buttons.
   * @author Sanjib Saha, July 15, 2014
   */
  public JPanel getAddRemoveButtonPanel(ActionListener addFileListener,
      ActionListener removeFileListener, String chooseButtonString, Dimension panelSize) {

    JButton chooseButton = new JButton("Add");
    chooseButton.addActionListener(addFileListener);

    JButton removeButton = new JButton("Remove");
    removeButton.addActionListener(removeFileListener);

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
}
