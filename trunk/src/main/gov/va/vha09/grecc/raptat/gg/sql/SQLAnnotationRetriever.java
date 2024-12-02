/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.sql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.ISQLDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriver;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.drivers.SQLDriverException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoConnectionException;
import src.main.gov.va.vha09.grecc.raptat.ss.sql.exception.NoDataException;

/**
 * @author Glenn Gobbel
 *
 */
public class SQLAnnotationRetriever {

  String schemaName = "RapTATTest";
  private static TextAnalyzer textAnalyzer = new TextAnalyzer();
  /*
   * General, unspecified object for storing the information used to connect to a database
   */
  private DbConnectionObject databaseConnectionInformation;
  private int groupBatchSize;
  private int currentBatch = 0;
  private ISQLDriver __sql_driver;


  /*
   * The databaseConnectionInformation is a placeholder used to represent all the needed information
   * to connect to the database for retrieving annotations
   */
  public SQLAnnotationRetriever(
      DbConnectionObject databaseConnectionInformation, int groupBatchSize)
      throws SQLDriverException {
    this.databaseConnectionInformation = databaseConnectionInformation;
    this.groupBatchSize = groupBatchSize;
    __sql_driver = new SQLDriver(databaseConnectionInformation);
  }


  /*
   * Implement this method to set up a connection to the database and make sure it is valid. If so,
   * return true. If not, return false;
   */
  public Connection connectToDatabase() {
    String connectionUrl = "jdbc:sqlserver://" + this.databaseConnectionInformation.server + ";";

    if (this.databaseConnectionInformation.database != null) {
      connectionUrl += "databaseName=" + this.databaseConnectionInformation.database + ";";
    }

    Connection connection = null;

    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      connection = DriverManager.getConnection(connectionUrl,
          this.databaseConnectionInformation.username, this.databaseConnectionInformation.password);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return connection;
  }


  /**
   *
   * @param idIndex
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  public Set<AnnotationGroup> getNextAnnotationGroupBatch(int idIndex)
      throws NoConnectionException, NoDataException {
    long maxRows = this.getTotalNumberOfRows();
    Set<AnnotationGroup> groupsToReturn = null;
    while (idIndex <= maxRows) {
      List<Integer> documentIDs = this.getNextDocumentIdBatch(idIndex);
      groupsToReturn = new HashSet<>(this.groupBatchSize);

      for (int documentID : documentIDs) {
        AnnotationGroup annotationGroup = this.getNextAnnotationGroup(documentID);
        groupsToReturn.add(annotationGroup);
      }
      idIndex += this.groupBatchSize;
    }
    return groupsToReturn;
  }


  /**
   * May 29, 2018
   *
   * @param documentID
   *
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  private RaptatDocument getDocumentDetails(int documentID)
      throws NoConnectionException, NoDataException {
    RaptatDocument document = null;
    Connection connection = this.connectToDatabase();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }
    ResultSet rs;
    try {
      String query =
          "select id_document " + ", document_blob " + ", document_source " + ", doc_source_id "
              + " from " + schemaName + ".document " + "where id_document = " + documentID;

      rs = __sql_driver.executeSelect(query);

      while (rs.next()) {
        /*
         * Glenn Note - 9/17/18 - Need to convert the document blob into a string so that correct
         * offsets can be calculated. The code block below should do that. Note that a static field,
         * a TextAnalyzer instance is used to convert the string from a SQL blob to a RaptatDocument
         * instance.
         */

