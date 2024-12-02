package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class TokenizerFactory {
	// private static int tokenizerUsers = 0;
	private static Tokenizer tokenizerInstance = null;

	private TokenizerFactory() {
	}

	public static synchronized Tokenizer getInstance() {
		// tokenizerUsers++ ;
		if ( TokenizerFactory.tokenizerInstance == null ) {
			createInstance();
		}
		return TokenizerFactory.tokenizerInstance;
	}

	private static void createInstance() {
		if ( TokenizerFactory.tokenizerInstance == null ) {
			TokenizerModel theModel;

			try (InputStream modelIn = TokenizerFactory.class
					.getResourceAsStream("en-token.bin")) {
				System.out.println("Loading tokenizer model");

				if ( (theModel = new TokenizerModel(modelIn)) != null ) {
					TokenizerFactory.tokenizerInstance = new TokenizerME(
							theModel);
				}
			}
			catch (IOException e) {
				System.out.println("Unable to load tokenizer model\n" + e);
				e.printStackTrace();
			}
		}
	}

	// public static void release()
	// {
	// tokenizerUsers-- ;
	// if ( tokenizerUsers < 1 )
	// {
	// tokenizerUsers = 0;
	// tokenizerInstance = null;
	// }
	// }

}
