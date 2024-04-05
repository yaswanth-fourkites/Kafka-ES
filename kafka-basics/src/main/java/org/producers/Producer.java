package org.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Producer {
    private static final Logger log = LoggerFactory.getLogger(Producer.class.getSimpleName());
    public static void main(String[] args) {
        log.info("Kafka Producer");

        // create Producer Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","127.0.0.1:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());
        properties.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());

        // create Producer
        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        // create Producer Record
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>("demo_topic","hello-world");
        producer.send(producerRecord);

        // Flush and Close
        producer.flush();
        producer.close();
    }
}
