package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.exporters;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;

/**
 * ArffExportDescriptor
 *
 * <p>
 * - This is a Java bean class used to encapsulated the fields used by an ArffExporter instance
 *
 * @author gtony
 * @date May 26, 2019
 */
public class ArffExportDescriptor {

  public final String projectOriginName;
  public final String fileDescription;
  public final PhraseClass phraseClass;


  /**
   * @param projectOriginName - This will be written in the comments section of a .arff file.
   *        Generally provides the name of the project from which the study originated (e.g.
   *        'Girotra ABI Project').
   * @param fileDescription - This will be written in the comments section of the .arff file and
   *        used to describe the data used to create the file. Note that creation date is not needed
   *        as this is generated automatically and stored in the comments section.
   * @param phraseClass - An enum that describes the class (e.g. INDEX_VALUE) and potential values
   *        (e.g. left_tbi, right_abi, etc) assigned to each instance in the .arff file
   */
  public ArffExportDescriptor(final String projectOriginName, final String fileDescription,
      final PhraseClass phraseClass) {
    super();
    this.projectOriginName = projectOriginName == null ? "" : projectOriginName;
    this.fileDescription = fileDescription == null ? "" : fileDescription;
    this.phraseClass = phraseClass == null ? PhraseClass.DEFAULT : phraseClass;
  }
}
