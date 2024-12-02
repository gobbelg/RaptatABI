package src.main.gov.va.vha09.grecc.raptat.gg.importer.schema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class SchemaFilter {

  public File schemaFile;


  public SchemaFilter(File pontFile) {
    this.schemaFile = pontFile;
  }


  public SchemaFilter(String pathToPont) {
    this.schemaFile = new File(pathToPont);
  }


  public List<String> extractClasses(File pontFile) {
    Scanner scan = null;
    String token;
    String className;
    List<String> conceptsList = new Vector<>();
    try {
      scan = new Scanner(new FileReader(this.schemaFile));
      scan.nextLine(); // omit the first line which contains the date
      scan.nextLine(); // contains ;
      scan.nextLine(); // contains top level class
      while (scan.hasNext()) {
        scan.useDelimiter("defclass");
        token = scan.next();
        String[] strInf = token.split(" ");
        if (strInf[1].contains("(is-a")) {
          className = strInf[1].replace("(is-a", "");
        } else {
          className = strInf[1].trim();
        }

        System.out.println(className.replace("+", ""));
        conceptsList.add(className.replace("+", ""));
      }

    } catch (FileNotFoundException fe) {
      fe.printStackTrace();
    } finally {
      scan.close();
    }
    conceptsList.remove(0);
    conceptsList.remove(1);

    return conceptsList;
  }
}
