package io.afero.cloudhsm.v2.simulator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author nrheckman 8/29/18 11:13 AM
 */
public class LoadSimulator implements Runnable {
	private static final Logger LOG = LogManager.getLogger(LoadSimulator.class);

	private final String keyAlias;
	private final int rate;
	private final int count;

	public LoadSimulator(String user, String password, String keyAlias, int rate, int count) {
		this.keyAlias = keyAlias;
		this.rate = rate;
		this.count = count;
	}

	@Override
	public void run() {

	}
}
