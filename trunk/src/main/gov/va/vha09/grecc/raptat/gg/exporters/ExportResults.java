package src.main.gov.va.vha09.grecc.raptat.gg.exporters;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class ExportResults {
  /**
   * ****************************************************** Action listener used as part of
   * ExportResults class for saving and exporting a set of annotations to eHOST XML
   *
   * @author Glenn Gobbel - Jun 4, 2012 *****************************************************
   */
  private class SaveAndExportListener implements ActionListener {

    /**
     * *********************************************************** OVERRIDES PARENT METHOD
     *
     * @param arg0
     * @author Glenn Gobbel - Jun 4, 2012
     *         ***********************************************************
     */
    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent arg0) {
      String pathToOriginalTextSource;
      ObjectInputStream resultInStream;
      List<AnnotatedPhrase> raptatAnnotations;

      if ((ExportResults.this.annotationSavelocation =
          GeneralHelper.getDirectory("Choose directory" + " for writing results")) != null) {
        ExportResults.this.annotationSavelocation.setWritable(true);
        System.out.println(ExportResults.this.annotationSavelocation.getAbsolutePath());
        try {
          // getting the annotations by deserialization
          System.out.println("Deserializing and Exporting Raptat Annotations to eHOST XML");

          for (String curResultPath : ExportResults.this.resultPaths) {
            raptatAnnotations = new ArrayList<>();
            resultInStream = new ObjectInputStream(new FileInputStream(new File(curResultPath)));

            raptatAnnotations = (List<AnnotatedPhrase>) resultInStream.readObject();
            pathToOriginalTextSource = (String) resultInStream.readObject();
            resultInStream.close();

            ExportResults.this.xml_exporter = new XMLExporter(pathToOriginalTextSource);
            ExportResults.this.xml_exporter.writeAllDataObj(raptatAnnotations);
            ExportResults.this.xml_exporter
                .writeXMLFile(ExportResults.this.annotationSavelocation.getAbsolutePath());
          }
          GeneralHelper.errorWriter("Export complete");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      ExportResults.this.exportFrame.setVisible(false);
      ExportResults.this.exportFrame.dispose();
    }
  }


  public JFrame exportFrame;


  String selectedformat;

  File annotationSavelocation;
  XMLExporter xml_exporter;

  List<String> resultPaths = OptionsManager.getInstance().getAnnotationResultPaths();

  /** Create the application. */
  public ExportResults() {
    initialize();
  }

  /** Initialize the contents of the frame. */
  private void initialize() {
    this.exportFrame = new JFrame();
    this.exportFrame.setTitle("Export annotations");
    this.exportFrame.getContentPane().setBackground(new Color(192, 192, 192));
    this.exportFrame.setBounds(100, 100, 520, 259);
    this.exportFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 0};
    gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights =
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    this.exportFrame.getContentPane().setLayout(gridBagLayout);

    DefaultComboBoxModel cbxModel = new DefaultComboBoxModel();
    cbxModel.addElement("XML");
    cbxModel.addElement("CSV");
    cbxModel.addElement("UIMA");

    JLabel label = new JLabel("");
    GridBagConstraints gbc_label = new GridBagConstraints();
    gbc_label.insets = new Insets(0, 0, 5, 5);
    gbc_label.gridx = 1;
    gbc_label.gridy = 3;
    this.exportFrame.getContentPane().add(label, gbc_label);

    JLabel label_1 = new JLabel("");
    GridBagConstraints gbc_label_1 = new GridBagConstraints();
    gbc_label_1.insets = new Insets(0, 0, 5, 5);
    gbc_label_1.gridx = 1;
    gbc_label_1.gridy = 4;
    this.exportFrame.getContentPane().add(label_1, gbc_label_1);

    JLabel lblExportTheRaptat = new JLabel("Choose export format");
    lblExportTheRaptat.setFont(new Font("Cambria", Font.BOLD, 15));
    GridBagConstraints gbc_lblExportTheRaptat = new GridBagConstraints();
    gbc_lblExportTheRaptat.insets = new Insets(0, 0, 5, 5);
    gbc_lblExportTheRaptat.gridx = 1;
    gbc_lblExportTheRaptat.gridy = 5;
    this.exportFrame.getContentPane().add(lblExportTheRaptat, gbc_lblExportTheRaptat);
    JComboBox comboBox = new JComboBox(cbxModel);
    comboBox.setToolTipText("Select the format for exporting the RapTAT annotations");
    comboBox.setBackground(Color.WHITE);
    comboBox.setFont(new Font("Cambria", Font.PLAIN, 12));

    GridBagConstraints gbc_comboBox = new GridBagConstraints();
    gbc_comboBox.insets = new Insets(0, 0, 5, 5);
    gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox.gridx = 2;
    gbc_comboBox.gridy = 5;
    this.exportFrame.getContentPane().add(comboBox, gbc_comboBox);
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        ExportResults.this.selectedformat = (String) cb.getModel().getSelectedItem();
        System.out.println(ExportResults.this.selectedformat);
      }
    });

    JLabel lblProvideALocation = new JLabel("Please specify export directory");
    lblProvideALocation.setFont(new Font("Cambria", Font.BOLD, 15));
    GridBagConstraints gbc_lblProvideALocation = new GridBagConstraints();
    gbc_lblProvideALocation.insets = new Insets(0, 0, 5, 5);
    gbc_lblProvideALocation.gridx = 1;
    gbc_lblProvideALocation.gridy = 7;
    this.exportFrame.getContentPane().add(lblProvideALocation, gbc_lblProvideALocation);

    JButton btnOk = new JButton("EXPORT & SAVE");
    btnOk.addActionListener(new SaveAndExportListener());

    btnOk.setFont(new Font("Dialog", Font.BOLD, 11));
    GridBagConstraints gbc_btnOk = new GridBagConstraints();
    gbc_btnOk.insets = new Insets(0, 0, 5, 5);
    gbc_btnOk.gridx = 2;
    gbc_btnOk.gridy = 7;
    this.exportFrame.getContentPane().add(btnOk, gbc_btnOk);

    JButton btnCancel = new JButton("CLOSE");
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        ExportResults.this.exportFrame.setVisible(false);
        ExportResults.this.exportFrame.dispose();
      }
    });

    btnCancel.setFont(new Font("Dialog", Font.BOLD, 11));
    GridBagConstraints gbc_btnCancel = new GridBagConstraints();
    gbc_btnCancel.insets = new Insets(0, 0, 5, 0);
    gbc_btnCancel.gridx = 3;
    gbc_btnCancel.gridy = 7;
    this.exportFrame.getContentPane().add(btnCancel, gbc_btnCancel);
  }


  /** Launch the application. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          ExportResults window = new ExportResults();
          window.exportFrame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  public static void runExporter() {
    new ExportResults().exportFrame.setVisible(true);
  }
}
