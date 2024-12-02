/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import cc.mallet.fst.CRF;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

/**
 * Members of this class store the data structures that are a TokenSequenceAnnotator instances uses
 * to identify and generate AnnotatedPhrase instances within a RaptatDocument object.
 *
 * @author Glenn T. Gobbel Feb 13, 2016
 */
@Deprecated
public class RaptatSolution implements Serializable {
  private static final long serialVersionUID = -2706352557463022644L;


  private ProbabilisticTSFinderSolution probabilisticSolution = null;

  private CRF crfSolution = null;
  private RaptatConstants.solutionType solutionType = null;
  private HashMap<ContextType, AttributeTaggerSolution> cueScopeModels = null;

  public void addCueScopeModel(ContextType type, AttributeTaggerSolution model) {
    this.cueScopeModels.put(type, model);
  }


  public Map<ContextType, AttributeTaggerSolution> getAllAttributeTaggers() {
    return this.cueScopeModels;
  }


  public AttributeTaggerSolution getAttributeTagger(ContextType cueScopeType) {
    return this.cueScopeModels.get(cueScopeType);
  }


  public CRF getCrfSolution() {
    return this.crfSolution;
  }


  public ProbabilisticTSFinderSolution getProbabilisticSolution() {
    return this.probabilisticSolution;
  }


  public RaptatConstants.solutionType getSolutionType() {
    return this.solutionType;
  }


  public void setCrfSolution(CRF crfSolution) {
    this.crfSolution = crfSolution;
  }


  public void setProbabilisticSolution(ProbabilisticTSFinderSolution probabilisticSolution) {
    this.probabilisticSolution = probabilisticSolution;
  }


  public void setSolutionType(RaptatConstants.solutionType solutionType) {
    this.solutionType = solutionType;
  }


  public static RaptatSolution loadFromFile(File solutionFile) {
    RaptatSolution theSolution = null;
    ObjectInputStream inObject = null;

    try {
      inObject = new ObjectInputStream(new FileInputStream(solutionFile));
      theSolution = (RaptatSolution) inObject.readObject();
    } catch (IOException e) {
      System.out.println("Unable to open and read in solution file.  " + e.getClass().getName()
          + " Error " + e.getMessage());
      // e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.out.println("Unable to open and read in solution file. " + e.getClass().getName()
          + " Error " + e.getMessage());
      // e.printStackTrace();
    } finally {
      try {
        if (inObject != null) {
          inObject.close();
        }
      } catch (IOException e) {
        System.out.println("Unable to close solution file\n" + e.getMessage());
      }
    }

    return theSolution;
  };
}
