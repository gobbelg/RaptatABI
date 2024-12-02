package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;

public class POSTaggerFactory {
	private static POSTagger taggerInstance = null;

	private POSTaggerFactory() {
	}

	public static synchronized POSTagger getInstance() {
		if ( POSTaggerFactory.taggerInstance == null ) {
			createInstance();
		}
		return POSTaggerFactory.taggerInstance;
	}

	private static void createInstance() {
		if ( POSTaggerFactory.taggerInstance == null ) {
			POSModel theModel = null;

			try (InputStream modelIn = POSTaggerFactory.class
					.getResourceAsStream(
							RaptatConstants.TAGGER_MODEL_FILE_NAME)) {

				System.out.println("Loading part-of-speech tagger model");
				theModel = new POSModel(modelIn);

				if ( theModel != null ) {
					POSTaggerFactory.taggerInstance = new POSTaggerME(theModel);
				}
			}
			catch (IOException e) {
				System.out.println("Unable to load POS tagger\n" + e);
				e.printStackTrace();
			}
		}
	}
}
