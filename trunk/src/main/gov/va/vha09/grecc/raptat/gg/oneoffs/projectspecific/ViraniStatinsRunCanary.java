package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.CrfSuiteRunner;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.tagging.CSAttributeTagger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.tab.TabReader;
import src.main.gov.va.vha09.grecc.raptat.gg.sql.AnnotatedPhraseFields;

public class ViraniStatinsRunCanary {

	private enum CanaryOutputColumn {
		NOTE_TITLE {
			@Override
			public String toString() {
				return "noteTitle";
			}
		},
		NOTE_ID {
			@Override
			public String toString() {
				return "noteID";
			}
		},
		OUTPUT_CRITERIA {
			@Override
			public String toString() {
				return "outputCriteria";
			}
		},
		NOTE_START_OFFSET {
			@Override
			public String toString() {
				return "noteStartOffset";
			}
		},
		NOTE_END_OFFSET {
			@Override
			public String toString() {
				return "noteEndOffset";
			}
		},
		FILE_START_OFFSET {
			@Override
			public String toString() {
				return "fileStartOffset";
			}
		},
		FILE_END_OFFSET {
			@Override
			public String toString() {
				return "fileEndOffset";
			}
		},
		SENTENCE {
			@Override
			public String toString() {
				return "sentence";
			}
		},
		STATINS {
			@Override
			public String toString() {
				return "statins";
			}
		},
		ADVERSE_EFFECTS {
			@Override
			public String toString() {
				return "adverseEffects";
			}
		},
		INTOLERANCES {
			@Override
			public String toString() {
				return "intolerances";
			}
		}
	}

	private class CanaryOutputFields {
		private final String noteTitle;
		private final String noteID;
		private final String outputCriteria;
		private final String noteStartOffset;
		private final String noteEndOffset;
		private final String fileStartOffset;
		private final String fileEndOffset;
		private final String sentence;
		private final String[] statins;
		private final String[] adverseEffects;
		private final String[] intolerances;

		private CanaryOutputFields(String noteTitle, String noteID,
				String outputCriteria, String noteStartOffset,
				String noteEndOffset, String fileStartOffset,
				String fileEndOffset, String sentence, String[] statins,
				String[] adverseEffects, String[] intolerances) {
			super();
			this.noteTitle = noteTitle;
			this.noteID = noteID;
			this.outputCriteria = outputCriteria;
			this.noteStartOffset = noteStartOffset;
			this.noteEndOffset = noteEndOffset;
			this.fileStartOffset = fileStartOffset;
			this.fileEndOffset = fileEndOffset;
			this.sentence = sentence;
			this.statins = statins;
			this.adverseEffects = adverseEffects;
			this.intolerances = intolerances;
		}

		private CanaryOutputFields(String[] canaryOutputFields) {
			this(canaryOutputFields[CanaryOutputColumn.NOTE_TITLE.ordinal()],
					canaryOutputFields[CanaryOutputColumn.NOTE_ID.ordinal()],
					canaryOutputFields[CanaryOutputColumn.OUTPUT_CRITERIA
							.ordinal()],
					canaryOutputFields[CanaryOutputColumn.NOTE_START_OFFSET
							.ordinal()],
					canaryOutputFields[CanaryOutputColumn.NOTE_END_OFFSET
							.ordinal()],
					canaryOutputFields[CanaryOutputColumn.FILE_START_OFFSET
							.ordinal()],
					canaryOutputFields[CanaryOutputColumn.FILE_END_OFFSET
							.ordinal()],
					canaryOutputFields[CanaryOutputColumn.SENTENCE.ordinal()],
					canaryOutputFields[CanaryOutputColumn.STATINS.ordinal()]
							.split(","),
					canaryOutputFields[CanaryOutputColumn.ADVERSE_EFFECTS
							.ordinal()].split(","),
					canaryOutputFields[CanaryOutputColumn.INTOLERANCES
							.ordinal()].split(","));
		}

		private String[] getField(CanaryOutputColumn field) {

			switch (field) {
			case STATINS: {
				return this.statins;
			}
			case ADVERSE_EFFECTS: {
				return this.adverseEffects;
			}
			case INTOLERANCES: {
				return this.intolerances;
			}
			default: {
				return null;
			}
			}
		}
	}

	private enum RunType {
		RUN_CANARY
	}

	public static final String RELATIVE_CANARY_PATH = "/src/main/resources/canary/perl_code";
	public static final String CANARY_OUTPUT_FILENAME = "SentenceLevelOutput_1.txt";

