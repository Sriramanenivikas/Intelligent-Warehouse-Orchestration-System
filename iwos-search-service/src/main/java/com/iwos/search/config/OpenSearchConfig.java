package com.iwos.search.config;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.host:localhost}")
    private String host;

    @Value("${opensearch.port:9200}")
    private int port;

    @Bean
    public OpenSearchClient openSearchClient() {
        var transport = ApacheHttpClient5TransportBuilder
                .builder(new HttpHost("http", host, port))
                .build();
        return new OpenSearchClient(transport);
    }
}
