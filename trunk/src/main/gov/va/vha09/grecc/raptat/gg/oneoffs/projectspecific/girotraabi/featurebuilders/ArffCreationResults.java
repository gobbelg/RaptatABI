package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.featurebuilders;

import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.ModuleResultBase;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.PostArffFileCompleted.PostArffFileResults;

public class ArffCreationResults extends ModuleResultBase {

	private PostArffFileResults postArffWriteResults;
	private String arffFilePath;

	public ArffCreationResults(PostArffFileResults postArffWriteResults,
			String arffFilePath) {
		super();
		this.postArffWriteResults = postArffWriteResults;
		this.arffFilePath = arffFilePath;
	}

	public String getArffFilePath() {
		return this.arffFilePath;
	}

	public PostArffFileResults getPostArffWriteResults() {
		return this.postArffWriteResults;
	}

}
