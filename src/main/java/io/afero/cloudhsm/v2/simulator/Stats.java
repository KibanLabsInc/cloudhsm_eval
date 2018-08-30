package io.afero.cloudhsm.v2.simulator;

/**
 * @author nrheckman 8/30/18 12:02 PM
 */
public class Stats {
	private long max;
	private long min;
	private long mean;

	public Stats(long max, long min, long mean) {
		this.max = max;
		this.min = min;
		this.mean = mean;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public long getMean() {
		return mean;
	}
}
