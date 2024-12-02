package src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types;

/* First created by JCasGen Fri Sep 06 13:09:01 CDT 2013 */

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
public class UIMAAnnotatedPhrase_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = UIMAAnnotatedPhrase.typeIndexID;
  /**
   * @generated
   * @modifiable
   */
  @SuppressWarnings("hiding")
  public static final boolean featOkTst = JCasRegistry
      .getFeatOkTst("main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
  /** @generated */
  private final FSGenerator fsGenerator = new FSGenerator() {
    @Override
    public FeatureStructure createFS(int addr, CASImpl cas) {
      if (UIMAAnnotatedPhrase_Type.this.useExistingInstance) {
        // Return eq fs instance if already created
        FeatureStructure fs = UIMAAnnotatedPhrase_Type.this.jcas.getJfsFromCaddr(addr);
        if (null == fs) {
          fs = new UIMAAnnotatedPhrase(addr, UIMAAnnotatedPhrase_Type.this);
          UIMAAnnotatedPhrase_Type.this.jcas.putJfsFromCaddr(addr, fs);
          return fs;
        }
        return fs;
      } else {
        return new UIMAAnnotatedPhrase(addr, UIMAAnnotatedPhrase_Type.this);
      }
    }
  };
  /** @generated */
  final Feature casFeat_concept;

  /** @generated */
  final int casFeatCode_concept;
  /** @generated */
  final Feature casFeat_Tokens;

  /** @generated */
  final int casFeatCode_Tokens;


  /**
   * initialize variables to correspond with Cas Type and Features
   *
   * @generated
   */
  public UIMAAnnotatedPhrase_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType,
        getFSGenerator());

    this.casFeat_concept =
        jcas.getRequiredFeatureDE(casType, "concept", "uima.cas.String", featOkTst);
    this.casFeatCode_concept = null == this.casFeat_concept ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_concept).getCode();

    this.casFeat_Tokens =
        jcas.getRequiredFeatureDE(casType, "Tokens", "uima.cas.FSArray", featOkTst);
    this.casFeatCode_Tokens = null == this.casFeat_Tokens ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_Tokens).getCode();
  }


  /** @generated */
  public String getConcept(int addr) {
    if (featOkTst && this.casFeat_concept == null) {
      this.jcas.throwFeatMissing("concept",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_concept);
  }


  /** @generated */
  public int getTokens(int addr) {
    if (featOkTst && this.casFeat_Tokens == null) {
      this.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    return this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens);
  }


  /** @generated */
  public int getTokens(int addr, int i) {
    if (featOkTst && this.casFeat_Tokens == null) {
      this.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    if (this.lowLevelTypeChecks) {
      return this.ll_cas
          .ll_getRefArrayValue(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens), i, true);
    }
    this.jcas.checkArrayBounds(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens), i);
    return this.ll_cas
        .ll_getRefArrayValue(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens), i);
  }


  /** @generated */
  public void setConcept(int addr, String v) {
    if (featOkTst && this.casFeat_concept == null) {
      this.jcas.throwFeatMissing("concept",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_concept, v);
  }


  /** @generated */
  public void setTokens(int addr, int v) {
    if (featOkTst && this.casFeat_Tokens == null) {
      this.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    this.ll_cas.ll_setRefValue(addr, this.casFeatCode_Tokens, v);
  }


  /** @generated */
  public void setTokens(int addr, int i, int v) {
    if (featOkTst && this.casFeat_Tokens == null) {
      this.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    if (this.lowLevelTypeChecks) {
      this.ll_cas.ll_setRefArrayValue(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens), i,
          v, true);
    }
    this.jcas.checkArrayBounds(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens), i);
    this.ll_cas.ll_setRefArrayValue(this.ll_cas.ll_getRefValue(addr, this.casFeatCode_Tokens), i,
        v);
  }


  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {
    return this.fsGenerator;
  }
}
