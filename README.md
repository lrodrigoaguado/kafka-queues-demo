# kafka-queues-demo

Short demo of the Queues for Kafka functionality introduced in Apache Kafka 4.0

## Disclaimer

The code and/or instructions here available are NOT intended for production usage. It's only meant to serve as an example or reference and does not replace the need to follow actual and official documentation of referenced products.

## Setup

### Prerequisites

You will need an environment with docker, java and maven to execute the demo.

Spin up your Kafka cluster by executing

```bash
docker compose up -d
```

### Check Control Center

Open http://localhost:9021 and check cluster is healthy.

If everything worked well, you have a cluster consisting of three Kafka brokers and Confluent Control Center.

Start by creating the topic in the cluster:

```bash
docker exec kafka-1 kafka-topics --bootstrap-server localhost:19092 --create --topic test-queues --replication-factor 1 --partitions 2
```

This command will create the "test-queues" topic in the cluster having 2 partitions.

## Run Demo

First of all, move to the project folder and build it with the command

```bash
cd kafka-queue-demo
mvn clean compile package
```

To execute the Producer, run

```bash
mvn exec:java@run-producer
```

It will launch a Producer that publishes records every 200 milliseconds with a random number from 0 to 4 in either the 0 or 1 partition (also decided randomly).

Now open a new terminal and run:

```bash
mvn exec:java@run-consumer
```

and it will launch a Shared Consumer, belonging to the shared group "queues-demo-share-group". You can open as many terminals as you like and execute as many consumers as you want (theoretical limit according to https://cwiki.apache.org/confluence/display/KAFKA/KIP-932%3A+Queues+for+Kafka is 200).

## Considerations

Even though the topic has only two partitions, you will see that more than two consumers can consume the topic collaboratively, and each consumer will get records from both partitions.



In Shared Groups, Consumers have to acknowledge the processing of the messages, returning one of these three states (see [KIP-932: Queues for Kafka - Apache Kafka - Apache Software Foundation](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=255070434#KIP932:QueuesforKafka-In-flightrecords) for full details):

- ACCEPT: the message was correctly processed

- RELEASE: the message could not be processed due to some temporal failure

- REJECT: the message could not be processed, and it will not be possible in the future.

The Consumer in the demo will REJECT 10% of the records, and mark as RELEASE 20% of them, randomly.



The Consumer processes each record every 300 milliseconds, so given that records are produced every 100 milliseconds AND some of the consumed records are acknowledged as RELEASE  you may need around four consumers (or more, depending on the random numbers generated!) to be able to consume all the incoming records.

For legibility purposes, each consumer will get a maximum of 10 records in each batch.
