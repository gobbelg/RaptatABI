package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.io.Serializable;
import java.util.Hashtable;

public class AnnotationConditionalTable extends Hashtable<String, AnnotationProbabilityList>
    implements Serializable {
  private static final long serialVersionUID = -7056784662887772064L;


  public AnnotationConditionalTable(int startCapacity) {
    super(startCapacity);
  }

  // TODO Remove unused code found by UCDetector
  // /******************************************************************
  // * Returns the OccurProbTriplet associated with the input tokenStringKey
  // * that determines how many times it has occurred in unannotated form
  // * and the likelihood that it is not part of an annotated phrase.
  // ******************************************************************/
  // public OccurProbTriplet getNotAnnotatedValue(String tokenStringKey)
  // {
  // return get(tokenStringKey).get(0);
  // }

  // TODO Remove unused code found by UCDetector
  // /******************************************************************
  // * Returns the OccurProbTriplet associated with the input tokenStringKey
  // * that determines how many times it has occurred in annotated form
  // * and the likelihood that it is not part of an annotated phrase.
  // ******************************************************************/
  // public OccurProbTriplet getAnnotatedValue(String tokenStringKey)
  // {
  // return get(tokenStringKey).get(1);
  // }

  // TODO Remove unused code found by UCDetector
  // /****************************************************************
  // * Increment the number of times the string associated with a
  // * given token **was not** part of an annotated phrase
  // *
  // ****************************************************************/
  // public void incrementNotAnnotated(String tokenStringKey)
  // {
  // if (containsKey(tokenStringKey))
  // {
  // get(tokenStringKey).get(0).occurrences++;
  // }
  // else
  // {
  // // If neither first nor second key exists, create a new One-way
  // // likelihood table with firstKey returning the table
  // put(tokenStringKey, new AnnotationProbabilityList());
  //
  // toString();
  //
  // // Now set the value to 1 as it's the first time this
  // // firstKey, secondKey combination has been seen.
  // get(tokenStringKey).set( 0, new OccurProbTriplet(1, 0.0, 0.0) );
  // }
  // }

  // TODO Remove unused code found by UCDetector
  // /****************************************************************
  // * Increment the number of times the string associated with a
  // * given token **was** part of an annotated phrase
  // *
  // ****************************************************************/
  // public void incrementAnnotated(String tokenStringKey)
  // {
  // if (containsKey(tokenStringKey))
  // {
  // get(tokenStringKey).get(1).occurrences++;
  // }
  // else
  // {
  // // If neither first nor second key exists, create a new One-way
  // // likelihood table with firstKey returning the table
  // put(tokenStringKey, new AnnotationProbabilityList());
  //
  // toString();
  //
  // // Now set the value to 1 as it's the first time this
  // // firstKey, secondKey combination has been seen.
  // get(tokenStringKey).set( 1, new OccurProbTriplet(1, 0.0, 0.0) );
  // }
  // }
}
