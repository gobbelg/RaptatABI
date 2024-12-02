/** */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.raptatutilities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.ArrayUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.candidates.Thesaurus;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.SimpleHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.LiverSegmentConjunctionProcessor;

/** @author Glenn Gobbel */
public class PhraseVariantBuilder {
  /*
   * Convenience object to store configurations to pass between method calls
   */
  private class ParseConfiguration {
    /*
     * Store the String tag associated with lists of String objects that will be varied with order
     * and synonyms as a map from String tag to list
     */
    private final Map<String, List<String>> orderElementToStringsList;

    /*
     * Store the varying orders of String tags to be used to create phrase variants
     */
    private final List<String[]> elementOrders;


    /**
     * @param orderElementToStringsList
     * @param tokenStrings
     */
    private ParseConfiguration(Map<String, List<String>> orderElementToStringsList,
        List<String[]> elementOrders) {
      super();
      this.orderElementToStringsList = orderElementToStringsList;
      this.elementOrders = elementOrders;
    }
  }

  private static final char START_ELEMENT = '(';

  private static final char END_ELEMENT = ')';

  /* Make it easy to change the element tag if needed */
  private static final String ELEMENT_TAG = ":";

  private static final String ELEMENT_TAG_STRING =
      ".*\\w" + PhraseVariantBuilder.ELEMENT_TAG + "\\s.*";

  private static final Pattern ORDER_ELEMENT_TAG_PATTERN =
      Pattern.compile(PhraseVariantBuilder.ELEMENT_TAG_STRING);

  private static final Pattern NON_ALPHANUMERIC_ELEMENT_TAG_PATTERN =
      Pattern.compile("[^A-Za-z0-9- ]");


  private final Thesaurus thesaurus;

  /** */
  public PhraseVariantBuilder() {
    this.thesaurus = Thesaurus.INSTANCE;
  }


  public List<List<String>> getVariantsAsList(String[] phraseVariantDescriptors,
      boolean useThesaurus) {
    SimpleHashTree phraseVariantTree = null;
    try {
      List<List<String>> configurations = parse(phraseVariantDescriptors);
      List<ParseConfiguration> phraseVariantConfigurations =
          getPhraseVariantConfigurations(configurations, useThesaurus);
      phraseVariantTree = generatePhraseVariants(phraseVariantConfigurations);
    } catch (IllegalArgumentException e) {
      System.err.println("Error in parsing configuration for phrase variant generation\n"
          + e.getLocalizedMessage());
      System.exit(-1);
    }

    return phraseVariantTree.toList();
  }


  /**
   * Apr 23, 2018
   *
   * @param configChars
   * @param delimitChars
   * @param argChar
   */
  public void processCharacter(Deque<Character> configChars, Deque<Character> delimitChars,
      char argChar) throws IllegalArgumentException {
    switch (argChar) {
      case START_ELEMENT:
        if (!delimitChars.isEmpty()) {
          /*
           * Add START_ELEMENT charto configChars if it comes in the middle of the configuration
           * string (i.e. after another START_ELEMENT and before a matching END_ELEMENT
           */
          configChars.addLast(argChar);
        }
        delimitChars.addLast(argChar);
        break;
      case END_ELEMENT:
        /*
         * The delimiChars stack should always contain a START_ELEMENT char when we come to an
         * END_ELEMENT char
         */
        if (delimitChars.isEmpty()) {
          throw new IllegalArgumentException(
              "Unmatched closing bracket in phrase variant configuration");
        }
        delimitChars.removeLast();
        /*
         * Add END_ELEMENT chart configChars if it comes in the middle of the configuration string
         * (i.e. before the first START_ELEMENT matches its corresponding END_ELEMENT
         */
        if (!delimitChars.isEmpty()) {
          configChars.addLast(argChar);
        }
        break;
      default:
        /*
         * The delimiChars stack should always contain a START_ELEMENT char when we come to any
         * other character
         */
        if (delimitChars.isEmpty()) {
          throw new IllegalArgumentException(
              "Unbracketed configuration character in phrase variant configuration");
        }
        configChars.addLast(argChar);
        break;
    }
  }


