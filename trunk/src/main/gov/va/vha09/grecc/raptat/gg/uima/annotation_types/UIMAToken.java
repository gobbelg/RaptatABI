/* First created by JCasGen Fri Aug 23 16:55:57 CDT 2013 */
package src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Updated by JCasGen Mon Sep 09 11:17:54 CDT 2013 XML source: /Users/glenn/Documents
 * /workspace/ADAHF2/resources/desc/UIMATokenDescriptor.xml
 *
 * @generated
 */
public class UIMAToken extends Annotation {
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(UIMAToken.class);
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
  public UIMAToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }


  /** @generated */
  public UIMAToken(JCas jcas) {
    super(jcas);
    readObject();
  }


  /** @generated */
  public UIMAToken(JCas jcas, int begin, int end) {
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
  protected UIMAToken() {
    /* intentionally empty block */
  }


  /**
   * getter for POS - gets
   *
   * @generated
   */
  public String getPOS() {
    if (UIMAToken_Type.featOkTst && ((UIMAToken_Type) this.jcasType).casFeat_POS == null) {
      this.jcasType.jcas.throwFeatMissing("POS",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((UIMAToken_Type) this.jcasType).casFeatCode_POS);
  }


  /**
   * getter for Stem - gets
   *
   * @generated
   */
  public String getStem() {
    if (UIMAToken_Type.featOkTst && ((UIMAToken_Type) this.jcasType).casFeat_stem == null) {
      this.jcasType.jcas.throwFeatMissing("stem",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((UIMAToken_Type) this.jcasType).casFeatCode_stem);
  }


  // *--------------*
  // * Feature: POS

  /**
   * getter for tokenStringAugmented - gets
   *
   * @generated
   */
  public String getTokenStringAugmented() {
    if (UIMAToken_Type.featOkTst
        && ((UIMAToken_Type) this.jcasType).casFeat_tokenStringAugmented == null) {
      this.jcasType.jcas.throwFeatMissing("tokenStringAugmented",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((UIMAToken_Type) this.jcasType).casFeatCode_tokenStringAugmented);
  }


  /** @generated */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }


  // *--------------*
  // * Feature: Stem

  /**
   * setter for POS - sets
   *
   * @generated
   */
  public void setPOS(String v) {
    if (UIMAToken_Type.featOkTst && ((UIMAToken_Type) this.jcasType).casFeat_POS == null) {
      this.jcasType.jcas.throwFeatMissing("POS",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((UIMAToken_Type) this.jcasType).casFeatCode_POS, v);
  }


  /**
   * setter for Stem - sets
   *
   * @generated
   */
  public void setStem(String v) {
    if (UIMAToken_Type.featOkTst && ((UIMAToken_Type) this.jcasType).casFeat_stem == null) {
      this.jcasType.jcas.throwFeatMissing("stem",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((UIMAToken_Type) this.jcasType).casFeatCode_stem, v);
  }


  // *--------------*
  // * Feature: tokenStringAugmented

  /**
   * setter for tokenStringAugmented - sets
   *
   * @generated
   */
  public void setTokenStringAugmented(String v) {
    if (UIMAToken_Type.featOkTst
        && ((UIMAToken_Type) this.jcasType).casFeat_tokenStringAugmented == null) {
      this.jcasType.jcas.throwFeatMissing("tokenStringAugmented",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAToken");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((UIMAToken_Type) this.jcasType).casFeatCode_tokenStringAugmented, v);
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
