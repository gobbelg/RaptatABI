/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;

/** @author sahask */
public class SchemaSettingsOptionWindow extends JDialog {

  public class CellRenderer extends DefaultTableCellRenderer {

    /** */
    private static final long serialVersionUID = 2555308282774271527L;


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      Component c =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      if (column >= 1) {
        Boolean active = (Boolean) ((TableModel) table.getModel()).getValueAt(row, 1);

        if (!active) {
          c.setBackground(Color.LIGHT_GRAY);
        } else if (active) {
          c.setBackground(Color.WHITE);
        }
      } else {
        c.setBackground(Color.WHITE);
      }

      return c;
    }
  }

  class CheckBoxRenderer extends DefaultTableCellRenderer {
    /** */
    private static final long serialVersionUID = -5295457725310702930L;


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      JCheckBox checkBox = new JCheckBox();

      if (value != null) {
        checkBox.setSelected((Boolean) value);
      }

      checkBox.setHorizontalAlignment(SwingConstants.CENTER);

      if (value != null && (Boolean) value) {
        checkBox.setBackground(Color.WHITE);
      } else {
        checkBox.setBackground(Color.LIGHT_GRAY);
      }

      return checkBox;
    }
  }

  class PrintTable implements ActionListener {

    /**
     * Invokes a File chooser to select the RapTAT XML file location.
     *
     * @param ev Action Event for the listener
     */
    @Override
    public void actionPerformed(ActionEvent ev) {

      printTable(SchemaSettingsOptionWindow.this.table);
    }
  }

  // private final List <Concept> concepts;

  class TableChangeListener implements TableModelListener {

    @Override
    public void tableChanged(TableModelEvent e) {
      int row = e.getFirstRow();
      int column = e.getColumn();
      TableModel model = (TableModel) e.getSource();
      Object data = model.getValueAt(row, column);

      // CheckBox column
      if (column == 1) {
        if (!(Boolean) data) {
          model.setValueAt("", row, 2);
          model.setValueAt("", row, 3);
        } else {
          model.setValueAt("Select", row, 2);
          model.setValueAt("Select", row, 3);
        }
      }
    }
  }

  class TableModel extends AbstractTableModel {

    /** */
    private static final long serialVersionUID = -7749645479495142423L;

    private final String[] columnNames = {"Attributes", "Cue Determined", "Cue", "Scope"};

    private final ArrayList<Object[]> data = new ArrayList<>();


    // public final Object[] longValues = {"", true, "None of the above",
    // 20};

    public void addRow(Object[] rowData) {
      this.data.add(rowData);
    }


    /*
     * JTable uses this method to determine the default renderer/ editor for each cell. If we didn't
     * implement this method, then the last column would contain text ("true"/"false"), rather than
     * a check box.
     */
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
          return String.class;
        case 1:
          return Boolean.class;
        case 2:
          return String.class;
        case 3:
          return String.class;
      }

      return null;
    }


    @Override
    public int getColumnCount() {
      return this.columnNames.length;
    }


    @Override
    public String getColumnName(int col) {
      return this.columnNames[col];
    }


    @Override
    public int getRowCount() {
      return this.data.size();
    }


    @Override
    public Object getValueAt(int row, int col) {
      return this.data.get(row)[col];
    }


    @Override
    public boolean isCellEditable(int row, int col) {
      // Note that the data/cell address is constant,
      // no matter where the cell appears onscreen.
      if (col >= 2) {
        return (Boolean) getValueAt(row, 1);
      }

      return col == 1;
    }


    @Override
    public void setValueAt(Object value, int row, int col) {
      this.data.get(row)[col] = value;
      if (col == 1) {
        fireTableCellUpdated(row, 1);
        fireTableCellUpdated(row, 2);
        fireTableCellUpdated(row, 3);
      } else {
        fireTableCellUpdated(row, col);
      }
    }
  }

  private static final long serialVersionUID = 7363944803747373479L;


  JTable table;

  List<List<String>> dataTable;

  public SchemaSettingsOptionWindow(Point position, Dimension dim, String schemaPath,
      List<List<String>> tableForData) {
    this.dataTable = tableForData;

    int windowWidth = (int) dim.getWidth() / 2;
    int windowHeight = (int) dim.getHeight() * 2 / 3;
    windowWidth = windowWidth < RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        ? RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        ? RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        : windowHeight;
    this.setSize(windowWidth, windowHeight);
    setTitle("Set Annotation Training Options");
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    position.translate(40, 40); // Move option window slightly down and to
    // right
    this.setLocation(position);

    List<SchemaConcept> schemaConcepts = SchemaImporter.importSchemaConcepts(schemaPath);
    HashSet<String> attributes = SchemaConcept.getAttributeListFromProjectSchema(schemaConcepts);

    Box mainPanel = Box.createVerticalBox();

    mainPanel.add(buildSchemaSettingPanel(attributes, schemaConcepts));
    mainPanel.add(buildButtonPanel());

    this.add(mainPanel);
    setVisible(true);
    pack();
  }


  private Component buildButtonPanel() {
    Box runPanel = Box.createVerticalBox();
    runPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton runButton = new JButton("Print table");
    runButton.addActionListener(new PrintTable());

    buttonPanel.add(runButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    runPanel.add(buttonPanel);

    return runPanel;
  }


  private JPanel buildSchemaSettingPanel(HashSet<String> attributes,
      List<SchemaConcept> schemaConcepts) {
    this.table = new JTable(new TableModel());
    this.table.setPreferredScrollableViewportSize(new Dimension(520, 150));
    this.table.setFillsViewportHeight(true);
    this.table.getModel().addTableModelListener(new TableChangeListener());

    setupColumnsWithCheckBox(this.table.getColumnModel().getColumn(1));
    setupColumnsWithComboBox(this.table.getColumnModel().getColumn(2), schemaConcepts);
    setupColumnsWithComboBox(this.table.getColumnModel().getColumn(3), schemaConcepts);

    initData(this.table, attributes);
    initColumnSizes(this.table);

    // Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(this.table);
    JPanel schemaSettingsPanel = new JPanel();

    // Add the scroll pane to this panel.
    schemaSettingsPanel.add(scrollPane);

    return schemaSettingsPanel;
  }


  /*
   * This method picks good column sizes. If all column heads are wider than the column's cells'
   * contents, then you can just use column.sizeWidthToFit().
   */
  private void initColumnSizes(JTable table) {
    TableModel model = (TableModel) table.getModel();
    TableColumn column = null;
    Component comp = null;

    int headerWidth = 0;
    int cellWidth = 0;

    table.setDefaultRenderer(Object.class, new CellRenderer());
    column = table.getColumnModel().getColumn(1);
    column.setPreferredWidth(10);

    // TableCellRenderer headerRenderer =
    // table.getTableHeader().getDefaultRenderer();

    // for (int i = 0; i < 4; i++) {
    // column = table.getColumnModel().getColumn(i);
    //
    // // comp = headerRenderer.getTableCellRendererComponent(null,
    // column.getHeaderValue(), false, false, 0, 0);
    //
    // if(i == 2){
    // column.setPreferredWidth(30);
    // }
    // headerWidth = comp.getPreferredSize().width;
    //
    // comp =
    // table.getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(table,
    // longValues[i],false, false, 0, i);
    // cellWidth = comp.getPreferredSize().width;
    //
    // column.setPreferredWidth(Math.max(headerWidth, cellWidth));
    // }

  }


  private void initData(JTable table, HashSet<String> attributes) {
    TableModel model = (TableModel) table.getModel();

    List<String> attrs = new ArrayList<>(attributes);
    Collections.sort(attrs);

    for (String attr : attrs) {
      model.addRow(new Object[] {attr, false, "", ""});
    }
  }


  private void printTable(JTable table) {
    TableModel model = (TableModel) table.getModel();
    ArrayList<String> strTable;

    for (int i = 0; i < model.getRowCount(); i++) {
      strTable = new ArrayList<>();

      for (int j = 0; j < model.getColumnCount(); j++) {
        strTable.add(model.getValueAt(i, j).toString());
      }

      this.dataTable.add(strTable);
    }
  }


  private void setupColumnsWithCheckBox(TableColumn column) {
    JCheckBox chkBox = new JCheckBox();
    column.setCellEditor(new DefaultCellEditor(chkBox));

    // DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    column.setCellRenderer(new CheckBoxRenderer());
  }


  private void setupColumnsWithComboBox(TableColumn column, List<SchemaConcept> schemaConcepts) {

    JComboBox comboBox = new JComboBox();

    for (SchemaConcept c : schemaConcepts) {
      comboBox.addItem(c.getConceptName());
    }

    column.setCellEditor(new DefaultCellEditor(comboBox));

    // Set up tool tips for the sport cells.
    // DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    // column.setCellRenderer(new CellRenderer());
  }


  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public static void main(String[] args) {
    String schemaPath = "C:\\Sanjib\\projectschema.xml";
    new SchemaSettingsOptionWindow(new Point(100, 100), new Dimension(600, 800), schemaPath,
        new ArrayList<List<String>>());
  }
}
