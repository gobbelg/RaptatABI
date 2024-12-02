package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * Takes AnnotatedPhrase instances and uses them to set the context of surrounding sentences.
 *
 * @author glenn
 */
class SentenceContextChecker implements ContextChecker, Serializable {

  /** */
  private static final long serialVersionUID = -2970076423752117486L;

  private static final String NOT_STRING = "_NOT";

  private static Evaluator evaluator = new Evaluator();


  private final String contextDescriptor;

  private final RaptatPair<Integer, Integer> requiredDistancesBackwardAndForward;
  /*
   * Note that this is only applied to concepts that have a ! ('not') operator directly in front of
   * them in the contextDescriptor string
   */
  private final RaptatPair<Integer, Integer> forbiddenDistancesBackwardAndForward;
  private final String evaluatorString;
  private final Map<String, Integer> forbiddenConcepts = new HashMap<>();
  private final Map<String, Integer> requiredConcepts = new HashMap<>();

  SentenceContextChecker(String contextDescriptor,
      RaptatPair<Integer, Integer> requiredDistancesBackwardAndForward,
      RaptatPair<Integer, Integer> forbiddenDistancesBackwardAndForward) {
    this.contextDescriptor = contextDescriptor.replaceAll(" ", "").toLowerCase();
    this.evaluatorString = initializeEvaluatorString(this.contextDescriptor);
    this.requiredDistancesBackwardAndForward = requiredDistancesBackwardAndForward;
    this.forbiddenDistancesBackwardAndForward = forbiddenDistancesBackwardAndForward;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    SentenceContextChecker other = (SentenceContextChecker) obj;

    return new EqualsBuilder().append(this.contextDescriptor, other.contextDescriptor)
        .append(this.requiredDistancesBackwardAndForward, other.requiredDistancesBackwardAndForward)
        .append(this.forbiddenDistancesBackwardAndForward,
            other.forbiddenDistancesBackwardAndForward)
        .isEquals();
  }


  /** @return the evaluator */
  public Evaluator getEvaluator() {
    return SentenceContextChecker.evaluator;
  }


  /** @return the evaluatorString */
  public String getEvaluatorString() {
    return this.evaluatorString;
  }


  /** @return the forbiddenConcepts */
  public Map<String, Integer> getForbiddenConcepts() {
    return this.forbiddenConcepts;
  }


  /** @return the forbiddenDistancesBackwardAndForward */
  public RaptatPair<Integer, Integer> getForbiddenDistancesBackwardAndForward() {
    return this.forbiddenDistancesBackwardAndForward;
  }


  /** @return the requiredConcepts */
  public Map<String, Integer> getRequiredConcepts() {
    return this.requiredConcepts;
  }


