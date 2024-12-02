/* First created by JCasGen Thu Aug 22 14:27:13 CDT 2013 */
package src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Stores detailed information about the original source document from which the current CAS was
 * initialized. All information (like size) refers to the source document and not to the document in
 * the CAS which may be converted and filtered by a CAS Initializer. For example this information
 * will be written to the Semantic Search index so that the original document contents can be
 * retrieved by queries. Updated by JCasGen Mon Sep 09 11:17:14 CDT 2013 XML source:
 * /Users/glenn/Documents/workspace/ADAHF2/resources/desc/ TextCollectionReaderDescriptor.xml
 *
 * @generated
 */
public class SourceDocumentInformation extends Annotation {
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(SourceDocumentInformation.class);
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int type = typeIndexID;


  /**
   * Internal - constructor used by generator
   *
   * @generated
   */
  public SourceDocumentInformation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }


  /** @generated */
  public SourceDocumentInformation(JCas jcas) {
    super(jcas);
    readObject();
  }


  /** @generated */
  public SourceDocumentInformation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }


  /**
   * Never called. Disable default constructor
   *
   * @generated
   */
  protected SourceDocumentInformation() {
    /* intentionally empty block */
  }


  /**
   * getter for documentSize - gets Size of original document in bytes before processing by CAS
   * Initializer. Either absolute file size of size within file or other source.
   *
   * @generated
   */
  public int getDocumentSize() {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_documentSize == null) {
      this.jcasType.jcas.throwFeatMissing("documentSize",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.jcasType.ll_cas.ll_getIntValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_documentSize);
  }


  /**
   * getter for lastSegment - gets For a CAS that represents a segment of a larger source document,
   * this flag indicates whether this CAS is the final segment of the source document. This is
   * useful for downstream components that want to take some action after having seen all of the
   * segments of a particular source document.
   *
   * @generated
   */
  public boolean getLastSegment() {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_lastSegment == null) {
      this.jcasType.jcas.throwFeatMissing("lastSegment",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.jcasType.ll_cas.ll_getBooleanValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_lastSegment);
  }


  // *--------------*
  // * Feature: uri

  /**
   * getter for offsetInSource - gets Byte offset of the start of document content within original
   * source file or other input source. Only used if the CAS document was retrieved from an source
   * where one physical source file contained several conceptual documents. Zero otherwise.
   *
   * @generated
   */
  public int getOffsetInSource() {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_offsetInSource == null) {
      this.jcasType.jcas.throwFeatMissing("offsetInSource",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.jcasType.ll_cas.ll_getIntValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_offsetInSource);
  }


  /**
   * getter for SourceName - gets
   *
   * @generated
   */
  public String getSourceName() {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_SourceName == null) {
      this.jcasType.jcas.throwFeatMissing("SourceName",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_SourceName);
  }


  // *--------------*
  // * Feature: offsetInSource

  /**
   * getter for SourcePath - gets
   *
   * @generated
   */
  public String getSourcePath() {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_SourcePath == null) {
      this.jcasType.jcas.throwFeatMissing("SourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_SourcePath);
  }


  /** @generated */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }


  // *--------------*
  // * Feature: documentSize

  /**
   * getter for uri - gets URI of document. (For example, file:///MyDirectory/myFile.txt for a
   * simple file or http://incubator.apache.org/uima/index.html for content from a web source.)
   *
   * @generated
   */
  public String getUri() {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_uri == null) {
      this.jcasType.jcas.throwFeatMissing("uri",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_uri);
  }


  /**
   * setter for documentSize - sets Size of original document in bytes before processing by CAS
   * Initializer. Either absolute file size of size within file or other source.
   *
   * @generated
   */
  public void setDocumentSize(int v) {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_documentSize == null) {
      this.jcasType.jcas.throwFeatMissing("documentSize",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.jcasType.ll_cas.ll_setIntValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_documentSize, v);
  }


  // *--------------*
  // * Feature: lastSegment

  /**
   * setter for lastSegment - sets For a CAS that represents a segment of a larger source document,
   * this flag indicates whether this CAS is the final segment of the source document. This is
   * useful for downstream components that want to take some action after having seen all of the
   * segments of a particular source document.
   *
   * @generated
   */
  public void setLastSegment(boolean v) {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_lastSegment == null) {
      this.jcasType.jcas.throwFeatMissing("lastSegment",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.jcasType.ll_cas.ll_setBooleanValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_lastSegment, v);
  }


  /**
   * setter for offsetInSource - sets Byte offset of the start of document content within original
   * source file or other input source. Only used if the CAS document was retrieved from an source
   * where one physical source file contained several conceptual documents. Zero otherwise.
   *
   * @generated
   */
  public void setOffsetInSource(int v) {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_offsetInSource == null) {
      this.jcasType.jcas.throwFeatMissing("offsetInSource",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.jcasType.ll_cas.ll_setIntValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_offsetInSource, v);
  }


  // *--------------*
  // * Feature: SourcePath

  /**
   * setter for SourceName - sets
   *
   * @generated
   */
  public void setSourceName(String v) {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_SourceName == null) {
      this.jcasType.jcas.throwFeatMissing("SourceName",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_SourceName, v);
  }


  /**
   * setter for SourcePath - sets
   *
   * @generated
   */
  public void setSourcePath(String v) {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_SourcePath == null) {
      this.jcasType.jcas.throwFeatMissing("SourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_SourcePath, v);
  }


  // *--------------*
  // * Feature: SourceName

  /**
   * setter for uri - sets URI of document. (For example, file:///MyDirectory/myFile.txt for a
   * simple file or http://incubator.apache.org/uima/index.html for content from a web source.)
   *
   * @generated
   */
  public void setUri(String v) {
    if (SourceDocumentInformation_Type.featOkTst
        && ((SourceDocumentInformation_Type) this.jcasType).casFeat_uri == null) {
      this.jcasType.jcas.throwFeatMissing("uri",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((SourceDocumentInformation_Type) this.jcasType).casFeatCode_uri, v);
  }


  /**
   *
   * <!-- begin-user-doc --> Write your own initialization here <!-- end-user-doc -->
   *
   * @generated modifiable
   */
  private void readObject() {
    /* default - does nothing empty block */
  }
}
