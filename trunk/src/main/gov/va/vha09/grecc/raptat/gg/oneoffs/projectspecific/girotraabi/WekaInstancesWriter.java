/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author VHATVHGOBBEG
 *
 */
public class WekaInstancesWriter {

  private PrintWriter pw;
  private Instances instances;


  public WekaInstancesWriter(final String outputDirectoryPath, final String fileName,
      final Instances instances) {
    this.instances = instances;
    File outputFile = new File(outputDirectoryPath + File.separator + fileName);
    try {
      this.pw = new PrintWriter(outputFile);
    } catch (FileNotFoundException e) {
      System.err.println(
          "Unable to generate printwriter for writing Weka instances\n" + e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }

  /**
   *
   */
  private WekaInstancesWriter() {}

  public void writeInstancesToFile() {

    Attribute classAttribute = this.instances.classAttribute();
    Enumeration<Attribute> attributeEnumerator = this.instances.enumerateAttributes();

    writeHeader(classAttribute, attributeEnumerator);

    /* Print instances and their attribute values */
    Iterator<Instance> instancesIterator = this.instances.iterator();

    while (instancesIterator.hasNext()) {
      Instance instance = instancesIterator.next();
      attributeEnumerator = this.instances.enumerateAttributes();

      if (attributeEnumerator.hasMoreElements()) {
        Attribute attribute = attributeEnumerator.nextElement();
        String featureValue = instance.isMissing(attribute) ? "0" : instance.stringValue(attribute);
        this.pw.print(featureValue);
      }

      while (attributeEnumerator.hasMoreElements()) {
        Attribute attribute = attributeEnumerator.nextElement();
        String featureValue = instance.isMissing(attribute) ? "0" : instance.stringValue(attribute);
        this.pw.print("\t");
        this.pw.print(featureValue);
      }

      /* Print class attribute as enumerator does not output class attribute */
      String featureValue =
          instance.isMissing(classAttribute) ? "ERROR" : instance.stringValue(classAttribute);
      this.pw.print("\t");
      this.pw.print(featureValue);

      this.pw.println();
      this.pw.flush();
    }

    this.pw.close();
  }

  /**
   * Write header line of attribute names
   *
   * @param classAttribute
   * @param attributeEnumerator
   */
  private void writeHeader(final Attribute classAttribute,
      final Enumeration<Attribute> attributeEnumerator) {
    /* Print header of attribute names */
    {
      if (attributeEnumerator.hasMoreElements()) {
        Attribute attribute = attributeEnumerator.nextElement();
        this.pw.print(attribute.name());
      }

      while (attributeEnumerator.hasMoreElements()) {
        Attribute attribute = attributeEnumerator.nextElement();
        this.pw.print("\t");
        this.pw.print(attribute.name());
      }
    }

    /* Print class attribute as enumerator does not output class attribute */
    this.pw.print("\t");
    this.pw.print(classAttribute.name());
    this.pw.println();
  }


  /**
   * @param args
   */
  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }

}
