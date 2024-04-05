package org.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class ConsumerGroupWithGracefulShutdown {
    private static final Logger log = LoggerFactory.getLogger(ConsumerGroupWithGracefulShutdown.class.getSimpleName());
    public static void main(String[] args) {
        log.info("Kafka Consumer");

        String groupId = "consumer_group-1";
        String topic="demo_topic";

        // create Consumer Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","127.0.0.1:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer",StringDeserializer.class.getName());
        properties.setProperty("group.id",groupId);
        properties.setProperty("auto.offset.reset","earliest");

        // create Consumers
        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(properties);

        // get the main thread
        final Thread mainthread = Thread.currentThread();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                log.info("detecting shutdown ");
                consumer.wakeup();

                try {
                    mainthread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        // poll the data
        try {
            // subscribe to the topic
            consumer.subscribe(List.of(topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    // Process the received message
                    System.out.printf("Consumer Record: key = %s, value = %s, topic = %s, partition = %d, offset = %d%n",
                            record.key(), record.value(), record.topic(), record.partition(), record.offset());
                }
            }
        }catch(WakeupException e){
            System.out.println(e.getMessage());
        }
        finally {
            consumer.close();
        }
    }
}
