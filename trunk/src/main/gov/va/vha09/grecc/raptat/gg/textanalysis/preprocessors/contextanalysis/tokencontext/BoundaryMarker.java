package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext;

import java.io.Serializable;
import java.util.List;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * An interface for classes that determine the boundaries of context within an ordered set of
 * strings, such as those representing a sentence or phrase. For example, for determining negation
 * context, a negation boundary marker will mark those tokens that 1) indicate that the tokens after
 * it should be negated ("pre" marker), 2) that tokens before it should be negated ("post" marker),
 * 3) that it is not a real negating term ("pseudo" marker)), or 4) that it terminates a set of
 * tokens that were negated by a "pre" or "post" marker ("term" marker).
 *
 * @author Glenn Gobbel
 */
public interface BoundaryMarker extends Serializable {

  public RaptatPair<Mark[], Boolean> markContextBoundaries(List<RaptatToken> theTokens);


  public RaptatPair<Mark[], Boolean> markContextBoundaries(String[] tokenStrings);
}
