package src.main.gov.va.vha09.grecc.raptat.gg.uima.makers;

import java.util.List;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.UIMASentence;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.UIMAToken;

public class UIMASentenceMaker extends AnnotationMaker {

  private static final UIMATokenMaker tokenMaker = new UIMATokenMaker();


  public UIMASentenceMaker() {
    // TODO Auto-generated constructor stub
  }


  public UIMASentence newAnnotation(JCas inputJCas, AnnotatedPhrase inSentence, String sourcePath) {
    int begin = Integer.valueOf(inSentence.getRawTokensStartOff());
    int end = Integer.valueOf(inSentence.getRawTokensEndOff());
    UIMASentence resultSentence = this.newAnnotation(inputJCas, begin, end);

    List<RaptatToken> theTokens = inSentence.getProcessedTokens();
    FeatureStructure[] uimaTokenArray = new FeatureStructure[theTokens.size()];

    int i = 0;
    for (RaptatToken curToken : theTokens) {
      UIMAToken uimaToken = tokenMaker.newAnnotation(inputJCas, curToken);
      uimaToken.setPOS(curToken.getPOS());
      uimaTokenArray[i++] = tokenMaker.newAnnotation(inputJCas, curToken);
    }

    FSArray uimaTokens = new FSArray(inputJCas, uimaTokenArray.length);
    uimaTokens.copyFromArray(uimaTokenArray, 0, 0, uimaTokenArray.length);
    resultSentence.setSentenceTokens(uimaTokens);
    resultSentence.setContainsAnnotatedPhrase(false);
    resultSentence.setSourcePath(sourcePath);
    return resultSentence;
  }


  @Override
  public UIMASentence newAnnotation(JCas jcas, int start, int end) {
    return new UIMASentence(jcas, start, end);
  }
}
