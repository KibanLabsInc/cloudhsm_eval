package io.afero.cloudhsm.v2.simulator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	public LoadSimulator(String provider, PrivateKey key, int concurrency, int count) {
		this.count = count;

		executorService = Executors.newFixedThreadPool(concurrency);
		signatureTimingSupplierFactory = new SignatureTimingSupplierFactory(provider, key);
	}

	@Override
	public Stats call() {
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
		private final PrivateKey key;
		private final String provider;

		private SignatureTimingSupplierFactory(String provider, PrivateKey key) {
			this.key = key;
			this.provider = provider;
		}

		Supplier<Long> create() {
			return new SignatureTimingSupplier(provider, key);
		}

		private class SignatureTimingSupplier implements Supplier<Long> {

			private String provider;
			private PrivateKey key;

			SignatureTimingSupplier(String provider, PrivateKey key) {
				this.provider = provider;
				this.key = key;
			}

			@Override
			public Long get() {
				byte[] bytes = new byte[32];
				random.nextBytes(bytes);

				long nanos = System.nanoTime();
				try {
					Signature signatureInstance = Signature.getInstance("NONEwithECDSA", provider);
					signatureInstance.initSign(key);

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
