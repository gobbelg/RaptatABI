/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.ss.sql.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.AttrValueDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.AttributeDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.ConceptDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.LinkTypeDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SchemasDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/** @author VHATVHSAHAS1 */
public class SchemaParser {
  List<SchemaConcept> _schemaConcepts;

  String _schemaName;

  public SchemaParser(String schemaName) {
    this._schemaName = schemaName;
  }


  public void addAttributes(HashMap<String, List<String>> attributeValueMap, long conceptID,
      long schemaID) throws NoConnectionException, NoDataException {
    long attrID;
    long attrValueID;

    for (String attr : attributeValueMap.keySet()) {
      attrID = AttributeDriver.getAttributeID(attr, this._schemaName);
      ConceptDriver.addConceptAttribute(attrID, conceptID, this._schemaName);

      for (String value : attributeValueMap.get(attr)) {
        attrValueID = AttrValueDriver.getAttrValueID(value, this._schemaName);
        AttributeDriver.addAttributeValues(attrID, attrValueID, schemaID, this._schemaName);
      }
    }
  }


  public void addLinks(long schemaID, SchemaConcept schemaConcept) {
    long linkTypeID;
    long conceptID;

    try {
      linkTypeID =
          LinkTypeDriver.getLinkTypeID(schemaConcept.getConceptName(), schemaID, this._schemaName);

      for (String left : schemaConcept.getLinkedFromConcepts()) {
        conceptID = ConceptDriver.getConceptID(left, schemaID, this._schemaName);
        LinkTypeDriver.addLinkTypeConcept(linkTypeID, conceptID, 1, this._schemaName);
      }

      for (String right : schemaConcept.getLinkedToConcepts()) {
        conceptID = ConceptDriver.getConceptID(right, schemaID, this._schemaName);
        LinkTypeDriver.addLinkTypeConcept(linkTypeID, conceptID, 2, this._schemaName);
      }

      addAttributes(schemaConcept.getAttributeValueMap(), linkTypeID, schemaID);
    } catch (NoConnectionException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoDataException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  public long addSchema(File file, String source, AtomicBoolean schemaExists) {
    String fileHash;
    String filePath = file.getAbsolutePath();
    FileInputStream fis = null;
    long schemaID = -1;

    try {
      fis = new FileInputStream(file);
      fileHash = getFileHash(file);
      schemaID = SchemasDriver.getSchemaID(fis, source, filePath, fileHash, schemaExists,
          this._schemaName);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoConnectionException ex) {
      ex.printStackTrace();
    } finally {
      try {
        fis.close();
      } catch (IOException ex) {
        Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    return schemaID;
  }


  public void addSchemaConcepts(long schemaID, SchemaConcept schemaConcept) {
    long conceptID;
    try {
      conceptID =
          ConceptDriver.getConceptID(schemaConcept.getConceptName(), schemaID, this._schemaName);
      addAttributes(schemaConcept.getAttributeValueMap(), conceptID, schemaID);
    } catch (NoConnectionException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoDataException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  public String getFileHash(File file) {
    try {
      FileInputStream fis = new FileInputStream(file);
      MessageDigest digest = MessageDigest.getInstance("SHA-512");

      byte[] byteBuffer = new byte[1024];
      int bytesRead;

      while ((bytesRead = fis.read(byteBuffer)) != -1) {
        digest.update(byteBuffer, 0, bytesRead);
      }

      byte[] hashedBytes = digest.digest();
      fis.close();
      return new String(hashedBytes, StandardCharsets.UTF_8);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    }

    return "";
  }


  public List<SchemaConcept> getSchemaConcepts() {
    return this._schemaConcepts;
  }


  public long parseSchema(File schemaFile, String source) {
    AtomicBoolean schemaExists = new AtomicBoolean(false);

    long schemaID = addSchema(schemaFile, source, schemaExists);

    if (schemaExists.get()) {
      return schemaID;
    }

    this._schemaConcepts = SchemaImporter.importSchemaConcepts(schemaFile);

    for (SchemaConcept schemaConcept : this._schemaConcepts) {
      if (schemaConcept.isRelationship()) {
        addLinks(schemaID, schemaConcept);
      } else {
        addSchemaConcepts(schemaID, schemaConcept);
      }
    }

    return schemaID;
  }


  public static void main(String[] args) {
    SQLDriver driver = new SQLDriver("test", "test1234");
    driver.setServerAndDB("localhost", "akinlp");

    SchemaParser parser = new SchemaParser("test.");
    File schemaFile = new File("P:\\workspace\\LEO_Raptat\\data\\New folder\\projectschema.xml");
    long id = parser.parseSchema(schemaFile, "eHost");
    System.out.println(id);
  }
}
