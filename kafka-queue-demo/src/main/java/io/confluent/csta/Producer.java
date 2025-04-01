/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.confluent.csta;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class Producer {

    private static final String TOPIC = "test-queues";

    public static void main(String[] args) throws Exception {
        Random random = new Random();

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:29092");

        try(KafkaProducer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer())) {

            while (true) {
                try {
                    int randomNumber = random.nextInt(5); // Generates 0 to 4 (exclusive of 5)
                    int partitionNumber = random.nextInt(2);
                    Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(TOPIC, partitionNumber, null, String.valueOf(randomNumber)));
                    RecordMetadata metadata = future.get(); // Wait for the result (synchronous)
                    System.out.printf("Published record: %d into partition: %d, offset: %d\n", randomNumber, metadata.partition(), metadata.offset());

                    // Optional: Add a delay to slow down the output
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // Handle the exception if the thread is interrupted
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}