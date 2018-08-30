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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author nrheckman 8/29/18 11:13 AM
 */
public class LoadSimulator implements Runnable {
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
	public void run() {
		LOG.debug("Starting simulator for " + count + " signing requests");

		AtomicInteger completeCounter = new AtomicInteger(0);
		DescriptiveStatistics statistics = new DescriptiveStatistics();

		Consumer<Long> timingConsumer = (nanos) -> {
			synchronized (statistics) {
				statistics.addValue(nanos);
				if (completeCounter.getAndIncrement() % 100 == 0) {
					printStats(statistics);
				}
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

		printStats(statistics);
	}

	private void printStats(DescriptiveStatistics statistics) {
		long maxNanos = Double.valueOf(statistics.getMax()).longValue();
		long minNanos = Double.valueOf(statistics.getMin()).longValue();
		long meanNanos = Double.valueOf(statistics.getMean()).longValue();
		LOG.trace("Max: " + TimeUnit.MILLISECONDS.convert(maxNanos, TimeUnit.NANOSECONDS) + ", " +
				"Min: " + TimeUnit.MILLISECONDS.convert(minNanos, TimeUnit.NANOSECONDS) + ", " +
				"Mean: " + TimeUnit.MILLISECONDS.convert(meanNanos, TimeUnit.NANOSECONDS));
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
					Signature signatureInstance = Signature.getInstance("NONEwithECDSA");
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