  /** @return the requiredDistancesBackwardAndForward */
  public RaptatPair<Integer, Integer> getRequiredDistancesBackwardAndForward() {
    return this.requiredDistancesBackwardAndForward;
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.contextDescriptor)
        .append(this.requiredDistancesBackwardAndForward)
        .append(this.forbiddenDistancesBackwardAndForward).toHashCode();
  }


  @Override
  public boolean isContextCompliant(AnnotatedPhrase annotatedPhrase) {
    String resultString = null;

    AnnotatedPhrase sentence = annotatedPhrase.getAssociatedSentence();
    this.setContext(sentence, this.forbiddenConcepts,
        this.forbiddenDistancesBackwardAndForward.left,
        this.forbiddenDistancesBackwardAndForward.right);
    this.setContext(sentence, this.requiredConcepts, this.requiredDistancesBackwardAndForward.left,
        this.requiredDistancesBackwardAndForward.right);

    /*
     * Add underline character to concept names in case they begin with a number, which is not
     * allowed by jeval code
     */
    for (String concept : this.forbiddenConcepts.keySet()) {
      SentenceContextChecker.evaluator.putVariable(
          "_" + concept + SentenceContextChecker.NOT_STRING,
          this.forbiddenConcepts.get(concept).toString());
    }

    for (String concept : this.requiredConcepts.keySet()) {
      SentenceContextChecker.evaluator.putVariable("_" + concept,
          this.requiredConcepts.get(concept).toString());
    }

    try {
      resultString = SentenceContextChecker.evaluator.evaluate(this.evaluatorString);
    } catch (EvaluationException e) {
      e.printStackTrace();
    }

    resetContext();
    return resultString.equals("1.0");
  }


  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this).append(this.evaluatorString)
        .append(this.forbiddenConcepts).append(this.forbiddenDistancesBackwardAndForward)
        .append(this.requiredConcepts).append(this.requiredDistancesBackwardAndForward);
    return tsb.toString();
  }


  /**
   * Nov 20, 2017
   *
   * <p>
   * Takes a string describing the context and extracts the concepts included in the descriptor
   *
   * @param contextDescriptor
   * @return
   */
  private String initializeEvaluatorString(String contextDescriptor) {
    String contextConceptString = contextDescriptor.replaceAll("[&()|!]", " ");
    contextConceptString = contextConceptString.trim();
    String[] contextConcepts = contextConceptString.split("\\s+");

    HashSet<String> conceptSet = new HashSet<>(Arrays.asList(contextConcepts));
    String descriptor = this.contextDescriptor;

    /*
     * We have to distinguish 'forbidden' (start with !) from required (no !)W2 because the user can
     * specify different distances of contextual impact of concepts.
     *
     * Note that we prepend an underline character to all concept names in case they begin with a
     * number, which is not allowed by jeval code
     */
    for (String concept : conceptSet) {
      if (contextDescriptor.contains("!" + concept)) {
        this.forbiddenConcepts.put(concept, 0);
        descriptor = descriptor.replaceAll(concept,
            "#{_" + concept + SentenceContextChecker.NOT_STRING + "}");
      }

      Pattern regex = Pattern.compile("[^!]" + concept + "|^" + concept);
      Matcher regexMatcher = regex.matcher(contextDescriptor);
      if (regexMatcher.find()) {
        this.requiredConcepts.put(concept, 0);
        descriptor = descriptor.replaceAll(concept, "#{_" + concept + "}");
      }
    }
    return descriptor;
  }


  private void resetContext() {
    for (String concept : this.forbiddenConcepts.keySet()) {
      this.forbiddenConcepts.put(concept, 0);
    }

    for (String concept : this.requiredConcepts.keySet()) {
      this.requiredConcepts.put(concept, 0);
    }
  }


  /**
   * Nov 21, 2017
   *
   * @param sentence
   * @param contextualConcepts
   */
  private void setContext(AnnotatedPhrase sentence, Map<String, Integer> contextualConcepts) {
    for (String sentenceConcept : sentence.getSentenceAssociatedConcepts()) {
      if (contextualConcepts.containsKey(sentenceConcept)) {
        contextualConcepts.put(sentenceConcept, 1);
      }
    }
  }


  private void setContext(AnnotatedPhrase sentence, Map<String, Integer> contextualConcepts,
      int distanceBackward, int distanceForward) {
    AnnotatedPhrase surroundingSentence = sentence;

    for (int i = 0; surroundingSentence != null && i <= distanceBackward; i++) {
      this.setContext(surroundingSentence, contextualConcepts);
      surroundingSentence = surroundingSentence.getPreviousSentence();
    }

    surroundingSentence = sentence.getNextSentence();
    for (int i = 0; surroundingSentence != null && i < distanceForward; i++) {
      this.setContext(surroundingSentence, contextualConcepts);
      surroundingSentence = surroundingSentence.getNextSentence();
    }
  }


  public static void main(String[] args) {
    String contextDescriptor;
    RaptatPair<Integer, Integer> requiredDistances;
    RaptatPair<Integer, Integer> forbiddenDistances;

    contextDescriptor = "!(A && B)  || !C";
    requiredDistances = new RaptatPair<>(1, 1);
    forbiddenDistances = new RaptatPair<>(0, 0);

    SentenceContextChecker cc =
        new SentenceContextChecker(contextDescriptor, requiredDistances, forbiddenDistances);

    Evaluator evaluator = cc.getEvaluator();
    evaluator.putVariable("_A", "1");
    evaluator.putVariable("_B", "1");
    evaluator.putVariable("_C" + SentenceContextChecker.NOT_STRING, "0");

    try {
      System.out.println("EvaluatorString = " + cc.getEvaluatorString());
      String result = cc.getEvaluator().evaluate(cc.getEvaluatorString());
      System.out.println(result.equals("1.0"));
    } catch (EvaluationException e) {
      e.printStackTrace();
    }
  }
}
