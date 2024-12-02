package src.main.gov.va.vha09.grecc.raptat.gg.weka.transformers;

import src.main.gov.va.vha09.grecc.raptat.gg.weka.datastructures.FeatureSet;

/**
 * Transformer Interface - Provides a general interface for transforming a set of annotations of a
 * text file into features for machine learning
 *
 * @author Glenn Gobbel, Dept. of Veterans Affairs
 * @date Jan 12, 2019
 */
public interface Transformer {

  public FeatureSet transform();
}
