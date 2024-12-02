/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

// TODO: Need to add facility to respond directly to user selecting or de-selecting
// check box rather than waiting until user closes dialog to process
/**
 * Class to create a dialog for the user to select from a list using check boxes
 *
 * @author Glenn T. Gobbel Nov 21, 2016
 */
public class CheckboxQueryDialog {
  private static final long serialVersionUID = -646364584311656215L;


  private List<JCheckBox> queryCheckboxes;

  public JPanel mainPanel;

  private Component parentComponent;

  /**
   * Creates a query box with checkboxes provided in a list. The list is a list of pairs where the
   * left is a checkbox to be added and the right side of the pair indicates whether the checkbox is
   * enabled.
   *
   * @param checkBoxes
   * @param title
   */
  public CheckboxQueryDialog(Component parentComponent,
      List<RaptatPair<JCheckBox, Boolean>> checkBoxes, String title) {
    this.parentComponent = parentComponent;
    int checkNumber = checkBoxes.size();
    this.queryCheckboxes = new ArrayList<>(checkNumber);
    Set<String> nameSet = new HashSet<>(checkNumber);

    this.mainPanel = new JPanel();
    Box queryPanel = Box.createVerticalBox();
    queryPanel.setBorder(BorderFactory.createTitledBorder(title));
    Dimension querySize = getQueryDimension(checkBoxes, title);
    queryPanel.setMinimumSize(querySize);
    queryPanel.setPreferredSize(querySize);
    queryPanel.setMaximumSize(querySize);
    this.mainPanel.add(queryPanel);
    ListIterator<RaptatPair<JCheckBox, Boolean>> checkBoxIterator = checkBoxes.listIterator();

    if (checkBoxes.size() > 0) {
      while (checkBoxIterator.hasNext()) {
        RaptatPair<JCheckBox, Boolean> checkBox = checkBoxIterator.next();
        String checkBoxName = checkBox.left.getText();
        if (!nameSet.contains(checkBoxName)) {
          nameSet.add(checkBoxName);
          this.queryCheckboxes.add(checkBox.left);
          checkBox.left.setSelected(false);
          checkBox.left.setEnabled(checkBox.right);
          queryPanel.add(checkBox.left);
        }
      }
    }
  }


  public int showQuery() {
    int queryResult = JOptionPane.showConfirmDialog(this.parentComponent, this.mainPanel,
        "Make selection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (queryResult == JOptionPane.OK_OPTION && !checkBoxChecked()) {
      return JOptionPane.CANCEL_OPTION;
    }
    return queryResult;
  }


  private boolean checkBoxChecked() {
    for (JCheckBox checkBox : this.queryCheckboxes) {
      if (checkBox.isSelected()) {
        return true;
      }
    }
    return false;
  }


  /**
   * @param checkBoxes
   * @param title
   * @return
   */
  private Dimension getQueryDimension(List<RaptatPair<JCheckBox, Boolean>> checkBoxes,
      String title) {
    int maxStringLength = title.length();

    for (RaptatPair<JCheckBox, Boolean> checkBox : checkBoxes) {
      int nameLength = checkBox.left.getText().length();
      maxStringLength = nameLength > maxStringLength ? nameLength : maxStringLength;
    }
    return new Dimension(10 + maxStringLength * 12, 20 + checkBoxes.size() * 30);
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        String[] names = new String[] {"General", "Assertion", "ReasonNoMeds"};
        List<RaptatPair<JCheckBox, Boolean>> checkBoxes = new ArrayList<>();
        for (String name : names) {
          Boolean setEnabled = name.equals("General") || name.equals("Assertion");
          checkBoxes.add(new RaptatPair<>(new JCheckBox(name), setEnabled));
        }

        CheckboxQueryDialog cqd = new CheckboxQueryDialog(null, checkBoxes, "test");

        int queryResult = cqd.showQuery();

        if (queryResult == JOptionPane.OK_OPTION) {
          for (RaptatPair<JCheckBox, Boolean> checkBox : checkBoxes) {
            System.out.println(checkBox.left.getText() + " Selected:" + checkBox.left.isSelected());
          }

        } else {
          System.out.println("User canceled");
        }
        System.exit(0);
      }
    });
  }
}
