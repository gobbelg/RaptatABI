package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.metafeatures;

import java.util.SortedSet;

import javax.swing.SwingUtilities;

import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.RaptatTokenPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PhraseClass;

/**
 * AnnotationToTokenPhraseComparator - Determines the similarity of a
 * RaptatTokenPhrase object to an AnnotatedPhrase. Similarity is based first on
 * whether the name of the label of the RaptatTokenPhrase is the same as the
 * conceptName field of the AnnotatedPhrase when doing a case insensitive string
 * comparison. If they are not the same, similarity is minimal. If they are the
 * same, similarity is calculated based on the degree of overlap, where perfect
 * overlap is given a value of 1.
 *
 * @author Glenn Gobbel
 * @date May 27, 2019
 */
public class AnnotationToTokenPhraseComparator {

	private enum RunType {
		TEST_RUN;
	}

	public static int getSimilarityScore(final RaptatTokenPhrase tokenPhrase,
			final AnnotatedPhrase annotatedPhrase) {
		int result = Integer.MIN_VALUE;

		try {
			for (RaptatTokenPhrase.Label tokenPhraseLabel : tokenPhrase
					.getPhraseLabels()) {

				/*
				 * Determine which phrase class has a concept name equal to the
				 * name of a label of the tokenPhrase parameter
				 */
				PhraseClass foundClass = null;
				for (PhraseClass phraseClass : PhraseClass.values()) {

					if ( phraseClass.conceptName.equalsIgnoreCase(
							tokenPhraseLabel.getCategory()) ) {
						foundClass = phraseClass;
						String annotatedPhraseConcept = annotatedPhrase
								.getConceptName().toLowerCase();
						SortedSet<String> foundClassifications = foundClass
								.getClassifications();
						if ( foundClassifications
								.contains(annotatedPhraseConcept) ) {
							result = 0;
							break;
						}
					}
				}
			}
			/*
			 * If we have run through all tokenPhrase labels and values of the
			 * PhraseClass and have found nothing (foundClass == null), throw an
			 * error
			 */
		}
		catch (IllegalArgumentException exception) {
			System.err.println(
					"Illegal RaptatTokenPhrase label.\nRaptatTokenPhrase label name does not match an existing PhraseClass value\n"
							+ exception.getLocalizedMessage());
			exception.printStackTrace();
		}

		if ( result < 0 ) {
			return result;
		}

		int annotationStart = Integer
				.parseInt(annotatedPhrase.getRawTokensStartOff());
		int annotationEnd = Integer
				.parseInt(annotatedPhrase.getRawTokensEndOff());
		int tokenStart = tokenPhrase.getStartOffsetAsInt();
		int tokenEnd = tokenPhrase.getEndOffsetAsInt();

		/* Check if tokenPhrase and annotatedPhrase overlap */
		if ( (tokenEnd > annotationStart) && (tokenStart < annotationEnd) ) {

			double tokenOverlapFraction = (double) (tokenEnd - annotationStart)
					/ (tokenEnd - tokenStart);
			double annotationOverlapFraction = (double) (annotationEnd
					- tokenStart) / (annotationEnd - annotationStart);

			result = (int) Math.round(tokenOverlapFraction
					* annotationOverlapFraction * (Integer.MAX_VALUE - 1));
		}

		return result;
	}

	public static void main(final String[] args) {

		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RunType runType = RunType.TEST_RUN;

				switch (runType) {
				case TEST_RUN:
					break;
				default:
					break;
				}
				System.exit(0);
			}
		});
	}
}
