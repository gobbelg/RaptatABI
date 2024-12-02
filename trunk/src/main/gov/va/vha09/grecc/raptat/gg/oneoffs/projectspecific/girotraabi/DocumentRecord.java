/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.nio.file.Path;

/**
 * @author Glenn Gobbel
 * @date 23-03-21
 *
 */
public record DocumentRecord(byte[] documentTextAsByteArray, Path documentPath,
		String fileName,
		PatientDocumentStationRecord patientDocumentStationIdentifiers,
		String noteDate) {
}
