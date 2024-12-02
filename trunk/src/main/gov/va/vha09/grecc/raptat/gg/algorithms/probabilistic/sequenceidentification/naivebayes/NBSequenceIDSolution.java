package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.naivebayes;

import java.util.Hashtable;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.conceptmapping.naivebayes.NBPriorTable;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.AnnotationConditionalTable;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.StrPairSet;

public class NBSequenceIDSolution {

  private int numOfAttributes;
  private AnnotationConditionalTable[] conditionals;
  private int[] vocabSize;
  private StrPairSet[] modifiedElements;
  private NBPriorTable priors;
  private Hashtable<String, Boolean> modifiedConcepts;


  public NBSequenceIDSolution(int maxTokens) {
    // First element is document number, last element is just concepts
    // Other data are the attributes associated with the concepts
    this.numOfAttributes = maxTokens;
    this.conditionals = new AnnotationConditionalTable[maxTokens];
    this.vocabSize = new int[maxTokens];
    this.modifiedElements = new StrPairSet[maxTokens];

    for (int i = 0; i < maxTokens; i++) {
      this.vocabSize[i] = 0;
      this.conditionals[i] =
          new AnnotationConditionalTable(RaptatConstants.NB_UNIQUE_TOKEN_TABLE_SIZE);

      // This element (modifiedMappings[i] could be smaller if we are
      // updating after every document. Needs to be larger if we only
      // update after reading in all data.
      this.modifiedElements[i] = new StrPairSet(RaptatConstants.NB_UNIQUE_TOKEN_CONCEPT_PAIRS);
    }

    this.priors = new NBPriorTable(RaptatConstants.NB_CONCEPT_TABLE_SIZE, maxTokens);
    this.modifiedConcepts = new Hashtable<>(RaptatConstants.NB_CONCEPT_TABLE_SIZE);
  }
}
