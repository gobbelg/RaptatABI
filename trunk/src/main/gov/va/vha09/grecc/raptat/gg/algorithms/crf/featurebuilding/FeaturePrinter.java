/** */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding;

import java.util.Iterator;
import java.util.List;
import com.github.jcrfsuite.util.Pair;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;

/** @author Glenn T. Gobbel Apr 22, 2016 */
public class FeaturePrinter {
  /**
   * @param cueSentenceFeatures
   */
  public static void printAllFeatures(ItemSequence sentenceFeatures) {
    System.out.println("\n\n");
    for (int i = 0; i < sentenceFeatures.size(); i++) {
      StringBuilder sb = new StringBuilder();
      Item curToken = sentenceFeatures.get(i);
      for (int j = 0; j < curToken.size(); j++) {
        String curAttribute = curToken.get(j).getAttr();
        sb.append(curAttribute).append(" ");
      }
      System.out.println(sb.toString());
    }
  }


  /**
   * @param labels
   * @param tokens
   * @return
   */
  public static void printLabelsAndSentence(List<Pair<String, Double>> labels,
      ItemSequence tokens) {
    Iterator<Pair<String, Double>> labelIterator = labels.iterator();
    if (labelIterator.hasNext()) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < tokens.size(); i++) {
        String curLabel = labelIterator.next().first;
        sb.append(" ").append(curLabel).append(":");
        Item curToken = tokens.get(i);
        for (int j = 0; j < curToken.size(); j++) {
          String curAttribute = curToken.get(j).getAttr();
          if (curAttribute.startsWith("W=") && !curAttribute.contains("@")) {
            sb.append(curAttribute.substring(2)).append(" ");
          }
        }
      }
      System.out.println(sb.substring(1));
    }
  }
}
