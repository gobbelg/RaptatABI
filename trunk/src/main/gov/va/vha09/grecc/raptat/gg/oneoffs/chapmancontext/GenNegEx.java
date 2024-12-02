package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.chapmancontext;

/**
 * ************************************************************************************* Author:
 * Junebae Kye, Supervisor: Imre Solti Date: 06/30/2010
 *
 * <p>
 * Wendy Chapman's NegEx algorithm in Java.
 *
 * <p>
 * Generated sentence boundaries serve as WINDOW for negation (suggested by Wendy Chapman)
 *
 * <p>
 * NOTES: If the negation scope exists in a sentence, it will print the negation scope such as 1 -
 * 1, 0 - 24. If the negation does not exist in a sentence, it will print -1 If a pre-UMLS phrase is
 * used as a post-UMLS phrase, for example, pain and fever denied, it will print the negation scope
 * of, in this case, 0 - 2, for an option of yes or print -2 for an option of no
 *
 * <p>
 * **************************************************************************************
 */
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GenNegEx {
  private final List<String> pseNegPhrases; // list of pseudo-negation phrases
  private final List<String> negPhrases; // list of negation phrases
  private final List<String> postNegPhrases; // list of post-negation pharses
  private final List<String> conjunctions; // list of conjunctions
  private final boolean value; // boolean for an option of yes or no


  // post: constructs a GenNegEx object
  // creates a list of negation phrases, pseudo-negation phrases,
  // post-negation phrases, and
  // conjunction
  public GenNegEx(boolean value) {
    this.pseNegPhrases = new LinkedList<>();
    this.negPhrases = new LinkedList<>();
    this.postNegPhrases = new LinkedList<>();
    this.conjunctions = new LinkedList<>();
    processPhrases(this.pseNegPhrases, this.negPhrases, this.postNegPhrases, this.conjunctions);
    sorts(this.pseNegPhrases);
    sorts(this.negPhrases);
    sorts(this.postNegPhrases);
    sorts(this.conjunctions);
    this.value = value;
  }


  // post: returns a negation scope of an input sentence
  public String negScope(String line) {
    String[] s = line.split("\\s+");
    return helper(s, 0);
  }


  // post: returns index of negation phrase if any negation phrase is found in
  // a sentence
  // returns -1 if no negation phrase is found
  private int contains(String[] s, List<String> list, int index, int type) {
    int counts = 0;

    // Note that "token" (below) is an element in the list of negation and
    // pseudonegation phrases, not the sentence itself
    for (String token : list) {
      // Break the tested element into strings
      String[] element = token.split("\\s+");
      if (element.length == 1) {
        if (s[index].equals(element[0])) {
          return index + 1;
        }
      }
      // Check to make sure that s has more elements
      // beyond the index that are at least as long
      // as the size of the element being checked
      else if (s.length - index >= element.length) {
        String firstWord = s[index];
        if (firstWord.equals(element[0])) {
          counts++;
          for (int i = 1; i < element.length; i++) {
            if (s[index + i].equals(element[i])) {
              counts++;
            } else {
              counts = 0;
              break;
            }
            if (counts == element.length) {
              if (type == 0) {
                return index + i + 1;
              } else {
                return index;
              }
            }
          }
        }
      }
    }
    return -1;
  }


  // post: processes data and returns negation scope
  // returns -1 if no negation phrase is found
  private String helper(String[] s, int index) {
    // If index is beyond end of string, we can
    // just return -1 for not found
    if (index < s.length) {
      for (int i = index; i < s.length; i++) {
        // The last parameter in contains() corresponds to
        // type. The contains() method forces this helper method to skip
        // over all
        // tokens that precede any phrase within pseNegPhrases.
        int indexII = contains(s, this.pseNegPhrases, i, 0);
        if (indexII != -1) {
          return helper(s, indexII);
        } else {
          int indexIII = contains(s, this.negPhrases, i, 0);

          // Indicates we found one of the negPhrases and
          // indexIII will be set to the token just after
          // the found negPhrase
          if (indexIII != -1) {
            int indexIV = -1;
            for (int j = indexIII; j < s.length; j++) {
              indexIV = contains(s, this.conjunctions, j, 1);
              if (indexIV != -1) {
                break;
              }
            }
            // If there is a conjunction, we return the range
            // from the end of the negPhrase to the end of the
            // conjunction
            if (indexIV != -1) {
              return indexIII + " - " + indexIV;
            }
            // I'm not sure what this is doing. It seems
            // to be selecting everything if the negPhrase
            // extends to the end of the sentence. I'm
            // not sure why this would be the case.
            else if (indexIII > s.length - 1) {
              if (this.value) {
                return "0 - " + (indexIII - 2);
              } else {
                return "-2";
              }
            } else {
              return indexIII + " - " + (s.length - 1);
            }
          }
          // Now we look for a postNegPhrase, setting the contains()
          // parameter type to '1'.
          else {
            int indexV = contains(s, this.postNegPhrases, i, 1);
            if (indexV != -1) {
              return "0 - " + indexV;
            }
          }
        }
      }
    }
    return "-1";
  }


  // post: saves pseudo negation phrases, negation phrases, conjunctions into
  // the database
  private void processPhrases(List<String> pseNegPhrases, List<String> negPhrases,
      List<String> postNegPhrases, List<String> conjunctions) {
    pseNegPhrases.add("no increase");
    pseNegPhrases.add("no change");
    pseNegPhrases.add("no suspicious change");
    pseNegPhrases.add("no significant change");
    pseNegPhrases.add("no interval change");
    pseNegPhrases.add("no definite change");
    pseNegPhrases.add("not extend");
    pseNegPhrases.add("not cause");
    pseNegPhrases.add("not drain");
    pseNegPhrases.add("not significant interval change");
    pseNegPhrases.add("not certain if");
    pseNegPhrases.add("not certain whether");
    pseNegPhrases.add("gram negative");
    pseNegPhrases.add("without difficulty");
    pseNegPhrases.add("not necessarily");
    pseNegPhrases.add("not only");

    negPhrases.add("absence of");
    negPhrases.add("cannot see");
    negPhrases.add("cannot");
    negPhrases.add("checked for");
    negPhrases.add("declined");
    negPhrases.add("declines");
    negPhrases.add("denied");
    negPhrases.add("denies");
    negPhrases.add("denying");
    negPhrases.add("evaluate for");
    negPhrases.add("fails to reveal");
    negPhrases.add("free of");
    negPhrases.add("negative for");
    negPhrases.add("never developed");
    negPhrases.add("never had");
    negPhrases.add("no");
    negPhrases.add("no abnormal");
    negPhrases.add("no cause of");
    negPhrases.add("no complaints of");
    negPhrases.add("no evidence");
    negPhrases.add("no new evidence");
    negPhrases.add("no other evidence");
    negPhrases.add("no evidence to suggest");
    negPhrases.add("no findings of");
    negPhrases.add("no findings to indicate");
    negPhrases.add("no mammographic evidence of");
    negPhrases.add("no new");
    negPhrases.add("no radiographic evidence of");
    negPhrases.add("no sign of");
    negPhrases.add("no significant");
    negPhrases.add("no signs of");
    negPhrases.add("no suggestion of");
    negPhrases.add("no suspicious");
    negPhrases.add("not");
    negPhrases.add("not appear");
    negPhrases.add("not appreciate");
    negPhrases.add("not associated with");
    negPhrases.add("not complain of");
    negPhrases.add("not demonstrate");
    negPhrases.add("not exhibit");
    negPhrases.add("not feel");
    negPhrases.add("not had");
    negPhrases.add("not have");
    negPhrases.add("not know of");
    negPhrases.add("not known to have");
    negPhrases.add("not reveal");
    negPhrases.add("not see");
    negPhrases.add("not to be");
    negPhrases.add("patient was not");
    negPhrases.add("rather than");
    negPhrases.add("resolved");
    negPhrases.add("test for");
    negPhrases.add("to exclude");
    negPhrases.add("unremarkable for");
    negPhrases.add("with no");
    negPhrases.add("without any evidence of");
    negPhrases.add("without evidence");
    negPhrases.add("without indication of");
    negPhrases.add("without sign of");
    negPhrases.add("without");
    negPhrases.add("rule out for");
    negPhrases.add("rule him out for");
    negPhrases.add("rule her out for");
    negPhrases.add("rule the patient out for");
    negPhrases.add("rule him out");
    negPhrases.add("rule her out");
    negPhrases.add("rule out");
    negPhrases.add("r/o");
    negPhrases.add("ro");
    negPhrases.add("rule the patient out");
    negPhrases.add("rules out");
    negPhrases.add("rules him out");
    negPhrases.add("rules her out");
    negPhrases.add("ruled the patient out for");
    negPhrases.add("rules the patient out");
    negPhrases.add("ruled him out against");
    negPhrases.add("ruled her out against");
    negPhrases.add("ruled him out");
    negPhrases.add("ruled her out");
    negPhrases.add("ruled out against");
    negPhrases.add("ruled the patient out against");
    negPhrases.add("did rule out for");
    negPhrases.add("did rule out against");
    negPhrases.add("did rule out");
    negPhrases.add("did rule him out for");
    negPhrases.add("did rule him out against");
    negPhrases.add("did rule him out");
    negPhrases.add("did rule her out for");
    negPhrases.add("did rule her out against");
    negPhrases.add("did rule her out");
    negPhrases.add("did rule the patient out against");
    negPhrases.add("did rule the patient out for");
    negPhrases.add("did rule the patient out");
    negPhrases.add("can rule out for");
    negPhrases.add("can rule out against");
    negPhrases.add("can rule out");
    negPhrases.add("can rule him out for");
    negPhrases.add("can rule him out against");
    negPhrases.add("can rule him out");
    negPhrases.add("can rule her out for");
    negPhrases.add("can rule her out against");
    negPhrases.add("can rule her out");
    negPhrases.add("can rule the patient out for");
    negPhrases.add("can rule the patient out against");
    negPhrases.add("can rule the patient out");
    negPhrases.add("adequate to rule out for");
    negPhrases.add("adequate to rule out");
    negPhrases.add("adequate to rule him out for");
    negPhrases.add("adequate to rule him out");
    negPhrases.add("adequate to rule her out for");
    negPhrases.add("adequate to rule her out");
    negPhrases.add("adequate to rule the patient out for");
    negPhrases.add("adequate to rule the patient out against");
    negPhrases.add("adequate to rule the patient out");
    negPhrases.add("sufficient to rule out for");
    negPhrases.add("sufficient to rule out against");
    negPhrases.add("sufficient to rule out");
    negPhrases.add("sufficient to rule him out for");
    negPhrases.add("sufficient to rule him out against");
    negPhrases.add("sufficient to rule him out");
    negPhrases.add("sufficient to rule her out for");
    negPhrases.add("sufficient to rule her out against");
    negPhrases.add("sufficient to rule her out");
    negPhrases.add("sufficient to rule the patient out for");
    negPhrases.add("sufficient to rule the patient out against");
    negPhrases.add("sufficient to rule the patient out");
    negPhrases.add("what must be ruled out is");

    postNegPhrases.add("should be ruled out for");
    postNegPhrases.add("ought to be ruled out for");
    postNegPhrases.add("may be ruled out for");
    postNegPhrases.add("might be ruled out for");
    postNegPhrases.add("could be ruled out for");
    postNegPhrases.add("will be ruled out for");
    postNegPhrases.add("can be ruled out for");
    postNegPhrases.add("must be ruled out for");
    postNegPhrases.add("is to be ruled out for");
    postNegPhrases.add("be ruled out for");
    postNegPhrases.add("unlikely");
    postNegPhrases.add("free");
    postNegPhrases.add("was ruled out");
    postNegPhrases.add("is ruled out");
    postNegPhrases.add("are ruled out");
    postNegPhrases.add("have been ruled out");
    postNegPhrases.add("has been ruled out");
    postNegPhrases.add("being ruled out");
    postNegPhrases.add("should be ruled out");
    postNegPhrases.add("ought to be ruled out");
    postNegPhrases.add("may be ruled out");
    postNegPhrases.add("might be ruled out");
    postNegPhrases.add("could be ruled out");
    postNegPhrases.add("will be ruled out");
    postNegPhrases.add("can be ruled out");
    postNegPhrases.add("must be ruled out");
    postNegPhrases.add("is to be ruled out");
    postNegPhrases.add("be ruled out");

    conjunctions.add("but");
    conjunctions.add("however");
    conjunctions.add("nevertheless");
    conjunctions.add("yet");
    conjunctions.add("though");
    conjunctions.add("although");
    conjunctions.add("still");
    conjunctions.add("aside from");
    conjunctions.add("except");
    conjunctions.add("apart from");
    conjunctions.add("secondary to");
    conjunctions.add("as the cause of");
    conjunctions.add("as the source of");
    conjunctions.add("as the reason of");
    conjunctions.add("as the etiology of");
    conjunctions.add("as the origin of");
    conjunctions.add("as the cause for");
    conjunctions.add("as the source for");
    conjunctions.add("as the reason for");
    conjunctions.add("as the etiology for");
    conjunctions.add("as the origin for");
    conjunctions.add("as the secondary cause of");
    conjunctions.add("as the secondary source of");
    conjunctions.add("as the secondary reason of");
    conjunctions.add("as the secondary etiology of");
    conjunctions.add("as the secondary origin of");
    conjunctions.add("as the secondary cause for");
    conjunctions.add("as the secondary source for");
    conjunctions.add("as the secondary reason for");
    conjunctions.add("as the secondary etiology for");
    conjunctions.add("as the secondary origin for");
    conjunctions.add("as a cause of");
    conjunctions.add("as a source of");
    conjunctions.add("as a reason of");
    conjunctions.add("as a etiology of");
    conjunctions.add("as a cause for");
    conjunctions.add("as a source for");
    conjunctions.add("as a reason for");
    conjunctions.add("as a etiology for");
    conjunctions.add("as a secondary cause of");
    conjunctions.add("as a secondary source of");
    conjunctions.add("as a secondary reason of");
    conjunctions.add("as a secondary etiology of");
    conjunctions.add("as a secondary origin of");
    conjunctions.add("as a secondary cause for");
    conjunctions.add("as a secondary source for");
    conjunctions.add("as a secondary reason for");
    conjunctions.add("as a secondary etiology for");
    conjunctions.add("as a secondary origin for");
    conjunctions.add("cause of");
    conjunctions.add("cause for");
    conjunctions.add("causes of");
    conjunctions.add("causes for");
    conjunctions.add("source of");
    conjunctions.add("source for");
    conjunctions.add("sources of");
    conjunctions.add("sources for");
    conjunctions.add("reason of");
    conjunctions.add("reason for");
    conjunctions.add("reasons of");
    conjunctions.add("reasons for");
    conjunctions.add("etiology of");
    conjunctions.add("etiology for");
    conjunctions.add("trigger event for");
    conjunctions.add("origin of");
    conjunctions.add("origin for");
    conjunctions.add("origins of");
    conjunctions.add("origins for");
    conjunctions.add("other possibilities of");
  }


  // post: sorts a list in descending order
  private void sorts(List<String> list) {
    Collections.sort(list);
    Collections.reverse(list);
  }
}
