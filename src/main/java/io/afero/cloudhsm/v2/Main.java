package io.afero.cloudhsm.v2;

import io.afero.cloudhsm.v2.keystore.KeyStoreFactory;
import io.afero.cloudhsm.v2.simulator.LoadSimulator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author nrheckman 8/29/18 10:34 AM
 */
public class Main {
	private static final Logger LOG = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		LOG.info("Application started with arguments, " + Arrays.toString(args));

		if (args.length < 3) {
			LOG.error("Expected application arguments <provider> <keyAlias>, <concurrency>, <count> were not found. " +
					"If running from gradle, use --args '<provider> <keyAlias> <concurrency> <count>'");
			System.exit(1);
		}

		String provider = args[0];
		String keyAlias = args[1];
		int concurrency = Integer.parseInt(args[2]);
		int count = Integer.parseInt(args[3]);

		Scanner scanner = new Scanner(System.in);
		System.out.print("Key Alias credentials? ");
		String credentials = scanner.nextLine();

		KeyStoreFactory keyStoreFactory = new KeyStoreFactory();

		Runnable simulator = null;
		try {
			simulator = new LoadSimulator(
					keyStoreFactory.create(provider, credentials),
					credentials, keyAlias, concurrency, count);
		} catch (Exception e) {
			LOG.fatal("Unable to instantiate simulator", e);
			System.exit(1);
		}

		long simulatorStartNanos = System.nanoTime();
		simulator.run();

		LOG.debug("Simulator total execution ms: " +
				TimeUnit.MILLISECONDS.convert(
						System.nanoTime() - simulatorStartNanos,
						TimeUnit.NANOSECONDS));
	}
}
