/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.ContextHandling;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ************************************************
 *
 * @author vhatvhgobbeg - Jul 11, 2013 ************************************************
 */
public final class ConceptMapTrainOptionsWindow extends JDialog implements ActionListener {
  private static final long serialVersionUID = -2504190679050972929L;


  JComboBox learningMethods, reasonNoMedsContextMethods, negationContextMethods;

  ButtonGroup includeNullsGroup, useStemsForTrainingGroup, usePOSForTrainingGroup,
      invertTokenSequenceGroup, removeStopWordsGroup, useNegationGroup;
  JButton trainSaveButton;
  boolean windowInitiated = false;

  private final OptionsManager theOptions = OptionsManager.getInstance();
  private final JTextField maxTokensField;

  protected ConceptMapTrainOptionsWindow(Point position, Dimension dim) {
    int windowWidth = (int) dim.getWidth() * 2 / 3;
    int windowHeight = (int) dim.getHeight() * 4 / 5;
    windowWidth = windowWidth < RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        ? RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        ? RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        : windowHeight;
    this.setSize(windowWidth, windowHeight);
    setTitle("Set Concept Map Training Options");
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    position.translate(40, 40); // Move option window slightly down
    // and
    // to right
    this.setLocation(position);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        ConceptMapTrainOptionsWindow.this.theOptions.trainOptWindowOpen = false;
      }
    });
    Box mainPanel = Box.createVerticalBox();

    mainPanel.add(buildLMSelectionPanel());

    JPanel allButtons = new JPanel();
    allButtons.setLayout(new GridLayout(2, 2));

    this.includeNullsGroup = new ButtonGroup();
    String panelTitle = "Insert Nulls";
    JPanel includeNullsPanel = this.theOptions.buildYesNoPanel(panelTitle, this.includeNullsGroup,
        this.theOptions.includeNulls);
    includeNullsPanel.setToolTipText("<html>Choose whether to equalize all<br>"
        + "phrases with respect to number of tokens.<br><br>"
        + "Pad <b>'null'</b> dummy tokens onto phrases<br>shorter "
        + "than the longest one in the data.</html>");
    allButtons.add(includeNullsPanel);

    this.useStemsForTrainingGroup = new ButtonGroup();
    panelTitle = "Use Token Word Stems";
    JPanel useStemsPanel = this.theOptions.buildYesNoPanel(panelTitle,
        this.useStemsForTrainingGroup, this.theOptions.useStems);
    useStemsPanel.setToolTipText("<html>Choose whether to convert all <br>"
        + "word tokens to their appropriate stem<br> forms before training </html>");
    allButtons.add(useStemsPanel);

    this.usePOSForTrainingGroup = new ButtonGroup();
    panelTitle = "Use Parts of Speech";
    JPanel usePOSPanel = this.theOptions.buildYesNoPanel(panelTitle, this.usePOSForTrainingGroup,
        this.theOptions.usePOS);
    usePOSPanel.setToolTipText("<html>Choose whether to include <br>"
        + "the parts-of-speech of word tokens<br> for system training </html>");
    allButtons.add(usePOSPanel);

    this.invertTokenSequenceGroup = new ButtonGroup();
    panelTitle = "Invert Token Sequence";
    JPanel invertTokensPanel = this.theOptions.buildYesNoPanel(panelTitle,
        this.invertTokenSequenceGroup, this.theOptions.invertTokenSequence);
    invertTokensPanel.setToolTipText("<html>Choose whether to use the standard <br>"
        + "(left-to-right reading) or the <br>  inverted token sequence s<br> for system training </html>");
    allButtons.add(invertTokensPanel);

    this.removeStopWordsGroup = new ButtonGroup();
    panelTitle = "Remove Stop Words";
    JPanel removeStopWordPanel = this.theOptions.buildYesNoPanel(panelTitle,
        this.removeStopWordsGroup, this.theOptions.removeStopWords,
        TitledBorder.DEFAULT_JUSTIFICATION, BorderFactory.createEtchedBorder());
    removeStopWordPanel.setToolTipText(
        "<html>Choose whether to remove <br>" + "short, common words as defined by LVG </html>");

    JPanel maxTokensPanel = new JPanel();
    Border b2 = BorderFactory.createTitledBorder("Max Tokens Per Phrase");
    maxTokensPanel.setBorder(b2);
    this.maxTokensField = new JTextField(2);
    this.maxTokensField.setText(Integer.toString(this.theOptions.getTrainingTokenNumber()));
    maxTokensPanel.add(this.maxTokensField);

    JPanel stopWordAndTokensPanel = new JPanel(new GridLayout(1, 2));
    stopWordAndTokensPanel.add(removeStopWordPanel);
    stopWordAndTokensPanel.add(maxTokensPanel);

    JPanel contextMethodPanel = new JPanel();
    contextMethodPanel.setBorder(BorderFactory.createTitledBorder("Reason No Meds Context"));
    this.reasonNoMedsContextMethods = new JComboBox();
    contextMethodPanel.add(buildContextMethodPanel(this.reasonNoMedsContextMethods,
        this.theOptions.reasonNoMedsProcessing, "", TitledBorder.DEFAULT_JUSTIFICATION));

    JPanel useNegationPanel = new JPanel();
    useNegationPanel.setBorder(BorderFactory.createTitledBorder("Negation Context"));
    this.negationContextMethods = new JComboBox();
    useNegationPanel.add(buildContextMethodPanel(this.negationContextMethods,
        this.theOptions.negationProcessing, "", TitledBorder.DEFAULT_JUSTIFICATION));

    JPanel allContextPanels = new JPanel(new GridLayout(1, 2));
    allContextPanels.add(contextMethodPanel);
    allContextPanels.add(useNegationPanel);

    mainPanel.add(allButtons);
    mainPanel.add(stopWordAndTokensPanel);
    mainPanel.add(allContextPanels);
    mainPanel.add(buildSavePanel());

    this.add(mainPanel);

    this.learningMethods.setEnabled(false);
    this.negationContextMethods.setEnabled(false);
    this.reasonNoMedsContextMethods.setEnabled(false);
    setVisible(true);
    this.windowInitiated = true;
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    // pack();
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
      if (eventSource == this.trainSaveButton) {
        processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        saveTrainOptions();
      }
    }
  }


  /**
   * ************************************************************** Builds a panel to determine what
   * context usage during training
   *
   * @return JPanel **************************************************************
   */
  private JPanel buildContextMethodPanel(JComboBox theContextMethods,
      ContextHandling curHandlingMethod, String panelTitle, int titleJustification) {
    JPanel methodPanel = new JPanel();
    methodPanel.setLayout(new BorderLayout());
    Border b1 = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), panelTitle,
        titleJustification, TitledBorder.DEFAULT_JUSTIFICATION);
    methodPanel.setBorder(b1);

    for (String curMethod : RaptatConstants.CONTEXT_METHODS) {
      theContextMethods.addItem(curMethod);
      if (curHandlingMethod.name().equalsIgnoreCase(curMethod.replaceAll("\\s|-", ""))) {
        theContextMethods.setSelectedItem(curMethod);
      }
    }
    methodPanel.add(theContextMethods);
    return methodPanel;
  }


  /**
   * ************************************************************** Builds a panel to determine what
   * learning method to use for training the model
   *
   * @return JPanel **************************************************************
   */
  private JPanel buildLMSelectionPanel() {
    JPanel lmSelectionPanel = new JPanel();
    Border b1 = BorderFactory.createTitledBorder("Concept Mapping Method");
    lmSelectionPanel.setBorder(b1);
    this.learningMethods = new JComboBox();

    for (String curMethod : RaptatConstants.CONCEPT_MAPPING_METHODS) {
      this.learningMethods.addItem(curMethod);
      if (this.theOptions.curLearnMethod.equals(curMethod)) {
        this.learningMethods.setSelectedItem(curMethod);
      }
    }

    this.learningMethods.addActionListener(this);
    lmSelectionPanel.add(this.learningMethods);
    this.learningMethods.setEnabled(true); // Set to true once other
    // methods
    // implemented
    return lmSelectionPanel;
  }


  /**
   * ************************************************************** Create JPanel to save the
   * settings for the training options
   *
   * @return Component **************************************************************
   */
  private JPanel buildSavePanel() {
    JPanel savePanel = new JPanel();
    this.trainSaveButton = new JButton("Save & Close");
    this.trainSaveButton.addActionListener(this);
    savePanel.add(Box.createRigidArea(new Dimension(0, 60)));
    savePanel.add(this.trainSaveButton, BorderLayout.CENTER);
    return savePanel;
  }


  /**
   * ************************************************************** Save all the settings currently
   * set in the options window dialog.
   *
   * <p>
   * **************************************************************
   */
  private void saveTrainOptions() {
    AbstractButton curButton;

    this.theOptions.curLearnMethod = (String) this.learningMethods.getSelectedItem();
    Enumeration<AbstractButton> e = this.includeNullsGroup.getElements();
    while (e.hasMoreElements()) {
      curButton = e.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.includeNulls = true;
        } else {
          this.theOptions.includeNulls = false;
        }

        break;
      }
    }

    e = this.useStemsForTrainingGroup.getElements();
    while (e.hasMoreElements()) {
      curButton = e.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.useStems = true;
        } else {
          this.theOptions.useStems = false;
        }

        break;
      }
    }

    e = this.usePOSForTrainingGroup.getElements();
    while (e.hasMoreElements()) {
      curButton = e.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.usePOS = true;
        } else {
          this.theOptions.usePOS = false;
        }

        break;
      }
    }

    e = this.invertTokenSequenceGroup.getElements();
    while (e.hasMoreElements()) {
      curButton = e.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.invertTokenSequence = true;
        } else {
          this.theOptions.invertTokenSequence = false;
        }

        break;
      }
    }

    e = this.removeStopWordsGroup.getElements();
    while (e.hasMoreElements()) {
      curButton = e.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.removeStopWords = true;
        } else {
          this.theOptions.removeStopWords = false;
        }

        break;
      }
    }

    setNegationHandling((String) this.negationContextMethods.getSelectedItem());
    setReasonNoMedsHandling((String) this.reasonNoMedsContextMethods.getSelectedItem());

    try {
      int maxTokens = Integer.parseInt(this.maxTokensField.getText());

      if (maxTokens < 1) {
        throw new NumberFormatException();
      } else {
        this.theOptions.setTrainingTokenNumber(maxTokens);
      }
    } catch (NumberFormatException e1) {
      System.err.println(e1.getMessage());
      GeneralHelper.errorWriter("<html><b>Maximum Tokens Number Input Error:</b><br>"
          + "Please use a positive integer</html>");
    }
  }


  private void setNegationHandling(String handlingAsString) {
    handlingAsString = handlingAsString.replaceAll("\\s|-", "");
    for (ContextHandling curMethod : ContextHandling.values()) {
      if (handlingAsString.equalsIgnoreCase(curMethod.name())) {
        this.theOptions.negationProcessing = curMethod;
        return;
      }
    }
  }


  /**
   * @param selectedItem
   */
  private void setReasonNoMedsHandling(String handlingAsString) {
    handlingAsString = handlingAsString.replaceAll("\\s|-", "");
    for (ContextHandling curMethod : ContextHandling.values()) {
      if (handlingAsString.equalsIgnoreCase(curMethod.name())) {
        this.theOptions.reasonNoMedsProcessing = curMethod;
        return;
      }
    }
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new ConceptMapTrainOptionsWindow(new Point(0, 0), new Dimension(0, 0));
      }
    });
  }
}
