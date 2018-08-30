package io.afero.cloudhsm.v2.simulator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author nrheckman 8/29/18 11:13 AM
 */
public class LoadSimulator implements Callable<Stats> {
	private static final Logger LOG = LogManager.getLogger(LoadSimulator.class);

	private final int count;
	private final ExecutorService executorService;
	private final SignatureTimingSupplierFactory signatureTimingSupplierFactory;

	public LoadSimulator(KeyStore keyStore, String credentials, String keyAlias, int concurrency, int count) throws Exception {
		this.count = count;

		executorService = Executors.newFixedThreadPool(concurrency);
		signatureTimingSupplierFactory = new SignatureTimingSupplierFactory(keyStore, keyAlias, credentials);
	}

	@Override
	public Stats call() throws Exception {
		LOG.debug("Starting simulator for " + count + " signing requests");

		DescriptiveStatistics statistics = new DescriptiveStatistics();

		Consumer<Long> timingConsumer = (nanos) -> {
			synchronized (statistics) {
				statistics.addValue(nanos);
			}
		};

		for (int i = 0; i < count; i++) {
			CompletableFuture.supplyAsync(signatureTimingSupplierFactory.create(), executorService)
					.thenAccept(timingConsumer);
		}

		try {
			executorService.shutdown();
			executorService.awaitTermination(600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new CompletionException(e);
		}

		return new Stats(
				TimeUnit.MILLISECONDS.convert(Double.valueOf(statistics.getMax()).longValue(), TimeUnit.NANOSECONDS),
				TimeUnit.MILLISECONDS.convert(Double.valueOf(statistics.getMin()).longValue(), TimeUnit.NANOSECONDS),
				TimeUnit.MILLISECONDS.convert(Double.valueOf(statistics.getMean()).longValue(), TimeUnit.NANOSECONDS));
	}

	private static class SignatureTimingSupplierFactory {
		private static final Random random = new SecureRandom();
		private final KeyStore keyStore;
		private final String keyAlias;
		private final String credentials;

		private SignatureTimingSupplierFactory(KeyStore keyStore, String keyAlias, String credentials) {
			this.keyStore = keyStore;
			this.keyAlias = keyAlias;
			this.credentials = credentials;
		}

		Supplier<Long> create() {
			return new SignatureTimingSupplier();
		}

		private class SignatureTimingSupplier implements Supplier<Long> {

			@Override
			public Long get() {
				byte[] bytes = new byte[32];
				random.nextBytes(bytes);

				long nanos = System.nanoTime();
				try {
					String provider = keyStore.getProvider().getName();
					Signature signatureInstance = Signature.getInstance("NONEwithECDSA", provider);
					signatureInstance.initSign((PrivateKey)keyStore.getKey(keyAlias, credentials.toCharArray()));

					signatureInstance.update(bytes);
					signatureInstance.sign();
				} catch (Exception e) {
					LOG.error("Sign failed", e);
					System.exit(1);
				}

				return System.nanoTime() - nanos;
			}
		}

	}
}
