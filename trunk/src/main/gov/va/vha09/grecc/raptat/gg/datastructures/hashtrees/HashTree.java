package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * @author Glenn Gobbel - Jul 17, 2014
 * @param <V>
 */
public abstract class HashTree<V extends HashTree> extends Hashtable<String, V>
    implements Serializable {

  private static final long serialVersionUID = 8736252875924509092L;
  private static Logger logger = Logger.getLogger(HashTree.class);
  protected int treeDepth;


  public HashTree() {
    super(RaptatConstants.LABELED_HASH_TREE_SIZE_DEPTH_0_DEFAULT);
    this.treeDepth = 0;
  }


  public HashTree(HashTree<V> inputTree) {
    super(inputTree);
  }


  public HashTree(int hashSize) {
    super(hashSize);
    this.treeDepth = 0;
  }


  public HashTree(int hashSize, int depth) {
    super(hashSize);
    this.treeDepth = depth;
  }


  public boolean canEqual(Object other) {
    return other instanceof HashTree;
  }


  public void compareTo(Object other) {
    if (other instanceof HashTree) {
      HashTree otherTree = (HashTree) other;

      boolean treesEqual = super.equals(otherTree);
      System.out.println(
          "HashTree at depth " + this.treeDepth + (treesEqual ? " equals " : " does not equal ")
              + "other HashTree at depth " + otherTree.treeDepth);

      System.out.println("Tree depth equivalence: " + (this.treeDepth == otherTree.treeDepth));

      if (!treesEqual) {
        Set<String> thisSet = keySet();
        Set<String> otherSet = otherTree.keySet();
        boolean keySetsEqual = thisSet.equals(otherSet);
        System.out.println("KeySets for compared trees are "
            + (keySet().equals(otherTree.keySet()) ? "equal" : "not equal"));
        if (keySetsEqual) {
          System.out.println(
              "-----------------------\nEvaluating next tree level\n-----------------------");
          for (String curKey : thisSet) {
            System.out.println("Current Key::" + curKey);
            HashTree thisNextTree = get(curKey);
            HashTree otherNextTree = (HashTree) otherTree.get(curKey);
            thisNextTree.compareTo(otherNextTree);
          }
        }
      }

    } else {
      System.out.println("Cannot compare\nCompared object not a HashTree instance");
    }
  }


  @Override
  public boolean equals(Object other) {
    // logger.setLevel(Level.DEBUG);

    if (other instanceof HashTree) {
      HashTree otherTree = (HashTree) other;
      if (otherTree.canEqual(this)) {
        EqualsBuilder eBuilder = new EqualsBuilder();
        boolean result = eBuilder.appendSuper(super.equals(otherTree))
            .append(this.treeDepth, otherTree.treeDepth).isEquals();

        if (!result && HashTree.logger.isDebugEnabled()) {
          this.compareTo(otherTree);
        }
        return result;
      }
    }

    return false;
  }


  @Override
  public int hashCode() {
    int oddNumber = 43;
    int primeNumber = 19;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);
    return hcBuilder.append(this.treeDepth).append(super.hashCode()).toHashCode();
  }


  public boolean write(ObjectOutputStream outStream) {
    boolean noWriteErrors = true;

    if (outStream != null) {
      try {
        outStream.writeObject(this);
      } catch (IOException e) {
        System.out.println(e);
        e.printStackTrace();
        noWriteErrors = false;
      }
    } else {
      noWriteErrors = false;
    }
    return noWriteErrors;
  }


  /**
   * *********************************************************** Determine the size of the next
   * subtree based on the depth of this tree and default settings
   *
   * @return treeSize
   * @author Glenn Gobbel - Apr 6, 2012 ***********************************************************
   */
  protected int getNextTreeSize() {
    int treeSize;
    switch (this.treeDepth) {
      case 0:
        treeSize = RaptatConstants.LABELED_HASH_TREE_SIZE_DEPTH_1_DEFAULT;
        break;
      case 1:
        treeSize = RaptatConstants.LABELED_HASH_TREE_SIZE_DEPTH_2_DEFAULT;
        break;
      default:
        treeSize = RaptatConstants.LABELED_HASH_TREE_SIZE_DEPTH_OVER2_DEFAULT;
        break;
    }
    return treeSize;
  }
}