  /**
   * Apr 24, 2018
   *
   * @param phraseVariantConfigurations
   */
  protected SimpleHashTree generatePhraseVariants(
      List<ParseConfiguration> phraseVariantConfigurations) {
    SimpleHashTree hashTree = new SimpleHashTree(4);
    for (ParseConfiguration configuration : phraseVariantConfigurations) {
      List<String[]> elementOrders = configuration.elementOrders;
      Map<String, List<String>> orderElementsMap = configuration.orderElementToStringsList;

      this.addConfiguration(elementOrders, orderElementsMap, hashTree);
    }
    return hashTree;
  }


  /**
   * Apr 24, 2018
   *
   * @param elementOrders
   * @param orderElementsMap
   * @param hashTree
   */
  private void addConfiguration(List<String[]> elementOrders,
      Map<String, List<String>> orderElementsMap, SimpleHashTree hashTree) {
    for (String[] elementOrder : elementOrders) {
      this.addConfiguration(elementOrder, orderElementsMap, hashTree, 0);
    }
  }


  /**
   * Apr 24, 2018
   *
   * @param elementOrders
   * @param orderElementMap
   * @param hashTree
   * @param elementOrderIndex
   */
  private void addConfiguration(String[] elementOrder, Map<String, List<String>> orderElementMap,
      SimpleHashTree hashTree, int elementOrderIndex) {
    String element = elementOrder[elementOrderIndex];
    List<String> tokenStrings = orderElementMap.get(element);
    for (String tokenString : tokenStrings) {
      SimpleHashTree nextHashTree = null;

      if ((nextHashTree = hashTree.get(tokenString)) == null) {
        nextHashTree = new SimpleHashTree(4);
        hashTree.put(tokenString, nextHashTree);
      }

      if (elementOrderIndex < elementOrder.length - 1) {
        this.addConfiguration(elementOrder, orderElementMap, nextHashTree, elementOrderIndex + 1);
      }
    }
  }


  private List<ParseConfiguration> getPhraseVariantConfigurations(List<List<String>> configurations,
      boolean useThesaurus) throws IllegalArgumentException {
    List<ParseConfiguration> results = new ArrayList<>(configurations.size());
    Matcher orderElementTagMatcher = PhraseVariantBuilder.ORDER_ELEMENT_TAG_PATTERN.matcher("");
    Matcher nonAlphanumericElementMatcher =
        PhraseVariantBuilder.NON_ALPHANUMERIC_ELEMENT_TAG_PATTERN.matcher("");

    for (List<String> configuration : configurations) {
      /*
       * Store the String tag associated with lists of String objects that will be varied with order
       * and synonyms as a map from String tag to list
       */
      Map<String, List<String>> orderElementToTokenStrings = new HashMap<>();

      /*
       * Store the varying orders of String tags to be used to create phrase variants
       */
      List<String[]> elementOrders = new ArrayList<>();

      for (String configElement : configuration) {
        orderElementTagMatcher.reset(configElement);
        nonAlphanumericElementMatcher.reset(configElement);

        /*
         * If there is a match, it means that it is a list of String objects
         */
        if (orderElementTagMatcher.find()) {
          try {
            setOrderElementMapping(orderElementToTokenStrings, configElement, useThesaurus);
          } catch (IllegalArgumentException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
          }
        }
        /*
         * Must be a list of elements specifying order and used as labels for the list of String
         * objects mentioned above
         */
        else if (!nonAlphanumericElementMatcher.find()) {
          String[] elementCandidates = configElement.trim().split("\\s+");
          elementOrders.add(elementCandidates);
        } else {
          throw new IllegalArgumentException(
              "Only alphanumeric token strings can generate phrase variants");
        }
      }

      results.add(new ParseConfiguration(orderElementToTokenStrings, elementOrders));
    }

    return results;
  }