	private List<List<String>> convertToCanaryOutput(
			Map<CanaryOutputFields, Map<CanaryOutputColumn, List<AnnotatedPhrase>>> outputToPhraseMap) {

		for (Map<CanaryOutputColumn, List<AnnotatedPhrase>> mappedValue : outputToPhraseMap
				.values()) {

			for (List<AnnotatedPhrase> phraseList : mappedValue.values()) {
				phraseList.removeIf(phrase -> phrase.getPhraseAttributes()
						.stream().anyMatch(attribute -> attribute.getName()
								.equalsIgnoreCase("negated")));
			}
		}
		return null;
	}

	private List<AnnotatedPhrase> getAssociatedPhrases(
			CanaryOutputFields canaryFields, CanaryOutputColumn column) {

		List<AnnotatedPhrase> resultPhrases = new ArrayList<>();
		for (String phraseText : canaryFields.getField(column)) {
			AnnotatedPhraseFields phraseFields = new AnnotatedPhraseFields(
					column.toString(), canaryFields.noteStartOffset,
					canaryFields.noteEndOffset, phraseText);
			AnnotatedPhrase phrase = new AnnotatedPhrase(phraseFields);
			resultPhrases.add(phrase);
		}
		return resultPhrases;
	}

	private Map<CanaryOutputColumn, List<AnnotatedPhrase>> getAssociatedPhrases(
			CanaryOutputFields canaryFields, CanaryOutputColumn[] columns) {
		Map<CanaryOutputColumn, List<AnnotatedPhrase>> resultMap = new HashMap<>();

		for (CanaryOutputColumn column : columns) {
			List<AnnotatedPhrase> associatedPhrases = this
					.getAssociatedPhrases(canaryFields, column);
			resultMap.put(column, associatedPhrases);
		}

		return resultMap;
	}

	private CanaryOutputFields getCanaryOutputFields(String[] dataLine) {
		String noteTitle = dataLine[CanaryOutputColumn.NOTE_TITLE.ordinal()];
		String noteID = dataLine[CanaryOutputColumn.NOTE_ID.ordinal()];
		String outputCriteria = dataLine[CanaryOutputColumn.OUTPUT_CRITERIA
				.ordinal()];
		String noteStartOffset = dataLine[CanaryOutputColumn.NOTE_START_OFFSET
				.ordinal()];
		String noteEndOffset = dataLine[CanaryOutputColumn.NOTE_END_OFFSET
				.ordinal()];
		String fileStartOffset = dataLine[CanaryOutputColumn.FILE_START_OFFSET
				.ordinal()];
		String fileEndOffset = dataLine[CanaryOutputColumn.FILE_END_OFFSET
				.ordinal()];
		String sentenceOffset = dataLine[CanaryOutputColumn.SENTENCE.ordinal()];

		String[] statins = new String[1];
		if ( dataLine.length > 8 ) {
			statins = dataLine[CanaryOutputColumn.STATINS.ordinal()].split(",");
		}

		String[] adverseEffects = new String[1];
		if ( dataLine.length > 9 ) {
			adverseEffects = dataLine[CanaryOutputColumn.ADVERSE_EFFECTS
					.ordinal()].split(",");
		}
		String[] intolerances = new String[1];
		if ( dataLine.length > 10 ) {
			intolerances = dataLine[CanaryOutputColumn.INTOLERANCES.ordinal()]
					.split(",");
		}

		return new CanaryOutputFields(noteTitle, noteID, outputCriteria,
				noteStartOffset, noteEndOffset, fileStartOffset, fileEndOffset,
				sentenceOffset, statins, adverseEffects, intolerances);
	}

	private Map<CanaryOutputFields, Map<CanaryOutputColumn, List<AnnotatedPhrase>>> getFieldsToPhraseMap(
			List<String[]> canaryOutput) {

		Map<CanaryOutputFields, Map<CanaryOutputColumn, List<AnnotatedPhrase>>> resultMap = new HashMap<>();
		for (String[] dataLine : canaryOutput) {
			CanaryOutputFields dataFields = getCanaryOutputFields(dataLine);
			CanaryOutputColumn[] columns = new CanaryOutputColumn[] {};
			Map<CanaryOutputColumn, List<AnnotatedPhrase>> associatedPhrases = this
					.getAssociatedPhrases(dataFields, columns);
			resultMap.put(dataFields, associatedPhrases);
		}
		return resultMap;
	}

