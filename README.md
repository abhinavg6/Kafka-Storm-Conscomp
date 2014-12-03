Kafka-Storm-Conscomp Topology
=============================

A kafka event producer, and a storm topology to process events from kafka and store data to MongoDB. 
* The kafka producer reads a csv file and sends US consumer complaints events to a local kafka broker. 
* The storm topology sources the events from kafka broker, transforms the events for processing, counts events per compliant type-subtype, then calculates rolling statistics per complaint type-subtype, and finally persists the stats to a local MongoDB instance.

For each project, please download latest version of maven to run mvn commands from command-line, or import it as a maven project in your IDE (provided maven plug-in is present). Please run **mvn clean install** and **mvn eclipse:eclipse** if you're running from a command line, and then import the project in your IDE.

Following setup will need to be done on local machine to run these projects:
* Install Zookeeper - [Visit zookeeper](http://zookeeper.apache.org/doc/trunk/zookeeperStarted.html) (For mac users, it's also available via brew)
* Install Kafka - [Visit kafka](http://kafka.apache.org/documentation.html#quickstart) (For mac users, it's also available via brew)
* Install Storm - [Visit storm setup](http://ptgoetz.github.io/blog/2013/12/18/running-apache-storm-on-windows) (For mac users, it's also available via brew)
* Install MongoDB - [Visit mongodb](http://docs.mongodb.org/manual/tutorial/install-mongodb-on-windows/) (For mac users, it's also available via brew)
* Zookeeper config - In zookeeper's conf/zoo.cfg, change the *dataDir* to a directory of your choice. Rest can be left as is (leave *clientPort* to be 2181).
* Kafka config - In kafka's config/server.properties, keep *broker_id=0*, *port=9092*, *log.dirs=directory of your choice*, *num.partitions=1* and *zookeeper.connect=localhost:2181*. Rest can be left as is.
* Storm config - In storm's conf/storm.yaml, following configuration is required (port numbers can be of choice, though should be open for use)

   storm.zookeeper.servers:
      - "127.0.0.1"
   storm.zookeeper.port: 2181
   nimbus.host: "127.0.0.1"
   storm.local.dir: "directory of your choice"
   storm.messaging.transport: backtype.storm.messaging.netty.Context
   supervisor.slots.ports:
      - 7400
      - 7401
      - 7402
      - 7403

* Storm lib add-ons - Following jars need to be in storm's classpath to run this topology (these can be copied from your maven repository to storm's lib directory - provided you've already done the **mvn clean install** for the topology and event producer programs):

*storm-kafka-version-incubating.jar, commons-math3-version.jar, kafka_version.jar, scala-library-version.jar, mongo-java-driver-version.jar, metrics-core-version.jar*

* MongoDB startup - Start Mongo DB instance from command line by running **mongod**. Then from another command-line instance, login to the database using Mongo shell by running **mongo**. 
* MongoDB config - From Mongo shell, create the database *storm-meetup* with command **use storm-meetup**. The topology would log records to a collection *conscompstats* (you don't need to create it). Once the topology has finished processing all events (after all steps below), the collection can be queries by command **db.conscompstats.count()** or **db.conscompstats.find()**.
* Zookeeper startup - From a command line, try starting zookeeper by running **(Zookeeper home directory)/bin/zkStart.sh start**. Check if it's running with a Java process name of *QuorumPeerMain*.
* Kafka startup - From a command line, try starting the Kafka instance by running **(kafka home/directory)/bin/kafka-server-start.sh (kafka home directory)/config/server.properties**. Check if it's running with a Java process name of *kafka*. 
* Kafka topic creation - From a command line, create a topic by running **(kafka home directory)/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 1 --replication-factor 1 --topic test-conscomp-event-topic**. You can test if it's created by running **(kafka home directory)/bin/kafka-topics.sh --zookeeper localhost:2181 --list**.
* Event submission to Kafka - From your IDE in project *kafkaeventproducer*, run the main class **EventSenderMain** by passing arguments *cons_comp_data.csv* and *test-conscomp-event-topic*. It'll submit all records in the CSV file as events to kafka broker.
* Storm startup - From a command line, start the *Nimbus* master dameon by running **(storm home directory)/bin/storm nimbus**. Check if it's running with a Java process name of *nimbus*. Then from another command line instance, start the *Supervisor* slave dameon by running **(storm home directory)/bin/storm supervisor**. Check if it's running with a Java process name of *supervisor*. Then from another command line instance, start the *Storm UI* server by running **(storm home directory)/bin/storm ui**. Check if it's running with a Java process name of *core*.
* Storm topology submission - Once you've done **mvn clean install** and generated the project JAR, submit the topology from command line by running **(storm home directory)/bin/storm jar (project directory)/target/singlenodestormtopology-0.0.1-SNAPSHOT.jar com.sapient.storm_meetup.singlenodestormtopology.ConsCompTopology cons-comp-topology**. Check if any java processes with name *worker* have been started, which would mean that topology has started processing kafka events.
* Storm topology monitoring - In your browser, go to http//localhost:8080/index.html. It should show the topology, and different components in it. Now you can play around with it.
