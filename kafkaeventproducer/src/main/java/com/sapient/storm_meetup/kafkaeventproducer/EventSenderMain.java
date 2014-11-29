package com.sapient.storm_meetup.kafkaeventproducer;

import java.io.IOException;

/**
 * Main class to trigger the file loading, and event sending process to Kafka.
 * First argument to this program should be a csv file name, and second argument
 * should be the name of the kafka topic.
 * 
 * @author abhinavg6
 *
 */
public class EventSenderMain {

	public static void main(String[] args) throws IOException, Exception {

		KafkaEventProducer producer = new KafkaEventProducer();

		// Init kafka config
		producer.initKafkaConfig();
		// Init file config - Pass the file name as first parameter
		producer.initFileConfig(args[0]);
		// Send file data to Kafka broker - Pass the topic name as second
		// parameter
		System.out.println("Starting event production");
		producer.sendFileDataToKafka(args[1]);
		System.out.println("All events sent");

		producer.cleanup();

	}

}
