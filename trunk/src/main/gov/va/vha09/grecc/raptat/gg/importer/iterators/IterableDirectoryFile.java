package src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.DocumentRecord;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PatientDocumentStationRecord;

public class IterableDirectoryFile implements IterableData {

	public class FileIterator implements Iterator<DocumentRecord> {

		private Iterator<Path> filePathIterator;
		private final static String ID_LABEL_REGEX = "(\\d+)_(\\d+)_(\\d+)";
		private final static String DATE_LABEL_REGEX = "(\\d{4}\\-\\d{2}\\-\\d{2})";

		private FileIterator() {
			try {
				this.filePathIterator = Files
						.walk(IterableDirectoryFile.this.directoryPath,
								FileVisitOption.FOLLOW_LINKS)
						.iterator();
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
				System.err.println(
						"Unable to create iterator for files in directory:"
								+ IterableDirectoryFile.this.directoryPath
										.toAbsolutePath());
				e.printStackTrace();
			}
		}

		@Override
		public boolean hasNext() {
			if ( (IterableDirectoryFile.this.currentFileContentsasByteArray != null)
					&& (IterableDirectoryFile.this.currentFilePath != null) ) {
				// If we have already read the contents of a file, we have a
				// next element
				return true;
			}
			else {
				// Otherwise, iterate over files until we find one that we can
				// read
				while ( this.filePathIterator.hasNext() ) {
					Path filePath = this.filePathIterator.next();
					if ( !Files.isDirectory(filePath) ) {

						try {
							// timer.tic( "Reading in file from disk" );
							IterableDirectoryFile.this.currentFileContentsasByteArray = FileUtils
									.readFileToByteArray(filePath.toFile());
							// .readFileToString(filePath.toFile(),
							// "UTF-8");
							// timer.toc( "File read complete" );
							IterableDirectoryFile.this.currentFilePath = filePath;
							IterableDirectoryFile.this.currentFileName = filePath
									.toFile().getName();
							return true;
						}
						catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}

				// If we reach this point, we have iterated over all files and
				// have no next element
				return false;
			}

		}

		@Override
		public DocumentRecord next() {
			if ( !hasNext() ) {
				throw new NoSuchElementException();
			}

			PatientDocumentStationRecord pdsRecord = getPatientDocumentStationRecord(
					IterableDirectoryFile.this.currentFileContentsasByteArray,
					IterableDirectoryFile.this.fileIdLabelHeader);
			String noteDate = getNoteDate(
					IterableDirectoryFile.this.currentFileContentsasByteArray,
					IterableDirectoryFile.this.dateLabelHeader);
			DocumentRecord documentRecord = new DocumentRecord(
					IterableDirectoryFile.this.currentFileContentsasByteArray,
					IterableDirectoryFile.this.currentFilePath,
					IterableDirectoryFile.this.currentFileName, pdsRecord,
					noteDate);
			IterableDirectoryFile.this.currentFileContentsasByteArray = null;

			return documentRecord;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private String getNoteDate(byte[] fileContentsAsByteArray,
				String dateLabel) {

			String date = "";
			try {
				Pattern regex = Pattern.compile(dateLabel + DATE_LABEL_REGEX);
				String documentText = new String(fileContentsAsByteArray,
						"UTF-8");
				Matcher regexMatcher = regex.matcher(documentText);
				if ( regexMatcher.find() ) {
					return regexMatcher.group(1);
				}
			}
			catch (PatternSyntaxException | UnsupportedEncodingException ex) {
				System.err.println("Unable to extract date from note");
				System.err.println(ex.getLocalizedMessage());
				System.exit(-1);
			}
			return date;
		}

		private PatientDocumentStationRecord getPatientDocumentStationRecord(
				byte[] fileContentsAsByteArray, String idLabel) {

			PatientDocumentStationRecord pdsRecord = null;
			try {
				Pattern regex = Pattern.compile(idLabel + ID_LABEL_REGEX);
				String documentText = new String(fileContentsAsByteArray,
						"UTF-8");
				Matcher regexMatcher = regex.matcher(documentText);
				if ( regexMatcher.find() ) {
					String patientId = regexMatcher.group(1);
					String docId = regexMatcher.group(2);
					String sta3n = regexMatcher.group(3);
					pdsRecord = new PatientDocumentStationRecord(patientId,
							docId, sta3n);
				}
			}
			catch (PatternSyntaxException | UnsupportedEncodingException ex) {
				System.err.println("Unable to extract identifiers from note");
				System.err.println(ex.getLocalizedMessage());
				System.exit(-1);
			}
			return pdsRecord;
		}

	}

	public enum IterableDirectoryFileArgs {
		DIRECTORY_PATH_STRING, FILE_ID_LABEL_HEADER, DATE_LABEL_HEADER
	}

	public Path currentFilePath;
	private byte[] currentFileContentsasByteArray;
	private Path directoryPath;
	private String currentFileName;
	private String fileIdLabelHeader;

	private String dateLabelHeader;

	public IterableDirectoryFile(String[] dataIteratorArgs) throws IOException {

		this.directoryPath = Path.of(
				dataIteratorArgs[IterableDirectoryFileArgs.DIRECTORY_PATH_STRING
						.ordinal()]);
		this.fileIdLabelHeader = dataIteratorArgs[IterableDirectoryFileArgs.FILE_ID_LABEL_HEADER
				.ordinal()];
		this.dateLabelHeader = dataIteratorArgs[IterableDirectoryFileArgs.DATE_LABEL_HEADER
				.ordinal()];

		this.currentFileContentsasByteArray = null;
		this.currentFilePath = null;
		this.currentFileName = null;

	}

	@Override
	public Iterator<DocumentRecord> iterator() {
		return new FileIterator();
	}

}
