package src.main.gov.va.vha09.grecc.raptat.gg.uima.resourceinterfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;

/**
 * ****************************************************** This is just a wrapper that adds
 * functionality to the AlgorithmSolution class so that AlgorithmSolution instances can be used as
 * external resources within UIMA
 *
 * @author Glenn Gobbel - Sep 9, 2013 *****************************************************
 */
public class UIMAAlgorithmSolutionExtension extends ProbabilisticTSFinderSolution
    implements UIMASolution, SharedResourceObject {
  private static final long serialVersionUID = 8997739732917115791L;


  public UIMAAlgorithmSolutionExtension() {
    // TODO Auto-generated constructor stub
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD Necessary
   * for implementing the shared resource object interface for use with UIMA
   *
   * @param inputResource
   * @throws ResourceInitializationException
   * @author Glenn Gobbel - Aug 26, 2013 ***********************************************************
   */
  @Override
  public void load(DataResource inputResource) throws ResourceInitializationException {
    ObjectInputStream inObject = null;

    try {
      inObject = new ObjectInputStream(inputResource.getInputStream());
      // inObject = new ObjectInputStream( new FileInputStream( new File(
      // inputResource.getUri() ) ) );
      copyFields((ProbabilisticTSFinderSolution) inObject.readObject());
    } catch (Exception e) {
      System.out.println("Unable to open and read in solution file");
      e.printStackTrace();
    } finally {
      try {
        if (inObject != null) {
          inObject.close();
        }
      } catch (IOException e) {
        System.out.println("Unable to close solution file");
        e.printStackTrace();
      }
    }
  }


  private void copyFields(ProbabilisticTSFinderSolution inputSolution) {
    this.unlabeledHashTreePath = inputSolution.getUnlabeledHashTreePath();
    this.sequenceIDSolution = inputSolution.getSequenceIdentifier();
    this.conceptMapSolution = inputSolution.getConceptMapSolution();
    this.acceptableConcepts = inputSolution.getAcceptableConcepts();
    this.smoothingMethod = inputSolution.getSmoothingMethod();
    this.trainingTokenNumber = inputSolution.getTrainingTokenNumber();
  }
}