        InputStream inputStream = rs.getBinaryStream("document_blob");
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
          result.write(buffer, 0, length);
        }

        // --Check that this is the correct way to encode string
        // using utf-8
        String docString = result.toString(StandardCharsets.UTF_8.toString());
        String fileName = new File(rs.getString("document_source")).getName();
        document = SQLAnnotationRetriever.textAnalyzer.processText(docString, fileName);

        // document = new RaptatDocument();
        // document.setTextSourcePath( new File( rs.getString(
        // "document_source" ) ).getName() );

        if (document == null) {
          throw new NoDataException("No document found for document_id: " + documentID);
        }
      }
    } catch (SQLException | IOException ex) {
      ex.printStackTrace();
    }
    return document;
  }


  /**
   * May 29, 2018
   *
   * @param documentID
   *
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  private AnnotationGroup getNextAnnotationGroup(int documentID)
      throws NoConnectionException, NoDataException {
    RaptatDocument document = this.getDocumentDetails(documentID);

    List<AnnotatedPhrase> annotatedPhrases = this.getNextAnnotationGroupPhrases(documentID);
    AnnotationGroup annotationGroup = new AnnotationGroup(document, annotatedPhrases);
    return annotationGroup;
  }


  private List<AnnotatedPhrase> getNextAnnotationGroupPhrases(int documentID)
      throws NoConnectionException, NoDataException {
    List<AnnotatedPhrase> annotatedPhrases = new ArrayList<AnnotatedPhrase>();

    String annotatorId = "";
    String annotatorName = "";
    String retrievedMentionID = "";
    String retrievedStartOffset = "";
    String retrievedEndOffset = "";
    String retrievedPhraseText = "";
    String annotationId = "";
    String conceptName = "";
    String annotationCreationDate = "";

    List<ConceptRelation> relations;
    List<RaptatAttribute> attributes;

    HashMap<String, List<ConceptRelation>> mapAnnotationsConceptRelations =
        this.retrieveConceptRelations(documentID);
    HashMap<String, List<RaptatAttribute>> mapAnnotationsAttributes =
        this.retrieveAttributes(documentID);

    Connection connection = this.connectToDatabase();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }
    ResultSet rs;
    try {
      String query = "select AV.[id_document]" + " ,AV.[id_sentence]" + " ,AV.[id_span]"
          + " ,AV.[id_annotation]" + " ,AV.[annotator_name]" + " ,AV.[start_offset]"
          + " ,AV.[end_offset]" + " ,AV.[span_text]" + " ,AV.[xml_annotation_id]"
          + " ,C.concept_name, AV.annotation_creation_date" + " FROM " + schemaName
          + ".[Annotation_View] as AV" + " join " + schemaName + ".[Concept] as C"
          + " on AV.id_concept = C.id_concept" + " where id_document = " + documentID;

      rs = __sql_driver.executeSelect(query);

      while (rs.next()) {
        annotationId = rs.getString("id_annotation");
        annotatorId = rs.getString("id_annotation");
        annotatorName = rs.getString("annotator_name");
        retrievedMentionID = rs.getString("xml_annotation_id");
        retrievedStartOffset = rs.getString("start_offset");
        retrievedEndOffset = rs.getString("end_offset");
        retrievedPhraseText = rs.getString("span_text");
        conceptName = rs.getString("concept_name");
        annotationCreationDate = rs.getString("annotation_creation_date");
        relations = mapAnnotationsConceptRelations.get(annotationId);
        attributes = mapAnnotationsAttributes.get(annotationId);

        AnnotatedPhraseFields phraseFields = new AnnotatedPhraseFields(annotatorName, annotatorId,
            conceptName, retrievedMentionID, retrievedStartOffset, retrievedEndOffset,
            retrievedPhraseText, relations, attributes, annotationCreationDate);

        AnnotatedPhrase annotatedPhrase = new AnnotatedPhrase(phraseFields);
        annotatedPhrases.add(annotatedPhrase);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return annotatedPhrases;
  }


  /**
   * Jun 12, 2018
   *
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  private List<Integer> getNextDocumentIdBatch(long idIndex)
      throws NoConnectionException, NoDataException {
    List<Integer> documentIDs = new ArrayList<>(this.groupBatchSize);
    documentIDs = this.retrieveBatch(this.currentBatch++, this.groupBatchSize, idIndex);

    return documentIDs;
  }


  /**
   *
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  private long getTotalNumberOfRows() throws NoDataException, NoConnectionException {
    long totalRows = 0;
    Connection connection = this.connectToDatabase();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }
    ResultSet rs;
    try {
      String query = "select count(*) as cnt from " + schemaName + ".document";

      rs = __sql_driver.executeSelect(query);

      while (rs.next()) {
        totalRows = rs.getLong("cnt");
      }

      if (totalRows == 0) {
        throw new NoDataException("No documents: ");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return totalRows;
  }


  /**
   *
   * @param documentId
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  private HashMap<String, List<RaptatAttribute>> retrieveAttributes(int documentId)
      throws NoDataException, NoConnectionException {
    RaptatAttributeFields raptatAttributeFields = null;
    HashMap<String, List<RaptatAttribute>> mapAnnotationAttributes =
        new HashMap<String, List<RaptatAttribute>>();
    List<RaptatAttribute> attributes;

    Connection connection = this.connectToDatabase();
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }
    ResultSet rs;
    try {
      String query = "select [id_document]" + " ,[id_annotation]" + " ,[id_attribute]"
          + " ,[attribute_name]" + " ,[attribute_value]" + " ,[xml_attribute_id]" + " FROM "
          + schemaName + ".[Attribute_View]" + " where id_document = " + documentId;

      rs = __sql_driver.executeSelect(query);
      String xmlAttributeId = "";
      String attributeName = "";
      String attributeValue = "";
      String annotationId = "";
      while (rs.next()) {
        List<String> attributeValues = new ArrayList<String>();
        xmlAttributeId = rs.getString("xml_attribute_id");
        attributeName = rs.getString("attribute_name");
        attributeValue = rs.getString("attribute_value");
        annotationId = rs.getString("id_annotation");
        attributeValues.add(attributeValue);
        if (mapAnnotationAttributes.containsKey(annotationId)) {
          attributes = mapAnnotationAttributes.get(annotationId);
        } else {
          attributes = new ArrayList<RaptatAttribute>();
        }
        raptatAttributeFields =
            new RaptatAttributeFields(xmlAttributeId, attributeName, attributeValues);
        RaptatAttribute attribute = new RaptatAttribute(raptatAttributeFields);
        attributes.add(attribute);
        mapAnnotationAttributes.put(annotationId, attributes);
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return mapAnnotationAttributes;
  }


  /**
   * Sends SQL query to retrieve a particular batch of documentIDs and returns them in a list.
   *
   * Jun 12, 2018
   *
   * @param batchNumber
   * @param groupBatchSize2
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  private List<Integer> retrieveBatch(int batchNumber, int groupBatchSize2, long idIndex)
      throws NoConnectionException, NoDataException {
    /*
     * This is where to include or call a query that will get batch 'batchNumber' of documentIDs
     * from the DocumentID table
     */
    List<Integer> documentIds = null;
    Connection connection = this.connectToDatabase();
    long upperlimit = idIndex + groupBatchSize2;
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }
    ResultSet rs;
    long id = 0;
    try {
      String query = "select distinct top " + groupBatchSize2 + " id_document as id \r\n" + "from "
          + schemaName + ".sentence" + " where id_document between " + idIndex + " and "
          + upperlimit + " order by id_document";

      rs = __sql_driver.executeSelect(query);

      documentIds = new ArrayList<Integer>(rs.getFetchSize());
      while (rs.next()) {
        id = rs.getLong("id");
        documentIds.add((int) id);
      }

      if (documentIds.size() == 0) {
        throw new NoDataException("No sentences: ");
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return documentIds;
  }


  /**
   *
   * @param documentId
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  private HashMap<String, List<ConceptRelation>> retrieveConceptRelations(int documentId)
      throws NoDataException, NoConnectionException {
    Connection connection = this.connectToDatabase();
    String targetAnnotation = null;
    HashMap<String, List<ConceptRelation>> mapAnnotationConceptRelations =
        new HashMap<String, List<ConceptRelation>>();
    List<ConceptRelation> conceptRelations;
    if (connection == null) {
      throw new NoConnectionException("connection problem");
    }
    ResultSet rs;
    try {
      String query = "select [id_document]" + " ,[id_annotation]" + " ,[id_targer_annotation]"
          + " ,[link_type]" + " ,[xml_relation_id]" + " FROM [ORD_Matheny_201312053D_RES1]."
          + schemaName + ".[Relation_View] " + " where id_document = " + documentId;

      rs = __sql_driver.executeSelect(query);

      while (rs.next()) {
        String annotationId = rs.getString("id_annotation");
        String linkType = rs.getString("link_type");
        String xmlRelationID = rs.getString("xml_relation_id");
        targetAnnotation = rs.getString("id_targer_annotation");
        ConceptRelationFields relationFields =
            new ConceptRelationFields(xmlRelationID, linkType, targetAnnotation);
        if (mapAnnotationConceptRelations.containsKey(annotationId)) {
          conceptRelations = mapAnnotationConceptRelations.get(annotationId);
        } else {
          conceptRelations = new ArrayList<>();
        }
        conceptRelations.add(new ConceptRelation(relationFields));
        mapAnnotationConceptRelations.put(annotationId, conceptRelations);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return mapAnnotationConceptRelations;

  }

  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  /*
   * private List<ConceptRelation> retrieveConceptRelations(int annotationID) throws
   * NoDataException, NoConnectionException { List<ConceptRelation> relations = retrieveRelations(
   * annotationID );
   *
   * return relations; }
   */

  /**
   * May 29, 2018
   *
   * @param documentID
   *
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  /*
   * private List<AnnotatedPhrase> getNextAnnotationGroupPhrases(int documentID) throws
   * NoConnectionException, NoDataException { List<AnnotatedPhrase> annotatedPhrases = new
   * ArrayList<AnnotatedPhrase>(); List<Integer> annotationIDs = retrieveAnnotationIDs( documentID
   * );
   *
   * for (int annotationID : annotationIDs) { AnnotatedPhrase annotatedPhrase =
   * retrieveAnnotatedPhrase( annotationID ); annotatedPhrases.add( annotatedPhrase ); }
   *
   * return annotatedPhrases; }
   */

  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  /*
   * private List<String> queryToGetLinkIDs(int annotationID) throws NoDataException,
   * NoConnectionException {
   *
   * Include code here that queries or returns from a previous query all the id_link instances
   * associated with the annotationID as the id_souce_annotation in the 'link' SQL table
   *
   * List<String> linkIds = new ArrayList<String>(); Connection connection = connectToDatabase(); if
   * ( connection == null ) { throw new NoConnectionException( "connection problem" ); } ResultSet
   * rs; try { String query = "select id_link" + " from RapTAT.link"
   * +" where id_source_annotation = "+annotationID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { linkIds.add( rs.getString( "id_link" ) ); } } catch (SQLException ex) {
   * ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); } return linkIds;
   * }
   */

  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  /*
   * private AnnotatedPhrase retrieveAnnotatedPhrase(int annotationID) throws NoConnectionException,
   * NoDataException { AnnotatedPhraseFields phraseFields = retrieveAnnotatedPhraseFields(
   * annotationID ); return new AnnotatedPhrase( phraseFields ); }
   */

  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  /*
   * private AnnotatedPhraseFields retrieveAnnotatedPhraseFields(int annotationID) throws
   * NoConnectionException, NoDataException {
   *
   * These will have to be retrieve through a query. You can use an AnnotatedPhraseFields object if
   * needed to store and return multiple string values. Modify this as needed.
   *
   * String annotatorId = ""; String annotatorName = ""; String retrievedMentionID = ""; String
   * retrievedStartOffset = ""; String retrievedEndOffset = ""; String retrievedPhraseText = "";
   * Connection connection = connectToDatabase(); if ( connection == null ) { throw new
   * NoConnectionException( "connection problem" ); } ResultSet rs; try { String query =
   * "select a.id_annotation " + " , an.id_annotator" + " , an.annotator_name" + " , s.start_offset"
   * + " , s.end_offset" + " , s.span_text" + " , a.xml_annotation_id" +
   * " from RapTAT.annotation  as a" + " inner join  RapTAT.span as s" +
   * " on S.id_annotation = a.id_annotation" + " inner join RapTAT.annotator as an" +
   * " on a.id_annotator = an.id_annotator" + " where a.id_annotation = "+annotationID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { annotatorId = rs.getString( "id_annotation" ); annotatorName =
   * rs.getString( "annotator_name" ); retrievedMentionID = rs.getString( "xml_annotation_id" );
   * retrievedStartOffset = rs.getString( "start_offset" ); retrievedEndOffset = rs.getString(
   * "end_offset" ); retrievedPhraseText = rs.getString( "span_text" ); } } catch (SQLException ex)
   * { ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); }
   *
   * String conceptname = retrieveConceptName( annotationID ); List<ConceptRelation> relations =
   * retrieveConceptRelations( annotationID ); List<RaptatAttribute> attributes =
   * retrieveAttributes( annotationID );
   *
   * return new AnnotatedPhraseFields( annotatorId, annotatorName, conceptname, retrievedMentionID,
   * retrievedStartOffset, retrievedEndOffset, retrievedPhraseText, relations, attributes ); }
   */

  /*
   * private String retrieveConceptName(int annotationID) throws NoConnectionException { String
   * conceptName = ""; Connection connection = connectToDatabase(); if ( connection == null ) {
   * throw new NoConnectionException( "connection problem" ); } ResultSet rs; try { String query =
   * "select C.concept_name" + " from RapTAT.concept as C" + " join RapTAT.annotation as A" +
   * " on C.id_concept = A.id_concept" + " where A.id_annotation = "+annotationID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { conceptName = rs.getString( "concept_name" ); } } catch (SQLException ex)
   * { ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); } return
   * conceptName; }
   */

  /**
   * Jun 12, 2018
   *
   * @param documentID
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  /*
   * private List<Integer> retrieveAnnotationIDs(int documentID) throws NoConnectionException,
   * NoDataException {
   *
   * This is where to include or call a query or method that will get batch 'batchNumber' of
   * documentIDs from the DocumentID table
   *
   *
   * List<Integer> annotationIds = new ArrayList<Integer>(); Connection connection =
   * connectToDatabase(); if ( connection == null ) { throw new NoConnectionException(
   * "connection problem" ); } ResultSet rs; try { String query = "select id_annotation  " +
   * "from RapTAT.sentence  as SN " + "inner join  RapTAT.span as SP " +
   * "on SN.id_sentence = sp.id_sentence " + "where id_document = "+documentID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { annotationIds.add( rs.getInt( "id_annotation" ) ); }
   *
   * if(annotationIds.isEmpty()) { throw new NoDataException(
   * "No annotations found for document_id: "+documentID); } } catch (SQLException ex) {
   * ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); } return
   * annotationIds;
   *
   * }
   */

  /**
   * Jun 12, 2018
   *
   * @param attributeID
   * @return
   * @throws NoConnectionException
   */
  /*
   * private RaptatAttributeFields retrieveAttributeFields(String attrAttrValId) throws
   * NoConnectionException {
   *
   * You will need to insert code or SQL queries to retrieve the attribute_name, the
   * xml_attribute_id, and the attribute_values (there may be 1 or more) associated with
   * attributeID.
   *
   * RaptatAttributeFields raptatAttributeFields = null; Connection connection =
   * connectToDatabase(); if ( connection == null ) { throw new NoConnectionException(
   * "connection problem" ); } ResultSet rs; try { String query = "select R.id_attr_attrval"
   * +" , A.id_attribute" +" , A.attribute_name" +" , Rel.xml_attribute_id" +" , R.id_value"
   * +" , AV.attribute_value" +" from RapTAT.rel_attr_attrval as R" +" join RapTAT.attribute as A"
   * +" on R.id_attribute = A.id_attribute" +" join RapTAT.rel_ann_attr as Rel"
   * +" on Rel.id_attr_attrval = R.id_attr_attrval" +" join RapTAT.attr_value as AV"
   * +" on R.id_value = AV.id_value" +" where A.id_attribute = "+attrAttrValId;
   *
   * rs = __sql_driver.executeSelect( query ); List<String> attributeValues = new
   * ArrayList<String>(); String xmlAttributeId =""; String attributeName =""; String attributeValue
   * = ""; while ( rs.next()) { xmlAttributeId = rs.getString( "xml_attribute_id" ); attributeName =
   * rs.getString( "attribute_name" ); attributeValue = rs.getString( "attribute_value" );
   * attributeValues.add( attributeValue ); } raptatAttributeFields = new RaptatAttributeFields(
   * xmlAttributeId, attributeName, attributeValues );
   *
   * } catch (SQLException ex) { ex.printStackTrace(); } finally { __sql_driver.closeConnection(
   * connection ); } return raptatAttributeFields; }
   */
  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  /*
   * private List<RaptatAttribute> retrieveAttributes(int annotationID) throws NoDataException,
   * NoConnectionException { List<RaptatAttribute> attributes = new ArrayList<RaptatAttribute>();
   * List<String> attributeIds = retrieveIDAnnAttrIDs( annotationID );
   *
   * for (String attributeId : attributeIds) { RaptatAttributeFields attributeFields =
   * retrieveAttributeFields( attributeId ); RaptatAttribute attribute = new RaptatAttribute(
   * attributeFields ); attributes.add( attribute ); } return attributes; }
   */

  /*
   * private List<RaptatAttribute> retrieveAttributes(int annotationID) throws NoDataException,
   * NoConnectionException { RaptatAttributeFields raptatAttributeFields = null;
   * List<RaptatAttribute> attributes = new ArrayList<RaptatAttribute>();
   *
   * Connection connection = connectToDatabase(); if ( connection == null ) { throw new
   * NoConnectionException( "connection problem" ); } ResultSet rs; try { String query =
   * "select RAAV.id_attribute" + " , A.attribute_name" + " , AV.attribute_value" +
   * " , RAA.xml_attribute_id" + " from RapTAT.rel_ann_attr as RAA" +
   * " join  RapTAT.rel_attr_attrval as RAAV" + " on RAA.id_attr_attrval = RAAV.id_attr_attrval" +
   * " join RapTAT.attribute as A" + " on  RAAV.id_attribute = A.id_attribute" +
   * " join RapTAT.attr_value as AV" + " on RAAV.id_value = AV.id_value" +
   * " where RAA.id_annotation = "+annotationID;
   *
   * rs = __sql_driver.executeSelect( query ); String xmlAttributeId =""; String attributeName ="";
   * String attributeValue = ""; while ( rs.next()) { List<String> attributeValues = new
   * ArrayList<String>(); xmlAttributeId = rs.getString( "xml_attribute_id" ); attributeName =
   * rs.getString( "attribute_name" ); attributeValue = rs.getString( "attribute_value" );
   * attributeValues.add( attributeValue ); raptatAttributeFields = new RaptatAttributeFields(
   * xmlAttributeId, attributeName, attributeValues ); RaptatAttribute attribute = new
   * RaptatAttribute( raptatAttributeFields ); attributes.add( attribute ); }
   *
   * } catch (SQLException ex) { ex.printStackTrace(); } finally { __sql_driver.closeConnection(
   * connection ); } return attributes; }
   */

  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  /*
   * private List<String> retrieveIDAnnAttrIDs(int annotationID) throws NoDataException,
   * NoConnectionException {
   *
   * This is where to include or call a query or method that retrieve all the id_attr_attrval values
   * in the rel_attr_attrval SQL table associated with a given annotation
   *
   *
   * List<String> attributeIds = null; Connection connection = connectToDatabase(); if ( connection
   * == null ) { throw new NoConnectionException( "connection problem" ); } ResultSet rs; try {
   * String query = "select R.id_ann_attrid" +" , R.id_attr_attrval" +" , R.id_annotation"
   * +" , R.xml_attribute_id" +" , A.id_attribute" +" , A.id_value"
   * +" from RapTAT.rel_ann_attr as R" +" inner join RapTAT.rel_attr_attrval as A"
   * +" on R.id_attr_attrval = A.id_attr_attrval" +" where R.id_annotation = "+annotationID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * attributeIds = new ArrayList<String>(rs.getFetchSize()); while ( rs.next()) { attributeIds.add(
   * rs.getString( "id_attribute" ) ); }
   *
   * if(attributeIds.size()==0) { throw new NoDataException(
   * "No IDAnnAttrIDs for annotation : "+annotationID); } } catch (SQLException ex) {
   * ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); } return
   * attributeIds; }
   */

  /**
   * Jun 12, 2018
   *
   * @param annotationID
   * @return
   * @throws NoConnectionException
   * @throws NoDataException
   */
  /*
   * private List<ConceptRelation> retrieveRelations(int annotationID) throws NoDataException,
   * NoConnectionException {
   *
   * Here you will need to retrieve all the id_links from the link table associated with
   * annotationID as the id_source_annotation. For each id_link, you will need to get the
   * id_link_type, and xml_relation_id, and you will need to get the link_type based on the
   * id_link_type. You will also need to retrieve the xml_annotation_id for all the
   * id_targetAnnotations.
   *
   * Note that there may be more than one id_link per id_annotation, and there may be more than one
   * id_target_annotation per id_link).
   *
   * List<ConceptRelation> relationList = new ArrayList<ConceptRelation>();
   *
   * List<String> linkIDs = queryToGetLinkIDs( annotationID );
   *
   * for (String linkID : linkIDs) { String linkType = retrieveTypeOfLink( linkID ); String
   * xmlRelationID = retrieveXmlRelationID( linkID ); List<String> targetAnnotations =
   * retrieveTargetAnnotations( linkID ); ConceptRelationFields relationFields = new
   * ConceptRelationFields( xmlRelationID, linkType, targetAnnotations ); relationList.add( new
   * ConceptRelation( relationFields ) ); }
   *
   * return relationList;
   *
   * return getRelationsFromLinkIds (relationList, annotationID);
   *
   *
   * }
   */

  /*
   * private List<ConceptRelation> getRelationsFromLinkIds(List<ConceptRelation> relationList, int
   * annotationID) throws NoDataException, NoConnectionException { Connection connection =
   * connectToDatabase(); String targetAnnotation = null;
   *
   * if ( connection == null ) { throw new NoConnectionException( "connection problem" ); }
   * ResultSet rs; try { String query = "select L.id_link" + " , LT.link_type" +
   * " , L.xml_relation_id" + " , L.id_source_annotation" + " , L.id_targer_annotation" +
   * " from  RapTAT.link as L" + " join RapTAT.link_type as LT" +
   * " on L.id_link_type = LT.id_link_type" + " where id_source_annotation =  "+annotationID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { String linkType = rs.getString( "link_type" ); String xmlRelationID =
   * rs.getString( "xml_relation_id" ); targetAnnotation = rs.getString( "id_targer_annotation" );
   * ConceptRelationFields relationFields = new ConceptRelationFields( xmlRelationID, linkType,
   * targetAnnotation ); relationList.add( new ConceptRelation( relationFields ) ); } } catch
   * (SQLException ex) { ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection
   * ); } return relationList; }
   */

  /**
   * Jun 12, 2018
   *
   * @param linkID
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  /*
   * private List<String> retrieveTargetAnnotations(String linkID) throws NoDataException,
   * NoConnectionException {
   *
   * This is where to include or call a query or method that will get all 'id_target_annotations'
   * from the link table (or from a previous query)
   *
   * List<String> targetAnnotations = null; Connection connection = connectToDatabase(); if (
   * connection == null ) { throw new NoConnectionException( "connection problem" ); } ResultSet rs;
   * try { String query = "select id_targer_annotation" + " from RapTAT.link" +
   * " where id_link =  "+linkID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * targetAnnotations = new ArrayList<String>(rs.getFetchSize()); while ( rs.next()) {
   * targetAnnotations.add( rs.getString( "id_targer_annotation" ) ); }
   *
   * if(targetAnnotations.size()==0) { throw new NoDataException(
   * "No target annotations for id_link : "+linkID); } } catch (SQLException ex) {
   * ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); } return
   * targetAnnotations; }
   */

  /**
   * Jun 12, 2018
   *
   * @param linkID
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  /*
   * private String retrieveTypeOfLink(String linkID) throws NoDataException, NoConnectionException
   * {
   *
   * This is where to include a method or call to determine the name of a link (i.e., "link_type")
   * based on the id_link
   *
   * String linkType = ""; Connection connection = connectToDatabase(); if ( connection == null ) {
   * throw new NoConnectionException( "connection problem" ); } ResultSet rs; try { String query =
   * "select LT.link_type" + " from RapTAT.link_type as LT" + " join RapTAT.link as L" +
   * " on LT.id_link_type = L.id_link_type" +" where id_link = "+linkID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { linkType = rs.getString( "link_type" ); }
   *
   * if(linkType == "") { throw new NoDataException( "No link_type found for link_id: "+linkID); } }
   * catch (SQLException ex) { ex.printStackTrace(); } finally { __sql_driver.closeConnection(
   * connection ); } return linkType; }
   */

  /**
   * Jun 12, 2018
   *
   * @param linkID
   * @return
   * @throws NoDataException
   * @throws NoConnectionException
   */
  /*
   * private String retrieveXmlRelationID(String linkID) throws NoDataException,
   * NoConnectionException {
   *
   * This is where to include or call a query or method that will get batch 'xml_relation_id' from
   * the link table (or from a previous query)
   *
   * String xmlRelationId = ""; Connection connection = connectToDatabase(); if ( connection == null
   * ) { throw new NoConnectionException( "connection problem" ); } ResultSet rs; try { String query
   * = "select xml_relation_id" + " from RapTAT.link" +" where id_link = "+linkID;
   *
   * rs = __sql_driver.executeSelect( query );
   *
   * while ( rs.next()) { xmlRelationId = rs.getString( "id_link" ); }
   *
   * if(xmlRelationId == "") { throw new NoDataException(
   * "No xmlRelationId found for id_link: "+linkID); } } catch (SQLException ex) {
   * ex.printStackTrace(); } finally { __sql_driver.closeConnection( connection ); } return
   * xmlRelationId; }
   */

}
