package io.afero.cloudhsm.v2.keystore;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;

import static java.lang.Class.forName;

/**
 * @author nrheckman 8/29/18 1:15 PM
 */
public class KeyStoreFactory {

	public KeyStore create(String provider, String credentials) throws Exception {
		KeyStoreSupplier supplier;
		switch (provider) {
			case "cavium":
				supplier = new CaviumKeyStoreSupplier(credentials);
				break;
			case "luna":
				supplier = new LunaKeyStoreSupplier(credentials);
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
		private final String credentials;

		private CaviumKeyStoreSupplier(String credentials) {
			this.credentials = credentials;
		}

		@Override
		public KeyStore get() throws Exception {
			String[] credentialParts = credentials.split(":");
			System.setProperty("HSM_PARTITION", "PARTITION_1");
			System.setProperty("HSM_USER", credentialParts[0]);
			System.setProperty("HSM_PASSWORD", credentialParts[1]);

			Class<?> caviumProviderClass = forName("com.cavium.provider.CaviumProvider");
			Provider provider = (Provider)caviumProviderClass.newInstance();
			java.security.Security.addProvider(provider);

			KeyStore keyStore = KeyStore.getInstance("cavium", provider);
			keyStore.load(null, credentials.toCharArray());

			return keyStore;
		}
	}

	private static class LunaKeyStoreSupplier implements KeyStoreSupplier {
		private final String credentials;

		public LunaKeyStoreSupplier(String credentials) {
			this.credentials = credentials;
		}

		@Override
		public KeyStore get() throws Exception {
			Class<?> lunaProviderClass = Class.forName("com.safenetinc.luna.provider.LunaProvider");
			Provider provider = (Provider)lunaProviderClass.newInstance();
			java.security.Security.addProvider(provider);
			byte[] bytes;
			KeyStore keyStore;
			try (InputStream is = KeyStoreFactory.class.getClassLoader().getResourceAsStream("keystore.luna")) {
				keyStore = KeyStore.getInstance("Luna", provider);
				keyStore.load(is, credentials.toCharArray());
			}

			return keyStore;
		}
	}
}
