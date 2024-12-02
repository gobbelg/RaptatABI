/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
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
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/** @author Glenn T. Gobbel Apr 14, 2016 */
public class TokenOptionsWindow extends JDialog {
  /** @author Glenn T. Gobbel Apr 14, 2016 */
  private class CancelButtonListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      dispose();
    }
  }

  /** @author Glenn T. Gobbel Apr 14, 2016 */
  private class SaveButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      boolean validInput = false;

      boolean useStems = getButtonGroupSetting(TokenOptionsWindow.this.useStemsForTrainingGroup);
      TokenOptionsWindow.this.tokenOptions.setUseStems(useStems);

      boolean usePOS = getButtonGroupSetting(TokenOptionsWindow.this.usePOSForTrainingGroup);
      TokenOptionsWindow.this.tokenOptions.setUsePOS(usePOS);

      boolean removeStopWords = getButtonGroupSetting(TokenOptionsWindow.this.removeStopWordsGroup);
      TokenOptionsWindow.this.tokenOptions.setRemoveStopWords(removeStopWords);

      try {
        int maxTokens = Integer.parseInt(TokenOptionsWindow.this.maxTokensField.getText());

        if (maxTokens < 1) {
          throw new NumberFormatException();
        } else {
          validInput = true;
          TokenOptionsWindow.this.tokenOptions.setMaxTokenNumber(maxTokens);
        }
      } catch (NumberFormatException e1) {
        System.err.println(e1.getMessage());
        GeneralHelper.errorWriter("<html><b>Maximum Tokens Number Input Error:</b><br>"
            + "Please use a positive integer from 1 to 7<html>");
      }

      if (validInput) {
        dispose();
      }
    }


    private boolean getButtonGroupSetting(ButtonGroup buttonGroup) {
      Enumeration<AbstractButton> e = buttonGroup.getElements();
      while (e.hasMoreElements()) {
        AbstractButton curButton = e.nextElement();
        if (curButton.isSelected()) {
          if (curButton.getText().equals("Yes")) {
            return true;
          }

          return false;
        }
      }
      return false;
    }
  }

  private static final long serialVersionUID = 9107259906434043194L;

  private static TokenOptionsWindow tokenOptionsWindowInstance;

  private static final Logger LOGGER = Logger.getLogger(TokenOptionsWindow.class);


  private ButtonGroup useStemsForTrainingGroup;


  private ButtonGroup usePOSForTrainingGroup;

  private ButtonGroup removeStopWordsGroup;

  private JTextField maxTokensField;

  private AbstractButton saveButton;

  private AbstractButton cancelButton;

  private TokenProcessingOptions tokenOptions;

  /**
   * @param tokenOptions
   * @param parent
   * @param thePoint
   * @param theDimension
   */
  public TokenOptionsWindow(TokenProcessingOptions tokenOptions, Window parent, Point thePoint,
      Dimension dim) {
    this(parent, tokenOptions);
    Point point;
    if (parent != null) {
      int x = parent.getX() + (parent.getWidth() - getWidth()) / 2;
      int y = parent.getY() + (parent.getHeight() - getHeight()) / 2;
      point = new Point(x, y);
    } else {
      point = new Point(0, 0);
    }

    initializeWindow(point, dim);
  }

  /**
   * @param parent
   */
  public TokenOptionsWindow(Window parent, TokenProcessingOptions tokenOptions) {
    super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
    this.tokenOptions = tokenOptions;
    TokenOptionsWindow.LOGGER.setLevel(Level.INFO);
  }


  /** @return the tokenOptions */
  public TokenProcessingOptions getTokenOptions() {
    return this.tokenOptions;
  }


  /** @return */
  private JPanel getOptionsButtonsPanel() {
    String panelTitle;
    OptionsManager theOptions = OptionsManager.getInstance();
    JPanel optionsButtonPanel = new JPanel();
    optionsButtonPanel.setLayout(new GridLayout(2, 2));

    this.useStemsForTrainingGroup = new ButtonGroup();
    panelTitle = "Use Token Word Stems";
    JPanel useStemsPanel =
        theOptions.buildYesNoPanel(panelTitle, this.useStemsForTrainingGroup, theOptions.useStems);
    useStemsPanel.setToolTipText(
        "<html>Convert all word tokens to their<br>" + "lemmatized forms before training </html>");
    optionsButtonPanel.add(useStemsPanel);

    this.usePOSForTrainingGroup = new ButtonGroup();
    panelTitle = "Use Parts of Speech";
    JPanel usePOSPanel =
        theOptions.buildYesNoPanel(panelTitle, this.usePOSForTrainingGroup, theOptions.usePOS);
    usePOSPanel.setToolTipText(
        "<html>Include the parts-of-speech of each word<br>" + "for training </html>");
    optionsButtonPanel.add(usePOSPanel);

    this.removeStopWordsGroup = new ButtonGroup();
    panelTitle = "Remove Stop Words";
    JPanel removeStopWordPanel = theOptions.buildYesNoPanel(panelTitle, this.removeStopWordsGroup,
        theOptions.removeStopWords, TitledBorder.DEFAULT_JUSTIFICATION,
        BorderFactory.createEtchedBorder());
    removeStopWordPanel.setToolTipText(
        "<html>Choose whether to remove <br>" + "short, common words from training</html>");
    optionsButtonPanel.add(removeStopWordPanel);

    JPanel maxTokensPanel = new JPanel();
    maxTokensPanel.setBorder(BorderFactory.createTitledBorder("Max Tokens Per Phrase"));
    this.maxTokensField = new JTextField(2);
    this.maxTokensField.setText(Integer.toString(theOptions.getTrainingTokenNumber()));
    maxTokensPanel.add(this.maxTokensField);
    maxTokensPanel.setToolTipText("<html>Choose the maximum number<br>"
        + "of tokens allowed within a trainin phrase<br>( <b>&lt=7</b> recommended )</html>");
    optionsButtonPanel.add(maxTokensPanel);

    return optionsButtonPanel;
  }


  /** @return */
  private Component getSaveCancelPanel() {
    JPanel savePanelContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    Box savePanel = Box.createHorizontalBox();
    savePanel.add(Box.createVerticalStrut(50));
    this.saveButton = new JButton("Save");
    this.cancelButton = new JButton("Cancel");
    this.saveButton.addActionListener(new SaveButtonListener());
    this.cancelButton.addActionListener(new CancelButtonListener());

    savePanel.add(this.saveButton);
    savePanel.add(Box.createHorizontalStrut(10));
    savePanel.add(this.cancelButton);
    savePanel.add(Box.createHorizontalStrut(10));
    savePanelContainer.add(savePanel);

    return savePanelContainer;
  }


  /**
   * @param point
   * @param dim
   */
  private void initializeWindow(Point position, Dimension dim) {
    setTitle("Cue-Scope Training Options");
    int windowWidth = (int) dim.getWidth() * 2 / 3;
    int windowHeight = (int) dim.getHeight() * 4 / 5;
    windowWidth = windowWidth < RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        ? RaptatConstants.TRAIN_OPTIONS_MIN_WIDTH
        : windowWidth;
    windowHeight = windowHeight < RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        ? RaptatConstants.TRAIN_OPTIONS_MIN_HEIGHT
        : windowHeight;
    this.setSize(windowWidth, windowHeight);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    position.translate(40, 40);
    this.setLocation(position);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        TokenOptionsWindow.this.dispose();
        TokenOptionsWindow.tokenOptionsWindowInstance = null;
      }
    });

    Box mainPanel = Box.createVerticalBox();
    JPanel optionButtonsPanel = getOptionsButtonsPanel();
    mainPanel.add(optionButtonsPanel);
    mainPanel.add(Box.createHorizontalStrut(400));
    Component saveCancelPanel = getSaveCancelPanel();
    mainPanel.add(saveCancelPanel);

    this.add(mainPanel);
    pack();
  }


  /**
   * @param usePOSForTrainingGroup2
   * @param usePOS
   */
  private void setButtonGroup(ButtonGroup buttonGroup, boolean setToYes) {
    Enumeration<AbstractButton> e = buttonGroup.getElements();
    while (e.hasMoreElements()) {
      AbstractButton curButton = e.nextElement();
      /*
       * Enable the yes button if both the button is the "yes" button and setToYes is true. Also, if
       * the button is the "no" button and not the "yes" button and setToYes is false, enable the
       * button.
       */
      buttonGroup.setSelected(curButton.getModel(),
          !(curButton.getText().equals("Yes") ^ setToYes));
    }
  }


  private void setOptions(TokenProcessingOptions inputOptions) {
    TokenOptionsWindow.this.tokenOptions = inputOptions;

    setButtonGroup(this.usePOSForTrainingGroup, inputOptions.usePOS());
    setButtonGroup(this.useStemsForTrainingGroup, inputOptions.useStems());
    setButtonGroup(this.removeStopWordsGroup, inputOptions.removeStopWords());
    this.maxTokensField.setText(Integer.toString(inputOptions.getMaxTokenNumber()));

    update(getGraphics());
  }


  public static synchronized TokenOptionsWindow getInstance(TokenProcessingOptions tokenOptions,
      Window parent, Point thePoint, Dimension theDimension) {
    if (TokenOptionsWindow.tokenOptionsWindowInstance == null) {
      TokenOptionsWindow.tokenOptionsWindowInstance =
          new TokenOptionsWindow(tokenOptions, parent, thePoint, theDimension);
    }

    TokenOptionsWindow.tokenOptionsWindowInstance.setOptions(tokenOptions);
    TokenOptionsWindow.tokenOptionsWindowInstance.setVisible(true);
    return TokenOptionsWindow.tokenOptionsWindowInstance;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        TokenOptionsWindow windowInstance =
            TokenOptionsWindow.getInstance(new TokenProcessingOptions(OptionsManager.getInstance()),
                null, new Point(0, 0), new Dimension(0, 0));

        TokenProcessingOptions returnedOptions = windowInstance.getTokenOptions();

        System.out.println("Stem settings:" + returnedOptions.useStems());
      }
    });
  }
}
