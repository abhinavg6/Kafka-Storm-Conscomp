package com.sapient.storm_meetup.singlenodestormtopology;

import org.apache.commons.lang.StringUtils;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * A bolt to transform the incoming kafka messages to valid consumer complaint
 * tuples for further processing. It then emits the transformed tuples for
 * further processing.
 * 
 * @author abhinavg6
 *
 */
public class ConsCompTransformBolt extends BaseBasicBolt {

	private static final long serialVersionUID = -3440545222632913766L;

	public void execute(Tuple input, BasicOutputCollector collector) {
		// Get the message from the tuple
		String message = input.getString(0);

		// Parse the incoming message from kafka
		String[] messageTokens = StringUtils.split(message, ',');
		String state = null;
		String product = null;
		String subproduct = null;
		try {
			for (String messageToken : messageTokens) {
				if (StringUtils.isNotEmpty(messageToken)) {
					int index = messageToken.indexOf("=");
					if (messageToken.startsWith("STATE")) {
						state = messageToken.substring(index + 1);
						if (StringUtils.isEmpty(state)) {
							state = "UNKNOWN";
						}
					} else if (messageToken.startsWith("PRODUCT")) {
						product = messageToken.substring(index + 1);
						if (StringUtils.isEmpty(product)) {
							product = "UNKNOWN";
						}
					} else if (messageToken.startsWith("SUBPRODUCT")) {
						subproduct = messageToken.substring(index + 1);
						if (StringUtils.isEmpty(subproduct)) {
							subproduct = "UNKNOWN";
						}
					}
				}
			}

			// Emit the new tuple with fields
			collector.emit(new Values(state, product, subproduct));

		} catch (Exception e) {
			// TODO Replace by log4j
			System.out.println("Can't parse the message " + message + " :: "
					+ e.getMessage());
			return;
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// emit the tuple with field "state", "product" and "subproduct"
		declarer.declare(new Fields("state", "product", "subproduct"));
	}

}
