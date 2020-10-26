package de.alley.maverick.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private String elasticsearchPort;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient esClient() {
        log.info(String.format("Init elasticsearch client -> %s:%s", elasticsearchHost, elasticsearchPort));
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(elasticsearchHost, Integer.parseInt(elasticsearchPort), "http")));
    }

}