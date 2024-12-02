package src.main.gov.va.vha09.grecc.raptat.gg.core;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public enum UserPreferences {
  INSTANCE;

  private static final String WORKING_DIRECTORY = "workingDirectory";
  private static final String LVG_LOCATION = "lvgDirectory";
  private static final String ANNOTATOR = "annotator";
  private static final String USE_RAPTAT = "useRaptatInEhost";
  private static final String APPLICATION_REGISTERED = "registerAKIProject";
  private Preferences userSettings;
  private Preferences raptatProjects;

  private HashSet<String> raptatProjectSet;
  private String lvgPath = "undefined";
  private String workingDirectoryPath = "undefined";
  private String annotator = "undefined";
  private String useRaptatInEhost = "undefined";

  private UserPreferences() {
    this.userSettings = Preferences.userRoot().node(this.getClass().getName());
    this.raptatProjects = this.userSettings.node("raptatProjects");
    try {
      String[] projectPaths = this.raptatProjects.keys();
      this.raptatProjectSet = new HashSet<>(Arrays.asList(projectPaths));
    } catch (BackingStoreException ex) {
      System.err.println("Error in setting up user preferences:" + ex.getLocalizedMessage());
      System.exit(-1);
    }
  }

  /**
   * Adds the absolute path to a raptat project to the raptatProjects preferences node. The value is
   * currently specified as "undefined" to allow for addition of further information in the future.
   *
   * @param newProject
   */
  public void addRaptatProject(String newProject) {
    this.raptatProjects.put(newProject, "undefined");
    this.raptatProjectSet.add(newProject);
  }

  public void deregisterAKIProject() {
    this.userSettings.put(UserPreferences.APPLICATION_REGISTERED, "undefined");
  }

  public String getAnnotator() {
    this.annotator = this.userSettings.get(UserPreferences.ANNOTATOR, "undefined");

    if (this.annotator.equals("undefined")) {
      String annotatorName;
      String startString = "Firstname Lastname";
      boolean finished = false;
      do {
        annotatorName =
            JOptionPane.showInputDialog(null, "Please enter your first and last name", startString);
        if (annotatorName == null) {
          return "";
        }

        if (annotatorName.equals(startString)) {
          JOptionPane.showMessageDialog(null, "Interesting - your name is 'Firstname Lastname' ?");
        } else {
          String[] testName = annotatorName.split(" ");
          if (testName.length != 2 || testName[0].length() < 2 || testName[1].length() < 2) {
            GeneralHelper.errorWriter("Name must include one first and one last name\n"
                + "separated by a single space, and each\n"
                + "name must contain at least 2 characters");
          } else {
            this.annotator = annotatorName;
            this.userSettings.put(UserPreferences.ANNOTATOR, this.annotator);
            return this.annotator;
          }
        }
      } while (!finished);
    }
    return this.annotator;
  }

  public String getAnnotator(HashMap<String, String> passwords) {
    this.annotator = this.userSettings.get(UserPreferences.ANNOTATOR, "undefined");

    if (this.annotator.equals("undefined") || !isApplicationRegistered()) {
      String annotatorName = checkUsernameAndPassword(passwords);

      if (annotatorName != null) {
        this.annotator = annotatorName;
        this.userSettings.put(UserPreferences.ANNOTATOR, this.annotator);
        return this.annotator;
      } else {
        return null;
      }
    }
    return this.annotator;
  }

  public String getLvgPath() {
    return this.lvgPath;
  }

  public HashSet<String> getRaptatProjects() {
    return this.raptatProjectSet;
  }

  public String getWorkingDirectoryPath() {
    return this.workingDirectoryPath;
  }

  public int initializeLVGLocation() {
    this.lvgPath = this.userSettings.get(UserPreferences.LVG_LOCATION, "undefined");

    if (!isValidLVGDirectory(new File(this.lvgPath))) {
      return this.setLvgPath();
    } else {
      return 1;
    }
  }

  public boolean initializeWorkingDirectory(boolean setWorking) {
    return this.initializeWorkingDirectory(setWorking, "The working directory must be (re)set");
  }

  public boolean initializeWorkingDirectory(boolean alwaysSet, String userPrompt) {
    if (alwaysSet) {
      return setWorkingDirectory(userPrompt);
    } else {
      this.workingDirectoryPath =
          this.userSettings.get(UserPreferences.WORKING_DIRECTORY, "undefined");
      File workingDirectory = new File(this.workingDirectoryPath);
      if (this.workingDirectoryPath.equals("undefined") || !isValid(workingDirectory)) {
        return setWorkingDirectory(userPrompt);
      } else {
        return true;
      }
    }
  }

  public boolean isApplicationRegistered() {
    String isRegistered =
        this.userSettings.get(UserPreferences.APPLICATION_REGISTERED, "undefined");
    return isRegistered.equalsIgnoreCase("true");
  }

  public void registerAKIProject() {
    this.userSettings.put(UserPreferences.APPLICATION_REGISTERED, "true");
  }

  public void removeProject(String projectPath) {
    this.raptatProjects.remove(projectPath);
    this.raptatProjectSet.remove(projectPath);
  }

  public void resetAll() {
    try {
      String[] preferenceKeys = this.userSettings.keys();
      for (String curKey : preferenceKeys) {
        this.userSettings.put(curKey, "undefined");
      }

      this.raptatProjects.removeNode();
    } catch (BackingStoreException ex) {
      java.util.logging.Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, null,
          ex);
    }
  }

  public void setAnnotator(String annotator) {
    if (annotator != null & annotator.length() > 0) {
      this.userSettings.put(UserPreferences.ANNOTATOR, annotator);

    } else {
      this.userSettings.put(UserPreferences.ANNOTATOR, "undefined");
    }
  }

  public int setLvgPath() {
    // getDirectory() returns null if user chooses cancel in subsequent
    // dialog
    File lvgFile = GeneralHelper.getDirectory("Select the directory containing LVG");
    if (lvgFile != null) {
      if (isValidLVGDirectory(lvgFile)) {
        this.lvgPath = lvgFile.getAbsolutePath();
        this.userSettings.put(UserPreferences.LVG_LOCATION, this.lvgPath);
        return 1;
      } else {
        GeneralHelper.errorWriter("Unreadable configuration file in subdirectory data/config/");
        return -1;
      }
    }
    return 0;
  }

  public int setLvgPath(String path) {
    File lvgFile = new File(path);
    if (lvgFile == null || !isValidLVGDirectory(lvgFile)) {
      return setLvgPath();
    }
    this.lvgPath = lvgFile.getAbsolutePath();
    this.userSettings.put(UserPreferences.LVG_LOCATION, this.lvgPath);
    return 1;
  }

  public void setRaptatInEhost(boolean useRaptat) {
    this.userSettings.put(UserPreferences.USE_RAPTAT, Boolean.toString(useRaptat));
  }

  public boolean useRaptatInEhost() {
    this.useRaptatInEhost = this.userSettings.get(UserPreferences.USE_RAPTAT, "undefined");

    if (this.useRaptatInEhost.equals("undefined")) {
      this.useRaptatInEhost = "false";
      this.userSettings.put(UserPreferences.USE_RAPTAT, this.useRaptatInEhost);
    }
    return Boolean.parseBoolean(this.useRaptatInEhost);
  }

  private String checkUsernameAndPassword(HashMap<String, String> passwords) {
    // Using a JPanel as the message for the JOptionPane
    JPanel userPanel = new JPanel();
    userPanel.setLayout(new GridLayout(3, 1));

    JPanel textPanel = new JPanel();
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new FlowLayout());
    JPanel passwordPanel = new JPanel();
    passwordPanel.setLayout(new FlowLayout());

    JLabel msg = new JLabel("<html>Enter first name and last name in the top field,"
        + "<br>and your assigned password in the bottom field</html>");
    JLabel usernameLbl = new JLabel("Username:");
    JLabel passwordLbl = new JLabel("Password:");
    JTextField usernameFld = new JTextField(30) {
      /** */
      private static final long serialVersionUID = 8140949669861535092L;

      @Override
      public void addNotify() {
        super.addNotify();
        this.requestFocus();
      }
    };
    usernameFld.setText("Firstname Lastname");
    JPasswordField passwordFld = new JPasswordField(30);

    textPanel.add(msg);
    namePanel.add(usernameLbl);
    namePanel.add(usernameFld);
    passwordPanel.add(passwordLbl);
    passwordPanel.add(passwordFld);

    userPanel.add(textPanel);
    userPanel.add(namePanel);
    userPanel.add(passwordPanel);

    usernameFld.selectAll();
    usernameFld.requestFocusInWindow();

    // As the JOptionPane accepts an object as the message
    // it allows us to use any component we like - in this case
    // a JPanel containing the dialog components we want
    int input = JOptionPane.showConfirmDialog(null, userPanel, "Username and Password",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (input == 0) // OK Button = 0
    {
      String username = usernameFld.getText();
      char[] enteredPassword = passwordFld.getPassword();
      boolean valid = validate(username, enteredPassword, passwords);
      Arrays.fill(enteredPassword, '0');

      if (valid) {
        return username;
      }
    } else {
      return null;
    }
    return null;
  }

  private boolean isReadable(File testFile) {
    BufferedReader inReader = null;
    try {
      // This just checks to see if the file exists and throws
      // an error if not.
      inReader = new BufferedReader(new FileReader(testFile));
    } catch (FileNotFoundException e1) {
      return false;
    }

    try {
      // This line checks to make sure the file is readable.
      inReader.readLine();
    } catch (IOException e1) {
      return false;
    } finally {

      try {
        inReader.close();
      } catch (IOException e) {
        System.out.println(e);
      }
    }

    return true;
  }

  private boolean isValid(File workingDirectory) {
    if (workingDirectory != null && workingDirectory.exists()) {
      if (!workingDirectory.canRead()) {
        GeneralHelper.errorWriter("Unable to read from chosen workspace directory");
        return false;
      }
      if (!workingDirectory.canWrite()) {
        GeneralHelper.errorWriter("Unable to write to chosen workspace directory");
        return false;
      }
      return true;
    }
    GeneralHelper.errorWriter("Not a valid workspace directory");
    return false;
  }

  private boolean isValidLVGDirectory(File lvgDirectory) {
    String dirPath = lvgDirectory.getAbsolutePath();
    String configPath = dirPath + File.separator + "data" + File.separator + "config"
        + File.separator + "lvg.properties";
    File configFile = new File(configPath);

    return isReadable(configFile);
  }

  private boolean setWorkingDirectory(String userPrompt) {
    GeneralHelper.errorWriter(userPrompt);
    File solutionDirectory = GeneralHelper.getDirectory("Select working directory");
    if (solutionDirectory != null) {
      this.workingDirectoryPath = solutionDirectory.getAbsolutePath();
      this.userSettings.put(UserPreferences.WORKING_DIRECTORY, this.workingDirectoryPath);
      return true;
    } else {
      GeneralHelper.errorWriter("Unable to create working directory");
      return false;
    }
  }

  private boolean validate(String username, char[] enteredPassword,
      HashMap<String, String> passwords) {
    String[] testName = username.split(" ");
    if (testName.length != 2 || testName[0].length() < 2 || testName[1].length() < 2) {
      GeneralHelper.errorWriter("Name must include one first and one last name\n"
          + "separated by a single space, and each\n" + "name must contain at least 2 characters");
      return false;
    }

    String lastName = testName[testName.length - 1].toLowerCase();
    if (!passwords.containsKey(lastName)) {
      GeneralHelper.errorWriter("Username is not a registered as an annotator");
      return false;
    }

    if (!Arrays.equals(enteredPassword, passwords.get(lastName).toCharArray())) {
      GeneralHelper.errorWriter("Password is incorrect");
      return false;
    }
    return true;
  }
}
