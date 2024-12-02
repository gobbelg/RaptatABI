package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;

/*
 * Class used to keep track of whether a reference annotation has been matched by a Raptat
 * annotation and vice versa.
 *
 * @author Glenn Gobbel - Jun 18, 2012 modified by Sanjib Saha - Aug 8, 2014
 */
class AnnotationScorerNode {
  AnnotatedPhrase annotatedPhrase = null;

  /*
   * partialMatch indicates some overlap with another annotation and matching concepts
   */
  boolean partialMatch = false;

  /*
   * exactMatch indicates all tokens of two annotations overlap exactly and their concepts match
   */
  boolean exactMatch = false;

  // Stores phrase specific matching information
  final HashMap<String, Boolean> theAttributes = new HashMap<>();

  boolean partialMatchIgnoringConcept = false;
  boolean exactMatchIgnoringConcept = false;

  /*
   * The annotation from a reference or raptat-generated set that matches this one either partially
   * or exactly in terms of token overlap
   */
  AnnotatedPhrase matchingAnnotation = null;


  AnnotationScorerNode(AnnotatedPhrase annotatedPhrase) {
    this.annotatedPhrase = annotatedPhrase;
  }


  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this, RaptatStringStyle.getInstance());
    tsb.append("AnnotatedPhrase", this.annotatedPhrase);
    tsb.append("\nAttributes");
    List<String> keys = new ArrayList<>(this.theAttributes.keySet());
    Collections.sort(keys);
    for (String key : this.theAttributes.keySet()) {
      tsb.append(key).append("Value", this.theAttributes.get(key));
    }
    tsb.append("\nPartialMatch", this.partialMatch);
    tsb.append("ExactMatch", this.exactMatch);
    tsb.append("PartialMatchIgnoringConcept", this.partialMatchIgnoringConcept);
    tsb.append("ExactMatchIgnoringConcept", this.exactMatchIgnoringConcept);
    tsb.append("MatchingAnnotation", this.matchingAnnotation);
    return tsb.toString();
  }


  /**
   * Sep 15, 2017
   *
   * @param other
   */
  void setMatchedAnnotation(AnnotationScorerNode other) {
    this.matchingAnnotation = other.annotatedPhrase;
    other.matchingAnnotation = this.annotatedPhrase;
  }
}
