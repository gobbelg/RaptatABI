package src.main.gov.va.vha09.grecc.raptat.gg.weka.entities;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import src.main.gov.va.vha09.grecc.raptat.dw.feature_builders.builders.common.FeatureBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableData;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.ModuleParameter;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;

public class ArffCreationParameter extends ModuleParameter {
  private IterableData iterableData;
  private Optional<String> outputDirectory;
  private FeatureBuilder[] featureBuilders;
  private String originProject;
  private String fileDescription;
  private Map<PhraseClass, Optional<String>> phraseClassToXmlMap;


  public ArffCreationParameter(final IterableData iterableData,
      final Map<PhraseClass, Optional<String>> phraseClassToXmlMap,
      final Optional<String> arffOutputPath, final FeatureBuilder[] featureBuilders,
      final String originProject, final String fileDescription) {
    super();

    this.iterableData = iterableData;
    this.phraseClassToXmlMap = phraseClassToXmlMap;
    this.outputDirectory = arffOutputPath;
    this.featureBuilders = featureBuilders;
    this.originProject = originProject;
    this.fileDescription = fileDescription;

  }


  public FeatureBuilder[] getFeatureBuilders() {
    return this.featureBuilders;
  }


  public String getFileDescription() {
    return this.fileDescription;
  }


  public Optional<String> getOutputDirectory() throws URISyntaxException {
    return this.outputDirectory;
  }


  public Map<PhraseClass, Optional<String>> getPhraseClassToXmlDirectoryMap() {
    return this.phraseClassToXmlMap;
  }


  public String getProjectOrigin() {
    return this.originProject;
  }


  public IterableData getIterableTextData() {
    return this.iterableData;
  }


  @Override
  public void validate() {
    validateOutputDirectory(this.outputDirectory);
    validateFeatureBuilders(this.featureBuilders);
    validateOrigingProject(this.originProject);
    validateFileDescription(this.fileDescription);
    validatePhraseClassToXmlMap(this.phraseClassToXmlMap);
  }



  private void validateFeatureBuilders(final FeatureBuilder[] featureBuilders) {
    // TODO
  }


  private void validateFileDescription(final String fileDescription) {
    // TODO
  }


  private void validateOrigingProject(final String originProject) {
    // TODO
  }


  private void validateOutputDirectory(final Optional<String> outputDirectory2) {
    // TODO
  }


  private void validatePhraseClassToXmlMap(
      final Map<PhraseClass, Optional<String>> phraseClassToXmlMap2) {
    // TODO
  }

}
