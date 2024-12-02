/* First created by JCasGen Thu Aug 22 14:27:13 CDT 2013 */
package src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/**
 * Stores detailed information about the original source document from which the current CAS was
 * initialized. All information (like size) refers to the source document and not to the document in
 * the CAS which may be converted and filtered by a CAS Initializer. For example this information
 * will be written to the Semantic Search index so that the original document contents can be
 * retrieved by queries. Updated by JCasGen Mon Sep 09 11:17:14 CDT 2013
 *
 * @generated
 */
public class SourceDocumentInformation_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = SourceDocumentInformation.typeIndexID;
  /**
   * @generated
   * @modifiable
   */
  @SuppressWarnings("hiding")
  public static final boolean featOkTst = JCasRegistry.getFeatOkTst(
      "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
  /** @generated */
  private final FSGenerator fsGenerator = new FSGenerator() {
    @Override
    public FeatureStructure createFS(int addr, CASImpl cas) {
      if (SourceDocumentInformation_Type.this.useExistingInstance) {
        // Return eq fs instance if already created
        FeatureStructure fs = SourceDocumentInformation_Type.this.jcas.getJfsFromCaddr(addr);
        if (null == fs) {
          fs = new SourceDocumentInformation(addr, SourceDocumentInformation_Type.this);
          SourceDocumentInformation_Type.this.jcas.putJfsFromCaddr(addr, fs);
          return fs;
        }
        return fs;
      } else {
        return new SourceDocumentInformation(addr, SourceDocumentInformation_Type.this);
      }
    }
  };
  /** @generated */
  final Feature casFeat_uri;

  /** @generated */
  final int casFeatCode_uri;
  /** @generated */
  final Feature casFeat_offsetInSource;
  /** @generated */
  final int casFeatCode_offsetInSource;
  /** @generated */
  final Feature casFeat_documentSize;

  /** @generated */
  final int casFeatCode_documentSize;
  /** @generated */
  final Feature casFeat_lastSegment;
  /** @generated */
  final int casFeatCode_lastSegment;
  /** @generated */
  final Feature casFeat_SourcePath;

  /** @generated */
  final int casFeatCode_SourcePath;
  /** @generated */
  final Feature casFeat_SourceName;
  /** @generated */
  final int casFeatCode_SourceName;


  /**
   * initialize variables to correspond with Cas Type and Features
   *
   * @generated
   */
  public SourceDocumentInformation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType,
        getFSGenerator());

    this.casFeat_uri = jcas.getRequiredFeatureDE(casType, "uri", "uima.cas.String", featOkTst);
    this.casFeatCode_uri = null == this.casFeat_uri ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_uri).getCode();

    this.casFeat_offsetInSource =
        jcas.getRequiredFeatureDE(casType, "offsetInSource", "uima.cas.Integer", featOkTst);
    this.casFeatCode_offsetInSource =
        null == this.casFeat_offsetInSource ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) this.casFeat_offsetInSource).getCode();

    this.casFeat_documentSize =
        jcas.getRequiredFeatureDE(casType, "documentSize", "uima.cas.Integer", featOkTst);
    this.casFeatCode_documentSize = null == this.casFeat_documentSize ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_documentSize).getCode();

    this.casFeat_lastSegment =
        jcas.getRequiredFeatureDE(casType, "lastSegment", "uima.cas.Boolean", featOkTst);
    this.casFeatCode_lastSegment = null == this.casFeat_lastSegment ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_lastSegment).getCode();

    this.casFeat_SourcePath =
        jcas.getRequiredFeatureDE(casType, "SourcePath", "uima.cas.String", featOkTst);
    this.casFeatCode_SourcePath = null == this.casFeat_SourcePath ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_SourcePath).getCode();

    this.casFeat_SourceName =
        jcas.getRequiredFeatureDE(casType, "SourceName", "uima.cas.String", featOkTst);
    this.casFeatCode_SourceName = null == this.casFeat_SourceName ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_SourceName).getCode();
  }


  /** @generated */
  public int getDocumentSize(int addr) {
    if (featOkTst && this.casFeat_documentSize == null) {
      this.jcas.throwFeatMissing("documentSize",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.ll_cas.ll_getIntValue(addr, this.casFeatCode_documentSize);
  }


  /** @generated */
  public boolean getLastSegment(int addr) {
    if (featOkTst && this.casFeat_lastSegment == null) {
      this.jcas.throwFeatMissing("lastSegment",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.ll_cas.ll_getBooleanValue(addr, this.casFeatCode_lastSegment);
  }


  /** @generated */
  public int getOffsetInSource(int addr) {
    if (featOkTst && this.casFeat_offsetInSource == null) {
      this.jcas.throwFeatMissing("offsetInSource",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.ll_cas.ll_getIntValue(addr, this.casFeatCode_offsetInSource);
  }


  /** @generated */
  public String getSourceName(int addr) {
    if (featOkTst && this.casFeat_SourceName == null) {
      this.jcas.throwFeatMissing("SourceName",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_SourceName);
  }


  /** @generated */
  public String getSourcePath(int addr) {
    if (featOkTst && this.casFeat_SourcePath == null) {
      this.jcas.throwFeatMissing("SourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_SourcePath);
  }


  /** @generated */
  public String getUri(int addr) {
    if (featOkTst && this.casFeat_uri == null) {
      this.jcas.throwFeatMissing("uri",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_uri);
  }


  /** @generated */
  public void setDocumentSize(int addr, int v) {
    if (featOkTst && this.casFeat_documentSize == null) {
      this.jcas.throwFeatMissing("documentSize",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.ll_cas.ll_setIntValue(addr, this.casFeatCode_documentSize, v);
  }


  /** @generated */
  public void setLastSegment(int addr, boolean v) {
    if (featOkTst && this.casFeat_lastSegment == null) {
      this.jcas.throwFeatMissing("lastSegment",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.ll_cas.ll_setBooleanValue(addr, this.casFeatCode_lastSegment, v);
  }


  /** @generated */
  public void setOffsetInSource(int addr, int v) {
    if (featOkTst && this.casFeat_offsetInSource == null) {
      this.jcas.throwFeatMissing("offsetInSource",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.ll_cas.ll_setIntValue(addr, this.casFeatCode_offsetInSource, v);
  }


  /** @generated */
  public void setSourceName(int addr, String v) {
    if (featOkTst && this.casFeat_SourceName == null) {
      this.jcas.throwFeatMissing("SourceName",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_SourceName, v);
  }


  /** @generated */
  public void setSourcePath(int addr, String v) {
    if (featOkTst && this.casFeat_SourcePath == null) {
      this.jcas.throwFeatMissing("SourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_SourcePath, v);
  }


  /** @generated */
  public void setUri(int addr, String v) {
    if (featOkTst && this.casFeat_uri == null) {
      this.jcas.throwFeatMissing("uri",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.SourceDocumentInformation");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_uri, v);
  }


  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {
    return this.fsGenerator;
  }
}
