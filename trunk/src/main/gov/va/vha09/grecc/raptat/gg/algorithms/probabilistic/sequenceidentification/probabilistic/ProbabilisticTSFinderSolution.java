package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.ConceptMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.attributemapping.ConceptAttributeMapSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.TokenSequenceFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SequenceIDStructure;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTreeStreamer;

/**
 * Enables the storing of the components needed to do phrase matching and concept mapping based on
 * training on annotated documents and analysis of a raw text document. Note that the entire
 * ConceptMapSolution is stored, which can be used by a ConceptMapTrainer to map phrases, and a
 * SequenceIDStructure is stored, which is generally a LabeledHashTree structure. The
 * SequenceIDStructure is used to instantiate a SequenceIdentifier to identify phrases for
 * annotation.
 *
 * @author Glenn Gobbel - Feb 13, 2012 Updated - Dec 19, 2013
 */
public class ProbabilisticTSFinderSolution extends TokenSequenceFinderSolution {

  private static final long serialVersionUID = -2706352557463021234L;
  private static Logger logger = Logger.getLogger(ProbabilisticTSFinderSolution.class);


  protected String unlabeledHashTreePath;

  protected SequenceIDStructure sequenceIDSolution;
  protected ConceptMapSolution conceptMapSolution;
  protected HashSet<String> acceptableConcepts = null;

  protected PhraseIDSmoothingMethod smoothingMethod = null;
  protected int trainingTokenNumber;

  protected List<SchemaConcept> schemaConceptList;

  public ProbabilisticTSFinderSolution() {
    super();
    ProbabilisticTSFinderSolution.logger.setLevel(Level.INFO);
  }


  public ProbabilisticTSFinderSolution(String unlabeledHashTreePath,
      SequenceIDStructure sequenceIDSolution, ConceptMapSolution cmSolution,
      HashSet<String> acceptableConcepts, int trainTokenNumber,
      PhraseIDSmoothingMethod phraseIDSmoothing, List<SchemaConcept> schemaConceptList) {
    this();
    this.unlabeledHashTreePath = unlabeledHashTreePath;
    this.sequenceIDSolution = sequenceIDSolution;
    this.conceptMapSolution = cmSolution;
    this.acceptableConcepts = acceptableConcepts;
    this.trainingTokenNumber = trainTokenNumber;
    this.smoothingMethod = phraseIDSmoothing;
    this.conceptMapSolution.setSchemaConcepts(schemaConceptList);
  }


  @Override
  public boolean equals(Object other) {
    if (other instanceof ProbabilisticTSFinderSolution) {
      ProbabilisticTSFinderSolution otherSolution = (ProbabilisticTSFinderSolution) other;
      EqualsBuilder eBuilder = new EqualsBuilder();

      if (this.unlabeledHashTreePath != null && otherSolution.unlabeledHashTreePath != null) {
        // Add the trees pointed to by the unlabledHashTreePath (not the
        // path itself as
        // different paths may contain the same trees, in which case the
        // AlgorithmSolution
        // instances would not be considered different, based on
        // unlabeledHashTrees at
        // least)
        UnlabeledHashTreeStreamer thisTreeStreamer =
            new UnlabeledHashTreeStreamer(null, this.unlabeledHashTreePath);
        Iterator<UnlabeledHashTree> thisTreeIterator = thisTreeStreamer.iterator();
        UnlabeledHashTreeStreamer otherTreeStreamer =
            new UnlabeledHashTreeStreamer(null, otherSolution.unlabeledHashTreePath);
        Iterator<UnlabeledHashTree> otherTreeIterator = otherTreeStreamer.iterator();
        while (thisTreeIterator.hasNext() && otherTreeIterator.hasNext()) {
          eBuilder.append(thisTreeIterator.next(), otherTreeIterator.next());
        }
        // Return false if one solution has more unlabeledhashtrees than
        // the other
        if (thisTreeIterator.hasNext() || thisTreeIterator.hasNext()) {
          return false;
        }
      }
      // Else, if both unlabeledHashTreePaths are *not* both null, return
      // false
      else if (this.unlabeledHashTreePath != null || otherSolution.unlabeledHashTreePath != null) {
        return false;
      }

      // If one has null acceptableConcepts field and the other does not,
      // return false. Otherwise, if both are not null, add the fields to
      // eBuilder
      if (this.acceptableConcepts != null && otherSolution.acceptableConcepts != null) {
        eBuilder.append(this.acceptableConcepts, otherSolution.acceptableConcepts);
      } else if (this.acceptableConcepts != null || otherSolution.acceptableConcepts != null) {
        return false;
      }

      return eBuilder.append(this.sequenceIDSolution, otherSolution.sequenceIDSolution)
          .append(this.conceptMapSolution, otherSolution.conceptMapSolution)
          .append(this.smoothingMethod, otherSolution.smoothingMethod)
          .append(this.trainingTokenNumber, otherSolution.trainingTokenNumber).isEquals();
    }
    return false;
  }


  public HashSet<String> getAcceptableConcepts() {
    return this.acceptableConcepts;
  }


  public ConceptAttributeMapSolution getAttributeMapSolution() {
    return this.conceptMapSolution.getConceptAttributeMapSolution();
  }


  /** @return the conceptMapSolution */
  public ConceptMapSolution getConceptMapSolution() {
    return this.conceptMapSolution;
  }


  // public List<SchemaConcept> getSchemaConceptList()
  // {
  // return this.schemaConceptList;
  // }

  /** @return the solutionHashTree */
  public SequenceIDStructure getSequenceIdentifier() {
    return this.sequenceIDSolution;
  }


  /** @return the smoothingMethod */
  public PhraseIDSmoothingMethod getSmoothingMethod() {
    return this.smoothingMethod;
  }


