package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.context;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * A java swing application to use the ConTex algorithm.
 *
 * @author Oscar Ferrandez-Escamez Department of Biomedical Informatics, University of Utah, 2011
 */
public class smallFrame extends JFrame implements ActionListener {

  // private subclass -- highlight painter
  class AppHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
    public AppHighlightPainter(Color color) {
      super(color);
    }
  }

  /** */
  private static final long serialVersionUID = 4312098593410617974L;

  JButton Button = new JButton("close");
  JTextArea TextA = new JTextArea();
  JPanel bottomPanel = new JPanel();

  JPanel holdAll = new JPanel();

  // private subclass -- highlight painter
  Highlighter.HighlightPainter appHighlightPainter = new AppHighlightPainter(Color.pink);


  public smallFrame(String s, String[] cp) {
    this.TextA.setText(s);
    for (int i = 0; i < cp.length; i++) {
      highlight(this.TextA, "'" + cp[i].toLowerCase() + "'");
    }

    this.TextA.setEditable(false);
    this.TextA.setLineWrap(true);
    this.TextA.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane(this.TextA);

    this.bottomPanel.setLayout(new FlowLayout());
    this.bottomPanel.add(this.Button);

    JLabel a = new JLabel();
    a.setText("CONTEXT ALGORITHM RESULTS");
    JPanel upper = new JPanel();
    upper.setLayout(new FlowLayout());
    upper.add(a);

    this.holdAll.setLayout(new BorderLayout());
    this.holdAll.add(upper, BorderLayout.NORTH);
    this.holdAll.add(this.bottomPanel, BorderLayout.SOUTH);
    this.holdAll.add(scroll, BorderLayout.CENTER);
    this.holdAll.setBorder(new LineBorder(Color.white, 10));

    getContentPane().add(this.holdAll, BorderLayout.CENTER);

    this.Button.addActionListener(this);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    this.setLocation(200, 200);
    this.setSize(500, 300);
    setVisible(true);
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == this.Button) {
      setVisible(false);
    }
  }


  // highlight the concepts in the results frame
  public void highlight(JTextComponent textComp, String pattern) {
    // remove old
    try {
      Highlighter hilite = textComp.getHighlighter();
      Document doc = textComp.getDocument();
      String text = doc.getText(0, doc.getLength()).toLowerCase();
      int pos = 0;

      // Search for concepts and add into the highlighter
      while ((pos = text.indexOf(pattern, pos)) >= 0) {
        hilite.addHighlight(pos, pos + pattern.length(), this.appHighlightPainter);
        pos += pattern.length();
      }
    } catch (BadLocationException e) {
    }
  }
}
