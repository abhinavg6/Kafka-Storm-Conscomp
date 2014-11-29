Kafka-Storm-Conscomp Topology
=============================

A kafka event producer, and a storm topology to process events from kafka and store data to MongoDB. 
* The kafka producer reads a csv file and sends US consumer complaints events to a local kafka broker. 
* The storm topology sources the events from kafka broker, transforms the events for processing, counts events per compliant type-subtype, then calculates rolling statistics per complaint type-subtype, and finally persists the stats to a local MongoDB instance.

For each project, please download latest version of maven to run mvn commands from command-line, or import it as a maven project in your IDE (provided maven plug-in is present). Please run "mvn clean install" and "mvn eclipse:eclipse" if you're running from a command line, and then import the project in your IDE.

Following setup will need to be done on local machine to run these projects:
* Install Zookeeper - [Visit zookeeper](http://zookeeper.apache.org/doc/trunk/zookeeperStarted.html) (For mac users, it's also available via brew)
* Install Kafka - [Visit kafka](http://kafka.apache.org/documentation.html#quickstart) (For mac users, it's also available via brew)
* Install Storm - [Visit storm setup](http://ptgoetz.github.io/blog/2013/12/18/running-apache-storm-on-windows) (For mac users, it's also available via brew)
* Install MongoDB - [Visit mongodb](http://docs.mongodb.org/manual/tutorial/install-mongodb-on-windows/) (For mac users, it's also available via brew)
* Zookeeper config - In zookeeper's conf/zoo.cfg, change the "dataDir" to a directory of your choice. Rest can be left as is (leave clientPort to be 2181).
* Kafka config - In kafka's config/server.properties, keep "broker_id=0", "port=9092", "log.dirs=directory of your choice", "num.partitions=1" and "zookeeper.connect=localhost:2181". Rest can be left as is.
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

* Storm lib add-ons - Following jars need to be in storm's classpath to run this topology (these can be copied from your maven repository to storm's lib directory)

    storm-kafka-<version>-incubating.jar
    commons-math3-<version>.jar
    kafka_<version>.jar
    scala-library-<version>.jar
    mongo-java-driver-<version>.jar
    metrics-core-<version>.jar

* MongoDB config - From Mongo shell, create the database "storm-meetup", and collection "conscompstats"
* Kafka topic creation - Create a kafka topic "test-conscomp-event-topic" or whatever is used in your version of the programs.
* Try starting zookeeper (zkStart.sh start), kafka (kafka-server-start.sh pathto/server.properties), storm nimbus (storm nimbus), storm supervisor (storm supervisor) and MongoDB (mongod).
