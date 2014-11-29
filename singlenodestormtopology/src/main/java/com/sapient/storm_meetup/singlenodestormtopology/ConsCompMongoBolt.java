package com.sapient.storm_meetup.singlenodestormtopology;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

/**
 * A bolt to store the consumer complaints stats data into a local instance of
 * MongoDB
 * 
 * @author abhinavg6
 *
 */
public class ConsCompMongoBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 6813468953316526163L;

	private MongoClient mongoClient;
	private DB stormMeetupDB;
	private DBCollection consCompStatsColl;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {

		try {
			mongoClient = new MongoClient("localhost", 27017);
			stormMeetupDB = mongoClient.getDB("storm-meetup");
			consCompStatsColl = stormMeetupDB.getCollection("conscompstats");
		} catch (UnknownHostException e) {
			System.out
					.println("###############$$$$ -- Didn't get the connection to MongoDB");
		}

	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		String product = input.getStringByField("product");
		String subproduct = input.getStringByField("subproduct");
		Integer count = input.getIntegerByField("count");
		Double mean = input.getDoubleByField("mean");
		Double stddev = input.getDoubleByField("stddev");
		Double skewness = input.getDoubleByField("skewness");
		Double kurtosis = input.getDoubleByField("kurtosis");

		if (consCompStatsColl != null) {
			// Create the new object to be inserted
			BasicDBObject basicDBObject = new BasicDBObject("product", product)
					.append("subproduct", subproduct)
					.append("count", count)
					.append("rollingstats",
							new BasicDBObject("mean", mean)
									.append("stddev", stddev)
									.append("skewness", skewness)
									.append("kurtosis", kurtosis))
					.append("timestamp", new Date());
			// Add the object to MongoDB collection
			WriteResult writeResult = consCompStatsColl.insert(basicDBObject,
					WriteConcern.UNACKNOWLEDGED);
			// TODO - Replace by log4j
			System.out
					.println("########################$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"
							+ writeResult.getN());
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

}
