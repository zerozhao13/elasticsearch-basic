package com.phoenix.esdemo.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.config.AbstractReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

/**
 * @Author: zero
 * @Date: 20200628
 * 方法配置了ES Data的响应式客户端，并通过注解启用ReactiveElasticsearchRepositories
 */
@Configuration
@EnableReactiveElasticsearchRepositories
public class ReactiveElasticsearchConfig extends AbstractReactiveElasticsearchConfiguration {
    @Override
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }
}
