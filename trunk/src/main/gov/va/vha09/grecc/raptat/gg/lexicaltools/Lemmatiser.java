package src.main.gov.va.vha09.grecc.raptat.gg.lexicaltools;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.math.NumberUtils;
import gov.nih.nlm.nls.lvg.Api.LvgCmdApi;
import gov.nih.nlm.nls.lvg.Db.DbUninflection;
import gov.nih.nlm.nls.lvg.Db.InflectionRecord;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.LemmatiserFactory;

/**
 * @author VHATVHJAYARS Shrimalini Jayaramaraja The Lemmatiser class first pre-processes the input
 *         string by removing stop words, plurals, possessives,diacritics,etc and then performs
 *         Lemmatisation. The lemmatize() method returns the input phrase with each of it's
 *         component words stemmed.
 */
public class Lemmatiser {

  // static Logger log = Logger.getLogger("Lemmatiser.class");
  private static HashMap<String, String> stemsMap = new HashMap<>();
  private static Set<String> stemKeys;

  static {
    if (Lemmatiser.stemsMap != null) {
      Lemmatiser.stemKeys = Lemmatiser.stemsMap.keySet();
    }
  }


  LvgCmdApi lvgApi;

  Connection conn;
  String lvgPropertiesPath = RaptatConstants.LVG_PROPERTIES_PATH;

  private boolean removeStops;;

  /**
   * Convert the path separators to the format accepted by the LvgCmdApi class
   *
   * @param path
   * @return pathToLvg
   */
  public String convertClasspathFormat(String path) {

    String pathToLvg = path + this.lvgPropertiesPath;
    pathToLvg = pathToLvg.replace('\\', '/');
    return pathToLvg;
  }


  /**
   * Classpath to the Lvg properties file is determined
   *
   * @return String lvg properties path
   */
  public String getLvgPath() {
    String lvgdirPath = null;
    // if ((System.getenv().containsKey("LVG_DIR")))
    // lvgdirPath = System.getenv("LVG_DIR");
    lvgdirPath = UserPreferences.INSTANCE.getLvgPath();
    if (lvgdirPath.equalsIgnoreCase("undefined")) {
      UserPreferences.INSTANCE.setLvgPath();
      lvgdirPath = UserPreferences.INSTANCE.getLvgPath();
      if (lvgdirPath.equalsIgnoreCase("undefined")) {
        lvgdirPath = null;
        GeneralHelper.errorWriter("Unable to locate path" + "to LVG directory");
      }
    }
    return convertClasspathFormat(lvgdirPath);
  }


  /** @return the removeStops */
  public boolean getRemoveStops() {
    return this.removeStops;
  }


  /**
   * ************************************************************************ parses the entire
   * string to stem/lemmatize its components. The stems/root for a given word is looked up in the
   * stems HashMap first. If the stem does not exist locally, the stem is looked up from the db and
   * entered in the stems Map for reducing costly db lookups.Returns numerical values as is.
   * ************************************************************************
   */
  public String lemmatize(String theWord, int partOfSpeechMask) {
    String normalizedWord;

    // Tag the word with the POS so we can get POS specific stems if needed
    String wordPOSMerge = theWord + String.valueOf(partOfSpeechMask);

    if (NumberUtils.isCreatable(theWord)) {
      return theWord;
    } // Lemmatize only if the string is NOT a numeric value
    else {
      if (Lemmatiser.stemKeys.contains(wordPOSMerge)) {
        return Lemmatiser.stemsMap.get(wordPOSMerge);
      } else {
        normalizedWord = this.run(theWord, partOfSpeechMask);
        Lemmatiser.stemsMap.put(wordPOSMerge, normalizedWord);
        return normalizedWord;
      }
    }
  }