	private CSAttributeTagger getNegationTagger(String attributeTaggerPath) {
		AttributeTaggerSolution solution = AttributeTaggerSolution
				.loadFromFile(new File(attributeTaggerPath));
		CrfSuiteRunner taggerRunner = new CrfSuiteRunner();
		return new CSAttributeTagger(solution, taggerRunner);
	}

	private void runCanary(String canaryModelPath) {
		String workingDirectoryPath = this.getClass()
				.getResource(RELATIVE_CANARY_PATH).getFile();
		String wrapperPath = new File(workingDirectoryPath, "Wrapper.pl")
				.getAbsolutePath();
		String notesPath = new File(canaryModelPath, "Notes").getAbsolutePath();
		String configPath = "%"
				+ new File(canaryModelPath, "ConfigFile.txt").getAbsolutePath();
		String outputPath = "+"
				+ new File(canaryModelPath, "Output").getAbsolutePath();

		// Process tracks one external native process
		ProcessBuilder builder = new ProcessBuilder("perl", wrapperPath,
				notesPath, configPath, outputPath);
		builder.redirectErrorStream(true);

		builder.directory(new File(workingDirectoryPath));
		try {
			Process p = builder.start();

			// getInputStream gives an Input stream connected to
			// the process p's standard output. Use it to make
			// a BufferedReader to readLine() what the program writes out.
			try (BufferedReader is = new BufferedReader(
					new InputStreamReader(p.getInputStream()))) {

				String line;
				while ( (line = is.readLine()) != null ) {
					String trimmedLine = line.trim();
					System.out.println("\n" + trimmedLine);
				}
			}
		}
		catch (IOException exception) {
			System.out.println("Unable to start builder");
			System.exit(-1);
		}
	}

	private void writeRevisedCanaryOutput(
			List<List<String>> revisedCanaryOutput, String canaryModelPath) {
		// TODO Auto-generated method stub

	}

	protected List<String[]> getCanaryOutput(String canaryModelPath) {

		List<String[]> resultList = new ArrayList<>();
		File outputFile = new File(canaryModelPath + File.separator + "Output",
				CANARY_OUTPUT_FILENAME);
		DataImportReader outputFileReader = new TabReader(outputFile, false);
		String[] dataLine;

		int expectedDataLength = CanaryOutputColumn.values().length;
		while ( ((dataLine = outputFileReader.getNextData()) != null)
				&& (dataLine.length > 0) ) {

			if ( dataLine.length < expectedDataLength ) {
				String[] tempDataLine = Arrays.copyOf(dataLine,
						expectedDataLength);
				for (int i = dataLine.length; i < expectedDataLength; i++) {
					tempDataLine[i] = "";
				}
				dataLine = tempDataLine;
			}
			resultList.add(dataLine);
		}

		return resultList;
	}

	protected List<List<String>> reviseCanaryOutput(List<String[]> canaryOutput,
			String attributeTaggerPath) {
		Map<CanaryOutputFields, Map<CanaryOutputColumn, List<AnnotatedPhrase>>> fieldsToPhraseMap = getFieldsToPhraseMap(
				canaryOutput);
		List<AnnotatedPhrase> allPhrases = new ArrayList<>();

		for (CanaryOutputFields fields : fieldsToPhraseMap.keySet()) {
			Map<CanaryOutputColumn, List<AnnotatedPhrase>> nameToPhraseMap = fieldsToPhraseMap
					.get(fields);
			nameToPhraseMap
					.forEach((key, phrases) -> allPhrases.addAll(phrases));
		}

		CSAttributeTagger negationTagger = getNegationTagger(
				attributeTaggerPath);
		negationTagger.assignCSAttributes(allPhrases);
		List<List<String>> resultOutput = convertToCanaryOutput(
				fieldsToPhraseMap);

		return resultOutput;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UserPreferences.INSTANCE.initializeLVGLocation();
		ViraniStatinsRunCanary runner = new ViraniStatinsRunCanary();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RunType runType = RunType.RUN_CANARY;

				switch (runType) {
				case RUN_CANARY: {
					String canaryModelPath = "C:\\Users\\gobbelgt\\Documents\\temp\\bloodpressure";
					String attributeTaggerPath = "";
					runner.runCanary(canaryModelPath);
					List<String[]> canaryOutput = runner
							.getCanaryOutput(canaryModelPath);
					List<List<String>> revisedCanaryOutput = runner
							.reviseCanaryOutput(canaryOutput,
									attributeTaggerPath);
					runner.writeRevisedCanaryOutput(revisedCanaryOutput,
							canaryModelPath);
				}
					break;

				default:
					break;
				}
			}
		});
	}
}
