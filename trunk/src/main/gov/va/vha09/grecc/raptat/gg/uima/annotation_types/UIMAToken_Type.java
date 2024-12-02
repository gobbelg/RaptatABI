/* First created by JCasGen Fri Aug 23 16:55:57 CDT 2013 */
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
 * Updated by JCasGen Mon Sep 09 11:17:54 CDT 2013
 *
 * @generated
 */
public class UIMAToken_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = UIMAToken.typeIndexID;
  /**
   * @generated
   * @modifiable
   */
  @SuppressWarnings("hiding")
  public static final boolean featOkTst =
      JCasRegistry.getFeatOkTst("main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
  /** @generated */
  private final FSGenerator fsGenerator = new FSGenerator() {
    @Override
    public FeatureStructure createFS(int addr, CASImpl cas) {
      if (UIMAToken_Type.this.useExistingInstance) {
        // Return eq fs instance if already created
        FeatureStructure fs = UIMAToken_Type.this.jcas.getJfsFromCaddr(addr);
        if (null == fs) {
          fs = new UIMAToken(addr, UIMAToken_Type.this);
          UIMAToken_Type.this.jcas.putJfsFromCaddr(addr, fs);
          return fs;
        }
        return fs;
      } else {
        return new UIMAToken(addr, UIMAToken_Type.this);
      }
    }
  };
  /** @generated */
  final Feature casFeat_POS;

  /** @generated */
  final int casFeatCode_POS;
  /** @generated */
  final Feature casFeat_stem;
  /** @generated */
  final int casFeatCode_stem;
  /** @generated */
  final Feature casFeat_tokenStringAugmented;

  /** @generated */
  final int casFeatCode_tokenStringAugmented;


  /**
   * initialize variables to correspond with Cas Type and Features
   *
   * @generated
   */
  public UIMAToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    this.casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType,
        getFSGenerator());

    this.casFeat_POS = jcas.getRequiredFeatureDE(casType, "POS", "uima.cas.String", featOkTst);
    this.casFeatCode_POS = null == this.casFeat_POS ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_POS).getCode();

    this.casFeat_stem = jcas.getRequiredFeatureDE(casType, "stem", "uima.cas.String", featOkTst);
    this.casFeatCode_stem = null == this.casFeat_stem ? JCas.INVALID_FEATURE_CODE
        : ((FeatureImpl) this.casFeat_stem).getCode();

    this.casFeat_tokenStringAugmented =
        jcas.getRequiredFeatureDE(casType, "tokenStringAugmented", "uima.cas.String", featOkTst);
    this.casFeatCode_tokenStringAugmented =
        null == this.casFeat_tokenStringAugmented ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) this.casFeat_tokenStringAugmented).getCode();
  }


  /** @generated */
  public String getPOS(int addr) {
    if (featOkTst && this.casFeat_POS == null) {
      this.jcas.throwFeatMissing("POS",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_POS);
  }


  /** @generated */
  public String getStem(int addr) {
    if (featOkTst && this.casFeat_stem == null) {
      this.jcas.throwFeatMissing("stem",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_stem);
  }


  /** @generated */
  public String getTokenStringAugmented(int addr) {
    if (featOkTst && this.casFeat_tokenStringAugmented == null) {
      this.jcas.throwFeatMissing("tokenStringAugmented",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    return this.ll_cas.ll_getStringValue(addr, this.casFeatCode_tokenStringAugmented);
  }


  /** @generated */
  public void setPOS(int addr, String v) {
    if (featOkTst && this.casFeat_POS == null) {
      this.jcas.throwFeatMissing("POS",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_POS, v);
  }


  /** @generated */
  public void setStem(int addr, String v) {
    if (featOkTst && this.casFeat_stem == null) {
      this.jcas.throwFeatMissing("stem",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_stem, v);
  }


  /** @generated */
  public void setTokenStringAugmented(int addr, String v) {
    if (featOkTst && this.casFeat_tokenStringAugmented == null) {
      this.jcas.throwFeatMissing("tokenStringAugmented",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    this.ll_cas.ll_setStringValue(addr, this.casFeatCode_tokenStringAugmented, v);
  }


  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {
    return this.fsGenerator;
  }
}
