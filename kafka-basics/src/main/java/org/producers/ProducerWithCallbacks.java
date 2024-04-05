package org.producers;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerWithCallbacks {
    private static final Logger log = LoggerFactory.getLogger(Producer.class.getSimpleName());
    public static void main(String[] args) {
        log.info("Kafka Producer");

        // create Producer Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","127.0.0.1:9092");
//        properties.setProperty("batch.size","400");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());
        properties.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());

        // create Producer
        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        for(int i=0;i<30;i++){
            // create Producer Record
            ProducerRecord<String,String> producerRecord = new ProducerRecord<>("demo_topic","hello-world-"+Integer.toString(i));
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if(e==null){
                        System.out.println(
                                "Received new metadata \n"+
                                        "Topic: " + metadata.topic()+"\n"+
                                        "Partition: " + metadata.partition()+"\n"+
                                        "Offset: " + metadata.offset()+"\n"+
                                        "Timestamp: " + metadata.timestamp()+"\n"
                        );
                    }else {
                        System.out.println("Error while producing "+e.getMessage());
                    }
                }
            });
        }

        // Flush and Close
        producer.flush();
        producer.close();
    }
}
