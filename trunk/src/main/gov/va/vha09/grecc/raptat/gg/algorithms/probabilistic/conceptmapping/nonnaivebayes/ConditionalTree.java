package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.nonnaivebayes;

import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

/**
 * ********************************************************** ConditionalTree - Stores conditional
 * likelihood for given word phrase combinations
 *
 * @author Glenn Gobbel, Oct 15, 2010 *********************************************************
 */
public class ConditionalTree extends Hashtable<String, ConditionalTree> {
  private static final long serialVersionUID = -575831841689876486L;


  private NonNBConceptLookupTable theConcepts = null;

  public ConditionalTree(int hashSize) {
    super(hashSize);
  }


  private ConditionalTree(String[] phrase, int phraseIndex, int[] hashSizes, int hashSizeIndex,
      int[] conceptSizes, int conceptSizeIndex) {
    super(hashSizes[hashSizeIndex++]);
    this.theConcepts = new NonNBConceptLookupTable(conceptSizes[conceptSizeIndex++]);

    // Note that this next step is only dependent on phraseIndex and
    // phrase.length
    // so we can have extra elements in the hashSizes and conceptSizes
    // arrays
    if (phraseIndex < phrase.length) {
      String curAttribute = phrase[phraseIndex++];
      put(curAttribute, new ConditionalTree(phrase, phraseIndex, hashSizes, hashSizeIndex,
          conceptSizes, conceptSizeIndex));
    }
  }


  private ConditionalTree(String[] phrase, int[] hashSizes, int[] conceptSizes) {
    super(hashSizes[0]);
    this.theConcepts = new NonNBConceptLookupTable(conceptSizes[0]);

    if (phrase != null) {
      put(phrase[0], new ConditionalTree(phrase, 1, hashSizes, 1, conceptSizes, 1));
    }
  }


  public NonNBConceptLookupTable getConcepts() {
    return this.theConcepts;
  }


  /**
   * ************************************************************** Gets the last ConceptLookUpTable
   * referred to by a phrase
   *
   * @param phrase - String array holding the attributes of the phrase
   * @return - ConceptLookUpTable **************************************************************
   */
  public NonNBConceptLookupTable getConceptTable(String[] phrase) {
    if (phrase == null || phrase.length == 0) {
      return null;
    } else {
      ConditionalTree subTree;
      if (containsKey(phrase[0])) {
        subTree = get(phrase[0]);
      } else {
        return null;
      }

      if (phrase.length == 1) {
        return subTree.theConcepts;
      } else {
        return subTree.getConceptTable(GeneralHelper.getSubArray(phrase, 1));
      }
    }
  }


  /**
   * ************************************************************** Gets the last ConditionalTree
   * referred to by a phrase
   *
   * @param phrase - String array holding the attributes of the phrase
   * @return - ConditionalTree **************************************************************
   */
  public ConditionalTree getNextTree(String[] phrase) {
    if (phrase == null || phrase.length == 0) {
      return null;
    } else {
      ConditionalTree nextTree;
      if (containsKey(phrase[0])) {
        nextTree = get(phrase[0]);
      } else {
        return null;
      }

      if (phrase.length == 1) {
        return nextTree;
      } else {
        return nextTree.getNextTree(GeneralHelper.getSubArray(phrase, 1));
      }
    }
  }


  /**
   * ************************************************************** Increment the occurrences in the
   * ConceptLookupTables associated with the current phrase and concept.
   *
   * <p>
   * Precondition: inPhrase is not empty
   *
   * @param inPhrase - String array containing attributes to be incremented
   * @param curConcept - String containing concept mapped to attributes in inPhrase
   * @return - Boolean indicating if the phrase is unique
   *         **************************************************************
   */
  public boolean increment(String[] inPhrase, String curConcept) {
    boolean uniquePhrase = false;
    ConditionalTree curTree;
    NonNBOccurProbSet curVals;

    String[] subPhrase = GeneralHelper.getSubArray(inPhrase, 1);
    if (!containsKey(inPhrase[0])) {
      uniquePhrase = true;
      curTree = new ConditionalTree(subPhrase, RaptatConstants.NNB_COND_TREE_SIZES,
          RaptatConstants.NNB_CONCEPTS_PER_ATTRIBUTE);
      put(inPhrase[0], curTree);
    } else {
      curTree = get(inPhrase[0]);
    }

    if (subPhrase != null) {
      // Also set uniquePhrase to true if the subPhrase is unique in
      // terms of attributes or curConcept
      uniquePhrase = curTree.increment(subPhrase, curConcept);
    }

    if (curTree.theConcepts.containsKey(curConcept)) {
      curVals = curTree.theConcepts.get(curConcept);
    } else {
      // Finally, set uniquePhrase to true if the concept has
      // never been associated with the attribute at inPhrase[0]
      uniquePhrase = true;
      curVals = new NonNBOccurProbSet();
      curTree.theConcepts.put(curConcept, curVals);
    }

    curVals.incrementAll(uniquePhrase);

    return uniquePhrase;
  }


  public static void main(String[] args) {
    ConditionalTree myTestTree;

    myTestTree = new ConditionalTree(0);
    String[] myPhrase = {"my", "dog", "has", "fleas", "out", "the", "wazoo"};
    String[] newPhrase = {"my", "friend", "is", "here"};
    myTestTree.increment(myPhrase, "35");
    myTestTree.increment(newPhrase, "35");

    int j = 5;
    System.out.println(j);
  }
}
