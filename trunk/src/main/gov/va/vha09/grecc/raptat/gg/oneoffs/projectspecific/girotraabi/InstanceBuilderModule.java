package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import weka.core.Instances;

public abstract class InstanceBuilderModule {

	private Logger logger;
	private ModuleParameter parameter;

	protected InstanceBuilderModule(final ModuleParameter parameter) {
		this.logger = Logger.getLogger(InstanceBuilderModule.class.getName());
		parameter.validate();
		this.parameter = parameter;
	}

	public abstract Map<String, Map<PhraseClass, Instances>> buildMappedInstances();

	public abstract void close();

	public ModuleParameter getParameter() {
		return this.parameter;
	}

	// public abstract Map<String, Map<PhraseClass, Instances>>
	// processUpdated();

	public abstract ModuleResultBase process()
			throws FileNotFoundException, IOException;

	protected Logger getLogger() {
		return this.logger;
	}

}
