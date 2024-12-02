package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * Class to store the various String parameters used to uniquely define a given score, such as
 * concept name, attribute name, relationship name, and concept linked to by relationship,
 * relationship attribute nuame, and relationship attribute value. These are used to uniquely
 * specify a hash to a measure of performance during cross-validation or as a result of annotating a
 * reference set of documents.
 *
 * @author VHATVHGOBBEG
 *
 */
public class PerformanceScoreObject
{
    public final static PerformanceScoreObject EXACT_MATCH_PSO = new PerformanceScoreObject(
    		RaptatConstants.TOTAL_EXACT_MATCH_NAME);
    public static final PerformanceScoreObject PARTIAL_MATCH_PSO = new PerformanceScoreObject(
    		RaptatConstants.TOTAL_PARTIAL_MATCH_NAME);

    final String conceptName;
    final String attributeName;
    final String attributeValueName;
    final String relationshipName;
    final String conceptLinkedToByRelationship;
    final String relationshipAttributeName;
    final String relationshipAttributeValueName;
    private String toStringDelimiter = "\t";

    /**
     * Convenience constructor when the other fields are empty and not needed
     *
     * @param conceptName
     */
    public PerformanceScoreObject(String conceptName)
    {
        super();
        this.conceptName = conceptName;
        this.attributeName = "";
        this.attributeValueName = "";
        this.relationshipName = "";
        this.conceptLinkedToByRelationship = "";
        this.relationshipAttributeName = "";
        this.relationshipAttributeValueName = "";
    }

    /**
     * Convenience constructor when the other fields are empty and not needed
     *
     * @param conceptName
     * @param attributeName
     * @param attributeValueName
     */
    public PerformanceScoreObject(String conceptName, String attributeName,
            String attributeValueName)
    {
        super();
        this.conceptName = conceptName;
        this.attributeName = attributeName;
        this.attributeValueName = attributeValueName;
        this.relationshipName = "";
        this.conceptLinkedToByRelationship = "";
        this.relationshipAttributeName = "";
        this.relationshipAttributeValueName = "";
    }

    /**
     * @param conceptName
     * @param attributeName
     * @param attributeValueName
     * @param relationshipName
     * @param conceptLinkedToByRelationship
     * @param relationshipAttributeName
     * @param relationshipAttributeValueName
     */
    public PerformanceScoreObject(String conceptName, String attributeName,
            String attributeValueName, String relationshipName,
            String conceptLinkedToByRelationship, String relationshipAttributeName,
            String relationshipAttributeValueName)
    {
        super();
        if (conceptName == null) {
            conceptName = "";
        }
        if (attributeName == null) {
            attributeName = "";
        }
        if (attributeValueName == null) {
            attributeValueName = "";
        }
        if (relationshipName == null) {
            relationshipName = "";
        }
        if (conceptLinkedToByRelationship == null) {
            conceptLinkedToByRelationship = "";
        }
        if (relationshipAttributeName == null) {
            relationshipAttributeName = "";
        }
        if (relationshipAttributeValueName == null) {
            relationshipAttributeValueName = "";
        }
        this.conceptName = conceptName;
        this.attributeName = attributeName;
        this.attributeValueName = attributeValueName;
        this.relationshipName = relationshipName;
        this.conceptLinkedToByRelationship = conceptLinkedToByRelationship;
        this.relationshipAttributeName = relationshipAttributeName;
        this.relationshipAttributeValueName = relationshipAttributeValueName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PerformanceScoreObject other = (PerformanceScoreObject) obj;
        if (this.attributeName == null) {
            if (other.attributeName != null) {
                return false;
            }
        }
        else if (!this.attributeName.equals(other.attributeName)) {
            return false;
        }
        if (this.attributeValueName == null) {
            if (other.attributeValueName != null) {
                return false;
            }
        }
        else if (!this.attributeValueName.equals(other.attributeValueName)) {
            return false;
        }
        if (this.conceptLinkedToByRelationship == null) {
            if (other.conceptLinkedToByRelationship != null) {
                return false;
            }
        }
        else if (!this.conceptLinkedToByRelationship.equals(other.conceptLinkedToByRelationship)) {
            return false;
        }
        if (this.conceptName == null) {
            if (other.conceptName != null) {
                return false;
            }
        }
        else if (!this.conceptName.equals(other.conceptName)) {
            return false;
        }
        if (this.relationshipAttributeName == null) {
            if (other.relationshipAttributeName != null) {
                return false;
            }
        }
        else if (!this.relationshipAttributeName.equals(other.relationshipAttributeName)) {
            return false;
        }
        if (this.relationshipAttributeValueName == null) {
            if (other.relationshipAttributeValueName != null) {
                return false;
            }
        }
        else if (!this.relationshipAttributeValueName
                .equals(other.relationshipAttributeValueName)) {
            return false;
        }
        if (this.relationshipName == null) {
            if (other.relationshipName != null) {
                return false;
            }
        }
        else if (!this.relationshipName.equals(other.relationshipName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.attributeName == null ? 0 : this.attributeName.hashCode());
        result = prime * result
                + (this.attributeValueName == null ? 0 : this.attributeValueName.hashCode());
        result = prime * result + (this.conceptLinkedToByRelationship == null ? 0
                : this.conceptLinkedToByRelationship.hashCode());
        result = prime * result + (this.conceptName == null ? 0 : this.conceptName.hashCode());
        result = prime * result + (this.relationshipAttributeName == null ? 0
                : this.relationshipAttributeName.hashCode());
        result = prime * result + (this.relationshipAttributeValueName == null ? 0
                : this.relationshipAttributeValueName.hashCode());
        result = prime * result
                + (this.relationshipName == null ? 0 : this.relationshipName.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.conceptName);
        sb.append(this.toStringDelimiter).append(this.attributeName);
        sb.append(this.toStringDelimiter).append(this.attributeValueName);
        sb.append(this.toStringDelimiter).append(this.relationshipName);
        sb.append(this.toStringDelimiter).append(this.conceptLinkedToByRelationship);
        sb.append(this.toStringDelimiter).append(this.relationshipAttributeName);
        sb.append(this.toStringDelimiter).append(this.relationshipAttributeValueName);

        return sb.toString();
    }
}
