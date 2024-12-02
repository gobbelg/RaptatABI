package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

public class AssertionAnalyzer implements ContextAttributeAnalyzer {
  /** */
  private static final long serialVersionUID = 5376139675664307750L;

  private static final String regexString =
      "\\w+(_" + RaptatConstants.NEGATION_TAG + "|_" + RaptatConstants.NEGATION_TAG + "_\\w+)";
  private static final String REGEX_MATCH_VALUE = "negative";
  private static final String DEFAULT_VALUE = "positive";
  private static final String analyzerName = "assertion";


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

    return true;
  }


  @Override
  public String getAttributeName() {
    return AssertionAnalyzer.analyzerName;
  }


  @Override
  public RaptatAttribute getPhraseAttribute(List<RaptatToken> phraseTokenSequence) {
    String uniqueID = "RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique();
    for (RaptatToken curToken : phraseTokenSequence) {
      if (curToken.getTokenStringTagged().matches(AssertionAnalyzer.regexString)) {
        List<String> attributeValues =
            Arrays.asList(new String[] {AssertionAnalyzer.REGEX_MATCH_VALUE});

        return new RaptatAttribute(uniqueID, AssertionAnalyzer.analyzerName, attributeValues,
            AttributeAssignmentMethod.CONTEXTUAL);
      }
    }
    List<String> attributeValues = Arrays.asList(new String[] {AssertionAnalyzer.DEFAULT_VALUE});

    return new RaptatAttribute(uniqueID, AssertionAnalyzer.analyzerName, attributeValues,
        AttributeAssignmentMethod.CONTEXTUAL);
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(AssertionAnalyzer.analyzerName)
        .append(AssertionAnalyzer.regexString).append(AssertionAnalyzer.DEFAULT_VALUE)
        .append(AssertionAnalyzer.REGEX_MATCH_VALUE).toHashCode();
  }
}
