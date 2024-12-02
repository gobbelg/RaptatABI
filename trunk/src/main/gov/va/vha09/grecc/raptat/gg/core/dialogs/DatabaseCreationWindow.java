package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;

public class DatabaseCreationWindow extends JDialog {

  private class CreateDBListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String userName = DatabaseCreationWindow.this.userField.getText();
      String password = new String(DatabaseCreationWindow.this.passField.getPassword());
      String dbName = DatabaseCreationWindow.this.dbNameField.getText();
      String serverName = DatabaseCreationWindow.this.serverNameField.getText();
      String prefix = DatabaseCreationWindow.this.tablePrefix.getText().trim();

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
      // creating a new DB, so no DB name is supplied
      driver.setServerAndDB(serverName, null);

      try {
        boolean created = driver.createDatabase(dbName);

        if (created) {
          SQLDriver.executeQueryFile(".\\NLP DB\\NLP DB Create User.sql", dbName);
          // Creating a set of table with prefix and another set of
          // table without prefix
          SQLDriver.createTablesFromQueryFile(".\\NLP DB\\NLP DB Create Table.sql", dbName, prefix);
          // SQLDriver.createTablesWithPrefixFromQueryFile( ".\\NLP
          // DB\\NLP DB Create Table.sql", dbName, "" );

          JOptionPane.showMessageDialog(null, "Database successfully created.", "Success",
              JOptionPane.OK_OPTION);
        } else {
          JOptionPane.showMessageDialog(null, "Database exists.", "Success", JOptionPane.OK_OPTION);
        }
      } catch (NoConnectionException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Username and password does not match.", "Error",
            JOptionPane.OK_OPTION);
      }
    }
  }

  private class CreateTableListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String userName = DatabaseCreationWindow.this.userField.getText();
      String password = new String(DatabaseCreationWindow.this.passField.getPassword());
      String dbName = DatabaseCreationWindow.this.dbNameField.getText();
      String serverName = DatabaseCreationWindow.this.serverNameField.getText();
      String prefix = DatabaseCreationWindow.this.tablePrefix.getText().trim();

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

      try {
        // Creating a set of table with prefix and another set of table
        // without prefix
        SQLDriver.createTablesFromQueryFile(".\\NLP DB\\NLP DB Create Table.sql", dbName, prefix);
        // SQLDriver.createTablesWithPrefixFromQueryFile( ".\\NLP
        // DB\\NLP DB Create Table.sql", dbName, "" );
      } catch (NoConnectionException ex) {
        JOptionPane.showMessageDialog(null, "Username and password does not match.", "Error",
            JOptionPane.OK_OPTION);
        ex.printStackTrace();
      }

      JOptionPane.showMessageDialog(null, "Tables are created.", "Success", JOptionPane.OK_OPTION);
    }
  }

  /** */
  private static final long serialVersionUID = -4943575437025469613L;

  public static DatabaseCreationWindow createDBInstance = null;


  private JTextField dbNameField;


  private JTextField serverNameField;

  private JTextField userField;

  private JTextField tablePrefix;

  private JPasswordField passField;

  private DatabaseCreationWindow(Point thePoint, Dimension theDimension) {
    initialize(thePoint, theDimension);
  }

  private JPanel getOptionsPanel() {
    JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));

    Border b1 = BorderFactory.createTitledBorder("Server Name");
    this.serverNameField = new JTextField(15);
    this.serverNameField.setBorder(b1);
    this.serverNameField.setHorizontalAlignment(SwingConstants.LEFT);
    dbPanel.add(this.serverNameField);

    Border b2 = BorderFactory.createTitledBorder("Database Name");
    this.dbNameField = new JTextField(15);
    this.dbNameField.setBorder(b2);
    this.dbNameField.setHorizontalAlignment(SwingConstants.LEFT);
    dbPanel.add(this.dbNameField);

    Border b3 = BorderFactory.createTitledBorder("Table Prefix");
    this.tablePrefix = new JTextField(15);
    this.tablePrefix.setBorder(b3);
    this.tablePrefix.setHorizontalAlignment(SwingConstants.LEFT);
    dbPanel.add(this.tablePrefix);

    Font borderFont =
        new Font(dbPanel.getFont().getFontName(), Font.ITALIC, dbPanel.getFont().getSize());
    Border b4 =
        BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
            "Database", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont);
    dbPanel.setBorder(b4);

    return dbPanel;
  }


  private Component getRunPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton dbCreateButton = new JButton("Create Database & Table");
    JButton tableCreateButton = new JButton("Create Tables Only");
    JButton cancelButton = new JButton("Cancel");

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        DatabaseCreationWindow.createDBInstance.dispose();
        DatabaseCreationWindow.createDBInstance = null;
      }
    });

    // buttonPanel.add(dbCreateButton);
    // dbCreateButton.addActionListener(new CreateDBListener());
    buttonPanel.add(tableCreateButton);
    tableCreateButton.addActionListener(new CreateTableListener());
    buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    buttonPanel.add(cancelButton);

    return buttonPanel;
  }


  private JPanel getUserPassPanel() {
    JPanel userDetailPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

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
    setTitle("Database Creation");
    setResizable(false);
    thePosition.translate(10, 10);

    this.setLocation(thePosition);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        DatabaseCreationWindow.createDBInstance.dispose();
        DatabaseCreationWindow.createDBInstance = null;
      }
    });

    setLayout(new BorderLayout());
    this.add(getOptionsPanel(), BorderLayout.NORTH);
    this.add(getUserPassPanel(), BorderLayout.CENTER);
    this.add(getRunPanel(), BorderLayout.SOUTH);

    setVisible(true);
    pack();
  }


  public static synchronized DatabaseCreationWindow getInstance(Point thePoint,
      Dimension theDimension) {
    if (DatabaseCreationWindow.createDBInstance == null) {
      DatabaseCreationWindow.createDBInstance = new DatabaseCreationWindow(thePoint, theDimension);
    }

    return DatabaseCreationWindow.createDBInstance;
  }


  public static void main(String[] args) {
    DatabaseCreationWindow.getInstance(new Point(100, 100), new Dimension(600, 800));
  }
}
