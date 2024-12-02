/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.ss.sql.engine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.WindowHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.PhraseTokenIntegrator;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;

/** @author VHATVHSAHAS1 */
@SuppressWarnings("rawtypes")
public class AnnotationDBWindow extends JDialog {

  protected class AddReferenceXMLFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the Reference XML file location.
     *
     * @param ev Action Event for the listener
     */
    @SuppressWarnings("unchecked")
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
              && !AnnotationDBWindow.this.xmlListModel.contains(selectedFile.getName())) {
            AnnotationDBWindow.this.xmlListModel.addElement(selectedFile.getName());
            AnnotationDBWindow.this.xmlFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !AnnotationDBWindow.this.xmlListModel.contains(dirFile.getName())) {
                AnnotationDBWindow.this.xmlListModel.addElement(dirFile.getName());
                AnnotationDBWindow.this.xmlFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  protected class AddTextFileListener implements ActionListener {

    /**
     * Invokes a File chooser to select the Text file location.
     *
     * @param ev Action Event for the listener
     */
    @SuppressWarnings("unchecked")
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
              && !AnnotationDBWindow.this.textFileListModel.contains(selectedFile.getName())) {
            AnnotationDBWindow.this.textFileListModel.addElement(selectedFile.getName());
            AnnotationDBWindow.this.textFilesPathList.add(selectedFile.getAbsolutePath());
          } else if (selectedFile.isDirectory()) {
            dirFiles = selectedFile.listFiles();

            for (File dirFile : dirFiles) {
              if (WindowHelper.validFile(dirFile)
                  && !AnnotationDBWindow.this.textFileListModel.contains(dirFile.getName())) {
                AnnotationDBWindow.this.textFileListModel.addElement(dirFile.getName());
                AnnotationDBWindow.this.textFilesPathList.add(dirFile.getAbsolutePath());
              }
            }
          }
        }
      }
    }
  }

  protected class GetFileMapListerner implements ActionListener {

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
      fileChooser.setFileFilter(new FileNameExtensionFilter("Select File Map", "xlsx"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        AnnotationDBWindow.this.fileMapFile = fileChooser.getSelectedFile();
        AnnotationDBWindow.this.fileMapPath
            .setText(AnnotationDBWindow.this.fileMapFile.getAbsolutePath());

        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
      }
    }
  }

  protected class GetSchemaFileListerner implements ActionListener {

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
      fileChooser.setFileFilter(new FileNameExtensionFilter("Select Schema File", "xml"));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        AnnotationDBWindow.this.schemaFile = fileChooser.getSelectedFile();
        AnnotationDBWindow.this.schemaFilePath
            .setText(AnnotationDBWindow.this.schemaFile.getAbsolutePath());

        OptionsManager.getInstance().setLastSelectedDir(fileChooser.getCurrentDirectory());
      }
    }
  }

  protected class RemoveReferenceXMLFileListener implements ActionListener {

    /**
     * Removes the selected Reference XML files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = AnnotationDBWindow.this.xmlSelections.length - 1; i >= 0; i--) {
        AnnotationDBWindow.this.xmlFilesPathList.remove(AnnotationDBWindow.this.xmlSelections[i]);
        AnnotationDBWindow.this.xmlListModel
            .removeElementAt(AnnotationDBWindow.this.xmlSelections[i]);
      }
    }
  }

  protected class RemoveTextFileListener implements ActionListener {

    /**
     * Removes the selected Text files.
     *
     * @param e Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = AnnotationDBWindow.this.textFileSelections.length - 1; i >= 0; i--) {
        AnnotationDBWindow.this.textFilesPathList
            .remove(AnnotationDBWindow.this.textFileSelections[i]);
        AnnotationDBWindow.this.textFileListModel
            .removeElementAt(AnnotationDBWindow.this.textFileSelections[i]);
      }
    }
  }

  private class LoginListener implements ActionListener {
    Point position;
    Dimension dimension;


    public LoginListener(Point p, Dimension d) {
      this.position = p;
      this.dimension = d;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
      String userName = AnnotationDBWindow.this.userField.getText();
      String password = new String(AnnotationDBWindow.this.passField.getPassword());
      String dbName = AnnotationDBWindow.this.dbNameField.getText();
      String serverName = AnnotationDBWindow.this.serverNameField.getText();

      if (serverName.equals("")) {
        JOptionPane.showMessageDialog(null, "Server Name field can not be empty.", "Error",
            JOptionPane.OK_OPTION);
        return;
      }

      if (dbName.equals("")) {
        JOptionPane.showMessageDialog(null, "Database Name field can not be empty.", "Error",
            JOptionPane.OK_OPTION);
        return;
      }

      SQLDriver driver = new SQLDriver(userName, password);
      driver.setServerAndDB(serverName, dbName);

      Connection connect = SQLDriver.connect();
      if (connect == null) {
        JOptionPane.showMessageDialog(null, "Error login. Check all info.", "Error",
            JOptionPane.OK_OPTION);
      } else {
        SQLDriver.closeConnection(connect);
        AnnotationDBWindow.this.remove(AnnotationDBWindow.this.loginPanel);
        // initializeUpload( this.position, this.dimension );
        initializeSchemaName(this.position, this.dimension, dbName);
        validate();
        AnnotationDBWindow.this.repaint();
      }
    }
  }

  private class SelectListener implements ActionListener {
    Point position;
    Dimension dimension;
    String dbName;


    public SelectListener(Point p, Dimension d, String dbName) {
      this.position = p;
      this.dimension = d;
      this.dbName = dbName;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
      String schemaName = AnnotationDBWindow.this.schemaNameField.getText();

      Connection connect = SQLDriver.connect();
      if (connect == null) {
        JOptionPane.showMessageDialog(null, "Error login. Check all info.", "Error",
            JOptionPane.OK_OPTION);
      } else {
        try {
          if (SQLDriver.tablesExists(this.dbName, schemaName)) {
            SQLDriver.closeConnection(connect);
            AnnotationDBWindow.this.remove(AnnotationDBWindow.this.prefixPanel);
            initializeUpload(this.position, this.dimension, schemaName);
            validate();
            AnnotationDBWindow.this.repaint();
          } else {
            JOptionPane.showMessageDialog(null, "This schema does not exist.", "Error",
                JOptionPane.OK_OPTION);
          }
        } catch (NoConnectionException e1) {
          JOptionPane.showMessageDialog(null, "Username and password does not match.", "Error",
              JOptionPane.OK_OPTION);
          e1.printStackTrace();
        }
      }
    }
  }

  private class UploadAnnotationListener implements ActionListener {
    String _schemaName;


    public UploadAnnotationListener(String prefix) {
      this._schemaName = prefix;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
      if (AnnotationDBWindow.this.schemaFile == null) {
        JOptionPane.showMessageDialog(null, "Schema File is not Selected.", "File selection error",
            JOptionPane.OK_OPTION);
        return;
      }

      if (AnnotationDBWindow.this.fileMapPath == null) {
        JOptionPane.showMessageDialog(null, "File-Map File is not Selected.",
            "File selection error", JOptionPane.OK_OPTION);
        return;
      }

      AnnotationXMLParser xmlParser = new AnnotationXMLParser();
      SchemaParser sParser = new SchemaParser(this._schemaName);
      String schemaSource =
          AnnotationDBWindow.this.comboBoxScoringMode.getSelectedItem().toString();

      long schemaID = sParser.parseSchema(AnnotationDBWindow.this.schemaFile, schemaSource);
      List<List<AnnotatedPhrase>> phraseList = checkTextFilesWithXML();
      HashMap<String, Long> fileRefMap =
          XLSXReader.getFileReferenceNoMap(AnnotationDBWindow.this.fileMapPath.getText());
      String[] docPath;
      String docName;
      for (int i = 0; i < AnnotationDBWindow.this.xmlFilesPathList.size(); i++) {
        docPath = AnnotationDBWindow.this.xmlFilesPathList.get(i).split("\\\\");
        docName = docPath[docPath.length - 1].split("\\.")[0];

        xmlParser.uploadAnnotations(schemaID, phraseList.get(i),
            AnnotationDBWindow.this.xmlFilesPathList.get(i), fileRefMap.get(docName),
            this._schemaName);
        System.err.println("uploaded file: " + i);
      }

      JOptionPane.showMessageDialog(null, "Annotations uploaded to Database.", "DB message",
          JOptionPane.OK_OPTION);
    }
  }

  /** */
  private static final long serialVersionUID = -4702261440738050638L;

  public static AnnotationDBWindow windowInstance = null;


  protected DefaultListModel xmlListModel;


  protected DefaultListModel textFileListModel;

  protected JList xmlList;
  protected JList textFileList;
  protected List<String> xmlFilesPathList;

  protected List<String> textFilesPathList;
  protected JTextField schemaFilePath;
  protected JTextField fileMapPath;
  private JComboBox comboBoxScoringMode;
  protected int[] xmlSelections;

  protected int[] textFileSelections;

  protected File schemaFile = null;

  protected File fileMapFile = null;

  private JTextField dbNameField;

  private JTextField serverNameField;

  private JTextField schemaNameField;

  private JTextField userField;

  private JPasswordField passField;

  private JPanel loginPanel;

  private JPanel prefixPanel;

  public AnnotationDBWindow() {}

  private AnnotationDBWindow(Point thePoint, Dimension theDimension) {
    this.xmlFilesPathList = new ArrayList<>();
    this.textFilesPathList = new ArrayList<>();

    this.xmlListModel = new DefaultListModel();
    this.textFileListModel = new DefaultListModel();

    // initializeUpload(thePoint, theDimension);
    initializeLogin(thePoint, theDimension);
  }


  public void testProg() {
    AnnotationXMLParser xmlParser = new AnnotationXMLParser();
    this.xmlFilesPathList = new ArrayList<>();
    this.textFilesPathList = new ArrayList<>();

    // String path =
    // "D:\\CARTCL-IIR\\ActiveLearning\\Sanjib\\AdjudicatedAKI_Blocks_2_to_14_Copy\\AKI_Block";

    this.schemaFile = new File("P:\\workspace\\LEO_Raptat\\data\\New folder\\projectschema.xml");

    // xmlFilesPathList.add("D:\\CARTCL-IIR\\ActiveLearning\\Sanjib\\AdjudicatedAKI_Blocks_2_to_14_Copy"
    // +
    // "\\AKI_Block5_Fern\\adjudication\\Block_05_Note_004.txt.knowtator.xml");
    // textFilesPathList.add("D:\\CARTCL-IIR\\ActiveLearning\\Sanjib\\AdjudicatedAKI_Blocks_2_to_14_Copy\\"
    // + "AKI_Block5_Fern\\corpus\\Block_05_Note_004.txt");

    SchemaParser sParser = new SchemaParser("");
    long schemaID;
    List<List<AnnotatedPhrase>> phraseList;
    HashMap<String, Long> fileRefMap = XLSXReader.getFileReferenceNoMap(
        "P:\\workspace\\LEO_Raptat\\data\\New folder\\AnnotationID_TIUdocumentSID Correspondence.xlsx");

    String[] docPath;
    String docName;

    for (int j = 2; j < 14; j++) {
      // docName = path + j + "_Fern\\";
      docName = "D:\\CARTCL-IIR\\ActiveLearning\\Sanjib\\AdjudicatedAKI_Block_01_Copy\\";

      // schemaFile = new File(path + j + "_Fern\\" +
      // "config\\projectschema.xml");
      this.schemaFile = new File(docName + "config\\projectschema.xml");
      schemaID = sParser.parseSchema(this.schemaFile, "eHost");

      this.xmlFilesPathList.clear();
      for (File file : Arrays.asList(new File(docName + "saved\\").listFiles())) {
        this.xmlFilesPathList.add(file.getAbsolutePath());
      }

      this.textFilesPathList.clear();
      for (File file : Arrays.asList(new File(docName + "corpus\\").listFiles())) {
        this.textFilesPathList.add(file.getAbsolutePath());
      }

      phraseList = checkTextFilesWithXML();

      for (int i = 0; i < this.xmlFilesPathList.size(); i++) {
        docPath = this.xmlFilesPathList.get(i).split("\\\\");
        docName = docPath[docPath.length - 1].split("\\.")[0];

        xmlParser.uploadAnnotations(schemaID, phraseList.get(i), this.xmlFilesPathList.get(i),
            fileRefMap.get(docName), "");
        System.err.println("uploaded file: " + i);
      }
    }
  }


  @SuppressWarnings("unchecked")
  protected JPanel buildTextFileListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.textFileList = new JList(this.textFileListModel);
    this.textFileList.setBorder(
        new TitledBorder(null, "Text Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.textFileList);

    ListSelectionListener ListSelLis = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          AnnotationDBWindow.this.textFileList = (JList) listSelectionEvent.getSource();
          AnnotationDBWindow.this.textFileSelections =
              AnnotationDBWindow.this.textFileList.getSelectedIndices();
        }
      }
    };

    this.textFileList.addListSelectionListener(ListSelLis);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  @SuppressWarnings("unchecked")
  protected JPanel buildXMLListPanel(Dimension panelSize) {
    JScrollPane scrollPane = new JScrollPane();

    this.xmlList = new JList(this.xmlListModel);
    this.xmlList.setBorder(
        new TitledBorder(null, "XML Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));

    scrollPane.setViewportView(this.xmlList);

    ListSelectionListener ListSelLis = new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()) {
          AnnotationDBWindow.this.xmlList = (JList) listSelectionEvent.getSource();
          AnnotationDBWindow.this.xmlSelections =
              AnnotationDBWindow.this.xmlList.getSelectedIndices();
        }
      }
    };

    this.xmlList.addListSelectionListener(ListSelLis);

    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    scrollPane.setPreferredSize(panelSize);

    JPanel thePanel = new JPanel();
    thePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    thePanel.add(scrollPane);

    return thePanel;
  }


  protected JPanel getAddRemoveButtonPanel(ActionListener addFileListener,
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


  @SuppressWarnings("unchecked")
  private JPanel buildSchemaPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout());

    JPanel schemaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
    JButton selectSchemaButton = new JButton("<html><center>Select<br>Schema File</center></html>");
    selectSchemaButton.addActionListener(new GetSchemaFileListerner());
    schemaPanel.add(selectSchemaButton);

    this.schemaFilePath = new JTextField(30);
    this.schemaFilePath.setEditable(false);
    this.schemaFilePath.setText("Schema file path.");
    this.schemaFilePath.setHorizontalAlignment(SwingConstants.LEFT);
    schemaPanel.add(this.schemaFilePath);

    this.comboBoxScoringMode = new JComboBox();
    this.comboBoxScoringMode.setPreferredSize(new Dimension(150, 40));
    Border b2 = BorderFactory.createTitledBorder("Schema Source");
    this.comboBoxScoringMode.setBorder(b2);
    this.comboBoxScoringMode.addItem("eHost");
    this.comboBoxScoringMode.addItem("Knowtator");
    schemaPanel.add(this.comboBoxScoringMode);
    mainPanel.add(schemaPanel, BorderLayout.CENTER);

    JPanel fileMapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
    JButton selectFileMapButton =
        new JButton("<html><center>Select<br>File-Map File</center></html>");
    selectFileMapButton.addActionListener(new GetFileMapListerner());
    fileMapPanel.add(selectFileMapButton);

    this.fileMapPath = new JTextField(30);
    this.fileMapPath.setEditable(false);
    this.fileMapPath.setText("File map file path.");
    this.fileMapPath.setHorizontalAlignment(SwingConstants.LEFT);
    fileMapPanel.add(this.fileMapPath);
    mainPanel.add(fileMapPanel, BorderLayout.SOUTH);

    Font borderFont =
        new Font(schemaPanel.getFont().getFontName(), Font.ITALIC, schemaPanel.getFont().getSize());
    Border b3 = BorderFactory.createTitledBorder(
        BorderFactory.createBevelBorder(BevelBorder.RAISED), "Select configurations files",
        TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont);
    mainPanel.setBorder(b3);

    return mainPanel;
  }


  private List<List<AnnotatedPhrase>> checkTextFilesWithXML() {
    RaptatDocument doc;
    TextAnalyzer ta = new TextAnalyzer();
    PhraseTokenIntegrator integrator = new PhraseTokenIntegrator();

    List<AnnotatedPhrase> phrases;
    List<SchemaConcept> schemaConcepts = SchemaImporter.importSchemaConcepts(this.schemaFile);
    AnnotationImporter annotationImporter = new AnnotationImporter(SchemaImporter.getSchemaApp());

    List<List<AnnotatedPhrase>> phraseList = new ArrayList<>();

    for (int i = 0; i < this.xmlFilesPathList.size(); i++) {
      phrases = annotationImporter.importAnnotations(this.xmlFilesPathList.get(i),
          this.textFilesPathList.get(i), null, schemaConcepts);

      doc = ta.processDocument(this.textFilesPathList.get(i));
      integrator.addProcessedTokensToAnnotations(phrases, doc, false, 0);

      phraseList.add(phrases);
    }

    return phraseList;
  }


  private JPanel getOptionsPanelLogin() {
    JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));

    Border b1 = BorderFactory.createTitledBorder("Server Name");
    this.serverNameField = new JTextField(20);
    this.serverNameField.setBorder(b1);
    this.serverNameField.setHorizontalAlignment(SwingConstants.LEFT);
    dbPanel.add(this.serverNameField);

    Border b3 = BorderFactory.createTitledBorder("Database Name");
    this.dbNameField = new JTextField(20);
    this.dbNameField.setBorder(b3);
    this.dbNameField.setHorizontalAlignment(SwingConstants.LEFT);
    dbPanel.add(this.dbNameField);

    Font borderFont =
        new Font(dbPanel.getFont().getFontName(), Font.ITALIC, dbPanel.getFont().getSize());
    Border b4 =
        BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
            "Database", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont);
    dbPanel.setBorder(b4);

    return dbPanel;
  }


  private Component getRunPanel(String prefix) {
    Box runPanel = Box.createVerticalBox();
    runPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton runButton = new JButton("Upload Annotations");
    runButton.addActionListener(new UploadAnnotationListener(prefix));

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        try {
          AnnotationDBWindow.windowInstance.dispose();
          AnnotationDBWindow.windowInstance = null;
        } catch (Throwable ex) {
          Logger.getLogger(AnnotationDBWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    });

    buttonPanel.add(runButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    buttonPanel.add(cancelButton);
    runPanel.add(buttonPanel);

    return runPanel;
  }


  private Component getRunPanelLogin(Point thePosition, Dimension theDimension) {
    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton loginButton = new JButton("Login");
    JButton cancelButton = new JButton("Cancel");

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        AnnotationDBWindow.windowInstance.dispose();
        AnnotationDBWindow.windowInstance = null;
      }
    });

    buttonPanel.add(loginButton);
    loginButton.addActionListener(new LoginListener(thePosition, theDimension));

    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    buttonPanel.add(cancelButton);

    return buttonPanel;
  }


  private JPanel getUserPassPanelLogin() {
    JPanel userDetailPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));

    Border brdr = BorderFactory.createTitledBorder("User Name");
    this.userField = new JTextField(20);
    this.userField.setBorder(brdr);
    this.userField.setHorizontalAlignment(SwingConstants.LEFT);
    userDetailPanel.add(this.userField);

    brdr = BorderFactory.createTitledBorder("Password");
    this.passField = new JPasswordField(20);
    this.passField.setBorder(brdr);
    this.passField.setHorizontalAlignment(SwingConstants.LEFT);
    userDetailPanel.add(this.passField);

    Font borderFont = new Font(userDetailPanel.getFont().getFontName(), Font.ITALIC,
        userDetailPanel.getFont().getSize());
    Border b2 =
        BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
            "Admin Credentials", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont);
    userDetailPanel.setBorder(b2);

    return userDetailPanel;
  }


  private void initializeLogin(Point thePosition, Dimension theDimension) {
    int windowWidth = (int) (theDimension.width * 1.0);
    int windowHeight = (int) (theDimension.height * 0.8);

    windowWidth = windowWidth < RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        ? RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        ? RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        : windowHeight;

    this.setSize(windowWidth, windowHeight);
    setTitle("Database Login");
    setResizable(false);
    thePosition.translate(10, 10);

    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        AnnotationDBWindow.windowInstance.dispose();
        AnnotationDBWindow.windowInstance = null;
      }
    });

    this.loginPanel = new JPanel(new BorderLayout());
    this.loginPanel.add(getOptionsPanelLogin(), BorderLayout.NORTH);
    this.loginPanel.add(getUserPassPanelLogin(), BorderLayout.CENTER);
    this.loginPanel.add(getRunPanelLogin(thePosition, theDimension), BorderLayout.SOUTH);

    this.add(this.loginPanel);
    setVisible(true);
    pack();
  }


  private void initializeSchemaName(Point thePosition, Dimension theDimension, String dbName) {
    int windowWidth = (int) (theDimension.width * 0.7);
    int windowHeight = (int) (theDimension.height * 0.8);

    windowWidth = windowWidth < RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        ? RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        ? RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        : windowHeight;

    thePosition.translate(10, 10);
    this.setSize(windowWidth, windowHeight);
    setTitle("Table Prefix");
    setResizable(false);
    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        AnnotationDBWindow.windowInstance.dispose();
        AnnotationDBWindow.windowInstance = null;
      }
    });

    this.prefixPanel = new JPanel(new BorderLayout());

    JPanel inputPanel = new JPanel(new FlowLayout());
    Border b1 = BorderFactory.createTitledBorder("Schema Name");
    this.schemaNameField = new JTextField(20);
    this.schemaNameField.setBorder(b1);
    this.schemaNameField.setHorizontalAlignment(SwingConstants.LEFT);
    inputPanel.add(this.schemaNameField);

    JCheckBox chkbox = new JCheckBox("No Prefix");
    chkbox.setSelected(false);
    chkbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();

        if (selected) {
          AnnotationDBWindow.this.schemaNameField.setText("");
          AnnotationDBWindow.this.schemaNameField.setEditable(false);
        } else {
          AnnotationDBWindow.this.schemaNameField.setEditable(true);
        }
      }
    });
    inputPanel.add(chkbox);
    this.prefixPanel.add(inputPanel, BorderLayout.NORTH);

    JPanel selectPanel = new JPanel(new FlowLayout());
    JButton selectButton = new JButton("Select");
    JButton cancelButton = new JButton("Cancel");

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        AnnotationDBWindow.windowInstance.dispose();
        AnnotationDBWindow.windowInstance = null;
      }
    });

    selectPanel.add(selectButton);
    selectButton.addActionListener(new SelectListener(thePosition, theDimension, dbName));

    selectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    selectPanel.add(cancelButton);
    this.prefixPanel.add(selectPanel, BorderLayout.SOUTH);

    this.add(this.prefixPanel);
    setVisible(true);
    pack();
  }


  private void initializeUpload(Point thePosition, Dimension theDimension, String prefix) {
    int windowWidth = (int) (theDimension.width * 0.7);
    int windowHeight = (int) (theDimension.height * 0.8);

    windowWidth = windowWidth < RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        ? RaptatConstants.TRAIN_WINDOW_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        ? RaptatConstants.TRAIN_WINDOW_MIN_HEIGHT
        : windowHeight;

    this.setSize(windowWidth, windowHeight);
    setTitle("Upload Annotations");
    setResizable(false);
    thePosition.translate(10, 10);

    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        try {
          AnnotationDBWindow.windowInstance.dispose();
          AnnotationDBWindow.windowInstance = null;
          finalize();
        } catch (Throwable ex) {
          Logger.getLogger(AnnotationDBWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
      };
    });

    // Creates the Box
    Box listPanel = Box.createHorizontalBox();

    JPanel listButtonPanel = new JPanel(new GridLayout(1, 2));
    JPanel generalButtonPanel = new JPanel(new BorderLayout());
    JPanel optionsRunPanel = new JPanel(new GridLayout(1, 2));

    listPanel.setPreferredSize(new Dimension(windowWidth, (int) (windowHeight * 5.0 / 8)));

    // Adding the box for file lists
    setLayout(new BorderLayout());
    this.add(listPanel, BorderLayout.NORTH);

    Dimension panelSize = new Dimension((int) (0.35 * listPanel.getPreferredSize().width),
        listPanel.getPreferredSize().height);

    listPanel.add(buildXMLListPanel(panelSize));
    listPanel.add(buildTextFileListPanel(panelSize));

    // Adding the add and remove file buttons
    this.add(generalButtonPanel, BorderLayout.SOUTH);
    generalButtonPanel.add(listButtonPanel, BorderLayout.NORTH);

    panelSize = new Dimension((int) (0.30 * listPanel.getPreferredSize().width),
        (int) (0.10 * listPanel.getPreferredSize().height));

    JPanel refXMLButtonsPanel = new JPanel(new FlowLayout());
    refXMLButtonsPanel.add(getAddRemoveButtonPanel(new AddReferenceXMLFileListener(),
        new RemoveReferenceXMLFileListener(), "Reference XML", panelSize));
    listButtonPanel.add(refXMLButtonsPanel);

    JPanel textButtonsPanel = new JPanel(new FlowLayout());
    textButtonsPanel.add(getAddRemoveButtonPanel(new AddTextFileListener(),
        new RemoveTextFileListener(), "Text", panelSize));
    listButtonPanel.add(textButtonsPanel);

    // Adding Upload button
    optionsRunPanel.add(getRunPanel(prefix));
    optionsRunPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createLineBorder(Color.GRAY)));

    generalButtonPanel.add(Box.createRigidArea(new Dimension(0, 40)));
    generalButtonPanel.add(buildSchemaPanel(), BorderLayout.CENTER);
    generalButtonPanel.add(optionsRunPanel, BorderLayout.SOUTH);
    setVisible(true);
    pack();
  }


  public static synchronized AnnotationDBWindow getInstance(Point thePoint,
      Dimension theDimension) {
    if (AnnotationDBWindow.windowInstance == null) {
      AnnotationDBWindow.windowInstance = new AnnotationDBWindow(thePoint, theDimension);
    }

    return AnnotationDBWindow.windowInstance;
  }


  public static void main(String[] args) {
    AnnotationDBWindow.getInstance(new Point(100, 100), new Dimension(600, 800));

    // AnnotationDBWindow w = new AnnotationDBWindow();
    // w.testProg();
    // w.test2();
  }
}
