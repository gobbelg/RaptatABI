package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import java.util.ArrayList;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedObject;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.IndexedTree;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/**
 * ****************************************************** OffsetConverter interface implementation
 * that converts beginning and end character offsets from UTf-8 for OpenNLP to eHOST (or Knowtator
 * XML)
 *
 * @author Glenn Gobbel - Oct 21, 2013 *****************************************************
 */
public class UTFToAnnotatorOffsetConverter implements OffsetConverter {
  IndexedTree<IndexedObject<Integer>> offsetCorrections = null;


  /**
   * ********************************************
   *
   * @param pathToSource
   * @author Glenn Gobbel - Oct 21, 2013
   * @param annotationSource ********************************************
   */
  public UTFToAnnotatorOffsetConverter(String pathToSource, AnnotationApp annotationSource) {
    this.offsetCorrections = getOffsetCorrections(pathToSource, annotationSource);
  }


  private UTFToAnnotatorOffsetConverter() {}


  /**
   * *********************************************************** The method changes the actual
   * offsets within the phrases passed to the method.
   *
   * @param allAnnData
   * @author Glenn Gobbel - Jun 21, 2012 ***********************************************************
   */
  @Override
  public void convertAnnotatedPhraseOffsetList(List<AnnotatedPhrase> allAnnData) {
    Integer curOffset;
    IndexedObject<Integer> theCorrection;

    for (AnnotatedPhrase curAnnotation : allAnnData) {
      curOffset = Integer.parseInt(curAnnotation.getRawTokensStartOff());
      theCorrection = this.offsetCorrections.getClosestLesserOrEqualObject(curOffset);
      if (theCorrection != null) {
        curOffset += theCorrection.getIndexedObject();
        curAnnotation.setRawStartOff(String.valueOf(curOffset));
      }

      curOffset = Integer.parseInt(curAnnotation.getRawTokensEndOff());
      theCorrection = this.offsetCorrections.getClosestLesserOrEqualObject(curOffset);
      if (theCorrection != null) {
        curOffset += theCorrection.getIndexedObject();
        curAnnotation.setRawEndOff(String.valueOf(curOffset));
      }
    }
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD Take a
   * begin and end offset as integers and return a pair of Integer objects with the left object
   * being the converted begin offset and the right object being the converted end offset.
   *
   * @param begin
   * @param end
   * @return
   * @author Glenn Gobbel - Oct 21, 2013 ***********************************************************
   */
  @Override
  public RaptatPair<Integer, Integer> convertBeginAndEndOffsets(int begin, int end) {
    IndexedObject<Integer> theCorrection;

    theCorrection = this.offsetCorrections.getClosestLesserOrEqualObject(begin);
    if (theCorrection != null) {
      begin += theCorrection.getIndexedObject();
    }

    theCorrection = this.offsetCorrections.getClosestLesserOrEqualObject(end);
    if (theCorrection != null) {
      end += theCorrection.getIndexedObject();
    }
    return new RaptatPair<>(begin, end);
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD Method to
   * take a list of offsets, provided in pairs with the left element of the pair the beginning
   * offset and the right element of the pair the end offset, and return another list with the
   * convertted offsets provided in pairs in the same order as the input list.
   *
   * @param offsetList
   * @return List<Pair<Integer, Integer>> correctedOffsets
   * @author Glenn Gobbel - Oct 21, 2013 ***********************************************************
   */
  @Override
  public List<RaptatPair<Integer, Integer>> convertOffsetList(
      List<RaptatPair<Integer, Integer>> offsetList) {
    List<RaptatPair<Integer, Integer>> converted = new ArrayList<>(offsetList.size());

    for (RaptatPair<Integer, Integer> curOffset : offsetList) {
      converted.add(convertOffsetPair(curOffset));
    }
    return converted;
  }


  /**
   * *********************************************************** OVERRIDES PARENT METHOD Take a
   * pair, where left element = beginning offset and right element = end offset, and return another
   * pair with the left element giving the converted begin offset and the right element giving the
   * converted end offset.
   *
   * @param offsetsList
   * @return Pair<Integer,Integer> corrected
   * @author Glenn Gobbel - Oct 21, 2013 ***********************************************************
   */
  @Override
  public RaptatPair<Integer, Integer> convertOffsetPair(RaptatPair<Integer, Integer> theOffsets) {
    return convertBeginAndEndOffsets(theOffsets.left, theOffsets.right);
  }


  /**
   * *********************************************************** Determine the offsets needed to
   * generate the proper start and end offsets for an AnnotationApp (e.g. Knowtator or eHost) based
   * on a document that was analyzed by OpenNLP assuming UTF-8 type
   *
   * @return IndexedTree<IndexedObject<Integer>> used for correcting offsets
   * @author Glenn Gobbel -Oct 21, 2013
   * @param annotationSource ***********************************************************
   */
  private IndexedTree<IndexedObject<Integer>> getOffsetCorrections(String pathToOriginalTextSource,
      AnnotationApp annotationSource) {
    IndexedTree<IndexedObject<Integer>> offsetComparer =
        OffsetParser.calculateOffsetCorrectionsNew(pathToOriginalTextSource, annotationSource);
    List<IndexedObject<Integer>> offsets = offsetComparer.getAllNodes();
    List<IndexedObject<Integer>> invertedOffsets = new ArrayList<>(offsets.size());

    for (IndexedObject<Integer> curOffset : offsets) {
      int offsetValue = curOffset.getIndexedObject();
      int invertedOffsetIndex = curOffset.getIndex() + offsetValue;
      invertedOffsets.add(new IndexedObject<>(invertedOffsetIndex, -offsetValue));
    }

    return new IndexedTree<>(invertedOffsets);
  }
}
