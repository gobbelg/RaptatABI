package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class PrintWriterToStringTester {
  public static void main(String[] args) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter(os);
    StringBuffer myString = new StringBuffer();

    for (int i = 0; i < 1000; i++) {
      String s = "This is my line number " + i + "\n";
      pw.write(s);
    }
  }
}
