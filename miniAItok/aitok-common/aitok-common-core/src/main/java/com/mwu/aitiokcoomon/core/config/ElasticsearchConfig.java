package com.mwu.aitiokcoomon.core.config;



import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Bean
    public RestClient restClient() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        if (username != null && !username.isEmpty()) {
            credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            System.out.println("fJ: Elasticsearch");
        } else {
            System.out.println("fJ: Elasticsearch no auth");
        }

        RestClientBuilder builder = RestClient.builder(HttpHost.create(elasticsearchUri))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credsProvider));

        System.out.println("fJ: Elasticsearch client built with URI " + elasticsearchUri);
        return builder.build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        try {
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);
            System.out.println("Elasticsearch : " + username);
            return client;
        } catch (Exception e) {
            throw new IllegalStateException("Elasticsearch  " + e.getMessage(), e);
        }
    }
}
