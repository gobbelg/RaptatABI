/** */
package src.main.gov.va.vha09.grecc.raptat.gg.candidates;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.swing.SwingUtilities;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.TextReader;

/** @author Glenn T. Gobbel Jan 6, 2017 */
public enum Thesaurus {
  INSTANCE;

  private static final HashSet<String> ALLOWED_POS_SET =
      new HashSet<>(Arrays.asList(new String[] {"verb", "adj", "noun", "adv"}));


  HashMap<String, List<RaptatPair<String, List<String>>>> synonymMap;

  private Thesaurus() {
    InputStream dictionaryInputStream =
        this.getClass().getResourceAsStream(RaptatConstants.RELATIVE_THESAURUS_PATH);

    this.synonymMap = this.read(dictionaryInputStream);

    String[] allowedPos = new String[] {"verb"};

    System.out.println("Completed thesaurus read");
  }


  /**
   * May 4, 2018
   *
   * <p>
   * Get parts-of-speech for given word
   *
   * @param synonymWord
   * @return
   */
  public List<String> getPosAsList(String synonymWord) {
    List<String> resultList = new ArrayList<>();
    List<RaptatPair<String, List<String>>> posList = this.synonymMap.get(synonymWord);

    if (posList != null) {
      for (RaptatPair<String, List<String>> element : posList) {
        resultList.add(element.left);
      }
    }
    return resultList;
  }


  /**
   * Gets word synonyms without consideration of the part of speech
   *
   * @param synonymWord
   * @return
   */
  public List<String> getSynonymsAsList(String synonymWord) {
    return this.getSynonymsAsList(synonymWord, null);
  }


  /**
   * Get word synonyms for the specified part-of-speech May 4, 2018
   *
   * @param synonymWord
   * @param pos
   * @return
   */
  public List<String> getSynonymsAsList(String synonymWord, String pos) {
    List<String> resultList = new ArrayList<>();
    List<RaptatPair<String, List<String>>> synonymList = this.synonymMap.get(synonymWord);

    if (pos != null) {
      pos = pos.toLowerCase();
      if (!Thesaurus.ALLOWED_POS_SET.contains(pos)) {
        System.err.println(
            "When specifying thesaurus parts-of-speech, specify 'noun,' 'verb,' 'adj,' or 'adv' only");
        System.exit(-1);
      }
    }

    if (synonymList != null) {
      for (RaptatPair<String, List<String>> element : synonymList) {
        if (pos == null || element.left.equals(pos)) {
          resultList.addAll(element.right);
        }
      }
    }
    return resultList;
  }


  /**
   * @param thesaurusFilePath
   * @return
   */
  protected HashMap<String, List<RaptatPair<String, List<String>>>> read(String thesaurusFilePath) {
    HashMap<String, List<RaptatPair<String, List<String>>>> synonymMap = new HashMap<>();
    File thesaurusFile = new File(thesaurusFilePath);
    TextReader tr = new TextReader();
    tr.setFile(thesaurusFile);
    tr.setDelimiter("\\|");

    /* Skip first line which is just heading of character type */
    tr.getNextData();

    String[] nextData = null;
    while ((nextData = tr.getNextData()) != null && nextData.length > 0) {
      String phrase = nextData[0];
      int synonymLines = Integer.parseInt(nextData[1]);
      List<RaptatPair<String, List<String>>> synonymLists = new ArrayList<>(synonymLines);
      synonymMap.put(phrase, synonymLists);

      for (int i = 0; i < synonymLines; i++) {
        nextData = tr.getNextData();
        String pos = nextData[0];
        List<String> synonyms = new ArrayList<>(nextData.length - 1);
        for (int j = 1; j < nextData.length; j++) {
          synonyms.add(nextData[j]);
        }
        synonymLists.add(new RaptatPair<>(pos, synonyms));
      }
    }

    return synonymMap;
  }


  private HashMap<String, List<RaptatPair<String, List<String>>>> read(InputStream inputStream) {
    HashMap<String, List<RaptatPair<String, List<String>>>> synonymMap = new HashMap<>();
    try {
      String delimiter = "\\|";
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      String nextLine;

      /* Skip first line which is just heading of character type */
      nextLine = br.readLine();

      while ((nextLine = br.readLine()) != null && nextLine.length() > 0) {
        String[] nextData = nextLine.split(delimiter);
        String phrase = nextData[0];
        int synonymLines = Integer.parseInt(nextData[1]);
        List<RaptatPair<String, List<String>>> synonymLists = new ArrayList<>(synonymLines);
        synonymMap.put(phrase, synonymLists);

        for (int i = 0; i < synonymLines; i++) {
          nextLine = br.readLine();
          nextData = nextLine.split(delimiter);
          String pos = nextData[0];
          List<String> synonyms = new ArrayList<>(nextData.length - 1);
          for (int j = 1; j < nextData.length; j++) {
            synonyms.add(nextData[j]);
          }
          synonymLists.add(new RaptatPair<>(pos, synonyms));
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return synonymMap;
  }


  /**
   * @param args
   */
  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        Thesaurus thesaurus = Thesaurus.INSTANCE;

        System.out.println("\n\nEnter test phrase or EXIT to stop:");
        Scanner scanner = new Scanner(System.in);

        String testString;
        while (!(testString = scanner.nextLine()).equals("EXIT")) {
          String[] testElements = testString.toLowerCase().split("\\s+");
          try {
            int synonymIndex;
            try {
              synonymIndex = Integer.parseInt(testElements[testElements.length - 1]);
            } catch (NumberFormatException e) {
              System.out.println("---------------------------------------\n"
                  + "Synonym index is not an integer\nGetting synonym of first word\n"
                  + "---------------------------------------\n");
              synonymIndex = 0;
            }
            String synonymWord = testElements[synonymIndex];
            List<String> synonyms = thesaurus.getSynonymsAsList(synonymWord);
            Set<String> phraseSet = new HashSet<>();
            if (synonyms != null && synonyms.size() > 0) {
              for (String synonym : synonyms) {
                testElements[synonymIndex] = synonym;
                StringBuilder sb = new StringBuilder(testElements[0]);
                for (int i = 1; i < testElements.length - 1; i++) {
                  sb.append(" ").append(testElements[i]);
                }
                phraseSet.add(sb.toString());
              }

              List<String> phrasesForOutput = new ArrayList<>(phraseSet);
              Collections.sort(phrasesForOutput);
              for (String string : phrasesForOutput) {
                System.out.println(string);
              }
            } else {
              System.out.println("No synomyms found for " + "\"" + synonymWord + "\"");
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Synonym index outside of number of elements in phrase");
          }

          System.out.println("\n\nEnter test phrase or EXIT to stop:");
        }

        System.exit(0);
      }
    });
  }
}
