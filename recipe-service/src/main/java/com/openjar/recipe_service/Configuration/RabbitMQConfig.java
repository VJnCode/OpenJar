package com.openjar.recipe_service.Configuration;


//import ch.qos.logback.classic.pattern.MessageConverter;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMQConfig {
    public static final String LIKE_QUEUE = "like_queue";
    public static final String EXCHANGE = "openjar_exchange";
    public static final String LIKE_ROUTING_KEY = "like_routing_key";




    public static final String N_QUEUE = "notification_queue";
    public static final String N_ROUTING_KEY = "notification_routing_key";


    @Bean
    public Queue NOTIFICATIONQueue() {
        return new Queue(N_QUEUE, true);
    }

    @Bean
    public Binding notificationBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(NOTIFICATIONQueue())
                .to(exchange)
                .with( N_ROUTING_KEY);
    }

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
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}