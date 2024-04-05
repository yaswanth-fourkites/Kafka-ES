package org.example;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class OpenSearchConsumer {
    public static RestHighLevelClient createOpenSearchClient() {

        String connString = "https://u3f23v9m0c:r3w59cgwyr@kafka-consumer-7373908385.us-east-1.bonsaisearch.net:443";

        // we build a URI from the connection string
        RestHighLevelClient restHighLevelClient;
        URI connUri = URI.create(connString);
        // extract login information if it exists
        String userInfo = connUri.getUserInfo();

        if (userInfo == null) {
            // REST client without security
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));

        } else {
            // REST client with security
            String[] auth = userInfo.split(":");

            CredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                            .setHttpClientConfigCallback(
                                    httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                                            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));
        }

        return restHighLevelClient;
    }

    private static KafkaConsumer<String,String> createKafkaConsumer(){

        String groupId = "open_search-consumer-group";

        // Properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new KafkaConsumer<>(properties);
    }

    public static void main(String[] args) throws IOException {

        Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getSimpleName());

        // Creating a opensearch client
        RestHighLevelClient opensearchClient = createOpenSearchClient();

        // Create Consumer
        KafkaConsumer<String,String>consumer = createKafkaConsumer();

        // Create an index with wikimedia
        try(opensearchClient){

            boolean indexExists = opensearchClient.indices().exists(new GetIndexRequest("wikimedia"),RequestOptions.DEFAULT);
            if(!indexExists){
                opensearchClient.indices().create(new CreateIndexRequest("wikimedia"),RequestOptions.DEFAULT);
                log.info("Index created with name wikimedia");
            }else{
                log.error("Index already exists");
            }
            // Subscribe to the topic
            consumer.subscribe(List.of("wikimedia.recentchange"));

            // Poll the records
            while(true){
                ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(3000));
                log.info("Total Received RecordsCount "+records.count());

                for(ConsumerRecord<String,String>record:records){
                    try{
                        IndexResponse indexResponse = opensearchClient.index(new IndexRequest("wikimedia").source(record.value(), XContentType.JSON),RequestOptions.DEFAULT);
                        log.info("Document Inserted into the OpenSearch, Id:"+String.valueOf(indexResponse.getId()));
                    }catch (Exception e){
                        log.info(e.getMessage());
                    }
                }
            }

        }
        // close
    }
}
