package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.OccurProbTriplet;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.StrPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.StrPairSet;

/**
 * ConditionalTable - Store the occurrences and likelihood for association of particular tokens with
 * particular concepts. It is essentially a two-dimensional hash with the first key being a token
 * string, which returns a ConceptLookupTable. The second key is a concept that is used on the
 * returned ConceptLookupTable and returns a OccurProbTriplet, which contains the number of times
 * that a particular token string is associated with a given concept.
 *
 * @author Glenn Gobbel, Jul 7, 2010
 */
public class NBConditionalTable extends Hashtable<String, NBConceptLookupTable>
    implements Serializable {
  // Allows object of this class to be serialized
  private static final long serialVersionUID = -1509485800270785933L;

  protected StrPairSet modifiedElements;

  /*
   * This is used to determine whether the number of keys has changed since the last time the
   * probabilities were updated.
   */
  protected boolean vocabularySizeChanged;


  public NBConditionalTable(int startCapacity) {
    super(startCapacity);
    this.modifiedElements = new StrPairSet(RaptatConstants.NB_UNIQUE_TOKEN_CONCEPT_PAIRS);
    this.vocabularySizeChanged = false;
  }


  public void calcAllProbs() {
    // Run through all the tokens and their
    // associated concepts and calculate the values
    // used for calculating likelihood of a set of tokens
    // occurring with a particular concept
    Enumeration<String> tokenStrings = keys();
    while (tokenStrings.hasMoreElements()) {
      String curTokenString = tokenStrings.nextElement();
      NBConceptLookupTable curConcepts = get(curTokenString);
      Enumeration<String> theConcepts = curConcepts.keys();
      while (theConcepts.hasMoreElements()) {
        String curConcept = theConcepts.nextElement();
        OccurProbTriplet curSet = curConcepts.get(curConcept);
        curSet.unsmoothedProb = Math.log10(curSet.occurrences);
        curSet.smoothedProb = Math.log10(curSet.occurrences + 1);
        curSet.probabilitiesUpdated = true;
      }
    }
    this.modifiedElements.clear();
    this.vocabularySizeChanged = false;
  }


  /**
   * Calculate the likelihood values for entries that have been modified
   *
   * @param strPairSet
   */
  public void calcModifiedLikelihoods() {
    Iterator<StrPair> tokenStringConceptPairs = this.modifiedElements.iterator();
    while (tokenStringConceptPairs.hasNext()) {
      StrPair curElement = tokenStringConceptPairs.next();
      OccurProbTriplet curSet = getValue(curElement.left, curElement.right);

      curSet.unsmoothedProb = Math.log10(curSet.occurrences);
      curSet.smoothedProb = Math.log10(curSet.occurrences + 1);
      curSet.probabilitiesUpdated = true;
    }
    this.modifiedElements.clear();
    this.vocabularySizeChanged = false;
  }


  /**
   * **************************************************************** Returns the value specified by
   * two keys into the two dimensional table that backs this object.
   *
   * @param tokenString - Key into first dimension
   * @param concept - Key into second dimension
   * @return Triplet of values (occurrences and associated smoothed and unsmoothed likelihood) as
   *         specified by the keys ****************************************************************
   */
  public OccurProbTriplet getValue(String tokenString, String concept) {
    if (containsKey(tokenString) && get(tokenString).containsKey(concept)) {
      return get(tokenString).get(concept);
    } else {
      return null;
    }
  }


  /**
   * ************************************************************** Increment the occurrence table
   * at the location specified by the first and second keys
   *
   * @param tokenString - First key into the table
   * @param theConcept - Second key into the table
   *        **************************************************************
   */
  public boolean increment(String tokenString, String theConcept) {
    NBConceptLookupTable conceptTable;
    boolean isNewToken = true;
    this.modifiedElements.addPair(tokenString, theConcept);

    if (containsKey(tokenString)) {
      conceptTable = get(tokenString);
      if (conceptTable.containsKey(theConcept)) {
        OccurProbTriplet theSet = conceptTable.get(theConcept);
        theSet.occurrences++;
        theSet.probabilitiesUpdated = false;
      } else {
        // Put 1 if no value exists
        conceptTable.put(theConcept, new OccurProbTriplet(1, 0.0, 0.0));
      }

      isNewToken = false;
    } else {
      this.vocabularySizeChanged = true;

      // If neither first nor second key exists, create a new One-way
      // likelihood table with firstKey returning the table
      conceptTable = new NBConceptLookupTable(RaptatConstants.NB_CONCEPTS_PER_ATTRIBUTE);
      put(tokenString, conceptTable);

      // Now set the value to 1 as it's the first time this
      // firstKey, secondKey combination has been seen.
      conceptTable.put(theConcept, new OccurProbTriplet(1, 0.0, 0.0));
    }

    return isNewToken;
  }


  public void printFirstKeys() {
    Enumeration<String> firstDimKeys = keys();
    System.out.println("\nKeys for AttributeOccTable:");
    while (firstDimKeys.hasMoreElements()) {
      System.out.println(firstDimKeys.nextElement());
    }
  }


  public void printFirstSecondKeys() {
    System.out.println("\nFirst & Second Keys for AttributeOccTable:");

    Enumeration<String> firstDimKeys = keys();
    while (firstDimKeys.hasMoreElements()) {
      String firstKey = firstDimKeys.nextElement();
      if (containsKey(firstKey)) {
        NBConceptLookupTable tempTable = get(firstKey);
        Enumeration<String> secondKeys = tempTable.keys();
        System.out.println("\nAssociated concepts for attribute " + firstKey + ":");
        while (secondKeys.hasMoreElements()) {
          System.out.println("\t" + secondKeys.nextElement());
        }
      } else {
        System.out.println("No second keys associated with " + firstKey);
      }
    }
  }


  public void printSecondKeys(String firstKey) {
    if (containsKey(firstKey)) {
      NBConceptLookupTable tempTable = get(firstKey);
      Enumeration<String> secondDimKeys = tempTable.keys();
      System.out.println("\nAssociated concepts for attribute " + firstKey + ":");
      while (secondDimKeys.hasMoreElements()) {
        System.out.println(secondDimKeys.nextElement());
      }
    } else {
      System.out.println("No second keys associated with " + firstKey);
    }
  }


  @Override
  public String toString() {
    NBConceptLookupTable curConceptTable;
    String curAttribute;
    String resString = "{";
    Enumeration<String> attributeKeys = keys();
    while (attributeKeys.hasMoreElements()) {
      curAttribute = attributeKeys.nextElement();
      curConceptTable = get(curAttribute);
      if (curConceptTable != null && curConceptTable.size() > 0) {
        resString += curAttribute + " => " + curConceptTable.toString() + ",\n";
      } else {
        resString += curAttribute + " =>  " + ",\n";
      }
    }
    if (resString.endsWith(",\n")) {
      resString = resString.substring(0, resString.length() - 2);
    }
    resString += "}";
    return resString;
  }


  public boolean vocabularySizeChanged() {
    return this.vocabularySizeChanged;
  }
}
