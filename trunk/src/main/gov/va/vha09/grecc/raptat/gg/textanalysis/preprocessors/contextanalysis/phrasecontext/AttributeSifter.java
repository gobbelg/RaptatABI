/** */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;

/**
 * An AttributeSifter instance, given a list of AnnotatedPhrase instances, removes any phrase that
 * do not have the proper attributes from that list. This was refactored on 02/11/18 to correct a
 * significant error that may have failed to properly filter out phrases.
 *
 * @author vhatvhgobbeg - Jun 11, 2013
 */
public class AttributeSifter {
  /*
   * Maps from the name of an attribute to the values associated with that attribute
   */
  HashMap<String, List<String>> requiredAttributes;
  private boolean requireValueMatch;


  public AttributeSifter() {
    this.requiredAttributes = new HashMap<>();
  }


  public AttributeSifter(List<RaptatAttribute> theAttributes) {
    /*
     * Make requiredAttributes a bit larger than required in case we need to add other attributes
     */
    this.requiredAttributes = new HashMap<>(theAttributes.size() * 2);

    for (RaptatAttribute curAttribute : theAttributes) {
      this.requiredAttributes.put(curAttribute.getName(),
          new ArrayList<>(curAttribute.getValues()));
    }
  }


  public AttributeSifter(List<RaptatAttribute> theAttributes, boolean requireValueMatch) {
    this(theAttributes);
    this.requireValueMatch = requireValueMatch;
  }


  public void addAttribute(RaptatAttribute inputAttribute) {
    this.requiredAttributes.put(inputAttribute.getName(),
        new ArrayList<>(inputAttribute.getValues()));
  }


  /*
   * Sifts using the requireValueMatch field settting (set to true or false)
   */
  public void sift(List<AnnotatedPhrase> thePhrases) {
    this.sift(thePhrases, this.requireValueMatch);
  }


  /*
   * Method allows sifting with an alternative setting of requireValueMatch from that of of the
   * requireValueMatch field of this instance of AttributeSifter
   */
  public void sift(List<AnnotatedPhrase> thePhrases, boolean requireValueMatch) {
    Iterator<AnnotatedPhrase> phraseIterator = thePhrases.iterator();
    while (phraseIterator.hasNext()) {
      List<RaptatAttribute> phraseAttributeList = phraseIterator.next().getPhraseAttributes();
      Set<String> phraseAttributeNames = RaptatAttribute.getNameSet(phraseAttributeList);

      /*
       * Check that every attribute in the requiredAttributes field is in the set of attributes
       * corresponding the current AnnotatedPhrase instance
       */
      search: for (String requiredAttributeName : this.requiredAttributes.keySet()) {
        if (!phraseAttributeNames.contains(requiredAttributeName)) {
          phraseIterator.remove();
          break search;
        } else {
          if (requireValueMatch) {
            List<String> requiredValues = this.requiredAttributes.get(requiredAttributeName);

            /*
             * Check to make sure that every requireValue for the current requiredAttributeName is
             * contained in the values of the corresponding attribute for the current
             * AnnotatedPhrase instance
             */
            for (String requiredValue : requiredValues) {
              for (RaptatAttribute phraseAttribute : phraseAttributeList) {
                Set<String> attributeValueSet = new HashSet<>(phraseAttribute.getValues());
                if (!attributeValueSet.contains(requiredValue)) {
                  phraseIterator.remove();
                  break search;
                }
              }
            }
          }
        }
      }
    }
  }
}
