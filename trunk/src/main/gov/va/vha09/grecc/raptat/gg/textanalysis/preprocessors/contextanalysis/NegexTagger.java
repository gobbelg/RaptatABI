/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis;

import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeAssignmentMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UniqueIDGenerator;

/**
 * Created as a class to use tokens that have been assigned a context type to determine the
 * attributes of annotated phrases. It would be better if this were a type of
 * ContextAttributeAnalyzer so that it could be called by the annotated method of an Annotator
 * instance.
 *
 * @author Glenn T. Gobbel Mar 17, 2016
 */
public class NegexTagger {
  private static final Logger LOGGER = Logger.getLogger(NegexTagger.class);
  private UniqueIDGenerator idGenerator = UniqueIDGenerator.INSTANCE;


  public NegexTagger() {
    NegexTagger.LOGGER.setLevel(Level.INFO);
  }


  public void assignAttributes(List<AnnotatedPhrase> annotatedPhrases) {
    ContextType contextType = ContextType.NEGATION;

    for (AnnotatedPhrase curPhrase : annotatedPhrases) {
      RaptatAttribute attribute = null;
      String uniqueID = "RapTAT_Attribute_" + this.idGenerator.getUnique();

      for (RaptatToken curToken : curPhrase.getProcessedTokens()) {
        RaptatToken parentToken;
        if ((parentToken = curToken.getParentToken()) == null) {
          parentToken = curToken;
        }

        String assignmentValue;
        if ((assignmentValue = parentToken.getContextValue(contextType)) != null) {
          attribute = new RaptatAttribute(uniqueID, contextType.getTypeName(), assignmentValue,
              AttributeAssignmentMethod.CONTEXTUAL);
          break;
        }
      }
      if (attribute == null) {
        attribute = new RaptatAttribute(uniqueID, contextType.getTypeName(),
            contextType.getDefaultValue(), AttributeAssignmentMethod.CONTEXTUAL);
      }
      curPhrase.addAttribute(attribute);
    }
  }
}
