package src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.hashtrees.UnlabeledHashTree;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

public class AnnotationGroup implements Serializable {
  private RaptatDocument raptatDocument;

  public List<AnnotatedPhrase> referenceAnnotations;

  /*
   * the activeUnabeledPhrases are those that correspond to the activeSentences in the
   * raptatDocument field
   */
  private UnlabeledHashTree activeUnlabeledPhrases;

  /*
   * the baseUnabeledPhrases are those that correspond to the allSentences in the raptatDocument
   * field
   */
  private UnlabeledHashTree baseUnlabeledPhrases;

  /*
   * Unlabeled phrases based on the filtered sentence set of the filteredSentences with the
   * raptatDocument field. This is needed when training the system using only a subset of all the
   * sentences from a document.
   */
  private UnlabeledHashTree filteredUnlabeledPhrases;

  public List<AnnotatedPhrase> raptatAnnotations;

  public List<String[]> contextPhrases = new ArrayList<>();

  // These are the original phrases that were split during processing and
  // removed from the list of reference annotated phrases.
  // It contains the processed tokens
  private Hashtable<Integer, AnnotatedPhrase> splitPhrases;
  private int maxTokens = 4;
  // This boolean keeps track of whether the number of tokens
  // per phrase in the reference annotations corresponds to
  // the value of maxTokens. It is set ot false whenever
  // setSplitPhrases(), setMaxTokens(), or restoreSplitPhrases()
  // is called
  private boolean referenceAnnotationsSetToMax = false;

  private static final long serialVersionUID = -1055416906762079533L;


  public AnnotationGroup(RaptatDocument theDocument, List<AnnotatedPhrase> theAnnotations) {
    this.raptatDocument = theDocument;

    if (theAnnotations != null) {
      this.referenceAnnotations = new ArrayList<>(theAnnotations);
    } else {
      this.referenceAnnotations = new ArrayList<>();
    }
  }


  public void activateBaseSentences() {
    this.raptatDocument.activateBaseSentences();
    this.activeUnlabeledPhrases = this.baseUnlabeledPhrases;
  }


  public void activateFilteredSentences() throws Exception {
    this.raptatDocument.activateFilteredSentences();
    this.activeUnlabeledPhrases = this.filteredUnlabeledPhrases;
  }


  public void addContextPhrase(String[] contextStrings) {
    if (this.contextPhrases == null) {
      this.contextPhrases = new ArrayList<>(10);
    }
    this.contextPhrases.add(contextStrings);
  }


  public void addReferenceAnnotation(AnnotatedPhrase annotatedPhrase) {
    if (this.referenceAnnotations == null) {
      this.referenceAnnotations = new ArrayList<>(10);
    }
    this.referenceAnnotations.add(annotatedPhrase);
  }


  public void createFilteredSentences(Collection<SentenceFilter> sentenceFilters,
      List<AnnotatedPhrase> annotations) {
    this.raptatDocument.createFilteredSentences(sentenceFilters, annotations);
  }


  public void createFilteredSentences(SentenceFilter sentenceFilter,
      List<AnnotatedPhrase> annotations) {
    this.raptatDocument.createFilteredSentences(sentenceFilter, annotations);
  }


  public UnlabeledHashTree getActiveUnlabeledPhrases() {
    return this.activeUnlabeledPhrases;
  }


  public int getMaxTokens() {
    return this.maxTokens;
  }


  public RaptatDocument getRaptatDocument() {
    return this.raptatDocument;
  }


  /** @return the referenceAnnotationsSetToMax */
  public boolean isReferenceAnnotationsSetToMax() {
    return this.referenceAnnotationsSetToMax;
  }


  public void printBaseAnnotations() {
    String docPath = this.raptatDocument.getTextSourcePath().get();
    String doc = docPath.substring(docPath.lastIndexOf("\\") + 1);
    System.out.println("Document:" + doc);

    for (AnnotatedPhrase curPhrase : this.referenceAnnotations) {
      System.out.println(curPhrase.toString());
    }
  }


  public void printRaptatAnnotations() {
    String docPath = this.raptatDocument.getTextSourcePath().get();
    String doc = docPath.substring(docPath.lastIndexOf("\\") + 1);
    System.out.println("Document:" + doc);
    for (AnnotatedPhrase curPhrase : this.raptatAnnotations) {
      System.out.println(curPhrase.toString());
    }
  }


  /**
   * *********************************************************** Replace phrases that have been
   * split during processing because they were longer than the maximum number of tokens allowed for
   * training with the original phrases.
   *
   * @author Glenn Gobbel - Jul 15, 2013 ***********************************************************
   */
  public void restoreSplitPhrases() {
    if (this.splitPhrases != null && this.splitPhrases.size() > 0) {
      ListIterator<AnnotatedPhrase> theIterator = this.referenceAnnotations.listIterator();
      int lastIndex = -1;

      while (theIterator.hasNext()) {
        int curIndex = theIterator.next().getIndexInDocument();
        if (this.splitPhrases.containsKey(curIndex)) {
          if (curIndex == lastIndex) {
            theIterator.remove();
          } else {
            theIterator.set(this.splitPhrases.get(curIndex));
            lastIndex = curIndex;
          }
        }
      }
      this.splitPhrases.clear();
    }
    this.referenceAnnotationsSetToMax = false;
  }


  public void setActiveUnlabeledPhrases(UnlabeledHashTree theTree) {
    this.activeUnlabeledPhrases = theTree;
  }


  public void setBaseAnnotations(List<AnnotatedPhrase> theAnnotations) {
    this.referenceAnnotations = theAnnotations;
  }


  public void setBaseUnlabeledPhrases(UnlabeledHashTree theTree) {
    this.baseUnlabeledPhrases = theTree;
  }


  public void setFilteredUnlabeledPhrases(UnlabeledHashTree theTree) {
    this.filteredUnlabeledPhrases = theTree;
  }


  public void setMaxTokens(int maxTokensPerAnnotations) {
    this.referenceAnnotationsSetToMax = false;
    this.maxTokens = maxTokensPerAnnotations;
  }


  public void setRaptatAnnotations(List<AnnotatedPhrase> theAnnotations) {
    if (theAnnotations != null) {
      this.raptatAnnotations = theAnnotations;
    } else {
      this.raptatAnnotations = new ArrayList<>();
    }
  }


  public void setRaptatDocument(RaptatDocument document) {
    this.raptatDocument = document;
  }


  /**
   * @param referenceAnnotationsSetToMax the referenceAnnotationsSetToMax to set
   */
  public void setReferenceAnnotationsSetToMax(boolean referenceAnnotationsSetToMax) {
    this.referenceAnnotationsSetToMax = referenceAnnotationsSetToMax;
  }


  public void setSplitPhrases(Hashtable<Integer, AnnotatedPhrase> splitPhrases) {
    this.splitPhrases = splitPhrases;
  }
}