  /*
   * parses the entire string to stem/lemmatize its components. The stems/root for a given word is
   * looked up in the stems HashMap first. If the stem does not exist locally, the stem is looked up
   * from the db and entered in the stems Map for reducing costly db lookups.Returns numerical
   * values as is.
   */
  public String[] lemmatize(String[] phrase) {
    String[] normalizedPhrase = new String[phrase.length];
    try {
      for (int i = 0; i < phrase.length; i++) {

        if (NumberUtils.isNumber(phrase[i])) {
          normalizedPhrase[i] = phrase[i];
          // Lemmatize only if the string is NOT a numeric value
        } else {
          if (Lemmatiser.stemKeys.contains(phrase[i])) {
            normalizedPhrase[i] = Lemmatiser.stemsMap.get(phrase[i]);
            // log.info("from map: " + normalizedPhrase[i]);
          } else {
            normalizedPhrase[i] = this.run(phrase[i]);
            if (normalizedPhrase[i] != null) {
              Lemmatiser.stemsMap.put(phrase[i], normalizedPhrase[i]);
              // log.info("from lemmatiser: " +
              // normalizedPhrase[i]);
            } else {
              normalizedPhrase[i] = phrase[i];
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return normalizedPhrase;
  }


  /*
   * lemmatize(String[] phrase,boolean removeStops) is an overloaded method that takes in the Phrase
   * string and whether stop words need to be removed before Lemmatization.
   */
  public String[] lemmatizePhrase(String[] phrase) {
    String[] normalizedPhrase = new String[phrase.length];
    String[] preProcessedPhrase;

    try {
      preProcessedPhrase = preProcessAndRemoveDashColons(phrase);
      normalizedPhrase = this.lemmatize(preProcessedPhrase);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return normalizedPhrase;
  }


  // destroy lvgCmdApi object and closes all connections
  public void lvgCleanup() {
    try {
      if (this.lvgApi != null) {
        this.conn.close();
        this.lvgApi.CleanUp();
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  // create an LgCmdApi object
  public void lvgConnection(boolean removeStops) {
    this.removeStops = removeStops;
    try {
      if (removeStops == false) {
        /*
         * The options are 'l' for converting all strings to lowercase, 'rs' for removing
         * PARENTHETICAL plural forms (e.g. 'antigen(s)'), and 'q' for removing diacritics. The -f
         * indicates that this is for "flow" control, an LVG term.
         */
        this.lvgConnection("-f:rs:q:l");
      } else {
        /*
         * The addition of the 't' in this options string creates an lvg connection that removes
         * stop words
         */
        this.lvgConnection("-f:l:rs:q:t");
      }
    } catch (Exception e) {
      e.getMessage();
    }
  }


  /**
   * Uses Lvg methods to pre-process the phrases to remove stop words, convert to singular and
   * lowercase, remove diacritics, etc.
   *
   * @param inputString The string to be processed
   * @return Processed string
   */
  @SuppressWarnings("CallToPrintStackTrace")
  public String preProcess(String inputString) {
    String[] outputTokens;
    String resultString = null;

    try {
      String lvgOutput;

      if (inputString != null) {
        if (!inputString.contains("|")) {
          lvgOutput = this.lvgApi.MutateToString(inputString);

          if (lvgOutput != null) {
            outputTokens = lvgOutput.split("\\|");
            resultString = outputTokens[1];
          }
        } else {
          resultString = inputString.toLowerCase();
        }
      }
    } catch (Exception e2) {
      e2.printStackTrace();
    }

    return resultString;
  }


  public String[] preProcess(String[] phraseStrings) {
    String[] resultArray = new String[phraseStrings.length];

    for (int i = 0; i < phraseStrings.length; i++) {
      resultArray[i] = this.preProcess(phraseStrings[i]);
    }

    return resultArray;
  }


  /*
   * Overloaded preProcess method that takes in a String [] of phrase words and the boolean
   * parameter removeStops.
   */
  public String[] preProcessAndRemoveDashColons(String[] phrase) {

    String processedPhrase = null;
    String temp = "";
    String[] resultArray = new String[phrase.length];
    try {
      for (int i = 0; i < phrase.length; i++) {
        if (phrase[i].contains("[: -]")) {
          processDashColons(phrase[i]);
          temp = temp + " " + phrase[i];
        } else {
          temp = temp + " " + phrase[i];
        }
      }

      processedPhrase = this.preProcess(temp.trim()).trim();
      resultArray = processedPhrase.split(" ");

    } catch (Exception e) {

    }
    return resultArray;
  }


  public void printLvgOutput(String[] outputTokens) {
    for (int i = 0; i < outputTokens.length; i++) {
      System.out.println(outputTokens[i]);
    }
  }


  public void printStemsMap() {
    if (!Lemmatiser.stemsMap.isEmpty()) {

      for (Map.Entry<String, String> entry : Lemmatiser.stemsMap.entrySet()) {
        System.out.println(entry.getKey() + " " + entry.getValue());
      }
    }
  }


  public String processDashColons(String word) {
    String[] wordComponents;
    String[] stemmedComponents;
    String temp = "";
    if (!NumberUtils.isNumber(word)) {
      word = word.replaceAll("[: -]", " ");
      wordComponents = word.split(" ");
      stemmedComponents = lemmatizePhrase(wordComponents);
      for (int i = 0; i < stemmedComponents.length; i++) {
        temp = temp + " " + stemmedComponents[i];
      }
      // temp.trim();

    }
    System.out.println(temp.trim());
    return temp;
  }


  /**
   * removeSpecialChars replaces some special characters
   *
   * @param word
   * @return
   */
  public String removeSpecialChars(String word) {

    if (!NumberUtils.isNumber(word)) {
      word = word.replaceAll("- : [( ) # .]", " ");
    }

    return word;
  }


  /**
   * run method that normalizes every word in a given string
   *
   * @param word
   * @return
   */
  public String run(String word) {
    String inputWord = word;
    String normalizedWord = null;
    List<InflectionRecord> lemmaList;

    try {
      if (this.conn != null) {
        /*
         * Get Uninflected root word obtained using static method DbUninflection .GetUninflections*
         */

        lemmaList = DbUninflection.GetUninflections(inputWord, this.conn);
        boolean rootObtained = false;
        for (int j = 0; j < lemmaList.size(); j++) {
          InflectionRecord rec = lemmaList.get(j);
          // System.out.println(rec.GetCategory()+rec.GetUninflectedTerm()+Category.ToName(256));
          // filtering by 2047 which is a combination of all
          // categories so that words that may fall under
          // different categories can be represented with this code.
          if (!rootObtained && rec.GetCategory() == 2047) {
            normalizedWord = rec.GetUninflectedTerm();
            rootObtained = true;
          } else {
            normalizedWord = rec.GetUninflectedTerm();
            rootObtained = true;
          }
        }
      }
    } catch (SQLException sqle) {
      System.err.println(sqle.getMessage());
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    return normalizedWord;
  }


  /**
   * **************************************** Return the first uninflected word that matches the
   * part of speech of the input word
   *
   * <p>
   * ****************************************
   */
  public String run(String inputWord, int partOfSpeechMask) {
    List<InflectionRecord> lemmaList;

    try {
      if (this.conn != null) {
        lemmaList = DbUninflection.GetUninflections(inputWord, this.conn);
        for (int j = 0; j < lemmaList.size(); j++) {
          InflectionRecord rec = lemmaList.get(j);
          if ((rec.GetCategory() & partOfSpeechMask) > 0) {
            return rec.GetUninflectedTerm();
          }
        }
      }
    } catch (SQLException sqle) {
      System.err.println(sqle.getMessage());
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    return inputWord;
  }


  // create an LgCmdApi object
  private void lvgConnection(String options) {

    try {
      // options to remove stop words,plurals and diacritics.

      /** ******** Original Code ****************** */
      this.lvgApi = new LvgCmdApi(options, getLvgPath());
      /** ***************************************** */

      // obtain a connection to lvg db
      this.conn = this.lvgApi.GetConnection();
    } catch (Exception e) {
      e.printStackTrace();
      // log.info(e.getMessage());
    }
  }


  public static void main(String[] args) {
    int lvgSetting = UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      @SuppressWarnings("ResultOfObjectAllocationIgnored")
      public void run() {
        String[] testArray =
            new String[] {"Several", "physicians", "responded", "to", "the", "call"};

        Lemmatiser lt = LemmatiserFactory.getInstance(true);

        String[] resultArray = lt.preProcessAndRemoveDashColons(testArray);

        System.out.println("PREPROCESSING");
        System.out.println("Test:" + Arrays.toString(testArray));
        System.out.println("Result:" + Arrays.toString(resultArray));

        resultArray = lt.lemmatize(testArray);

        System.out.println("LEMMATIZATION");
        System.out.println("Test:" + Arrays.toString(testArray));
        System.out.println("Result:" + Arrays.toString(resultArray));
      }
    });
  }
}
