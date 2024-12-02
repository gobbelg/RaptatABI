package src.main.gov.va.vha09.grecc.raptat.kg.candidates;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ReadPDF {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    try {

      File file = new File("C:/Westinghouse Standard Tech Specs Vol 1.pdf");
      PDDocument document = PDDocument.load(file);
      PDFTextStripper pdfStripper = new PDFTextStripper();
      String text = pdfStripper.getText(document);
      System.out.println(text);
      document.close();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
