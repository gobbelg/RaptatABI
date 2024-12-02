package src.main.gov.va.vha09.grecc.raptat.gg.importer.schema;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;

public class EhostSchemaImporter extends SchemaImporter {

  public EhostSchemaImporter() {
    SchemaImporter.schemaApp = AnnotationApp.EHOST;
  }


  private void getAttributesWithValues(NodeList attributes, SchemaConcept schemaConcept) {
    Set<String> attributeValues = new HashSet<>();
    String attributeName = "";

    for (int i = 0; i < attributes.getLength(); i++) {
      Node node = attributes.item(i);
      String nodeName = node.getNodeName();

      if (nodeName.equals("Name")) {
        attributeName = node.getTextContent().toLowerCase();
      } else if (nodeName.equals("attributeDefOptionDef")) {
        attributeValues.add(node.getTextContent().toLowerCase());
      } else if (nodeName.equals("defaultValue")) {
        schemaConcept.setDefaultAttributeValue(attributeName, node.getTextContent().toLowerCase());
      }
    }

    /*
     * Set the default to null if there is no default. This will be used so that each attribute has
     * at least one attribute value
     */
    if (schemaConcept.getDefaultAttributeValue(attributeName) == null) {
      schemaConcept.setDefaultAttributeValue(attributeName, RaptatConstants.NULL_ATTRIBUTE_VALUE);
      attributeValues.add(RaptatConstants.NULL_ATTRIBUTE_VALUE);
    }

    /*
     * A NULL_ATTRIBUTE_VALUE should always be available for assigning when the attribute is
     * unassigned by an annotator
     */
    if (!attributeValues.contains(RaptatConstants.NULL_ATTRIBUTE_VALUE)) {
      attributeValues.add(RaptatConstants.NULL_ATTRIBUTE_VALUE);
    }

    schemaConcept.addAttributeValues(attributeName, new ArrayList<>(attributeValues));
  }


  @SuppressWarnings("null")
  private SchemaConcept getRelationshipAttributes(NodeList relationshipAttributes) {
    SchemaConcept schemaConcept = null;

    for (int i = 0; i < relationshipAttributes.getLength(); i++) {
      String nodeName = relationshipAttributes.item(i).getNodeName();

      if (nodeName.equals("Name")) {
        String className = relationshipAttributes.item(i).getTextContent().toLowerCase();
        schemaConcept = new SchemaConcept(className, true);
      } else if (nodeName.equals("Definition")) {
        // parsing the definition tag to figure out the concepts related
        // to the relation
        String[] concepts = relationshipAttributes.item(i).getTextContent().split("\\(\\(|\\)\\)");

        // To figure out sets to left and right side concepts
        List<String> left = new ArrayList<>();
        List<String> right = new ArrayList<>();

        boolean leftFound = false; // left false means we are searching
        // for left side concept set
        // left true means we are now searching for right side concept
        // set

        for (int k = 0; k < concepts.length; k++) {
          if (concepts[k].length() > 0) {
            if (leftFound) {
              right = Arrays.asList(concepts[k].split("\\)\\|\\("));
              break;
            } else {
              left = Arrays.asList(concepts[k].split("\\)\\|\\("));
              leftFound = true;
            }
          }
        }

        schemaConcept.addLinkedFromConceptsToRelation(left);
        schemaConcept.addLinkedToConceptsToRelation(right);
      } else if (nodeName.equals("attributeDef")) {
        getAttributesWithValues(relationshipAttributes.item(i).getChildNodes(), schemaConcept);
      }
    }

    return schemaConcept;
  }


