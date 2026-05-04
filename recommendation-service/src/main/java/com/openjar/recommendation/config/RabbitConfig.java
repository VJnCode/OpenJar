package com.openjar.recommendation.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String SYNC_QUEUE = "recipe.sync.queue";

    @Bean
    public Queue recipeSyncQueue() {
        // true = durable (survives RabbitMQ restarts)
        return new Queue(SYNC_QUEUE, true); 
    }
}