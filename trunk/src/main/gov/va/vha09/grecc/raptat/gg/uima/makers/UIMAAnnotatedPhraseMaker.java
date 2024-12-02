/** */
package src.main.gov.va.vha09.grecc.raptat.gg.uima.makers;

import java.util.Collections;
import java.util.List;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetTokenComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.UIMAAnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.UIMAToken;

/**
 * ******************************************************
 *
 * @author Glenn Gobbel - Sep 6, 2013 *****************************************************
 */
public class UIMAAnnotatedPhraseMaker extends AnnotationMaker {

  private static final StartOffsetTokenComparator tokenSorter = new StartOffsetTokenComparator();
  private static final UIMATokenMaker tokenMaker = new UIMATokenMaker();


  /**
   * ********************************************
   *
   * @author Glenn Gobbel - Sep 6, 2013 ********************************************
   */
  public UIMAAnnotatedPhraseMaker() {}


  public UIMAAnnotatedPhrase newAnnotation(JCas inputJCas, List<RaptatToken> annotationTokens) {
    FeatureStructure[] uimaTokenArray = new FeatureStructure[annotationTokens.size()];

    int i = 0;
    for (RaptatToken curToken : annotationTokens) {
      UIMAToken uimaToken = tokenMaker.newAnnotation(inputJCas, curToken);
      uimaToken.setPOS(curToken.getPOS());
      uimaTokenArray[i++] = tokenMaker.newAnnotation(inputJCas, curToken);
    }

    FSArray uimaTokens = new FSArray(inputJCas, uimaTokenArray.length);
    uimaTokens.copyFromArray(uimaTokenArray, 0, 0, uimaTokenArray.length);
    Collections.sort(annotationTokens, tokenSorter);
    int start = Integer.parseInt(annotationTokens.get(0).getStartOffset());
    int end = Integer.parseInt(annotationTokens.get(annotationTokens.size() - 1).getEndOffset());

    UIMAAnnotatedPhrase thePhrase = new UIMAAnnotatedPhrase(inputJCas, start, end);
    thePhrase.setTokens(uimaTokens);

    return thePhrase;
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD
   *
   * @param jcas
   * @param start
   * @param end
   * @return
   * @author Glenn Gobbel - Sep 6, 2013 ***********************************************************
   */
  @Override
  UIMAAnnotatedPhrase newAnnotation(JCas jcas, int start, int end) {
    return new UIMAAnnotatedPhrase(jcas, start, end);
  }
}
