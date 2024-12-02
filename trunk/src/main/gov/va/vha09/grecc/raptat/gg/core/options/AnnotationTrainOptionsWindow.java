package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.apache.commons.text.WordUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public final class AnnotationTrainOptionsWindow extends JDialog implements ActionListener {

  private static final long serialVersionUID = 7363944803747373479L;


  private final JComboBox phraseIDTrainingMethods, priorTokensTrainingMethods,
      postTokensTrainingMethods;

  private final ButtonGroup smoothingMethodButtonGroup;
  private JPanel smoothingPanel;
  private final ButtonGroup linkPriorGroup, linkPostGroup;
  private JButton annotationTrainSaveButton;
  private boolean windowInitiated = false;
  private final JTextField surroundingTokensField = new JTextField(10);
  private final OptionsManager theOptions = OptionsManager.getInstance();

  protected AnnotationTrainOptionsWindow(Point position, Dimension dim) {
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
    position.translate(40, 40); // Move option window slightly down and
    // to right
    this.setLocation(position);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        AnnotationTrainOptionsWindow.this.theOptions.annotationTrainOptWindowOpen = false;
      }
    });
    Box mainPanel = Box.createVerticalBox();

    JPanel phraseIDPanel = new JPanel(new GridLayout(1, 2));
    phraseIDPanel.setBorder(BorderFactory.createTitledBorder("Phrase Tokens Training"));
    this.phraseIDTrainingMethods = new JComboBox();
    this.phraseIDTrainingMethods.addActionListener(this);
    phraseIDPanel.add(buildTrainingSelectionPane(this.phraseIDTrainingMethods,
        this.theOptions.phraseIDTrainingMethod, "", TitledBorder.DEFAULT_JUSTIFICATION));
    this.smoothingMethodButtonGroup = new ButtonGroup();
    phraseIDPanel.add(buildSmoothingSelectionPanel(this.smoothingMethodButtonGroup));
    mainPanel.add(phraseIDPanel);

    JPanel priorTokenPanel = new JPanel(new GridLayout(1, 2));
    priorTokenPanel.setBorder(BorderFactory.createTitledBorder("Prior Tokens Training"));
    this.priorTokensTrainingMethods = new JComboBox();
    priorTokenPanel.add(buildTrainingSelectionPane(this.priorTokensTrainingMethods,
        this.theOptions.priorTokensTrainingMethod, "", TitledBorder.DEFAULT_JUSTIFICATION));
    this.linkPriorGroup = new ButtonGroup();
    priorTokenPanel.add(this.theOptions.buildYesNoPanel("Link to Phrase?", this.linkPriorGroup,
        this.theOptions.linkPhraseToPriorTokens, TitledBorder.CENTER,
        BorderFactory.createEmptyBorder()));
    mainPanel.add(priorTokenPanel);

    JPanel postTokenPanel = new JPanel(new GridLayout(1, 2));
    postTokenPanel.setBorder(BorderFactory.createTitledBorder("Post Tokens Training"));
    this.postTokensTrainingMethods = new JComboBox();
    postTokenPanel.add(buildTrainingSelectionPane(this.postTokensTrainingMethods,
        this.theOptions.postTokensTrainingMethod, "", TitledBorder.DEFAULT_JUSTIFICATION));
    this.linkPostGroup = new ButtonGroup();
    postTokenPanel.add(this.theOptions.buildYesNoPanel("Link to Phrase?", this.linkPostGroup,
        this.theOptions.linkPhraseToPostTokens, TitledBorder.CENTER,
        BorderFactory.createEmptyBorder()));
    mainPanel.add(postTokenPanel);

    JPanel surroundingTokensPanel = new JPanel();
    Border b2 = BorderFactory.createTitledBorder("Number of Prior/Post Tokens to Include?");
    surroundingTokensPanel.setBorder(b2);
    this.surroundingTokensField
        .setText(Integer.toString(this.theOptions.annotationSurroundingTokens));
    surroundingTokensPanel.add(this.surroundingTokensField);

    mainPanel.add(surroundingTokensPanel);
    mainPanel.add(buildSavePanel());

    this.add(mainPanel);
    setVisible(true);
    this.windowInitiated = true;
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    pack();
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    if (this.windowInitiated) // Only respond once window created
    {
      Object eventSource = e.getSource();
      if (eventSource == this.annotationTrainSaveButton) {
        processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        saveTrainOptions();
      } else if (eventSource == this.phraseIDTrainingMethods) {
        String trainMethod = (String) this.phraseIDTrainingMethods.getSelectedItem();
        if (trainMethod.equalsIgnoreCase("Probabilistic")) {
          setButtonGroupEnabling(true, this.smoothingMethodButtonGroup);
        } else {
          setButtonGroupEnabling(false, this.smoothingMethodButtonGroup);
        }
      }
    }
  }


  /**
   * ************************************************************** Create JPanel to save the
   * settings for the training options
   *
   * @return Component **************************************************************
   */
  private JPanel buildSavePanel() {
    JPanel savePanel = new JPanel();
    this.annotationTrainSaveButton = new JButton("Save & Close");
    this.annotationTrainSaveButton.addActionListener(this);
    savePanel.add(this.annotationTrainSaveButton);
    return savePanel;
  }


  private JPanel buildSmoothingSelectionPanel(ButtonGroup theButtons) {
    JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    this.smoothingPanel = new JPanel(new GridLayout(0, 1));
    this.smoothingPanel.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Smoothing"));
    JRadioButton curButton;

    for (PhraseIDSmoothingMethod smoothingSetting : PhraseIDSmoothingMethod.values()) {
      String smoothString = WordUtils.capitalize(smoothingSetting.name().toLowerCase());
      curButton = new JRadioButton(smoothString);
      theButtons.add(curButton);
      this.smoothingPanel.add(curButton);
      if (smoothingSetting == this.theOptions.phraseIDSmoothingMethod) {
        curButton.setSelected(true);
      } else {
        curButton.setSelected(false);
      }
    }

    if (this.theOptions.phraseIDTrainingMethod.equals(TSFinderMethod.valueOf("PROBABILISTIC"))) {
      setButtonGroupEnabling(true, theButtons);
    } else {
      setButtonGroupEnabling(false, theButtons);
    }
    outerPanel.add(this.smoothingPanel);
    return outerPanel;
  }


  /**
   * ************************************************************** Builds a panel to determine what
   * learning method to use for training the model
   *
   * @return JPanel **************************************************************
   */
  private JPanel buildTrainingSelectionPane(JComboBox theTrainingMethods,
      TSFinderMethod curTrainMethod, String panelTitle, int titleJustification) {
    JPanel atSelectionPanel = new JPanel();
    atSelectionPanel.setLayout(new BorderLayout());
    Border b1 = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), panelTitle,
        titleJustification, TitledBorder.DEFAULT_JUSTIFICATION);
    atSelectionPanel.setBorder(b1);

    for (String curMethod : RaptatConstants.PHRASE_ID_TRAINING_METHODS) {
      theTrainingMethods.addItem(curMethod);
      if (curTrainMethod.name().equalsIgnoreCase(curMethod.replaceAll("\\s|-", ""))) {
        theTrainingMethods.setSelectedItem(curMethod);
      }
    }
    theTrainingMethods.addActionListener(this);
    atSelectionPanel.add(theTrainingMethods, BorderLayout.CENTER);
    theTrainingMethods.setEnabled(true);
    return atSelectionPanel;
  }


  /**
   * ************************************************************** Save all the settings currently
   * set in the options window dialog.
   *
   * <p>
   * **************************************************************
   */
  private void saveTrainOptions() {
    Enumeration<AbstractButton> buttonEnumerator;
    AbstractButton curButton;

    setPhraseIDTrainingMethod((String) this.phraseIDTrainingMethods.getSelectedItem());
    setPriorTokenTrainingMethod((String) this.priorTokensTrainingMethods.getSelectedItem());
    setPostTokenTrainingMethod((String) this.postTokensTrainingMethods.getSelectedItem());

    buttonEnumerator = this.linkPriorGroup.getElements();
    while (buttonEnumerator.hasMoreElements()) {
      curButton = buttonEnumerator.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.linkPhraseToPriorTokens = true;
        } else {
          this.theOptions.linkPhraseToPriorTokens = false;
        }

        break;
      }
    }

    buttonEnumerator = this.linkPostGroup.getElements();
    while (buttonEnumerator.hasMoreElements()) {
      curButton = buttonEnumerator.nextElement();
      if (curButton.isSelected()) {
        if (curButton.getText().equals("Yes")) {
          this.theOptions.linkPhraseToPostTokens = true;
        } else {
          this.theOptions.linkPhraseToPostTokens = false;
        }
        break;
      }
    }

    this.theOptions.phraseIDSmoothingMethod = PhraseIDSmoothingMethod.valueOf(
        GeneralHelper.getSelectedButtonName(this.smoothingMethodButtonGroup).toUpperCase());

    try {
      this.theOptions.annotationSurroundingTokens =
          Integer.parseInt(this.surroundingTokensField.getText());
      if (this.theOptions.annotationSurroundingTokens < 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e1) {
      System.err.println(e1.getMessage());
      GeneralHelper.errorWriter("<html><b>Surrounding Tokens Number Input Error:</b><br>"
          + "Please use a positive integer</html>");
      GeneralHelper.errorWriter(
          "Token number reset to " + RaptatConstants.PHRASE_ID_SURROUNDING_TOKENS_DEFAULT);
      this.theOptions.annotationSurroundingTokens =
          RaptatConstants.PHRASE_ID_SURROUNDING_TOKENS_DEFAULT;
    }
  }


  private void setButtonGroupEnabling(boolean theSetting, ButtonGroup theButtonGroup) {
    Enumeration<AbstractButton> buttonElements = theButtonGroup.getElements();
    String groupTitle;
    while (buttonElements.hasMoreElements()) {
      buttonElements.nextElement().setEnabled(theSetting);
    }
    if (theSetting) {
      groupTitle = "Smoothing";
    } else {
      groupTitle = " ";
    }
    this.smoothingPanel
        .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), groupTitle));
  }


  private void setPhraseIDTrainingMethod(String methodAsString) {
    methodAsString = methodAsString.replaceAll("\\s|-", "");
    for (TSFinderMethod curMethod : TSFinderMethod.values()) {
      if (methodAsString.equalsIgnoreCase(curMethod.name())) {
        this.theOptions.phraseIDTrainingMethod = curMethod;
        return;
      }
    }
  }


  private void setPostTokenTrainingMethod(String methodAsString) {
    methodAsString = methodAsString.replaceAll("\\s|-", "");
    for (TSFinderMethod curMethod : TSFinderMethod.values()) {
      if (methodAsString.equalsIgnoreCase(curMethod.name())) {
        this.theOptions.priorTokensTrainingMethod = curMethod;
        return;
      }
    }
  }


  private void setPriorTokenTrainingMethod(String methodAsString) {
    methodAsString = methodAsString.replaceAll("\\s|-", "");
    for (TSFinderMethod curMethod : TSFinderMethod.values()) {
      if (methodAsString.equalsIgnoreCase(curMethod.name())) {
        this.theOptions.postTokensTrainingMethod = curMethod;
        return;
      }
    }
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new AnnotationTrainOptionsWindow(new Point(0, 0), new Dimension(0, 0));
      }
    });
  }
}
