package com.sapient.storm_meetup.singlenodestormtopology;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * A bolt to count the number of events per consumer complaint type (product)
 * and subtype (subproduct). It then emits the product, subproduct and count for
 * further processing, after a periodic tick time (specified by
 * Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS).
 * 
 * @author abhinavg6
 *
 */
public class ConsCompCountBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 1L;

	private final Map<String, Map<String, Integer>> productCountMap = new HashMap<String, Map<String, Integer>>();
	private final Integer countTickSeconds;

	public ConsCompCountBolt(Integer countTickSeconds) {
		// Set the tick period to emit the tuples
		this.countTickSeconds = countTickSeconds;
	}

	public void execute(Tuple input, BasicOutputCollector collector) {

		if (input.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
				&& input.getSourceStreamId().equals(
						Constants.SYSTEM_TICK_STREAM_ID)) {
			sendTuples(collector);
		} else {
			updateCounts(input);
		}

	}

	/**
	 * Update the counts per product and subproduct
	 * 
	 * @param input
	 */
	private void updateCounts(Tuple input) {

		// Increment count per product and subproduct
		String product = input.getStringByField("product");
		String subProduct = input.getStringByField("subproduct");
		if (productCountMap.containsKey(product)) {
			Map<String, Integer> subProductCountMap = productCountMap
					.get(product);
			if (subProductCountMap.containsKey(subProduct)) {
				Integer count = subProductCountMap.get(subProduct);
				count++;
				subProductCountMap.put(subProduct, count);
				productCountMap.put(product, subProductCountMap);
			} else {
				subProductCountMap.put(subProduct, 1);
				productCountMap.put(product, subProductCountMap);
			}
		} else {
			Map<String, Integer> subProductCountMap = new HashMap<String, Integer>();
			subProductCountMap.put(subProduct, 1);
			productCountMap.put(product, subProductCountMap);
		}
	}

	/**
	 * Emit the tuples
	 * 
	 * @param collector
	 */
	private void sendTuples(BasicOutputCollector collector) {

		for (String product : productCountMap.keySet()) {
			Map<String, Integer> subProductCountMap = productCountMap
					.get(product);
			for (String subProduct : subProductCountMap.keySet()) {
				collector.emit(new Values(product, subProduct,
						subProductCountMap.get(subProduct)));
			}
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// emit the tuple with field "product", "subproduct" and "count"
		declarer.declare(new Fields("product", "subproduct", "count"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, countTickSeconds);
		return conf;
	}
}
