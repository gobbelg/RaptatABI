package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import java.util.HashSet;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

public class FacilitatorBlockerDictionaryFactory {

  private static TokenContextAnalyzer dictionary = null;


  private FacilitatorBlockerDictionaryFactory() {
    // TODO Auto-generated constructor stub
  }


  public static synchronized TokenContextAnalyzer getInstance(String dictionaryPath,
      HashSet<String> acceptedConcepts) {
    if (dictionaryPath != null) {
      FacilitatorBlockerDictionaryFactory.dictionary =
          new TokenContextAnalyzer(dictionaryPath, acceptedConcepts);
    }

    return FacilitatorBlockerDictionaryFactory.dictionary;
  }
}
