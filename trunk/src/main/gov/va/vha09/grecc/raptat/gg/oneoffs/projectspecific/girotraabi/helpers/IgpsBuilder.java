package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers;

import java.util.List;
import java.util.Optional;

import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableData;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.GirotraABI;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters.AttributeFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

public class IgpsBuilder {

	private GirotraABI girotraAbiTool;

	private IterableData textDataIterator;

	private TextAnalyzer textAnalyzer;

	private Optional<String> lateralityDirectory = Optional.empty();

	private Optional<String> indexTypeDirectory = Optional.empty();

	private Optional<String> arffOutputPath = Optional.empty();

	private Optional<String> logTokenPhrasesDirectory = Optional.empty();

	private Optional<List<AttributeFilter>> attributeFilters = Optional.empty();

	private IgpsBuilder() {

	}

	public InstancesGetterParameters build() {

		validateFields();

		return new InstancesGetterParameters(this.girotraAbiTool,
				this.textDataIterator, this.textAnalyzer,
				this.lateralityDirectory, this.indexTypeDirectory,
				this.arffOutputPath, this.attributeFilters,
				this.logTokenPhrasesDirectory);
	}

	public IgpsBuilder setArffOutputPath(String arffOutputPath) {
		this.arffOutputPath = Optional.ofNullable(arffOutputPath);
		return this;
	}

	public IgpsBuilder setAttributeFilters(
			List<AttributeFilter> attributeFilters) {
		this.attributeFilters = Optional.of(attributeFilters);
		return this;
	}

	public IgpsBuilder setGirotraAbiTool(GirotraABI girotraAbiTool) {
		this.girotraAbiTool = girotraAbiTool;
		return this;
	}

	/*
	 * The indexTypeDirectoryTrain should contain the name of the folder
	 * containing eHOST *.xml files which contain annotations of index values
	 * with their index type assigned as a concept. So, for example, in the
	 * sentence, "The left ABI value was found to be 0.85," the '0.85' token
	 * would be annotated with an assigned concept of 'abi.'
	 */
	public IgpsBuilder setIndexTypeDirectory(String indexTypeDirectory) {
		if ( (indexTypeDirectory != null) && indexTypeDirectory.isBlank() ) {
			this.lateralityDirectory = null;
		}
		this.indexTypeDirectory = Optional.ofNullable(indexTypeDirectory);
		return this;
	}

	/*
	 * The lateralityDirectoryTrain should contain the name of the folder
	 * containing eHOST *.xml files which contain annotations of index values
	 * with their laterality assigned as a concept. So, for example, in the
	 * sentence, "The left ABI value was found to be 0.85," the '0.85' token
	 * would be annotated with an assigned concept of 'left.'
	 */
	public IgpsBuilder setLateralityDirectory(String lateralityDirectory) {
		if ( (lateralityDirectory != null) && lateralityDirectory.isBlank() ) {
			lateralityDirectory = null;
		}
		this.lateralityDirectory = Optional.ofNullable(lateralityDirectory);
		return this;
	}

	public IgpsBuilder setLogTokenPhrasesDirectory(
			String logTokenPhrasesDirectory) {
		if ( (logTokenPhrasesDirectory != null)
				&& logTokenPhrasesDirectory.isBlank() ) {
			logTokenPhrasesDirectory = null;
		}
		this.logTokenPhrasesDirectory = Optional
				.ofNullable(logTokenPhrasesDirectory);
		return this;
	}

	public IgpsBuilder setTextAnalyzer(TextAnalyzer textAnalyzer) {
		this.textAnalyzer = textAnalyzer;
		return this;
	}

	public IgpsBuilder setTextDataIterator(IterableData textDataIterator) {
		this.textDataIterator = textDataIterator;
		return this;
	}

	/**
	*
	*/
	private void validateFields() {
		// Validate the fields before creating an InstancesGetterParameters
		// instance
		if ( this.girotraAbiTool == null ) {
			throw new IllegalArgumentException("GirotraAbiTool cannot be null");
		}

		if ( this.textDataIterator == null ) {
			throw new IllegalArgumentException(
					"TextDataIterator cannot be null or blank");
		}

		if ( (this.textAnalyzer == null)
				|| (this.textAnalyzer.getTokenPhraseMaker() == null) ) {
			throw new IllegalArgumentException(
					"TextAnalyzer cannot be null or blank and \nmust contain a non-null TokenPhraseMaker");
		}
	}

	public static IgpsBuilder getBuilder() {
		return new IgpsBuilder();
	}

}
