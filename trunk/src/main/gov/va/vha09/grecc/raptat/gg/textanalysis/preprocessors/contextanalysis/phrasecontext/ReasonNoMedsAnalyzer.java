/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

/*
 *
 *
 * @author vhatvhgobbeg - Jun 11, 2013
 */
public class ReasonNoMedsAnalyzer implements ContextAttributeAnalyzer {
  /** */
  private static final long serialVersionUID = -4560501986524887135L;

  private static final String regexString = "\\w+(_" + RaptatConstants.REASON_NO_MEDS_TAG + "|_"
      + RaptatConstants.REASON_NO_MEDS_TAG + "_\\w+)";
  private static final String REGEX_MATCH_VALUE = "overt";
  private static final String DEFAULT_VALUE = "nonovert";
  private static final String analyzerName = "reasonnotonmeds";


  @Override
  public String getAttributeName() {
    return ReasonNoMedsAnalyzer.analyzerName;
  }


  @Override
  public RaptatAttribute getPhraseAttribute(List<RaptatToken> phraseTokenSequence) {
    String uniqueID = "RapTAT_Attribute_" + UniqueIDGenerator.INSTANCE.getUnique();
    for (RaptatToken curToken : phraseTokenSequence) {
      if (curToken.getTokenStringTagged().matches(ReasonNoMedsAnalyzer.regexString)) {
        List<String> attributeValues =
            Arrays.asList(new String[] {ReasonNoMedsAnalyzer.REGEX_MATCH_VALUE});

        return new RaptatAttribute(uniqueID, ReasonNoMedsAnalyzer.analyzerName, attributeValues,
            AttributeAssignmentMethod.CONTEXTUAL);
      }
    }
    List<String> attributeValues = Arrays.asList(new String[] {ReasonNoMedsAnalyzer.DEFAULT_VALUE});

    return new RaptatAttribute(uniqueID, ReasonNoMedsAnalyzer.analyzerName, attributeValues,
        AttributeAssignmentMethod.CONTEXTUAL);
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(ReasonNoMedsAnalyzer.analyzerName)
        .append(ReasonNoMedsAnalyzer.regexString).append(ReasonNoMedsAnalyzer.DEFAULT_VALUE)
        .append(ReasonNoMedsAnalyzer.REGEX_MATCH_VALUE).toHashCode();
  }
}
