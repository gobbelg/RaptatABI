package src.main.gov.va.vha09.grecc.raptat.gg.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * Primary class for entry into the RapTAT program. Sets up the application window, options
 * management, and menu bars.
 *
 * @author Glenn Gobbel, Sep 1, 2010
 */
public class RapTAT extends JFrame {

  /**
   * MenuManager - Manages the menus used with the RapTAT program
   *
   * @author Glenn Gobbel, Aug 19, 2010
   */
  private class MenuManager {

    /**
     * Inner class to MenuManager that responds to selections of menu items
     *
     * @author Glenn Gobbel, Sep 1, 2010
     */
    private class MenuListener implements ActionListener {

      @Override
      public void actionPerformed(ActionEvent e) {
        Object menuSource = e.getSource();
        if (menuSource == MenuManager.this.exitItem) {
          System.exit(0);
        }

        // Loads existing machine learned solution
        else if (menuSource == MenuManager.this.loadSolutionItem) {
          RapTAT.this.curAnnotationSolution = MenuManager.this.mResponder.loadConceptMapSolution();
          if (RapTAT.this.curAnnotationSolution == null) {
            JOptionPane.showMessageDialog(null, "Unable to Load Solution File", "",
                JOptionPane.WARNING_MESSAGE);
          }

        }

        // Updates the existing solution
        else if (menuSource == MenuManager.this.updateSolution) {
          JOptionPane.showMessageDialog(null, "This method has been depricated.", "",
              JOptionPane.ERROR_MESSAGE);
          // MenuManager.this.mResponder.updateAnnotator(
          // getLocation(), RapTAT.this.getSize() );
        }

        // Annotation Export DB Creation
        else if (menuSource == MenuManager.this.createDBTable) {
          MenuManager.this.mResponder.dbCreation(RapTAT.this.getLocation(), RapTAT.this.getSize());
        }
        // Upload annotation to DB
        else if (menuSource == MenuManager.this.uploadAnnotations) {
          MenuManager.this.mResponder.uploadAnnotations(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
        }

        // Generates a machine learned solution via training
        else if (menuSource == MenuManager.this.createMappingSolutionItem) {
          String learnMethod = RapTAT.this.annotatorOptions.getCurLearnMethod();
          boolean useNulls = RapTAT.this.annotatorOptions.getUseNulls();
          boolean useStems = RapTAT.this.annotatorOptions.getUseStems();
          boolean usePOS = RapTAT.this.annotatorOptions.getUsePOS();
          boolean invertTokenSequence = RapTAT.this.annotatorOptions.getInvertTokenSequence();
          boolean removeStopWords = RapTAT.this.annotatorOptions.getRemoveStopWords();
          ContextHandling reasonNoMedsProcessing =
              RapTAT.this.annotatorOptions.getReasonNoMedsProcessing();
          RapTAT.this.curAnnotationSolution =
              MenuManager.this.mResponder.trainConceptMapSolution(learnMethod, useNulls, useStems,
                  usePOS, invertTokenSequence, removeStopWords, reasonNoMedsProcessing);
        } else if (menuSource == MenuManager.this.testMapping) {
          System.out.println("No longer implemented");
          System.exit(-1);;
        } else if (menuSource == MenuManager.this.mapPhrases) {
          if (RapTAT.this.curAnnotationSolution == null) {
            actionPerformed(new ActionEvent(MenuManager.this.loadSolutionItem,
                ActionEvent.ACTION_PERFORMED, ""));
          }
          if (RapTAT.this.curAnnotationSolution != null) {
            MenuManager.this.mResponder.mapSolution(RapTAT.this.curAnnotationSolution,
                RapTAT.this.annotatorOptions);
          }

        } else if (menuSource == MenuManager.this.setTrainParametersItem) {
          RapTAT.this.annotatorOptions.setConceptMapTrainOptions(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
        } else if (menuSource == MenuManager.this.setTestParametersItem) {
          RapTAT.this.annotatorOptions.setTestOptions(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
        } else if (menuSource == MenuManager.this.setMapParametersItem) {
          RapTAT.this.annotatorOptions.setConceptMappingOptions(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
        } else if (e.getSource() == MenuManager.this.bootStrapAnalysisItem) {
          System.out.println("No longer implemented");
          System.exit(-1);
        } else if (e.getSource() == MenuManager.this.CrossValidationAnalysisItem) {
          System.out.println("No longer implemented");
          System.exit(-1);
        } else if (menuSource == MenuManager.this.setBootStrapParamItem) {
          RapTAT.this.annotatorOptions.setAnalysisOptions(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
        } else if (menuSource == MenuManager.this.annotateDocuments) {
          MenuManager.this.mResponder.annotateDocuments(RapTAT.this.getLocation(),
              RapTAT.this.getSize(), RapTAT.this);
        } else if (menuSource == MenuManager.this.trainConceptMappingAndPhraseMatching) {
          MenuManager.this.mResponder.trainAnnotator(RapTAT.this.getLocation(),
              RapTAT.this.getSize(), RapTAT.this);
        }

        // else if ( menuSource == testPerformanceAndUpdateSolution )
        else if (menuSource == MenuManager.this.scorePerformance) {
          MenuManager.this.mResponder.scorePerformance(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
          // mResponder.scoreAndUpdatePerformance();
        } else if (menuSource == MenuManager.this.runAnnotationBootstrapAnalysis) {
          System.out.println("Not implemented");
          System.exit(-1);
        } else if (menuSource == MenuManager.this.CrossValidationAnalysis) {
          boolean filterAnnotations = false;
          MenuManager.this.mResponder.runPhraseIDCrossValidationAnalysis(filterAnnotations);
        } else if (menuSource == MenuManager.this.exportResults) {
          MenuManager.this.mResponder.exportResults();
        } else if (menuSource == MenuManager.this.annotationTrainingOptions) {
          RapTAT.this.annotatorOptions.setAnnotationTrainOptions(RapTAT.this.getLocation(),
              RapTAT.this.getSize());
        } else if (menuSource == MenuManager.this.lvgDirectorySetting) {
          int resultSetting = UserPreferences.INSTANCE.setLvgPath();
          if (resultSetting != 0) {
            String resultString;
            if (resultSetting < 0) {
              resultString = "LVG path reset to ";
            } else {
              resultString = "LVG path changed to ";
            }
            GeneralHelper.errorWriter(resultString + UserPreferences.INSTANCE.getLvgPath());
          } else {
            RapTAT.logger.info("User canceled dialog to change LVG directory");
          }
        }
      } // End actionPerformed()
    } // End MenuListener Class

    private final JMenuItem loadSolutionItem, createMappingSolutionItem, testMapping, exitItem,
        setTrainParametersItem, setTestParametersItem, CrossValidationAnalysisItem,
        bootStrapAnalysisItem, setBootStrapParamItem, setCrossValidParamItem, mapPhrases,
        setMapParametersItem, annotateDocuments, trainConceptMappingAndPhraseMatching,
        runAnnotationBootstrapAnalysis, CrossValidationAnalysis, exportResults,
        annotationTrainingOptions, lvgDirectorySetting;
    // private final JMenuItem testPerformanceAndUpdateSolution,
    // loadConceptMappingAndPhraseMatching;
    private final JMenuItem scorePerformance;
    private final JMenuItem updateSolution;
    private final JMenuItem createDBTable;
    private final JMenuItem uploadAnnotations;
    private final JMenuBar menuBar;

    private final MenuResponseDispatcher mResponder = new MenuResponseDispatcher();


    /**
     * *************************************************************** Constructor for the
     * MenuManager class which manages the menus for the RapTAT application
     *
     * <p>
     * Modified by Sanjib Saha - July 16, 2014
     * ***************************************************************
     */
    public MenuManager() {
      MenuListener theListener = new MenuListener();

      this.menuBar = new JMenuBar();

      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);
      this.menuBar.add(fileMenu);
      this.exitItem = addItem(fileMenu, "Exit     ", KeyEvent.VK_X, theListener);

      JMenu conceptMappingMenu = new JMenu("Mapping ");
      this.menuBar.add(conceptMappingMenu);

      this.createMappingSolutionItem =
          addItem(conceptMappingMenu, "Create Mapping Solution", KeyEvent.VK_C, theListener);
      this.loadSolutionItem = addItem(conceptMappingMenu, "Load ConceptMapping Solution . . .     ",
          KeyEvent.VK_O, theListener);
      conceptMappingMenu.addSeparator();

      this.mapPhrases =
          addItem(conceptMappingMenu, "Map Phrases          ", KeyEvent.VK_M, theListener);
      conceptMappingMenu.addSeparator();

      // Malini: Added menu for statistical analysis
      JMenu conceptMapAnalysisMenu = new JMenu("Evaluation");
      conceptMapAnalysisMenu.setMnemonic(KeyEvent.VK_D);
      conceptMappingMenu.add(conceptMapAnalysisMenu);
      this.testMapping = addItem(conceptMapAnalysisMenu, "Simple Test Mapping              ",
          KeyEvent.VK_V, theListener);
      this.bootStrapAnalysisItem =
          addItem(conceptMapAnalysisMenu, "Bootstrap              ", KeyEvent.VK_B, theListener);
      this.CrossValidationAnalysisItem = addItem(conceptMapAnalysisMenu,
          "Cross Validate" + "          ", KeyEvent.VK_L, theListener);
      conceptMappingMenu.addSeparator();

      JMenu optionsMenu = new JMenu("Options");
      optionsMenu.setMnemonic(KeyEvent.VK_O);
      conceptMappingMenu.add(optionsMenu);
      this.setTrainParametersItem =
          addItem(optionsMenu, "Training        ", KeyEvent.VK_A, theListener);
      this.setMapParametersItem =
          addItem(optionsMenu, "Mapping             ", KeyEvent.VK_P, theListener);
      this.setTestParametersItem =
          addItem(optionsMenu, "Testing             ", KeyEvent.VK_E, theListener);
      JMenu analysisParamMenu = new JMenu("Evaluation        ");
      optionsMenu.add(analysisParamMenu);
      this.setBootStrapParamItem =
          addItem(analysisParamMenu, "BootStrap           ", KeyEvent.VK_S, theListener);
      this.setCrossValidParamItem =
          addItem(analysisParamMenu, "Leave One Out          ", KeyEvent.VK_D, theListener);

      JMenu annotationMenu = new JMenu("Annotation");

      this.menuBar.add(annotationMenu);
      this.trainConceptMappingAndPhraseMatching =
          addItem(annotationMenu, "Create Annotation Solution        ", KeyEvent.VK_C, theListener);
      this.updateSolution =
          addItem(annotationMenu, "Update Existing Solution        ", KeyEvent.VK_U, theListener);

      // Deactivated by Sanjib Saha on July 24, 2014. This menu item is
      // removed
      // loadConceptMappingAndPhraseMatching = addItem(annotationMenu,
      // "Load Existing Solution . . . ", VK_L,
      // theListener);
      annotationMenu.addSeparator();

      this.annotateDocuments =
          addItem(annotationMenu, "Annotate        ", KeyEvent.VK_A, theListener);
      this.exportResults = addItem(annotationMenu, "Export Annotations                     ",
          KeyEvent.VK_X, theListener);
      this.createDBTable =
          addItem(annotationMenu, "Create Database Tables", KeyEvent.VK_T, theListener);
      this.uploadAnnotations =
          addItem(annotationMenu, "Upload Annotations to Database", KeyEvent.VK_U, theListener);
      annotationMenu.addSeparator();

      JMenu annotationAnalysisMenu = new JMenu("Evaluation");
      annotationMenu.add(annotationAnalysisMenu);
      // testPerformanceAndUpdateSolution =
      // addItem(annotationAnalysisMenu,
      // "Score Performance & Update Solution ", VK_P,
      // theListener);
      this.scorePerformance =
          addItem(annotationAnalysisMenu, "Score Performance", KeyEvent.VK_S, theListener);
      this.runAnnotationBootstrapAnalysis =
          addItem(annotationAnalysisMenu, "Bootstrap         ", KeyEvent.VK_B, theListener);
      this.CrossValidationAnalysis =
          addItem(annotationAnalysisMenu, "Cross Validate         ", KeyEvent.VK_J, theListener);
      annotationMenu.addSeparator();

      JMenu annotationOptionsMenu = new JMenu("Options");
      annotationMenu.add(annotationOptionsMenu);
      this.annotationTrainingOptions =
          addItem(annotationOptionsMenu, "Training    ", KeyEvent.VK_P, theListener);
      this.lvgDirectorySetting =
          addItem(annotationOptionsMenu, "LVG Directory ", KeyEvent.VK_D, theListener);
    }


    /**
     * Gets the menu bar associated with the particular MenuManager
     *
     * @return JMenuBar
     * @uml.property name="menuBar"
     */
    public JMenuBar getMenuBar() {
      return this.menuBar;
    }


    /**
     * ************************************************************** Adds an item to a specific
     * menubar element
     *
     * @param menu - The menubar element to add the item to
     * @param title - The name of the menu item
     * @param key - The key used for a shortcut
     * @param listener - The listener to handle menu item selection
     * @return JMenuItem **************************************************** * *********
     */
    private JMenuItem addItem(JMenu menu, String title, int key, ActionListener listener) {
      JMenuItem item = new JMenuItem(title, key);
      item.addActionListener(listener);
      menu.add(item);
      return item;
    }
  } // End MenuManager Class

  private static final long serialVersionUID = -6344843122364215451L;

  @SuppressWarnings("FieldMayBeFinal")
  private static Logger logger = Logger.getLogger(RapTAT.class);


  private OptionsManager annotatorOptions;

  private ImageIcon backgroundImage;

  private final boolean isRedimensionable = false;

  // test if it works

  private ConceptMapSolution curAnnotationSolution = null;

  public RapTAT() {
    int lvgSetting;
    boolean setDefault = false;
    boolean defaultDirectoryValid = UserPreferences.INSTANCE.initializeWorkingDirectory(setDefault);
    if (defaultDirectoryValid) {
      do {
        // initializeUserPreferences() returns 1 if the chosen directory
        // is
        // good, 0 if user cancels prompt to get LVG directory, and -1
        // if
        // they put in an LVG directory that doesn't work
        lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

        if (lvgSetting > 0) {
          buildOptionsManager();
          buildApplicationWindow();
        }
        if (lvgSetting == 0) {
          GeneralHelper.errorWriter(
              "Please install the Lexical Variant" + " Generator (LVG) before running RapTAT");
        }

      } while (lvgSetting < 0);
    }
  }


  /**
   * ************************************************************** Puts a picture and JScrollBar
   * Panel in the main application window
   *
   * @param str - file holding the picture to put in the application window *
   *        *************************************************************
   */
  public void setBackgroundImage(String str) {

    try {
      // Get image and set size based on size of screen
      // URL url = RapTAT.class.getResource( str );
      // Image img = Toolkit.getDefaultToolkit().getImage( url );
      InputStream imageStream =
          this.getClass().getResourceAsStream(RaptatConstants.MAIN_WINDOW_IMAGE);
      BufferedImage bufferedImage = ImageIO.read(imageStream);
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      Image scaledImg =
          bufferedImage.getScaledInstance((int) dim.getWidth() * 1 / 2, -1, Image.SCALE_SMOOTH);
      this.backgroundImage = new ImageIcon(scaledImg);
    } catch (IOException e) {
      System.out.println("Unable to load background image for application");
      e.printStackTrace();
    }

    @SuppressWarnings("serial")
    JPanel backgroundPanel = new JPanel() {
      /** */
      private static final long serialVersionUID = 3659743722598625435L;


      @Override
      protected void paintComponent(Graphics g) {
        if (RapTAT.this.isRedimensionable) {
          // Scale image to size of component
          Dimension d = this.getSize();
          g.drawImage(RapTAT.this.backgroundImage.getImage(), 0, 0, d.width, d.height, null);
        } else {
          // Display image at at full size
          g.drawImage(RapTAT.this.backgroundImage.getImage(), 0, 0, null);
        }
        super.paintComponent(g);
      }
    };
    backgroundPanel.setBackground(new Color(244, 164, 96));

    backgroundPanel.setOpaque(false);
    getContentPane().add(backgroundPanel);

    Dimension backgroundImageDimensions =
        new Dimension(this.backgroundImage.getIconWidth() + getInsets().left + getInsets().right,
            this.backgroundImage.getIconHeight() + getInsets().top + getInsets().bottom);
    backgroundPanel.setPreferredSize(backgroundImageDimensions);

    JScrollPane scroller =
        new JScrollPane(buildEditorPane(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroller.setPreferredSize(new Dimension(350, 400));
    scroller.setMinimumSize(new Dimension(10, 10));
    scroller.setViewportBorder(
        BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.DARK_GRAY, Color.BLUE));
    backgroundPanel.setLayout(new GridBagLayout());
    backgroundPanel.add(scroller, getScrollPanelConstraints());
  }


  /**
   * ************************************************************** Create main window and menu bars
   * for application
   *
   * <p>
   * **************************************************************
   */
  private void buildApplicationWindow() {
    setBackgroundImage(RaptatConstants.MAIN_WINDOW_IMAGE);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setJMenuBar(new MenuManager().getMenuBar());
    setResizable(this.isRedimensionable);
    pack(); // Forces window size to size of background image
    setLocationRelativeTo(null); // Centers window on screen
    setVisible(true);
    OptionsManager.getInstance().storeMainWindowPositionSize(this.getLocation(), this.getSize());
  }


  /**
   * ************************************************************** Create an editor pane to hold
   * the text to go in the JScrollBarPanel within the main application window
   *
   * @return JEditorPane * *************************************************************
   */
  @SuppressWarnings("Convert2Lambda")
  private JEditorPane buildEditorPane() {
    final JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent hev) {
        try {
          if (hev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            editorPane.setPage(hev.getURL());
          }
        } catch (IOException e) {
        }
      }
    });

    try {
      InputStream helpFileInputStream =
          this.getClass().getResourceAsStream(RaptatConstants.MAIN_HELP_FILE);
      if (helpFileInputStream != null) {
        URL helpURL = getUrlFromStream(helpFileInputStream);
        editorPane.setPage(helpURL);
      } else {
        System.err.println("Couldn't find file: RapTAT_Help.htm");
      }
    } catch (IOException e) {
      System.err.println("Unable to load help page:" + RaptatConstants.MAIN_HELP_FILE + " "
          + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return editorPane;
  }


  /**
   * ************************************************************** Create a way to manage options
   * settings
   *
   * <p>
   * **************************************************************
   */
  private void buildOptionsManager() {
    this.annotatorOptions = OptionsManager.getInstance();
  }


  /**
   * ************************************************************** Used to determine how to set up
   * the layout to hold a JScrollPane in the main application window.
   *
   * @return GridBagConstraints * *************************************************************
   */
  private GridBagConstraints getScrollPanelConstraints() {
    GridBagConstraints theConstraints = new GridBagConstraints();
    theConstraints.gridx = 0;
    theConstraints.gridy = 0;
    theConstraints.gridwidth = 1;
    theConstraints.gridheight = 1;
    theConstraints.weightx = 100.0;
    theConstraints.weighty = 100.0;
    theConstraints.insets = new Insets(RaptatConstants.INSET, 3 * RaptatConstants.INSET,
        RaptatConstants.INSET, RaptatConstants.INSET);
    theConstraints.anchor = GridBagConstraints.WEST;
    theConstraints.fill = GridBagConstraints.NONE;

    return theConstraints;
  }


  /**
   * Oct 11, 2017
   *
   * @param helpFileInputStream
   * @return
   */
  private URL getUrlFromStream(InputStream inputStream) {
    URL resultUrl = null;

    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder inputText = new StringBuilder("");
      String line;

      while ((line = br.readLine()) != null) {
        inputText.append(line);
      }

      File tempUrlFile = File.createTempFile("rptHelp", ".html");
      PrintWriter pw = new PrintWriter(tempUrlFile);
      pw.write(inputText.toString());
      pw.flush();
      pw.close();

      resultUrl = tempUrlFile.toURI().toURL();
    } catch (IOException e) {
      System.err.println("Unable to convert stream to URL " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return resultUrl;
  }


  /**
   * ************************************************************** This is the main entry point
   * into the RapTAT application
   *
   * @param args void * *************************************************************
   */
  @SuppressWarnings("Convert2Lambda")
  public static void main(String[] args) {
    // Set up a simple configuration that logs on the console.
    // BasicConfigurator.configure();
    RapTAT.logger.info("Entering RapTAT application");
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      @SuppressWarnings("ResultOfObjectAllocationIgnored")
      public void run() {
        new RapTAT();
      }
    });
  }
} // End RapTAT Class
