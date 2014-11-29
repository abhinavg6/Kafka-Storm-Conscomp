package com.sapient.storm_meetup.singlenodestormtopology;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * A bolt to calculate the simple rolling statistics for a consumer complaint
 * type (product) and subtype (subproduct). The stats are (mean, standard
 * deviation, skewness, kurtosis) for the rate of increase of count of consumer
 * complaints per product and subproduct.It then emits the calculated stats for
 * further processing/storage.
 * 
 * @author abhinavg6
 *
 */
public class ConsCompStatBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 1L;

	private final Map<String, Map<String, Integer>> productCountMap = new HashMap<String, Map<String, Integer>>();
	private final Map<String, Map<String, DescriptiveStatistics>> productStatMap = new HashMap<String, Map<String, DescriptiveStatistics>>();
	private final Integer rollingWindowSize;

	public ConsCompStatBolt(Integer rollingWindowSize) {
		// Set the rolling window size for calculating stats
		this.rollingWindowSize = rollingWindowSize;
	}

	public void execute(Tuple input, BasicOutputCollector collector) {

		// Get the fields from the tuple
		String product = input.getStringByField("product");
		String subProduct = input.getStringByField("subproduct");
		Integer count = input.getIntegerByField("count");

		// Update the counts data
		Integer oldCount = updateCounts(product, subProduct, count);
		// Get the difference between old and new count
		double countDiff = count - oldCount;
		// Update the stats data
		DescriptiveStatistics stats = updateStats(product, subProduct,
				countDiff);

		if (stats != null) {
			// Emit the new tuple with rolling statistics
			collector.emit(new Values(product, subProduct, count, stats
					.getMean(), stats.getStandardDeviation(), stats
					.getSkewness(), stats.getKurtosis()));
		}
	}

	/**
	 * Update the counts data, and get the old count
	 * 
	 * @param product
	 * @param subProduct
	 * @param count
	 * @return
	 */
	private Integer updateCounts(String product, String subProduct,
			Integer count) {

		Integer oldCount = 0;
		if (productCountMap.containsKey(product)) {
			Map<String, Integer> subProductCountMap = productCountMap
					.get(product);
			if (subProductCountMap.containsKey(subProduct)) {
				oldCount = subProductCountMap.get(subProduct);
				// Add the new count to be used for next time
				subProductCountMap.put(subProduct, count);
				productCountMap.put(product, subProductCountMap);
			} else {
				// Add the new count to be used for next time
				subProductCountMap.put(subProduct, count);
				productCountMap.put(product, subProductCountMap);
			}
		} else {
			Map<String, Integer> subProductCountMap = new HashMap<String, Integer>();
			// Add the new count to be used for next time
			subProductCountMap.put(subProduct, count);
			productCountMap.put(product, subProductCountMap);
		}

		// Return the old count
		return oldCount;
	}

	/**
	 * Update/Create the stats data
	 * 
	 * @param product
	 * @param subProduct
	 * @param countDiff
	 */
	private DescriptiveStatistics updateStats(String product,
			String subProduct, double countDiff) {

		DescriptiveStatistics stats = null;
		if (productStatMap.containsKey(product)) {
			Map<String, DescriptiveStatistics> subProductStatMap = productStatMap
					.get(product);
			if (subProductStatMap.containsKey(subProduct)) {
				stats = subProductStatMap.get(subProduct);
				if (stats.getN() == rollingWindowSize) {
					// Clear the existing stats since number of data points are
					// equal to the window size
					stats.clear();
				}
				// Add the new value
				stats.addValue(countDiff);
				subProductStatMap.put(subProduct, stats);
				productStatMap.put(product, subProductStatMap);
			} else {
				stats = new DescriptiveStatistics();
				// Create new stats with fresh data
				stats.addValue(countDiff);
				subProductStatMap.put(subProduct, stats);
				productStatMap.put(product, subProductStatMap);
			}
		} else {
			Map<String, DescriptiveStatistics> subProductStatMap = new HashMap<String, DescriptiveStatistics>();
			stats = new DescriptiveStatistics();
			// Create new stats with fresh data
			stats.addValue(countDiff);
			subProductStatMap.put(subProduct, stats);
			productStatMap.put(product, subProductStatMap);
		}

		// Return the updated/fresh stats
		return stats;
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// emit the tuple with field "product", "subproduct" and "count"
		declarer.declare(new Fields("product", "subproduct", "count", "mean",
				"stddev", "skewness", "kurtosis"));
	}
}
