/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.ss.sql.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.AnnotationImporter;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.AnnotationDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.AnnotatorDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.AttrValueDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.AttributeDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.ConceptDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.DocumentDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.LinkDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.LinkTypeDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SentenceDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SpanDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/**
 * Parse the Annotation XML files and upload the annotation information to DB. Date May 27, 2015
 *
 * @author VHATVHSAHAS1
 */
public class AnnotationXMLParser {
  public void addAttributeToAnnotation(AnnotatedPhrase annotation, long annID, long schemaID,
      String schemaName) throws NoConnectionException, NoDataException {
    long attrID;
    long valueID;
    long attrAttrValID;

    for (RaptatAttribute attr : annotation.getPhraseAttributes()) {
      attrID = AttributeDriver.attributeExists(attr.getName(), schemaName);

      for (String value : attr.getValues()) {
        valueID = AttrValueDriver.valueExists(value, schemaName);
        attrAttrValID = AttributeDriver.attrAttrValueExists(attrID, valueID, schemaName);

        AnnotationDriver.addAnnotationAttribute(annID, attrAttrValID, attr.getID(), schemaName);
      }
    }
  }


  public long addDocument(File file, Long sourceID, String schemaName)
      throws NoConnectionException {
    String filePath = file.getAbsolutePath();
    FileInputStream fis = null;
    long docID = -1;

    try {
      fis = new FileInputStream(file);
      docID = DocumentDriver.getDocumentID(fis, filePath, sourceID, schemaName);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        fis.close();
      } catch (IOException ex) {
        Logger.getLogger(SchemaParser.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    return docID;
  }


  public void addRelations(HashMap<String, Long> annotationMap,
      HashMap<Long, ConceptRelation> relationMap, long schemaID, String schemaName) {
    ConceptRelation relation;
    long targetID;
    long linkTypeID;

    for (Long sourceID : relationMap.keySet()) {
      try {
        relation = relationMap.get(sourceID);
        targetID = annotationMap.get(relation.getRelatedAnnotationID());
        linkTypeID =
            LinkTypeDriver.getLinkTypeID(relation.getConceptRelationName(), schemaID, schemaName);

        LinkDriver.addLink(sourceID, targetID, linkTypeID, relation.getID(), schemaName);
      } catch (NoConnectionException ex) {
        Logger.getLogger(AnnotationXMLParser.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }


  public void addSpans(AnnotatedPhrase annotation, long annotationID, long docID, String schemaName)
      throws NoConnectionException {
    int sequence = 1;
    int start;
    int end;
    long sentenceID;

    while (annotation != null) {
      String annotationText = "";
      List<RaptatToken> tokens = annotation.getProcessedTokens();
      if (tokens.size() > 0) {
        AnnotatedPhrase tokenSentence = tokens.get(0).getSentenceOfOrigin();
        if (tokenSentence != null) {
          annotationText = tokenSentence.getPhraseStringUnprocessed();
        }
      }
      sentenceID = SentenceDriver.getsentenceID(annotationText, docID, schemaName);

      /*
       * Modified code (Glenn 06/12/18) so that we use unprocessed rather than processed offsets
       */
      start = Integer.parseInt(annotation.getProcessedTokensStartOffset());
      end = Integer.parseInt(annotation.getProcessedTokensEndOffset());

      start = Integer.parseInt(annotation.getRawTokensStartOff());
      end = Integer.parseInt(annotation.getRawTokensEndOff());

      SpanDriver.addSpan(annotationID, start, end, annotation.getPhraseStringUnprocessed(),
          sequence, sentenceID, schemaName);
      sequence++;
      annotation = annotation.getNextPhrase();
    }
  }


  public void parseAnnotationXML(String xmlFile, String textFile, String schemaFile, long schemaID,
      long sourceID, String schemaName) {
    AnnotationApp schemaApp =
        schemaName.endsWith(".pont") ? AnnotationApp.KNOWTATOR : AnnotationApp.EHOST;
    AnnotationImporter importer = new AnnotationImporter(schemaApp);
    List<AnnotatedPhrase> annotations =
        importer.importAnnotations(xmlFile, textFile, null, schemaFile);

    /*
     * This code appears to be unnecessary as it only adds processed tokens to annotations, and we
     * are only interested in unprocessed/raw tokens. Modified by Glenn 06/12/18
     */
    // TextAnalyzer ta = new TextAnalyzer();
    // RaptatDocument doc = ta.processDocument( textFile );
    // PhraseTokenIntegrator integrator = new PhraseTokenIntegrator();
    // integrator.addProcessedTokensToAnnotations( annotations, doc, false,
    // 0 );

    uploadAnnotations(schemaID, annotations, xmlFile, sourceID, schemaName);
  }


  public void uploadAnnotations(long schemaID, List<AnnotatedPhrase> annotations,
      String xmlFilePath, long sourceID, String schemaName) {
    Long annotatorID;
    Long conceptID;

    long annotationID;

    HashMap<String, Long> annotationMap = new HashMap<>();
    HashMap<Long, ConceptRelation> relationMap = new HashMap<>();

    HashMap<String, Long> annotatorMap = new HashMap<>();
    HashMap<String, Long> conceptMap = new HashMap<>();

    try {
      long docID = addDocument(new File(xmlFilePath), sourceID, schemaName);

      for (AnnotatedPhrase ann : annotations) {
        if (ann.getPrevPhrase() != null) {
          continue;
        }

        if ((annotatorID = annotatorMap.get(ann.getAnnotatorID())) == null) {
          annotatorID = AnnotatorDriver.getAnnotatorID(ann.getAnnotatorID(), schemaName);
          annotatorMap.put(ann.getAnnotatorID(), annotatorID);
        }

        if ((conceptID = conceptMap.get(ann.getConceptName())) == null) {
          conceptID = ConceptDriver.conceptExists(ann.getConceptName(), schemaID, schemaName);
          conceptMap.put(ann.getConceptName(), conceptID);
        }

        // check if annotation exists
        // if(!AnnotationDriver.annotationExists( annotatorID,
        // conceptID, ann.getMentionId(), schemaName )){
        annotationID =
            AnnotationDriver.addAnnotation(annotatorID, conceptID, ann.getMentionId(), schemaName);
        addSpans(ann, annotationID, docID, schemaName);
        annotationMap.put(ann.getMentionId(), annotationID);
        addAttributeToAnnotation(ann, annotationID, schemaID, schemaName);

        for (ConceptRelation relation : ann.getConceptRelations()) {
          relationMap.put(annotationID, relation);
        }
        // }

      }

      addRelations(annotationMap, relationMap, schemaID, schemaName);
    } catch (NoConnectionException ex) {
      System.err.println("Connection Error while processing " + xmlFilePath);
      Logger.getLogger(AnnotationXMLParser.class.getName()).log(Level.SEVERE,
          "Connection Error while processing " + xmlFilePath, ex);
    } catch (NoDataException ex) {
      System.err.println("Missing Data while processing " + xmlFilePath);
      Logger.getLogger(AnnotationXMLParser.class.getName()).log(Level.SEVERE,
          "Missing Data while processing " + xmlFilePath, ex);
    } catch (Exception ex) {
      System.err.println("Error occured while processing " + xmlFilePath);
      Logger.getLogger(AnnotationXMLParser.class.getName()).log(Level.SEVERE,
          "Error occured while processing " + xmlFilePath, ex);
    }
  }


  public static void main(String[] args) {
    AnnotationXMLParser parser = new AnnotationXMLParser();
    String xmlFile =
        "P:\\workspace\\LEO_Raptat\\data\\New folder\\Block_08_Note_013.txt.knowtator.xml";
    String textFile = "P:\\workspace\\LEO_Raptat\\data\\New folder\\Block_08_Note_013.txt";
    String schemaFile = "P:\\workspace\\LEO_Raptat\\data\\New folder\\projectschema.xml";

    SQLDriver driver = new SQLDriver("test", "test1234");
    driver.setServerAndDB("localhost", "akinlp");

    String schemaName = "test.";
    SchemaParser sParser = new SchemaParser(schemaName);
    long schemaID = sParser.parseSchema(new File(schemaFile), "eHost");
    parser.parseAnnotationXML(xmlFile, textFile, schemaFile, schemaID, 0, schemaName);
  }
}
