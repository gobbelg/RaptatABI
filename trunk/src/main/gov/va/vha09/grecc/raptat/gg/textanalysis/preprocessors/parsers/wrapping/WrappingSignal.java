/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.parsers.wrapping;

import java.io.Serializable;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.FSInput;

/**
 * Defines baseline forms based on regular expression matches to determine when and how to convert
 * text from wrapped to unwrapped using various classes and algorithms
 *
 * @author VHATVHGOBBEG
 *
 */
public interface WrappingSignal extends Serializable {

  public enum WrappingInput implements FSInput, WrappingSignal {
    STOP_WRAPPING, START_WRAPPING, CONTINUE_WRAPPING;
  }

}
