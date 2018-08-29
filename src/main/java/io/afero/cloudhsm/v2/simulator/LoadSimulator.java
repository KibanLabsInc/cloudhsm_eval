package io.afero.cloudhsm.v2.simulator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.KeyStore;
import java.security.Provider;

import static java.lang.Class.forName;

/**
 * @author nrheckman 8/29/18 11:13 AM
 */
public class LoadSimulator implements Runnable {
	private static final Logger LOG = LogManager.getLogger(LoadSimulator.class);

	private final String keyAlias;
	private final int concurrency;
	private final int count;
	private final KeyStore keyStore;

	public LoadSimulator(String user, String password, String keyAlias, int concurrency, int count) throws Exception {
		this.keyAlias = keyAlias;
		this.concurrency = concurrency;
		this.count = count;

		keyStore = createKeyStore(user, password);
	}

	@Override
	public void run() {

	}

	private KeyStore createKeyStore(String user, String password) throws Exception {
		LOG.trace("Creating KeyStore backed by Cavium provider");

		System.setProperty("HSM_PARTITION", "PARTITION_1");
		System.setProperty("HSM_USER", user);
		System.setProperty("HSM_PASSWORD", password);

		Class<?> caviumProviderClass = forName("com.cavium.provider.CaviumProvider");
		Provider provider = (Provider)caviumProviderClass.newInstance();
		java.security.Security.addProvider(provider);

		KeyStore keyStore = KeyStore.getInstance("cavium", provider);
		keyStore.load(null, (user + ":" + password).toCharArray());

		return keyStore;
	}
}
