/* First created by JCasGen Fri Aug 23 17:00:27 CDT 2013 */
package src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types;

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
public class UIMASentence extends Annotation {
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(UIMASentence.class);
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
  public UIMASentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }


  /** @generated */
  public UIMASentence(JCas jcas) {
    super(jcas);
    readObject();
  }


  /** @generated */
  public UIMASentence(JCas jcas, int begin, int end) {
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
  protected UIMASentence() {
    /* intentionally empty block */
  }


  /**
   * getter for containsAnnotatedPhrase - gets
   *
   * @generated
   */
  public boolean getContainsAnnotatedPhrase() {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_containsAnnotatedPhrase == null) {
      this.jcasType.jcas.throwFeatMissing("containsAnnotatedPhrase",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return this.jcasType.ll_cas.ll_getBooleanValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_containsAnnotatedPhrase);
  }


  /**
   * getter for phraseAnnotations - gets
   *
   * @generated
   */
  public FSArray getPhraseAnnotations() {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_phraseAnnotations == null) {
      this.jcasType.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return (FSArray) this.jcasType.ll_cas.ll_getFSForRef(this.jcasType.ll_cas.ll_getRefValue(
        this.addr, ((UIMASentence_Type) this.jcasType).casFeatCode_phraseAnnotations));
  }


  // *--------------*
  // * Feature: SentenceTokens

  /**
   * indexed getter for phraseAnnotations - gets an indexed value -
   *
   * @generated
   */
  public UIMAAnnotatedPhrase getPhraseAnnotations(int i) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_phraseAnnotations == null) {
      this.jcasType.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.jcas.checkArrayBounds(this.jcasType.ll_cas.ll_getRefValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_phraseAnnotations), i);
    return (UIMAAnnotatedPhrase) this.jcasType.ll_cas.ll_getFSForRef(
        this.jcasType.ll_cas.ll_getRefArrayValue(this.jcasType.ll_cas.ll_getRefValue(this.addr,
            ((UIMASentence_Type) this.jcasType).casFeatCode_phraseAnnotations), i));
  }


  /**
   * getter for SentenceTokens - gets
   *
   * @generated
   */
  public FSArray getSentenceTokens() {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_SentenceTokens == null) {
      this.jcasType.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return (FSArray) this.jcasType.ll_cas.ll_getFSForRef(this.jcasType.ll_cas
        .ll_getRefValue(this.addr, ((UIMASentence_Type) this.jcasType).casFeatCode_SentenceTokens));
  }


  /**
   * indexed getter for SentenceTokens - gets an indexed value -
   *
   * @generated
   */
  public UIMAToken getSentenceTokens(int i) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_SentenceTokens == null) {
      this.jcasType.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.jcas.checkArrayBounds(this.jcasType.ll_cas.ll_getRefValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_SentenceTokens), i);
    return (UIMAToken) this.jcasType.ll_cas.ll_getFSForRef(
        this.jcasType.ll_cas.ll_getRefArrayValue(this.jcasType.ll_cas.ll_getRefValue(this.addr,
            ((UIMASentence_Type) this.jcasType).casFeatCode_SentenceTokens), i));
  }


  /**
   * getter for sourcePath - gets
   *
   * @generated
   */
  public String getSourcePath() {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_sourcePath == null) {
      this.jcasType.jcas.throwFeatMissing("sourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    return this.jcasType.ll_cas.ll_getStringValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_sourcePath);
  }


  // *--------------*
  // * Feature: containsAnnotatedPhrase

  /** @generated */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }


  /**
   * setter for containsAnnotatedPhrase - sets
   *
   * @generated
   */
  public void setContainsAnnotatedPhrase(boolean v) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_containsAnnotatedPhrase == null) {
      this.jcasType.jcas.throwFeatMissing("containsAnnotatedPhrase",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.ll_cas.ll_setBooleanValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_containsAnnotatedPhrase, v);
  }


  // *--------------*
  // * Feature: phraseAnnotations

  /**
   * setter for phraseAnnotations - sets
   *
   * @generated
   */
  public void setPhraseAnnotations(FSArray v) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_phraseAnnotations == null) {
      this.jcasType.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.ll_cas.ll_setRefValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_phraseAnnotations,
        this.jcasType.ll_cas.ll_getFSRef(v));
  }


  /**
   * indexed setter for phraseAnnotations - sets an indexed value -
   *
   * @generated
   */
  public void setPhraseAnnotations(int i, UIMAAnnotatedPhrase v) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_phraseAnnotations == null) {
      this.jcasType.jcas.throwFeatMissing("phraseAnnotations",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.jcas.checkArrayBounds(this.jcasType.ll_cas.ll_getRefValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_phraseAnnotations), i);
    this.jcasType.ll_cas.ll_setRefArrayValue(
        this.jcasType.ll_cas.ll_getRefValue(this.addr,
            ((UIMASentence_Type) this.jcasType).casFeatCode_phraseAnnotations),
        i, this.jcasType.ll_cas.ll_getFSRef(v));
  }


  /**
   * setter for SentenceTokens - sets
   *
   * @generated
   */
  public void setSentenceTokens(FSArray v) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_SentenceTokens == null) {
      this.jcasType.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.ll_cas.ll_setRefValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_SentenceTokens,
        this.jcasType.ll_cas.ll_getFSRef(v));
  }


  /**
   * indexed setter for SentenceTokens - sets an indexed value -
   *
   * @generated
   */
  public void setSentenceTokens(int i, UIMAToken v) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_SentenceTokens == null) {
      this.jcasType.jcas.throwFeatMissing("SentenceTokens",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.jcas.checkArrayBounds(this.jcasType.ll_cas.ll_getRefValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_SentenceTokens), i);
    this.jcasType.ll_cas.ll_setRefArrayValue(
        this.jcasType.ll_cas.ll_getRefValue(this.addr,
            ((UIMASentence_Type) this.jcasType).casFeatCode_SentenceTokens),
        i, this.jcasType.ll_cas.ll_getFSRef(v));
  }


  // *--------------*
  // * Feature: sourcePath

  /**
   * setter for sourcePath - sets
   *
   * @generated
   */
  public void setSourcePath(String v) {
    if (UIMASentence_Type.featOkTst
        && ((UIMASentence_Type) this.jcasType).casFeat_sourcePath == null) {
      this.jcasType.jcas.throwFeatMissing("sourcePath",
          "main.gov.va.vha09.grecc.raptat.uima.annotation_types.UIMASentence");
    }
    this.jcasType.ll_cas.ll_setStringValue(this.addr,
        ((UIMASentence_Type) this.jcasType).casFeatCode_sourcePath, v);
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
