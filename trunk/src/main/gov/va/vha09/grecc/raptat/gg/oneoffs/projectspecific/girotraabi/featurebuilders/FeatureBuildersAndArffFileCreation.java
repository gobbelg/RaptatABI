package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders;

import java.text.MessageFormat;
import java.util.List;

import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.FeatureBuilderRunner;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.runner.RunDirection;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.TextDocumentLine;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.InstanceBuilderModule;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.ModuleResultBase;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.DocumentColumn;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.weka.entities.ArffCreationParameter;

public abstract class FeatureBuildersAndArffFileCreation
		extends InstanceBuilderModule {

	public FeatureBuildersAndArffFileCreation(
			final ArffCreationParameter arffCreationParameter) {
		super(arffCreationParameter);

	}

	/* Commented out as unneeded by Glenn on 4/12/23 */
	@SuppressWarnings("unused")
	@Override
	@Deprecated
	public ModuleResultBase process() {
		ArffCreationResults results = null;
		return results;

	}

	private boolean isIndexValue(final RaptatTokenPhrase phrase) {
		return phrase.getPhraseLabels()
				.contains(new RaptatTokenPhrase.Label("IndexValue"));
	}

	/**
	 * Builds the column features.
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 * @param runDirection
	 *            the run direction
	 */
	private static void build_column_features(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders,
			final RunDirection runDirection) {
		final FeatureBuilderRunner runner = new FeatureBuilderRunner(
				featureBuilders);
		final List<DocumentColumn> documentColumns = document
				.getDocumentColumns();
		final int size = documentColumns.size();
		for (int index = 0; index < size; index++) {
			System.out.println(MessageFormat.format(
					"Building column features for column {0} out of {1}", index,
					size));
			final List<RaptatTokenPhrase> phrase = documentColumns
					.get(index).columnPhrases;
			runner.process_column(phrase, runDirection,
					document.getProcessedTokens());
		}

	}

	/**
	 * Builds the features for columns, rows, and whole document
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 */
	private static void build_features(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders) {
		build_row_features(document, featureBuilders, RunDirection.Forward);
		build_column_features(document, featureBuilders, RunDirection.Forward);
		build_features_for_document(document, featureBuilders,
				RunDirection.Forward);

		build_row_features(document, featureBuilders, RunDirection.Reverse);
		build_column_features(document, featureBuilders, RunDirection.Reverse);
		build_features_for_document(document, featureBuilders,
				RunDirection.Reverse);
	}

	/**
	 * Builds the features for all phrases in the document, left-to-right,
	 * top-to-bottom
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 */
	private static void build_features_for_document(
			final RaptatDocument document,
			final FeatureBuilder[] featureBuilders,
			final RunDirection direction) {
		final FeatureBuilderRunner runner = new FeatureBuilderRunner(
				featureBuilders);
		runner.process_document(document.getTokenPhrases(), direction,
				document.getProcessedTokens());
	}

	/**
	 * Builds the row features.
	 *
	 * @param document
	 *            the document
	 * @param featureBuilders
	 *            the feature builders
	 * @param runDirection
	 *            the run direction
	 */
	private static void build_row_features(final RaptatDocument document,
			final FeatureBuilder[] featureBuilders,
			final RunDirection runDirection) {
		final FeatureBuilderRunner runner = new FeatureBuilderRunner(
				featureBuilders);
		final List<TextDocumentLine> textDocumentLines = document
				.getTextDocumentLines();
		final IndexedTree<IndexedObject<RaptatToken>> processedTokens = document
				.getProcessedTokens();
		for (final TextDocumentLine line : textDocumentLines) {
			runner.process_row(line.getTokenPhraseList(), runDirection,
					processedTokens);
		}

	}

	/**
	 * Gets the ehost xml document name.
	 *
	 * @param ehostXmlDocumentPath
	 *            the ehost xml document path
	 * @param fileName
	 *            the file name
	 * @return the ehost xml document name
	 */
	private static String getEhostDocName(final String ehostXmlDocumentPath,
			final String fileName) {
		return ehostXmlDocumentPath + "\\" + fileName + ".knowtator.xml";
	}

}
