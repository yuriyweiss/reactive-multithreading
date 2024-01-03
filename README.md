# reactive-multithreading
Study Spring WebFlux + Akka framework + Kafka

Build with Maven.

See doc/processing-schema.drawio for message flow diagram.

Planned training:
* Spring WebFlux client/server negotiation;
* Akka actors cooperative work with Spring beans, without direct integration;
* Akka actors non-blocking flow;
* Akka actors with time-consuming processing (blocking thread-pool);
* Akka + Kafka integration.

# Kafka
## start local Kafka
1. > cd ~/work/apps/kafka_2.12-2.5.0
2. > bin/windows/zookeeper-server-start.bat config/zookeeper.properties
3. > bin/windows/kafka-server-start.bat config/server.properties

## create Kafka topics
1. > bin/windows/kafka-topics.bat --create --topic REACT-REQUEST --bootstrap-server <my kafka server> --config retention.ms=86400000 --partitions 3

# MariaDB
## start server
cd ~/work/apps/MariaDB-10.5/bin
./mariadbd.exe

## client GUI
HeidiSQL
root/<my open password>

CREATE DATABASE `threads-study` /*!40100 COLLATE 'utf8_general_ci' */;
User: threads-study/threads-study

CREATE TABLE `request_data` (
`rquid` VARCHAR(36) NOT NULL COLLATE 'utf8_general_ci',
`status` VARCHAR(50) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED, SENT, PROCESSING, PROCESSED, ERROR' COLLATE 'utf8_general_ci',
`create_date` TIMESTAMP NULL DEFAULT NULL,
`request_date` TIMESTAMP NULL DEFAULT NULL,
`response_date` TIMESTAMP NULL DEFAULT NULL,
`message` VARCHAR(1024) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
`response` VARCHAR(4096) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
PRIMARY KEY (`rquid`) USING BTREE
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

# Processing service
Assuming that:
* Kafka topic has 10 partitions;
* One instance of Processing service application listens to one partition;
* Id of partition is set as java argument on start;
* Two instances of Processing service application can listen to one partition;
* One request is processed for 2 seconds.

We must reach processing service throughput = 300 tps, so we need 30 tps for 1 partition.
To reach 30 tps we need 60 concurrent threads, each waiting for 2 seconds lasting task.

## Investigation result
Spring KafkaListener concurrency uses exactly one working thread per partition.
We must process one message as soon as possible to get maximum throughput.
On extraction of one message we can submit it to thread pool of long running tasks and immediately continue execution.
To implement back-pressure we can add waiting into listener working thread, checking thread pool queue oversizing.
If thread pool of one partition is failing to process a lot of messages, Kafka working thread may wait until queue is drained a little.
If our system is not able to process all messages, we can add partitions to Kafka topic and new thread pools will be added to carry more messages.

## Current implementation
To reach 300 tps throughput for tasks running 1 second, I created topic with 3 partitions.
Listener concurrency is set to 3.
For each partition is created fixed thread pool of 200 threads.
In this case all incoming requests are processed as soon as possible, no waiting on oversized queues needed.

## Concurrent launch of application
You can launch several instances of application at the same time.
Kafka will reassign partitions between applications. Only one consumer will process messages from one partition.
Applications can be launched and stopped in any way. Partitions will be rebalanced each time.
So You can easily launch several PODs. When one POD will crash, other PODs will take it's work.

# Conclusion
For small applications with throughput up to 10000 tps Akka framework is not necessary.
We can use Spring Kafka + ExecutorService with same efficiency, but without complexity.
