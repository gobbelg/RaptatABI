/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.ss.sql.engine;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** @author sahask */
public class XLSXReader {

  public static HashMap<String, Long> getFileReferenceNoMap(String filePath) {
    String fileName = "";
    Long linkID = 0L;

    HashMap<String, Long> fileRefMap = new HashMap<>();

    try {
      FileInputStream file = new FileInputStream(new File(filePath));

      // Create Workbook instance holding reference to .xlsx file
      XSSFWorkbook workbook = new XSSFWorkbook(file);

      // Get first/desired sheet from the workbook
      XSSFSheet sheet = workbook.getSheetAt(0);

      // Iterate through each rows one by one
      Iterator<Row> rowIterator = sheet.iterator();
      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        // For each row, iterate through all the columns
        Iterator<Cell> cellIterator = row.cellIterator();

        while (cellIterator.hasNext()) {
          Cell cell = cellIterator.next();

          // Check the cell type and format accordingly
          switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
              linkID = Math.round(cell.getNumericCellValue());
              break;
            case Cell.CELL_TYPE_STRING:
              fileName = cell.getStringCellValue();
              break;
          }
        }

        fileRefMap.put(fileName, linkID);
      }
      file.close();
      workbook.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return fileRefMap;
  }


  public static void main(String[] args) {
    XLSXReader.getFileReferenceNoMap(
        "D:\\Saha Workspace" + "\\Corpus\\AnnotationID_TIUdocumentSID Correspondence.xlsx");
  }
}
