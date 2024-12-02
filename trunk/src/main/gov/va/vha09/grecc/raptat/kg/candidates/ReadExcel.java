package src.main.gov.va.vha09.grecc.raptat.kg.candidates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcel {

  public static void main(String[] args) {
    // TODO Auto-generated method stub

    File file = new File("C:/INL-OSSREM.xlsx");
    FileInputStream fis;
    try {
      fis = new FileInputStream(file);
      XSSFWorkbook wb = new XSSFWorkbook(fis);

      XSSFSheet sh = wb.getSheet("CONFIG");
      System.out.println(sh.getLastRowNum());
      System.out.println("Name: " + sh.getSheetName());

      for (int i = 0; i <= sh.getLastRowNum(); i++) {
        Row row = sh.getRow(i);
        System.out.println(row.getRowNum());
        for (int j = 0; j < row.getLastCellNum(); j++) {
          System.out.println("Val: " + row.getCell(j).getStringCellValue());
        }
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
