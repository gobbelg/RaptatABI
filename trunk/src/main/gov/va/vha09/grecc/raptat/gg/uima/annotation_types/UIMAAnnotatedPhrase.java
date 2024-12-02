package src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types;

/* First created by JCasGen Fri Sep 06 13:09:01 CDT 2013 */

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Updated by JCasGen Fri Sep 06 20:16:19 CDT 2013 XML source: /Users/glenn/RapTATFromArchive_130207
 * /RapTAT_Checkout_120514/trunk/resources/desc/UIMASentenceDescriptor.xml
 *
 * @generated
 */
public class UIMAAnnotatedPhrase extends Annotation {
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(UIMAAnnotatedPhrase.class);
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
  public UIMAAnnotatedPhrase(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }


  /** @generated */
  public UIMAAnnotatedPhrase(JCas jcas) {
    super(jcas);
    readObject();
  }


  /** @generated */
  public UIMAAnnotatedPhrase(JCas jcas, int begin, int end) {
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
  protected UIMAAnnotatedPhrase() {
    /* intentionally empty block */
  }


  /**
   * getter for concept - gets
   *
   * @generated
   */
  public String getConcept() {
    if (UIMAAnnotatedPhrase_Type.featOkTst
        && ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeat_concept == null) {
      this.jcasType.jcas.throwFeatMissing("concept",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_concept);
  }


  /**
   * getter for Tokens - gets
   *
   * @generated
   */
  public FSArray getTokens() {
    if (UIMAAnnotatedPhrase_Type.featOkTst
        && ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeat_Tokens == null) {
      this.jcasType.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    return (FSArray) this.jcasType.ll_cas.ll_getFSForRef(this.jcasType.ll_cas
        .ll_getRefValue(this.addr, ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_Tokens));
  }


  // *--------------*
  // * Feature: concept

  /**
   * indexed getter for Tokens - gets an indexed value -
   *
   * @generated
   */
  public UIMAToken getTokens(int i) {
    if (UIMAAnnotatedPhrase_Type.featOkTst
        && ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeat_Tokens == null) {
      this.jcasType.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    this.jcasType.jcas.checkArrayBounds(this.jcasType.ll_cas.ll_getRefValue(this.addr,
        ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_Tokens), i);
    return (UIMAToken) this.jcasType.ll_cas.ll_getFSForRef(
        this.jcasType.ll_cas.ll_getRefArrayValue(this.jcasType.ll_cas.ll_getRefValue(this.addr,
            ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_Tokens), i));
  }


  /** @generated */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }


  // *--------------*
  // * Feature: Tokens

  /**
   * setter for concept - sets
   *
   * @generated
   */
  public void setConcept(String v) {
    if (UIMAAnnotatedPhrase_Type.featOkTst
        && ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeat_concept == null) {
      this.jcasType.jcas.throwFeatMissing("concept",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_concept, v);
  }


  /**
   * setter for Tokens - sets
   *
   * @generated
   */
  public void setTokens(FSArray v) {
    if (UIMAAnnotatedPhrase_Type.featOkTst
        && ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeat_Tokens == null) {
      this.jcasType.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    this.jcasType.ll_cas.ll_setRefValue(this.addr,
        ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_Tokens,
        this.jcasType.ll_cas.ll_getFSRef(v));
  }


  /**
   * indexed setter for Tokens - sets an indexed value -
   *
   * @generated
   */
  public void setTokens(int i, UIMAToken v) {
    if (UIMAAnnotatedPhrase_Type.featOkTst
        && ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeat_Tokens == null) {
      this.jcasType.jcas.throwFeatMissing("Tokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMAAnnotatedPhrase");
    }
    this.jcasType.jcas.checkArrayBounds(this.jcasType.ll_cas.ll_getRefValue(this.addr,
        ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_Tokens), i);
    this.jcasType.ll_cas.ll_setRefArrayValue(
        this.jcasType.ll_cas.ll_getRefValue(this.addr,
            ((UIMAAnnotatedPhrase_Type) this.jcasType).casFeatCode_Tokens),
        i, this.jcasType.ll_cas.ll_getFSRef(v));
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
