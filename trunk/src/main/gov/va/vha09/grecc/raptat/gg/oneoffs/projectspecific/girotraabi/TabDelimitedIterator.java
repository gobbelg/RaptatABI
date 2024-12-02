package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

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

import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableData;

public class TabDelimitedIterator implements IterableData {

	public class FileIterator implements Iterator<DocumentRecord> {

		private Iterator<Path> filePathIterator;
		private final static String ID_LABEL_REGEX = "(\\d+)_(\\d+)_(\\d+)";
		private final static String DATE_LABEL_REGEX = "(\\d{4}\\-\\d{2}\\-\\d{2})";

		private FileIterator() {
			try {
				this.filePathIterator = Files
						.walk(TabDelimitedIterator.this.directoryPath,
								FileVisitOption.FOLLOW_LINKS)
						.iterator();
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
				System.err.println(
						"Unable to create iterator for files in directory:"
								+ TabDelimitedIterator.this.directoryPath
										.toAbsolutePath());
				e.printStackTrace();
			}
		}

		@Override
		public boolean hasNext() {
			if ( (TabDelimitedIterator.this.currentFileContentsasByteArray != null)
					&& (TabDelimitedIterator.this.currentFilePath != null) ) {
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
							TabDelimitedIterator.this.currentFileContentsasByteArray = FileUtils
									.readFileToByteArray(filePath.toFile());
							// .readFileToString(filePath.toFile(),
							// "UTF-8");
							// timer.toc( "File read complete" );
							TabDelimitedIterator.this.currentFilePath = filePath;
							TabDelimitedIterator.this.currentFileName = filePath
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
					TabDelimitedIterator.this.currentFileContentsasByteArray,
					TabDelimitedIterator.this.fileIdLabelHeader);
			String noteDate = getNoteDate(
					TabDelimitedIterator.this.currentFileContentsasByteArray,
					TabDelimitedIterator.this.dateLabelHeader);
			DocumentRecord documentRecord = new DocumentRecord(
					TabDelimitedIterator.this.currentFileContentsasByteArray,
					TabDelimitedIterator.this.currentFilePath,
					TabDelimitedIterator.this.currentFileName, pdsRecord,
					noteDate);
			TabDelimitedIterator.this.currentFileContentsasByteArray = null;

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

	public Path currentFilePath;
	private byte[] currentFileContentsasByteArray;
	private Path directoryPath;
	private String currentFileName;
	private String fileIdLabelHeader;
	private String dateLabelHeader;

	public TabDelimitedIterator() {
		// TODO Auto-generated constructor stub
	}

	public TabDelimitedIterator(String directoryPathString,
			String fileIdLabelHeader, String dateLabelHeader)
			throws IOException {
		this.directoryPath = Path.of(directoryPathString);
		this.currentFileContentsasByteArray = null;
		this.currentFilePath = null;
		this.currentFileName = null;
		this.fileIdLabelHeader = this.fileIdLabelHeader;
		this.dateLabelHeader = this.dateLabelHeader;
	}

	@Override
	public Iterator<DocumentRecord> iterator() {
		return new FileIterator();
	}

}
