package src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;

public class PhraseIDBootstrapSampler {
  protected ArrayList<String> theGroups;
  protected Hashtable<String, List<String[]>> subGroups;
  int sampleSize;
  private Random generator;


  /**
   * ********************************************
   *
   * @param seed - For creating a random number generator.
   * @author Glenn Gobbel - Jun 19, 2012 ********************************************
   */
  public PhraseIDBootstrapSampler(long seed) {
    // This is a list of the groups to be sampled with
    // 1 entry per group. The entries in this list
    // can be used as hashes to pull out xml and
    // text files from xmlGroups and textGroups fields/
    this.theGroups = new ArrayList<>();

    // Each group is stored here as a hash that returns
    // a set of elements within the group, namely
    // documents and xml files
    this.subGroups = new Hashtable<>();

    // Random number generator for creating bootstrap
    // samples
    this.generator = new Random(seed);
  }


  /**
   * ******************************************** Default, no argument constructor created only for
   * use by subclasses
   *
   * @author Glenn Gobbel - Jul 23, 2012 ********************************************
   */
  protected PhraseIDBootstrapSampler() {
    // This is a list of the groups to be sampled with
    // 1 entry per group. The entries in this list
    // can be used as hashes to pull out xml and
    // text files from xmlGroups and textGroups fields/
    this.theGroups = new ArrayList<>();

    // Each group is stored here as a hash that returns
    // a set of elements within the group, namely
    // documents and xml files
    this.subGroups = new Hashtable<>();
  }


  /**
   * *********************************************************** Create a hash in which the first
   * element hashes to the a string array containing full paths to the text document and the xml
   * file
   *
   * @param theElements
   * @author Glenn Gobbel - Jun 17, 2012 ***********************************************************
   */
  public void add(String[] theElements) {
    String groupingElement = theElements[0];
    if (!this.subGroups.containsKey(groupingElement)) {
      List<String[]> subGroupList = new ArrayList<>();
      this.subGroups.put(groupingElement, subGroupList);
      this.theGroups.add(groupingElement);
    }
    this.subGroups.get(groupingElement).add(GeneralHelper.getSubArray(theElements, 1));
  }


  public List<String[]> getBootstrapSample() {
    ArrayList<String[]> results = new ArrayList<>();
    int groups = this.theGroups.size();
    for (int i = 0; i < groups; i++) {
      // Add all elements mapped to by the element chosen
      // by the generator to the results list
      results.addAll(this.subGroups.get(String.valueOf(this.generator.nextInt(groups))));
    }
    return results;
  }


  public List<String[]> getEntireSample() {
    ArrayList<String[]> results = new ArrayList<>();
    for (String curKey : this.theGroups) {
      results.addAll(this.subGroups.get(curKey));
    }
    return results;
  }
}
