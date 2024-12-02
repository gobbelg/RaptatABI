/** */
package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.AnnotationOverlapGraphEdge;

/**
 * Used as a bean to compare relationships of two annotations, each from two different sets. For the
 * relationship to be the same, the two annotations being compared must have overlapping text, map
 * to the same concept, and the RelationshipScorerNode objects for the relationships under
 * consideration must be equivalent.
 *
 * @author VHATVHGOBBEG
 */
public class RelationshipScorerNode {
  public final AnnotationOverlapGraphEdge edge;
  public final String relationshipName;


  /**
   * @param edge
   * @param relationshipName
   */
  public RelationshipScorerNode(String relationshipName, AnnotationOverlapGraphEdge edge) {
    super();
    this.edge = edge;
    this.relationshipName = relationshipName;
  }


  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   *
   * ( Note that one of the fields, relationAttributes, is not compared during an 'equals' test)
   */
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
    RelationshipScorerNode other = (RelationshipScorerNode) obj;
    if (this.edge == null) {
      if (other.edge != null) {
        return false;
      }
    } else if (!this.edge.equals(other.edge)) {
      return false;
    }
    if (this.relationshipName == null) {
      if (other.relationshipName != null) {
        return false;
      }
    } else if (!this.relationshipName.equals(other.relationshipName)) {
      return false;
    }
    return true;
  }


  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   *
   * * ( Note that one of the fields, relationAttributes, is not used for hash calculation)
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.edge == null ? 0 : this.edge.hashCode());
    result =
        prime * result + (this.relationshipName == null ? 0 : this.relationshipName.hashCode());
    return result;
  }
}