  /**
   * Apr 23, 2018
   *
   * @param args
   */
  private List<List<String>> parse(String[] args) {
    List<List<String>> results = new ArrayList<>(args.length);

    for (String parseConfiguration : args) {
      System.out.println("Parsing phrase variant:" + parseConfiguration);

      if (parseConfiguration == null) {
        continue;
      }

      parseConfiguration = parseConfiguration.trim();

      /*
       * Create queues as array deques to store the configuration characters and for matching the
       * start and end delimiters
       */
      Deque<Character> configChars = new ArrayDeque<>();
      Deque<Character> delimitChars = new ArrayDeque<>();

      List<String> configurationElements = new ArrayList<>();
      results.add(configurationElements);
      for (char argChar : parseConfiguration.toCharArray()) {
        /*
         * Skip over spaces between sets of delimited configuration info
         */
        if (delimitChars.isEmpty() && argChar == ' ') {
          continue;
        }

        processCharacter(configChars, delimitChars, argChar);

        if (delimitChars.isEmpty()) {
          Character[] configCharsAsArray = configChars.toArray(new Character[] {});
          configChars.clear();
          String singleElement = new String(ArrayUtils.toPrimitive(configCharsAsArray));
          configurationElements.add(singleElement.trim());
        }
      }

      if (!delimitChars.isEmpty()) {
        throw new IllegalArgumentException(
            "Unmatched opening bracket in phrase variant configuration");
      }
    }

    return results;
  }


  /**
   * Apr 24, 2018
   *
   * @param elementTagMatcher
   * @param orderElementToTokenStrings
   * @param configElement
   * @param useThesaurus
   */
  private void setOrderElementMapping(Map<String, List<String>> orderElementToTokenStrings,
      String configElement, boolean useThesaurus) {
    String[] elementCandidates = configElement.trim().split("\\s+");

    String orderElement = "";
    Set<String> tokenStrings = new HashSet<>();
    for (String candidate : elementCandidates) {
      if (candidate.endsWith(PhraseVariantBuilder.ELEMENT_TAG)) {
        orderElement += candidate.substring(0, candidate.length() - 1);
      } else {
        candidate = candidate.toLowerCase();
        tokenStrings.add(candidate);
        if (useThesaurus) {
          List<String> candidateSynonyms = this.thesaurus.getSynonymsAsList(candidate);
          tokenStrings.addAll(candidateSynonyms);
        }
      }
    }
    orderElementToTokenStrings.put(orderElement, new ArrayList<>(tokenStrings));
  }


