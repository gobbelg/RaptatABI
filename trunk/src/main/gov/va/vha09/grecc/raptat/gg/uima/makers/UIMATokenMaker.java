package src.main.gov.va.vha09.grecc.raptat.gg.uima.makers;

import org.apache.uima.jcas.JCas;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.UIMAToken;

public class UIMATokenMaker extends AnnotationMaker {

  public UIMATokenMaker() {
    // TODO Auto-generated constructor stub
  }


  public UIMAToken newAnnotation(JCas jcas, RaptatToken inToken) {
    int start = Integer.valueOf(inToken.getStartOffset());
    int end = Integer.valueOf(inToken.getEndOffset());
    UIMAToken returnToken = this.newAnnotation(jcas, start, end);
    returnToken.setPOS(inToken.getPOS());
    returnToken.setStem(inToken.getStem());
    returnToken.setTokenStringAugmented(inToken.getTokenStringAugmented());
    return returnToken;
  }


  @Override
  UIMAToken newAnnotation(JCas jcas, int start, int end) {
    return new UIMAToken(jcas, start, end);
  }
}
