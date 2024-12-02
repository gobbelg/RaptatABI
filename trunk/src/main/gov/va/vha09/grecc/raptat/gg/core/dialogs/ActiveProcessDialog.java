/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;

/**
 * A dialog that shows no input and allows a background processing task. The dialog stays visible
 * until the task is finished.
 *
 * @author glenn
 */
public class ActiveProcessDialog extends javax.swing.JDialog {

  /** */
  private static final long serialVersionUID = 3201705558847369158L;

  private static final Logger LOGGER = Logger.getLogger(ActiveProcessDialog.class);


  private final String pathToImageIcon;

  private javax.swing.JLabel jLabel2;

  private javax.swing.JTextField jTextField1;

  // End of variables declaration

  /**
   * Creates new form RaptatTrainingDialog
   *
   * @param parent
   * @param pathToImage
   * @param dialogTitle
   * @param dialogText
   */
  public ActiveProcessDialog(Window parent, String pathToImage, String dialogTitle,
      String dialogText) {
    super(parent, Dialog.ModalityType.MODELESS);
    this.pathToImageIcon = pathToImage;
    initComponents();
    getContentPane().setBackground(Color.WHITE);
    setTitle(dialogTitle);
    this.jTextField1.setText(dialogText);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    pack();
    int x = parent.getX() + (parent.getWidth() - getWidth()) / 2;
    int y = parent.getY() + (parent.getHeight() - getHeight()) / 2;
    this.setLocation(x, y);
  }


  /** */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {
    this.jTextField1 = new javax.swing.JTextField();
    this.jLabel2 = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setAlwaysOnTop(true);
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

    this.jTextField1.setEditable(false);
    this.jTextField1.setBackground(null);
    this.jTextField1.setFont(new java.awt.Font("Calibri", 0, 18)); // NOI18N
    this.jTextField1.setBorder(null);

    InputStream imageResource = this.getClass().getResourceAsStream(this.pathToImageIcon);
    BufferedImage bufferedImage = null;
    try {
      bufferedImage = ImageIO.read(imageResource);
      Image scaledImg = bufferedImage.getScaledInstance(-1, 75, 4);
      this.jLabel2.setBackground(null);
      this.jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 18));
      this.jLabel2.setIcon(new ImageIcon(scaledImg));
    } catch (IOException e) {
      System.out.println("Unable to read image for interface " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup().addGap(27, 27, 27)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(this.jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 113,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(this.jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 450,
                    javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(38, Short.MAX_VALUE)));
    layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
            layout.createSequentialGroup().addContainerGap(16, Short.MAX_VALUE)
                .addComponent(this.jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 82,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(this.jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 59,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)));
  }


  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /*
     * Create and display the dialog
     */
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        String iconImagePath = "/src/main/resources/gears.png";
        String dialogTitle = "RapTAT Annotating";
        String dialogText = "Please wait while RapTAT training updates . . .";
        ActiveProcessDialog dialog =
            new ActiveProcessDialog(null, iconImagePath, dialogTitle, dialogText);
        dialog.setVisible(true);
        dialog.update(dialog.getGraphics());
        for (int i = 0; i < 100000; i++) {
          System.out.print(i);
        }
        dialog.setVisible(false);
        System.exit(0);
      }
    });
  }
}