  /**
   * This was created to build phrase variants for locations in liver and to build the actual
   * lexicon used by RapTAT as a text page Apr 16, 2018
   *
   * @param args
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        String conjunctionString = "and";
        String prependString = "ConceptPhrase\t10_location_liver\t";
        String appendString = "\tFALSE\tFALSE\tTRUE";
        String titleString = "LexicalType\tConceptPhrase\tRequiresConceptPhrase/ForwardWindow"
            + "\tBlockable/BackwardWindow\tOverridesRaptat";
        LiverSegmentConjunctionProcessor conjunctionProcessor =
            new LiverSegmentConjunctionProcessor(conjunctionString);
        String pathToPrintFile =
            "H:\\AllFolders\\ResAppProjects\\ORD_Matheny_201406009D_Cirrhosis\\Glenn"
                + "\\CrossValidation\\LiverLocations_v07_180610.txt";
        PrintWriter pw = null;
        try {
          pw = new PrintWriter(pathToPrintFile);
          pw.println(titleString);
          pw.flush();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          System.exit(-1);
        }

        PhraseVariantBuilder builder = new PhraseVariantBuilder();

        String[] phraseVariantDescriptors = getPhraseVariantDescriptors();

        boolean useThesaurus = false;
        List<List<String>> phraseVariantList =
            builder.getVariantsAsList(phraseVariantDescriptors, useThesaurus);
        int i = 1;
        for (List<String> phraseList : phraseVariantList) {
          Iterator<String> stringIterator = phraseList.iterator();

          if (stringIterator.hasNext()) {
            StringBuilder resultPhrase = new StringBuilder(stringIterator.next());

            while (stringIterator.hasNext()) {
              resultPhrase.append(" ").append(stringIterator.next());
            }
            String resultString = resultPhrase.toString();
            resultString = resultString.toLowerCase().trim();

            /*
             * If the resultString does NOT have the conjunctionString, just print it out.
             * Otherwise, make sure the segments are valid, and only print it out if they are. Also
             * replace conjunction string with ampersand and divisor string
             */
            if (resultString.contains(conjunctionString)) {
              if (conjunctionProcessor.validSegments(resultString)) {
                String resultStringAmpersand = resultString.replace(conjunctionString, "&");
                if (i % 10000 == 0) {
                  System.out.println(i + ") " + resultStringAmpersand);
                }
                i++;
                pw.println(prependString + resultStringAmpersand + appendString);
                pw.flush();

                String resultStringSlash = resultString.replace(conjunctionString, "/");
                if (i % 10000 == 0) {
                  System.out.println(i++ + ") " + resultStringSlash);
                }
                i++;
                pw.println(prependString + resultStringSlash + appendString);
                pw.flush();

                if (i % 10000 == 0) {
                  System.out.println(i++ + ") " + resultString);
                }
                i++;
                pw.println(prependString + resultString + appendString);
                pw.flush();
              }
            } else {
              if (i % 10000 == 0) {
                System.out.println(i++ + ") " + resultString);
              }
              i++;
              pw.println(prependString + resultString + appendString);
              pw.flush();
            }
          }
        }
        pw.close();
        System.out.println("Done");

        System.exit(0);
      }


      private String[] getPhraseVariantDescriptors() {
        String[] variantDescriptors = new String[8];

        variantDescriptors[0] =
            "(1 4 5) (1 4 6 7) (1 4 8 9) (1 5) (1 6 7) (1 8 9) (2 3 4 5) (2 3 4 6 7) (2 3 4 8 9) (2 3 5) (2 3 6 7) (2 3 8 9)"
                + "(1: adjacent near neighboring adjoining neighbouring along around within inside on)"
                + "(2: close adjacent rostral caudal superficial deep)" + "(3: to)" + "(4: the)"
                + "(5: gallbladder) (6: falciform) (7: ligament) (8: ligamentis) (9: falciformis)";

        variantDescriptors[1] = "(1) (1: dome subcapsular surface)";

        variantDescriptors[2] =
            "(1 2 3 4 5 6 7) (1 3 4 5 6 7) (3 4 5 6 7) (3 4 6 7) (4 5 6 7) (4 6 7) (3 4) (6 7)"
                + "(1: adjacent near neighboring  adjoining neighbouring along around within inside on over under above below)"
                + " (2: the)"
                + " (3: superior inferior upper lower medial superolateral superomedial inferolateral inferomedial anterior posterior anterolateral anteromedial peripheral interior exterior central subcapsular)"
                + " (4: edge edges margin margins border borders boundary boundaries perimeter perimeters aspect part portion region aspects parts portions regions division divisions segment segments)"
                + " (5: of)  (6: segment )"
                + " (7: 1 2 3 4 5 6 7 8 one two three four five six seven eight i ii iii iv v vi vii viii ia iia iiia iva va via viia viiia)";

        variantDescriptors[3] = " (1 2 3 4 5 6 7 8) (1 2 4 5 6 7 8) (1 2 7 8) (1 2 5 7 8)"
            + " (1: close adjacent rostral caudal superficial deep)" + " (2: to)" + " (3: the)"
            + " (4: superior inferior upper lower medial superolateral superomedial inferolateral inferomedial anterior posterior anterolateral anteromedial peripheral interior exterior central subcapsular)"
            + " (5: edge edges margin margins border borders boundary boundaries perimeter perimeters aspect part portion region aspects parts portions regions division divisions segment segments)"
            + " (6: of) (7: segment )"
            + " (8: 1 2 3 4 5 6 7 8 one two three four five six seven eight i ii iii iv v vi vii viii ia iia iiia iva va via viia viiia)";

        variantDescriptors[4] =
            " (1 2 3 4 5 6 7 8) (1 2 4 5 6 7 8)  (1: continguous) (2: with) (3: the)"
                + " (4: superior inferior upper lower medial superolateral superomedial inferolateral inferomedial anterior posterior anterolateral anteromedial peripheral interior exterior central subcapsular)"
                + " (5: edge edges margin margins border borders boundary boundaries perimeter perimeters aspect part portion region aspects parts portions regions division divisions segment segments)"
                + " (6: of) (7: segment )"
                + " (8: 1 2 3 4 5 6 7 8 one two three four five six seven eight i ii iii iv v vi vii viii ia iia iiia iva va via viia viiia)";

        variantDescriptors[5] =
            " (1 2 3 4 5 6 7 10 7) (1 3 4 5 6 7 10 7) (1 2 3 4 5 6 8 10 8) (1 2 3 4 5 6 8 10 8) (1 3 4 5 6 9 10 9) (1 3 4 5 6 9 10 9)"
                + " (3 4 5 6 7 10 7) (3 4 5 6 7 10 7) (3 4 5 6 8 10 8) (3 4 5 6 8 10 8) (3 4 5 6 9 10 9) (3 4 5 6 9 10 9)"
                + " (1 2 3 4 5 6 7 10 7) (1 3 4 5 6 7 10 7) (1 2 3 4 5 6 8 10 8) (1 2 3 4 5 6 8 10 8) (1 3 4 5 6 9 10 9) (1 3 4 5 6 9 10 9)"
                + " (1 2 3 4 6 7 10 7) (1 3 4 6 7 10 7) (1 2 3 4 6 8 10 8) (1 2 3 4 6 8 10 8) (1 3 4 6 9 10 9) (1 3 4 6 9 10 9)"
                + " (3 4 6 7 10 7) (3 4 6 7 10 7) (3 4 6 8 10 8) (3 4 6 8 10 8) (3 4 6 9 10 9) (3 4 6 9 10 9)"
                + " (1 2 3 4 6 7 10 7) (1 3 4 6 7 10 7) (1 2 3 4 6 8 10 8) (1 2 3 4 6 8 10 8) (1 3 4 6 9 10 9) (1 3 4 6 9 10 9)"
                + " (6 7) ( 6 8) ( 6 9) (6 7 10 7) (6 8 10 8) (6 9 10 9)"
                + " (1: adjacent near neighboring  adjoining neighbouring along around within inside on over under above below)"
                + " (2: the)"
                + " (3: superior inferior upper lower medial superolateral superomedial inferolateral inferomedial anterior posterior anterolateral anteromedial peripheral interior exterior central subcapsular)"
                + " (4: edge edges margin margins border borders boundary boundaries perimeter perimeters aspect part portion region aspects parts portions regions division divisions segment segments junction)"
                + " (5: of)  (6: segments ) (7: 1 2 3 4 5 6 7 8)"
                + " (8: one two three four five six seven eight)"
                + " (9: i ii iii iv v vi vii viii ia iia iiia iva va via viia viiia) (10: and)";

        variantDescriptors[6] =
            " (1 2 3 4 5 6 7 8 11 8) (1 2 4 5 6 7 8 11 8) (1 2 3 4 5 6 7 9 11 9) (1 2 4 5 6 7 9 11 9) (1 2 3 4 5 6 7 10 11 10) (1 2 4 5 6 7 10 11 10)"
                + " (1 2 7 8 11 8) (1 2 7 9 11 9) (1 2 7 10 11 10) (1: close adjacent rostral caudal superficial deep)"
                + " (2: to) (3: the)"
                + " (4: superior inferior upper lower medial superolateral superomedial inferolateral inferomedial anterior posterior anterolateral anteromedial peripheral interior exterior central subcapsular)"
                + " (5: edge edges margin margins border borders boundary boundaries perimeter perimeters aspect part portion region aspects parts portions regions division divisions segment segments junction)"
                + " (6: of) (7: segments ) (8: 1 2 3 4 5 6 7 8)"
                + " (9: one two three four five six seven eight)"
                + " (10: i ii iii iv v vi vii viii ia iia iiia iva va via viia viiia) (11: and)";

        variantDescriptors[7] =
            " (1 2 3 4 5 6 7 8 11 8) (1 2 4 5 6 7 8 11 8) (1 2 3 4 5 6 7 9 11 9) (1 2 4 5 6 7 9 11 9) (1 2 3 4 5 6 7 10 11 10) (1 2 4 5 6 7 10 11 10) "
                + " (1: continguous continuous) (2: with)  (3: the)"
                + " (4: superior inferior upper lower medial superolateral superomedial inferolateral inferomedial anterior posterior anterolateral anteromedial peripheral interior exterior central subcapsular)"
                + " (5: edge edges margin margins border borders boundary boundaries perimeter perimeters aspect part portion region aspects parts portions regions division divisions segment segments junction)"
                + " (6: of) (7: segments )  (8: 1 2 3 4 5 6 7 8)"
                + " (9: one two three four five six seven eight)"
                + " (10: i ii iii iv v vi vii viii ia iia iiia iva va via viia viiia)  (11: and)";

        return variantDescriptors;
      }
    });
  }
}
