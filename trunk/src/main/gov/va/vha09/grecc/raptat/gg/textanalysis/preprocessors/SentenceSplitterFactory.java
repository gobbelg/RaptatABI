package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceSplitterFactory {
	// private static int splitterUsers = 0;
	private static SentenceDetector splitterInstance = null;

	private SentenceSplitterFactory() {
	}

	public static SentenceDetector getInstance() {
		// splitterUsers++ ;
		if ( SentenceSplitterFactory.splitterInstance == null ) {
			createInstance();
		}
		return SentenceSplitterFactory.splitterInstance;
	}

	private static void createInstance() {
		if ( SentenceSplitterFactory.splitterInstance == null ) {
			SentenceModel theModel = null;

			try (InputStream modelIn = SentenceSplitterFactory.class
					.getResourceAsStream("en-sent.bin")) {
				System.out.println("Loading sentence splitter model");
				theModel = new SentenceModel(modelIn);
				theModel.useTokenEnd();

				if ( theModel != null ) {
					SentenceSplitterFactory.splitterInstance = new SentenceDetectorME(
							theModel);
				}
			}
			catch (IOException e) {
				System.out.println(
						"Unable to load sentence splitter model\n" + e);
				e.printStackTrace();
			}
		}
	}
}
