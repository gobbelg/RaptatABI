/* First created by JCasGen Fri Aug 23 17:00:27 CDT 2013 */
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
 * Updated by JCasGen Fri Sep 06 20:16:19 CDT 2013
 *
 * @generated
 */
public class UIMASentence_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = UIMASentence.typeIndexID;
  /**
   * @generated
   * @modifiable
   */
  @SuppressWarnings("hiding")
  public static final boolean featOkTst = JCasRegistry
      .getFeatOkTst("main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
  /** @generated */
  private final FSGenerator fsGenerator = new FSGenerator() {
    @Override
    public FeatureStructure createFS(int addr, CASImpl cas) {
      if (UIMASentence_Type.this.useExistingInstance) {
        // Return eq fs instance if already created
        FeatureStructure fs = UIMASentence_Type.this.jcas.getJfsFromCaddr(addr);
        if (null == fs) {
          fs = new UIMASentence(addr, UIMASentence_Type.this);
          UIMASentence_Type.this.jcas.putJfsFromCaddr(addr, fs);
          return fs;
        }
        return fs;
      } else {
        return new UIMASentence(addr, UIMASentence_Type.this);
      }
    }
  };
  /** @generated */
  final Feature casFeat_SentenceTokens;

  /** @generated */
  final int casFeatCode_SentenceTokens;
  /** @generated */
  final Feature casFeat_containsAnnotatedPhrase;
  /** @generated */
  final int casFeatCode_containsAnnotatedPhrase;
  /** @generated */
  final Feature casFeat_phraseAnnotations;

  /** @generated */
  final int casFeatCode_phraseAnnotations;

  /** @generated */
  final Feature casFeat_sourcePath;

  /** @generated */
  final int casFeatCode_sourcePath;


  /**
   * initialize variables to correspond with Cas Type and Features
   *
   * @generated
   */
  public UIMASentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType,
        getFSGenerator());

    this.casFeat_SentenceTokens =
        jcas.getRequiredFeatureDE(casType, "SentenceTokens", "uima.cas.FSArray", featOkTst);
    this.casFeatCode_SentenceTokens =
        null == this.casFeat_SentenceTokens ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) this.casFeat_SentenceTokens).getCode();

    this.casFeat_containsAnnotatedPhrase = jcas.getRequiredFeatureDE(casType,
        "containsAnnotatedPhrase", "uima.cas.Boolean", featOkTst);
    this.casFeatCode_containsAnnotatedPhrase =
        null == this.casFeat_containsAnnotatedPhrase ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) this.casFeat_containsAnnotatedPhrase).getCode();

    this.casFeat_phraseAnnotations =
        jcas.getRequiredFeatureDE(casType, "phraseAnnotations", "uima.cas.FSArray", featOkTst);
    this.casFeatCode_phraseAnnotations =
        null == this.casFeat_phraseAnnotations ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) this.casFeat_phraseAnnotations).getCode();

    this.casFeat_sourcePath =
        jcas.getRequiredFeatureDE(casType, "sourcePath", "uima.cas.String", featOkTst);
    this.casFeatCode_sourcePath = null == this.casFeat_sourcePath ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_sourcePath).getCode();
  }


  /** @generated */
  public boolean getContainsAnnotatedPhrase(int addr) {
    if (featOkTst && this.casFeat_containsAnnotatedPhrase == null) {
      this.jcas.throwFeatMissing("containsAnnotatedPhrase",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return this.ll_cas.ll_getBooleanValue(addr, this.casFeatCode_containsAnnotatedPhrase);
  }


  /** @generated */
  public int getPhraseAnnotations(int addr) {
    if (featOkTst && this.casFeat_phraseAnnotations == null) {
      this.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations);
  }


  /** @generated */
  public int getPhraseAnnotations(int addr, int i) {
    if (featOkTst && this.casFeat_phraseAnnotations == null) {
      this.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    if (this.lowLevelTypeChecks) {
      return this.ll_cas.ll_getRefArrayValue(
          this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations), i, true);
    }
    this.jcas.checkArrayBounds(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations),
        i);
    return this.ll_cas.ll_getRefArrayValue(
        this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations), i);
  }


  /** @generated */
  public int getSentenceTokens(int addr) {
    if (featOkTst && this.casFeat_SentenceTokens == null) {
      this.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens);
  }


  /** @generated */
  public int getSentenceTokens(int addr, int i) {
    if (featOkTst && this.casFeat_SentenceTokens == null) {
      this.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    if (this.lowLevelTypeChecks) {
      return this.ll_cas.ll_getRefArrayValue(
          this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens), i, true);
    }
    this.jcas.checkArrayBounds(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens),
        i);
    return this.ll_cas
        .ll_getRefArrayValue(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens), i);
  }


  /** @generated */
  public String getSourcePath(int addr) {
    if (featOkTst && this.casFeat_sourcePath == null) {
      this.jcas.throwFeatMissing("sourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_sourcePath);
  }


  /** @generated */
  public void setContainsAnnotatedPhrase(int addr, boolean v) {
    if (featOkTst && this.casFeat_containsAnnotatedPhrase == null) {
      this.jcas.throwFeatMissing("containsAnnotatedPhrase",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.ll_cas.ll_setBooleanValue(addr, this.casFeatCode_containsAnnotatedPhrase, v);
  }


  /** @generated */
  public void setPhraseAnnotations(int addr, int v) {
    if (featOkTst && this.casFeat_phraseAnnotations == null) {
      this.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.ll_cas.ll_setRefValue(addr, this.casFeatCode_phraseAnnotations, v);
  }


  /** @generated */
  public void setPhraseAnnotations(int addr, int i, int v) {
    if (featOkTst && this.casFeat_phraseAnnotations == null) {
      this.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    if (this.lowLevelTypeChecks) {
      this.ll_cas.ll_setRefArrayValue(
          this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations), i, v, true);
    }
    this.jcas.checkArrayBounds(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations),
        i);
    this.ll_cas.ll_setRefArrayValue(
        this.ll_cas.ll_getRefValue(addr, this.casFeatCode_phraseAnnotations), i, v);
  }


  /** @generated */
  public void setSentenceTokens(int addr, int v) {
    if (featOkTst && this.casFeat_SentenceTokens == null) {
      this.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.ll_cas.ll_setRefValue(addr, this.casFeatCode_SentenceTokens, v);
  }


  /** @generated */
  public void setSentenceTokens(int addr, int i, int v) {
    if (featOkTst && this.casFeat_SentenceTokens == null) {
      this.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    if (this.lowLevelTypeChecks) {
      this.ll_cas.ll_setRefArrayValue(
          this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens), i, v, true);
    }
    this.jcas.checkArrayBounds(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens),
        i);
    this.ll_cas.ll_setRefArrayValue(
        this.ll_cas.ll_getRefValue(addr, this.casFeatCode_SentenceTokens), i, v);
  }


  /** @generated */
  public void setSourcePath(int addr, String v) {
    if (featOkTst && this.casFeat_sourcePath == null) {
      this.jcas.throwFeatMissing("sourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_sourcePath, v);
  }


  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {
    return this.fsGenerator;
  }
}