  public TokenProcessingOptions getTokenTrainingOptions() {
    TokenProcessingOptions theOptions = null;
    if (this.conceptMapSolution != null) {
      theOptions = new TokenProcessingOptions(this.conceptMapSolution.getUseStems(),
          this.conceptMapSolution.getUsePOS(), this.conceptMapSolution.getRemoveStopWords(),
          this.conceptMapSolution.getInvertTokenSequence(),
          this.conceptMapSolution.getReasonNotOnMedsProcessing(),
          this.conceptMapSolution.getNegationProcessing(), this.trainingTokenNumber);
    }
    return theOptions;
  }


  public int getTrainingTokenNumber() {
    return this.trainingTokenNumber;
  }


  /** @return the unlabeledHashTreePath */
  public String getUnlabeledHashTreePath() {
    return this.unlabeledHashTreePath;
  }


  @Override
  public int hashCode() {
    int oddNumber = 87;
    int primeNumber = 7;
    HashCodeBuilder hcBuilder = new HashCodeBuilder(primeNumber, oddNumber);

    // We need to build the trees from the unlabeledHashTrees and not the
    // path so that the
    // hashCode is consistent with the equals() method
    if (this.unlabeledHashTreePath != null) {
      UnlabeledHashTreeStreamer thisTreeStreamer =
          new UnlabeledHashTreeStreamer(null, this.unlabeledHashTreePath);
      Iterator<UnlabeledHashTree> thisTreeIterator = thisTreeStreamer.iterator();
      while (thisTreeIterator.hasNext()) {
        hcBuilder.append(thisTreeIterator.next());
      }
    }

    if (this.acceptableConcepts != null) {
      hcBuilder.append(this.acceptableConcepts);
    }
    return hcBuilder.append(this.sequenceIDSolution).append(this.conceptMapSolution)
        .append(this.smoothingMethod).append(this.trainingTokenNumber).toHashCode();
  }


  /*
   * (non-Javadoc)
   *
   * @see src.main.gov.va.vha09.grecc.raptat.gg.candidates.coremodifications. solutions
   * .TokenSequenceFinderSolution#retrieveTokenProcessingOptions()
   */
  @Override
  public TokenProcessingOptions retrieveTokenProcessingOptions() {
    if (this.conceptMapSolution != null) {
      return this.conceptMapSolution.retrieveTokenProcessingOptions();
    }
    return null;
  }


  /**
   * Save the database
   *
   * @param solutionFile
   */
  public void saveToFile(File solutionFile) {
    ObjectOutputStream obj_out = null;

    try {
      // Write object with ObjectOutputStream
      obj_out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(solutionFile),
          RaptatConstants.DEFAULT_BUFFERED_STREAM_SIZE));

      // Write object out to disk
      obj_out.writeObject(this);

    } catch (FileNotFoundException e) {
      System.out.println("Unable to find solution file for writing");
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Problem writing solution file");
      e.printStackTrace();
    } finally {
      if (obj_out != null) {
        try {
          obj_out.flush();
          obj_out.close();
        } catch (IOException e) {
          System.out.println("Unable to close output stream " + "after writing to solution file");
          e.printStackTrace();
        }
      }
    }
  }


  /**
   * @param acceptableConcepts the acceptableConcepts to set
   */
  public void setAcceptableConcepts(HashSet<String> acceptableConcepts) {
    this.acceptableConcepts = acceptableConcepts;
  }


  /**
   * @param cmSolution
   */
  public void setConceptMapSolution(ConceptMapSolution cmSolution) {
    this.conceptMapSolution = cmSolution;
  }


  /**
   * @param sequenceIdentifier
   */
  public void setSequenceIdentifier(SequenceIDStructure sequenceIdentifier) {
    this.sequenceIDSolution = sequenceIdentifier;
  }


  /**
   * @param unlabeledHashTreePath the unlabeledHashTreePath to set
   */
  public void setUnlabeledHashTreePath(String unlabeledHashTreePath) {
    this.unlabeledHashTreePath = unlabeledHashTreePath;
  }


  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        ProbabilisticTSFinderSolution solution1 =
            (ProbabilisticTSFinderSolution) loadFromFile("Select first solution");
        ProbabilisticTSFinderSolution solution2 =
            (ProbabilisticTSFinderSolution) loadFromFile("Select second solution");

        ProbabilisticTSFinderSolution.logger.setLevel(Level.DEBUG);

        System.out.println("SequenceIDSolutions Equal:"
            + solution1.sequenceIDSolution.equals(solution2.sequenceIDSolution));

        if (ProbabilisticTSFinderSolution.logger.isDebugEnabled()) {
          solution1.sequenceIDSolution.showComparison(solution2.sequenceIDSolution);
        }

        if (solution1.acceptableConcepts != null && solution2.acceptableConcepts != null) {
          System.out.println("AcceptableConcepts Equal:"
              + solution1.acceptableConcepts.equals(solution2.acceptableConcepts));
        } else if (solution1.acceptableConcepts != null || solution2.acceptableConcepts != null) {
          System.out.println("AcceptableConcepts Equal: false");
        } else {
          System.out.println("AcceptableConcept both null");
        }

        System.out.println("ConceptMapSolutions Equal:"
            + solution1.conceptMapSolution.equals(solution2.conceptMapSolution));

        System.out.println("SmoothingMethods Equal:"
            + solution1.smoothingMethod.equals(solution2.smoothingMethod));

        System.out.println("TrainingTokenNumbers Equal:"
            + (solution1.trainingTokenNumber == solution2.trainingTokenNumber));

        System.exit(0);
      }
    });
  }
}
