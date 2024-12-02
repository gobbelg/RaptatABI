/** */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import java.util.Collections;
import java.util.List;

/**
 * ****************************************************** This is similar to the
 * ComparatorBinaryTree. This class allows for explicit storage of only IntegerPair instances.
 * Unlike ComparatorBinaryTree instances, this one allows searching with Integers and comparison to
 * the left side of the pair for the lookup() and getClosestLesserOrEqualObject() methods.
 *
 * @author Glenn Gobbel - Apr 27, 2012 *****************************************************
 */
public class IntegerPairComparer {
  private IntegerPair objectData = null;
  private IntegerPairComparer leftBranch = null;
  private IntegerPairComparer rightBranch = null;
  private int size = 0;


  /**
   * *********************************************************** Create an empty searchable binary
   * tree with null as the root node. The list can be sorted during construction if this is selected
   * by the user. Use of a sorted list will
   *
   * @author Glenn Gobbel - Apr 26, 2012 ***********************************************************
   */
  public IntegerPairComparer(List<IntegerPair> inputList) {
    Collections.sort(inputList);
    listToSBT(inputList, 0, inputList.size() - 1);
  }


  private IntegerPairComparer(List<IntegerPair> inputList, int start, int end) {
    listToSBT(inputList, start, end);
  }


  public IntegerPair getClosestGreaterObject(Integer testData) {
    // Place holder for unimplemented method
    throw new Error("Unimplemented method");
  }


  /**
   * *********************************************************** Get the object in the tree that is
   * closest in value to a test value but lesser or equal to.
   *
   * @param testData
   * @return Value of object in tree that is closest to the testValue but of equal or less value.
   * @author Glenn Gobbel - Apr 27, 2012 ***********************************************************
   */
  public IntegerPair getClosestLesserOrEqualObject(Integer testData) {
    if (this.objectData == null) {
      return null;
    }

    Integer objectValue = this.objectData.left;
    int curDataCompare = objectValue - testData;
    IntegerPair nextData;

    if (curDataCompare == 0) {
      return this.objectData;
    } else {
      if (curDataCompare > 0) // testValue < objectValue
      {
        if (this.leftBranch == null) {
          // No smaller value, so return nothing
          return null;
        }
        // Look at left branch to see if any value smaller than
        // the objectValue is less than testValue
        return this.leftBranch.getClosestLesserOrEqualObject(testData);
      } else
      // testValue > objectValue
      {
        if (this.rightBranch == null) {
          // No larger value, so return object value
          return this.objectData;
        }
        // Look at right branch to see if any value is larger than
        // the objectValue but still less than testValue
        nextData = this.rightBranch.getClosestLesserOrEqualObject(testData);
        return nextData != null ? nextData : this.objectData;
      }
    }
  }


  public RaptatPair<String, Integer> getSideAndDepth(Integer testValue) {
    return this.getSideAndDepth(testValue, 0, "root");
  }


  /**
   * *********************************************************** Returns true if the data is in the
   * binary tree
   *
   * @param testValue
   * @return
   * @author Glenn Gobbel - Apr 26, 2012 ***********************************************************
   */
  public boolean lookup(Integer testValue) {
    if (this.objectData == null) {
      return false;
    }

    Integer objectValue = this.objectData.left;
    int dataCompare = objectValue - testValue;

    if (dataCompare == 0) {
      return true;
    } else {
      if (dataCompare > 0) {
        return this.leftBranch != null && this.leftBranch.lookup(testValue);
      }
      return this.rightBranch != null && this.rightBranch.lookup(testValue);
    }
  }


  public int size() {
    return this.size;
  }


  private RaptatPair<String, Integer> getSideAndDepth(Integer testValue, int startDepth,
      String side) {

    if (this.objectData == null) {
      return null;
    }

    Integer objectValue = this.objectData.left;
    int dataCompare = objectValue - testValue;

    if (dataCompare == 0) {
      return new RaptatPair<>(side, startDepth);
    } else {
      if (dataCompare > 0) {
        if (this.leftBranch != null) {
          return this.leftBranch.getSideAndDepth(testValue, ++startDepth, "left");
        }
      } else {
        if (this.rightBranch != null) {
          return this.rightBranch.getSideAndDepth(testValue, ++startDepth, "right");
        }
      }
    }

    return null;
  }


  /**
   * ******************************************** Helper method for creating a SearchableBinaryTree
   *
   * @param node
   * @param left
   * @param rightBranch
   * @author Glenn Gobbel - Apr 26, 2012 ********************************************
   */
  private void listToSBT(List<IntegerPair> inputList, int start, int end) {
    int mid = (start + end) / 2;
    this.objectData = inputList.get(mid);
    this.size++;
    if (mid > start) {
      this.leftBranch = new IntegerPairComparer(inputList, start, mid - 1);
    }
    if (end > start) {
      this.rightBranch = new IntegerPairComparer(inputList, mid + 1, end);
    }
  }
}
