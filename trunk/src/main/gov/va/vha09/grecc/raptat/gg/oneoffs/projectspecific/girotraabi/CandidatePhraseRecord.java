/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers.AttributeHelper;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Record to store the documentID, startoffset, endOffset, and phrase text of a
 * phrase corresponding to a targeted concept within a document
 *
 * @author VHATVHGOBBEG
 *
 */
public record CandidatePhraseRecord(String documentId, String startOffset,
		String endOffset, String phraseText) {

	public static CandidatePhraseRecord getRecordFromInstance(Instances instancesObject,
			Instance instance) {

		int documentIdIndex = instancesObject
				.attribute(AttributeHelper.AttributeIndex.DOC_ID.toString())
				.index();
		int startOffsetIndex = instancesObject
				.attribute(
						AttributeHelper.AttributeIndex.START_OFFSET.toString())
				.index();
		int endOffsetIndex = instancesObject
				.attribute(AttributeHelper.AttributeIndex.END_OFFSET.toString())
				.index();
		int phraseTextIndex = instancesObject.attribute(
				AttributeHelper.AttributeIndex.INDEX_VALUE_STRING.toString())
				.index();

		String documentId = instance.stringValue(documentIdIndex);
		String startOffset = instance.stringValue(startOffsetIndex);
		String endOffset = instance.stringValue(endOffsetIndex);
		String phraseText = instance.stringValue(phraseTextIndex);

		return new CandidatePhraseRecord(documentId, startOffset, endOffset,
				phraseText);
	}

	public String toString(String delimiter) {
		StringBuilder sb = new StringBuilder(this.documentId);
		sb.append(delimiter).append(this.startOffset);
		sb.append(delimiter).append(this.endOffset);
		sb.append(delimiter).append(this.phraseText);
		return sb.toString();
	}

}
