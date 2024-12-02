package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;

public class SimpleHashTree extends HashTree<SimpleHashTree> {
  private static final long serialVersionUID = -1792270622459590644L;
  /* Store a quick way to get all parents and the children of each parent */


  /*
   * Store a quick way to look up whether a parent is also a child - if not, it is a direct
   * descendant of root
   */
  HashSet<String> allChildren;


  public SimpleHashTree() {
    super();
  }

  public SimpleHashTree(int i) {
    super(i);
  }


  public void print() {
    Enumeration<String> keyEnumerator = keys();
    System.out.println();

    while (keyEnumerator.hasMoreElements()) {
      String curKey = keyEnumerator.nextElement();
      this.print(curKey, get(curKey), 0);
    }
  }


  public List<List<String>> toList() {
    Enumeration<String> keyEnumerator = keys();
    List<List<String>> resultList = new ArrayList<>(size());

    while (keyEnumerator.hasMoreElements()) {
      String curKey = keyEnumerator.nextElement();
      SimpleHashTree nextTree = get(curKey);
      List<List<String>> startKeyList = new ArrayList<>();
      startKeyList.add(Arrays.asList(new String[] {curKey}));
      List<List<String>> elementList = nextTree.toList(startKeyList);
      resultList.addAll(elementList);
    }

    return resultList;
  }


  /**
   * Method Description - Helper method for parentChildListToHashTree(). This method has a
   * precondition that the parentKey must exist as a key within the parentChildHash HashMap field.
   *
   * @param parentKey
   * @param treeSize
   * @return
   * @author Glenn Gobbel - Feb 21, 2015
   * @param parentChildHash
   */
  private SimpleHashTree createChildTree(String parentKey,
      HashMap<String, HashSet<String>> parentChildHash, int treeSize) {
    treeSize = treeSize > 19 ? treeSize / 2 : treeSize;
    SimpleHashTree childTree = new SimpleHashTree(treeSize);
    put(parentKey, childTree);
    for (String curChild : parentChildHash.get(parentKey)) {
      SimpleHashTree children;
      /*
       * If the child is also a key in the parentChildHash, then it is also a parent, and we need to
       * add it's children to the tree
       */
      if (parentChildHash.containsKey(curChild)) {
        children = childTree.createChildTree(curChild, parentChildHash, treeSize);
      } else {
        children = new SimpleHashTree(0);
      }

      childTree.put(curChild, children);
    }

    return childTree;
  }


  private void print(String aKey, SimpleHashTree shTree, int numTabs) {
    for (int i = 0; i < numTabs; i++) {
      System.out.print("   ");
    }
    System.out.println(aKey);
    List<String> keys = new ArrayList<>(shTree.keySet());
    Collections.sort(keys);

    for (String curKey : keys) {
      this.print(curKey, shTree.get(curKey), numTabs + 1);
    }
  }


  private List<List<String>> toList(List<List<String>> stringLists) {
    List<List<String>> resultLists = new ArrayList<>(size());
    for (List<String> innerList : stringLists) {
      if (isEmpty()) {
        resultLists.add(innerList);
        continue;
      }

      Enumeration<String> keyEnumerator = keys();
      List<List<String>> subTreeLists = new ArrayList<>(size());

      while (keyEnumerator.hasMoreElements()) {
        /* Recursively build lists here by supplying a */
        String key = keyEnumerator.nextElement();
        SimpleHashTree nextTree = get(key);
        List<List<String>> startKeyList = new ArrayList<>();
        startKeyList.add(Arrays.asList(new String[] {key}));
        List<List<String>> elementList = nextTree.toList(startKeyList);
        subTreeLists.addAll(elementList);
      }

      for (List<String> subList : subTreeLists) {
        ArrayList<String> prependList = new ArrayList<>(innerList);
        prependList.addAll(subList);
        resultLists.add(prependList);
      }
    }

    return resultLists;
  }


  public static void main(String[] args) {
    String[][] testRels = {{"A", "1"}, {"B", "2"}, {"B", "3"}, {"1", "I"}, {"1", "II"},
        {"1", "III"}, {"3", "IV"}, {"3", "V"}, {"5", "ABCDE"}};

    List<RaptatPair<String, String>> allPairs = new ArrayList<>(testRels.length);
    for (String[] curArrayPair : testRels) {
      allPairs.add(new RaptatPair(curArrayPair[0], curArrayPair[1]));
    }
    SimpleHashTree resultTree = SimpleHashTree.parentChildListToHashTree(allPairs);
    resultTree.print();
    System.out.println("\n================================\n  SimpleHashTree "
        + "test complete\n================================");
  }


  /**
   * Method Description - Takes a list of pairs where the parent is on the left and the child is on
   * the right and builds a tree.
   *
   * @param elements
   * @return
   * @author Glenn Gobbel - Feb 21, 2015
   */
  public static SimpleHashTree parentChildListToHashTree(
      List<RaptatPair<String, String>> elements) {
    HashMap<String, HashSet<String>> parentChildHash;
    int treeSize = elements.size();
    if (treeSize < 10) {
      treeSize = 10;
    }

    SimpleHashTree rootTree = new SimpleHashTree(1);

    parentChildHash = new HashMap<>(treeSize);

    rootTree.allChildren = new HashSet<>(elements.size());

    for (RaptatPair<String, String> curPair : elements) {
      HashSet<String> curChildren;
      if ((curChildren = parentChildHash.get(curPair.left)) == null) {
        curChildren = new HashSet<>(treeSize);
        parentChildHash.put(curPair.left, curChildren);
      }
      curChildren.add(curPair.right);
      rootTree.allChildren.add(curPair.right);
    }

    SimpleHashTree childTree = new SimpleHashTree(treeSize);
    rootTree.put("root", childTree);
    /*
     * Now add any parents that are not also in allChildren tree - these will be first descendants
     * of root
     */
    for (String parentKey : parentChildHash.keySet()) {
      if (!rootTree.allChildren.contains(parentKey)) {
        SimpleHashTree children = childTree.createChildTree(parentKey, parentChildHash, treeSize);

        // childTree.put( parentKey, children );
      }
    }

    /* if only one child in rootTree, return the child as the root */
    if (childTree.keySet().size() < 2) {
      return childTree;
    }
    return rootTree;
  }
}
