/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

/**
 * @author VHATVHGOBBEG
 *
 */
public record PatientDocumentStationRecord(String patientId, String documentId,
		String sta3n) {

	public String toString(String delimiter) {
		StringBuilder sb = new StringBuilder(this.patientId);
		sb.append(delimiter);
		sb.append(this.documentId);
		sb.append(delimiter);
		sb.append(this.sta3n);
		return sb.toString();
	}

}
