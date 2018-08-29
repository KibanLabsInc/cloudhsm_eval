package io.afero.cloudhsm.v2.keystore;

import java.security.KeyStore;
import java.security.Provider;

import static java.lang.Class.forName;

/**
 * @author nrheckman 8/29/18 1:15 PM
 */
public class KeyStoreFactory {

	public KeyStore create(String provider, String user, String password) throws Exception {
		KeyStoreSupplier supplier;
		switch (provider) {
			case "cavium":
				supplier = new CaviumKeyStoreSupplier(user, password);
				break;
			default:
				throw new IllegalArgumentException("Unknown provider '" + provider + "'");
		}

		return supplier.get();
	}

	private interface KeyStoreSupplier {
		KeyStore get() throws Exception;
	}

	private static class CaviumKeyStoreSupplier implements KeyStoreSupplier {
		private final String user;
		private final String password;

		private CaviumKeyStoreSupplier(String user, String password) {
			this.user = user;
			this.password = password;
		}

		@Override
		public KeyStore get() throws Exception {
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
}
