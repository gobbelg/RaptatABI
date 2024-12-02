package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.context;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

/**
 * A java swing application to use the ConTex algorithm.
 *
 * @author Oscar Ferrandez-Escamez Department of Biomedical Informatics, University of Utah, 2011
 */
public class RunApp extends JFrame implements ActionListener {

  /*
   * A Listener to clear the text fields.
   */
  class ClearListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

      RunApp.this.TextA.setText("");
      RunApp.this.concepts.setText("");
    }
  }

  /** */
  private static final long serialVersionUID = -4186090487211097687L;


  JButton Button = new JButton("ConText");

  JButton Button2 = new JButton("Clear");
  JTextField concepts = new JTextField(50);
  JTextArea TextA = new JTextArea(13, 50);

  JPanel bottomPanel = new JPanel();

  JPanel holdAll = new JPanel();

  public RunApp() {
    this.bottomPanel.setLayout(new FlowLayout());
    this.bottomPanel.add(this.Button);
    this.bottomPanel.add(this.Button2);

    JLabel a = new JLabel();
    a.setText("CONTEXT ALGORITHM IMPLEMENTATION");
    JPanel upper = new JPanel();
    upper.setLayout(new FlowLayout());
    upper.add(a);

    JLabel conc = new JLabel();
    conc.setText("Write target concept(s) (separated by commas):");

    JLabel se = new JLabel();
    se.setText("Write the sentence(s) (one sentence per line):");

    this.concepts.setText("Fever, headache, diabetes");

    JPanel mid = new JPanel();
    FlowLayout lmd = new FlowLayout();
    lmd.setAlignment(FlowLayout.LEFT);
    mid.setLayout(lmd);
    mid.add(conc);
    mid.add(this.concepts);
    mid.add(se);

    this.TextA
        .setText("Patient denies fever, but complains of headache.\nFamily history of diabetes.");
    this.TextA.setLineWrap(true);
    this.TextA.setWrapStyleWord(true);

    JScrollPane scroll =
        new JScrollPane(this.TextA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    mid.add(scroll);

    this.holdAll.setLayout(new BorderLayout());
    this.holdAll.add(this.bottomPanel, BorderLayout.SOUTH);
    this.holdAll.add(upper, BorderLayout.NORTH);
    this.holdAll.add(mid, BorderLayout.CENTER);
    this.holdAll.setBorder(new LineBorder(Color.white, 10));

    setTitle("ConText Algorithm Demo");

    getContentPane().add(this.holdAll, BorderLayout.CENTER);

    this.Button.addActionListener(this);

    this.Button2.addActionListener(new ClearListener());

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }


  /**
   * Each non abstract class that implements the ActionListener must have this method.
   *
   * @param e the action event.
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    if (e.getSource() == this.Button) // this button will call the ConText
    // algorithm
    {
      String[] sentences = this.TextA.getText().split("\n");
      System.out.println(sentences.toString());
      String[] mp = this.concepts.getText().split(", *");

      String context_output = "";

      // for each concept
      // identify the concept within each sentence and analyze its context
      for (int i = 0; i < mp.length; i++) {
        for (int j = 0; j < sentences.length; j++) {
          ConText myApplication = new ConText();
          ArrayList res = null;

          try {
            res = myApplication.applyContext(mp[i], sentences[j]);
          } catch (Exception e1) {
            e1.printStackTrace();
          }

          if (res != null) {
            context_output += "ConText for '" + res.get(0) + "':\n" + "Sentence: '" + res.get(1)
                + "'\n" + "Negation: '" + res.get(2) + "'\n" + "Temporality: '" + res.get(3) + "'\n"
                + "Experiencer: '" + res.get(4) + "'\n\n";
          }
        }
      }

      System.out.println(context_output);
      if (context_output == "") {
        context_output = "No concept(s) found in the sentence(s).";
      }
      // new window with the results
      smallFrame res = new smallFrame(context_output, mp);
    }
  }


  public static void main(String[] args) {
    RunApp myApplication = new RunApp();

    // Specify where will it appear on the screen:
    myApplication.setLocation(100, 100);
    myApplication.setSize(660, 400);
    myApplication.setResizable(false);

    // Show it!
    myApplication.setVisible(true);
  }
}
