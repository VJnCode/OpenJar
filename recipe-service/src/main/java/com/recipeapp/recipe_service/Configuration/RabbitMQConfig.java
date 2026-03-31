package com.recipeapp.recipe_service.Configuration;


import ch.qos.logback.classic.pattern.MessageConverter;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMQConfig {
    public static final String LIKE_QUEUE = "like_queue";
    public static final String EXCHANGE = "openjar_exchange";
    public static final String LIKE_ROUTING_KEY = "like_routing_key";

    @Bean
    public Queue likeQueue() {
        return new Queue(LIKE_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding likeBinding(Queue likeQueue, TopicExchange exchange) {
        return BindingBuilder.bind(likeQueue).to(exchange).with(LIKE_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }
}
