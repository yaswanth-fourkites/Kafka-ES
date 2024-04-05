package org.producers;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerWithKeys {
    private static final Logger log = LoggerFactory.getLogger(Producer.class.getSimpleName());
    public static void main(String[] args) {
        log.info("Kafka Producer");

        // create Producer Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","127.0.0.1:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());

        // create Producer
        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);
        String topic = "demo_topic";

        for(int j=0;j<10;j++){
            for(int i=0;i<10;i++){
                String key = "id-"+i;
                String value = "hello-world-"+Integer.toString(i);

                // create Producer Record
                ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic,key,value);
                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if(e==null){
                            System.out.println("Key: " + key + " | " + "Partition: " + metadata.partition());
                        }else {
                            System.out.println("Error while producing "+e.getMessage());
                        }
                    }
                });
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Flush and Close
        producer.flush();
        producer.close();
    }
}
