package src.main.gov.va.vha09.grecc.raptat.gg.importer.schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class KnowtatorSchemaImporter extends SchemaImporter {

  private class ConceptTreeNode {

    private String key = "";
    private List<String> values = new ArrayList<>();
    private final List<ConceptTreeNode> childNodes = new LinkedList<>();


    private ConceptTreeNode(String readLine) {
      if (readLine.length() > 0) {
        setKeyAndValues(readLine);
      }
    }


    private String getChildren(BufferedReader reader, String readLine, int depth) {
      ConceptTreeNode lastNode = null;
      int startTabs = countStartTabs(readLine);

      try {
        while (readLine != null && startTabs >= depth) {
          if (startTabs == depth) {
            ConceptTreeNode childNode = new ConceptTreeNode(readLine);
            this.childNodes.add(childNode);
            lastNode = childNode;
            do {
              readLine = reader.readLine();
            } while (readLine != null && !readLine.matches("(?m).*[(]\\S+.*"));
          }
          if (startTabs > depth) {
            readLine = lastNode.getChildren(reader, readLine, startTabs);
          }

          if (readLine != null) {
            startTabs = countStartTabs(readLine);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      return readLine;
    }


    private String reformatString(String str) {
      /*
       * Discard ";+" characters used as continuation code in Protege schema
       */
      str = str.replaceAll("(?m)^;\\+", "");

      /* Replace tabs and opening parenthesis at start of string */
      str = str.replaceAll("^(?:;[+])*(\\t*)[(]", "");

      /* Replace training closing parentheses */
      str = str.replaceAll("(?m)[)]+\\s*\\z", "");

      /* Replace any escape-quote character sequence with single quote */
      str = str.replaceAll("(?m)\\\\\"", "'");

      /* Replace characters encoded by unicode within Protege */
      str = str.replaceAll("%28", "(");
      str = str.replaceAll("%29", ")");
      str = str.replaceAll("%2C", ",");
      str = str.replaceAll("%2F", "/");
      str = str.replaceAll("%3A", ":");
      str = str.replaceAll("%3D", "=");

      return str;
    }


    private void setKeyAndValues(String charString) {
      charString = reformatString(charString);

      /* Key is first string of characters followed by a space */
      List<String> quoteMatchList = new ArrayList<>();
      try {
        Matcher quoteMatcher = KnowtatorSchemaImporter.quoteCatcher.matcher(charString);
        while (quoteMatcher.find()) {
          quoteMatchList.add(quoteMatcher.group(1).trim());
        }

        /*
         * Replace all text within quotes to allow us to find all other text delimited by spaces.
         */
        String quoteReplacedString = charString.replaceAll("(?m)[\"]\\S[^\"]*[\"]",
            KnowtatorSchemaImporter.QUOTE_REPLACEMENT);
        String[] lineTokens = quoteReplacedString.split("\\s+");
        Iterator<String> quoteMatchIterator = quoteMatchList.iterator();

        if (lineTokens.length > 0
            && lineTokens[0].equals(KnowtatorSchemaImporter.QUOTE_REPLACEMENT)) {
          this.key = quoteMatchIterator.next().replaceAll("[+]", " ");
        } else {
          this.key = lineTokens[0].replaceAll("[+]", " ");
        }

        this.values = new ArrayList<>(lineTokens.length - 1);

        for (int i = 1; i < lineTokens.length; i++) {
          String curValue = lineTokens[i].equals(KnowtatorSchemaImporter.QUOTE_REPLACEMENT)
              ? quoteMatchIterator.next().replaceAll("[+]", " ")
              : lineTokens[i].replaceAll("[+]", " ");
          this.values.add(curValue);
        }
      } catch (PatternSyntaxException ex) {
        GeneralHelper.errorWriter("Knowtator schema format exception");
      }
    }
  }

  private static final Pattern quoteCatcher =
      Pattern.compile("[\"](\\S[^\"]*)[\"]", Pattern.MULTILINE);

  private static final Pattern tabCatcher = Pattern.compile("(^(?:;[+])*(\t+))", Pattern.MULTILINE);

  private static final String QUOTE_REPLACEMENT = "____NO_QUOTE___";

  protected static Logger logger = Logger.getLogger(KnowtatorSchemaImporter.class);

  static List<RaptatPair<String, String>> allPairs = new ArrayList<>();


  /*
   * Promoted the method getSchemaFile to the enclosing Abstract class, SchemaImporter
   */

  public KnowtatorSchemaImporter() {
    SchemaImporter.schemaApp = AnnotationApp.KNOWTATOR;
    KnowtatorSchemaImporter.logger.setLevel(Level.INFO);
  }


  private ConceptTreeNode buildNodeTree(BufferedReader reader) {
    ConceptTreeNode rootNode = new ConceptTreeNode("");

    String readLine;
    try {
      do {
        readLine = reader.readLine();
      } while (!readLine.matches("(?m).*[(]\\S+.*"));

      rootNode.getChildren(reader, readLine, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return rootNode;
  }


  private int countStartTabs(String readLine) {

    Matcher tabMatcher = KnowtatorSchemaImporter.tabCatcher.matcher(readLine);

    if (tabMatcher.find()) {
      return tabMatcher.group(2).length();
    }

    return 0;
  }


  private SchemaConcept createSchemaConcept(ConceptTreeNode inputNode,
      HashMap<String, SchemaConcept> schemaConceptMap) {
    SchemaConcept schemaParentConcept;
    SchemaConcept relation;

    String key;
    String conceptValue;
    String parentConceptName = "";
    String conceptName = inputNode.values.get(0).toLowerCase();

    boolean isRelation;
    HashSet<String> attrValues;

    SchemaConcept schemaConcept = new SchemaConcept(conceptName, false);

    for (ConceptTreeNode node : inputNode.childNodes) {
      key = node.key;
    }

    for (ConceptTreeNode node : inputNode.childNodes) {
      key = node.key;

      if (key.equals("is-a")) {
        parentConceptName = node.values.get(0).toLowerCase();
        schemaParentConcept = schemaConceptMap.get(parentConceptName);

        if (schemaParentConcept != null) {
          KnowtatorSchemaImporter.allPairs.add(new RaptatPair<>(parentConceptName, conceptName));

          for (Map.Entry<String, List<String>> entry : schemaParentConcept.getAttributeValueMap()
              .entrySet()) {
            schemaConcept.addAttributeValues(entry.getKey(), entry.getValue());
          }
        }
      } else if (key.equals("multislot") || key.equals("single-slot")) {
        attrValues = new HashSet<>();
        isRelation = getAttributeValues(node, attrValues);

        conceptValue = node.values.get(0).toLowerCase();

        if (isRelation) {
          if ((relation = schemaConceptMap.get(conceptValue)) == null) {
            relation = new SchemaConcept(conceptValue, true);
            relation.addLinkedToConceptsToRelation(new ArrayList<>(attrValues));
            schemaConceptMap.put(conceptValue, relation);
          }

          // this is added so that the Top lavel slot class is not
          // added
          if (!inputNode.values.get(0).toLowerCase()
              .equalsIgnoreCase(":clips_top_level_slot_class")) {
            relation.addLinkedFromConceptToRelation(inputNode.values.get(0));
          }
        } else {
          schemaConcept.addAttributeValues(conceptValue, new ArrayList<>(attrValues));
        }
      }
    }

    return schemaConcept;
  }


  private void getAttributesWithValues(NodeList attributes, SchemaConcept concept) {
    List<String> attributeValues = new ArrayList<>();
    String name = "";

    for (int i = 0; i < attributes.getLength(); i++) {
      String nodeName = attributes.item(i).getNodeName();
      if (nodeName.equals("Name")) {
        name = attributes.item(i).getTextContent();
      } else if (nodeName.equals("attributeDefOptionDef")) {
        attributeValues.add(attributes.item(i).getTextContent().toLowerCase());
      }
    }

    attributeValues.add(RaptatConstants.NULL_ATTRIBUTE_VALUE);

    concept.addAttributeValues(name, attributeValues);
  }


  // returns true if it is a relation
  private boolean getAttributeValues(ConceptTreeNode rootNode, HashSet<String> attrValues) {
    boolean isRelation = false;
    String key;

    for (ConceptTreeNode node : rootNode.childNodes) {
      key = node.key;

      if (key.equals("type") && node.values.get(0).equals("INSTANCE")) {
        isRelation = true;
      } else if (key.equals("value") || key.equals("allowed-values")) {
        for (String str : node.values) {
          attrValues.add(str);
        }
      } else if (key.equals("allowed-classes")) {
        for (String str : node.values) {
          attrValues.add(str);
        }
      }
    }

    return isRelation;
  }


  @SuppressWarnings("null")
  private SchemaConcept getConceptAttributes(NodeList conceptAttributes,
      SchemaConcept publicAttributes) {
    boolean inheritPublicAttribute = true;
    String className;

    SchemaConcept concept = null;

    for (int i = 0; i < conceptAttributes.getLength(); i++) {
      String nodeName = conceptAttributes.item(i).getNodeName();

      if (nodeName.equals("Name")) {
        className = conceptAttributes.item(i).getTextContent();
        // CHECKME: Are there repercussions to removing .toLowerCase()
        // method call?
        /*
         * Modified by Glenn Gobbel on 01-16-15 Removed call .toLowerCase()
         */
        // concept = new Concept( className.toLowerCase() );
        concept = new SchemaConcept(className, false);
      } else if (nodeName.equalsIgnoreCase("InHerit_Public_Attributes")) {
        inheritPublicAttribute =
            conceptAttributes.item(i).getTextContent().toLowerCase().equals("true");
      } else if (nodeName.equals("attributeDef")) {
        getAttributesWithValues(conceptAttributes.item(i).getChildNodes(), concept);
      }
    }

    if (inheritPublicAttribute) {
      String attributeName;
      List<String> publicAttributeValues;

      for (Map.Entry<String, List<String>> entry : publicAttributes.getAttributeValueMap()
          .entrySet()) {
        attributeName = entry.getKey();
        publicAttributeValues = entry.getValue();

        concept.addAttributeValues(attributeName, publicAttributeValues);
      }
    }

    return concept;
  }


  @SuppressWarnings("null")
  private SchemaConcept getRelationshipAttributes(NodeList relationshipAttributes) {
    String className;
    SchemaConcept concept = null;

    for (int i = 0; i < relationshipAttributes.getLength(); i++) {
      String nodeName = relationshipAttributes.item(i).getNodeName();

      if (nodeName.equals("Name")) {
        className = relationshipAttributes.item(i).getTextContent();
        concept = new SchemaConcept(className, true);
      } else if (nodeName.equals("attributeDef")) {
        getAttributesWithValues(relationshipAttributes.item(i).getChildNodes(), concept);
      }
    }

    return concept;
  }


  private BufferedReader getSchemaReader(File schemaFile) {
    Charset encoding = Charset.forName("UTF-8");
    BufferedReader reader = null;

    // InputStream in;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(schemaFile), encoding));

      /* Count lines up to first line after one or more blank lines */
      String readLine;
      int linesToRead = 0;
      do {
        readLine = reader.readLine();
        linesToRead++;
      } while (!readLine.equals(""));

      do {
        readLine = reader.readLine();
        linesToRead++;
      } while (!readLine.startsWith("("));

      /* Skip lines up to first line to be read */
      reader.close();
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(schemaFile), encoding));

      for (int i = 1; i < linesToRead; i++) {
        reader.readLine();
      }

    } catch (FileNotFoundException ex) {
      GeneralHelper.errorWriter("Unable to read schema file");
      ex.printStackTrace();
    } catch (IOException ex) {
      GeneralHelper.errorWriter("Unable to read schema file");
      ex.printStackTrace();
    }

    return reader;
  }


  private void prepareSchemaConceptList(ConceptTreeNode parentNode, String tab,
      HashMap<String, SchemaConcept> schemaConceptMap) {

    // System.out.println(tab + "key: " + parentNode.key);
    SchemaConcept schemaConcept;

    if (parentNode.key.equals("defclass")) {
      schemaConcept = createSchemaConcept(parentNode, schemaConceptMap);

      schemaConceptMap.put(schemaConcept.getConceptName(), schemaConcept);
    }

    for (ConceptTreeNode node : parentNode.childNodes) {
      prepareSchemaConceptList(node, tab + "\t", schemaConceptMap);
    }
  }


  private void printSchemaConceptMap(HashMap<String, SchemaConcept> schemaConceptMap) {
    SchemaConcept c;
    for (String key : schemaConceptMap.keySet()) {
      c = schemaConceptMap.get(key);
      if (c.isRelationship()) {
        continue;
      }

      System.out.println("concepts: " + c.getConceptName());

      for (Map.Entry<String, List<String>> s : c.getAttributeValueMap().entrySet()) {
        System.out.print("\t" + s.getKey() + ":");

        for (String str : s.getValue()) {
          System.out.print(" " + str);
        }
        System.out.println("");
      }
      System.out.println("\n");
    }

    System.out.println("\n");
    for (String key : schemaConceptMap.keySet()) {
      c = schemaConceptMap.get(key);
      // if (!c.isRelation()) {
      // continue;
      // }

      System.out.println("Relation: " + c.getConceptName());

      for (Map.Entry<String, List<String>> s : c.getAttributeValueMap().entrySet()) {
        System.out.print("\t" + s.getKey() + ":");

        for (String str : s.getValue()) {
          System.out.print(" " + str);
        }
        System.out.println("");
      }
      System.out.println("\n");
    }
  }


  public static List<SchemaConcept> importSchemaConcepts(File schemaFile) {
    KnowtatorSchemaImporter ksiImporter = new KnowtatorSchemaImporter();
    List<SchemaConcept> schemaConceptList = new ArrayList<>();
    BufferedReader reader = ksiImporter.getSchemaReader(schemaFile);
    HashMap<String, SchemaConcept> schemaConceptMap = new HashMap<>();

    // building tree
    if (reader != null) {
      ConceptTreeNode rootNode = ksiImporter.buildNodeTree(reader);

      try {
        reader.close();
      } catch (IOException ex) {
        GeneralHelper.errorWriter("Unable to close schema reader object");
        ex.printStackTrace();
      }

      ksiImporter.prepareSchemaConceptList(rootNode, "", schemaConceptMap);
    }

    // creating the conceptList
    SchemaConcept c;
    for (Map.Entry<String, SchemaConcept> entry : schemaConceptMap.entrySet()) {
      c = entry.getValue();

      schemaConceptList.add(c);
    }

    return schemaConceptList;
  }


  public static List<SchemaConcept> importSchemaConcepts(String schemaFilePath) {
    return importSchemaConcepts(new File(schemaFilePath));
  }


  @SuppressWarnings("Convert2Lambda")
  public static void main(String[] args) {
    KnowtatorSchemaImporter importer = new KnowtatorSchemaImporter();
    String schemaPath =
        "C:\\Users\\gobbelgt\\SubversionMatheny\\branches\\GlennWorkspace\\Resources"
            + "\\StressTestSchemaAndExamples\\AnnotationComparisonforRaptatDebugging\\ProjectSchema\\NuclearImagingSchema_Updated141104.pont";
    File schemaFile = new File(schemaPath);
    // File schemaFile = new File(
    // "C:\\Sanjib\\ComparisonForSanjib\\ProjectSchema\\NuclearImagingSchema_Updated141104.pont"
    // );

    List<SchemaConcept> schemaConceptList = SchemaImporter.importSchemaConcepts(schemaFile);
    //
    // System.out.println( "\n\n\n" );
    for (SchemaConcept c : schemaConceptList) {
      System.out.println("\n\n============================================\n" + c.getConceptName()
          + (c.isRelationship() ? " isRelationship" : "")
          + "\n============================================");

      int i = 0;
      for (String a : c.getAttributeValueMap().keySet()) {
        System.out.println("\n    Attribute_" + i++ + ":" + a);

        int j = 0;
        for (String s : c.getAttributeValueMap().get(a)) {
          System.out.println("      Value_" + j++ + ":" + s);
        }
      }

      if (c.isRelationship()) {
        System.out.println("\n    LinkedFrom:");
        for (String s : c.getLinkedFromConcepts()) {
          System.out.println("      " + s);
        }

        System.out.println("\n    LinkedTo:");
        for (String s : c.getLinkedToConcepts()) {
          System.out.println("      " + s);
        }
      }
    }
  }
}
