package io.confluent.csta;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;


//import org.apache.kafka.clients.admin.AlterShareGroupOffsetsResult;
import org.apache.kafka.clients.consumer.AcknowledgeType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaShareConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicIdPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class SharedConsumer {

    private static final String SHARE_GROUP = "queues-demo-share-group";
    private static final int RECORD_PROCESSING_TIME = 300;

    public static void main(String[] args) throws Exception {
        Random random = new Random();

//        Código para forzar que los consumidores consuman el topic desde el principio
//        Properties adminProperties = new Properties();
//        adminProperties.setProperty("bootstrap.servers", "localhost:29092");
//
//        try (AdminClient client = AdminClient.create(adminProperties)) {
//            ConfigEntry entry = new ConfigEntry("share.auto.offset.reset", "earliest");
//            AlterConfigOp op = new AlterConfigOp(entry, AlterConfigOp.OpType.SET);
//
//            Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>(1);
//            configs.put(new ConfigResource(ConfigResource.Type.GROUP, SHARE_GROUP), Arrays.asList(op));
//            try (Admin admin = AdminClient.create(adminProperties)) {
//                admin.incrementalAlterConfigs(configs).all().get();
//            }
//        }

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:29092");
        props.setProperty("group.id", SHARE_GROUP);
        KafkaShareConsumer<String, String> consumer = new KafkaShareConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Arrays.asList("test-queues"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));    // Return a batch of acquired records
            
            System.out.printf("Processing batch of %d records\n", records.count());
            System.out.println("|PARTITION | OFFSET | VALUE | Status   | Delivery count |");
            System.out.println("|-------------------------------------------------------|");

            for (ConsumerRecord<String, String> record : records) {
                int value = Integer.parseInt(record.value());
                int deliveryCount = record.deliveryCount().get();
                int ackType = random.nextInt(10);

                String action = "";

                if (ackType == 0) {
                    Thread.sleep(RECORD_PROCESSING_TIME);
                    consumer.acknowledge(record, AcknowledgeType.REJECT);
                    action = "REJECT ";
                }
                else if (ackType <= 3){
                    Thread.sleep(RECORD_PROCESSING_TIME);
                    consumer.acknowledge(record, AcknowledgeType.RELEASE);
                    action = "RELEASE";
                }
                else {
                    Thread.sleep(RECORD_PROCESSING_TIME);
                    consumer.acknowledge(record, AcknowledgeType.ACCEPT);
                    action = "ACCEPT ";
                }

                System.out.println(String.format("| %01d        | %03d    | %01d     | %s  | %d              |", record.partition(), record.offset(), value, action, deliveryCount));
            }

            Map<TopicIdPartition, Optional<KafkaException>> syncResult = consumer.commitSync();
            System.out.println(syncResult);
            System.out.println("committed\n");
            Thread.sleep(500);
        }
    }
}
