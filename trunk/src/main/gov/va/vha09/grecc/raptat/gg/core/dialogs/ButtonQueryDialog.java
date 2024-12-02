package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

/*
 * Class for creating JOptionPanes that present a variable set of JRadioButtons and ask the user to
 * pick just one. The selection is then returned to the caller.
 */
public class ButtonQueryDialog extends JOptionPane implements ActionListener {
  /** */
  private static final long serialVersionUID = -8398104461219417568L;


  private ButtonGroup buttonGroup;

  private String lastSetButton;

  public ButtonQueryDialog(String[] buttonNames, String title) {
    Set<String> nameSet = new HashSet<>(buttonNames.length);
    JPanel buttonPanel = new JPanel();
    JPanel queryPanel = new JPanel(new GridLayout(2, 1));
    queryPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    queryPanel.add(new JLabel(title));
    buttonPanel.setLayout(new FlowLayout());
    queryPanel.add(buttonPanel);

    if (buttonNames.length > 0) {
      this.buttonGroup = new ButtonGroup();
      for (int i = 0; i < buttonNames.length; i++) {
        if (!nameSet.contains(buttonNames[i])) {
          nameSet.add(buttonNames[i]);
          JRadioButton radioButton = new JRadioButton(buttonNames[i]);
          radioButton.addActionListener(this);
          radioButton.setActionCommand(buttonNames[i]);
          buttonPanel.add(radioButton);
          System.out.println(radioButton.getName() + "Length:" + radioButton.getWidth());
          this.buttonGroup.add(radioButton);
        }
      }
      Enumeration<AbstractButton> buttonElements = this.buttonGroup.getElements();
      AbstractButton nextButton = buttonElements.nextElement();
      nextButton.setSelected(true);
      this.lastSetButton = nextButton.getActionCommand();
      int maxLength = this.lastSetButton.length();
      while (buttonElements.hasMoreElements()) {
        int curLength = buttonElements.nextElement().getActionCommand().length();
        if (maxLength < curLength) {
          maxLength = curLength;
        }
      }

      buttonElements = this.buttonGroup.getElements();
      Dimension buttonDim = new Dimension(10 * maxLength, 20);
      while (buttonElements.hasMoreElements()) {
        AbstractButton button = buttonElements.nextElement();
        button.setMinimumSize(buttonDim);
        button.setMaximumSize(buttonDim);
        button.setPreferredSize(buttonDim);
      }
    }
    setMessage(queryPanel);
    setMessageType(JOptionPane.PLAIN_MESSAGE);
  }


  @Override
  public void actionPerformed(ActionEvent event) {
    this.lastSetButton = event.getActionCommand();
  }


  public String getLastButton() {
    return this.lastSetButton;
  }


  public String showQuery() {
    JDialog dialog = this.createDialog("Make selection");
    dialog.setVisible(true);
    return this.lastSetButton;
  }


  public static void main(String[] args) {

    ButtonQueryDialog query = new ButtonQueryDialog(new String[] {"eHOST", "Knowtator"
        /* , "alpha", "beta", "gamma" */ }, "Alpha");

    String selectedButton = query.showQuery();
    System.out.println("SelectedButton:" + selectedButton);
    System.exit(0);
  }
}
