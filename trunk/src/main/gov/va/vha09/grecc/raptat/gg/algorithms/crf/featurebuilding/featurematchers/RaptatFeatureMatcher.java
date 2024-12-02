package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.featurematchers;

import java.io.Serializable;

public interface RaptatFeatureMatcher extends Serializable {
  public boolean matches(String inputString);
}
