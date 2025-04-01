# kafka-queues-demo
Short demo of the Queues for Kafka functionality introduced in Apache Kafka 4.0

## Disclaimer

The code and/or instructions here available are NOT intended for production usage. It's only meant to serve as an example or reference and does not replace the need to follow actual and official documentation of referenced products.

## Setup

Spin up your Kafka cluster by executing 

```bash
docker compose up -d
```

### Check Control Center

Open http://localhost:9021 and check cluster is healthy.

If everything worked well, you have a cluster consisting of three Kafka brokers and Confluent Control Center.

Start by creating the topic in the cluster:

```bash
docker exec kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:19092 --create --topic test-queues --replication-factor 1 --partitions 2
```

This command will create the "test-queues" topic in the cluster having 2 partitions.

## Run Demo

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

Even though the topic has only two partitions, you will see that more than two consumers can consume the topic collaboratively, and each consumer will get records from both partitions.

The consumers process each record every 500 milliseconds, so given that records are produced every 200 milliseconds AND some of the consumed records are acknowledged as RELEASE (see https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=255070434#KIP932:QueuesforKafka-In-flightrecords for full details) you will need around five consumers (or more, depending on the random numbers generated!) to be able to consume all the incoming records. 


