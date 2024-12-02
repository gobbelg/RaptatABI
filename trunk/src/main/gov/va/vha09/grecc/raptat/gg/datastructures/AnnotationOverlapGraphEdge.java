/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.datastructures;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** @author sahask */
public class AnnotationOverlapGraphEdge implements Comparable<AnnotationOverlapGraphEdge> {
  private final String refNodeMentionID;
  private final String raptatNodeMentionID;
  private final int edgeValue;


  public AnnotationOverlapGraphEdge(String refNodeMentionID, String raptatNodeMentionID,
      int edgeValue) {
    this.refNodeMentionID = refNodeMentionID;
    this.raptatNodeMentionID = raptatNodeMentionID;
    this.edgeValue = edgeValue;
  }


  @Override
  public int compareTo(AnnotationOverlapGraphEdge o) {
    if (getEdgeValue() >= o.getEdgeValue()) {
      return -1;
    } else {
      return 1;
    }
  }


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
    AnnotationOverlapGraphEdge other = (AnnotationOverlapGraphEdge) obj;
    if (this.edgeValue != other.edgeValue) {
      return false;
    }
    if (this.raptatNodeMentionID == null) {
      if (other.raptatNodeMentionID != null) {
        return false;
      }
    } else if (!this.raptatNodeMentionID.equals(other.raptatNodeMentionID)) {
      return false;
    }
    if (this.refNodeMentionID == null) {
      if (other.refNodeMentionID != null) {
        return false;
      }
    } else if (!this.refNodeMentionID.equals(other.refNodeMentionID)) {
      return false;
    }
    return true;
  }


  public int getEdgeValue() {
    return this.edgeValue;
  }


  public String getRaptatNodeMentionID() {
    return this.raptatNodeMentionID;
  }


  public String getRefNodeMentionID() {
    return this.refNodeMentionID;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.edgeValue;
    result = prime * result
        + (this.raptatNodeMentionID == null ? 0 : this.raptatNodeMentionID.hashCode());
    result =
        prime * result + (this.refNodeMentionID == null ? 0 : this.refNodeMentionID.hashCode());
    return result;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("ReferenceNode", this.refNodeMentionID)
        .append("RaptatNode", this.raptatNodeMentionID).append("EdgeValue", this.edgeValue)
        .toString();
  }
}
