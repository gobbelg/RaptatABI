/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;

/**
 * @author VHATVHGOBBEG
 *
 */
/**
 * @author VHATVHGOBBEG
 *
 */
public class AbiHelper {

  public enum PrintMarks {
    LINE_SEPARATOR("---------------------------------------"),
    DOUBLE_LINE_SEPARATOR("=========================================");

    private String mark;

    private PrintMarks(final String mark) {
      this.mark = mark;
    }

    @Override
    public String toString() {
      return this.mark;
    }
  }

  private AbiHelper() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }

  /**
   * @param inputMap - Contains multilevel map from concept name to phrase class to attribute value
   *        hash to triple containing actual class, predicting class and a boolean of whether actual
   *        equals predicted.
   * @return Map of concepts to another map of attribute hashes to a list of triples of strings,
   *         each triple corresponding to the actual value, predicting value, and match of actual
   *         predicted for each phrase class. The last triple contains the actual vs predicted class
   *         and a boolean of whether they match for all phrase classes with the same attribute
   *         value hash combined.
   */
  public static Map<String, Map<String, List<String>>> matchPhraseClassesByConcept(
      final Map<String, Map<String, Map<String, Triple<String, String, Boolean>>>> inputMap) {

    /*
     * Used to store final results, which will be a mapping from concept name to a map of the
     * attribute value hashes to a list of actual, predicted, and actual vs predicted (true, false
     * string representing boolean) for each of the phrase classes
     */
    Map<String, Map<String, List<String>>> matchedPhraseClassResult = new HashMap<>();

    /*
     * Used to store intermediate results from combining multiple phrase classes that come from the
     * same attribute value hash. It will store
     */

    for (String conceptName : inputMap.keySet()) {

      Map<String, Map<String, Triple<String, String, Boolean>>> phraseToHashMap =
          inputMap.get(conceptName);

      /*
       * These two maps are used to create the returned map
       */
      Map<String, List<String>> mappedAttributeHashMap =
          matchedPhraseClassResult.computeIfAbsent(conceptName, k -> new HashMap<>());
      Map<String, MutableTriple<String, String, Boolean>> attributeHashToCombinedTriple =
          new HashMap<>();

      for (String phraseClass : phraseToHashMap.keySet()) {

        Map<String, Triple<String, String, Boolean>> attributeValueHashMap =
            phraseToHashMap.get(phraseClass);

        for (String attributeValueHash : attributeValueHashMap.keySet()) {

          /*
           * Get the Triple object mapped to by the attributeValueHash - note that results from
           * phrase classes that fall under the same concept should have the same
           * attributeValueHash. By default, predicted and actual are set to true.
           */
          MutableTriple<String, String, Boolean> combinedPhraseResultTriple =
              attributeHashToCombinedTriple.computeIfAbsent(attributeValueHash,
                  k -> MutableTriple.of("", "", true));
          Triple<String, String, Boolean> mappedTriple =
              attributeValueHashMap.get(attributeValueHash);

          List<String> resultStringList =
              mappedAttributeHashMap.computeIfAbsent(attributeValueHash, k -> new ArrayList<>());

          String actualClass = mappedTriple.getLeft();
          String predictedClass = mappedTriple.getMiddle();
          Boolean predictedEqualsActual = mappedTriple.getRight();
          resultStringList.add(actualClass);
          resultStringList.add(predictedClass);
          resultStringList.add(predictedEqualsActual.toString());

          combinedPhraseResultTriple
              .setLeft(combinedPhraseResultTriple.getLeft() + "_" + actualClass);
          combinedPhraseResultTriple
              .setMiddle(combinedPhraseResultTriple.getMiddle() + "_" + predictedClass);
          combinedPhraseResultTriple
              .setRight(!predictedEqualsActual ? false : combinedPhraseResultTriple.getRight());

        }
      }

      /*
       * After adding all the phrase class results, add the combined result to the end
       */
      for (String mappedAttributeHash : mappedAttributeHashMap.keySet()) {

        List<String> resultStringList = mappedAttributeHashMap.get(mappedAttributeHash);
        MutableTriple<String, String, Boolean> combinedPhraseResultTriple =
            attributeHashToCombinedTriple.get(mappedAttributeHash);
        cleanCombinedPhraseTriple(combinedPhraseResultTriple);

        String actualClass = combinedPhraseResultTriple.getLeft();
        String predictedClass = combinedPhraseResultTriple.getMiddle();
        Boolean predictedEqualsActual = combinedPhraseResultTriple.getRight();
        resultStringList.add(actualClass);
        resultStringList.add(predictedClass);
        resultStringList.add(predictedEqualsActual.toString());
      }


    }

    return matchedPhraseClassResult;
  }



  /**
   * @param matchedResults
   * @param directoryPath
   * @throws FileNotFoundException
   */
  public static void printMatchedResults(
      final Map<String, Map<String, List<String>>> matchedResults, final String directoryPath)
      throws FileNotFoundException {

    String outputPath =
        directoryPath + File.separator + "ResultsOuput_" + System.currentTimeMillis() + ".txt";
    File outputFile = new File(outputPath);
    PrintWriter pw = new PrintWriter(outputFile);

    matchedResults.keySet()
        .forEach(conceptName -> matchedResults.get(conceptName).keySet()
            .forEach(attributeHash -> pw.println(conceptName + "\t" + attributeHash + "\t"
                + String.join("\t", matchedResults.get(conceptName).get(attributeHash)))));

    matchedResults.keySet()
        .forEach(conceptName -> matchedResults.get(conceptName).keySet()
            .forEach(attributeHash -> System.out.println(conceptName + "\t" + attributeHash + "\t"
                + String.join("\t", matchedResults.get(conceptName).get(attributeHash)))));

    System.out.println("Output saved in file:\n" + outputFile.getAbsolutePath());

    pw.flush();
    pw.close();
  }

  /**
   * Handles the logic when there are two or more phrase class assignments, and one of the phrase
   * class classifications is unassigned. In that case, the combination should also be unassigned.
   *
   * Note that this may alter the values stored in the triple.
   *
   * @param combinedPhraseResultTriple
   */
  private static void cleanCombinedPhraseTriple(
      final MutableTriple<String, String, Boolean> combinedPhraseResultTriple) {

    String unassignedName = PhraseClass.NO_CLASS_ASSIGNED.getConceptName().toLowerCase();

    if (combinedPhraseResultTriple.left.toLowerCase().contains(unassignedName)) {
      combinedPhraseResultTriple.setLeft(unassignedName);
    }

    if (combinedPhraseResultTriple.middle.toLowerCase().contains(unassignedName)) {
      combinedPhraseResultTriple.setMiddle(unassignedName);
    }

    combinedPhraseResultTriple
        .setRight(combinedPhraseResultTriple.left.equals(combinedPhraseResultTriple.middle));

  }

}