  private SchemaConcept getSchemaConceptAttributes(NodeList conceptAttributes,
      SchemaConcept publicAttributesConcept) {
    boolean inheritPublicAttribute = true;
    String className;

    SchemaConcept schemaConcept = null;

    for (int i = 0; i < conceptAttributes.getLength(); i++) {
      String nodeName = conceptAttributes.item(i).getNodeName();

      if (nodeName.equals("Name")) {
        className = conceptAttributes.item(i).getTextContent().toLowerCase();
        schemaConcept = new SchemaConcept(className, false);
      } else if (nodeName.equalsIgnoreCase("InHerit_Public_Attributes")) {
        inheritPublicAttribute =
            conceptAttributes.item(i).getTextContent().toLowerCase().equals("true");
      } else if (nodeName.equals("attributeDef")) {
        getAttributesWithValues(conceptAttributes.item(i).getChildNodes(), schemaConcept);
      }
    }

    if (inheritPublicAttribute) {
      String attributeName;
      List<String> publicAttributeValues;

      for (Map.Entry<String, List<String>> entry : publicAttributesConcept.getAttributeValueMap()
          .entrySet()) {
        attributeName = entry.getKey();
        publicAttributeValues = entry.getValue();

        String defaultAttributeValue =
            publicAttributesConcept.getDefaultAttributeValue(attributeName);
        if (defaultAttributeValue == null) {
          defaultAttributeValue = RaptatConstants.NULL_ATTRIBUTE_VALUE;
          publicAttributeValues.add(defaultAttributeValue);
        }
        schemaConcept.addAttributeValues(attributeName, publicAttributeValues);
        schemaConcept.setDefaultAttributeValue(attributeName, defaultAttributeValue);
      }
    }

    return schemaConcept;
  }


  private List<SchemaConcept> parseProjectSchema(Document eHostXMLDocument) {
    // Creating the public attributes list
    List<SchemaConcept> schemaConcepts = new ArrayList<>();

    NodeList nodes = eHostXMLDocument.getElementsByTagName("Relationship_Rules");

    for (int j = 0; j < nodes.getLength(); j++) {
      NodeList publicAttributesList = nodes.item(j).getChildNodes();

      for (int i = 0; i < publicAttributesList.getLength(); i++) {
        if (publicAttributesList.item(i).getNodeName().equals("Relationship_Rule")) {
          schemaConcepts
              .add(getRelationshipAttributes(publicAttributesList.item(i).getChildNodes()));
        }
      }
    }

    // Reading the public attributes
    nodes = eHostXMLDocument.getElementsByTagName("attributeDefs");
    SchemaConcept publicAttributes = new SchemaConcept("public", false);

    for (int j = 0; j < nodes.getLength(); j++) {
      NodeList publicAttributesList = nodes.item(j).getChildNodes();

      for (int i = 0; i < publicAttributesList.getLength(); i++) {
        if (publicAttributesList.item(i).getNodeName().equals("attributeDef")) {
          getAttributesWithValues(publicAttributesList.item(i).getChildNodes(), publicAttributes);
        }
      }
    }

    // Creating the separate class attribute list
    nodes = eHostXMLDocument.getElementsByTagName("classDefs");

    for (int j = 0; j < nodes.getLength(); j++) {
      NodeList conceptLists = nodes.item(j).getChildNodes();

      for (int i = 0; i < conceptLists.getLength(); i++) {
        if (conceptLists.item(i).getNodeName().equals("classDef")) {
          schemaConcepts.add(
              getSchemaConceptAttributes(conceptLists.item(i).getChildNodes(), publicAttributes));
        }
      }
    }

    return schemaConcepts;
  }


  public static List<SchemaConcept> importSchemaConcepts(File schemaFile) {
    try {
      EhostSchemaImporter ehsImporter = new EhostSchemaImporter();
      DocumentBuilderFactory documentfactory = DocumentBuilderFactory.newInstance();
      documentfactory.setIgnoringElementContentWhitespace(true);
      Document doc = documentfactory.newDocumentBuilder().parse(schemaFile);
      return ehsImporter.parseProjectSchema(doc);
    } catch (SAXException SAXe) {
      SAXe.printStackTrace();
    } catch (IOException IOe) {
      IOe.printStackTrace();
    } catch (ParserConfigurationException PCe) {
      PCe.printStackTrace();
    }

    return null;
  }


  public static List<SchemaConcept> importSchemaConcepts(String xmlPath) {
    if (xmlPath != null & xmlPath.length() > 0) {
      File xmlFile = new File(xmlPath);
      return importSchemaConcepts(xmlFile);
    }

    return null;
  }


  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
}
