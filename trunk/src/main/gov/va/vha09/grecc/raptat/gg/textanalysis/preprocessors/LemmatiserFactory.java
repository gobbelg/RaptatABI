package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import src.main.gov.va.vha09.grecc.raptat.gg.lexicaltools.Lemmatiser;

public class LemmatiserFactory {

  // private static int stopWordStemmerUsers = 0;
  // private static int nonStopWordStemmerUsers = 0;
  private static Lemmatiser nonStopWordStemmerInstance = null;

  private static Lemmatiser stopWordStemmerInstance = null;


  private LemmatiserFactory() {}


  public static synchronized Lemmatiser getInstance(boolean removeStopWords) {
    if (removeStopWords) {
      if (LemmatiserFactory.stopWordStemmerInstance == null) {
        LemmatiserFactory.stopWordStemmerInstance = new Lemmatiser();
        LemmatiserFactory.stopWordStemmerInstance.lvgConnection(removeStopWords);
      }
      return LemmatiserFactory.stopWordStemmerInstance;
    }

    if (LemmatiserFactory.nonStopWordStemmerInstance == null) {
      LemmatiserFactory.nonStopWordStemmerInstance = new Lemmatiser();
      LemmatiserFactory.nonStopWordStemmerInstance.lvgConnection(removeStopWords);
    }
    return LemmatiserFactory.nonStopWordStemmerInstance;
  }
}
