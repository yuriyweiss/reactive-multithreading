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
2. > bin/windows/kafka-topics.bat --create --topic REACT-RESPONSE --bootstrap-server <my kafka server> --config retention.ms=86400000 --partitions 3

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
ENGINE=InnoDB
;
