package com.sapient.storm_meetup.singlenodestormtopology;

import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
//import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

/**
 * A topology which can be run on a local storm instance. It sources consumer
 * complaints events from a local kafka broker (both storm and kafka sharing the
 * same local zookeeper instance), calculates simple stats for different
 * complaint types, and stores them in a local MongoDB instance. The name of the
 * topology should be specified as an argument while running it on local storm
 * instance.
 * 
 * @author abhinavg6
 *
 */
public class ConsCompStormTopology {

	public static void main(String[] args) {
		// zookeeper hosts for the Kafka cluster
		ZkHosts zkHosts = new ZkHosts("localhost:2181");

		// Create the KafkaSpout configuration
		// Second argument is the topic name to read from
		// Third argument is the ZooKeeper root path for the spout to store the
		// consumer offsets
		// Fourth argument is consumer group id for storing the consumer offsets
		// in Zookeeper
		SpoutConfig kafkaConfig = new SpoutConfig(zkHosts,
				"test-conscomp-event-topic", "/conscompkafka", "conscomp");

		// Specify that the kafka messages are String
		kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

		// We want to consume all the first messages in
		// the topic every time we run the topology
		kafkaConfig.forceFromStart = true;

		// Now we create the topology
		TopologyBuilder builder = new TopologyBuilder();
		// set the kafka spout class
		builder.setSpout("kafkaspout", new KafkaSpout(kafkaConfig), 1);
		// set the transform bolt class
		builder.setBolt("transformbolt", new ConsCompTransformBolt(), 2)
				.shuffleGrouping("kafkaspout");
		// Set the count bolt class, with tick time as the argument
		builder.setBolt("countbolt", new ConsCompCountBolt(1), 2)
				.fieldsGrouping("transformbolt", new Fields("product"));
		// Set the stat bolt class, with rolling window size as the argument
		builder.setBolt("statbolt", new ConsCompStatBolt(5), 2).fieldsGrouping(
				"countbolt", new Fields("product"));
		// Set the mongo bolt class
		builder.setBolt("mongobolt", new ConsCompMongoBolt(), 1)
				.globalGrouping("statbolt");

		// create an instance of LocalCluster class
		// for executing topology in local mode.
		// LocalCluster cluster = new LocalCluster();

		Config conf = new Config();
		// Set the number of workers for this topology
		conf.setNumWorkers(3);

		/*
		 * // Submit topology for execution
		 * cluster.submitTopology("ConsCompTopology", conf,
		 * builder.createTopology()); try { // Wait for some time before exiting
		 * System.out.println("Waiting to consume from kafka");
		 * Thread.sleep(35000); } catch (Exception exception) {
		 * System.out.println("Thread interrupted exception : " + exception); }
		 * // kill the KafkaTopology cluster.killTopology("ConsCompTopology");
		 * // shut down the storm test cluster cluster.shutdown();
		 */

		try { // This statement submits the topology on local instance of storm.
				// args[0] = name of topology
			StormSubmitter.submitTopology(args[0], conf,
					builder.createTopology());
		} catch (AlreadyAliveException alreadyAliveException) {
			// TODO Replace by log4j
			System.out.println(alreadyAliveException);
		} catch (InvalidTopologyException invalidTopologyException) {
			// TODO Replace by log4j
			System.out.println(invalidTopologyException);
		}

	}
}
